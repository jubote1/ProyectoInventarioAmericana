package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * La escalera de rentabilidad: de la venta al resultado, por tienda.
 *
 * NO CALCULA NADA NUEVO. JUNTA LO QUE YA ESTA
 *
 * Venta e insumos salen del datamart, que los escribe el proceso semanal; las
 * comisiones y los gastos de operacion salen de gasto_semanal; la nomina, los
 * fijos, los servicios y la estructura salen de las tablas de Gerencia. Lo
 * unico que hace esta clase es ponerlos en el mismo orden en que se leen y
 * sacar el porcentaje sobre la venta.
 *
 * EL MES ES LA UNIDAD EXACTA, LA SEMANA ES UN PRORRATEO
 *
 * Venta, insumos, comisiones y gastos de operacion son SEMANALES. Nomina,
 * fijos, servicios y estructura son MENSUALES. El calendario de semanas es el
 * puente: un mes son las semanas que el calendario le asigno.
 *
 * Por eso el mes se puede sumar sin inventar nada, y la semana no: para ver una
 * semana sola hay que repartir el costo mensual entre las semanas del mes. Esa
 * vista existe, pero viene marcada, porque un arriendo no se gasta por semanas
 * iguales y nadie deberia tomar una decision creyendo que si.
 *
 * LO QUE FALTA NO SE MUESTRA COMO CERO
 *
 * Si un mes no tiene nomina cargada, la linea no vale cero: vale "no se sabe".
 * Un cero se suma al resultado y lo deja ver mejor de lo que es, y despues
 * nadie entiende por que el mes siguiente se desplomo. Por eso cada linea trae
 * {@link Linea#hayDato} y el resultado trae {@link Escalera#faltantes}.
 *
 * LA FIRMA DICE SI EL MES ES DE VERDAD
 *
 * La nomina y la estructura se firman con el usuario que las cargo. Cuando esa
 * firma dice SIMULACION, el mes se esta mirando con costos copiados de otro, y
 * la pantalla tiene que decirlo arriba y no en una nota al pie.
 */
public class GerenciaRentabilidadDAO {

	/** Los conceptos de gasto_semanal que son comisiones y no gasto de tienda. */
	private static final String COMISIONES = "16,17,18,20";

	/**
	 * El concepto 1 es "Total domicilios": un CONTEO de pedidos, no pesos. Entra
	 * en la tabla igual que los demas y sumarlo con los gastos le anadiria 429
	 * pesos imaginarios a la semana. Se excluye siempre.
	 */
	private static final int CONCEPTO_CONTEO = 1;

	public static class Linea {
		public String codigo = "";
		public String nombre = "";
		public double valor;
		/** Porcentaje sobre la venta de esa tienda. */
		public double pct;
		/** false cuando no hay dato cargado; la pantalla muestra vacio, no cero. */
		public boolean hayDato = true;
		/** true cuando la linea es un subtotal y no un costo que se resta. */
		public boolean subtotal;
		/** REAL, ESTIMADO o SIMULACION; vacio cuando no aplica. */
		public String origen = "";
	}

	public static class Tienda {
		public int idTienda;
		public String nombre = "";
		public double venta;
		public double meta;
		public double resultado;
		public double resultadoPct;
		public ArrayList<Linea> lineas = new ArrayList<Linea>();
	}

	public static class Escalera {
		public int anio;
		public int mes;
		public int semanas;
		public String desde = "";
		public String hasta = "";
		/** true cuando algun costo del mes viene de la simulacion. */
		public boolean simulado;
		/** Lo que no se pudo calcular, en palabras, para mostrarlo arriba. */
		public ArrayList<String> faltantes = new ArrayList<String>();
		public ArrayList<Tienda> tiendas = new ArrayList<Tienda>();
		public String error = "";
	}

	/** Una semana del calendario, para el selector y para la vista semanal. */
	public static class Semana {
		public int idSemana;
		public int numero;
		public int mes;
		public String desde = "";
		public String hasta = "";
		public boolean conVenta;
	}

	// =======================================================================
	// La escalera de un mes
	// =======================================================================

	/**
	 * El mes completo.
	 *
	 * @param idSemana cuando es mayor que cero se mira esa sola semana, y los
	 *                 costos mensuales se reparten entre las semanas del mes.
	 */
	public static Escalera escalera(final int anio, final int mes, final int idSemana) {
		final Escalera e = new Escalera();
		e.anio = anio;
		e.mes = mes;
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			e.error = "No se pudo conectar a la base";
			return (e);
		}
		try {
			//1. Las semanas del mes. Son el rango sobre el que se suma todo lo
			//   semanal, y el divisor con el que se reparte lo mensual.
			final ArrayList<Semana> delMes = semanasDe(cn, anio, mes);
			if (delMes.isEmpty()) {
				e.error = "El ano " + anio + " no tiene el calendario de semanas generado";
				return (e);
			}
			e.semanas = delMes.size();

			Semana sola = null;
			if (idSemana > 0) {
				for (int i = 0; i < delMes.size(); i++) {
					if (delMes.get(i).idSemana == idSemana) {
						sola = delMes.get(i);
					}
				}
			}
			e.desde = sola != null ? sola.desde : delMes.get(0).desde;
			e.hasta = sola != null ? sola.hasta : delMes.get(delMes.size() - 1).hasta;

			//Lo mensual se divide entre las semanas del mes solo cuando se pide
			//una semana sola. Para el mes completo el divisor es 1.
			final double divisor = sola != null ? delMes.size() : 1;

			final String fechas = sola != null ? "('" + sola.hasta + "')" : cierresDe(delMes);

			//2. Una sola consulta con todo. Son seis fuentes en cuatro esquemas,
			//   pero el servidor es el mismo, asi que no vale la pena traerlas
			//   por separado y cruzarlas en Java.
			final String sql =
				"SELECT t.idtienda, t.nombre,"
				+ " IFNULL(v.venta,0) AS venta, IFNULL(v.meta,0) AS meta,"
				+ " i.insumos, q.saldo AS saldo_inventario, c.comisiones, o.operacion,"
				+ " n.nomina, n.firma AS firma_nomina,"
				+ " f.fijos, s.servicios, s.origen AS origen_servicios"
				+ " FROM pizzaamericana.tienda t"
				//El universo son las tiendas que alguien configuro en Gerencia.
				//Con funcional = 'S' entraria la Bodega, que no es una tienda y
				//sale con 100% de perdida.
				+ " JOIN (SELECT DISTINCT idtienda FROM inventarioamericana.gerencia_gasto_fijo_tienda) u"
				+ "   ON u.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT idtienda, SUM(valor) venta, SUM(meta) meta"
				+ "              FROM datamart.venta_semanal_tienda WHERE fecha IN " + fechas
				+ "             GROUP BY idtienda) v ON v.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT idtienda, SUM(costo_total) insumos"
				+ "              FROM datamart.cierre_inventario_semanal WHERE fecha IN " + fechas
				+ "             GROUP BY idtienda) i ON i.idtienda = t.idtienda"
				//El inventario que queda en tienda es un SALDO, no un costo: es
				//la misma mercancia parada semana tras semana. Sumar las cuatro
				//semanas del mes la contaria cuatro veces. Se toma la del ultimo
				//cierre del periodo, que es lo que de verdad hay parado.
				+ " LEFT JOIN (SELECT idtienda, SUM(costo_sin_consumir) saldo"
				+ "              FROM datamart.cierre_inventario_semanal WHERE fecha = ?"
				+ "             GROUP BY idtienda) q ON q.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT idtienda, SUM(valor_gasto) comisiones"
				+ "              FROM inventarioamericana.gasto_semanal"
				+ "             WHERE fecha IN " + fechas + " AND idgasto_conf IN (" + COMISIONES + ")"
				+ "             GROUP BY idtienda) c ON c.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT g.idtienda, SUM(g.valor_gasto) operacion"
				+ "              FROM inventarioamericana.gasto_semanal g"
				+ "              JOIN inventarioamericana.gasto_configuracion gc"
				+ "                ON gc.idgasto_conf = g.idgasto_conf AND gc.activo = 1"
				+ "             WHERE g.fecha IN " + fechas + " AND g.idgasto_conf <> " + CONCEPTO_CONTEO
				+ "             GROUP BY g.idtienda) o ON o.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT idtienda,"
				+ "                   SUM(sueldo_basico+sueldo_variable+seguridad_social+liquidacion) nomina,"
				+ "                   MAX(usuario) firma"
				+ "              FROM inventarioamericana.gerencia_nomina_tienda"
				+ "             WHERE anio = ? AND mes = ? GROUP BY idtienda) n ON n.idtienda = t.idtienda"
				//El fijo aplica si su vigencia toca el mes. Se compara contra el
				//rango del CALENDARIO y no contra el mes de almanaque, porque el
				//mes de la compania empieza un lunes y puede arrancar en julio 31.
				+ " LEFT JOIN (SELECT idtienda, SUM(valor_mensual) fijos"
				+ "              FROM inventarioamericana.gerencia_gasto_fijo_tienda"
				+ "             WHERE vigencia_desde <= ?"
				+ "               AND (vigencia_hasta IS NULL OR vigencia_hasta >= ?)"
				+ "             GROUP BY idtienda) f ON f.idtienda = t.idtienda"
				+ " LEFT JOIN (SELECT idtienda, SUM(valor) servicios,"
				//Si alguno del mes es estimado, el total del mes es estimado.
				+ "                   MIN(origen) origen"
				+ "              FROM inventarioamericana.gerencia_gasto_servicio"
				+ "             WHERE anio = ? AND mes = ? GROUP BY idtienda) s ON s.idtienda = t.idtienda"
				+ " ORDER BY t.nombre";

			final PreparedStatement ps = cn.prepareStatement(sql);
			int p = 1;
			//El cierre con el que se mira el saldo de inventario: el ultimo del
			//periodo, sea la semana pedida o la ultima del mes.
			ps.setString(p++, sola != null ? sola.hasta : delMes.get(delMes.size() - 1).hasta);
			ps.setInt(p++, anio);
			ps.setInt(p++, mes);
			ps.setString(p++, delMes.get(delMes.size() - 1).hasta);
			ps.setString(p++, delMes.get(0).desde);
			ps.setInt(p++, anio);
			ps.setInt(p++, mes);
			final ResultSet rs = ps.executeQuery();

			final ArrayList<Tienda> crudas = new ArrayList<Tienda>();
			final ArrayList<double[]> costos = new ArrayList<double[]>();
			final ArrayList<String[]> marcas = new ArrayList<String[]>();
			double ventaTotal = 0;
			boolean faltaNomina = false;
			boolean faltaServicio = false;
			boolean faltaInsumo = false;

			while (rs.next()) {
				final Tienda t = new Tienda();
				t.idTienda = rs.getInt("idtienda");
				t.nombre = texto(rs.getString("nombre"));
				t.venta = rs.getDouble("venta");
				t.meta = rs.getDouble("meta");
				ventaTotal += t.venta;

				final double[] c = new double[7];
				final boolean[] hay = new boolean[7];
				c[0] = rs.getDouble("insumos");       hay[0] = !rs.wasNull();
				c[1] = rs.getDouble("saldo_inventario");
				c[2] = rs.getDouble("comisiones");    hay[2] = !rs.wasNull();
				c[3] = rs.getDouble("operacion");     hay[3] = !rs.wasNull();
				c[4] = rs.getDouble("nomina") / divisor;  hay[4] = !rs.wasNull();
				c[5] = rs.getDouble("fijos") / divisor;   hay[5] = !rs.wasNull();
				c[6] = rs.getDouble("servicios") / divisor; hay[6] = !rs.wasNull();

				if (!hay[0]) { faltaInsumo = true; }
				if (!hay[4]) { faltaNomina = true; }
				if (!hay[6]) { faltaServicio = true; }

				final String firmaNomina = texto(rs.getString("firma_nomina"));
				final String origenServicio = texto(rs.getString("origen_servicios"));
				if ("SIMULACION".equals(firmaNomina)) {
					e.simulado = true;
				}

				crudas.add(t);
				costos.add(c);
				marcas.add(new String[] { firmaNomina, origenServicio,
						hay[0] ? "S" : "N", hay[2] ? "S" : "N", hay[3] ? "S" : "N",
						hay[4] ? "S" : "N", hay[5] ? "S" : "N", hay[6] ? "S" : "N" });
			}
			rs.close();
			ps.close();

			//3. La estructura. Es lo unico que no se puede resolver tienda por
			//   tienda: la bolsa es de la compania y se reparte por lo que vende
			//   cada una sobre el total del mes, que es la regla que definio
			//   gerencia. Por eso va despues del recorrido, cuando ya se sabe el
			//   total.
			final double[] bolsa = poolDe(cn, anio, mes);
			final double pool = bolsa[0] / divisor;
			final boolean hayPool = bolsa[1] > 0;
			//La bolsa se marca por si misma y no por la bandera del mes: la
			//nomina puede estar simulada y la estructura ser real, y decir que
			//las dos lo estan haria dudar de un dato que si sirve.
			final boolean poolSimulado = bolsa[2] > 0;
			if (poolSimulado) {
				e.simulado = true;
			}

			for (int k = 0; k < crudas.size(); k++) {
				final Tienda t = crudas.get(k);
				final double[] c = costos.get(k);
				final String[] m = marcas.get(k);
				//Sin venta no hay porcentaje ni reparto posible. Se deja la fila
				//para que se vea que la tienda existe y no vendio, en vez de
				//desaparecerla y que nadie note el hueco.
				final double parte = ventaTotal > 0 ? t.venta / ventaTotal : 0;
				final double estructura = pool * parte;

				t.lineas.add(linea("VENTA", "Venta", t.venta, t.venta, true, true, ""));
				t.lineas.add(linea("INS", "Costo de insumos", c[0], t.venta, "S".equals(m[2]), false, ""));
				final double bruto = t.venta - c[0];
				t.lineas.add(linea("BRUTO", "Margen bruto", bruto, t.venta, true, true, ""));
				t.lineas.add(linea("COM", "Comisiones", c[2], t.venta, "S".equals(m[3]), false, ""));
				t.lineas.add(linea("OPE", "Gastos de operacion", c[3], t.venta, "S".equals(m[4]), false, ""));
				t.lineas.add(linea("NOM", "Nomina de tienda", c[4], t.venta, "S".equals(m[5]), false,
						"SIMULACION".equals(m[0]) ? "SIMULACION" : ""));
				final double contribucion = bruto - c[2] - c[3] - c[4];
				t.lineas.add(linea("CONTR", "Margen de contribucion", contribucion, t.venta, true, true, ""));
				t.lineas.add(linea("FIJ", "Gastos fijos", c[5], t.venta, "S".equals(m[6]), false, ""));
				t.lineas.add(linea("SER", "Servicios publicos", c[6], t.venta, "S".equals(m[7]), false, m[1]));
				final double deTienda = contribucion - c[5] - c[6];
				t.lineas.add(linea("TIENDA", "Resultado de tienda", deTienda, t.venta, true, true, ""));
				t.lineas.add(linea("EST", "Cuota de estructura", estructura, t.venta, hayPool, false,
						poolSimulado ? "SIMULACION" : ""));
				t.resultado = deTienda - estructura;
				t.lineas.add(linea("NETO", "Resultado neto", t.resultado, t.venta, true, true, ""));
				//Informativa y NO se resta: es el inventario que quedo parado en la
				//tienda al cierre, un saldo, no un gasto. Va en la escalera porque
				//un saldo que crece mientras la venta no, es plata quieta; pero si
				//se restara, el resultado quedaria mal dos veces, porque esa
				//mercancia ya se contara como costo cuando se consuma.
				t.lineas.add(linea("VAR", "Inventario en tienda al cierre", c[1], t.venta, true, false, "INFO"));

				t.resultadoPct = t.venta > 0 ? t.resultado * 100 / t.venta : 0;
				e.tiendas.add(t);
			}

			if (faltaInsumo) {
				e.faltantes.add("Hay tiendas sin cierre de inventario en el periodo: el costo de insumos "
						+ "de esas no esta, y el resultado se ve mejor de lo que es.");
			}
			if (faltaNomina) {
				e.faltantes.add("Hay tiendas sin nomina cargada en " + mes + "/" + anio + ".");
			}
			if (faltaServicio) {
				e.faltantes.add("Hay tiendas sin servicios publicos cargados en " + mes + "/" + anio + ".");
			}
			if (!hayPool) {
				e.faltantes.add("No hay bolsas de estructura cargadas en " + mes + "/" + anio
						+ ": la cuota de estructura va en cero.");
			}
		} catch (final Exception ex) {
			e.error = "Error armando la escalera";
			System.out.println("GerenciaRentabilidadDAO.escalera: " + ex.toString());
		} finally {
			cerrar(cn);
		}
		return (e);
	}

	private static Linea linea(final String codigo, final String nombre, final double valor,
			final double venta, final boolean hayDato, final boolean subtotal, final String origen) {
		final Linea l = new Linea();
		l.codigo = codigo;
		l.nombre = nombre;
		l.valor = valor;
		l.pct = venta > 0 ? valor * 100 / venta : 0;
		l.hayDato = hayDato;
		l.subtotal = subtotal;
		l.origen = origen == null ? "" : origen;
		return (l);
	}

	// =======================================================================
	// La tendencia de una tienda
	// =======================================================================

	/**
	 * El resultado semana a semana de una tienda en un ano.
	 *
	 * Sirve para lo que la foto del mes no puede decir: si un numero malo fue
	 * una semana o viene bajando hace dos meses. Los costos mensuales se
	 * reparten entre las semanas de su mes, asi que la linea es una tendencia y
	 * no una medicion exacta de cada semana.
	 */
	public static ArrayList<double[]> tendencia(final int idTienda, final int anio) {
		final ArrayList<double[]> filas = new ArrayList<double[]>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (filas);
		}
		try {
			//El idTienda viene de GerenciaSesion.entero, que ya lo dejo en un int
			//o en cero. Va pegado al texto y no por parametro a proposito: en esta
			//consulta aparece ocho veces, y ocho parametros posicionales que hay
			//que contar en el orden exacto en que el servidor los encuentra es
			//justo donde se cuelan los errores que no dan error, solo cifras
			//equivocadas.
			final String sql =
				"SELECT s.numero, s.mes,"
				//Los fijos se miran contra la fecha de CADA semana, no contra los
				//que estan abiertos hoy: un arriendo que empezo en julio no se
				//puede estar cobrando en las semanas de enero.
				+ " (SELECT IFNULL(SUM(ff.valor_mensual),0)"
				+ "    FROM inventarioamericana.gerencia_gasto_fijo_tienda ff"
				+ "   WHERE ff.idtienda = " + idTienda
				+ "     AND ff.vigencia_desde <= s.fecha_fin"
				+ "     AND (ff.vigencia_hasta IS NULL OR ff.vigencia_hasta >= s.fecha_inicio))"
				+ " / s.semanas_mes AS fijos,"
				+ " IFNULL(v.valor,0) venta, IFNULL(i.costo,0) insumos,"
				+ " IFNULL(c.com,0) comisiones, IFNULL(o.ope,0) operacion,"
				+ " IFNULL(n.nom,0)/s.semanas_mes nomina,"
				+ " IFNULL(sv.ser,0)/s.semanas_mes servicios"
				+ " FROM (SELECT x.*, (SELECT COUNT(*) FROM inventarioamericana.gerencia_semana y"
				+ "                     WHERE y.anio = x.anio AND y.mes = x.mes) semanas_mes"
				+ "         FROM inventarioamericana.gerencia_semana x WHERE x.anio = ?) s"
				+ " LEFT JOIN datamart.venta_semanal_tienda v"
				+ "        ON v.fecha = s.fecha_fin AND v.idtienda = " + idTienda
				+ " LEFT JOIN (SELECT fecha, SUM(costo_total) costo FROM datamart.cierre_inventario_semanal"
				+ "             WHERE idtienda = " + idTienda + " GROUP BY fecha) i ON i.fecha = s.fecha_fin"
				+ " LEFT JOIN (SELECT fecha, SUM(valor_gasto) com FROM inventarioamericana.gasto_semanal"
				+ "             WHERE idtienda = " + idTienda + " AND idgasto_conf IN (" + COMISIONES + ")"
				+ "             GROUP BY fecha) c ON c.fecha = s.fecha_fin"
				+ " LEFT JOIN (SELECT g.fecha, SUM(g.valor_gasto) ope FROM inventarioamericana.gasto_semanal g"
				+ "              JOIN inventarioamericana.gasto_configuracion gc"
				+ "                ON gc.idgasto_conf = g.idgasto_conf AND gc.activo = 1"
				+ "             WHERE g.idtienda = " + idTienda + " AND g.idgasto_conf <> " + CONCEPTO_CONTEO
				+ "             GROUP BY g.fecha) o ON o.fecha = s.fecha_fin"
				+ " LEFT JOIN (SELECT anio, mes,"
				+ "                   SUM(sueldo_basico+sueldo_variable+seguridad_social+liquidacion) nom"
				+ "              FROM inventarioamericana.gerencia_nomina_tienda WHERE idtienda = " + idTienda
				+ "             GROUP BY anio, mes) n ON n.anio = s.anio AND n.mes = s.mes"
				+ " LEFT JOIN (SELECT anio, mes, SUM(valor) ser FROM inventarioamericana.gerencia_gasto_servicio"
				+ "             WHERE idtienda = " + idTienda + " GROUP BY anio, mes) sv"
				+ "        ON sv.anio = s.anio AND sv.mes = s.mes"
				+ " ORDER BY s.numero";
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setInt(1, anio);
			
final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final double venta = rs.getDouble("venta");
				//Una semana sin venta no se grafica: es una semana que todavia no
				//ha pasado o que no se ha cerrado, y pintarla en -100% haria ver
				//un derrumbe donde no hay nada.
				if (venta <= 0) {
					continue;
				}
				final double resultado = venta - rs.getDouble("insumos") - rs.getDouble("comisiones")
						- rs.getDouble("operacion") - rs.getDouble("nomina")
						- rs.getDouble("fijos") - rs.getDouble("servicios");
				filas.add(new double[] { rs.getInt("numero"), rs.getInt("mes"), venta,
						resultado, resultado * 100 / venta });
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO.tendencia: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (filas);
	}

	// =======================================================================
	// El detalle de una linea
	// =======================================================================

	public static class Detalle {
		public String concepto = "";
		public double valor;
		public String nota = "";
	}

	/**
	 * De que esta hecha una cifra.
	 *
	 * Sin esto el tablero solo sirve para asustarse. Con esto sirve para hacer
	 * algo: el 37% de insumos de una tienda son unos insumos concretos, y la
	 * conversacion pasa de "estamos mal" a "la harina".
	 */
	public static ArrayList<Detalle> detalle(final int idTienda, final int anio, final int mes,
			final String linea) {
		final ArrayList<Detalle> filas = new ArrayList<Detalle>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (filas);
		}
		try {
			final ArrayList<Semana> delMes = semanasDe(cn, anio, mes);
			if (delMes.isEmpty()) {
				return (filas);
			}
			final String fechas = cierresDe(delMes);
			String sql = null;

			if ("INS".equals(linea) || "VAR".equals(linea)) {
				//El saldo se mira en UN cierre, el ultimo del mes; el consumo se suma
				//en todos. Si el saldo se sumara quedaria multiplicado por las
				//semanas, porque es la misma mercancia parada.
				final boolean saldo = "VAR".equals(linea);
				final String campo = saldo ? "costo_sin_consumir" : "costo_total";
				final String cuando = saldo
						? " AND c.fecha = '" + delMes.get(delMes.size() - 1).hasta + "'"
						: " AND c.fecha IN " + fechas;
				sql = "SELECT i.nombre_insumo AS concepto, SUM(c." + campo + ") AS valor,"
					+ " CONCAT(ROUND(SUM(" + (saldo ? "c.inventario_final" : "c.consumo") + "),1),"
					+ "        ' ',IFNULL(i.unidad_medida,'')) AS nota"
					+ " FROM datamart.cierre_inventario_semanal c"
					+ " LEFT JOIN inventarioamericana.insumo i ON i.idinsumo = c.idinsumo"
					+ " WHERE c.idtienda = ?" + cuando
					+ " GROUP BY i.nombre_insumo, i.unidad_medida HAVING valor <> 0"
					+ " ORDER BY valor DESC";
			} else if ("COM".equals(linea) || "OPE".equals(linea)) {
				final String cuales = "COM".equals(linea)
						? " AND g.idgasto_conf IN (" + COMISIONES + ")"
						: " AND gc.activo = 1 AND g.idgasto_conf <> " + CONCEPTO_CONTEO;
				sql = "SELECT gc.nombre_gasto AS concepto, SUM(g.valor_gasto) AS valor, '' AS nota"
					+ " FROM inventarioamericana.gasto_semanal g"
					+ " JOIN inventarioamericana.gasto_configuracion gc ON gc.idgasto_conf = g.idgasto_conf"
					+ " WHERE g.idtienda = ? AND g.fecha IN " + fechas + cuales
					+ " GROUP BY gc.nombre_gasto HAVING valor <> 0 ORDER BY valor DESC";
			} else if ("FIJ".equals(linea)) {
				sql = "SELECT c.nombre AS concepto, f.valor_mensual AS valor,"
					+ " CONCAT('rige desde ', f.vigencia_desde) AS nota"
					+ " FROM inventarioamericana.gerencia_gasto_fijo_tienda f"
					+ " JOIN inventarioamericana.gerencia_concepto_gasto c ON c.idconcepto = f.idconcepto"
					+ " WHERE f.idtienda = ? AND f.vigencia_desde <= '" + delMes.get(delMes.size() - 1).hasta
					+ "' AND (f.vigencia_hasta IS NULL OR f.vigencia_hasta >= '" + delMes.get(0).desde + "')"
					+ " ORDER BY f.valor_mensual DESC";
			} else if ("SER".equals(linea)) {
				sql = "SELECT c.nombre AS concepto, s.valor, s.origen AS nota"
					+ " FROM inventarioamericana.gerencia_gasto_servicio s"
					+ " JOIN inventarioamericana.gerencia_concepto_gasto c ON c.idconcepto = s.idconcepto"
					+ " WHERE s.idtienda = ? AND s.anio = " + anio + " AND s.mes = " + mes;
			} else if ("NOM".equals(linea)) {
				sql = "SELECT 'Sueldo basico' AS concepto, sueldo_basico AS valor,"
					+ "        CONCAT(empleados,' empleados') AS nota"
					+ "   FROM inventarioamericana.gerencia_nomina_tienda"
					+ "  WHERE idtienda = ? AND anio = " + anio + " AND mes = " + mes
					+ " UNION ALL SELECT 'Sueldo variable', sueldo_variable, usuario"
					+ "   FROM inventarioamericana.gerencia_nomina_tienda"
					+ "  WHERE idtienda = ? AND anio = " + anio + " AND mes = " + mes
					+ " UNION ALL SELECT 'Seguridad social', seguridad_social, ''"
					+ "   FROM inventarioamericana.gerencia_nomina_tienda"
					+ "  WHERE idtienda = ? AND anio = " + anio + " AND mes = " + mes
					+ " UNION ALL SELECT 'Liquidacion', liquidacion, ''"
					+ "   FROM inventarioamericana.gerencia_nomina_tienda"
					+ "  WHERE idtienda = ? AND anio = " + anio + " AND mes = " + mes;
			} else if ("EST".equals(linea)) {
				sql = "SELECT tipo AS concepto, valor, usuario AS nota"
					+ " FROM inventarioamericana.gerencia_pool_estructura"
					+ " WHERE anio = " + anio + " AND mes = " + mes + " AND ? >= 0 ORDER BY valor DESC";
			}
			if (sql == null) {
				return (filas);
			}

			final PreparedStatement ps = cn.prepareStatement(sql);
			final int cuantos = "NOM".equals(linea) ? 4 : 1;
			for (int k = 1; k <= cuantos; k++) {
				ps.setInt(k, idTienda);
			}
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Detalle d = new Detalle();
				d.concepto = texto(rs.getString("concepto"));
				d.valor = rs.getDouble("valor");
				d.nota = texto(rs.getString("nota"));
				filas.add(d);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO.detalle: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (filas);
	}

	// =======================================================================
	// Apoyo
	// =======================================================================

	/** Las semanas que el calendario le asigno a ese mes. */
	public static ArrayList<Semana> semanasDe(final Connection cn, final int anio, final int mes) {
		final ArrayList<Semana> lista = new ArrayList<Semana>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT s.idsemana, s.numero, s.mes, s.fecha_inicio, s.fecha_fin,"
					+ " EXISTS (SELECT 1 FROM datamart.venta_semanal_tienda v"
					+ "          WHERE v.fecha = s.fecha_fin AND v.valor > 0) AS con_venta"
					+ " FROM inventarioamericana.gerencia_semana s"
					+ " WHERE s.anio = ? AND s.mes = ? ORDER BY s.numero");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Semana s = new Semana();
				s.idSemana = rs.getInt("idsemana");
				s.numero = rs.getInt("numero");
				s.mes = rs.getInt("mes");
				s.desde = texto(rs.getString("fecha_inicio"));
				s.hasta = texto(rs.getString("fecha_fin"));
				s.conVenta = rs.getInt("con_venta") == 1;
				lista.add(s);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO.semanasDe: " + e.toString());
		}
		return (lista);
	}

	/** Las semanas de un mes, abriendo y cerrando conexion. */
	public static ArrayList<Semana> semanasDe(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (new ArrayList<Semana>());
		}
		try {
			return (semanasDe(cn, anio, mes));
		} finally {
			cerrar(cn);
		}
	}

	/** Los meses del ano que ya tienen calendario, para el selector. */
	public static ArrayList<int[]> mesesDe(final int anio) {
		final ArrayList<int[]> lista = new ArrayList<int[]>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (lista);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT s.mes, COUNT(*) AS semanas,"
					+ " SUM(EXISTS (SELECT 1 FROM datamart.venta_semanal_tienda v"
					+ "              WHERE v.fecha = s.fecha_fin AND v.valor > 0)) AS con_venta"
					+ " FROM inventarioamericana.gerencia_semana s"
					+ " WHERE s.anio = ? GROUP BY s.mes ORDER BY s.mes");
			ps.setInt(1, anio);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				lista.add(new int[] { rs.getInt("mes"), rs.getInt("semanas"), rs.getInt("con_venta") });
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO.mesesDe: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	/**
	 * La bolsa de estructura del mes.
	 *
	 * Devuelve {valor, cuantas bolsas, cuantas simuladas}. Las dos ultimas son
	 * lo que deja distinguir "no hay estructura cargada" de "la estructura vale
	 * cero", que no es lo mismo.
	 */
	private static double[] poolDe(final Connection cn, final int anio, final int mes) {
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT IFNULL(SUM(valor),0) AS valor, COUNT(*) AS cuantas,"
					+ " SUM(usuario = 'SIMULACION') AS simuladas"
					+ " FROM inventarioamericana.gerencia_pool_estructura WHERE anio = ? AND mes = ?");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			double[] r = new double[] { 0, 0, 0 };
			if (rs.next()) {
				r = new double[] { rs.getDouble("valor"), rs.getInt("cuantas"), rs.getInt("simuladas") };
			}
			rs.close();
			ps.close();
			return (r);
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO.poolDe: " + e.toString());
			return (new double[] { 0, 0, 0 });
		}
	}

	/**
	 * Los cierres de las semanas, como lista para un IN.
	 *
	 * Van armadas y no por parametro porque el numero de semanas cambia con el
	 * mes. Las fechas salen de la base, no del usuario, y ademas se validan
	 * contra el formato antes de entrar: nada que venga de afuera llega aqui.
	 */
	private static String cierresDe(final ArrayList<Semana> semanas) {
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < semanas.size(); i++) {
			final String f = semanas.get(i).hasta;
			if (!f.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
				continue;
			}
			if (sb.length() > 1) {
				sb.append(',');
			}
			sb.append('\'').append(f).append('\'');
		}
		//Una lista vacia haria un IN () que no compila. Con una fecha imposible
		//el resultado es cero filas, que es lo correcto.
		if (sb.length() == 1) {
			sb.append("'1900-01-01'");
		}
		return (sb.append(')').toString());
	}

	private static String texto(final String s) {
		return (s == null ? "" : s.trim());
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidadDAO cerrando: " + e.toString());
		}
	}
}

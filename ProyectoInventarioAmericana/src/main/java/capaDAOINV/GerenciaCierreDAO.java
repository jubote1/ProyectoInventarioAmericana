package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * El cierre del mes, area por area: lo que hoy es el Excel GASTOS_FIJOS_TIENDAS.
 *
 * Una pestana por area de estructura -ADMINISTRATIVA, LOGISTICA Y PRODUCCION,
 * CONTACT CENTER- y despues una por tienda. Esta clase arma las de estructura;
 * las de tienda tienen su propio camino y entran despues.
 *
 * SE CONSERVAN LOS CONCEPTOS DEL EXCEL, NO SUS CUENTAS
 *
 * El Excel es la base de QUE se mide: nomina, gasolina, arriendo, fumigacion. No
 * es la base de COMO se calcula, porque adentro tiene decisiones que nadie puede
 * explicar: la seguridad social esta tecleada a mano y sale distinta para dos
 * personas en la misma situacion, la cuota administrativa son bloques asignados
 * a dedo, y la hoja de publicidad divide entre 6 habiendo once puntos de venta.
 * Aqui cada numero sale de un hecho: una persona en el maestro de empleados, un
 * valor con vigencia, un porcentaje con fecha.
 *
 * EL MES ES LA UNIDAD; LAS SEMANAS SON UN PRORRATEO
 *
 * Una nomina y un arriendo son mensuales. Cuando se piden semanas sueltas, el
 * valor del mes se reparte entre las semanas que el calendario le asigno y se
 * multiplica por las pedidas. Eso NO es lo que de verdad se gasto esa semana
 * -un arriendo no se paga por cuartos- y por eso el resultado viene marcado con
 * {@link Cierre#prorrateado}, para que la pantalla lo diga y nadie tome una
 * decision creyendo otra cosa.
 *
 * UNA PERSONA SIN SALARIO NO VALE CERO
 *
 * Vale "no se sabe". Un cero se suma a la nomina y la deja ver mas barata de lo
 * que es, y despues nadie entiende por que el mes siguiente se disparo. Por eso
 * la persona aparece igual en la lista, con su aviso, y el area trae la cuenta
 * de cuantas estan asi.
 */
public class GerenciaCierreDAO {

	/** Las areas de estructura, en el orden en que van las pestanas. */
	/**
	 * Las areas de estructura, en el orden de las pestanas del libro.
	 *
	 * PUBLICIDAD no tiene nomina: ninguna persona esta asignada a esa area y en
	 * el Excel esa hoja son tres renglones de gasto. Entra igual porque sus
	 * 9.587.100 al mes se reparten entre las once tiendas, y mientras no tuvo
	 * pestana ese dinero estaba cargado en la base sin verse en ninguna parte.
	 */
	private static final String[][] AREAS = {
		{ "ADMINISTRATIVA", "Administrativa" },
		{ "LOGISTICA",      "Logistica y produccion" },
		{ "CONTACT",        "Contact center" },
		{ "PUBLICIDAD",     "Publicidad" },
	};

	public static class Persona {
		public int id;
		public String nombre = "";
		public String cargo = "";
		public double basico;
		public double variable;
		public double seguridadSocial;
		public double liquidacion;
		public double costo;
		/** Vacio cuando esta completa; con texto cuando le falta algo. */
		public String aviso = "";
	}

	public static class Gasto {
		public int idConcepto;
		public String nombre = "";
		public String tipo = "";
		public double mensual;
		/** Lo que le toca al periodo pedido; igual al mensual si es el mes entero. */
		public double periodo;
	}

	public static class Area {
		public String codigo = "";
		public String nombre = "";
		/** AREA para las de estructura, TIENDA para un punto de venta. */
		public String tipo = "AREA";
		/** Solo cuando tipo es TIENDA. */
		public int idTienda;
		public ArrayList<Persona> personas = new ArrayList<Persona>();
		public double sueldos;
		public double seguridadSocial;
		public double liquidacion;
		public double nomina;
		public ArrayList<Gasto> gastos = new ArrayList<Gasto>();
		public double totalGastos;
		public double total;
		public int sinSalario;
		/** true cuando el area nunca ha tenido gastos: no es un dato que falte. */
		public boolean sinGastosNunca;
		public ArrayList<String> avisos = new ArrayList<String>();
	}

	public static class Cierre {
		public int anio;
		public int mes;
		/** Cuantas semanas tiene el mes segun el calendario de gerencia. */
		public int semanasDelMes;
		/** Cuantas se pidieron. Igual a semanasDelMes cuando es el mes entero. */
		public int semanasPedidas;
		public String desde = "";
		public String hasta = "";
		public boolean prorrateado;
		public double porcentajeSeguridadSocial;
		public double porcentajeLiquidacion;
		public ArrayList<Area> areas = new ArrayList<Area>();
		/** Lo que no pertenece a una sola pestana y hay que decir arriba. */
		public ArrayList<String> avisosGenerales = new ArrayList<String>();
		public String error = "";
	}

	// =======================================================================
	// El cierre
	// =======================================================================

	/**
	 * Las areas de estructura de un periodo.
	 *
	 * @param idSemanaDesde cero para el mes completo
	 * @param idSemanaHasta cero para el mes completo
	 */
	public static Cierre cierre(final int anio, final int mes,
			final int idSemanaDesde, final int idSemanaHasta) {
		final Cierre c = new Cierre();
		c.anio = anio;
		c.mes = mes;
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			c.error = "No se pudo conectar a la base";
			return (c);
		}
		try {
			//1. El periodo. Las semanas del mes son el divisor del prorrateo y el
			//   rango de fechas que se muestra arriba.
			final ArrayList<GerenciaRentabilidadDAO.Semana> delMes =
					GerenciaRentabilidadDAO.semanasDe(cn, anio, mes);
			if (delMes.isEmpty()) {
				c.error = "El ano " + anio + " no tiene el calendario de semanas generado";
				return (c);
			}
			c.semanasDelMes = delMes.size();

			final ArrayList<GerenciaRentabilidadDAO.Semana> pedidas =
					recortar(delMes, idSemanaDesde, idSemanaHasta);
			c.semanasPedidas = pedidas.size();
			c.desde = pedidas.get(0).desde;
			c.hasta = pedidas.get(pedidas.size() - 1).hasta;
			c.prorrateado = c.semanasPedidas < c.semanasDelMes;
			final double factor = c.semanasDelMes > 0
					? (double) c.semanasPedidas / c.semanasDelMes : 1;

			//2. Los dos porcentajes, a la fecha de cierre del periodo. Van por
			//   parametro con vigencia: el dia que cambien, los meses viejos
			//   siguen dando lo que ya se reporto.
			c.porcentajeSeguridadSocial = parametro(cn, "SEGURIDAD_SOCIAL_ESTRUCTURA", c.hasta, 25);
			c.porcentajeLiquidacion = parametro(cn, "LIQUIDACION_ESTRUCTURA", c.hasta, 22);

			//3. Cada area de estructura.
			for (int i = 0; i < AREAS.length; i++) {
				final Area a = new Area();
				a.codigo = AREAS[i][0];
				a.nombre = AREAS[i][1];
				a.tipo = "AREA";
				cargarPersonas(cn, a, c.porcentajeSeguridadSocial, c.porcentajeLiquidacion, factor,
						c.desde, c.hasta);
				cargarNominaDigitada(cn, a, c.anio, c.mes, factor);
				cargarGastos(cn, a, c.hasta, c.desde, factor);
				a.total = a.nomina + a.totalGastos;
				c.areas.add(a);
			}

			//4. Y cada punto de venta, con la misma forma, para que la pantalla
			//   los pinte igual y se puedan comparar.
			cargarTiendas(cn, c, factor);

			//5. Un cargo sin area no pertenece a ninguna pestana y su gente
			//   desaparece de la cuenta. Se avisa una sola vez, arriba.
			final int sinArea = contarSinArea(cn);
			if (sinArea > 0) {
				c.avisosGenerales.add("Hay " + sinArea + " persona(s) activa(s) con un cargo que "
						+ "todavia no tiene area asignada: no estan sumando en ninguna pestana.");
			}
		} catch (final Exception ex) {
			c.error = "Error armando el cierre";
			System.out.println("GerenciaCierreDAO.cierre: " + ex.toString());
		} finally {
			cerrar(cn);
		}
		return (c);
	}

	// =======================================================================
	// La nomina del area
	// =======================================================================

	/**
	 * La nomina digitada del area, cuando el mes la tiene.
	 *
	 * POR QUE NO ALCANZA CON SUMAR LAS PERSONAS
	 *
	 * El total que el libro de gerencia reporta como "NOMINA (n)" de cada area es
	 * la columna SUELDO VARIABLE -lo que de verdad se pago, con recargos y
	 * extras-, no el basico. Comprobado exacto contra las pestanas: logistica
	 * 22.425.000 y contact 26.652.000.
	 *
	 * El variable no esta en ninguna parte del sistema. cargarPersonas hace
	 * variable = basico porque no tiene de donde sacarlo, y eso deja por fuera
	 * entre el 6% y el 33% de la nomina de un area segun el mes.
	 *
	 * Por eso se digita, igual que ya se digitan los servicios y la nomina de las
	 * tiendas. Cuando el mes tiene fila en gerencia_nomina_area, manda esa y la
	 * lista de personas queda como detalle de quien la compone. Cuando no la
	 * tiene, no se toca nada: queda lo calculado, y la pantalla ya avisa que el
	 * variable es igual al basico.
	 *
	 * El factor es el mismo prorrateo de semanas que usa el resto del cierre: un
	 * mes a medias trae la parte que le toca.
	 */
	private static void cargarNominaDigitada(final Connection cn, final Area a, final int anio,
			final int mes, final double factor) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT sueldo_variable, seguridad_social, liquidacion, personas"
					+ "  FROM inventarioamericana.gerencia_nomina_area"
					+ " WHERE area = ? AND anio = ? AND mes = ?");
			ps.setString(1, a.codigo);
			ps.setInt(2, anio);
			ps.setInt(3, mes);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return;
			}
			a.sueldos = rs.getDouble("sueldo_variable") * factor;
			a.seguridadSocial = rs.getDouble("seguridad_social") * factor;
			a.liquidacion = rs.getDouble("liquidacion") * factor;
			a.nomina = a.sueldos + a.seguridadSocial + a.liquidacion;
			//El aviso de "sin salario" era sobre lo calculado, que ya no se esta
			//usando: dejarlo seria mandar a corregir algo que no esta sumando.
			a.sinSalario = 0;
			final int personas = rs.getInt("personas");
			if (personas > 0 && personas != a.personas.size()) {
				a.avisos.add("La nomina digitada del mes cuenta " + personas + " persona(s) y en el "
						+ "maestro de empleados hay " + a.personas.size()
						+ ": la lista de abajo no explica el total completo.");
			}
		} catch (final Exception e) {
			a.avisos.add("No se pudo leer la nomina digitada del area.");
			System.out.println("GerenciaCierreDAO.cargarNominaDigitada " + a.codigo + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
	}

	/**
	 * Las personas del area, con su costo.
	 *
	 * El BASICO sale del maestro de empleados, que es donde lo mantiene gestion
	 * humana. El VARIABLE -lo que de verdad se le pago, con recargos y extras-
	 * todavia no tiene de donde salir, asi que por ahora es igual al basico y
	 * queda dicho en la pantalla. Cuando se defina, se cambia aqui y solo aqui.
	 */
	private static void cargarPersonas(final Connection cn, final Area a,
			final double pctSeguridad, final double pctLiquidacion, final double factor,
			final String desde, final String hasta) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT e.id, e.nombre_largo, t.descripcion AS cargo, e.salario"
					+ "  FROM general.empleado e"
					+ "  JOIN general.tipo_empleado t ON t.idtipoempleado = e.idtipoempleado"
					+ "  JOIN inventarioamericana.gerencia_nomina_cargo c"
					+ "    ON c.idtipoempleado = e.idtipoempleado"
					//La excepcion por persona manda sobre el area del cargo. El area
					//sale del CARGO, y quien se movio a la oficina sin que se lo
					//cambiaran queda contado en la tienda: es el caso de Vanessa
					//Arroyave -cargo Artesano- y Mayra Mazo -Administrador Punto de
					//Venta-, que hoy apoyan administrativa y asi las cuenta el Excel.
					//
					//La marca NO va en empleado.idtipoempleado2, que era lo primero
					//que se penso: ese campo ya tiene duenio. UsuarioDAO lo usa para
					//mandarle al POS el rol administrativo de quien apoya como
					//administrador del punto -Vanessa tiene ahi el rol 1 y de ahi
					//salen sus permisos-, EmpleadoEventoDAO cuenta pizzeros por el y
					//DesempenoDomiciliarioDAO mira los dos roles. Pisarlo la dejaria
					//con permisos de artesana.
					+ "  LEFT JOIN inventarioamericana.gerencia_persona_area x"
					+ "    ON x.id = e.id"
					//es_empleado deja por fuera las cuentas que no son personas. No
					//alcanza: el usuario tecnico SuperUsuario esta marcado como
					//empleado y no se le puede apagar, porque en el POS un
					//es_empleado = 0 significa "es temporal" y le tumbaria el
					//ingreso. Se excluye con area NINGUNA en la tabla de excepciones.
					+ " WHERE e.activo = 1 AND e.es_empleado = 1"
					+ "   AND IFNULL(x.area, c.area) = ?"
					+ " ORDER BY e.nombre_largo");
			ps.setString(1, a.codigo);
			rs = ps.executeQuery();
			while (rs.next()) {
				final Persona p = new Persona();
				p.id = rs.getInt("id");
				p.nombre = texto(rs.getString("nombre_largo"));
				p.cargo = texto(rs.getString("cargo"));
				p.basico = rs.getDouble("salario");
				if (p.basico <= 0) {
					//Entra a la lista para que se vea que existe, pero sin sumar:
					//un cero inventado abarata la nomina y nadie lo nota.
					p.aviso = "Sin salario en el maestro de empleados";
					a.sinSalario++;
					a.personas.add(p);
					continue;
				}
				//EL VARIABLE SALE DE LA BIOMETRIA, NO ES UNA COPIA DEL BASICO
				//
				//Durante las primeras entregas esto hacia p.variable = p.basico
				//para salir del paso. Era una mentira comoda: el Excel tiene el
				//variable entre 6% y 33% por encima del basico, asi que la
				//nomina salia corta y nadie podia explicar por que.
				//
				//El variable es basico + recargos, y los recargos los calcula
				//GerenciaNominaCalculoDAO contra las marcaciones, con lo que es
				//de ley: nocturno desde las 19:00, dominical, extras, auxilio de
				//transporte. Aqui solo se lee lo que ese motor dejo calculado
				//para la semana.
				//
				//Cuando la semana no tiene calculo, el variable vuelve a ser el
				//basico y la persona queda con su aviso: es preferible que se
				//vea corto y marcado a que se vea completo y falso.
				final double[] recargos = recargosDe(cn, p.id, desde, hasta);
				p.variable = p.basico + recargos[0];
				if (recargos[1] == 0) {
					p.aviso = (p.aviso.length() > 0 ? p.aviso + " " : "")
							+ "Sin calculo de recargos en el periodo: va el basico.";
				}
				p.seguridadSocial = p.variable * pctSeguridad / 100.0;
				p.liquidacion = p.variable * pctLiquidacion / 100.0;
				p.costo = p.variable + p.seguridadSocial + p.liquidacion;

				a.sueldos += p.variable;
				a.seguridadSocial += p.seguridadSocial;
				a.liquidacion += p.liquidacion;
				a.personas.add(p);
			}
			a.nomina = (a.sueldos + a.seguridadSocial + a.liquidacion) * factor;
			a.sueldos = a.sueldos * factor;
			a.seguridadSocial = a.seguridadSocial * factor;
			a.liquidacion = a.liquidacion * factor;
			if (a.sinSalario > 0) {
				a.avisos.add(a.sinSalario + " persona(s) sin salario cargado: su costo no esta sumando.");
			}
			//Publicidad no tiene gente por diseno -en el libro esa hoja son tres
			//renglones de gasto- asi que avisar que le faltan personas seria
			//mandar a buscar un dato que no existe.
			if (a.personas.isEmpty() && !"PUBLICIDAD".equals(a.codigo)) {
				a.avisos.add("No hay personas activas con un cargo asignado a esta area.");
			}
		} catch (final Exception e) {
			a.avisos.add("No se pudo leer la nomina del area.");
			System.out.println("GerenciaCierreDAO.cargarPersonas " + a.codigo + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
	}

	// =======================================================================
	// Los puntos de venta
	// =======================================================================

	/**
	 * Una pestana por punto de venta, con la misma forma que las de estructura.
	 *
	 * A QUE TIENDA PERTENECE CADA PERSONA: LO DICE LA BIOMETRIA
	 *
	 * El maestro de empleados NO tiene tienda. Y la mitad de la gente de punto
	 * de venta trabaja en mas de una: de los 88 con marcas en julio de 2026, 44
	 * marcaron en dos o mas, y uno en seis.
	 *
	 * La regla que definio gerencia es: cada quien cuenta donde MAS estuvo. Se
	 * toma la tienda con mas marcas en el periodo, y con empate gana la de la
	 * marca mas reciente. Verificado contra el Excel de julio: Manrique da 10
	 * personas, Itagui 9 y Pilarica 8, los mismos del Excel.
	 *
	 * Es una aproximacion a proposito -la persona se carga entera a una sola
	 * tienda, no repartida por horas- porque es lo que pidio gerencia para
	 * arrancar. El reparto proporcional ya existe en GerenciaNominaEmpleadoDAO
	 * y entra despues.
	 *
	 * QUIEN NO MARCO NO APARECE
	 *
	 * Sin marcas no hay como saber donde estuvo, y repartirlo a dedo o mandarlo
	 * a la primera tienda seria inventar. Esas personas se cuentan aparte y se
	 * avisan, para que se vea cuanta nomina quedo sin asignar.
	 */
	private static void cargarTiendas(final Connection cn, final Cierre c, final double factor) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT t.idtienda, t.nombre AS tienda, e.id, e.nombre_largo, e.salario,"
					+ "      tp.descripcion AS cargo"
					+ "  FROM (SELECT ev.id, ev.idtienda,"
					+ "               ROW_NUMBER() OVER (PARTITION BY ev.id"
					+ "                   ORDER BY COUNT(*) DESC, MAX(ev.fecha) DESC) rn"
					+ "          FROM general.empleado_evento ev"
					+ "         WHERE ev.fecha BETWEEN ? AND ?"
					+ "         GROUP BY ev.id, ev.idtienda) x"
					+ "  JOIN general.empleado e ON e.id = x.id"
					+ "  JOIN general.tipo_empleado tp ON tp.idtipoempleado = e.idtipoempleado"
					+ "  JOIN inventarioamericana.gerencia_nomina_cargo g"
					+ "    ON g.idtipoempleado = e.idtipoempleado"
					//La misma excepcion por persona que en las areas. Sin esto,
					//quien se paso a la oficina quedaria contado DOS veces: en su
					//area y en la tienda donde todavia marca.
					+ "  LEFT JOIN inventarioamericana.gerencia_persona_area px"
					+ "    ON px.id = e.id"
					+ "  JOIN pizzaamericana.tienda t ON t.idtienda = x.idtienda"
					+ " WHERE x.rn = 1 AND e.activo = 1 AND e.es_empleado = 1"
					+ "   AND IFNULL(px.area, g.area) = 'TIENDA'"
					+ " ORDER BY t.nombre, e.nombre_largo");
			ps.setString(1, c.desde);
			ps.setString(2, c.hasta);
			rs = ps.executeQuery();

			final java.util.LinkedHashMap<Integer, Area> porTienda =
					new java.util.LinkedHashMap<Integer, Area>();
			while (rs.next()) {
				final int idTienda = rs.getInt("idtienda");
				Area a = porTienda.get(Integer.valueOf(idTienda));
				if (a == null) {
					a = new Area();
					a.tipo = "TIENDA";
					a.idTienda = idTienda;
					a.codigo = "T" + idTienda;
					a.nombre = texto(rs.getString("tienda"));
					porTienda.put(Integer.valueOf(idTienda), a);
				}
				final Persona p = new Persona();
				p.id = rs.getInt("id");
				p.nombre = texto(rs.getString("nombre_largo"));
				p.cargo = texto(rs.getString("cargo"));
				p.basico = rs.getDouble("salario");
				if (p.basico <= 0) {
					p.aviso = "Sin salario en el maestro de empleados";
					a.sinSalario++;
					a.personas.add(p);
					continue;
				}
				//Igual que en las areas: el variable es basico + recargos, y los
				//recargos los calcula el motor contra la biometria. En punto de
				//venta pesan mas que en oficina -ahi es donde estan los turnos
				//nocturnos y los domingos- asi que copiar el basico dejaba la
				//nomina de tienda mucho mas corta de lo que de verdad cuesta.
				final double[] recargos = recargosDe(cn, p.id, c.desde, c.hasta);
				p.variable = p.basico + recargos[0];
				if (recargos[1] == 0) {
					p.aviso = "Sin calculo de recargos en el periodo: va el basico.";
				}
				p.seguridadSocial = p.variable * c.porcentajeSeguridadSocial / 100.0;
				p.liquidacion = p.variable * c.porcentajeLiquidacion / 100.0;
				p.costo = p.variable + p.seguridadSocial + p.liquidacion;
				a.sueldos += p.variable;
				a.seguridadSocial += p.seguridadSocial;
				a.liquidacion += p.liquidacion;
				a.personas.add(p);
			}
			rs.close();
			ps.close();
			rs = null;
			ps = null;

			//Las tiendas que operan pero no quedaron en el recorrido entran
			//igual, vacias: una pestana que falta se lee como que la tienda no
			//existe, y lo que pasa es que nadie marco.
			for (final int[] fila : tiendasQueOperan(cn)) {
				if (!porTienda.containsKey(Integer.valueOf(fila[0]))) {
					final Area a = new Area();
					a.tipo = "TIENDA";
					a.idTienda = fila[0];
					a.codigo = "T" + fila[0];
					a.nombre = nombreTienda(cn, fila[0]);
					a.avisos.add("Ninguna persona marco biometria en esta tienda en el periodo.");
					porTienda.put(Integer.valueOf(fila[0]), a);
				}
			}

			for (final Area a : porTienda.values()) {
				a.nomina = (a.sueldos + a.seguridadSocial + a.liquidacion) * factor;
				a.sueldos = a.sueldos * factor;
				a.seguridadSocial = a.seguridadSocial * factor;
				a.liquidacion = a.liquidacion * factor;
				if (a.sinSalario > 0) {
					a.avisos.add(a.sinSalario + " persona(s) sin salario cargado: su costo no "
							+ "esta sumando.");
				}
				cargarGastosTienda(cn, a, c.hasta, c.desde, c.anio, c.mes, factor);
				a.total = a.nomina + a.totalGastos;
				c.areas.add(a);
			}

			final int sinMarcar = contarSinMarcar(cn, c.desde, c.hasta);
			if (sinMarcar > 0) {
				c.avisosGenerales.add(sinMarcar + " persona(s) de punto de venta no marcaron "
						+ "biometria en el periodo: no se sabe en que tienda estuvieron y su nomina "
						+ "no esta sumando en ninguna pestana.");
			}
		} catch (final Exception e) {
			c.avisosGenerales.add("No se pudieron armar las pestanas de los puntos de venta.");
			System.out.println("GerenciaCierreDAO.cargarTiendas: " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
	}

	/**
	 * Los gastos de un punto de venta: los fijos y los servicios publicos.
	 *
	 * EL VALOR GENERAL SE HEREDA
	 *
	 * Un gasto con idtienda = 0 rige en toda tienda que no tenga el suyo. Por
	 * eso se traen los dos y gana el propio. Sin esto, una tienda que hereda la
	 * alarma apareceria sin alarma.
	 */
	private static void cargarGastosTienda(final Connection cn, final Area a, final String hasta,
			final String desde, final int anio, final int mes, final double factor) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT c.idconcepto, c.nombre, c.tipo, c.orden,"
					//El propio manda sobre el general; MAX con el CASE escoge
					//el propio cuando existe sin necesidad de dos consultas.
					+ "      MAX(CASE WHEN g.idtienda = ? THEN g.valor_mensual END) AS propio,"
					+ "      MAX(CASE WHEN g.idtienda = 0 THEN g.valor_mensual END) AS general"
					+ "  FROM inventarioamericana.gerencia_gasto_fijo_tienda g"
					+ "  JOIN inventarioamericana.gerencia_concepto_gasto c"
					+ "    ON c.idconcepto = g.idconcepto AND c.activo = 'S'"
					+ " WHERE g.idtienda IN (?, 0)"
					+ "   AND g.vigencia_desde <= ?"
					+ "   AND (g.vigencia_hasta IS NULL OR g.vigencia_hasta >= ?)"
					+ " GROUP BY c.idconcepto, c.nombre, c.tipo, c.orden"
					+ " ORDER BY c.orden");
			ps.setInt(1, a.idTienda);
			ps.setInt(2, a.idTienda);
			ps.setString(3, hasta);
			ps.setString(4, desde);
			rs = ps.executeQuery();
			while (rs.next()) {
				final double propio = rs.getDouble("propio");
				final boolean hayPropio = !rs.wasNull();
				final double general = rs.getDouble("general");
				final boolean hayGeneral = !rs.wasNull();
				if (!hayPropio && !hayGeneral) {
					continue;
				}
				final Gasto g = new Gasto();
				g.idConcepto = rs.getInt("idconcepto");
				g.nombre = texto(rs.getString("nombre"))
						+ (hayPropio ? "" : " (general)");
				g.tipo = texto(rs.getString("tipo"));
				g.mensual = hayPropio ? propio : general;
				g.periodo = g.mensual * factor;
				a.totalGastos += g.periodo;
				a.gastos.add(g);
			}
			rs.close();
			ps.close();

			//Los servicios publicos van por mes y no por vigencia: la factura
			//llega despues y puede venir estimada. El origen viaja en el nombre
			//para que no se lea como un valor firme.
			ps = cn.prepareStatement(
					"SELECT c.nombre, s.valor, s.origen"
					+ "  FROM inventarioamericana.gerencia_gasto_servicio s"
					+ "  JOIN inventarioamericana.gerencia_concepto_gasto c"
					+ "    ON c.idconcepto = s.idconcepto"
					+ " WHERE s.idtienda = ? AND s.anio = ? AND s.mes = ?"
					+ " ORDER BY c.orden");
			ps.setInt(1, a.idTienda);
			ps.setInt(2, anio);
			ps.setInt(3, mes);
			rs = ps.executeQuery();
			while (rs.next()) {
				final Gasto g = new Gasto();
				g.nombre = texto(rs.getString("nombre"))
						+ ("ESTIMADO".equals(rs.getString("origen")) ? " (estimado)" : "");
				g.tipo = "SERVICIO";
				g.mensual = rs.getDouble("valor");
				g.periodo = g.mensual * factor;
				a.totalGastos += g.periodo;
				a.gastos.add(g);
			}
		} catch (final Exception e) {
			a.avisos.add("No se pudieron leer los gastos de la tienda.");
			System.out.println("GerenciaCierreDAO.cargarGastosTienda " + a.nombre + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
	}

	/** Las tiendas con host de base de datos: las que de verdad operan. */
	private static ArrayList<int[]> tiendasQueOperan(final Connection cn) {
		final ArrayList<int[]> lista = new ArrayList<int[]>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT idtienda FROM pizzaamericana.tienda"
					+ " WHERE hosbd IS NOT NULL AND hosbd <> '' AND idtienda <> 12"
					+ " ORDER BY nombre");
			rs = ps.executeQuery();
			while (rs.next()) {
				lista.add(new int[] { rs.getInt(1) });
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.tiendasQueOperan: " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (lista);
	}

	private static String nombreTienda(final Connection cn, final int idTienda) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String nombre = "Tienda " + idTienda;
		try {
			ps = cn.prepareStatement("SELECT nombre FROM pizzaamericana.tienda WHERE idtienda = ?");
			ps.setInt(1, idTienda);
			rs = ps.executeQuery();
			if (rs.next()) {
				nombre = texto(rs.getString(1));
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.nombreTienda: " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (nombre);
	}

	/** Gente de punto de venta activa que no marco en el periodo. */
	private static int contarSinMarcar(final Connection cn, final String desde, final String hasta) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int cuantas = 0;
		try {
			ps = cn.prepareStatement(
					"SELECT COUNT(*) FROM general.empleado e"
					+ "  JOIN inventarioamericana.gerencia_nomina_cargo g"
					+ "    ON g.idtipoempleado = e.idtipoempleado"
					+ "  LEFT JOIN inventarioamericana.gerencia_persona_area px"
					+ "    ON px.id = e.id"
					+ " WHERE e.activo = 1 AND e.es_empleado = 1"
					+ "   AND IFNULL(px.area, g.area) = 'TIENDA'"
					+ "   AND NOT EXISTS (SELECT 1 FROM general.empleado_evento ev"
					+ "                    WHERE ev.id = e.id AND ev.fecha BETWEEN ? AND ?)");
			ps.setString(1, desde);
			ps.setString(2, hasta);
			rs = ps.executeQuery();
			if (rs.next()) {
				cuantas = rs.getInt(1);
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.contarSinMarcar: " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (cuantas);
	}

	// =======================================================================
	// Los gastos del area
	// =======================================================================

	/**
	 * Los gastos fijos del area vigentes en el periodo.
	 *
	 * La vigencia se compara contra el rango del CALENDARIO y no contra el mes
	 * de almanaque, porque el mes de la compania empieza un lunes y puede
	 * arrancar en el 29 del mes anterior.
	 */
	private static void cargarGastos(final Connection cn, final Area a,
			final String hasta, final String desde, final double factor) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = cn.prepareStatement(
					"SELECT g.idconcepto, c.nombre, c.tipo, SUM(g.valor_mensual) AS valor"
					+ "  FROM inventarioamericana.gerencia_gasto_area g"
					+ "  JOIN inventarioamericana.gerencia_concepto_gasto c"
					+ "    ON c.idconcepto = g.idconcepto"
					+ " WHERE g.area = ? AND c.activo = 'S'"
					+ "   AND g.vigencia_desde <= ?"
					+ "   AND (g.vigencia_hasta IS NULL OR g.vigencia_hasta >= ?)"
					+ " GROUP BY g.idconcepto, c.nombre, c.tipo, c.orden"
					+ " ORDER BY c.orden");
			ps.setString(1, a.codigo);
			ps.setString(2, hasta);
			ps.setString(3, desde);
			rs = ps.executeQuery();
			while (rs.next()) {
				final Gasto g = new Gasto();
				g.idConcepto = rs.getInt("idconcepto");
				g.nombre = texto(rs.getString("nombre"));
				g.tipo = texto(rs.getString("tipo"));
				g.mensual = rs.getDouble("valor");
				g.periodo = g.mensual * factor;
				a.totalGastos += g.periodo;
				a.gastos.add(g);
			}
			//Un area sin gastos en el periodo puede ser dos cosas muy distintas, y
			//decirlas igual confunde: contact center y logistica NO manejan
			//gastos -en el Excel esas hojas solo traen nomina- mientras que un
			//area que si los maneja y aparece vacia es un dato que falta cargar.
			if (a.gastos.isEmpty()) {
				a.sinGastosNunca = !tuvoGastosAlgunaVez(cn, a.codigo);
				if (!a.sinGastosNunca) {
					a.avisos.add("Esta area tiene gastos configurados, pero ninguno vigente en el "
							+ "periodo consultado.");
				}
			}
		} catch (final Exception e) {
			a.avisos.add("No se pudieron leer los gastos del area.");
			System.out.println("GerenciaCierreDAO.cargarGastos " + a.codigo + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
	}

	// =======================================================================
	// Apoyo
	// =======================================================================

	/** Las semanas pedidas, o todas las del mes cuando no se pidio ninguna. */
	private static ArrayList<GerenciaRentabilidadDAO.Semana> recortar(
			final ArrayList<GerenciaRentabilidadDAO.Semana> delMes,
			final int desde, final int hasta) {
		if (desde <= 0 && hasta <= 0) {
			return (delMes);
		}
		final int a = desde > 0 ? desde : hasta;
		final int b = hasta > 0 ? hasta : desde;
		final int menor = Math.min(a, b);
		final int mayor = Math.max(a, b);
		final ArrayList<GerenciaRentabilidadDAO.Semana> dentro =
				new ArrayList<GerenciaRentabilidadDAO.Semana>();
		for (int i = 0; i < delMes.size(); i++) {
			final int id = delMes.get(i).idSemana;
			if (id >= menor && id <= mayor) {
				dentro.add(delMes.get(i));
			}
		}
		//Si lo que pidieron no cae en el mes, vale mas mostrar el mes entero que
		//una pantalla vacia sin explicacion.
		return (dentro.isEmpty() ? delMes : dentro);
	}

	/** Un parametro de nomina a una fecha, o el valor de respaldo si no esta. */
	private static double parametro(final Connection cn, final String codigo,
			final String fecha, final double siNo) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		double valor = siNo;
		try {
			ps = cn.prepareStatement(
					"SELECT valor FROM inventarioamericana.gerencia_nomina_parametro"
					+ " WHERE codigo = ? AND vigencia_desde <= ?"
					+ "   AND (vigencia_hasta IS NULL OR vigencia_hasta >= ?)"
					+ " ORDER BY vigencia_desde DESC LIMIT 1");
			ps.setString(1, codigo);
			ps.setString(2, fecha);
			ps.setString(3, fecha);
			rs = ps.executeQuery();
			if (rs.next()) {
				valor = rs.getDouble(1);
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.parametro " + codigo + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (valor);
	}

	/** Personas activas cuyo cargo todavia no tiene area. */
	private static int contarSinArea(final Connection cn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int cuantas = 0;
		try {
			ps = cn.prepareStatement(
					"SELECT COUNT(*) FROM general.empleado e"
					+ "  LEFT JOIN inventarioamericana.gerencia_nomina_cargo c"
					+ "    ON c.idtipoempleado = e.idtipoempleado"
					//Quien tiene excepcion por persona YA tiene area, aunque su
					//cargo no la tenga: no hay nada que avisar de el.
					+ "  LEFT JOIN inventarioamericana.gerencia_persona_area x"
					+ "    ON x.id = e.id"
					+ " WHERE e.activo = 1 AND e.es_empleado = 1 AND x.area IS NULL"
					+ "   AND (c.area IS NULL OR c.area = 'SIN DEFINIR')");
			rs = ps.executeQuery();
			if (rs.next()) {
				cuantas = rs.getInt(1);
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.contarSinArea: " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (cuantas);
	}

	/**
	 * Si el area ha tenido gastos configurados alguna vez, con o sin vigencia.
	 *
	 * Es lo que separa "esta area no maneja gastos" de "falta cargarlos". Sin
	 * esto las dos se ven como un cero y la pantalla no puede decir cual es.
	 */
	private static boolean tuvoGastosAlgunaVez(final Connection cn, final String area) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean tuvo = false;
		try {
			ps = cn.prepareStatement(
					"SELECT 1 FROM inventarioamericana.gerencia_gasto_area WHERE area = ? LIMIT 1");
			ps.setString(1, area);
			rs = ps.executeQuery();
			tuvo = rs.next();
		} catch (final Exception e) {
			System.out.println("GerenciaCierreDAO.tuvoGastosAlgunaVez " + area + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (tuvo);
	}

	/**
	 * Los recargos que el motor de nomina ya calculo para esa persona.
	 *
	 * Lee gerencia_nomina_estimado, que llena GerenciaNominaCalculoDAO contra
	 * las marcaciones. Ese motor es el que sabe de ley: nocturno desde las
	 * 19:00 por la Ley 2466 de 2025, dominical, extras diurnas y nocturnas.
	 * Aqui NO se recalcula nada; duplicar esa logica garantizaria que las dos
	 * pantallas terminaran diciendo cifras distintas del mismo mes.
	 *
	 * Las semanas del periodo se suman, porque el cierre es mensual y el motor
	 * trabaja por semana.
	 *
	 * @return [recargos en pesos, cuantas semanas tenian calculo]
	 */
	private static double[] recargosDe(final Connection cn, final int idEmpleado,
			final String desde, final String hasta) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		final double[] r = new double[] { 0, 0 };
		try {
			ps = cn.prepareStatement(
					"SELECT IFNULL(SUM(sueldo_variable), 0), COUNT(*)"
					+ "  FROM inventarioamericana.gerencia_nomina_estimado"
					+ " WHERE idempleado = ? AND semana BETWEEN ? AND ?");
			ps.setInt(1, idEmpleado);
			ps.setString(2, desde);
			ps.setString(3, hasta);
			rs = ps.executeQuery();
			if (rs.next()) {
				r[0] = rs.getDouble(1);
				r[1] = rs.getInt(2);
			}
		} catch (final Exception e) {
			//Sin recargos la persona queda con su basico y su aviso. Tumbar el
			//cierre entero por esto seria peor.
			System.out.println("GerenciaCierreDAO.recargosDe " + idEmpleado + ": " + e.toString());
		} finally {
			cerrar(rs);
			cerrar(ps);
		}
		return (r);
	}

	private static String texto(final String valor) {
		return (valor == null ? "" : valor.trim());
	}

	private static void cerrar(final ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (final Exception e) {
			//Cerrar no es parte del resultado.
		}
	}

	private static void cerrar(final PreparedStatement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
		} catch (final Exception e) {
			//Cerrar no es parte del resultado.
		}
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			//Cerrar no es parte del resultado.
		}
	}
}

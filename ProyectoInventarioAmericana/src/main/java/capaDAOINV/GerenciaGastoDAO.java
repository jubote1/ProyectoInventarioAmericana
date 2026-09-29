package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * Los gastos parametrizables del menu Gerencia.
 *
 * TRES NATURALEZAS, TRES TRATAMIENTOS
 *
 * FIJO      Arriendo, alarma, seguro, contador. No cambia mes a mes. Se guarda
 *           CON VIGENCIA: cuando sube, no se sobrescribe el valor, se le cierra
 *           el registro viejo y se abre uno nuevo. Un UPDATE sobre el valor
 *           haria que un mes ya reportado cambiara por detras, y nadie podria
 *           explicar despues por que subio ni desde cuando.
 *
 * SERVICIO  Los servicios publicos. La factura llega despues de cerrado el
 *           periodo. Mientras no llegue se estima con el promedio de los
 *           ultimos meses cargados, y queda MARCADO como estimado.
 *
 * Y aparte, las cuatro bolsas de estructura y la nomina agregada por tienda.
 *
 * EL ESTIMADO SE MARCA, SIEMPRE
 *
 * Un estimado que se ve igual que un real es peor que no tener el dato: quien
 * mira toma una decision creyendo que el numero es firme. Por eso
 * {@link Servicio#origen} viaja hasta la pantalla junto al valor, y
 * {@link Servicio#mesesPromediados} dice sobre cuantos meses se saco: uno solo
 * no vale lo mismo que tres.
 *
 * Y CUANDO NO HAY CON QUE ESTIMAR, NO SE INVENTA UN CERO
 *
 * Una tienda nueva no tiene historia. Ahi el metodo devuelve null y la pantalla
 * dice "sin base para estimar". Un cero se suma al total y se pierde de vista;
 * un vacio se ve.
 */
public class GerenciaGastoDAO {

	/** Cuantos meses hacia atras se miran para estimar un servicio. */
	private static final int MESES_PARA_PROMEDIAR = 3;

	public static class Concepto {
		public int idConcepto;
		public String nombre = "";
		public String tipo = "";
		public String linea = "";
		public int orden;
	}

	public static class GastoFijo {
		public int idGasto;
		public int idTienda;
		public int idConcepto;
		public String concepto = "";
		public double valorMensual;
		public String vigenciaDesde = "";
		public String vigenciaHasta = "";
		public String observacion = "";
		public String usuario = "";
	}

	public static class Servicio {
		public int idServicio;
		public int idTienda;
		public int idConcepto;
		public String concepto = "";
		public int anio;
		public int mes;
		public double valor;
		public String origen = "";
		public int mesesPromediados;
		public String usuario = "";
	}

	public static class Estimacion {
		public double valor;
		public int meses;
	}

	public static class Pool {
		public int anio;
		public int mes;
		public String tipo = "";
		public double valor;
		public String usuario = "";
	}

	public static class Nomina {
		public int idTienda;
		public int anio;
		public int mes;
		public int empleados;
		public double sueldoBasico;
		public double sueldoVariable;
		public double seguridadSocial;
		public double liquidacion;
		public String usuario = "";
	}

	public static class Tienda {
		public int idTienda;
		public String nombre = "";
	}

	// =======================================================================
	// CATALOGOS
	// =======================================================================

	public static ArrayList<Concepto> obtenerConceptos() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Concepto> conceptos = new ArrayList<Concepto>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idconcepto, nombre, tipo, linea, orden FROM gerencia_concepto_gasto"
					+ " WHERE activo = 'S' ORDER BY orden, nombre");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Concepto c = new Concepto();
				c.idConcepto = rs.getInt("idconcepto");
				c.nombre = rs.getString("nombre");
				c.tipo = rs.getString("tipo");
				c.linea = rs.getString("linea");
				c.orden = rs.getInt("orden");
				conceptos.add(c);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerConceptos: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (conceptos);
	}

	/**
	 * Las tiendas que de verdad operan.
	 *
	 * Se dejan por fuera Poblado y Medayoung: estan en el maestro pero no tienen
	 * host de base de datos, no venden por el sistema y meterlas en el reparto
	 * de la estructura le quitaria plata a las que si venden.
	 */
	public static ArrayList<Tienda> obtenerTiendas() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Tienda> tiendas = new ArrayList<Tienda>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtienda, nombre FROM tienda"
					+ " WHERE idtienda NOT IN (6, 14) ORDER BY idtienda");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Tienda t = new Tienda();
				t.idTienda = rs.getInt("idtienda");
				t.nombre = rs.getString("nombre");
				tiendas.add(t);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerTiendas: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (tiendas);
	}

	// =======================================================================
	// GASTOS FIJOS
	// =======================================================================

	/**
	 * Los gastos fijos vigentes a una fecha.
	 *
	 * "Vigente el 2026-07-31" es lo que estaba rigiendo ese dia, no lo que rige
	 * hoy. Por eso la consulta compara contra la fecha que se pide y no contra
	 * NOW(): mirar julio en octubre tiene que mostrar el arriendo de julio.
	 */
	public static ArrayList<GastoFijo> obtenerFijosVigentes(final String fecha, final int idTienda) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<GastoFijo> gastos = new ArrayList<GastoFijo>();
		try {
			final StringBuilder sql = new StringBuilder(
					"SELECT g.idgasto, g.idtienda, g.idconcepto, c.nombre, g.valor_mensual,"
					+ " g.vigencia_desde, g.vigencia_hasta, g.observacion, g.usuario"
					+ " FROM gerencia_gasto_fijo_tienda g"
					+ " JOIN gerencia_concepto_gasto c ON c.idconcepto = g.idconcepto"
					+ " WHERE g.vigencia_desde <= ?"
					+ "   AND (g.vigencia_hasta IS NULL OR g.vigencia_hasta >= ?)");
			if (idTienda > 0) {
				sql.append(" AND g.idtienda = ?");
			}
			sql.append(" ORDER BY g.idtienda, c.orden");

			final PreparedStatement ps = cn.prepareStatement(sql.toString());
			ps.setString(1, fecha);
			ps.setString(2, fecha);
			if (idTienda > 0) {
				ps.setInt(3, idTienda);
			}
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				gastos.add(leerFijo(rs));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerFijosVigentes: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (gastos);
	}

	/** Toda la historia de un concepto en una tienda, de lo mas nuevo a lo mas viejo. */
	public static ArrayList<GastoFijo> obtenerHistoriaFijo(final int idTienda, final int idConcepto) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<GastoFijo> gastos = new ArrayList<GastoFijo>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT g.idgasto, g.idtienda, g.idconcepto, c.nombre, g.valor_mensual,"
					+ " g.vigencia_desde, g.vigencia_hasta, g.observacion, g.usuario"
					+ " FROM gerencia_gasto_fijo_tienda g"
					+ " JOIN gerencia_concepto_gasto c ON c.idconcepto = g.idconcepto"
					+ " WHERE g.idtienda = ? AND g.idconcepto = ?"
					+ " ORDER BY g.vigencia_desde DESC");
			ps.setInt(1, idTienda);
			ps.setInt(2, idConcepto);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				gastos.add(leerFijo(rs));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerHistoriaFijo: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (gastos);
	}

	/**
	 * Pone un valor nuevo a regir desde una fecha.
	 *
	 * Lo hace en dos pasos y en una sola transaccion: le cierra la vigencia al
	 * registro que estaba abierto -el dia antes- y abre el nuevo. Si lo segundo
	 * falla, lo primero se deshace: un concepto sin ningun registro vigente
	 * dejaria la tienda sin arriendo en el tablero.
	 *
	 * Devuelve vacio si salio bien, o el motivo.
	 */
	public static String guardarFijo(final GastoFijo gasto, final String usuario) {
		if (gasto.vigenciaDesde == null || gasto.vigenciaDesde.trim().length() == 0) {
			return ("Falta la fecha desde la que rige");
		}
		if (gasto.valorMensual < 0) {
			return ("El valor no puede ser negativo");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			cn.setAutoCommit(false);

			//No se puede abrir una vigencia que empiece antes de una que ya
			//existe: quedarian dos valores rigiendo el mismo dia y el tablero
			//tendria que adivinar cual usar.
			final PreparedStatement choque = cn.prepareStatement(
					"SELECT COUNT(*) FROM gerencia_gasto_fijo_tienda"
					+ " WHERE idtienda = ? AND idconcepto = ? AND vigencia_desde >= ?");
			choque.setInt(1, gasto.idTienda);
			choque.setInt(2, gasto.idConcepto);
			choque.setString(3, gasto.vigenciaDesde);
			final ResultSet rsChoque = choque.executeQuery();
			int posteriores = 0;
			if (rsChoque.next()) {
				posteriores = rsChoque.getInt(1);
			}
			rsChoque.close();
			choque.close();

			if (posteriores > 0) {
				cn.rollback();
				return ("Ya hay un valor registrado para ese concepto desde esa fecha o despues. "
						+ "Revise la historia del concepto.");
			}

			final PreparedStatement cerrarVigente = cn.prepareStatement(
					"UPDATE gerencia_gasto_fijo_tienda"
					+ " SET vigencia_hasta = DATE_SUB(?, INTERVAL 1 DAY)"
					+ " WHERE idtienda = ? AND idconcepto = ? AND vigencia_hasta IS NULL");
			cerrarVigente.setString(1, gasto.vigenciaDesde);
			cerrarVigente.setInt(2, gasto.idTienda);
			cerrarVigente.setInt(3, gasto.idConcepto);
			cerrarVigente.executeUpdate();
			cerrarVigente.close();

			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_gasto_fijo_tienda"
					+ " (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta,"
					+ "  observacion, usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?, NULL, ?, ?, ?)");
			ps.setInt(1, gasto.idTienda);
			ps.setInt(2, gasto.idConcepto);
			ps.setDouble(3, gasto.valorMensual);
			ps.setString(4, gasto.vigenciaDesde);
			ps.setString(5, gasto.observacion == null ? "" : gasto.observacion);
			ps.setString(6, usuario);
			ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
			ps.close();

			cn.commit();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.guardarFijo: " + e.toString());
			error = "No se pudo guardar el gasto";
			try {
				cn.rollback();
			} catch (final Exception e1) {
				System.out.println("GerenciaGastoDAO.guardarFijo rollback: " + e1.toString());
			}
		} finally {
			try {
				if (cn != null) {
					cn.setAutoCommit(true);
				}
			} catch (final Exception e2) {
				System.out.println("GerenciaGastoDAO.guardarFijo autocommit: " + e2.toString());
			}
			cerrar(cn);
		}
		return (error);
	}

	// =======================================================================
	// SERVICIOS
	// =======================================================================

	/** Lo que hay cargado de servicios para un mes. */
	public static ArrayList<Servicio> obtenerServicios(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Servicio> servicios = new ArrayList<Servicio>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT s.idservicio, s.idtienda, s.idconcepto, c.nombre, s.anio, s.mes,"
					+ " s.valor, s.origen, s.meses_promediados, s.usuario"
					+ " FROM gerencia_gasto_servicio s"
					+ " JOIN gerencia_concepto_gasto c ON c.idconcepto = s.idconcepto"
					+ " WHERE s.anio = ? AND s.mes = ?"
					+ " ORDER BY s.idtienda, c.orden");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Servicio s = new Servicio();
				s.idServicio = rs.getInt("idservicio");
				s.idTienda = rs.getInt("idtienda");
				s.idConcepto = rs.getInt("idconcepto");
				s.concepto = rs.getString("nombre");
				s.anio = rs.getInt("anio");
				s.mes = rs.getInt("mes");
				s.valor = rs.getDouble("valor");
				s.origen = rs.getString("origen");
				s.mesesPromediados = rs.getInt("meses_promediados");
				s.usuario = rs.getString("usuario") == null ? "" : rs.getString("usuario");
				servicios.add(s);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerServicios: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (servicios);
	}

	/**
	 * El promedio de los ultimos meses cargados, para estimar el que falta.
	 *
	 * Solo promedia valores REALES. Promediar estimados haria que un mes sin
	 * factura se calculara sobre otro mes sin factura, y al tercero el numero ya
	 * no tendria ninguna relacion con lo que se paga.
	 *
	 * Mira hacia atras {@value #MESES_PARA_PROMEDIAR} meses. Devuelve null si no
	 * hay ni uno: una tienda nueva no tiene con que estimarse, y hay que decirlo
	 * en vez de responder cero.
	 */
	public static Estimacion estimarServicio(final int idTienda, final int idConcepto,
			final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		Estimacion estimacion = null;
		try {
			//El periodo se compara como un solo numero -anio*12+mes- para no
			//tener que partir la condicion en "el ano anterior desde el mes tal
			//y este ano hasta el mes cual", que es donde se equivoca uno en
			//enero.
			final int periodo = (anio * 12) + mes;
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT AVG(valor), COUNT(*) FROM gerencia_gasto_servicio"
					+ " WHERE idtienda = ? AND idconcepto = ? AND origen = 'REAL'"
					+ "   AND ((anio * 12) + mes) < ?"
					+ "   AND ((anio * 12) + mes) >= ?");
			ps.setInt(1, idTienda);
			ps.setInt(2, idConcepto);
			ps.setInt(3, periodo);
			ps.setInt(4, periodo - MESES_PARA_PROMEDIAR);
			final ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getInt(2) > 0) {
				estimacion = new Estimacion();
				estimacion.valor = rs.getDouble(1);
				estimacion.meses = rs.getInt(2);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.estimarServicio: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (estimacion);
	}

	/**
	 * Guarda el valor de un servicio.
	 *
	 * Un REAL pisa a un ESTIMADO sin preguntar, que es lo que se quiere cuando
	 * por fin llega la factura. Al reves no: un estimado NO pisa un real, o una
	 * corrida del estimador borraria la factura que alguien ya cargo.
	 */
	public static String guardarServicio(final Servicio servicio, final String usuario) {
		if (servicio.valor < 0) {
			return ("El valor no puede ser negativo");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			final String sql = "INSERT INTO gerencia_gasto_servicio"
					+ " (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados,"
					+ "  usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE"
					+ "   valor = IF(? = 'REAL' OR origen <> 'REAL', ?, valor),"
					+ "   origen = IF(? = 'REAL' OR origen <> 'REAL', ?, origen),"
					+ "   meses_promediados = IF(? = 'REAL' OR origen <> 'REAL', ?, meses_promediados),"
					+ "   usuario = ?, fecha_registro = ?";
			final PreparedStatement ps = cn.prepareStatement(sql);
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			int i = 1;
			ps.setInt(i++, servicio.idTienda);
			ps.setInt(i++, servicio.idConcepto);
			ps.setInt(i++, servicio.anio);
			ps.setInt(i++, servicio.mes);
			ps.setDouble(i++, servicio.valor);
			ps.setString(i++, servicio.origen);
			ps.setInt(i++, servicio.mesesPromediados);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.setString(i++, servicio.origen);
			ps.setDouble(i++, servicio.valor);
			ps.setString(i++, servicio.origen);
			ps.setString(i++, servicio.origen);
			ps.setString(i++, servicio.origen);
			ps.setInt(i++, servicio.mesesPromediados);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.guardarServicio: " + e.toString());
			error = "No se pudo guardar el servicio";
		} finally {
			cerrar(cn);
		}
		return (error);
	}

	// =======================================================================
	// ESTRUCTURA Y NOMINA
	// =======================================================================

	public static ArrayList<Pool> obtenerPools(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Pool> pools = new ArrayList<Pool>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT anio, mes, tipo, valor, usuario FROM gerencia_pool_estructura"
					+ " WHERE anio = ? AND mes = ? ORDER BY tipo");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Pool p = new Pool();
				p.anio = rs.getInt("anio");
				p.mes = rs.getInt("mes");
				p.tipo = rs.getString("tipo");
				p.valor = rs.getDouble("valor");
				p.usuario = rs.getString("usuario") == null ? "" : rs.getString("usuario");
				pools.add(p);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerPools: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (pools);
	}

	public static String guardarPool(final Pool pool, final String usuario) {
		if (pool.valor < 0) {
			return ("El valor no puede ser negativo");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_pool_estructura (anio, mes, tipo, valor, usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE valor = ?, usuario = ?, fecha_registro = ?");
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			ps.setInt(1, pool.anio);
			ps.setInt(2, pool.mes);
			ps.setString(3, pool.tipo);
			ps.setDouble(4, pool.valor);
			ps.setString(5, usuario);
			ps.setTimestamp(6, ahora);
			ps.setDouble(7, pool.valor);
			ps.setString(8, usuario);
			ps.setTimestamp(9, ahora);
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.guardarPool: " + e.toString());
			error = "No se pudo guardar la bolsa de estructura";
		} finally {
			cerrar(cn);
		}
		return (error);
	}

	public static ArrayList<Nomina> obtenerNominas(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Nomina> nominas = new ArrayList<Nomina>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable,"
					+ " seguridad_social, liquidacion, usuario"
					+ " FROM gerencia_nomina_tienda WHERE anio = ? AND mes = ? ORDER BY idtienda");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Nomina n = new Nomina();
				n.idTienda = rs.getInt("idtienda");
				n.anio = rs.getInt("anio");
				n.mes = rs.getInt("mes");
				n.empleados = rs.getInt("empleados");
				n.sueldoBasico = rs.getDouble("sueldo_basico");
				n.sueldoVariable = rs.getDouble("sueldo_variable");
				n.seguridadSocial = rs.getDouble("seguridad_social");
				n.liquidacion = rs.getDouble("liquidacion");
				n.usuario = rs.getString("usuario") == null ? "" : rs.getString("usuario");
				nominas.add(n);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.obtenerNominas: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (nominas);
	}

	public static String guardarNomina(final Nomina nomina, final String usuario) {
		if (nomina.sueldoBasico < 0 || nomina.sueldoVariable < 0
				|| nomina.seguridadSocial < 0 || nomina.liquidacion < 0) {
			return ("Los valores no pueden ser negativos");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_tienda (idtienda, anio, mes, empleados,"
					+ " sueldo_basico, sueldo_variable, seguridad_social, liquidacion,"
					+ " usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE empleados = ?, sueldo_basico = ?,"
					+ " sueldo_variable = ?, seguridad_social = ?, liquidacion = ?,"
					+ " usuario = ?, fecha_registro = ?");
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			int i = 1;
			ps.setInt(i++, nomina.idTienda);
			ps.setInt(i++, nomina.anio);
			ps.setInt(i++, nomina.mes);
			ps.setInt(i++, nomina.empleados);
			ps.setDouble(i++, nomina.sueldoBasico);
			ps.setDouble(i++, nomina.sueldoVariable);
			ps.setDouble(i++, nomina.seguridadSocial);
			ps.setDouble(i++, nomina.liquidacion);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.setInt(i++, nomina.empleados);
			ps.setDouble(i++, nomina.sueldoBasico);
			ps.setDouble(i++, nomina.sueldoVariable);
			ps.setDouble(i++, nomina.seguridadSocial);
			ps.setDouble(i++, nomina.liquidacion);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.guardarNomina: " + e.toString());
			error = "No se pudo guardar la nomina";
		} finally {
			cerrar(cn);
		}
		return (error);
	}

	// =======================================================================
	// LA VENTA DEL MES, QUE ES LA QUE REPARTE LA ESTRUCTURA
	// =======================================================================

	/**
	 * La venta de cada tienda en un mes, sumando las semanas que el calendario
	 * le asigno a ese mes.
	 *
	 * Aqui es donde el calendario deja de ser un adorno: "julio" no es del 1 al
	 * 31, es el conjunto de semanas que gerencia le asigno a julio. Sumar por
	 * fecha del reloj partiria una semana entre dos meses y ninguno de los dos
	 * cuadraria con la venta real de sus semanas.
	 *
	 * El cruce va por el domingo: datamart.venta_semanal_tienda guarda la
	 * semana con la fecha de su cierre, que es el mismo domingo que
	 * gerencia_semana tiene en fecha_fin.
	 *
	 * Devuelve la lista vacia si el ano no tiene calendario. No se cae a "el mes
	 * calendario" como plan B: un reparto hecho sobre otra definicion de mes se
	 * ve igual de bien y esta mal, y nadie se enteraria.
	 */
	public static ArrayList<double[]> ventaPorTiendaDelMes(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<double[]> ventas = new ArrayList<double[]>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT v.idtienda, SUM(v.valor)"
					+ " FROM inventarioamericana.gerencia_semana s"
					+ " JOIN datamart.venta_semanal_tienda v ON v.fecha = s.fecha_fin"
					+ " WHERE s.anio = ? AND s.mes = ?"
					+ " GROUP BY v.idtienda ORDER BY v.idtienda");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ventas.add(new double[] { rs.getInt(1), rs.getDouble(2) });
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.ventaPorTiendaDelMes: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (ventas);
	}

	/** Cuantas semanas le asigno el calendario a ese mes. Cero si no hay calendario. */
	public static int semanasDelMes(final int anio, final int mes) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		int cuantas = 0;
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT COUNT(*) FROM gerencia_semana WHERE anio = ? AND mes = ?");
			ps.setInt(1, anio);
			ps.setInt(2, mes);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				cuantas = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO.semanasDelMes: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (cuantas);
	}

	// =======================================================================

	private static GastoFijo leerFijo(final ResultSet rs) throws java.sql.SQLException {
		final GastoFijo g = new GastoFijo();
		g.idGasto = rs.getInt("idgasto");
		g.idTienda = rs.getInt("idtienda");
		g.idConcepto = rs.getInt("idconcepto");
		g.concepto = rs.getString("nombre");
		g.valorMensual = rs.getDouble("valor_mensual");
		g.vigenciaDesde = rs.getString("vigencia_desde");
		g.vigenciaHasta = rs.getString("vigencia_hasta") == null ? "" : rs.getString("vigencia_hasta");
		g.observacion = rs.getString("observacion") == null ? "" : rs.getString("observacion");
		g.usuario = rs.getString("usuario") == null ? "" : rs.getString("usuario");
		return (g);
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaGastoDAO: cerrando conexion " + e.toString());
		}
	}
}

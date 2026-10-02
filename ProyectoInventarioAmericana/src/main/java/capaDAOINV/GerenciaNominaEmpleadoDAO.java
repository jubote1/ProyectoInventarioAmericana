package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import conexionINV.ConexionBaseDatos;

/**
 * Nomina cargada por EMPLEADO y semana, repartida entre tiendas segun donde
 * marco biometria esa semana.
 *
 * POR QUE ESTO Y NO SOLO gerencia_nomina_tienda
 *
 * gerencia_nomina_tienda sirve perfecto para un empleado que es de una sola
 * tienda -el 90% del Excel-. El problema son las tres hojas compartidas
 * (ADMINISTRATIVA, LOGISTICA Y PRODUCCION, CONTAC CENTER): esas personas no
 * son de una tienda, y hoy el Excel las reparte PAREJO entre tiendas (Contac
 * Center literalmente /6), sin mirar quien trabajo donde.
 *
 * Aqui se carga el costo POR PERSONA -para todos, no solo los compartidos: un
 * empleado de tienda propia deberia salir ~100% en su tienda por biometria, y
 * eso es un chequeo gratis- y el reparto entre tiendas se calcula solo, a
 * partir de general.empleado_evento (555 mil marcas, 700 empleados, 15
 * tiendas, en vivo desde 2019).
 *
 * COMO SE EMPAREJA UNA MARCA
 *
 * Se recorren los eventos de la semana en orden. Un INGRESO abre un turno; el
 * SALIDA que sigue lo cierra, y los minutos van a la tienda del INGRESO. Esto
 * es DISTINTO de como empareja Servicios (ReporteHorariosDAO), que compara
 * por MISMO dia calendario y por eso pierde los turnos que cruzan medianoche:
 * aqui se compara por el timestamp real, asi que un turno de 22:00 a 02:00 se
 * cuenta completo. Un INGRESO sin SALIDA (o viceversa) queda huerfano y no
 * suma nada -mejor perder ese turno que inventarle una duracion-. Un turno de
 * mas de {@link #MAX_MINUTOS_TURNO} minutos tambien se descarta completo: eso
 * ya no es un turno, es una marca que se le olvido a alguien.
 *
 * QUE PASA SIN MARCACIONES ESA SEMANA
 *
 * No se reparte nada -no se inventa un 100% a ninguna tienda-. Si el empleado
 * sigue activo, aparece en {@link #pendientesSinReparto} para que alguien lo
 * revise a mano (vacaciones, incapacidad, o de verdad no marco). Si esta
 * inactivo, no se avisa: ya no hay nada que revisar.
 *
 * LAS MARCAS MANUALES (uso_biometria = 'N') CUENTAN IGUAL
 *
 * Excluirlas sesgaria el reparto hacia las tiendas con mejor huellero, no
 * hacia donde de verdad trabajo la gente. Se usan todas para saber DONDE
 * estuvo el empleado; el campo queda guardado en el evento por si algun dia
 * hace falta filtrar por confianza.
 */
public class GerenciaNominaEmpleadoDAO {

	/** Un turno mas largo que esto no es un turno real: es un INGRESO o SALIDA que se olvido marcar. */
	private static final int MAX_MINUTOS_TURNO = 16 * 60;

	public static class NominaEmpleado {
		public int idEmpleado;
		public String nombreEmpleado = "";
		public String semana = ""; // domingo de cierre, yyyy-MM-dd
		public double sueldoBasico;
		public double sueldoVariable;
		public double seguridadSocial;
		public double liquidacion;
		public String origen = "MANUAL";
		public String usuario = "";

		public double total() {
			return (this.sueldoBasico + this.sueldoVariable + this.seguridadSocial + this.liquidacion);
		}
	}

	public static class RepartoTienda {
		public int idEmpleado;
		public String nombreEmpleado = "";
		public String semana = "";
		public int idTienda;
		public String nombreTienda = "";
		public long minutos;
		public double porcentaje;
		public double sueldoBasico;
		public double sueldoVariable;
		public double seguridadSocial;
		public double liquidacion;
	}

	public static class Empleado {
		public int idEmpleado;
		public String nombreLargo = "";
		public boolean activo;
	}

	private static class Evento {
		String tipo;
		java.sql.Timestamp fechaHoraLog;
		int idTienda;
	}

	// =======================================================================
	// BUSQUEDA DE EMPLEADO (general.empleado: la misma identidad que biometria)
	// =======================================================================

	/** Empleados cuyo nombre contiene el filtro. Vacio devuelve los primeros 30 activos. */
	public static ArrayList<Empleado> buscarEmpleados(final String filtro) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		final ArrayList<Empleado> empleados = new ArrayList<Empleado>();
		try {
			final boolean hayFiltro = filtro != null && filtro.trim().length() > 0;
			final String sql = "SELECT id, nombre_largo, activo FROM empleado"
					+ (hayFiltro ? " WHERE nombre_largo LIKE ?" : " WHERE activo = 1")
					+ " ORDER BY nombre_largo LIMIT 30";
			final PreparedStatement ps = cn.prepareStatement(sql);
			if (hayFiltro) {
				ps.setString(1, "%" + filtro.trim() + "%");
			}
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Empleado e = new Empleado();
				e.idEmpleado = rs.getInt("id");
				e.nombreLargo = rs.getString("nombre_largo");
				e.activo = rs.getInt("activo") == 1;
				empleados.add(e);
			}
			rs.close();
			ps.close();
		} catch (final Exception ex) {
			System.out.println("GerenciaNominaEmpleadoDAO.buscarEmpleados: " + ex.toString());
		} finally {
			cerrar(cn);
		}
		return (empleados);
	}

	// =======================================================================
	// CARGA DE NOMINA POR EMPLEADO Y SEMANA
	// =======================================================================

	/** Lo cargado para una semana, con el nombre ya cruzado. */
	public static ArrayList<NominaEmpleado> obtenerNominaSemana(final String semana) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<NominaEmpleado> lista = new ArrayList<NominaEmpleado>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social,"
					+ " liquidacion, origen, usuario"
					+ " FROM gerencia_nomina_empleado_semana WHERE semana = ? ORDER BY idempleado");
			ps.setString(1, semana);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				lista.add(leerNomina(rs));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.obtenerNominaSemana: " + e.toString());
		} finally {
			cerrar(cn);
		}
		cruzarNombres(lista);
		return (lista);
	}

	/**
	 * Guarda o reemplaza el costo cargado de un empleado en una semana.
	 *
	 * NO recalcula el reparto: eso es un paso aparte ({@link #calcularReparto}),
	 * a proposito. Cargar un valor no deberia disparar automaticamente una
	 * consulta a 15 tiendas por cada tecla; se calcula cuando el usuario lo pide,
	 * o por lote.
	 */
	public static String guardarNomina(final NominaEmpleado n, final String usuario) {
		if (n.idEmpleado <= 0) {
			return ("Falta el empleado");
		}
		if (n.semana == null || n.semana.trim().length() < 10) {
			return ("Falta la semana");
		}
		if (n.sueldoBasico < 0 || n.sueldoVariable < 0 || n.seguridadSocial < 0 || n.liquidacion < 0) {
			return ("Los valores no pueden ser negativos");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_empleado_semana"
					+ " (idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social, liquidacion,"
					+ "  origen, usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE sueldo_basico = ?, sueldo_variable = ?, seguridad_social = ?,"
					+ " liquidacion = ?, origen = ?, usuario = ?, fecha_registro = ?");
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			int i = 1;
			ps.setInt(i++, n.idEmpleado);
			ps.setString(i++, n.semana);
			ps.setDouble(i++, n.sueldoBasico);
			ps.setDouble(i++, n.sueldoVariable);
			ps.setDouble(i++, n.seguridadSocial);
			ps.setDouble(i++, n.liquidacion);
			ps.setString(i++, n.origen == null || n.origen.trim().isEmpty() ? "MANUAL" : n.origen);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.setDouble(i++, n.sueldoBasico);
			ps.setDouble(i++, n.sueldoVariable);
			ps.setDouble(i++, n.seguridadSocial);
			ps.setDouble(i++, n.liquidacion);
			ps.setString(i++, n.origen == null || n.origen.trim().isEmpty() ? "MANUAL" : n.origen);
			ps.setString(i++, usuario);
			ps.setTimestamp(i++, ahora);
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.guardarNomina: " + e.toString());
			error = "No se pudo guardar la nomina del empleado";
		} finally {
			cerrar(cn);
		}
		return (error);
	}

	// =======================================================================
	// EL REPARTO: de general.empleado_evento a gerencia_nomina_empleado_tienda_semana
	// =======================================================================

	/**
	 * Calcula y GUARDA el reparto de todos los empleados cargados en una semana.
	 * Reprocesable: primero borra lo que hubiera calculado antes para esa
	 * semana, asi que correrlo de nuevo con biometria corregida da el resultado
	 * correcto, no uno sumado sobre el viejo.
	 *
	 * @return cuantos empleados quedaron repartidos, de los cargados en la semana
	 */
	public static int calcularReparto(final String semana) {
		final ArrayList<NominaEmpleado> cargados = obtenerNominaSemana(semana);
		if (cargados.isEmpty()) {
			return (0);
		}
		final String[] rango = rangoDeLaSemana(semana);
		if (rango == null) {
			System.out.println("GerenciaNominaEmpleadoDAO.calcularReparto: semana ilegible '" + semana + "'");
			return (0);
		}

		final ConexionBaseDatos conGeneral = new ConexionBaseDatos();
		final ConexionBaseDatos conLocal = new ConexionBaseDatos();
		final Connection cnGeneral = conGeneral.obtenerConexionBDGeneral();
		final Connection cnLocal = conLocal.obtenerConexionBDPrincipalLocal();
		int repartidos = 0;
		try {
			cnLocal.setAutoCommit(false);

			//Se borra el reparto viejo de TODOS los cargados de la semana antes de
			//volver a calcular: un reprocesamiento tiene que reemplazar, no sumar.
			final PreparedStatement borrar = cnLocal.prepareStatement(
					"DELETE FROM gerencia_nomina_empleado_tienda_semana WHERE semana = ?");
			borrar.setString(1, semana);
			borrar.executeUpdate();
			borrar.close();

			for (final NominaEmpleado n : cargados) {
				final ArrayList<Evento> eventos = eventosDeLaSemana(cnGeneral, n.idEmpleado, rango[0], rango[1]);
				final Map<Integer, Long> minutosPorTienda = emparejar(eventos);
				if (minutosPorTienda.isEmpty()) {
					//Sin marcaciones: no se reparte nada. pendientesSinReparto() es quien
					//avisa de esto, filtrando por activo.
					continue;
				}
				long totalMinutos = 0;
				for (final long m : minutosPorTienda.values()) {
					totalMinutos += m;
				}
				if (totalMinutos <= 0) {
					continue;
				}
				for (final Map.Entry<Integer, Long> entrada : minutosPorTienda.entrySet()) {
					final double porcentaje = entrada.getValue() / (double) totalMinutos;
					final PreparedStatement ins = cnLocal.prepareStatement(
							"INSERT INTO gerencia_nomina_empleado_tienda_semana"
							+ " (idempleado, semana, idtienda, minutos, porcentaje,"
							+ "  sueldo_basico, sueldo_variable, seguridad_social, liquidacion, fecha_calculo)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					int i = 1;
					ins.setInt(i++, n.idEmpleado);
					ins.setString(i++, semana);
					ins.setInt(i++, entrada.getKey());
					ins.setLong(i++, entrada.getValue());
					ins.setDouble(i++, porcentaje);
					ins.setDouble(i++, n.sueldoBasico * porcentaje);
					ins.setDouble(i++, n.sueldoVariable * porcentaje);
					ins.setDouble(i++, n.seguridadSocial * porcentaje);
					ins.setDouble(i++, n.liquidacion * porcentaje);
					ins.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
					ins.executeUpdate();
					ins.close();
				}
				repartidos++;
			}
			cnLocal.commit();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.calcularReparto: " + e.toString());
			try {
				cnLocal.rollback();
			} catch (final Exception e1) {
			}
			repartidos = 0;
		} finally {
			try {
				cnLocal.setAutoCommit(true);
			} catch (final Exception e2) {
			}
			cerrar(cnGeneral);
			cerrar(cnLocal);
		}
		return (repartidos);
	}

	/** El reparto ya calculado para una semana, con nombres de empleado y tienda. */
	public static ArrayList<RepartoTienda> obtenerReparto(final String semana) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<RepartoTienda> lista = new ArrayList<RepartoTienda>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT r.idempleado, r.semana, r.idtienda, t.nombre, r.minutos, r.porcentaje,"
					+ " r.sueldo_basico, r.sueldo_variable, r.seguridad_social, r.liquidacion"
					+ " FROM gerencia_nomina_empleado_tienda_semana r"
					+ " JOIN tienda t ON t.idtienda = r.idtienda"
					+ " WHERE r.semana = ? ORDER BY r.idempleado, r.porcentaje DESC");
			ps.setString(1, semana);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final RepartoTienda r = new RepartoTienda();
				r.idEmpleado = rs.getInt("idempleado");
				r.semana = rs.getString("semana");
				r.idTienda = rs.getInt("idtienda");
				r.nombreTienda = rs.getString("nombre");
				r.minutos = rs.getLong("minutos");
				r.porcentaje = rs.getDouble("porcentaje");
				r.sueldoBasico = rs.getDouble("sueldo_basico");
				r.sueldoVariable = rs.getDouble("sueldo_variable");
				r.seguridadSocial = rs.getDouble("seguridad_social");
				r.liquidacion = rs.getDouble("liquidacion");
				lista.add(r);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.obtenerReparto: " + e.toString());
		} finally {
			cerrar(cn);
		}
		cruzarNombresReparto(lista);
		return (lista);
	}

	/**
	 * Empleados con nomina cargada esa semana que quedaron SIN ningun minuto que
	 * repartir, y que siguen activos hoy -a un empleado inactivo no hay nada que
	 * avisarle a nadie-.
	 */
	public static ArrayList<NominaEmpleado> pendientesSinReparto(final String semana) {
		final ArrayList<NominaEmpleado> cargados = obtenerNominaSemana(semana);
		if (cargados.isEmpty()) {
			return (cargados);
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final java.util.Set<Integer> conReparto = new java.util.HashSet<Integer>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT DISTINCT idempleado FROM gerencia_nomina_empleado_tienda_semana WHERE semana = ?");
			ps.setString(1, semana);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				conReparto.add(Integer.valueOf(rs.getInt(1)));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.pendientesSinReparto: " + e.toString());
		} finally {
			cerrar(cn);
		}

		final ArrayList<NominaEmpleado> pendientes = new ArrayList<NominaEmpleado>();
		for (final NominaEmpleado n : cargados) {
			if (!conReparto.contains(Integer.valueOf(n.idEmpleado))) {
				pendientes.add(n);
			}
		}
		filtrarSoloActivos(pendientes);
		return (pendientes);
	}

	// =======================================================================
	// EMPAREJAMIENTO DE BIOMETRIA
	// =======================================================================

	/**
	 * Los eventos de un empleado dentro de un rango de fechas, en orden
	 * cronologico. Se leen TODOS -biometricos y manuales por igual, ver la nota
	 * de la clase- y se marca cual es cual solo para referencia.
	 */
	private static ArrayList<Evento> eventosDeLaSemana(final Connection cnGeneral, final int idEmpleado,
			final String desde, final String hasta) {
		final ArrayList<Evento> eventos = new ArrayList<Evento>();
		try {
			final PreparedStatement ps = cnGeneral.prepareStatement(
					"SELECT tipo_evento, fecha_hora_log, idtienda FROM empleado_evento"
					+ " WHERE id = ? AND fecha >= ? AND fecha <= ?"
					+ " ORDER BY fecha_hora_log ASC");
			ps.setInt(1, idEmpleado);
			ps.setString(2, desde);
			ps.setString(3, hasta);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Evento e = new Evento();
				e.tipo = rs.getString("tipo_evento");
				e.fechaHoraLog = rs.getTimestamp("fecha_hora_log");
				e.idTienda = rs.getInt("idtienda");
				eventos.add(e);
			}
			rs.close();
			ps.close();
		} catch (final Exception ex) {
			System.out.println("GerenciaNominaEmpleadoDAO.eventosDeLaSemana empleado " + idEmpleado + ": " + ex);
		}
		return (eventos);
	}

	/**
	 * INGRESO abre turno, el SALIDA que sigue lo cierra y los minutos van a la
	 * tienda del INGRESO. Ver la nota de la clase para por que esto es distinto
	 * de como empareja Servicios.
	 */
	private static Map<Integer, Long> emparejar(final ArrayList<Evento> eventos) {
		final Map<Integer, Long> minutos = new LinkedHashMap<Integer, Long>();
		Evento ingresoAbierto = null;
		for (final Evento e : eventos) {
			if (e.fechaHoraLog == null) {
				continue;
			}
			if ("INGRESO".equalsIgnoreCase(e.tipo)) {
				//Dos INGRESO seguidos sin SALIDA de por medio: el primero queda
				//huerfano -se descarta, no se le inventa un cierre-.
				ingresoAbierto = e;
			} else if ("SALIDA".equalsIgnoreCase(e.tipo)) {
				if (ingresoAbierto != null) {
					final long minutosTurno = (e.fechaHoraLog.getTime() - ingresoAbierto.fechaHoraLog.getTime())
							/ 60000L;
					if (minutosTurno > 0 && minutosTurno <= MAX_MINUTOS_TURNO) {
						final Integer tienda = Integer.valueOf(ingresoAbierto.idTienda);
						final Long previo = minutos.get(tienda);
						minutos.put(tienda, Long.valueOf((previo == null ? 0L : previo.longValue()) + minutosTurno));
					}
					ingresoAbierto = null;
				}
				//SALIDA sin INGRESO abierto: huerfana, se ignora.
			}
		}
		return (minutos);
	}

	// =======================================================================
	// AUXILIARES
	// =======================================================================

	/** Lunes a domingo, dado el domingo de cierre. Null si la fecha no se puede leer. */
	private static String[] rangoDeLaSemana(final String domingo) {
		try {
			final java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("yyyy-MM-dd");
			final java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(formato.parse(domingo.trim()));
			if (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY) {
				System.out.println("GerenciaNominaEmpleadoDAO: la semana " + domingo + " no cae domingo");
				return (null);
			}
			final String fin = formato.format(cal.getTime());
			cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
			return (new String[] { formato.format(cal.getTime()), fin });
		} catch (final Exception e) {
			return (null);
		}
	}

	private static void filtrarSoloActivos(final ArrayList<NominaEmpleado> lista) {
		if (lista.isEmpty()) {
			return;
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		final java.util.Set<Integer> activos = new java.util.HashSet<Integer>();
		try {
			final PreparedStatement ps = cn.prepareStatement("SELECT id FROM empleado WHERE activo = 1");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				activos.add(Integer.valueOf(rs.getInt(1)));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.filtrarSoloActivos: " + e.toString());
		} finally {
			cerrar(cn);
		}
		final java.util.Iterator<NominaEmpleado> it = lista.iterator();
		while (it.hasNext()) {
			if (!activos.contains(Integer.valueOf(it.next().idEmpleado))) {
				it.remove();
			}
		}
	}

	private static void cruzarNombres(final ArrayList<NominaEmpleado> lista) {
		if (lista.isEmpty()) {
			return;
		}
		final Map<Integer, String> nombres = nombresPorId();
		for (final NominaEmpleado n : lista) {
			final String nombre = nombres.get(Integer.valueOf(n.idEmpleado));
			n.nombreEmpleado = nombre == null ? ("Empleado " + n.idEmpleado) : nombre;
		}
	}

	private static void cruzarNombresReparto(final ArrayList<RepartoTienda> lista) {
		if (lista.isEmpty()) {
			return;
		}
		final Map<Integer, String> nombres = nombresPorId();
		for (final RepartoTienda r : lista) {
			final String nombre = nombres.get(Integer.valueOf(r.idEmpleado));
			r.nombreEmpleado = nombre == null ? ("Empleado " + r.idEmpleado) : nombre;
		}
	}

	private static Map<Integer, String> nombresPorId() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		final Map<Integer, String> nombres = new LinkedHashMap<Integer, String>();
		try {
			final PreparedStatement ps = cn.prepareStatement("SELECT id, nombre_largo FROM empleado");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				nombres.put(Integer.valueOf(rs.getInt(1)), rs.getString(2));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO.nombresPorId: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (nombres);
	}

	private static NominaEmpleado leerNomina(final ResultSet rs) throws java.sql.SQLException {
		final NominaEmpleado n = new NominaEmpleado();
		n.idEmpleado = rs.getInt("idempleado");
		n.semana = rs.getString("semana");
		n.sueldoBasico = rs.getDouble("sueldo_basico");
		n.sueldoVariable = rs.getDouble("sueldo_variable");
		n.seguridadSocial = rs.getDouble("seguridad_social");
		n.liquidacion = rs.getDouble("liquidacion");
		n.origen = rs.getString("origen");
		n.usuario = rs.getString("usuario") == null ? "" : rs.getString("usuario");
		return (n);
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleadoDAO: cerrando conexion " + e.toString());
		}
	}
}

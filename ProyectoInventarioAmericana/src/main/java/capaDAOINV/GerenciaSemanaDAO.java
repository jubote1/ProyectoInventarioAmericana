package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * El calendario de semanas de gerencia.
 *
 * QUE PROBLEMA RESUELVE
 *
 * Una semana de lunes a domingo casi nunca cabe dentro de un mes. La del 28 de
 * julio al 3 de agosto tiene cuatro dias en julio y tres en agosto. Si cada
 * reporte decide por su cuenta a que mes pertenece, el mismo mes da dos
 * numeros distintos segun quien lo saque. Se define una vez, queda guardado, y
 * todos los reportes leen de aqui.
 *
 * LA REGLA POR DEFECTO
 *
 * La semana se le carga al mes donde caen la mayoria de sus dias. Siete dias
 * siempre se parten cuatro y tres -o siete y cero-, asi que nunca hay empate y
 * no hace falta desempatar. Es la misma regla que decir "el mes donde cae el
 * jueves", porque el jueves es el dia del medio de una semana que empieza en
 * lunes.
 *
 * La propuesta queda EDITABLE. La regla es un punto de partida, no una orden:
 * si gerencia quiere mover una semana de mes, la mueve.
 *
 * POR QUE UN ANO EMPIEZA DONDE TERMINO EL ANTERIOR
 *
 * Si cada ano se generara por su cuenta desde el lunes de la semana del 1 de
 * enero, dos anos seguidos podrian reclamar el mismo lunes -o dejar una semana
 * sin dueno en el medio-. Por eso, si el ano anterior ya existe, este arranca
 * el dia siguiente a su ultimo domingo. Y la llave unica sobre fecha_inicio
 * hace que, si algo se escapa, el INSERT falle en vez de dejar un dia contado
 * dos veces.
 */
public class GerenciaSemanaDAO {

	/** Una semana del calendario. */
	public static class Semana {
		public int idSemana;
		public int anio;
		public int numero;
		public String fechaInicio = "";
		public String fechaFin = "";
		public int mes;
	}

	/** El calendario guardado de un ano, en orden. */
	public static ArrayList<Semana> obtenerSemanas(final int anio) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<Semana> semanas = new ArrayList<Semana>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idsemana, anio, numero, fecha_inicio, fecha_fin, mes"
					+ " FROM gerencia_semana WHERE anio = ? ORDER BY numero");
			ps.setInt(1, anio);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Semana s = new Semana();
				s.idSemana = rs.getInt("idsemana");
				s.anio = rs.getInt("anio");
				s.numero = rs.getInt("numero");
				s.fechaInicio = rs.getString("fecha_inicio");
				s.fechaFin = rs.getString("fecha_fin");
				s.mes = rs.getInt("mes");
				semanas.add(s);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.obtenerSemanas: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (semanas);
	}

	/**
	 * El domingo con que cerro el ano anterior, o vacio si ese ano no existe.
	 * De ahi arranca la generacion, para que no queden huecos ni traslapes.
	 */
	public static String ultimoDomingoDe(final int anio) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String fecha = "";
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT MAX(fecha_fin) FROM gerencia_semana WHERE anio = ?");
			ps.setInt(1, anio);
			final ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getString(1) != null) {
				fecha = rs.getString(1);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.ultimoDomingoDe: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (fecha);
	}

	/** ABIERTO, CERRADO, o vacio si el ano todavia no se ha guardado. */
	public static String estadoAnio(final int anio) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String estado = "";
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT estado FROM gerencia_anio WHERE anio = ?");
			ps.setInt(1, anio);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				estado = rs.getString(1);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.estadoAnio: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (estado == null ? "" : estado);
	}

	/**
	 * Guarda el calendario completo de un ano, en una sola transaccion.
	 *
	 * Se borra y se vuelve a insertar el ano entero en vez de ir fila por fila
	 * porque el calendario es una sola cosa: un ano al que le falta una semana,
	 * o que tiene dos para el mismo lunes, no sirve para nada. O queda completo
	 * o queda como estaba.
	 *
	 * Devuelve vacio si todo salio bien, o el motivo si no.
	 */
	public static String guardarAnio(final int anio, final ArrayList<Semana> semanas,
			final String usuario) {
		if (semanas == null || semanas.isEmpty()) {
			return ("El calendario llego vacio");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		String error = "";
		try {
			cn.setAutoCommit(false);

			final PreparedStatement borrar = cn.prepareStatement(
					"DELETE FROM gerencia_semana WHERE anio = ?");
			borrar.setInt(1, anio);
			borrar.executeUpdate();
			borrar.close();

			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_semana (anio, numero, fecha_inicio, fecha_fin, mes)"
					+ " VALUES (?, ?, ?, ?, ?)");
			for (int i = 0; i < semanas.size(); i++) {
				final Semana s = semanas.get(i);
				ps.setInt(1, anio);
				ps.setInt(2, s.numero);
				ps.setString(3, s.fechaInicio);
				ps.setString(4, s.fechaFin);
				ps.setInt(5, s.mes);
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();

			//El ano queda registrado como ABIERTO si es la primera vez. Si ya
			//existia se le respeta el estado: guardar no es reabrir.
			final PreparedStatement anioPs = cn.prepareStatement(
					"INSERT INTO gerencia_anio (anio, estado, usuario, fecha_registro)"
					+ " VALUES (?, 'ABIERTO', ?, ?)"
					+ " ON DUPLICATE KEY UPDATE usuario = ?, fecha_registro = ?");
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			anioPs.setInt(1, anio);
			anioPs.setString(2, usuario);
			anioPs.setTimestamp(3, ahora);
			anioPs.setString(4, usuario);
			anioPs.setTimestamp(5, ahora);
			anioPs.executeUpdate();
			anioPs.close();

			cn.commit();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.guardarAnio: " + e.toString());
			//Un lunes repetido lo detiene la llave unica. Se traduce, porque
			//"Duplicate entry" no le dice nada a quien esta en la pantalla.
			if (e.toString().indexOf("uk_gerencia_semana_inicio") >= 0) {
				error = "Hay una semana que ya pertenece a otro ano. "
						+ "Revise el 31 de diciembre y el 1 de enero.";
			} else {
				error = "No se pudo guardar el calendario";
			}
			try {
				cn.rollback();
			} catch (final Exception e1) {
				System.out.println("GerenciaSemanaDAO.guardarAnio rollback: " + e1.toString());
			}
		} finally {
			try {
				if (cn != null) {
					cn.setAutoCommit(true);
				}
			} catch (final Exception e2) {
				System.out.println("GerenciaSemanaDAO.guardarAnio autocommit: " + e2.toString());
			}
			cerrar(cn);
		}
		return (error);
	}

	/** Cierra o reabre un ano. */
	public static boolean cambiarEstadoAnio(final int anio, final String estado, final String usuario) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		boolean listo = false;
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_anio (anio, estado, usuario, fecha_registro)"
					+ " VALUES (?, ?, ?, ?)"
					+ " ON DUPLICATE KEY UPDATE estado = ?, usuario = ?, fecha_registro = ?");
			final Timestamp ahora = new Timestamp(System.currentTimeMillis());
			ps.setInt(1, anio);
			ps.setString(2, estado);
			ps.setString(3, usuario);
			ps.setTimestamp(4, ahora);
			ps.setString(5, estado);
			ps.setString(6, usuario);
			ps.setTimestamp(7, ahora);
			listo = (ps.executeUpdate() > 0);
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.cambiarEstadoAnio: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (listo);
	}

	/** Los anos que ya tienen calendario, del mas nuevo al mas viejo. */
	public static ArrayList<int[]> aniosConCalendario() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		final ArrayList<int[]> anios = new ArrayList<int[]>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT anio, COUNT(*) FROM gerencia_semana GROUP BY anio ORDER BY anio DESC");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				anios.add(new int[] { rs.getInt(1), rs.getInt(2) });
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO.aniosConCalendario: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (anios);
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaSemanaDAO: cerrando conexion " + e.toString());
		}
	}
}

package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import conexionINV.ConexionBaseDatos;

/**
 * Quien puede entrar al menu Gerencia.
 *
 * POR QUE ESTE MENU PIDE IDENTIFICARSE APARTE
 *
 * inventarioamericana.usuario tiene UNA sola fila: el usuario 'admin'. Toda la
 * aplicacion de inventarios, en las once tiendas, entra con ese mismo usuario
 * compartido. La sesion de hoy no dice quien es la persona, dice que alguien
 * sabe la clave de 'admin'.
 *
 * Eso alcanza para despachar inventario. No alcanza para ver nomina. Por eso el
 * menu Gerencia no se apoya en esa sesion: pide identificarse contra
 * pizzaamericana.usuario, que tiene 127 personas de verdad, y el permiso sale
 * del catalogo de seguridad que ya existe -rol, pantalla, rol_pantalla,
 * usuario_rol-, el mismo del central. No se inventa un segundo sistema de
 * perfiles: dos listas de personas es una que se actualiza y otra que se
 * olvida el dia que alguien se va de la empresa.
 *
 * EL PERMISO SE VUELVE A PREGUNTAR EN CADA LLAMADO
 *
 * No basta con esconder el menu. Las pantallas del central se pueden invocar
 * directo por URL, y estas tambien. Por eso cada servlet de Gerencia llama a
 * {@link #tienePermiso} antes de responder nada, y no confia en que la pantalla
 * haya hecho la pregunta. Si a alguien le quitan el rol mientras tiene la
 * sesion abierta, deja de entrar en el siguiente clic.
 *
 * LA CLAVE VIAJA Y SE GUARDA EN TEXTO PLANO EN LA BASE
 *
 * Asi esta hoy pizzaamericana.usuario, y este archivo no lo empeora ni lo
 * arregla: usa PreparedStatement para que al menos no se pueda inyectar SQL, y
 * nunca escribe la clave en el log. Cifrar esas contrasenas es un trabajo
 * aparte que toca el central entero.
 */
public class GerenciaAccesoDAO {

	/** El nombre del rol en pizzaamericana.rol. Lo siembra la migracion. */
	public static final String ROL = "Gerencia";

	/** Lo que se necesita saber de quien entro. */
	public static class Persona {
		public int id;
		public String usuario = "";
		public String nombre = "";
	}

	/**
	 * Valida usuario y clave contra el central y exige que este activo.
	 *
	 * Devuelve null cuando no cuadra. No se distingue "no existe" de "clave
	 * mala" a proposito: decirlo ayuda mas a quien esta adivinando usuarios que
	 * a quien se equivoco de tecla.
	 */
	public static Persona autenticar(final String usuario, final String clave) {
		if (usuario == null || clave == null
				|| usuario.trim().length() == 0 || clave.length() == 0) {
			return (null);
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPedidosLocal();
		Persona persona = null;
		try {
			final String sql = "SELECT id, nombre, nombre_largo FROM usuario"
					+ " WHERE nombre = ? AND password = ? AND activo = 1";
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setString(1, usuario.trim());
			ps.setString(2, clave);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				persona = new Persona();
				persona.id = rs.getInt("id");
				persona.usuario = rs.getString("nombre");
				persona.nombre = rs.getString("nombre_largo");
				if (persona.nombre == null || persona.nombre.trim().length() == 0) {
					persona.nombre = persona.usuario;
				}
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			//La clave no entra al log ni por error: solo se dice que fallo.
			System.out.println("GerenciaAccesoDAO.autenticar: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (persona);
	}

	/**
	 * Si esa persona tiene hoy el rol Gerencia sobre esa pantalla.
	 *
	 * Se pregunta por la pantalla y no solo por el rol para que manana se pueda
	 * dar acceso al calendario sin dar acceso a la nomina, sin tocar codigo.
	 */
	public static boolean tienePermiso(final int idUsuario, final String urlHtml) {
		if (idUsuario <= 0 || urlHtml == null || urlHtml.trim().length() == 0) {
			return (false);
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPedidosLocal();
		boolean puede = false;
		try {
			final String sql = "SELECT COUNT(*) FROM usuario_rol ur"
					+ " JOIN rol r ON r.idrol = ur.idrol AND r.activo = 'S'"
					+ " JOIN rol_pantalla rp ON rp.idrol = r.idrol"
					+ " JOIN pantalla p ON p.idpantalla = rp.idpantalla AND p.activo = 'S'"
					+ " WHERE ur.idusuario = ? AND p.url_html = ?";
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setInt(1, idUsuario);
			ps.setString(2, urlHtml.trim());
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				puede = (rs.getInt(1) > 0);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			//Si la consulta falla NO se deja pasar. Este filtro protege nomina:
			//falla cerrado, al reves del SeguridadFilter del central, que
			//protege pantallas de consulta y ahi si es peor tumbar el sistema.
			System.out.println("GerenciaAccesoDAO.tienePermiso: " + e.toString());
			puede = false;
		} finally {
			cerrar(cn);
		}
		return (puede);
	}

	/** Las pantallas de Gerencia a las que esa persona puede entrar. */
	public static String pantallasDe(final int idUsuario) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPedidosLocal();
		final StringBuilder lista = new StringBuilder();
		try {
			final String sql = "SELECT p.url_html FROM usuario_rol ur"
					+ " JOIN rol r ON r.idrol = ur.idrol AND r.activo = 'S'"
					+ " JOIN rol_pantalla rp ON rp.idrol = r.idrol"
					+ " JOIN pantalla p ON p.idpantalla = rp.idpantalla AND p.activo = 'S'"
					+ " JOIN menu_modulo m ON m.idmodulo = p.idmodulo"
					+ " WHERE ur.idusuario = ? AND m.nombre = ?"
					+ " ORDER BY p.orden";
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setInt(1, idUsuario);
			ps.setString(2, ROL);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (lista.length() > 0) {
					lista.append(",");
				}
				lista.append(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaAccesoDAO.pantallasDe: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista.toString());
	}

	/**
	 * Deja constancia de un cambio.
	 *
	 * Las tablas guardan quien hizo el ultimo cambio; esto guarda los
	 * anteriores. Si falla no se cae la operacion: una bitacora que no se pudo
	 * escribir es un problema, pero perder el dato que el usuario acaba de
	 * guardar es peor.
	 */
	public static void registrar(final String usuario, final String accion, final String detalle) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_bitacora (usuario, accion, detalle, fecha) VALUES (?, ?, ?, ?)");
			ps.setString(1, recortar(usuario, 100));
			ps.setString(2, recortar(accion, 40));
			ps.setString(3, recortar(detalle, 400));
			ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaAccesoDAO.registrar: " + e.toString());
		} finally {
			cerrar(cn);
		}
	}

	private static String recortar(final String texto, final int largo) {
		final String limpio = (texto == null) ? "" : texto;
		return (limpio.length() <= largo ? limpio : limpio.substring(0, largo));
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaAccesoDAO: cerrando conexion " + e.toString());
		}
	}
}

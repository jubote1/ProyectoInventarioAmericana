package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * El maestro de insumos visto completo, para la pantalla de administracion.
 *
 * Va aparte de InsumoDAO a proposito. Esa clase arma objetos Insumo con un
 * constructor de once parametros que usan el alta, la edicion y media docena de
 * pantallas: agregarle campos obliga a tocar todos esos sitios por una columna
 * que solo se lee en una tabla. Aca se leen las columnas de frente y nadie mas
 * se entera.
 *
 * Lo que la pantalla vieja no mostraba y si importa: el grupo de varianza, el
 * umbral de tolerancia, si el insumo maneja canastas y cuanto trae cada una, y
 * sobre todo si el insumo tiene o no varianza registrada. Un insumo sin costo
 * o sin umbral no aparece en ningun informe de perdidas, y hasta ahora no habia
 * donde darse cuenta.
 */
public class InsumoMaestroDAO {

	/** Una fila del maestro, con todo lo que la pantalla muestra. */
	public static class Fila {
		public int idInsumo;
		public String nombre = "";
		public String unidadMedida = "";
		public String categoria = "";
		public String grupoVarianza = "";
		public double costoUnidad;
		public double embalajeCosto;
		public String manejaCanastas = "";
		public int cantidadXCanasta;
		public String controlTienda = "";
		public double umbral;
		public boolean tieneUmbral;
		public int tiendasHomologadas;
	}

	/**
	 * Todo el maestro, ordenado por nombre.
	 *
	 * Son 120 insumos: se traen todos de una y la pantalla filtra en el
	 * navegador, que es mas rapido que ir y volver al servidor por cada letra
	 * que se escriba en el buscador.
	 */
	public static ArrayList<Fila> obtenerMaestro() {
		final ArrayList<Fila> lista = new ArrayList<Fila>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			final String sql =
					"SELECT i.idinsumo, i.nombre_insumo, i.unidad_medida, IFNULL(i.categoria, '') AS categoria,"
					+ "      IFNULL(i.grupo_varianza, '') AS grupo_varianza,"
					+ "      i.costo_unidad, i.embalaje_costo, i.manejacanastas, i.cantidadxcanasta,"
					+ "      IFNULL(i.control_tienda, 'N') AS control_tienda,"
					+ "      d.cantidad AS umbral,"
					+ "      (SELECT COUNT(*) FROM insumo_homologacion_tienda h"
					+ "        WHERE h.idinsumo = i.idinsumo) AS tiendas"
					+ "  FROM insumo i"
					+ "  LEFT JOIN varianza_diferencia d ON d.idinsumo = i.idinsumo"
					+ " ORDER BY i.nombre_insumo";
			final PreparedStatement ps = cn.prepareStatement(sql);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Fila f = new Fila();
				f.idInsumo = rs.getInt("idinsumo");
				f.nombre = texto(rs.getString("nombre_insumo"));
				f.unidadMedida = texto(rs.getString("unidad_medida"));
				f.categoria = texto(rs.getString("categoria"));
				f.grupoVarianza = texto(rs.getString("grupo_varianza"));
				f.costoUnidad = rs.getDouble("costo_unidad");
				f.embalajeCosto = rs.getDouble("embalaje_costo");
				f.manejaCanastas = texto(rs.getString("manejacanastas"));
				f.cantidadXCanasta = rs.getInt("cantidadxcanasta");
				f.controlTienda = texto(rs.getString("control_tienda"));
				f.umbral = rs.getDouble("umbral");
				//getDouble devuelve 0 tanto para un umbral de cero como para uno
				//que no esta configurado: hay que preguntarle al ResultSet.
				f.tieneUmbral = !rs.wasNull();
				f.tiendasHomologadas = rs.getInt("tiendas");
				lista.add(f);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("InsumoMaestroDAO.obtenerMaestro: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	/**
	 * Cambia el grupo de varianza de un insumo.
	 *
	 * Es una operacion propia y no un campo mas de la edicion general porque
	 * esa edicion manda once campos de una: para marcar un insumo como caro
	 * habria que reenviar el costo, la categoria y las canastas, y cualquier
	 * combo que llegue vacio pisaria el valor bueno.
	 *
	 * @param grupo CAROS, CARNES o vacio para quitarle el grupo
	 * @return OK, GRUPOMALO o NOK
	 */
	public static String fijarGrupoVarianza(final int idInsumo, final String grupo) {
		if (idInsumo <= 0) {
			return ("NOK");
		}
		//Lista blanca: lo que llega es texto del navegador y termina en la base.
		final String limpio = grupo == null ? "" : grupo.trim().toUpperCase();
		if (!"".equals(limpio) && !"CAROS".equals(limpio) && !"CARNES".equals(limpio)) {
			return ("GRUPOMALO");
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"UPDATE insumo SET grupo_varianza = ? WHERE idinsumo = ?");
			if ("".equals(limpio)) {
				ps.setNull(1, java.sql.Types.VARCHAR);
			} else {
				ps.setString(1, limpio);
			}
			ps.setInt(2, idInsumo);
			final int filas = ps.executeUpdate();
			ps.close();
			return (filas > 0 ? "OK" : "NOK");
		} catch (final Exception e) {
			System.out.println("InsumoMaestroDAO.fijarGrupoVarianza: " + e.toString());
			return ("NOK");
		} finally {
			cerrar(cn);
		}
	}

	private static String texto(final String valor) {
		return (valor == null ? "" : valor);
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("InsumoMaestroDAO: no cerro la conexion, " + e.toString());
		}
	}
}

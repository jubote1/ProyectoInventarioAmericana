package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * La composicion de los productos: cuanto insumo descuenta cada uno.
 *
 * POR QUE ESTO EXISTE
 *
 * item_inventario_x_producto vive SOLO en cada tienda. Verificado el
 * 2026-09-17 contra las once: no hay dos iguales. Las diferencias no son de
 * forma -en La Mota la Americana Especial XL lleva 100 de pasta contra 70 en
 * Manrique, y 85 de champinon contra 150-.
 *
 * Eso mueve el consumo teorico, que es contra lo que se mide la varianza. Con
 * recetas distintas, comparar la varianza entre tiendas es comparar dos cosas
 * medidas con reglas diferentes.
 *
 * Aca la fuente de verdad es inventarioamericana.producto_insumo, en el
 * central, y a las tiendas se les replica desde ahi.
 *
 * LA LLAVE IMPORTA
 *
 * En la tienda la llave es un autonumerico, y por eso se colaron pares
 * repetidos: Guacamole Kg / Con Guacamole MD esta dos veces -25 y 60- en LAS
 * ONCE tiendas, y el POS recorre todas las filas armando un descuento por
 * cada una, asi que descuenta 85. En el maestro la llave es (idproducto,
 * iditem) y eso no se puede repetir.
 *
 * EL COSTO
 *
 * Misma formula del monitoreo de varianzas: se divide por embalaje_costo y no
 * por mil, porque el queso trae 1.000 pero la carne molida trae 400 y el
 * tomate 500. Si esa formula cambia, cambia en los dos lados o la pantalla de
 * costeo y la de varianza van a decir cosas distintas.
 */
public class ComposicionProductoDAO {

	/** La cantidad llevada a pesos. */
	private static final String VALOR =
			" (pi.cantidad * i.costo_unidad / IF(i.embalaje_costo > 0, i.embalaje_costo, 1)) ";

	// =======================================================================
	// Lo que se devuelve
	// =======================================================================

	/** Un producto del catalogo. */
	public static class Producto {
		public int idProducto;
		public String descripcion = "";
		public String tamano = "";
		public String tipo = "";
		public int insumos;
	}

	/** Una linea de costeo: un insumo dentro de lo que se pidio costear. */
	public static class LineaCosto {
		public int idInsumo;
		public String insumo = "";
		public String unidad = "";
		public double cantidad;
		public double costoUnitario;
		public double costo;
		public boolean sinCosto;
		public boolean sinHomologar;
	}

	/** Una diferencia entre el maestro y una tienda. */
	public static class Diferencia {
		public int idProducto;
		public String producto = "";
		public int idItem;
		public String insumo = "";
		public String tipo = "";
		public double enMaestro;
		public double enTienda;
	}

	// =======================================================================
	// El catalogo
	// =======================================================================

	/** Los productos que tienen composicion, para el selector de la pantalla. */
	public static ArrayList<Producto> obtenerCatalogo(final String filtroTipo) {
		final ArrayList<Producto> lista = new ArrayList<Producto>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			final StringBuilder sql = new StringBuilder();
			sql.append("SELECT c.idproducto, IFNULL(c.descripcion,'') AS descripcion,")
				.append("       IFNULL(c.tamano,'') AS tamano, IFNULL(c.tipo_producto,'') AS tipo,")
				.append("       (SELECT COUNT(*) FROM producto_insumo pi")
				.append("         WHERE pi.idproducto = c.idproducto) AS insumos")
				.append("  FROM producto_catalogo c");
			//Solo los que descuentan algo: un producto sin composicion no se
			//puede costear y en el selector solo estorba.
			sql.append(" WHERE EXISTS (SELECT 1 FROM producto_insumo pi2 WHERE pi2.idproducto = c.idproducto)");
			if (filtroTipo != null && filtroTipo.trim().length() > 0) {
				sql.append(" AND c.tipo_producto = ?");
			}
			sql.append(" ORDER BY c.descripcion");

			final PreparedStatement ps = cn.prepareStatement(sql.toString());
			if (filtroTipo != null && filtroTipo.trim().length() > 0) {
				ps.setString(1, filtroTipo.trim());
			}
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Producto p = new Producto();
				p.idProducto = rs.getInt("idproducto");
				p.descripcion = texto(rs.getString("descripcion"));
				p.tamano = texto(rs.getString("tamano"));
				p.tipo = texto(rs.getString("tipo"));
				p.insumos = rs.getInt("insumos");
				lista.add(p);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.obtenerCatalogo: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	// =======================================================================
	// El costeo
	// =======================================================================

	/**
	 * Costea un conjunto de productos, sumando lo que gastan del mismo insumo.
	 *
	 * Se reciben varios y no uno porque asi se arma un plato: para costear una
	 * Hawaiana extragrande se escoge la base -Pizza XL, que trae la masa y el
	 * queso- y encima el sabor -Hawaiana XL, que trae el jamon y la pina-. Cada
	 * uno es un producto distinto en el POS y ninguno por si solo es la pizza.
	 *
	 * Si dos de los productos escogidos gastan el mismo insumo, las cantidades
	 * se suman en una sola linea: es lo que de verdad sale de la nevera.
	 *
	 * @param idProductos ids separados por coma, tal como llegan de la pantalla
	 */
	public static ArrayList<LineaCosto> costear(final String idProductos) {
		final ArrayList<LineaCosto> lista = new ArrayList<LineaCosto>();
		final String limpios = soloNumerosYComas(idProductos);
		if (limpios.length() == 0) {
			return (lista);
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			//El LEFT JOIN contra el insumo es a proposito: un item sin
			//homologar tiene que APARECER en el costeo, con la cantidad y sin
			//el valor. Con un JOIN normal se caeria de la lista y el total
			//quedaria bajo sin que nadie lo note.
			final String sql =
					"SELECT pi.iditem, IFNULL(i.idinsumo, 0) AS idinsumo,"
					+ "      IFNULL(i.nombre_insumo, CONCAT('Item ', pi.iditem, ' sin homologar')) AS insumo,"
					+ "      IFNULL(i.unidad_medida, '') AS unidad,"
					+ "      SUM(pi.cantidad) AS cantidad,"
					+ "      IFNULL(i.costo_unidad / IF(i.embalaje_costo > 0, i.embalaje_costo, 1), 0) AS unitario,"
					+ "      IFNULL(SUM(" + VALOR + "), 0) AS costo,"
					+ "      IFNULL(i.costo_unidad, 0) AS costo_unidad"
					+ "  FROM producto_insumo pi"
					//La homologacion es por tienda; para costear basta con
					//saber a que insumo corresponde el item, que es igual en
					//todas -verificado: las 1.717 filas son identidad-.
					+ "  LEFT JOIN (SELECT DISTINCT idinsumo, insumotienda"
					+ "               FROM insumo_homologacion_tienda) h ON h.insumotienda = pi.iditem"
					+ "  LEFT JOIN insumo i ON i.idinsumo = h.idinsumo"
					+ " WHERE pi.idproducto IN (" + limpios + ")"
					+ " GROUP BY pi.iditem, i.idinsumo, i.nombre_insumo, i.unidad_medida,"
					+ "          i.costo_unidad, i.embalaje_costo"
					+ " ORDER BY costo DESC";

			final Statement stm = cn.createStatement();
			final ResultSet rs = stm.executeQuery(sql);
			while (rs.next()) {
				final LineaCosto l = new LineaCosto();
				l.idInsumo = rs.getInt("idinsumo");
				l.insumo = texto(rs.getString("insumo"));
				l.unidad = texto(rs.getString("unidad"));
				l.cantidad = rs.getDouble("cantidad");
				l.costoUnitario = rs.getDouble("unitario");
				l.costo = rs.getDouble("costo");
				l.sinHomologar = (l.idInsumo == 0);
				l.sinCosto = !l.sinHomologar && rs.getDouble("costo_unidad") <= 0;
				lista.add(l);
			}
			rs.close();
			stm.close();
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.costear: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	// =======================================================================
	// Traer de una tienda
	// =======================================================================

	/**
	 * Llena el maestro con lo que tiene una tienda.
	 *
	 * Los pares repetidos se resuelven sumando, que es lo que hoy hace el POS:
	 * recorre todas las filas y arma un descuento por cada una, asi que
	 * Guacamole 25 mas 60 descuenta 85. Sumar conserva el comportamiento
	 * actual; quedaria mal callarlo, asi que los repetidos se devuelven en el
	 * detalle para que alguien decida cual era el bueno.
	 *
	 * @return un texto con lo que paso
	 */
	public static String traerDeTienda(final int idTienda, final String host) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cnTienda = null;
		Connection cnCentral = null;
		try {
			cnTienda = con.obtenerConexionBDTiendaRemota(host);
			if (cnTienda == null) {
				return ("NOCONECTA");
			}
			cnCentral = con.obtenerConexionBDPrincipalLocal();

			//1. El catalogo de productos.
			final Statement st1 = cnTienda.createStatement();
			final ResultSet rp = st1.executeQuery(
					"SELECT idproducto, descripcion, tamano, tipo_producto FROM producto");
			final PreparedStatement pc = cnCentral.prepareStatement(
					"INSERT INTO producto_catalogo (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)"
					+ " VALUES (?,?,?,?,?)"
					+ " ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),"
					+ " tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),"
					+ " idtienda_origen = VALUES(idtienda_origen)");
			int productos = 0;
			while (rp.next()) {
				pc.setInt(1, rp.getInt("idproducto"));
				pc.setString(2, rp.getString("descripcion"));
				pc.setString(3, rp.getString("tamano"));
				pc.setString(4, rp.getString("tipo_producto"));
				pc.setInt(5, idTienda);
				pc.addBatch();
				productos++;
				if (productos % 200 == 0) {
					pc.executeBatch();
				}
			}
			pc.executeBatch();
			pc.close();
			rp.close();
			st1.close();

			//2. La composicion, ya agrupada por par para que los repetidos
			//   entren sumados en una sola fila.
			final Statement st2 = cnTienda.createStatement();
			final ResultSet rc = st2.executeQuery(
					"SELECT idproducto, iditem, SUM(cantidad) AS cantidad, COUNT(*) AS veces"
					+ "  FROM item_inventario_x_producto GROUP BY idproducto, iditem");
			final PreparedStatement pi = cnCentral.prepareStatement(
					"INSERT INTO producto_insumo (idproducto, iditem, cantidad) VALUES (?,?,?)"
					+ " ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad)");
			int filas = 0;
			int repetidos = 0;
			while (rc.next()) {
				pi.setInt(1, rc.getInt("idproducto"));
				pi.setInt(2, rc.getInt("iditem"));
				pi.setDouble(3, rc.getDouble("cantidad"));
				pi.addBatch();
				filas++;
				if (rc.getInt("veces") > 1) {
					repetidos++;
				}
				if (filas % 200 == 0) {
					pi.executeBatch();
				}
			}
			pi.executeBatch();
			pi.close();
			rc.close();
			st2.close();

			return ("OK|" + productos + "|" + filas + "|" + repetidos);
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.traerDeTienda: " + e.toString());
			return ("ERROR|" + e.toString());
		} finally {
			cerrar(cnTienda);
			cerrar(cnCentral);
		}
	}

	// =======================================================================
	// Comparar
	// =======================================================================

	/**
	 * Que tiene de distinto una tienda contra el maestro.
	 *
	 * Se lee la tienda entera y se compara en memoria. Son mil doscientas
	 * filas: no vale la pena una tabla temporal, y cruzar dos servidores en
	 * una sola consulta no se puede.
	 */
	public static ArrayList<Diferencia> compararConTienda(final int idTienda, final String host) {
		final ArrayList<Diferencia> diferencias = new ArrayList<Diferencia>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cnTienda = null;
		Connection cnCentral = null;
		try {
			cnTienda = con.obtenerConexionBDTiendaRemota(host);
			if (cnTienda == null) {
				return (diferencias);
			}
			cnCentral = con.obtenerConexionBDPrincipalLocal();

			//Lo de la tienda, agrupado igual que como lo suma el POS.
			final java.util.HashMap<String, Double> enTienda = new java.util.HashMap<String, Double>();
			final Statement st = cnTienda.createStatement();
			final ResultSet rs = st.executeQuery(
					"SELECT idproducto, iditem, SUM(cantidad) AS cantidad"
					+ "  FROM item_inventario_x_producto GROUP BY idproducto, iditem");
			while (rs.next()) {
				enTienda.put(rs.getInt("idproducto") + "_" + rs.getInt("iditem"),
						Double.valueOf(rs.getDouble("cantidad")));
			}
			rs.close();
			st.close();

			//Lo del maestro, con los nombres para que la pantalla se entienda.
			final Statement stc = cnCentral.createStatement();
			final ResultSet rm = stc.executeQuery(
					"SELECT pi.idproducto, pi.iditem, pi.cantidad,"
					+ "      IFNULL(c.descripcion, CONCAT('Producto ', pi.idproducto)) AS producto,"
					+ "      IFNULL(i.nombre_insumo, CONCAT('Item ', pi.iditem)) AS insumo"
					+ "  FROM producto_insumo pi"
					+ "  LEFT JOIN producto_catalogo c ON c.idproducto = pi.idproducto"
					+ "  LEFT JOIN (SELECT DISTINCT idinsumo, insumotienda"
					+ "               FROM insumo_homologacion_tienda) h ON h.insumotienda = pi.iditem"
					+ "  LEFT JOIN insumo i ON i.idinsumo = h.idinsumo"
					+ " ORDER BY producto, insumo");
			while (rm.next()) {
				final String llave = rm.getInt("idproducto") + "_" + rm.getInt("iditem");
				final double enMaestro = rm.getDouble("cantidad");
				final Double valorTienda = enTienda.remove(llave);

				if (valorTienda == null) {
					diferencias.add(armar(rm, "FALTA", enMaestro, 0));
				} else if (Math.abs(valorTienda.doubleValue() - enMaestro) > 0.0001) {
					diferencias.add(armar(rm, "DISTINTA", enMaestro, valorTienda.doubleValue()));
				}
			}
			rm.close();
			stc.close();

			//Lo que quedo en el mapa es lo que la tienda tiene de mas.
			final java.util.Iterator<String> sobrantes = enTienda.keySet().iterator();
			while (sobrantes.hasNext()) {
				final String llave = sobrantes.next();
				final Diferencia d = new Diferencia();
				d.idProducto = Integer.parseInt(llave.substring(0, llave.indexOf('_')));
				d.idItem = Integer.parseInt(llave.substring(llave.indexOf('_') + 1));
				d.producto = "Producto " + d.idProducto;
				d.insumo = "Item " + d.idItem;
				d.tipo = "SOBRA";
				d.enTienda = enTienda.get(llave).doubleValue();
				diferencias.add(d);
			}
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.compararConTienda: " + e.toString());
		} finally {
			cerrar(cnTienda);
			cerrar(cnCentral);
		}
		return (diferencias);
	}

	private static Diferencia armar(final ResultSet rs, final String tipo, final double maestro,
			final double tienda) throws Exception {
		final Diferencia d = new Diferencia();
		d.idProducto = rs.getInt("idproducto");
		d.idItem = rs.getInt("iditem");
		d.producto = texto(rs.getString("producto"));
		d.insumo = texto(rs.getString("insumo"));
		d.tipo = tipo;
		d.enMaestro = maestro;
		d.enTienda = tienda;
		return (d);
	}

	// =======================================================================
	// Replicar
	// =======================================================================

	/**
	 * Manda el maestro a una tienda, dejandola exactamente igual.
	 *
	 * Se hace DELETE y luego INSERT dentro de UNA transaccion. Actualizar fila
	 * por fila dejaria en la tienda lo que el maestro ya no tiene, que es la
	 * mitad del problema que esto viene a resolver.
	 *
	 * Si algo falla a mitad de camino se deshace todo: una tienda con la
	 * composicion a medias descontaria inventario mal en cada venta hasta que
	 * alguien se diera cuenta.
	 *
	 * @return OK|filasAntes|filasDespues, NOCONECTA, VACIO o ERROR|detalle
	 */
	public static String replicarATienda(final int idTienda, final String host) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cnTienda = null;
		Connection cnCentral = null;
		try {
			cnCentral = con.obtenerConexionBDPrincipalLocal();

			//Se lee el maestro ANTES de tocar la tienda. Si el maestro esta
			//vacio, replicar dejaria a la tienda sin composicion: no descuenta
			//nada y el inventario se desvia sin ruido.
			final ArrayList<int[]> pares = new ArrayList<int[]>();
			final ArrayList<Double> cantidades = new ArrayList<Double>();
			final Statement stc = cnCentral.createStatement();
			final ResultSet rm = stc.executeQuery(
					"SELECT idproducto, iditem, cantidad FROM producto_insumo");
			while (rm.next()) {
				pares.add(new int[] {rm.getInt("idproducto"), rm.getInt("iditem")});
				cantidades.add(Double.valueOf(rm.getDouble("cantidad")));
			}
			rm.close();
			stc.close();
			if (pares.isEmpty()) {
				return ("VACIO");
			}

			cnTienda = con.obtenerConexionBDTiendaRemota(host);
			if (cnTienda == null) {
				return ("NOCONECTA");
			}

			final Statement conteo = cnTienda.createStatement();
			final ResultSet rc = conteo.executeQuery(
					"SELECT COUNT(*) AS filas FROM item_inventario_x_producto");
			int antes = 0;
			if (rc.next()) {
				antes = rc.getInt("filas");
			}
			rc.close();
			conteo.close();

			cnTienda.setAutoCommit(false);
			try {
				final Statement borrar = cnTienda.createStatement();
				borrar.executeUpdate("DELETE FROM item_inventario_x_producto");
				borrar.close();

				final PreparedStatement ps = cnTienda.prepareStatement(
						"INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (?,?,?)");
				for (int i = 0; i < pares.size(); i++) {
					ps.setInt(1, pares.get(i)[0]);
					ps.setInt(2, pares.get(i)[1]);
					ps.setDouble(3, cantidades.get(i).doubleValue());
					ps.addBatch();
					if ((i + 1) % 200 == 0) {
						ps.executeBatch();
					}
				}
				ps.executeBatch();
				ps.close();
				cnTienda.commit();
			} catch (final Exception e) {
				cnTienda.rollback();
				throw e;
			} finally {
				cnTienda.setAutoCommit(true);
			}

			return ("OK|" + antes + "|" + pares.size());
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.replicarATienda: " + e.toString());
			return ("ERROR|" + e.toString());
		} finally {
			cerrar(cnTienda);
			cerrar(cnCentral);
		}
	}

	/** Deja constancia de una replica. */
	public static void registrarReplica(final int idTienda, final String usuario,
			final int filasEnviadas, final int filasAntes, final String resultado,
			final String detalle) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO producto_insumo_replica"
					+ " (idtienda, usuario, filas_enviadas, filas_antes, resultado, detalle)"
					+ " VALUES (?,?,?,?,?,?)");
			ps.setInt(1, idTienda);
			ps.setString(2, usuario);
			ps.setInt(3, filasEnviadas);
			ps.setInt(4, filasAntes);
			ps.setString(5, resultado);
			ps.setString(6, detalle == null ? "" : (detalle.length() > 500
					? detalle.substring(0, 500) : detalle));
			ps.executeUpdate();
			ps.close();
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.registrarReplica: " + e.toString());
		} finally {
			cerrar(cn);
		}
	}

	/** Cuantas filas tiene el maestro hoy. */
	public static int filasMaestro() {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		int filas = 0;
		try {
			cn = con.obtenerConexionBDPrincipalLocal();
			final Statement stm = cn.createStatement();
			final ResultSet rs = stm.executeQuery("SELECT COUNT(*) AS filas FROM producto_insumo");
			if (rs.next()) {
				filas = rs.getInt("filas");
			}
			rs.close();
			stm.close();
		} catch (final Exception e) {
			System.out.println("ComposicionProductoDAO.filasMaestro: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (filas);
	}

	// =======================================================================
	// Plomeria
	// =======================================================================

	/** Deja pasar solo digitos y comas: es texto que viene del navegador. */
	private static String soloNumerosYComas(final String texto) {
		if (texto == null) {
			return ("");
		}
		final StringBuilder limpio = new StringBuilder();
		for (int i = 0; i < texto.length(); i++) {
			final char c = texto.charAt(i);
			if ((c >= '0' && c <= '9') || c == ',') {
				limpio.append(c);
			}
		}
		String salida = limpio.toString();
		while (salida.indexOf(",,") >= 0) {
			salida = salida.replace(",,", ",");
		}
		while (salida.startsWith(",")) {
			salida = salida.substring(1);
		}
		while (salida.endsWith(",")) {
			salida = salida.substring(0, salida.length() - 1);
		}
		return (salida);
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
			System.out.println("ComposicionProductoDAO: no cerro la conexion, " + e.toString());
		}
	}
}

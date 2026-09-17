package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

/**
 * Monitoreo de varianzas de inventario, costeado en pesos.
 *
 * DE DONDE SALE LA INFORMACION
 *
 * No hay que salir a consultar las tiendas. ServicioDiarioReplicaVarianza ya
 * trae cada noche la varianza de cada tienda a datamart.varianza_resumen_
 * historico: 283.929 filas, once tiendas, desde 2025-03-19 y al dia. La
 * pantalla lee esa tabla, que esta en el mismo servidor, y por eso responde
 * rapido aunque se pidan meses.
 *
 * Las dos tiendas que faltan -Poblado y Medayoung- no replican porque no
 * tienen host de base de datos configurado en pizzaamericana.tienda. No es
 * algo que esta pantalla pueda arreglar.
 *
 * LA TRAMPA DEL COSTO, QUE ES LA QUE IMPORTA
 *
 * insumo.costo_unidad NO esta en la misma unidad que la varianza. La columna
 * que dice a cuanto equivale es embalaje_costo:
 *
 *   - unidad y paquete: embalaje_costo vale 0 en los 95 insumos que se miden
 *     asi, y el costo es por esa unidad. Masa Grande vale 1.377 y una varianza
 *     de 31 unidades son 42.687 pesos.
 *   - gramos: embalaje_costo dice a cuantos gramos corresponde costo_unidad, y
 *     NO siempre son mil. El queso trae 1.000 -20.200 el kilo-, pero la carne
 *     molida trae 400 -11.047 por 400 gramos, o sea 27.618 el kilo- y el tomate
 *     y la cebolla traen 500.
 *
 * Dividir siempre por mil, que fue lo primero que hice, subvalora la carne
 * molida a menos de la mitad y el tomate a la mitad. Y multiplicar de frente
 * sin dividir da numeros mil veces mas grandes: el queso de un mes solo daria
 * 18.351 millones.
 *
 * La pantalla de edicion de insumos ya dice esto mismo con todas las letras
 * -"Por 400 gramos, el costo es 11.047"-, asi que el dato siempre estuvo ahi.
 *
 * OJO CON Masa Baguette: tiene embalaje_costo = 1, lo que la deja en 1.592 el
 * gramo, o sea 1.592.000 el kilo. Es un error de parametrizacion, no de esta
 * consulta. No se corrige aca a proposito: la formula no debe adivinar sobre
 * datos malos, y el insumo se ve raro en la pantalla para que alguien lo
 * arregle en el maestro.
 *
 * EL SIGNO
 *
 * Varianza negativa es faltante, o sea plata perdida. Positiva es sobrante,
 * que casi nunca es una ganancia real: suele ser un conteo malo o una receta
 * mal parametrizada. Por eso la pantalla los muestra por separado y no solo el
 * neto, que se compensa y esconde el problema: en los ultimos 30 dias el neto
 * fueron 20 millones, pero el faltante bruto fueron 33,7.
 */
public class VarianzaMonitoreoDAO {

	/**
	 * La varianza llevada a pesos.
	 *
	 * Ver arriba por que el divisor es embalaje_costo y no mil. Cuando vale 0
	 * -que es el caso de todo lo que se mide por unidad o por paquete- se
	 * divide por uno. Es el corazon de toda la pantalla, y por eso esta en un
	 * solo lugar y no repetido en cada consulta.
	 */
	private static final String VALOR =
			" (v.varianza * i.costo_unidad / IF(i.embalaje_costo > 0, i.embalaje_costo, 1)) ";

	/** El JOIN comun: de la fila de historia al insumo del maestro. */
	private static final String ORIGEN =
			"   FROM datamart.varianza_resumen_historico v"
			+ "  JOIN inventarioamericana.insumo_homologacion_tienda h"
			+ "    ON h.idtienda = v.idtienda AND h.insumotienda = v.iditem"
			+ "  JOIN inventarioamericana.insumo i ON i.idinsumo = h.idinsumo";

	/** El umbral por insumo, para saber que dia se salio de rango. */
	private static final String UMBRAL =
			"  LEFT JOIN inventarioamericana.varianza_diferencia d ON d.idinsumo = i.idinsumo";

	private static final String FUERA =
			" SUM(IF(d.cantidad IS NOT NULL AND ABS(v.varianza) > d.cantidad, 1, 0)) AS fuera";

	// =======================================================================
	// Lo que devuelve cada consulta
	// =======================================================================

	/** Una linea de cualquiera de los cortes: por tienda, por insumo o por dia. */
	public static class Linea {
		public int id;
		public String etiqueta = "";
		public String etiqueta2 = "";
		public String unidad = "";
		public String grupo = "";
		public double cantidad;
		public double valor;
		public double perdida;
		public double sobrante;
		public int dias;
		public int diasFueraUmbral;
	}

	/** Los totales de arriba. */
	public static class Resumen {
		public double valor;
		public double perdida;
		public double sobrante;
		public int tiendas;
		public int insumos;
		public int dias;
		public int diasFueraUmbral;
	}

	// =======================================================================
	// El filtro
	// =======================================================================

	/**
	 * Arma las condiciones comunes a todas las consultas.
	 *
	 * Las tiendas llegan como una lista separada por comas desde la pantalla.
	 * Se limpia dejando solo digitos y comas antes de meterla en el SQL: es
	 * texto que viene del navegador y no puede entrar crudo a la consulta. El
	 * grupo se compara contra una lista blanca por la misma razon.
	 */
	private static String filtro(final String tiendas, final String grupo, final int idInsumo) {
		final StringBuilder w = new StringBuilder();
		w.append(" WHERE v.fecha BETWEEN ? AND ? ");

		final String limpias = soloNumerosYComas(tiendas);
		if (limpias.length() > 0) {
			w.append(" AND v.idtienda IN (").append(limpias).append(") ");
		}
		if ("CAROS".equals(grupo) || "CARNES".equals(grupo)) {
			w.append(" AND i.grupo_varianza = '").append(grupo).append("' ");
		} else if ("SINGRUPO".equals(grupo)) {
			w.append(" AND i.grupo_varianza IS NULL ");
		}
		if (idInsumo > 0) {
			w.append(" AND i.idinsumo = ").append(idInsumo).append(" ");
		}
		return (w.toString());
	}

	/** Deja pasar solo digitos y comas, y descarta las comas sobrantes. */
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
		//Una lista como "1,,2" o ",1," la rechaza MySQL.
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

	// =======================================================================
	// Las consultas
	// =======================================================================

	/** Los totales del periodo, que son las tarjetas de arriba. */
	public static Resumen obtenerResumen(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {
		final Resumen r = new Resumen();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final String sql =
					"SELECT ROUND(SUM(" + VALOR + ")) AS valor,"
					+ "      ROUND(SUM(IF(v.varianza < 0," + VALOR + ", 0))) AS perdida,"
					+ "      ROUND(SUM(IF(v.varianza > 0," + VALOR + ", 0))) AS sobrante,"
					+ "      COUNT(DISTINCT v.idtienda) AS tiendas,"
					+ "      COUNT(DISTINCT i.idinsumo) AS insumos,"
					+ "      COUNT(DISTINCT v.fecha) AS dias,"
					+ FUERA
					+ ORIGEN
					+ UMBRAL
					+ filtro(tiendas, grupo, idInsumo);

			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setString(1, fechaDesde);
			ps.setString(2, fechaHasta);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				r.valor = rs.getDouble("valor");
				r.perdida = rs.getDouble("perdida");
				r.sobrante = rs.getDouble("sobrante");
				r.tiendas = rs.getInt("tiendas");
				r.insumos = rs.getInt("insumos");
				r.dias = rs.getInt("dias");
				r.diasFueraUmbral = rs.getInt("fuera");
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("VarianzaMonitoreoDAO.obtenerResumen: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (r);
	}

	/** El corte por tienda, que es con el que se comparan entre ellas. */
	public static ArrayList<Linea> obtenerPorTienda(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {
		final String sql =
				"SELECT v.idtienda AS id, IFNULL(t.nombre, CONCAT('Tienda ', v.idtienda)) AS etiqueta,"
				+ "      '' AS etiqueta2, '' AS unidad, '' AS grupo,"
				+ "      SUM(v.varianza) AS cantidad,"
				+ "      ROUND(SUM(" + VALOR + ")) AS valor,"
				+ "      ROUND(SUM(IF(v.varianza < 0," + VALOR + ", 0))) AS perdida,"
				+ "      ROUND(SUM(IF(v.varianza > 0," + VALOR + ", 0))) AS sobrante,"
				+ "      COUNT(DISTINCT v.fecha) AS dias,"
				+ FUERA
				+ ORIGEN
				+ UMBRAL
				+ "  LEFT JOIN pizzaamericana.tienda t ON t.idtienda = v.idtienda"
				+ filtro(tiendas, grupo, idInsumo)
				+ " GROUP BY v.idtienda, t.nombre ORDER BY valor ASC";
		return (consultar(sql, fechaDesde, fechaHasta, "obtenerPorTienda"));
	}

	/** El corte por insumo: por donde se esta yendo la plata. */
	public static ArrayList<Linea> obtenerPorInsumo(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {
		final String sql =
				"SELECT i.idinsumo AS id, i.nombre_insumo AS etiqueta,"
				+ "      IFNULL(i.categoria, '') AS etiqueta2, i.unidad_medida AS unidad,"
				+ "      IFNULL(i.grupo_varianza, '') AS grupo,"
				+ "      SUM(v.varianza) AS cantidad,"
				+ "      ROUND(SUM(" + VALOR + ")) AS valor,"
				+ "      ROUND(SUM(IF(v.varianza < 0," + VALOR + ", 0))) AS perdida,"
				+ "      ROUND(SUM(IF(v.varianza > 0," + VALOR + ", 0))) AS sobrante,"
				+ "      COUNT(DISTINCT v.fecha) AS dias,"
				+ FUERA
				+ ORIGEN
				+ UMBRAL
				+ filtro(tiendas, grupo, idInsumo)
				+ " GROUP BY i.idinsumo, i.nombre_insumo, i.categoria, i.unidad_medida, i.grupo_varianza"
				+ " ORDER BY valor ASC";
		return (consultar(sql, fechaDesde, fechaHasta, "obtenerPorInsumo"));
	}

	/** La serie diaria, para ver si esto viene mejorando o empeorando. */
	public static ArrayList<Linea> obtenerPorDia(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {
		final String sql =
				"SELECT 0 AS id, DATE_FORMAT(v.fecha, '%Y-%m-%d') AS etiqueta,"
				+ "      '' AS etiqueta2, '' AS unidad, '' AS grupo,"
				+ "      SUM(v.varianza) AS cantidad,"
				+ "      ROUND(SUM(" + VALOR + ")) AS valor,"
				+ "      ROUND(SUM(IF(v.varianza < 0," + VALOR + ", 0))) AS perdida,"
				+ "      ROUND(SUM(IF(v.varianza > 0," + VALOR + ", 0))) AS sobrante,"
				+ "      1 AS dias, 0 AS fuera"
				+ ORIGEN
				+ filtro(tiendas, grupo, idInsumo)
				+ " GROUP BY v.fecha ORDER BY v.fecha ASC";
		return (consultar(sql, fechaDesde, fechaHasta, "obtenerPorDia"));
	}

	/**
	 * El detalle: cada cruce de insumo con tienda.
	 *
	 * Se limita a 500 lineas. Sin tope, pedir un ano de las once tiendas
	 * devuelve miles de filas que ni el navegador ni quien mira alcanzan a
	 * usar. Van ordenadas por perdida, asi que las primeras son las que
	 * importan.
	 */
	public static ArrayList<Linea> obtenerDetalle(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {
		final String sql =
				"SELECT i.idinsumo AS id, i.nombre_insumo AS etiqueta,"
				+ "      IFNULL(t.nombre, CONCAT('Tienda ', v.idtienda)) AS etiqueta2,"
				+ "      i.unidad_medida AS unidad, IFNULL(i.grupo_varianza, '') AS grupo,"
				+ "      SUM(v.varianza) AS cantidad,"
				+ "      ROUND(SUM(" + VALOR + ")) AS valor,"
				+ "      ROUND(SUM(IF(v.varianza < 0," + VALOR + ", 0))) AS perdida,"
				+ "      ROUND(SUM(IF(v.varianza > 0," + VALOR + ", 0))) AS sobrante,"
				+ "      COUNT(DISTINCT v.fecha) AS dias,"
				+ FUERA
				+ ORIGEN
				+ UMBRAL
				+ "  LEFT JOIN pizzaamericana.tienda t ON t.idtienda = v.idtienda"
				+ filtro(tiendas, grupo, idInsumo)
				+ " GROUP BY i.idinsumo, i.nombre_insumo, t.nombre, v.idtienda, i.unidad_medida, i.grupo_varianza"
				+ " ORDER BY valor ASC LIMIT 500";
		return (consultar(sql, fechaDesde, fechaHasta, "obtenerDetalle"));
	}

	/**
	 * Los insumos que se pueden escoger en el filtro.
	 *
	 * Solo los que de verdad tienen varianza guardada. La tabla de insumos
	 * tiene 120 y la varianza se lleva de 54: llenar el combo con los 120
	 * dejaria 66 opciones que siempre devuelven la pantalla vacia.
	 */
	public static ArrayList<Linea> obtenerInsumosConVarianza() {
		final ArrayList<Linea> lista = new ArrayList<Linea>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final String sql =
					"SELECT DISTINCT i.idinsumo AS id, i.nombre_insumo AS etiqueta,"
					+ "      i.unidad_medida AS unidad, IFNULL(i.grupo_varianza, '') AS grupo"
					+ "  FROM inventarioamericana.insumo i"
					+ "  JOIN inventarioamericana.insumo_homologacion_tienda h ON h.idinsumo = i.idinsumo"
					+ " WHERE EXISTS (SELECT 1 FROM datamart.varianza_resumen_historico v"
					+ "                WHERE v.idtienda = h.idtienda AND v.iditem = h.insumotienda)"
					+ " ORDER BY i.nombre_insumo";
			final PreparedStatement ps = cn.prepareStatement(sql);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Linea l = new Linea();
				l.id = rs.getInt("id");
				l.etiqueta = texto(rs.getString("etiqueta"));
				l.unidad = texto(rs.getString("unidad"));
				l.grupo = texto(rs.getString("grupo"));
				lista.add(l);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("VarianzaMonitoreoDAO.obtenerInsumosConVarianza: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	// =======================================================================
	// Plomeria
	// =======================================================================

	private static ArrayList<Linea> consultar(final String sql, final String fechaDesde,
			final String fechaHasta, final String desde) {
		final ArrayList<Linea> lista = new ArrayList<Linea>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		Connection cn = null;
		try {
			cn = con.obtenerConexionBDDatamartLocal();
			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setString(1, fechaDesde);
			ps.setString(2, fechaHasta);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Linea l = new Linea();
				l.id = rs.getInt("id");
				l.etiqueta = texto(rs.getString("etiqueta"));
				l.etiqueta2 = texto(rs.getString("etiqueta2"));
				l.unidad = texto(rs.getString("unidad"));
				l.grupo = texto(rs.getString("grupo"));
				l.cantidad = rs.getDouble("cantidad");
				l.valor = rs.getDouble("valor");
				l.perdida = rs.getDouble("perdida");
				l.sobrante = rs.getDouble("sobrante");
				l.dias = rs.getInt("dias");
				l.diasFueraUmbral = rs.getInt("fuera");
				lista.add(l);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("VarianzaMonitoreoDAO." + desde + ": " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
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
			System.out.println("VarianzaMonitoreoDAO: no cerro la conexion, " + e.toString());
		}
	}
}

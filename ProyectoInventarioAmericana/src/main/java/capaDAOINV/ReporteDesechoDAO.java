package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import conexionINV.ConexionBaseDatos;

/**
 * Reporte de desechos y devoluciones.
 *
 * Reemplaza el libro de Excel que se llevaba a mano, con una hoja por tienda y
 * una matriz mensual. Ese libro se alimentaba transcribiendo los registros del
 * sistema, con dos problemas: se dejo de actualizar despues de enero, y las
 * transcripciones se desviaban. Por ejemplo el desecho 8062 quedo anotado como
 * "MEDIANA ESTOFADA ELABORADA" por 13.000 pesos cuando en la base es "Masa
 * Mediana" por 892.
 *
 * Sobre el costeo: la columna desecho.tipo dice como se cobra cada tipo de
 * desecho. 'G' es por gramos, que es el caso de los ingredientes y de las masas
 * adelantadas, y 'C' es por unidad, que es el caso de las pizzas y las masas
 * porcionadas. El reporte semanal de Servicios no usa esa columna: adivina
 * mirando cual de los dos campos viene lleno. Hoy acierta porque en la practica
 * se llena el campo que corresponde, pero aqui se usa el tipo, que es el dato
 * que de verdad manda.
 */
public class ReporteDesechoDAO {

	/**
	 * Clasificacion en las categorias del reporte. El orden de los WHEN importa
	 * y no se puede reacomodar:
	 *
	 * - 'Masa%' va primero, porque si no "Masa Grande" caeria en GRANDE cuando
	 *   es masa cruda, no una pizza armada.
	 * - EXTRAGRANDE va antes que GRANDE, porque la palabra "Extragrande"
	 *   contiene "grande" y quedaria contada como pizza grande.
	 * - El tipo 20, "Elaborada PlataformaDIDI", no dice el tamano en su nombre.
	 *   Vale 17.000, o sea una grande, y con 281 usos es el tercero mas
	 *   frecuente, asi que se clasifica de forma explicita para que no caiga en
	 *   OTROS.
	 *
	 * Lo que no reconoce queda en OTROS a proposito, visible en el reporte. Si
	 * manana agregan un tipo nuevo se va a ver ahi, en vez de desaparecer.
	 */
	private static final String CATEGORIA =
			" CASE"
			+ "  WHEN d.iddesecho = 20 THEN 'PIZZA GRANDE'"
			+ "  WHEN d.descripcion LIKE 'Masa%' THEN 'MASAS'"
			+ "  WHEN d.descripcion LIKE 'Porci%' THEN 'PORCION'"
			+ "  WHEN d.descripcion LIKE '%Pizzeta%' THEN 'PIZZETA'"
			+ "  WHEN d.descripcion LIKE '%Extragrande%' THEN 'PIZZA EXTRAGRANDE'"
			+ "  WHEN d.descripcion LIKE '%Mediana%' THEN 'PIZZA MEDIANA'"
			+ "  WHEN d.descripcion LIKE '%Grande%' THEN 'PIZZA GRANDE'"
			+ "  WHEN d.descripcion LIKE '%Dedito%' THEN 'DEDITOS'"
			+ "  WHEN d.descripcion LIKE '%Nugget%' THEN 'NUGGETS'"
			+ "  WHEN d.descripcion LIKE '%Lasa%' THEN 'LASANA'"
			+ "  WHEN d.descripcion LIKE '%Maduro%' THEN 'MADURO'"
			+ "  WHEN d.descripcion LIKE '%Hamburguesa%' THEN 'HAMBURGUESA'"
			+ "  WHEN d.tipo = 'G' THEN 'INGREDIENTES'"
			+ "  ELSE 'OTROS'"
			+ " END";

	/**
	 * De donde viene el desecho. La matriz del Excel separaba punto de venta de
	 * DIDI; aqui se agrega PLATAFORMA para los tipos de otras plataformas, que
	 * en el libro quedaban mezclados con el punto de venta.
	 */
	private static final String ORIGEN =
			" CASE"
			+ "  WHEN d.descripcion LIKE '%DIDI%' THEN 'DIDI'"
			+ "  WHEN d.descripcion LIKE '%Plataforma%' THEN 'PLATAFORMA'"
			+ "  ELSE 'PUNTO DE VENTA'"
			+ " END";

	/** El costo depende del tipo: por gramos o por unidad. */
	private static final String COSTO =
			" IF(d.tipo = 'G', IFNULL(dt.gramos,0) * d.costo, IFNULL(dt.cantidad,0) * d.costo)";

	/** Las unidades tambien: gramos para los tipo G, cantidad para los tipo C. */
	private static final String UNIDADES =
			" IF(d.tipo = 'G', IFNULL(dt.gramos,0), IFNULL(dt.cantidad,0))";

	/** Una celda de la matriz: un mes, una tienda, una categoria. */
	public static class Celda {
		public int mes = 0;

		public int idTienda = 0;

		public String tienda = "";

		public String categoria = "";

		public String origen = "";

		public double unidades = 0;

		public double costo = 0;

		public int registros = 0;
	}

	/** Una linea del detalle, equivalente a una fila de las hojas por tienda. */
	public static class Linea {
		public int idDesechoTienda = 0;

		public String fecha = "";

		public int mes = 0;

		public int semana = 0;

		public int idTienda = 0;

		public String tienda = "";

		public String producto = "";

		public String categoria = "";

		public String origen = "";

		public String destino = "";

		public String motivo = "";

		public String descripcion = "";

		public String tipo = "";

		public double gramos = 0;

		public double cantidad = 0;

		public double costo = 0;

		public String usuario = "";

		public String estado = "";

		public String fechaCarro = "";

		public String fechaBodega = "";
	}

	/**
	 * Matriz de desechos por mes, tienda y categoria.
	 *
	 * @param anio     ano a consultar
	 * @param mes      mes especifico, o 0 para todo el ano
	 * @param idTienda tienda especifica, o 0 para todas
	 */
	public static ArrayList<Celda> obtenerMatriz(int anio, int mes, int idTienda) {
		Logger logger = Logger.getLogger("log_file");
		ArrayList<Celda> celdas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		if (con1 == null) {
			return (celdas);
		}
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select MONTH(dt.fecha) as mes, dt.idtienda, IFNULL(t.nombre,'') as tienda,");
			sql.append(CATEGORIA).append(" as categoria,");
			sql.append(ORIGEN).append(" as origen,");
			sql.append(" SUM(").append(UNIDADES).append(") as unidades,");
			sql.append(" SUM(").append(COSTO).append(") as costo,");
			sql.append(" COUNT(*) as registros");
			sql.append(" from desecho_tienda dt");
			sql.append(" join desecho d on d.iddesecho = dt.iddesecho");
			sql.append(" left join tienda t on t.idtienda = dt.idtienda");
			sql.append(" where YEAR(dt.fecha) = ?");
			if (mes > 0) {
				sql.append(" and MONTH(dt.fecha) = ?");
			}
			if (idTienda > 0) {
				sql.append(" and dt.idtienda = ?");
			}
			sql.append(" group by mes, dt.idtienda, tienda, categoria, origen");
			sql.append(" order by mes, tienda, categoria");

			PreparedStatement pst = con1.prepareStatement(sql.toString());
			int p = 1;
			pst.setInt(p++, anio);
			if (mes > 0) {
				pst.setInt(p++, mes);
			}
			if (idTienda > 0) {
				pst.setInt(p++, idTienda);
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Celda celda = new Celda();
				celda.mes = rs.getInt("mes");
				celda.idTienda = rs.getInt("idtienda");
				celda.tienda = rs.getString("tienda");
				celda.categoria = rs.getString("categoria");
				celda.origen = rs.getString("origen");
				celda.unidades = rs.getDouble("unidades");
				celda.costo = rs.getDouble("costo");
				celda.registros = rs.getInt("registros");
				celdas.add(celda);
			}
			rs.close();
			pst.close();
			con1.close();
		} catch (Exception e) {
			logger.error("obtenerMatriz: " + e.toString());
			System.out.println("obtenerMatriz: " + e.toString());
			try {
				con1.close();
			} catch (Exception e1) {
			}
		}
		return (celdas);
	}

	/**
	 * Detalle de los desechos, una linea por registro.
	 *
	 * Trae las fechas de recibido en carro y en bodega desde
	 * cambio_estado_aprovechable, que es la cadena de custodia del aprovechable.
	 *
	 * @param fechaDesde formato aaaa-mm-dd
	 * @param fechaHasta formato aaaa-mm-dd
	 * @param idTienda   tienda especifica, o 0 para todas
	 */
	public static ArrayList<Linea> obtenerDetalle(String fechaDesde, String fechaHasta, int idTienda) {
		Logger logger = Logger.getLogger("log_file");
		ArrayList<Linea> lineas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		if (con1 == null) {
			return (lineas);
		}
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select dt.iddesecho_tienda, dt.fecha, MONTH(dt.fecha) as mes,");
			// La semana del mes, como en el libro: 1RA SEMANA, 2DA SEMANA...
			sql.append(" FLOOR((DAYOFMONTH(dt.fecha) - 1) / 7) + 1 as semana,");
			sql.append(" dt.idtienda, IFNULL(t.nombre,'') as tienda,");
			sql.append(" d.descripcion as producto,");
			sql.append(CATEGORIA).append(" as categoria,");
			sql.append(ORIGEN).append(" as origen,");
			sql.append(" d.destino, d.tipo,");
			sql.append(" IFNULL(dt.motivo,'') as motivo, IFNULL(dt.descripcion,'') as descripcion,");
			sql.append(" IFNULL(dt.gramos,0) as gramos, IFNULL(dt.cantidad,0) as cantidad,");
			sql.append(COSTO).append(" as costo,");
			sql.append(" IFNULL(dt.usuario,'') as usuario, IFNULL(e.nombre,'') as estado,");
			sql.append(" (select MIN(c.fecha_cambio) from cambio_estado_aprovechable c");
			sql.append("   where c.idaprovechable = dt.iddesecho_tienda and c.idestado = 2) as fechacarro,");
			sql.append(" (select MIN(c.fecha_cambio) from cambio_estado_aprovechable c");
			sql.append("   where c.idaprovechable = dt.iddesecho_tienda and c.idestado = 3) as fechabodega");
			sql.append(" from desecho_tienda dt");
			sql.append(" join desecho d on d.iddesecho = dt.iddesecho");
			sql.append(" left join tienda t on t.idtienda = dt.idtienda");
			sql.append(" left join estado_aprovechable e on e.idestado = dt.idestado");
			sql.append(" where dt.fecha between ? and ?");
			if (idTienda > 0) {
				sql.append(" and dt.idtienda = ?");
			}
			sql.append(" order by dt.idtienda, dt.fecha, dt.iddesecho_tienda");

			PreparedStatement pst = con1.prepareStatement(sql.toString());
			pst.setString(1, fechaDesde);
			pst.setString(2, fechaHasta);
			if (idTienda > 0) {
				pst.setInt(3, idTienda);
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Linea linea = new Linea();
				linea.idDesechoTienda = rs.getInt("iddesecho_tienda");
				linea.fecha = rs.getString("fecha") == null ? "" : rs.getString("fecha");
				linea.mes = rs.getInt("mes");
				linea.semana = rs.getInt("semana");
				linea.idTienda = rs.getInt("idtienda");
				linea.tienda = rs.getString("tienda");
				linea.producto = rs.getString("producto") == null ? "" : rs.getString("producto");
				linea.categoria = rs.getString("categoria");
				linea.origen = rs.getString("origen");
				linea.destino = rs.getString("destino") == null ? "" : rs.getString("destino");
				linea.tipo = rs.getString("tipo") == null ? "" : rs.getString("tipo");
				linea.motivo = rs.getString("motivo");
				linea.descripcion = rs.getString("descripcion");
				linea.gramos = rs.getDouble("gramos");
				linea.cantidad = rs.getDouble("cantidad");
				linea.costo = rs.getDouble("costo");
				linea.usuario = rs.getString("usuario");
				linea.estado = rs.getString("estado");
				linea.fechaCarro = rs.getString("fechacarro") == null ? "" : rs.getString("fechacarro");
				linea.fechaBodega = rs.getString("fechabodega") == null ? "" : rs.getString("fechabodega");
				lineas.add(linea);
			}
			rs.close();
			pst.close();
			con1.close();
		} catch (Exception e) {
			logger.error("obtenerDetalle: " + e.toString());
			System.out.println("obtenerDetalle: " + e.toString());
			try {
				con1.close();
			} catch (Exception e1) {
			}
		}
		return (lineas);
	}

	/** Los anos que tienen desechos registrados, para llenar el filtro. */
	public static ArrayList<Integer> obtenerAnios() {
		Logger logger = Logger.getLogger("log_file");
		ArrayList<Integer> anios = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		if (con1 == null) {
			return (anios);
		}
		try {
			// Se acota desde 2020: hay un registro con fecha de 1904, claramente un
			// error de digitacion, y no tiene sentido ofrecerlo en el filtro.
			PreparedStatement pst = con1.prepareStatement("select distinct YEAR(fecha) as anio from desecho_tienda"
					+ " where YEAR(fecha) >= 2020 order by anio desc");
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				anios.add(Integer.valueOf(rs.getInt("anio")));
			}
			rs.close();
			pst.close();
			con1.close();
		} catch (Exception e) {
			logger.error("obtenerAnios: " + e.toString());
			try {
				con1.close();
			} catch (Exception e1) {
			}
		}
		return (anios);
	}
}

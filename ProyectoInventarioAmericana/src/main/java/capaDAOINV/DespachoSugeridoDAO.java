package capaDAOINV;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import conexionINV.ConexionBaseDatos;

/**
 * Motor de despacho inteligente basado en el aprendizaje del histórico de despachos
 * reales de las últimas 4 semanas por tienda.
 *
 * Mantiene un control de vigencia (28 días / 4 semanas). Cuando se vence o cuando
 * el usuario lo solicita, recalcula el promedio real despachado de cada insumo,
 * ajustando automáticamente los empaques/canastas estándar.
 */
public class DespachoSugeridoDAO {

	private static final Logger logger = Logger.getLogger("log_file");
	public static final int DIAS_VIGENCIA_DEFECTO = 28; // 4 semanas

	public static class EstadoControl {
		public int idTienda;
		public boolean tieneControl;
		public long diasDesdeUltimaActualizacion;
		public boolean requiereActualizar;
		public String fechaUltimaActualizacion = "";
		public int diasVigencia = DIAS_VIGENCIA_DEFECTO;
		public String fechaDesde = "";
		public String fechaHasta = "";
		public int totalDespachosAnalizados;
		public String usuarioActualizo = "";
	}

	public static class InsumoSugeridoDTO {
		public int idInsumo;
		public String nombreInsumo = "";
		public String unidadMedida = "";
		public int idCategoria;
		public String nombreCategoria = "";
		public String colorCategoria = "#E2E8F0";
		public String manejaCanastas = "N";
		public int cantidadXCanasta;
		public String nombreContenedor = "";
		public double promedio4Sem;
		public double total4Sem;
		public int vecesDespachado;
		public double cantidadSugerida;
		public int canastasSugeridas;
		public double cantidadTienda;
		public double cantidadLlevar;
		public int orden;
	}

	public static class ResultadoActualizacion {
		public boolean ok;
		public String mensaje = "";
		public int insumosCalculados;
		public int despachosAnalizados;
		public String fechaDesde = "";
		public String fechaHasta = "";
	}

	/**
	 * Consulta el estado de vigencia del sugerido de 4 semanas para una tienda.
	 */
	public static EstadoControl consultarControl(final int idTienda) {
		final EstadoControl est = new EstadoControl();
		est.idTienda = idTienda;
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			est.requiereActualizar = true;
			return est;
		}

		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtienda, fecha_ultima_actualizacion, dias_vigencia, fecha_desde_analisis, "
					+ "fecha_hasta_analisis, total_despachos_analizados, usuario_actualizo "
					+ "FROM despacho_sugerido_control WHERE idtienda = ?");
			ps.setInt(1, idTienda);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				est.tieneControl = true;
				final Timestamp ts = rs.getTimestamp("fecha_ultima_actualizacion");
				if (ts != null) {
					final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					est.fechaUltimaActualizacion = sdf.format(ts);
					final long diffMs = System.currentTimeMillis() - ts.getTime();
					est.diasDesdeUltimaActualizacion = Math.max(0, diffMs / (24L * 3600L * 1000L));
				}
				est.diasVigencia = rs.getInt("dias_vigencia");
				final Date d1 = rs.getDate("fecha_desde_analisis");
				final Date d2 = rs.getDate("fecha_hasta_analisis");
				if (d1 != null) est.fechaDesde = d1.toString();
				if (d2 != null) est.fechaHasta = d2.toString();
				est.totalDespachosAnalizados = rs.getInt("total_despachos_analizados");
				est.usuarioActualizo = rs.getString("usuario_actualizo");
				est.requiereActualizar = (est.diasDesdeUltimaActualizacion >= est.diasVigencia);
			} else {
				est.tieneControl = false;
				est.requiereActualizar = true;
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			logger.error("DespachoSugeridoDAO.consultarControl: " + e.toString());
			est.requiereActualizar = true;
		} finally {
			cerrar(cn);
		}
		return est;
	}

	/**
	 * Recalcula el inventario sugerido aprendiendo del histórico de las últimas 4 semanas
	 * de la tienda especificada y actualiza las tablas de control y detalle.
	 */
	public static ResultadoActualizacion recalcularSugerido4Semanas(final int idTienda, final String usuario) {
		final ResultadoActualizacion res = new ResultadoActualizacion();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			res.ok = false;
			res.mensaje = "No se pudo conectar a la base de datos";
			return res;
		}

		try {
			// 1. Determinar fecha final de la ventana: fecha más reciente registrada para la tienda o hoy
			String fechaHasta = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
			final PreparedStatement psMax = cn.prepareStatement(
					"SELECT MAX(fecha_despacho) FROM insumo_despacho_tienda WHERE idtienda = ?");
			psMax.setInt(1, idTienda);
			final ResultSet rsMax = psMax.executeQuery();
			if (rsMax.next() && rsMax.getString(1) != null) {
				fechaHasta = rsMax.getString(1);
			}
			rsMax.close();
			psMax.close();

			// 28 días hacia atrás
			final Date dHasta = Date.valueOf(fechaHasta);
			final Date dDesde = new Date(dHasta.getTime() - (28L * 24L * 3600L * 1000L));
			final String fechaDesde = dDesde.toString();

			res.fechaDesde = fechaDesde;
			res.fechaHasta = fechaHasta;

			// 2. Total de despachos en la ventana
			final PreparedStatement psTot = cn.prepareStatement(
					"SELECT COUNT(DISTINCT iddespacho) FROM insumo_despacho_tienda "
					+ "WHERE idtienda = ? AND fecha_despacho >= ? AND fecha_despacho <= ?");
			psTot.setInt(1, idTienda);
			psTot.setString(2, fechaDesde);
			psTot.setString(3, fechaHasta);
			final ResultSet rsTot = psTot.executeQuery();
			int totalDespachos = 0;
			if (rsTot.next()) {
				totalDespachos = rsTot.getInt(1);
			}
			rsTot.close();
			psTot.close();

			res.despachosAnalizados = totalDespachos;

			// 3. Consultar histórico por insumo
			final PreparedStatement psInsumos = cn.prepareStatement(
					"SELECT dt.idinsumo, IFNULL(i.manejacanastas,'N') as manejacanastas, "
					+ "IFNULL(i.cantidadxcanasta, 0) as cantidadxcanasta, "
					+ "COUNT(DISTINCT d.iddespacho) as veces, SUM(dt.cantidad) as total "
					+ "FROM insumo_despacho_tienda d "
					+ "JOIN insumo_despacho_tienda_detalle dt ON dt.iddespacho = d.iddespacho "
					+ "JOIN insumo i ON i.idinsumo = dt.idinsumo "
					+ "WHERE d.idtienda = ? AND d.fecha_despacho >= ? AND d.fecha_despacho <= ? "
					+ "AND dt.cantidad > 0 "
					+ "GROUP BY dt.idinsumo, i.manejacanastas, i.cantidadxcanasta");
			psInsumos.setInt(1, idTienda);
			psInsumos.setString(2, fechaDesde);
			psInsumos.setString(3, fechaHasta);
			final ResultSet rsIns = psInsumos.executeQuery();

			// Preparamos el upsert en despacho_sugerido_detalle
			final PreparedStatement psUpsert = cn.prepareStatement(
					"INSERT INTO despacho_sugerido_detalle "
					+ "(idtienda, idinsumo, promedio_despachado, total_despachado_4sem, veces_despachado, "
					+ "sugerido_calculado, canastas_sugeridas, fecha_actualizacion) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, NOW()) "
					+ "ON DUPLICATE KEY UPDATE "
					+ "promedio_despachado = VALUES(promedio_despachado), "
					+ "total_despachado_4sem = VALUES(total_despachado_4sem), "
					+ "veces_despachado = VALUES(veces_despachado), "
					+ "sugerido_calculado = VALUES(sugerido_calculado), "
					+ "canastas_sugeridas = VALUES(canastas_sugeridas), "
					+ "fecha_actualizacion = NOW()");

			int procesados = 0;
			while (rsIns.next()) {
				final int idInsumo = rsIns.getInt("idinsumo");
				final String manejaCan = rsIns.getString("manejacanastas");
				final int cXcanasta = rsIns.getInt("cantidadxcanasta");
				final int veces = rsIns.getInt("veces");
				final double total = rsIns.getDouble("total");

				double prom;
				if (totalDespachos > 0 && veces >= (totalDespachos * 0.4)) {
					prom = total / (double) totalDespachos;
				} else if (veces > 0) {
					prom = total / (double) veces;
				} else {
					prom = 0;
				}

				double sugerido;
				int canastas = 0;
				if ("S".equalsIgnoreCase(manejaCan) && cXcanasta > 0) {
					canastas = (int) Math.round(prom / cXcanasta);
					if (canastas == 0 && prom > 0) {
						canastas = 1;
					}
					sugerido = canastas * cXcanasta;
				} else {
					sugerido = Math.round(prom);
				}

				psUpsert.setInt(1, idTienda);
				psUpsert.setInt(2, idInsumo);
				psUpsert.setDouble(3, Math.round(prom * 10.0) / 10.0);
				psUpsert.setDouble(4, total);
				psUpsert.setInt(5, veces);
				psUpsert.setDouble(6, sugerido);
				psUpsert.setInt(7, canastas);
				psUpsert.addBatch();
				procesados++;
			}
			rsIns.close();
			psInsumos.close();

			if (procesados > 0) {
				psUpsert.executeBatch();
			}
			psUpsert.close();

			// 4. Actualizar tabla de control
			final PreparedStatement psCtrl = cn.prepareStatement(
					"INSERT INTO despacho_sugerido_control "
					+ "(idtienda, fecha_ultima_actualizacion, dias_vigencia, fecha_desde_analisis, "
					+ "fecha_hasta_analisis, total_despachos_analizados, usuario_actualizo) "
					+ "VALUES (?, NOW(), ?, ?, ?, ?, ?) "
					+ "ON DUPLICATE KEY UPDATE "
					+ "fecha_ultima_actualizacion = NOW(), "
					+ "fecha_desde_analisis = VALUES(fecha_desde_analisis), "
					+ "fecha_hasta_analisis = VALUES(fecha_hasta_analisis), "
					+ "total_despachos_analizados = VALUES(total_despachos_analizados), "
					+ "usuario_actualizo = VALUES(usuario_actualizo)");
			psCtrl.setInt(1, idTienda);
			psCtrl.setInt(2, DIAS_VIGENCIA_DEFECTO);
			psCtrl.setString(3, fechaDesde);
			psCtrl.setString(4, fechaHasta);
			psCtrl.setInt(5, totalDespachos);
			psCtrl.setString(6, usuario != null && !usuario.trim().isEmpty() ? usuario : "sistema");
			psCtrl.executeUpdate();
			psCtrl.close();

			res.ok = true;
			res.insumosCalculados = procesados;
			res.mensaje = "Sugerido actualizado con éxito para " + procesados + " insumos basados en "
					+ totalDespachos + " despachos reales (" + fechaDesde + " al " + fechaHasta + ").";
		} catch (final Exception e) {
			logger.error("DespachoSugeridoDAO.recalcularSugerido4Semanas: " + e.toString());
			res.ok = false;
			res.mensaje = "Error al recalcular sugerido: " + e.getMessage();
		} finally {
			cerrar(cn);
		}
		return res;
	}

	/**
	 * Obtiene el listado completo de insumos para despachar a una tienda,
	 * con el sugerido inteligente de 4 semanas, empaques y stock actual en tienda si lo hay.
	 */
	public static ArrayList<InsumoSugeridoDTO> obtenerSugeridoTienda(final int idTienda, final String fechaSurtir) {
		final ArrayList<InsumoSugeridoDTO> lista = new ArrayList<InsumoSugeridoDTO>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return lista;
		}

		// Obtener inventario actual de la tienda si existe el servicio
		final Map<Integer, Double> stockTienda = new HashMap<Integer, Double>();
		try {
			String fechaFormato = "";
			if (fechaSurtir != null && !fechaSurtir.trim().isEmpty()) {
				final java.util.Date f1 = new SimpleDateFormat("dd/MM/yyyy").parse(fechaSurtir);
				fechaFormato = new SimpleDateFormat("yyyy-MM-dd").format(f1);
			}
			final ArrayList<capaModeloINV.InsumoTienda> insumosTienda =
					InventarioDAO.ObtenerInsumosTiendaAutomatico(idTienda, fechaFormato);
			if (insumosTienda != null) {
				for (final capaModeloINV.InsumoTienda it : insumosTienda) {
					stockTienda.put(Integer.valueOf(it.getIdinsumo()), Double.valueOf(it.getCantidad()));
				}
			}
		} catch (final Exception e) {
			// Si la tienda no responde o no tiene inventario automático cargado, stock queda en 0
		}

		try {
			final String sql = 
					"SELECT a.idinsumo, a.nombre_insumo, a.unidad_medida, "
					+ "IFNULL(a.idcategoria_despacho, 0) as idcategoria_despacho, "
					+ "IFNULL(b.nombre_categoria, 'Sin Categoría') as nombre_categoria, "
					+ "IFNULL(b.color_categoria, '#E2E8F0') as color_categoria, "
					+ "IFNULL(a.manejacanastas, 'N') as manejacanastas, "
					+ "IFNULL(a.cantidadxcanasta, 0) as cantidadxcanasta, "
					+ "IFNULL(a.nombrecontenedor, '') as nombrecontenedor, "
					+ "IFNULL(a.orden, 9999) as orden, "
					+ "IFNULL(s.promedio_despachado, 0) as promedio_despachado, "
					+ "IFNULL(s.total_despachado_4sem, 0) as total_despachado_4sem, "
					+ "IFNULL(s.veces_despachado, 0) as veces_despachado, "
					+ "IFNULL(s.sugerido_calculado, 0) as sugerido_calculado, "
					+ "IFNULL(s.canastas_sugeridas, 0) as canastas_sugeridas "
					+ "FROM insumo a "
					+ "LEFT JOIN categoria_despacho_insumo b ON a.idcategoria_despacho = b.idcategoria_despacho "
					+ "LEFT JOIN despacho_sugerido_detalle s ON s.idinsumo = a.idinsumo AND s.idtienda = ? "
					+ "ORDER BY a.orden ASC, a.nombre_insumo ASC";

			final PreparedStatement ps = cn.prepareStatement(sql);
			ps.setInt(1, idTienda);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final InsumoSugeridoDTO item = new InsumoSugeridoDTO();
				item.idInsumo = rs.getInt("idinsumo");
				item.nombreInsumo = rs.getString("nombre_insumo");
				item.unidadMedida = rs.getString("unidad_medida");
				item.idCategoria = rs.getInt("idcategoria_despacho");
				item.nombreCategoria = rs.getString("nombre_categoria");
				item.colorCategoria = rs.getString("color_categoria");
				item.manejaCanastas = rs.getString("manejacanastas");
				item.cantidadXCanasta = rs.getInt("cantidadxcanasta");
				item.nombreContenedor = rs.getString("nombrecontenedor");
				item.orden = rs.getInt("orden");
				item.promedio4Sem = rs.getDouble("promedio_despachado");
				item.total4Sem = rs.getDouble("total_despachado_4sem");
				item.vecesDespachado = rs.getInt("veces_despachado");
				item.cantidadSugerida = rs.getDouble("sugerido_calculado");
				item.canastasSugeridas = rs.getInt("canastas_sugeridas");

				final Double st = stockTienda.get(Integer.valueOf(item.idInsumo));
				item.cantidadTienda = (st != null ? st.doubleValue() : 0.0);

				// Cantidad a llevar por defecto arranca en el sugerido calculado
				item.cantidadLlevar = item.cantidadSugerida;

				lista.add(item);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			logger.error("DespachoSugeridoDAO.obtenerSugeridoTienda: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return lista;
	}

	private static void cerrar(final Connection cn) {
		if (cn != null) {
			try {
				cn.close();
			} catch (final Exception e) {
			}
		}
	}
}

package capaDAOINV;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import capaModeloINV.InsumoDespachoTiendaDetalle;
import conexionINV.ConexionBaseDatos;


/**
 * Clase que se encarga de implementar todos aquellos m�todos que tienen una interacci�n directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDespachoTiendaDetalleDAO {
	
	
/**
 * M�todo para retornar el detalle de un despacho de tienda.
 * @param idDespacho
 * @return Un ArrayList con el detalle del despacho seleccionado.
 */
	public static ArrayList<InsumoDespachoTiendaDetalle> obtenerDetalleDespachoTienda(int idDespacho)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<InsumoDespachoTiendaDetalle> detalleDespacho = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select MAX(a.iddespacho_detalle) AS iddespacho_detalle, a.iddespacho, a.idinsumo, SUM(cantidad) AS cantidad, a.contenedor, a.color, a.lote, a.estado , IFNULL(c.color_categoria,'') as color_categoria from  insumo_despacho_tienda_detalle a, insumo b left outer join categoria_despacho_insumo c on b.idcategoria_despacho = c.idcategoria_despacho where a.iddespacho = " + idDespacho + " and a.idinsumo = b.idinsumo GROUP BY a.iddespacho, a.idinsumo, a.contenedor, a.color, a.lote, a.estado , IFNULL(c.color_categoria,'') order by b.orden asc";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			//Variables para capturar cada despacho
			int idDespachoDetalle;
			int idInsumo;
			double cantidad;
			String contenedor;
			String color;
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0, "");
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				contenedor = rs.getString("contenedor");
				color = rs.getString("color_categoria");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespacho, idDespachoDetalle, idInsumo, cantidad, contenedor);
				insDetalleTemp.setColor(color);
				detalleDespacho.add(insDetalleTemp);	
			}
			
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(detalleDespacho);
	}
	
	public static ArrayList<InsumoDespachoTiendaDetalle> obtenerDetalleDespachoTiendaCompleto(int idDespacho)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<InsumoDespachoTiendaDetalle> detalleDespacho = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select IFNULL(a.iddespacho_detalle, 0) as iddespacho_detalle, IFNULL(a.iddespacho,0) as iddespacho, b.idinsumo, IFNULL(a.cantidad,0) as cantidad, IFNULL(a.contenedor,'') as contenedor, b.nombre_insumo, IFNULL(a.lote,'') as lote, IFNULL(a.estado,0) as estado, IFNULL(a.color,'') as color, a.caducidad_lote  from  insumo b left outer join insumo_despacho_tienda_detalle a on a.idinsumo = b.idinsumo and a.iddespacho = " + idDespacho + " order by b.orden asc";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			//Variables para capturar cada despacho
			int idDespachoDetalle;
			int idInsumo;
			double cantidad;
			String contenedor;
			String lote;
			int estado;
			String color;
			String caducidadLote;
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0, "");
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				contenedor = rs.getString("contenedor");
				lote = rs.getString("lote");
				estado = rs.getInt("estado");
				color = rs.getString("color");
				caducidadLote = rs.getString("caducidad_lote");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespachoDetalle,idDespacho, idInsumo, cantidad, contenedor);
				insDetalleTemp.setLote(lote);
				insDetalleTemp.setEstado(estado);
				insDetalleTemp.setColor(color);
				insDetalleTemp.setCaducidadLote(caducidadLote);
				detalleDespacho.add(insDetalleTemp);	
			}
			
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(detalleDespacho);
	}
	
	
	/**
	 * M�todo que se encarga de realizar la inserci�n de un detalle de insumo en el sistema de inventarios
	 * @param iddespacho
	 * @param idinsumo
	 * @param cantidad
	 * @param contenedor
	 * @return
	 */
	public static int InsertarDetalleInsumoDespachoTienda(int iddespacho, int idinsumo, double cantidad, String contenedor) {
	    Logger logger = Logger.getLogger("log_file");
	    int idDespachoDetalle = 0;
	    ConexionBaseDatos con = new ConexionBaseDatos();
	    Connection con1 = null;

	    // Modificamos la consulta eliminando el campo caducidad_lote
	    String insert = "INSERT INTO insumo_despacho_tienda_detalle (iddespacho, idinsumo, cantidad, contenedor) " +
	                    "VALUES (?, ?, ?, ?)";
	    try {
	        con1 = con.obtenerConexionBDPrincipalLocal(); 

	        // Usamos try-with-resources solo para el PreparedStatement
	        try (PreparedStatement psInsert = con1.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
	            psInsert.setInt(1, iddespacho);
	            psInsert.setInt(2, idinsumo);
	            psInsert.setDouble(3, cantidad);
	            psInsert.setString(4, contenedor);

	            // Ejecutar la inserción
	            logger.info("Ejecutando consulta INSERT: " + psInsert);
	            psInsert.executeUpdate();
	            
	            // Obtener la clave generada automáticamente
	            try (ResultSet rs = psInsert.getGeneratedKeys()) {
	                if (rs.next()) {
	                    idDespachoDetalle = rs.getInt(1);  // Recupera el ID generado
	                }
	            }
	        }
	    } catch (Exception e) {
	        logger.error("Error al insertar detalle insumo despacho tienda: " + e.getMessage(), e);
	        idDespachoDetalle = 0;
	    } finally {
	        if (con1 != null) {
	            try {
	                // Cerrar la conexión aquí si es necesario
	            	con1.close();
	            } catch (Exception e) {
	                logger.error("Error al cerrar la conexión: " + e.getMessage(), e);
	            }
	        }
	    }

	    return idDespachoDetalle;
	}

	
	
	/**
	 * M�todo que se encarga de realizar la inserci�n de un detalle de insumo en el sistema de inventarios
	 * @param iddespacho
	 * @param idinsumo
	 * @param cantidad
	 * @param contenedor
	 * @return
	 */
	public static int ActualizarDetalleInsumoDespachoTienda( int iddespacho, int idinsumo, double cantidad, String contenedor, String lote, int estado, int idDespachoDetalle, String caducidadLote) {	
	    Logger logger = Logger.getLogger("log_file");
	    ConexionBaseDatos con = new ConexionBaseDatos();
	    Connection con1 = null;
	    String querySelect = "SELECT 1 FROM insumo_despacho_tienda_detalle WHERE iddespacho_detalle = ?";
	    String queryUpdate = "UPDATE insumo_despacho_tienda_detalle SET cantidad = ?, contenedor = ?, lote = ?, estado = ?, caducidad_lote = ? WHERE iddespacho_detalle = ?";
	    String queryInsert = "INSERT INTO insumo_despacho_tienda_detalle (iddespacho, idinsumo, cantidad, contenedor, lote, estado, caducidad_lote) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    int idRespuesta = 0;
	    try {	     
	        con1 = con.obtenerConexionBDPrincipalLocal();
	        boolean actualiza;
	        try (PreparedStatement psSelect = con1.prepareStatement(querySelect)) {
	            psSelect.setInt(1, idDespachoDetalle);
	            try (ResultSet rs = psSelect.executeQuery()) {
	                actualiza = rs.next();
	            }
	        }
	        if (actualiza) {
	            try (PreparedStatement psUpdate = con1.prepareStatement(queryUpdate)) {
	                psUpdate.setDouble(1, cantidad);
	                psUpdate.setString(2, contenedor); 
	                psUpdate.setString(3, lote);      
	                psUpdate.setInt(4, estado);
	                if (caducidadLote == null || caducidadLote.equals(new String("null"))) {
	                	psUpdate.setNull(5, java.sql.Types.NULL);
	                    
	                } else {
	                	psUpdate.setDate(5, java.sql.Date.valueOf(caducidadLote));
	                }
	                psUpdate.setInt(6, idDespachoDetalle);

	                logger.info("Ejecutando consulta UPDATE: " + psUpdate);
	                System.out.println("Ejecutando consulta UPDATE: " + psUpdate);
	                System.out.println("Ejecutando consulta UPDATE: " + queryUpdate);
	                psUpdate.executeUpdate();
	                idRespuesta = iddespacho; 
	            }
	        } else {
	            try (PreparedStatement psInsert = con1.prepareStatement(queryInsert)) {
	                psInsert.setInt(1, iddespacho);
	                psInsert.setInt(2, idinsumo);
	                psInsert.setDouble(3, cantidad);
	                psInsert.setString(4, contenedor); 
	                psInsert.setString(5, lote);      
	                psInsert.setInt(6, estado);
	                if (caducidadLote == null || caducidadLote.equals(new String("null"))) {
	                	psInsert.setNull(7, java.sql.Types.NULL);
	                    
	                } else {
	                	psInsert.setDate(7, java.sql.Date.valueOf(caducidadLote));
	                }

	                logger.info("Ejecutando consulta INSERT: " + psInsert);
	                psInsert.executeUpdate();
	                idRespuesta = iddespacho; 
	            }
	        }

	    } catch (Exception e) {
	    	System.out.println("Error en ActualizarDetalleInsumoDespachoTienda " + e.toString());
	        logger.error("Error en ActualizarDetalleInsumoDespachoTienda: " + e.getMessage(), e);
	        return 0;
	    } finally {
	        if (con1 != null) {  try {
	                con1.close();
	            } catch (Exception e) {
	                logger.error("Error al cerrar la conexión: " + e.getMessage(), e);
	            }
	        }
	    }
	    return idRespuesta;
	}
	
	
	public static int ActualizarDetalleInsumoLoteDespachoTienda(int iddespacho, int idinsumo, String lote, String color, int idDespachoDetalle, String caducidadLote) {
	    Logger logger = Logger.getLogger("log_file");
	    ConexionBaseDatos con = new ConexionBaseDatos();
	    int idDespachoDetalleResult = 0;
	    Connection con1 = null;
	    
	    String select = "SELECT 1 FROM insumo_despacho_tienda_detalle WHERE iddespacho_detalle = ?";
	    String update = "UPDATE insumo_despacho_tienda_detalle SET lote = ?, color = ?, caducidad_lote = ? WHERE iddespacho_detalle = ?";

	    try {
	        con1 = con.obtenerConexionBDPrincipalLocal(); // Obtención de la conexión fuera del try-with-resources

	        try (PreparedStatement psSelect = con1.prepareStatement(select);
	             PreparedStatement psUpdate = con1.prepareStatement(update)) {
	            
	            // Verificar si existe el registro
	            psSelect.setInt(1, idDespachoDetalle);
	            try (ResultSet rs = psSelect.executeQuery()) {
	                if (rs.next()) {
	                    // El registro existe, proceder a la actualización
	                    psUpdate.setString(1, lote);
	                    psUpdate.setString(2, color);
	                    
	                    if (caducidadLote != null && !caducidadLote.isEmpty()) {
	                        psUpdate.setDate(3, java.sql.Date.valueOf(caducidadLote));
	                    } else {
	                        psUpdate.setNull(3, java.sql.Types.DATE);
	                    }
	                    
	                    psUpdate.setInt(4, idDespachoDetalle);
	                    logger.info("Ejecutando consulta UPDATE: " + psUpdate);
	                    psUpdate.executeUpdate();
	                    idDespachoDetalleResult = iddespacho;
	                }
	            }
	        }
	    } catch (Exception e) {
	        logger.error("Error al actualizar detalle insumo lote despacho tienda: " + e.getMessage(), e);
	        idDespachoDetalleResult = 0;
	    } finally {
	        // Cerrar la conexión de manera convencional
	        if (con1 != null) {
	            try {
	                con1.close();
	            } catch (Exception e) {
	                logger.error("Error al cerrar la conexión: " + e.getMessage(), e);
	            }
	        }
	    }
	    
	    return idDespachoDetalleResult;
	}

	
	public static void actualizarMarcadoDespachoTienda(int idDespacho, String marcado)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean actualiza = false;
		try
		{
			Statement stm = con1.createStatement();
			String update = "update insumo_despacho_tienda set marcado = " +  marcado + " where iddespacho = " + idDespacho; 
			logger.info(update);
			stm.executeUpdate(update);
			stm.close();
			con1.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
	}
	
	
}

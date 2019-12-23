package capaDAO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import capaModelo.InsumoDespachoTiendaDetalle;
import conexion.ConexionBaseDatos;


/**
 * Clase que se encarga de implementar todos aquellos métodos que tienen una interacción directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDespachoTiendaDetalleDAO {
	
	
/**
 * Método para retornar el detalle de un despacho de tienda.
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
			String consulta = "select a.* from  insumo_despacho_tienda_detalle a, insumo b where a.iddespacho = " + idDespacho + " and a.idinsumo = b.idinsumo order by b.orden asc";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			//Variables para capturar cada despacho
			int idDespachoDetalle;
			int idInsumo;
			double cantidad;
			String contenedor;
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0, "");
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				contenedor = rs.getString("contenedor");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespacho, idDespachoDetalle, idInsumo, cantidad, contenedor);
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
			String consulta = "select IFNULL(a.iddespacho_detalle, 0) as iddespacho_detalle, IFNULL(a.iddespacho,0) as iddespacho, b.idinsumo, IFNULL(a.cantidad,0) as cantidad, IFNULL(a.contenedor,'') as contenedor, b.nombre_insumo from  insumo b left outer join insumo_despacho_tienda_detalle a on a.idinsumo = b.idinsumo and a.iddespacho = " + idDespacho + " order by b.orden asc";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			//Variables para capturar cada despacho
			int idDespachoDetalle;
			int idInsumo;
			double cantidad;
			String contenedor;
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0, "");
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				contenedor = rs.getString("contenedor");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespacho, idDespachoDetalle, idInsumo, cantidad, contenedor);
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
	 * Método que se encarga de realizar la inserción de un detalle de insumo en el sistema de inventarios
	 * @param iddespacho
	 * @param idinsumo
	 * @param cantidad
	 * @param contenedor
	 * @return
	 */
	public static int InsertarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo,double cantidad, String contenedor)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDespachoDetalle = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
				
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into insumo_despacho_tienda_detalle (iddespacho,idinsumo, cantidad, contenedor) values (" + iddespacho + ", " + idinsumo  + ", " + cantidad +" , '" + contenedor +"' )"; 
			logger.info(insert);
			stm.executeUpdate(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDespachoDetalle=rs.getInt(1);
				
	        }
	        rs.close();
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
			return(0);
		}
		return(idDespachoDetalle);
	}
	
	
	/**
	 * Método que se encarga de realizar la inserción de un detalle de insumo en el sistema de inventarios
	 * @param iddespacho
	 * @param idinsumo
	 * @param cantidad
	 * @param contenedor
	 * @return
	 */
	public static int ActualizarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo,double cantidad, String contenedor)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDespachoDetalle = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
				
		try
		{
			Statement stm = con1.createStatement();
			String update = "update insumo_despacho_tienda_detalle set cantidad = " + cantidad + " , contenedor = '" + contenedor + "'  where iddespacho = " + iddespacho + " and idinsumo = " + idinsumo; 
			logger.info(update);
			stm.executeUpdate(update);
			idDespachoDetalle = iddespacho;
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
			return(0);
		}
		return(idDespachoDetalle);
	}
	
	
}

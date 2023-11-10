package capaDAOINV;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import capaModeloINV.InsumoDespachoTiendaDetalle;
import conexionINV.ConexionBaseDatos;


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
			String consulta = "select IFNULL(a.iddespacho_detalle, 0) as iddespacho_detalle, IFNULL(a.iddespacho,0) as iddespacho, b.idinsumo, IFNULL(a.cantidad,0) as cantidad, IFNULL(a.contenedor,'') as contenedor, b.nombre_insumo, IFNULL(a.lote,'') as lote, IFNULL(a.estado,0) as estado, IFNULL(a.color,'') as color  from  insumo b left outer join insumo_despacho_tienda_detalle a on a.idinsumo = b.idinsumo and a.iddespacho = " + idDespacho + " order by b.orden asc";
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
			InsumoDespachoTiendaDetalle insDetalleTemp = new InsumoDespachoTiendaDetalle(0,0,0, 0, "");
			while(rs.next()){
				idDespachoDetalle = rs.getInt("iddespacho_detalle");
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				contenedor = rs.getString("contenedor");
				lote = rs.getString("lote");
				estado = rs.getInt("estado");
				color = rs.getString("color");
				insDetalleTemp = new InsumoDespachoTiendaDetalle(idDespachoDetalle,idDespacho, idInsumo, cantidad, contenedor);
				insDetalleTemp.setLote(lote);
				insDetalleTemp.setEstado(estado);
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
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
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
	public static int ActualizarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo,double cantidad, String contenedor, String lote, int estado, int idDespachoDetalle)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean actualiza = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from insumo_despacho_tienda_detalle where iddespacho_detalle = " + idDespachoDetalle; 
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				actualiza = true;
				break;
			}
			String update = "";
			String insert = "";
			if(actualiza)
			{
				if(contenedor.equals(new String("")))
				{
					update = "update insumo_despacho_tienda_detalle set cantidad = " + cantidad + ", lote ='" +  lote + "', estado =" + estado  + "  where iddespacho_detalle = " + idDespachoDetalle; 
				}else
				{
					update = "update insumo_despacho_tienda_detalle set cantidad = " + cantidad + " , contenedor = '" + contenedor + "' , lote ='" +  lote + "', estado =" + estado + "  where iddespacho_detalle = " + idDespachoDetalle; 
				}
				logger.info(update);
				stm.executeUpdate(update);
			}else
			{
				if(contenedor.equals(new String("")))
				{
					insert = "insert into insumo_despacho_tienda_detalle (iddespacho, idinsumo, cantidad, contenedor,lote, estado) values(" + iddespacho + "," + idinsumo + "," + cantidad + ", '" + contenedor + "' ,'" + lote + "' ," + estado + ")";
				}else
				{
					insert = "insert into insumo_despacho_tienda_detalle (iddespacho, idinsumo, cantidad, lote, estado) values(" + iddespacho + "," + idinsumo + "," + cantidad +",'" + lote + "' ," + estado + ")";
				}
				logger.info(insert);
				stm.executeUpdate(insert);
			}
			idDespachoDetalle = iddespacho;
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
			return(0);
		}
		return(idDespachoDetalle);
	}
	
	
	public static int ActualizarDetalleInsumoLoteDespachoTienda(int iddespacho,int idinsumo, String lote, String color, int idDespachoDetalle)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean actualiza = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from insumo_despacho_tienda_detalle where iddespacho_detalle = " + idDespachoDetalle; 
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				actualiza = true;
				break;
			}
			String update = "";
			if(actualiza)
			{
				update = "update insumo_despacho_tienda_detalle set lote ='" +  lote + "' , color ='"+ color +"'  where iddespacho_detalle = " + idDespachoDetalle; 
				logger.info(update);
				stm.executeUpdate(update);
			}
			idDespachoDetalle = iddespacho;
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
			return(0);
		}
		return(idDespachoDetalle);
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

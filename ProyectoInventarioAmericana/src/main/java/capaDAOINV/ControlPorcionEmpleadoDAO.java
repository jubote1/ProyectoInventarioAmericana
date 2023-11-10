package capaDAOINV;

import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import capaModeloINV.ControlPorcionEmpleado;
import conexionINV.ConexionBaseDatos;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Clase que se encarga de la implementación de toda la interacción con la base de datos para la entidad Llave
 * @author JuanDavid
 *
 */
public class ControlPorcionEmpleadoDAO {
	
	/**
	 * Método que se encarga de retornar la información de todas las llaves definidas en el sistema.
	 * @return Se retorna un ArrayList con todos las llaves definidas en el sistema
	 */
	public static boolean insertarControlPorcionEmpleado(ControlPorcionEmpleado controlPorcion)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into control_porcion_empleado (idusuario,idtienda,idpedido,iddetalle_pedido,fecha) values("+ controlPorcion.getIdUsuario() + "," + controlPorcion.getIdTienda() + "," + controlPorcion.getIdPedido() + "," + controlPorcion.getIdDetallePedido() + ", '" + controlPorcion.getFecha() + "')";
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
			respuesta = true;
		}catch (Exception e){
			logger.error(e.toString());
			respuesta = false;
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(respuesta);
		
	}
	
	public static boolean eliminarControlPorcionEmpleado(int idUsuario, int idTienda, int idPedido, int idDetallePedido, String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String delete = "delete from  control_porcion_empleado where idusuario = " + idUsuario + " and idpedido = " + idPedido + " and iddetalle_pedido = " + idDetallePedido + " and idtienda = " + idTienda;
			stm.executeUpdate(delete);
			stm.close();
			con1.close();
			respuesta = true;
		}catch (Exception e){
			logger.error(e.toString());
			respuesta = false;
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(respuesta);
		
	}
	
	public static boolean validarControlPorcionEmpleado(int idUsuario, String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from  control_porcion_empleado where idusuario = '"+ idUsuario + "' and fecha ='" + fecha + "'";
			ResultSet rs = stm.executeQuery(select);
			while(rs.next())
			{
				respuesta = true;
				break;
			}
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString());
			respuesta = false;
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(respuesta);	
	}
	
	public static ArrayList<ControlPorcionEmpleado> consultarControlPorcionEmpleadoDia(String fecha, int idTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<ControlPorcionEmpleado> controlPorcionDia = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String select = "select a.* , b.nombre_largo from  control_porcion_empleado a, general.empleado b where a.idusuario = b.id and a.fecha = '"+ fecha + "' and a.idtienda = " + idTienda;
			ResultSet rs = stm.executeQuery(select);
			ControlPorcionEmpleado controlTemp;
			int idUsuario;
			String nombre;
			int idPedido;
			int idDetallePedido;
			while(rs.next())
			{
				idUsuario = rs.getInt("idusuario");
				nombre = rs.getString("nombre_largo");
				idPedido = rs.getInt("idpedido");
				idDetallePedido = rs.getInt("iddetalle_pedido");
				controlTemp = new ControlPorcionEmpleado(idUsuario, idTienda, idPedido, idDetallePedido,fecha);
				controlTemp.setNombreEmpleado(nombre);
				controlPorcionDia.add(controlTemp);
			}
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.error(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(controlPorcionDia);	
	}
	
}

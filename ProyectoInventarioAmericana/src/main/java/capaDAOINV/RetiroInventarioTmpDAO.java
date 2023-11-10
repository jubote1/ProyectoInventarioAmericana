package capaDAOINV;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import conexionINV.ConexionBaseDatos;

public class RetiroInventarioTmpDAO {
	
	public static boolean insertarRetiroInventarioTmp( String fecha, int idDespacho, String tiendaBodega, String observacion, int idTienda, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tiendaBodega);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String insert = "insert into retiro_inventario_tmp (iddespacho,fecha_sistema, observacion, idtienda, tienda) values (" + idDespacho + ", '" + fecha + "' , '" + observacion + "' ," +  idTienda + " , '" + tienda +"')"; 
			stm.executeUpdate(insert);
			resultado = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}
	
	public static boolean borrarRetiroInventarioTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String delete = "delete from retiro_inventario_tmp where iddespacho = " + idDespacho; 
			stm.executeUpdate(delete);
			resultado = true;
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}
	
	
	public static boolean existeRetiroInventarioTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String consulta = "select * from retiro_inventario_tmp where iddespacho = " + idDespacho; 
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next())
			{
				resultado = true;
				break;
			}
			stm.close();
			con1.close();
		}
		catch (Exception e){
			resultado = false;
			try
			{
				con1.close();
			}catch(Exception e1)	
			{
			}
			
		}
		return(resultado);
	}


}

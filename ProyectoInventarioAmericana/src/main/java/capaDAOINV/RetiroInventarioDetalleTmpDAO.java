package capaDAOINV;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import capaModeloINV.ModificadorInventario;
import conexionINV.ConexionBaseDatos;

public class RetiroInventarioDetalleTmpDAO {
	
	public static boolean insertarRetiroInventarioDetTmp( int idDespacho, ModificadorInventario retiro, String tiendaBodega)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tiendaBodega);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String insert = "insert into retiro_inventario_detalle_tmp (iddespacho,iditem,cantidad) values (" + idDespacho + ", " + retiro.getIdItem() + ", " + retiro.getCantidad() + ")"; 
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
	
	public static boolean borrarRetiroInventarioDetallesTmp( int idDespacho, String tienda)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDTiendaRemota(tienda);
		boolean resultado = false;
		try
		{
			//Realizamos la inserción del IdInventario
			Statement stm = con1.createStatement();
			String delete = "delete from retiro_inventario_detalle_tmp where iddespacho = " + idDespacho; 
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

}

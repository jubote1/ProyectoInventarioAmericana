package capaDAOINV;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModeloINV.Desecho;
import conexionINV.ConexionBaseDatos;

/**
 * Clase que se encarga de implementar toda la interacción con la base de datos para le entidad Producto.
 * @author JuanDavid
 *
 */
public class DesechoDAO {
	
	public static ArrayList<Desecho> obtenerDesechos()
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<Desecho> desechos = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from desecho where habilitado = 'S'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idDesecho;
			String descripcion;
			double costo;
			String tipo;
			while(rs.next()){
				idDesecho = rs.getInt("iddesecho");
				descripcion = rs.getString("descripcion");
				costo = rs.getDouble("costo");
				tipo = rs.getString("tipo");
				Desecho desecho = new Desecho(idDesecho, descripcion, tipo, costo);
				desechos.add(desecho);
			}
			rs.close();
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
		return(desechos);
		
	}
	

	public static int insertarDesecho(Desecho des)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDesechoIns = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into desecho (descripcion, costo, tipo) values ('" + des.getDescripcion() + "' , " + des.getCosto() + " , '" + des.getTipo() + "')"; 
			logger.info(insert);
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDesechoIns = rs.getInt(1);
				
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
			return(0);
		}
		return(idDesechoIns);
	}


	public static void eliminarDesecho (int idDesecho)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String delete = "delete from desecho  where iddesecho = " + idDesecho; 
			logger.info(delete);
			stm.executeUpdate(delete);
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
		
	}

	

	public static Desecho retornarDesecho(int idDesecho)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		Desecho des = new Desecho(0,"","", 0);
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from desecho where iddesecho = " + idDesecho;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			String descripcion = "";
			double costo = 0;
			String tipo = "";
			while(rs.next()){
				descripcion = rs.getString("descripcion");
				costo = rs.getDouble("costo");
				tipo = rs.getString("tipo");
			}
			des = new Desecho(idDesecho, descripcion,tipo, costo);
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
		return(des);
	}


}

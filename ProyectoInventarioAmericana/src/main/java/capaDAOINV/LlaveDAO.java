package capaDAOINV;

import java.sql.Connection;
import org.apache.log4j.Logger;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import capaModeloINV.Llave;
import conexionINV.ConexionBaseDatos;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Clase que se encarga de la implementación de toda la interacción con la base de datos para la entidad Llave
 * @author JuanDavid
 *
 */
public class LlaveDAO {
	
	/**
	 * Método que se encarga de retornar la información de todas las llaves definidas en el sistema.
	 * @return Se retorna un ArrayList con todos las llaves definidas en el sistema
	 */
	public static ArrayList obtenerLlaves(boolean auditoria)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList llaves = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from llaves";
			if(auditoria)
			{
				logger.info(consulta);
			}logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			ResultSetMetaData rsMd = (ResultSetMetaData) rs.getMetaData();
			int numeroColumnas = rsMd.getColumnCount();
			
			while(rs.next()){
				String [] fila = new String[numeroColumnas];
				for(int y = 0; y < numeroColumnas; y++)
				{
					fila[y] = rs.getString(y+1);
				}
				llaves.add(fila);
				
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
		return(llaves);
		
	}
	
	
	/**
	 * Método de la capa DAO que se encarga de retornar los Municipios del sistema en un arrayList de objetos de tipo Municipio
	 * @return Un ArrayList con los Municipios.
	 */
	public static ArrayList<Llave> consultarLlaves(int idTienda, int tipoConsulta)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<Llave> llaves = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta;
			if(tipoConsulta == 1)
			{
				consulta = "select * from llaves where idtienda =  " + idTienda + " and disponible = 1";
			}else
			{
				consulta = "select * from llaves where idtienda =  " + idTienda;
			}
			ResultSet rs = stm.executeQuery(consulta);
			int idLlave;
			String nombre;
			while(rs.next()){
				idLlave = rs.getInt("idllave");
				nombre = rs.getString("nombre");
				Llave llaveTemp = new Llave(idLlave, nombre, idTienda);
				llaves.add(llaveTemp);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(llaves);
		
	}
	
	public static ArrayList<Llave> consultarLlavesPrestadasObjeto(int idTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<Llave> llaves = new ArrayList();
		
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from llaves where idtienda =  " + idTienda + " and disponible = 0";
			ResultSet rs = stm.executeQuery(consulta);
			int idLlave;
			String nombre;
			while(rs.next()){
				idLlave = rs.getInt("idllave");
				nombre = rs.getString("nombre");
				Llave llaveTemp = new Llave(idLlave, nombre, idTienda);
				llaves.add(llaveTemp);
				
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			logger.info(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(llaves);
		
	}
	
	public static boolean  validarDisponibilidadLlave(int idLlave)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from llaves where idllave = " + idLlave + " and disponible = 1";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				respuesta = true;
				break;
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
		return(respuesta);
		
	}
	
}

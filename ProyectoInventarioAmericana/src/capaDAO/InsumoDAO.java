package capaDAO;
import conexion.ConexionBaseDatos;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import capaModelo.Cliente;
import capaModelo.Insumo;


/**
 * Clase que se encarga de implementar todos aquellos métodos que tienen una interacción directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDAO {
	
	
/**
 * Método que se encarga de insertar en base de datos la información de la entidad Especialidad
 * @param Espe recibe como parámetro un objeto Modelo Especialidad con base en el cual se realiza la inserción de la
 * especialidad.
 * @return Se retonra valor entero con el id de la especiliadad insertada.
 */
public static int insertarInsmo(Insumo insumo)
{
	int idInsumoIns = 0;
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	try
	{
		Statement stm = con1.createStatement();
		int controlCantidad = 0;
		if(insumo.isControl_cantidad())
		{
			controlCantidad = 1;
		}else
		{
			controlCantidad = 0;
		}
		String insert = "insert into insumo (nombre_insumo, unidad_medida, precio_unidad, manejacanastas, cantidadxcanasta, nombrecontenedor, categoria, control_cantidad, costo_unidad) values ('" + insumo.getNombre() + "', '" + insumo.getUnidadMedida() + "' , " + 0 + " , '"+ insumo.getManejacanasta() + "' , " + insumo.getCantidaxcanasta() + " , '" + insumo.getNombreContenedor() + "' , '" + insumo.getCategoria() + "' , " +  controlCantidad + " , " + insumo.getCostoUnidad() +")"; 
		stm.executeUpdate(insert);
		ResultSet rs = stm.getGeneratedKeys();
		if (rs.next()){
			idInsumoIns=rs.getInt(1);
		}
		stm.close();
		con1.close();
	}
	catch (Exception e){
		System.out.println(e.toString());
		try
		{
			con1.close();
		}catch(Exception e1)
		{
		}
		return(0);
	}
	return(idInsumoIns);
}

/**
 * Método que se encarga de retornar una especialidad dado un idespecialidad
 * @param idespecialidad recibe como parámetro un intero id especialidad y con base en esto, realiza la consulta
 * en base de datos y retorna la información.
 * @return Se retorna la información de la especialidad en un objeto Modelo Especialidad.
 */
public static Insumo retornarInsumo(int idInsumo)
{
	Logger logger = Logger.getLogger("log_file");
	int idEspecialidadEli = 0;
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	Insumo insumo = new Insumo(0, "", "", 0, "",0, "", "", false,0);
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo  where idinsumo = " + idInsumo; 
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		String nombre = "";
		String unidadMedida = "";
		double precioUnidad = 0;
		String manejaCanasta = "";
		int cantidadCanasta = 0;
		String nombreContenedor = "";
		String categoria = ""; 
		boolean controlCantidad = false;
		int intControlCantidad = 0;
		double costoUnidad = 0;
		while(rs.next()){
			nombre = rs.getString("nombre_insumo");
			unidadMedida = rs.getString("unidad_medida");
			try
			{
				precioUnidad = rs.getDouble("precio_unidad");
			}catch(Exception e)
			{
				precioUnidad = 0;
			}
			manejaCanasta = rs.getString("manejacanastas");
			cantidadCanasta = rs.getInt("cantidadxcanasta");
			nombreContenedor = rs.getString("nombrecontenedor");
			categoria = rs.getString("categoria");
			intControlCantidad = rs.getInt("control_cantidad");
			if(intControlCantidad == 1)
			{
				controlCantidad = true;
			}else
			{
				controlCantidad = false;
			}
			try
			{
				costoUnidad = rs.getDouble("costo_unidad");
			}catch(Exception e)
			{
				costoUnidad = 0;
			}
			break;
		}
		insumo = new Insumo(idInsumo, nombre, unidadMedida,precioUnidad,manejaCanasta, cantidadCanasta, nombreContenedor, categoria, controlCantidad, costoUnidad);
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
	return(insumo);
}

/**
 * Método que tiene como objetivo modificar una especialidad.
 * @param Espe Recibe como parámetro un objeto Modelo Especiliadad con base en la cual se hará la modificación.
 * @return Se retorna un string indicadno si el proceso fue exitoso o no.
 */
public static String editarInsumo(Insumo insumo)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	String resultado = "";
	try
	{
		Statement stm = con1.createStatement();
		int intControlCantidad = 0;
		if(insumo.isControl_cantidad())
		{
			intControlCantidad = 1;
		}else
		{
			intControlCantidad = 0;
		}
		String update = "update insumo set nombre_insumo ='" + insumo.getNombre() + "', unidad_medida =  '" + insumo.getUnidadMedida() 
		+ "' , precio_unidad = " + insumo.getPrecioUnidad() + " , manejacanastas = '" + insumo.getManejacanasta() + "' , cantidadxcanasta = " + insumo.getCantidaxcanasta() + " , nombrecontenedor = '" + insumo.getNombreContenedor() + "' , categoria = '" + insumo.getCategoria() + "' , control_cantidad = " + intControlCantidad + " , costo_unidad = " + insumo.getCostoUnidad() + " where idinsumo = " + insumo.getIdinsumo(); 
		logger.info(update);
		stm.executeUpdate(update);
		resultado = "exitoso";
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
		resultado = "error";
	}
	return(resultado);
}


public static ArrayList<Insumo> retornarInsumos()
{
	Logger logger = Logger.getLogger("log_file");
	int idEspecialidadEli = 0;
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	Insumo insumo = new Insumo(0, "", "", 0, "",0, "", "", false,0);
	ArrayList<Insumo> insumos = new ArrayList();
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo";
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		int idInsumo = 0;
		String nombre = "";
		String unidadMedida = "";
		double precioUnidad = 0;
		String manejaCanasta = "";
		int cantidadCanasta = 0;
		String nombreContenedor = "";
		String categoria = ""; 
		boolean controlCantidad = false;
		int intControlCantidad = 0;
		double costoUnidad = 0;
		while(rs.next()){
			idInsumo = rs.getInt("idinsumo");
			nombre = rs.getString("nombre_insumo");
			unidadMedida = rs.getString("unidad_medida");
			try
			{
				precioUnidad = rs.getDouble("precio_unidad");
			}catch(Exception e)
			{
				precioUnidad = 0;
			}
			manejaCanasta = rs.getString("manejacanastas");
			cantidadCanasta = rs.getInt("cantidadxcanasta");
			nombreContenedor = rs.getString("nombrecontenedor");
			categoria = rs.getString("categoria");
			intControlCantidad = rs.getInt("control_cantidad");
			if(intControlCantidad == 1)
			{
				controlCantidad = true;
			}else
			{
				controlCantidad = false;
			}
			try
			{
				costoUnidad = rs.getDouble("costo_unidad");
			}catch(Exception e)
			{
				costoUnidad = 0;
			}
			insumo = new Insumo(idInsumo, nombre, unidadMedida,precioUnidad,manejaCanasta, cantidadCanasta, nombreContenedor, categoria, controlCantidad, costoUnidad);
			insumos.add(insumo);
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
	return(insumos);
}

}

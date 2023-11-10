package capaDAOINV;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import capaModeloINV.Insumo;
import conexionINV.ConexionBaseDatos;


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
		String insert = "insert into insumo (nombre_insumo, unidad_medida, precio_unidad, manejacanastas, cantidadxcanasta, nombrecontenedor, categoria, control_cantidad, costo_unidad, control_tienda) values ('" + insumo.getNombre() + "', '" + insumo.getUnidadMedida() + "' , " + 0 + " , '"+ insumo.getManejacanasta() + "' , " + insumo.getCantidaxcanasta() + " , '" + insumo.getNombreContenedor() + "' , '" + insumo.getCategoria() + "' , " +  controlCantidad + " , " + insumo.getCostoUnidad() + " , '" + insumo.getControlTienda() +"')"; 
		stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
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
	Insumo insumo = new Insumo(0, "", "", 0, "",0, "", "", false,0,"");
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
		String controlTienda= "N";
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
			controlTienda = rs.getString("control_tienda");
			break;
		}
		insumo = new Insumo(idInsumo, nombre, unidadMedida,precioUnidad,manejaCanasta, cantidadCanasta, nombreContenedor, categoria, controlCantidad, costoUnidad, controlTienda);
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
		+ "' , precio_unidad = " + insumo.getPrecioUnidad() + " , manejacanastas = '" + insumo.getManejacanasta() + "' , cantidadxcanasta = " + insumo.getCantidaxcanasta() + " , nombrecontenedor = '" + insumo.getNombreContenedor() + "' , categoria = '" + insumo.getCategoria() + "' , control_cantidad = " + intControlCantidad + " , costo_unidad = " + insumo.getCostoUnidad() + " , control_tienda = '" + insumo.getControlTienda() + "' where idinsumo = " + insumo.getIdinsumo(); 
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
	Insumo insumo = new Insumo(0, "", "", 0, "",0, "", "", false,0, "");
	ArrayList<Insumo> insumos = new ArrayList();
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select a.* , b.color_categoria from  insumo a left outer join  categoria_despacho_insumo b on a.idcategoria_despacho = b.idcategoria_despacho";
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
		String controlTienda = "";
		String colorCategoria = "";
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
			controlTienda = rs.getString("control_tienda");
			colorCategoria = rs.getString("color_categoria");
			insumo = new Insumo(idInsumo, nombre, unidadMedida,precioUnidad,manejaCanasta, cantidadCanasta, nombreContenedor, categoria, controlCantidad, costoUnidad, controlTienda);
			insumo.setColorCategoria(colorCategoria);
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


public static String actualizarPrecioInsumoRetirado(int idInsumo , double costoUnidad)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDInventarioPOSBodega();
	String resultado = "";
	//Realizamos la lógica para extraer desde cuando se hace la consulta del cambio de precio.
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Calendar calendarioActual = Calendar.getInstance();
	String fechaActual = "";
	Date datFechaAnterior;
	String fechaAnterior = "";
	try
	{
		//OJO
		fechaActual = dateFormat.format(calendarioActual.getTime());
		//fechaActual = "2020-08-09";
	}catch(Exception exc)
	{
		System.out.println(exc.toString());
	}
	int diaActual = calendarioActual.get(Calendar.DAY_OF_WEEK);
	
	//Domingo
	if(diaActual == 1)
	{
		calendarioActual.add(Calendar.DAY_OF_YEAR, -6);
	}
	else if(diaActual == 2)
	{
		//No hacemos nada dado qeu es lunes  y simplemente actualizamos lo del lunes y punto
	}
	else if(diaActual == 3)
	{
		//Si es martes se resta uno solo
		calendarioActual.add(Calendar.DAY_OF_YEAR, -1);
	}
	else if(diaActual == 4)
	{
		//Si es miercoles se resta dos
		calendarioActual.add(Calendar.DAY_OF_YEAR, -2);
	}
	else if(diaActual == 5)
	{
		//Si es jueves se resta tres
		calendarioActual.add(Calendar.DAY_OF_YEAR, -3);
	}
	else if(diaActual == 6)
	{
		//Si es viernes se resta cuatro
		calendarioActual.add(Calendar.DAY_OF_YEAR, -4);
	}
	else if(diaActual == 7)
	{
		//Si es sabado se resta cinco
		calendarioActual.add(Calendar.DAY_OF_YEAR, -5);
	}
	//Llevamos a un string la fecha anterior para el cálculo de la venta
	datFechaAnterior = calendarioActual.getTime();
	fechaAnterior = dateFormat.format(datFechaAnterior);
	
	
	try
	{
		Statement stm = con1.createStatement();
		
		String update = "UPDATE retiro_inventario_detalle a SET precio = " + costoUnidad + " WHERE a.idretiro_inventario IN (SELECT b.idretiro_inventario FROM retiro_inventario b WHERE b.fecha_sistema >= '" + fechaAnterior + "' AND b.fecha_sistema <= '" + fechaActual +"') AND a.iditem = " + idInsumo;
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

}

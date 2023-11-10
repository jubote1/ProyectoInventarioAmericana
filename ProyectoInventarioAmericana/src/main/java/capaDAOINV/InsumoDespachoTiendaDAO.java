package capaDAOINV;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.util.SystemOutLogger;

import capaModeloINV.Insumo;
import capaModeloINV.InsumoDespachoTienda;
import conexionINV.ConexionBaseDatos;


/**
 * Clase que se encarga de implementar todos aquellos métodos que tienen una interacción directa con la base de datos
 * @author JuanDavid
 *
 */
public class InsumoDespachoTiendaDAO {
	
	

	/**
	 * Método que nos permitirá validar si existe o no inventario pendiente de ingresar para la tienda en cuestión.
	 * @param idTienda
	 * @param fecha
	 * @return
	 */
public static boolean existeInsumoDespachadoTienda(int idTienda, String fecha)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	boolean resultado = false;
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo_despacho_tienda where idtienda = " + idTienda + " and fecha_despacho = '" + fecha + "' and estado ='DESPACHADO'";
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		while(rs.next()){
			resultado = true;
			break;
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
	return(resultado);
}

public static boolean existeInsumoDespachoTienda(int idTienda, String fecha)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	boolean resultado = false;
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select * from  insumo_despacho_tienda where idtienda = " + idTienda + " and fecha_despacho = '" + fecha + "'";
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		while(rs.next()){
			resultado = true;
			break;
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
	return(resultado);
}

public static String retornarEstadoDespacho(int idDespacho)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	String estado = "";
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "select estado from  insumo_despacho_tienda where iddespacho = " + idDespacho;
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		while(rs.next()){
			estado = rs.getString("estado");
			break;
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
	return(estado);
}

/**
 * Método que se encarga de retornar los despachos de tienda pendientes
 * @param idTienda
 * @param fecha
 * @return Un ArrayList con todos los InsusmosDespachoTienda pendientes para la tienda y la fecha.
 */
public static ArrayList<InsumoDespachoTienda> obtenerInsumoDespachoTienda(int idTienda, String fechaDesde, String fechaHasta)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();;
	ArrayList<InsumoDespachoTienda> insumoDespachos = new ArrayList();
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "";
		if(idTienda == 0)
		{
			consulta = "select a.*, b.nombre  from  insumo_despacho_tienda a, tienda b where a.idtienda = b.idtienda and a.fecha_despacho >= '" + fechaDesde + "' and a.fecha_despacho <= '" + fechaHasta + "'";
			
		}else
		{
			consulta = "select a.*, b.nombre  from  insumo_despacho_tienda a, tienda b where a.idtienda = b.idtienda and a.idtienda = " + idTienda + " and a.fecha_despacho >= '" + fechaDesde + "' and a.fecha_despacho <= '" + fechaHasta + "'";
			
		}
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		//Variables para capturar cada despacho
		int idDespacho;
		String fechaReal;
		String observacion = "";
		String tienda, estado;
		String fechaDespacho = "";
		String novedadTienda = "";
		String diferencia = "";
		String marcado = "";
		InsumoDespachoTienda insTemp = new InsumoDespachoTienda(0,0,"", "", "", "");
		while(rs.next()){
			idDespacho = rs.getInt("iddespacho");
			fechaReal = rs.getString("fecha_real");
			observacion = rs.getString("observacion");
			tienda = rs.getString("nombre");
			estado = rs.getString("estado");
			fechaDespacho = rs.getString("fecha_despacho");
			novedadTienda = rs.getString("novedad_tienda");
			diferencia = rs.getString("diferencia");
			marcado = rs.getString("marcado");
			insTemp = new InsumoDespachoTienda(idDespacho, idTienda, fechaDespacho, fechaReal, estado, observacion);
			insTemp.setTienda(tienda);
			insTemp.setNovedadTienda(novedadTienda);
			insTemp.setDiferencia(diferencia);
			insTemp.setMarcado(marcado);
			insumoDespachos.add(insTemp);
		}
		
		stm.close();
		con1.close();
	}
	catch (Exception e){
		logger.info(e.toString());
		try
		{
			con1.close();
		}catch(Exception e1)
		{
		}
	}
	return(insumoDespachos);
}

/**
 * Método de la capa de acceso a datos que se encarga de la inserción de despacho de pedido, teniendo en cuenta que la tabla hace las veces
 * de encabezado del despacho de insumos para la tienda.
 * @param idtienda Se recibe el idtienda de la tienda asociada al inventario.
 * @param fechasurtir Fecha que determina la fecha de llevado de los insumos a la tienda
 * @return Retorna un enterio con el iddespacho que representa como el encabezado del detalle de los insumos que se llevará a la tienda
 */
public static int InsertarInsumoDespachoTienda(int idtienda, String fechasurtir, String observacion, String estado)
{
	Logger logger = Logger.getLogger("log_file");
	int idDespacho = 0;
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	Date fechaTemporal = new Date();
	DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
	String fechaSurtirFinal ="";
	try
	{
		fechaTemporal = new SimpleDateFormat("dd/MM/yyyy").parse(fechasurtir);
		fechaSurtirFinal = formatoFinal.format(fechaTemporal);
		
	}catch(Exception e){
		logger.error(e.toString());
		return(0);
	}
	
	try
	{
		Statement stm = con1.createStatement();
		String insert = "";
		if(estado.equals(new String("")))
		{
			insert = "insert into insumo_despacho_tienda (idtienda,fecha_despacho, observacion) values (" + idtienda + ", '" + fechaSurtirFinal + "' , '"  + observacion  + "' )"; 
			
		}else
		{
			insert = "insert into insumo_despacho_tienda (idtienda,fecha_despacho, observacion, estado) values (" + idtienda + ", '" + fechaSurtirFinal + "' , '"  + observacion + "' , '" + estado  + "' )"; 
			
		}
		logger.info(insert);
		stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = stm.getGeneratedKeys();
		if (rs.next()){
			idDespacho=rs.getInt(1);
			System.out.println(idDespacho);
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
	return(idDespacho);
	
}

public static void actualizarNovedadDespachoTienda(int idDespacho, String novedad, String diferencia)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	
	try
	{
		Statement stm = con1.createStatement();
		String update =  "update insumo_despacho_tienda set novedad_tienda = '" + novedad +"' , diferencia = '" + diferencia + "' where iddespacho =" + idDespacho;
		System.out.println(update);
		stm.executeUpdate(update);
		logger.info(update);
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

public static void actualizarObservacionDespachoTienda(int idDespacho, String observacion)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	
	try
	{
		Statement stm = con1.createStatement();
		String update =  "update insumo_despacho_tienda set observacion = '" + observacion +"' where iddespacho =" + idDespacho;
		logger.info(update);
		stm.executeUpdate(update);
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

/**
 * Método que retorna el encabezado de un despacho de inventario dado un iddespacho enviado como parámetro.
 * @param idDespacho
 * @return
 */
public static InsumoDespachoTienda obtenerInsumoDespacho(int idDespacho)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();;
	InsumoDespachoTienda despacho = new InsumoDespachoTienda(0,0,"", "", "", "");
	try
	{
		Statement stm = con1.createStatement();
		String consulta = "";
		consulta = "select a.*, b.nombre  from  insumo_despacho_tienda a, tienda b where a.idtienda = b.idtienda and a.iddespacho= " + idDespacho;
		logger.info(consulta);
		ResultSet rs = stm.executeQuery(consulta);
		//Variables para capturar cada despacho
		String fechaReal;
		String observacion = "";
		String tienda, estado;
		String fechaDespacho = "";
		int idTienda = 0;
		
		while(rs.next()){
			fechaReal = rs.getString("fecha_real");
			observacion = rs.getString("observacion");
			tienda = rs.getString("nombre");
			estado = rs.getString("estado");
			fechaDespacho = rs.getString("fecha_despacho");
			idTienda = rs.getInt("idtienda");
			despacho = new InsumoDespachoTienda(idDespacho, idTienda, fechaDespacho, fechaReal, estado, observacion);
			despacho.setTienda(tienda);
			break;
		}
		
		stm.close();
		con1.close();
	}
	catch (Exception e){
		logger.info(e.toString());
		try
		{
			con1.close();
		}catch(Exception e1)
		{
		}
	}
	return(despacho);
}

public static int actualizarEstadoDespacho(int idDespacho)
{
	Logger logger = Logger.getLogger("log_file");
	ConexionBaseDatos con = new ConexionBaseDatos();
	Connection con1 = con.obtenerConexionBDPrincipalLocal();
	try
	{
		Statement stm = con1.createStatement();
		//Primero realizamos un select para saber si tenemos que hacer o no el update
		String select = "select * from insumo_despacho_tienda where iddespacho = " + idDespacho + " and estado ='PORCONFIRMAR'";
		ResultSet rs = stm.executeQuery(select);
		boolean actualizacion = false;
		while(rs.next())
		{
			actualizacion = true;
			break;
		}
		if(actualizacion)
		{
			String update = "update insumo_despacho_tienda set estado = 'DESPACHADO' where iddespacho =" + idDespacho;
			logger.info(update);
			stm.executeUpdate(update);
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
	return(idDespacho);
	
}

}

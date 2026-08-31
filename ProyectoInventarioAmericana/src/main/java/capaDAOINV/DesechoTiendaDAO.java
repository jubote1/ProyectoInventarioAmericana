package capaDAOINV;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModeloINV.DesechoTienda;
import conexionINV.ConexionBaseDatos;

/**
 * Clase que se encarga de implementar toda la interacci�n con la base de datos para le entidad Producto.
 * @author JuanDavid
 *
 */
public class DesechoTiendaDAO {
	
	public static ArrayList<DesechoTienda> obtenerDesechosTiendaFechas(String fechaInicial, String fechaFinal, int idTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<DesechoTienda> desechosFechas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.*, b.descripcion descdesecho, b.costo, a.idestado, e.nombre, (SELECT fecha_cambio FROM cambio_estado_aprovechable h WHERE h.idaprovechable = a.iddesecho_tienda AND h.idestado = 2 limit 1 ) AS fechacarro, (SELECT fecha_cambio FROM cambio_estado_aprovechable h WHERE h.idaprovechable = a.iddesecho_tienda AND h.idestado = 3 limit 1) AS fechabodega from desecho_tienda a, desecho b, estado_aprovechable e where a.iddesecho = b.iddesecho and a.fecha >= '" + fechaInicial + "' and a.fecha <= '" + fechaFinal +"' and idtienda = " + idTienda + " and a.idestado = e.idestado ";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idDesechoTienda;
			int numeroDesecho;
			String fecha;
			String descripcion;
			String motivo;
			int idDesecho;
			String descripcionDesecho;
			double gramos;
			double costo;
			double cantidad;
			String usuario;
			int idEstado;
			String estado;
			String fechaCarro;
			String fechaBodega;
			while(rs.next()){
				idDesechoTienda = rs.getInt("iddesecho_tienda");
				numeroDesecho = rs.getInt("numero_desecho");
				idTienda = rs.getInt("idtienda");
				fecha = rs.getString("fecha");
				descripcion = rs.getString("descripcion");
				motivo = rs.getString("motivo");
				idDesecho = rs.getInt("iddesecho");
				gramos = rs.getDouble("gramos");
				costo = rs.getDouble("costo");
				cantidad = rs.getDouble("cantidad");
				usuario = rs.getString("usuario");
				descripcionDesecho = rs.getString("descdesecho");
				idEstado = rs.getInt("idestado");
				estado = rs.getString("nombre");
				fechaCarro = rs.getString("fechacarro");
				fechaBodega = rs.getString("fechabodega");
				DesechoTienda desechoTienda = new DesechoTienda(idDesechoTienda, numeroDesecho, idTienda, fecha, descripcion, motivo, idDesecho, gramos, cantidad, usuario );
				desechoTienda.setDescripcionDesecho(descripcionDesecho);
				desechoTienda.setCosto(costo);
				desechoTienda.setIdEstado(idEstado);
				desechoTienda.setEstado(estado);
				desechoTienda.setFechaCarro(fechaCarro);
				desechoTienda.setFechaBodega(fechaBodega);
				desechosFechas.add(desechoTienda);
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
		return(desechosFechas);
		
	}
	
	public static ArrayList<DesechoTienda> obtenerDesechosFechas(String fechaInicial, String fechaFinal)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<DesechoTienda> desechosFechas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select a.*, b.descripcion descdesecho, b.costo from desecho_tienda a, desecho b where a.iddesecho = b.iddesecho and a.fecha >= '" + fechaInicial + "' and a.fecha <= '" + fechaFinal;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idDesechoTienda;
			int idTienda;
			int numeroDesecho;
			String fecha;
			String descripcion;
			String motivo;
			int idDesecho;
			String descripcionDesecho;
			double gramos;
			double costo;
			double cantidad;
			String usuario;
			while(rs.next()){
				idDesechoTienda = rs.getInt("iddesecho_tienda");
				numeroDesecho = rs.getInt("numero_desecho");
				idTienda = rs.getInt("idtienda");
				fecha = rs.getString("fecha");
				descripcion = rs.getString("descripcion");
				motivo = rs.getString("motivo");
				idDesecho = rs.getInt("iddesecho");
				gramos = rs.getDouble("gramos");
				costo = rs.getDouble("costo");
				cantidad = rs.getDouble("cantidad");
				usuario = rs.getString("usuario");
				descripcionDesecho = rs.getString("descdesecho");
				DesechoTienda desechoTienda = new DesechoTienda(idDesechoTienda, numeroDesecho, idTienda, fecha, descripcion, motivo, idDesecho, gramos, cantidad, usuario );
				desechoTienda.setDescripcionDesecho(descripcionDesecho);
				desechoTienda.setCosto(costo);
				desechosFechas.add(desechoTienda);
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
		return(desechosFechas);
		
	}
	
	public static ArrayList<DesechoTienda> obtenerDesechosTiendaFecha(String fecha, int idTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<DesechoTienda> desechosFechas = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from desecho_tienda where fecha = '" + fecha + "' and idtienda = " + idTienda;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idDesechoTienda;
			int numeroDesecho;
			String descripcion;
			String motivo;
			int idDesecho;
			double gramos;
			double cantidad;
			String usuario;
			while(rs.next()){
				idDesechoTienda = rs.getInt("iddesecho_tienda");
				numeroDesecho = rs.getInt("numero_desecho");
				idTienda = rs.getInt("idtienda");
				fecha = rs.getString("fecha");
				descripcion = rs.getString("descripcion");
				motivo = rs.getString("motivo");
				idDesecho = rs.getInt("iddesecho");
				gramos = rs.getDouble("gramos");
				cantidad = rs.getDouble("cantidad");
				usuario = rs.getString("usuario");
				DesechoTienda desechoTienda = new DesechoTienda(idDesechoTienda, numeroDesecho, idTienda, fecha, descripcion, motivo, idDesecho, gramos , cantidad, usuario);
				desechosFechas.add(desechoTienda);
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
		return(desechosFechas);
		
	}

	public static int insertarDesechoTienda(DesechoTienda des)
	{
		Logger logger = Logger.getLogger("log_file");
		int idDesechoIns = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into desecho_tienda (numero_desecho, idtienda, fecha, descripcion, motivo, iddesecho, gramos, cantidad, usuario) values (" + des.getNumeroDesecho() + ", " + des.getIdTienda() + " , '" + des.getFecha() + "' , '" + des.getDescripcion() + "' , '" + des.getMotivo() + "', " + des.getIdDesecho() + " , " + des.getGramos() + " , " + des.getCantidad() + " , '" + des.getUsuario() + "')"; 
			logger.info(insert);
			System.out.println(insert);
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idDesechoIns = rs.getInt(1);
				
	        }
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
		return(idDesechoIns);
	}


	public static void eliminarDesechoTienda (int idDesechoTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String delete = "delete from desecho_tienda  where iddesecho_tienda = " + idDesechoTienda; 
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

	

	public static DesechoTienda retornarDesechoTienda(int idDesechoTienda)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		DesechoTienda des = new DesechoTienda(0,0,0,"", "", "", 0, 0, 0, "");
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from desecho_tienda where iddesecho_tienda = " + idDesechoTienda;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int numeroDesecho = 0;
			int idTienda = 0;
			String fecha  = "";
			String descripcion = "";
			String motivo = "";
			int idDesecho = 0;
			double gramos = 0;
			double cantidad = 0;
			String usuario = "";
			while(rs.next()){
				numeroDesecho = rs.getInt("numero_desecho");
				idTienda = rs.getInt("idtienda");
				fecha = rs.getString("fecha");
				descripcion = rs.getString("descripcion");
				motivo = rs.getString("motivo");
				idDesecho = rs.getInt("iddesecho");
				gramos = rs.getDouble("gramos");
				cantidad = rs.getDouble("cantidad");
				usuario = rs.getString("usuario");
			}
			des = new DesechoTienda(idDesechoTienda, numeroDesecho, idTienda, fecha, descripcion, motivo, idDesecho, gramos, cantidad, usuario );
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
	
	
	public static boolean validarExistenciaNumeroDesecho(int numeroDesecho)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from desecho_tienda where numero_desecho = " + numeroDesecho;
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				respuesta = true;
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
		return(respuesta);
	}

	public static boolean actualizarEstadoAprovechable (int idAprovechable, int idEstado)
	{
		boolean respuesta = false;
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String update = "update desecho_tienda set idestado = " + idEstado +"  where iddesecho_tienda = " + idAprovechable; 
			logger.info(update);
			stm.executeUpdate(update);
			String insert = "insert into cambio_estado_aprovechable (idaprovechable, idestado) values(" + idAprovechable + "," + idEstado + ")";
			logger.info(insert);
			stm.executeUpdate(insert);
			stm.close();
			con1.close();
			respuesta = true;
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
		return(respuesta);
	}
	
}

package capaDAOINV;

import java.sql.Connection;
import org.apache.log4j.Logger;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import capaModeloINV.Llave;
import capaModeloINV.LlaveEvento;
import conexionINV.ConexionBaseDatos;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Clase que se encarga de la implementación de toda la interacción con la base de datos para la entidad Llave
 * @author JuanDavid
 *
 */
public class LlaveEventoDAO {
	
	/**
	 * Método que se encarga de retornar la información de todas las llaves definidas en el sistema.
	 * @return Se retorna un ArrayList con todos las llaves definidas en el sistema
	 */
	public static boolean insertarLlaveEvento(LlaveEvento llaveEvento)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean respuesta = false;
		try
		{
			Statement stm = con1.createStatement();
			String insert = "insert into llaves_evento (idllaves, idusuario_registro, idusuario_presto, observacion, evento) values("+ llaveEvento.getIdLlave() + " , " + llaveEvento.getIdUsuarioRegistro() + " , " + llaveEvento.getIdUsuarioPrestamo() + " , '" + llaveEvento.getObservacion() + "' , '" + llaveEvento.getEvento() + "')";
			stm.executeUpdate(insert);
			String update = "";
			if(llaveEvento.getEvento().equals(new String("P")))
			{
				update = "update llaves set disponible = 0 where idllave = " + llaveEvento.getIdLlave();
			}else if(llaveEvento.getEvento().equals(new String("D")))
			{
				update = "update llaves set disponible = 1 where idllave = " + llaveEvento.getIdLlave();
			}
			//Realizamos update para dejar como no disponible las llaves
			stm.executeUpdate(update);
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
	
	public static LlaveEvento consultarUltimoEventoLlave(int idLlave)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		LlaveEvento llaveEvento = new LlaveEvento();
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from llaves_evento where idllaves = " + idLlave + " order by idevento desc limit 1";
			ResultSet rs = stm.executeQuery(select);
			int idEvento;
			int idUsuarioRegistro;
			int idUsuarioPresto;
			String observacion;
			String fecha;
			String evento;
			while(rs.next())
			{
				idEvento = rs.getInt("idevento");
				idUsuarioRegistro = rs.getInt("idusuario_registro");
				idUsuarioPresto = rs.getInt("idusuario_presto");
				observacion = rs.getString("observacion");
				evento = rs.getString("evento");
				fecha = rs.getString("fecha");
				llaveEvento = new LlaveEvento(idLlave, idUsuarioRegistro, idUsuarioPresto, observacion);
				llaveEvento.setIdEvento(idEvento);
				llaveEvento.setFecha(fecha);
				llaveEvento.setEvento(evento);
				break;
			}
			rs.close();
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString() + " ERROR " + e.getMessage());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(llaveEvento);
		
	}
	
	public static ArrayList<LlaveEvento> consultarEventosLlave(int idLlave)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		ArrayList<LlaveEvento> llavesEvento = new ArrayList();
		LlaveEvento llaveEvento = new LlaveEvento();
		try
		{
			Statement stm = con1.createStatement();
			String select = "select * from llaves_evento where idllaves = " + idLlave + " order by idevento desc limit 30";
			ResultSet rs = stm.executeQuery(select);
			int idEvento;
			int idUsuarioRegistro;
			int idUsuarioPresto;
			String observacion;
			String fecha;
			String evento;
			while(rs.next())
			{
				idEvento = rs.getInt("idevento");
				idUsuarioRegistro = rs.getInt("idusuario_registro");
				idUsuarioPresto = rs.getInt("idusuario_presto");
				observacion = rs.getString("observacion");
				evento = rs.getString("evento");
				fecha = rs.getString("fecha");
				llaveEvento = new LlaveEvento(idLlave, idUsuarioRegistro, idUsuarioPresto, observacion);
				llaveEvento.setIdEvento(idEvento);
				llaveEvento.setFecha(fecha);
				llaveEvento.setEvento(evento);
				llavesEvento.add(llaveEvento);
			}
			stm.close();
			con1.close();
		}catch (Exception e){
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
		}
		return(llavesEvento);
		
	}
	
}

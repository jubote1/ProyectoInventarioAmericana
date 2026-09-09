package capaDAOINV;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModeloINV.Usuario;
import conexionINV.ConexionBaseDatos;
/**
 * Clase que se encarga de implementar toda la interacción con la base de datos para la entidad Usuario.
 * @author JuanDavid
 *
 */
public class UsuarioDAO {

	/**
	 * Método que se encarga de validar la existencia y de un usuario y su contraseña en la base de datos.
	 * @param usuario Se recibe como parámetro un objeto MOdelo Usuario, el cual trae la información base para la validación,
	 * autenticación del usuario.
	 * @return Se retorna un valor booleano que indica si el proceso de autenticación es satifactorio o no.
	 */
	public static boolean validarUsuario(Usuario usuario)
	{
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		boolean autenticado = false;
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select count(*) from usuario where nombre = '" + usuario.getNombreUsuario() + "' and password = '" + usuario.getContrasena()+"'";
			//OJO: no se registra la consulta completa porque lleva la contrasena en texto
			//plano y quedaba escrita en el archivo de log.
			logger.info("validarUsuario para el usuario: " + usuario.getNombreUsuario());
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				try{
					int cantidad = Integer.parseInt(rs.getString(1));
					autenticado = (cantidad > 0);
				}catch(Exception e){
					logger.error(e.toString());
					autenticado = false;
				}
			}
			rs.close();
			stm.close();
		}catch (Exception e){
			logger.error(e.toString());
		}
		finally
		{
			//OJO: los cierres tienen que ir aqui. Antes estaban DENTRO del while y
			//DESPUES del return(true), asi que la conexion solo se cerraba cuando el
			//login FALLABA: cada inicio de sesion exitoso dejaba una conexion dormida
			//en el servidor, y con wait_timeout en 8 horas no la reciclaba nadie.
			try
			{
				if (con1 != null) con1.close();
			}catch(Exception e1)
			{
				logger.error(e1.toString());
			}
		}
		return(autenticado);
		
	}
	
	/**
	 * Método que se encarga de validar si un usuario existe o no en la base de datos
	 * @param usuario Recibe como parámetro un objeto Modelo Usuario con base en el cual se realiza la consulta.
	 * @return Se retorna un valor booleano con base en el cual se realiza la validación del usuario en base de datos
	 * 
	 */
	public static String validarAutenticacion(Usuario usuario)
	{
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		String resultado = "";
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select administrador from usuario where nombre = '" + usuario.getNombreUsuario() + "'";
			ResultSet rs = stm.executeQuery(consulta);
			while(rs.next()){
				
				try{
					resultado = rs.getString(1);
					
				}catch(Exception e){
					
					
				}
				rs.close();
				stm.close();
				con1.close();
			}
		}catch (Exception e){
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

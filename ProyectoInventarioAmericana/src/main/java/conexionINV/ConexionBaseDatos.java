package conexionINV;
import java.sql.*;
/**
 * Clase que se encarga de la implementación de la conexión a las bases de datos del sistema contact center y la
 * base de datos de cada tienda.
 * @author JuanDavid
 *
 */
public class ConexionBaseDatos {
	
	
	
	public static void main(String args[]){
		
		ConexionBaseDatos cn = new ConexionBaseDatos();
	}

	
	
	public Connection obtenerConexionBDGeneralLocal(){
		try {
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			con = DriverManager.getConnection(
		            "jdbc:mysql://localhost/general?"
		            + "user=root&password=4m32017&serverTimezone=UTC");
			
//			con = DriverManager.getConnection(
//		            "jdbc:mysql://192.168.0.25/general?"
//		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	/**
	 * Método que implementa la conexión a la base de datos del sistema principal de contact center
	 * @return
	 */
	public Connection obtenerConexionBDPrincipalLocal(){
		try {
			/**
			 * Se realiza el registro del drive de Mysql
			 */
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			
			/**
			 * Se realiza la creación de la conexión a la base de datos
			 */
			con = DriverManager.getConnection(
		            "jdbc:mysql://localhost/inventarioamericana?"
		            + "user=root&password=4m32017&serverTimezone=UTC");
			
//			con = DriverManager.getConnection(
//		            "jdbc:mysql://172.19.0.25/inventarioamericana?"
//		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	
	/**
	 * Método que se encarga de tener conexión al sistema principal de temas generales
	 * @return
	 */
	public Connection obtenerConexionBDGeneral(){
		try {
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			con = DriverManager.getConnection(
		            "jdbc:mysql://localhost/general?"
		            + "user=root&password=4m32017&serverTimezone=UTC");
			
			//con = DriverManager.getConnection(
		    //        "jdbc:mysql://192.168.0.25/general?"
		    //        + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	
	
	public Connection obtenerConexionBDPedidosLocal(){
		try {
			/**
			 * Se realiza el registro del drive de Mysql
			 */
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			
			con = DriverManager.getConnection(
		            "jdbc:mysql://localhost/pizzaamericana?"
		            + "user=root&password=4m32017&serverTimezone=UTC");
			
//			con = DriverManager.getConnection(
//		            "jdbc:mysql://192.168.0.25/pizzaamericana?"
//		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
	public Connection obtenerConexionBDTiendaRemota(String url){
		try {
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {
			
			
			DriverManager.setLoginTimeout(10);
			con = DriverManager.getConnection(
		            "jdbc:mysql://" + url + "/tiendaamericana?"
		            + "user=root&password=4m32017&serverTimezone=UTC");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage() + " base de datos " +  url);

		}
		return(con);
	}
	
	public Connection obtenerConexionBDInventarioPOSBodega(){
		try {
			/**
			 * Se realiza el registro del drive de Mysql
			 */
		    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		    

		} catch (Exception e) {

		    System.out.println(e.toString());

		}
		
		Connection con = null;
		//...

		try {

			
			con = DriverManager.getConnection(
		            "jdbc:mysql://localhost/tiendaamericana?"
		            + "user=root&password=4m32017&serverTimezone=UTC");
			
//			con = DriverManager.getConnection(
//		            "jdbc:mysql://192.168.0.25/pizzaamericana?"
//		            + "user=root&password=4m32017");

		    // Otros y operaciones sobre la base de datos...

		} catch (SQLException ex) {

		    // Mantener el control sobre el tipo de error
		    System.out.println("SQLException: " + ex.getMessage());

		}
		return(con);
	}
	
}

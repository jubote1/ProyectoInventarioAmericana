package capaDAOINV;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModeloINV.CapacitacionEmpleado;
import capaModeloINV.Desecho;
import conexionINV.ConexionBaseDatos;

/**
 * Clase que se encarga de implementar toda la interacción con la base de datos para le entidad Producto.
 * @author JuanDavid
 *
 */
public class CapacitacionEmpleadoDAO {
	
	public static ArrayList<CapacitacionEmpleado> consultarCapacitaciones(String fecha)
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<CapacitacionEmpleado> capacitaciones = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fecha = new SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			Statement stm = con1.createStatement();
			String consulta = "select * from capacitacion_empleado where fecha = '" + fecha + "'";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idCapacitacionEmp;
			int id;
			String nombreLargo;
			String horaInicio;
			String horaFinal;
			String observacion;
			CapacitacionEmpleado capEmp = new CapacitacionEmpleado(0,0,"","","","", "");
			while(rs.next()){
				idCapacitacionEmp = rs.getInt("idcapacitacion_emp");
				id = rs.getInt("id");
				nombreLargo = rs.getString("nombre_largo");
				horaInicio = rs.getString("hora_inicio");
				horaFinal = rs.getString("hora_final");
				observacion = rs.getString("observacion");
				capEmp = new CapacitacionEmpleado(idCapacitacionEmp,id,fecha,horaInicio,horaFinal,observacion,nombreLargo);
				capacitaciones.add(capEmp);
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
		return(capacitaciones);
		
	}
	
	public static int insertarCapacitacionEmpleado(CapacitacionEmpleado capEmp)
	{
		Logger logger = Logger.getLogger("log_file");
		int idCapEmp = 0;
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		String fecha = capEmp.getFecha();
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fecha = new SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			Statement stm = con1.createStatement();
			String insert = "insert into capacitacion_empleado (id, fecha, hora_inicio, hora_final, observacion, nombre_largo) values (" + capEmp.getIdEmpleado() + " , '" + fecha + "' , '" + capEmp.getHoraInicio() + "' , '" + capEmp.getHoraFinal() + "' , '" + capEmp.getObservacion() + "' , '" + capEmp.getNombreLargo() + "')"; 
			logger.info(insert);
			stm.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
			System.out.println(insert);
			ResultSet rs = stm.getGeneratedKeys();
			if (rs.next()){
				idCapEmp = rs.getInt(1);
	        }
			stm.close();
			con1.close();
		}
		catch (Exception e){
			logger.error(e.toString());
			System.out.println(e.toString());
			try
			{
				con1.close();
			}catch(Exception e1)
			{
			}
			return(0);
		}
		return(idCapEmp);
	}

}

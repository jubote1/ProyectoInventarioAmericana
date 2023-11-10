package capaDAOINV;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import capaModeloINV.Desecho;
import capaModeloINV.VarianzaDiferencia;
import conexionINV.ConexionBaseDatos;

/**
 * Clase que se encarga de implementar toda la interacción con la base de datos para le entidad Producto.
 * @author JuanDavid
 *
 */
public class VarianzaDiferenciaDAO {
	
	public static ArrayList<VarianzaDiferencia> obtenerVarianzaDiferencia()
	{
		Logger logger = Logger.getLogger("log_file");
		ArrayList<VarianzaDiferencia> varianzaDiferencias = new ArrayList<>();
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "select * from varianza_diferencia";
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idInsumo;
			double cantidad;
			while(rs.next()){
				idInsumo = rs.getInt("idinsumo");
				cantidad = rs.getDouble("cantidad");
				VarianzaDiferencia diferencia = new VarianzaDiferencia(idInsumo, cantidad);
				varianzaDiferencias.add(diferencia);
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
		return(varianzaDiferencias);
		
	}
	
}

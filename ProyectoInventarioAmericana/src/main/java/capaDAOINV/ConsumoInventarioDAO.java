package capaDAOINV;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import capaModeloINV.Insumo;
import capaModeloINV.InsumoConsumido;
import capaModeloINV.InsumoConsumidoSemana;
import conexionINV.ConexionBaseDatos;

public class ConsumoInventarioDAO {
	
	public static ArrayList<InsumoConsumidoSemana> consultarConsumos(int idTienda, String fechaIni, String fechaFin, String tipoConsulta)
	{
		//Realizamos el cambio del formato de la fecha
		//Tratamiento de la fecha para consulta
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fechaFinal = fechaFin;
		String fechaInicial = "";
		try
		{
			//Realizamos proceso para fecha inicial
			fechaTemporal = new SimpleDateFormat("dd/MM/yyyy").parse(fechaIni);
			fechaInicial = formatoFinal.format(fechaTemporal);
		}catch(Exception e){
			System.out.println(e.toString());
		}
		Logger logger = Logger.getLogger("log_file");
		ConexionBaseDatos con = new ConexionBaseDatos();
		Connection con1 = con.obtenerConexionBDPrincipalLocal();
		InsumoConsumido insumoCon = new InsumoConsumido(0, "", 0.0, "", "	");
		ArrayList<InsumoConsumido> insumosConsumidos = new ArrayList();
		ArrayList<InsumoConsumidoSemana> insumosSemana = new ArrayList();
		try
		{
			Statement stm = con1.createStatement();
			String consulta = "SELECT a.idinsumo, b.nombre_insumo, a.cantidad, a.fecha_consumo, \r\n" + 
					"case DAYOFWEEK(a.fecha_consumo)  \r\n" + 
					"	when 1 then 'Domingo'\r\n" + 
					"	when 2 then 'Lunes'\r\n" + 
					"	when 3 then 'Martes'\r\n" + 
					"	when 4 then 'Miercoles' \r\n" + 
					"	when 5 then 'Jueves'\r\n" + 
					"	when 6 then 'Viernes'\r\n" + 
					"	when 7 then 'Sabado'\r\n" + 
					"END AS dia\r\n" + 
					"FROM consumo_inventario a, insumo b WHERE a.idinsumo = b.idinsumo AND a.fecha_consumo >= '"+ fechaInicial +"' AND a.fecha_consumo <= '" + fechaFinal + "'";
			//Validamos si viene la tienda
			if(idTienda > 0)
			{
				consulta = consulta + " and a.idtienda = " + idTienda;
			}
			if(tipoConsulta.equals(new String("resumida")))
			{
				consulta = consulta + " " + "and b.varianza_resumida = 1 ";
			}
			logger.info(consulta);
			ResultSet rs = stm.executeQuery(consulta);
			int idInsumo = 0;
			String nombreInsumo = "";
			double cantidad;
			String fecha;
			String dia;
			while(rs.next()){
				idInsumo = rs.getInt("idinsumo");
				nombreInsumo = rs.getString("nombre_insumo");
				try
				{
					cantidad = rs.getDouble("cantidad");
				}catch(Exception e)
				{
					cantidad = 0;
				}
				fecha = rs.getString("fecha_consumo");
				dia = rs.getString("dia");
				insumoCon = new InsumoConsumido(idInsumo, nombreInsumo, cantidad, fecha, dia);
				insumosConsumidos.add(insumoCon);
			}
			stm.close();
			con1.close();
			//Posteriormente vamos a realizar un procesamiento para organizar la información por día
			InsumoConsumido insumoConTemp;
			InsumoConsumidoSemana insumoConSemTemp;
			boolean encontrado;
			for(int i = 0; i < insumosConsumidos.size(); i++)
			{
				encontrado = false;
				insumoConTemp = insumosConsumidos.get(i);
				for(int j = 0; j < insumosSemana.size(); j++)
				{
					insumoConSemTemp = insumosSemana.get(j);
					if(insumoConTemp.getIdInsumo() == insumoConSemTemp.getIdInsumo())
					{
						encontrado = true;
						if(insumoConTemp.getDia().equals(new String("Lunes")))
						{
							insumoConSemTemp.setLunes(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Martes")))
						{
							insumoConSemTemp.setMartes(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Miercoles")))
						{
							insumoConSemTemp.setMiercoles(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Jueves")))
						{
							insumoConSemTemp.setJueves(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Viernes")))
						{
							insumoConSemTemp.setViernes(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Sabado")))
						{
							insumoConSemTemp.setSabado(insumoConTemp.getCantidad());
						}else if(insumoConTemp.getDia().equals(new String("Domingo")))
						{
							insumoConSemTemp.setDomingo(insumoConTemp.getCantidad());
						}
						insumosSemana.set(j, insumoConSemTemp);
						break;
					}
				}
				if(!encontrado)
				{
					insumoConSemTemp = new InsumoConsumidoSemana(insumoConTemp.getIdInsumo(), insumoConTemp.getNombre(),0,0,0,0,0,0,0);
					if(insumoConTemp.getDia().equals(new String("Lunes")))
					{
						insumoConSemTemp.setLunes(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Martes")))
					{
						insumoConSemTemp.setMartes(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Miercoles")))
					{
						insumoConSemTemp.setMiercoles(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Jueves")))
					{
						insumoConSemTemp.setJueves(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Viernes")))
					{
						insumoConSemTemp.setViernes(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Sabado")))
					{
						insumoConSemTemp.setSabado(insumoConTemp.getCantidad());
					}else if(insumoConTemp.getDia().equals(new String("Domingo")))
					{
						insumoConSemTemp.setDomingo(insumoConTemp.getCantidad());
					}
					insumosSemana.add(insumoConSemTemp);
				}
			}
			
			
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
		}
		return(insumosSemana);
	}
	

}

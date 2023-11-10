package capaControladorINV;

import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.*;

import capaDAOINV.ControlPorcionEmpleadoDAO;
import capaDAOINV.TiendaDAO;
import capaModeloINV.ControlPorcionEmpleado;
import capaModeloINV.Tienda;

public class TiendaCtrl {
	
	public String obtenerTiendas(){
		JSONArray listJSON = new JSONArray();
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
		
		for (Tienda tienda : tiendas) {
			JSONObject cadaViajeJSON = new JSONObject();
			cadaViajeJSON.put("id", tienda.getIdTienda());
			cadaViajeJSON.put("nombre", tienda.getNombreTienda());
			cadaViajeJSON.put("destino", tienda.getDsnTienda());
			listJSON.add(cadaViajeJSON);
		}
		return listJSON.toJSONString();
	}
	public String obtenerUrlTienda(int idtienda){
		JSONArray listJSON = new JSONArray();
		Tienda tienda = TiendaDAO.obtenerUrlTienda(idtienda);
		JSONObject urlJSON = new JSONObject();
		urlJSON.put("urltienda", tienda.getUrl());
		urlJSON.put("dsnodbc", tienda.getDsnTienda());
		listJSON.add(urlJSON);
		return listJSON.toJSONString();
	}

	public  String insertarControlPorcionEmpleado(ControlPorcionEmpleado controlPorcion)
	{
		ControlPorcionEmpleadoDAO.insertarControlPorcionEmpleado(controlPorcion);
		JSONObject respuestaJSON = new JSONObject();
		respuestaJSON.put("resultado", "OK");
		return(respuestaJSON.toJSONString());
	}
	
	
	public  String eliminarControlPorcionEmpleado(int idUsuario, int idTienda, int idPedido, int idDetallePedido, String fecha)
	{
		ControlPorcionEmpleadoDAO.eliminarControlPorcionEmpleado(idUsuario, idTienda, idPedido, idDetallePedido, fecha);
		JSONObject respuestaJSON = new JSONObject();
		respuestaJSON.put("resultado", "OK");
		return(respuestaJSON.toJSONString());
	}
	
	public String validarControlPorcionEmpleado(int idUsuario, String fecha)
	{
		boolean respuesta = ControlPorcionEmpleadoDAO.validarControlPorcionEmpleado(idUsuario,fecha);
		JSONObject respuestaJSON = new JSONObject();
		respuestaJSON.put("resultado", respuesta);
		return(respuestaJSON.toJSONString());
	}
	
	public String consultarControlPorcionEmpleadoDia(String fecha, int idTienda)
	{
		ArrayList<ControlPorcionEmpleado> controlPorcion =  ControlPorcionEmpleadoDAO.consultarControlPorcionEmpleadoDia(fecha,idTienda);
		JSONArray arregloContPorcion = new JSONArray();
		JSONObject jsonContPorcion = new JSONObject();
		ControlPorcionEmpleado contPorcionTemp;
		for(int i = 0; i < controlPorcion.size(); i++)
		{
			contPorcionTemp = (ControlPorcionEmpleado)controlPorcion.get(i);
			jsonContPorcion = new JSONObject();
			jsonContPorcion.put("idusuario", contPorcionTemp.getIdUsuario());
			jsonContPorcion.put("idtienda", contPorcionTemp.getIdTienda());
			jsonContPorcion.put("idpedido", contPorcionTemp.getIdPedido());
			jsonContPorcion.put("iddetallepedido", contPorcionTemp.getIdDetallePedido());
			jsonContPorcion.put("fecha", contPorcionTemp.getFecha());
			jsonContPorcion.put("nombreempleado", contPorcionTemp.getNombreEmpleado());
			arregloContPorcion.add(jsonContPorcion);
		}
		return(arregloContPorcion.toJSONString());
	}

}

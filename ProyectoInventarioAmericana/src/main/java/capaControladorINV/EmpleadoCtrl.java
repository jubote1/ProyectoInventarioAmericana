package capaControladorINV;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.CapacitacionEmpleadoDAO;
import capaDAOINV.ConsumoInventarioDAO;
import capaDAOINV.DesechoDAO;
import capaDAOINV.DesechoTiendaDAO;
import capaDAOINV.GeneralDAO;
import capaDAOINV.InsumoDAO;
import capaDAOINV.InsumoDespachoTiendaDAO;
import capaDAOINV.InsumoDespachoTiendaDetalleDAO;
import capaDAOINV.InsumoRequeridoTiendaDAO;
import capaDAOINV.InventarioDAO;
import capaDAOINV.LlaveDAO;
import capaDAOINV.ParametrosDAO;
import capaDAOINV.TiendaDAO;
import capaModeloINV.*;
import utilidadesINV.ControladorEnvioCorreo;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class EmpleadoCtrl {
	

	public String insertarCapacitacionEmpleado(CapacitacionEmpleado capEmp)
	{
		int intRespuesta  = CapacitacionEmpleadoDAO.insertarCapacitacionEmpleado(capEmp);
		String respuesta = "";
		if(intRespuesta > 0)
		{
			respuesta = "OK";
		}else
		{
			respuesta = "NOK";
		}
		return(respuesta);
	}
	
	
	public String consultarCapacitaciones(String fecha)
	{
		ArrayList<CapacitacionEmpleado> capacitaciones  = CapacitacionEmpleadoDAO.consultarCapacitaciones(fecha);
		JSONArray listJSON = new JSONArray();
		JSONObject cadaCapJSON = new JSONObject();
		for(int i = 0; i < capacitaciones.size(); i++)
		{
			CapacitacionEmpleado capTemp = capacitaciones.get(i);
			cadaCapJSON = new JSONObject();
			cadaCapJSON.put("idcapacitacionemp", capTemp.getIdCapacitacionEmp());
			cadaCapJSON.put("id", capTemp.getIdEmpleado());
			cadaCapJSON.put("nombrelargo", capTemp.getNombreLargo());
			cadaCapJSON.put("fecha", fecha);
			cadaCapJSON.put("idcapacitacionemp", fecha);
			cadaCapJSON.put("horainicio", capTemp.getHoraInicio());
			cadaCapJSON.put("horafinal", capTemp.getHoraFinal());
			cadaCapJSON.put("observacion", capTemp.getObservacion());
			listJSON.add(cadaCapJSON);
		}
		return(listJSON.toJSONString());
	}
	
	

}






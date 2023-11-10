package capaControladorINV;

import java.awt.Color;
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
import capaDAOINV.LlaveEventoDAO;
import capaDAOINV.ParametrosDAO;
import capaDAOINV.RetiroInventarioDetalleTmpDAO;
import capaDAOINV.RetiroInventarioTmpDAO;
import capaDAOINV.TiendaDAO;
import capaDAOINV.VarianzaDiferenciaDAO;
import capaModeloINV.*;
import utilidadesINV.ControladorEnvioCorreo;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class InventarioCtrl {
	
	/** ESTE MÉTDO ATIENDE LA CAPA DE PRESENTACIÓN
	 * Método que se encarga de definir la lógica de negocio para armar los inventarios de surtir una pizzeria en cuanto
	 * recupera el inventario actual de la tienda y lo cruza contra el deber ser y de esta manera precarga unos valores predifidos
	 * para ser mostrardos en la capa de presentación.
	 * @param idtienda valor con el idtienda de la cual se desea armar el inventario
	 * @param fecha para la cual se desea surtir con base en esta fecha es que se retorna el valor de los inventarios de la tienda, 
	 * adicionalmente se cálcula el día de la semana que se va a surtir, siendo el domingo el primer día de la semana.
	 * @return Se retornará un valor string en formato JSON con la base para ser desplegada en la capa de presentación.
	 */
	public String CalcularInventarioTienda(int idtienda, String fecha)
	{
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONArray listJSON = new JSONArray();
		String fechaFormato = "";
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fechaFormato = new  SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fechaFormato);
		ArrayList<InsumoRequeridoTienda> insRequeridosTienda = InventarioDAO.ObtenerInsumosRequeridosTienda(idtienda, diasemana);
		for (InsumoRequeridoTienda insReqTienda : insRequeridosTienda)
		{
			JSONObject cadaJSON = new JSONObject();
			cadaJSON.put("idinsumo", insReqTienda.getIdinsumo());
			cadaJSON.put("requerido", insReqTienda.getCantidad());
			cadaJSON.put("unidadmedida", insReqTienda.getUnidadMedida());
			double cantidadLlevar = 0;
			int cantidadxcanasta;
			String manejacanastas;
			for (InsumoTienda insTienda : insumosTienda)
			{
				if(insReqTienda.getIdinsumo() == insTienda.getIdinsumo())
				{
					cadaJSON.put("nombreinsumo", insTienda.getNombreInsumo());
					cadaJSON.put("cantidadtienda", insTienda.getCantidad());
					if(insReqTienda.getManejacanasta().equals("S"))
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						int canastas = 0;
						int residuocanastas = 0;
						if(cantidadLlevar > 0)
						{
							canastas = (int)cantidadLlevar / insReqTienda.getCantidadxcanasta();
							residuocanastas = (int)cantidadLlevar % insReqTienda.getCantidadxcanasta();
						}
						if (residuocanastas > 0)
						{
							canastas++;
							cantidadLlevar = insReqTienda.getCantidadxcanasta()*canastas;
						}
						cadaJSON.put("cantidadcanastas", canastas);
					}
					else
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						cadaJSON.put("cantidadcanastas", 0);
					}
									
					if (cantidadLlevar < 0)
					{
						cantidadLlevar = 0;
					}
					break;
				}
				
			}
			cadaJSON.put("cantidadllevar", cantidadLlevar);
			cadaJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
			cadaJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
			cadaJSON.put("manejacanastas", insReqTienda.getManejacanasta());
			listJSON.add(cadaJSON);
		}
		
		return(listJSON.toString());
	}
	
	public String CalcularInventarioTiendaSinFecha(int idtienda, String fecha)
	{
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONArray listJSON = new JSONArray();
		String fechaFormato = "";
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fechaFormato = new  SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomaticoSinFecha(idtienda);
		ArrayList<InsumoRequeridoTienda> insRequeridosTienda = InventarioDAO.ObtenerInsumosRequeridosTienda(idtienda, diasemana);
		for (InsumoRequeridoTienda insReqTienda : insRequeridosTienda)
		{
			JSONObject cadaJSON = new JSONObject();
			cadaJSON.put("idinsumo", insReqTienda.getIdinsumo());
			cadaJSON.put("requerido", insReqTienda.getCantidad());
			cadaJSON.put("unidadmedida", insReqTienda.getUnidadMedida());
			double cantidadLlevar = 0;
			int cantidadxcanasta;
			String manejacanastas;
			for (InsumoTienda insTienda : insumosTienda)
			{
				if(insReqTienda.getIdinsumo() == insTienda.getIdinsumo())
				{
					cadaJSON.put("nombreinsumo", insTienda.getNombreInsumo());
					cadaJSON.put("cantidadtienda", insTienda.getCantidad());
					if(insReqTienda.getManejacanasta().equals("S"))
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						int canastas = 0;
						int residuocanastas = 0;
						if(cantidadLlevar > 0)
						{
							canastas = (int)cantidadLlevar / insReqTienda.getCantidadxcanasta();
							residuocanastas = (int)cantidadLlevar % insReqTienda.getCantidadxcanasta();
						}
						if (residuocanastas > 0)
						{
							canastas++;
							cantidadLlevar = insReqTienda.getCantidadxcanasta()*canastas;
						}
						cadaJSON.put("cantidadcanastas", canastas);
					}
					else
					{
						cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
						cadaJSON.put("cantidadcanastas", 0);
					}
									
					if (cantidadLlevar < 0)
					{
						cantidadLlevar = 0;
					}
					break;
				}
				
			}
			cadaJSON.put("cantidadllevar", cantidadLlevar);
			cadaJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
			cadaJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
			cadaJSON.put("manejacanastas", insReqTienda.getManejacanasta());
			listJSON.add(cadaJSON);
		}
		
		return(listJSON.toString());
	}
	
	public String verificarExistenciaDespachoTienda(int idtienda, String fecha)
	{
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONObject respuesta = new JSONObject();
		String fechaFormato = "";
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			fechaFormato = new  SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		boolean existe = InsumoDespachoTiendaDAO.existeInsumoDespachoTienda(idtienda, fechaFormato);
		respuesta.put("existe", existe);
		return(respuesta.toJSONString());
	}
	
	/**
	 * Método que se encarga de la conformación del despacho para la modificación de este en caso de que se requiera
	 * @param idDespacho
	 * @return
	 */
	public String obtenerDespachoModificar(int idDespacho)
	{
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		ArrayList<InsumoDespachoTiendaDetalle> insumosDespachados = InsumoDespachoTiendaDetalleDAO.obtenerDetalleDespachoTiendaCompleto(idDespacho);
		ArrayList<Insumo> insumos = InsumoDAO.retornarInsumos();
		JSONArray listJSON = new JSONArray();
		for (InsumoDespachoTiendaDetalle cadaInsDesp : insumosDespachados)
		{
			JSONObject cadaJSON = new JSONObject();
			//Recorremos el arreglo de insumos para recuperar el insumo
			for (Insumo cadaInsumo : insumos)
			{
				//Coinciden y podemos extraer la información de los insumos
				if(cadaInsumo.getIdinsumo() == cadaInsDesp.getIdInsumo())
				{
					cadaJSON.put("iddespachodetalle", cadaInsDesp.getIdDespachoDetalle());
					cadaJSON.put("idinsumo", cadaInsDesp.getIdInsumo());
					cadaJSON.put("unidadmedida", cadaInsumo.getUnidadMedida());
					cadaJSON.put("nombreinsumo", cadaInsumo.getNombre());
					cadaJSON.put("categoria", cadaInsumo.getCategoria());
					cadaJSON.put("nombrecontenedor", cadaInsumo.getNombreContenedor());
					cadaJSON.put("cantidadxcanasta", cadaInsumo.getCantidaxcanasta());
					cadaJSON.put("manejacanastas", cadaInsumo.getManejacanasta());
					cadaJSON.put("color", cadaInsDesp.getColor());
					cadaJSON.put("lote", cadaInsDesp.getLote());
					cadaJSON.put("estado", cadaInsDesp.getEstado());
					cadaJSON.put("colorcategoria", cadaInsumo.getColorCategoria());
					double cantidadLlevar = cadaInsDesp.getCantidad();
					if(cadaInsumo.getManejacanasta().equals("S"))
					{
						int canastas = (int)cantidadLlevar / cadaInsumo.getCantidaxcanasta();
						cadaJSON.put("cantidadcanastas", canastas);
					}
					else
					{
						cadaJSON.put("cantidadcanastas", 0);
					}
					cadaJSON.put("cantidadllevar", cantidadLlevar);
					break;
				}
			}
			listJSON.add(cadaJSON);
		}
		return(listJSON.toString());
	}
	
	
	public String ObtenerInsumosRequeridosTienda(int idtien, int diasemana)
	{
		JSONArray listJSON = new JSONArray();
		ArrayList<InsumoRequeridoTienda> insReq = InventarioDAO.ObtenerInsumosRequeridosTienda(idtien, diasemana);
		for (InsumoRequeridoTienda insReqTienda : insReq)
		{
			JSONObject cadaInsReqJSON = new JSONObject();
			cadaInsReqJSON.put("idinsumo", insReqTienda.getIdinsumo());
			cadaInsReqJSON.put("nombreinsumo", insReqTienda.getNombreInsumo());
			cadaInsReqJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
			cadaInsReqJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
			cadaInsReqJSON.put("cantidad", insReqTienda.getCantidad());
			cadaInsReqJSON.put("cantidadminima", insReqTienda.getCantidadMinima());
			listJSON.add(cadaInsReqJSON);
		}
		return(listJSON.toJSONString());
	}
	
	public String insertarActualizarInsumReqTienda(InsumoRequeridoTienda ins)
	{
		InsumoRequeridoTiendaDAO.insertarActualizarInsumReqTienda(ins);
		JSONArray listJSON = new JSONArray();
		JSONObject insReqJSON = new JSONObject();
		insReqJSON.put("respuesta", "exitoso");
		listJSON.add(insReqJSON);
		return(listJSON.toJSONString());
	}
	
	/**
	 * Método que se encarga de calgular los inventarios a llevar a una tienda y generar el excel correspondiente
	 * @param idtienda Se recibe el idtienda de la cual se calculara el inventario a llevar
	 * @param fecha Se recibe parámetro fecha de la cual se cálcular el inventario
	 * @return
	 */
	public String CalcularInventarioTiendaFormatoExcel(int idtienda, String fecha)
	{
		String rutaArchivoGenerado="";
		String rutaArchivoBD = ParametrosDAO.obtenerParametroTexto("RUTAINV");
		String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
		//obtenemos día de la semana para recuperar inventario requerido de la tienda
		// Creamos una instancia del calendario
		GregorianCalendar cal = new GregorianCalendar();
		int diasemana = 0;
		JSONArray listJSON = new JSONArray();
		try
		{
			Date fecha1 = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e)
		{
			System.out.println(e.toString() + e.getMessage() + e.getStackTrace());
		}
		String nombreTienda = TiendaDAO.obtenerNombreTienda(idtienda);
		//Creamos el libro en Excel y la hoja en cuestión, definimos los encabezados.
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("inventarioSurtir");
		sheet.setColumnWidth(0, 7500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 4500);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 5500);
		sheet.setColumnWidth(5, 5500);
		sheet.setColumnWidth(6, 5500);
		String[] headers = new String[]{
	            "Producto",
	            "Cantidad \n Tienda",
	            "Cantidad \n Sugerida a Llevar",
	            "Empaque"
	        };
		
		try
		{
			   rutaArchivoGenerado = rutaArchivoBD + "InventarioSurtir"+ nombreTienda +".xls";
			   
			   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
			   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "InventarioSurtir"+ nombreTienda +".xls";
				
				//Esta parte de recuperación de los insumos tienda se establece control dado que está recuperando constantemente
			   boolean bandRecuperoInsumos = false;
			   int contadorReintentos = 1;
			   ArrayList<InsumoTienda> insumosTienda = new ArrayList();
			   
			   /**
			    * Este ciclo while tiene como objetivo realizar reintentos en caso de que no se encuentre información en la tabla
			    * esto se puede dar en el momento en que corra al mismo tiempo el reporte de inventarios y el servicio que trae los 
			    * inventario de una tienda
			    */
			   while(!bandRecuperoInsumos)
			   {
				   insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
				   if ((insumosTienda.size() > 0)|| contadorReintentos > 3)
				   {
					   bandRecuperoInsumos = true;
				   }
				   else
				   {
					   Thread.sleep(10000);
					   contadorReintentos++;
				   }
			   }
				//ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
				ArrayList<InsumoRequeridoTienda> insRequeridosTienda = InventarioDAO.ObtenerInsumosRequeridosTienda(idtienda, diasemana);
				//Contralaremos la fila en la que vamos con la variable fila
				int fila = 0;
				int filasInforme = 0;
				int insumorequeridos = insRequeridosTienda.size();
				//Definimos un arreglo donde iremos dejando los datos
				Object[][] data = new Object[insumorequeridos][5];
				for (InsumoRequeridoTienda insReqTienda : insRequeridosTienda)
				{
					//instanciamos el arreglo donde llevaremos la información para el excel
					//data = new Object[insRequeridosTienda.size()][5];
					JSONObject cadaJSON = new JSONObject();
					cadaJSON.put("idinsumo", insReqTienda.getIdinsumo());
					cadaJSON.put("requerido", insReqTienda.getCantidad());
					cadaJSON.put("unidadmedida", insReqTienda.getUnidadMedida());
					double cantidadLlevar = 0;
					int cantidadxcanasta;
					String manejacanastas;
					for (InsumoTienda insTienda : insumosTienda)
					{
						if(insReqTienda.getIdinsumo() == insTienda.getIdinsumo())
						{
							filasInforme++;
							data[fila][0]= new String( insTienda.getNombreInsumo());
							cadaJSON.put("nombreinsumo", insTienda.getNombreInsumo());
							//EN este punto llenamos lo que tiene la tienda
							data[fila][1]= insTienda.getCantidad();
							cadaJSON.put("cantidadtienda", insTienda.getCantidad());
							if(insReqTienda.getManejacanasta().equals("S"))
							{
								//En este punto deberemos de controlar como se maneja el insumo si este controla o no
								// por cantidad y no por minimo de almacenamiento
								if(insTienda.getControlCantidad() == 1)
								{
									//Si se hace el control por cantidad Mínima entonces se valida si lo que tiene 
									//la tienda es menor o igual a lo que se debe tener como cantidad mínima
									if(insTienda.getCantidad() <= insReqTienda.getCantidadMinima())
									{
										//Si esto se cumple se debe llevar el valor de insumo requerido con el valor de cantidad
										cantidadLlevar = insReqTienda.getCantidad();
									}
									else
									{
										// sino se tiene menos del mínimo entonces no se debe llevar nada
										cantidadLlevar = 0;
									}
								}
								else{
									cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
								}
								int canastas = (int)cantidadLlevar / insReqTienda.getCantidadxcanasta();
								int residuocanastas = (int)cantidadLlevar % insReqTienda.getCantidadxcanasta();
								if (residuocanastas > 0)
								{
									canastas++;
									cantidadLlevar = insReqTienda.getCantidadxcanasta()*canastas;
								}
								if (cantidadLlevar > 0)
								{
									cadaJSON.put("cantidadcanastas", canastas);
									data[fila][3]= canastas +  insReqTienda.getNombrecontenedor();
								}
								else
								{
									cadaJSON.put("cantidadcanastas", "");
									data[fila][3]= "";
								}
								
							}
							else
							{
								//Validamos si el insumo tienda hace el control cantidad Minima
								if(insTienda.getControlCantidad() == 1)
								{
									//Si se hace el control por cantidad Mínima entonces se valida si lo que tiene 
									//la tienda es menor o igual a lo que se debe tener como cantidad mínima
									if(insTienda.getCantidad() <= insReqTienda.getCantidadMinima())
									{
										//Si esto se cumple se debe llevar el valor de insumo requerido con el valor de cantidad
										cantidadLlevar = insReqTienda.getCantidad();
									}
									else
									{
										// sino se tiene menos del mínimo entonces no se debe llevar nada
										cantidadLlevar = 0;
									}
								}
								else{
									cantidadLlevar =  insReqTienda.getCantidad() - insTienda.getCantidad();
								}
								cadaJSON.put("cantidadcanastas", 0);
								data[fila][3]= 0;
							}
											
							if (cantidadLlevar < 0)
							{
								cantidadLlevar = 0;
							}
							break;
						}
						
						
					}
					cadaJSON.put("cantidadllevar", cantidadLlevar);
					data[fila][2]= cantidadLlevar;
					cadaJSON.put("nombrecontenedor", insReqTienda.getNombrecontenedor());
					cadaJSON.put("cantidadxcanasta", insReqTienda.getCantidadxcanasta());
					cadaJSON.put("manejacanastas", insReqTienda.getManejacanasta());
					listJSON.add(cadaJSON);
					//Aumentamos en uno el valor de la variable fila
					fila++;
				}
				
				//Agregamos nombre del reporte
				//Creamos el estilo para el nombre del reporte
				Font whiteFont = workbook.createFont();
	            whiteFont.setColor(IndexedColors.BLUE.index);
	            whiteFont.setFontHeightInPoints((short) 14.00);
	            whiteFont.setBold(true);
	            HSSFCellStyle cellheader = workbook.createCellStyle();
	            cellheader.setWrapText(true);
	            cellheader.setFont(whiteFont);
	            cellheader.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la segunda fila de información
	            Font fontSegFila = workbook.createFont();
	            fontSegFila.setColor(IndexedColors.ORANGE.index);
	            fontSegFila.setFontHeightInPoints((short) 10.00);
	            fontSegFila.setBold(true);
	            HSSFCellStyle cellInfoReporte = workbook.createCellStyle();
	            cellInfoReporte.setBorderBottom(BorderStyle.THIN);
	            cellInfoReporte.setBorderTop(BorderStyle.THIN);
	            cellInfoReporte.setBorderLeft(BorderStyle.THIN);
	            cellInfoReporte.setBorderRight(BorderStyle.THIN);
	            cellInfoReporte.setWrapText(true);
	            cellInfoReporte.setFont(fontSegFila);
	            cellInfoReporte.setAlignment(HorizontalAlignment .CENTER);
	            	            
	            //Creamos el estilo para la tercer fila de encabezados
	            Font fontTerFila = workbook.createFont();
	            //fontTerFila.setColor(IndexedColors.ORANGE.index);
	            fontTerFila.setFontHeightInPoints((short) 10.00);
	            fontTerFila.setBold(true);
	            HSSFCellStyle styleEnc = workbook.createCellStyle();
	            styleEnc.setBorderBottom(BorderStyle.THIN);
	            styleEnc.setBorderTop(BorderStyle.THIN);
	            styleEnc.setBorderLeft(BorderStyle.THIN);
	            styleEnc.setBorderRight(BorderStyle.THIN);
	            styleEnc.setWrapText(true);
	            styleEnc.setFont(fontTerFila);
	            styleEnc.setAlignment(HorizontalAlignment .CENTER);
	            
	            //Creamos el estilo para la informacion del reporte
	            HSSFCellStyle styleInfRep = workbook.createCellStyle();
	            styleInfRep.setBorderBottom(BorderStyle.THIN);
	            styleInfRep.setBorderTop(BorderStyle.THIN);
	            styleInfRep.setBorderLeft(BorderStyle.THIN);
	            styleInfRep.setBorderRight(BorderStyle.THIN);
	            styleInfRep.setWrapText(true);
	                        
	            //NOMBRE DEL REPORTE
	            HSSFRow headerRow = sheet.createRow((short) 0);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
	            Cell cellHeader = headerRow.createCell((short) 0);
	            cellHeader.setCellValue(new HSSFRichTextString("LISTADO DE INSUMOS POR PUNTO DE VENTA \n" + nombreTienda ));
	            headerRow.setHeight((short)1000);
	            cellHeader.setCellStyle(cellheader);
	            
	            //Realizamos la adición de la imagen del logo de pizza americana
	            InputStream inputStream = new FileInputStream(rutaImagenReporte);
	            byte[] imageBytes = IOUtils.toByteArray(inputStream);
	            int pictureIdx = workbook.addPicture(imageBytes, workbook.PICTURE_TYPE_PNG);
	            //close the input stream
	            //Returns an object that handles instantiating concrete classes
	            CreationHelper helper = workbook.getCreationHelper();
	            //Creates the top-level drawing patriarch.
	            Drawing drawing = sheet.createDrawingPatriarch();
	            //Create an anchor that is attached to the worksheet
	            ClientAnchor anchor = helper.createClientAnchor();
	            //set top-left corner for the image
	             anchor.setDx1(0);
	            anchor.setDy1(0);
	            anchor.setDx2(1023);
	            anchor.setDy2(6000);
	            anchor.setCol1(5);
	            anchor.setRow1(0);
	            anchor.setCol2(5);
	            anchor.setRow2(1);
	            //Creates a picture
	            Picture pict = drawing.createPicture(anchor, pictureIdx);
	            //Reset the image to the original size
	            pict.resize();
	            
	            //Aplicamos los bordes a la región merge
	            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            
	            //Para la imagen
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$1:$G$1"));
	          //Aplicamos los bordes a la región merge
	            cellRangeAddress = new CellRangeAddress(0, 0, 4, 6);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            
	            //Etiquetas de segunda linea de informaicón 
	            HSSFRow equitetasInfReporte = sheet.createRow(1);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$B$2"));
	            //Aplicamos los bordes a la región merge
	            cellRangeAddress = new CellRangeAddress(1, 1, 0, 1);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$C$2:$D$2"));
	          //Aplicamos los bordes a la región merge
	            cellRangeAddress = new CellRangeAddress(1, 1, 2, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            Cell cellFila2 = equitetasInfReporte.createCell((short) 0);
	            cellFila2.setCellValue("TIENDA: " + nombreTienda);
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 2);
	            cellFila2.setCellValue("FECHA: " + fecha);
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 4);
	            cellFila2.setCellValue("RESPONSABLE DE \n SEPARAR INSUMOS");
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 5);
	            cellFila2.setCellValue("REVISION Y \n VERIFICACIÓN LIDER \n CALIDAD Y LOG");
	            cellFila2.setCellStyle(cellInfoReporte);
	            cellFila2 = equitetasInfReporte.createCell((short) 6);
	            cellFila2.setCellValue("VERIFICADO POR \n ADMON EN PUNTO DE \n VENTA");
	            cellFila2.setCellStyle(cellInfoReporte);
				//Etiquetas reporte
	            HSSFRow equitetasRow = sheet.createRow(2);
		        for (int i = 0; i < headers.length; ++i) {
		            String header = headers[i];
		            HSSFCell cell = equitetasRow.createCell(i);
		            cell.setCellValue(header);
		            cell.setCellStyle(styleEnc);
		            if(i == headers.length -1)
		            {
		            	cell = equitetasRow.createCell(4);
		            	cell.setCellStyle(styleEnc);
		            	cell = equitetasRow.createCell(5);
		            	cell.setCellStyle(styleEnc);
		            	cell = equitetasRow.createCell(6);
		            	cell.setCellStyle(styleEnc);
		            }
		        }
		        
		        //
		        Cell datos;
		        for (int i = 0; i < filasInforme; ++i) {
		            HSSFRow dataRow = sheet.createRow(i + 3);
		            
		            Object[] d = data[i];
		            
		            String nombreInsumo = (String) d[0];
		            
		            //Aqui se puede dar un problema cuando se este agregando el inventario
		            double cantidadTienda = (double) d[1];
		            
		            double cantidadLlevar = (double) d[2];
		            
		            String empaque = "";
		            try
		            {
		            	empaque = (String) d[3];
		            }catch(Exception e)
		            {
		            	empaque = "";
		            }
		            
		            datos = dataRow.createCell(0);
		            datos.setCellValue(nombreInsumo);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep);
		        }
		        //Agregamos el estilo para el pie de página
		        //Creamos el estilo para la segunda fila de información
	            Font fontFinal = workbook.createFont();
	            fontFinal.setFontHeightInPoints((short) 8.00);
	            fontFinal.setBold(true);
	            HSSFCellStyle cellInfoFinal = workbook.createCellStyle();
	            cellInfoFinal.setBorderBottom(BorderStyle.THIN);
	            cellInfoFinal.setBorderTop(BorderStyle.THIN);
	            cellInfoFinal.setBorderLeft(BorderStyle.THIN);
	            cellInfoFinal.setBorderRight(BorderStyle.THIN);
	            cellInfoFinal.setWrapText(true);
	            cellInfoFinal.setFont(fontFinal);
	            cellInfoFinal.setAlignment(HorizontalAlignment .CENTER);
	            
	            HSSFCellStyle cellInfoOBS = workbook.createCellStyle();
	            cellInfoOBS.setBorderBottom(BorderStyle.THIN);
	            cellInfoOBS.setBorderTop(BorderStyle.THIN);
	            cellInfoOBS.setBorderLeft(BorderStyle.THIN);
	            cellInfoOBS.setBorderRight(BorderStyle.THIN);
	            cellInfoOBS.setWrapText(true);
	            cellInfoOBS.setFont(fontFinal);
	            cellInfoOBS.setAlignment(HorizontalAlignment.LEFT);
	            	            
		        
		        //Agreamos los items del final del formato
		        int filaFinal = filasInforme + 3;
		        HSSFRow ultDatos = sheet.createRow(filaFinal);
		        datos = ultDatos.createCell(0);
		        datos.setCellValue("HORA DE SALIDA PLANTA:\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(1);
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(2);
		        datos.setCellValue("HORA DE LLEGADA A PLANTA :\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(4);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(5);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(6);
		        datos.setCellValue("FIRMA\n \n");
		        datos.setCellStyle(cellInfoFinal);
		        //Hacemos merge a varias filas
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            int filaFirmaIni = filaFinal+1;
	            int filaFimraFin  = filaFinal+2;
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$E$"+(filaFirmaIni)+":$E$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 4, 4);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$F$"+(filaFirmaIni)+":$F$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 5, 5);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(filaFirmaIni)+":$G$" + (filaFimraFin)));
		        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 6, 6);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            filaFinal++;
	            ultDatos = sheet.createRow(filaFinal);
		        datos = ultDatos.createCell(0);
		        datos.setCellValue("HORA DE LLEGADA PUNTO DE VENTA:\n");
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(1);
		        datos.setCellStyle(cellInfoFinal);
		        datos = ultDatos.createCell(2);
		        datos.setCellValue("HORA DE SALIDA DE PUNTO DE VENTA :\n");
		        datos.setCellStyle(cellInfoFinal);
		      //Hacemos merge a varias filas
		        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            filaFinal++;
	            ultDatos = sheet.createRow(filaFinal);
	            datos = ultDatos.createCell(0);
		        datos.setCellValue("OBSERVACIONES:\n");
		        datos.setCellStyle(cellInfoOBS);
	            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(filaFinal+1)+":$G$" + (filaFinal+4)));
		        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal+3, 0, 6);
	            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
	            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	            
		    workbook.write(fileOut);
			fileOut.close();
			
		}catch(Exception e)
		{
			System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
		}
		return(rutaArchivoGenerado);
        
	}
	
	/**
	 * Método en la clase controladora que se encarga de realizar la inserción del encabezado del despacho de pedido y de hacer la 
	 * interface con la capa DAO.
	 * @param idtienda El id de la tienda a la cual se le guardará el envío de insumos.
	 * @param fechasurtir Fecha que relaciona el envió del pedido a la tienda
	 * @return El sistema retornará el iddespacho que hace las veces de encabezado de despacho de pedido.
	 */
	public String InsertarInsumoDespachoTienda(int idtienda, String fechasurtir, String observacion, String estado)
	{
		JSONArray listJSON = new JSONArray();
		int iddespacho = InsumoDespachoTiendaDAO.InsertarInsumoDespachoTienda(idtienda, fechasurtir, observacion,estado);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespacho", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String actualizarNovedadDespachoTienda(int idDespacho, String novedad, String diferencia)
	{
		InsumoDespachoTiendaDAO.actualizarNovedadDespachoTienda(idDespacho, novedad, diferencia);
		JSONObject respuesta = new JSONObject();
		respuesta.put("respuesta", "exitoso");
		return(respuesta.toJSONString());
	}
	
	public String actualizarObservacionDespachoTienda(int idDespacho, String observacion)
	{
		InsumoDespachoTiendaDAO.actualizarObservacionDespachoTienda(idDespacho, observacion);
		JSONObject respuesta = new JSONObject();
		respuesta.put("respuesta", "exitoso");
		return(respuesta.toJSONString());
	}
	
	public String retornarEstadoDespacho(int idDespacho)
	{
		String estado = InsumoDespachoTiendaDAO.retornarEstadoDespacho(idDespacho);
		JSONObject respuesta = new JSONObject();
		respuesta.put("estado", estado);
		return(respuesta.toJSONString());
	}
	
	public String InsertarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo, double cantidad, String contenedor)
	{
		JSONArray listJSON = new JSONArray();
		int iddespachodetalle = InsumoDespachoTiendaDetalleDAO.InsertarDetalleInsumoDespachoTienda(iddespacho,idinsumo,cantidad,contenedor);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespachodetalle", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String ActualizarDetalleInsumoDespachoTienda(int iddespacho,int idinsumo, double cantidad, String contenedor, String lote, int estado, int idDespachoDetalle)
	{
		JSONArray listJSON = new JSONArray();
		int iddespachodetalle = InsumoDespachoTiendaDetalleDAO.ActualizarDetalleInsumoDespachoTienda(iddespacho,idinsumo,cantidad,contenedor,lote,estado,idDespachoDetalle);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespachodetalle", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String ActualizarDetalleLoteInsumoDespachoTienda(int iddespacho,int idinsumo, String lote, String color, int idDespachoDetalle)
	{
		JSONArray listJSON = new JSONArray();
		int iddespachodetalle = InsumoDespachoTiendaDetalleDAO.ActualizarDetalleInsumoLoteDespachoTienda(iddespacho,idinsumo,lote,color, idDespachoDetalle);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("iddespachodetalle", iddespacho);
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	
	public String actualizarMarcadoDespachoTienda(int iddespacho, String marcado)
	{
		JSONArray listJSON = new JSONArray();
		InsumoDespachoTiendaDetalleDAO.actualizarMarcadoDespachoTienda(iddespacho, marcado);
		JSONObject Respuesta = new JSONObject();
		Respuesta.put("resultado", "exitoso");
		listJSON.add(Respuesta);
		return(listJSON.toJSONString());
	}
	/**
	 * Método que retorna un JSON con los despachos para ser mostrados en un GRID en Pantalla
	 * @param idTienda
	 * @param fechaDesde
	 * @param fechaHasta
	 * @return
	 */
	public static String obtenerInsumoDespachoTienda(int idTienda, String fechaDesde, String fechaHasta)
	{
		String respuesta = "";
		//Cambiamos fechas de formato
		String fechaFormatoDesde = "";
		String fechaFormatoHasta = "";
		try
		{
			Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fechaDesde);
			Date fecha2 = new SimpleDateFormat("dd/MM/yyyy").parse(fechaHasta);
			fechaFormatoDesde = new  SimpleDateFormat("yyyy-MM-dd").format(fecha1);
			fechaFormatoHasta = new  SimpleDateFormat("yyyy-MM-dd").format(fecha2);
		}catch(Exception e)
		{
			System.out.println(e.toString());
		}
		ArrayList<InsumoDespachoTienda> despachos =  InsumoDespachoTiendaDAO.obtenerInsumoDespachoTienda(idTienda,fechaFormatoDesde, fechaFormatoHasta);
		JSONArray listJSON = new JSONArray();
		for (InsumoDespachoTienda despacho : despachos)
		{
			JSONObject cadaDespachoJSON = new JSONObject();
			cadaDespachoJSON.put("iddespacho", despacho.getIdDespacho());
			cadaDespachoJSON.put("fechareal", despacho.getFechaReal());
			cadaDespachoJSON.put("fechasurtir", despacho.getFechaDespacho());
			cadaDespachoJSON.put("tienda", despacho.getTienda());
			cadaDespachoJSON.put("idtienda", despacho.getIdTienda());
			cadaDespachoJSON.put("estado", despacho.getEstado());
			cadaDespachoJSON.put("observacion", despacho.getObservacion());
			cadaDespachoJSON.put("novedadtienda", despacho.getNovedadTienda());
			cadaDespachoJSON.put("diferencia", despacho.getDiferencia());
			cadaDespachoJSON.put("marcado", despacho.getMarcado());
			listJSON.add(cadaDespachoJSON);
		}
		return(listJSON.toJSONString());
	}
	
	
	public  String consultarLlaves(int idTienda, int tipoConsulta)
	{
		String respuesta = "";
		
		ArrayList<Llave> llaves =  LlaveDAO.consultarLlaves(idTienda, tipoConsulta);
		JSONArray listJSON = new JSONArray();
		for (Llave llaveTmp : llaves)
		{
			JSONObject cadaLlaveJSON = new JSONObject();
			cadaLlaveJSON.put("idllave", llaveTmp.getIdLlave());
			cadaLlaveJSON.put("nombre", llaveTmp.getNombre());
			cadaLlaveJSON.put("idtienda", llaveTmp.getIdTienda());
			listJSON.add(cadaLlaveJSON);
		}
		return(listJSON.toJSONString());
	}
	
	public String consultarUltimoEventoLlave(int idLlave)
	{
		LlaveEvento llaveEvento =  LlaveEventoDAO.consultarUltimoEventoLlave(idLlave);
		JSONObject llaveJSON = new JSONObject();
		llaveJSON.put("idllave", llaveEvento.getIdLlave());
		llaveJSON.put("idevento", llaveEvento.getIdEvento());
		llaveJSON.put("idusuarioregistro", llaveEvento.getIdUsuarioRegistro());
		llaveJSON.put("idusuarioprestamo", llaveEvento.getIdUsuarioPrestamo());
		llaveJSON.put("observacion", llaveEvento.getObservacion());
		llaveJSON.put("fecha", llaveEvento.getFecha());
		llaveJSON.put("evento", llaveEvento.getEvento());
		return(llaveJSON.toJSONString());
	}
	
	public String consultarEventosLlave(int idLlave)
	{
		ArrayList<LlaveEvento> llavesEvento = LlaveEventoDAO.consultarEventosLlave(idLlave);
		JSONArray JSONLlaveEventos = new JSONArray();
		for(LlaveEvento llaveEvento: llavesEvento)
		{
			JSONObject llaveJSON = new JSONObject();
			llaveJSON.put("idllave", llaveEvento.getIdLlave());
			llaveJSON.put("idevento", llaveEvento.getIdEvento());
			llaveJSON.put("idusuarioregistro", llaveEvento.getIdUsuarioRegistro());
			llaveJSON.put("idusuarioprestamo", llaveEvento.getIdUsuarioPrestamo());
			llaveJSON.put("observacion", llaveEvento.getObservacion());
			llaveJSON.put("fecha", llaveEvento.getFecha());
			llaveJSON.put("evento", llaveEvento.getEvento());
			JSONLlaveEventos.add(llaveJSON);
		}
		return(JSONLlaveEventos.toJSONString());
	}
	
	public String consultarLLavesPrestadas(int idTienda)
	{
		String respuesta = "";
		
		ArrayList<Llave> llaves =  LlaveDAO.consultarLlavesPrestadasObjeto(idTienda);
		JSONArray listJSON = new JSONArray();
		for (Llave llaveTmp : llaves)
		{
			JSONObject cadaLlaveJSON = new JSONObject();
			cadaLlaveJSON.put("idllave", llaveTmp.getIdLlave());
			cadaLlaveJSON.put("nombre", llaveTmp.getNombre());
			cadaLlaveJSON.put("idtienda", llaveTmp.getIdTienda());
			listJSON.add(cadaLlaveJSON);
		}
		return(listJSON.toJSONString());
	}
	
	public static String insertarLlavesEvento(LlaveEvento llavEv)
	{
		//Se debe validar la situación que las llaves estén disponibles
		if(llavEv.getEvento().equals(new String("P")))
		{
			boolean llaveDisponible = LlaveDAO.validarDisponibilidadLlave(llavEv.getIdLlave());
			if(llaveDisponible)
			{
				String respuesta = "";
				boolean resultado = LlaveEventoDAO.insertarLlaveEvento(llavEv);
				JSONObject respuestaJSON = new JSONObject();
				respuestaJSON.put("resultado", resultado);
				return(respuestaJSON.toJSONString());
			}else
			{
				return("ERROR NO DISPONIBLE");
			}
		}else
		{
			String respuesta = "";
			boolean resultado = LlaveEventoDAO.insertarLlaveEvento(llavEv);
			JSONObject respuestaJSON = new JSONObject();
			respuestaJSON.put("resultado", resultado);
			return(respuestaJSON.toJSONString());
		}
	}
	
	public String ConsultarInventariosDespachados(int idtienda, String fecha)
	{
		ArrayList<InsumoDespachadoTienda> insumosDespachadosTienda = InventarioDAO.ConsultarInventariosDespachados(idtienda, fecha);
		JSONArray listJSON = new JSONArray();
		for (InsumoDespachadoTienda insDespTienda : insumosDespachadosTienda)
		{
			JSONObject Respuesta = new JSONObject();
			Respuesta.put("idinsumo", insDespTienda.getIdinsumo());
			Respuesta.put("nombreinsumo", insDespTienda.getNombreInsumo());
			Respuesta.put("cantidadsurtir", insDespTienda.getCantidadSurtir());
			Respuesta.put("contenedor", insDespTienda.getContenedor());
			Respuesta.put("unidadmedida", insDespTienda.getUnidadMedida());
			listJSON.add(Respuesta);
		}
			return(listJSON.toJSONString());
	}
	
	public String ConsultarInventarioTienda(int idtienda)
	{
		ArrayList<InsumoTienda> insumosTienda = InventarioDAO.ConsultarInsumosTienda(idtienda);
		JSONArray listJSON = new JSONArray();
		for (InsumoTienda insTienda : insumosTienda)
		{
			JSONObject Respuesta = new JSONObject();
			Respuesta.put("idinsumo", insTienda.getIdinsumo());
			Respuesta.put("nombreinsumo", insTienda.getNombreInsumo());
			Respuesta.put("cantidad", insTienda.getCantidad());
			Respuesta.put("fechainsercion", insTienda.getFecha());
			listJSON.add(Respuesta);
		}
			return(listJSON.toJSONString());
	}

	/**
	 *Este método se encarga de recorrer una a un las tiendas y verificar si el día en cuestión se surte y si es el caso 
	 *enviar un correo con el calculo de los viajes en un archivo en formato excel.
	 */
	public void CalcularInventariosTiendas()
	{
		//Obtengo las tiendas parametrizadas en el sistema de inventarios
		
		ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
		String[] rutasArchivos = new String[tiendas.size()];
		//Obtenemos la fecha actual, con base en la cual realizaremos el recorrido
		// En el String fecha guardaremos el contenido de la fecha
		Date fechaTemporal = new Date();
		DateFormat formatoFinal = new SimpleDateFormat("yyyy-MM-dd");
		String fecha="";
		try
		{
			fecha = formatoFinal.format(fechaTemporal);
			System.out.println("fecha transformada " + fecha );
			
		}catch(Exception e){
			System.out.println("Problema transformando la fecha actual " + e.toString());
		}
		int fila = 0;
		for(Tienda tien : tiendas)
		{
			int idtienda = tien.getIdTienda();
			try
			{
				
					rutasArchivos[fila] = CalcularInventarioTiendaFormatoExcel(idtienda, fecha);
				
				
				
			}
			catch(Exception e)
			{
				System.out.println(e.toString() + " " + e.fillInStackTrace() + " " + e.getMessage());
			}
			fila++;
			
		}
		
		//Realizamos el envío del correo electrónico con los archivo
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		Correo correo = new Correo();
		correo.setAsunto("INVENTARIOS A SURTIR TIENDAS PIZZA AMERICANA");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTESURTIR");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		correo.setMensaje("A continuación todos los inventarios de las tiendas de pizza americana");
		correo.setRutasArchivos(rutasArchivos);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreo();
	}

public static void main(String[] args)
{
	InventarioCtrl inventario = new InventarioCtrl();
	inventario.GenerarDespachoTiendaFormatoExcel(7, "16/05/2023",5912 );
}


public static String insertarInsumo(Insumo insumo)
{
	int idInsumoIns = InsumoDAO.insertarInsmo(insumo);
	JSONObject respuesta = new JSONObject();
	if(idInsumoIns > 0 )
	{
		respuesta.putIfAbsent("resultado", "exitoso");
	}else
	{
		respuesta.putIfAbsent("resultado", "error");
	}
	return(respuesta.toJSONString());
}

public String retornarInsumo(int idInsumo)
{
	Insumo insumo = InsumoDAO.retornarInsumo(idInsumo);
	JSONObject  insumoResp = new JSONObject();
	insumoResp.put("idinsumo", insumo.getIdinsumo());
	insumoResp.put("nombreinsumo", insumo.getNombre());
	insumoResp.put("unidadmedida", insumo.getUnidadMedida());
	insumoResp.put("preciounidad", insumo.getPrecioUnidad());
	insumoResp.put("manejacanastas", insumo.getManejacanasta());
	insumoResp.put("cantidadcanasta", insumo.getCantidaxcanasta());
	insumoResp.put("nombrecontenedor", insumo.getNombreContenedor());
	insumoResp.put("categoria", insumo.getCategoria());
	insumoResp.put("costounidad", insumo.getCostoUnidad());
	insumoResp.put("controltienda", insumo.getControlTienda());
	String controlCantidad  = "";
	if(insumo.isControl_cantidad())
	{
		controlCantidad = "S" ;
	}else
	{
		controlCantidad = "N";
	}
	insumoResp.put("controlcantidad", controlCantidad);
	insumoResp.put("costounidad", insumo.getCostoUnidad());
	return(insumoResp.toJSONString());
}

public String editarInsumo(Insumo insumo)
{
	String resultado = InsumoDAO.editarInsumo(insumo);
	JSONObject respuesta = new JSONObject();
	respuesta.put("resultado", resultado);
	//En este punto realizamos el cambio para realizar la actualización de precios en lo que va de la semana
	resultado = InsumoDAO.actualizarPrecioInsumoRetirado(insumo.getIdinsumo(), insumo.getCostoUnidad());
	return(respuesta.toJSONString());
}

public String retornarInsumos()
{
	ArrayList<Insumo> insumos = InsumoDAO.retornarInsumos();
	JSONObject  insumoResp = new JSONObject();
	JSONArray insumosJSON = new JSONArray();
	for(int i = 0; i < insumos.size(); i++)
	{
		Insumo insumoTemp = insumos.get(i);
		insumoResp = new JSONObject();
		insumoResp.put("idinsumo", insumoTemp.getIdinsumo());
		insumoResp.put("nombreinsumo", insumoTemp.getNombre());
		insumoResp.put("unidadmedida", insumoTemp.getUnidadMedida());
		insumoResp.put("preciounidad", insumoTemp.getPrecioUnidad());
		insumoResp.put("manejacanastas", insumoTemp.getManejacanasta());
		insumoResp.put("cantidadcanasta", insumoTemp.getCantidaxcanasta());
		insumoResp.put("nombrecontenedor", insumoTemp.getNombreContenedor());
		insumoResp.put("categoria", insumoTemp.getCategoria());
		insumoResp.put("costounidad", insumoTemp.getCostoUnidad());
		insumosJSON.add(insumoResp);
	}
	return(insumosJSON.toJSONString());
}

public String retornarInsumosGrid()
{
	ArrayList<Insumo> insumos = InsumoDAO.retornarInsumos();
	JSONObject  insumoResp = new JSONObject();
	JSONArray insumosJSON = new JSONArray();
	for(int i = 0; i < insumos.size(); i++)
	{
		Insumo insumoTemp = insumos.get(i);
		insumoResp = new JSONObject();
		insumoResp.put("idinsumo", insumoTemp.getIdinsumo());
		insumoResp.put("nombreinsumo", insumoTemp.getNombre());
		insumoResp.put("unidadmedida", insumoTemp.getUnidadMedida());
		insumoResp.put("categoria", insumoTemp.getCategoria());
		insumoResp.put("costounidad", insumoTemp.getCostoUnidad());
		insumosJSON.add(insumoResp);
	}
	return(insumosJSON.toJSONString());
}


public ArrayList<InsumoDespachoTiendaDetalle> obtenerDetalleDespachoTienda(int idDespacho)
{
	ArrayList<InsumoDespachoTiendaDetalle> insumosDespachados = InsumoDespachoTiendaDetalleDAO.obtenerDetalleDespachoTienda(idDespacho);
	return(insumosDespachados);
	
}

public InsumoDespachoTienda obtenerInsumoDespacho(int idDespacho)
{
	InsumoDespachoTienda despacho = InsumoDespachoTiendaDAO.obtenerInsumoDespacho(idDespacho);
	return(despacho);
}

/**
 * Método que se encarga de generar un excel con lo despachado para la tienda 
 * @param idtienda
 * @param fecha
 * @param idDespacho
 */
public void GenerarDespachoTiendaFormatoExcel(int idtienda, String fecha, int idDespacho)
{
	int estiloDetalle = 0;
	//Obtenemos la información del despacho para poner allí las observaciones
	InsumoDespachoTienda infoDespacho =  obtenerInsumoDespacho(idDespacho);
	String rutaArchivoGenerado="";
	String rutaArchivoBD = ParametrosDAO.obtenerParametroTexto("RUTAINV");
	String rutaImagenReporte = rutaArchivoBD + "LogoPizzaAmericana.png";
	//obtenemos día de la semana para recuperar inventario requerido de la tienda
	// Creamos una instancia del calendario
	GregorianCalendar cal = new GregorianCalendar();
	int diasemana = 0;
	JSONArray listJSON = new JSONArray();
	try
	{
		Date fecha1 = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
		fecha = new SimpleDateFormat("yyyy-MM-dd").format(fecha1);
		System.out.println("FECHA TRANSFORMADA " + fecha);
		cal.setTime(fecha1);
		diasemana = cal.get(Calendar.DAY_OF_WEEK);
	}catch(Exception e)
	{
		System.out.println(e.toString() + e.getMessage() + e.getStackTrace());
		try
		{
			Date fecha1 = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
			System.out.println("FECHA TRANSFORMADA " + fecha);
			cal.setTime(fecha1);
			diasemana = cal.get(Calendar.DAY_OF_WEEK);
		}catch(Exception e2)
		{
			System.out.println(e2.toString() + e2.getMessage() + e2.getStackTrace());
		}
	}
	String nombreTienda = TiendaDAO.obtenerNombreTienda(idtienda);
	//Creamos el libro en Excel y la hoja en cuestión, definimos los encabezados.
	HSSFWorkbook workbook = new HSSFWorkbook();
	HSSFSheet sheet = workbook.createSheet("inventarioDespachado");
	sheet.setColumnWidth(0, 7500);
	sheet.setColumnWidth(1, 3500);
	sheet.setColumnWidth(2, 4500);
	sheet.setColumnWidth(3, 3000);
	sheet.setColumnWidth(4, 5500);
	sheet.setColumnWidth(5, 5500);
	sheet.setColumnWidth(6, 5500);
	String[] headers = new String[]{
            "Producto",
            "Cantidad \n Tienda",
            "Cantidad \n Despachada",
            "Empaque"
        };
	
	try
	{
		   rutaArchivoGenerado = rutaArchivoBD + "InventarioDespachado"+ nombreTienda +".xls";
		   
		   FileOutputStream fileOut = new FileOutputStream(rutaArchivoGenerado);
		   rutaArchivoGenerado = rutaArchivoGenerado + "%&" + "InventarioDespachado"+ nombreTienda +".xls";
			
			//Esta parte de recuperación de los insumos tienda se establece control dado que está recuperando constantemente
		   boolean bandRecuperoInsumos = false;
		   int contadorReintentos = 1;
		   ArrayList<InsumoTienda> insumosTienda = new ArrayList();
		   
		   /**
		    * Este ciclo while tiene como objetivo realizar reintentos en caso de que no se encuentre información en la tabla
		    * esto se puede dar en el momento en que corra al mismo tiempo el reporte de inventarios y el servicio que trae los 
		    * inventario de una tienda
		    */
		   while(!bandRecuperoInsumos)
		   {
			   insumosTienda = InventarioDAO.ObtenerInsumosTiendaAutomatico(idtienda, fecha);
			   if ((insumosTienda.size() > 0)|| contadorReintentos > 3)
			   {
				   bandRecuperoInsumos = true;
			   }
			   else
			   {
				   Thread.sleep(10000);
				   contadorReintentos++;
			   }
		   }
		   
		   	//En este punto obtenemos los insumos que se despacharon para la tienda en el despacho generado con el fin
			// de en base en estos generar el recorrido y mostrar el formato.
			ArrayList<InsumoDespachoTiendaDetalle> insumosDespachados = obtenerDetalleDespachoTienda(idDespacho);
			//System.out.println("INFORMACIÓN RECUPERADA INSUMOS TIENDA "  + insumosTienda.size() + " informacón despacho " + insumosDespachados.size());
			//Contralaremos la fila en la que vamos con la variable fila
			int fila = 0;
			int filasInforme = 0;
			int cantInsumoDespachados = insumosDespachados.size();
			//Definimos un arreglo donde iremos dejando los datos
			Object[][] data = new Object[cantInsumoDespachados][6];
			//con base en insumos despachados es que haremos el recorrido y la generación del archivo de excel
			for (InsumoDespachoTiendaDetalle cadaInsumoDespachado : insumosDespachados)
			{
				for (InsumoTienda insTienda : insumosTienda)
				{
					if(cadaInsumoDespachado.getIdInsumo() == insTienda.getIdinsumo())
					{
						filasInforme++;
						data[fila][0]= new String( insTienda.getNombreInsumo());
						//EN este punto llenamos lo que tiene la tienda
						data[fila][1]= insTienda.getCantidad();
						data[fila][2]= cadaInsumoDespachado.getCantidad();
						data[fila][3]= cadaInsumoDespachado.getContenedor();
						data[fila][5]= cadaInsumoDespachado.getColor();
						fila++;
						break;
					}
					
					
				}
			}
			
			//Recorremos y damos formato
			//Agregamos nombre del reporte
			//Creamos el estilo para el nombre del reporte
			Font whiteFont = workbook.createFont();
            whiteFont.setColor(IndexedColors.BLUE.index);
            whiteFont.setFontHeightInPoints((short) 14.00);
            whiteFont.setBold(true);
            HSSFCellStyle cellheader = workbook.createCellStyle();
            cellheader.setWrapText(true);
            cellheader.setFont(whiteFont);
            cellheader.setAlignment(HorizontalAlignment .CENTER);
            
            //Creamos el estilo para la segunda fila de información
            Font fontSegFila = workbook.createFont();
            fontSegFila.setColor(IndexedColors.ORANGE.index);
            fontSegFila.setFontHeightInPoints((short) 10.00);
            fontSegFila.setBold(true);
            HSSFCellStyle cellInfoReporte = workbook.createCellStyle();
            cellInfoReporte.setBorderBottom(BorderStyle.THIN);
            cellInfoReporte.setBorderTop(BorderStyle.THIN);
            cellInfoReporte.setBorderLeft(BorderStyle.THIN);
            cellInfoReporte.setBorderRight(BorderStyle.THIN);
            cellInfoReporte.setWrapText(true);
            cellInfoReporte.setFont(fontSegFila);
            cellInfoReporte.setAlignment(HorizontalAlignment .CENTER);
            	            
            //Creamos el estilo para la tercer fila de encabezados
            Font fontTerFila = workbook.createFont();
            //fontTerFila.setColor(IndexedColors.ORANGE.index);
            fontTerFila.setFontHeightInPoints((short) 10.00);
            fontTerFila.setBold(true);
            HSSFCellStyle styleEnc = workbook.createCellStyle();
            styleEnc.setBorderBottom(BorderStyle.THIN);
            styleEnc.setBorderTop(BorderStyle.THIN);
            styleEnc.setBorderLeft(BorderStyle.THIN);
            styleEnc.setBorderRight(BorderStyle.THIN);
            styleEnc.setWrapText(true);
            styleEnc.setFont(fontTerFila);
            styleEnc.setAlignment(HorizontalAlignment .CENTER);
            
            //Creamos el estilo para la informacion del reporte
            HSSFCellStyle styleInfRep = workbook.createCellStyle();
            styleInfRep.setBorderBottom(BorderStyle.THIN);
            styleInfRep.setBorderTop(BorderStyle.THIN);
            styleInfRep.setBorderLeft(BorderStyle.THIN);
            styleInfRep.setBorderRight(BorderStyle.THIN);
            styleInfRep.setWrapText(true);
            styleInfRep.setFillForegroundColor((short)13);
        	styleInfRep.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            HSSFCellStyle styleInfRep1 = workbook.createCellStyle();
            styleInfRep1.setBorderBottom(BorderStyle.THIN);
            styleInfRep1.setBorderTop(BorderStyle.THIN);
            styleInfRep1.setBorderLeft(BorderStyle.THIN);
            styleInfRep1.setBorderRight(BorderStyle.THIN);
            styleInfRep1.setWrapText(true);
            styleInfRep1.setFillForegroundColor((short)44);
        	styleInfRep1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            HSSFCellStyle styleInfRep2 = workbook.createCellStyle();
            styleInfRep2.setBorderBottom(BorderStyle.THIN);
            styleInfRep2.setBorderTop(BorderStyle.THIN);
            styleInfRep2.setBorderLeft(BorderStyle.THIN);
            styleInfRep2.setBorderRight(BorderStyle.THIN);
            styleInfRep2.setWrapText(true);
            styleInfRep2.setFillForegroundColor((short)42);
        	styleInfRep2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            HSSFCellStyle styleInfRep3 = workbook.createCellStyle();
            styleInfRep3.setBorderBottom(BorderStyle.THIN);
            styleInfRep3.setBorderTop(BorderStyle.THIN);
            styleInfRep3.setBorderLeft(BorderStyle.THIN);
            styleInfRep3.setBorderRight(BorderStyle.THIN);
            styleInfRep3.setWrapText(true);
            styleInfRep3.setFillForegroundColor((short)31);
        	styleInfRep3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        	
        	HSSFCellStyle styleInfRep4 = workbook.createCellStyle();
            styleInfRep4.setBorderBottom(BorderStyle.THIN);
            styleInfRep4.setBorderTop(BorderStyle.THIN);
            styleInfRep4.setBorderLeft(BorderStyle.THIN);
            styleInfRep4.setBorderRight(BorderStyle.THIN);
            styleInfRep4.setWrapText(true);
            styleInfRep4.setFillForegroundColor((short)22);
        	styleInfRep4.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        	
        	HSSFCellStyle styleInfRep5 = workbook.createCellStyle();
            styleInfRep5.setBorderBottom(BorderStyle.THIN);
            styleInfRep5.setBorderTop(BorderStyle.THIN);
            styleInfRep5.setBorderLeft(BorderStyle.THIN);
            styleInfRep5.setBorderRight(BorderStyle.THIN);
            styleInfRep5.setWrapText(true);
            styleInfRep5.setFillForegroundColor((short)1);
        	styleInfRep5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
                        
            //NOMBRE DEL REPORTE
            HSSFRow headerRow = sheet.createRow((short) 0);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
            Cell cellHeader = headerRow.createCell((short) 0);
            cellHeader.setCellValue(new HSSFRichTextString("LISTADO DE INSUMOS POR PUNTO DE VENTA \n" + nombreTienda ));
            headerRow.setHeight((short)1000);
            cellHeader.setCellStyle(cellheader);
            
            //Realizamos la adición de la imagen del logo de pizza americana
            InputStream inputStream = new FileInputStream(rutaImagenReporte);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            int pictureIdx = workbook.addPicture(imageBytes, workbook.PICTURE_TYPE_PNG);
            //close the input stream
            //Returns an object that handles instantiating concrete classes
            CreationHelper helper = workbook.getCreationHelper();
            //Creates the top-level drawing patriarch.
            Drawing drawing = sheet.createDrawingPatriarch();
            //Create an anchor that is attached to the worksheet
            ClientAnchor anchor = helper.createClientAnchor();
            //set top-left corner for the image
             anchor.setDx1(0);
            anchor.setDy1(0);
            anchor.setDx2(1023);
            anchor.setDy2(6000);
            anchor.setCol1(5);
            anchor.setRow1(0);
            anchor.setCol2(5);
            anchor.setRow2(1);
            //Creates a picture
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            //Reset the image to the original size
            pict.resize();
            
            //Aplicamos los bordes a la región merge
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            //Para la imagen
            sheet.addMergedRegion(CellRangeAddress.valueOf("$E$1:$G$1"));
          //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(0, 0, 4, 6);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
            //Etiquetas de segunda linea de informaicón 
            HSSFRow equitetasInfReporte = sheet.createRow(1);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$B$2"));
            //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(1, 1, 0, 1);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$C$2:$D$2"));
          //Aplicamos los bordes a la región merge
            cellRangeAddress = new CellRangeAddress(1, 1, 2, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            Cell cellFila2 = equitetasInfReporte.createCell((short) 0);
            cellFila2.setCellValue("TIENDA: " + nombreTienda);
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 2);
            cellFila2.setCellValue("FECHA: " + fecha);
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 4);
            cellFila2.setCellValue("RESPONSABLE DE \n SEPARAR INSUMOS");
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 5);
            cellFila2.setCellValue("REVISION Y \n VERIFICACIÓN LIDER \n CALIDAD Y LOG");
            cellFila2.setCellStyle(cellInfoReporte);
            cellFila2 = equitetasInfReporte.createCell((short) 6);
            cellFila2.setCellValue("VERIFICADO POR \n ADMON EN PUNTO DE \n VENTA");
            cellFila2.setCellStyle(cellInfoReporte);
			//Etiquetas reporte
            HSSFRow equitetasRow = sheet.createRow(2);
	        for (int i = 0; i < headers.length; ++i) {
	            String header = headers[i];
	            HSSFCell cell = equitetasRow.createCell(i);
	            cell.setCellValue(header);
	            cell.setCellStyle(styleEnc);
	            if(i == headers.length -1)
	            {
	            	cell = equitetasRow.createCell(4);
	            	cell.setCellStyle(styleEnc);
	            	cell = equitetasRow.createCell(5);
	            	cell.setCellStyle(styleEnc);
	            	cell = equitetasRow.createCell(6);
	            	cell.setCellStyle(styleEnc);
	            }
	        }
	        
	        //
	        Cell datos;
	        for (int i = 0; i < filasInforme; ++i) {
	            HSSFRow dataRow = sheet.createRow(i + 3);
	            
	            Object[] d = data[i];
	            
	            String nombreInsumo = (String) d[0];
	            
	            //Aqui se puede dar un problema cuando se este agregando el inventario
	            double cantidadTienda = (double) d[1];
	            
	            double cantidadLlevar = (double) d[2];
	            
	            String color = "";
	            
	            String empaque = "";
	            try
	            {
	            	empaque = (String) d[3];
	            }catch(Exception e)
	            {
	            	empaque = "";
	            }
	            
	            try
	            {
	            	color = (String) d[5];
	            	color = color.substring(1,color.length());	            }catch(Exception e)
	            {
	            	color = "";
	            }
	            
	            datos = dataRow.createCell(0);
	            datos.setCellValue(nombreInsumo);
	            if(color.equals(new String("")))
	            {
	                HSSFPalette palette = workbook.getCustomPalette();
	                HSSFColor myColor = palette.findSimilarColor(0, 0, 0);
	                estiloDetalle = 5;
	            }else
	            {
	                int resultRed = Integer.valueOf(color.substring(0, 2), 16);
	                int resultGreen = Integer.valueOf(color.substring(2, 4), 16);
	                int resultBlue = Integer.valueOf(color.substring(4, 6), 16);
	                HSSFWorkbook hwb = new HSSFWorkbook();
	                HSSFPalette palette = hwb.getCustomPalette();
	                HSSFColor myColor = palette.findSimilarColor(resultRed, resultGreen, resultBlue);
	                short palIndex = myColor.getIndex();
	                if(palIndex == 13)
	                {
	                	estiloDetalle = 0;
	                }else if(palIndex == 44)
	                {
	                	estiloDetalle = 1;
	                }else if(palIndex == 42)
	                {
	                	estiloDetalle = 2;
	                }else if(palIndex == 31)
	                {
	                	estiloDetalle = 3;
	                }else if(palIndex == 22)
	                {
	                	estiloDetalle = 4;
	                }
	            }
	            if(estiloDetalle == 0)
	            {
	            	datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep);
	            }else if(estiloDetalle == 1)
	            {
	            	datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep1);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep1);
	            }else if(estiloDetalle == 2)
	            {
	            	datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep2);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep2);
	            }else if(estiloDetalle == 3)
	            {
	            	datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep3);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep3);
	            }else if(estiloDetalle == 4)
	            {
	            	datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep4);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep4);
	            }else if(estiloDetalle == 5)
	            {
	            	datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(1);
		            datos.setCellValue(cantidadTienda);
		            datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(2);
		            datos.setCellValue(cantidadLlevar);
		            datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(3);
		            datos.setCellValue(empaque);
		            datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(4);
		            datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(5);
		            datos.setCellStyle(styleInfRep5);
		            datos = dataRow.createCell(6);
		            datos.setCellStyle(styleInfRep5);
	            }
	        }
	        //Agregamos el estilo para el pie de página
	        //Creamos el estilo para la segunda fila de información
            Font fontFinal = workbook.createFont();
            fontFinal.setFontHeightInPoints((short) 8.00);
            fontFinal.setBold(true);
            HSSFCellStyle cellInfoFinal = workbook.createCellStyle();
            cellInfoFinal.setBorderBottom(BorderStyle.THIN);
            cellInfoFinal.setBorderTop(BorderStyle.THIN);
            cellInfoFinal.setBorderLeft(BorderStyle.THIN);
            cellInfoFinal.setBorderRight(BorderStyle.THIN);
            cellInfoFinal.setWrapText(true);
            cellInfoFinal.setFont(fontFinal);
            cellInfoFinal.setAlignment(HorizontalAlignment .CENTER);
            
            HSSFCellStyle cellInfoOBS = workbook.createCellStyle();
            cellInfoOBS.setBorderBottom(BorderStyle.THIN);
            cellInfoOBS.setBorderTop(BorderStyle.THIN);
            cellInfoOBS.setBorderLeft(BorderStyle.THIN);
            cellInfoOBS.setBorderRight(BorderStyle.THIN);
            cellInfoOBS.setWrapText(true);
            cellInfoOBS.setFont(fontFinal);
            cellInfoOBS.setAlignment(HorizontalAlignment.LEFT);
            	            
	        
	        //Agreamos los items del final del formato
	        int filaFinal = filasInforme + 3;
	        HSSFRow ultDatos = sheet.createRow(filaFinal);
	        datos = ultDatos.createCell(0);
	        datos.setCellValue("HORA DE SALIDA PLANTA:\n");
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(1);
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(2);
	        datos.setCellValue("HORA DE LLEGADA A PLANTA :\n");
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(4);
	        datos.setCellValue("FIRMA\n \n");
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(5);
	        datos.setCellValue("FIRMA\n \n");
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(6);
	        datos.setCellValue("FIRMA\n \n");
	        datos.setCellStyle(cellInfoFinal);
	        //Hacemos merge a varias filas
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
	        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            int filaFirmaIni = filaFinal+1;
            int filaFimraFin  = filaFinal+2;
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$E$"+(filaFirmaIni)+":$E$" + (filaFimraFin)));
	        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 4, 4);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$F$"+(filaFirmaIni)+":$F$" + (filaFimraFin)));
	        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 5, 5);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(filaFirmaIni)+":$G$" + (filaFimraFin)));
	        cellRangeAddress = new CellRangeAddress((filaFirmaIni-1), (filaFimraFin-1), 6, 6);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            filaFinal++;
            ultDatos = sheet.createRow(filaFinal);
	        datos = ultDatos.createCell(0);
	        datos.setCellValue("HORA DE LLEGADA PUNTO DE VENTA:\n");
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(1);
	        datos.setCellStyle(cellInfoFinal);
	        datos = ultDatos.createCell(2);
	        datos.setCellValue("HORA DE SALIDA DE PUNTO DE VENTA :\n");
	        datos.setCellStyle(cellInfoFinal);
	      //Hacemos merge a varias filas
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(filaFinal+1)+":$D$" + (filaFinal+1)));
	        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal, 2, 3);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            filaFinal++;
            ultDatos = sheet.createRow(filaFinal);
            datos = ultDatos.createCell(0);
	        datos.setCellValue("OBSERVACIONES:\n" + infoDespacho.getObservacion());
	        datos.setCellStyle(cellInfoOBS);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(filaFinal+1)+":$G$" + (filaFinal+4)));
	        cellRangeAddress = new CellRangeAddress(filaFinal, filaFinal+3, 0, 6);
            HSSFRegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook);
            HSSFRegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook);
            
	    workbook.write(fileOut);
		fileOut.close();
		String[] rutasArchivos = new String[1];
		rutasArchivos[0] = rutaArchivoGenerado;
		//Luego de cerrado el archivo, realizamos el envío al correo del archivo
		CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
		Correo correo = new Correo();
		correo.setAsunto(nombreTienda + "-" + fecha + "ARCHIVO INVENTARIO DESPACHADO");
		correo.setContrasena(infoCorreo.getClaveCorreo());
		ArrayList correos = GeneralDAO.obtenerCorreosParametro("REPORTEDESPACHO");
		correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
		System.out.println("PROBANDO VERSIÓN OJOOO " + infoCorreo.getCuentaCorreo() + " " + infoCorreo.getClaveCorreo());
		correo.setMensaje("A continuación la información ingresada para despacho a la tienda "  +nombreTienda + " en la fecha " + fecha);
		correo.setRutasArchivos(rutasArchivos);
		ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correos);
		contro.enviarCorreo();
		
	}catch(Exception e)
	{
		System.out.println("problemas en la generacion del archivo " + e.toString() + e.getMessage() + e.getStackTrace().toString() );
	}
	
    
}


public String consultarConsumos(int idTienda, String fechaInicial, String fechaFinal, String tipoConsulta)
{
	ArrayList<InsumoConsumidoSemana> insumosSemana = ConsumoInventarioDAO.consultarConsumos(idTienda, fechaInicial, fechaFinal, tipoConsulta);
	JSONObject  insumoConResp = new JSONObject();
	JSONArray insumosConJSON = new JSONArray();
	for(InsumoConsumidoSemana insTemp: insumosSemana)
	{
		insumoConResp = new JSONObject();
		insumoConResp.put("idinsumo", insTemp.getIdInsumo());
		insumoConResp.put("nombreinsumo", insTemp.getNombre());
		insumoConResp.put("lunes", insTemp.getLunes());
		insumoConResp.put("martes", insTemp.getMartes());
		insumoConResp.put("miercoles", insTemp.getMiercoles());
		insumoConResp.put("jueves", insTemp.getJueves());
		insumoConResp.put("viernes", insTemp.getViernes());
		insumoConResp.put("sabado", insTemp.getSabado());
		insumoConResp.put("domingo", insTemp.getDomingo());
		insumosConJSON.add(insumoConResp);
	}
	return(insumosConJSON.toJSONString());
	
}

	public String obtenerDesechos()
	{
		ArrayList<Desecho> desechos =  DesechoDAO.obtenerDesechos();
		JSONObject  desechoConResp = new JSONObject();
		JSONArray desechosConJSON = new JSONArray();
		for(Desecho desTemp: desechos)
		{
			desechoConResp = new JSONObject();
			desechoConResp.put("iddesecho",  desTemp.getIdDesecho());
			desechoConResp.put("descripcion",  desTemp.getDescripcion());
			desechoConResp.put("tipo",  desTemp.getTipo());
			desechosConJSON.add(desechoConResp);
		}
		return(desechosConJSON.toJSONString());
	}
	
	
	public String obtenerVarianzaDiferencia()
	{
		ArrayList<VarianzaDiferencia> diferenciasVar =  VarianzaDiferenciaDAO.obtenerVarianzaDiferencia();
		JSONObject  diferencias = new JSONObject();
		JSONArray diferenciasJSON = new JSONArray();
		for(VarianzaDiferencia varTemp: diferenciasVar)
		{
			diferencias = new JSONObject();
			diferencias.put("idinsumo",  varTemp.getIdInsumo());
			diferencias.put("cantidad",  varTemp.getCantidad());
			diferenciasJSON.add(diferencias);
		}
		return(diferenciasJSON.toJSONString());
	}
	
	public String obtenerDesechosTiendaFecha(String fecha, int idTienda)
	{
		ArrayList<DesechoTienda> desechosTienda =  DesechoTiendaDAO.obtenerDesechosTiendaFecha(fecha,idTienda);
		JSONObject  desechoConResp = new JSONObject();
		JSONArray desechosConJSON = new JSONArray();
		for(DesechoTienda desTemp: desechosTienda)
		{
			desechoConResp = new JSONObject();
			desechoConResp.put("iddesechotienda",  desTemp.getIdDesechoTienda());
			desechoConResp.put("numerodesecho",  desTemp.getNumeroDesecho());
			desechoConResp.put("idtienda",  desTemp.getIdTienda());
			desechoConResp.put("fecha",  desTemp.getFecha());
			desechoConResp.put("descripcion",  desTemp.getDescripcion());
			desechoConResp.put("motivo",  desTemp.getMotivo());
			desechoConResp.put("iddesecho",  desTemp.getIdDesecho());
			desechoConResp.put("gramos",  desTemp.getGramos());
			desechoConResp.put("cantidad",  desTemp.getCantidad());
			desechoConResp.put("usuario",  desTemp.getUsuario());
			desechosConJSON.add(desechoConResp);
		}
		return(desechosConJSON.toJSONString());
		
	}
	
	public String obtenerDesechosFecha(String fechaAnterior, String fechaActual)
	{
		//Formato para mostrar las cantidades
		DecimalFormat formatea = new DecimalFormat("###,###");
		String respuesta = "";
		ArrayList <Tienda> tiendas = TiendaDAO.obtenerTiendas();
		Tienda tiendaTemp;
		Double valorDesechoTienda;
		for(int j = 0; j < tiendas.size(); j++)
		{
			tiendaTemp = tiendas.get(j);
			respuesta = respuesta + "<table border='2'> <tr> <td colspan='10'> REPORTE DESECHOS " + tiendaTemp.getNombreTienda()+ "</td></tr>";
			respuesta = respuesta + "<tr>"
					+  "<th width='50' ><strong>Id Desecho</strong></td>"
					+  "<th width='70'><strong>Numero Desecho</strong></td>"
					+  "<th width='80'><strong>Fecha</strong></td>"
					+  "<th width='200'><strong>Descripción</strong></td>"
					+  "<th width='200'><strong>Motivo</strong></td>"
					+  "<th width='130'><strong>Desecho</strong></td>"
					+  "<th width='50'><strong>Gramos</strong></td>"
					+  "<th width='50'><strong>Cantidad</strong></td>"
					+  "<th width='80'><strong>Costo</strong></td>"
					+  "<th width='80'><strong>Usuario</strong></td>"
					+  "</tr>";
			InventarioCtrl invCtrl = new InventarioCtrl();
			ArrayList<DesechoTienda> desechos = invCtrl.obtenerDesechosTiendaFechas(fechaAnterior, fechaActual, tiendaTemp.getIdTienda());
			valorDesechoTienda = 0.0;
			DesechoTienda desTiendaTemp;
			for(int k = 0; k < desechos.size(); k++)
			{
				desTiendaTemp = desechos.get(k);
				if(desTiendaTemp.getGramos() > 0 && desTiendaTemp.getCantidad() == 0)
				{
					valorDesechoTienda = valorDesechoTienda + (desTiendaTemp.getGramos()* desTiendaTemp.getCosto());
					respuesta = respuesta + "<tr><td width='50'>" + desTiendaTemp.getIdDesechoTienda() +  "</td><td width='70'>" + desTiendaTemp.getNumeroDesecho()+ "</td><td width='80'>" + desTiendaTemp.getFecha() + "</td><td width='200'>" + desTiendaTemp.getDescripcion() + "</td><td width='200'>" + desTiendaTemp.getMotivo() + "</td><td width='130'>" + desTiendaTemp.getDescripcionDesecho() + "</td><td width='50'>" + formatea.format(desTiendaTemp.getGramos()) + "</td><td width='50'>" + " " + "</td><td width='80'>" + formatea.format((desTiendaTemp.getGramos()* desTiendaTemp.getCosto())) + "</td><td width='80'>" + desTiendaTemp.getUsuario() + "</td></tr>";
					
				}else
				{
					valorDesechoTienda = valorDesechoTienda + (desTiendaTemp.getCosto()*desTiendaTemp.getCantidad());
					respuesta = respuesta + "<tr><td width='50'>" + desTiendaTemp.getIdDesechoTienda() +  "</td><td width='70'>" + desTiendaTemp.getNumeroDesecho()+ "</td><td width='50'>" + desTiendaTemp.getFecha() + "</td><td width='200'>" + desTiendaTemp.getDescripcion() + "</td><td width='200'>" + desTiendaTemp.getMotivo() + "</td><td width='130'>" + desTiendaTemp.getDescripcionDesecho() + "</td><td width='50'>" + " " + "</td><td width='50'>" + desTiendaTemp.getCantidad() + "</td><td width='80'>" + formatea.format((desTiendaTemp.getCosto()*desTiendaTemp.getCantidad())) + "</td><td width='80'>" + desTiendaTemp.getUsuario() + "</td></tr>";
					
				}
			}
			respuesta = respuesta + "<tr><td width='600' COLSPAN='5'>TOTAL TIENDA </td><td width='390' COLSPAN='5'>" + formatea.format(valorDesechoTienda) + " </td></tr></table> <br/>";
			
		}
		return(respuesta);
		
	}
	
	
	public String validarExistenciaNumeroDesecho(int numeroDesecho)
	{
		boolean existe = DesechoTiendaDAO.validarExistenciaNumeroDesecho(numeroDesecho);
		JSONObject  respuestaJSON = new JSONObject();
		respuestaJSON.put("existe", existe);
		return(respuestaJSON.toJSONString());
	}
	
	public String insertarDesechoTienda(DesechoTienda des)
	{
		int intRespuesta  = DesechoTiendaDAO.insertarDesechoTienda(des);
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
	
	
	public  String eliminarDesechoTienda (int idDesechoTienda)
	{
		DesechoTiendaDAO.eliminarDesechoTienda (idDesechoTienda);
		String respuesta = "";
		respuesta = "OK";
		return(respuesta);
	}
	
	public ArrayList<DesechoTienda> obtenerDesechosTiendaFechas(String fechaInicial, String fechaFinal, int idTienda)
	{
		ArrayList<DesechoTienda> desechosTienda =  DesechoTiendaDAO.obtenerDesechosTiendaFechas(fechaInicial, fechaFinal, idTienda);
		return(desechosTienda);
		
	}

	/**
	 * Método para la replica del retiro de inventario en la tienda Bodega, método que se ejecuta como servicio con el fin de
	 * tenerlo para confirmación del retiro de inventario en BODEGA
	 * @param idDespacho
	 */
	public String insertarRetiroInventarioBodega(int idDespacho)
	{
		//Recuperamos los despachos pendientes
		InsumoDespachoTienda insumoDespacho = InsumoDespachoTiendaDAO.obtenerInsumoDespacho(idDespacho);
		//Recorremos los despachos pendientes de la tienda para la fecha
		//Variable donde almacenamos el resultado de la inserción del encabezado
		boolean insercionEnc = false;
		//Vamos llevando un control de la inserción de cada detalle
		boolean insercionDet = false;
		//Variable para controlar si hubo error en la inserción del despacho en la tienda
		boolean huboError = false;
		boolean verificarExisteDespacho = false;
		//Obtener variable de TIENDA BODEGA
		String hostBodega = ParametrosDAO.obtenerParametroTexto("HOSTBODEGA");
		ArrayList correosError = GeneralDAO.obtenerCorreosParametro("ERRORREPLICAINV");
		ArrayList correosExitoso = GeneralDAO.obtenerCorreosParametro("REPLICAINV");
		//REALIZAREMOS LA INSERCIÓN DEL DESPACHO EN C
		//Verificamos que el despacho a ingresar no haya sido ya ingresado en la tienda
		verificarExisteDespacho = RetiroInventarioTmpDAO.existeRetiroInventarioTmp(idDespacho, hostBodega);
		//En el caso correcto que no exista el despacho
		if(!verificarExisteDespacho)
		{
			huboError = false;
			//Recuperamos los detalles de los insumos del despacho para ser insertados en la tienda.
			ArrayList<InsumoDespachoTiendaDetalle> detallesDespacho  = InsumoDespachoTiendaDetalleDAO.obtenerDetalleDespachoTienda(idDespacho);
			//Comenzamos a recorrer el detalle y a insertarlo en tienda.
			//Verificaremos si hay más de un detalle para insertar
			
			if(detallesDespacho.size() > 0)
			{
				//En este punto hacemos la inserción del encabezado
				insercionEnc = RetiroInventarioTmpDAO.insertarRetiroInventarioTmp(insumoDespacho.getFechaDespacho(), insumoDespacho.getIdDespacho(), hostBodega, insumoDespacho.getObservacion(), insumoDespacho.getIdTienda(), insumoDespacho.getTienda());
			}
			for(InsumoDespachoTiendaDetalle detalleDespacho: detallesDespacho)
			{
				//Se valida que se haya insertado el encabezado
				if(insercionEnc)
				{
					//Realizamos la homologación del idInsumo entre bodega y tienda
					ModificadorInventario modIngreso = new ModificadorInventario(detalleDespacho.getIdInsumo(), detalleDespacho.getCantidad() );
					insercionDet = RetiroInventarioDetalleTmpDAO.insertarRetiroInventarioDetTmp(idDespacho, modIngreso, hostBodega);
					//Es porque hubo error en la inserción de un detalle, por lo cual devolvemos y notificamos el error
					if(!insercionDet)
					{
						//Borramos los detalles
						RetiroInventarioDetalleTmpDAO.borrarRetiroInventarioDetallesTmp(idDespacho, hostBodega);
						//Borramos el encabezado
						RetiroInventarioTmpDAO.borrarRetiroInventarioTmp(idDespacho, hostBodega);
						Correo correo = new Correo();
						correo.setAsunto("ERROR INSERTANDO RETIRO DE INVENTARIO EN BODEGA " + insumoDespacho.getFechaDespacho());
						CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
						correo.setContrasena(infoCorreo.getClaveCorreo());
						correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
						correo.setMensaje("A continuación informamos que la tienda " + insumoDespacho.getTienda() + " tuvo problemas en la creación del detalle del despacho de pedido. ");
						ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
						contro.enviarCorreoHTML();
						huboError = true;
						break;
					}
				}else
				{
					//Se borra el encabezado del despacho y se quiebra el ciclo for
					RetiroInventarioTmpDAO.borrarRetiroInventarioTmp(idDespacho, hostBodega);
					Correo correo = new Correo();
					correo.setAsunto("ERROR REPLICA ENCABEZADO DEL RETIRO " + insumoDespacho.getFechaDespacho());
					CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
					correo.setContrasena(infoCorreo.getClaveCorreo());
					correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
					correo.setMensaje("A continuación informamos que la tienda " + insumoDespacho.getTienda() + " tuvo problemas en la creación del encabezado del despacho de pedido. ");
					ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
					contro.enviarCorreoHTML();
					break;
				}
			}
			//En caso de que no hayamos tenido problemas en la inserción del encabezado es posible el envío
			//del correo
			if(insercionEnc)
			{
				//Validamos si no hubo error en la inserción del despacho y si no es así, enviamos correos con la confirmación
				Correo correo = new Correo();
				correo.setAsunto("REPLICA DE DESPACHO EN BODEGA - PARA LA TIENDA " + insumoDespacho.getTienda() + " " + insumoDespacho.getFechaDespacho());
				CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
				correo.setContrasena(infoCorreo.getClaveCorreo());
				correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
				correo.setMensaje("Se ha replicado correctamente el retiro en la BODEGA para la tienda " + insumoDespacho.getTienda()  + " el despacho de Inventario número " + idDespacho);
				ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosExitoso);
				contro.enviarCorreoHTML();
			}
		}else
		{
			//Enviamos correo indicando que se está intentando ingresar un despacho que ya existe
			Correo correo = new Correo();
			correo.setAsunto("ERROR RETIRO REPETIDO " + insumoDespacho.getFechaDespacho() + " , despacho " + idDespacho );
			CorreoElectronico infoCorreo = ControladorEnvioCorreo.recuperarCorreo("CUENTACORREOREPORTES", "CLAVECORREOREPORTE");
			correo.setContrasena(infoCorreo.getClaveCorreo());
			correo.setUsuarioCorreo(infoCorreo.getCuentaCorreo());
			correo.setMensaje("A continuación informamos que la tienda " + insumoDespacho.getTienda() + " tuvo problemas se está intentando ingresar un posible despacho que ya existe. El despacho es el  número " + insumoDespacho.getIdDespacho());
			ControladorEnvioCorreo contro = new ControladorEnvioCorreo(correo, correosError);
			contro.enviarCorreoHTML();
		}
		JSONObject respuesta = new JSONObject();
		respuesta.put("respuesta", "ejecutado");
		return(respuesta.toJSONString());
	}
	
	public String actualizarEstadoDespacho(int idDespacho)
	{
		InsumoDespachoTiendaDAO.actualizarEstadoDespacho(idDespacho);
		JSONObject respuesta = new JSONObject();
		respuesta.put("respuesta", "ejecutado");
		return(respuesta.toJSONString());
		
	}
	
	public String obtenerParametro(String valorParametro)
	{
		JSONObject ResultadoJSON = new JSONObject();
		Parametro parametro = ParametrosDAO.obtenerParametro(valorParametro);
		ResultadoJSON.put("valortexto", parametro.getValorTexto());
		ResultadoJSON.put("valornumerico", parametro.getValorNumerico());
		System.out.println(ResultadoJSON.toJSONString());
		return ResultadoJSON.toJSONString();
	}

	
}





package capaControladorINV;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.ReporteDesechoDAO;

/**
 * Reporte de desechos y devoluciones, para verlo en pantalla y bajarlo a Excel.
 *
 * Reproduce el libro que se llevaba a mano: una matriz mensual de tienda contra
 * categoria de producto, y una hoja de detalle por tienda. La diferencia es que
 * sale de la base de datos, asi que no se desactualiza ni depende de que alguien
 * transcriba bien.
 *
 * Las categorias son las nueve del libro original, mas cuatro que el libro no
 * tenia y que dejaban plata por fuera: MADURO, HAMBURGUESA, INGREDIENTES y
 * OTROS. Los ingredientes se cobran por gramos y en el libro no aparecian.
 */
public class ReporteDesechoCtrl {

	/**
	 * Orden de las columnas. Las nueve primeras son las del libro, en su mismo
	 * orden, para que a quien lo usa no le cambie la lectura. Las cuatro ultimas
	 * son las que faltaban.
	 */
	public static final String[] CATEGORIAS = {
		"PORCION", "PIZZETA", "PIZZA MEDIANA", "PIZZA GRANDE", "PIZZA EXTRAGRANDE",
		"NUGGETS", "LASANA", "DEDITOS", "MASAS",
		"MADURO", "HAMBURGUESA", "INGREDIENTES", "OTROS"
	};

	public static final String[] MESES = {
		"", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
		"JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
	};

	/** Rojo de la marca, el mismo del logo y de las pantallas. */
	private static final byte[] ROJO_MARCA = new byte[] { (byte) 208, (byte) 32, (byte) 31 };

	/**
	 * Arma el JSON que consume la pantalla: la matriz por mes y tienda, los
	 * totales por categoria y el detalle.
	 *
	 * @param anio     ano a consultar
	 * @param mes      mes especifico, o 0 para todo el ano
	 * @param idTienda tienda especifica, o 0 para todas
	 */
	@SuppressWarnings("unchecked")
	public String obtenerReporte(int anio, int mes, int idTienda) {
		JSONObject respuesta = new JSONObject();
		try {
			ArrayList<ReporteDesechoDAO.Celda> celdas = ReporteDesechoDAO.obtenerMatriz(anio, mes, idTienda);

			// Se indexa por mes y tienda para armar las filas de la matriz.
			LinkedHashMap<String, LinkedHashMap<String, double[]>> porFila = new LinkedHashMap<>();
			LinkedHashMap<String, Integer> mesesFila = new LinkedHashMap<>();
			LinkedHashMap<String, String> tiendaFila = new LinkedHashMap<>();
			LinkedHashSet<Integer> mesesConDatos = new LinkedHashSet<>();

			for (ReporteDesechoDAO.Celda celda : celdas) {
				String llave = celda.mes + "|" + celda.tienda;
				mesesConDatos.add(Integer.valueOf(celda.mes));
				if (!porFila.containsKey(llave)) {
					porFila.put(llave, new LinkedHashMap<String, double[]>());
					mesesFila.put(llave, Integer.valueOf(celda.mes));
					tiendaFila.put(llave, celda.tienda);
				}
				LinkedHashMap<String, double[]> fila = porFila.get(llave);
				double[] acumulado = fila.get(celda.categoria);
				if (acumulado == null) {
					// posicion 0 unidades, 1 costo, 2 registros
					acumulado = new double[] { 0, 0, 0 };
					fila.put(celda.categoria, acumulado);
				}
				acumulado[0] = acumulado[0] + celda.unidades;
				acumulado[1] = acumulado[1] + celda.costo;
				acumulado[2] = acumulado[2] + celda.registros;
			}

			JSONArray filas = new JSONArray();
			for (Map.Entry<String, LinkedHashMap<String, double[]>> entrada : porFila.entrySet()) {
				String llave = entrada.getKey();
				JSONObject fila = new JSONObject();
				fila.put("mes", mesesFila.get(llave));
				fila.put("nombremes", MESES[mesesFila.get(llave).intValue()]);
				fila.put("tienda", tiendaFila.get(llave));
				JSONObject valores = new JSONObject();
				JSONObject costos = new JSONObject();
				double totalUnidades = 0;
				double totalCosto = 0;
				for (String categoria : CATEGORIAS) {
					double[] acumulado = entrada.getValue().get(categoria);
					double unidades = (acumulado == null) ? 0 : acumulado[0];
					double costo = (acumulado == null) ? 0 : acumulado[1];
					valores.put(categoria, Double.valueOf(unidades));
					costos.put(categoria, Double.valueOf(costo));
					totalUnidades = totalUnidades + unidades;
					totalCosto = totalCosto + costo;
				}
				fila.put("unidades", valores);
				fila.put("costos", costos);
				fila.put("totalunidades", Double.valueOf(totalUnidades));
				fila.put("totalcosto", Double.valueOf(totalCosto));
				filas.add(fila);
			}

			// Totales por origen: el libro separaba punto de venta de DIDI.
			LinkedHashMap<String, double[]> porOrigen = new LinkedHashMap<>();
			for (ReporteDesechoDAO.Celda celda : celdas) {
				double[] acumulado = porOrigen.get(celda.origen);
				if (acumulado == null) {
					acumulado = new double[] { 0, 0 };
					porOrigen.put(celda.origen, acumulado);
				}
				acumulado[0] = acumulado[0] + celda.costo;
				acumulado[1] = acumulado[1] + celda.registros;
			}
			JSONArray origenes = new JSONArray();
			for (Map.Entry<String, double[]> entrada : porOrigen.entrySet()) {
				JSONObject item = new JSONObject();
				item.put("origen", entrada.getKey());
				item.put("costo", Double.valueOf(entrada.getValue()[0]));
				item.put("registros", Double.valueOf(entrada.getValue()[1]));
				origenes.add(item);
			}

			JSONArray categorias = new JSONArray();
			for (String categoria : CATEGORIAS) {
				categorias.add(categoria);
			}

			JSONArray anios = new JSONArray();
			for (Integer valor : ReporteDesechoDAO.obtenerAnios()) {
				anios.add(valor);
			}

			respuesta.put("respuesta", "OK");
			respuesta.put("categorias", categorias);
			respuesta.put("filas", filas);
			respuesta.put("origenes", origenes);
			respuesta.put("anios", anios);
		} catch (Exception e) {
			System.out.println("obtenerReporte: " + e.toString());
			respuesta.put("respuesta", "NOK");
			respuesta.put("detalle", e.toString());
			respuesta.put("filas", new JSONArray());
			respuesta.put("origenes", new JSONArray());
			respuesta.put("anios", new JSONArray());
		}
		return (respuesta.toJSONString());
	}

	/** Detalle para la pantalla, una linea por registro de desecho. */
	@SuppressWarnings("unchecked")
	public String obtenerDetalle(String fechaDesde, String fechaHasta, int idTienda) {
		JSONObject respuesta = new JSONObject();
		JSONArray lineas = new JSONArray();
		try {
			for (ReporteDesechoDAO.Linea linea : ReporteDesechoDAO.obtenerDetalle(fechaDesde, fechaHasta, idTienda)) {
				JSONObject item = new JSONObject();
				item.put("id", Integer.valueOf(linea.idDesechoTienda));
				item.put("fecha", linea.fecha);
				item.put("semana", Integer.valueOf(linea.semana));
				item.put("tienda", linea.tienda);
				item.put("producto", linea.producto);
				item.put("categoria", linea.categoria);
				item.put("origen", linea.origen);
				item.put("destino", linea.destino);
				item.put("motivo", linea.motivo);
				item.put("descripcion", linea.descripcion);
				item.put("tipo", linea.tipo);
				item.put("gramos", Double.valueOf(linea.gramos));
				item.put("cantidad", Double.valueOf(linea.cantidad));
				item.put("costo", Double.valueOf(linea.costo));
				item.put("usuario", linea.usuario);
				item.put("estado", linea.estado);
				item.put("fechacarro", linea.fechaCarro);
				item.put("fechabodega", linea.fechaBodega);
				lineas.add(item);
			}
			respuesta.put("respuesta", "OK");
		} catch (Exception e) {
			System.out.println("obtenerDetalle: " + e.toString());
			respuesta.put("respuesta", "NOK");
			respuesta.put("detalle", e.toString());
		}
		respuesta.put("lineas", lineas);
		return (respuesta.toJSONString());
	}

	/**
	 * Escribe el libro de Excel en el flujo de salida, con la misma forma del
	 * archivo que se llevaba a mano: una hoja de matriz mensual y una hoja de
	 * detalle por cada tienda.
	 *
	 * Se transmite directo en vez de escribirlo a disco como hacen los otros
	 * reportes del proyecto: asi no depende del parametro RUTAINV ni deja
	 * archivos acumulados en el servidor.
	 */
	public void escribirExcel(OutputStream salida, int anio, int mes, int idTienda) throws Exception {
		XSSFWorkbook libro = new XSSFWorkbook();
		try {
			CellStyle estiloTitulo = estiloTitulo(libro);
			CellStyle estiloEncabezado = estiloEncabezado(libro);
			CellStyle estiloTexto = estiloTexto(libro);
			CellStyle estiloNumero = estiloNumero(libro, "#,##0.##");
			CellStyle estiloPesos = estiloNumero(libro, "$ #,##0");
			CellStyle estiloTotal = estiloTotal(libro, "#,##0.##");
			CellStyle estiloTotalPesos = estiloTotal(libro, "$ #,##0");

			hojaMatriz(libro, anio, mes, idTienda, estiloTitulo, estiloEncabezado, estiloTexto,
					estiloNumero, estiloPesos, estiloTotal, estiloTotalPesos);

			String fechaDesde = anio + "-" + (mes > 0 ? dos(mes) : "01") + "-01";
			String fechaHasta = anio + "-" + (mes > 0 ? dos(mes) : "12") + "-31";
			hojasDetalle(libro, fechaDesde, fechaHasta, idTienda, estiloTitulo, estiloEncabezado,
					estiloTexto, estiloNumero, estiloPesos, estiloTotalPesos);

			libro.write(salida);
		} finally {
			libro.close();
		}
	}

	private String dos(int valor) {
		return (valor < 10 ? "0" + valor : String.valueOf(valor));
	}

	/** Hoja con la matriz mensual, equivalente a la hoja PUNTOS DE VENTA. */
	private void hojaMatriz(XSSFWorkbook libro, int anio, int mes, int idTienda, CellStyle estiloTitulo,
			CellStyle estiloEncabezado, CellStyle estiloTexto, CellStyle estiloNumero, CellStyle estiloPesos,
			CellStyle estiloTotal, CellStyle estiloTotalPesos) {
		Sheet hoja = libro.createSheet("PUNTOS DE VENTA");
		hoja.setColumnWidth(0, 4200);
		hoja.setColumnWidth(1, 6200);
		for (int c = 0; c < CATEGORIAS.length; c++) {
			hoja.setColumnWidth(2 + c, 4300);
		}
		hoja.setColumnWidth(2 + CATEGORIAS.length, 3800);
		hoja.setColumnWidth(3 + CATEGORIAS.length, 4200);

		int f = 0;
		Row filaTitulo = hoja.createRow(f++);
		celda(filaTitulo, 0, "MATRIZ DE DESECHOS Y DEVOLUCIONES " + anio, estiloTitulo);
		hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3 + CATEGORIAS.length));
		f++;

		ArrayList<ReporteDesechoDAO.Celda> celdas = ReporteDesechoDAO.obtenerMatriz(anio, mes, idTienda);

		// Un bloque por mes, como en el libro original.
		LinkedHashSet<Integer> meses = new LinkedHashSet<>();
		for (ReporteDesechoDAO.Celda celda : celdas) {
			meses.add(Integer.valueOf(celda.mes));
		}

		for (Integer mesActual : meses) {
			Row filaEnc = hoja.createRow(f++);
			celda(filaEnc, 0, MESES[mesActual.intValue()], estiloEncabezado);
			celda(filaEnc, 1, "PUNTO DE VENTA", estiloEncabezado);
			for (int c = 0; c < CATEGORIAS.length; c++) {
				celda(filaEnc, 2 + c, CATEGORIAS[c], estiloEncabezado);
			}
			celda(filaEnc, 2 + CATEGORIAS.length, "TOTAL UNIDADES", estiloEncabezado);
			celda(filaEnc, 3 + CATEGORIAS.length, "COSTO TOTAL", estiloEncabezado);

			// Acumular por tienda para este mes
			LinkedHashMap<String, double[]> porTienda = new LinkedHashMap<>();
			for (ReporteDesechoDAO.Celda celda : celdas) {
				if (celda.mes != mesActual.intValue()) {
					continue;
				}
				double[] fila = porTienda.get(celda.tienda);
				if (fila == null) {
					// una posicion por categoria, mas unidades totales y costo total
					fila = new double[CATEGORIAS.length + 2];
					porTienda.put(celda.tienda, fila);
				}
				int indice = indiceCategoria(celda.categoria);
				if (indice >= 0) {
					fila[indice] = fila[indice] + celda.unidades;
				}
				fila[CATEGORIAS.length] = fila[CATEGORIAS.length] + celda.unidades;
				fila[CATEGORIAS.length + 1] = fila[CATEGORIAS.length + 1] + celda.costo;
			}

			double[] totalMes = new double[CATEGORIAS.length + 2];
			for (Map.Entry<String, double[]> entrada : porTienda.entrySet()) {
				Row fila = hoja.createRow(f++);
				celda(fila, 0, "", estiloTexto);
				celda(fila, 1, entrada.getKey(), estiloTexto);
				for (int c = 0; c < CATEGORIAS.length; c++) {
					celdaNumero(fila, 2 + c, entrada.getValue()[c], estiloNumero);
					totalMes[c] = totalMes[c] + entrada.getValue()[c];
				}
				celdaNumero(fila, 2 + CATEGORIAS.length, entrada.getValue()[CATEGORIAS.length], estiloNumero);
				celdaNumero(fila, 3 + CATEGORIAS.length, entrada.getValue()[CATEGORIAS.length + 1], estiloPesos);
				totalMes[CATEGORIAS.length] = totalMes[CATEGORIAS.length] + entrada.getValue()[CATEGORIAS.length];
				totalMes[CATEGORIAS.length + 1] = totalMes[CATEGORIAS.length + 1]
						+ entrada.getValue()[CATEGORIAS.length + 1];
			}

			Row filaTotal = hoja.createRow(f++);
			celda(filaTotal, 0, "", estiloTotal);
			celda(filaTotal, 1, "TOTAL MES", estiloTotal);
			for (int c = 0; c < CATEGORIAS.length; c++) {
				celdaNumero(filaTotal, 2 + c, totalMes[c], estiloTotal);
			}
			celdaNumero(filaTotal, 2 + CATEGORIAS.length, totalMes[CATEGORIAS.length], estiloTotal);
			celdaNumero(filaTotal, 3 + CATEGORIAS.length, totalMes[CATEGORIAS.length + 1], estiloTotalPesos);
			f++;
		}

		if (meses.isEmpty()) {
			Row fila = hoja.createRow(f);
			celda(fila, 0, "No hay desechos registrados en el periodo consultado.", estiloTexto);
		}
	}

	/** Una hoja de detalle por tienda, como las hojas MANRIQUE, BELLO y demas. */
	private void hojasDetalle(XSSFWorkbook libro, String fechaDesde, String fechaHasta, int idTienda,
			CellStyle estiloTitulo, CellStyle estiloEncabezado, CellStyle estiloTexto, CellStyle estiloNumero,
			CellStyle estiloPesos, CellStyle estiloTotalPesos) {
		ArrayList<ReporteDesechoDAO.Linea> lineas = ReporteDesechoDAO.obtenerDetalle(fechaDesde, fechaHasta,
				idTienda);

		LinkedHashSet<String> tiendas = new LinkedHashSet<>();
		for (ReporteDesechoDAO.Linea linea : lineas) {
			tiendas.add(linea.tienda);
		}

		String[] encabezados = { "SEMANA", "FECHA", "N. DESECHO", "PRODUCTO", "CATEGORIA", "ORIGEN", "DESTINO",
				"NOVEDAD", "GRAMOS", "CANTIDAD", "COSTO", "USUARIO", "ESTADO", "RECIBIDO CARRO",
				"RECIBIDO BODEGA" };
		int[] anchos = { 3200, 3200, 3000, 8000, 5200, 4200, 5200, 11000, 3000, 3000, 3600, 6000, 4200, 5200,
				5200 };

		for (String tienda : tiendas) {
			// Excel no admite mas de 31 caracteres ni ciertos signos en el nombre
			// de una hoja, asi que se recorta y se limpia.
			String nombreHoja = tienda.replaceAll("[\\\\/\\?\\*\\[\\]:]", " ").trim();
			if (nombreHoja.length() == 0) {
				nombreHoja = "SIN TIENDA";
			}
			if (nombreHoja.length() > 28) {
				nombreHoja = nombreHoja.substring(0, 28);
			}
			if (libro.getSheet(nombreHoja) != null) {
				continue;
			}
			Sheet hoja = libro.createSheet(nombreHoja);
			for (int c = 0; c < anchos.length; c++) {
				hoja.setColumnWidth(c, anchos[c]);
			}

			int f = 0;
			Row filaTitulo = hoja.createRow(f++);
			celda(filaTitulo, 0, "MATRIZ DE DESECHOS Y DEVOLUCIONES - " + tienda, estiloTitulo);
			hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, encabezados.length - 1));
			Row filaSub = hoja.createRow(f++);
			celda(filaSub, 0, "SISTEMA DE GESTION DE LA CALIDAD", estiloTexto);
			f++;

			Row filaEnc = hoja.createRow(f++);
			for (int c = 0; c < encabezados.length; c++) {
				celda(filaEnc, c, encabezados[c], estiloEncabezado);
			}

			double totalTienda = 0;
			for (ReporteDesechoDAO.Linea linea : lineas) {
				if (!tienda.equals(linea.tienda)) {
					continue;
				}
				Row fila = hoja.createRow(f++);
				celda(fila, 0, semanaTexto(linea.semana), estiloTexto);
				celda(fila, 1, linea.fecha, estiloTexto);
				celdaNumero(fila, 2, linea.idDesechoTienda, estiloNumero);
				celda(fila, 3, linea.producto, estiloTexto);
				celda(fila, 4, linea.categoria, estiloTexto);
				celda(fila, 5, linea.origen, estiloTexto);
				celda(fila, 6, linea.destino, estiloTexto);
				celda(fila, 7, linea.motivo, estiloTexto);
				// Se muestra el campo que corresponde al tipo de cobro y el otro se
				// deja vacio, para que no parezca que hay dos medidas del mismo
				// desecho.
				if ("G".equals(linea.tipo)) {
					celdaNumero(fila, 8, linea.gramos, estiloNumero);
					celda(fila, 9, "", estiloTexto);
				} else {
					celda(fila, 8, "", estiloTexto);
					celdaNumero(fila, 9, linea.cantidad, estiloNumero);
				}
				celdaNumero(fila, 10, linea.costo, estiloPesos);
				celda(fila, 11, linea.usuario, estiloTexto);
				celda(fila, 12, linea.estado, estiloTexto);
				celda(fila, 13, linea.fechaCarro, estiloTexto);
				celda(fila, 14, linea.fechaBodega, estiloTexto);
				totalTienda = totalTienda + linea.costo;
			}

			Row filaTotal = hoja.createRow(f);
			celda(filaTotal, 9, "TOTAL TIENDA", estiloEncabezado);
			celdaNumero(filaTotal, 10, totalTienda, estiloTotalPesos);
		}
	}

	private String semanaTexto(int semana) {
		switch (semana) {
			case 1: return ("1RA SEMANA");
			case 2: return ("2DA SEMANA");
			case 3: return ("3RA SEMANA");
			case 4: return ("4TA SEMANA");
			default: return ("5TA SEMANA");
		}
	}

	private int indiceCategoria(String categoria) {
		for (int i = 0; i < CATEGORIAS.length; i++) {
			if (CATEGORIAS[i].equals(categoria)) {
				return (i);
			}
		}
		return (-1);
	}

	private void celda(Row fila, int columna, String valor, CellStyle estilo) {
		org.apache.poi.ss.usermodel.Cell celda = fila.createCell(columna);
		celda.setCellValue(valor == null ? "" : valor);
		celda.setCellStyle(estilo);
	}

	private void celdaNumero(Row fila, int columna, double valor, CellStyle estilo) {
		org.apache.poi.ss.usermodel.Cell celda = fila.createCell(columna);
		celda.setCellValue(valor);
		celda.setCellStyle(estilo);
	}

	private CellStyle estiloTitulo(XSSFWorkbook libro) {
		Font fuente = libro.createFont();
		fuente.setBold(true);
		fuente.setFontHeightInPoints((short) 14);
		fuente.setColor(IndexedColors.WHITE.getIndex());
		CellStyle estilo = libro.createCellStyle();
		estilo.setFont(fuente);
		estilo.setAlignment(HorizontalAlignment.CENTER);
		((org.apache.poi.xssf.usermodel.XSSFCellStyle) estilo)
				.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(ROJO_MARCA, null));
		estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return (estilo);
	}

	private CellStyle estiloEncabezado(XSSFWorkbook libro) {
		Font fuente = libro.createFont();
		fuente.setBold(true);
		fuente.setFontHeightInPoints((short) 9);
		CellStyle estilo = libro.createCellStyle();
		estilo.setFont(fuente);
		estilo.setAlignment(HorizontalAlignment.CENTER);
		estilo.setWrapText(true);
		estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		bordes(estilo);
		return (estilo);
	}

	private CellStyle estiloTexto(XSSFWorkbook libro) {
		Font fuente = libro.createFont();
		fuente.setFontHeightInPoints((short) 9);
		CellStyle estilo = libro.createCellStyle();
		estilo.setFont(fuente);
		bordes(estilo);
		return (estilo);
	}

	private CellStyle estiloNumero(XSSFWorkbook libro, String formato) {
		Font fuente = libro.createFont();
		fuente.setFontHeightInPoints((short) 9);
		CellStyle estilo = libro.createCellStyle();
		estilo.setFont(fuente);
		estilo.setAlignment(HorizontalAlignment.RIGHT);
		estilo.setDataFormat(libro.createDataFormat().getFormat(formato));
		bordes(estilo);
		return (estilo);
	}

	private CellStyle estiloTotal(XSSFWorkbook libro, String formato) {
		Font fuente = libro.createFont();
		fuente.setBold(true);
		fuente.setFontHeightInPoints((short) 9);
		CellStyle estilo = libro.createCellStyle();
		estilo.setFont(fuente);
		estilo.setAlignment(HorizontalAlignment.RIGHT);
		estilo.setDataFormat(libro.createDataFormat().getFormat(formato));
		estilo.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		bordes(estilo);
		return (estilo);
	}

	private void bordes(CellStyle estilo) {
		estilo.setBorderTop(BorderStyle.THIN);
		estilo.setBorderBottom(BorderStyle.THIN);
		estilo.setBorderLeft(BorderStyle.THIN);
		estilo.setBorderRight(BorderStyle.THIN);
	}
}

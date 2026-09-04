package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.ReporteDesechoCtrl;

/**
 * Matriz de desechos por mes, tienda y categoria, para la pantalla del reporte.
 *
 * Parametros: anio, mes (0 = todo el ano), idtienda (0 = todas)
 */
@WebServlet("/ConsultarReporteDesechos")
public class ConsultarReporteDesechos extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ConsultarReporteDesechos() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try {
			int anio = entero(request.getParameter("anio"), 0);
			int mes = entero(request.getParameter("mes"), 0);
			int idTienda = entero(request.getParameter("idtienda"), 0);
			if (anio <= 0) {
				anio = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
			}
			ReporteDesechoCtrl ctrl = new ReporteDesechoCtrl();
			out.write(ctrl.obtenerReporte(anio, mes, idTienda));
		} catch (Exception e) {
			System.out.println("ConsultarReporteDesechos: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando el reporte\","
					+ "\"filas\":[],\"origenes\":[],\"anios\":[]}");
		}
	}

	private int entero(String valor, int porDefecto) {
		try {
			return (Integer.parseInt(valor));
		} catch (Exception e) {
			return (porDefecto);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

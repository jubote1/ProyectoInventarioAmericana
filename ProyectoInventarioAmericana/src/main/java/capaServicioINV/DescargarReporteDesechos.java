package capaServicioINV;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.ReporteDesechoCtrl;

/**
 * Baja el reporte de desechos como libro de Excel, con la misma forma del
 * archivo que se llevaba a mano: una hoja con la matriz mensual y una hoja de
 * detalle por cada tienda.
 *
 * El libro se transmite directo al navegador. Los otros reportes del proyecto
 * lo escriben primero en la ruta del parametro RUTAINV y despues entregan el
 * nombre del archivo; aqui no, para no depender de esa ruta ni ir acumulando
 * archivos en el servidor.
 *
 * Parametros: anio, mes (0 = todo el ano), idtienda (0 = todas)
 */
@WebServlet("/DescargarReporteDesechos")
public class DescargarReporteDesechos extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public DescargarReporteDesechos() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int anio = entero(request.getParameter("anio"), java.util.Calendar.getInstance()
				.get(java.util.Calendar.YEAR));
		int mes = entero(request.getParameter("mes"), 0);
		int idTienda = entero(request.getParameter("idtienda"), 0);

		String nombre = "DESECHOS Y DEVOLUCIONES " + anio + (mes > 0 ? " - " + mes : "") + ".xlsx";

		try {
			ReporteDesechoCtrl ctrl = new ReporteDesechoCtrl();
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
			// Sin cache: el reporte cambia cada vez que una tienda registra un desecho
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			OutputStream salida = response.getOutputStream();
			ctrl.escribirExcel(salida, anio, mes, idTienda);
			salida.flush();
		} catch (Exception e) {
			System.out.println("DescargarReporteDesechos: " + e.toString());
			// Si ya se empezo a escribir el libro no se puede cambiar la respuesta a
			// un error, asi que solo queda dejarlo en el log.
			if (!response.isCommitted()) {
				response.reset();
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write("No se pudo generar el archivo de Excel. Avise a tecnologia.");
			}
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

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
 * Detalle de los desechos, una linea por registro. Es el equivalente a las
 * hojas por tienda del libro que se llevaba a mano.
 *
 * Parametros: fechadesde, fechahasta (aaaa-mm-dd), idtienda (0 = todas)
 */
@WebServlet("/ConsultarDetalleDesechos")
public class ConsultarDetalleDesechos extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ConsultarDetalleDesechos() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String fechaDesde = request.getParameter("fechadesde");
			String fechaHasta = request.getParameter("fechahasta");
			int idTienda = 0;
			try {
				idTienda = Integer.parseInt(request.getParameter("idtienda"));
			} catch (Exception e) {
				idTienda = 0;
			}
			if (fechaDesde == null || fechaDesde.trim().equals("") || fechaHasta == null
					|| fechaHasta.trim().equals("")) {
				out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Faltan las fechas\",\"lineas\":[]}");
				return;
			}
			ReporteDesechoCtrl ctrl = new ReporteDesechoCtrl();
			out.write(ctrl.obtenerDetalle(fechaDesde.trim(), fechaHasta.trim(), idTienda));
		} catch (Exception e) {
			System.out.println("ConsultarDetalleDesechos: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando el detalle\",\"lineas\":[]}");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

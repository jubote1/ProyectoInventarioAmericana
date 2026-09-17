package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.VarianzaMonitoreoCtrl;

/**
 * Monitoreo de varianzas: la consulta que alimenta toda la pantalla.
 *
 * Parametros:
 *   fecha, fechahasta  dd/MM/yyyy o yyyy-MM-dd
 *   tiendas            lista separada por comas; vacio o 0 son todas
 *   grupo              CAROS, CARNES, SINGRUPO; cualquier otra cosa son todos
 *   idinsumo           0 son todos
 *
 * Una fecha que no se entienda NO se reemplaza por la de hoy. Esa costumbre
 * -que estaba en la pantalla de conciliacion del central- hace que la pantalla
 * muestre el resultado de una consulta que nadie pidio, y quien mira no tiene
 * como darse cuenta. Aca se responde FECHAMALA y la pantalla lo dice.
 */
@WebServlet("/ConsultarMonitoreoVarianza")
public class ConsultarMonitoreoVarianza extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public ConsultarMonitoreoVarianza() {
		super();
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		try {
			//El combo de insumos se pide por aparte, una sola vez al abrir.
			if ("insumos".equals(request.getParameter("que"))) {
				out.write(new VarianzaMonitoreoCtrl().obtenerInsumos());
				return;
			}

			final String desde = aFechaSql(request.getParameter("fecha"));
			final String hasta = aFechaSql(request.getParameter("fechahasta"));
			if (desde.length() == 0 || hasta.length() == 0) {
				out.write("{\"respuesta\":\"FECHAMALA\"}");
				return;
			}
			//Al reves no devuelve nada y parece que no hubiera varianza.
			if (desde.compareTo(hasta) > 0) {
				out.write("{\"respuesta\":\"FECHASALREVES\"}");
				return;
			}

			final String tiendas = request.getParameter("tiendas") == null ? ""
					: request.getParameter("tiendas").trim();
			final String grupo = request.getParameter("grupo") == null ? ""
					: request.getParameter("grupo").trim().toUpperCase();

			int idInsumo = 0;
			try {
				idInsumo = Integer.parseInt(request.getParameter("idinsumo"));
			} catch (final Exception e) {
				idInsumo = 0;
			}

			out.write(new VarianzaMonitoreoCtrl().obtenerMonitoreo(desde, hasta, tiendas, grupo, idInsumo));

		} catch (final Exception e) {
			System.out.println("ConsultarMonitoreoVarianza: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando la varianza\"}");
		}
	}

	/** De dd/MM/yyyy a yyyy-MM-dd. Vacio si no se entiende. */
	private String aFechaSql(final String fecha) {
		if (fecha == null || fecha.trim().length() == 0) {
			return ("");
		}
		final String limpia = fecha.trim();
		if (limpia.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
			return (limpia);
		}
		try {
			final SimpleDateFormat origen = new SimpleDateFormat("dd/MM/yyyy");
			origen.setLenient(false);
			final Date convertida = origen.parse(limpia);
			return (new SimpleDateFormat("yyyy-MM-dd").format(convertida));
		} catch (final ParseException e) {
			System.out.println("ConsultarMonitoreoVarianza: fecha ilegible '" + limpia + "'");
			return ("");
		}
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

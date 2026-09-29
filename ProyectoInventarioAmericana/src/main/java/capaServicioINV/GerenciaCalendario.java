package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaCtrl;

/**
 * El calendario de semanas de gerencia.
 *
 * GET  que=consultar  el calendario guardado de un ano
 *      que=generar    la propuesta de un ano, SIN guardarla
 *
 * POST que=guardar    guarda el calendario completo del ano
 *      que=estado     cierra o reabre el ano
 *
 * Lo que cambia datos va por POST a proposito. Un GET se puede disparar desde
 * un enlace, desde el historial o desde una imagen en otra pagina; reescribir
 * un ano entero no puede quedar a merced de eso.
 */
@WebServlet("/GerenciaCalendario")
public class GerenciaCalendario extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** Contra que pantalla se pide el permiso. */
	private static final String PANTALLA = "CalendarioSemanas.html";

	/** Ni calendarios del siglo pasado ni de dentro de diez anos. */
	private static final int ANIO_MINIMO = 2020;
	private static final int ANIO_MAXIMO = 2040;

	public GerenciaCalendario() {
		super();
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		final String negativa = GerenciaSesion.negativa(request, PANTALLA);
		if (negativa != null) {
			out.write(negativa);
			return;
		}

		try {
			final int anio = GerenciaSesion.entero(request, "anio",
					Calendar.getInstance().get(Calendar.YEAR));
			if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
				out.write("{\"respuesta\":\"ANIOMALO\",\"detalle\":\"El ano tiene que estar entre "
						+ ANIO_MINIMO + " y " + ANIO_MAXIMO + "\"}");
				return;
			}

			final GerenciaCtrl ctrl = new GerenciaCtrl();
			if ("generar".equals(GerenciaSesion.texto(request, "que"))) {
				out.write(ctrl.generarCalendario(anio));
			} else {
				out.write(ctrl.consultarCalendario(anio));
			}
		} catch (final Exception e) {
			System.out.println("GerenciaCalendario: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando el calendario\"}");
		}
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		final String negativa = GerenciaSesion.negativa(request, PANTALLA);
		if (negativa != null) {
			out.write(negativa);
			return;
		}

		try {
			final int anio = GerenciaSesion.entero(request, "anio", 0);
			if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
				out.write("{\"respuesta\":\"ANIOMALO\",\"detalle\":\"El ano tiene que estar entre "
						+ ANIO_MINIMO + " y " + ANIO_MAXIMO + "\"}");
				return;
			}

			final GerenciaCtrl ctrl = new GerenciaCtrl();
			final String usuario = GerenciaSesion.usuarioDe(request);
			final String que = GerenciaSesion.texto(request, "que");

			if ("estado".equals(que)) {
				out.write(ctrl.cambiarEstadoAnio(anio, GerenciaSesion.texto(request, "estado"), usuario));
				return;
			}
			if ("guardar".equals(que)) {
				out.write(ctrl.guardarCalendario(anio, GerenciaSesion.texto(request, "semanas"), usuario));
				return;
			}
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaCalendario POST: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error guardando el calendario\"}");
		}
	}
}

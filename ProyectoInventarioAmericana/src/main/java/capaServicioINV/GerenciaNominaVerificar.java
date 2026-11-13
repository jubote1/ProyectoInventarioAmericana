package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaNominaCalculoCtrl;

/**
 * Verificacion de la liquidacion de UNA persona, semana a semana. Solo consulta: no guarda nada.
 *
 * GET  que=empleados   las personas que se pueden escoger
 *      que=verificar   idempleado=N & semanas=aaaa-mm-dd,aaaa-mm-dd,...   (los domingos de cierre)
 *
 * El permiso es el de la pantalla de calculo de nomina: es la misma informacion, vista de otro
 * modo, y asi no hace falta abrir un permiso nuevo en el catalogo del central.
 */
@WebServlet("/GerenciaNominaVerificar")
public class GerenciaNominaVerificar extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** El mismo permiso del calculo de nomina. */
	private static final String PANTALLA = "NominaCalculo.html";

	/** Mas que esto no es una verificacion, es un informe, y cada semana trae decenas de filas. */
	private static final int MAX_SEMANAS = 8;

	public GerenciaNominaVerificar() {
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
			final GerenciaNominaCalculoCtrl ctrl = new GerenciaNominaCalculoCtrl();
			final String que = GerenciaSesion.texto(request, "que");
			if ("empleados".equals(que)) {
				out.write(ctrl.listarParaVerificar());
				return;
			}
			if ("verificar".equals(que)) {
				final int idEmpleado = GerenciaSesion.entero(request, "idempleado", 0);
				if (idEmpleado <= 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Escoja un empleado\"}");
					return;
				}
				final ArrayList<String> semanas = new ArrayList<String>();
				for (final String s : GerenciaSesion.texto(request, "semanas").split(",")) {
					final String limpia = s.trim();
					if (limpia.length() == 0) {
						continue;
					}
					if (!limpia.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
						out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Una semana no se entiende: " + limpia + "\"}");
						return;
					}
					if (!semanas.contains(limpia)) {
						semanas.add(limpia);
					}
				}
				if (semanas.isEmpty() || semanas.size() > MAX_SEMANAS) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Escoja entre 1 y " + MAX_SEMANAS + " semanas\"}");
					return;
				}
				out.write(ctrl.verificarEmpleado(idEmpleado, semanas));
				return;
			}
			out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta que=empleados o que=verificar\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaNominaVerificar: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando\"}");
		}
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

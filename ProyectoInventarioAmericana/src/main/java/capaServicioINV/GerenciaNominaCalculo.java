package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaNominaCalculoCtrl;

/**
 * El calculo de nomina y la carga del dato real.
 *
 * GET  que=estimados     lo calculado de una semana
 *      que=parametros    la ley vigente y su historia
 *      que=desviaciones  que tan bien viene estimando, por tienda
 *
 * POST que=calcular      recalcula la semana desde la biometria
 *      que=pasar         pasa el estimado a la nomina que lee el tablero
 *      que=cargar        carga en lote la nomina real pegada de Siigo
 *      que=parametro     cambia un parametro abriendo una vigencia nueva
 */
@WebServlet("/GerenciaNominaCalculo")
public class GerenciaNominaCalculo extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "NominaCalculo.html";

	/**
	 * Lo que se pega puede traer ciento cuarenta y ocho lineas. Un tope hay que
	 * ponerlo -si no, alguien pega un archivo de diez megas y tumba la memoria-
	 * pero holgado, porque un ano entero de una tienda cabe aqui.
	 */
	private static final int MAX_PEGADO = 2000000;

	public GerenciaNominaCalculo() {
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

			if ("parametros".equals(que)) {
				out.write(ctrl.consultarParametros());
				return;
			}

			final String semana = GerenciaSesion.texto(request, "semana");
			if (!esFecha(semana)) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la semana, como aaaa-mm-dd\"}");
				return;
			}

			if ("desviaciones".equals(que)) {
				out.write(ctrl.consultarDesviaciones(semana));
				return;
			}
			out.write(ctrl.consultarEstimados(semana));
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculo: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando\"}");
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
			final GerenciaNominaCalculoCtrl ctrl = new GerenciaNominaCalculoCtrl();
			final String usuario = GerenciaSesion.usuarioDe(request);
			final String que = GerenciaSesion.texto(request, "que");

			if ("parametro".equals(que)) {
				final String codigo = GerenciaSesion.texto(request, "codigo");
				//El codigo escoge una fila de la tabla de parametros. Se valida
				//el formato aunque venga de la propia pantalla: lo que no este en
				//la forma esperada tiene que caer, no colarse.
				if (!codigo.matches("^[A-Z0-9_]{3,40}$")) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Codigo de parametro invalido\"}");
					return;
				}
				final double valor = GerenciaSesion.valor(request, "valor");
				if (valor < 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"El valor no se entiende. Escriba solo numeros.\"}");
					return;
				}
				out.write(ctrl.guardarParametro(codigo, valor,
						GerenciaSesion.texto(request, "desde"),
						GerenciaSesion.texto(request, "observacion"), usuario));
				return;
			}

			final String semana = GerenciaSesion.texto(request, "semana");
			if (!esFecha(semana)) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la semana, como aaaa-mm-dd\"}");
				return;
			}

			if ("calcular".equals(que)) {
				out.write(ctrl.calcular(semana, usuario));
				return;
			}
			if ("pasar".equals(que)) {
				out.write(ctrl.pasarANomina(semana, usuario));
				return;
			}
			if ("cargar".equals(que)) {
				final String pegado = request.getParameter("datos");
				if (pegado == null || pegado.trim().length() == 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"No vino nada que cargar\"}");
					return;
				}
				if (pegado.length() > MAX_PEGADO) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"Lo pegado es demasiado grande. Cargue una semana a la vez.\"}");
					return;
				}
				//El origen marca el dato de por vida: es lo que distingue un real
				//de un estimado y lo que decide si entra a la desviacion. Solo se
				//aceptan los dos valores previstos.
				final String origen = GerenciaSesion.texto(request, "origen");
				if (!"SIIGO".equals(origen) && !"MANUAL".equals(origen)) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"El origen debe ser SIIGO o MANUAL\"}");
					return;
				}
				out.write(ctrl.cargar(semana, pegado, origen, usuario));
				return;
			}

			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculo POST: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error procesando\"}");
		}
	}

	private static boolean esFecha(final String s) {
		return (s != null && s.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"));
	}
}

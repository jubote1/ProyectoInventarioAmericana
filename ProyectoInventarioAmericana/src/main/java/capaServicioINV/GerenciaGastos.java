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
 * Los gastos parametrizables por tienda.
 *
 * GET  que=consultar  la matriz del mes: tiendas, conceptos, fijos y servicios
 *      que=historia   toda la historia de un concepto en una tienda
 *
 * POST que=fijo       pone un valor nuevo a regir desde una fecha
 *      que=servicio   carga el valor de un servicio, real o estimado aceptado
 */
@WebServlet("/GerenciaGastos")
public class GerenciaGastos extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "GastosTienda.html";

	public GerenciaGastos() {
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
			final GerenciaCtrl ctrl = new GerenciaCtrl();

			if ("historia".equals(GerenciaSesion.texto(request, "que"))) {
				final int idTienda = GerenciaSesion.entero(request, "idtienda", 0);
				final int idConcepto = GerenciaSesion.entero(request, "idconcepto", 0);
				if (idTienda <= 0 || idConcepto <= 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la tienda o el concepto\"}");
					return;
				}
				out.write(ctrl.consultarHistoriaFijo(idTienda, idConcepto));
				return;
			}

			final Calendar hoy = Calendar.getInstance();
			final int anio = GerenciaSesion.entero(request, "anio", hoy.get(Calendar.YEAR));
			final int mes = GerenciaSesion.entero(request, "mes", hoy.get(Calendar.MONTH) + 1);
			if (mes < 1 || mes > 12) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"El mes tiene que ir de 1 a 12\"}");
				return;
			}
			out.write(ctrl.consultarGastos(anio, mes));
		} catch (final Exception e) {
			System.out.println("GerenciaGastos: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando los gastos\"}");
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
			final GerenciaCtrl ctrl = new GerenciaCtrl();
			final String usuario = GerenciaSesion.usuarioDe(request);
			final String que = GerenciaSesion.texto(request, "que");

			final int idTienda = GerenciaSesion.entero(request, "idtienda", 0);
			final int idConcepto = GerenciaSesion.entero(request, "idconcepto", 0);
			if (idTienda <= 0 || idConcepto <= 0) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la tienda o el concepto\"}");
				return;
			}

			//Un valor que no se entiende llega como -1. NO se guarda como cero:
			//un cero se suma al total y despues nadie sabe si era cero de verdad
			//o si alguien digito algo que el sistema no supo leer.
			final double valor = GerenciaSesion.valor(request, "valor");
			if (valor < 0) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
						+ "\"El valor no se entiende. Escriba solo numeros, con punto o coma para los decimales.\"}");
				return;
			}

			if ("fijo".equals(que)) {
				final String desde = GerenciaSesion.texto(request, "desde");
				if (!desde.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"La fecha desde la que rige tiene que venir como aaaa-mm-dd\"}");
					return;
				}
				out.write(ctrl.guardarGastoFijo(idTienda, idConcepto, valor, desde,
						GerenciaSesion.texto(request, "observacion"), usuario));
				return;
			}

			if ("servicio".equals(que)) {
				final int anio = GerenciaSesion.entero(request, "anio", 0);
				final int mes = GerenciaSesion.entero(request, "mes", 0);
				if (anio <= 0 || mes < 1 || mes > 12) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta el ano o el mes\"}");
					return;
				}
				out.write(ctrl.guardarServicio(idTienda, idConcepto, anio, mes, valor,
						GerenciaSesion.texto(request, "origen"),
						GerenciaSesion.entero(request, "meses", 0), usuario));
				return;
			}

			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaGastos POST: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error guardando el gasto\"}");
		}
	}
}

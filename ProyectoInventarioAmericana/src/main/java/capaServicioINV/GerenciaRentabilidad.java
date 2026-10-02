package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaRentabilidadCtrl;

/**
 * El tablero de rentabilidad.
 *
 * GET que=escalera   la escalera de venta a resultado, por tienda
 *     que=meses      los meses del ano que tienen calendario
 *     que=semanas    las semanas de un mes
 *     que=tendencia  el resultado semana a semana de una tienda
 *     que=detalle    de que esta hecha una linea
 *
 * Solo lee. No tiene POST a proposito: aqui no se corrige nada, se mira. Lo que
 * este mal se arregla donde se carga -GastosTienda, EstructuraGerencia- y asi
 * no hay dos sitios por donde pueda entrar el mismo dato.
 */
@WebServlet("/GerenciaRentabilidad")
public class GerenciaRentabilidad extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "Rentabilidad.html";

	public GerenciaRentabilidad() {
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
			final GerenciaRentabilidadCtrl ctrl = new GerenciaRentabilidadCtrl();
			final Calendar hoy = Calendar.getInstance();
			final String que = GerenciaSesion.texto(request, "que");
			final int anio = GerenciaSesion.entero(request, "anio", hoy.get(Calendar.YEAR));

			if ("meses".equals(que)) {
				out.write(ctrl.consultarMeses(anio));
				return;
			}

			if ("tendencia".equals(que)) {
				final int idTienda = GerenciaSesion.entero(request, "idtienda", 0);
				if (idTienda <= 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la tienda\"}");
					return;
				}
				out.write(ctrl.consultarTendencia(idTienda, anio));
				return;
			}

			final int mes = GerenciaSesion.entero(request, "mes", hoy.get(Calendar.MONTH) + 1);
			if (mes < 1 || mes > 12) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"El mes tiene que ir de 1 a 12\"}");
				return;
			}

			if ("semanas".equals(que)) {
				out.write(ctrl.consultarSemanas(anio, mes));
				return;
			}

			if ("detalle".equals(que)) {
				final int idTienda = GerenciaSesion.entero(request, "idtienda", 0);
				final String linea = GerenciaSesion.texto(request, "linea");
				if (idTienda <= 0 || linea.length() == 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la tienda o la linea\"}");
					return;
				}
				//La linea viene de los botones de la pantalla, pero igual se
				//valida: va a parar a un switch que escoge una consulta, y lo que
				//no este en la lista tiene que caer, no colarse.
				if (!linea.matches("^[A-Z]{3,6}$")) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Linea desconocida\"}");
					return;
				}
				out.write(ctrl.consultarDetalle(idTienda, anio, mes, linea));
				return;
			}

			out.write(ctrl.consultarEscalera(anio, mes, GerenciaSesion.entero(request, "idsemana", 0)));
		} catch (final Exception e) {
			System.out.println("GerenciaRentabilidad: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error armando el tablero\"}");
		}
	}
}

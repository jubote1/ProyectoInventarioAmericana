package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaCierreCtrl;
import capaControladorINV.GerenciaRentabilidadCtrl;

/**
 * El cierre del mes por areas: lo que hoy es el Excel GASTOS_FIJOS_TIENDAS.
 *
 * GET que=meses    los meses del ano que tienen calendario
 *     que=semanas  las semanas de un mes, para escoger un rango
 *     (sin que)    el cierre del periodo
 *
 * Solo lee. Los valores se corrigen donde se cargan -GastosTienda,
 * EstructuraGerencia- para que no haya dos puertas por donde entre el mismo
 * dato.
 *
 * Los meses y las semanas se piden al controlador de rentabilidad: es el mismo
 * calendario de gerencia y duplicarlo seria tener dos listas que algun dia
 * dejan de decir lo mismo.
 */
@WebServlet("/GerenciaCierre")
public class GerenciaCierre extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "CierreGerencia.html";

	public GerenciaCierre() {
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
			final Calendar hoy = Calendar.getInstance();
			final String que = GerenciaSesion.texto(request, "que");
			final int anio = GerenciaSesion.entero(request, "anio", hoy.get(Calendar.YEAR));

			if ("meses".equals(que)) {
				out.write(new GerenciaRentabilidadCtrl().consultarMeses(anio));
				return;
			}

			final int mes = GerenciaSesion.entero(request, "mes", hoy.get(Calendar.MONTH) + 1);
			if (mes < 1 || mes > 12) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"El mes tiene que ir de 1 a 12\"}");
				return;
			}

			if ("semanas".equals(que)) {
				out.write(new GerenciaRentabilidadCtrl().consultarSemanas(anio, mes));
				return;
			}

			out.write(new GerenciaCierreCtrl().consultarCierre(anio, mes,
					GerenciaSesion.entero(request, "desde", 0),
					GerenciaSesion.entero(request, "hasta", 0)));
		} catch (final Exception e) {
			System.out.println("GerenciaCierre: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error armando el cierre\"}");
		}
	}
}

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
import capaDAOINV.GerenciaGastoDAO;

/**
 * Las cuatro bolsas de estructura y la nomina agregada por tienda.
 *
 * GET  que=consultar  las bolsas y la nomina del mes
 *
 * POST que=pool       el valor de una bolsa del mes
 *      que=nomina     la nomina de una tienda en el mes
 *
 * LAS BOLSAS NO TRAEN PORCENTAJE
 *
 * Administrativa, logistica y produccion, contact center y publicidad se
 * reparten entre las tiendas por participacion en la venta: se suma la venta de
 * todas y a cada una le toca su porcentaje. Ese calculo NO se guarda aca, se
 * hace al mostrar el tablero contra la venta real del periodo.
 *
 * Congelarlo seria repetir el problema que tiene hoy el Excel: sus pesos estan
 * en puntos enteros -12, 12, 12, 10, 9, 9, 9, 9, 8, 7, 3- que suman 100 y que
 * salieron de una venta vieja. Hoy a Manrique le cargan 12% cuando vende 10,5%,
 * y a Niquia 3% cuando vende 5,9%.
 *
 * LA NOMINA VA AGREGADA, NUNCA PERSONA POR PERSONA
 *
 * Para el tablero el total por tienda basta. Lo que no se guarda no se filtra.
 */
@WebServlet("/GerenciaEstructura")
public class GerenciaEstructura extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "EstructuraGerencia.html";

	/** Las unicas bolsas que existen. Cualquier otra cosa se rechaza. */
	private static final String[] TIPOS = { "ADMINISTRATIVA", "LOGISTICA", "CONTACT", "PUBLICIDAD" };

	public GerenciaEstructura() {
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
			final int anio = GerenciaSesion.entero(request, "anio", hoy.get(Calendar.YEAR));
			final int mes = GerenciaSesion.entero(request, "mes", hoy.get(Calendar.MONTH) + 1);
			if (mes < 1 || mes > 12) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"El mes tiene que ir de 1 a 12\"}");
				return;
			}
			out.write(new GerenciaCtrl().consultarEstructura(anio, mes));
		} catch (final Exception e) {
			System.out.println("GerenciaEstructura: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando la estructura\"}");
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

			final int anio = GerenciaSesion.entero(request, "anio", 0);
			final int mes = GerenciaSesion.entero(request, "mes", 0);
			if (anio <= 0 || mes < 1 || mes > 12) {
				out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta el ano o el mes\"}");
				return;
			}

			if ("pool".equals(que)) {
				final String tipo = GerenciaSesion.texto(request, "tipo").toUpperCase();
				if (!conocido(tipo)) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Esa bolsa de estructura no existe\"}");
					return;
				}
				final double valor = GerenciaSesion.valor(request, "valor");
				if (valor < 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"El valor no se entiende\"}");
					return;
				}
				out.write(ctrl.guardarPool(anio, mes, tipo, valor, usuario));
				return;
			}

			if ("nomina".equals(que)) {
				final int idTienda = GerenciaSesion.entero(request, "idtienda", 0);
				if (idTienda <= 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta la tienda\"}");
					return;
				}
				final double basico = GerenciaSesion.valor(request, "basico");
				final double variable = GerenciaSesion.valor(request, "variable");
				final double seguridad = GerenciaSesion.valor(request, "seguridad");
				final double liquidacion = GerenciaSesion.valor(request, "liquidacion");
				if (basico < 0 || variable < 0 || seguridad < 0 || liquidacion < 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"Alguno de los valores no se entiende. Escriba solo numeros.\"}");
					return;
				}

				final GerenciaGastoDAO.Nomina n = new GerenciaGastoDAO.Nomina();
				n.idTienda = idTienda;
				n.anio = anio;
				n.mes = mes;
				n.empleados = GerenciaSesion.entero(request, "empleados", 0);
				n.sueldoBasico = basico;
				n.sueldoVariable = variable;
				n.seguridadSocial = seguridad;
				n.liquidacion = liquidacion;
				out.write(ctrl.guardarNomina(n, usuario));
				return;
			}

			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaEstructura POST: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error guardando la estructura\"}");
		}
	}

	private boolean conocido(final String tipo) {
		for (int i = 0; i < TIPOS.length; i++) {
			if (TIPOS[i].equals(tipo)) {
				return (true);
			}
		}
		return (false);
	}
}

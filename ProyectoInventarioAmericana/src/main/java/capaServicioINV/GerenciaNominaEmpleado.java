package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import capaControladorINV.GerenciaNominaEmpleadoCtrl;
import capaDAOINV.GerenciaNominaEmpleadoDAO;

/**
 * Nomina por empleado y su reparto entre tiendas segun biometria.
 *
 * GET  que=buscarempleado  empleados cuyo nombre contiene "filtro"
 *      que=consultar       lo cargado, el reparto y los pendientes de una semana
 *
 * POST que=guardar    carga el costo de un empleado en una semana
 *      que=calcular    recalcula el reparto de toda una semana desde biometria
 */
@WebServlet("/GerenciaNominaEmpleado")
public class GerenciaNominaEmpleado extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA = "NominaEmpleado.html";

	public GerenciaNominaEmpleado() {
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
			final GerenciaNominaEmpleadoCtrl ctrl = new GerenciaNominaEmpleadoCtrl();
			final String que = GerenciaSesion.texto(request, "que");

			if ("buscarempleado".equals(que)) {
				out.write(ctrl.buscarEmpleados(GerenciaSesion.texto(request, "filtro")));
				return;
			}

			final String semana = GerenciaSesion.texto(request, "semana");
			out.write(ctrl.consultarSemana(semana));
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleado: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error consultando la nomina\"}");
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
			final GerenciaNominaEmpleadoCtrl ctrl = new GerenciaNominaEmpleadoCtrl();
			final String usuario = GerenciaSesion.usuarioDe(request);
			final String que = GerenciaSesion.texto(request, "que");
			final String semana = GerenciaSesion.texto(request, "semana");

			if ("calcular".equals(que)) {
				out.write(ctrl.calcularReparto(semana, usuario));
				return;
			}

			if ("guardar".equals(que)) {
				final int idEmpleado = GerenciaSesion.entero(request, "idempleado", 0);
				if (idEmpleado <= 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":\"Falta el empleado\"}");
					return;
				}
				//Cada componente se valida por separado: si alguno no se entiende
				//llega como -1 y no se guarda nada, en vez de guardar los otros
				//tres con uno convertido en cero sin que nadie lo pida.
				final double basico = GerenciaSesion.valor(request, "basico");
				final double variable = GerenciaSesion.valor(request, "variable");
				final double seguridad = GerenciaSesion.valor(request, "seguridad");
				final double liquidacion = GerenciaSesion.valor(request, "liquidacion");
				if (basico < 0 || variable < 0 || seguridad < 0 || liquidacion < 0) {
					out.write("{\"respuesta\":\"INVALIDO\",\"detalle\":"
							+ "\"Alguno de los valores no se entiende. Escriba solo numeros, "
							+ "con punto o coma para los decimales -deje en 0 el que no aplique-.\"}");
					return;
				}
				final GerenciaNominaEmpleadoDAO.NominaEmpleado n = new GerenciaNominaEmpleadoDAO.NominaEmpleado();
				n.idEmpleado = idEmpleado;
				n.semana = semana;
				n.sueldoBasico = basico;
				n.sueldoVariable = variable;
				n.seguridadSocial = seguridad;
				n.liquidacion = liquidacion;
				n.origen = "MANUAL";
				out.write(ctrl.guardarNomina(n, usuario));
				return;
			}

			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaNominaEmpleado POST: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error guardando la nomina\"}");
		}
	}
}

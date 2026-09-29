package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import capaDAOINV.GerenciaAccesoDAO;

/**
 * El ingreso al menu Gerencia.
 *
 * GET  dice si hay alguien identificado y a que pantallas puede entrar.
 * POST accion=ingresar valida usuario y clave; accion=salir cierra.
 *
 * POR QUE SE PIDE LA CLAVE OTRA VEZ
 *
 * La aplicacion de inventarios entra con un unico usuario compartido -'admin',
 * la unica fila de inventarioamericana.usuario- que usan las once tiendas. Esa
 * sesion no dice quien es la persona. Para ver nomina hay que saber quien esta
 * mirando, asi que Gerencia pide identificarse contra el central, que si tiene
 * a cada quien con su usuario.
 *
 * No cambia nada de como se entra hoy a las demas pantallas: quien no vaya a
 * Gerencia no se entera de que esto existe.
 */
@WebServlet("/GerenciaAcceso")
public class GerenciaAcceso extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public GerenciaAcceso() {
		super();
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		final GerenciaAccesoDAO.Persona persona = GerenciaSesion.persona(request);
		if (persona == null) {
			out.write("{\"respuesta\":\"NOSESION\"}");
			return;
		}
		//Las pantallas se vuelven a consultar y no se leen de la sesion: si le
		//quitaron el rol mientras tenia la ventana abierta, el menu se le cierra.
		final String pantallas = GerenciaAccesoDAO.pantallasDe(persona.id);
		out.write("{\"respuesta\":\"OK\",\"usuario\":\"" + escapar(persona.usuario)
				+ "\",\"nombre\":\"" + escapar(persona.nombre)
				+ "\",\"pantallas\":\"" + escapar(pantallas) + "\"}");
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		final String accion = GerenciaSesion.texto(request, "accion");

		if ("salir".equals(accion)) {
			final HttpSession sesion = request.getSession(false);
			if (sesion != null) {
				sesion.removeAttribute(GerenciaSesion.ATRIBUTO);
			}
			out.write("{\"respuesta\":\"OK\"}");
			return;
		}

		if (!"ingresar".equals(accion)) {
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Accion desconocida\"}");
			return;
		}

		final String usuario = GerenciaSesion.texto(request, "usuario");
		final String clave = request.getParameter("clave");

		final GerenciaAccesoDAO.Persona persona = GerenciaAccesoDAO.autenticar(usuario, clave);
		if (persona == null) {
			//No se distingue "no existe" de "clave mala": decirlo ayuda mas a
			//quien esta probando usuarios que a quien se equivoco de tecla.
			out.write("{\"respuesta\":\"NOAUTENTICADO\",\"detalle\":\"Usuario o clave incorrectos\"}");
			return;
		}

		final String pantallas = GerenciaAccesoDAO.pantallasDe(persona.id);
		if (pantallas.length() == 0) {
			//La persona existe pero no tiene el rol. Se dice claro, porque aca
			//no hay nada que adivinar: hay que pedirle el permiso a gerencia.
			out.write("{\"respuesta\":\"SINPERMISO\",\"detalle\":"
					+ "\"Su usuario existe pero no tiene el rol Gerencia\"}");
			GerenciaAccesoDAO.registrar(persona.usuario, "ACCESO",
					"Intento entrar a Gerencia sin el rol");
			return;
		}

		final HttpSession sesion = request.getSession(true);
		sesion.setAttribute(GerenciaSesion.ATRIBUTO, persona);
		GerenciaAccesoDAO.registrar(persona.usuario, "ACCESO", "Entro al menu Gerencia");

		out.write("{\"respuesta\":\"OK\",\"usuario\":\"" + escapar(persona.usuario)
				+ "\",\"nombre\":\"" + escapar(persona.nombre)
				+ "\",\"pantallas\":\"" + escapar(pantallas) + "\"}");
	}

	/** Un nombre con comillas o con una barra invertida rompe el JSON a mano. */
	private String escapar(final String texto) {
		return (texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\""));
	}
}

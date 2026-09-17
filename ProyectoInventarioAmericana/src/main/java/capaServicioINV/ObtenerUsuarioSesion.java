package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import capaModeloINV.Usuario;

/**
 * Devuelve el nombre de quien tiene la sesion abierta.
 *
 * La barra de arriba tenia un LABEL para el nombre del usuario que nunca se
 * llenaba: ValidarUsuarioAplicacion responde texto plano -OK, OKA o NOK- y no
 * trae el nombre. Agregarselo habria obligado a cambiar las doce paginas que
 * comparan esa respuesta como texto, asi que se hace aparte y el menu se llena
 * solo: la pagina que carga el menu no tiene que hacer nada.
 */
@WebServlet("/ObtenerUsuarioSesion")
public class ObtenerUsuarioSesion extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public ObtenerUsuarioSesion() {
		super();
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		final HttpSession sesion = request.getSession(false);
		final Usuario usuario = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");

		String nombre = "";
		if (usuario != null) {
			//El nombre largo es el que se le muestra a la gente. Si no esta, se
			//cae al de ingreso antes que dejar la barra en blanco.
			nombre = usuario.getNombreLargo();
			if (nombre == null || nombre.trim().length() == 0) {
				nombre = usuario.getNombreUsuario() == null ? "" : usuario.getNombreUsuario();
			}
		}

		final PrintWriter out = response.getWriter();
		out.write("{\"nombre\":\"" + escapar(nombre.trim()) + "\"}");
	}

	/** Un nombre con comillas o con una barra invertida rompe el JSON a mano. */
	private String escapar(final String texto) {
		return (texto.replace("\\", "\\\\").replace("\"", "\\\""));
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

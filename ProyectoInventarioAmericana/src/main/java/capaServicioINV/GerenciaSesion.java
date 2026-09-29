package capaServicioINV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import capaDAOINV.GerenciaAccesoDAO;

/**
 * El portero del menu Gerencia.
 *
 * Toda pantalla de Gerencia pregunta aca antes de responder nada. No alcanza
 * con que el menu esconda el boton: los servlets se pueden llamar directo por
 * URL, igual que los del central, y detras de estos hay nomina.
 *
 * La comprobacion sale a la base en cada llamado en vez de confiar en lo que
 * quedo guardado en la sesion cuando la persona entro. Cuesta una consulta y
 * paga: a quien le quiten el rol deja de entrar en el siguiente clic, y no
 * cuando se le venza la sesion.
 */
public final class GerenciaSesion {

	/** Donde queda guardado quien entro a Gerencia. */
	public static final String ATRIBUTO = "gerencia";

	private GerenciaSesion() {
	}

	/** Quien entro a Gerencia, o null si nadie. */
	public static GerenciaAccesoDAO.Persona persona(final HttpServletRequest request) {
		final HttpSession sesion = request.getSession(false);
		if (sesion == null) {
			return (null);
		}
		final Object guardado = sesion.getAttribute(ATRIBUTO);
		return (guardado instanceof GerenciaAccesoDAO.Persona
				? (GerenciaAccesoDAO.Persona) guardado : null);
	}

	/**
	 * Si quien esta pidiendo puede entrar a esa pantalla.
	 *
	 * Devuelve null cuando si puede, y el JSON de la negativa cuando no, para
	 * que el servlet lo escriba tal cual y se devuelva. Se distingue NOSESION
	 * de SINPERMISO porque la pantalla hace cosas distintas: con el primero
	 * muestra el ingreso, con el segundo dice que hay que pedir el permiso.
	 */
	public static String negativa(final HttpServletRequest request, final String pantalla) {
		final GerenciaAccesoDAO.Persona persona = persona(request);
		if (persona == null) {
			return ("{\"respuesta\":\"NOSESION\"}");
		}
		if (!GerenciaAccesoDAO.tienePermiso(persona.id, pantalla)) {
			return ("{\"respuesta\":\"SINPERMISO\",\"detalle\":"
					+ "\"Su usuario no tiene permiso sobre esta pantalla de Gerencia\"}");
		}
		return (null);
	}

	/** El nombre con el que se firman los cambios. */
	public static String usuarioDe(final HttpServletRequest request) {
		final GerenciaAccesoDAO.Persona persona = persona(request);
		return (persona == null ? "" : persona.usuario);
	}

	/** Un entero de la request, con valor por defecto si no se entiende. */
	public static int entero(final HttpServletRequest request, final String nombre, final int siNo) {
		try {
			return (Integer.parseInt(request.getParameter(nombre).trim()));
		} catch (final Exception e) {
			return (siNo);
		}
	}

	/**
	 * Un valor en pesos de la request.
	 *
	 * Acepta coma o punto como decimal y quita los separadores de miles, porque
	 * quien digita un arriendo lo escribe como lo lee: 3.885.000 o 3885000,50.
	 * Si no se entiende devuelve -1 y el servlet lo rechaza, en vez de guardar
	 * un cero que despues nadie sabe si era cero de verdad.
	 */
	public static double valor(final HttpServletRequest request, final String nombre) {
		final String crudo = request.getParameter(nombre);
		if (crudo == null || crudo.trim().length() == 0) {
			return (-1);
		}
		String limpio = crudo.trim().replace(" ", "").replace("$", "");
		final int ultimaComa = limpio.lastIndexOf(',');
		final int ultimoPunto = limpio.lastIndexOf('.');
		//El separador decimal es el ultimo que aparezca; el otro es de miles.
		if (ultimaComa >= 0 && ultimaComa > ultimoPunto) {
			limpio = limpio.replace(".", "").replace(',', '.');
		} else {
			limpio = limpio.replace(",", "");
		}
		try {
			final double valor = Double.parseDouble(limpio);
			return (valor < 0 ? -1 : valor);
		} catch (final Exception e) {
			return (-1);
		}
	}

	/** Un texto de la request, nunca null. */
	public static String texto(final HttpServletRequest request, final String nombre) {
		final String crudo = request.getParameter(nombre);
		return (crudo == null ? "" : crudo.trim());
	}
}

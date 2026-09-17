package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.ComposicionProductoDAO;
import capaDAOINV.TiendaDAO;
import capaModeloINV.Tienda;
import capaModeloINV.Usuario;

/**
 * Composicion y costeo de productos.
 *
 * Acciones, por el parametro "que":
 *   catalogo   los productos que se pueden costear
 *   costear    cuanto cuesta un conjunto de productos
 *   comparar   en que difiere una tienda del maestro
 *   traer      llena el maestro con lo que tiene una tienda
 *   replicar   manda el maestro a una tienda
 *
 * TRAER y REPLICAR escriben. Replicar borra y reescribe la composicion entera
 * de una tienda, que es lo que decide cuanto inventario descuenta cada venta,
 * asi que exige sesion y queda registrado a nombre de quien lo hizo. Un cambio
 * de esos sin responsable no se puede auditar despues.
 */
@WebServlet("/ComposicionProducto")
public class ComposicionProducto extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public ComposicionProducto() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		try {
			final String que = request.getParameter("que") == null ? "catalogo"
					: request.getParameter("que").trim();

			if ("catalogo".equals(que)) {
				catalogo(request, out);
			} else if ("costear".equals(que)) {
				costear(request, out);
			} else if ("comparar".equals(que)) {
				comparar(request, out);
			} else if ("traer".equals(que)) {
				traer(request, out);
			} else if ("replicar".equals(que)) {
				replicar(request, out);
			} else {
				out.write("{\"respuesta\":\"ACCIONDESCONOCIDA\"}");
			}
		} catch (final Exception e) {
			System.out.println("ComposicionProducto: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error procesando la solicitud\"}");
		}
	}

	// =======================================================================
	// Leer
	// =======================================================================

	@SuppressWarnings("unchecked")
	private void catalogo(final HttpServletRequest request, final PrintWriter out) {
		final JSONObject raiz = new JSONObject();
		final JSONArray lista = new JSONArray();
		final ArrayList<ComposicionProductoDAO.Producto> productos =
				ComposicionProductoDAO.obtenerCatalogo(request.getParameter("tipo"));
		for (int i = 0; i < productos.size(); i++) {
			final ComposicionProductoDAO.Producto p = productos.get(i);
			final JSONObject o = new JSONObject();
			o.put("idproducto", p.idProducto);
			o.put("descripcion", p.descripcion);
			o.put("tamano", p.tamano);
			o.put("tipo", p.tipo);
			o.put("insumos", p.insumos);
			lista.add(o);
		}
		raiz.put("respuesta", "OK");
		raiz.put("productos", lista);
		raiz.put("filasmaestro", ComposicionProductoDAO.filasMaestro());
		out.write(raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	private void costear(final HttpServletRequest request, final PrintWriter out) {
		final JSONObject raiz = new JSONObject();
		final JSONArray lista = new JSONArray();
		final ArrayList<ComposicionProductoDAO.LineaCosto> lineas =
				ComposicionProductoDAO.costear(request.getParameter("idproductos"));
		double total = 0;
		int sinCosto = 0;
		int sinHomologar = 0;
		for (int i = 0; i < lineas.size(); i++) {
			final ComposicionProductoDAO.LineaCosto l = lineas.get(i);
			final JSONObject o = new JSONObject();
			o.put("idinsumo", l.idInsumo);
			o.put("insumo", l.insumo);
			o.put("unidad", l.unidad);
			o.put("cantidad", l.cantidad);
			o.put("unitario", l.costoUnitario);
			o.put("costo", l.costo);
			o.put("sincosto", l.sinCosto);
			o.put("sinhomologar", l.sinHomologar);
			lista.add(o);
			total = total + l.costo;
			if (l.sinCosto) {
				sinCosto++;
			}
			if (l.sinHomologar) {
				sinHomologar++;
			}
		}
		raiz.put("respuesta", "OK");
		raiz.put("lineas", lista);
		raiz.put("total", Double.valueOf(total));
		//Se dicen aparte para que la pantalla pueda avisar que el total esta
		//incompleto. Un total que no lo dice se lee como si fuera el real.
		raiz.put("sincosto", sinCosto);
		raiz.put("sinhomologar", sinHomologar);
		out.write(raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	private void comparar(final HttpServletRequest request, final PrintWriter out) {
		final int idTienda = entero(request.getParameter("idtienda"));
		final Tienda tienda = tienda(idTienda);
		if (tienda == null || tienda.getHosbd() == null || tienda.getHosbd().trim().length() == 0) {
			out.write("{\"respuesta\":\"SINHOST\"}");
			return;
		}
		final JSONObject raiz = new JSONObject();
		final JSONArray lista = new JSONArray();
		final ArrayList<ComposicionProductoDAO.Diferencia> diferencias =
				ComposicionProductoDAO.compararConTienda(idTienda, tienda.getHosbd());
		int faltan = 0;
		int sobran = 0;
		int distintas = 0;
		for (int i = 0; i < diferencias.size(); i++) {
			final ComposicionProductoDAO.Diferencia d = diferencias.get(i);
			final JSONObject o = new JSONObject();
			o.put("idproducto", d.idProducto);
			o.put("producto", d.producto);
			o.put("iditem", d.idItem);
			o.put("insumo", d.insumo);
			o.put("tipo", d.tipo);
			o.put("maestro", Double.valueOf(d.enMaestro));
			o.put("tienda", Double.valueOf(d.enTienda));
			lista.add(o);
			if ("FALTA".equals(d.tipo)) {
				faltan++;
			} else if ("SOBRA".equals(d.tipo)) {
				sobran++;
			} else {
				distintas++;
			}
		}
		raiz.put("respuesta", "OK");
		raiz.put("diferencias", lista);
		raiz.put("faltan", faltan);
		raiz.put("sobran", sobran);
		raiz.put("distintas", distintas);
		raiz.put("tienda", tienda.getNombreTienda());
		out.write(raiz.toJSONString());
	}

	// =======================================================================
	// Escribir
	// =======================================================================

	private void traer(final HttpServletRequest request, final PrintWriter out) {
		final String usuario = usuarioEnSesion(request);
		if (usuario == null) {
			out.write("{\"respuesta\":\"SINSESION\"}");
			return;
		}
		final int idTienda = entero(request.getParameter("idtienda"));
		final Tienda tienda = tienda(idTienda);
		if (tienda == null || tienda.getHosbd() == null || tienda.getHosbd().trim().length() == 0) {
			out.write("{\"respuesta\":\"SINHOST\"}");
			return;
		}
		final String resultado = ComposicionProductoDAO.traerDeTienda(idTienda, tienda.getHosbd());
		final String[] partes = resultado.split("\\|");
		if (!"OK".equals(partes[0])) {
			out.write("{\"respuesta\":\"" + partes[0] + "\"}");
			return;
		}
		out.write("{\"respuesta\":\"OK\",\"productos\":" + partes[1]
				+ ",\"filas\":" + partes[2] + ",\"repetidos\":" + partes[3]
				+ ",\"tienda\":\"" + escapar(tienda.getNombreTienda()) + "\"}");
	}

	private void replicar(final HttpServletRequest request, final PrintWriter out) {
		final String usuario = usuarioEnSesion(request);
		if (usuario == null) {
			out.write("{\"respuesta\":\"SINSESION\"}");
			return;
		}
		final int idTienda = entero(request.getParameter("idtienda"));
		final Tienda tienda = tienda(idTienda);
		if (tienda == null || tienda.getHosbd() == null || tienda.getHosbd().trim().length() == 0) {
			out.write("{\"respuesta\":\"SINHOST\"}");
			return;
		}

		final String resultado = ComposicionProductoDAO.replicarATienda(idTienda, tienda.getHosbd());
		final String[] partes = resultado.split("\\|");

		if ("OK".equals(partes[0])) {
			ComposicionProductoDAO.registrarReplica(idTienda, usuario,
					Integer.parseInt(partes[2]), Integer.parseInt(partes[1]), "OK", "");
			out.write("{\"respuesta\":\"OK\",\"antes\":" + partes[1] + ",\"despues\":" + partes[2]
					+ ",\"tienda\":\"" + escapar(tienda.getNombreTienda()) + "\"}");
		} else {
			//El intento fallido tambien queda registrado: si una tienda quedo
			//rara, lo primero que uno quiere saber es si alguien intento
			//replicarle y no pudo.
			ComposicionProductoDAO.registrarReplica(idTienda, usuario, 0, 0, partes[0],
					partes.length > 1 ? partes[1] : "");
			out.write("{\"respuesta\":\"" + partes[0] + "\"}");
		}
	}

	// =======================================================================
	// Plomeria
	// =======================================================================

	/** El nombre de quien tiene la sesion, o null si no hay. */
	private String usuarioEnSesion(final HttpServletRequest request) {
		final HttpSession sesion = request.getSession(false);
		final Usuario usuario = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
		if (usuario == null) {
			return (null);
		}
		String nombre = usuario.getNombreLargo();
		if (nombre == null || nombre.trim().length() == 0) {
			nombre = usuario.getNombreUsuario();
		}
		return (nombre == null || nombre.trim().length() == 0 ? null : nombre.trim());
	}

	/**
	 * La tienda con su host de base de datos.
	 *
	 * Se busca en obtenerTiendas, que lee pizzaamericana.tienda, y NO en
	 * obtenerUrlTienda, que lee inventarioamericana.tienda: ahi la columna url
	 * esta vacia o nula en las trece tiendas, asi que devolveria una tienda sin
	 * host y todo intento de conectarse fallaria sin decir por que.
	 */
	private Tienda tienda(final int idTienda) {
		if (idTienda <= 0) {
			return (null);
		}
		try {
			final ArrayList<Tienda> tiendas = TiendaDAO.obtenerTiendas();
			for (int i = 0; i < tiendas.size(); i++) {
				if (tiendas.get(i).getIdTienda() == idTienda) {
					return (tiendas.get(i));
				}
			}
			return (null);
		} catch (final Exception e) {
			System.out.println("ComposicionProducto.tienda: " + e.toString());
			return (null);
		}
	}

	private int entero(final String valor) {
		try {
			return (Integer.parseInt(valor));
		} catch (final Exception e) {
			return (0);
		}
	}

	private String escapar(final String texto) {
		if (texto == null) {
			return ("");
		}
		return (texto.replace("\\", "\\\\").replace("\"", "\\\""));
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.InsumoMaestroDAO;

/**
 * El maestro de insumos para la pantalla de administracion.
 *
 * Dos acciones, por el parametro "que":
 *   lista  (por defecto) devuelve los 120 insumos con todo lo que se muestra
 *   grupo                cambia el grupo de varianza de un insumo
 *
 * No se metio dentro de CRUDInsumo. Ese servlet reparte por un numero de
 * operacion, y confundir un numero con otro sale caro: en el central, una
 * pantalla llamaba a la operacion 4 de fidelizacion creyendo que consultaba
 * puntos cuando lo que hacia era desactivar al cliente del programa. Una
 * accion con nombre no se puede confundir con otra.
 */
@WebServlet("/MaestroInsumos")
public class MaestroInsumos extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public MaestroInsumos() {
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
			final String que = request.getParameter("que") == null ? "lista"
					: request.getParameter("que").trim();

			if ("grupo".equals(que)) {
				int idInsumo = 0;
				try {
					idInsumo = Integer.parseInt(request.getParameter("idinsumo"));
				} catch (final Exception e) {
					idInsumo = 0;
				}
				final String grupo = request.getParameter("grupo") == null ? ""
						: request.getParameter("grupo");
				final String resultado = InsumoMaestroDAO.fijarGrupoVarianza(idInsumo, grupo);
				out.write("{\"respuesta\":\"" + resultado + "\"}");
				return;
			}

			final JSONObject raiz = new JSONObject();
			final JSONArray lista = new JSONArray();
			final ArrayList<InsumoMaestroDAO.Fila> filas = InsumoMaestroDAO.obtenerMaestro();
			for (int i = 0; i < filas.size(); i++) {
				final InsumoMaestroDAO.Fila f = filas.get(i);
				final JSONObject o = new JSONObject();
				o.put("idinsumo", f.idInsumo);
				o.put("nombre", f.nombre);
				o.put("unidadmedida", f.unidadMedida);
				o.put("categoria", f.categoria);
				o.put("grupovarianza", f.grupoVarianza);
				o.put("costounidad", f.costoUnidad);
				o.put("embalajecosto", f.embalajeCosto);
				o.put("manejacanastas", f.manejaCanastas);
				o.put("cantidadxcanasta", f.cantidadXCanasta);
				o.put("controltienda", f.controlTienda);
				o.put("umbral", f.umbral);
				o.put("tieneumbral", f.tieneUmbral);
				o.put("tiendas", f.tiendasHomologadas);
				lista.add(o);
			}
			raiz.put("respuesta", "OK");
			raiz.put("insumos", lista);
			out.write(raiz.toJSONString());

		} catch (final Exception e) {
			System.out.println("MaestroInsumos: " + e.toString());
			out.write("{\"respuesta\":\"NOK\",\"insumos\":[]}");
		}
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}

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

import capaDAOINV.DespachoSugeridoDAO;

/**
 * Servlet que atiende la nueva versión interactiva de despacho de inventario
 * a tiendas con el motor de sugerido de 4 semanas.
 */
@WebServlet("/DespachoSugerido")
public class DespachoSugeridoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public DespachoSugeridoServlet() {
		super();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		procesar(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		procesar(request, response);
	}

	@SuppressWarnings("unchecked")
	private void procesar(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();

		final String accion = request.getParameter("accion") == null ? "consultar_control"
				: request.getParameter("accion").trim();

		int idTienda = 0;
		try {
			idTienda = Integer.parseInt(request.getParameter("idtienda"));
		} catch (final Exception e) {
			idTienda = 0;
		}

		if (idTienda <= 0) {
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Tienda no válida\"}");
			return;
		}

		try {
			if ("consultar_control".equalsIgnoreCase(accion)) {
				final DespachoSugeridoDAO.EstadoControl est = DespachoSugeridoDAO.consultarControl(idTienda);
				final JSONObject json = new JSONObject();
				json.put("respuesta", "OK");
				json.put("idtienda", est.idTienda);
				json.put("tiene_control", est.tieneControl);
				json.put("dias_desde_actualizacion", est.diasDesdeUltimaActualizacion);
				json.put("requiere_actualizar", est.requiereActualizar);
				json.put("fecha_ultima_actualizacion", est.fechaUltimaActualizacion);
				json.put("dias_vigencia", est.diasVigencia);
				json.put("fecha_desde", est.fechaDesde);
				json.put("fecha_hasta", est.fechaHasta);
				json.put("total_despachos", est.totalDespachosAnalizados);
				json.put("usuario_actualizo", est.usuarioActualizo);
				out.write(json.toJSONString());

			} else if ("actualizar_4sem".equalsIgnoreCase(accion)) {
				final String usuario = request.getParameter("usuario") != null
						? request.getParameter("usuario").trim() : "sistema";
				final DespachoSugeridoDAO.ResultadoActualizacion r =
						DespachoSugeridoDAO.recalcularSugerido4Semanas(idTienda, usuario);

				final JSONObject json = new JSONObject();
				json.put("respuesta", r.ok ? "OK" : "NOK");
				json.put("detalle", r.mensaje);
				json.put("insumos_calculados", r.insumosCalculados);
				json.put("despachos_analizados", r.despachosAnalizados);
				json.put("fecha_desde", r.fechaDesde);
				json.put("fecha_hasta", r.fechaHasta);
				out.write(json.toJSONString());

			} else if ("obtener_inventario".equalsIgnoreCase(accion)) {
				final String fechaSurtir = request.getParameter("fechasurtir");
				final ArrayList<DespachoSugeridoDAO.InsumoSugeridoDTO> items =
						DespachoSugeridoDAO.obtenerSugeridoTienda(idTienda, fechaSurtir);

				final JSONObject json = new JSONObject();
				json.put("respuesta", "OK");
				final JSONArray arr = new JSONArray();
				for (final DespachoSugeridoDAO.InsumoSugeridoDTO it : items) {
					final JSONObject o = new JSONObject();
					o.put("idinsumo", it.idInsumo);
					o.put("nombreinsumo", it.nombreInsumo);
					o.put("unidadmedida", it.unidadMedida);
					o.put("idcategoria", it.idCategoria);
					o.put("nombrecategoria", it.nombreCategoria);
					o.put("colorcategoria", it.colorCategoria);
					o.put("manejacanastas", it.manejaCanastas);
					o.put("cantidadxcanasta", it.cantidadXCanasta);
					o.put("nombrecontenedor", it.nombreContenedor);
					o.put("promedio4sem", it.promedio4Sem);
					o.put("total4sem", it.total4Sem);
					o.put("vecesdespachado", it.vecesDespachado);
					o.put("sugerido", it.cantidadSugerida);
					o.put("canastassugeridas", it.canastasSugeridas);
					o.put("cantidadtienda", it.cantidadTienda);
					o.put("cantidadllevar", it.cantidadLlevar);
					o.put("orden", it.orden);
					arr.add(o);
				}
				json.put("insumos", arr);

				// Incluir también el estado de control de la tienda
				final DespachoSugeridoDAO.EstadoControl est = DespachoSugeridoDAO.consultarControl(idTienda);
				final JSONObject ctrl = new JSONObject();
				ctrl.put("tiene_control", est.tieneControl);
				ctrl.put("dias_desde_actualizacion", est.diasDesdeUltimaActualizacion);
				ctrl.put("requiere_actualizar", est.requiereActualizar);
				ctrl.put("fecha_ultima_actualizacion", est.fechaUltimaActualizacion);
				ctrl.put("total_despachos", est.totalDespachosAnalizados);
				json.put("control", ctrl);

				out.write(json.toJSONString());

			} else {
				out.write("{\"respuesta\":\"ACCIONDESCONOCIDA\"}");
			}
		} catch (final Exception e) {
			out.write("{\"respuesta\":\"NOK\",\"detalle\":\"Error: " + e.getMessage() + "\"}");
		}
	}
}

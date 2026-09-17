package capaControladorINV;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.VarianzaMonitoreoDAO;

/**
 * Arma lo que consume la pantalla de monitoreo de varianzas.
 *
 * Todo va en una sola respuesta -resumen, tiendas, insumos, serie diaria y
 * detalle- en vez de cinco llamados. Son cinco consultas contra la misma tabla
 * y con el mismo filtro: separarlas obligaria a la pantalla a coordinar cinco
 * respuestas que pueden llegar en desorden, y a mostrar tarjetas que no cuadran
 * con la tabla de abajo mientras una de ellas va en camino.
 */
public class VarianzaMonitoreoCtrl {

	@SuppressWarnings("unchecked")
	public String obtenerMonitoreo(final String fechaDesde, final String fechaHasta,
			final String tiendas, final String grupo, final int idInsumo) {

		final JSONObject raiz = new JSONObject();

		final VarianzaMonitoreoDAO.Resumen r = VarianzaMonitoreoDAO.obtenerResumen(
				fechaDesde, fechaHasta, tiendas, grupo, idInsumo);

		final JSONObject resumen = new JSONObject();
		resumen.put("valor", r.valor);
		resumen.put("perdida", r.perdida);
		resumen.put("sobrante", r.sobrante);
		resumen.put("tiendas", r.tiendas);
		resumen.put("insumos", r.insumos);
		resumen.put("dias", r.dias);
		resumen.put("diasfuerumbral", r.diasFueraUmbral);
		raiz.put("resumen", resumen);

		raiz.put("tiendas", aJson(VarianzaMonitoreoDAO.obtenerPorTienda(
				fechaDesde, fechaHasta, tiendas, grupo, idInsumo)));
		raiz.put("insumos", aJson(VarianzaMonitoreoDAO.obtenerPorInsumo(
				fechaDesde, fechaHasta, tiendas, grupo, idInsumo)));
		raiz.put("dias", aJson(VarianzaMonitoreoDAO.obtenerPorDia(
				fechaDesde, fechaHasta, tiendas, grupo, idInsumo)));
		raiz.put("detalle", aJson(VarianzaMonitoreoDAO.obtenerDetalle(
				fechaDesde, fechaHasta, tiendas, grupo, idInsumo)));

		raiz.put("respuesta", "OK");
		raiz.put("desde", fechaDesde);
		raiz.put("hasta", fechaHasta);
		return (raiz.toJSONString());
	}

	/** El combo de insumos del filtro. */
	@SuppressWarnings("unchecked")
	public String obtenerInsumos() {
		final JSONObject raiz = new JSONObject();
		final JSONArray lista = new JSONArray();
		final ArrayList<VarianzaMonitoreoDAO.Linea> insumos =
				VarianzaMonitoreoDAO.obtenerInsumosConVarianza();
		for (int i = 0; i < insumos.size(); i++) {
			final VarianzaMonitoreoDAO.Linea l = insumos.get(i);
			final JSONObject o = new JSONObject();
			o.put("id", l.id);
			o.put("etiqueta", l.etiqueta);
			o.put("unidad", l.unidad);
			o.put("grupo", l.grupo);
			lista.add(o);
		}
		raiz.put("respuesta", "OK");
		raiz.put("insumos", lista);
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJson(final ArrayList<VarianzaMonitoreoDAO.Linea> lineas) {
		final JSONArray salida = new JSONArray();
		for (int i = 0; i < lineas.size(); i++) {
			final VarianzaMonitoreoDAO.Linea l = lineas.get(i);
			final JSONObject o = new JSONObject();
			o.put("id", l.id);
			o.put("etiqueta", l.etiqueta);
			o.put("etiqueta2", l.etiqueta2);
			o.put("unidad", l.unidad);
			o.put("grupo", l.grupo);
			o.put("cantidad", l.cantidad);
			o.put("valor", l.valor);
			o.put("perdida", l.perdida);
			o.put("sobrante", l.sobrante);
			o.put("dias", l.dias);
			o.put("diasfuerumbral", l.diasFueraUmbral);
			salida.add(o);
		}
		return (salida);
	}
}

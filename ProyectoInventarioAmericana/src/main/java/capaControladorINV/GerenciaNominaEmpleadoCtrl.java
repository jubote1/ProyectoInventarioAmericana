package capaControladorINV;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.GerenciaGastoDAO;
import capaDAOINV.GerenciaNominaEmpleadoDAO;

/**
 * Lo que consume NominaEmpleado.html: cargar el costo de un empleado por
 * semana, y ver como queda repartido entre tiendas segun su biometria.
 *
 * Clase aparte de GerenciaCtrl a proposito -no porque el tema no encaje, sino
 * para no tocar un archivo de 700+ lineas que otras pantallas de Gerencia
 * siguen usando activamente-.
 */
public class GerenciaNominaEmpleadoCtrl {

	@SuppressWarnings("unchecked")
	public String buscarEmpleados(final String filtro) {
		final JSONObject raiz = new JSONObject();
		final ArrayList<GerenciaNominaEmpleadoDAO.Empleado> lista = GerenciaNominaEmpleadoDAO.buscarEmpleados(filtro);
		final JSONArray empleados = new JSONArray();
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaNominaEmpleadoDAO.Empleado e = lista.get(i);
			final JSONObject o = new JSONObject();
			o.put("id", Integer.valueOf(e.idEmpleado));
			o.put("nombre", e.nombreLargo);
			o.put("activo", Boolean.valueOf(e.activo));
			empleados.add(o);
		}
		raiz.put("empleados", empleados);
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Todo lo de una semana: lo cargado, como quedo repartido, las tiendas -para
	 * pintar nombres- y quien esta cargado pero sin ninguna marca para repartir.
	 */
	@SuppressWarnings("unchecked")
	public String consultarSemana(final String semana) {
		final JSONObject raiz = new JSONObject();
		if (!semanaValida(semana)) {
			raiz.put("respuesta", "INVALIDO");
			raiz.put("detalle", "La semana tiene que ser un domingo, en formato aaaa-mm-dd");
			return (raiz.toJSONString());
		}

		final ArrayList<GerenciaNominaEmpleadoDAO.NominaEmpleado> cargados = GerenciaNominaEmpleadoDAO
				.obtenerNominaSemana(semana);
		final JSONArray jCargados = new JSONArray();
		double totalCargado = 0;
		for (int i = 0; i < cargados.size(); i++) {
			final GerenciaNominaEmpleadoDAO.NominaEmpleado n = cargados.get(i);
			jCargados.add(aJsonNomina(n));
			totalCargado += n.total();
		}

		final ArrayList<GerenciaNominaEmpleadoDAO.RepartoTienda> reparto = GerenciaNominaEmpleadoDAO
				.obtenerReparto(semana);
		final JSONArray jReparto = new JSONArray();
		for (int i = 0; i < reparto.size(); i++) {
			final GerenciaNominaEmpleadoDAO.RepartoTienda r = reparto.get(i);
			final JSONObject o = new JSONObject();
			o.put("idempleado", Integer.valueOf(r.idEmpleado));
			o.put("empleado", r.nombreEmpleado);
			o.put("idtienda", Integer.valueOf(r.idTienda));
			o.put("tienda", r.nombreTienda);
			o.put("minutos", Long.valueOf(r.minutos));
			o.put("porcentaje", Double.valueOf(r.porcentaje * 100));
			o.put("basico", Double.valueOf(r.sueldoBasico));
			o.put("variable", Double.valueOf(r.sueldoVariable));
			o.put("seguridad", Double.valueOf(r.seguridadSocial));
			o.put("liquidacion", Double.valueOf(r.liquidacion));
			o.put("total", Double.valueOf(r.sueldoBasico + r.sueldoVariable + r.seguridadSocial + r.liquidacion));
			jReparto.add(o);
		}

		final ArrayList<GerenciaNominaEmpleadoDAO.NominaEmpleado> pendientes = GerenciaNominaEmpleadoDAO
				.pendientesSinReparto(semana);
		final JSONArray jPendientes = new JSONArray();
		for (int i = 0; i < pendientes.size(); i++) {
			jPendientes.add(aJsonNomina(pendientes.get(i)));
		}

		raiz.put("semana", semana);
		raiz.put("cargados", jCargados);
		raiz.put("totalcargado", Double.valueOf(totalCargado));
		raiz.put("reparto", jReparto);
		raiz.put("pendientes", jPendientes);
		raiz.put("tiendas", aJsonTiendas());
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public String guardarNomina(final GerenciaNominaEmpleadoDAO.NominaEmpleado nomina, final String usuario) {
		final JSONObject raiz = new JSONObject();
		final String error = GerenciaNominaEmpleadoDAO.guardarNomina(nomina, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "NOMINA_EMPLEADO",
				"Empleado " + nomina.idEmpleado + " semana " + nomina.semana + ", total "
						+ ((long) nomina.total()));
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Calcula y guarda el reparto de todos los cargados en una semana. Se puede
	 * llamar tantas veces como haga falta -por ejemplo, si se corrigio una
	 * marcacion de biometria despues de la primera corrida-: cada llamado
	 * reemplaza el calculo anterior de esa semana entera, no lo acumula.
	 */
	@SuppressWarnings("unchecked")
	public String calcularReparto(final String semana, final String usuario) {
		final JSONObject raiz = new JSONObject();
		if (!semanaValida(semana)) {
			raiz.put("respuesta", "INVALIDO");
			raiz.put("detalle", "La semana tiene que ser un domingo, en formato aaaa-mm-dd");
			return (raiz.toJSONString());
		}
		final int repartidos = GerenciaNominaEmpleadoDAO.calcularReparto(semana);
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "NOMINA_EMPLEADO_REPARTO",
				"Semana " + semana + ", " + repartidos + " empleados repartidos");
		raiz.put("respuesta", "OK");
		raiz.put("repartidos", Integer.valueOf(repartidos));
		return (raiz.toJSONString());
	}

	// =======================================================================

	@SuppressWarnings("unchecked")
	private JSONObject aJsonNomina(final GerenciaNominaEmpleadoDAO.NominaEmpleado n) {
		final JSONObject o = new JSONObject();
		o.put("idempleado", Integer.valueOf(n.idEmpleado));
		o.put("empleado", n.nombreEmpleado);
		o.put("semana", n.semana);
		o.put("basico", Double.valueOf(n.sueldoBasico));
		o.put("variable", Double.valueOf(n.sueldoVariable));
		o.put("seguridad", Double.valueOf(n.seguridadSocial));
		o.put("liquidacion", Double.valueOf(n.liquidacion));
		o.put("total", Double.valueOf(n.total()));
		o.put("origen", n.origen);
		o.put("usuario", n.usuario);
		return (o);
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJsonTiendas() {
		final JSONArray lista = new JSONArray();
		final ArrayList<GerenciaGastoDAO.Tienda> tiendas = GerenciaGastoDAO.obtenerTiendas();
		for (int i = 0; i < tiendas.size(); i++) {
			final JSONObject o = new JSONObject();
			o.put("idtienda", Integer.valueOf(tiendas.get(i).idTienda));
			o.put("nombre", tiendas.get(i).nombre);
			lista.add(o);
		}
		return (lista);
	}

	private boolean semanaValida(final String semana) {
		return (semana != null && semana.trim().matches("^\\d{4}-\\d{2}-\\d{2}$"));
	}

}

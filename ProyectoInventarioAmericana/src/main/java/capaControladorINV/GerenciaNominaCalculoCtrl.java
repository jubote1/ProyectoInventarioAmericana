package capaControladorINV;

import java.util.ArrayList;

import capaDAOINV.GerenciaNominaCalculoDAO;

/**
 * El JSON del calculo de nomina.
 *
 * Igual que el resto del menu Gerencia: el texto se arma a mano y todo lo que
 * viene de la base pasa por {@link #esc}. Las cifras viajan crudas; el formato
 * es de la pantalla.
 */
public class GerenciaNominaCalculoCtrl {

	public String consultarEstimados(final String semana) {
		final ArrayList<GerenciaNominaCalculoDAO.Estimado> lista =
				GerenciaNominaCalculoDAO.estimadosDe(semana);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"semana\":\"");
		sb.append(esc(semana)).append("\",\"estimados\":[");
		double total = 0;
		double totalCorregido = 0;
		int conAviso = 0;
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaNominaCalculoDAO.Estimado e = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"idempleado\":").append(e.idEmpleado);
			sb.append(",\"nombre\":\"").append(esc(e.nombre)).append('"');
			sb.append(",\"cargo\":\"").append(esc(e.cargo)).append('"');
			sb.append(",\"idtienda\":").append(e.idTiendaPrincipal);
			sb.append(",\"salario\":").append(num(e.salarioBase));
			sb.append(",\"h_ordinarias\":").append(num(e.horasOrdinarias));
			sb.append(",\"h_nocturnas\":").append(num(e.horasNocturnas));
			sb.append(",\"h_dominicales\":").append(num(e.horasDominicales));
			sb.append(",\"h_dom_noct\":").append(num(e.horasDomNoct));
			sb.append(",\"h_extra\":").append(num(e.horasExtra));
			sb.append(",\"h_totales\":").append(num(e.horasTotales));
			sb.append(",\"turnos\":").append(e.turnos);
			sb.append(",\"basico\":").append(num(e.sueldoBasico));
			sb.append(",\"variable\":").append(num(e.sueldoVariable));
			sb.append(",\"auxilio\":").append(num(e.auxilioTransporte));
			sb.append(",\"seg_social\":").append(num(e.seguridadSocial));
			sb.append(",\"liquidacion\":").append(num(e.liquidacion));
			sb.append(",\"costo\":").append(num(e.costoTotal));
			sb.append(",\"correccion\":").append(num(e.factorCorreccion));
			sb.append(",\"costo_corregido\":").append(num(e.costoCorregido));
			sb.append(",\"origen_cargado\":\"").append(esc(e.origenCargado)).append('"');
			sb.append(",\"aviso\":\"").append(esc(e.aviso)).append("\"}");
			total += e.costoTotal;
			totalCorregido += e.costoCorregido;
			if (e.aviso.length() > 0) {
				conAviso++;
			}
		}
		sb.append("],\"total\":").append(num(total));
		sb.append(",\"total_corregido\":").append(num(totalCorregido));
		sb.append(",\"con_aviso\":").append(conAviso);
		return (sb.append('}').toString());
	}

	public String consultarParametros() {
		final ArrayList<GerenciaNominaCalculoDAO.Parametro> lista =
				GerenciaNominaCalculoDAO.listarParametros();
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"parametros\":[");
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaNominaCalculoDAO.Parametro p = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"id\":").append(p.id);
			sb.append(",\"codigo\":\"").append(esc(p.codigo)).append('"');
			sb.append(",\"nombre\":\"").append(esc(p.nombre)).append('"');
			sb.append(",\"valor\":").append(num(p.valor));
			sb.append(",\"unidad\":\"").append(esc(p.unidad)).append('"');
			sb.append(",\"desde\":\"").append(esc(p.desde)).append('"');
			sb.append(",\"hasta\":\"").append(esc(p.hasta)).append('"');
			sb.append(",\"observacion\":\"").append(esc(p.observacion)).append("\"}");
		}
		return (sb.append("]}").toString());
	}

	public String consultarDesviaciones(final String semana) {
		final ArrayList<GerenciaNominaCalculoDAO.Desviacion> lista =
				GerenciaNominaCalculoDAO.desviaciones(semana);
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"desviaciones\":[");
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaNominaCalculoDAO.Desviacion d = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"idtienda\":").append(d.idTienda);
			sb.append(",\"tienda\":\"").append(esc(d.tienda)).append('"');
			sb.append(",\"muestras\":").append(d.muestras);
			sb.append(",\"promedio\":").append(num(d.promedio));
			sb.append(",\"dispersion\":").append(num(d.dispersion));
			sb.append(",\"se_aplica\":").append(d.seAplica);
			sb.append(",\"por_que_no\":\"").append(esc(d.porQueNo)).append("\"}");
		}
		return (sb.append("]}").toString());
	}

	public String calcular(final String semana, final String usuario) {
		final int n = GerenciaNominaCalculoDAO.calcular(semana, usuario);
		if (n < 0) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"No se pudo calcular. "
					+ "Revise que la semana sea un domingo y que haya parametros vigentes.\"}");
		}
		return ("{\"respuesta\":\"OK\",\"empleados\":" + n + "}");
	}

	public String pasarANomina(final String semana, final String usuario) {
		final int n = GerenciaNominaCalculoDAO.pasarANomina(semana, usuario);
		if (n < 0) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"No se pudo pasar el estimado a la nomina\"}");
		}
		return ("{\"respuesta\":\"OK\",\"filas\":" + n + "}");
	}

	public String cargar(final String semana, final String pegado, final String origen,
			final String usuario) {
		final GerenciaNominaCalculoDAO.ResultadoCarga r =
				GerenciaNominaCalculoDAO.cargarLote(semana, pegado, origen, usuario);
		//Apenas entra un real, se mide contra el estimado que ya existia. Hacerlo
		//aqui y no en un boton aparte es lo que hace que el historico de
		//desviacion se arme solo, sin que nadie tenga que acordarse.
		final int medidas = r.cargados > 0 ? GerenciaNominaCalculoDAO.registrarDesviacion(semana) : 0;

		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\"");
		sb.append(",\"cargados\":").append(r.cargados);
		sb.append(",\"no_encontrados\":").append(r.noEncontrados);
		sb.append(",\"ilegibles\":").append(r.ilegibles);
		sb.append(",\"desviaciones\":").append(Math.max(medidas, 0));
		sb.append(",\"problemas\":[");
		//Se muestran los primeros cincuenta: con mas que eso el problema no es
		//una linea suelta, es el formato del archivo.
		final int tope = Math.min(r.problemas.size(), 50);
		for (int i = 0; i < tope; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('"').append(esc(r.problemas.get(i))).append('"');
		}
		sb.append(']');
		sb.append(",\"problemas_totales\":").append(r.problemas.size());
		return (sb.append('}').toString());
	}

	public String guardarParametro(final String codigo, final double valor, final String desde,
			final String observacion, final String usuario) {
		return (GerenciaNominaCalculoDAO.guardarParametro(codigo, valor, desde, observacion, usuario));
	}

	private static String num(final double d) {
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			return ("0");
		}
		return (String.valueOf(Math.round(d * 100) / 100.0));
	}

	private static String esc(final String s) {
		if (s == null) {
			return ("");
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '"' || c == '\\') {
				sb.append('\\').append(c);
			} else if (c < 32) {
				sb.append(' ');
			} else {
				sb.append(c);
			}
		}
		return (sb.toString());
	}
}

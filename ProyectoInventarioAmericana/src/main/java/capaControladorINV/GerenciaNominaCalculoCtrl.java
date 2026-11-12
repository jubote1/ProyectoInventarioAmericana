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
		double totalReal = 0;
		int conReal = 0;
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
			sb.append(",\"h_festivas\":").append(num(e.horasFestivas));
			sb.append(",\"h_fest_noct\":").append(num(e.horasFestNoct));
			sb.append(",\"h_extra\":").append(num(e.horasExtra));
			sb.append(",\"h_extra_festiva\":").append(num(e.horasExtraFestiva));
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
			sb.append(",\"real_basico\":").append(num(e.realBasico));
			sb.append(",\"real_variable\":").append(num(e.realVariable));
			sb.append(",\"real_seg_social\":").append(num(e.realSegSocial));
			sb.append(",\"real_liquidacion\":").append(num(e.realLiquidacion));
			sb.append(",\"real_costo\":").append(num(e.realCostoTotal));
			sb.append(",\"recargo_nocturno\":").append(num(e.recargoNocturno));
			sb.append(",\"recargo_dominical\":").append(num(e.recargoDominical));
			sb.append(",\"recargo_festivo\":").append(num(e.recargoFestivo));
			sb.append(",\"recargo_extra\":").append(num(e.recargoExtra));
			sb.append(",\"recargo_extra_festiva\":").append(num(e.recargoExtraFestiva));
			sb.append(",\"novedad_tipo\":\"").append(esc(e.novedadTipo)).append('"');
			sb.append(",\"novedad_accion\":\"").append(esc(e.novedadAccion)).append('"');
			sb.append(",\"novedad_obs\":\"").append(esc(e.novedadObs)).append('"');
			sb.append(",\"novedad_dias\":").append(e.novedadDias);
			sb.append(",\"novedad_permanente\":").append(e.novedadPermanente);
			sb.append(",\"es_sena\":").append(e.esSena);
			sb.append(",\"es_sena_electiva\":").append(e.esSenaElectiva);
			sb.append(",\"es_inactivo\":").append(e.esInactivo);
			sb.append(",\"es_posible_despido\":").append(e.esPosibleDespido);
			sb.append(",\"aviso\":\"").append(esc(e.aviso)).append("\"}");
			total += e.costoTotal;
			totalCorregido += e.costoCorregido;
			if (e.realCostoTotal > 0 || (e.origenCargado != null && e.origenCargado.length() > 0 && !"ESTIMADO".equals(e.origenCargado))) {
				totalReal += (e.realCostoTotal > 0 ? e.realCostoTotal : e.costoCorregido);
				conReal++;
			}
			if (e.aviso.length() > 0) {
				conAviso++;
			}
		}
		sb.append("],\"total\":").append(num(total));
		sb.append(",\"total_corregido\":").append(num(totalCorregido));
		sb.append(",\"total_real\":").append(num(totalReal));
		sb.append(",\"con_real\":").append(conReal);
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

	public String consultarPeriodos() {
		final ArrayList<GerenciaNominaCalculoDAO.PeriodoNomina> lista =
				GerenciaNominaCalculoDAO.listarPeriodosRecientes();
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"periodos\":[");
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaNominaCalculoDAO.PeriodoNomina p = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"id\":").append(p.idPeriodo);
			sb.append(",\"desde\":\"").append(esc(p.fechaInferior)).append('"');
			sb.append(",\"hasta\":\"").append(esc(p.fechaSuperior)).append('"');
			sb.append(",\"semanas\":[");
			for (int j = 0; j < p.domingos.size(); j++) {
				if (j > 0) {
					sb.append(',');
				}
				sb.append('"').append(esc(p.domingos.get(j))).append('"');
			}
			sb.append("]}");
		}
		return (sb.append("]}").toString());
	}

	public String cargar(final String semana, final String pegado, final String origen,
			final String usuario) {
		return (cargar(semana, pegado, origen, usuario, 0));
	}

	public String cargar(final String semana, final String pegado, final String origen,
			final String usuario, final int idPeriodo) {
		final GerenciaNominaCalculoDAO.ResultadoCarga r =
				GerenciaNominaCalculoDAO.cargarLote(semana, pegado, origen, usuario, idPeriodo);

		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\"");
		sb.append(",\"cargados\":").append(r.cargados);
		sb.append(",\"no_encontrados\":").append(r.noEncontrados);
		sb.append(",\"ilegibles\":").append(r.ilegibles);
		sb.append(",\"problemas\":[");
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

	public String guardarNovedad(final String semana, final int idEmpleado,
			final String tipoNovedad, final String accion, final double dias,
			final boolean permanente, final String observacion, final String usuario) {
		final String err = GerenciaNominaCalculoDAO.guardarNovedad(semana, idEmpleado, tipoNovedad, accion, dias, permanente, observacion, usuario);
		if (err != null && !err.isEmpty()) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"" + esc(err) + "\"}");
		}
		return (consultarEstimados(semana));
	}

	public String consultarConciliacion(final String modo, final int idPeriodo, final int anio, final int mes) {
		final GerenciaNominaCalculoDAO.ResumenConciliacion r =
				GerenciaNominaCalculoDAO.consultarConciliacion(modo, idPeriodo, anio, mes);

		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\"");
		sb.append(",\"modo\":\"").append(esc(r.modo)).append('"');
		sb.append(",\"idperiodo\":").append(r.idPeriodo);
		sb.append(",\"periodo_texto\":\"").append(esc(r.periodoTexto)).append('"');
		sb.append(",\"fecha_desde\":\"").append(esc(r.fechaDesde)).append('"');
		sb.append(",\"fecha_hasta\":\"").append(esc(r.fechaHasta)).append('"');
		sb.append(",\"semanas\":[");
		for (int i = 0; i < r.domingos.size(); i++) {
			if (i > 0) sb.append(',');
			sb.append('"').append(esc(r.domingos.get(i))).append('"');
		}
		sb.append(']');
		sb.append(",\"total_real\":").append(num(r.totalRealPagado));
		sb.append(",\"total_biometria\":").append(num(r.totalBiometria));
		sb.append(",\"diff_pesos\":").append(num(r.diffPesos));
		sb.append(",\"diff_pct\":").append(num(r.diffPct));
		sb.append(",\"horas_real\":").append(num(r.horasTotalesReal));
		sb.append(",\"horas_biometria\":").append(num(r.horasTotalesBio));
		sb.append(",\"diff_horas\":").append(num(r.diffHoras));
		sb.append(",\"total_empleados\":").append(r.totalEmpleados);
		sb.append(",\"cant_cuadrados\":").append(r.cantCuadrados);
		sb.append(",\"cant_diferencias\":").append(r.cantDiferencias);
		sb.append(",\"cant_solo_real\":").append(r.cantSoloReal);
		sb.append(",\"cant_solo_bio\":").append(r.cantSoloBio);

		sb.append(",\"filas\":[");
		for (int i = 0; i < r.filas.size(); i++) {
			final GerenciaNominaCalculoDAO.FilaConciliacion f = r.filas.get(i);
			if (i > 0) sb.append(',');
			sb.append("{\"idempleado\":").append(f.idEmpleado);
			sb.append(",\"cedula\":\"").append(esc(f.cedula)).append('"');
			sb.append(",\"nombre\":\"").append(esc(f.nombre)).append('"');
			sb.append(",\"cargo\":\"").append(esc(f.cargo)).append('"');
			sb.append(",\"tienda\":\"").append(esc(f.tienda)).append('"');
			sb.append(",\"idtienda\":").append(f.idTienda);
			sb.append(",\"horas_bio\":").append(num(f.horasBio));
			sb.append(",\"basico_bio\":").append(num(f.basicoBio));
			sb.append(",\"variable_bio\":").append(num(f.variableBio));
			sb.append(",\"auxilio_bio\":").append(num(f.auxilioBio));
			sb.append(",\"seg_social_bio\":").append(num(f.segSocialBio));
			sb.append(",\"provisiones_bio\":").append(num(f.liquidacionBio));
			sb.append(",\"costo_bio\":").append(num(f.costoBio));
			sb.append(",\"tiene_biometria\":").append(f.tieneBiometria);
			sb.append(",\"idcargue\":").append(f.idCargue);
			sb.append(",\"horas_real\":").append(num(f.horasReal));
			sb.append(",\"total_real\":").append(num(f.totalPagadoReal));
			sb.append(",\"origen_real\":\"").append(esc(f.origenReal)).append('"');
			sb.append(",\"fecha_cargue\":\"").append(esc(f.fechaCargue)).append('"');
			sb.append(",\"observacion\":\"").append(esc(f.observacion)).append('"');
			sb.append(",\"estado\":\"").append(esc(f.estado)).append('"');
			sb.append(",\"tiene_real\":").append(f.tieneReal);
			sb.append(",\"diff_horas\":").append(num(f.diffHoras));
			sb.append(",\"diff_pesos\":").append(num(f.diffPesos));
			sb.append(",\"diff_pct\":").append(num(f.diffPct));
			sb.append(",\"semaforo\":\"").append(esc(f.semaforo)).append('"');
			sb.append(",\"diagnostico\":\"").append(esc(f.diagnostico)).append('"');
			sb.append(",\"es_sena\":").append(f.esSena).append('}');
		}
		sb.append(']');
		return (sb.append('}').toString());
	}

	public String guardarCargueConciliacion(final String modo, final int idPeriodo, final int anio, final int mes,
			final String pegado, final String tipoValor, final String origen, final String usuario) {
		final GerenciaNominaCalculoDAO.ResultadoCarga r =
				GerenciaNominaCalculoDAO.guardarCargueConciliacion(modo, idPeriodo, anio, mes, pegado, tipoValor, origen, usuario);

		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\"");
		sb.append(",\"cargados\":").append(r.cargados);
		sb.append(",\"no_encontrados\":").append(r.noEncontrados);
		sb.append(",\"ilegibles\":").append(r.ilegibles);
		sb.append(",\"total_pesos\":").append(num(r.totalPesos));
		sb.append(",\"total_horas\":").append(num(r.totalHoras));
		sb.append(",\"problemas\":[");
		final int tope = Math.min(r.problemas.size(), 50);
		for (int i = 0; i < tope; i++) {
			if (i > 0) sb.append(',');
			sb.append('"').append(esc(r.problemas.get(i))).append('"');
		}
		sb.append(']');
		sb.append(",\"problemas_totales\":").append(r.problemas.size());
		return (sb.append('}').toString());
	}

	public String guardarNotaConciliacion(final int idCargue, final String observacion, final String estado, final String usuario) {
		return (GerenciaNominaCalculoDAO.guardarNotaConciliacion(idCargue, observacion, estado, usuario));
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

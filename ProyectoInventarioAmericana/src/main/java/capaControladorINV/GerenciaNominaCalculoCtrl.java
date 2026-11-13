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

	/** Las personas que se pueden escoger en la pantalla de verificacion. */
	public String listarParaVerificar() {
		final ArrayList<String[]> lista = GerenciaNominaCalculoDAO.listarParaVerificar();
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\",\"empleados\":[");
		for (int i = 0; i < lista.size(); i++) {
			final String[] f = lista.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"id\":").append(f[0]);
			sb.append(",\"nombre\":\"").append(esc(f[1])).append('"');
			sb.append(",\"cargo\":\"").append(esc(f[2])).append('"');
			sb.append(",\"salario\":").append(f[3]);
			sb.append(",\"medio_tiempo\":\"").append(f[4]).append('"');
			sb.append(",\"activo\":\"").append(f[5]).append("\"}");
		}
		return (sb.append("]}").toString());
	}

	private static void arreglo(final StringBuilder sb, final double[] v) {
		sb.append('[');
		for (int i = 0; i < v.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(String.valueOf(Math.round(v[i] * 1000) / 1000.0));
		}
		sb.append(']');
	}

	/** El detalle de la liquidacion de una persona, semana a semana. */
	public String verificarEmpleado(final int idEmpleado, final ArrayList<String> semanas) {
		final GerenciaNominaCalculoDAO.Verificacion v = GerenciaNominaCalculoDAO.verificarEmpleado(idEmpleado, semanas);
		if (v.error.length() > 0) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"" + esc(v.error) + "\"}");
		}
		final StringBuilder sb = new StringBuilder("{\"respuesta\":\"OK\"");
		final GerenciaNominaCalculoDAO.DatosPersona p = v.persona;
		sb.append(",\"persona\":{\"id\":").append(p.id);
		sb.append(",\"cedula\":\"").append(esc(p.cedula)).append('"');
		sb.append(",\"nombre\":\"").append(esc(p.nombre)).append('"');
		sb.append(",\"cargo\":\"").append(esc(p.cargo)).append('"');
		sb.append(",\"area\":\"").append(esc(p.area)).append('"');
		sb.append(",\"salario\":").append(num(p.salario));
		sb.append(",\"medio_tiempo\":").append(p.medioTiempo);
		sb.append(",\"activo\":").append(p.activo);
		sb.append(",\"sena\":").append(p.esSena);
		sb.append(",\"horas_enteras\":").append(p.horasEnteras).append('}');
		sb.append(",\"parametros\":[");
		for (int i = 0; i < v.parametros.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"codigo\":\"").append(esc(v.parametros.get(i)[0])).append("\",\"valor\":")
					.append(num(Double.parseDouble(v.parametros.get(i)[1]))).append('}');
		}
		sb.append("],\"semanas\":[");
		for (int i = 0; i < v.semanas.size(); i++) {
			final GerenciaNominaCalculoDAO.SemanaDet s = v.semanas.get(i);
			if (i > 0) {
				sb.append(',');
			}
			sb.append("{\"semana\":\"").append(esc(s.semana)).append('"');
			sb.append(",\"desde\":\"").append(esc(s.desde)).append('"');
			sb.append(",\"hasta\":\"").append(esc(s.hasta)).append('"');
			sb.append(",\"error\":\"").append(esc(s.error)).append('"');
			if (s.error.length() > 0 || s.calculo == null) {
				sb.append('}');
				continue;
			}
			final GerenciaNominaCalculoDAO.Estimado e = s.calculo;
			sb.append(",\"horas_enteras\":").append(s.horasEnteras);
			sb.append(",\"valor_hora\":").append(num(s.valorHora));
			sb.append(",\"novedad\":\"").append(esc(s.novedad)).append('"');
			sb.append(",\"costo_sin_novedad\":").append(num(s.costoSinNovedad));
			sb.append(",\"calculo\":{\"salario\":").append(num(e.salarioBase));
			sb.append(",\"h_ord\":").append(num(e.horasOrdinarias));
			sb.append(",\"h_noc\":").append(num(e.horasNocturnas));
			sb.append(",\"h_dom\":").append(num(e.horasDominicales));
			sb.append(",\"h_dom_noct\":").append(num(e.horasDomNoct));
			sb.append(",\"h_fest\":").append(num(e.horasFestivas));
			sb.append(",\"h_fest_noct\":").append(num(e.horasFestNoct));
			sb.append(",\"h_extra\":").append(num(e.horasExtra));
			sb.append(",\"h_extra_fest\":").append(num(e.horasExtraFestiva));
			sb.append(",\"h_tot\":").append(num(e.horasTotales));
			sb.append(",\"turnos\":").append(e.turnos);
			sb.append(",\"basico\":").append(num(e.sueldoBasico));
			sb.append(",\"rec_noc\":").append(num(e.recargoNocturno));
			sb.append(",\"rec_dom\":").append(num(e.recargoDominical));
			sb.append(",\"rec_fest\":").append(num(e.recargoFestivo));
			sb.append(",\"rec_extra\":").append(num(e.recargoExtra));
			sb.append(",\"rec_extra_fest\":").append(num(e.recargoExtraFestiva));
			sb.append(",\"variable\":").append(num(e.sueldoVariable));
			sb.append(",\"auxilio\":").append(num(e.auxilioTransporte));
			sb.append(",\"seg_social\":").append(num(e.seguridadSocial));
			sb.append(",\"liquidacion\":").append(num(e.liquidacion));
			sb.append(",\"costo\":").append(num(e.costoTotal));
			sb.append(",\"factor_correccion\":").append(num(e.factorCorreccion));
			sb.append(",\"costo_corregido\":").append(num(e.costoCorregido));
			sb.append(",\"aviso\":\"").append(esc(e.aviso)).append("\"}");
			sb.append(",\"guardado\":{\"hay\":").append(s.hayGuardado);
			sb.append(",\"costo\":").append(num(s.guardadoCosto));
			sb.append(",\"horas\":").append(num(s.guardadoHoras));
			sb.append(",\"fecha\":\"").append(esc(s.guardadoFecha)).append("\"}");
			sb.append(",\"tablero\":{\"hay\":").append(s.hayTablero);
			sb.append(",\"origen\":\"").append(esc(s.tableroOrigen)).append('"');
			sb.append(",\"total\":").append(num(s.tableroTotal)).append('}');
			sb.append(",\"real\":{\"hay\":").append(s.hayReal);
			sb.append(",\"origen\":\"").append(esc(s.realOrigen)).append('"');
			sb.append(",\"total\":").append(num(s.realTotal)).append('}');
			sb.append(",\"turnos_det\":[");
			for (int k = 0; k < s.turnos.size(); k++) {
				final GerenciaNominaCalculoDAO.TurnoDet t = s.turnos.get(k);
				if (k > 0) {
					sb.append(',');
				}
				sb.append("{\"dia\":\"").append(esc(t.dia)).append('"');
				sb.append(",\"dia_semana\":\"").append(esc(t.diaSemana)).append('"');
				sb.append(",\"entra\":\"").append(esc(t.entra)).append('"');
				sb.append(",\"sale\":\"").append(esc(t.sale)).append('"');
				sb.append(",\"tienda\":\"").append(esc(t.tienda)).append('"');
				sb.append(",\"estimado\":").append(t.estimado);
				sb.append(",\"horas\":").append(num(t.horas));
				sb.append(",\"cat\":");
				arreglo(sb, t.cat);
				sb.append('}');
			}
			sb.append("],\"dias_det\":[");
			for (int k = 0; k < s.dias.size(); k++) {
				final GerenciaNominaCalculoDAO.DiaDet d = s.dias.get(k);
				if (k > 0) {
					sb.append(',');
				}
				sb.append("{\"fecha\":\"").append(esc(d.fecha)).append('"');
				sb.append(",\"dia_semana\":\"").append(esc(d.diaSemana)).append('"');
				sb.append(",\"festivo\":").append(d.festivo);
				sb.append(",\"domingo\":").append(d.domingo);
				sb.append(",\"minutos\":");
				arreglo(sb, d.minutos);
				sb.append(",\"horas_exactas\":").append(num(d.horasExactas));
				sb.append(",\"redondeado\":").append(d.redondeado);
				sb.append(",\"horas_pagadas\":").append(num(d.horasPagadas));
				sb.append(",\"pagadas\":");
				arreglo(sb, d.pagadas);
				sb.append('}');
			}
			sb.append("],\"explicacion\":[");
			for (int k = 0; k < s.explicacion.size(); k++) {
				final GerenciaNominaCalculoDAO.FilaExp f = s.explicacion.get(k);
				if (k > 0) {
					sb.append(',');
				}
				sb.append("{\"tipo\":\"").append(esc(f.tipo)).append('"');
				sb.append(",\"concepto\":\"").append(esc(f.concepto)).append('"');
				sb.append(",\"formula\":\"").append(esc(f.formula)).append('"');
				sb.append(",\"con_valor\":").append(f.conValor);
				sb.append(",\"valor\":").append(num(f.valor)).append('}');
			}
			sb.append("]}");
		}
		return (sb.append("]}").toString());
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

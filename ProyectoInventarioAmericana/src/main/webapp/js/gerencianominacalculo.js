/**
 * Calculo de nomina: el estimado, la carga del real y la calibracion.
 *
 * LO ESTIMADO Y LO REAL NO SE PUEDEN VER IGUAL
 *
 * Toda fila dice de donde viene su cifra. Un estimado que se ve como un hecho
 * es mas peligroso que no tener el numero: alguien cierra un turno creyendo que
 * el margen es ese. Por eso el estimado va con su etiqueta y la fila que ya
 * tiene real se pinta aparte.
 *
 * LAS HORAS VAN AL LADO DEL COSTO
 *
 * No por completitud: son la unica forma de que alguien note que una cifra esta
 * mal. Un costo de dos millones no dice nada; dos millones con cuatro horas
 * marcadas se ve mal de inmediato.
 */

var NC_SEMANAS_ATRAS = 16;

var datosEstimado = null;
var parametros = null;
var codigoEnCambio = '';
var filtroActivo = 'todos';
var textoFiltro = '';
var empleadoEnModal = null;
var modoVista = 'real'; // 'real', 'estimado', 'comparativo'
var listaPeriodos = [];

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	llenarSemanas();
	$('#semana').on('change', consultar);
	$('#btnCalcular').on('click', calcular);
	$('#btnPasar').on('click', pasar);
	$('#btnCargar').on('click', cargar);
	$('#btnGuardarParametro').on('click', guardarParametro);
	$('#btnCancelarCambio').on('click', function () { $('#panelCambio').hide(); });
	$('#tabEstimado').on('click', function (e) { e.preventDefault(); verPestana('estimado'); });
	$('#tabCargar').on('click', function (e) { e.preventDefault(); verPestana('cargar'); });
	$('#tabCalibracion').on('click', function (e) { e.preventDefault(); verPestana('calibracion'); });
	$('#tabParametros').on('click', function (e) { e.preventDefault(); verPestana('parametros'); });

	// Modos de vista: Real vs Estimado vs Comparativo
	$('.btn-modo').on('click', function () {
		$('.btn-modo').removeClass('active btn-primary').addClass('btn-default');
		$(this).addClass('active btn-primary').removeClass('btn-default');
		modoVista = $(this).data('modo');
		actualizarBadgeModo();
		pintarEstimado();
	});

	// Destino del cargue: Semana vs Quincena
	$('#selDestinoCargue').on('change', function () {
		if ($(this).val() === 'PERIODO') {
			$('#divSelPeriodo').show();
		} else {
			$('#divSelPeriodo').hide();
		}
	});

	// Filtros y buscador
	$('#filtroEmpleado').on('input', function () {
		textoFiltro = $.trim($(this).val()).toLowerCase();
		pintarEstimado();
	});
	$('.btn-filtro').on('click', function () {
		$('.btn-filtro').removeClass('active btn-primary').addClass('btn-default');
		$(this).addClass('active btn-primary').removeClass('btn-default');
		filtroActivo = $(this).data('filtro');
		pintarEstimado();
	});

	// Modal Novedad
	$('.opcion-novedad-card').on('click', function () {
		$(this).find('input[type="radio"]').prop('checked', true);
		$('.opcion-novedad-card').removeClass('activa');
		$(this).addClass('activa');
	});
	$('#btnGuardarNov').on('click', guardarNovedad);
	$('#btnRestablecerNov').on('click', restablecerNovedad);

	consultar();
	cargarParametros();
	cargarPeriodosNomina();
}

function actualizarBadgeModo() {
	if (modoVista === 'real') {
		$('#badgeResumenVista').html('<i class="fas fa-check-double text-success"></i> Modo Real (Predeterminado: muestra SIIGO donde haya real y Estimado donde falte)');
	} else if (modoVista === 'estimado') {
		$('#badgeResumenVista').html('<i class="fas fa-calculator text-info"></i> Modo Estimado (Cálculo biométrico puro de la semana)');
	} else if (modoVista === 'comparativo') {
		$('#badgeResumenVista').html('<i class="fas fa-balance-scale text-warning"></i> Modo Comparativo (Auditoría: Estimado Biometría vs Real Pagado SIIGO)');
	}
}

function cargarPeriodosNomina() {
	$.getJSON('GerenciaNominaCalculo', { que: 'periodos' }, function (data) {
		if (!data || !data.periodos) { return; }
		listaPeriodos = data.periodos;
		var html = '';
		for (var i = 0; i < listaPeriodos.length; i++) {
			var p = listaPeriodos[i];
			var cantSemanas = p.semanas ? p.semanas.length : 2;
			html += '<option value="' + p.id + '">'
				+ 'Quincena #' + p.id + ' (' + p.desde + ' al ' + p.hasta + ') — '
				+ cantSemanas + ' semana' + (cantSemanas !== 1 ? 's' : '') + '</option>';
		}
		$('#selPeriodoNomina').html(html);
	});
}

/**
 * Las semanas son los domingos hacia atras.
 *
 * La semana en curso NO se ofrece: todavia no ha terminado, las marcaciones
 * estan a medias, y el estimado saldria bajo sin que eso signifique nada.
 */
function llenarSemanas() {
	var hoy = new Date();
	var d = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
	//Nuestras semanas van de lunes a domingo, asi que el domingo del calendario
	//de esta semana es el CIERRE de la semana pasada: esa ya esta completa.
	d.setDate(d.getDate() - d.getDay());
	//Salvo que hoy sea domingo: entonces ese domingo es el de hoy, la semana
	//todavia no cierra, y hay que irse a la anterior.
	if (hoy.getDay() === 0) {
		d.setDate(d.getDate() - 7);
	}
	var html = '';
	for (var i = 0; i < NC_SEMANAS_ATRAS; i++) {
		var txt = iso(d);
		html += '<option value="' + txt + '">' + txt + '</option>';
		d.setDate(d.getDate() - 7);
	}
	$('#semana').html(html);
}

function semana() {
	return ($('#semana').val());
}

function verPestana(cual) {
	$('.ger-pestanas a').removeClass('activa');
	$('#panelEstimado, #panelCargar, #panelCalibracion, #panelParametros').hide();
	if (cual === 'estimado') { $('#tabEstimado').addClass('activa'); $('#panelEstimado').show(); }
	if (cual === 'cargar') { $('#tabCargar').addClass('activa'); $('#panelCargar').show(); }
	if (cual === 'calibracion') {
		$('#tabCalibracion').addClass('activa'); $('#panelCalibracion').show(); cargarCalibracion();
	}
	if (cual === 'parametros') { $('#tabParametros').addClass('activa'); $('#panelParametros').show(); }
}

// ===========================================================================
// El estimado
// ===========================================================================

function consultar() {
	$.getJSON('GerenciaNominaCalculo', { que: 'estimados', semana: semana() }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		datosEstimado = data;
		pintarEstimado();
	});
}

function calcular() {
	$('#btnCalcular').prop('disabled', true).text('Calculando...');
	$.post('GerenciaNominaCalculo', { que: 'calcular', semana: semana() }, function (data) {
		$('#btnCalcular').prop('disabled', false)
			.html('<i class="fas fa-calculator"></i> Calcular la semana');
		if (!Gerencia.respondio(data)) { return; }
		Gerencia.avisar('Se estimaron ' + data.empleados + ' empleados.', true);
		consultar();
	}, 'json').fail(function () {
		$('#btnCalcular').prop('disabled', false)
			.html('<i class="fas fa-calculator"></i> Calcular la semana');
		Gerencia.avisar('No se pudo calcular.');
	});
}

function pasar() {
	$.post('GerenciaNominaCalculo', { que: 'pasar', semana: semana() }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		Gerencia.avisar('Quedaron ' + data.filas + ' personas en la nomina de la semana, como '
			+ 'ESTIMADO. Las que ya tenian un real NO se tocaron.', true);
		consultar();
	}, 'json');
}

function pintarEstimado() {
	var d = datosEstimado;
	var rawLista = d.estimados || [];
	var vistos = {};
	var lista = [];
	for (var v = 0; v < rawLista.length; v++) {
		var idEmp = rawLista[v].idempleado;
		if (vistos[idEmp]) { continue; }
		vistos[idEmp] = true;
		lista.push(rawLista[v]);
	}

	// Contadores para filtros
	var cntTodos = lista.length;
	var cntAvisos = 0;
	var cntJustificados = 0;
	for (var c = 0; c < lista.length; c++) {
		if (lista[c].novedad_tipo) {
			cntJustificados++;
		} else if (lista[c].aviso) {
			cntAvisos++;
		}
	}
	$('#cntTodos').text(cntTodos);
	$('#cntAvisos').text(cntAvisos);
	$('#cntJustificados').text(cntJustificados);

	// Resumen de cajas según modo
	var totalEstimado = d.total || 0;
	var totalCorregido = d.total_corregido || totalEstimado;
	var totalReal = d.total_real || 0;
	var conReal = d.con_real || 0;

	var html = '';
	html += caja('Personas', lista.length, false);
	if (modoVista === 'comparativo') {
		html += caja('Estimado Biometría', '$' + milesNC(totalCorregido), false);
		html += caja('Real Pagado SIIGO', '$' + milesNC(totalReal), conReal > 0);
		var difTotal = totalReal - totalCorregido;
		var pctTotal = totalCorregido > 0 ? (difTotal / totalCorregido * 100) : 0;
		var signo = difTotal > 0 ? '+' : '';
		html += caja('Desviación Neta', (conReal > 0 ? (signo + '$' + milesNC(difTotal) + ' (' + unaDecimal(pctTotal) + '%)') : 'Sin real cargado'), difTotal > 0);
	} else if (modoVista === 'real') {
		var totalVigente = (conReal > 0 ? totalReal : totalCorregido);
		html += caja('Nómina Total Vigente', '$' + milesNC(totalVigente), false);
		html += caja('Con Real SIIGO', conReal + ' / ' + lista.length, conReal === lista.length);
		html += caja('Con aviso / novedad', cntAvisos, cntAvisos > 0);
	} else {
		html += caja('Costo estimado', '$' + milesNC(totalEstimado), false);
		if (Math.round(totalCorregido) !== Math.round(totalEstimado)) {
			html += caja('Con correccion', '$' + milesNC(totalCorregido), false);
		}
		html += caja('Con aviso', cntAvisos, cntAvisos > 0);
	}
	$('#cajasEstimado').html(html);

	// Encabezado de la tabla según modo
	var thHtml = '';
	if (modoVista === 'comparativo') {
		thHtml = '<tr>'
			+ '<th>Empleado</th><th>Cargo</th><th class="ger-num">Salario</th>'
			+ '<th class="ger-num" title="Costo estimado por biometría">Estimado Biometr&iacute;a</th>'
			+ '<th class="ger-num" title="Costo real liquidado en SIIGO">Real Pagado (SIIGO)</th>'
			+ '<th class="ger-num" title="Diferencia en pesos">Diferencia ($)</th>'
			+ '<th class="ger-num" title="Porcentaje de variación">% Desv.</th>'
			+ '<th>Origen</th><th>Novedad / Estado</th></tr>';
	} else {
		var esReal = (modoVista === 'real');
		thHtml = '<tr>'
			+ '<th>Empleado</th><th>Cargo</th><th class="ger-num">Salario</th>'
			+ '<th class="ger-num">Horas</th>'
			+ '<th class="ger-num" title="Horas y valor en la franja de 19:00 a 06:00 (Ley 2466)">Rec. Nocturno<span class="th-sub">horas &middot; valor $</span></th>'
			+ '<th class="ger-num" title="Horas y valor en domingos y festivos">Dom / Fes<span class="th-sub">horas &middot; valor $</span></th>'
			+ '<th class="ger-num" title="Horas extra semanales">Extra<span class="th-sub">horas &middot; valor $</span></th>'
			+ '<th class="ger-num">' + (esReal ? 'B&aacute;sico (Fijo)' : 'B&aacute;sico') + '</th>'
			+ '<th class="ger-num" title="Variable de SIIGO o recargos de biometría">' + (esReal ? 'Variable Pagado' : 'Total Variable') + '</th>'
			+ '<th class="ger-num">Seg. Social</th>'
			+ '<th class="ger-num" title="Cesantías, intereses, prima y vacaciones">Provisiones</th>'
			+ '<th class="ger-num">' + (esReal ? 'Costo Total Real' : 'Costo Total') + '</th>'
			+ '<th>Novedad / Estado</th></tr>';
	}
	$('#tablaEstimado thead').html(thHtml);

	var colsTotal = (modoVista === 'comparativo' ? 9 : 13);
	if (lista.length === 0) {
		$('#tablaEstimado tbody').html('<tr><td colspan="' + colsTotal + '" class="ger-vacio">'
			+ 'Esta semana no esta calculada. Use el boton de arriba.</td></tr>');
		return;
	}

	var cuerpo = '';
	var visibles = 0;
	for (var i = 0; i < lista.length; i++) {
		var e = lista[i];

		// Filtrado
		if (filtroActivo === 'aviso' && (!e.aviso || e.novedad_tipo)) { continue; }
		if (filtroActivo === 'justificados' && !e.novedad_tipo) { continue; }
		if (textoFiltro && (e.nombre.toLowerCase().indexOf(textoFiltro) === -1
				&& e.cargo.toLowerCase().indexOf(textoFiltro) === -1)) { continue; }

		visibles++;

		// Repetir encabezado cada 25 filas visibles
		if (visibles > 1 && (visibles - 1) % 25 === 0) {
			if (modoVista === 'comparativo') {
				cuerpo += '<tr class="sub-header-repite">'
					+ '<td>Empleado</td><td>Cargo</td><td class="ger-num">Salario</td>'
					+ '<td class="ger-num">Estimado Biometr&iacute;a</td>'
					+ '<td class="ger-num">Real Pagado (SIIGO)</td>'
					+ '<td class="ger-num">Diferencia ($)</td>'
					+ '<td class="ger-num">% Desv.</td>'
					+ '<td>Origen</td><td>Novedad / Estado</td></tr>';
			} else {
				cuerpo += '<tr class="sub-header-repite">'
					+ '<td>Empleado</td><td>Cargo</td><td class="ger-num">Salario</td>'
					+ '<td class="ger-num">Horas</td><td class="ger-num">Rec. Nocturno</td>'
					+ '<td class="ger-num">Dom / Fes</td><td class="ger-num">Extra</td>'
					+ '<td class="ger-num">B&aacute;sico</td><td class="ger-num">Total Variable</td>'
					+ '<td class="ger-num">Seg. Social</td><td class="ger-num">Provisiones</td>'
					+ '<td class="ger-num">Costo Total</td><td>Novedad / Estado</td></tr>';
			}
		}

		var yaReal = (e.real_costo && e.real_costo > 0) || (e.origen_cargado && e.origen_cargado !== '' && e.origen_cargado !== 'ESTIMADO');

		if (modoVista === 'comparativo') {
			var estC = e.costo_corregido || e.costo || 0;
			var realC = yaReal ? (e.real_costo || estC) : 0;
			var dif = realC - estC;
			var pct = estC > 0 ? (dif / estC * 100) : 0;
			var colDif = dif > 0 ? '#C21C1F' : (dif < 0 ? '#166534' : '#64748B');

			cuerpo += '<tr>';
			cuerpo += '<td><strong>' + escNC(e.nombre) + '</strong></td>';
			cuerpo += '<td style="font-size:11.5px;color:#6C7482;">' + escNC(e.cargo) + '</td>';
			cuerpo += '<td class="ger-num">' + (e.salario > 0 ? '$' + milesNC(e.salario) : '<span class="ger-vacio">-</span>') + '</td>';
			cuerpo += '<td class="ger-num">$' + milesNC(estC) + '</td>';
			cuerpo += '<td class="ger-num">' + (yaReal ? '<strong>$' + milesNC(realC) + '</strong>' : '<span class="badge" style="background:#F1F5F9; color:#94A3B8;">Pendiente</span>') + '</td>';
			cuerpo += '<td class="ger-num" style="font-weight:bold; color:' + colDif + ';">' + (yaReal ? (dif > 0 ? '+' : '') + '$' + milesNC(dif) : '&mdash;') + '</td>';
			cuerpo += '<td class="ger-num" style="color:' + colDif + ';">' + (yaReal ? (pct > 0 ? '+' : '') + unaDecimal(pct) + '%' : '&mdash;') + '</td>';
			cuerpo += '<td>' + (yaReal ? '<span class="ger-origen ger-real">' + escNC(e.origen_cargado || 'SIIGO') + '</span>' : '<span class="badge" style="background:#E2E8F0; color:#64748B;">Estimado</span>') + '</td>';
		} else {
			// Modo Real (predeterminado) o Modo Estimado
			var esReal = (modoVista === 'real');
			var bas = (esReal && yaReal && e.real_basico > 0) ? e.real_basico : e.basico;
			var varM = (esReal && yaReal) ? e.real_variable : e.variable;
			var seg = (esReal && yaReal && e.real_seg_social > 0) ? e.real_seg_social : e.seg_social;
			var liq = (esReal && yaReal && e.real_liquidacion > 0) ? e.real_liquidacion : e.liquidacion;
			var tot = (esReal && yaReal && e.real_costo > 0) ? e.real_costo : e.costo_corregido;

			cuerpo += '<tr>';
			cuerpo += '<td><strong>' + escNC(e.nombre) + '</strong></td>';
			cuerpo += '<td style="font-size:11.5px;color:#6C7482;">' + escNC(e.cargo) + '</td>';
			cuerpo += '<td class="ger-num">' + (e.salario > 0 ? '$' + milesNC(e.salario)
				: '<span class="ger-vacio">sin salario</span>') + '</td>';
			cuerpo += '<td class="ger-num"><strong>' + formatearHorasNC(e.h_totales) + '</strong>'
				+ '<div style="font-size:10.5px;color:#6C7482;">' + e.turnos + ' turnos</div></td>';

			// Recargo nocturno (horas + monto)
			var hNoc = e.h_nocturnas + e.h_dom_noct;
			var vNoc = e.recargo_nocturno || 0;
			cuerpo += '<td class="ger-num">'
				+ (hNoc > 0 ? '<strong>' + formatearHorasNC(hNoc) + ' h</strong>'
					+ (vNoc > 0 ? '<div style="font-size:11px;color:#102F6F;font-weight:600;">$' + milesNC(vNoc) + '</div>' : '')
					: '<span style="color:#94A3B8;">&mdash;</span>') + '</td>';

			// Dominical / Festivo (horas + monto)
			var hDom = e.h_dominicales + e.h_dom_noct;
			var vDom = e.recargo_dominical || 0;
			cuerpo += '<td class="ger-num">'
				+ (hDom > 0 ? '<strong>' + formatearHorasNC(hDom) + ' h</strong>'
					+ (vDom > 0 ? '<div style="font-size:11px;color:#102F6F;font-weight:600;">$' + milesNC(vDom) + '</div>' : '')
					: '<span style="color:#94A3B8;">&mdash;</span>') + '</td>';

			// Horas extra (horas + monto)
			var hExt = e.h_extra || 0;
			var vExt = e.recargo_extra || 0;
			cuerpo += '<td class="ger-num">'
				+ (hExt > 0 ? '<strong>' + formatearHorasNC(hExt) + ' h</strong>'
					+ (vExt > 0 ? '<div style="font-size:11px;color:#102F6F;font-weight:600;">$' + milesNC(vExt) + '</div>' : '')
					: '<span style="color:#94A3B8;">&mdash;</span>') + '</td>';

			cuerpo += '<td class="ger-num">$' + milesNC(bas) + '</td>';
			cuerpo += '<td class="ger-num">' + (varM > 0 ? '<strong>$' + milesNC(varM) + '</strong>' : '$0')
				+ (!esReal && e.auxilio > 0 ? '<div style="font-size:11px;color:#6C7482;">+$' + milesNC(e.auxilio) + ' aux</div>' : '') + '</td>';
			cuerpo += '<td class="ger-num">$' + milesNC(seg) + '</td>';
			cuerpo += '<td class="ger-num">$' + milesNC(liq) + '</td>';
			cuerpo += '<td class="ger-num" style="font-weight:bold; color:#102F6F;">$' + milesNC(tot)
				+ (!esReal && e.correccion !== 0 ? '<div style="font-size:11px;color:#8A6400;">'
					+ (e.correccion > 0 ? '+' : '') + unaDecimal(e.correccion) + '% corregido</div>' : '')
				+ (esReal && yaReal ? '<div style="font-size:11px;color:#166534;"><i class="fas fa-check"></i> ' + escNC(e.origen_cargado || 'SIIGO') + '</div>' : '')
				+ '</td>';
		}

		// Novedad / Estado (común a todos los modos)
		cuerpo += '<td style="white-space:nowrap;">';
		if (e.novedad_tipo) {
			var badgeClase = 'badge-directivo';
			var icono = 'fa-check-circle';
			var label = 'Justificado';
			if (e.novedad_tipo === 'DIRECTIVO_INCLUIR') { label = 'Directivo'; icono = 'fa-user-tie'; badgeClase = 'badge-directivo'; }
			else if (e.novedad_tipo === 'SENA_LECTIVA') { label = 'SENA (Lectiva)'; icono = 'fa-graduation-cap'; badgeClase = 'badge-directivo'; }
			else if (e.novedad_tipo === 'SENA_PRACTICA') { label = 'SENA (Práctica)'; icono = 'fa-graduation-cap'; badgeClase = 'badge-directivo'; }
			else if (e.novedad_tipo === 'VACACIONES_EXCLUIR') { label = 'Vacaciones'; icono = 'fa-umbrella-beach'; badgeClase = 'badge-vacaciones'; }
			else if (e.novedad_tipo === 'VACACIONES_PARCIAL') { 
				var dFmt = (e.novedad_dias != null ? (e.novedad_dias % 1 === 0 ? e.novedad_dias : Number(e.novedad_dias).toFixed(2)) : 3);
				label = 'Vac. (' + dFmt + 'd)'; icono = 'fa-calendar-day'; badgeClase = 'badge-vacaciones'; 
			}
			else if (e.novedad_tipo === 'INCAPACIDAD_INCLUIR') { label = 'Incapacidad'; icono = 'fa-notes-medical'; badgeClase = 'badge-incapacidad'; }
			else if (e.novedad_tipo === 'PERMISO_INCLUIR') { label = 'Permiso'; icono = 'fa-check'; badgeClase = 'badge-permiso'; }
			else if (e.novedad_tipo === 'NO_LABORO_EXCLUIR') { label = 'No laboró'; icono = 'fa-user-slash'; badgeClase = 'badge-excluido'; }
			cuerpo += '<span class="badge-nov ' + badgeClase + '" title="' + escNC(e.aviso) + '">'
				+ '<i class="fas ' + icono + '"></i> ' + label + '</span> '
				+ '<button class="btn btn-default btn-xs" style="padding:1px 5px;" onclick="abrirModalNovedad(' + e.idempleado + ')" title="Editar novedad">'
				+ '<i class="fas fa-pencil-alt"></i></button>';
		} else if (e.es_sena_electiva) {
			cuerpo += '<span class="badge-nov badge-directivo" title="' + escNC(e.aviso) + '">'
				+ '<i class="fas fa-graduation-cap"></i> SENA (Lectiva)</span> '
				+ '<button class="btn btn-default btn-xs" style="padding:1px 5px; opacity:0.7;" onclick="abrirModalNovedad(' + e.idempleado + ')" title="Gestionar condición">'
				+ '<i class="fas fa-pencil-alt"></i></button>';
		} else if (e.es_sena) {
			cuerpo += '<span class="badge-nov badge-directivo" title="' + escNC(e.aviso) + '">'
				+ '<i class="fas fa-graduation-cap"></i> SENA (Práctica)</span> '
				+ '<button class="btn btn-default btn-xs" style="padding:1px 5px; opacity:0.7;" onclick="abrirModalNovedad(' + e.idempleado + ')" title="Gestionar condición">'
				+ '<i class="fas fa-pencil-alt"></i></button>';
		} else if (e.aviso) {
			cuerpo += '<button class="btn btn-xs btn-warning btn-resolver-nov" onclick="abrirModalNovedad(' + e.idempleado + ')">'
				+ '<i class="fas fa-exclamation-triangle"></i> Resolver</button>';
		} else if (yaReal) {
			cuerpo += '<span class="ger-origen ger-real">' + escNC(e.origen_cargado || 'SIIGO') + '</span>';
		} else {
			cuerpo += '<span class="badge-nov" style="background:#F1F5F9; color:#64748B;"><i class="fas fa-check"></i> Normal</span> '
				+ '<button class="btn btn-default btn-xs" style="padding:1px 5px; opacity:0.6;" onclick="abrirModalNovedad(' + e.idempleado + ')" title="Marcar novedad">'
				+ '<i class="fas fa-plus"></i></button>';
		}
		cuerpo += '</td></tr>';

		if (e.aviso) {
			if (e.novedad_tipo || e.es_sena_electiva || e.es_sena) {
				cuerpo += '<tr style="background:#F0FDF4;"><td colspan="' + colsTotal + '" style="font-size:11.5px; color:#166534; padding:3px 10px;">'
					+ '<i class="fas fa-check-circle text-success"></i> ' + escNC(e.aviso)
					+ (e.novedad_obs ? ' &mdash; <em>' + escNC(e.novedad_obs) + '</em>' : '') + '</td></tr>';
			} else {
				cuerpo += '<tr class="ger-fila-sinbase"><td colspan="' + colsTotal + '" style="font-size:11.5px; padding:3px 10px;">'
					+ '<i class="fas fa-exclamation-triangle" style="color:#C21C1F;"></i> '
					+ escNC(e.aviso) + '</td></tr>';
			}
		}
	}

	if (visibles === 0) {
		cuerpo = '<tr><td colspan="' + colsTotal + '" class="ger-vacio" style="padding:24px; text-align:center;">'
			+ 'No se encontraron empleados con el filtro seleccionado.</td></tr>';
	}
	$('#tablaEstimado tbody').html(cuerpo);
}

function abrirModalNovedad(idempleado) {
	if (!datosEstimado || !datosEstimado.estimados) { return; }
	var emp = null;
	for (var i = 0; i < datosEstimado.estimados.length; i++) {
		if (datosEstimado.estimados[i].idempleado === idempleado) {
			emp = datosEstimado.estimados[i];
			break;
		}
	}
	if (!emp) { return; }
	empleadoEnModal = emp;

	$('#novEmpNombre').text(emp.nombre);
	$('#novEmpCargo').text(emp.cargo);
	$('#novEmpSalario').text('$' + milesNC(emp.salario));
	$('#novEmpHoras').text(formatearHorasNC(emp.h_totales) + ' horas · ' + emp.turnos + ' turnos');
	$('#novAvisoActual').html(emp.aviso
		? '<i class="fas fa-info-circle"></i> ' + escNC(emp.aviso)
		: '<span style="color:#166534;"><i class="fas fa-check-circle"></i> Sin alertas previas</span>');

	var tipoElegido = emp.novedad_tipo || '';
	if (!tipoElegido) {
		var nombreL = emp.nombre.toLowerCase();
		var cargoL = emp.cargo.toLowerCase();
		if (emp.es_sena_electiva) {
			tipoElegido = 'SENA_LECTIVA';
		} else if (emp.es_sena) {
			tipoElegido = 'SENA_PRACTICA';
		} else if (nombreL.indexOf('vigoya') !== -1 || cargoL.indexOf('gerente') !== -1
				|| cargoL.indexOf('director') !== -1 || cargoL.indexOf('coordinador') !== -1) {
			tipoElegido = 'DIRECTIVO_INCLUIR';
		} else if (emp.turnos === 0) {
			tipoElegido = 'VACACIONES_EXCLUIR';
		} else {
			tipoElegido = 'INCAPACIDAD_INCLUIR';
		}
	}

	$('input[name="optNovedad"]').prop('checked', false);
	$('.opcion-novedad-card').removeClass('activa');
	var $radio = $('input[name="optNovedad"][value="' + tipoElegido + '"]');
	if ($radio.length) {
		$radio.prop('checked', true);
		$radio.closest('.opcion-novedad-card').addClass('activa');
	}

	$('#novDiasLaborados').val(emp.novedad_dias || 3);
	$('#novPermanente').prop('checked', emp.novedad_permanente === true || emp.novedad_permanente === 1);
	$('#novObservacion').val(emp.novedad_obs || '');

	if (emp.novedad_tipo) {
		$('#btnRestablecerNov').show();
	} else {
		$('#btnRestablecerNov').hide();
	}

	$('#modalNovedad').modal('show');
}

function guardarNovedad() {
	if (!empleadoEnModal) { return; }
	var $sel = $('input[name="optNovedad"]:checked');
	if (!$sel.length) {
		alert('Seleccione una opción de situación / decisión.');
		return;
	}
	var tipo = $sel.val();
	var accion = $sel.data('accion') || 'INCLUIR';
	var dias = parseFloat($('#novDiasLaborados').val()) || 7;
	var perm = $('#novPermanente').is(':checked') ? 1 : 0;
	var obs = $.trim($('#novObservacion').val());

	$('#btnGuardarNov').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Guardando...');
	$.post('GerenciaNominaCalculo', {
		que: 'novedad',
		semana: semana(),
		idempleado: empleadoEnModal.idempleado,
		tipo_novedad: tipo,
		accion: accion,
		dias: dias,
		permanente: perm,
		observacion: obs
	}, function (data) {
		$('#btnGuardarNov').prop('disabled', false).html('<i class="fas fa-save"></i> Guardar decisión y recalcular');
		if (!Gerencia.respondio(data)) { return; }
		datosEstimado = data;
		pintarEstimado();
		$('#modalNovedad').modal('hide');
		Gerencia.avisar('Novedad guardada correctamente para ' + empleadoEnModal.nombre + '.', true);
	}, 'json').fail(function () {
		$('#btnGuardarNov').prop('disabled', false).html('<i class="fas fa-save"></i> Guardar decisión y recalcular');
		Gerencia.avisar('Error guardando la novedad.');
	});
}

function restablecerNovedad() {
	if (!empleadoEnModal) { return; }
	if (!confirm('¿Desea quitar esta justificación y volver al cálculo biométrico original?')) {
		return;
	}
	$('#btnRestablecerNov').prop('disabled', true).text('Restableciendo...');
	$.post('GerenciaNominaCalculo', {
		que: 'novedad',
		semana: semana(),
		idempleado: empleadoEnModal.idempleado,
		tipo_novedad: 'RESTABLECER'
	}, function (data) {
		$('#btnRestablecerNov').prop('disabled', false).html('<i class="fas fa-trash-alt"></i> Quitar novedad');
		if (!Gerencia.respondio(data)) { return; }
		datosEstimado = data;
		pintarEstimado();
		$('#modalNovedad').modal('hide');
		Gerencia.avisar('Novedad restablecida para ' + empleadoEnModal.nombre + '.', true);
	}, 'json').fail(function () {
		$('#btnRestablecerNov').prop('disabled', false).html('<i class="fas fa-trash-alt"></i> Quitar novedad');
		Gerencia.avisar('Error al restablecer la novedad.');
	});
}

function caja(nombre, valor, malo) {
	return ('<div class="ger-mes-caja' + (malo ? ' ger-mes-corto' : '') + '">'
		+ '<div class="n">' + escNC(nombre) + '</div>'
		+ '<div class="v" style="font-size:16px;">' + escNC(String(valor)) + '</div></div>');
}

// ===========================================================================
// La carga del real
// ===========================================================================

function cargar() {
	var texto = $('#pegado').val();
	if (!$.trim(texto)) {
		Gerencia.avisar('Pegue primero la informacion.');
		return;
	}
	var destino = $('#selDestinoCargue').val();
	var idPeriodo = 0;
	if (destino === 'PERIODO') {
		idPeriodo = parseInt($('#selPeriodoNomina').val(), 10) || 0;
		if (idPeriodo <= 0) {
			Gerencia.avisar('Seleccione una quincena (período de nómina).');
			return;
		}
	}
	$('#btnCargar').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Cargando nómina...');
	$.post('GerenciaNominaCalculo', {
		que: 'cargar',
		semana: semana(),
		idperiodo: idPeriodo,
		origen: $('#origen').val(),
		datos: texto
	}, function (data) {
		$('#btnCargar').prop('disabled', false).html('<i class="fas fa-upload"></i> Cargar nómina');
		if (!Gerencia.respondio(data)) { return; }
		pintarResultadoCarga(data);
		consultar();
	}, 'json').fail(function () {
		$('#btnCargar').prop('disabled', false).html('<i class="fas fa-upload"></i> Cargar nómina');
		Gerencia.avisar('No se pudo cargar.');
	});
}

function pintarResultadoCarga(d) {
	var problemas = d.problemas || [];
	var bien = problemas.length === 0;
	var html = '<div class="ger-aviso ' + (bien ? 'ger-aviso-ok' : 'ger-aviso-mal') + '">';
	html += '<div><strong>' + d.cargados + '</strong> personas cargadas';
	if (d.desviaciones > 0) {
		html += ', <strong>' + d.desviaciones + '</strong> desviaciones medidas contra el estimado';
	}
	html += '.</div>';
	if (d.no_encontrados > 0) {
		html += '<div><strong>' + d.no_encontrados + '</strong> documentos que no existen en el '
			+ 'maestro de empleados. Esas personas NO quedaron cargadas.</div>';
	}
	if (d.ilegibles > 0) {
		html += '<div><strong>' + d.ilegibles + '</strong> lineas que no se pudieron leer.</div>';
	}
	html += '</div>';

	if (problemas.length > 0) {
		html += '<div class="table-responsive"><table class="ger-tabla"><thead><tr>'
			+ '<th>Lo que no entro</th></tr></thead><tbody>';
		for (var i = 0; i < problemas.length; i++) {
			html += '<tr><td style="font-size:12.5px;">' + escNC(problemas[i]) + '</td></tr>';
		}
		if (d.problemas_totales > problemas.length) {
			html += '<tr><td class="ger-nota">Y ' + (d.problemas_totales - problemas.length)
				+ ' mas. Con tantos, lo que esta mal no es una linea: es el formato.</td></tr>';
		}
		html += '</tbody></table></div>';
	}
	$('#resultadoCarga').html(html);
}

// ===========================================================================
// La calibracion
// ===========================================================================

function cargarCalibracion() {
	$.getJSON('GerenciaNominaCalculo', { que: 'desviaciones', semana: semana() }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		var lista = data.desviaciones || [];
		if (lista.length === 0) {
			$('#tablaCalibracion tbody').html('<tr><td colspan="5" class="ger-vacio">'
				+ 'Todavia no hay semanas con estimado y real para comparar. La historia se arma '
				+ 'sola a medida que se carguen reales.</td></tr>');
			return;
		}
		var cuerpo = '';
		for (var i = 0; i < lista.length; i++) {
			var d = lista[i];
			cuerpo += '<tr><td>' + escNC(d.tienda) + '</td>';
			cuerpo += '<td class="ger-num">' + d.muestras + '</td>';
			cuerpo += '<td class="ger-num" style="font-weight:bold;color:'
				+ (d.promedio > 0 ? '#8A6400' : '#16704F') + ';">'
				+ (d.promedio > 0 ? '+' : '') + unaDecimal(d.promedio) + '%</td>';
			cuerpo += '<td class="ger-num">' + unaDecimal(d.dispersion) + '</td>';
			cuerpo += '<td>' + (d.se_aplica
				? '<span class="ger-origen ger-real">Se aplica</span>'
				: '<span class="ger-origen ger-estimado">No</span> <span class="ger-nota">'
					+ escNC(d.por_que_no) + '</span>') + '</td></tr>';
		}
		$('#tablaCalibracion tbody').html(cuerpo);
	});
}

// ===========================================================================
// Los parametros
// ===========================================================================

function cargarParametros() {
	$.getJSON('GerenciaNominaCalculo', { que: 'parametros' }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		parametros = data.parametros || [];
		pintarParametros();
	});
}

function pintarParametros() {
	var hoy = iso(new Date());
	var cuerpo = '';
	for (var i = 0; i < parametros.length; i++) {
		var p = parametros[i];
		//Vigente es el que ya empezo y no ha terminado. Los demas se ven
		//apagados: son historia o son futuro, y confundirlos con el que rige es
		//justo lo que esta tabla existe para evitar.
		var vigente = p.desde <= hoy && (!p.hasta || p.hasta >= hoy);
		cuerpo += '<tr' + (vigente ? '' : ' style="opacity:.5;"') + '>';
		cuerpo += '<td><strong>' + escNC(p.nombre) + '</strong>'
			+ '<div style="font-size:11px;color:#6C7482;">' + escNC(p.codigo) + '</div></td>';
		cuerpo += '<td class="ger-num" style="font-weight:bold;">'
			+ (p.unidad === 'PESOS' ? '$' + milesNC(p.valor) : unaDecimal(p.valor)) + '</td>';
		cuerpo += '<td style="font-size:11.5px;">' + escNC(p.unidad) + '</td>';
		cuerpo += '<td>' + escNC(p.desde) + '</td>';
		cuerpo += '<td>' + (p.hasta ? escNC(p.hasta)
			: '<span class="ger-origen ger-real">Rige hoy</span>') + '</td>';
		cuerpo += '<td style="font-size:11.5px;color:#6C7482;">' + escNC(p.observacion) + '</td>';
		cuerpo += '<td>' + (vigente
			? '<button class="btn btn-default ger-mini ger-cambiar" data-codigo="'
				+ escNC(p.codigo) + '" data-nombre="' + escNC(p.nombre) + '">Cambiar</button>'
			: '') + '</td>';
		cuerpo += '</tr>';
	}
	$('#tablaParametros tbody').html(cuerpo);

	$('.ger-cambiar').off('click').on('click', function () {
		codigoEnCambio = $(this).data('codigo');
		$('#tituloCambio').text('Cambiar: ' + $(this).data('nombre'));
		$('#valorNuevo').val('');
		$('#rigeDesde').val('');
		$('#sustento').val('');
		$('#panelCambio').show();
		$('#valorNuevo').focus();
	});
}

function guardarParametro() {
	if (!codigoEnCambio) { return; }
	var desde = $.trim($('#rigeDesde').val());
	if (!/^\d{4}-\d{2}-\d{2}$/.test(desde)) {
		Gerencia.avisar('La fecha desde la que rige debe ir como aaaa-mm-dd.');
		return;
	}
	var valor = Gerencia.numero($('#valorNuevo').val());
	if (valor === null) {
		Gerencia.avisar('El valor no se entiende. Escriba solo numeros.');
		return;
	}
	$.post('GerenciaNominaCalculo', {
		que: 'parametro', codigo: codigoEnCambio, valor: valor,
		desde: desde, observacion: $.trim($('#sustento').val())
	}, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		Gerencia.avisar('Listo. La vigencia anterior quedo cerrada el dia antes, y las semanas ya '
			+ 'calculadas no cambian hasta que se recalculen.', true);
		$('#panelCambio').hide();
		cargarParametros();
	}, 'json');
}

// ===========================================================================
// Auxiliares
// ===========================================================================

function iso(d) {
	return (d.getFullYear() + '-' + dosNC(d.getMonth() + 1) + '-' + dosNC(d.getDate()));
}

function dosNC(n) {
	return (n < 10 ? '0' + n : '' + n);
}

function milesNC(valor) {
	if (valor === null || valor === undefined || isNaN(valor)) { return (''); }
	var n = Math.round(valor);
	var signo = n < 0 ? '-' : '';
	return (signo + Math.abs(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function formatearHorasNC(v) {
	if (v === null || v === undefined || isNaN(v)) { return ('0'); }
	var r = Math.round(v * 10) / 10;
	if (Math.abs(r - Math.round(r)) < 0.05) {
		return Math.round(r);
	}
	return r;
}

function unaDecimal(v) {
	if (v === null || v === undefined || isNaN(v)) { return (''); }
	var r = Math.round(v * 10) / 10;
	if (Math.abs(r - Math.round(r)) < 0.05) {
		return Math.round(r);
	}
	return r;
}

function escNC(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;')
		.replace(/>/g, '&gt;').replace(/"/g, '&quot;'));
}

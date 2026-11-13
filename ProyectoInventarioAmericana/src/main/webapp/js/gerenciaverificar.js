/**
 * Verificar la liquidacion de un empleado, semana a semana.
 *
 * SOLO CONSULTA. El servidor recalcula con las mismas funciones del calculo de nomina y
 * devuelve el detalle; esta pantalla no guarda nada. Su unico trabajo es mostrarlo de
 * forma que se pueda revisar: los turnos, como cayo cada hora, y de donde sale cada valor.
 *
 * LO QUE SE CONTRASTA
 *
 * Cada semana trae lo recalculado ahora, lo que hay guardado en el estimado y lo que paso al
 * tablero. Si lo guardado no coincide con lo recalculado es que algo cambio despues de calcular
 * (una marcacion corregida, un parametro, una novedad) y la pantalla lo dice.
 *
 * Todo en ES5: la pantalla convive con jQuery 1.11.
 */

var VL_SEMANAS_ATRAS = 16;
var VL_MAX_SEMANAS = 8;
var VL_EMPLEADOS = [];
var VL_DATOS = null;

var VL_CATS = ['Ordinaria', 'Nocturna', 'Dominical', 'Dom. nocturna', 'Festiva', 'Fest. nocturna'];

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	llenarSemanas();
	cargarEmpleados();
	$('#btnVer').on('click', ver);
	$('#buscarEmp').on('input', pintarEmpleados);
	$('.btn-rapido').on('click', function () { rapido(parseInt($(this).data('n'), 10)); });
	$('#btnAbrirTodo').on('click', function () { $('.vl-semana').addClass('abierta'); });
	$('#btnCerrarTodo').on('click', function () { $('.vl-semana').removeClass('abierta'); });
	$('#btnLey').on('click', function () { $('#panelLey').toggle(); });
}

// ------------------------------------------------------------------------------------------
// Formatos
// ------------------------------------------------------------------------------------------

function esc(s) {
	return ($('<div/>').text(s === null || s === undefined ? '' : s).html());
}

function pesos(v) {
	var n = Math.round(Number(v) || 0);
	return ((n < 0 ? '-$ ' : '$ ') + Math.abs(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

/** Horas con hasta dos decimales, sin ceros de relleno; "-" cuando es cero. */
function hrs(v) {
	var n = Math.round((Number(v) || 0) * 100) / 100;
	if (n === 0) {
		return ('<span class="vl-cero">-</span>');
	}
	var t = (Math.abs(n) === Math.floor(Math.abs(n))) ? String(n) : n.toFixed(2).replace(/0+$/, '').replace(/\.$/, '');
	return (t.replace('.', ','));
}

function iso(d) {
	var m = d.getMonth() + 1;
	var dd = d.getDate();
	return (d.getFullYear() + '-' + (m < 10 ? '0' : '') + m + '-' + (dd < 10 ? '0' : '') + dd);
}

function fechaCorta(texto) {
	if (!texto || texto.length < 10) {
		return ('');
	}
	return (texto.substring(8, 10) + '/' + texto.substring(5, 7));
}

// ------------------------------------------------------------------------------------------
// Los selectores
// ------------------------------------------------------------------------------------------

function llenarSemanas() {
	var hoy = new Date();
	var d = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
	//Las semanas van de lunes a domingo; el ultimo domingo ya pasado es el cierre de la ultima semana completa.
	d.setDate(d.getDate() - d.getDay());
	if (hoy.getDay() === 0) {
		d.setDate(d.getDate() - 7);
	}
	var html = '';
	for (var i = 0; i < VL_SEMANAS_ATRAS; i++) {
		var txt = iso(d);
		var ini = new Date(d.getFullYear(), d.getMonth(), d.getDate() - 6);
		html += '<option value="' + txt + '">' + fechaCorta(iso(ini)) + ' al ' + fechaCorta(txt) + ' (dom. ' + txt + ')</option>';
		d.setDate(d.getDate() - 7);
	}
	$('#semDesde').html(html);
	$('#semHasta').html(html);
	//Por defecto, las cuatro ultimas semanas completas. Las opciones van de la mas reciente a la mas vieja.
	$('#semHasta').prop('selectedIndex', 0);
	$('#semDesde').prop('selectedIndex', 3);
}

function rapido(n) {
	$('#semHasta').prop('selectedIndex', 0);
	$('#semDesde').prop('selectedIndex', n - 1);
	if ($('#empleado').val()) {
		ver();
	}
}

function cargarEmpleados() {
	$.getJSON(server + 'GerenciaNominaVerificar', { que: 'empleados' }, function (d) {
		if (!Gerencia.respondio(d)) {
			return;
		}
		VL_EMPLEADOS = d.empleados || [];
		pintarEmpleados();
	}).fail(function () {
		Gerencia.avisar('No se pudo traer la lista de empleados.');
	});
}

function pintarEmpleados() {
	var filtro = $.trim($('#buscarEmp').val()).toLowerCase();
	var actual = $('#empleado').val();
	var html = '';
	var n = 0;
	for (var i = 0; i < VL_EMPLEADOS.length; i++) {
		var e = VL_EMPLEADOS[i];
		var texto = (e.nombre + ' ' + e.cargo).toLowerCase();
		if (filtro !== '' && texto.indexOf(filtro) === -1) {
			continue;
		}
		n++;
		html += '<option value="' + e.id + '">' + esc(e.nombre) + ' — ' + esc(e.cargo) +
			(e.medio_tiempo === 'S' ? ' (medio tiempo)' : '') + (e.activo === 'N' ? ' (inactivo)' : '') + '</option>';
	}
	$('#empleado').html(html);
	if (actual && $('#empleado option[value="' + actual + '"]').length) {
		$('#empleado').val(actual);
	}
	$('#contadorEmp').text(n + ' de ' + VL_EMPLEADOS.length + ' personas');
}

/** Los domingos del rango escogido, del mas viejo al mas reciente. */
function semanasEscogidas() {
	var iDesde = $('#semDesde').prop('selectedIndex');
	var iHasta = $('#semHasta').prop('selectedIndex');
	//Las opciones van de la mas reciente (0) a la mas vieja: "desde" es la de indice mayor.
	var mayor = Math.max(iDesde, iHasta);
	var menor = Math.min(iDesde, iHasta);
	var lista = [];
	for (var i = mayor; i >= menor; i--) {
		lista.push($('#semDesde option').eq(i).val());
	}
	return (lista);
}

// ------------------------------------------------------------------------------------------
// La consulta
// ------------------------------------------------------------------------------------------

function ver() {
	var id = $('#empleado').val();
	if (!id) {
		Gerencia.avisar('Escoja un empleado.');
		return;
	}
	var semanas = semanasEscogidas();
	if (semanas.length > VL_MAX_SEMANAS) {
		Gerencia.avisar('Escoja máximo ' + VL_MAX_SEMANAS + ' semanas.');
		return;
	}
	$('#btnVer').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Calculando...');
	$('#mensaje').empty();
	$.getJSON(server + 'GerenciaNominaVerificar', { que: 'verificar', idempleado: id, semanas: semanas.join(',') }, function (d) {
		$('#btnVer').prop('disabled', false).html('<i class="fas fa-search"></i> Ver liquidación');
		if (d.respuesta === 'NOK' || d.respuesta === 'INVALIDO') {
			$('#mensaje').html('<div class="ger-aviso ger-aviso-mal" style="display:block;">' + esc(d.detalle) + '</div>');
			$('#resultado').hide();
			return;
		}
		if (!Gerencia.respondio(d)) {
			return;
		}
		VL_DATOS = d;
		pintar(d);
	}).fail(function () {
		$('#btnVer').prop('disabled', false).html('<i class="fas fa-search"></i> Ver liquidación');
		Gerencia.avisar('No se pudo consultar.');
	});
}

// ------------------------------------------------------------------------------------------
// Pintar
// ------------------------------------------------------------------------------------------

function pintar(d) {
	pintarPersona(d.persona);
	pintarResumen(d);
	pintarLey(d.parametros || []);
	var html = '';
	for (var i = 0; i < d.semanas.length; i++) {
		html += pintarSemana(d.semanas[i], i, d.persona);
	}
	$('#semanas').html(html);
	$('.vl-semana-cab').on('click', function () { $(this).parent().toggleClass('abierta'); });
	$('#resultado').show();
	//La primera semana de la lista abierta; el resto cerradas para no abrumar.
	$('.vl-semana').first().addClass('abierta');
}

function pintarPersona(p) {
	var html = '<div class="vl-persona"><span class="nombre">' + esc(p.nombre) + '</span>' +
		'<span class="dato">Cédula <b>' + esc(p.cedula) + '</b></span>' +
		'<span class="dato">Cargo <b>' + esc(p.cargo) + '</b></span>' +
		'<span class="dato">Área <b>' + esc(p.area || '-') + '</b></span>' +
		'<span class="dato">Salario mensual <b>' + pesos(p.salario) + '</b></span>' +
		'<span class="dato">Horas <b>' + (p.horas_enteras ? 'enteras por día' : 'con decimales') + '</b></span>' +
		(p.medio_tiempo ? '<span class="vl-chip vl-info">Medio tiempo</span>' : '') +
		(p.sena ? '<span class="vl-chip vl-info">Aprendiz SENA</span>' : '') +
		(!p.activo ? '<span class="vl-chip vl-aviso">Inactivo / retirado</span>' : '') +
		'</div>';
	$('#panelPersona').html(html);
}

function celda(valor, esHoras) {
	return ('<td>' + (esHoras ? hrs(valor) : (Math.round(valor) === 0 ? '<span class="vl-cero">-</span>' : pesos(valor))) + '</td>');
}

function pintarResumen(d) {
	var cabH = '<tr><th class="izq">Semana</th><th>Turnos</th><th>Total h</th><th>Ordinaria</th><th>Nocturna</th>' +
		'<th>Dominical</th><th>Dom. noct.</th><th>Festiva</th><th>Fest. noct.</th><th>Extra ord.</th><th>Extra fest.</th></tr>';
	var cabP = '<tr><th class="izq">Semana</th><th>Básico</th><th>Rec. nocturno</th><th>Rec. dominical</th><th>Rec. festivo</th>' +
		'<th>Extra ord.</th><th>Extra fest.</th><th>Auxilio</th><th>Seg. social</th><th>Liquidación</th><th>Costo</th>' +
		'<th>Guardado</th><th>Real / tablero</th></tr>';
	var t = { turnos: 0, h_tot: 0, h_ord: 0, h_noc: 0, h_dom: 0, h_dom_noct: 0, h_fest: 0, h_fest_noct: 0, h_extra: 0, h_extra_fest: 0,
		basico: 0, rec_noc: 0, rec_dom: 0, rec_fest: 0, rec_extra: 0, rec_extra_fest: 0, auxilio: 0, seg_social: 0, liquidacion: 0, costo: 0, guardado: 0 };
	var filasH = '';
	var filasP = '';
	for (var i = 0; i < d.semanas.length; i++) {
		var s = d.semanas[i];
		var etq = fechaCorta(s.desde) + ' al ' + fechaCorta(s.hasta);
		var enlace = '<td class="izq"><a href="#" class="vl-ir" data-i="' + i + '">' + etq + '</a></td>';
		if (s.error || !s.calculo) {
			filasH += '<tr>' + enlace + '<td colspan="10" class="izq"><span class="vl-chip vl-mal">' + esc(s.error || 'sin cálculo') + '</span></td></tr>';
			filasP += '<tr>' + enlace + '<td colspan="12" class="izq"><span class="vl-chip vl-mal">' + esc(s.error || 'sin cálculo') + '</span></td></tr>';
			continue;
		}
		var c = s.calculo;
		filasH += '<tr>' + enlace + '<td>' + c.turnos + '</td>' + celda(c.h_tot, true) + celda(c.h_ord, true) + celda(c.h_noc, true) +
			celda(c.h_dom, true) + celda(c.h_dom_noct, true) + celda(c.h_fest, true) + celda(c.h_fest_noct, true) +
			celda(c.h_extra, true) + celda(c.h_extra_fest, true) + '</tr>';
		var real = s.real.hay ? pesos(s.real.total) + ' <span class="vl-chip vl-ok">' + esc(s.real.origen) + '</span>'
			: (s.tablero.hay ? pesos(s.tablero.total) + ' <span class="vl-chip vl-aviso">estimado</span>' : '<span class="vl-cero">no pasó</span>');
		filasP += '<tr>' + enlace + celda(c.basico) + celda(c.rec_noc) + celda(c.rec_dom) + celda(c.rec_fest) + celda(c.rec_extra) +
			celda(c.rec_extra_fest) + celda(c.auxilio) + celda(c.seg_social) + celda(c.liquidacion) +
			'<td><b>' + pesos(c.costo) + '</b></td><td>' + (s.guardado.hay ? pesos(s.guardado.costo) : '<span class="vl-cero">sin guardar</span>') +
			'</td><td>' + real + '</td></tr>';
		for (var k in t) {
			if (k === 'guardado') {
				t.guardado += s.guardado.hay ? s.guardado.costo : 0;
			} else if (c[k] !== undefined) {
				t[k] += Number(c[k]) || 0;
			}
		}
	}
	filasH += '<tr class="vl-total"><td class="izq">TOTAL</td><td>' + t.turnos + '</td>' + celda(t.h_tot, true) + celda(t.h_ord, true) +
		celda(t.h_noc, true) + celda(t.h_dom, true) + celda(t.h_dom_noct, true) + celda(t.h_fest, true) + celda(t.h_fest_noct, true) +
		celda(t.h_extra, true) + celda(t.h_extra_fest, true) + '</tr>';
	filasP += '<tr class="vl-total"><td class="izq">TOTAL</td>' + celda(t.basico) + celda(t.rec_noc) + celda(t.rec_dom) + celda(t.rec_fest) +
		celda(t.rec_extra) + celda(t.rec_extra_fest) + celda(t.auxilio) + celda(t.seg_social) + celda(t.liquidacion) +
		'<td>' + pesos(t.costo) + '</td><td>' + pesos(t.guardado) + '</td><td></td></tr>';
	$('#tablaHoras').html(cabH + filasH);
	$('#tablaPesos').html(cabP + filasP);
	$('.vl-ir').on('click', function (e) {
		e.preventDefault();
		var i = parseInt($(this).data('i'), 10);
		var caja = $('.vl-semana').eq(i);
		caja.addClass('abierta');
		$('html, body').animate({ scrollTop: caja.offset().top - 20 }, 250);
	});
}

function pintarLey(params) {
	var html = '<h4 class="ger-titulo">La ley aplicada a la última semana pedida</h4><p class="ger-sub">Son los parámetros de nómina vigentes en esa fecha (se cambian en Cálculo de nómina &rsaquo; Parámetros de ley).</p>' +
		'<div class="vl-tabla-scroll"><table class="vl-tabla"><tr><th class="izq">Parámetro</th><th>Valor</th></tr>';
	for (var i = 0; i < params.length; i++) {
		html += '<tr><td class="izq">' + esc(params[i].codigo) + '</td><td>' + String(params[i].valor).replace('.', ',') + '</td></tr>';
	}
	html += '</table></div>';
	$('#panelLey').html(html);
}

function pintarSemana(s, idx, persona) {
	var etq = fechaCorta(s.desde) + ' al ' + fechaCorta(s.hasta) + ' (domingo ' + s.semana + ')';
	if (s.error || !s.calculo) {
		return ('<div class="vl-semana"><div class="vl-semana-cab"><h4>' + etq + '</h4><span class="vl-chip vl-mal">' + esc(s.error || 'sin cálculo') + '</span></div></div>');
	}
	var c = s.calculo;
	var chips = '<span class="vl-chip vl-info">' + hrsTxt(c.h_tot) + ' h</span>' +
		'<span class="vl-chip vl-info">' + pesos(c.costo) + '</span>';
	if (s.guardado.hay) {
		var dif = Math.round(c.costo - s.guardado.costo);
		chips += Math.abs(dif) <= 1
			? '<span class="vl-chip vl-ok">Coincide con lo guardado</span>'
			: '<span class="vl-chip vl-mal">Difiere de lo guardado en ' + pesos(dif) + '</span>';
	} else {
		chips += '<span class="vl-chip vl-aviso">Esa semana no está calculada/guardada</span>';
	}
	if (s.novedad) {
		chips += '<span class="vl-chip vl-aviso">Con novedad</span>';
	}
	var cuerpo = '';
	if (c.aviso) {
		cuerpo += '<div class="ger-aviso" style="display:block; background:#FDE9C6; color:#8A6400; border:1px solid #F0CE8E;">' + esc(c.aviso) + '</div>';
	}
	cuerpo += cuadroContraste(s, c);
	cuerpo += '<div class="vl-h5">1. Turnos marcados en la biometría</div>' + tablaTurnos(s);
	cuerpo += '<div class="vl-h5">2. Día por día: cómo cayó cada hora y qué se paga</div>' + tablaDias(s);
	cuerpo += '<div class="vl-h5">3. Cómo se calculó cada valor</div>' + tablaExplicacion(s);
	return ('<div class="vl-semana" id="sem' + idx + '"><div class="vl-semana-cab"><h4><i class="fas fa-calendar-week"></i> ' + etq +
		'</h4>' + chips + '</div><div class="vl-semana-cuerpo">' + cuerpo + '</div></div>');
}

function hrsTxt(v) {
	var n = Math.round((Number(v) || 0) * 100) / 100;
	return (String(n).replace('.', ','));
}

function cuadroContraste(s, c) {
	var h = '<div class="vl-tabla-scroll"><table class="vl-tabla"><tr><th class="izq">Cifra</th><th>Valor</th><th class="izq">Detalle</th></tr>';
	h += '<tr><td class="izq">Recalculado ahora</td><td><b>' + pesos(c.costo) + '</b></td><td class="izq vl-formula">Con los turnos, parámetros y novedades de hoy.</td></tr>';
	h += '<tr><td class="izq">Guardado en el estimado</td><td>' + (s.guardado.hay ? pesos(s.guardado.costo) : '-') +
		'</td><td class="izq vl-formula">' + (s.guardado.hay ? 'Calculado el ' + esc(s.guardado.fecha) + '. ' +
		(Math.abs(c.costo - s.guardado.costo) <= 1 ? 'Es igual a lo recalculado.' : 'Es distinto: después de calcular cambió una marcación, un parámetro, una novedad o el salario. Vuelva a calcular la semana si el cambio es válido.')
		: 'Esa semana no tiene estimado guardado.') + '</td></tr>';
	h += '<tr><td class="izq">Lo que pasó al tablero</td><td>' + (s.tablero.hay ? pesos(s.tablero.total) : '-') +
		'</td><td class="izq vl-formula">' + (s.tablero.hay ? 'Origen: ' + esc(s.tablero.origen) + '.' : 'No se ha pasado esa semana al tablero.') + '</td></tr>';
	if (s.real.hay) {
		var dif = Math.round(s.real.total - c.costo);
		h += '<tr><td class="izq">Real cargado</td><td>' + pesos(s.real.total) + '</td><td class="izq vl-formula">Origen ' + esc(s.real.origen) +
			'. Diferencia contra lo recalculado: <b>' + pesos(dif) + '</b>.</td></tr>';
	}
	if (s.novedad) {
		h += '<tr class="vl-fila-novedad"><td class="izq">Novedad</td><td>' + pesos(s.costo_sin_novedad) + ' &rarr; ' + pesos(c.costo) +
			'</td><td class="izq vl-formula">' + esc(s.novedad) + '</td></tr>';
	}
	h += '</table></div>';
	return (h);
}

function tablaTurnos(s) {
	if (!s.turnos_det.length) {
		return ('<p class="ger-vacio">No hay turnos marcados esa semana: la persona queda con su básico y sin recargos.</p>');
	}
	var h = '<div class="vl-tabla-scroll"><table class="vl-tabla"><tr><th class="izq">Día</th><th class="izq">Entra</th><th class="izq">Sale</th><th class="izq">Tienda</th><th>Horas</th>';
	for (var i = 0; i < 6; i++) {
		h += '<th>' + VL_CATS[i] + '</th>';
	}
	h += '</tr>';
	var tot = [0, 0, 0, 0, 0, 0];
	var totH = 0;
	for (var k = 0; k < s.turnos_det.length; k++) {
		var t = s.turnos_det[k];
		h += '<tr' + (t.estimado ? ' class="vl-fila-novedad"' : '') + '><td class="izq">' + esc(t.dia_semana) + ' ' + fechaCorta(t.dia) + '</td><td class="izq">' +
			esc(t.entra.substring(11)) + '</td><td class="izq">' + esc(t.sale.substring(11)) + (t.estimado ? ' <span class="vl-chip vl-aviso">salida asumida</span>' : '') +
			(t.sale.substring(0, 10) !== t.entra.substring(0, 10) ? ' <span class="vl-chip vl-info">día siguiente</span>' : '') +
			'</td><td class="izq">' + esc(t.tienda) + '</td><td><b>' + hrs(t.horas) + '</b></td>';
		for (var i = 0; i < 6; i++) {
			h += '<td>' + hrs(t.cat[i]) + '</td>';
			tot[i] += t.cat[i];
		}
		totH += t.horas;
		h += '</tr>';
	}
	h += '<tr class="vl-total"><td class="izq" colspan="4">Total de los turnos</td><td>' + hrs(totH) + '</td>';
	for (var j = 0; j < 6; j++) {
		h += '<td>' + hrs(tot[j]) + '</td>';
	}
	h += '</tr></table></div>';
	h += '<div class="vl-nota">Las horas se parten en bloques de una hora según la franja en que caen (nocturna desde las 19:00 hasta las 06:00; dominical y festivo por la fecha de cada bloque). Una salida marcada como asumida no se marcó en el huellero: se supone la hora de cierre del punto de venta, con un tope de 10 horas.</div>';
	return (h);
}

function tablaDias(s) {
	if (!s.dias_det.length) {
		return ('<p class="ger-vacio">Sin días con marcaciones.</p>');
	}
	var h = '<div class="vl-tabla-scroll"><table class="vl-tabla"><tr><th class="izq" rowspan="2">Día</th><th rowspan="2">Horas exactas</th>' +
		'<th rowspan="2">' + (s.horas_enteras ? 'Redondeo (entero)' : 'Redondeo') + '</th><th colspan="6" style="text-align:center;">Horas que se pagan por franja</th><th rowspan="2">Total pagado</th></tr><tr>';
	for (var i = 0; i < 6; i++) {
		h += '<th>' + VL_CATS[i] + '</th>';
	}
	h += '</tr>';
	var tot = [0, 0, 0, 0, 0, 0];
	var totEx = 0;
	var totPag = 0;
	for (var k = 0; k < s.dias_det.length; k++) {
		var d = s.dias_det[k];
		var clase = d.festivo ? 'vl-festivo' : (d.domingo ? 'vl-domingo' : '');
		var redondeo;
		if (d.redondeado) {
			var diff = Math.round((d.horas_pagadas - d.horas_exactas) * 100) / 100;
			redondeo = (diff === 0 ? 'exacto' : (diff > 0 ? '+' : '') + String(diff).replace('.', ',') + ' h');
		} else {
			redondeo = 'sin redondeo';
		}
		h += '<tr class="' + clase + '"><td class="izq">' + esc(d.dia_semana) + ' ' + fechaCorta(d.fecha) +
			(d.festivo ? ' <span class="vl-chip vl-aviso">festivo</span>' : '') + (d.domingo && !d.festivo ? ' <span class="vl-chip vl-info">domingo</span>' : '') +
			'</td><td>' + hrs(d.horas_exactas) + '</td><td>' + redondeo + '</td>';
		for (var i2 = 0; i2 < 6; i2++) {
			h += '<td>' + hrs(d.pagadas[i2]) + '</td>';
			tot[i2] += d.pagadas[i2];
		}
		h += '<td><b>' + hrs(d.horas_pagadas) + '</b></td></tr>';
		totEx += d.horas_exactas;
		totPag += d.horas_pagadas;
	}
	h += '<tr class="vl-total"><td class="izq">Total de la semana</td><td>' + hrs(totEx) + '</td><td></td>';
	for (var j = 0; j < 6; j++) {
		h += '<td>' + hrs(tot[j]) + '</td>';
	}
	h += '<td>' + hrs(totPag) + '</td></tr></table></div>';
	h += '<div class="vl-nota">' + (s.horas_enteras
		? 'Esta persona es de punto de venta o contact center: las horas de cada día se redondean al entero más cercano (3,2 h pagan 3; 3,7 h pagan 4) y se reparten entre las franjas en proporción a lo que cayó en cada una.'
		: 'Esta persona es de bodega o administrativa: las horas conservan sus decimales.') +
		' Si lo que se paga no cuadra con los turnos, revise aquí el redondeo.</div>';
	return (h);
}

function tablaExplicacion(s) {
	var h = '<div class="vl-tabla-scroll"><table class="vl-tabla"><tr><th class="izq">Concepto</th><th class="izq">Cómo se calculó</th><th>Valor</th></tr>';
	for (var i = 0; i < s.explicacion.length; i++) {
		var f = s.explicacion[i];
		var clase = f.tipo === 'total' ? 'vl-total' : (f.tipo === 'subtotal' ? 'vl-sub' : (f.tipo === 'info' ? 'vl-fila-info' : (f.tipo === 'novedad' ? 'vl-fila-novedad' : '')));
		h += '<tr class="' + clase + '"><td class="izq"><b>' + esc(f.concepto) + '</b></td><td class="izq vl-formula">' + esc(f.formula) + '</td><td>' +
			(f.con_valor ? pesos(f.valor) : '') + '</td></tr>';
	}
	h += '</table></div>';
	return (h);
}

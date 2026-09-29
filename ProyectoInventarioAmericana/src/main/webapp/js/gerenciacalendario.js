/**
 * Calendario de semanas de gerencia.
 *
 * La pantalla trabaja sobre una copia en memoria del ano y solo escribe cuando
 * se le da Guardar. Generar propone; guardar es otra decision. Un boton que
 * genere Y guarde convierte un clic equivocado en un ano reescrito.
 */

var MESES = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
	'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

var semanas = [];
var estado = '';
var anioEnPantalla = 0;

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	llenarAnios();
	$('#anio').on('change', function () { consultar(parseInt($('#anio').val(), 10)); });
	$('#btnGenerar').on('click', generar);
	$('#btnGuardar').on('click', guardar);
	$('#btnCerrar').on('click', cambiarEstado);
	consultar(new Date().getFullYear());
}

/** Del ano pasado a tres adelante: alcanza para planear y no llena el combo. */
function llenarAnios() {
	var hoy = new Date().getFullYear();
	var opciones = '';
	for (var a = hoy - 1; a <= hoy + 3; a++) {
		opciones += '<option value="' + a + '"' + (a === hoy ? ' selected' : '') + '>' + a + '</option>';
	}
	$('#anio').html(opciones);
}

function consultar(anio) {
	anioEnPantalla = anio;
	$.getJSON('GerenciaCalendario', { que: 'consultar', anio: anio }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		semanas = data.semanas || [];
		estado = data.estado || '';
		pintar();
		if (semanas.length === 0) {
			Gerencia.avisar('El ano ' + anio + ' todavia no tiene calendario. '
				+ 'Use Generar propuesta y reviselo antes de guardar.');
		} else {
			Gerencia.avisar('');
		}
	}).fail(function () {
		Gerencia.avisar('No se pudo consultar el calendario.');
	});
}

function generar() {
	var anio = parseInt($('#anio').val(), 10);
	if (estado === 'CERRADO') {
		Gerencia.avisar('El ano ' + anio + ' esta cerrado. Hay que reabrirlo antes de cambiarlo.');
		return;
	}
	if (semanas.length > 0
		&& !confirm('El ano ' + anio + ' ya tiene calendario guardado.\n\n'
			+ 'Generar reemplaza lo que hay en pantalla por la propuesta. '
			+ 'Lo guardado no cambia hasta que le de Guardar.\n\nContinuar?')) {
		return;
	}
	$.getJSON('GerenciaCalendario', { que: 'generar', anio: anio }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		semanas = data.semanas || [];
		estado = data.estado || '';
		pintar();
		Gerencia.avisar('Propuesta de ' + semanas.length + ' semanas. '
			+ 'Revisela y guarde. Todavia no se ha guardado nada.');
	}).fail(function () {
		Gerencia.avisar('No se pudo generar la propuesta.');
	});
}

function guardar() {
	if (semanas.length === 0) {
		Gerencia.avisar('No hay nada que guardar.');
		return;
	}
	var anio = parseInt($('#anio').val(), 10);
	var lineas = [];
	for (var i = 0; i < semanas.length; i++) {
		var s = semanas[i];
		lineas.push(s.numero + ';' + s.inicio + ';' + s.fin + ';' + s.mes);
	}
	$('#btnGuardar').prop('disabled', true);
	$.post('GerenciaCalendario', { que: 'guardar', anio: anio, semanas: lineas.join('\n') },
		function (data) {
			$('#btnGuardar').prop('disabled', false);
			if (data && data.respuesta === 'INVALIDO') {
				//El servidor revisa huecos, traslapes y meses cortos. Lo que
				//diga se muestra tal cual: es mas concreto que "datos malos".
				Gerencia.avisar(data.detalle);
				return;
			}
			if (!Gerencia.respondio(data)) { return; }
			Gerencia.avisar('Calendario de ' + anio + ' guardado: ' + data.semanas + ' semanas.', true);
			consultar(anio);
		}, 'json').fail(function () {
			$('#btnGuardar').prop('disabled', false);
			Gerencia.avisar('No se pudo guardar el calendario.');
		});
}

function cambiarEstado() {
	var anio = parseInt($('#anio').val(), 10);
	var nuevo = (estado === 'CERRADO') ? 'ABIERTO' : 'CERRADO';
	var pregunta = (nuevo === 'CERRADO')
		? 'Cerrar el ano ' + anio + ' congela su calendario: nadie lo podra cambiar '
			+ 'mientras siga cerrado.\n\nEs lo que evita que un mes ya reportado cambie '
			+ 'de tamano por detras.\n\nCerrar?'
		: 'Reabrir el ano ' + anio + ' permite cambiarlo otra vez.\n\nSi ya se reportaron '
			+ 'meses de este ano, cambiar las semanas hace que dejen de ser comparables '
			+ 'con lo que ya se mostro.\n\nReabrir?';
	if (!confirm(pregunta)) { return; }

	$.post('GerenciaCalendario', { que: 'estado', anio: anio, estado: nuevo }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		estado = nuevo;
		pintar();
		Gerencia.avisar('El ano ' + anio + ' quedo ' + nuevo.toLowerCase() + '.', true);
	}, 'json').fail(function () {
		Gerencia.avisar('No se pudo cambiar el estado del ano.');
	});
}

function pintar() {
	pintarEstado();
	pintarResumen();
	pintarTabla();
}

function pintarEstado() {
	var cerrado = (estado === 'CERRADO');
	$('#estadoAnio').html(estado === ''
		? '<span class="ger-nota">Sin calendario guardado</span>'
		: '<span class="ger-estado ' + (cerrado ? 'ger-cerrado' : 'ger-abierto') + '">'
			+ estado + '</span>');
	$('#btnCerrar').html(cerrado
		? '<i class="fas fa-lock-open"></i> Reabrir a&ntilde;o'
		: '<i class="fas fa-lock"></i> Cerrar a&ntilde;o');
	$('#btnGuardar').prop('disabled', cerrado);
	$('#btnGenerar').prop('disabled', cerrado);
	$('#btnCerrar').prop('disabled', semanas.length === 0);
}

function pintarResumen() {
	var porMes = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
	for (var i = 0; i < semanas.length; i++) {
		porMes[semanas[i].mes]++;
	}
	var html = '';
	for (var m = 1; m <= 12; m++) {
		//Menos de cuatro se marca en rojo. No bloquea la pantalla: el que
		//bloquea es el servidor al guardar, que es donde importa.
		var corto = (porMes[m] < 4);
		html += '<div class="ger-mes-caja' + (corto ? ' ger-mes-corto' : '') + '">'
			+ '<div class="n">' + MESES[m].substring(0, 3) + '</div>'
			+ '<div class="v">' + porMes[m] + '</div></div>';
	}
	html += '<div class="ger-mes-caja" style="border-color:#102F6F;">'
		+ '<div class="n">Total</div><div class="v">' + semanas.length + '</div></div>';
	$('#resumenMeses').html(html);
}

function pintarTabla() {
	var cuerpo = $('#tablaSemanas tbody');
	if (semanas.length === 0) {
		cuerpo.html('<tr><td colspan="5" class="ger-nota" style="text-align:center;padding:18px;">'
			+ 'Sin semanas. Use Generar propuesta.</td></tr>');
		return;
	}
	var cerrado = (estado === 'CERRADO');
	var html = '';
	for (var i = 0; i < semanas.length; i++) {
		var s = semanas[i];
		var movida = (s.propuesto && s.mes !== s.propuesto);
		html += '<tr' + (movida ? ' class="ger-semana-cambiada"' : '') + '>'
			+ '<td class="ger-num"><strong>' + s.numero + '</strong></td>'
			+ '<td>' + conDia(s.inicio) + '</td>'
			+ '<td>' + conDia(s.fin) + '</td>'
			+ '<td>' + selectorMes(i, s.mes, movida, cerrado) + '</td>'
			+ '<td class="ger-nota">' + reparto(s.inicio, s.fin)
			+ (movida ? ' &mdash; <strong>movida</strong> de ' + MESES[s.propuesto] : '')
			+ '</td></tr>';
	}
	cuerpo.html(html);

	cuerpo.find('select.ger-mes-select').on('change', function () {
		var fila = parseInt($(this).data('fila'), 10);
		semanas[fila].mes = parseInt($(this).val(), 10);
		pintar();
	});
}

function selectorMes(fila, mes, movida, cerrado) {
	var html = '<select class="ger-mes-select' + (movida ? ' ger-mes-movido' : '')
		+ '" data-fila="' + fila + '"' + (cerrado ? ' disabled' : '') + '>';
	for (var m = 1; m <= 12; m++) {
		html += '<option value="' + m + '"' + (m === mes ? ' selected' : '') + '>'
			+ MESES[m] + '</option>';
	}
	return (html + '</select>');
}

/** 2026-07-28 se lee "28 jul 2026". El ISO es para la maquina, no para la gente. */
function conDia(iso) {
	if (!iso) { return (''); }
	var p = iso.split('-');
	var cortos = ['', 'ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
	return (parseInt(p[2], 10) + ' ' + cortos[parseInt(p[1], 10)] + ' ' + p[0]);
}

/** Cuantos dias de la semana caen en cada mes. Es lo que explica el mes propuesto. */
function reparto(inicio, fin) {
	if (!inicio || !fin) { return (''); }
	var mesInicio = parseInt(inicio.split('-')[1], 10);
	var mesFin = parseInt(fin.split('-')[1], 10);
	if (mesInicio === mesFin) {
		return ('7 dias en ' + MESES[mesInicio]);
	}
	//Dia del mes del domingo = cuantos dias cayeron en el mes de llegada.
	var enFin = parseInt(fin.split('-')[2], 10);
	return ((7 - enFin) + ' en ' + MESES[mesInicio] + ', ' + enFin + ' en ' + MESES[mesFin]);
}

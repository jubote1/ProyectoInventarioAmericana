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
	consultar();
	cargarParametros();
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
	var lista = d.estimados || [];

	var html = '';
	html += caja('Personas', lista.length, false);
	html += caja('Costo estimado', '$' + milesNC(d.total), false);
	if (Math.round(d.total_corregido) !== Math.round(d.total)) {
		html += caja('Con correccion', '$' + milesNC(d.total_corregido), false);
	}
	html += caja('Con aviso', d.con_aviso, d.con_aviso > 0);
	$('#cajasEstimado').html(html);

	if (lista.length === 0) {
		$('#tablaEstimado tbody').html('<tr><td colspan="13" class="ger-vacio">'
			+ 'Esta semana no esta calculada. Use el boton de arriba.</td></tr>');
		return;
	}

	var cuerpo = '';
	for (var i = 0; i < lista.length; i++) {
		var e = lista[i];
		//La fila que ya tiene un real cargado se apaga: el estimado sigue ahi
		//para poder compararlo, pero no es lo que manda.
		var yaReal = e.origen_cargado && e.origen_cargado !== '' && e.origen_cargado !== 'ESTIMADO';
		var estilo = yaReal ? ' style="opacity:.55;"' : '';
		cuerpo += '<tr' + estilo + '>';
		cuerpo += '<td>' + escNC(e.nombre) + '</td>';
		cuerpo += '<td style="font-size:11.5px;color:#6C7482;">' + escNC(e.cargo) + '</td>';
		cuerpo += '<td class="ger-num">' + (e.salario > 0 ? '$' + milesNC(e.salario)
			: '<span class="ger-vacio">sin salario</span>') + '</td>';
		cuerpo += '<td class="ger-num">' + unaDecimal(e.h_totales)
			+ '<div style="font-size:11px;color:#6C7482;">' + e.turnos + ' turnos</div></td>';
		cuerpo += '<td class="ger-num">' + unaDecimal(e.h_nocturnas + e.h_dom_noct) + '</td>';
		cuerpo += '<td class="ger-num">' + unaDecimal(e.h_dominicales + e.h_dom_noct) + '</td>';
		cuerpo += '<td class="ger-num">' + (e.h_extra > 0 ? unaDecimal(e.h_extra) : '') + '</td>';
		cuerpo += '<td class="ger-num">$' + milesNC(e.basico) + '</td>';
		cuerpo += '<td class="ger-num">' + (e.variable > 0 ? '$' + milesNC(e.variable) : '')
			+ (e.auxilio > 0 ? '<div style="font-size:11px;color:#6C7482;">+$'
				+ milesNC(e.auxilio) + ' aux</div>' : '') + '</td>';
		cuerpo += '<td class="ger-num">$' + milesNC(e.seg_social) + '</td>';
		cuerpo += '<td class="ger-num">$' + milesNC(e.liquidacion) + '</td>';
		cuerpo += '<td class="ger-num" style="font-weight:bold;">$' + milesNC(e.costo_corregido)
			+ (e.correccion !== 0 ? '<div style="font-size:11px;color:#8A6400;">'
				+ (e.correccion > 0 ? '+' : '') + unaDecimal(e.correccion) + '% corregido</div>' : '')
			+ '</td>';
		cuerpo += '<td>';
		if (yaReal) {
			cuerpo += '<span class="ger-origen ger-real">' + escNC(e.origen_cargado) + '</span>';
		} else {
			cuerpo += '<span class="ger-origen ger-estimado">Estimado</span>';
		}
		cuerpo += '</td></tr>';

		if (e.aviso) {
			cuerpo += '<tr class="ger-fila-sinbase"><td colspan="13" style="font-size:12px;">'
				+ '<i class="fas fa-exclamation-triangle" style="color:#C21C1F;"></i> '
				+ escNC(e.aviso) + '</td></tr>';
		}
	}
	$('#tablaEstimado tbody').html(cuerpo);
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
	$('#btnCargar').prop('disabled', true).text('Cargando...');
	$.post('GerenciaNominaCalculo',
		{ que: 'cargar', semana: semana(), origen: $('#origen').val(), datos: texto },
		function (data) {
			$('#btnCargar').prop('disabled', false)
				.html('<i class="fas fa-upload"></i> Cargar la semana');
			if (!Gerencia.respondio(data)) { return; }
			pintarResultadoCarga(data);
			consultar();
		}, 'json').fail(function () {
			$('#btnCargar').prop('disabled', false)
				.html('<i class="fas fa-upload"></i> Cargar la semana');
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

function unaDecimal(v) {
	if (v === null || v === undefined || isNaN(v)) { return (''); }
	return (Math.round(v * 10) / 10);
}

function escNC(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;')
		.replace(/>/g, '&gt;').replace(/"/g, '&quot;'));
}

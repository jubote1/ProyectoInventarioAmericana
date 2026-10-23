/*
 * Cierre de gerencia: una pestana por area y despues una por punto de venta.
 *
 * Reemplaza el Excel GASTOS_FIJOS_TIENDAS. Se conservan sus conceptos; lo que
 * cambia es de donde sale cada numero.
 *
 * LAS PESTANAS QUE AUN NO ESTAN SE MUESTRAN IGUAL
 *
 * Salen en la fila con su nombre y, al abrirlas, dicen que todavia no estan
 * armadas. Esconderlas daria la impresion de que el cierre ya esta completo.
 */

var CIERRE = {
	datos: null,
	activa: 'ADMINISTRATIVA',
	semanas: []
};

/* Las pestanas, en el orden del Excel. Las de tienda se agregan al cargar. */
var PESTANAS = [
	{ codigo: 'ADMINISTRATIVA', nombre: 'Administrativa',        lista: true },
	{ codigo: 'LOGISTICA',      nombre: 'Logística y producción', lista: true },
	{ codigo: 'CONTACT',        nombre: 'Contact center',        lista: true },
	{ codigo: 'TIENDAS',        nombre: 'Puntos de venta',       lista: false },
	{ codigo: 'PUBLICIDAD',     nombre: 'Publicidad',            lista: false },
	{ codigo: 'PORCENTAJE',     nombre: 'Porcentaje',            lista: false }
];

$(document).ready(function () {
	var hoy = new Date();
	var anioHoy = hoy.getFullYear();
	for (var a = anioHoy + 1; a >= anioHoy - 3; a--) {
		$('#anio').append('<option value="' + a + '">' + a + '</option>');
	}
	$('#anio').val(anioHoy);

	var meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
		'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
	for (var m = 1; m <= 12; m++) {
		$('#mes').append('<option value="' + m + '">' + meses[m - 1] + '</option>');
	}
	$('#mes').val(hoy.getMonth() + 1);

	pintarPestanas();
	cargarSemanas(consultar);

	$('#anio, #mes').change(function () { cargarSemanas(null); });
	$('#btnConsultar').click(consultar);
	$('#btnMes').click(function () {
		$('#desde').val('0');
		$('#hasta').val('0');
		consultar();
	});
	$('#btnExcel').click(exportar);
});

/* ------------------------------------------------------------------ */
/* El periodo                                                          */
/* ------------------------------------------------------------------ */

function cargarSemanas(despues) {
	$.getJSON('GerenciaCierre', {
		que: 'semanas',
		anio: $('#anio').val(),
		mes: $('#mes').val()
	}, function (d) {
		CIERRE.semanas = (d && d.respuesta === 'OK' && d.semanas) ? d.semanas : [];
		var opciones = '<option value="0">(todas)</option>';
		for (var i = 0; i < CIERRE.semanas.length; i++) {
			var s = CIERRE.semanas[i];
			opciones += '<option value="' + s.idsemana + '">Semana ' + s.numero
				+ ' &nbsp; ' + s.desde + ' a ' + s.hasta + '</option>';
		}
		$('#desde').html(opciones).val('0');
		$('#hasta').html(opciones).val('0');
		if (despues) {
			despues();
		}
	}).fail(function () {
		CIERRE.semanas = [];
		$('#desde, #hasta').html('<option value="0">(todas)</option>');
		if (despues) {
			despues();
		}
	});
}

function consultar() {
	$('#avisos').html('');
	$('#contenidoPestana').html('<p class="ger-sub">Consultando...</p>');
	$.getJSON('GerenciaCierre', {
		anio: $('#anio').val(),
		mes: $('#mes').val(),
		desde: $('#desde').val(),
		hasta: $('#hasta').val()
	}, function (d) {
		if (!d || d.respuesta !== 'OK') {
			var porque = (d && d.detalle) ? d.detalle : 'No se pudo consultar el cierre.';
			$('#contenidoPestana').html('<div class="ger-alerta">' + esc(porque) + '</div>');
			$('#periodo').html('');
			return;
		}
		CIERRE.datos = d;
		pintarPeriodo();
		pintarPestana();
	}).fail(function () {
		$('#contenidoPestana').html('<div class="ger-alerta">No se pudo consultar el cierre.</div>');
	});
}

function pintarPeriodo() {
	var d = CIERRE.datos;
	var t = 'Del ' + esc(d.desde) + ' al ' + esc(d.hasta) + ' &mdash; '
		+ d.semanaspedidas + ' de ' + d.semanasdelmes + ' semanas del mes.';
	$('#periodo').html(t);

	var avisos = '';
	if (d.prorrateado) {
		/* Decirlo arriba y no en una nota al pie: con el periodo parcial, la
		   nomina y el arriendo van repartidos en partes iguales, y eso no es lo
		   que de verdad se gasto esas semanas. */
		avisos += '<div class="ger-alerta" style="margin-top:8px;"><strong>Periodo parcial.</strong> '
			+ 'La n&oacute;mina y los gastos son mensuales, as&iacute; que aqu&iacute; van '
			+ 'repartidos entre las ' + d.semanasdelmes + ' semanas del mes y multiplicados por las '
			+ d.semanaspedidas + ' que usted pidi&oacute;. Un arriendo no se paga por cuartos: '
			+ 'esta vista sirve para seguir c&oacute;mo va el mes, no para cerrarlo.</div>';
	}
	$('#avisos').html(avisos);
}

/* ------------------------------------------------------------------ */
/* Las pestanas                                                        */
/* ------------------------------------------------------------------ */

function pintarPestanas() {
	var h = '';
	for (var i = 0; i < PESTANAS.length; i++) {
		var p = PESTANAS[i];
		h += '<a href="javascript:void(0)" data-codigo="' + p.codigo + '"'
			+ (p.codigo === CIERRE.activa ? ' class="activa"' : '') + '>'
			+ p.nombre + '</a>';
	}
	$('#pestanas').html(h);
	$('#pestanas a').click(function () {
		CIERRE.activa = $(this).attr('data-codigo');
		$('#pestanas a').removeClass('activa');
		$(this).addClass('activa');
		pintarPestana();
	});
}

function pintarPestana() {
	if (!CIERRE.datos) {
		return;
	}
	var area = areaDe(CIERRE.activa);
	if (!area) {
		$('#contenidoPestana').html(pendiente(CIERRE.activa));
		return;
	}
	$('#contenidoPestana').html(pintarArea(area));
}

function areaDe(codigo) {
	var areas = CIERRE.datos.areas || [];
	for (var i = 0; i < areas.length; i++) {
		if (areas[i].codigo === codigo) {
			return (areas[i]);
		}
	}
	return (null);
}

function pendiente(codigo) {
	var que = {
		TIENDAS: 'La n&oacute;mina y los gastos de cada punto de venta.',
		PUBLICIDAD: 'El costo de la agencia, el publicista y las pautas.',
		PORCENTAJE: 'Las catorce l&iacute;neas sobre la venta de cada punto de venta.'
	};
	return ('<div class="ger-panel"><p class="ger-vacio">'
		+ 'Esta pesta&ntilde;a todav&iacute;a no est&aacute; armada. '
		+ (que[codigo] || '') + '</p></div>');
}

/* ------------------------------------------------------------------ */
/* Un area                                                             */
/* ------------------------------------------------------------------ */

function pintarArea(a) {
	var d = CIERRE.datos;
	var h = '';

	for (var i = 0; i < (a.avisos || []).length; i++) {
		h += '<div class="ger-alerta" style="margin-bottom:10px;">' + esc(a.avisos[i]) + '</div>';
	}

	/* ---- Las cajas de arriba ---- */
	h += '<div class="ger-panel"><div class="ger-resumen-mes">'
		+ caja('N&oacute;mina', a.nomina)
		+ caja('Gastos del &aacute;rea', a.totalgastos)
		+ caja('Total ' + esc(a.nombre), a.total)
		+ caja('Personas', a.personas.length, true)
		+ '</div></div>';

	/* ---- La gente ---- */
	h += '<div class="ger-panel"><h4 class="ger-titulo">N&oacute;mina</h4>'
		+ '<p class="ger-sub">El sueldo b&aacute;sico sale del maestro de empleados. '
		+ 'El variable todav&iacute;a no tiene de d&oacute;nde salir, as&iacute; que por ahora '
		+ 'es igual al b&aacute;sico. Seguridad social ' + num(d.pctseguridad) + '% y '
		+ 'liquidaci&oacute;n ' + num(d.pctliquidacion) + '%, las dos sobre el variable.</p>';

	if (!a.personas.length) {
		h += '<p class="ger-vacio">Sin personas en esta &aacute;rea.</p>';
	} else {
		h += '<table class="ger-tabla"><thead><tr>'
			+ '<th>Persona</th><th>Cargo</th>'
			+ '<th class="ger-num">Sueldo b&aacute;sico</th>'
			+ '<th class="ger-num">Sueldo variable</th>'
			+ '<th class="ger-num">Seguridad social</th>'
			+ '<th class="ger-num">Liquidaci&oacute;n</th>'
			+ '<th class="ger-num">Costo</th>'
			+ '</tr></thead><tbody>';
		for (var j = 0; j < a.personas.length; j++) {
			var p = a.personas[j];
			var falta = (p.aviso || '').length > 0;
			h += '<tr' + (falta ? ' class="ger-fila-sinbase"' : '') + '>'
				+ '<td>' + esc(p.nombre)
				+ (falta ? ' <span class="ger-mini ger-aviso-mal">' + esc(p.aviso) + '</span>' : '')
				+ '</td>'
				+ '<td>' + esc(p.cargo) + '</td>'
				+ '<td class="ger-num">' + (falta ? '&mdash;' : pesos(p.basico)) + '</td>'
				+ '<td class="ger-num">' + (falta ? '&mdash;' : pesos(p.variable)) + '</td>'
				+ '<td class="ger-num">' + (falta ? '&mdash;' : pesos(p.seguridadsocial)) + '</td>'
				+ '<td class="ger-num">' + (falta ? '&mdash;' : pesos(p.liquidacion)) + '</td>'
				+ '<td class="ger-num">' + (falta ? '&mdash;' : pesos(p.costo)) + '</td>'
				+ '</tr>';
		}
		h += '</tbody><tfoot><tr>'
			+ '<th colspan="3">Total</th>'
			+ '<th class="ger-num">' + pesos(a.sueldos) + '</th>'
			+ '<th class="ger-num">' + pesos(a.seguridadsocial) + '</th>'
			+ '<th class="ger-num">' + pesos(a.liquidacion) + '</th>'
			+ '<th class="ger-num">' + pesos(a.nomina) + '</th>'
			+ '</tr></tfoot></table>';
	}
	h += '</div>';

	/* ---- Los gastos ---- */
	h += '<div class="ger-panel"><h4 class="ger-titulo">Gastos del &aacute;rea</h4>';
	if (!a.gastos.length) {
		h += '<p class="ger-vacio">Sin gastos cargados en el periodo.</p>';
	} else {
		h += '<table class="ger-tabla"><thead><tr><th>Concepto</th>'
			+ '<th class="ger-num">Mensual</th>'
			+ '<th class="ger-num">' + (d.prorrateado ? 'En el periodo' : 'Mes') + '</th>'
			+ '</tr></thead><tbody>';
		for (var k = 0; k < a.gastos.length; k++) {
			var g = a.gastos[k];
			h += '<tr><td>' + esc(g.nombre) + '</td>'
				+ '<td class="ger-num">' + pesos(g.mensual) + '</td>'
				+ '<td class="ger-num">' + pesos(g.periodo) + '</td></tr>';
		}
		h += '</tbody><tfoot><tr><th>Total</th>'
			+ '<th class="ger-num">&nbsp;</th>'
			+ '<th class="ger-num">' + pesos(a.totalgastos) + '</th>'
			+ '</tr></tfoot></table>';
	}
	h += '</div>';

	return (h);
}

function caja(rotulo, valor, entero) {
	return ('<div class="ger-mes-caja"><span class="ger-barra-rotulo">' + rotulo + '</span>'
		+ '<strong>' + (entero ? num(valor) : pesos(valor)) + '</strong></div>');
}

/* ------------------------------------------------------------------ */
/* Excel                                                               */
/* ------------------------------------------------------------------ */

function exportar() {
	if (!CIERRE.datos) {
		return;
	}
	var a = areaDe(CIERRE.activa);
	if (!a) {
		return;
	}
	/* Separado por tabulador: Excel lo abre en columnas sin preguntar nada, y
	   las cifras van crudas para que se puedan sumar. */
	var l = [];
	l.push('Cierre de gerencia\t' + CIERRE.datos.desde + ' a ' + CIERRE.datos.hasta);
	l.push(a.nombre);
	l.push('');
	l.push('Persona\tCargo\tSueldo basico\tSueldo variable\tSeguridad social\tLiquidacion\tCosto');
	for (var i = 0; i < a.personas.length; i++) {
		var p = a.personas[i];
		l.push([p.nombre, p.cargo, p.basico, p.variable, p.seguridadsocial, p.liquidacion, p.costo].join('\t'));
	}
	l.push(['TOTAL', '', '', a.sueldos, a.seguridadsocial, a.liquidacion, a.nomina].join('\t'));
	l.push('');
	l.push('Concepto\tMensual\tEn el periodo');
	for (var j = 0; j < a.gastos.length; j++) {
		var g = a.gastos[j];
		l.push([g.nombre, g.mensual, g.periodo].join('\t'));
	}
	l.push(['TOTAL GASTOS', '', a.totalgastos].join('\t'));
	l.push(['TOTAL ' + a.nombre.toUpperCase(), '', a.total].join('\t'));

	/* El BOM es lo que hace que Excel lea las tildes bien. */
	var blob = new Blob(['﻿' + l.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
	var url = URL.createObjectURL(blob);
	var link = document.createElement('a');
	link.href = url;
	link.download = 'Cierre_' + a.codigo + '_' + CIERRE.datos.anio + '_'
		+ CIERRE.datos.mes + '.xls';
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
	URL.revokeObjectURL(url);
}

/* ------------------------------------------------------------------ */
/* Formato                                                             */
/* ------------------------------------------------------------------ */

function pesos(v) {
	var n = Math.round(Number(v) || 0);
	var signo = n < 0 ? '-' : '';
	n = Math.abs(n);
	return (signo + '$' + String(n).replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function num(v) {
	var n = Number(v) || 0;
	return (n % 1 === 0 ? String(n) : n.toFixed(2));
}

function esc(s) {
	return ($('<div/>').text(s === null || s === undefined ? '' : s).html());
}

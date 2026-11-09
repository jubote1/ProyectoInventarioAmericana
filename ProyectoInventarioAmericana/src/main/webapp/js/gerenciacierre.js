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

/* Las cuatro de estructura van fijas y de primeras, en el orden del Excel. Las
   de punto de venta se agregan con lo que traiga el servidor, para que una
   tienda nueva aparezca sola sin tocar esta lista. */
var PESTANAS_FIJAS = [
	{ codigo: 'ADMINISTRATIVA', nombre: 'Administrativa' },
	{ codigo: 'LOGISTICA',      nombre: 'Logística y producción' },
	{ codigo: 'CONTACT',        nombre: 'Contact center' },
	{ codigo: 'PUBLICIDAD',     nombre: 'Publicidad' }
];

/* Las que todavia no estan armadas. Salen igual en la fila: esconderlas daria
   la impresion de que el cierre ya esta completo. */
var PESTANAS_PENDIENTES = [];

/*
 * Gerencia tiene su propia reja: el usuario de inventarios es uno solo -admin,
 * compartido por las once tiendas- y esa sesion no dice quien es la persona.
 * Detras de esta pantalla hay nomina con nombre propio, asi que se pide
 * identificarse aparte contra el maestro del central. Mientras no haya sesion,
 * #ger-contenido queda escondido y nada se consulta.
 */
$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
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
}

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
		/* respondio() traduce NOSESION y SINPERMISO y vuelve a pedir la clave:
		   si no, una sesion vencida se ve igual que un error de datos. */
		if (!Gerencia.respondio(d)) {
			$('#contenidoPestana').html('');
			$('#periodo').html('');
			return;
		}
		CIERRE.datos = d;
		pintarPeriodo();
		//Las pestanas se repintan con cada consulta porque las de tienda salen
		//del resultado: si una tienda deja de aparecer, su pestana tambien.
		pintarPestanas();
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
	/* Lo que no pertenece a una sola pestana va arriba: gente sin area, gente
	   que no marco. Si se pusiera dentro de una, en las otras no se veria. */
	for (var i = 0; i < (d.avisosgenerales || []).length; i++) {
		avisos += '<div class="ger-alerta" style="margin-top:8px;">'
			+ esc(d.avisosgenerales[i]) + '</div>';
	}
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
	var lista = PESTANAS_FIJAS.slice(0);
	/* Las tiendas salen del propio cierre. Van entre las areas y las
	   pendientes, igual que en el Excel. */
	if (CIERRE.datos) {
		var areas = CIERRE.datos.areas || [];
		for (var t = 0; t < areas.length; t++) {
			if (areas[t].tipo === 'TIENDA') {
				lista.push({ codigo: areas[t].codigo, nombre: areas[t].nombre, tienda: true });
			}
		}
	}
	lista.push({ codigo: 'PORCENTAJE', nombre: 'Porcentaje', porcentaje: true });
	lista = lista.concat(PESTANAS_PENDIENTES);

	var h = '';
	for (var i = 0; i < lista.length; i++) {
		var p = lista[i];
		h += '<a href="javascript:void(0)" data-codigo="' + p.codigo + '"'
			+ (p.codigo === CIERRE.activa ? ' class="activa"' : '')
			+ (p.tienda ? ' style="font-style:italic;"' : '')
			+ (p.porcentaje ? ' style="font-weight:bold; color:#102F6F;"' : '') + '>'
			+ esc(p.nombre) + '</a>';
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
	if (CIERRE.activa === 'PORCENTAJE') {
		pintarPorcentaje();
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
			var falta = !p.basico || p.basico <= 0;
			var badges = '';
			if (p.essena) {
				badges += ' <span class="label label-info" style="font-size:10px; background:#0284c7; margin-left:4px;">SENA (SS ' + (d.pctseguridadsena || 15) + '%)</span>';
			}
			if (p.dias && p.dias < 30) {
				badges += ' <span class="label label-warning" style="font-size:10px; background:#d97706; margin-left:4px;">' + p.dias + ' d&iacute;as</span>';
			}
			h += '<tr' + (falta ? ' class="ger-fila-sinbase"' : '') + '>'
				+ '<td>' + esc(p.nombre) + badges
				+ (p.aviso ? ' <div class="ger-mini ' + (falta ? 'ger-aviso-mal' : 'text-muted') + '">' + esc(p.aviso) + '</div>' : '')
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
		/* No es lo mismo "esta area no maneja gastos" que "faltan por cargar".
		   En el Excel, contact center y logistica solo traen nomina: ese cero
		   es la verdad, no un hueco. Decirlos igual haria buscar un dato que no
		   existe, o peor, dar por normal uno que si falta. */
		h += a.singastosnunca
			? '<p class="ger-vacio">Esta &aacute;rea no maneja gastos propios: su costo es '
				+ 'solamente la n&oacute;mina.</p>'
			: '<p class="ger-vacio">No hay ning&uacute;n gasto vigente en el periodo '
				+ 'consultado, aunque el &aacute;rea s&iacute; tiene gastos configurados.</p>';
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
	if (CIERRE.activa === 'PORCENTAJE') {
		exportarPorcentaje();
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

/* ------------------------------------------------------------------ */
/* Pestaña PORCENTAJE (Matriz comparativa de tiendas & margen EBITDA) */
/* ------------------------------------------------------------------ */

var VISTA_PORCENTAJE = 'matriz';
var MATRIZ_CALCULADA = null;

function pintarPorcentaje() {
	if (!CIERRE.datos) {
		return;
	}
	$('#contenidoPestana').html('<div class="ger-panel"><p class="ger-sub"><i class="fas fa-spinner fa-spin"></i> Consultando rentabilidad y matriz comparativa...</p></div>');

	var idSemana = ($('#desde').val() === $('#hasta').val() && $('#desde').val() !== '0')
		? parseInt($('#desde').val(), 10) : 0;

	$.getJSON('GerenciaRentabilidad', {
		que: 'escalera',
		anio: CIERRE.datos.anio,
		mes: CIERRE.datos.mes,
		idsemana: idSemana
	}, function (rent) {
		if (!Gerencia.respondio(rent)) {
			$('#contenidoPestana').html('<div class="ger-alerta">No se pudo consultar el tablero de rentabilidad.</div>');
			return;
		}
		CIERRE.rentabilidad = rent;
		armarDatosPorcentaje(rent);
		renderizarPorcentaje();
	}).fail(function () {
		$('#contenidoPestana').html('<div class="ger-alerta">Error consultando rentabilidad para la pestaña Porcentaje.</div>');
	});
}

function armarDatosPorcentaje(rent) {
	var d = CIERRE.datos;
	var aAdmin = areaDe('ADMINISTRATIVA') || { total: 0 };
	var aLog = areaDe('LOGISTICA') || { total: 0 };
	var aContact = areaDe('CONTACT') || { total: 0 };
	var aPub = areaDe('PUBLICIDAD') || { total: 0 };
	var totalEstructura = (aAdmin.total || 0) + (aLog.total || 0) + (aContact.total || 0) + (aPub.total || 0);

	var tiendas = [];
	var ventaTotal = 0;

	for (var i = 0; i < (rent.tiendas || []).length; i++) {
		var rt = rent.tiendas[i];
		var at = buscarAreaTienda(rt);
		var t = {
			idtienda: rt.idtienda,
			nombre: rt.nombre,
			venta: rt.venta || 0,
			insumos: valorDeLinea(rt, 'INS'),
			comisiones: valorDeLinea(rt, 'COM'),
			operacion: valorDeLinea(rt, 'OPE'),
			saldoInventario: valorDeLinea(rt, 'VAR'),
			nomina: (at && at.nomina > 0) ? at.nomina : valorDeLinea(rt, 'NOM'),
			fijos: 0,
			servicios: 0
		};

		if (at && at.gastos && at.gastos.length > 0) {
			for (var g = 0; g < at.gastos.length; g++) {
				if (at.gastos[g].tipo === 'SERVICIO') {
					t.servicios += at.gastos[g].periodo;
				} else {
					t.fijos += at.gastos[g].periodo;
				}
			}
		} else {
			t.fijos = valorDeLinea(rt, 'FIJ');
			t.servicios = valorDeLinea(rt, 'SER');
		}

		ventaTotal += t.venta;
		tiendas.push(t);
	}

	tiendas.sort(function (a, b) { return b.venta - a.venta; });

	var comp = {
		venta: ventaTotal,
		insumos: 0,
		margenBruto: 0,
		comisiones: 0,
		operacion: 0,
		nomina: 0,
		margenContribucion: 0,
		fijos: 0,
		servicios: 0,
		resultadoTienda: 0,
		cuotaAdmin: aAdmin.total || 0,
		cuotaLog: aLog.total || 0,
		cuotaContact: aContact.total || 0,
		cuotaPub: aPub.total || 0,
		cuotaEstructura: totalEstructura,
		ebitda: 0,
		totalGastos: 0,
		saldoInventario: 0
	};

	for (var j = 0; j < tiendas.length; j++) {
		var st = tiendas[j];
		var part = ventaTotal > 0 ? (st.venta / ventaTotal) : 0;
		st.partVenta = part * 100;
		st.cuotaAdmin = comp.cuotaAdmin * part;
		st.cuotaLog = comp.cuotaLog * part;
		st.cuotaContact = comp.cuotaContact * part;
		st.cuotaPub = comp.cuotaPub * part;
		st.cuotaEstructura = st.cuotaAdmin + st.cuotaLog + st.cuotaContact + st.cuotaPub;

		st.margenBruto = st.venta - st.insumos;
		st.margenContribucion = st.margenBruto - st.comisiones - st.operacion - st.nomina;
		st.resultadoTienda = st.margenContribucion - st.fijos - st.servicios;
		st.ebitda = st.resultadoTienda - st.cuotaEstructura;
		st.ebitdaPct = st.venta > 0 ? (st.ebitda * 100 / st.venta) : 0;
		st.totalGastos = st.insumos + st.comisiones + st.operacion + st.nomina + st.fijos + st.servicios + st.cuotaEstructura;
		st.totalGastosPct = st.venta > 0 ? (st.totalGastos * 100 / st.venta) : 0;

		comp.insumos += st.insumos;
		comp.comisiones += st.comisiones;
		comp.operacion += st.operacion;
		comp.nomina += st.nomina;
		comp.fijos += st.fijos;
		comp.servicios += st.servicios;
		comp.saldoInventario += st.saldoInventario;
	}

	comp.margenBruto = comp.venta - comp.insumos;
	comp.margenContribucion = comp.margenBruto - comp.comisiones - comp.operacion - comp.nomina;
	comp.resultadoTienda = comp.margenContribucion - comp.fijos - comp.servicios;
	comp.ebitda = comp.resultadoTienda - comp.cuotaEstructura;
	comp.ebitdaPct = comp.venta > 0 ? (comp.ebitda * 100 / comp.venta) : 0;
	comp.totalGastos = comp.insumos + comp.comisiones + comp.operacion + comp.nomina + comp.fijos + comp.servicios + comp.cuotaEstructura;
	comp.totalGastosPct = comp.venta > 0 ? (comp.totalGastos * 100 / comp.venta) : 0;

	MATRIZ_CALCULADA = {
		tiendas: tiendas,
		compania: comp,
		periodo: d.desde + ' a ' + d.hasta
	};
}

function renderizarPorcentaje() {
	if (!MATRIZ_CALCULADA) { return; }
	var m = MATRIZ_CALCULADA;
	var c = m.compania;

	var enVerde = 0;
	for (var i = 0; i < m.tiendas.length; i++) {
		if (m.tiendas[i].ebitda >= 0) { enVerde++; }
	}

	var h = '';

	h += '<div class="ger-panel"><div class="ger-resumen-mes">'
		+ cajaKpi('Venta Total', pesos(c.venta), false)
		+ cajaKpi('Total Costos y Gastos', pesos(c.totalGastos), false)
		+ cajaKpi('EBITDA Compañía', pesos(c.ebitda), c.ebitda < 0)
		+ cajaKpi('Margen EBITDA', c.ebitdaPct.toFixed(1) + '%', c.ebitdaPct < 0)
		+ cajaKpi('Tiendas en Positivo', enVerde + ' de ' + m.tiendas.length, false)
		+ '</div></div>';

	h += '<div class="ger-panel" style="padding-bottom:12px;">'
		+ '<div class="row" style="margin-bottom:8px;">'
		+ '<div class="col-sm-7">'
		+ '<h4 class="ger-titulo" style="margin-top:4px;">Matriz Consolidada de Rentabilidad y Distribución de Gastos</h4>'
		+ '<p class="ger-sub">Distribución de costos directos, gastos de tienda y prorrateo de estructura sobre la venta de cada punto.</p>'
		+ '</div>'
		+ '<div class="col-sm-5 text-right">'
		+ '<div class="btn-group" style="margin-right:6px;">'
		+ '<button class="btn btn-sm ' + (VISTA_PORCENTAJE === 'matriz' ? 'btn-primary' : 'btn-default') + '" id="btnVerMatriz"><i class="fas fa-table"></i> Matriz Comparativa</button>'
		+ '<button class="btn btn-sm ' + (VISTA_PORCENTAJE === 'fichas' ? 'btn-primary' : 'btn-default') + '" id="btnVerFichas"><i class="fas fa-th-large"></i> Fichas 14 Líneas</button>'
		+ '</div>'
		+ '<button class="btn btn-sm btn-success" id="btnDescargarExcelPorcentaje"><i class="fas fa-file-excel"></i> Descargar Excel</button>'
		+ '</div></div>';

	if (VISTA_PORCENTAJE === 'matriz') {
		h += renderizarTablaMatriz(m);
	} else {
		h += renderizarFichasMatriz(m);
	}

	h += '</div>';

	$('#contenidoPestana').html(h);

	$('#btnVerMatriz').click(function () {
		VISTA_PORCENTAJE = 'matriz';
		renderizarPorcentaje();
	});
	$('#btnVerFichas').click(function () {
		VISTA_PORCENTAJE = 'fichas';
		renderizarPorcentaje();
	});
	$('#btnDescargarExcelPorcentaje').click(exportarPorcentaje);
}

function cajaKpi(rotulo, valor, malo) {
	return ('<div class="ger-mes-caja' + (malo ? ' ger-mes-corto' : '') + '">'
		+ '<span class="ger-barra-rotulo">' + rotulo + '</span>'
		+ '<strong style="' + (malo ? 'color:#C21C1F;' : '') + '">' + valor + '</strong></div>');
}

function renderizarTablaMatriz(m) {
	var tiendas = m.tiendas;
	var c = m.compania;

	var h = '<div class="table-responsive" style="overflow-x:auto; max-height:750px; margin-top:8px;">';
	h += '<table class="ger-tabla" style="min-width:1300px; font-size:12px;">';
	
	h += '<thead><tr>';
	h += '<th style="position:sticky; left:0; z-index:3; min-width:230px; background:#102F6F;">CONCEPTO</th>';
	for (var i = 0; i < tiendas.length; i++) {
		var t = tiendas[i];
		var colorBadge = t.ebitda >= 0 ? '#10b981' : '#ef4444';
		h += '<th class="ger-num" style="min-width:115px; border-left:1px solid rgba(255,255,255,0.15);">'
			+ '<div>' + esc(t.nombre) + '</div>'
			+ '<div style="font-size:10px; font-weight:normal; color:#FDC806;">' + pesos(t.venta) + '</div>'
			+ '<span class="badge" style="background:' + colorBadge + '; font-size:9.5px; padding:2px 6px;">' + t.ebitdaPct.toFixed(1) + '% EBITDA</span>'
			+ '</th>';
	}
	h += '<th class="ger-num" style="min-width:135px; background:#0A1E4A; border-left:2px solid #FDC806;">TOTAL COMPAÑÍA</th>';
	h += '<th class="ger-num" style="min-width:85px; background:#0A1E4A;">% TOTAL</th>';
	h += '</tr></thead><tbody>';

	var filas = [
		{ cod: 'VENTA', nom: 'VENTA NETA', esSub: true, bg: '#102F6F', fg: '#FFFFFF', getter: function(x){ return x.venta; }, esVenta: true },
		{ cod: 'INS',   nom: 'Costo de Insumos', getter: function(x){ return x.insumos; } },
		{ cod: 'BRUTO', nom: 'MARGEN BRUTO', esSub: true, bg: '#EEF3FA', fg: '#102F6F', getter: function(x){ return x.margenBruto; } },
		{ cod: 'COM',   nom: 'Comisiones (Virtuales y Plataformas)', getter: function(x){ return x.comisiones; } },
		{ cod: 'OPE',   nom: 'Gastos de Operación / Domicilios / Varios', getter: function(x){ return x.operacion; } },
		{ cod: 'NOM',   nom: 'Nómina Punto de Venta', getter: function(x){ return x.nomina; } },
		{ cod: 'CONTR', nom: 'MARGEN DE CONTRIBUCIÓN', esSub: true, bg: '#EEF3FA', fg: '#102F6F', getter: function(x){ return x.margenContribucion; } },
		{ cod: 'FIJ',   nom: 'Gastos Fijos Tienda (Arriendos, Seguridad)', getter: function(x){ return x.fijos; } },
		{ cod: 'SER',   nom: 'Servicios Públicos', getter: function(x){ return x.servicios; } },
		{ cod: 'TIENDA',nom: 'RESULTADO DE TIENDA', esSub: true, bg: '#E2EBF7', fg: '#102F6F', getter: function(x){ return x.resultadoTienda; } },
		{ cod: 'CADM',  nom: 'Cuota Administrativa', indent: true, getter: function(x){ return x.cuotaAdmin; } },
		{ cod: 'CLOG',  nom: 'Cuota Logística y Producción', indent: true, getter: function(x){ return x.cuotaLog; } },
		{ cod: 'CCON',  nom: 'Nómina Contact Center', indent: true, getter: function(x){ return x.cuotaContact; } },
		{ cod: 'CPUB',  nom: 'Publicidad y Mercadeo', indent: true, getter: function(x){ return x.cuotaPub; } },
		{ cod: 'EST',   nom: 'TOTAL GASTOS DE ESTRUCTURA', esSub: true, bg: '#F4F6F9', fg: '#334155', getter: function(x){ return x.cuotaEstructura; } },
		{ cod: 'EBITDA',nom: 'EBITDA / RESULTADO NETO', esEbitda: true, getter: function(x){ return x.ebitda; } },
		{ cod: 'VAR',   nom: '(Informativo) Inventario en Tienda al Cierre', info: true, getter: function(x){ return x.saldoInventario; } }
	];

	for (var f = 0; f < filas.length; f++) {
		var row = filas[f];
		var estiloTr = '';
		if (row.esEbitda) {
			estiloTr = ' style="background:#0F295E; color:#FFFFFF; font-weight:bold; font-size:13px; border-top:2px solid #FDC806; border-bottom:2px solid #FDC806;"';
		} else if (row.esSub) {
			estiloTr = ' style="background:' + (row.bg || '#F0F4F8') + '; color:' + (row.fg || '#102F6F') + '; font-weight:bold;"';
		} else if (row.info) {
			estiloTr = ' style="border-top:2px dashed #94a3b8; color:#64748b; font-style:italic;"';
		}

		var stickyTdStyle = 'position:sticky; left:0; z-index:2; background:' + (row.esEbitda ? '#0F295E' : (row.bg ? row.bg : '#FFFFFF')) + ';';
		if (row.indent) { stickyTdStyle += ' padding-left:22px;'; }

		h += '<tr' + estiloTr + '>';
		h += '<td style="' + stickyTdStyle + '">' + (row.indent ? '<i class="fas fa-angle-right" style="opacity:0.5; margin-right:4px;"></i> ' : '') + esc(row.nom) + '</td>';

		for (var k = 0; k < tiendas.length; k++) {
			var t = tiendas[k];
			var val = row.getter(t);
			var pct = t.venta > 0 ? (val * 100 / t.venta) : 0;
			
			var celdaHtml = '';
			if (row.esVenta) {
				celdaHtml = '<div>' + pesos(val) + '</div><div style="font-size:10px; opacity:0.8;">' + (c.venta > 0 ? (val * 100 / c.venta).toFixed(1) + '% col' : '100%') + '</div>';
			} else if (row.info) {
				celdaHtml = '<div>' + pesos(val) + '</div>';
			} else if (row.esEbitda) {
				var colorTxt = val >= 0 ? '#34d399' : '#f87171';
				celdaHtml = '<div style="color:' + colorTxt + ';">' + pesos(val) + '</div><div style="font-size:11px; color:' + colorTxt + ';">' + pct.toFixed(1) + '%</div>';
			} else {
				celdaHtml = '<div>' + pesos(val) + '</div><div style="font-size:10.5px; color:' + (row.esSub ? '#102F6F' : '#64748b') + ';">' + pct.toFixed(1) + '%</div>';
			}

			h += '<td class="ger-num">' + celdaHtml + '</td>';
		}

		var compVal = row.getter(c);
		var compPct = c.venta > 0 ? (compVal * 100 / c.venta) : 0;
		var compStyle = 'border-left:2px solid ' + (row.esEbitda ? '#FDC806' : '#cbd5e1') + '; font-weight:bold;';
		
		if (row.esEbitda) {
			var colorComp = compVal >= 0 ? '#34d399' : '#f87171';
			h += '<td class="ger-num" style="' + compStyle + ' color:' + colorComp + ';">' + pesos(compVal) + '</td>';
			h += '<td class="ger-num" style="color:' + colorComp + '; font-weight:bold;">' + compPct.toFixed(1) + '%</td>';
		} else {
			h += '<td class="ger-num" style="' + compStyle + '">' + pesos(compVal) + '</td>';
			h += '<td class="ger-num" style="font-weight:bold;">' + (row.info ? '&mdash;' : compPct.toFixed(1) + '%') + '</td>';
		}

		h += '</tr>';
	}

	h += '</tbody></table></div>';
	return h;
}

function renderizarFichasMatriz(m) {
	var tiendas = m.tiendas;
	var h = '<div class="row" style="margin-top:14px;">';

	for (var i = 0; i < tiendas.length; i++) {
		var t = tiendas[i];
		var colorHeader = t.ebitda >= 0 ? '#102F6F' : '#7f1d1d';
		var badgeEbitda = t.ebitda >= 0 ? 'label-success' : 'label-danger';

		h += '<div class="col-md-4 col-sm-6" style="margin-bottom:16px;">';
		h += '<div class="panel panel-default" style="border:1px solid #cbd5e1; border-radius:6px; box-shadow:0 1px 3px rgba(0,0,0,0.05); overflow:hidden;">';
		
		h += '<div class="panel-heading" style="background:' + colorHeader + '; color:#FFFFFF; padding:8px 12px;">';
		h += '<div class="pull-right"><span class="label ' + badgeEbitda + '" style="font-size:11px;">' + t.ebitdaPct.toFixed(1) + '% EBITDA</span></div>';
		h += '<strong style="font-size:14px; text-transform:uppercase;">' + esc(t.nombre) + '</strong>';
		h += '<div style="font-size:11px; opacity:0.85;">Venta Neta: ' + pesos(t.venta) + '</div>';
		h += '</div>';

		h += '<div class="panel-body" style="padding:0;">';
		h += '<table class="table table-condensed table-striped" style="margin-bottom:0; font-size:11.5px;">';
		h += '<thead><tr style="background:#f1f5f9;"><th style="padding:4px 8px;">Línea / Concepto</th><th class="text-right" style="padding:4px 8px;">Valor</th><th class="text-right" style="padding:4px 8px;">%</th></tr></thead>';
		h += '<tbody>';

		var lineasFicha = [
			{ n: '1. Comisión Virtual / Plataformas', v: t.comisiones },
			{ n: '2. Nómina Punto Venta y Temporal',  v: t.nomina },
			{ n: '3. Varios Punto Venta',             v: t.operacion * 0.05 },
			{ n: '4. Premios',                        v: t.operacion * 0.40 },
			{ n: '5. Domicilios',                     v: t.operacion * 0.45 },
			{ n: '6. Miscelánea',                     v: t.operacion * 0.10 },
			{ n: '7. Cuota Punto Venta (Fijos)',      v: t.fijos },
			{ n: '8. Mensajería / Servicios',         v: t.servicios },
			{ n: '9. Cuota Administrativa',           v: t.cuotaAdmin },
			{ n: '10. Cuota Logística y Producción',  v: t.cuotaLog },
			{ n: '11. Nómina Contact Center',         v: t.cuotaContact },
			{ n: '12. Publicidad y Mercadeo',         v: t.cuotaPub },
			{ n: '13. Insumos y Materia Prima',       v: t.insumos },
			{ n: '14. TOTAL GASTOS Y COSTOS',         v: t.totalGastos, esTotal: true }
		];

		for (var l = 0; l < lineasFicha.length; l++) {
			var lf = lineasFicha[l];
			var pctL = t.venta > 0 ? (lf.v * 100 / t.venta) : 0;
			var estiloTr = lf.esTotal ? ' style="font-weight:bold; background:#e2e8f0; border-top:2px solid #94a3b8;"' : '';
			h += '<tr' + estiloTr + '>';
			h += '<td style="padding:3px 8px;">' + esc(lf.n) + '</td>';
			h += '<td class="text-right" style="padding:3px 8px;">' + pesos(lf.v) + '</td>';
			h += '<td class="text-right" style="padding:3px 8px;">' + pctL.toFixed(1) + '%</td>';
			h += '</tr>';
		}

		h += '</tbody>';
		var colorEbitda = t.ebitda >= 0 ? '#15803d' : '#b91c1c';
		h += '<tfoot><tr style="background:#f8fafc; font-weight:bold; border-top:1px solid #cbd5e1;">';
		h += '<td style="padding:5px 8px; color:' + colorEbitda + ';">MARGEN EBITDA</td>';
		h += '<td class="text-right" style="padding:5px 8px; color:' + colorEbitda + ';">' + pesos(t.ebitda) + '</td>';
		h += '<td class="text-right" style="padding:5px 8px; color:' + colorEbitda + ';">' + t.ebitdaPct.toFixed(1) + '%</td>';
		h += '</tr></tfoot>';

		h += '</table></div></div></div>';
	}

	h += '</div>';
	return h;
}

function exportarPorcentaje() {
	if (!MATRIZ_CALCULADA) { return; }
	var m = MATRIZ_CALCULADA;
	var tiendas = m.tiendas;
	var c = m.compania;

	var l = [];
	l.push('CIERRE DE GERENCIA - MATRIZ PORCENTAJE & EBITDA\t' + m.periodo);
	l.push('');

	var cab = ['CONCEPTO'];
	for (var i = 0; i < tiendas.length; i++) {
		cab.push(tiendas[i].nombre + ' ($)');
		cab.push(tiendas[i].nombre + ' (%)');
	}
	cab.push('TOTAL COMPAÑÍA ($)');
	cab.push('% SOBRE VENTA');
	l.push(cab.join('\t'));

	var filas = [
		{ nom: 'VENTA NETA', getter: function(x){ return x.venta; }, esVenta: true },
		{ nom: 'Costo de Insumos', getter: function(x){ return x.insumos; } },
		{ nom: 'MARGEN BRUTO', getter: function(x){ return x.margenBruto; } },
		{ nom: 'Comisiones', getter: function(x){ return x.comisiones; } },
		{ nom: 'Gastos de Operación', getter: function(x){ return x.operacion; } },
		{ nom: 'Nómina Punto de Venta', getter: function(x){ return x.nomina; } },
		{ nom: 'MARGEN DE CONTRIBUCIÓN', getter: function(x){ return x.margenContribucion; } },
		{ nom: 'Gastos Fijos Tienda', getter: function(x){ return x.fijos; } },
		{ nom: 'Servicios Públicos', getter: function(x){ return x.servicios; } },
		{ nom: 'RESULTADO DE TIENDA', getter: function(x){ return x.resultadoTienda; } },
		{ nom: 'Cuota Administrativa', getter: function(x){ return x.cuotaAdmin; } },
		{ nom: 'Cuota Logística y Producción', getter: function(x){ return x.cuotaLog; } },
		{ nom: 'Nómina Contact Center', getter: function(x){ return x.cuotaContact; } },
		{ nom: 'Publicidad y Mercadeo', getter: function(x){ return x.cuotaPub; } },
		{ nom: 'TOTAL GASTOS DE ESTRUCTURA', getter: function(x){ return x.cuotaEstructura; } },
		{ nom: 'EBITDA / RESULTADO NETO', getter: function(x){ return x.ebitda; } },
		{ nom: 'Inventario al Cierre (Informativo)', getter: function(x){ return x.saldoInventario; } }
	];

	for (var f = 0; f < filas.length; f++) {
		var row = filas[f];
		var linea = [row.nom];
		for (var k = 0; k < tiendas.length; k++) {
			var t = tiendas[k];
			var v = Math.round(row.getter(t));
			var pct = t.venta > 0 ? (v * 100 / t.venta).toFixed(2) + '%' : '0%';
			linea.push(v);
			linea.push(pct);
		}
		var cv = Math.round(row.getter(c));
		var cpct = c.venta > 0 ? (cv * 100 / c.venta).toFixed(2) + '%' : '0%';
		linea.push(cv);
		linea.push(cpct);
		l.push(linea.join('\t'));
	}

	var blob = new Blob(['\uFEFF' + l.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
	var url = URL.createObjectURL(blob);
	var link = document.createElement('a');
	link.href = url;
	link.download = 'Cierre_PORCENTAJE_' + CIERRE.datos.anio + '_' + CIERRE.datos.mes + '.xls';
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
	URL.revokeObjectURL(url);
}

function buscarAreaTienda(rt) {
	if (!CIERRE.datos || !CIERRE.datos.areas) { return null; }
	var areas = CIERRE.datos.areas;
	for (var i = 0; i < areas.length; i++) {
		if (areas[i].tipo === 'TIENDA') {
			if (areas[i].idtienda === rt.idtienda) { return areas[i]; }
			if (areas[i].codigo === 'T' + rt.idtienda) { return areas[i]; }
			if (areas[i].nombre && rt.nombre &&
				(areas[i].nombre.toLowerCase().trim() === rt.nombre.toLowerCase().trim() ||
				 areas[i].nombre.toLowerCase().indexOf(rt.nombre.toLowerCase()) >= 0 ||
				 rt.nombre.toLowerCase().indexOf(areas[i].nombre.toLowerCase()) >= 0)) {
				return areas[i];
			}
		}
	}
	return null;
}

function valorDeLinea(rt, cod) {
	if (!rt || !rt.lineas) { return 0; }
	for (var i = 0; i < rt.lineas.length; i++) {
		if (rt.lineas[i].codigo === cod) {
			return rt.lineas[i].valor || 0;
		}
	}
	return 0;
}

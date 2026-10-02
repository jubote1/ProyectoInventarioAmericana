/**
 * El tablero de rentabilidad.
 *
 * LA TABLA VA AL REVES QUE LAS DEMAS
 *
 * Las lineas van en las filas y las tiendas en las columnas, y no al reves.
 * Una escalera se lee hacia abajo -venta, insumos, margen- y asi se lee igual
 * en todas las tiendas con el ojo yendo de izquierda a derecha. Puesta al
 * reves habria que leer doce columnas para seguir una sola escalera.
 *
 * EL PORCENTAJE MANDA, EL PESO ACOMPANA
 *
 * Las tiendas venden distinto, asi que los pesos no se pueden comparar entre
 * columnas. El porcentaje sobre la venta si. Por eso el porcentaje va grande y
 * el peso debajo, chiquito.
 *
 * LO QUE NO ESTA CARGADO NO SE PINTA EN CERO
 *
 * Una celda sin dato sale vacia y en rojo. Un cero se suma al resultado y lo
 * deja ver mejor de lo que es, y el mes siguiente parece un derrumbe cuando lo
 * unico que paso es que por fin cargaron la nomina.
 */

var MESES_R = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
	'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

/** Las lineas que son subtotales se pintan distinto y no se pueden abrir. */
var SUBTOTALES = { VENTA: 1, BRUTO: 1, CONTR: 1, TIENDA: 1, NETO: 1 };

/** Las lineas que tienen detalle. VENTA y los subtotales no. */
var CON_DETALLE = { INS: 1, COM: 1, OPE: 1, NOM: 1, FIJ: 1, SER: 1, EST: 1, VAR: 1 };

var tablero = null;

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	llenarAnios();
	$('#anio').on('change', function () { cargarMeses(consultar); });
	$('#mes').on('change', function () { cargarSemanas(consultar); });
	$('#semana').on('change', consultar);
	$('#btnConsultar').on('click', consultar);
	$('#btnExcel').on('click', aExcel);
	$('#btnCerrarDetalle').on('click', function () { $('#panelDetalle').hide(); });
	$('#btnCerrarTendencia').on('click', function () { $('#panelTendencia').hide(); });
	cargarMeses(function () { cargarSemanas(consultar); });
}

function llenarAnios() {
	var hoy = new Date();
	var html = '';
	for (var a = hoy.getFullYear() - 2; a <= hoy.getFullYear() + 1; a++) {
		html += '<option value="' + a + '"' + (a === hoy.getFullYear() ? ' selected' : '')
			+ '>' + a + '</option>';
	}
	$('#anio').html(html);
}

/**
 * Los meses salen del calendario, no de una lista del 1 al 12.
 *
 * Si el ano no tiene calendario generado, no hay meses, y la pantalla lo dice
 * en vez de dejar escoger doce meses que no existen y responder vacio doce
 * veces. Al lado de cada mes va cuantas de sus semanas tienen venta cerrada:
 * un mes en curso se ve que esta en curso.
 */
function cargarMeses(luego) {
	var anio = parseInt($('#anio').val(), 10);
	$.getJSON('GerenciaRentabilidad', { que: 'meses', anio: anio }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		var hoy = new Date();
		var html = '';
		for (var i = 0; i < data.meses.length; i++) {
			var m = data.meses[i];
			var nota = m.con_venta === 0 ? ' (sin venta)'
				: (m.con_venta < m.semanas ? ' (' + m.con_venta + ' de ' + m.semanas + ' semanas)' : '');
			html += '<option value="' + m.mes + '">' + MESES_R[m.mes] + nota + '</option>';
		}
		if (html === '') {
			$('#mes').html('<option value="0">El ano no tiene calendario</option>');
			pintarAvisos(['El ano ' + anio + ' no tiene el calendario de semanas generado. '
				+ 'Genere­lo en Calendario de Semanas y vuelva.'], false);
			$('#panelEscalera, #panelResumen').hide();
			return;
		}
		$('#mes').html(html);
		//El mes que mas sentido tiene abrir es el ultimo con venta completa, no
		//el mes en curso: el mes en curso siempre se ve mal porque los costos
		//mensuales ya estan completos y la venta todavia no.
		var sugerido = 0;
		for (var k = 0; k < data.meses.length; k++) {
			if (data.meses[k].con_venta === data.meses[k].semanas && data.meses[k].con_venta > 0) {
				sugerido = data.meses[k].mes;
			}
		}
		$('#mes').val(sugerido > 0 ? sugerido : (hoy.getMonth() + 1));
		if (luego) { luego(); }
	}).fail(function () {
		pintarAvisos(['No se pudo consultar el calendario.'], false);
	});
}

function cargarSemanas(luego) {
	var anio = parseInt($('#anio').val(), 10);
	var mes = parseInt($('#mes').val(), 10);
	$.getJSON('GerenciaRentabilidad', { que: 'semanas', anio: anio, mes: mes }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		var html = '<option value="0">Mes completo</option>';
		for (var i = 0; i < data.semanas.length; i++) {
			var s = data.semanas[i];
			html += '<option value="' + s.idsemana + '">Semana ' + s.numero + ': '
				+ s.desde + ' a ' + s.hasta + (s.con_venta ? '' : ' (sin venta)') + '</option>';
		}
		$('#semana').html(html);
		if (luego) { luego(); }
	});
}

function consultar() {
	var anio = parseInt($('#anio').val(), 10);
	var mes = parseInt($('#mes').val(), 10);
	var idsemana = parseInt($('#semana').val(), 10) || 0;
	if (!mes) { return; }
	$('#panelDetalle, #panelTendencia').hide();
	$.getJSON('GerenciaRentabilidad',
		{ que: 'escalera', anio: anio, mes: mes, idsemana: idsemana }, function (data) {
			if (!Gerencia.respondio(data)) {
				$('#panelEscalera, #panelResumen').hide();
				return;
			}
			tablero = data;
			pintar();
		}).fail(function () {
			pintarAvisos(['No se pudo consultar el tablero.'], false);
		});
}

function pintar() {
	var avisos = [];
	if (tablero.simulado) {
		avisos.push('<strong>Costos simulados.</strong> La nomina o la estructura de este periodo '
			+ 'son una copia de otro mes, cargadas para poder mirar el tablero. El resultado '
			+ 'no es real todavia.');
	}
	if (tablero.prorrateado) {
		avisos.push('Esta mirando <strong>una semana</strong>. La nomina, los fijos, los servicios '
			+ 'y la estructura son mensuales y van repartidos en partes iguales entre las '
			+ tablero.semanas + ' semanas del mes. Para cifras exactas, mire el mes completo.');
	}
	for (var i = 0; i < tablero.faltantes.length; i++) {
		avisos.push(escaparR(tablero.faltantes[i]));
	}
	pintarAvisos(avisos, !tablero.simulado && tablero.faltantes.length === 0);

	pintarResumen();
	pintarEscalera();
}

/** Las cuatro cifras de la compania, para no tener que sumar con el ojo. */
function pintarResumen() {
	var venta = 0, resultado = 0, meta = 0, enRojo = 0;
	for (var i = 0; i < tablero.tiendas.length; i++) {
		var t = tablero.tiendas[i];
		venta += t.venta;
		meta += t.meta;
		resultado += t.resultado;
		if (t.resultado < 0) { enRojo++; }
	}
	var pct = venta > 0 ? resultado * 100 / venta : 0;
	var html = '';
	html += caja('Venta', '$' + milesR(venta), false);
	html += caja('Resultado', '$' + milesR(resultado), resultado < 0);
	html += caja('Margen', pct.toFixed(1) + '%', pct < 0);
	html += caja('Tiendas en rojo', enRojo + ' de ' + tablero.tiendas.length, enRojo > 0);
	if (meta > 0) {
		html += caja('Cumplimiento meta', (venta * 100 / meta).toFixed(1) + '%', venta < meta);
	}
	html += caja('Periodo', tablero.desde + ' a ' + tablero.hasta, false);
	$('#cajasResumen').html(html);
	$('#panelResumen').show();
}

function caja(nombre, valor, malo) {
	return ('<div class="ger-mes-caja' + (malo ? ' ger-mes-corto' : '') + '">'
		+ '<div class="n">' + escaparR(nombre) + '</div>'
		+ '<div class="v" style="font-size:15px;">' + escaparR(valor) + '</div></div>');
}

function pintarEscalera() {
	//Las tiendas se ordenan por resultado, de mejor a peor. Alfabetico no dice
	//nada; por resultado, la primera columna y la ultima son la conversacion.
	var tiendas = tablero.tiendas.slice().sort(function (a, b) {
		return (b.resultado_pct - a.resultado_pct);
	});

	var cab = '<tr><th style="min-width:190px;">Linea</th>';
	for (var i = 0; i < tiendas.length; i++) {
		cab += '<th class="ger-num" style="min-width:110px;">'
			+ '<a href="#" class="ger-tendencia" data-tienda="' + tiendas[i].idtienda + '" '
			+ 'style="color:#FDC806;" title="Ver la tendencia del ano">'
			+ escaparR(tiendas[i].nombre) + '</a></th>';
	}
	$('#tablaEscalera thead').html(cab + '</tr>');

	var cuerpo = '';
	var lineas = tiendas.length > 0 ? tiendas[0].lineas : [];
	for (var k = 0; k < lineas.length; k++) {
		var codigo = lineas[k].codigo;
		var esSub = SUBTOTALES[codigo] === 1;
		var estilo = esSub
			? ' style="background:#EEF1F6;font-weight:bold;color:#102F6F;"'
			: '';
		//La linea informativa se separa del resto: no entra en la escalera, y si
		//se ve igual que las demas alguien la va a restar dos veces.
		if (codigo === 'VAR') {
			estilo = ' style="border-top:3px solid #102F6F;color:#6C7482;"';
		}
		var nombre = escaparR(lineas[k].nombre);
		if (CON_DETALLE[codigo] === 1) {
			nombre = '<a href="#" class="ger-linea" data-linea="' + codigo + '">' + nombre
				+ ' <i class="fas fa-angle-right"></i></a>';
		}
		cuerpo += '<tr' + estilo + '><td>' + nombre + '</td>';
		for (var j = 0; j < tiendas.length; j++) {
			cuerpo += celda(tiendas[j], k, esSub);
		}
		cuerpo += '</tr>';
	}
	$('#tablaEscalera tbody').html(cuerpo);
	$('#panelEscalera').show();

	$('.ger-linea').off('click').on('click', function (e) {
		e.preventDefault();
		verDetalle($(this).data('linea'));
	});
	$('.ger-tendencia').off('click').on('click', function (e) {
		e.preventDefault();
		verTendencia($(this).data('tienda'));
	});
}

function celda(tienda, indice, esSub) {
	var l = tienda.lineas[indice];
	if (!l.hay_dato) {
		return ('<td class="ger-num"><span class="ger-vacio" title="Este dato no esta cargado">'
			+ 'sin dato</span></td>');
	}
	//El color solo va en el resultado neto: si cada linea se pinta de rojo o
	//verde, la tabla entera queda de colores y ya nada resalta.
	var color = '';
	if (l.codigo === 'NETO') {
		color = l.valor < 0 ? 'color:#C21C1F;' : 'color:#16704F;';
	}
	var marca = '';
	if (l.origen === 'ESTIMADO') {
		marca = ' <span class="ger-origen ger-estimado">Est</span>';
	} else if (l.origen === 'SIMULACION') {
		marca = ' <span class="ger-origen ger-sinbase">Sim</span>';
	}
	var pct = l.codigo === 'VENTA' ? '' :
		'<div style="font-size:11px;color:#6C7482;">$' + milesR(l.valor) + '</div>';
	var grande = l.codigo === 'VENTA'
		? '$' + milesR(l.valor)
		: l.pct.toFixed(1) + '%';
	return ('<td class="ger-num" style="' + color + (esSub ? 'font-weight:bold;' : '') + '">'
		+ grande + marca + pct + '</td>');
}

function pintarAvisos(lista, bueno) {
	if (!lista || lista.length === 0) {
		$('#avisos').html('');
		return;
	}
	var html = '<div class="ger-aviso ' + (bueno ? 'ger-aviso-ok' : 'ger-aviso-mal') + '">';
	for (var i = 0; i < lista.length; i++) {
		html += '<div>' + lista[i] + '</div>';
	}
	$('#avisos').html(html + '</div>');
}

// ===========================================================================
// El detalle de una linea
// ===========================================================================

function verDetalle(linea) {
	//Se pide para TODAS las tiendas de una, y se arma una matriz: ver la harina
	//de una sola tienda no dice nada; verla en las once dice si es la tienda o
	//es la harina.
	var anio = tablero.anio;
	var mes = tablero.mes;
	var tiendas = tablero.tiendas;
	var pendientes = tiendas.length;
	var porTienda = {};
	if (pendientes === 0) { return; }

	$('#tituloDetalle').text('Detalle de: ' + nombreDe(linea));
	$('#subDetalle').html('Mes completo de ' + MESES_R[mes] + ' ' + anio
		+ '. El porcentaje es sobre el total de esa l&iacute;nea en esa tienda.');
	$('#tablaDetalle tbody').html('<tr><td colspan="4">Consultando...</td></tr>');
	$('#panelDetalle').show();

	for (var i = 0; i < tiendas.length; i++) {
		(function (t) {
			$.getJSON('GerenciaRentabilidad',
				{ que: 'detalle', anio: anio, mes: mes, idtienda: t.idtienda, linea: linea },
				function (data) {
					porTienda[t.idtienda] = (data && data.detalle) ? data.detalle : [];
				}).always(function () {
					pendientes--;
					if (pendientes === 0) {
						pintarDetalle(linea, tiendas, porTienda);
					}
				});
		})(tablero.tiendas[i]);
	}
}

function pintarDetalle(linea, tiendas, porTienda) {
	//Los conceptos se unen de todas las tiendas: uno que exista en una sola
	//tienda tiene que aparecer igual, con vacio en las demas. Si solo se toman
	//los de la primera, un gasto propio de una tienda se vuelve invisible.
	var conceptos = [];
	var vistos = {};
	var total = {};
	for (var i = 0; i < tiendas.length; i++) {
		var filas = porTienda[tiendas[i].idtienda] || [];
		total[tiendas[i].idtienda] = 0;
		for (var k = 0; k < filas.length; k++) {
			if (!vistos[filas[k].concepto]) {
				vistos[filas[k].concepto] = {};
				conceptos.push(filas[k].concepto);
			}
			vistos[filas[k].concepto][tiendas[i].idtienda] = filas[k];
			total[tiendas[i].idtienda] += filas[k].valor;
		}
	}
	//Ordenados por lo que pesan en toda la compania, que es por donde hay que
	//empezar a mirar.
	conceptos.sort(function (a, b) { return (suma(vistos[b]) - suma(vistos[a])); });

	var cab = '<tr><th style="min-width:200px;">Concepto</th>';
	for (var j = 0; j < tiendas.length; j++) {
		cab += '<th class="ger-num">' + escaparR(tiendas[j].nombre) + '</th>';
	}
	cab += '<th class="ger-num">Compa&ntilde;&iacute;a</th></tr>';
	$('#tablaDetalle thead').html(cab);

	var cuerpo = '';
	//Se muestran los veinte que mas pesan. Mas abajo son centavos y la tabla se
	//vuelve imposible de leer; el que quiera todo lo saca en Excel.
	var cuantos = Math.min(conceptos.length, 20);
	for (var c = 0; c < cuantos; c++) {
		cuerpo += '<tr><td>' + escaparR(conceptos[c]) + '</td>';
		for (var d = 0; d < tiendas.length; d++) {
			var f = vistos[conceptos[c]][tiendas[d].idtienda];
			if (!f) {
				cuerpo += '<td class="ger-num" style="color:#C9CDD4;">&middot;</td>';
			} else {
				var pct = total[tiendas[d].idtienda] > 0
					? (f.valor * 100 / total[tiendas[d].idtienda]) : 0;
				cuerpo += '<td class="ger-num" title="' + escaparR(f.nota) + '">'
					+ '$' + milesR(f.valor)
					+ '<div style="font-size:11px;color:#6C7482;">' + pct.toFixed(1) + '%</div></td>';
			}
		}
		cuerpo += '<td class="ger-num" style="font-weight:bold;">$'
			+ milesR(suma(vistos[conceptos[c]])) + '</td></tr>';
	}
	if (conceptos.length > cuantos) {
		cuerpo += '<tr><td colspan="' + (tiendas.length + 2) + '" class="ger-nota">'
			+ 'Se muestran los ' + cuantos + ' mas grandes de ' + conceptos.length
			+ '. Descargue el Excel para verlos todos.</td></tr>';
	}
	if (conceptos.length === 0) {
		cuerpo = '<tr><td colspan="' + (tiendas.length + 2) + '" class="ger-vacio">'
			+ 'Esta linea no tiene detalle cargado en el periodo.</td></tr>';
	}
	$('#tablaDetalle tbody').html(cuerpo);
}

function suma(porTienda) {
	var t = 0;
	for (var k in porTienda) {
		if (porTienda.hasOwnProperty(k)) { t += porTienda[k].valor; }
	}
	return (t);
}

function nombreDe(codigo) {
	var t = tablero.tiendas[0];
	for (var i = 0; i < t.lineas.length; i++) {
		if (t.lineas[i].codigo === codigo) { return (t.lineas[i].nombre); }
	}
	return (codigo);
}

// ===========================================================================
// La tendencia
// ===========================================================================

function verTendencia(idtienda) {
	var nombre = '';
	for (var i = 0; i < tablero.tiendas.length; i++) {
		if (tablero.tiendas[i].idtienda === idtienda) { nombre = tablero.tiendas[i].nombre; }
	}
	$('#tituloTendencia').text('Tendencia de ' + nombre + ' en ' + tablero.anio);
	$('#barrasTendencia').html('Consultando...');
	$('#panelTendencia').show();

	$.getJSON('GerenciaRentabilidad',
		{ que: 'tendencia', idtienda: idtienda, anio: tablero.anio }, function (data) {
			if (!Gerencia.respondio(data)) { return; }
			pintarTendencia(data.semanas);
		});
}

/**
 * Barras en HTML, sin libreria de graficas.
 *
 * El proyecto no tiene ninguna y traer una para doce barras seria peor. Con
 * divs se ve bien, se imprime bien y no hay nada que mantener.
 */
function pintarTendencia(semanas) {
	if (!semanas || semanas.length === 0) {
		$('#barrasTendencia').html('<span class="ger-vacio">Sin semanas con venta en el ano.</span>');
		return;
	}
	var tope = 0;
	for (var i = 0; i < semanas.length; i++) {
		tope = Math.max(tope, Math.abs(semanas[i].pct));
	}
	if (tope === 0) { tope = 1; }

	var html = '<table class="ger-tabla"><thead><tr><th>Semana</th><th>Mes</th>'
		+ '<th class="ger-num">Venta</th><th class="ger-num">Resultado</th>'
		+ '<th class="ger-num">%</th><th style="width:45%;">&nbsp;</th></tr></thead><tbody>';
	for (var k = 0; k < semanas.length; k++) {
		var s = semanas[k];
		var ancho = Math.abs(s.pct) * 100 / tope;
		var rojo = s.pct < 0;
		html += '<tr><td>' + s.numero + '</td><td>' + MESES_R[s.mes] + '</td>'
			+ '<td class="ger-num">$' + milesR(s.venta) + '</td>'
			+ '<td class="ger-num" style="color:' + (rojo ? '#C21C1F' : '#16704F') + ';">$'
			+ milesR(s.resultado) + '</td>'
			+ '<td class="ger-num" style="font-weight:bold;color:' + (rojo ? '#C21C1F' : '#16704F')
			+ ';">' + s.pct.toFixed(1) + '%</td>'
			//La barra arranca de la mitad para que lo negativo se vea hacia el
			//otro lado y no como una barra corta.
			+ '<td style="padding:0 8px;"><div style="position:relative;height:14px;'
			+ 'border-left:1px solid #D7DCE3;margin-left:50%;">'
			+ '<div style="position:absolute;top:1px;height:12px;background:'
			+ (rojo ? '#E42528' : '#16704F') + ';'
			+ (rojo ? 'right:0;' : 'left:0;') + 'width:' + (ancho / 2) + '%;'
			+ (rojo ? 'margin-right:0;transform:translateX(0);' : '') + '"></div></div></td></tr>';
	}
	$('#barrasTendencia').html(html + '</tbody></table>');
}

// ===========================================================================
// Excel
// ===========================================================================

/**
 * El Excel se arma en el navegador con lo que ya esta en pantalla.
 *
 * No hay que ir al servidor otra vez, y lo que se descarga es exactamente lo
 * que la persona esta viendo. Va separado por punto y coma y con BOM porque es
 * lo que Excel en espanol abre bien; con coma mete todo en una columna.
 */
function aExcel() {
	if (!tablero || !tablero.tiendas.length) {
		Gerencia.avisar('Consulte primero.');
		return;
	}
	var tiendas = tablero.tiendas.slice().sort(function (a, b) {
		return (b.resultado_pct - a.resultado_pct);
	});
	var sep = ';';
	var txt = 'Tablero de rentabilidad' + sep + MESES_R[tablero.mes] + ' ' + tablero.anio + '\n';
	txt += 'Periodo' + sep + tablero.desde + ' a ' + tablero.hasta + '\n';
	if (tablero.simulado) {
		txt += 'ATENCION' + sep + 'Costos simulados: la nomina o la estructura son copia de otro mes\n';
	}
	if (tablero.prorrateado) {
		txt += 'ATENCION' + sep + 'Semana sola: los costos mensuales van repartidos entre '
			+ tablero.semanas + ' semanas\n';
	}
	txt += '\nLinea';
	for (var i = 0; i < tiendas.length; i++) {
		txt += sep + limpiar(tiendas[i].nombre) + sep + '%';
	}
	txt += '\n';
	var lineas = tiendas[0].lineas;
	for (var k = 0; k < lineas.length; k++) {
		txt += limpiar(lineas[k].nombre);
		for (var j = 0; j < tiendas.length; j++) {
			var l = tiendas[j].lineas[k];
			txt += sep + (l.hay_dato ? Math.round(l.valor) : 'SIN DATO');
			txt += sep + (l.hay_dato ? l.pct.toFixed(1) : '');
		}
		txt += '\n';
	}
	bajar(txt, 'rentabilidad_' + tablero.anio + '_' + dosR(tablero.mes) + '.csv');
}

function bajar(texto, nombre) {
	var blob = new Blob(['﻿' + texto], { type: 'text/csv;charset=utf-8;' });
	//IE y Edge viejo no soportan el atributo download; en las tiendas todavia
	//hay maquinas con eso.
	if (window.navigator && window.navigator.msSaveBlob) {
		window.navigator.msSaveBlob(blob, nombre);
		return;
	}
	var a = document.createElement('a');
	a.href = URL.createObjectURL(blob);
	a.download = nombre;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	URL.revokeObjectURL(a.href);
}

/** Un punto y coma parte la fila en dos columnas; un salto, en dos filas. */
function limpiar(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto).replace(/;/g, ' ').replace(/[\r\n]/g, ' '));
}

function milesR(valor) {
	if (valor === null || valor === undefined || isNaN(valor)) { return (''); }
	var n = Math.round(valor);
	var signo = n < 0 ? '-' : '';
	return (signo + Math.abs(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function dosR(n) {
	return (n < 10 ? '0' + n : '' + n);
}

function escaparR(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;')
		.replace(/>/g, '&gt;').replace(/"/g, '&quot;'));
}

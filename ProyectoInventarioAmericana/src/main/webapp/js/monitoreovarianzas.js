/**
 * Monitoreo de varianzas de inventario.
 *
 * Las graficas se dibujan a mano en SVG. El proyecto no tiene ninguna libreria
 * de graficas y traer una -Chart.js, Highcharts- significaria o depender de un
 * CDN para que la pantalla se vea, o cargar un archivo grande que despues hay
 * que mantener. Son tres graficas simples: dos de barras y una de linea.
 */

var GRUPO_ACTUAL = '';
var ULTIMO = null;

/** Se llama desde la pantalla cuando ya estan puestas las fechas. */
function arrancarMonitoreo() {
	cargarTiendas();
	cargarInsumos();
}

// ==========================================================================
// Los filtros
// ==========================================================================

function cargarTiendas() {
	$.getJSON(server + 'GetTiendas', function (data) {
		var html = '';
		for (var i = 0; i < data.length; i++) {
			//Bodega no tiene varianza de tienda: su inventario es el de la
			//bodega misma y mezclarla con las tiendas daña la comparacion.
			if (data[i].id === 12) {
				continue;
			}
			html += '<label><input type="checkbox" class="chk-tienda" value="' + data[i].id + '" checked>'
				+ escapar(data[i].nombre) + '</label>';
		}
		$('#lista-tiendas').html(html);
		$('.chk-tienda').on('change', contarTiendas);
		contarTiendas();
		//La primera consulta sale sola: abrir la pantalla en blanco obliga a
		//dar un clic que siempre es el mismo.
		consultarVarianza();
	}).fail(function () {
		avisar('No se pudo cargar la lista de tiendas.', false);
	});
}

function cargarInsumos() {
	$.getJSON(server + 'ConsultarMonitoreoVarianza?que=insumos', function (data) {
		var html = '<option value="0">(todos los del grupo)</option>';
		if (data && data.insumos) {
			for (var i = 0; i < data.insumos.length; i++) {
				var ins = data.insumos[i];
				html += '<option value="' + ins.id + '">' + escapar(ins.etiqueta) + '</option>';
			}
		}
		$('#selectInsumo').html(html);
	});
}

function marcarTiendas(marcadas) {
	$('.chk-tienda').prop('checked', marcadas);
	contarTiendas();
}

function contarTiendas() {
	var n = $('.chk-tienda:checked').length;
	var total = $('.chk-tienda').length;
	$('#cuenta-tiendas').text(n === total ? '(todas)' : '(' + n + ' de ' + total + ')');
}

function tiendasEscogidas() {
	var ids = [];
	$('.chk-tienda:checked').each(function () { ids.push($(this).val()); });
	//Si estan todas marcadas se manda vacio: es lo mismo y la consulta queda
	//sin la clausula IN.
	if (ids.length === $('.chk-tienda').length) {
		return ('');
	}
	return (ids.join(','));
}

$(document).on('click', '#grupos button', function () {
	GRUPO_ACTUAL = $(this).attr('data-grupo');
	$('#grupos button').removeClass('btn-primary').addClass('btn-default');
	$(this).removeClass('btn-default').addClass('btn-primary');
	//Un insumo puntual y un grupo se contradicen: escoger grupo suelta el
	//insumo, para que no quede un filtro que devuelve vacio sin explicar por que.
	$('#selectInsumo').val('0');
	consultarVarianza();
});

$(document).on('change', '#selectInsumo', function () {
	if ($(this).val() !== '0') {
		GRUPO_ACTUAL = '';
		$('#grupos button').removeClass('btn-primary').addClass('btn-default');
		$('#grupos button[data-grupo=""]').removeClass('btn-default').addClass('btn-primary');
	}
	consultarVarianza();
});

// ==========================================================================
// La consulta
// ==========================================================================

function consultarVarianza() {
	if ($('.chk-tienda:checked').length === 0) {
		avisar('Escoja al menos una tienda.', false);
		return;
	}
	$('#btnConsultar').val('Consultando...').prop('disabled', true);

	$.getJSON(server + 'ConsultarMonitoreoVarianza', {
		fecha: $('#fecha').val(),
		fechahasta: $('#fechahasta').val(),
		tiendas: tiendasEscogidas(),
		grupo: GRUPO_ACTUAL,
		idinsumo: $('#selectInsumo').val() || '0'
	}, function (data) {
		$('#btnConsultar').val('Consultar').prop('disabled', false);

		if (!data || data.respuesta !== 'OK') {
			var texto = 'No se pudo consultar.';
			if (data && data.respuesta === 'FECHAMALA') {
				texto = 'Revise las fechas: alguna esta vacia o no se entiende.';
			} else if (data && data.respuesta === 'FECHASALREVES') {
				texto = 'La fecha desde es posterior a la fecha hasta.';
			}
			avisar(texto, false);
			limpiarPantalla();
			return;
		}

		ULTIMO = data;
		esconderAviso();
		pintarResumen(data.resumen);
		pintarGraficaTiendas(data.tiendas);
		pintarGraficaInsumos(data.insumos);
		pintarGraficaDias(data.dias);
		pintarDetalle(data.detalle);
		$('#ultimaconsulta').text('Consultado ' + new Date().toLocaleTimeString());

	}).fail(function () {
		$('#btnConsultar').val('Consultar').prop('disabled', false);
		avisar('No hubo respuesta del servidor.', false);
	});
}

// ==========================================================================
// Las tarjetas
// ==========================================================================

function pintarResumen(r) {
	if (!r) { return; }
	//La perdida llega negativa. Se muestra en positivo porque la tarjeta ya
	//dice "faltante": un signo menos sobre la palabra faltante se lee doble.
	$('#res-perdida').text(pesos(Math.abs(r.perdida)));
	$('#res-sobrante').text(pesos(Math.abs(r.sobrante)));
	$('#res-neto').text(pesos(r.valor));
	$('#res-neto').css('color', r.valor < 0 ? '#C21C1F' : '#16704F');
	$('#res-umbral').text(numero(r.diasfuerumbral));

	$('#res-neto-pie').text(r.tiendas + ' tienda(s), ' + r.dias + ' dia(s), ' + r.insumos + ' insumo(s)');
	$('#res-umbral-pie').text('de ' + numero(r.dias * r.insumos * r.tiendas) + ' mediciones del periodo');
}

// ==========================================================================
// Las graficas
// ==========================================================================

/** Barras horizontales: la etiqueta a la izquierda y el valor al final. */
function barrasHorizontales(destino, filas, vacio) {
	if (!filas || filas.length === 0) {
		$(destino).html('<div class="mv-vacio">' + vacio + '</div>');
		return;
	}
	var alto = 26;
	var anchoEtiqueta = 130;
	var anchoValor = 95;
	var ancho = Math.max($(destino).width() || 620, 420);
	var anchoBarra = ancho - anchoEtiqueta - anchoValor - 10;

	var tope = 0;
	for (var i = 0; i < filas.length; i++) {
		tope = Math.max(tope, Math.abs(filas[i].perdida));
	}
	if (tope === 0) { tope = 1; }

	var svg = '<svg width="' + ancho + '" height="' + (filas.length * alto + 8) + '" '
		+ 'viewBox="0 0 ' + ancho + ' ' + (filas.length * alto + 8) + '">';
	for (var j = 0; j < filas.length; j++) {
		var f = filas[j];
		var valor = Math.abs(f.perdida);
		var largo = Math.round(anchoBarra * valor / tope);
		var y = j * alto + 4;
		svg += '<text x="0" y="' + (y + 13) + '" font-size="12" fill="#333">'
			+ escapar(recortar(f.etiqueta, 20)) + '</text>';
		svg += '<rect x="' + anchoEtiqueta + '" y="' + y + '" width="' + Math.max(largo, 1)
			+ '" height="16" fill="#C21C1F" rx="2"></rect>';
		svg += '<text x="' + (anchoEtiqueta + Math.max(largo, 1) + 6) + '" y="' + (y + 13)
			+ '" font-size="11.5" fill="#6C7482">' + pesos(valor) + '</text>';
	}
	svg += '</svg>';
	$(destino).html(svg);
}

function pintarGraficaTiendas(filas) {
	barrasHorizontales('#graf-tiendas', filas, 'No hay varianza en el periodo escogido.');
}

function pintarGraficaInsumos(filas) {
	//Solo los diez de mayor faltante. Llegan ordenados por valor ascendente,
	//que con la perdida en negativo pone los peores de primeros.
	var top = [];
	for (var i = 0; i < filas.length && top.length < 10; i++) {
		if (filas[i].perdida < 0) {
			top.push(filas[i]);
		}
	}
	barrasHorizontales('#graf-insumos', top, 'No hay faltantes en el periodo escogido.');
}

/** Linea del faltante diario, con la banda bajo la curva. */
function pintarGraficaDias(filas) {
	var destino = '#graf-dias';
	if (!filas || filas.length === 0) {
		$(destino).html('<div class="mv-vacio">No hay dias con varianza en el periodo.</div>');
		return;
	}
	var ancho = Math.max($(destino).width() || 900, 420);
	var alto = 190;
	var margenIzq = 70;
	var margenAbajo = 26;
	var margenArriba = 10;

	var tope = 0;
	for (var i = 0; i < filas.length; i++) {
		tope = Math.max(tope, Math.abs(filas[i].perdida));
	}
	if (tope === 0) { tope = 1; }

	var anchoUtil = ancho - margenIzq - 12;
	var altoUtil = alto - margenAbajo - margenArriba;
	//Con un solo dia no hay division posible: se pone el punto en el centro.
	var paso = filas.length > 1 ? anchoUtil / (filas.length - 1) : 0;

	var puntos = '';
	var area = '';
	for (var j = 0; j < filas.length; j++) {
		var x = margenIzq + (filas.length > 1 ? j * paso : anchoUtil / 2);
		var y = margenArriba + altoUtil - (altoUtil * Math.abs(filas[j].perdida) / tope);
		puntos += (j === 0 ? '' : ' ') + x.toFixed(1) + ',' + y.toFixed(1);
		area += (j === 0 ? 'M' : 'L') + x.toFixed(1) + ',' + y.toFixed(1);
	}
	var xFin = margenIzq + (filas.length > 1 ? (filas.length - 1) * paso : anchoUtil / 2);
	area += 'L' + xFin.toFixed(1) + ',' + (margenArriba + altoUtil)
		+ 'L' + margenIzq + ',' + (margenArriba + altoUtil) + 'Z';

	var svg = '<svg width="' + ancho + '" height="' + alto + '" viewBox="0 0 ' + ancho + ' ' + alto + '">';
	//Tres lineas guia con su valor, para que la curva se pueda leer en pesos.
	for (var g = 0; g <= 2; g++) {
		var yg = margenArriba + altoUtil - (altoUtil * g / 2);
		svg += '<line x1="' + margenIzq + '" y1="' + yg + '" x2="' + (ancho - 12) + '" y2="' + yg
			+ '" stroke="#E2E6EC" stroke-width="1"></line>';
		svg += '<text x="0" y="' + (yg + 4) + '" font-size="10.5" fill="#6C7482">'
			+ pesos(tope * g / 2) + '</text>';
	}
	svg += '<path d="' + area + '" fill="#C21C1F" fill-opacity="0.10"></path>';
	svg += '<polyline points="' + puntos + '" fill="none" stroke="#C21C1F" stroke-width="2"></polyline>';
	//Solo las fechas de los extremos: con 30 o 300 dias, ponerlas todas es una
	//mancha negra ilegible.
	svg += '<text x="' + margenIzq + '" y="' + (alto - 8) + '" font-size="10.5" fill="#6C7482">'
		+ escapar(filas[0].etiqueta) + '</text>';
	if (filas.length > 1) {
		svg += '<text x="' + (ancho - 12) + '" y="' + (alto - 8) + '" font-size="10.5" fill="#6C7482" '
			+ 'text-anchor="end">' + escapar(filas[filas.length - 1].etiqueta) + '</text>';
	}
	svg += '</svg>';
	$(destino).html(svg);
}

// ==========================================================================
// El detalle
// ==========================================================================

function pintarDetalle(filas) {
	if ($.fn.DataTable.isDataTable('#grid-detalle')) {
		$('#grid-detalle').DataTable().destroy();
	}
	var cuerpo = '';
	if (!filas || filas.length === 0) {
		$('#grid-detalle tbody').html('');
		$('#detalle-sub').text('No hay lineas para este filtro.');
	} else {
		for (var i = 0; i < filas.length; i++) {
			var f = filas[i];
			cuerpo += '<tr>'
				+ '<td>' + escapar(f.etiqueta) + '</td>'
				+ '<td>' + etiquetaGrupo(f.grupo) + '</td>'
				+ '<td>' + escapar(f.etiqueta2) + '</td>'
				+ '<td class="mv-num">' + numero(Math.round(f.cantidad * 10) / 10) + '</td>'
				+ '<td>' + escapar(f.unidad) + '</td>'
				+ '<td class="mv-num mv-neg">' + pesos(Math.abs(f.perdida)) + '</td>'
				+ '<td class="mv-num mv-pos">' + pesos(Math.abs(f.sobrante)) + '</td>'
				+ '<td class="mv-num" style="color:' + (f.valor < 0 ? '#C21C1F' : '#16704F') + ';">'
				+ pesos(f.valor) + '</td>'
				+ '<td class="mv-num">' + numero(f.diasfuerumbral) + ' de ' + numero(f.dias) + '</td>'
				+ '</tr>';
		}
		$('#grid-detalle tbody').html(cuerpo);
		$('#detalle-sub').text(filas.length + ' linea(s), ordenadas por faltante.'
			+ (filas.length === 500 ? ' Se muestran las 500 peores.' : ''));
	}

	$('#grid-detalle').DataTable({
		"paging": true,
		"pageLength": 25,
		"searching": true,
		"info": true,
		"order": [],
		"language": {
			"search": "Buscar:",
			"lengthMenu": "Mostrar _MENU_ lineas",
			"info": "_START_ a _END_ de _TOTAL_",
			"infoEmpty": "sin lineas",
			"zeroRecords": "nada que coincida",
			"paginate": { "first": "Primera", "last": "Ultima", "next": "Siguiente", "previous": "Anterior" }
		}
	});
}

function etiquetaGrupo(grupo) {
	if (grupo === 'CAROS') {
		return ('<span class="mv-etiqueta mv-g-caros">COSTOSOS</span>');
	}
	if (grupo === 'CARNES') {
		return ('<span class="mv-etiqueta mv-g-carnes">CARNES</span>');
	}
	return ('<span class="mv-etiqueta mv-g-vacio">-</span>');
}

// ==========================================================================
// Utilidades
// ==========================================================================

function limpiarPantalla() {
	$('#res-perdida, #res-sobrante, #res-neto').text('$ 0');
	$('#res-umbral').text('0');
	$('#graf-tiendas, #graf-insumos, #graf-dias').html('<div class="mv-vacio">Sin informacion.</div>');
	if ($.fn.DataTable.isDataTable('#grid-detalle')) {
		$('#grid-detalle').DataTable().destroy();
	}
	$('#grid-detalle tbody').html('');
}

function avisar(texto, bien) {
	$('#mv-aviso').text(texto)
		.removeClass('mv-aviso-ok mv-aviso-mal')
		.addClass(bien ? 'mv-aviso-ok' : 'mv-aviso-mal')
		.show();
}

function esconderAviso() {
	$('#mv-aviso').hide();
}

/** Pesos redondeados, con punto de miles. No se muestran centavos. */
function pesos(valor) {
	var n = Math.round(valor || 0);
	var signo = n < 0 ? '-' : '';
	return (signo + '$ ' + numero(Math.abs(n)));
}

function numero(valor) {
	var n = valor || 0;
	var partes = n.toString().split('.');
	partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
	return (partes.join(','));
}

function recortar(texto, largo) {
	if (!texto) { return (''); }
	return (texto.length > largo ? texto.substring(0, largo - 1) + '…' : texto);
}

/** Un nombre con < o & rompe el HTML que se arma por concatenacion. */
function escapar(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;'));
}

/**
 * Las cuatro bolsas de estructura y la nomina agregada por tienda.
 *
 * La tabla del medio -como quedaria repartido- no se guarda en ninguna parte.
 * Se calcula cada vez contra la venta real de las semanas que el calendario le
 * asigno al mes. Es la respuesta a como se reparte la estructura: por
 * participacion en la venta, con la venta de hoy y no con la de hace un ano.
 */

var MESES_E = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
	'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

var BOLSAS = [
	{ tipo: 'ADMINISTRATIVA', nombre: 'Administrativa' },
	{ tipo: 'LOGISTICA', nombre: 'Logistica y produccion' },
	{ tipo: 'CONTACT', nombre: 'Contact center' },
	{ tipo: 'PUBLICIDAD', nombre: 'Publicidad y mercadeo' }
];

var infoE = null;
var cambiosNomina = {};

$(function () {
	Gerencia.proteger('ger-contenido', arrancarE);
});

function arrancarE() {
	llenarPeriodoE();
	$('#btnConsultar').on('click', consultarE);
	$('#anio, #mes').on('change', consultarE);
	$('#btnGuardarPools').on('click', guardarPools);
	$('#btnGuardarNomina').on('click', guardarNomina);
	pintarBolsas();
	consultarE();
}

function llenarPeriodoE() {
	var hoy = new Date();
	var anios = '';
	for (var a = hoy.getFullYear() - 2; a <= hoy.getFullYear() + 1; a++) {
		anios += '<option value="' + a + '"' + (a === hoy.getFullYear() ? ' selected' : '') + '>' + a + '</option>';
	}
	$('#anio').html(anios);
	var meses = '';
	for (var m = 1; m <= 12; m++) {
		meses += '<option value="' + m + '"' + (m === hoy.getMonth() + 1 ? ' selected' : '') + '>'
			+ MESES_E[m] + '</option>';
	}
	$('#mes').html(meses);
}

function periodoE() {
	return ({ anio: parseInt($('#anio').val(), 10), mes: parseInt($('#mes').val(), 10) });
}

function pintarBolsas() {
	var html = '';
	for (var i = 0; i < BOLSAS.length; i++) {
		html += '<div class="col-md-3 col-sm-6"><div class="ger-campo">'
			+ '<label>' + BOLSAS[i].nombre + '</label>'
			+ '<input type="text" class="form-control bolsa" id="bolsa_' + BOLSAS[i].tipo + '"'
			+ ' data-tipo="' + BOLSAS[i].tipo + '" style="text-align:right;" />'
			+ '</div></div>';
	}
	$('#bolsas').html(html);
	$('.bolsa').on('input', calcularReparto);
}

function consultarE() {
	var p = periodoE();
	cambiosNomina = {};
	$.getJSON('GerenciaEstructura', { que: 'consultar', anio: p.anio, mes: p.mes }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		infoE = data;

		for (var i = 0; i < data.pools.length; i++) {
			$('#bolsa_' + data.pools[i].tipo).val(
				data.pools[i].valor === 0 ? '' : milesE(data.pools[i].valor));
		}

		$('#infoSemanas').text(data.semanas === 0
			? 'Este mes no tiene semanas en el calendario'
			: 'El calendario le asigno ' + data.semanas + ' semana(s) a ' + MESES_E[p.mes]);

		pintarNomina();
		calcularReparto();
		contarNomina();
		Gerencia.avisar('');
	}).fail(function () {
		Gerencia.avisar('No se pudo consultar la estructura.');
	});
}

// =========================================================================
// BOLSAS Y REPARTO
// =========================================================================

function valorBolsa(tipo) {
	var v = Gerencia.numero($('#bolsa_' + tipo).val());
	return (v === null ? 0 : v);
}

function calcularReparto() {
	var total = 0;
	for (var i = 0; i < BOLSAS.length; i++) {
		total += valorBolsa(BOLSAS[i].tipo);
	}
	$('#totalBolsas').text(total === 0 ? '' : 'Total de estructura del mes: ' + Gerencia.pesos(total));

	var cuerpo = $('#tablaReparto tbody');
	if (!infoE) { return; }

	if (!infoE.reparto || infoE.reparto.length === 0) {
		//Sin calendario o sin venta no se reparte parejo como plan B: un reparto
		//que se ve bien y esta mal es peor que no mostrar ninguno.
		cuerpo.html('<tr><td colspan="8" style="padding:16px;text-align:center;" class="ger-vacio">'
			+ (infoE.semanas === 0
				? 'Este mes no tiene semanas asignadas en el calendario. Defina el calendario primero.'
				: 'No hay venta registrada para las semanas de este mes.')
			+ '</td></tr>');
		return;
	}

	var html = '';
	var sumaVenta = 0;
	for (var r = 0; r < infoE.reparto.length; r++) {
		var fila = infoE.reparto[r];
		var parte = fila.participacion / 100;
		var adm = valorBolsa('ADMINISTRATIVA') * parte;
		var log = valorBolsa('LOGISTICA') * parte;
		var con = valorBolsa('CONTACT') * parte;
		var pub = valorBolsa('PUBLICIDAD') * parte;
		sumaVenta += fila.venta;

		html += '<tr><td><strong>' + nombreTienda(fila.idtienda) + '</strong></td>'
			+ '<td class="ger-num">' + Gerencia.pesos(fila.venta) + '</td>'
			+ '<td class="ger-num"><strong>' + fila.participacion.toFixed(2) + '%</strong></td>'
			+ '<td class="ger-num">' + Gerencia.pesos(adm) + '</td>'
			+ '<td class="ger-num">' + Gerencia.pesos(log) + '</td>'
			+ '<td class="ger-num">' + Gerencia.pesos(con) + '</td>'
			+ '<td class="ger-num">' + Gerencia.pesos(pub) + '</td>'
			+ '<td class="ger-num"><strong>' + Gerencia.pesos(adm + log + con + pub) + '</strong></td></tr>';
	}
	html += '<tr style="background:#F4F6F8;"><td><strong>Total</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(sumaVenta) + '</strong></td>'
		+ '<td class="ger-num"><strong>100,00%</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(valorBolsa('ADMINISTRATIVA')) + '</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(valorBolsa('LOGISTICA')) + '</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(valorBolsa('CONTACT')) + '</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(valorBolsa('PUBLICIDAD')) + '</strong></td>'
		+ '<td class="ger-num"><strong>' + Gerencia.pesos(total) + '</strong></td></tr>';
	cuerpo.html(html);
}

function nombreTienda(idtienda) {
	if (!infoE || !infoE.tiendas) { return ('Tienda ' + idtienda); }
	for (var i = 0; i < infoE.tiendas.length; i++) {
		if (infoE.tiendas[i].idtienda === idtienda) {
			return (infoE.tiendas[i].nombre);
		}
	}
	//Una tienda que vende pero no esta en el maestro tiene que verse, no
	//desaparecer del reparto: si no, el 100% no suma 100.
	return ('Tienda ' + idtienda + ' (fuera del maestro)');
}

function guardarPools() {
	var p = periodoE();
	var pendientes = [];
	for (var i = 0; i < BOLSAS.length; i++) {
		var crudo = $('#bolsa_' + BOLSAS[i].tipo).val();
		var valor = Gerencia.numero(crudo);
		if (valor === null && $.trim(crudo) !== '') {
			Gerencia.avisar('El valor de ' + BOLSAS[i].nombre + ' no se entiende. Escriba solo numeros.');
			return;
		}
		pendientes.push({ tipo: BOLSAS[i].tipo, nombre: BOLSAS[i].nombre, valor: (valor === null ? 0 : valor) });
	}

	$('#btnGuardarPools').prop('disabled', true);
	mandarEnCadena(pendientes, 0, [], function (item, seguir) {
		$.post('GerenciaEstructura', {
			que: 'pool', anio: p.anio, mes: p.mes, tipo: item.tipo, valor: item.valor
		}, function (data) { seguir(data); }, 'json').fail(function () { seguir(null); });
	}, function (fallos) {
		$('#btnGuardarPools').prop('disabled', false);
		if (fallos.length === 0) {
			Gerencia.avisar('Bolsas de ' + MESES_E[p.mes] + ' guardadas.', true);
		} else {
			Gerencia.avisar('No se pudo guardar: ' + fallos.join(' | '));
		}
		consultarE();
	});
}

// =========================================================================
// NOMINA
// =========================================================================

function nominaDe(idtienda) {
	if (!infoE || !infoE.nominas) { return (null); }
	for (var i = 0; i < infoE.nominas.length; i++) {
		if (infoE.nominas[i].idtienda === idtienda) {
			return (infoE.nominas[i]);
		}
	}
	return (null);
}

function pintarNomina() {
	var html = '';
	for (var t = 0; t < infoE.tiendas.length; t++) {
		var tienda = infoE.tiendas[t];
		var n = nominaDe(tienda.idtienda);
		html += '<tr data-tienda="' + tienda.idtienda + '">'
			+ '<td><strong>' + tienda.nombre + '</strong></td>'
			+ celdaNomina(tienda.idtienda, 'empleados', n ? n.empleados : 0, true)
			+ celdaNomina(tienda.idtienda, 'basico', n ? n.basico : 0, false)
			+ celdaNomina(tienda.idtienda, 'variable', n ? n.variable : 0, false)
			+ celdaNomina(tienda.idtienda, 'seguridad', n ? n.seguridad : 0, false)
			+ celdaNomina(tienda.idtienda, 'liquidacion', n ? n.liquidacion : 0, false)
			+ '<td class="ger-num total-nomina"><strong>'
			+ (n ? Gerencia.pesos(n.total) : '') + '</strong></td></tr>';
	}
	$('#tablaNomina tbody').html(html);

	$('.celda-nomina').on('input', function () {
		var idtienda = $(this).data('tienda');
		cambiosNomina[idtienda] = true;
		$(this).addClass('ger-sucio');
		recalcularFila(idtienda);
		contarNomina();
	});
}

function celdaNomina(idtienda, campo, valor, entero) {
	var texto = '';
	if (valor && valor !== 0) {
		texto = entero ? valor : milesE(valor);
	}
	return ('<td class="ger-num"><input type="text" class="ger-entrada celda-nomina"'
		+ ' data-tienda="' + idtienda + '" data-campo="' + campo + '"'
		+ ' value="' + texto + '" /></td>');
}

function leerCampo(idtienda, campo) {
	var celda = $('.celda-nomina[data-tienda="' + idtienda + '"][data-campo="' + campo + '"]');
	var v = Gerencia.numero(celda.val());
	return (v === null ? 0 : v);
}

function recalcularFila(idtienda) {
	//El costo del mes es variable + seguridad + liquidacion, igual que en la
	//hoja de hoy. El basico NO se suma: es la base sobre la que se calculan los
	//otros, y sumarlo lo contaria dos veces.
	var total = leerCampo(idtienda, 'variable') + leerCampo(idtienda, 'seguridad')
		+ leerCampo(idtienda, 'liquidacion');
	$('tr[data-tienda="' + idtienda + '"] .total-nomina').html('<strong>'
		+ (total === 0 ? '' : Gerencia.pesos(total)) + '</strong>');
}

function contarNomina() {
	var n = Object.keys(cambiosNomina).length;
	$('#cuantasNominas').text(n === 0 ? 'Sin cambios' : (n + ' tienda(s) por guardar'));
	$('#btnGuardarNomina').prop('disabled', n === 0);
}

function guardarNomina() {
	var p = periodoE();
	var tiendas = Object.keys(cambiosNomina);
	if (tiendas.length === 0) { return; }

	var pendientes = [];
	for (var i = 0; i < tiendas.length; i++) {
		var idtienda = parseInt(tiendas[i], 10);
		pendientes.push({
			nombre: nombreTienda(idtienda),
			idtienda: idtienda,
			empleados: Math.round(leerCampo(idtienda, 'empleados')),
			basico: leerCampo(idtienda, 'basico'),
			variable: leerCampo(idtienda, 'variable'),
			seguridad: leerCampo(idtienda, 'seguridad'),
			liquidacion: leerCampo(idtienda, 'liquidacion')
		});
	}

	$('#btnGuardarNomina').prop('disabled', true);
	mandarEnCadena(pendientes, 0, [], function (item, seguir) {
		$.post('GerenciaEstructura', {
			que: 'nomina', anio: p.anio, mes: p.mes, idtienda: item.idtienda,
			empleados: item.empleados, basico: item.basico, variable: item.variable,
			seguridad: item.seguridad, liquidacion: item.liquidacion
		}, function (data) { seguir(data); }, 'json').fail(function () { seguir(null); });
	}, function (fallos) {
		$('#btnGuardarNomina').prop('disabled', false);
		if (fallos.length === 0) {
			Gerencia.avisar('Nomina de ' + MESES_E[p.mes] + ' guardada para '
				+ pendientes.length + ' tienda(s).', true);
		} else {
			Gerencia.avisar('No se pudo guardar: ' + fallos.join(' | '));
		}
		consultarE();
	});
}

// =========================================================================

/** Uno detras de otro, para que dos escrituras no se pisen. */
function mandarEnCadena(lista, indice, fallos, mandar, alTerminar) {
	if (indice >= lista.length) {
		alTerminar(fallos);
		return;
	}
	mandar(lista[indice], function (data) {
		if (!data || data.respuesta !== 'OK') {
			fallos.push(lista[indice].nombre + ': '
				+ ((data && data.detalle) ? data.detalle : 'no respondio'));
		}
		mandarEnCadena(lista, indice + 1, fallos, mandar, alTerminar);
	});
}

function milesE(valor) {
	if (valor === null || valor === undefined || isNaN(valor)) { return (''); }
	return (Math.round(valor).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

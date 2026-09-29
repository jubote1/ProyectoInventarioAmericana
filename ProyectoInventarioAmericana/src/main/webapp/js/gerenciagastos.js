/**
 * Gastos parametrizables por tienda.
 *
 * Dos matrices tienda x concepto: los fijos y los servicios. Se escribe encima
 * y se guarda de una sola vez, porque asi es como llega la informacion: una hoja
 * al mes, no un dato suelto.
 *
 * SOLO SE MANDA LO QUE SE TOCO
 *
 * La pantalla lleva la cuenta de que celdas cambiaron y manda unicamente esas.
 * Mandarlas todas abriria una vigencia nueva por cada concepto de cada tienda
 * cada vez que alguien abre la pantalla y le da guardar, y la historia -que es
 * justo lo que estas tablas existen para conservar- quedaria llena de cambios
 * que nunca ocurrieron.
 */

var MESES_G = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
	'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

var datos = null;
var cambiosFijos = {};
var cambiosServicios = {};

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	llenarPeriodo();
	$('#btnConsultar').on('click', consultar);
	$('#anio, #mes').on('change', consultar);
	$('#tabFijos').on('click', function (e) { e.preventDefault(); verPestana('fijos'); });
	$('#tabServicios').on('click', function (e) { e.preventDefault(); verPestana('servicios'); });
	$('#btnGuardarFijos').on('click', guardarFijos);
	$('#btnGuardarServicios').on('click', guardarServicios);
	$('#btnAceptarEstimados').on('click', aceptarEstimados);
	$('#btnCerrarHistoria').on('click', function () { $('#panelHistoria').hide(); });
	consultar();
}

function llenarPeriodo() {
	var hoy = new Date();
	var anios = '';
	for (var a = hoy.getFullYear() - 2; a <= hoy.getFullYear() + 1; a++) {
		anios += '<option value="' + a + '"' + (a === hoy.getFullYear() ? ' selected' : '') + '>' + a + '</option>';
	}
	$('#anio').html(anios);
	var meses = '';
	for (var m = 1; m <= 12; m++) {
		meses += '<option value="' + m + '"' + (m === hoy.getMonth() + 1 ? ' selected' : '') + '>'
			+ MESES_G[m] + '</option>';
	}
	$('#mes').html(meses);
}

function periodo() {
	return ({ anio: parseInt($('#anio').val(), 10), mes: parseInt($('#mes').val(), 10) });
}

function consultar() {
	var p = periodo();
	cambiosFijos = {};
	cambiosServicios = {};
	$('#panelHistoria').hide();
	$.getJSON('GerenciaGastos', { que: 'consultar', anio: p.anio, mes: p.mes }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		datos = data;
		//Por defecto los cambios rigen desde el primer dia del mes consultado.
		//Es lo que casi siempre se quiere, y si no, se cambia.
		$('#rigeDesde').val(p.anio + '-' + dos(p.mes) + '-01');
		$('#vigenteAl').text('Mostrando lo vigente al ' + data.vigenteal);
		pintarFijos();
		pintarServicios();
		contar();
		Gerencia.avisar('');
	}).fail(function () {
		Gerencia.avisar('No se pudieron consultar los gastos.');
	});
}

function verPestana(cual) {
	$('#tabFijos').toggleClass('activa', cual === 'fijos');
	$('#tabServicios').toggleClass('activa', cual === 'servicios');
	$('#panelFijos').toggle(cual === 'fijos');
	$('#panelServicios').toggle(cual === 'servicios');
	$('#panelHistoria').hide();
}

// =========================================================================
// FIJOS
// =========================================================================

function conceptosDe(tipo) {
	var lista = [];
	for (var i = 0; i < datos.conceptos.length; i++) {
		if (datos.conceptos[i].tipo === tipo) {
			lista.push(datos.conceptos[i]);
		}
	}
	return (lista);
}

function valorFijo(idtienda, idconcepto) {
	for (var i = 0; i < datos.fijos.length; i++) {
		var f = datos.fijos[i];
		if (f.idtienda === idtienda && f.idconcepto === idconcepto) {
			return (f);
		}
	}
	return (null);
}

function pintarFijos() {
	var conceptos = conceptosDe('FIJO');
	var encabezado = '<tr><th style="min-width:120px;">Tienda</th>';
	for (var c = 0; c < conceptos.length; c++) {
		encabezado += '<th class="ger-num" style="min-width:95px;">' + conceptos[c].nombre + '</th>';
	}
	encabezado += '<th class="ger-num" style="min-width:110px;">Total mes</th></tr>';
	$('#tablaFijos thead').html(encabezado);

	var cuerpo = '';
	for (var t = 0; t < datos.tiendas.length; t++) {
		var tienda = datos.tiendas[t];
		var total = 0;
		cuerpo += '<tr><td><strong>' + tienda.nombre + '</strong></td>';
		for (var k = 0; k < conceptos.length; k++) {
			var concepto = conceptos[k];
			var actual = valorFijo(tienda.idtienda, concepto.idconcepto);
			var valor = actual ? actual.valor : 0;
			total += valor;
			var llave = tienda.idtienda + '_' + concepto.idconcepto;
			cuerpo += '<td class="ger-num">'
				+ '<input type="text" class="ger-entrada celda-fijo" data-llave="' + llave + '"'
				+ ' data-tienda="' + tienda.idtienda + '" data-concepto="' + concepto.idconcepto + '"'
				+ ' data-original="' + valor + '"'
				+ ' data-nombre="' + escapar(tienda.nombre + ' / ' + concepto.nombre) + '"'
				+ ' value="' + (valor === 0 ? '' : miles(valor)) + '" /></td>';
		}
		cuerpo += '<td class="ger-num"><strong>' + Gerencia.pesos(total) + '</strong></td></tr>';
	}
	$('#tablaFijos tbody').html(cuerpo);

	$('.celda-fijo').on('input', function () { marcar($(this), cambiosFijos); });
	$('.celda-fijo').on('dblclick', function () {
		verHistoria($(this).data('tienda'), $(this).data('concepto'), $(this).data('nombre'));
	});
}

/** Marca la celda como tocada, o la desmarca si volvio a su valor original. */
function marcar(celda, bolsa) {
	var llave = celda.data('llave');
	var nuevo = Gerencia.numero(celda.val());
	var original = parseFloat(celda.data('original'));

	if (nuevo === null && $.trim(celda.val()) !== '') {
		//Lo que no se entiende se senala aqui mismo, no al guardar: quien esta
		//digitando tiene que verlo mientras todavia esta mirando esa celda.
		celda.addClass('ger-sucio').css('border-color', '#C21C1F');
		delete bolsa[llave];
		contar();
		return;
	}
	celda.css('border-color', '');
	var valor = (nuevo === null) ? 0 : nuevo;
	if (valor === original) {
		celda.removeClass('ger-sucio');
		delete bolsa[llave];
	} else {
		celda.addClass('ger-sucio');
		bolsa[llave] = {
			tienda: celda.data('tienda'),
			concepto: celda.data('concepto'),
			valor: valor,
			nombre: celda.data('nombre')
		};
	}
	contar();
}

function contar() {
	var f = Object.keys(cambiosFijos).length;
	var s = Object.keys(cambiosServicios).length;
	$('#cuantosCambios').text(f === 0 ? 'Sin cambios' : (f + ' celda(s) por guardar'));
	$('#cuantosServicios').text(s === 0 ? 'Sin cambios' : (s + ' celda(s) por guardar'));
	$('#btnGuardarFijos').prop('disabled', f === 0);
	$('#btnGuardarServicios').prop('disabled', s === 0);
}

function guardarFijos() {
	var desde = $.trim($('#rigeDesde').val());
	if (!/^\d{4}-\d{2}-\d{2}$/.test(desde)) {
		Gerencia.avisar('La fecha desde la que rigen los cambios tiene que ir como aaaa-mm-dd.');
		return;
	}
	var llaves = Object.keys(cambiosFijos);
	if (llaves.length === 0) { return; }

	if (!confirm('Se van a abrir ' + llaves.length + ' vigencia(s) nueva(s) desde el ' + desde
		+ '.\n\nLo anterior no se borra: queda cerrado el dia antes y se puede consultar.\n\nContinuar?')) {
		return;
	}

	$('#btnGuardarFijos').prop('disabled', true);
	enCadena(llaves, 0, cambiosFijos, function (cambio, seguir) {
		$.post('GerenciaGastos', {
			que: 'fijo',
			idtienda: cambio.tienda,
			idconcepto: cambio.concepto,
			valor: cambio.valor,
			desde: desde,
			observacion: $.trim($('#observacion').val())
		}, function (data) { seguir(data, cambio); }, 'json').fail(function () {
			seguir(null, cambio);
		});
	}, function (fallos) {
		$('#btnGuardarFijos').prop('disabled', false);
		terminar(fallos, llaves.length, 'gasto fijo');
	});
}

// =========================================================================
// SERVICIOS
// =========================================================================

function pintarServicios() {
	var conceptos = conceptosDe('SERVICIO');
	if (conceptos.length === 0) {
		$('#tablaServicios thead').html('');
		$('#tablaServicios tbody').html('<tr><td class="ger-nota" style="padding:14px;">'
			+ 'No hay conceptos marcados como servicio en el catalogo.</td></tr>');
		return;
	}

	var encabezado = '<tr><th style="min-width:130px;">Tienda</th>';
	for (var c = 0; c < conceptos.length; c++) {
		encabezado += '<th class="ger-num" style="min-width:130px;">' + conceptos[c].nombre + '</th>'
			+ '<th style="min-width:130px;">Origen</th>';
	}
	encabezado += '</tr>';
	$('#tablaServicios thead').html(encabezado);

	var cuerpo = '';
	for (var t = 0; t < datos.tiendas.length; t++) {
		var tienda = datos.tiendas[t];
		var clase = '';
		var celdas = '';
		for (var k = 0; k < conceptos.length; k++) {
			var concepto = conceptos[k];
			var s = filaServicio(tienda.idtienda, concepto.idconcepto);
			var llave = tienda.idtienda + '_' + concepto.idconcepto;

			var valorTexto = '';
			var original = 0;
			if (s && s.valor !== null && s.valor !== undefined) {
				valorTexto = miles(s.valor);
				original = s.valor;
			}
			//Solo cuenta como "ya guardado" lo que esta en la base. Un estimado
			//que la pantalla acaba de calcular no lo esta, y por eso su valor
			//original es cero: si alguien lo deja igual y guarda, se guarda.
			if (s && !s.guardado) {
				original = 0;
			}

			celdas += '<td class="ger-num">'
				+ '<input type="text" class="ger-entrada celda-servicio" data-llave="' + llave + '"'
				+ ' data-tienda="' + tienda.idtienda + '" data-concepto="' + concepto.idconcepto + '"'
				+ ' data-original="' + original + '"'
				+ ' data-meses="' + (s ? s.meses : 0) + '"'
				+ ' data-origen="' + (s ? s.origen : 'SINBASE') + '"'
				+ ' data-nombre="' + escapar(tienda.nombre + ' / ' + concepto.nombre) + '"'
				+ ' value="' + valorTexto + '" /></td>'
				+ '<td>' + etiquetaOrigen(s) + '</td>';

			if (s && s.origen === 'SINBASE') { clase = ' class="ger-fila-sinbase"'; }
			else if (s && s.origen === 'ESTIMADO' && clase === '') { clase = ' class="ger-fila-estimada"'; }
		}
		cuerpo += '<tr' + clase + '><td><strong>' + tienda.nombre + '</strong></td>' + celdas + '</tr>';
	}
	$('#tablaServicios tbody').html(cuerpo);

	$('.celda-servicio').on('input', function () { marcar($(this), cambiosServicios); });
}

function filaServicio(idtienda, idconcepto) {
	for (var i = 0; i < datos.servicios.length; i++) {
		var s = datos.servicios[i];
		if (s.idtienda === idtienda && s.idconcepto === idconcepto) {
			return (s);
		}
	}
	return (null);
}

function etiquetaOrigen(s) {
	if (!s) { return (''); }
	if (s.origen === 'REAL') {
		return ('<span class="ger-origen ger-real">Real</span>');
	}
	if (s.origen === 'ESTIMADO') {
		return ('<span class="ger-origen ger-estimado">Estimado</span> '
			+ '<span class="ger-nota">' + s.meses + ' mes(es)'
			+ (s.guardado ? ', aceptado' : ', propuesto') + '</span>');
	}
	//Sin base no se acompana de un numero. Un cero se suma al total y se pierde
	//de vista; un vacio se ve y alguien pregunta.
	return ('<span class="ger-origen ger-sinbase">Sin base</span> '
		+ '<span class="ger-vacio">sin historia para estimar</span>');
}

function guardarServicios() {
	var llaves = Object.keys(cambiosServicios);
	if (llaves.length === 0) { return; }
	var p = periodo();
	$('#btnGuardarServicios').prop('disabled', true);
	enCadena(llaves, 0, cambiosServicios, function (cambio, seguir) {
		$.post('GerenciaGastos', {
			que: 'servicio',
			idtienda: cambio.tienda,
			idconcepto: cambio.concepto,
			anio: p.anio,
			mes: p.mes,
			valor: cambio.valor,
			origen: 'REAL'
		}, function (data) { seguir(data, cambio); }, 'json').fail(function () {
			seguir(null, cambio);
		});
	}, function (fallos) {
		$('#btnGuardarServicios').prop('disabled', false);
		terminar(fallos, llaves.length, 'servicio');
	});
}

/**
 * Guarda los estimados que la pantalla propuso, marcados como estimados.
 *
 * Sirve para cerrar un mes sin esperar las facturas. Cuando lleguen se digitan
 * encima y el real reemplaza al estimado; al reves no pasa, un estimado nunca
 * pisa una factura ya cargada.
 */
function aceptarEstimados() {
	var pendientes = [];
	for (var i = 0; i < datos.servicios.length; i++) {
		var s = datos.servicios[i];
		if (s.origen === 'ESTIMADO' && !s.guardado) {
			pendientes.push(s);
		}
	}
	if (pendientes.length === 0) {
		Gerencia.avisar('No hay estimados pendientes de aceptar en este mes.');
		return;
	}
	if (!confirm('Se van a guardar ' + pendientes.length + ' valor(es) estimado(s).\n\n'
		+ 'Quedan marcados como ESTIMADO. Cuando llegue la factura se digita encima '
		+ 'y el real los reemplaza.\n\nContinuar?')) {
		return;
	}

	var p = periodo();
	var bolsa = {};
	var llaves = [];
	for (var k = 0; k < pendientes.length; k++) {
		var llave = 'e' + k;
		llaves.push(llave);
		bolsa[llave] = {
			tienda: pendientes[k].idtienda,
			concepto: pendientes[k].idconcepto,
			valor: pendientes[k].valor,
			meses: pendientes[k].meses,
			nombre: pendientes[k].tienda + ' / ' + pendientes[k].concepto
		};
	}

	$('#btnAceptarEstimados').prop('disabled', true);
	enCadena(llaves, 0, bolsa, function (cambio, seguir) {
		$.post('GerenciaGastos', {
			que: 'servicio',
			idtienda: cambio.tienda,
			idconcepto: cambio.concepto,
			anio: p.anio,
			mes: p.mes,
			valor: cambio.valor,
			origen: 'ESTIMADO',
			meses: cambio.meses
		}, function (data) { seguir(data, cambio); }, 'json').fail(function () {
			seguir(null, cambio);
		});
	}, function (fallos) {
		$('#btnAceptarEstimados').prop('disabled', false);
		terminar(fallos, llaves.length, 'estimado');
	});
}

// =========================================================================
// HISTORIA
// =========================================================================

function verHistoria(idtienda, idconcepto, nombre) {
	$.getJSON('GerenciaGastos', { que: 'historia', idtienda: idtienda, idconcepto: idconcepto },
		function (data) {
			if (!Gerencia.respondio(data)) { return; }
			$('#tituloHistoria').text('Historia: ' + nombre);
			var html = '';
			if (!data.historia || data.historia.length === 0) {
				html = '<tr><td colspan="5" class="ger-nota" style="padding:12px;">'
					+ 'Todavia no hay ningun valor registrado.</td></tr>';
			} else {
				for (var i = 0; i < data.historia.length; i++) {
					var h = data.historia[i];
					html += '<tr><td>' + h.desde + '</td>'
						+ '<td>' + (h.hasta === '' ? '<strong>vigente</strong>' : h.hasta) + '</td>'
						+ '<td class="ger-num">' + Gerencia.pesos(h.valor) + '</td>'
						+ '<td>' + escapar(h.observacion) + '</td>'
						+ '<td class="ger-nota">' + escapar(h.usuario) + '</td></tr>';
				}
			}
			$('#tablaHistoria tbody').html(html);
			$('#panelHistoria').show();
			$('html, body').animate({ scrollTop: $('#panelHistoria').offset().top - 20 }, 300);
		});
}

// =========================================================================
// AYUDAS
// =========================================================================

/**
 * Manda los cambios uno detras de otro, no todos a la vez.
 *
 * Cada gasto fijo abre una vigencia dentro de una transaccion que primero
 * cierra la anterior. Lanzarlos en paralelo haria que dos escrituras sobre la
 * misma tienda se pisaran y quedara mas de una vigencia abierta.
 */
function enCadena(llaves, indice, bolsa, mandar, alTerminar) {
	if (!enCadena.fallos || indice === 0) { enCadena.fallos = []; }
	if (indice >= llaves.length) {
		alTerminar(enCadena.fallos);
		return;
	}
	var cambio = bolsa[llaves[indice]];
	mandar(cambio, function (data) {
		if (!data || data.respuesta !== 'OK') {
			enCadena.fallos.push(cambio.nombre + ': '
				+ ((data && data.detalle) ? data.detalle : 'no respondio'));
		}
		enCadena(llaves, indice + 1, bolsa, mandar, alTerminar);
	});
}

function terminar(fallos, total, que) {
	if (fallos.length === 0) {
		Gerencia.avisar('Se guardaron ' + total + ' ' + que + '(s).', true);
	} else {
		//Los que fallaron se nombran uno por uno. "Hubo errores" obliga a
		//revisar la matriz entera para encontrar cual.
		Gerencia.avisar('Se guardaron ' + (total - fallos.length) + ' de ' + total + '. '
			+ 'No se pudo con: ' + fallos.join(' | '));
	}
	consultar();
}

function miles(valor) {
	if (valor === null || valor === undefined || isNaN(valor)) { return (''); }
	return (Math.round(valor).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function dos(n) {
	return (n < 10 ? '0' + n : '' + n);
}

function escapar(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;')
		.replace(/>/g, '&gt;').replace(/"/g, '&quot;'));
}

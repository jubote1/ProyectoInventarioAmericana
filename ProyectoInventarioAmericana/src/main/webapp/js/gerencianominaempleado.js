/**
 * Nomina por empleado y su reparto por tienda segun biometria.
 *
 * El flujo es: se busca un empleado, se cargan sus cuatro valores de esa
 * semana, se guarda. Cuando ya se cargaron los que hagan falta, se le da
 * "Calcular reparto por biometria" y el servidor mira empleado_evento y
 * reparte. Volver a darle al boton reemplaza el calculo anterior de esa
 * semana entera -es reprocesable a proposito, por si se corrigio una
 * marcacion despues de la primera corrida-.
 */

var empleadoElegido = null;
var buscarTimeout = null;

$(function () {
	Gerencia.proteger('ger-contenido', arrancar);
});

function arrancar() {
	$('#semana').val(domingoMasReciente());
	$('#btnConsultar').on('click', consultar);
	$('#btnCalcular').on('click', calcular);
	$('#btnGuardar').on('click', guardar);
	$('#buscarEmpleado').on('input', function () { buscarEmpleado($(this).val()); });
	$('#buscarEmpleado').on('blur', function () {
		//Se espera un poco: si fue un clic en la lista, ese clic tiene que
		//alcanzar a correr antes de que la lista se esconda.
		setTimeout(function () { $('#listaEmpleados').hide(); }, 200);
	});
	$(document).on('click', function (e) {
		if (!$(e.target).closest('#buscarEmpleado, #listaEmpleados').length) {
			$('#listaEmpleados').hide();
		}
	});
	consultar();
}

/** El domingo de hoy, o el pasado si hoy no es domingo -el "cierre" mas reciente. */
function domingoMasReciente() {
	var hoy = new Date();
	var dia = hoy.getDay(); // 0 = domingo
	hoy.setDate(hoy.getDate() - dia);
	return (hoy.getFullYear() + '-' + dos(hoy.getMonth() + 1) + '-' + dos(hoy.getDate()));
}

function dos(n) {
	return (n < 10 ? '0' + n : '' + n);
}

// ===========================================================================
// BUSCAR EMPLEADO
// ===========================================================================

function buscarEmpleado(filtro) {
	empleadoElegido = null;
	$('#idEmpleadoElegido').val('');
	if (buscarTimeout) {
		clearTimeout(buscarTimeout);
	}
	if ($.trim(filtro).length < 2) {
		$('#listaEmpleados').hide();
		return;
	}
	buscarTimeout = setTimeout(function () {
		$.getJSON('GerenciaNominaEmpleado', { que: 'buscarempleado', filtro: filtro }, function (data) {
			if (!Gerencia.respondio(data)) { return; }
			pintarSugerencias(data.empleados);
		});
	}, 250);
}

function pintarSugerencias(empleados) {
	var caja = $('#listaEmpleados');
	caja.empty();
	if (!empleados || empleados.length === 0) {
		caja.append('<div class="ger-nota">Sin resultados</div>');
		caja.show();
		return;
	}
	empleados.forEach(function (e) {
		var fila = $('<div></div>').text(e.nombre + (e.activo ? '' : '  (inactivo)'));
		if (!e.activo) { fila.addClass('ger-inactivo'); }
		fila.on('click', function () { elegirEmpleado(e); });
		caja.append(fila);
	});
	caja.show();
}

function elegirEmpleado(e) {
	empleadoElegido = e;
	$('#buscarEmpleado').val(e.nombre);
	$('#idEmpleadoElegido').val(e.id);
	$('#listaEmpleados').hide();
}

// ===========================================================================
// GUARDAR
// ===========================================================================

function numeroOCero(id) {
	var texto = $.trim($('#' + id).val());
	return (texto === '' ? '0' : texto);
}

function guardar() {
	var idEmpleado = $('#idEmpleadoElegido').val();
	if (!idEmpleado) {
		Gerencia.avisar('Busque el empleado y elijalo de la lista antes de guardar.');
		return;
	}
	var semana = $.trim($('#semana').val());
	if (!/^\d{4}-\d{2}-\d{2}$/.test(semana)) {
		Gerencia.avisar('La semana tiene que ser una fecha aaaa-mm-dd.');
		return;
	}
	$('#btnGuardar').prop('disabled', true);
	$.post('GerenciaNominaEmpleado', {
		que: 'guardar',
		idempleado: idEmpleado,
		semana: semana,
		basico: numeroOCero('cBasico'),
		variable: numeroOCero('cVariable'),
		seguridad: numeroOCero('cSeguridad'),
		liquidacion: numeroOCero('cLiquidacion')
	}, function (data) {
		$('#btnGuardar').prop('disabled', false);
		if (!Gerencia.respondio(data)) { return; }
		Gerencia.avisar('Guardado.', true);
		$('#avisoGuardar').text('Guardado ' + $('#buscarEmpleado').val() + ' para la semana ' + semana + '.');
		limpiarFormularioCarga();
		consultar();
	}, 'json').fail(function () {
		$('#btnGuardar').prop('disabled', false);
		Gerencia.avisar('No se pudo guardar.');
	});
}

function limpiarFormularioCarga() {
	$('#buscarEmpleado').val('');
	$('#idEmpleadoElegido').val('');
	empleadoElegido = null;
	$('#cBasico, #cVariable, #cSeguridad, #cLiquidacion').val('0');
}

// ===========================================================================
// CONSULTAR / CALCULAR
// ===========================================================================

function consultar() {
	var semana = $.trim($('#semana').val());
	if (!/^\d{4}-\d{2}-\d{2}$/.test(semana)) {
		Gerencia.avisar('La semana tiene que ser una fecha aaaa-mm-dd.');
		return;
	}
	$.getJSON('GerenciaNominaEmpleado', { que: 'consultar', semana: semana }, function (data) {
		if (!Gerencia.respondio(data)) { return; }
		pintarCargados(data.cargados, data.totalcargado);
		pintarReparto(data.reparto);
		pintarPendientes(data.pendientes);
		Gerencia.avisar('');
	}).fail(function () {
		Gerencia.avisar('No se pudo consultar la semana.');
	});
}

function calcular() {
	var semana = $.trim($('#semana').val());
	if (!/^\d{4}-\d{2}-\d{2}$/.test(semana)) {
		Gerencia.avisar('La semana tiene que ser una fecha aaaa-mm-dd.');
		return;
	}
	$('#btnCalcular').prop('disabled', true);
	$('#avisoCalculo').text('Calculando...');
	$.post('GerenciaNominaEmpleado', { que: 'calcular', semana: semana }, function (data) {
		$('#btnCalcular').prop('disabled', false);
		if (!Gerencia.respondio(data)) { $('#avisoCalculo').text(''); return; }
		$('#avisoCalculo').text(data.repartidos + ' empleado(s) repartido(s).');
		consultar();
	}, 'json').fail(function () {
		$('#btnCalcular').prop('disabled', false);
		$('#avisoCalculo').text('');
		Gerencia.avisar('No se pudo calcular el reparto.');
	});
}

// ===========================================================================
// PINTAR
// ===========================================================================

function pesos(v) {
	return ('$' + Math.round(v).toLocaleString('es-CO'));
}

function pintarCargados(lista, total) {
	var cuerpo = $('#tablaCargados tbody');
	cuerpo.empty();
	if (!lista || lista.length === 0) {
		cuerpo.append('<tr><td colspan="8" class="ger-vacio">Nada cargado todavia esta semana.</td></tr>');
		return;
	}
	lista.forEach(function (n) {
		cuerpo.append('<tr>'
			+ '<td>' + n.empleado + '</td>'
			+ '<td class="ger-num">' + pesos(n.basico) + '</td>'
			+ '<td class="ger-num">' + pesos(n.variable) + '</td>'
			+ '<td class="ger-num">' + pesos(n.seguridad) + '</td>'
			+ '<td class="ger-num">' + pesos(n.liquidacion) + '</td>'
			+ '<td class="ger-num"><strong>' + pesos(n.total) + '</strong></td>'
			+ '<td>' + n.origen + '</td>'
			+ '<td>' + n.usuario + '</td>'
			+ '</tr>');
	});
	cuerpo.append('<tr><td><strong>TOTAL</strong></td><td colspan="4"></td>'
		+ '<td class="ger-num"><strong>' + pesos(total) + '</strong></td><td colspan="2"></td></tr>');
}

function pintarReparto(lista) {
	var cuerpo = $('#tablaReparto tbody');
	cuerpo.empty();
	if (!lista || lista.length === 0) {
		cuerpo.append('<tr><td colspan="5" class="ger-vacio">'
			+ 'Sin reparto calculado. Cargue empleados y de clic en "Calcular reparto por biometria".</td></tr>');
		return;
	}
	lista.forEach(function (r) {
		cuerpo.append('<tr>'
			+ '<td>' + r.empleado + '</td>'
			+ '<td>' + r.tienda + '</td>'
			+ '<td class="ger-num">' + r.minutos + '</td>'
			+ '<td class="ger-num">' + r.porcentaje.toFixed(1) + '%</td>'
			+ '<td class="ger-num">' + pesos(r.total) + '</td>'
			+ '</tr>');
	});
}

function pintarPendientes(lista) {
	if (!lista || lista.length === 0) {
		$('#panelPendientes').hide();
		return;
	}
	$('#tituloPendientes').text(lista.length + ' empleado(s) activo(s) cargado(s) sin ninguna marcacion esa semana:');
	var caja = $('#listaPendientes');
	caja.empty();
	lista.forEach(function (n) {
		caja.append('<div>&bull; ' + n.empleado + ' &mdash; ' + pesos(n.total) + '</div>');
	});
	$('#panelPendientes').show();
}

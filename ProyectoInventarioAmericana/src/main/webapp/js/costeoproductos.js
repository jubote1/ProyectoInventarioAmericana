/**
 * Costeo y composicion de productos.
 *
 * La pantalla tiene dos caras: costear lo que sale de la nevera al vender algo,
 * y administrar la composicion que se replica a las tiendas.
 *
 * Replicar es la accion peligrosa: reemplaza la tabla completa de una tienda y
 * con eso cambia cuanto inventario descuenta cada venta de ahi en adelante. Por
 * eso el boton arranca deshabilitado y toca escribir REPLICAR para soltarlo.
 */

var ESCOGIDOS = [];
var PRODUCTOS = [];

$(document).ready(function () {
	cargarCatalogo();
	cargarTiendas();

	//El boton de replicar no se habilita hasta que se escriba la palabra. No
	//es burocracia: es la unica accion de esta pantalla que se puede hacer sin
	//querer y que deja una tienda descontando mal.
	$('#confirmaReplica').on('input', function () {
		$('#btnReplicar').prop('disabled', $(this).val().trim().toUpperCase() !== 'REPLICAR');
	});
});

// ==========================================================================
// Catalogo y tiendas
// ==========================================================================

function cargarCatalogo() {
	$.getJSON(server + 'ComposicionProducto', { que: 'catalogo' }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			avisar('#cp-aviso-costeo', 'No se pudo cargar el catalogo de productos.', 'mal');
			return;
		}
		PRODUCTOS = data.productos || [];
		$('#res-maestro').text(numero(data.filasmaestro || 0));

		var html = '';
		for (var i = 0; i < PRODUCTOS.length; i++) {
			var p = PRODUCTOS[i];
			html += '<option value="' + p.idproducto + '">' + escapar(p.descripcion)
				+ (p.tamano ? ' (' + escapar(p.tamano) + ')' : '')
				+ ' - ' + p.insumos + ' insumo(s)</option>';
		}
		$('#selectProducto').html(html);

		if (PRODUCTOS.length === 0) {
			avisar('#cp-aviso-costeo', 'El maestro esta vacio. Vaya a "Composicion por tienda" y '
				+ 'traiga la composicion desde una tienda de referencia.', 'ojo');
		}
	}).fail(function () {
		avisar('#cp-aviso-costeo', 'No hubo respuesta del servidor.', 'mal');
	});
}

function cargarTiendas() {
	$.getJSON(server + 'GetTiendas', function (data) {
		var html = '';
		for (var i = 0; i < data.length; i++) {
			//Bodega no tiene composicion de tienda: su inventario es el de la
			//bodega misma.
			if (data[i].id === 12) {
				continue;
			}
			html += '<option value="' + data[i].id + '">' + escapar(data[i].nombre) + '</option>';
		}
		$('#selectTiendaTraer, #selectTiendaComparar, #selectTiendaReplicar').html(html);
	});
}

// ==========================================================================
// Costear
// ==========================================================================

function adicionarProducto() {
	var id = $('#selectProducto').val();
	if (!id) {
		return;
	}
	//Se permite el mismo producto dos veces a proposito: una pizza con doble
	//queso se arma escogiendo el adicional dos veces.
	var p = buscarProducto(id);
	ESCOGIDOS.push({ id: id, texto: p ? p.descripcion : ('Producto ' + id) });
	pintarEscogidos();
	costear();
}

function quitarProducto(indice) {
	ESCOGIDOS.splice(indice, 1);
	pintarEscogidos();
	costear();
}

function limpiarEscogidos() {
	ESCOGIDOS = [];
	pintarEscogidos();
	costear();
}

function buscarProducto(id) {
	for (var i = 0; i < PRODUCTOS.length; i++) {
		if (String(PRODUCTOS[i].idproducto) === String(id)) {
			return (PRODUCTOS[i]);
		}
	}
	return (null);
}

function pintarEscogidos() {
	if (ESCOGIDOS.length === 0) {
		$('#escogidos').html('<span class="cp-vacio">Todavia no ha escogido nada.</span>');
		return;
	}
	var html = '';
	for (var i = 0; i < ESCOGIDOS.length; i++) {
		html += '<span class="cp-ficha">' + escapar(ESCOGIDOS[i].texto)
			+ '<span class="cp-quitar" onclick="quitarProducto(' + i + ')" title="Quitar">&times;</span></span>';
	}
	$('#escogidos').html(html);
}

function costear() {
	if (ESCOGIDOS.length === 0) {
		limpiarCosteo();
		return;
	}
	var ids = [];
	for (var i = 0; i < ESCOGIDOS.length; i++) {
		ids.push(ESCOGIDOS[i].id);
	}

	$.getJSON(server + 'ComposicionProducto', { que: 'costear', idproductos: ids.join(',') },
		function (data) {
			if (!data || data.respuesta !== 'OK') {
				avisar('#cp-aviso-costeo', 'No se pudo costear.', 'mal');
				return;
			}
			pintarCosteo(data);
		}).fail(function () {
			avisar('#cp-aviso-costeo', 'No hubo respuesta del servidor.', 'mal');
		});
}

function pintarCosteo(data) {
	var lineas = data.lineas || [];
	var total = data.total || 0;

	$('#res-total').text(pesos(total));
	$('#res-insumos').text(lineas.length);
	$('#res-problemas').text((data.sincosto || 0) + (data.sinhomologar || 0));
	$('#res-total-pie').text(ESCOGIDOS.length + ' producto(s) escogido(s)');

	//Un total incompleto tiene que decirlo. Si no, se lee como el costo real y
	//alguien fija un precio con el.
	if ((data.sincosto || 0) + (data.sinhomologar || 0) > 0) {
		avisar('#cp-aviso-costeo', 'El total esta incompleto: ' + (data.sincosto || 0)
			+ ' insumo(s) sin costo cargado y ' + (data.sinhomologar || 0)
			+ ' sin homologar en el maestro. Aparecen marcados abajo.', 'ojo');
	} else {
		esconder('#cp-aviso-costeo');
	}

	if ($.fn.DataTable.isDataTable('#grid-costeo')) {
		$('#grid-costeo').DataTable().destroy();
	}

	var cuerpo = '';
	for (var i = 0; i < lineas.length; i++) {
		var l = lineas[i];
		var malo = l.sincosto || l.sinhomologar;
		var porcentaje = total !== 0 ? (l.costo / total * 100) : 0;
		cuerpo += '<tr>'
			+ '<td' + (malo ? ' class="cp-alerta"' : '') + '>' + escapar(l.insumo)
			+ (l.sinhomologar ? ' (sin homologar)' : (l.sincosto ? ' (sin costo)' : '')) + '</td>'
			+ '<td class="cp-num">' + numero(l.cantidad) + '</td>'
			+ '<td>' + escapar(l.unidad) + '</td>'
			+ '<td class="cp-num">' + pesosFinos(l.unitario) + '</td>'
			+ '<td class="cp-num">' + pesos(l.costo) + '</td>'
			+ '<td class="cp-num">' + numero(Math.round(porcentaje * 10) / 10) + ' %</td>'
			+ '</tr>';
	}
	$('#grid-costeo tbody').html(cuerpo);
	$('#costeo-sub').text(lineas.length + ' insumo(s), de mayor a menor.');

	$('#grid-costeo').DataTable({
		"paging": false, "searching": false, "info": false, "order": [],
		"language": { "zeroRecords": "sin insumos" }
	});
}

function limpiarCosteo() {
	$('#res-total').text('$ 0');
	$('#res-insumos').text('0');
	$('#res-problemas').text('0');
	$('#res-total-pie').text('escoja los productos');
	if ($.fn.DataTable.isDataTable('#grid-costeo')) {
		$('#grid-costeo').DataTable().destroy();
	}
	$('#grid-costeo tbody').html('');
	$('#costeo-sub').text('De mayor a menor.');
	esconder('#cp-aviso-costeo');
}

// ==========================================================================
// Composicion
// ==========================================================================

function traerDeTienda() {
	var id = $('#selectTiendaTraer').val();
	var nombre = $('#selectTiendaTraer option:selected').text();
	$.confirm({
		title: 'Traer al maestro',
		content: 'Se va a reemplazar el maestro con la composicion de <b>' + escapar(nombre)
			+ '</b>. Ninguna tienda se toca.',
		buttons: {
			confirmar: {
				text: 'Traer', btnClass: 'btn-primary',
				action: function () { traerDeTiendaConfirmado(id); }
			},
			cancelar: { text: 'Cancelar' }
		}
	});
}

function traerDeTiendaConfirmado(id) {
	$.getJSON(server + 'ComposicionProducto', { que: 'traer', idtienda: id }, function (data) {
		if (!data) {
			avisar('#cp-aviso-maestro', 'No hubo respuesta del servidor.', 'mal');
			return;
		}
		if (data.respuesta === 'OK') {
			var texto = 'Maestro actualizado desde ' + data.tienda + ': ' + numero(data.filas)
				+ ' filas de composicion y ' + numero(data.productos) + ' productos.';
			if (data.repetidos > 0) {
				texto += ' OJO: ' + data.repetidos + ' par(es) producto-insumo venian repetidos en esa '
					+ 'tienda y se guardaron sumados, que es como los descuenta el POS hoy. Vale la '
					+ 'pena revisar cual era el bueno.';
			}
			avisar('#cp-aviso-maestro', texto, data.repetidos > 0 ? 'ojo' : 'ok');
			cargarCatalogo();
		} else {
			avisar('#cp-aviso-maestro', mensajeError(data.respuesta), 'mal');
		}
	}).fail(function () {
		avisar('#cp-aviso-maestro', 'No hubo respuesta del servidor.', 'mal');
	});
}

function compararTienda() {
	var id = $('#selectTiendaComparar').val();
	$('#btnComparar').val('Comparando...').prop('disabled', true);

	$.getJSON(server + 'ComposicionProducto', { que: 'comparar', idtienda: id }, function (data) {
		$('#btnComparar').val('Comparar').prop('disabled', false);
		if (!data || data.respuesta !== 'OK') {
			$('#resumen-diferencias').text(mensajeError(data ? data.respuesta : ''));
			return;
		}
		pintarDiferencias(data);
	}).fail(function () {
		$('#btnComparar').val('Comparar').prop('disabled', false);
		$('#resumen-diferencias').text('No hubo respuesta del servidor.');
	});
}

function pintarDiferencias(data) {
	var filas = data.diferencias || [];

	if (filas.length === 0) {
		$('#resumen-diferencias').text(data.tienda + ' esta igual al maestro.');
	} else {
		$('#resumen-diferencias').text(data.tienda + ': ' + data.faltan + ' le falta(n), '
			+ data.sobran + ' le sobra(n), ' + data.distintas + ' con cantidad distinta.');
	}

	if ($.fn.DataTable.isDataTable('#grid-diferencias')) {
		$('#grid-diferencias').DataTable().destroy();
	}

	var cuerpo = '';
	for (var i = 0; i < filas.length; i++) {
		var f = filas[i];
		cuerpo += '<tr>'
			+ '<td>' + escapar(f.producto) + '</td>'
			+ '<td>' + escapar(f.insumo) + '</td>'
			+ '<td>' + etiquetaTipo(f.tipo) + '</td>'
			+ '<td class="cp-num">' + (f.tipo === 'SOBRA' ? '-' : numero(f.maestro)) + '</td>'
			+ '<td class="cp-num">' + (f.tipo === 'FALTA' ? '-' : numero(f.tienda)) + '</td>'
			+ '</tr>';
	}
	$('#grid-diferencias tbody').html(cuerpo);

	$('#grid-diferencias').DataTable({
		"paging": true, "pageLength": 25, "searching": true, "info": true, "order": [],
		"language": {
			"search": "Buscar:", "lengthMenu": "Mostrar _MENU_ diferencias",
			"info": "_START_ a _END_ de _TOTAL_", "infoEmpty": "sin diferencias",
			"zeroRecords": "nada que coincida",
			"paginate": { "first": "Primera", "last": "Ultima", "next": "Siguiente", "previous": "Anterior" }
		}
	});
}

function etiquetaTipo(tipo) {
	if (tipo === 'FALTA') {
		return ('<span class="cp-etiqueta cp-t-falta">LE FALTA</span>');
	}
	if (tipo === 'SOBRA') {
		return ('<span class="cp-etiqueta cp-t-sobra">LE SOBRA</span>');
	}
	return ('<span class="cp-etiqueta cp-t-distinta">CANTIDAD DISTINTA</span>');
}

function replicarATienda() {
	var id = $('#selectTiendaReplicar').val();
	var nombre = $('#selectTiendaReplicar option:selected').text();

	$.confirm({
		title: 'Replicar a ' + escapar(nombre),
		content: 'Se borra la composicion que tiene <b>' + escapar(nombre) + '</b> y se escribe la '
			+ 'del maestro.<br><br>Desde ese momento esa tienda descuenta el inventario con estas '
			+ 'cantidades, y su varianza se va a medir contra ellas.',
		buttons: {
			confirmar: {
				text: 'Si, replicar', btnClass: 'btn-danger',
				action: function () { replicarConfirmado(id); }
			},
			cancelar: { text: 'Cancelar' }
		}
	});
}

function replicarConfirmado(id) {
	$('#btnReplicar').val('Replicando...').prop('disabled', true);

	$.getJSON(server + 'ComposicionProducto', { que: 'replicar', idtienda: id }, function (data) {
		$('#btnReplicar').val('Replicar');
		//Se vuelve a pedir la palabra para la siguiente: replicar dos tiendas
		//seguidas de un clic es justo como se replica a la que no era.
		$('#confirmaReplica').val('');
		$('#btnReplicar').prop('disabled', true);

		if (!data) {
			avisar('#cp-aviso-replica', 'No hubo respuesta del servidor.', 'mal');
			return;
		}
		if (data.respuesta === 'OK') {
			avisar('#cp-aviso-replica', data.tienda + ' quedo replicada: tenia ' + numero(data.antes)
				+ ' filas y quedo con ' + numero(data.despues) + '.', 'ok');
		} else {
			avisar('#cp-aviso-replica', mensajeError(data.respuesta), 'mal');
		}
	}).fail(function () {
		$('#btnReplicar').val('Replicar').prop('disabled', true);
		$('#confirmaReplica').val('');
		avisar('#cp-aviso-replica', 'No hubo respuesta del servidor.', 'mal');
	});
}

// ==========================================================================
// Utilidades
// ==========================================================================

function mensajeError(codigo) {
	if (codigo === 'SINSESION') {
		return ('Su sesion se vencio. Vuelva a entrar.');
	}
	if (codigo === 'SINHOST') {
		return ('Esa tienda no tiene host de base de datos configurado. Poblado y Medayoung estan asi.');
	}
	if (codigo === 'NOCONECTA') {
		return ('No se pudo conectar a la base de esa tienda.');
	}
	if (codigo === 'VACIO') {
		return ('El maestro esta vacio. Traiga primero la composicion de una tienda de referencia.');
	}
	return ('No se pudo completar la operacion.');
}

function avisar(donde, texto, clase) {
	$(donde).text(texto)
		.removeClass('cp-aviso-ok cp-aviso-mal cp-aviso-ojo')
		.addClass('cp-aviso-' + clase)
		.show();
}

function esconder(donde) {
	$(donde).hide();
}

function pesos(valor) {
	var n = Math.round(valor || 0);
	return ((n < 0 ? '-$ ' : '$ ') + numeroEntero(Math.abs(n)));
}

/** Para el costo unitario, que en gramos es de centavos. */
function pesosFinos(valor) {
	var v = valor || 0;
	if (v !== 0 && Math.abs(v) < 10) {
		return ('$ ' + (Math.round(v * 1000) / 1000).toString().replace('.', ','));
	}
	return (pesos(v));
}

function numero(valor) {
	var n = Math.round((valor || 0) * 1000) / 1000;
	var partes = n.toString().split('.');
	partes[0] = numeroEntero(parseInt(partes[0], 10) || 0);
	return (partes.length > 1 ? partes[0] + ',' + partes[1] : partes[0]);
}

function numeroEntero(valor) {
	return (String(valor).replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function escapar(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;'));
}

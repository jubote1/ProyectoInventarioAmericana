/**
 * Maestro de insumos.
 *
 * La tabla ahora se llena desde MaestroInsumos y no desde CRUDInsumo con
 * idoperacion=6: esa operacion solo devolvia cinco columnas -id, nombre,
 * unidad, categoria y costo- y lo que hacia falta ver no estaba. Un insumo sin
 * umbral de varianza o sin homologar en las tiendas se veia igual de sano que
 * los demas, y es justo lo que lo deja por fuera de todos los informes.
 *
 * El alta y la edicion siguen llamando a CRUDInsumo con las mismas operaciones
 * 1, 2 y 4 y los mismos parametros de siempre. Eso no se toco: lo que cambia es
 * lo que se ve, no la forma de guardar.
 *
 * El grupo de varianza se guarda aparte, con MaestroInsumos?que=grupo, y no
 * como un campo mas de la edicion. La operacion 2 manda once campos de una, asi
 * que marcar un insumo como caro obligaria a reenviar el costo y la categoria,
 * y cualquier combo que llegara vacio pisaria el valor bueno.
 */

var server;
var table;
var INSUMOS = [];
var FILTRO = '';

$(document).ready(function () {

	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));

	$('#userForm').bootstrapValidator({
		framework: 'bootstrap',
		icon: {
			valid: 'glyphicon glyphicon-ok',
			invalid: 'glyphicon glyphicon-remove',
			validating: 'glyphicon glyphicon-refresh'
		},
		fields: {
			nombreinsumoedit: {
				validators: {
					notEmpty: { message: 'El nombre del insumo es requerido' }
				}
			}
		}
	});

	inicializarSelects();
	pintarInsumos();
	setInterval('validarVigenciaLogueo()', 600000);
});

// ==========================================================================
// La sesion
// ==========================================================================

function validarVigenciaLogueo() {
	var respuesta = '';
	$.ajax({
		url: server + 'ValidarUsuarioAplicacion',
		dataType: 'text',
		type: 'post',
		async: false,
		success: function (data) { respuesta = data; }
	});
	//Antes esto leia data[0].respuesta con dataType json, pero el servlet
	//responde texto plano: la lectura fallaba, la excepcion se comia el
	//resultado y el switch caia siempre en default. Es decir que cada diez
	//minutos sacaba al usuario de la pantalla aunque la sesion estuviera viva.
	if (respuesta !== 'OK' && respuesta !== 'OKA') {
		location.href = server + 'Index.html';
	}
}

// ==========================================================================
// La tabla
// ==========================================================================

function pintarInsumos() {
	$.getJSON(server + 'MaestroInsumos', function (data) {
		if (!data || data.respuesta !== 'OK') {
			avisar('No se pudo cargar el maestro de insumos.', false);
			return;
		}
		INSUMOS = data.insumos || [];
		pintarResumen();
		dibujarTabla();
	}).fail(function () {
		avisar('No hubo respuesta del servidor.', false);
	});
}

/** Un insumo que no va a aparecer bien en los informes. */
function hayQueRevisar(ins) {
	return (ins.costounidad <= 0 || !ins.tieneumbral || ins.tiendas === 0 || costoRaro(ins));
}

/**
 * Costo que no puede ser.
 *
 * Masa Baguette quedo con "equivale a 1 gramo", lo que la deja en 1.592 el
 * gramo: 1.592.000 el kilo. No se corrige solo, se muestra para que alguien
 * lo arregle.
 */
function costoRaro(ins) {
	if (ins.unidadmedida !== 'gramos') {
		return (false);
	}
	return (ins.embalajecosto <= 1 || costoUnitario(ins) * 1000 > 200000);
}

/** El costo llevado a gramo, unidad o paquete, segun corresponda. */
function costoUnitario(ins) {
	var divisor = ins.embalajecosto > 0 ? ins.embalajecosto : 1;
	return (ins.costounidad / divisor);
}

function pintarResumen() {
	var caros = 0, carnes = 0, revisar = 0, sinCosto = 0;
	for (var i = 0; i < INSUMOS.length; i++) {
		if (INSUMOS[i].grupovarianza === 'CAROS') { caros++; }
		if (INSUMOS[i].grupovarianza === 'CARNES') { carnes++; }
		if (hayQueRevisar(INSUMOS[i])) { revisar++; }
		if (INSUMOS[i].costounidad <= 0) { sinCosto++; }
	}
	$('#res-total').text(INSUMOS.length);
	$('#res-total-pie').text(sinCosto + ' sin costo cargado');
	$('#res-caros').text(caros);
	$('#res-carnes').text(carnes);
	$('#res-revisar').text(revisar);
}

function dibujarTabla() {
	if ($.fn.DataTable.isDataTable('#grid-insumos')) {
		$('#grid-insumos').DataTable().destroy();
	}

	var cuerpo = '';
	var mostrados = 0;
	for (var i = 0; i < INSUMOS.length; i++) {
		var ins = INSUMOS[i];
		if (!pasaFiltro(ins)) {
			continue;
		}
		mostrados++;

		var equivale = ins.unidadmedida === 'gramos'
			? numero(ins.embalajecosto) + ' g'
			: '1 ' + escapar(ins.unidadmedida);

		var porKilo = ins.unidadmedida === 'gramos'
			? pesos(costoUnitario(ins) * 1000) + ' / kilo'
			: pesos(ins.costounidad) + ' / ' + escapar(ins.unidadmedida);

		cuerpo += '<tr>'
			+ '<td>' + ins.idinsumo + '</td>'
			+ '<td>' + escapar(ins.nombre) + '</td>'
			+ '<td>' + escapar(ins.unidadmedida) + '</td>'
			+ '<td>' + escapar(ins.categoria) + '</td>'
			+ '<td>' + selectorGrupo(ins) + '</td>'
			+ '<td class="ins-num' + (ins.costounidad <= 0 ? ' ins-alerta' : '') + '">'
			+ (ins.costounidad <= 0 ? 'sin costo' : pesos(ins.costounidad)) + '</td>'
			+ '<td class="ins-num">' + equivale + '</td>'
			+ '<td class="ins-num' + (costoRaro(ins) ? ' ins-alerta' : '') + '">' + porKilo + '</td>'
			+ '<td class="ins-num' + (ins.tieneumbral ? '' : ' ins-flojo') + '">'
			+ (ins.tieneumbral ? numero(ins.umbral) : 'sin umbral') + '</td>'
			+ '<td class="ins-num' + (ins.tiendas === 0 ? ' ins-alerta' : '') + '">'
			+ numero(ins.tiendas) + '</td>'
			+ '<td><button type="button" onclick="editarInsumo(' + ins.idinsumo + ')" '
			+ 'class="btn btn-default btn-xs"><i class="fas fa-edit"></i> Editar</button></td>'
			+ '</tr>';
	}

	$('#grid-insumos tbody').html(cuerpo);
	$('#ins-sub').text(mostrados + ' de ' + INSUMOS.length + ' insumo(s). '
		+ 'El grupo se cambia aqui mismo y se guarda solo; lo demas con Editar.');

	$('#grid-insumos').DataTable({
		"paging": true,
		"pageLength": 25,
		"searching": true,
		"info": true,
		"order": [],
		//La columna del grupo tiene un combo adentro: ordenarla por su HTML no
		//significa nada y confunde a quien la use.
		"columnDefs": [{ "orderable": false, "targets": [4, 10] }],
		"language": {
			"search": "Buscar:",
			"lengthMenu": "Mostrar _MENU_ insumos",
			"info": "_START_ a _END_ de _TOTAL_",
			"infoEmpty": "sin insumos",
			"zeroRecords": "nada que coincida",
			"paginate": { "first": "Primera", "last": "Ultima", "next": "Siguiente", "previous": "Anterior" }
		}
	});
}

function pasaFiltro(ins) {
	if (FILTRO === '') { return (true); }
	if (FILTRO === 'REVISAR') { return (hayQueRevisar(ins)); }
	if (FILTRO === 'SINGRUPO') { return (ins.grupovarianza === ''); }
	return (ins.grupovarianza === FILTRO);
}

function selectorGrupo(ins) {
	var opciones = [['', '(sin grupo)'], ['CAROS', 'Insumos costosos'], ['CARNES', 'Carnes']];
	var html = '<select class="form-control ins-selgrupo sel-grupo" data-id="' + ins.idinsumo + '">';
	for (var i = 0; i < opciones.length; i++) {
		html += '<option value="' + opciones[i][0] + '"'
			+ (ins.grupovarianza === opciones[i][0] ? ' selected' : '') + '>'
			+ opciones[i][1] + '</option>';
	}
	html += '</select>';
	return (html);
}

$(document).on('change', '.sel-grupo', function () {
	var idInsumo = $(this).attr('data-id');
	var grupo = $(this).val();
	var combo = $(this);
	combo.prop('disabled', true);

	$.getJSON(server + 'MaestroInsumos', { que: 'grupo', idinsumo: idInsumo, grupo: grupo },
		function (data) {
			combo.prop('disabled', false);
			if (data && data.respuesta === 'OK') {
				//Se actualiza la copia local para que las tarjetas de arriba
				//cuadren sin tener que volver a pedir los 120 insumos.
				for (var i = 0; i < INSUMOS.length; i++) {
					if (String(INSUMOS[i].idinsumo) === String(idInsumo)) {
						INSUMOS[i].grupovarianza = grupo;
					}
				}
				pintarResumen();
				avisar('Grupo guardado.', true);
			} else {
				avisar('No se pudo guardar el grupo.', false);
				pintarInsumos();
			}
		}).fail(function () {
			combo.prop('disabled', false);
			avisar('No hubo respuesta del servidor al guardar el grupo.', false);
		});
});

$(document).on('click', '.ins-filtros button', function () {
	FILTRO = $(this).attr('data-filtro');
	$('.ins-filtros button').removeClass('btn-primary').addClass('btn-default');
	$(this).removeClass('btn-default').addClass('btn-primary');
	dibujarTabla();
});

// ==========================================================================
// Alta y edicion. Se conservan tal cual estaban.
// ==========================================================================

function guardarInsumo() {
	var nombre = encodeURIComponent($('#nombreinsumo').val());
	var unidadMedida = $('#selectunidadmedida').val();
	var manejaCanastas = $('#selectmanejacanastas').val();
	var cantidadCanasta = $('#cantidadcanastas').val();
	var nombreContenedor = $('#selectnombrecontenedor').val();
	var categoria = encodeURIComponent($('#selectcategoria').val());
	var controlCantidad = $('#selectcontrolcantidad').val();
	var costoUnidad = $('#costounidad').val();
	var controlTienda = "N";

	$.getJSON(server + 'CRUDInsumo?idoperacion=1&nombreinsumo=' + nombre
		+ "&unidadmedida=" + unidadMedida + "&manejacanastas=" + manejaCanastas
		+ "&cantidadcanasta=" + cantidadCanasta + "&nombrecontenedor=" + nombreContenedor
		+ "&categoria=" + categoria + "&controlcantidad=" + controlCantidad
		+ "&costounidad=" + costoUnidad + "&controltienda=" + controlTienda, function (data) {
			if (data && data.resultado === 'COSTOMALO') {
				avisar('Revise el costo: escriba solo numeros, con coma para los decimales '
					+ '(por ejemplo 20.200 o 1377,5). No se creo el insumo.', false);
				return;
			}
			//Antes no refrescaba: el insumo quedaba creado pero la tabla seguia
			//igual, y daba la impresion de que no habia guardado.
			$('#addData').modal('hide');
			pintarInsumos();
			avisar('Insumo creado.', true);
		});
}

function editarInsumo(idInsumo) {
	$.ajax({
		url: server + 'CRUDInsumo?idoperacion=4&idinsumo=' + idInsumo,
		dataType: 'json',
		async: false,
		success: function (data) {
			var respuesta = data;
			$("#idinsumoedit").val(respuesta.idinsumo);
			$("#nombreinsumoedit").val(respuesta.nombreinsumo);
			$("#selectunidadmedidaedit").val(respuesta.unidadmedida.trim());
			$("#selectmanejacanastasedit").val(respuesta.manejacanastas.trim());
			$("#cantidadcanastasedit").val(respuesta.cantidadcanasta);
			$("#selectnombrecontenedoredit").val(respuesta.nombrecontenedor);
			$("#selectcategoriaedit").val(respuesta.categoria);
			$("#selectcontrolcantidadedit").val(respuesta.controlcantidad);
			$("#costounidadedit").val(respuesta.costounidad);
			$("#embalajecosto").val(respuesta.embalajecosto);
			$('#controltienda').prop('checked', respuesta.controltienda === 'S');

			if ($("#selectunidadmedidaedit").val() === "unidad") {
				$("#infocostounidad").val("Por unidad el costo es " + respuesta.costounidad);
			} else if ($("#selectunidadmedidaedit").val() === "gramos") {
				$("#infocostounidad").val("Por " + respuesta.embalajecosto + " gramos, el costo es "
					+ respuesta.costounidad);
			} else if ($("#selectunidadmedidaedit").val() === "paquete") {
				$("#infocostounidad").val("Por " + respuesta.embalajecosto + " unidades, el costo es "
					+ respuesta.costounidad);
			}

			bootbox.dialog({
				title: 'Editar insumo',
				message: $('#userForm'),
				show: false
			}).on('shown.bs.modal', function () {
				$('#userForm').show().bootstrapValidator('resetForm');
			}).on('hide.bs.modal', function () {
				//Bootbox borra el cuerpo del modal al cerrarlo, y el formulario
				//va adentro: hay que devolverlo al body o la segunda edicion
				//abre un modal vacio.
				$('#userForm').hide().appendTo('body');
			}).modal('show');
		}
	});
}

function confirmarEditarInsumo() {
	var idInsumo = $('#idinsumoedit').val();
	var nombreEncode = encodeURIComponent($('#nombreinsumoedit').val());
	var unidadMedida = $('#selectunidadmedidaedit').val();
	var manejaCanastas = $('#selectmanejacanastasedit').val();
	var cantidadCanasta = $('#cantidadcanastasedit').val();
	var nombreContenedor = $('#selectnombrecontenedoredit').val();
	if (nombreContenedor === 'null' || nombreContenedor === null) {
		nombreContenedor = "";
	}
	var categoriaEncode = encodeURIComponent($('#selectcategoriaedit').val());
	var controlCantidad = $('#selectcontrolcantidadedit').val();
	var costoUnidad = $('#costounidadedit').val();
	var controlTienda = $('#controltienda').is(':checked') ? "S" : "N";

	$.ajax({
		url: server + 'CRUDInsumo?idoperacion=2&nombreinsumo=' + nombreEncode
			+ "&unidadmedida=" + unidadMedida + "&manejacanastas=" + manejaCanastas
			+ "&cantidadcanasta=" + cantidadCanasta + "&nombrecontenedor=" + nombreContenedor
			+ "&categoria=" + categoriaEncode + "&controlcantidad=" + controlCantidad
			+ "&costounidad=" + costoUnidad + "&idinsumo=" + idInsumo
			+ "&controltienda=" + controlTienda,
		dataType: 'json',
		async: false,
		success: function (data) {
			//El costo malo no se guarda. Antes se decia "actualizado" pasara lo
			//que pasara, y como el costo quedaba en cero el mensaje era mentira
			//justo cuando mas importaba.
			if (data && data.resultado === 'COSTOMALO') {
				bootbox.alert('Revise el costo: escriba solo numeros, con coma para los decimales '
					+ '(por ejemplo 20.200 o 1377,5). NO se guardaron los cambios.');
				return;
			}
			pintarInsumos();
			$('#userForm').parents('.bootbox').modal('hide');
			bootbox.alert('El insumo ha sido actualizado');
		}
	});
}

function habDeshabCanastas() {
	$('#cantidadcanastas').prop('disabled', $('#selectmanejacanastas').val() !== 'S');
}

function habDeshabCanastasEdit() {
	$('#cantidadcanastasedit').prop('disabled', $('#selectmanejacanastasedit').val() !== 'S');
}

// ==========================================================================
// Los combos
// ==========================================================================

function inicializarSelects() {
	var str = '';
	str += '<option value="unidad">unidad</option>';
	str += '<option value="gramos">gramos</option>';
	str += '<option value="paquete">paquete</option>';
	str += '<option value="unidades">unidades</option>';
	$('#selectunidadmedida').html(str);
	$('#selectunidadmedidaedit').html(str);

	str = '<option value="S">S</option><option value="N">N</option>';
	$('#selectmanejacanastas').html(str);
	$('#selectmanejacanastasedit').html(str);

	str = '<option value="no"></option>';
	str += '<option value="Paq">Paq</option>';
	str += '<option value="Can">Can</option>';
	str += '<option value="Frasco">Frasco</option>';
	str += '<option value="Bolsa">Bolsa</option>';
	$('#selectnombrecontenedor').html(str);
	$('#selectnombrecontenedoredit').html(str);

	str = '<option value="Bebidas">Bebidas</option>';
	str += '<option value="Insumos Basicos">Insumos Basicos</option>';
	str += '<option value="Insumos Carnicos">Insumos Carnicos</option>';
	str += '<option value="Insumos no Carnicos">Insumos no Carnicos</option>';
	str += '<option value="Miscelanea">Miscelanea</option>';
	str += '<option value="Limpieza">Limpieza</option>';
	str += '<option value="Otros Alimentos">Otros Alimentos</option>';
	$('#selectcategoria').html(str);
	$('#selectcategoriaedit').html(str);

	str = '<option value="N">N</option><option value="S">S</option>';
	$('#selectcontrolcantidad').html(str);
	$('#selectcontrolcantidadedit').html(str);
}

// ==========================================================================
// Utilidades
// ==========================================================================

function avisar(texto, bien) {
	$('#ins-aviso').text(texto)
		.removeClass('ins-aviso-ok ins-aviso-mal')
		.addClass(bien ? 'ins-aviso-ok' : 'ins-aviso-mal')
		.show();
	if (bien) {
		setTimeout(function () { $('#ins-aviso').fadeOut(); }, 2500);
	}
}

function pesos(valor) {
	var n = Math.round(valor || 0);
	var signo = n < 0 ? '-' : '';
	return (signo + '$ ' + numero(Math.abs(n)));
}

function numero(valor) {
	var n = Math.round((valor || 0) * 100) / 100;
	var partes = n.toString().split('.');
	partes[0] = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
	return (partes.join(','));
}

function escapar(texto) {
	if (texto === null || texto === undefined) { return (''); }
	return (String(texto)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;'));
}

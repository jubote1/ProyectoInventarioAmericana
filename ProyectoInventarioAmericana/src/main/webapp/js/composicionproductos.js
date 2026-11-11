/**
 * Composición de Productos y Ficha Técnica - Pizza Americana (Versión Calidad y Logística)
 * Vista técnica sin exposición de costos, precios ni márgenes financieros.
 */

var PRODUCTOS = [];
var TAMANOS_MAP = {
	'XL': { id: 6, code: 'XL', desc: 'Pizza XL', nombre: 'Extra Grande', porciones: '10 a 12 Porciones', diametro: '40 cm' },
	'GD': { id: 7, code: 'GD', desc: 'Pizza GD', nombre: 'Grande', porciones: '8 a 10 Porciones', diametro: '35 cm' },
	'MD': { id: 8, code: 'MD', desc: 'Pizza MD', nombre: 'Mediana', porciones: '6 a 8 Porciones', diametro: '30 cm' },
	'PZ': { id: 9, code: 'PZ', desc: 'Pizza PZ', nombre: 'Personal', porciones: '4 Porciones', diametro: '22 cm' }
};

var STUDIO_STATE = {
	tamano: 'XL',
	saborNombre: 'Hawaiana',
	saborId: 303,
	adicionesIds: [],
	recetaOriginal: [],
	recetaSimulada: []
};

$(document).ready(function () {
	var hoy = new Date();
	var fechaStr = hoy.getDate() + '/' + (hoy.getMonth() + 1) + '/' + hoy.getFullYear();
	$('#print-fecha').text(fechaStr);

	cargarCatalogo();
	cargarTiendas();
});

function cargarCatalogo() {
	$.getJSON(server + 'ComposicionProducto', { que: 'catalogo' }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			$.alert('No se pudo cargar el catálogo de productos');
			return;
		}
		PRODUCTOS = data.productos || [];
		$('#res-maestro').text((data.filasmaestro || 0).toLocaleString('es-CO'));

		poblarSaboresPorTamano(STUDIO_STATE.tamano);
		poblarSelectAdiciones();
		pintarCatalogoCompleto(PRODUCTOS);
	}).fail(function () {
		$.alert('Error de conexión al cargar catálogo.');
	});
}

function cargarTiendas() {
	$.getJSON(server + 'GetTiendas', function (data) {
		var ops = '<option value="">Seleccione una tienda...</option>';
		for (var i = 0; i < data.length; i++) {
			ops += '<option value="' + data[i].id + '">' + data[i].nombre + '</option>';
		}
		$('#selectTiendaTraer').html(ops);
		$('#selectTiendaComparar').html(ops);
	});
}

function seleccionarTamano(tamano) {
	STUDIO_STATE.tamano = tamano;
	$('.size-card').removeClass('active');
	$('#card-tamano-' + tamano).addClass('active');

	var tInfo = TAMANOS_MAP[tamano];
	$('#info-tamano-txt').text(tInfo.nombre + ' (' + tInfo.diametro + ')');
	$('#info-porciones-txt').text(tInfo.porciones);
	$('#info-empaque-txt').text('Caja Pizza ' + tamano);

	poblarSaboresPorTamano(tamano);
}

function poblarSaboresPorTamano(tamano) {
	var pizzasTamano = PRODUCTOS.filter(function (p) {
		return p.tipo === 'PIZZA' && p.tamano === tamano;
	});

	$('#info-tamano-sabor').text('Mostrando ' + pizzasTamano.length + ' pizzas tamaño ' + tamano);

	var html = '';
	var selectedSabor = null;
	for (var i = 0; i < pizzasTamano.length; i++) {
		var p = pizzasTamano[i];
		var nombreLimpio = p.descripcion.replace(new RegExp('\\s+' + tamano + '$', 'i'), '').trim();
		var esActive = (p.idproducto === STUDIO_STATE.saborId) ||
			(!selectedSabor && nombreLimpio.toLowerCase().indexOf('hawaiana') >= 0);

		if (esActive && !selectedSabor) {
			selectedSabor = p;
		}

		html += '<span class="flavor-pill ' + (esActive ? 'active' : '') + '" '
			+ 'data-id="' + p.idproducto + '" '
			+ 'data-nombre="' + nombreLimpio + '" '
			+ 'onclick="seleccionarSabor(' + p.idproducto + ', \'' + nombreLimpio.replace(/'/g, "\\'") + '\')">'
			+ nombreLimpio
			+ '</span>';
	}

	if (!selectedSabor && pizzasTamano.length > 0) {
		selectedSabor = pizzasTamano[0];
	}

	$('#pills-sabores-container').html(html);

	if (selectedSabor) {
		seleccionarSabor(selectedSabor.idproducto, selectedSabor.descripcion.replace(new RegExp('\\s+' + tamano + '$', 'i'), '').trim());
	}
}

function seleccionarSabor(idProducto, nombreSabor) {
	STUDIO_STATE.saborId = idProducto;
	STUDIO_STATE.saborNombre = nombreSabor;

	$('.flavor-pill').removeClass('active');
	$('.flavor-pill[data-id="' + idProducto + '"]').addClass('active');

	cargarRecetaVisual();
}

function poblarSelectAdiciones() {
	var adiciones = PRODUCTOS.filter(function (p) {
		return p.tipo === 'ADICION';
	});
	var ops = '<option value="">Seleccione un topping para agregar...</option>';
	for (var i = 0; i < adiciones.length; i++) {
		ops += '<option value="' + adiciones[i].idproducto + '">' + adiciones[i].descripcion + '</option>';
	}
	$('#select-adicion').html(ops);
}

function agregarAdicion() {
	var idAdic = parseInt($('#select-adicion').val());
	if (!idAdic) {
		$.alert('Seleccione un topping o adición');
		return;
	}
	if (STUDIO_STATE.adicionesIds.indexOf(idAdic) >= 0) {
		$.alert('Este topping ya está agregado a la receta');
		return;
	}
	STUDIO_STATE.adicionesIds.push(idAdic);
	cargarRecetaVisual();
}

function quitarAdicion(idAdic) {
	STUDIO_STATE.adicionesIds = STUDIO_STATE.adicionesIds.filter(function (id) {
		return id !== idAdic;
	});
	cargarRecetaVisual();
}

function cargarRecetaVisual() {
	var ids = [STUDIO_STATE.saborId].concat(STUDIO_STATE.adicionesIds);
	$('#tbody-receta-studio').html('<tr><td colspan="5" class="text-center" style="padding:20px; color:#718096;"><i class="fas fa-spinner fa-spin"></i> Cargando insumos técnicos...</td></tr>');

	$.getJSON(server + 'ComposicionProducto', { que: 'costear', idproductos: ids.join(',') }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			$('#tbody-receta-studio').html('<tr><td colspan="5" class="text-danger text-center">Error al cargar receta</td></tr>');
			return;
		}

		STUDIO_STATE.recetaOriginal = $.extend(true, [], data.lineas || []);
		STUDIO_STATE.recetaSimulada = $.extend(true, [], data.lineas || []);

		pintarTablaReceta();
		recalcularEspecificacionesTecnicas();
	});
}

function pintarTablaReceta() {
	var lineas = STUDIO_STATE.recetaSimulada;
	var html = '';

	for (var i = 0; i < lineas.length; i++) {
		var l = lineas[i];
		var esBase = l.categoria === 'BASE' || l.categoria === 'MASA' || l.categoria === 'CAJA';
		var badgeClass = l.categoria === 'QUESO' ? 'badge-warning' : (l.categoria === 'BASE' || l.categoria === 'MASA' ? 'badge-primary' : 'badge-default');

		html += '<tr>'
			+ '<td><strong>' + l.insumo + '</strong></td>'
			+ '<td><span class="badge ' + badgeClass + '">' + (l.categoria || 'TOPPING') + '</span></td>'
			+ '<td class="text-right">'
			+ '<input type="number" step="any" class="input-gramaje" value="' + l.cantidad + '" '
			+ 'oninput="ajustarGramaje(' + i + ', this.value)">'
			+ '</td>'
			+ '<td>' + l.unidad + '</td>'
			+ '<td class="text-center">'
			+ (esBase ? '<span class="text-muted" style="font-size:11px;">Estándar</span>'
				: '<button class="btn btn-default btn-xs" onclick="quitarInsumo(' + i + ')" title="Quitar ingrediente"><i class="fas fa-times text-danger"></i></button>')
			+ '</td>'
			+ '</tr>';
	}

	$('#tbody-receta-studio').html(html);
}

function ajustarGramaje(idx, nuevoValor) {
	var val = parseFloat(nuevoValor);
	if (isNaN(val) || val < 0) val = 0;

	STUDIO_STATE.recetaSimulada[idx].cantidad = val;
	$('#tag-simulacion-activa').show();
	recalcularEspecificacionesTecnicas();
}

function quitarInsumo(idx) {
	STUDIO_STATE.recetaSimulada.splice(idx, 1);
	pintarTablaReceta();
	$('#tag-simulacion-activa').show();
	recalcularEspecificacionesTecnicas();
}

function restablecerReceta() {
	STUDIO_STATE.adicionesIds = [];
	cargarRecetaVisual();
	$('#tag-simulacion-activa').hide();
}

function recalcularEspecificacionesTecnicas() {
	var lineas = STUDIO_STATE.recetaSimulada;
	var pesoTotal = 0;
	var pesoMasa = 0;
	var pesoQueso = 0;
	var pesoToppings = 0;
	var cantToppings = 0;

	for (var i = 0; i < lineas.length; i++) {
		var l = lineas[i];
		var cant = l.cantidad;
		var cat = (l.categoria || '').toUpperCase();

		if (l.unidad === 'gramos' || l.unidad === 'gr' || l.unidad === 'g') {
			pesoTotal += cant;
		}

		if (cat === 'MASA' || cat === 'BASE') {
			pesoMasa += cant;
			$('#kpi-tipo-masa').text(l.insumo);
		} else if (cat === 'QUESO') {
			pesoQueso += cant;
		} else if (cat !== 'CAJA' && cat !== 'EMPAQUE') {
			pesoToppings += cant;
			cantToppings++;
		}
	}

	$('#kpi-peso-total').text(Math.round(pesoTotal) + ' g');
	$('#kpi-peso-masa').text(Math.round(pesoMasa) + ' g');
	$('#kpi-peso-queso').text(Math.round(pesoQueso) + ' g');
	$('#kpi-peso-toppings').text(Math.round(pesoToppings) + ' g');
	$('#kpi-cant-toppings').text(cantToppings + ' ingrediente(s)');

	// Datos para impresión
	$('#print-producto-nombre').text('Pizza ' + STUDIO_STATE.saborNombre);
	$('#print-producto-tamano').text(TAMANOS_MAP[STUDIO_STATE.tamano].nombre + ' (' + STUDIO_STATE.tamano + ')');
	$('#print-producto-porciones').text(TAMANOS_MAP[STUDIO_STATE.tamano].porciones);
	$('#print-producto-peso').text(Math.round(pesoTotal) + ' gramos');
}

function imprimirFichaTecnica() {
	window.print();
}

// ==========================================================================
// PESTAÑA 2: CATÁLOGO DE FICHAS TÉCNICAS
// ==========================================================================

function pintarCatalogoCompleto(lista) {
	var html = '';
	for (var i = 0; i < lista.length; i++) {
		var p = lista[i];
		html += '<tr>'
			+ '<td><code>' + p.idproducto + '</code></td>'
			+ '<td><strong>' + p.descripcion + '</strong></td>'
			+ '<td><span class="badge badge-primary">' + (p.tamano || 'N/A') + '</span></td>'
			+ '<td><span class="badge badge-default">' + p.tipo + '</span></td>'
			+ '<td>' + p.insumos + ' insumo(s) asignados</td>'
			+ '<td class="text-center">'
			+ '<button class="btn btn-default btn-xs" onclick="cargarFichaEnStudio(' + p.idproducto + ')">'
			+ '<i class="fas fa-eye text-primary"></i> Ver Ficha'
			+ '</button>'
			+ '</td>'
			+ '</tr>';
	}
	$('#tbody-catalogo-completo').html(html);
}

function filtrarCatalogo() {
	var txt = $.trim($('#buscar-catalogo').val()).toLowerCase();
	var tipo = $('#filtro-tipo').val();

	var filtrados = PRODUCTOS.filter(function (p) {
		var coincideTxt = (p.descripcion.toLowerCase().indexOf(txt) >= 0) ||
			(p.tamano && p.tamano.toLowerCase().indexOf(txt) >= 0) ||
			(String(p.idproducto).indexOf(txt) >= 0);
		var coincideTipo = !tipo || (p.tipo === tipo);
		return coincideTxt && coincideTipo;
	});

	pintarCatalogoCompleto(filtrados);
}

function cargarFichaEnStudio(idProducto) {
	var prod = PRODUCTOS.find(function (p) { return p.idproducto === idProducto; });
	if (!prod) return;

	if (prod.tipo === 'PIZZA' && prod.tamano && TAMANOS_MAP[prod.tamano]) {
		seleccionarTamano(prod.tamano);
		var nombreLimpio = prod.descripcion.replace(new RegExp('\\s+' + prod.tamano + '$', 'i'), '').trim();
		seleccionarSabor(prod.idproducto, nombreLimpio);
		$('.nav-tabs a[href="#tab-pizza-studio"]').tab('show');
	} else {
		$.alert('<strong>' + prod.descripcion + '</strong> tiene ' + prod.insumos + ' insumos en receta técnica central.');
	}
}

// ==========================================================================
// PESTAÑA 3: COMPOSICIÓN POR TIENDA
// ==========================================================================

function traerDeTienda() {
	var id = $('#selectTiendaTraer').val();
	if (!id) {
		$.alert('Seleccione una tienda');
		return;
	}
	var nom = $('#selectTiendaTraer option:selected').text();

	$.confirm({
		title: 'Actualizar Maestro Central',
		content: '¿Desea traer la composición de recetas de la tienda <b>' + nom + '</b> al Maestro Central?',
		buttons: {
			confirmar: {
				text: 'Sí, Traer',
				btnClass: 'btn-primary',
				action: function () {
					$('#cp-aviso-maestro').removeClass('alert-danger alert-success').addClass('alert-info').show()
						.html('<i class="fas fa-spinner fa-spin"></i> Conectando con la tienda ' + nom + '...');

					$.getJSON(server + 'ComposicionProducto', { que: 'traer', idtienda: id }, function (data) {
						if (data && data.respuesta === 'OK') {
							$('#cp-aviso-maestro').removeClass('alert-info').addClass('alert-success')
								.html('<i class="fas fa-check-circle"></i> Maestro actualizado: ' + (data.filas || 0) + ' recetas importadas.');
							$('#res-maestro').text((data.filas || 0).toLocaleString('es-CO'));
						} else {
							$('#cp-aviso-maestro').removeClass('alert-info').addClass('alert-danger')
								.html('<i class="fas fa-times-circle"></i> No se pudo traer: ' + (data.detalle || 'Error desconocido'));
						}
					}).fail(function () {
						$('#cp-aviso-maestro').removeClass('alert-info').addClass('alert-danger')
							.html('<i class="fas fa-times-circle"></i> Error de conexión con la tienda.');
					});
				}
			},
			cancelar: function () { }
		}
	});
}

function compararTienda() {
	var id = $('#selectTiendaComparar').val();
	if (!id) {
		$.alert('Seleccione una tienda para comparar');
		return;
	}
	var nom = $('#selectTiendaComparar option:selected').text();
	$('#btnComparar').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Comparando...');

	$.getJSON(server + 'ComposicionProducto', { que: 'comparar', idtienda: id }, function (data) {
		$('#btnComparar').prop('disabled', false).html('<i class="fas fa-search"></i> Comparar Recetas');

		if (!data || data.respuesta !== 'OK') {
			$.alert('No se pudo comparar: ' + ((data && data.detalle) ? data.detalle : 'Falla de red'));
			return;
		}

		var difs = data.diferencias || [];
		$('#resumen-diferencias').text(nom + ': ' + difs.length + ' diferencias encontradas');

		var tbody = '';
		for (var i = 0; i < difs.length; i++) {
			var d = difs[i];
			var tagDiag = d.tipo === 'CANTIDAD_DISTINTA'
				? '<span class="label label-warning">Gramaje Distinto</span>'
				: (d.tipo === 'FALTA_EN_TIENDA'
					? '<span class="label label-danger">Falta en Tienda</span>'
					: '<span class="label label-info">Sobra en Tienda</span>');

			tbody += '<tr>'
				+ '<td><strong>' + d.producto + '</strong></td>'
				+ '<td>' + d.insumo + '</td>'
				+ '<td>' + tagDiag + '</td>'
				+ '<td class="text-right">' + (d.cantMaestro !== null ? d.cantMaestro : '-') + '</td>'
				+ '<td class="text-right">' + (d.cantTienda !== null ? d.cantTienda : '-') + '</td>'
				+ '</tr>';
		}
		if (difs.length === 0) {
			tbody = '<tr><td colspan="5" class="text-center text-success" style="padding:15px;"><i class="fas fa-check-circle"></i> <b>100% Homologado:</b> Todas las recetas de esta tienda coinciden exactamente con el estándar central de calidad.</td></tr>';
		}
		$('#grid-diferencias tbody').html(tbody);
	}).fail(function () {
		$('#btnComparar').prop('disabled', false).html('<i class="fas fa-search"></i> Comparar Recetas');
		$.alert('No se pudo conectar a la tienda ' + nom);
	});
}

/**
 * Costeo de Productos y Ficha Técnica - Pizza Americana
 * Permite:
 * 1. Pizza Studio: Armador visual guiado, Food Cost %, margen y simulador en vivo de gramajes.
 * 2. Costeador Libre: Armado libre de cualquier producto o combo del POS.
 * 3. Composición por Tienda: Traer al maestro, comparar diferencias y replicar a tiendas.
 */

var PRODUCTOS = [];
var TAMANOS_MAP = {
	'XL': { id: 6, code: 'XL', desc: 'Pizza XL', nombre: 'Extra Grande', porciones: '10 a 12 Porciones', diametro: '40 cm', precio: 58000 },
	'GD': { id: 7, code: 'GD', desc: 'Pizza GD', nombre: 'Grande', porciones: '8 a 10 Porciones', diametro: '35 cm', precio: 49000 },
	'MD': { id: 8, code: 'MD', desc: 'Pizza MD', nombre: 'Mediana', porciones: '6 a 8 Porciones', diametro: '30 cm', precio: 40000 },
	'PZ': { id: 9, code: 'PZ', desc: 'Pizza PZ', nombre: 'Personal', porciones: '4 Porciones', diametro: '22 cm', precio: 22000 }
};

var STUDIO_STATE = {
	tamano: 'XL',
	saborNombre: 'Hawaiana',
	saborId: 303,
	adicionesIds: [],
	recetaOriginal: [],
	recetaSimulada: [],
	pvp: 58000
};

var LIBRE_ESCOGIDOS = [];

$(document).ready(function () {
	Gerencia.proteger('ger-contenido', function () {
		// Fecha para impresión
		var hoy = new Date();
		var fechaStr = hoy.getDate() + '/' + (hoy.getMonth() + 1) + '/' + hoy.getFullYear();
		$('#print-fecha').text(fechaStr);

		cargarCatalogo();
		cargarTiendas();

		// Confirmación de réplica
		$('#confirmaReplica').on('input', function () {
			$('#btnReplicar').prop('disabled', $(this).val().trim().toUpperCase() !== 'REPLICAR');
		});
	});
});

// ==========================================================================
// Carga de Catálogo y Tiendas
// ==========================================================================

function cargarCatalogo() {
	$.getJSON(server + 'ComposicionProducto', { que: 'catalogo' }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			alert('No se pudo cargar el catalogo de productos.');
			return;
		}
		PRODUCTOS = data.productos || [];
		$('#res-maestro').text(numero(data.filasmaestro || 0));

		// Actualizar precios base de tamaños si vienen del backend
		for (var code in TAMANOS_MAP) {
			var base = buscarProducto(TAMANOS_MAP[code].id);
			if (base && base.precio > 0) {
				TAMANOS_MAP[code].precio = base.precio;
				$('#precio-base-' + code).text(pesos(base.precio));
			}
		}

		// Rellenar select del costeador libre
		var htmlLibre = '<option value="">-- Seleccione un producto --</option>';
		for (var i = 0; i < PRODUCTOS.length; i++) {
			var p = PRODUCTOS[i];
			htmlLibre += '<option value="' + p.idproducto + '">' + escapar(p.descripcion)
				+ (p.tamano ? ' (' + escapar(p.tamano) + ')' : '')
				+ (p.precio > 0 ? ' [' + pesos(p.precio) + ']' : '')
				+ ' - ' + p.insumos + ' insumo(s)</option>';
		}
		$('#selectProductoLibre').html(htmlLibre);

		// Inicializar Pizza Studio
		renderSabores();
		renderAdiciones();
		seleccionarTamano('XL');
		seleccionarSabor('Hawaiana');

	}).fail(function () {
		alert('No hubo respuesta del servidor al cargar el catálogo.');
	});
}

function cargarTiendas() {
	$.getJSON(server + 'GetTiendas', function (data) {
		var html = '';
		for (var i = 0; i < data.length; i++) {
			if (data[i].id === 12) { continue; } // Bodega
			html += '<option value="' + data[i].id + '">' + escapar(data[i].nombre) + '</option>';
		}
		$('#selectTiendaTraer, #selectTiendaComparar, #selectTiendaReplicar').html(html);
	});
}

function buscarProducto(id) {
	for (var i = 0; i < PRODUCTOS.length; i++) {
		if (String(PRODUCTOS[i].idproducto) === String(id)) {
			return (PRODUCTOS[i]);
		}
	}
	return (null);
}

// ==========================================================================
// PIZZA STUDIO: Selección y Renderizado
// ==========================================================================

function seleccionarTamano(tamano) {
	if (!TAMANOS_MAP[tamano]) { return; }
	STUDIO_STATE.tamano = tamano;

	// Actualizar tarjetas de tamaño en UI
	$('.size-card').removeClass('active');
	$('#card-tamano-' + tamano).addClass('active');

	// Actualizar disco visual
	$('#pizza-visual-disc')
		.removeClass('crust-xl crust-gd crust-md crust-pz')
		.addClass('crust-' + tamano.toLowerCase());

	var info = TAMANOS_MAP[tamano];
	$('#pizza-spec-text').text(info.nombre + ' • ' + info.diametro + ' • ' + info.porciones);

	// Re-renderizar sabores y adiciones adaptados a este tamaño
	renderSabores();
	renderAdiciones();

	// Encontrar sabor equivalente en el nuevo tamaño
	actualizarSaborParaTamano();
	costearPizzaStudio();
}

function obtenerSaboresDisponibles() {
	var sabMap = {};
	var sufijo = ' ' + STUDIO_STATE.tamano;
	for (var i = 0; i < PRODUCTOS.length; i++) {
		var p = PRODUCTOS[i];
		if (p.tipo === 'D') {
			var desc = p.descripcion.trim();
			// Quitar sufijo de tamaño si coincide
			var root = desc.replace(new RegExp('\\s+(XL|GD|MD|PZ|XG)$', 'i'), '').trim();
			if (!sabMap[root]) {
				sabMap[root] = { root: root, variants: {} };
			}
			// Determinar a qué tamaño pertenece
			if (desc.toUpperCase().endsWith('XL') || desc.toUpperCase().endsWith('XG')) {
				sabMap[root].variants['XL'] = p;
			} else if (desc.toUpperCase().endsWith('GD')) {
				sabMap[root].variants['GD'] = p;
			} else if (desc.toUpperCase().endsWith('MD')) {
				sabMap[root].variants['MD'] = p;
			} else if (desc.toUpperCase().endsWith('PZ')) {
				sabMap[root].variants['PZ'] = p;
			}
		}
	}
	return sabMap;
}

function renderSabores() {
	var filtro = ($('#filtro-sabores').val() || '').trim().toLowerCase();
	var sabMap = obtenerSaboresDisponibles();
	var tam = STUDIO_STATE.tamano;

	var html = '';
	var listaRoots = Object.keys(sabMap).sort();

	for (var i = 0; i < listaRoots.length; i++) {
		var root = listaRoots[i];
		var item = sabMap[root];
		var pTam = item.variants[tam];

		// Si no tiene variante para este tamaño o no coincide con filtro
		if (!pTam) { continue; }
		if (filtro && root.toLowerCase().indexOf(filtro) === -1) { continue; }

		var activo = (root.toLowerCase() === STUDIO_STATE.saborNombre.toLowerCase());
		html += '<div class="flavor-pill ' + (activo ? 'active' : '') + '" onclick="seleccionarSabor(\'' + escapar(root) + '\')">'
			+ '<span><i class="fas fa-pizza-slice" style="margin-right:6px; opacity:0.8;"></i>' + escapar(root) + '</span>'
			+ '<span class="flavor-tag">' + pTam.insumos + ' ins.</span>'
			+ '</div>';
	}

	if (!html) {
		html = '<div style="grid-column: 1/-1; padding:15px; text-align:center; color:#A0AEC0;">'
			+ '<i class="fas fa-search"></i> No se encontraron sabores que coincidan.</div>';
	}
	$('#grid-sabores').html(html);
}

function filtrarSabores() {
	renderSabores();
}

function seleccionarSabor(rootName) {
	STUDIO_STATE.saborNombre = rootName;
	actualizarSaborParaTamano();
	renderSabores();
	costearPizzaStudio();
}

function actualizarSaborParaTamano() {
	var sabMap = obtenerSaboresDisponibles();
	var item = sabMap[STUDIO_STATE.saborNombre];
	var tam = STUDIO_STATE.tamano;

	if (item && item.variants[tam]) {
		STUDIO_STATE.saborId = item.variants[tam].idproducto;
		$('#flavor-selected-label').text(item.variants[tam].descripcion);
	} else {
		// Fallback al primer sabor disponible
		var listaRoots = Object.keys(sabMap);
		for (var i = 0; i < listaRoots.length; i++) {
			if (sabMap[listaRoots[i]].variants[tam]) {
				STUDIO_STATE.saborNombre = listaRoots[i];
				STUDIO_STATE.saborId = sabMap[listaRoots[i]].variants[tam].idproducto;
				$('#flavor-selected-label').text(sabMap[listaRoots[i]].variants[tam].descripcion);
				break;
			}
		}
	}
}

function renderAdiciones() {
	var tam = STUDIO_STATE.tamano;
	var html = '';

	for (var i = 0; i < PRODUCTOS.length; i++) {
		var p = PRODUCTOS[i];
		var esAdic = (p.tipo === 'A') || p.descripcion.toUpperCase().startsWith('ADIC');
		if (!esAdic) { continue; }

		// Verificar que termine en el tamaño actual
		var coincideTam = p.descripcion.toUpperCase().endsWith(' ' + tam) || (tam === 'XL' && p.descripcion.toUpperCase().endsWith(' XG'));
		if (!coincideTam) { continue; }

		// Nombre corto
		var nombreCorto = p.descripcion.replace(/Adici[oó]n\s+/i, '').replace(new RegExp('\\s+' + tam + '$', 'i'), '').trim();
		var activa = STUDIO_STATE.adicionesIds.indexOf(p.idproducto) !== -1;

		html += '<div class="addition-chip ' + (activa ? 'active' : '') + '" onclick="toggleAdicion(' + p.idproducto + ')">'
			+ (activa ? '<i class="fas fa-check" style="margin-right:4px;"></i>' : '<i class="fas fa-plus" style="margin-right:4px;"></i>')
			+ escapar(nombreCorto) + (p.precio > 0 ? ' (+' + pesos(p.precio) + ')' : '')
			+ '</div>';
	}

	if (!html) {
		html = '<span style="color:#A0AEC0; font-size:12px;">No hay adiciones registradas para este tamaño.</span>';
	}
	$('#grid-adiciones').html(html);
}

function toggleAdicion(idProducto) {
	var idx = STUDIO_STATE.adicionesIds.indexOf(idProducto);
	if (idx === -1) {
		STUDIO_STATE.adicionesIds.push(idProducto);
	} else {
		STUDIO_STATE.adicionesIds.splice(idx, 1);
	}
	renderAdiciones();
	costearPizzaStudio();
}

// ==========================================================================
// PIZZA STUDIO: Costeo y Métricas
// ==========================================================================

function costearPizzaStudio() {
	var baseObj = TAMANOS_MAP[STUDIO_STATE.tamano];
	var ids = [baseObj.id];

	if (STUDIO_STATE.saborId) {
		ids.push(STUDIO_STATE.saborId);
	}
	for (var i = 0; i < STUDIO_STATE.adicionesIds.length; i++) {
		ids.push(STUDIO_STATE.adicionesIds[i]);
	}

	// Actualizar badges de componentes del ensamble
	var badgesHtml = '<span class="label label-primary" style="margin-right:4px;">' + escapar(baseObj.desc) + '</span>';
	var pSabor = buscarProducto(STUDIO_STATE.saborId);
	if (pSabor) {
		badgesHtml += '<span class="label label-danger" style="margin-right:4px;">' + escapar(pSabor.descripcion) + '</span>';
	}
	for (var j = 0; j < STUDIO_STATE.adicionesIds.length; j++) {
		var pAd = buscarProducto(STUDIO_STATE.adicionesIds[j]);
		if (pAd) {
			badgesHtml += '<span class="label label-info" style="margin-right:4px;">+ ' + escapar(pAd.descripcion) + '</span>';
		}
	}
	$('#studio-items-badges').html(badgesHtml);

	// Nombre en disco
	var nombreCompleto = 'Pizza ' + STUDIO_STATE.saborNombre + ' ' + STUDIO_STATE.tamano;
	$('#pizza-disc-title').text(nombreCompleto);

	// Actualizar campos de impresión
	$('#print-producto-nombre').text(nombreCompleto);
	$('#print-producto-tamano').text(baseObj.nombre + ' (' + baseObj.diametro + ')');
	$('#print-producto-porciones').text(baseObj.porciones);

	// Calcular PVP base de referencia
	var pvpBase = baseObj.precio;
	if (pSabor && pSabor.precio > 0) {
		pvpBase += pSabor.precio;
	}
	for (var k = 0; k < STUDIO_STATE.adicionesIds.length; k++) {
		var pAd2 = buscarProducto(STUDIO_STATE.adicionesIds[k]);
		if (pAd2 && pAd2.precio > 0) {
			pvpBase += pAd2.precio;
		}
	}
	STUDIO_STATE.pvp = pvpBase;
	$('#kpi-inp-pvp').val(pvpBase);
	$('#print-producto-pvp').text(pesos(pvpBase));

	// Consultar backend
	$.getJSON(server + 'ComposicionProducto', { que: 'costear', idproductos: ids.join(',') }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			alert('No se pudo costear la pizza.');
			return;
		}

		var lineas = data.lineas || [];
		STUDIO_STATE.recetaOriginal = JSON.parse(JSON.stringify(lineas));
		STUDIO_STATE.recetaSimulada = JSON.parse(JSON.stringify(lineas));

		$('#tag-simulacion-activa').hide();

		if ((data.sincosto || 0) + (data.sinhomologar || 0) > 0) {
			$('#alerta-sin-costo').show();
		} else {
			$('#alerta-sin-costo').hide();
		}

		renderRecetaTable();
		renderToppingsSVG();
		recalcularRentabilidad();
	}).fail(function () {
		alert('No hubo respuesta del servidor al costear.');
	});
}

function renderRecetaTable() {
	var tbody = '';
	var totalStdGramos = 0;
	var totalSimGramos = 0;
	var totalCosto = 0;

	for (var i = 0; i < STUDIO_STATE.recetaSimulada.length; i++) {
		var r = STUDIO_STATE.recetaSimulada[i];
		var orig = STUDIO_STATE.recetaOriginal[i];
		var costoLinea = r.cantidad * r.unitario;
		r.costo = costoLinea;
		totalCosto += costoLinea;

		var esGr = (r.unidad || '').toUpperCase() === 'GR' || (r.unidad || '').toUpperCase() === 'G';
		if (esGr) {
			totalStdGramos += orig.cantidad;
			totalSimGramos += r.cantidad;
		}

		var esModificado = Math.abs(r.cantidad - orig.cantidad) > 0.001;
		var catClass = obtenerClaseCategoria(r.categoria);

		tbody += '<tr class="' + (esModificado ? 'row-simulada' : '') + '">'
			+ '<td><b>' + escapar(r.insumo) + '</b>'
			+ (r.sincosto ? ' <span class="label label-danger">Sin Costo</span>' : '')
			+ (r.sinhomologar ? ' <span class="label label-warning">Sin Homologar</span>' : '')
			+ '</td>'
			+ '<td><span class="badge-cat ' + catClass + '">' + escapar(r.categoria || 'Insumo') + '</span></td>'
			+ '<td class="text-right">' + numero(orig.cantidad) + ' ' + escapar(r.unidad) + '</td>'
			+ '<td class="text-center no-print">'
			+ '  <div class="qty-stepper">'
			+ '    <button type="button" onclick="cambiarGramaje(' + i + ', -5)">-</button>'
			+ '    <input type="number" step="any" value="' + (Math.round(r.cantidad * 100) / 100) + '" onchange="editarGramajeInput(' + i + ', this.value)">'
			+ '    <button type="button" onclick="cambiarGramaje(' + i + ', 5)">+</button>'
			+ '  </div>'
			+ '</td>'
			+ '<td>' + escapar(r.unidad) + '</td>'
			+ '<td class="text-right">' + pesosFinos(r.unitario) + '</td>'
			+ '<td class="text-right font-weight-bold" id="costo-linea-' + i + '">' + pesos(costoLinea) + '</td>'
			+ '<td class="text-right" id="pct-linea-' + i + '">0.0%</td>'
			+ '<td class="text-center no-print">'
			+ (esModificado ? '<span class="label label-warning" title="Modificado en simulación"><i class="fas fa-edit"></i> Simulado</span>' : '<span class="text-muted"><i class="fas fa-check"></i> Estándar</span>')
			+ '</td>'
			+ '</tr>';
	}

	$('#tbody-studio-ingredientes').html(tbody);
	$('#foot-std-gramos').text(numero(Math.round(totalStdGramos)) + ' g');
	$('#foot-sim-gramos').text(numero(Math.round(totalSimGramos)) + ' g');
	$('#foot-total-costo').text(pesos(totalCosto));
	$('#badge-total-gramos').text(numero(Math.round(totalSimGramos)) + ' g Insumos');

	// Actualizar porcentajes de línea
	for (var j = 0; j < STUDIO_STATE.recetaSimulada.length; j++) {
		var item = STUDIO_STATE.recetaSimulada[j];
		var pct = totalCosto > 0 ? ((item.costo / totalCosto) * 100) : 0;
		$('#pct-linea-' + j).text((Math.round(pct * 10) / 10) + '%');
	}

	renderCategoryBar(totalCosto);
}

function cambiarGramaje(index, delta) {
	var actual = STUDIO_STATE.recetaSimulada[index].cantidad;
	var nuevo = Math.max(0, actual + delta);
	STUDIO_STATE.recetaSimulada[index].cantidad = Math.round(nuevo * 100) / 100;
	$('#tag-simulacion-activa').show();
	renderRecetaTable();
	recalcularRentabilidad();
}

function editarGramajeInput(index, val) {
	var num = parseFloat(val);
	if (isNaN(num) || num < 0) { num = 0; }
	STUDIO_STATE.recetaSimulada[index].cantidad = Math.round(num * 100) / 100;
	$('#tag-simulacion-activa').show();
	renderRecetaTable();
	recalcularRentabilidad();
}

function restablecerGramajesOriginales() {
	STUDIO_STATE.recetaSimulada = JSON.parse(JSON.stringify(STUDIO_STATE.recetaOriginal));
	$('#tag-simulacion-activa').hide();
	renderRecetaTable();
	recalcularRentabilidad();
}

function recalcularRentabilidad() {
	var costoTotal = 0;
	for (var i = 0; i < STUDIO_STATE.recetaSimulada.length; i++) {
		costoTotal += STUDIO_STATE.recetaSimulada[i].costo;
	}

	var pvp = parseFloat($('#kpi-inp-pvp').val()) || 0;
	STUDIO_STATE.pvp = pvp;
	$('#print-producto-pvp').text(pesos(pvp));

	var margenPesos = pvp - costoTotal;
	var margenPct = pvp > 0 ? ((margenPesos / pvp) * 100) : 0;
	var foodCostPct = pvp > 0 ? ((costoTotal / pvp) * 100) : 0;
	var pvpSugerido30 = costoTotal > 0 ? Math.round(costoTotal / 0.30) : 0;

	// Actualizar KPIs
	$('#kpi-costo-total').text(pesos(costoTotal));
	$('#kpi-costo-sub').text(STUDIO_STATE.recetaSimulada.length + ' insumos en ensamble');
	$('#kpi-margen-pesos').text(pesos(margenPesos));
	$('#kpi-margen-pct').text((Math.round(margenPct * 10) / 10) + '% margen bruto');

	$('#kpi-foodcost-pct').text((Math.round(foodCostPct * 10) / 10) + '%');
	$('#kpi-pvp-sugerido').text(pesos(pvpSugerido30));

	// Semáforo Food Cost
	var $fcBadge = $('#kpi-foodcost-badge');
	$fcBadge.removeClass('badge-fc-optimo badge-fc-alerta badge-fc-peligro');
	if (foodCostPct <= 32.0) {
		$fcBadge.addClass('badge-fc-optimo').text('Óptimo (≤ 32%)');
	} else if (foodCostPct <= 37.9) {
		$fcBadge.addClass('badge-fc-alerta').text('Moderado (33-37%)');
	} else {
		$fcBadge.addClass('badge-fc-peligro').text('Alto (> 38%)');
	}
}

// ==========================================================================
// Gráfica de Categorías y Visualizador de Toppings
// ==========================================================================

function renderCategoryBar(totalCosto) {
	if (!totalCosto || totalCosto <= 0) {
		$('#category-stacked-bar').html('');
		$('#category-legend-box').html('');
		return;
	}

	var catTotals = {};
	for (var i = 0; i < STUDIO_STATE.recetaSimulada.length; i++) {
		var r = STUDIO_STATE.recetaSimulada[i];
		var cat = normalizarCategoria(r.categoria);
		catTotals[cat] = (catTotals[cat] || 0) + r.costo;
	}

	var barHtml = '';
	var legendHtml = '';
	var topCat = '';
	var topVal = 0;

	var catConfig = {
		'Quesos': { clase: 'seg-quesos', color: '#F59E0B' },
		'Cárnicos': { clase: 'seg-carnicos', color: '#EF4444' },
		'Masas / Bases': { clase: 'seg-masas', color: '#8B5CF6' },
		'Vegetales / Frutas': { clase: 'seg-nocarnicos', color: '#10B981' },
		'Salsas': { clase: 'seg-salsas', color: '#DC2626' },
		'Empaque': { clase: 'seg-empaque', color: '#64748B' },
		'Otros': { clase: 'seg-otros', color: '#94A3B8' }
	};

	for (var cName in catConfig) {
		var val = catTotals[cName] || 0;
		if (val > 0) {
			var pct = (val / totalCosto) * 100;
			if (val > topVal) {
				topVal = val;
				topCat = cName;
			}
			barHtml += '<div class="category-seg ' + catConfig[cName].clase + '" style="width:' + pct + '%;" title="' + cName + ': ' + pesos(val) + ' (' + (Math.round(pct * 10) / 10) + '%)"></div>';
			legendHtml += '<div><span class="cat-bullet" style="background:' + catConfig[cName].color + ';"></span>'
				+ '<b>' + cName + ':</b> ' + pesos(val) + ' (' + (Math.round(pct * 10) / 10) + '%)</div>';
		}
	}

	$('#category-stacked-bar').html(barHtml);
	$('#category-legend-box').html(legendHtml);
	if (topCat) {
		$('#categoria-top-label').text('Mayor impacto: ' + topCat + ' (' + (Math.round((topVal / totalCosto) * 100)) + '%)');
	}
}

function normalizarCategoria(cat) {
	if (!cat) { return 'Otros'; }
	var c = cat.toLowerCase();
	if (c.indexOf('queso') !== -1) { return 'Quesos'; }
	if (c.indexOf('carnic') !== -1 || c.indexOf('cárnic') !== -1) { return 'Cárnicos'; }
	if (c.indexOf('basic') !== -1 || c.indexOf('básic') !== -1 || c.indexOf('masa') !== -1) { return 'Masas / Bases'; }
	if (c.indexOf('no carnic') !== -1 || c.indexOf('vegetal') !== -1 || c.indexOf('fruta') !== -1) { return 'Vegetales / Frutas'; }
	if (c.indexOf('salsa') !== -1 || c.indexOf('pasta') !== -1) { return 'Salsas'; }
	if (c.indexOf('miscelan') !== -1 || c.indexOf('empaque') !== -1 || c.indexOf('caja') !== -1) { return 'Empaque'; }
	return 'Otros';
}

function obtenerClaseCategoria(cat) {
	var norm = normalizarCategoria(cat);
	switch (norm) {
		case 'Quesos': return 'label-warning';
		case 'Cárnicos': return 'label-danger';
		case 'Masas / Bases': return 'label-primary';
		case 'Vegetales / Frutas': return 'label-success';
		case 'Salsas': return 'label-danger';
		case 'Empaque': return 'label-default';
		default: return 'label-info';
	}
}

function renderToppingsSVG() {
	var svg = '';
	var sabor = (STUDIO_STATE.saborNombre || '').toLowerCase();

	// Patrones de toppings distribuidos en radios concéntricos
	var coords = [
		[32, 28], [50, 22], [68, 28], [78, 46], [72, 68], [50, 78], [28, 68], [22, 46],
		[40, 38], [60, 38], [62, 58], [38, 58], [50, 50]
	];

	// Si lleva piña (Hawaiana)
	if (sabor.indexOf('hawai') !== -1) {
		for (var i = 0; i < coords.length; i += 2) {
			svg += '<rect x="' + (coords[i][0] - 3) + '" y="' + (coords[i][1] - 3) + '" width="7" height="7" rx="1.5" fill="#FDE047" stroke="#CA8A04" stroke-width="0.8" transform="rotate(15 ' + coords[i][0] + ' ' + coords[i][1] + ')" />';
		}
	}

	// Si lleva jamón (Hawaiana, Carnes, Jamón y Queso)
	if (sabor.indexOf('hawai') !== -1 || sabor.indexOf('jamon') !== -1 || sabor.indexOf('jamón') !== -1 || sabor.indexOf('carn') !== -1) {
		for (var j = 1; j < coords.length; j += 2) {
			svg += '<rect x="' + (coords[j][0] - 4) + '" y="' + (coords[j][1] - 3) + '" width="8" height="6" rx="1" fill="#F472B6" stroke="#DB2777" stroke-width="0.8" transform="rotate(35 ' + coords[j][0] + ' ' + coords[j][1] + ')" />';
		}
	}

	// Si lleva peperoni
	if (sabor.indexOf('peperoni') !== -1 || sabor.indexOf('pepperoni') !== -1 || sabor.indexOf('carn') !== -1) {
		for (var k = 0; k < coords.length; k += 2) {
			svg += '<circle cx="' + coords[k][0] + '" cy="' + coords[k][1] + '" r="5.5" fill="#B91C1C" stroke="#7F1D1D" stroke-width="0.8" />';
			svg += '<circle cx="' + (coords[k][0] - 1) + '" cy="' + (coords[k][1] - 1) + '" r="0.8" fill="#F87171" />';
		}
	}

	// Si lleva pollo o champiñones
	if (sabor.indexOf('pollo') !== -1) {
		for (var m = 0; m < coords.length; m += 2) {
			svg += '<ellipse cx="' + coords[m][0] + '" cy="' + coords[m][1] + '" rx="4" ry="2.5" fill="#FEF08A" stroke="#EAB308" stroke-width="0.8" transform="rotate(20 ' + coords[m][0] + ' ' + coords[m][1] + ')" />';
		}
	}
	if (sabor.indexOf('champi') !== -1) {
		for (var n = 1; n < coords.length; n += 2) {
			svg += '<path d="M ' + (coords[n][0] - 3) + ' ' + coords[n][1] + ' a 3 3 0 0 1 6 0 z" fill="#E2E8F0" stroke="#94A3B8" stroke-width="0.8" />';
		}
	}

	// Especias / Orégano general
	for (var s = 0; s < 18; s++) {
		var rx = 25 + Math.floor(Math.sin(s * 1.5) * 22 + 25);
		var ry = 25 + Math.floor(Math.cos(s * 1.5) * 22 + 25);
		svg += '<circle cx="' + rx + '" cy="' + ry + '" r="0.6" fill="#15803D" />';
	}

	$('#pizza-svg-toppings').html(svg);
}

function imprimirFichaTecnica() {
	window.print();
}

// ==========================================================================
// PESTAÑA 2: COSTEADOR LIBRE
// ==========================================================================

function adicionarProductoLibre() {
	var id = $('#selectProductoLibre').val();
	if (!id) { return; }
	var p = buscarProducto(id);
	LIBRE_ESCOGIDOS.push({ id: id, texto: p ? p.descripcion : ('Producto ' + id) });
	pintarProductosLibres();
	costearProductosLibres();
}

function quitarProductoLibre(indice) {
	LIBRE_ESCOGIDOS.splice(indice, 1);
	pintarProductosLibres();
	costearProductosLibres();
}

function limpiarProductosLibres() {
	LIBRE_ESCOGIDOS = [];
	pintarProductosLibres();
	costearProductosLibres();
}

function pintarProductosLibres() {
	if (LIBRE_ESCOGIDOS.length === 0) {
		$('#escogidos-libres').html('<span style="color:#A0AEC0; font-size:12.5px;">Ningún producto seleccionado.</span>');
		return;
	}
	var html = '';
	for (var i = 0; i < LIBRE_ESCOGIDOS.length; i++) {
		html += '<span class="label label-primary" style="display:inline-block; margin:2px 4px; font-size:12px; padding:4px 8px;">'
			+ escapar(LIBRE_ESCOGIDOS[i].texto)
			+ ' <span style="cursor:pointer; margin-left:6px; color:#FECACA; font-weight:bold;" onclick="quitarProductoLibre(' + i + ')">&times;</span></span>';
	}
	$('#escogidos-libres').html(html);
}

function costearProductosLibres() {
	if (LIBRE_ESCOGIDOS.length === 0) {
		$('#res-libre-total').text('$ 0');
		$('#res-libre-insumos').text('0');
		$('#res-libre-problemas').text('0');
		$('#res-libre-pie').text('0 productos');
		if ($.fn.DataTable.isDataTable('#grid-costeo-libre')) {
			$('#grid-costeo-libre').DataTable().destroy();
		}
		$('#grid-costeo-libre tbody').html('');
		return;
	}

	var ids = [];
	for (var i = 0; i < LIBRE_ESCOGIDOS.length; i++) {
		ids.push(LIBRE_ESCOGIDOS[i].id);
	}

	$.getJSON(server + 'ComposicionProducto', { que: 'costear', idproductos: ids.join(',') }, function (data) {
		if (!data || data.respuesta !== 'OK') {
			alert('No se pudo costear la selección libre.');
			return;
		}

		var lineas = data.lineas || [];
		var total = data.total || 0;

		$('#res-libre-total').text(pesos(total));
		$('#res-libre-insumos').text(lineas.length);
		$('#res-libre-problemas').text((data.sincosto || 0) + (data.sinhomologar || 0));
		$('#res-libre-pie').text(LIBRE_ESCOGIDOS.length + ' producto(s)');

		if ($.fn.DataTable.isDataTable('#grid-costeo-libre')) {
			$('#grid-costeo-libre').DataTable().destroy();
		}

		var tbody = '';
		for (var j = 0; j < lineas.length; j++) {
			var l = lineas[j];
			var malo = l.sincosto || l.sinhomologar;
			var pct = total > 0 ? ((l.costo / total) * 100) : 0;
			tbody += '<tr>'
				+ '<td' + (malo ? ' style="color:#DC2626; font-weight:bold;"' : '') + '>' + escapar(l.insumo)
				+ (l.sincosto ? ' (sin costo)' : '') + (l.sinhomologar ? ' (sin homologar)' : '') + '</td>'
				+ '<td><span class="badge-cat ' + obtenerClaseCategoria(l.categoria) + '">' + escapar(l.categoria || 'Otros') + '</span></td>'
				+ '<td class="text-right">' + numero(l.cantidad) + '</td>'
				+ '<td>' + escapar(l.unidad) + '</td>'
				+ '<td class="text-right">' + pesosFinos(l.unitario) + '</td>'
				+ '<td class="text-right font-weight-bold">' + pesos(l.costo) + '</td>'
				+ '<td class="text-right">' + (Math.round(pct * 10) / 10) + ' %</td>'
				+ '</tr>';
		}
		$('#grid-costeo-libre tbody').html(tbody);

		$('#grid-costeo-libre').DataTable({
			"paging": false, "searching": true, "info": false, "order": [[5, "desc"]],
			"language": { "search": "Buscar insumo:", "zeroRecords": "Sin insumos" }
		});
	});
}

// ==========================================================================
// PESTAÑA 3: COMPOSICIÓN POR TIENDA
// ==========================================================================

function traerDeTienda() {
	var id = $('#selectTiendaTraer').val();
	var nombre = $('#selectTiendaTraer option:selected').text();
	$.confirm({
		title: 'Traer al maestro central',
		content: 'Se va a actualizar el maestro central con la composición registrada en <b>' + escapar(nombre)
			+ '</b>. Ninguna tienda se modifica.',
		buttons: {
			confirmar: {
				text: 'Traer al Maestro', btnClass: 'btn-primary',
				action: function () { traerDeTiendaConfirmado(id); }
			},
			cancelar: { text: 'Cancelar' }
		}
	});
}

function traerDeTiendaConfirmado(id) {
	$.getJSON(server + 'ComposicionProducto', { que: 'traer', idtienda: id }, function (data) {
		if (!data) {
			avisar('#cp-aviso-maestro', 'No hubo respuesta del servidor.', 'alert-danger');
			return;
		}
		if (data.respuesta === 'OK') {
			var texto = 'Maestro actualizado desde ' + data.tienda + ': ' + numero(data.filas)
				+ ' filas de composición y ' + numero(data.productos) + ' productos.';
			if (data.repetidos > 0) {
				texto += ' NOTA: ' + data.repetidos + ' par(es) producto-insumo venían repetidos en esa tienda y se consolidaron sumados.';
			}
			avisar('#cp-aviso-maestro', texto, data.repetidos > 0 ? 'alert-warning' : 'alert-success');
			cargarCatalogo();
		} else {
			avisar('#cp-aviso-maestro', mensajeError(data.respuesta), 'alert-danger');
		}
	}).fail(function () {
		avisar('#cp-aviso-maestro', 'Error de conexión con el servidor.', 'alert-danger');
	});
}

function compararTienda() {
	var id = $('#selectTiendaComparar').val();
	$('#btnComparar').text('Comparando...').prop('disabled', true);

	$.getJSON(server + 'ComposicionProducto', { que: 'comparar', idtienda: id }, function (data) {
		$('#btnComparar').html('<i class="fas fa-search"></i> Comparar').prop('disabled', false);
		if (!data || data.respuesta !== 'OK') {
			$('#resumen-diferencias').text(mensajeError(data ? data.respuesta : ''));
			return;
		}
		pintarDiferencias(data);
	}).fail(function () {
		$('#btnComparar').html('<i class="fas fa-search"></i> Comparar').prop('disabled', false);
		$('#resumen-diferencias').text('Error al conectar con la tienda.');
	});
}

function pintarDiferencias(data) {
	var filas = data.diferencias || [];
	if (filas.length === 0) {
		$('#resumen-diferencias').html('<span style="color:#059669;"><i class="fas fa-check-circle"></i> ' + data.tienda + ' está 100% sincronizada con el maestro.</span>');
	} else {
		$('#resumen-diferencias').html(data.tienda + ': <b style="color:#DC2626;">' + data.faltan + ' faltan</b>, <b style="color:#D97706;">' + data.sobran + ' sobran</b>, <b style="color:#2563EB;">' + data.distintas + ' cantidad distinta</b>.');
	}

	if ($.fn.DataTable.isDataTable('#grid-diferencias')) {
		$('#grid-diferencias').DataTable().destroy();
	}

	var tbody = '';
	for (var i = 0; i < filas.length; i++) {
		var f = filas[i];
		tbody += '<tr>'
			+ '<td>' + escapar(f.producto) + '</td>'
			+ '<td>' + escapar(f.insumo) + '</td>'
			+ '<td>' + etiquetaTipo(f.tipo) + '</td>'
			+ '<td class="text-right">' + (f.tipo === 'SOBRA' ? '-' : numero(f.maestro)) + '</td>'
			+ '<td class="text-right">' + (f.tipo === 'FALTA' ? '-' : numero(f.tienda)) + '</td>'
			+ '</tr>';
	}
	$('#grid-diferencias tbody').html(tbody);

	$('#grid-diferencias').DataTable({
		"paging": true, "pageLength": 25, "searching": true, "info": true, "order": [],
		"language": {
			"search": "Buscar:", "lengthMenu": "Mostrar _MENU_ diferencias",
			"info": "_START_ a _END_ de _TOTAL_", "infoEmpty": "Sin diferencias",
			"zeroRecords": "Nada que coincida",
			"paginate": { "first": "Primera", "last": "Última", "next": "Siguiente", "previous": "Anterior" }
		}
	});
}

function etiquetaTipo(tipo) {
	if (tipo === 'FALTA') {
		return ('<span class="label label-danger">FALTA EN TIENDA</span>');
	}
	if (tipo === 'SOBRA') {
		return ('<span class="label label-warning">SOBRA EN TIENDA</span>');
	}
	return ('<span class="label label-info">CANTIDAD DISTINTA</span>');
}

function replicarATienda() {
	var id = $('#selectTiendaReplicar').val();
	var nombre = $('#selectTiendaReplicar option:selected').text();

	$.confirm({
		title: 'Confirmar Replicación a ' + escapar(nombre),
		content: 'Se reemplazará la composición de <b>' + escapar(nombre) + '</b> por la receta del maestro central.<br><br>Desde este momento esa tienda descontará inventario con estas recetas estándar.',
		buttons: {
			confirmar: {
				text: 'Sí, Replicar Ahora', btnClass: 'btn-danger',
				action: function () { replicarConfirmado(id); }
			},
			cancelar: { text: 'Cancelar' }
		}
	});
}

function replicarConfirmado(id) {
	$('#btnReplicar').text('Replicando...').prop('disabled', true);

	$.getJSON(server + 'ComposicionProducto', { que: 'replicar', idtienda: id }, function (data) {
		$('#btnReplicar').html('<i class="fas fa-sync-alt"></i> Replicar al POS de Tienda');
		$('#confirmaReplica').val('');
		$('#btnReplicar').prop('disabled', true);

		if (!data) {
			avisar('#cp-aviso-replica', 'No hubo respuesta del servidor.', 'alert-danger');
			return;
		}
		if (data.respuesta === 'OK') {
			avisar('#cp-aviso-replica', data.tienda + ' quedó replicada con éxito: tenía ' + numero(data.antes)
				+ ' filas y quedó con ' + numero(data.despues) + '.', 'alert-success');
		} else {
			avisar('#cp-aviso-replica', mensajeError(data.respuesta), 'alert-danger');
		}
	}).fail(function () {
		$('#btnReplicar').html('<i class="fas fa-sync-alt"></i> Replicar al POS de Tienda').prop('disabled', true);
		$('#confirmaReplica').val('');
		avisar('#cp-aviso-replica', 'No hubo respuesta del servidor.', 'alert-danger');
	});
}

// ==========================================================================
// Utilidades de Formato
// ==========================================================================

function mensajeError(codigo) {
	if (codigo === 'SINSESION') { return ('Su sesión ha expirado. Vuelva a iniciar sesión.'); }
	if (codigo === 'SINHOST') { return ('Esa tienda no tiene host de base de datos configurado.'); }
	if (codigo === 'NOCONECTA') { return ('No se pudo conectar a la base de datos de esa tienda.'); }
	if (codigo === 'VACIO') { return ('El maestro está vacío. Traiga primero la composición de una tienda de referencia.'); }
	return ('No se pudo completar la operación.');
}

function avisar(donde, texto, claseAlert) {
	$(donde).html(texto)
		.removeClass('alert-success alert-danger alert-warning alert-info')
		.addClass(claseAlert)
		.show();
}

function pesos(valor) {
	var n = Math.round(valor || 0);
	return ((n < 0 ? '-$ ' : '$ ') + numeroEntero(Math.abs(n)));
}

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

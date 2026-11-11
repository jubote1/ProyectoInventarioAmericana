/**
 * despacharinventarionuevo.js
 * Motor interactivo de Despacho de Inventario a Tiendas con Sugerido Predictivo de 4 Semanas.
 * Pizza Americana - Versión 2.0
 */

var server = "";
var replicaInmPOSBodega = "S";
var usuarioActual = "sistema";
var tiendas = [];
var inventarioActual = []; // Lista completa de insumos devuelta por DespachoSugerido
var filtroCategoriaActiva = "TODOS";
var busquedaTexto = "";
var estadoControlActual = null;

$(document).ready(function () {
	// 1. Obtener la ruta base del servidor
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));

	// 2. Parámetro de réplica bodega
	$.getJSON(server + 'GetParametro?parametro=REPINMPOSBODEGA', function (data) {
		if (data && data.valortexto) {
			replicaInmPOSBodega = data.valortexto;
		}
	});

	// 3. Usuario en sesión
	$.getJSON(server + 'ObtenerUsuarioSesion', function (data) {
		if (data && data.nombre) {
			usuarioActual = data.nombre;
		}
	});

	// 4. Inicializar fecha de hoy (DD/MM/YYYY)
	inicializarFechaHoy();

	// 5. Inicializar datepicker si está disponible
	if ($.fn.datepicker) {
		$("#fechasurtir").datepicker({
			dateFormat: 'dd/mm/yy',
			dayNamesMin: ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá'],
			monthNames: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
		});
	}

	// 6. Cargar catálogo de tiendas
	cargarTiendas();

	// 7. Evento cambio de tienda para refrescar el banner de control
	$("#selectTiendas").on("change", function () {
		var idTienda = $(this).children(":selected").attr("id");
		if (idTienda) {
			consultarEstadoControl(idTienda);
		}
	});

	// 8. Navegación con teclado arriba/abajo en inputs de cantidad
	$('body').on('keydown', '.stepper-input', function (e) {
		if (e.keyCode === 38) { // Flecha arriba
			e.preventDefault();
			moverFoco($(this), -1);
		} else if (e.keyCode === 40) { // Flecha abajo
			e.preventDefault();
			moverFoco($(this), 1);
		}
	});
});

function moverFoco($input, delta) {
	var inputs = $('.stepper-input:visible');
	var idx = inputs.index($input);
	var nextIdx = idx + delta;
	if (nextIdx >= 0 && nextIdx < inputs.length) {
		inputs.eq(nextIdx).focus().select();
	}
}

function inicializarFechaHoy() {
	var hoy = new Date();
	var dd = String(hoy.getDate()).padStart(2, '0');
	var mm = String(hoy.getMonth() + 1).padStart(2, '0');
	var yyyy = hoy.getFullYear();
	$("#fechasurtir").val(dd + '/' + mm + '/' + yyyy);
}

/**
 * Carga el combo de tiendas y consulta el estado del sugerido de la primera tienda
 */
function cargarTiendas() {
	$.getJSON(server + 'GetTiendas', function (data) {
		tiendas = data || [];
		var html = '';
		for (var i = 0; i < tiendas.length; i++) {
			var t = tiendas[i];
			html += '<option value="' + t.nombre + '" id="' + t.id + '">' + t.nombre + '</option>';
		}
		$("#selectTiendas").html(html);

		// Consultar el estado de la primera tienda seleccionada
		if (tiendas.length > 0) {
			consultarEstadoControl(tiendas[0].id);
		}
	});
}

/**
 * Consulta si el sugerido predictivo de la tienda está vigente o tiene más de 4 semanas
 */
function consultarEstadoControl(idTienda) {
	if (!idTienda) return;
	$.getJSON(server + 'DespachoSugerido?accion=consultar_control&idtienda=' + idTienda, function (data) {
		if (data && data.respuesta === 'OK') {
			estadoControlActual = data;
			renderizarBannerControl(data);
		}
	});
}

/**
 * Renderiza el banner informativo con el estado del sugerido predictivo
 */
function renderizarBannerControl(data) {
	var $banner = $("#banner-control");
	var $icon = $("#banner-icon");
	var $texto = $("#banner-texto");
	var $detalle = $("#banner-detalle");
	var $btnUpdate = $("#btnBannerUpdate");

	$banner.removeClass("vigente desactualizado cargando");

	if (!data.tiene_control) {
		$banner.addClass("desactualizado").show();
		$icon.attr("class", "fas fa-exclamation-circle mr-2");
		$texto.html("<strong>Atención:</strong> Esta tienda aún no tiene un inventario sugerido predictivo calculado.");
		$detalle.text("Al presionar 'Cargar / Calcular Despacho', el sistema calculará automáticamente el promedio de las últimas 4 semanas.");
		$btnUpdate.show();
	} else if (data.requiere_actualizar) {
		$banner.addClass("desactualizado").show();
		$icon.attr("class", "fas fa-history mr-2");
		$texto.html("<strong>Sugerido pendiente de actualización:</strong> Han transcurrido " + data.dias_desde_actualizacion + " días desde el último recalculo (límite recomendado: 28 días / 4 semanas).");
		$detalle.text("Última actualización: " + (data.fecha_ultima_actualizacion || 'N/A') + " · Despachos analizados: " + (data.total_despachos || 0));
		$btnUpdate.show();
	} else {
		$banner.addClass("vigente").show();
		$icon.attr("class", "fas fa-check-circle mr-2");
		$texto.html("<strong>Sugerido predictivo al día:</strong> Calculado con base en las 4 semanas del " + data.fecha_desde + " al " + data.fecha_hasta + ".");
		$detalle.text("Analizó " + data.total_despachos + " despachos reales · Vigencia: " + data.dias_desde_actualizacion + " de 28 días.");
		$btnUpdate.hide();
	}
}

/**
 * Carga el inventario de la tienda para la fecha seleccionada.
 * Si requiere actualización de 4 semanas, avisa y lo hace de inmediato.
 */
function cargarInventarioTienda() {
	var fechaSurtir = $("#fechasurtir").val();
	var idTienda = $("#selectTiendas option:selected").attr("id");
	var nombreTienda = $("#selectTiendas").val();

	if (!fechaSurtir || fechaSurtir.trim() === '') {
		$.alert({
			title: 'Fecha requerida',
			content: 'Por favor indique la fecha a surtir.',
			type: 'orange'
		});
		return;
	}

	if (!idTienda) {
		$.alert({
			title: 'Tienda requerida',
			content: 'Por favor seleccione una tienda destino válida.',
			type: 'orange'
		});
		return;
	}

	// Consultar estado previo de control
	$("#btnCalcular").prop("disabled", true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Verificando...');

	$.getJSON(server + 'DespachoSugerido?accion=consultar_control&idtienda=' + idTienda, function (ctrl) {
		estadoControlActual = ctrl;

		if (!ctrl.tiene_control || ctrl.requiere_actualizar) {
			// Requiere recalcular las 4 semanas
			mostrarAvisoActualizacionYEjecutar(idTienda, nombreTienda, fechaSurtir, function () {
				ejecutarCargaInventario(idTienda, fechaSurtir);
			});
		} else {
			// Está al día, cargar directamente
			ejecutarCargaInventario(idTienda, fechaSurtir);
		}
	}).fail(function () {
		// En caso de fallo de red, intentar cargar directamente
		ejecutarCargaInventario(idTienda, fechaSurtir);
	});
}

/**
 * Muestra el banner/modal informando al usuario que se está aprendiendo de las últimas 4 semanas
 */
function mostrarAvisoActualizacionYEjecutar(idTienda, nombreTienda, fechaSurtir, callback) {
	var $banner = $("#banner-control");
	$banner.removeClass("vigente desactualizado").addClass("cargando").show();
	$("#banner-icon").attr("class", "fas fa-spinner fa-spin mr-2");
	$("#banner-texto").html("<strong>Actualizando aprendizaje predictivo:</strong> Analizando el histórico de consumos y despachos de las últimas 4 semanas para " + nombreTienda + "...");
	$("#banner-detalle").text("Por favor espere unos segundos mientras se recalculan los promedios y canastas sugeridas.");
	$("#btnBannerUpdate").hide();

	$.ajax({
		url: server + 'DespachoSugerido?accion=actualizar_4sem&idtienda=' + idTienda + '&usuario=' + encodeURIComponent(usuarioActual),
		dataType: 'json',
		type: 'POST',
		success: function (res) {
			if (res && res.respuesta === 'OK') {
				consultarEstadoControl(idTienda);
				if (typeof callback === 'function') {
					callback();
				}
			} else {
				$.alert({
					title: 'Aviso al recalcular 4 semanas',
					content: (res && res.detalle) ? res.detalle : 'No se pudo completar el recalculo automático.',
					type: 'red'
				});
				if (typeof callback === 'function') {
					callback();
				}
			}
		},
		error: function () {
			if (typeof callback === 'function') {
				callback();
			}
		}
	});
}

/**
 * Fuerza manualmente la actualización del sugerido de 4 semanas
 */
function forzarActualizacion4Semanas() {
	var idTienda = $("#selectTiendas option:selected").attr("id");
	var nombreTienda = $("#selectTiendas").val();

	if (!idTienda) {
		$.alert({
			title: 'Tienda no seleccionada',
			content: 'Seleccione una tienda primero.',
			type: 'orange'
		});
		return;
	}

	$.confirm({
		title: '<i class="fas fa-sync-alt text-info"></i> Recalcular Sugerido de 4 Semanas',
		content: '¿Desea recalcular el sugerido predictivo para <strong>' + nombreTienda + '</strong>?<br><br>' +
			'<small class="text-muted">El sistema consultará los despachos reales realizados en las últimas 4 semanas, ' +
			'hallará los promedios reales de consumo y ajustará las cantidades y canastas sugeridas.</small>',
		type: 'blue',
		buttons: {
			confirmar: {
				text: 'Sí, Recalcular Ahora',
				btnClass: 'btn-primary',
				action: function () {
					mostrarAvisoActualizacionYEjecutar(idTienda, nombreTienda, $("#fechasurtir").val(), function () {
						ejecutarCargaInventario(idTienda, $("#fechasurtir").val());
					});
				}
			},
			cancelar: {
				text: 'Cancelar'
			}
		}
	});
}

/**
 * Invoca el servlet para traer todos los insumos, sugeridos, stock y cantidades
 */
function ejecutarCargaInventario(idTienda, fechaSurtir) {
	$("#btnCalcular").prop("disabled", true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Cargando Insumos...');

	$.ajax({
		url: server + 'DespachoSugerido?accion=obtener_inventario&idtienda=' + idTienda + '&fechasurtir=' + encodeURIComponent(fechaSurtir),
		dataType: 'json',
		type: 'GET',
		success: function (data) {
			$("#btnCalcular").prop("disabled", false).html('<i class="fas fa-search mr-1"></i> Cargar / Calcular Despacho');

			if (data && data.respuesta === 'OK') {
				inventarioActual = data.insumos || [];
				if (data.control) {
					renderizarBannerControl(data.control);
				}

				// Construir filtros de categorías
				construirFiltrosCategorias();

				// Renderizar tabla
				filtroCategoriaActiva = "TODOS";
				busquedaTexto = "";
				$("#busqueda-insumo").val("");
				renderizarTabla();
				actualizarKPIs();

				// Mostrar paneles
				$("#panel-kpis").fadeIn(200);
				$("#panel-toolbar").fadeIn(200);
				$("#panel-tabla").fadeIn(200);

				// Scroll suave hacia la tabla
				$('html, body').animate({
					scrollTop: $("#panel-kpis").offset().top - 20
				}, 400);

			} else {
				$.alert({
					title: 'Error al consultar inventario',
					content: data.detalle || 'No fue posible obtener el inventario de la tienda.',
					type: 'red'
				});
			}
		},
		error: function (xhr, status, error) {
			$("#btnCalcular").prop("disabled", false).html('<i class="fas fa-search mr-1"></i> Cargar / Calcular Despacho');
			$.alert({
				title: 'Error de comunicación',
				content: 'Ocurrió un error al contactar al servidor: ' + error,
				type: 'red'
			});
		}
	});
}

/**
 * Crea las pastillas (pills) dinámicas de categorías basadas en los insumos recibidos
 */
function construirFiltrosCategorias() {
	var mapaCategorias = {};
	for (var i = 0; i < inventarioActual.length; i++) {
		var it = inventarioActual[i];
		var cat = it.nombrecategoria || "General";
		if (!mapaCategorias[cat]) {
			mapaCategorias[cat] = {
				id: it.idcategoria,
				color: it.colorcategoria || "#64748B",
				cuenta: 0
			};
		}
		mapaCategorias[cat].cuenta++;
	}

	var html = '<span class="filter-pill active" onclick="filtrarCategoria(\'TODOS\', this)">Todos (' + inventarioActual.length + ')</span>';
	html += '<span class="filter-pill" onclick="filtrarCategoria(\'SOLO_DESPACHO\', this)" style="border-color: #16704F; color: #16704F;">' +
		'<i class="fas fa-check-circle mr-1"></i> Solo con Despacho (&gt; 0)' +
		'</span>';

	var categorias = Object.keys(mapaCategorias).sort();
	for (var j = 0; j < categorias.length; j++) {
		var nombreCat = categorias[j];
		var meta = mapaCategorias[nombreCat];
		html += '<span class="filter-pill" onclick="filtrarCategoria(\'' + nombreCat.replace(/'/g, "\\'") + '\', this)">' +
			nombreCat + ' (' + meta.cuenta + ')' +
			'</span>';
	}

	$("#contenedor-filtros-cat").html(html);
}

/**
 * Filtra por categoría o modo especial (SOLO_DESPACHO, TODOS)
 */
function filtrarCategoria(categoria, elem) {
	filtroCategoriaActiva = categoria;
	$(".filter-pill").removeClass("active");
	if (elem) {
		$(elem).addClass("active");
	}
	renderizarTabla();
}

/**
 * Filtro de búsqueda en vivo por texto
 */
function aplicarFiltrosTabla() {
	busquedaTexto = ($("#busqueda-insumo").val() || "").trim().toLowerCase();
	renderizarTabla();
}

/**
 * Renderiza el cuerpo de la tabla aplicando los filtros de categoría y texto
 */
function renderizarTabla() {
	var tbody = $("#tbody-despacho");
	var html = '';
	var visibleCount = 0;

	for (var i = 0; i < inventarioActual.length; i++) {
		var it = inventarioActual[i];

		// Filtro 1: Categoría
		if (filtroCategoriaActiva === 'SOLO_DESPACHO') {
			if (it.cantidadllevar <= 0) continue;
		} else if (filtroCategoriaActiva !== 'TODOS') {
			if (it.nombrecategoria !== filtroCategoriaActiva) continue;
		}

		// Filtro 2: Texto
		if (busquedaTexto.length > 0) {
			var nombre = (it.nombreinsumo || "").toLowerCase();
			var cat = (it.nombrecategoria || "").toLowerCase();
			if (nombre.indexOf(busquedaTexto) === -1 && cat.indexOf(busquedaTexto) === -1) {
				continue;
			}
		}

		visibleCount++;
		var esActivo = it.cantidadllevar > 0;
		var rowClass = esActivo ? 'row-activo' : 'row-inactivo';

		// Formato promedio
		var promTexto = it.promedio4sem > 0 ? it.promedio4sem.toLocaleString('es-CO', { minimumFractionDigits: 1, maximumFractionDigits: 2 }) : '-';
		var tooltipHist = 'Histórico: ' + it.total4sem + ' en ' + it.vecesdespachado + ' despachos analizados';

		// Empaque / Canastas
		var empaqueHtml = calcularHtmlEmpaque(it);

		// Badge Categoría
		var colorCat = it.colorcategoria || '#475569';
		var badgeCat = '<span class="badge-cat" style="background-color: ' + hexToRgba(colorCat, 0.12) + '; color: ' + colorCat + '; border-color: ' + hexToRgba(colorCat, 0.3) + ';">' +
			(it.nombrecategoria || 'General') + '</span>';

		html += '<tr id="fila_' + it.idinsumo + '" class="' + rowClass + '">';
		html += '  <td>';
		html += '    <div style="font-weight: 700; color: #1E293B;">' + it.nombreinsumo + '</div>';
		html += '    <small style="color: #64748B;">Unidad: ' + it.unidadmedida + '</small>';
		html += '  </td>';
		html += '  <td>' + badgeCat + '</td>';
		html += '  <td class="text-right" title="' + tooltipHist + '" style="cursor: help; color: #475569;">' + promTexto + '</td>';
		html += '  <td class="text-right" style="font-weight: 700; color: #102F6F;">' + it.sugerido + '</td>';
		html += '  <td class="text-right" style="color: #64748B;">' + it.cantidadtienda + '</td>';
		html += '  <td class="text-center">';
		html += '    <div class="stepper-box">';
		html += '      <button type="button" class="stepper-btn" onclick="ajustarCantidad(' + it.idinsumo + ', -1)" title="Disminuir">-</button>';
		html += '      <input type="number" step="any" min="0" class="stepper-input" id="cant_' + it.idinsumo + '" value="' + it.cantidadllevar + '" oninput="actualizarCantidadInput(' + it.idinsumo + ', this.value)" onfocus="this.select()">';
		html += '      <button type="button" class="stepper-btn" onclick="ajustarCantidad(' + it.idinsumo + ', 1)" title="Aumentar">+</button>';
		html += '    </div>';
		html += '  </td>';
		html += '  <td id="celda_empaque_' + it.idinsumo + '">' + empaqueHtml + '</td>';
		html += '</tr>';
	}

	if (visibleCount === 0) {
		html = '<tr><td colspan="7" class="text-center" style="padding: 30px; color: #94A3B8;">' +
			'<i class="fas fa-search mr-2"></i> No se encontraron insumos con los filtros seleccionados.</td></tr>';
	}

	tbody.html(html);
}

/**
 * Calcula el texto y badge de empaques o canastas para un insumo
 */
function calcularHtmlEmpaque(it) {
	if (it.manejacanastas === 'S' && it.cantidadxcanasta > 0 && it.cantidadllevar > 0) {
		var empaques = Math.ceil(it.cantidadllevar / it.cantidadxcanasta);
		var nombreContenedor = it.nombrecontenedor && it.nombrecontenedor.trim() !== '' ? it.nombrecontenedor : 'Canastas';
		return '<span class="badge-empaque" title="' + it.cantidadxcanasta + ' unidades/' + it.unidadmedida + ' por cada ' + nombreContenedor + '">' +
			'<i class="fas fa-box mr-1"></i> ' + empaques + ' ' + nombreContenedor +
			'</span>';
	} else if (it.manejacanastas === 'S' && it.cantidadxcanasta > 0) {
		return '<span style="color: #94A3B8; font-size: 11.5px;">Cap: ' + it.cantidadxcanasta + ' / ' + (it.nombrecontenedor || 'canasta') + '</span>';
	}
	return '<span style="color: #CBD5E1;">-</span>';
}

/**
 * Ajusta la cantidad con los botones +/-
 */
function ajustarCantidad(idInsumo, delta) {
	var item = buscarItemPorId(idInsumo);
	if (!item) return;

	var paso = (item.manejacanastas === 'S' && item.cantidadxcanasta > 1) ? item.cantidadxcanasta : 1;
	var nuevaCant = Math.max(0, Math.round((item.cantidadllevar + (delta * paso)) * 100) / 100);

	item.cantidadllevar = nuevaCant;
	$("#cant_" + idInsumo).val(nuevaCant);

	actualizarEstadoFila(item);
	actualizarKPIs();
}

/**
 * Actualiza la cantidad cuando el usuario digita directamente en el input
 */
function actualizarCantidadInput(idInsumo, valor) {
	var item = buscarItemPorId(idInsumo);
	if (!item) return;

	var cantNum = parseFloat(valor);
	if (isNaN(cantNum) || cantNum < 0) {
		cantNum = 0;
	}

	item.cantidadllevar = cantNum;
	actualizarEstadoFila(item);
	actualizarKPIs();
}

/**
 * Actualiza visualmente la fila: cambia color (verde si > 0, tenue si 0) y recalculas empaques
 */
function actualizarEstadoFila(item) {
	var $fila = $("#fila_" + item.idinsumo);
	if ($fila.length === 0) return;

	if (item.cantidadllevar > 0) {
		$fila.removeClass("row-inactivo").addClass("row-activo");
	} else {
		$fila.removeClass("row-activo").addClass("row-inactivo");
	}

	$("#celda_empaque_" + item.idinsumo).html(calcularHtmlEmpaque(item));
}

/**
 * Busca un insumo por su ID en la lista en memoria
 */
function buscarItemPorId(idInsumo) {
	for (var i = 0; i < inventarioActual.length; i++) {
		if (inventarioActual[i].idinsumo === idInsumo) {
			return inventarioActual[i];
		}
	}
	return null;
}

/**
 * Llena la cantidad a llevar de todos los insumos con su valor sugerido de 4 semanas
 */
function cargarTodoSugerido() {
	if (!inventarioActual || inventarioActual.length === 0) return;

	for (var i = 0; i < inventarioActual.length; i++) {
		inventarioActual[i].cantidadllevar = inventarioActual[i].sugerido;
	}

	renderizarTabla();
	actualizarKPIs();

	$.alert({
		title: '<i class="fas fa-magic text-primary"></i> Sugerido Cargado',
		content: 'Se han cargado las cantidades sugeridas de las últimas 4 semanas en todos los insumos.',
		type: 'blue'
	});
}

/**
 * Pone en cero la cantidad a surtir de todos los insumos
 */
function limpiarTodoACero() {
	if (!inventarioActual || inventarioActual.length === 0) return;

	for (var i = 0; i < inventarioActual.length; i++) {
		inventarioActual[i].cantidadllevar = 0;
	}

	renderizarTabla();
	actualizarKPIs();
}

/**
 * Recalcula y actualiza los indicadores KPI y el pie de confirmación
 */
function actualizarKPIs() {
	var totalCatalogo = inventarioActual.length;
	var cantItemsDespacho = 0;
	var totalEmpaques = 0;

	for (var i = 0; i < inventarioActual.length; i++) {
		var it = inventarioActual[i];
		if (it.cantidadllevar > 0) {
			cantItemsDespacho++;
			if (it.manejacanastas === 'S' && it.cantidadxcanasta > 0) {
				totalEmpaques += Math.ceil(it.cantidadllevar / it.cantidadxcanasta);
			}
		}
	}

	$("#kpi-cant-items").text(cantItemsDespacho);
	$("#kpi-cant-canastas").text(totalEmpaques);
	$("#kpi-total-catalogo").text(totalCatalogo);

	if (estadoControlActual) {
		$("#kpi-total-despachos-hist").text(estadoControlActual.total_despachos || 0);
		if (estadoControlActual.fecha_desde && estadoControlActual.fecha_hasta) {
			$("#kpi-rango-hist").text(estadoControlActual.fecha_desde + " al " + estadoControlActual.fecha_hasta);
		}
	}

	$("#lbl-total-items-footer").text(cantItemsDespacho + " insumos");
	$("#lbl-total-empaques-footer").text(totalEmpaques + " empaques");

	// Habilitar/deshabilitar botón principal
	if (cantItemsDespacho > 0) {
		$("#btnConfirmarDespacho").prop("disabled", false);
	} else {
		$("#btnConfirmarDespacho").prop("disabled", true);
	}
}

/**
 * Inicia el proceso de confirmación y guardado del despacho
 */
function iniciarConfirmacionDespacho() {
	var fechaSurtir = $("#fechasurtir").val();
	var idTienda = $("#selectTiendas option:selected").attr("id");
	var nombreTienda = $("#selectTiendas").val();

	if (!fechaSurtir || fechaSurtir.trim() === '') {
		$.alert({ title: 'Atención', content: 'La fecha a surtir no puede estar vacía.', type: 'orange' });
		return;
	}

	if (!idTienda) {
		$.alert({ title: 'Atención', content: 'Debe seleccionar una tienda válida.', type: 'orange' });
		return;
	}

	// Contar ítems con cantidad > 0
	var itemsADespachar = [];
	var totalEmpaques = 0;
	for (var i = 0; i < inventarioActual.length; i++) {
		var it = inventarioActual[i];
		if (it.cantidadllevar > 0) {
			itemsADespachar.push(it);
			if (it.manejacanastas === 'S' && it.cantidadxcanasta > 0) {
				totalEmpaques += Math.ceil(it.cantidadllevar / it.cantidadxcanasta);
			}
		}
	}

	if (itemsADespachar.length === 0) {
		$.alert({
			title: 'Sin Insumos para Despachar',
			content: 'No hay ningún insumo con cantidad mayor a cero. Ajuste las cantidades antes de confirmar.',
			type: 'orange'
		});
		return;
	}

	// Validar si ya existe un despacho en esa fecha para esa tienda
	$.getJSON(server + 'ExisteDespachoTiendaFecha?fechasurtir=' + encodeURIComponent(fechaSurtir) + '&idtienda=' + idTienda, function (data) {
		var conteo = data && data[0] ? data[0].conteo : 0;
		if (conteo > 0) {
			$.confirm({
				title: '<i class="fas fa-exclamation-triangle text-warning"></i> Despacho Existente Detectado',
				content: 'Ya existe un despacho registrado para la tienda <strong>' + nombreTienda + '</strong> en la fecha <strong>' + fechaSurtir + '</strong>.<br><br>' +
					'¿Desea registrar un <strong>despacho adicional</strong> para esta misma fecha?',
				type: 'orange',
				buttons: {
					continuar: {
						text: 'Sí, Registrar Despacho Adicional',
						btnClass: 'btn-warning',
						action: function () {
							solicitarConfirmacionFinal(idTienda, nombreTienda, fechaSurtir, itemsADespachar, totalEmpaques);
						}
					},
					cancelar: {
						text: 'Cancelar'
					}
				}
			});
		} else {
			solicitarConfirmacionFinal(idTienda, nombreTienda, fechaSurtir, itemsADespachar, totalEmpaques);
		}
	}).fail(function () {
		solicitarConfirmacionFinal(idTienda, nombreTienda, fechaSurtir, itemsADespachar, totalEmpaques);
	});
}

/**
 * Muestra el diálogo modal de confirmación con el resumen completo del despacho
 */
function solicitarConfirmacionFinal(idTienda, nombreTienda, fechaSurtir, itemsADespachar, totalEmpaques) {
	var observacion = ($("#observacion-despacho").val() || "").trim();

	var htmlResumen = '<div style="font-size: 13.5px; line-height: 1.6;">' +
		'<p>Está a punto de enviar el inventario hacia:</p>' +
		'<div style="background:#F1F5F9; border-left: 4px solid #16704F; padding: 10px 14px; border-radius: 4px; margin-bottom: 12px;">' +
		'  <div><strong>Tienda Destino:</strong> ' + nombreTienda + '</div>' +
		'  <div><strong>Fecha a Surtir:</strong> ' + fechaSurtir + '</div>' +
		'  <div><strong>Total Insumos a Despachar:</strong> <span class="text-success font-weight-bold">' + itemsADespachar.length + ' ítems</span></div>' +
		'  <div><strong>Total Empaques / Canastas:</strong> <span class="text-warning font-weight-bold">' + totalEmpaques + ' empaques</span></div>' +
		(observacion ? '  <div style="margin-top: 6px;"><strong>Observación:</strong> <em>' + escapeHtml(observacion) + '</em></div>' : '') +
		'</div>' +
		'<p style="margin: 0;">¿Confirma la generación y despacho oficial de este inventario?</p>' +
		'</div>';

	$.confirm({
		title: '<i class="fas fa-truck-loading text-success"></i> Confirmación de Despacho a Tienda',
		content: htmlResumen,
		type: 'green',
		buttons: {
			confirmar: {
				text: '<i class="fas fa-check mr-1"></i> Sí, Despachar Inventario',
				btnClass: 'btn-success',
				action: function () {
					ejecutarEnvioDespacho(idTienda, nombreTienda, fechaSurtir, itemsADespachar, observacion);
				}
			},
			cancelar: {
				text: 'Revisar / Modificar'
			}
		}
	});
}

/**
 * Guarda en base de datos la cabecera y el detalle del despacho, luego genera el archivo y retiro
 */
function ejecutarEnvioDespacho(idTienda, nombreTienda, fechaSurtir, itemsADespachar, observacion) {
	var estado = (replicaInmPOSBodega === 'S') ? "DESPACHADO" : "PORCONFIRMAR";
	var obsCodificada = encodeURIComponent(observacion.substring(0, 500));

	$("#btnConfirmarDespacho").prop("disabled", true).html('<i class="fas fa-spinner fa-spin mr-2"></i> Procesando Despacho...');

	// 1. Insertar cabecera de despacho
	var urlCabecera = server + 'InsertarDespachoTienda?fechasurtir=' + encodeURIComponent(fechaSurtir) +
		'&idtienda=' + idTienda +
		'&observacion=' + obsCodificada +
		'&estado=' + estado;

	$.ajax({
		url: urlCabecera,
		dataType: 'json',
		type: 'GET',
		success: function (dataCabecera) {
			var resp = (dataCabecera && dataCabecera.length > 0) ? dataCabecera[0] : null;
			var idDespacho = resp ? resp.iddespacho : 0;

			if (!idDespacho || idDespacho === 0) {
				$("#btnConfirmarDespacho").prop("disabled", false).html('<i class="fas fa-paper-plane mr-2"></i> Confirmar y Despachar a Tienda');
				$.alert({
					title: 'Error al Crear Despacho',
					content: 'No fue posible registrar la cabecera del despacho en la base de datos.',
					type: 'red'
				});
				return;
			}

			// 2. Insertar detalles
			var erroresDetalle = [];
			var insertados = 0;

			for (var i = 0; i < itemsADespachar.length; i++) {
				var it = itemsADespachar[i];
				var contenedor = "";
				if (it.manejacanastas === 'S' && it.cantidadxcanasta > 0) {
					contenedor = String(Math.ceil(it.cantidadllevar / it.cantidadxcanasta));
				}

				var urlDetalle = server + 'InsertarDetalleDespachoTienda?iddespacho=' + idDespacho +
					'&idinsumo=' + it.idinsumo +
					'&cantidad=' + it.cantidadllevar +
					'&contenedor=' + encodeURIComponent(contenedor);

				$.ajax({
					url: urlDetalle,
					dataType: 'json',
					async: false,
					success: function (dataDet) {
						var d = (dataDet && dataDet.length > 0) ? dataDet[0] : null;
						if (d && d.iddespachodetalle > 0) {
							insertados++;
						} else {
							erroresDetalle.push(it.nombreinsumo);
						}
					},
					error: function () {
						erroresDetalle.push(it.nombreinsumo);
					}
				});
			}

			// 3. Procesos posteriores: archivo POS y retiro bodega
			$.getJSON(server + 'GenerarArchivoDespachoTienda?idtienda=' + idTienda + '&iddespacho=' + idDespacho + '&fecha=' + encodeURIComponent(fechaSurtir), function () { });

			if (replicaInmPOSBodega === 'S') {
				$.getJSON(server + 'InsertarRetiroInventarioBodega?iddespacho=' + idDespacho, function () { });
			}

			$("#btnConfirmarDespacho").prop("disabled", false).html('<i class="fas fa-paper-plane mr-2"></i> Confirmar y Despachar a Tienda');

			// 4. Mostrar resultado al usuario
			if (erroresDetalle.length > 0) {
				$.alert({
					title: '<i class="fas fa-exclamation-triangle text-warning"></i> Despacho con Novedades',
					content: 'El despacho <strong>#' + idDespacho + '</strong> se generó, pero los siguientes insumos presentaron novedad al guardarse:<br><br>' +
						'<code>' + erroresDetalle.join(', ') + '</code>',
					type: 'orange'
				});
			} else {
				$.confirm({
					title: '<i class="fas fa-check-circle text-success"></i> ¡Despacho Exitoso!',
					content: '<div style="font-size: 14px; text-align: center; padding: 10px 0;">' +
						'<i class="fas fa-truck-loading" style="font-size: 42px; color: #16704F; margin-bottom: 12px; display: block;"></i>' +
						'Se ha generado y despachado con éxito el inventario para <strong>' + nombreTienda + '</strong>.<br>' +
						'Número de Despacho Oficial: <strong style="font-size: 20px; color: #102F6F;">#' + idDespacho + '</strong><br>' +
						'<small class="text-muted">Total ' + insertados + ' insumos procesados · Archivo de integración generado.</small>' +
						'</div>',
					type: 'green',
					buttons: {
						aceptar: {
							text: 'Aceptar y Nuevo Despacho',
							btnClass: 'btn-success',
							action: function () {
								limpiarTodoACero();
								$("#observacion-despacho").val("");
								$("#panel-tabla").hide();
								$("#panel-toolbar").hide();
								$("#panel-kpis").hide();
								consultarEstadoControl(idTienda);
							}
						}
					}
				});
			}
		},
		error: function (xhr, status, error) {
			$("#btnConfirmarDespacho").prop("disabled", false).html('<i class="fas fa-paper-plane mr-2"></i> Confirmar y Despachar a Tienda');
			$.alert({
				title: 'Error de Red',
				content: 'No se pudo completar el despacho: ' + error,
				type: 'red'
			});
		}
	});
}

function hexToRgba(hex, alpha) {
	if (!hex || hex.indexOf('#') === -1) return 'rgba(100, 116, 139, ' + alpha + ')';
	var c = hex.substring(1);
	if (c.length === 3) c = c[0] + c[0] + c[1] + c[1] + c[2] + c[2];
	var num = parseInt(c, 16);
	return 'rgba(' + (num >> 16) + ', ' + ((num >> 8) & 255) + ', ' + (num & 255) + ', ' + alpha + ')';
}

function escapeHtml(str) {
	return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

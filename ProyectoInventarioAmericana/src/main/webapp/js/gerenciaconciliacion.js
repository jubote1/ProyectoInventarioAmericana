(function ($) {
	'use strict';

	var PANTALLA = 'ConciliacionNomina.html';
	var server = window.location.protocol + '//' + window.location.host
		+ window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/') + 1);

	var modoActual = 'QUINCENA'; // 'QUINCENA' o 'MES'
	var datosActuales = null;
	var filtroEstadoActual = 'todos';
	var filtroTiendaActual = '';
	var filtroTextoActual = '';

	$(document).ready(function () {
		revisarAcceso();
		configurarEventos();
	});

	// =========================================================================
	// Acceso y Sesión de Gerencia
	// =========================================================================

	function revisarAcceso() {
		$.ajax({
			url: server + 'GerenciaAcceso',
			type: 'GET',
			dataType: 'json',
			cache: false,
			success: function (data) {
				if (data && data.respuesta === 'OK') {
					mostrarContenido(data.quien || 'Gerencia');
				} else {
					mostrarReja();
				}
			},
			error: function () {
				mostrarReja();
			}
		});
	}

	function mostrarReja() {
		$('#ger-reja').show();
		$('#ger-contenido').hide();
		$('#ger-usuario').focus();
	}

	function mostrarContenido(quien) {
		$('#ger-reja').hide();
		$('#ger-contenido').show();
		$('#ger-usuario-activo').text(quien);
		cargarPeriodos();
	}

	$('#formAcceso').on('submit', function (e) {
		e.preventDefault();
		var u = $('#ger-usuario').val().trim();
		var c = $('#ger-clave').val();
		if (!u || !c) {
			$('#ger-reja-mensaje').text('Ingrese usuario y clave').show();
			return;
		}
		$('#ger-reja-mensaje').hide();
		$.ajax({
			url: server + 'GerenciaAcceso',
			type: 'POST',
			data: { usuario: u, clave: c },
			dataType: 'json',
			success: function (data) {
				if (data && data.respuesta === 'OK') {
					mostrarContenido(data.quien || u);
				} else {
					$('#ger-reja-mensaje').text(data.detalle || 'Credenciales no autorizadas para Gerencia').show();
				}
			},
			error: function () {
				$('#ger-reja-mensaje').text('Error de conexión con el servidor de autenticación').show();
			}
		});
	});

	$('#btnSalirGerencia').on('click', function (e) {
		e.preventDefault();
		$.ajax({
			url: server + 'GerenciaAcceso?accion=salir',
			type: 'POST',
			complete: function () {
				location.reload();
			}
		});
	});

	// =========================================================================
	// Carga de Períodos y Configuración
	// =========================================================================

	function cargarPeriodos() {
		$.ajax({
			url: server + 'GerenciaNominaCalculo?que=periodos',
			type: 'GET',
			dataType: 'json',
			cache: false,
			success: function (data) {
				var $sel = $('#selPeriodoNomina');
				$sel.empty();
				if (data && data.periodos && data.periodos.length > 0) {
					for (var i = 0; i < data.periodos.length; i++) {
						var p = data.periodos[i];
						var label = 'Período ' + p.id + ' (' + p.desde + ' al ' + p.hasta + ')';
						if (p.semanas && p.semanas.length > 0) {
							label += ' - ' + p.semanas.length + ' sem';
						}
						var $opt = $('<option></option>').val(p.id).text(label);
						$opt.data('info', p);
						$sel.append($opt);
					}
					// Seleccionar el período reciente por defecto
					actualizarTextoPeriodo();
					consultarConciliacion();
				} else {
					$sel.append('<option value="">No hay períodos registrados</option>');
				}
			},
			error: function () {
				$('#selPeriodoNomina').html('<option value="">Error cargando períodos</option>');
			}
		});
	}

	function actualizarTextoPeriodo() {
		if (modoActual === 'QUINCENA') {
			var opt = $('#selPeriodoNomina option:selected');
			var p = opt.data('info');
			if (p) {
				var semanasTxt = (p.semanas && p.semanas.length > 0) ? p.semanas.join(', ') : 'Sin domingos asignados';
				$('#txtInfoPeriodo').html('<strong>Quincena ' + p.id + ':</strong> ' + p.desde + ' al ' + p.hasta
					+ ' &nbsp;&bull;&nbsp; <strong>Semanas de cierre financiero:</strong> ' + semanasTxt);
			}
		} else {
			var anio = $('#selAnioMes').val();
			var mes = $('#selMes').val();
			var mesNom = $('#selMes option:selected').text();
			$('#txtInfoPeriodo').html('<strong>Mes Completo:</strong> ' + mesNom + ' ' + anio
				+ ' &nbsp;&bull;&nbsp; Incluye todas las semanas asignadas a este mes en el calendario gerencial.');
		}
	}

	// =========================================================================
	// Eventos de la Interfaz
	// =========================================================================

	function configurarEventos() {
		// Toggle modo Quincena vs Mes
		$('#btnModoQuincena').on('click', function (e) {
			e.preventDefault();
			modoActual = 'QUINCENA';
			$('#btnModoQuincena').addClass('btn-primary active').removeClass('btn-default');
			$('#btnModoMes').addClass('btn-default').removeClass('btn-primary active');
			$('#bloqueQuincena').show();
			$('#bloqueMes').hide();
			actualizarTextoPeriodo();
			consultarConciliacion();
		});

		$('#btnModoMes').on('click', function (e) {
			e.preventDefault();
			modoActual = 'MES';
			$('#btnModoMes').addClass('btn-primary active').removeClass('btn-default');
			$('#btnModoQuincena').addClass('btn-default').removeClass('btn-primary active');
			$('#bloqueQuincena').hide();
			$('#bloqueMes').show();
			actualizarTextoPeriodo();
			consultarConciliacion();
		});

		$('#selPeriodoNomina').on('change', function () {
			actualizarTextoPeriodo();
			consultarConciliacion();
		});

		$('#selAnioMes, #selMes').on('change', function () {
			actualizarTextoPeriodo();
			consultarConciliacion();
		});

		$('#btnConsultar').on('click', function () {
			consultarConciliacion();
		});

		// Drawer de Cargue
		$('#btnAbrirCargue').on('click', function () {
			$('#panelCargueDrawer').slideDown(200);
			$('#txtPegadoReal').focus();
		});

		$('#btnCerrarDrawer').on('click', function () {
			$('#panelCargueDrawer').slideUp(200);
		});

		// Filtros por píldora
		$('.filtro-pill').on('click', function () {
			$('.filtro-pill').removeClass('activa');
			$(this).addClass('activa');
			filtroEstadoActual = $(this).data('filtro');
			aplicarFiltrosYRenderizar();
		});

		// Filtro Tienda
		$('#selFiltroTienda').on('change', function () {
			filtroTiendaActual = $(this).val();
			aplicarFiltrosYRenderizar();
		});

		// Búsqueda en vivo
		$('#txtBuscarEmpleado').on('input', function () {
			filtroTextoActual = $(this).val().toLowerCase().trim();
			aplicarFiltrosYRenderizar();
		});

		// Previsualizar pegado
		$('#btnPrevisualizar').on('click', function () {
			previsualizarPegado();
		});

		// Guardar cargue
		$('#btnGuardarCargue').on('click', function () {
			guardarCargueReal();
		});

		// Exportar a Excel
		$('#btnExportarExcel').on('click', function () {
			exportarExcel();
		});

		// Guardar Nota en Modal
		$('#btnGuardarNota').on('click', function () {
			guardarNotaModal();
		});
	}

	// =========================================================================
	// Consulta de Conciliación (AJAX)
	// =========================================================================

	function consultarConciliacion() {
		var idPeriodo = $('#selPeriodoNomina').val() || 0;
		var anio = $('#selAnioMes').val() || 2026;
		var mes = $('#selMes').val() || 10;

		var url = server + 'GerenciaNominaCalculo?que=conciliacion&modo=' + modoActual
			+ '&idperiodo=' + idPeriodo + '&anio=' + anio + '&mes=' + mes;

		$('#tbodyConciliacion').html('<tr><td colspan="9" class="text-center" style="padding:30px; color:#64748B;">'
			+ '<i class="fas fa-spinner fa-spin fa-2x"></i>'
			+ '<div style="margin-top:8px;">Calculando y cruzando nómina real vs biometría...</div></td></tr>');

		$.ajax({
			url: url,
			type: 'GET',
			dataType: 'json',
			cache: false,
			success: function (data) {
				if (data && data.respuesta === 'OK') {
					datosActuales = data;
					actualizarKPIs(data);
					poblarSelectTiendas(data.filas || []);
					aplicarFiltrosYRenderizar();
				} else {
					$('#tbodyConciliacion').html('<tr><td colspan="9" class="text-center text-danger" style="padding:20px;">'
						+ '<i class="fas fa-exclamation-triangle mr-1"></i> ' + (data.detalle || 'Error al obtener la conciliación')
						+ '</td></tr>');
				}
			},
			error: function () {
				$('#tbodyConciliacion').html('<tr><td colspan="9" class="text-center text-danger" style="padding:20px;">'
					+ '<i class="fas fa-times-circle mr-1"></i> Error de conexión con el servidor'
					+ '</td></tr>');
			}
		});
	}

	function actualizarKPIs(d) {
		$('#kpiTotalReal').text(formatoPesos(d.total_real));
		$('#kpiSubReal').text((d.total_empleados - d.cant_solo_bio) + ' empleados con pago');

		$('#kpiTotalBio').text(formatoPesos(d.total_biometria));
		$('#kpiSubBio').text(formatoNum(d.horas_biometria, 1) + ' horas estimadas');

		var diffSign = d.diff_pesos >= 0 ? '+' : '';
		var diffPctSign = d.diff_pct >= 0 ? '+' : '';
		var badgeClass = Math.abs(d.diff_pct) <= 1.0 ? 'text-success' : (Math.abs(d.diff_pct) <= 5.0 ? 'text-warning' : 'text-danger');
		$('#kpiDiffPesos').html('<span class="' + badgeClass + '">' + diffSign + formatoPesos(d.diff_pesos) + '</span>');
		$('#kpiDiffPct').html('<span class="' + badgeClass + ' font-weight-bold">' + diffPctSign + d.diff_pct + '%</span> de desviación');

		var diffHorasSign = d.diff_horas >= 0 ? '+' : '';
		$('#kpiHorasTotales').text(formatoNum(d.horas_real, 1) + ' vs ' + formatoNum(d.horas_biometria, 1));
		$('#kpiSubHoras').html('&Delta; ' + diffHorasSign + formatoNum(d.diff_horas, 1) + ' hrs diferencia');

		$('#kpiEstadoAuditoria').html('<span class="text-success">' + d.cant_cuadrados + '</span> / <span class="text-danger">' + d.cant_diferencias + '</span>');
		$('#kpiSubAuditoria').text('Total ' + d.total_empleados + ' | Solo Nómina: ' + d.cant_solo_real + ' | Solo Bio: ' + d.cant_solo_bio);

		// Contadores de píldoras
		$('#cntTodos').text(d.total_empleados);
		$('#cntDiferencias').text(d.cant_diferencias);
		$('#cntCuadrados').text(d.cant_cuadrados);
		$('#cntSoloReal').text(d.cant_solo_real);
		$('#cntSoloBio').text(d.cant_solo_bio);
	}

	function poblarSelectTiendas(filas) {
		var tiendas = {};
		for (var i = 0; i < filas.length; i++) {
			var t = filas[i].tienda;
			if (t && t !== 'Sin Asignar' && t !== 'Solo en Nómina') {
				tiendas[t] = true;
			}
		}
		var $sel = $('#selFiltroTienda');
		var selVal = $sel.val();
		$sel.empty().append('<option value="">Todas las Tiendas</option>');
		var ordenadas = Object.keys(tiendas).sort();
		for (var j = 0; j < ordenadas.length; j++) {
			$sel.append('<option value="' + ordenadas[j] + '">' + ordenadas[j] + '</option>');
		}
		if (tiendas[selVal]) {
			$sel.val(selVal);
		}
	}

	// =========================================================================
	// Renderizado y Filtros de la Tabla
	// =========================================================================

	function aplicarFiltrosYRenderizar() {
		if (!datosActuales || !datosActuales.filas) {
			return;
		}

		var filas = datosActuales.filas;
		var filtradas = [];

		for (var i = 0; i < filas.length; i++) {
			var f = filas[i];

			// Filtro por estado
			if (filtroEstadoActual === 'diferencias') {
				if (f.semaforo !== 'ROJO' && f.semaforo !== 'AMARILLO') continue;
			} else if (filtroEstadoActual === 'cuadrados') {
				if (f.semaforo !== 'VERDE') continue;
			} else if (filtroEstadoActual === 'solo_real') {
				if (f.semaforo !== 'AZUL_SOLO_REAL') continue;
			} else if (filtroEstadoActual === 'solo_bio') {
				if (f.semaforo !== 'AZUL_SOLO_BIO') continue;
			}

			// Filtro por tienda
			if (filtroTiendaActual && f.tienda !== filtroTiendaActual) {
				continue;
			}

			// Filtro por texto
			if (filtroTextoActual) {
				var txt = (f.nombre + ' ' + f.cedula + ' ' + f.cargo + ' ' + f.tienda).toLowerCase();
				if (txt.indexOf(filtroTextoActual) === -1) {
					continue;
				}
			}

			filtradas.push(f);
		}

		renderizarTabla(filtradas);
	}

	function renderizarTabla(filas) {
		var $tbody = $('#tbodyConciliacion');
		$tbody.empty();

		if (filas.length === 0) {
			$tbody.html('<tr><td colspan="9" class="text-center" style="padding:24px; color:#64748B;">'
				+ '<i class="fas fa-search mr-1"></i> No se encontraron empleados con los filtros aplicados'
				+ '</td></tr>');
			return;
		}

		var totHorasBio = 0, totHorasReal = 0;
		var totBio = 0, totReal = 0;

		for (var i = 0; i < filas.length; i++) {
			var f = filas[i];
			totHorasBio += f.horas_bio;
			totHorasReal += f.horas_real;
			totBio += f.costo_bio;
			totReal += f.total_real;

			var $tr = $('<tr></tr>');

			// Semáforo Badge
			var badgeHtml = '';
			if (f.semaforo === 'VERDE') {
				badgeHtml = '<span class="badge-cuadrado"><i class="fas fa-check-circle mr-1"></i>Cuadrado</span>';
			} else if (f.semaforo === 'AMARILLO') {
				badgeHtml = '<span class="badge-alerta"><i class="fas fa-exclamation-triangle mr-1"></i>Diferencia</span>';
			} else if (f.semaforo === 'ROJO') {
				badgeHtml = '<span class="badge-descuadre"><i class="fas fa-times-circle mr-1"></i>Descuadre</span>';
			} else if (f.semaforo === 'AZUL_SOLO_REAL') {
				badgeHtml = '<span class="badge-solo-real"><i class="fas fa-file-invoice mr-1"></i>Solo Nómina</span>';
			} else if (f.semaforo === 'AZUL_SOLO_BIO') {
				badgeHtml = '<span class="badge-solo-bio"><i class="fas fa-fingerprint mr-1"></i>Solo Reloj</span>';
			}

			// Columna Empleado
			var senaBadge = f.es_sena ? ' <span class="badge" style="background:#ea580c; font-size:9.5px;">SENA</span>' : '';
			var colEmpleado = '<div><strong>' + escapar(f.nombre) + '</strong>' + senaBadge + '</div>'
				+ '<div style="font-size:11px; color:#64748B;">C.C. ' + escapar(f.cedula) + ' &middot; ' + escapar(f.cargo) + '</div>';

			// Columna Horas
			var diffHorasStr = (f.diff_horas > 0 ? '+' : '') + formatoNum(f.diff_horas, 1);
			var colHoras = '<div style="font-size:12px;">'
				+ '<span style="color:#2563eb;" title="Horas Biometría">' + formatoNum(f.horas_bio, 1) + '</span> / '
				+ '<span style="color:#059669; font-weight:bold;" title="Horas Reportadas en Nómina">' + (f.tiene_real ? formatoNum(f.horas_real, 1) : '-') + '</span>'
				+ '</div>'
				+ '<div style="font-size:11px; color:' + (Math.abs(f.diff_horas) > 2 ? '#b91c1c' : '#64748b') + ';">'
				+ '&Delta; ' + diffHorasStr + ' hrs'
				+ '</div>';

			// Columna Biometría
			var colBio = '<div>' + formatoPesos(f.costo_bio) + '</div>'
				+ '<div style="font-size:10.5px; color:#64748B;">Bás: ' + formatoPesos(f.basico_bio) + ' &middot; Var: ' + formatoPesos(f.variable_bio) + '</div>';

			// Columna Real
			var origenLabel = f.origen_real ? ' <span class="badge" style="font-size:9px; background:#475569;">' + f.origen_real + '</span>' : '';
			var colReal = '<div><strong>' + (f.tiene_real ? formatoPesos(f.total_real) : '<span style="color:#94a3b8;">Sin cargar</span>') + '</strong>' + origenLabel + '</div>';

			// Columna Diferencia
			var diffSign = f.diff_pesos >= 0 ? '+' : '';
			var diffPctSign = f.diff_pct >= 0 ? '+' : '';
			var diffColor = f.semaforo === 'VERDE' ? '#15803d' : (f.semaforo === 'AMARILLO' ? '#b45309' : (f.semaforo === 'ROJO' ? '#b91c1c' : '#475569'));
			var colDiff = '<div style="font-weight:bold; color:' + diffColor + ';">' + diffSign + formatoPesos(f.diff_pesos) + '</div>'
				+ '<div style="font-size:11px; color:' + diffColor + ';">' + diffPctSign + f.diff_pct + '%</div>';

			// Columna Diagnóstico y Nota
			var obsHtml = '';
			if (f.observacion) {
				obsHtml = '<div style="margin-top:4px; font-size:11px; background:#FEF3C7; color:#92400E; padding:2px 6px; border-radius:4px; border:1px solid #FDE68A;">'
					+ '<i class="fas fa-sticky-note mr-1"></i><strong>Nota:</strong> ' + escapar(f.observacion)
					+ '</div>';
			}
			var colDiagnostico = '<div style="font-size:11.5px; color:#1E293B; line-height:1.3;">' + escapar(f.diagnostico) + '</div>' + obsHtml;

			// Columna Acción
			var btnAccion = '<button type="button" class="btn btn-default btn-xs btn-abrir-nota" data-idx="' + i + '" title="Agregar observación de conciliación">'
				+ '<i class="fas fa-comment-alt text-primary"></i> Conciliar'
				+ '</button>';

			$tr.append('<td>' + colEmpleado + '</td>');
			$tr.append('<td>' + escapar(f.tienda) + '</td>');
			$tr.append('<td class="ger-num">' + colHoras + '</td>');
			$tr.append('<td class="ger-num">' + colBio + '</td>');
			$tr.append('<td class="ger-num">' + colReal + '</td>');
			$tr.append('<td class="ger-num">' + colDiff + '</td>');
			$tr.append('<td style="text-align:center;">' + badgeHtml + '</td>');
			$tr.append('<td>' + colDiagnostico + '</td>');
			$tr.append('<td style="text-align:center;">' + btnAccion + '</td>');

			$tbody.append($tr);
		}

		// Footer Totales
		$('#tfHoras').text(formatoNum(totHorasBio, 1) + ' vs ' + formatoNum(totHorasReal, 1));
		$('#tfTotalBio').text(formatoPesos(totBio));
		$('#tfTotalReal').text(formatoPesos(totReal));
		var totDiff = totReal - totBio;
		$('#tfDiff').html('<span style="color:' + (totDiff >= 0 ? '#b91c1c' : '#15803d') + ';">' + (totDiff >= 0 ? '+' : '') + formatoPesos(totDiff) + '</span>');
		$('#tfTiendas').text(filas.length + ' empleados');

		// Click para abrir nota
		$('.btn-abrir-nota').on('click', function () {
			var idx = $(this).data('idx');
			abrirModalNota(filas[idx]);
		});
	}

	// =========================================================================
	// Modal de Nota / Justificación de Conciliación
	// =========================================================================

	function abrirModalNota(f) {
		$('#modalIdCargue').val(f.idcargue || 0);
		$('#modalSelEstado').val(f.estado || 'CONCILIADO');
		$('#modalTxtObservacion').val(f.observacion || '');

		var diffSign = f.diff_pesos >= 0 ? '+' : '';
		var infoHtml = '<div style="background:#F1F5F9; padding:8px 12px; border-radius:6px;">'
			+ '<strong>' + escapar(f.nombre) + '</strong> (C.C. ' + escapar(f.cedula) + ')<br/>'
			+ '<span style="color:#64748B;">Biometría Est: ' + formatoPesos(f.costo_bio) + ' (' + formatoNum(f.horas_bio, 1) + ' hrs)</span> | '
			+ '<span style="color:#0F172A; font-weight:bold;">Real Nómina: ' + formatoPesos(f.total_real) + ' (' + formatoNum(f.horas_real, 1) + ' hrs)</span><br/>'
			+ '<span style="color:#DC2626; font-weight:bold;">Diferencia: ' + diffSign + formatoPesos(f.diff_pesos) + ' (' + f.diff_pct + '%)</span>'
			+ '</div>';
		$('#modalNotaInfo').html(infoHtml);

		$('#modalNota').modal('show');
	}

	function guardarNotaModal() {
		var idCargue = parseInt($('#modalIdCargue').val(), 10);
		var obs = $('#modalTxtObservacion').val().trim();
		var estado = $('#modalSelEstado').val();

		if (idCargue <= 0) {
			alert('Este empleado aún no tiene un registro de nómina real cargado en el sistema.');
			return;
		}

		$.ajax({
			url: server + 'GerenciaNominaCalculo',
			type: 'POST',
			data: {
				que: 'nota_conciliacion',
				idcargue: idCargue,
				observacion: obs,
				estado: estado
			},
			dataType: 'json',
			success: function (data) {
				if (data && data.respuesta === 'OK') {
					$('#modalNota').modal('hide');
					consultarConciliacion();
				} else {
					alert(data.detalle || 'Error al guardar la nota');
				}
			},
			error: function () {
				alert('Error de conexión con el servidor');
			}
		});
	}

	// =========================================================================
	// Previsualización y Guardado de Cargue Real
	// =========================================================================

	function previsualizarPegado() {
		var texto = $('#txtPegadoReal').val();
		if (!texto || texto.trim().length === 0) {
			$('#resumenPrevisualizacion').html('<div class="text-danger" style="font-size:12px;">Pegue primero las líneas de texto en el recuadro.</div>');
			return;
		}

		var lineas = texto.split(/\r?\n/);
		var leidos = 0;
		var totalPesos = 0;
		var totalHoras = 0;
		var candidatos = [];

		for (var i = 0; i < lineas.length; i++) {
			var l = lineas[i].trim();
			if (!l) continue;

			var campos = l.split(/[\t;]/);
			if (campos.length < 2) {
				campos = l.split(/\s{2,}/);
			}

			var candidateCedula = null;
			var candidateVal = 0;
			var candidateHoras = 0;

			for (var c = 0; c < campos.length; c++) {
				var t = campos[c].trim();
				if (!t) continue;
				if (t === 'CC' || t === 'CE' || t === 'TI' || t === 'CD' || t === 'PT' || t === 'NIT') continue;

				var limpio = t.replace(/\./g, '').replace(/,/g, '').replace(/\$/g, '').replace(/"/g, '').trim();
				if (t.indexOf('E') !== -1 || t.indexOf('e') !== -1) {
					var dExp = parseFloat(t);
					if (!isNaN(dExp)) limpio = String(Math.round(dExp));
				}

				if (/^\d+$/.test(limpio)) {
					var num = parseInt(limpio, 10);
					if (!candidateCedula && limpio.length >= 6 && limpio.length <= 11 && num > 100000 && num < 2000000000) {
						candidateCedula = limpio;
					} else if (num >= 5000) {
						candidateVal = num;
					} else if (num > 0 && num <= 500) {
						candidateHoras = num;
					}
				} else {
					var fDec = parseFloat(t.replace(/\$/g, '').replace(',', '.'));
					if (!isNaN(fDec)) {
						if (fDec >= 5000) candidateVal = fDec;
						else if (fDec > 0 && fDec <= 500) candidateHoras = fDec;
					}
				}
			}

			if (candidateCedula && candidateVal > 0) {
				leidos++;
				totalPesos += candidateVal;
				totalHoras += candidateHoras;
				if (candidatos.length < 4) {
					candidatos.push('C.C. ' + candidateCedula + ' &rarr; $' + formatoPesos(candidateVal) + (candidateHoras > 0 ? ' (' + candidateHoras + ' hrs)' : ''));
				}
			}
		}

		var html = '<div class="alert alert-success" style="padding:8px 12px; font-size:12px; margin-bottom:4px;">'
			+ '<strong><i class="fas fa-check mr-1"></i> Previsualización exitosa:</strong> '
			+ leidos + ' registros válidos reconocidos. Total valor: <strong>' + formatoPesos(totalPesos) + '</strong>'
			+ (totalHoras > 0 ? ' | Total horas: <strong>' + formatoNum(totalHoras, 1) + ' hrs</strong>' : '')
			+ '<br/><small style="color:#166534;">Muestra: ' + candidatos.join(' &nbsp;&middot;&nbsp; ') + '</small>'
			+ '</div>';
		$('#resumenPrevisualizacion').html(html);
	}

	function guardarCargueReal() {
		var texto = $('#txtPegadoReal').val();
		if (!texto || texto.trim().length === 0) {
			alert('Pegue primero las líneas de texto para guardar.');
			return;
		}

		var idPeriodo = $('#selPeriodoNomina').val() || 0;
		var anio = $('#selAnioMes').val() || 2026;
		var mes = $('#selMes').val() || 10;
		var tipoValor = $('#selTipoValor').val();
		var origen = $('#selOrigenCargue').val();

		if (!confirm('¿Confirma que desea guardar y conciliar estos registros de nómina real para '
			+ (modoActual === 'QUINCENA' ? 'la Quincena ' + idPeriodo : 'el Mes ' + mes + '/' + anio) + '?')) {
			return;
		}

		$('#btnGuardarCargue').prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Guardando...');

		$.ajax({
			url: server + 'GerenciaNominaCalculo',
			type: 'POST',
			data: {
				que: 'cargar_conciliacion',
				modo: modoActual,
				idperiodo: idPeriodo,
				anio: anio,
				mes: mes,
				tipo_valor: tipoValor,
				origen: origen,
				datos: texto
			},
			dataType: 'json',
			success: function (data) {
				$('#btnGuardarCargue').prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Guardar Nómina Real');
				if (data && data.respuesta === 'OK') {
					var msg = '¡Nómina real guardada con éxito!\n'
						+ 'Cargados: ' + data.cargados + ' empleados.\n'
						+ 'Total $: ' + formatoPesos(data.total_pesos) + '\n'
						+ 'Total Horas: ' + formatoNum(data.total_horas, 1) + ' hrs.';
					if (data.no_encontrados > 0) {
						msg += '\n\nAtención: ' + data.no_encontrados + ' documentos no existen en el maestro de empleados.';
					}
					alert(msg);
					$('#panelCargueDrawer').slideUp(200);
					$('#txtPegadoReal').val('');
					$('#resumenPrevisualizacion').empty();
					consultarConciliacion();
				} else {
					alert(data.detalle || 'Error al guardar el cargue de nómina.');
				}
			},
			error: function () {
				$('#btnGuardarCargue').prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Guardar Nómina Real');
				alert('Error de conexión con el servidor al guardar el cargue.');
			}
		});
	}

	// =========================================================================
	// Exportación a Excel
	// =========================================================================

	function exportarExcel() {
		if (!datosActuales || !datosActuales.filas || datosActuales.filas.length === 0) {
			alert('No hay datos disponibles para exportar.');
			return;
		}

		var filas = datosActuales.filas;
		var html = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">';
		html += '<head><meta charset="utf-8"/><style>';
		html += 'table { border-collapse: collapse; } th, td { border: 1px solid #ccc; padding: 4px 8px; font-family: Arial; font-size: 11px; }';
		html += 'th { background-color: #102F6F; color: #ffffff; }';
		html += '</style></head><body>';
		html += '<h3>CONCILIACIÓN DE NÓMINA - PIZZA AMERICANA</h3>';
		html += '<p><strong>Período:</strong> ' + escapar(datosActuales.periodo_texto) + '</p>';
		html += '<table><thead><tr>';
		html += '<th>Cédula</th><th>Nombre</th><th>Cargo</th><th>Tienda</th>';
		html += '<th>Horas Biometría</th><th>Horas Real</th><th>Diferencia Horas</th>';
		html += '<th>Biometría Costo ($)</th><th>Nómina Real ($)</th><th>Diferencia ($)</th><th>Variación (%)</th>';
		html += '<th>Semáforo</th><th>Diagnóstico</th><th>Observación Conciliación</th><th>Estado</th>';
		html += '</tr></thead><tbody>';

		for (var i = 0; i < filas.length; i++) {
			var f = filas[i];
			html += '<tr>';
			html += '<td>' + f.cedula + '</td>';
			html += '<td>' + escapar(f.nombre) + '</td>';
			html += '<td>' + escapar(f.cargo) + '</td>';
			html += '<td>' + escapar(f.tienda) + '</td>';
			html += '<td>' + f.horas_bio + '</td>';
			html += '<td>' + f.horas_real + '</td>';
			html += '<td>' + f.diff_horas + '</td>';
			html += '<td>' + f.costo_bio + '</td>';
			html += '<td>' + f.total_real + '</td>';
			html += '<td>' + f.diff_pesos + '</td>';
			html += '<td>' + f.diff_pct + '%</td>';
			html += '<td>' + f.semaforo + '</td>';
			html += '<td>' + escapar(f.diagnostico) + '</td>';
			html += '<td>' + escapar(f.observacion || '') + '</td>';
			html += '<td>' + escapar(f.estado || '') + '</td>';
			html += '</tr>';
		}

		html += '</tbody></table></body></html>';

		var blob = new Blob(['\ufeff' + html], { type: 'application/vnd.ms-excel;charset=utf-8' });
		var url = URL.createObjectURL(blob);
		var a = document.createElement('a');
		a.href = url;
		a.download = 'Conciliacion_Nomina_' + (datosActuales.periodo_texto || 'Periodo').replace(/[^a-zA-Z0-9_-]/g, '_') + '.xls';
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
		URL.revokeObjectURL(url);
	}

	// =========================================================================
	// Funciones Auxiliares de Formato
	// =========================================================================

	function formatoPesos(n) {
		if (isNaN(n) || n === null) return '$ 0';
		var val = Math.round(n);
		var neg = val < 0;
		val = Math.abs(val);
		var s = val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
		return (neg ? '-$ ' : '$ ') + s;
	}

	function formatoNum(n, dec) {
		if (isNaN(n) || n === null) return '0';
		return Number(n).toFixed(dec !== undefined ? dec : 0);
	}

	function escapar(s) {
		if (!s) return '';
		return String(s)
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;');
	}

})(jQuery);

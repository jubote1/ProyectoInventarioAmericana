/**
 * El portero de las pantallas de Gerencia, del lado del navegador.
 *
 * ESTO NO ES LA SEGURIDAD. La seguridad esta en el servidor: cada servlet
 * pregunta por el permiso antes de responder. Esto es lo que hace que la
 * pantalla se vea bien -que pida la clave en vez de mostrar una tabla vacia con
 * un error-, y nada mas. Si alguien desactiva este archivo desde el navegador
 * no consigue ver un solo dato.
 *
 * POR QUE PIDE LA CLAVE OTRA VEZ
 *
 * La aplicacion de inventarios entra con un unico usuario compartido que usan
 * las once tiendas. Esa sesion no dice quien es la persona. Detras de estas
 * pantallas hay nomina, asi que aca hay que identificarse con el usuario
 * propio, el mismo del contact center.
 */

var Gerencia = (function () {

	var quien = null;
	var alArrancar = null;

	/**
	 * Monta la reja sobre la pantalla.
	 *
	 * contenido  el id del div que hay que esconder mientras no haya permiso
	 * listo      que hacer cuando ya se puede
	 */
	function proteger(contenido, listo) {
		alArrancar = listo;
		pintarFormulario(contenido);
		$('#' + contenido).hide();
		preguntar(contenido);
	}

	function preguntar(contenido) {
		$.getJSON('GerenciaAcceso', function (data) {
			if (data && data.respuesta === 'OK') {
				entrar(contenido, data);
			} else {
				$('#ger-acceso').show();
				$('#ger-usuario').focus();
			}
		}).fail(function () {
			mensaje('No se pudo consultar el acceso. Intente de nuevo.');
			$('#ger-acceso').show();
		});
	}

	function entrar(contenido, data) {
		quien = data;
		$('#ger-acceso').hide();
		$('#' + contenido).show();
		$('#ger-quien').text(data.nombre || data.usuario);
		$('#ger-barra').show();
		if (alArrancar) {
			alArrancar(data);
		}
	}

	function ingresar(contenido) {
		var usuario = $.trim($('#ger-usuario').val());
		var clave = $('#ger-clave').val();
		if (usuario === '' || clave === '') {
			mensaje('Escriba su usuario y su clave.');
			return;
		}
		$('#ger-entrar').prop('disabled', true).text('Validando...');
		$.post('GerenciaAcceso', { accion: 'ingresar', usuario: usuario, clave: clave },
			function (data) {
				$('#ger-entrar').prop('disabled', false).text('Entrar');
				//La clave no se queda en el formulario ni cuando falla.
				$('#ger-clave').val('');
				if (data && data.respuesta === 'OK') {
					mensaje('');
					entrar(contenido, data);
				} else if (data && data.respuesta === 'SINPERMISO') {
					mensaje('Su usuario existe, pero no tiene el rol Gerencia. '
						+ 'Hay que pedirlo: se asigna desde el central, en Asignar Rol a Usuario.');
				} else {
					mensaje((data && data.detalle) ? data.detalle : 'No se pudo validar.');
				}
			}, 'json').fail(function () {
				$('#ger-entrar').prop('disabled', false).text('Entrar');
				$('#ger-clave').val('');
				mensaje('No se pudo validar. Revise la conexion.');
			});
	}

	function salir() {
		$.post('GerenciaAcceso', { accion: 'salir' }, function () {
			location.reload();
		});
	}

	function mensaje(texto) {
		if (texto === '') {
			$('#ger-mensaje').hide().text('');
		} else {
			$('#ger-mensaje').show().text(texto);
		}
	}

	/**
	 * Traduce la respuesta del servidor cuando dice que no.
	 *
	 * Devuelve true cuando la respuesta era buena y la pantalla puede seguir.
	 */
	function respondio(data) {
		if (!data) {
			avisar('El servidor no respondio.');
			return (false);
		}
		if (data.respuesta === 'OK') {
			return (true);
		}
		if (data.respuesta === 'NOSESION') {
			avisar('Se cerro la sesion de Gerencia. Vuelva a entrar.');
			setTimeout(function () { location.reload(); }, 1500);
			return (false);
		}
		if (data.respuesta === 'SINPERMISO') {
			avisar('Su usuario ya no tiene permiso sobre esta pantalla.');
			return (false);
		}
		avisar(data.detalle ? data.detalle : 'No se pudo completar la operacion.');
		return (false);
	}

	/** El aviso de arriba. Se usa para todo, bueno y malo. */
	function avisar(texto, bueno) {
		var caja = $('#ger-aviso');
		if (!texto) {
			caja.hide();
			return;
		}
		caja.removeClass('ger-aviso-ok ger-aviso-mal')
			.addClass(bueno ? 'ger-aviso-ok' : 'ger-aviso-mal')
			.text(texto).show();
		if (bueno) {
			setTimeout(function () { caja.fadeOut(400); }, 3000);
		}
	}

	function pintarFormulario(contenido) {
		var html =
			'<div id="ger-acceso" class="ger-acceso" style="display:none;">'
			+ '  <div class="ger-acceso-caja">'
			+ '    <h4>Menu Gerencia</h4>'
			+ '    <p class="ger-acceso-nota">Esta pantalla muestra informacion de costos y de nomina. '
			+ '       Identifiquese con su usuario propio, el mismo del contact center.</p>'
			+ '    <div class="ger-campo"><label>Usuario</label>'
			+ '      <input type="text" id="ger-usuario" class="form-control" autocomplete="off" /></div>'
			+ '    <div class="ger-campo"><label>Clave</label>'
			+ '      <input type="password" id="ger-clave" class="form-control" autocomplete="off" /></div>'
			+ '    <div id="ger-mensaje" class="ger-mensaje" style="display:none;"></div>'
			+ '    <button id="ger-entrar" class="btn btn-primary btn-block">Entrar</button>'
			+ '  </div>'
			+ '</div>'
			+ '<div id="ger-barra" class="ger-barra" style="display:none;">'
			+ '  <span class="ger-barra-rotulo">Gerencia</span>'
			+ '  <span id="ger-quien" class="ger-barra-quien"></span>'
			+ '  <a href="#" id="ger-cerrar" class="ger-barra-salir">Cerrar Gerencia</a>'
			+ '</div>'
			+ '<div id="ger-aviso" class="ger-aviso" style="display:none;"></div>';

		$('#' + contenido).before(html);

		$('#ger-entrar').on('click', function () { ingresar(contenido); });
		$('#ger-clave').on('keypress', function (e) {
			if (e.which === 13) { ingresar(contenido); }
		});
		$('#ger-usuario').on('keypress', function (e) {
			if (e.which === 13) { $('#ger-clave').focus(); }
		});
		$('#ger-cerrar').on('click', function (e) { e.preventDefault(); salir(); });
	}

	/** Pesos, sin decimales, como se leen en un informe. */
	function pesos(valor) {
		if (valor === null || valor === undefined || isNaN(valor)) {
			return ('');
		}
		return ('$ ' + Math.round(valor).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
	}

	/**
	 * Lee un numero digitado, aceptando coma o punto.
	 *
	 * Devuelve null cuando no se entiende, y quien llama tiene que rechazarlo.
	 * Convertir lo que no se entiende en cero es como se cuelan los ceros que
	 * despues nadie sabe de donde salieron.
	 */
	function numero(texto) {
		if (texto === null || texto === undefined) {
			return (null);
		}
		var limpio = String(texto).trim().replace(/[\s$]/g, '');
		if (limpio === '') {
			return (null);
		}
		var coma = limpio.lastIndexOf(',');
		var punto = limpio.lastIndexOf('.');
		if (coma >= 0 && coma > punto) {
			limpio = limpio.replace(/\./g, '').replace(',', '.');
		} else {
			limpio = limpio.replace(/,/g, '');
		}
		if (!/^\d+(\.\d+)?$/.test(limpio)) {
			return (null);
		}
		var valor = parseFloat(limpio);
		return (isNaN(valor) ? null : valor);
	}

	return {
		proteger: proteger,
		respondio: respondio,
		avisar: avisar,
		pesos: pesos,
		numero: numero,
		quien: function () { return (quien); }
	};
})();

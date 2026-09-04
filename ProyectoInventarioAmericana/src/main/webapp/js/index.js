/** index.js **/

var server;

$(function () 
{

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
		
	$('#txtPassword').keypress(function (event) {
            if (event.which == 13) {
                autenticar();
                //return false; only if needed
            }
        });

});

//Muestra el error en la caja de la tarjeta de ingreso. Si la pagina no la tiene,
//cae al alert de siempre, para no romper ninguna pantalla que reutilice esto.
function mostrarErrorIngreso(mensaje)
{
	var caja = $('#mensajeError');
	if (caja.length > 0)
	{
		caja.text(mensaje).show();
	}
	else
	{
		alert(mensaje);
	}
}

function autenticar()
{
	var usuario =  $('#txtUsuario').val();
	var password =  $('#txtPassword').val();

	if (usuario.trim() === '' || password === '')
	{
		mostrarErrorIngreso('Escriba el usuario y la clave para continuar.');
		return;
	}

	$('#btnIngresar').prop('disabled', true);

	$.ajax({
	    				url: server + 'GetIngresarAplicacion',
	    				dataType: 'text',
	    				type: 'post',
	    				data: {'txtUsuario' : usuario , 'txtPassword' : password },
	    				async: false,
	    				success: function(data){
	    						if(data == 'OK')
	    						{
	    							location.href = server + "CalcularInventario.html";
	    						}
	    						else
	    						{
	    							mostrarErrorIngreso(data);
	    							$('#txtPassword').val('');
	    							$('#btnIngresar').prop('disabled', false);
	    							$('#txtPassword').focus();
	    						}
							},
							error: function(){
									//Antes un servidor caido no mostraba nada: el boton quedaba
									//muerto y el usuario no sabia si habia pasado algo.
									mostrarErrorIngreso('No hubo respuesta del servidor. Intente de nuevo o avise a tecnologia.');
									$('#btnIngresar').prop('disabled', false);
							}
						});
}


	

var server;
var tiendas;
var urlTienda ="";
var consumos;
var bandera = false;
var dataTableDespacho;


// Se arma el valor de la variable global server, con base en la cual se realiza llamado a los servicios.
$(document).ready(function() {

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
	

    

	} );

//Se obtiene el listado de tiendas con el fin de seleccionar la tienda a surtir.

$(function(){


	
});

$('body').keyup(function(e) {
	if(e.keyCode==38)//38 para arriba
      mover(e,-1);
    if(e.keyCode==40)//40 para abajo
      mover(e,1);
 });


function mover(event, to) {
   let list = $('input');
   let index = list.index($(event.target));
   index = (index + to) % list.length;
   list.eq(index).focus();
}




//Método principal invocado para validar los datos de los parámetros y adicionalmente ejecutar los servicios para 
//recuperar la informacion y pintarla en pantalla.
function ConsultaDesechos()
{

	var fechaInicial = $("#fechainicial").val();
	
	if(fechaInicial == '' || fechaInicial == null)
	{
		alert ('La fecha Inicial debe ser diferente a vacía');
		return;
	}

	
	if(existeFecha(fechaInicial))
	{
	}
	else
	{
		alert ('La fecha Inicial no es correcta');
		return;
	}

	//Validaremos si la fecha inicial es un lunes
	var dateFinal = $("#fechainicial").datepicker( 'getDate' );
	
	//Realizaremos una verificación de que sea lunes
	var dia = dateFinal.getDay();

	if(dia != 1)
	{
		alert ('La fecha seleccionada deberá ser un Lunes');
		$("#fechainicial").val('');
		return;
	}

	//Formateamos el valor de la fecha Inicial
	var finalMes = dateFinal.getMonth()+1;
    if(finalMes < 10)
    {
        finalMes = "0" + finalMes;
    }
    var finalDia = dateFinal.getDate();
    if(finalDia  < 10)
    {
        finalDia = "0" + finalDia;
    }
    fechaInicial = dateFinal.getFullYear() + "-" + finalMes + "-" + finalDia;


	dateFinal.setDate(dateFinal.getDate() + 6);

	finalMes = dateFinal.getMonth()+1;
    if(finalMes < 10)
    {
        finalMes = "0" + finalMes;
    }
    finalDia = dateFinal.getDate();
    if(finalDia  < 10)
    {
        finalDia = "0" + finalDia;
    }
    var fechaFinal = dateFinal.getFullYear() + "-" + finalMes + "-" + finalDia;


    strInv = "";
    var direccionURL = server + 'ObtenerDesechosFecha?fechainicial=' + fechaInicial +  "&fechafinal=" + fechaFinal;
	$.ajax({ 
	    		url: direccionURL, 	
	    		dataType: 'text', 
	    		async: false, 
	    		success: function(data1){ 
	    				strInv = data1;
						$('#desechos').html(strInv);
				} 
			});
	
}



function validarSiNumero(numero)
{
    if (!/^([0-9])*$/.test(numero))
     {
     	return(false);
     }else
     {
     	return(true);
     }
  }



function reiniciarInventario()
{
	$('#fechasurtir').attr('disabled', false);
	$('#selectTiendas').attr('disabled', false);
	bandera = false;
	inventarios = "";
	var str = '';
	$('#inventario').html(str);
	$('#observacion').val(str);
}

// Método creado para confirmar que una fecha exista
function existeFecha(fecha){
      var fechaf = fecha.split("/");
      var day = fechaf[0];
      var month = fechaf[1];
      var year = fechaf[2];
      var date = new Date(year,month,'0');
      if((day-0)>(date.getDate()-0)){
            return false;
      }
      return true;
}


function validarFechaMenorActual(date1, date2){
      var fechaini = new Date();
      var fechafin = new Date();
      var fecha1 = date1.split("/");
      var fecha2 = date2.split("/");
      fechaini.setFullYear(fecha1[2],fecha1[1]-1,fecha1[0]);
      fechafin.setFullYear(fecha2[2],fecha2[1]-1,fecha2[0]);
      
      if (fechaini > fechafin)
        return false;
      else
        return true;
}

function desactivarBotones()
{
	$('#calcularinventario').attr('disabled', true);
	$('#confirmarinventario').attr('disabled', true);
	$('#reiniciarinventario').attr('disabled', true);
}

function activarBotones()
{
	$('#calcularinventario').attr('disabled', false);
	$('#confirmarinventario').attr('disabled', false);
	$('#reiniciarinventario').attr('disabled', false);
}


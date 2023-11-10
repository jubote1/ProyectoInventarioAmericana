	

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

	getListaTiendas();
	getTipoConsulta();
	
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



//Método que invoca el servicio para obtener las tiendas
function getListaTiendas(){
	$.getJSON(server + 'GetTiendas', function(data){
		tiendas = data;
		var str = '';
		str +='<option value="TODAS" id ="todas">TODAS</option>';
		for(var i = 0; i < data.length;i++){
			var cadaTienda  = data[i];
			str +='<option value="'+ cadaTienda.nombre +'" id ="'+ cadaTienda.id +'">' + cadaTienda.nombre +'</option>';
		}
		$('#selectTiendas').html(str);
	});
}

function getTipoConsulta(){
	var str = '';
	str +='<option value="resumida" id ="resumido">RESUMIDA</option>';
	str +='<option value="todo" id ="todo">TODO</option>';
	$('#selectTipoConsulta').html(str);
}



//Método principal invocado para validar los datos de los parámetros y adicionalmente ejecutar los servicios para 
//recuperar la informacion y pintarla en pantalla.
function ConsultaConsumos()
{

	var fechaInicial = $("#fechainicial").val();
	var tienda = $("#selectTiendas").val();
	var tipoConsulta = $("#selectTipoConsulta").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	
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

	dateFinal.setDate(dateFinal.getDate() + 6);

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
    var fechaFinal = dateFinal.getFullYear() + "-" + finalMes + "-" + finalDia;


	if (tienda == '' || tienda == null)
	{

		alert ('La tienda no puede estar vacía');
		return;
	}
	if(tienda == 'TODAS')
	{
		idtienda = 0 ;
	}
    var nombreTabla = "";
    if(tienda == "TODAS")
    {
    	nombreTabla = "CONSUMOS DE TODAS LAS TIENDAS ENTRE LAS FECHA INICIAL " + fechaInicial + " Y FECHA FINAL " + fechaFinal;
    }else
    {
    	nombreTabla = "CONSUMOS LA TIENDA " + tienda + " ENTRE LAS FECHA INICIAL " + fechaInicial + " Y FECHA FINAL " + fechaFinal;
    }
    strInv = "";
    var direccionURL = server + 'ConsultarConsumos?fechainicial=' + fechaInicial + "&idtienda=" + idtienda + "&fechafinal=" + fechaFinal + "&tipoconsulta=" + tipoConsulta;
	console.log(direccionURL);
	$.ajax({ 
	    		url: direccionURL, 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				consumos = data1;
	    				var consumo;
						strInv += '<table id="consumos" class="table table-bordered">';
						strInv += '<thead><tr><th COLSPAN="1"><img src="LogoAmericana.png" class="img-circle" /></th>';
						strInv += '<th COLSPAN="7"> <h2>' + nombreTabla +'</h2></th></tr>'
						strInv += '<tr><th>Nom/Insumo</th><th>LUNES</th><th>MARTES</th><th>MIERCOLES</th><th>JUEVES</th><th>VIERNES</th><th>SABADO</th><th>DOMINGO</th></tr></thead>';
				        strInv += '<tbody>';
						for (var i = 0; i < consumos.length; i++)
						{
							consumo = consumos[i];
							strInv +='<tr> ';
							strInv +='<td><label>' + consumo.nombreinsumo + '</label> </td>';
							strInv +='<td><label>' + consumo.lunes + '</label> </td>';
							strInv +='<td><label>' + consumo.martes + '</label> </td>';
							strInv +='<td><label>' + consumo.miercoles + '</label> </td>';
							strInv +='<td><label>' + consumo.jueves + '</label> </td>';
							strInv +='<td><label>' + consumo.viernes + '</label> </td>';
							strInv +='<td><label>' + consumo.sabado + '</label> </td>';
							strInv +='<td><label>' + consumo.domingo + '</label> </td>';
							strInv +='</tr> ';
						}
						strInv +='</tbody> ';
						$('#consumos').html(strInv);
					
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


	

var server;
var empleados;
var urlTienda ="";
var inventarios;
var bandera = false;
var dataTableDespacho;
var dtCapacitacion;


// Se arma el valor de la variable global server, con base en la cual se realiza llamado a los servicios.
$(document).ready(function() {

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
	

    

	} );

//Se obtiene el listado de tiendas con el fin de seleccionar la tienda a surtir.

$(function(){

	getListaEmpleados();
	dtCapacitacion = $('#grid-capacitacion').DataTable( {
    		"aoColumns": [
    		{ "mData": "idcapacitacionemp" },
    		{ "mData": "id"},
            { "mData": "nombrelargo" },
            { "mData": "fecha" },
            { "mData": "horainicio" },
            { "mData": "horafinal" },
            { "mData": "observacion" },
            {
                "mData": "accion",
                className: "center",
                defaultContent: '<input type="button" class="btn btn-default btn-xs" onclick="eliminarDetallePedido()" value="Eliminar"></button>'
            }
        	]
    });	
	
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

function traerCapacitacion()
{
	var fecha = $("#fechaconsulta").val();
	var table;
	if ( $.fn.dataTable.isDataTable( '#grid-capacitacion' ) ) {
    	table = $('#grid-capacitacion').DataTable();
    }
	
	$.getJSON(server + 'ConsultarCapacitaciones?fechaconsulta=' + fecha, function(data1){
			table.clear().draw();
			for(var i = 0; i < data1.length;i++){
				var cadaDetalle = data1[i];
				table.row.add( {
					"idcapacitacionemp": cadaDetalle.idcapacitacionemp,
					"id": cadaDetalle.id,
					"nombrelargo": cadaDetalle.nombrelargo,
					"fecha": cadaDetalle.fecha,
			        "horainicio": cadaDetalle.horainicio,
			        "horafinal":  cadaDetalle.horafinal,
			        "observacion": cadaDetalle.observacion,
			        "accion":'<button type="button" class="btn btn-danger btn-xs" onclick="eliminarDetallePedido(' + cadaDetalle.idcapacitacionemp + ')"><i class="fas fa-trash-alt fa-2x"></i></button>'
			    	}).draw();
			}
		});
}


//Método que invoca el servicio para obet
function getListaEmpleados(){
	$.getJSON(server + 'ObtenerEmpleados', function(data){
		empleados = data;
		var str = '';
		for(var i = 0; i < data.length;i++){
			var cadaEmpleado  = data[i];
			str +='<option value="'+ cadaEmpleado.nombre +'" id ="'+ cadaEmpleado.id +'">' + cadaEmpleado.nombre +'</option>';
		}
		$('#selectEmpleados').html(str);
	});
}



function regCapacitacion()
{
	
	
	var fecha = $("#fechacapacitacion").val();
	if(fecha == '')
	{
		$.alert("Debes seleccionar fecha de la capacitación");
		return;
	}
	var id = $("#selectEmpleados option:selected").attr('id');
	var nombreLargo = $("#selectEmpleados").val();
	if(nombreLargo == '')
	{
		$.alert("Debes seleccionar un empleado");
		return;
	}
	var horaInicio = $("#horainicio").val();
	if(horaInicio == '')
	{
		$.alert("Debes seleccionar una hora de inicio.");
		return;
	}
	var horaFinal = $("#horafinal").val();
	if(horaFinal == '')
	{
		$.alert("Debes seleccionar una hora final.");
		return;
	}
	var observacion  = $("#observacion").val();
	if(observacion == '')
	{
		$.alert("Debes ingresar una observación de la capacitación.");
		return;
	}
	//Vamos a realizar la validación antes de confirmar si desea o no ingresar
	$.ajax({ 
	    		url: server + 'InsertarCapacitacionEmpleado?id=' + id + "&fecha=" + fecha + "&horainicio=" + horaInicio + "&horafinal=" + horaFinal + "&observacion=" + observacion + "&nombrelargo=" + nombreLargo, 
	    		dataType: 'text', 
	    		async: false, 
	    		success: function(data3){ 
	    			console.log(data3)
	    			if(data3 == 'OK')
	    			{
	    				$.alert("Se insertó correctamente la Capacitación.");
	    				$("#fechacapacitacion").val('');
	    				$("#selectEmpleados").val('');
	    				$("#horainicio").val('');
	    				$("#horafinal").val('');
	    				$("#observacion").val('');
	    			}else
	    			{
	    				$.alert("SE TUVO ERRORES al insertar la Capacitación.");
	    			}
	    		}
	    	});
}








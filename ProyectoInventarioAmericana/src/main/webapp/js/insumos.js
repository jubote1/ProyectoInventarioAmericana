var server;
var table;


$(document).ready(function() {

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
	

		//Lo primero que realizaremos es validar si está logueado
	
		
	    table = $('#grid-insumos').DataTable( {
    		"aoColumns": [
            { "mData": "idinsumo" },
            { "mData": "nombreinsumo" },
            { "mData": "unidadmedida" },
            { "mData": "categoria" },
            { "mData": "costounidad" },
            {
                "mData": "accion",
                className: "center",
                defaultContent: '<button type="button" class="btn btn-default btn-xs" onclick="EditarInsumo()">Edicion</button>'
            }
        ]
    	} );
  	  	


  	  	//
  	  	$('#userForm')
        .bootstrapValidator({
            framework: 'bootstrap',
            icon: {
                valid: 'glyphicon glyphicon-ok',
                invalid: 'glyphicon glyphicon-remove',
                validating: 'glyphicon glyphicon-refresh'
            },
            fields: {
                nombreinsumoedit: {
                    validators: {
                        notEmpty: {
                            message: 'El nombre de la especialidad es requerido'
                        },
                        regexp: {
                            regexp: /^[a-zA-Z\s]+$/,
                            message: 'El nombre de la especialidad debe contener solo letras'
                        }
                    }
                },
            }
        })
        
    	//
    	inicializarSelects();
		pintarInsumos();
		setInterval('validarVigenciaLogueo()',600000);
});

function validarVigenciaLogueo()
{
	var d = new Date();
	
	var respuesta ='';
	$.ajax({ 
	   	url: server + 'ValidarUsuarioAplicacion', 
	   	dataType: 'json',
	   	type: 'post', 
	   	async: false, 
	   	success: function(data){
			    respuesta =  data[0].respuesta;		
		} 
	});
	switch(respuesta)
	{
		case 'OK':
				break;
		case 'OKA':
				break;	
		default:
				location.href = server +"Index.html";
		    	break;
	}
		    		
}

function guardarInsumo()
{
	var nombre = $('#nombreinsumo').val();
	var unidadMedida = $('#selectunidadmedida').val();
	var manejaCanastas = $('#selectmanejacanastas').val();
	var cantidadCanasta = $('#cantidadcanastas').val();
	var nombreContenedor = $('#selectnombrecontenedor').val();
	var categoria = $('#selectcategoria').val();
	var controlCantidad = $('#selectcontrolcantidad').val();
	var costoUnidad = $('#costounidad').val();
	var controlTienda = "N";
	var idInsumo;
	$.getJSON(server + 'CRUDInsumo?idoperacion=1&nombreinsumo=' + nombre + "&unidadmedida=" + unidadMedida + "&manejacanastas=" + manejaCanastas + "&cantidadcanasta=" + cantidadCanasta + "&nombrecontenedor=" + nombreContenedor + "&categoria=" + categoria + "&controlcantidad=" + controlCantidad + "&costounidad=" + costoUnidad + "&controltienda=" + controlTienda , function(data){
		var respuesta = data;
		idInsumo= respuesta.idinsumo;
				
	});
}


function inicializarSelects()
{
	var str = '';
	// UNIDAD DE MEDIDA
	str +='<option value="unidad" id ="unidad">unidad</option>';
	str +='<option value="gramos" id ="gramos">gramos</option>';
	str +='<option value="paquete" id ="paquete">paquete</option>';
	str +='<option value="unidades" id ="unidades">unidades</option>';
	$('#selectunidadmedida').html(str);
	$('#selectunidadmedidaedit').html(str);
	str = '';
	// MANEJA CANASTAS
	str +='<option value="S" id ="S">S</option>';
	str +='<option value="N" id ="N">N</option>';
	$('#selectmanejacanastas').html(str);
	$('#selectmanejacanastasedit').html(str);
	str = '';
	// NOMBRE CONTENEDOR
	str +='<option value="no" id ="no"></option>';
	str +='<option value="Paq" id ="Paq">Paq</option>';
	str +='<option value="Can" id ="Can">Can</option>';
	str +='<option value="Frasco" id ="Frasco">Frasco</option>';
	str +='<option value="Bolsa" id ="Bolsa">Bolsa</option>';																												
	$('#selectnombrecontenedor').html(str);
	$('#selectnombrecontenedoredit').html(str);
	str = '';
	// CATEGORIA
	str +='<option value="Bebidas" id ="Bebidas">Bebidas</option>';
	str +='<option value="Insumos Basicos" id ="Insumos Básicos">Insumos Básicos</option>';
	str +='<option value="Insumos Carnicos" id ="Insumos Cárnicos">Insumos Cárnicos</option>';
	str +='<option value="Insumos no Carnicos" id ="Insumos no Cárnicos">Insumos no Cárnicos</option>';
	str +='<option value="Miscelanea" id ="Miscelanea">Miscelanea</option>';
	str +='<option value="Limpieza" id ="Limpieza">Limpieza</option>';		
	str +='<option value="Otros Alimentos" id ="Otros Alimentos">Otros Alimentos</option>';																													
	$('#selectcategoria').html(str);
	$('#selectcategoriaedit').html(str);
	str = '';
	// CONTROL UNIDAD
	str +='<option value="N" id ="N">N</option>';
	str +='<option value="S" id ="S">S</option>';																											
	$('#selectcontrolcantidad').html(str);
	$('#selectcontrolcantidadedit').html(str);
	str = '';
}


function pintarInsumos()
{
	$.getJSON(server + 'CRUDInsumo?idoperacion=6' , function(data1){
			table.clear().draw();
			for(var i = 0; i < data1.length;i++){
				table.row.add({
					"idinsumo": data1[i].idinsumo, 
					"nombreinsumo": data1[i].nombreinsumo,
					"unidadmedida": data1[i].unidadmedida, 
					"categoria": data1[i].categoria, 
					"costounidad": data1[i].costounidad, 
					"accion":'<button type="button" onclick="editarInsumo('+data1[i].idinsumo +')" class="btn btn-default btn-xs editButton" ' + 'data-id="' + data1[i].idinsumo + '" ><i class="fas fa-edit">Editar</i></button>'
				}).draw();
				//table.row.add(data1[i]).draw();
			}
		});
}

function editarInsumo(idInsumo)
{
	//
				
	// Get the record's ID via attribute
	
		$.ajax({ 
    				url: server + 'CRUDInsumo?idoperacion=4&idinsumo=' + idInsumo, 
    				dataType: 'json', 
    				async: false, 
    				success: function(data){ 
    						respuesta = data;
    						$("#idinsumoedit").val(respuesta.idinsumo);
    						$("#nombreinsumoedit").val(respuesta.nombreinsumo);
    						$("#selectunidadmedidaedit").val(respuesta.unidadmedida.trim());
    						$("#selectmanejacanastasedit").val(respuesta.manejacanastas.trim());
    						$("#cantidadcanastasedit").val(respuesta.cantidadcanasta);
    						$("#selectnombrecontenedoredit").val(respuesta.nombrecontenedor);
    						$("#selectcategoriaedit").val(respuesta.categoria);
    						$("#selectcontrolcantidadedit").val(respuesta.controlcantidad);
    						$("#costounidadedit").val(respuesta.costounidad);
    						if(respuesta.controltienda == 'S')
    						{
    							$('#controltienda').prop('checked', true);
    						}else
    						{
    							$('#controltienda').prop('checked', false);
    						}
				            // Show the dialog
				            bootbox
				                .dialog({
				                    title: 'Editar Insumo',
				                    message: $('#userForm'),
				                    show: false // We will show it manually later
				                })
				                .on('shown.bs.modal', function() {
				                    $('#userForm')
				                        .show()                             // Show the login form
				                        .bootstrapValidator('resetForm'); // Reset form
				                })
				                .on('hide.bs.modal', function(e) {
				                    // Bootbox will remove the modal (including the body which contains the login form)
				                    // after hiding the modal
				                    // Therefor, we need to backup the form
				                    $('#userForm').hide().appendTo('body');
				                })
				                .modal('show');
					} 
		});

		//
}

function confirmarEditarInsumo()
{
	
           
            var idInsumo = $('#idinsumoedit').val();
            var nombre = $('#nombreinsumoedit').val();
            var nombreEncode = encodeURIComponent(nombre);
			var unidadMedida = $('#selectunidadmedidaedit').val();
			var manejaCanastas = $('#selectmanejacanastasedit').val();
			var cantidadCanasta = $('#cantidadcanastasedit').val();
			var nombreContenedor = $('#selectnombrecontenedoredit').val();
			var categoria = $('#selectcategoriaedit').val();
			var categoriaEncode = encodeURIComponent(categoria);
			var controlCantidad = $('#selectcontrolcantidadedit').val();
			var costoUnidad = $('#costounidadedit').val();
			var controlTienda = "N";
			if($('#controltienda').is(':checked'))
            {
                controlTienda = "S";
            }
			// The url and method might be different in your application
            $.ajax({ 
    				url: server + 'CRUDInsumo?idoperacion=2&nombreinsumo=' + nombreEncode + "&unidadmedida=" + unidadMedida + "&manejacanastas=" + manejaCanastas + "&cantidadcanasta=" + cantidadCanasta + "&nombrecontenedor=" + nombreContenedor + "&categoria=" + categoriaEncode + "&controlcantidad=" + controlCantidad + "&costounidad=" + costoUnidad + "&idinsumo=" + idInsumo + "&controltienda=" + controlTienda , 
    				dataType: 'json', 
    				async: false, 
    				success: function(data){
    					pintarInsumos();
               			 // Hide the dialog
                		$('#userForm').parents('.bootbox').modal('hide');

                		// You can inform the user that the data is updated successfully
                		// by highlighting the row or showing a message box
                		bootbox.alert('El insumo ha sido actualizada');
    				} 
			}); 
            //
            

        

}
	

var server;
var tiendas;
var urlTienda ="";
var inventarios;
var bandera = false;
var dataTableDespacho;
//variable donde almacenaran los despachos que están activos en este momento para diferentes fines
var despachos;
//Variable donde se almacenará los valores cargados para modificar y con base en estos se hará la comparación de modificaciones.
var inventarios;
//Tendremos unas variables que se llenarán dando click en el DataTable
var estado;
var fechaSurtir;

// Se arma el valor de la variable global server, con base en la cual se realiza llamado a los servicios.
$(document).ready(function() {

	//Obtenemos el valor de la variable server
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	server = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));
	

    

	} );

//Se obtiene el listado de tiendas con el fin de seleccionar la tienda a surtir.


$(function(){
	//inicializamos el DataTable
	 dataTableDespacho = $('#grid-despachos').DataTable( {
    		"aoColumns": [
            { "mData": "iddespacho" },
            { "mData": "tienda" },
            { "mData": "idtienda" , "visible": false},
            { "mData": "fechareal" },
            { "mData": "fechasurtir" , "visible": false},
            { "mData": "estado" },
            { "mData": "observacion" },
            { "mData": "accion",
                className: "center",
                defaultContent: '<input type="button" class="btn btn-default btn-xs" onclick="consultarDespacho()" value="Consultar"></button>' }
        ]
    	} );


	getListaTiendas();

	//Manejos acción para click sobre el DataTable
	$('#grid-despachos tbody').on('click', 'tr', function () {
        //cerramos la notificacion en caso de que esté abierta
        datos = dataTableDespacho.row( this ).data();
        fechaSurtir = datos.fechasurtir;
        estado = datos.estado;
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



function imprimirDivInventario(nombreDiv)
{
	var contenido= document.getElementById(nombreDiv).innerHTML;
    var contenidoOriginal= document.body.innerHTML;
    document.body.innerHTML = contenido;
    window.print();
    document.body.innerHTML = contenidoOriginal;
}


//Método que invoca el servicio para obet
function getListaTiendas(){
	$.getJSON(server + 'GetTiendas', function(data){
		tiendas = data;
		var str = '';
		//Agregamos la opción de todos
		str +='<option value="0" id ="0">TODAS</option>';
		for(var i = 0; i < data.length;i++){
			var cadaTienda  = data[i];
			str +='<option value="'+ cadaTienda.nombre +'" id ="'+ cadaTienda.id +'">' + cadaTienda.nombre +'</option>';
		}
		$('#selectTiendas').html(str);
	});
}

//Metodo para validar los datos
function validarDatos() 
{

	var fechaini = $("#fechasurtirdesde").val();
	var fechafin = $("#fechasurtirhasta").val();
	if(fechaini == '' || fechaini == null)
	{
		alert ('La fecha inicial debe ser diferente a vacía');
		return(false);
	}

	if(fechafin == '' || fechafin == null)
	{
		alert ('La fecha final debe ser diferente a vacía');
		return(false);
	}
	if(existeFecha(fechaini))
	{
	}
	else
	{
		alert ('La fecha desde no es correcta');
		return(false);
	}

	if(existeFecha(fechafin))
	{
	}
	else
	{
		alert ('La fecha hasta no es correcta');
		return(false);
	}
	if(validarFechaMenorActual(fechaini, fechafin))
	{
	}
	else
	{
		alert ('La fecha inicial es mayor a la fecha final, favor corregir');
		return(false);
	}
	//Incluimos validación de cantidad de días de la consulta
	if(validarDiferenciaFechas(fechaini, fechafin))
	{
	}
	else
	{
		alert ('La diferencia entre la fecha Inicial y Final no puede ser mayor a 60 días');
		return(false);
	}
	return(true);
}

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

function validarDiferenciaFechas(date1, date2){
      var fechaini = new Date();
      var fechafin = new Date();
      var fecha1 = date1.split("/");
      var fecha2 = date2.split("/");
      fechaini.setFullYear(fecha1[2],fecha1[1]-1,fecha1[0]);
      fechafin.setFullYear(fecha2[2],fecha2[1]-1,fecha2[0]);
      var diferencia = fechafin - fechaini;
      var dias = diferencia/(1000*60*60*24);
      if(dias > 60)
      {
      	return(false);
      }else
      {
      	return(true);
      }
}


//Método principal invocado para validar los datos de los parámetros y adicionalmente ejecutar los servicios para 
//recuperar la informacion y pintarla en pantalla.
function consultarDespachos() 
{
	var resultado = validarDatos();
	if(!resultado)
	{
		return;
	}
	var fechasurtirDesde = $("#fechasurtirdesde").val();
	var fechasurtirHasta = $("#fechasurtirhasta").val();
	//Tenemos que considerar cuando se seleccionen todas las fechas
	var tienda = $("#selectTiendas").val();
	var idtienda = $("#selectTiendas option:selected").attr('id');
	
	
	if (tienda == '' || tienda == null)
	{

		alert ('La tienda no puede estar vacía');
		return;
	}
	// Si pasa a este punto es porque paso las validaciones
	
	$.ajax({ 
	    		url: server + 'ConsultarDespachos?fechasurtirdesde=' + fechasurtirDesde + "&idtienda=" + idtienda + "&fechasurtirhasta=" + fechasurtirHasta, 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				
	    			despachos = data1;
	    			var despacho;
					var strInv='';
					dataTableDespacho.clear().draw();
					for (var i = 0; i < despachos.length; i++)
					{
						despacho = despachos[i];
						dataTableDespacho.row.add({
										"iddespacho": despacho.iddespacho,
										"tienda": despacho.tienda,
										"idtienda": despacho.idtienda,
								        "fechareal": despacho.fechareal,
								        "fechasurtir": despacho.fechasurtir,
								        "estado":   despacho.estado,
								        "observacion": despacho.observacion,
								        "accion": "<button type='button' class='btn btn-danger btn-xs' onclick='modificarDespacho(" + despacho.iddespacho + ' , ' + despacho.idtienda + ")'><i class='fas fa-copy fa-2x'></i></button>"
								    	}).draw();
					
					} 
					
				} 
			});
	 
	
}

function generarExcel()
{
	$("#inventariosurtir").table2excel({
	    filename: "InventarioSurtir"
	  });

}

function reiniciarInventario()
{
	inventarios = '';
	//Tendremos unas variables que se llenarán dando click en el DataTable
	estado = '';
	fechaSurtir = '';
	var str = '';
	$('#modificarDespacho').html(str);
}

function modificarDespacho(idDespacho, idTienda)
{
	//Validaremos el estado del despacho
	$.ajax({ 
	url: server + 'RetornarEstadoDespacho?iddespacho=' + idDespacho , 
	dataType: 'json', 
	async: false, 
	success: function(data2)
		{
			estado = data2.estado;
		}
	});
	if(estado == 'PREINGRESADO')
	{
		$.alert('Si el despacho ya fue preingresado, se modificará la información en bodega pero en tienda, se deberá realizar la novedad, pues el inventario ya fue replicado');
	}else if(estado == 'DESPACHADO')
	{
		$.alert('El despacho está en estado DESPACHADO, por lo tanto la modificación realizada podría ser incluida antes de ser enviada a la tienda');
	}
	//Con el idDespacho invocamos servicio para traernos y conformar la info  de los despachos.
	$.ajax({ 
	    		url: server + 'ObtenerDespachoModificar?iddespacho=' + idDespacho , 
	    		dataType: 'json', 
	    		async: false, 
	    		success: function(data1){ 
	    				inventarios = data1;
	    				bandera = true;
	    				var inventario;
						
						var strInv='';
						
						strInv += '<table id="inventariosurtir" class="table table-bordered">';
						strInv += '<thead><th COLSPAN="2"><img src="LogoAmericana.png" class="img-circle" /></th>';
						strInv += '<th COLSPAN="4"> <h2>PRODUCTOS A LLEVAR TIENDA </h2></th>';
						strInv += '<tr><th>Nom/Insumo</th><th>Cant/Ing Ini</th><th>Cant Modificar</th><th>Empaque</th><th>Unidad</th></tr></thead>';
				        strInv += '<tbody>';
						for (var i = 0; i < inventarios.length; i++)
						{
							inventario = inventarios[i];
							strInv +='<tr> ';
							strInv +='<td> ';
							strInv +='<label>' + inventario.nombreinsumo + '</label> </td>';
							strInv +='<td> ';
							strInv +='<label>' + inventario.cantidadllevar + '</label> </td>';
							strInv +='<td> ';
							strInv += '<input type="text" name="' + inventario.cantidadcanastas + '"" value="' + inventario.cantidadllevar +'" id="'+ "cant" + inventario.idinsumo +'" maxlength="10" size="10" onchange="modificarContenedor(this)"> </td>';
							//if(inventario.cantidadcanastas > 0)
							if(inventario.cantidadxcanasta > 0)
							{
								strInv +='<td><input type="text" value="' + inventario.cantidadcanastas + inventario.nombrecontenedor +'" id="'+ "cont" + inventario.idinsumo +'" maxlength="10" size="10" readonly></td>';
							}
							else
							{
								strInv +='<td></td>';
							}

							strInv +='<td><label>' + inventario.unidadmedida + '</label> </td>';
							strInv +='</tr> ';
						}
						strInv +='<tr><td COLSPAN="6">';
						strInv +='<label for="comment">Observacion:</label>';
						strInv +='<textarea class="form-control" rows="3" id="observacion"></textarea>';
						strInv +='</td></tr>';
						strInv +='<tr><td COLSPAN="2"><input type="button" class="btn btn-primary btn-sd" value="Confirmar Modificación Inventario" onclick="confirmarModInventario(' + idDespacho + ' , ' + idTienda +')"> </td>';
						strInv +='</tr>';
						strInv +='<tr><td COLSPAN="2"><input type="button" class="btn btn-primary btn-sd" value="Confirmar Despacho" onclick="confirmarDespacho(' + idDespacho + ' , ' + idTienda +')"> </td>';
						strInv +='</tr>';
						strInv +='</tbody> ';
						$('#modificarDespacho').html(strInv);
					
				} 
			});

	$('#modDespacho').modal('show');
}


//Opción que implementará la lógica para la confirmación de la modificación del inventario
function confirmarDespacho(idDespacho, idTienda)
{
	$.confirm({
				'title'		: 'Confirmacion Final despacho',
				'content'	: 'Desea confirmar el envío a la tienda del despacho ' +  idDespacho,
				'type': 'dark',
   				'typeAnimated': true,
				'buttons'	: {
					'Si'	: {
						'class'	: 'blue',
						'action': function()
						{	
							$.ajax({ 
							    		url: server + 'ActualizarEstadoDespacho?iddespacho=' + idDespacho, 
							    		dataType: 'json', 
							    		async: false, 
							    		success: function(data2){
							    			//Realizamos el proceso para despachar para la tienda bodega
			    							$.getJSON(server + 'InsertarRetiroInventarioBodega?iddespacho=' + idDespacho, function(data3){
			    							});
								   			resultado = data2[0];
								   			if(resultado.respuesta == 'ejecutado')
								   			{
								   				$.alert('Se ha despachado correctamente:  ' + idDespacho);
								    			reiniciarInventario();
								   			}
								   		}
								   	});
						}
					},
						'No'	: {
							'class'	: 'gray',
							'action': function(){}	// Nothing to do in this case. You can as well omit the action property.
						}
					}
				});
}

//Opción que implementará la lógica para la confirmación de la modificación del inventario
function confirmarModInventario(idDespacho, idTienda)
{
	
	var mensajeError = validarCantidades()
	if(mensajeError.length == 0)
	{

	}else
	{
		$.alert("Se tienen valores incorrectos en los insumos : " + mensajeError);
		return;
	}
	

	var observacion = encodeURIComponent($("#observacion").val().substring(0,500));
	// Variable que almacenará si se tuvieron o no errores en el proceso
	var huboErroresDetalle = false;
	//Variable donde se controlará si efectivamente si hubo o no cambios
	var siHuboCambio = false;
	var insumosErorres = "";
	$.confirm({
				'title'		: 'Confirmacion de Modificación',
				'content'	: 'Desea modificar realmente el despacho número ' +  idDespacho,
				'type': 'dark',
   				'typeAnimated': true,
				'buttons'	: {
					'Si'	: {
						'class'	: 'blue',
						'action': function()
						{

								for (var i = 0; i < inventarios.length; i++)
								{
									var inventario = inventarios[i];
									if($("#cant"+inventario.idinsumo).val() >= 0)
									{
										var cantidad = $("#cant"+inventario.idinsumo).val();
										var cantidadInicial = inventario.cantidadllevar;
										if(cantidad != cantidadInicial)
										{
											var contenedor = $("#cont"+inventario.idinsumo).val();
											if (contenedor == undefined || contenedor == null)
											{
												contenedor = "";
											}
											var idinsumo = inventario.idinsumo;
											$.ajax({ 
									    		url: server + 'ActualizarDetalleDespachoTienda?iddespacho=' + idDespacho + "&idinsumo=" + idinsumo + "&cantidad=" + cantidad + "&contenedor=" + contenedor, 
									    		dataType: 'json', 
									    		async: false, 
									    		success: function(data2){
									    			siHuboCambio = true;
										   			detalle = data2[0];
										   			if(detalle.iddespachodetalle == 0)
										   			{
										   				insumosErorres = insumosErrores + " " + inventario.nombreinsumo;
										   				huboErroresDetalle = true;
										   			}
										   		}
										   	});
										}
									}
								}
								if(huboErroresDetalle)
								{
									$.alert('No se insertó de manera correcta los siguientes insumos ' + insumosErrores);
								}else
								{
									if(siHuboCambio)
									{
										
										$.alert('Se ha actualizado correctamente el Inventario para La Tienda, con el despacho Número ' + idDespacho);
										//Realiza el llamado asíncrono a la generación del archivo.
										$.getJSON(server + 'GenerarArchivoDespachoTienda?idtienda=' + idTienda + '&iddespacho=' + idDespacho +  '&fecha=' + fechaSurtir , function(data2){
						    			});
						    			reiniciarInventario();
			    					}
			    					else
			    					{
			    						$.alert('NO SE TUVIERON MODIFICACIONES con el despacho Número ' + idDespacho);
			    						reiniciarInventario();
			    					}
								    					
		
								}
					}
				},
					'No'	: {
						'class'	: 'gray',
						'action': function(){}	// Nothing to do in this case. You can as well omit the action property.
					}
				}
			});
	
}

//Función para validar las cantidades a enviar antes de realizar la inserción consumiendo los servicios.
function validarCantidades()
{
	var mensajeError = '';
	for (var i = 0; i < inventarios.length; i++)
	{
		var inventario = inventarios[i];
		var valorCampo = $("#cant"+inventario.idinsumo).val();
		if(valorCampo != "0")
		{
			if(!validarSiNumero(valorCampo))
			{
				mensajeError = mensajeError + ' ' +  inventario.nombreinsumo;
			}
			
		}
	}
	return(mensajeError);	
}

function modificarContenedor(objeto)
{
	var idInsumoModificado = $(objeto).attr('id');
	var valorModificado = $(objeto).val();
	idInsumoModificado = idInsumoModificado.substring(4, idInsumoModificado.length );
	for(var i = 0; i < inventarios.length;i++)
	{
		if(inventarios[i].idinsumo == idInsumoModificado)
		{
			if(inventarios[i].manejacanastas == 'S')
			{
				if($.isNumeric(valorModificado))
				{
					var canastas = Math.floor(valorModificado/inventarios[i].cantidadxcanasta);
					var residuo = valorModificado % inventarios[i].cantidadxcanasta;
					if(residuo > 0 )
					{
						canastas++;
						valorModificado =  inventarios[i].cantidadxcanasta *  canastas;
					}
					$(objeto).val(valorModificado);
					$("#cont"+idInsumoModificado).val(canastas+inventarios[i].nombrecontenedor);
				}
				else
				{
					alert("El valor modificado no es númerico");
				}
			}
		}
	}
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
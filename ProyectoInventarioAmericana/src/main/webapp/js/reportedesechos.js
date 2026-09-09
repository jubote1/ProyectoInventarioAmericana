/*
 * Reporte de desechos y devoluciones.
 *
 * Arma en pantalla la misma matriz del libro que se llevaba a mano: un bloque
 * por mes, una fila por tienda y una columna por categoria de producto. La
 * diferencia es que sale de la base de datos, asi que no se desactualiza.
 *
 * Las categorias vienen del servicio y no van escritas aqui: si manana se
 * agrega un tipo de desecho nuevo, aparece solo.
 */

var categoriasReporte = [];
var filasReporte = [];
var tablaDetalle = null;
var tiendasVistas = {};

function inicializarReporte()
{
    //El año se llena con los años que de verdad tienen registros. La primera
    //consulta se hace con el año en curso.
    var anioActual = new Date().getFullYear();
    $('#anio').append('<option value="' + anioActual + '">' + anioActual + '</option>');

    $('#vista').on('change', function () {
        //Cambiar entre cantidades y costos no necesita volver al servidor.
        pintarMatriz();
    });

    consultarReporte();
}

function consultarReporte()
{
    var anio = $('#anio').val() || new Date().getFullYear();
    var mes = $('#mes').val();
    var idtienda = $('#tienda').val();

    $('#btnconsultar').prop('disabled', true).val('Consultando...');
    $('#cargando').show();
    $('#matriz').empty();
    $('#mensajeVacio').hide();
    $('#panelTotales').hide();
    $('#panelDetalle').hide();

    $.getJSON(server + 'ConsultarReporteDesechos?anio=' + encodeURIComponent(anio)
            + '&mes=' + encodeURIComponent(mes)
            + '&idtienda=' + encodeURIComponent(idtienda), function (datos) {

        $('#btnconsultar').prop('disabled', false).val('Consultar');
        $('#cargando').hide();

        if (datos.respuesta !== 'OK')
        {
            $.alert('No se pudo consultar el reporte.<br><br>' + escaparTexto(datos.detalle || ''));
            return;
        }

        llenarAnios(datos.anios, anio);
        categoriasReporte = datos.categorias || [];
        filasReporte = datos.filas || [];

        llenarTiendas(filasReporte);
        pintarTotales(filasReporte, datos.origenes || []);
        pintarMatriz();
        consultarDetalle(anio, mes, idtienda);

    }).fail(function () {
        $('#btnconsultar').prop('disabled', false).val('Consultar');
        $('#cargando').hide();
        $.alert('No hubo respuesta del servidor. Si acaba de iniciar sesion, recargue la pagina.');
    });
}

function llenarAnios(anios, seleccionado)
{
    if (!anios || anios.length === 0) { return; }
    //Se reconstruye solo si cambio, para no perder la seleccion del usuario.
    if ($('#anio option').length === anios.length) { return; }
    var html = '';
    for (var i = 0; i < anios.length; i++)
    {
        html += '<option value="' + anios[i] + '"' + (String(anios[i]) === String(seleccionado) ? ' selected' : '')
             + '>' + anios[i] + '</option>';
    }
    $('#anio').html(html);
}

function llenarTiendas(filas)
{
    //La lista de tiendas se saca de los datos del año. Una tienda sin desechos
    //no aparece, y eso esta bien: no habria nada que mostrarle.
    var nuevas = {};
    for (var i = 0; i < filas.length; i++)
    {
        if (filas[i].tienda) { nuevas[filas[i].tienda] = true; }
    }
    var nombres = Object.keys(nuevas).sort();
    var iguales = (nombres.length === Object.keys(tiendasVistas).length);
    if (iguales) { return; }
    tiendasVistas = nuevas;

    //El filtro por tienda usa el nombre, porque es lo que trae la matriz. El
    //servicio recibe idtienda, asi que aqui se deja en 0 y el filtrado del
    //nombre se hace en pantalla.
    var html = '<option value="0" selected>Todas las tiendas</option>';
    for (var j = 0; j < nombres.length; j++)
    {
        html += '<option value="nombre:' + escaparAtributo(nombres[j]) + '">' + escaparTexto(nombres[j])
             + '</option>';
    }
    $('#tienda').html(html);
}

function tiendaFiltrada()
{
    var valor = $('#tienda').val() || '0';
    if (valor.indexOf('nombre:') === 0) { return (valor.substring(7)); }
    return ('');
}

function pintarTotales(filas, origenes)
{
    var costo = 0;
    var nombreTienda = tiendaFiltrada();
    for (var i = 0; i < filas.length; i++)
    {
        if (nombreTienda !== '' && filas[i].tienda !== nombreTienda) { continue; }
        costo += Number(filas[i].totalcosto);
    }

    var puntoVenta = 0;
    var plataformas = 0;
    var registros = 0;
    for (var j = 0; j < origenes.length; j++)
    {
        registros += Number(origenes[j].registros);
        if (origenes[j].origen === 'PUNTO DE VENTA') { puntoVenta += Number(origenes[j].costo); }
        else { plataformas += Number(origenes[j].costo); }
    }

    $('#totCosto').text(pesos(costo));
    $('#totRegistros').text(entero(registros));
    $('#totPuntoVenta').text(pesos(puntoVenta));
    $('#totPlataformas').text(pesos(plataformas));

    //Los totales por origen vienen del año completo y no se filtran por tienda,
    //asi que solo se muestran cuando no hay tienda seleccionada.
    $('#totPuntoVenta').closest('.col-md-3').toggle(nombreTienda === '');
    $('#totPlataformas').closest('.col-md-3').toggle(nombreTienda === '');
    $('#panelTotales').show();
}

function pintarMatriz()
{
    if (filasReporte.length === 0)
    {
        $('#matriz').empty();
        $('#mensajeVacio').show();
        return;
    }
    $('#mensajeVacio').hide();

    var verCostos = ($('#vista').val() === 'costos');
    var nombreTienda = tiendaFiltrada();

    //Agrupar por mes, respetando el orden en que vienen
    var meses = [];
    var porMes = {};
    for (var i = 0; i < filasReporte.length; i++)
    {
        var fila = filasReporte[i];
        if (nombreTienda !== '' && fila.tienda !== nombreTienda) { continue; }
        if (!porMes[fila.mes])
        {
            porMes[fila.mes] = { nombre: fila.nombremes, filas: [] };
            meses.push(fila.mes);
        }
        porMes[fila.mes].filas.push(fila);
    }

    if (meses.length === 0)
    {
        $('#matriz').empty();
        $('#mensajeVacio').show();
        return;
    }

    var html = '';
    for (var m = 0; m < meses.length; m++)
    {
        var bloque = porMes[meses[m]];
        html += '<div class="bloque-mes">';
        html += '<div class="titulo-mes">' + escaparTexto(bloque.nombre)
             + (verCostos ? ' &middot; costos' : ' &middot; cantidades') + '</div>';
        html += '<div class="contenedor-tabla"><table class="matriz">';

        html += '<thead><tr><th class="tienda-col">Punto de venta</th>';
        for (var c = 0; c < categoriasReporte.length; c++)
        {
            html += '<th>' + escaparTexto(categoriasReporte[c]) + '</th>';
        }
        html += '<th>Total unid.</th><th>Costo total</th></tr></thead><tbody>';

        var totales = [];
        for (var t = 0; t < categoriasReporte.length; t++) { totales.push(0); }
        var totalUnidades = 0;
        var totalCosto = 0;

        for (var f = 0; f < bloque.filas.length; f++)
        {
            var reg = bloque.filas[f];
            html += '<tr><td class="tienda">' + escaparTexto(reg.tienda) + '</td>';
            for (var k = 0; k < categoriasReporte.length; k++)
            {
                var cat = categoriasReporte[k];
                var unidades = Number(reg.unidades[cat] || 0);
                var costoCat = Number(reg.costos[cat] || 0);
                var valor = verCostos ? costoCat : unidades;
                html += '<td class="' + (valor === 0 ? 'cero' : '') + '">'
                     + (valor === 0 ? '-' : (verCostos ? pesos(valor) : numero(valor))) + '</td>';
                totales[k] += valor;
            }
            html += '<td class="col-costo">' + numero(Number(reg.totalunidades)) + '</td>';
            html += '<td class="col-costo">' + pesos(Number(reg.totalcosto)) + '</td></tr>';
            totalUnidades += Number(reg.totalunidades);
            totalCosto += Number(reg.totalcosto);
        }

        html += '<tr class="fila-total"><td class="tienda">TOTAL MES</td>';
        for (var z = 0; z < categoriasReporte.length; z++)
        {
            html += '<td>' + (totales[z] === 0 ? '-' : (verCostos ? pesos(totales[z]) : numero(totales[z])))
                 + '</td>';
        }
        html += '<td>' + numero(totalUnidades) + '</td><td>' + pesos(totalCosto) + '</td></tr>';
        html += '</tbody></table></div></div>';
    }

    $('#matriz').html(html);
}

function consultarDetalle(anio, mes, idtienda)
{
    var desde = anio + '-' + (Number(mes) > 0 ? dosDigitos(mes) : '01') + '-01';
    var hasta = anio + '-' + (Number(mes) > 0 ? dosDigitos(mes) : '12') + '-31';

    $.getJSON(server + 'ConsultarDetalleDesechos?fechadesde=' + desde + '&fechahasta=' + hasta
            + '&idtienda=' + encodeURIComponent(idtienda), function (datos) {

        var lineas = datos.lineas || [];
        var nombreTienda = tiendaFiltrada();

        //DataTables no admite reemplazar el tbody por debajo: hay que destruirla.
        if (tablaDetalle !== null)
        {
            tablaDetalle.destroy();
            tablaDetalle = null;
        }
        $('#grid-detalle tbody').empty();

        if (lineas.length === 0)
        {
            $('#panelDetalle').hide();
            return;
        }

        var filas = '';
        for (var i = 0; i < lineas.length; i++)
        {
            var l = lineas[i];
            if (nombreTienda !== '' && l.tienda !== nombreTienda) { continue; }
            var claseDestino = (l.destino === 'APROVECHABLE') ? 'et-aprov' : 'et-noaprov';
            var claseOrigen = 'et-pv';
            if (l.origen === 'DIDI') { claseOrigen = 'et-didi'; }
            else if (l.origen === 'PLATAFORMA') { claseOrigen = 'et-plataforma'; }

            filas += '<tr>'
                + '<td>' + semanaTexto(l.semana) + '</td>'
                + '<td>' + escaparTexto(l.fecha) + '</td>'
                + '<td>' + l.id + '</td>'
                + '<td>' + escaparTexto(l.tienda) + '</td>'
                + '<td>' + escaparTexto(l.producto) + '</td>'
                + '<td>' + escaparTexto(l.categoria) + '</td>'
                + '<td><span class="etiqueta ' + claseOrigen + '">' + escaparTexto(l.origen) + '</span></td>'
                + '<td><span class="etiqueta ' + claseDestino + '">' + escaparTexto(l.destino) + '</span></td>'
                + '<td>' + escaparTexto(l.motivo) + '</td>'
                + '<td style="text-align:right;">' + (l.tipo === 'G' ? numero(Number(l.gramos)) : '') + '</td>'
                + '<td style="text-align:right;">' + (l.tipo === 'G' ? '' : numero(Number(l.cantidad))) + '</td>'
                + '<td style="text-align:right;">' + pesos(Number(l.costo)) + '</td>'
                + '<td>' + escaparTexto(l.usuario) + '</td>'
                + '<td>' + escaparTexto(l.estado) + '</td>'
                + '<td>' + escaparTexto(l.fechacarro) + '</td>'
                + '<td>' + escaparTexto(l.fechabodega) + '</td>'
                + '</tr>';
        }

        if (filas === '')
        {
            $('#panelDetalle').hide();
            return;
        }

        $('#grid-detalle tbody').html(filas);
        $('#panelDetalle').show();

        tablaDetalle = $('#grid-detalle').DataTable({
            "order": [],
            "pageLength": 25,
            "language": {
                "emptyTable": "Sin desechos",
                "info": "Mostrando _START_ a _END_ de _TOTAL_ registros",
                "infoEmpty": "Sin registros",
                "infoFiltered": "(filtrado de _MAX_)",
                "lengthMenu": "Ver _MENU_ registros",
                "search": "Buscar:",
                "zeroRecords": "No hay coincidencias",
                "paginate": { "first": "Primero", "last": "Ultimo", "next": "Siguiente", "previous": "Anterior" }
            }
        });

    }).fail(function () {
        $('#panelDetalle').hide();
    });
}

function descargarExcel()
{
    var anio = $('#anio').val() || new Date().getFullYear();
    var mes = $('#mes').val();
    //La descarga va por navegacion directa y no por AJAX: el servlet responde
    //con el archivo, y asi el navegador se encarga de guardarlo.
    window.location.href = server + 'DescargarReporteDesechos?anio=' + encodeURIComponent(anio)
        + '&mes=' + encodeURIComponent(mes) + '&idtienda=0';
}

function semanaTexto(semana)
{
    switch (Number(semana))
    {
        case 1: return ('1RA');
        case 2: return ('2DA');
        case 3: return ('3RA');
        case 4: return ('4TA');
        default: return ('5TA');
    }
}

function dosDigitos(valor)
{
    return (Number(valor) < 10 ? '0' + Number(valor) : String(Number(valor)));
}

function pesos(valor)
{
    var n = Number(valor);
    if (isNaN(n)) { return ('$ 0'); }
    return ('$ ' + Math.round(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function numero(valor)
{
    var n = Number(valor);
    if (isNaN(n)) { return ('0'); }
    //Los gramos pueden traer decimales; las unidades no. Se muestran solo cuando
    //de verdad los hay.
    var texto = (n % 1 === 0) ? String(n) : n.toFixed(1);
    return (texto.replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function entero(valor)
{
    return (Math.round(Number(valor) || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.'));
}

function escaparTexto(texto)
{
    //El motivo y el usuario los escribe la gente en la tienda, y se pintan
    //dentro del HTML de la tabla.
    return (String(texto === null || texto === undefined ? '' : texto)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;'));
}

function escaparAtributo(texto)
{
    return (escaparTexto(texto).replace(/"/g, '&quot;'));
}

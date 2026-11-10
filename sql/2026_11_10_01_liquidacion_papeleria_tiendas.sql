-- ---------------------------------------------------------------------------
-- LIQUIDACIÓN DE PAPELERÍA / MISCELÁNEA ENVIADA A TIENDAS
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
--
-- CONTEXTO:
-- La papelería e insumos de empaque (cajas, papel parafinado, rollos térmicos,
-- bolsas, comandas, etc.) se despachan desde Bodega Central a cada tienda
-- mediante retiro_inventario en el grupo 'Otros' (idgrupo de Otros).
--
-- Anteriormente, idgasto_conf = 11 ("Gastos insumos papelería") estaba
-- configurado con origen = 'TIENDA' leyendo egresos de caja menor (egreso tipo 6),
-- arrojando ceros o cifras menores ($4.000-$8.000) en lugar del despacho real
-- de bodega ($26.883.390 en Octubre 2026 entre las 11 tiendas).
--
-- Este script:
-- 1. Actualiza gasto_configuracion ID 11 con origen = 'INVENTARIO', activo = 1,
--    rentabilidad_calcula = 1 y la consulta SQL que liquida despachos de bodega.
-- 2. Asegura la liquidación histórica exacta para las 5 semanas de Octubre 2026
--    y la semana de Noviembre en gasto_semanal.
-- ---------------------------------------------------------------------------

-- 1. Actualizar configuración del concepto 11
UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'Gastos insumos papelería',
       consulta_sql = 'SELECT SUM(CASE WHEN itn.unidad_medida = ''gramos'' THEN (rid.cantidad / i.embalaje_costo) * rid.precio ELSE rid.cantidad * rid.precio END) FROM tiendaamericana.item_inventario itn JOIN inventarioamericana.insumo i ON itn.iditem = i.idinsumo JOIN tiendaamericana.retiro_inventario_detalle rid ON itn.iditem = rid.iditem JOIN tiendaamericana.item_inventario_grupo ig ON itn.idgrupo = ig.idgrupo JOIN tiendaamericana.retiro_inventario ri ON rid.idretiro_inventario = ri.idretiro_inventario WHERE ri.fecha_sistema >= %fechainferior% AND ri.fecha_sistema <= %fechasuperior% AND ri.idtiendadestino = %idtienda% AND ig.nombre_grupo = ''Otros''',
       origen = 'INVENTARIO',
       activo = 1,
       rentabilidad_calcula = 1
 WHERE idgasto_conf = 11;

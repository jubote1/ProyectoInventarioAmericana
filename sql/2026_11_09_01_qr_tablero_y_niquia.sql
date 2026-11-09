-- ---------------------------------------------------------------------------
-- EL QR EN EL TABLERO DE GERENCIA, Y LA SEMANA QUE NIQUIA NO RESPONDIO
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente. No borra nada.
--
-- QUE SE REVISO (2026-11-09)
--
-- No se perdio ninguna fila de gasto_configuracion: las 36 siguen ahi, y el
-- concepto 25 "Comision QR Bancolombia" tambien. El dato del QR TAMPOCO dejo
-- de guardarse: ReporteConciliacionWompi (Servicios) lo escribe cada semana en
-- gasto_semanal, con la venta en valor_calculo y la comision en valor_gasto, y
-- esta completo hasta el 2026-11-08.
--
-- Lo que fallaba eran dos cosas distintas:
--
-- 1. EL TABLERO DE GERENCIA NO LO NOMBRABA. La lista de comisiones de
--    GerenciaRentabilidadDAO era "16,17,18,20": Virtual, PAYU, Rappi y DiDi.
--    El 25 no estaba, asi que el QR no salia. Eso se arregla en el codigo, no
--    aqui. El reporte de costo por tienda del central SI lo contaba
--    (pizzaamericana.concepto_costo_gasto_semanal), de modo que los dos
--    reportes daban distinto sobre los mismos datos.
--
-- 2. NIQUIA QUEDO EN CERO la semana del 2 al 8 de noviembre, en los conceptos
--    24 y 25. La tienda no respondio cuando corrio el proceso y el valor se
--    guardo como 0 en vez de quedar como "sin respuesta". Es lo que corrige
--    este script.
--
-- DE DONDE SALEN LOS VALORES
--
-- De la base de Niquia (172.19.12.19), con las MISMAS consultas del proceso y
-- la misma ventana, lunes 2 a domingo 8:
--
--   24  PedidoDAO.obtenerTotalesPedidosSemanaTarjeta -> forma_pago con
--       tipoformapago='TARJETA', sin anulados  ........  $3.205.690
--   25  TiendaDAO.obtenerTotalFormaPagoEntreFechas -> idforma_pago 7
--       (SONO QR, el parametro IDQRBANCOLOMBIA de la tienda)  $1.308.890
--       comision 2% (parametro QRBANCOLOMBIA)  ..........  $26.177,80
--
-- El metodo se valido primero contra San Antonio, donde el proceso si corrio:
-- la consulta da 6.079.630 y lo guardado es 6.079.630, al peso.
--
-- El 24 lleva el mismo valor en los dos campos porque asi lo guarda el proceso
-- (ahi valor_gasto es la VENTA con datafono, no la comision; la comision es el
-- 5% de porcentaje_gasto de su fila).
--
-- Reversa: sql/respaldos/devolver_niquia_qr_2026-11-08.sql
-- ---------------------------------------------------------------------------

UPDATE inventarioamericana.gasto_semanal
   SET valor_calculo = 3205690, valor_gasto = 3205690
 WHERE idgasto_conf = 24 AND idtienda = 13 AND fecha = '2026-11-08'
   AND valor_calculo = 0;

UPDATE inventarioamericana.gasto_semanal
   SET valor_calculo = 1308890, valor_gasto = 26177.8
 WHERE idgasto_conf = 25 AND idtienda = 13 AND fecha = '2026-11-08'
   AND valor_calculo = 0;

-- Como quedo.
SELECT idgasto_conf, idtienda, fecha, valor_calculo, valor_gasto
  FROM inventarioamericana.gasto_semanal
 WHERE idtienda = 13 AND fecha = '2026-11-08' AND idgasto_conf IN (24, 25);

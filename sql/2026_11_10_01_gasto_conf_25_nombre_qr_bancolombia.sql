-- ---------------------------------------------------------------------------
-- AJUSTE DE NOMBRE EN CONCEPTO 25: QR BANCOLOMBIA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente.
--
-- Restaura el nombre a 'QR Bancolombia' para que no empiece por 'Comision '
-- y sea procesado limpiamente por el reporte semanal de gastos existente.
-- ---------------------------------------------------------------------------

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'QR Bancolombia'
 WHERE idgasto_conf = 25;

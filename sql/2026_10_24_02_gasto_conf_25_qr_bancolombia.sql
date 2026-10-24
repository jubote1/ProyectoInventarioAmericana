-- ---------------------------------------------------------------------------
-- EL CONCEPTO 25 NO ES UNA PROMO DE DICIEMBRE: ES EL QR DE BANCOLOMBIA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente. Solo cambia el nombre; no toca ningun dato historico.
--
-- QUE PASA
--
-- gasto_configuracion 25 se llama "NO USAR - XL Promo Deditos Santa", un nombre
-- de una promocion vieja. Pero el concepto esta VIVO: ReporteConciliacionWompi
-- le escribe cada semana la comision del QR de Bancolombia, con la tarifa del
-- parametro QRBANCOLOMBIA, en gasto_semanal.
--
--     GastoSemanal(0, idTienda, 25, fecha, ventaTotalQR, totalComisionQR)
--
-- O sea que un concepto que dice NO USAR es una de las cinco comisiones que
-- componen la comision virtual de una tienda. En Manrique, septiembre de 2026,
-- puso $144.576.
--
-- POR QUE IMPORTA AHORA
--
-- La pantalla de costo por tienda del central desglosa la comision virtual en
-- sus cinco fuentes, y una de ellas saldria en pantalla como "NO USAR - XL
-- Promo Deditos Santa". Quien lea eso va a creer que hay un error, o peor, va a
-- pedir que lo quiten.
-- ---------------------------------------------------------------------------

UPDATE gasto_configuracion
   SET nombre_gasto = 'Comision QR Bancolombia'
 WHERE idgasto_conf = 25
   AND nombre_gasto LIKE 'NO USAR - XL Promo Deditos%';

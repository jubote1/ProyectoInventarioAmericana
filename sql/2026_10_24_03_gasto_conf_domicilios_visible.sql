-- ---------------------------------------------------------------------------
-- DEVOLVERLE AL REPORTE EL CONTEO DE DOMICILIOS
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente. No toca gasto_semanal ni ningun dato historico.
--
-- QUE PASO, CONTADO COMPLETO
--
-- Hay un reporte, fuera de estos cuatro repositorios, que lee
-- gasto_configuracion y filtra por activo = 1. Dejo de mostrar dos cosas:
-- la comision del QR y el conteo de domicilios. Las dos salen de aqui.
--
--   2026-09-29  Para marcar cuales conceptos calcula el tablero de
--               rentabilidad, use la columna activo, que ya tenia duenio:
--               puse en 1 los quince que tienen consulta y en 0 los veinte
--               que no. Eso apago las comisiones (16, 17, 18, 20, 24, 25) y
--               prendio el conteo de domicilios (1).
--   2026-10-08  Lo devolvi con la regla activo = IF(consulta_sql='NA',1,0).
--               Las comisiones volvieron. El conteo de domicilios volvio a 0
--               y ahi se quedo.
--
-- O sea que el conteo aparecio entre el 29 de septiembre y el 8 de octubre, y
-- desaparecio despues. Eso es lo que se vio.
--
-- LO QUE NO PUEDO PROBAR, Y HAY QUE DECIRLO
--
-- La reversion del 8 de octubre uso una REGLA que yo deduje, no los valores
-- originales fila por fila: ya no los tengo. El respaldo
-- _esquemas/RESPALDO_gasto_configuracion_2026_10_08.sql se tomo despues de mi
-- primer cambio, asi que no sirve de testigo.
--
-- Entonces no se si el 1 estaba en 0 antes del 29 de septiembre. Lo que si se
-- es que en 0 el reporte no lo muestra, y el dato se necesita. Se pone en 1.
--
-- POR QUE ES SEGURO
--
-- activo no decide que se calcula. GastoConfiguracionDAO de Servicios hace
-- SELECT * sin filtrar, y por eso el concepto 1 se siguio escribiendo todas
-- las semanas aunque estuviera en 0: hasta el 2026-10-18 tiene sus 144 filas.
-- Prenderlo no cambia lo que se escribe, solo lo vuelve visible.
--
-- Si el reporte igual no lo trae, esto se deshace con un UPDATE y hay que
-- buscar por otro lado: seria que filtra por otra cosa.
-- ---------------------------------------------------------------------------

UPDATE gasto_configuracion
   SET activo = 1
 WHERE idgasto_conf = 1
   AND activo <> 1;

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================
SELECT idgasto_conf id, nombre_gasto, activo, rentabilidad_calcula AS tablero,
       IF(consulta_sql = 'NA', 'NA', 'consulta') AS sql_
  FROM gasto_configuracion
 WHERE idgasto_conf IN (1, 16, 17, 18, 20, 24, 25)
 ORDER BY idgasto_conf;

-- Ningun concepto que el costo por tienda use puede estar marcado NO USAR.
-- Tiene que dar cero filas.
SELECT c.idgasto_conf, c.nombre_gasto, c.activo
  FROM gasto_configuracion c
  JOIN pizzaamericana.concepto_costo_gasto_semanal m ON m.idgasto_conf = c.idgasto_conf
 WHERE c.nombre_gasto LIKE 'NO USAR%';

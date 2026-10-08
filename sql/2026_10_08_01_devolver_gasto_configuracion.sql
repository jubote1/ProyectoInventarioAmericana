-- ---------------------------------------------------------------------------
-- DEVOLVER gasto_configuracion A COMO ESTABA, Y SACAR LO NUEVO APARTE
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente. No toca gasto_semanal ni ningun dato historico.
--
-- EL ERROR QUE SE CORRIGE AQUI
--
-- Para marcar cuales conceptos calcula el tablero de rentabilidad nuevo, use
-- una columna que YA EXISTIA y que ya tenia duenio: `activo`. Y renombre 15
-- conceptos para documentarlos. Las dos cosas cambiaron el mundo viejo:
-- un reporte dejo de traer conceptos.
--
-- La leccion es simple: lo nuevo no se marca en una columna que lo viejo ya
-- usa. Se marca en una columna propia. Eso es lo que hace este script.
--
-- 1. Devuelve nombre_gasto, activo y porcentaje_gasto a como estaban.
-- 2. Agrega `rentabilidad_calcula`, que es de la pantalla nueva y de nadie mas.
--
-- Respaldo del estado de hoy, por si hay que deshacer esto:
--   _esquemas/RESPALDO_gasto_configuracion_2026_10_08.sql
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LOS NOMBRES
--
-- Los 7 que renombre con "NO USAR - " y los 8 a los que les agregue el origen
-- entre parentesis. Se escriben uno por uno y no con un REPLACE generico: un
-- REPLACE tocaria filas que ya tenian su nombre asi desde antes.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pago Virtual'              WHERE idgasto_conf = 2;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pedidos Rappi'             WHERE idgasto_conf = 3;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pagos PAYU'                WHERE idgasto_conf = 4;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pago Datafono'             WHERE idgasto_conf = 12;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Descuento con Reintegro'   WHERE idgasto_conf = 13;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Descuento sin Reintegro'   WHERE idgasto_conf = 14;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pedidos DIDI'              WHERE idgasto_conf = 21;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pago Datafono'             WHERE idgasto_conf = 24;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pago Virtual'              WHERE idgasto_conf = 26;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pagos PAYU'                WHERE idgasto_conf = 32;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pedidos DIDI'              WHERE idgasto_conf = 33;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Pedidos Rappi'             WHERE idgasto_conf = 34;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Descuentos con Reintegro'  WHERE idgasto_conf = 35;
UPDATE inventarioamericana.gasto_configuracion SET nombre_gasto = 'Descuentos sin Reintegro'  WHERE idgasto_conf = 36;

-- EL 25 ES EL UNICO QUE NO SE PUEDE RECONSTRUIR CON CERTEZA.
--
-- Hoy dice 'QR Bancolombia (Wompi)'. Segun la nota que deje el 2026-10-03,
-- antes decia 'NO USAR - XL Promo Deditos Santa', y ese prefijo NO lo puse yo:
-- ya venia. Si es asi, mi script se lo borro.
--
-- Se devuelve a ese nombre porque es el que esta documentado. Si al verlo en
-- pantalla resulta que no era, corrijalo a mano: es un solo UPDATE.
UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - XL Promo Deditos Santa'
 WHERE idgasto_conf = 25 AND nombre_gasto = 'QR Bancolombia (Wompi)';

-- ===========================================================================
-- 2. LA BANDERA activo, COMO ESTABA
--
-- La regla original: activo = 1 en las que NO tienen consulta, 0 en las que si.
-- Suena al reves y lo es, pero asi estaba y asi lo esperan los lectores viejos.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET activo = IF(consulta_sql = 'NA', 1, 0);

-- ===========================================================================
-- 3. EL PORCENTAJE DEL DATAFONO
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET porcentaje_gasto = 5
 WHERE idgasto_conf = 24 AND porcentaje_gasto = 0;

-- ===========================================================================
-- 4. LA COLUMNA NUEVA, QUE ES LA QUE DEBI HABER USADO DESDE EL PRINCIPIO
--
-- Solo la lee el tablero de rentabilidad de ProyectoInventarioAmericana.
-- Ningun proceso viejo la conoce, asi que prenderla y apagarla no le mueve
-- nada a nadie mas.
--
-- Arranca en 0 para TODAS. Se prende en los nueve conceptos que el tablero
-- suma como gasto de operacion: el conteo de domicilios, los egresos de caja
-- por tipo y los desechos. Los demas son recaudo por canal o conteos de
-- pedidos, y sumarlos como gasto seria contar la venta con tarjeta como si
-- fuera un costo.
-- ===========================================================================

SET @n := (SELECT COUNT(*) FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = 'inventarioamericana'
             AND TABLE_NAME = 'gasto_configuracion'
             AND COLUMN_NAME = 'rentabilidad_calcula');
SET @s := IF(@n = 0,
	'ALTER TABLE inventarioamericana.gasto_configuracion
	   ADD COLUMN rentabilidad_calcula TINYINT NOT NULL DEFAULT 0
	   COMMENT ''Solo para el tablero de rentabilidad del inventario. NO la mira ningun proceso viejo''',
	'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

UPDATE inventarioamericana.gasto_configuracion
   SET rentabilidad_calcula = 0;

UPDATE inventarioamericana.gasto_configuracion
   SET rentabilidad_calcula = 1
 WHERE idgasto_conf IN (1, 5, 6, 7, 8, 9, 10, 11, 15);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT idgasto_conf id, nombre_gasto, porcentaje_gasto pct, activo,
       rentabilidad_calcula AS tablero,
       IF(consulta_sql = 'NA', 'NA', 'consulta') AS sql_
  FROM inventarioamericana.gasto_configuracion
 ORDER BY idgasto_conf;

-- Las dos cuentas de control:
--   nombres_con_marca_mia tiene que dar 0 (salvo el 25, que es a proposito).
--   activo tiene que volver a 20 / 16.
SELECT SUM(nombre_gasto LIKE 'NO USAR%' AND idgasto_conf <> 25) AS nombres_con_marca_mia,
       SUM(activo = 1) AS activas,
       SUM(activo = 0) AS apagadas,
       SUM(rentabilidad_calcula = 1) AS las_del_tablero
  FROM inventarioamericana.gasto_configuracion;

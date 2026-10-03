-- ---------------------------------------------------------------------------
-- LOS CONCEPTOS DE GASTO QUE QUEDARON REEMPLAZADOS
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente. NO toca ningun dato historico: solo nombres y comentarios.
--
-- QUE SE DESCUBRIO
--
-- Siete conceptos de gasto_semanal tienen un gemelo, y no son duplicados: son
-- dos mediciones distintas del mismo hecho, escritas por procesos distintos.
--
--   2  Pago Virtual     -> 26, de ReporteConciliacionWompi
--   3  Pedidos Rappi    -> 34, de ReporteSemanalRappi
--   4  Pagos PAYU       -> 32, de ReporteConciliacionPAYU
--   12 Pago Datafono    -> 24, de ReporteConciliacionWompi
--   13 Descuento c/rein -> 35, del POS, en el cierre semanal
--   14 Descuento s/rein -> 36, del POS, en el cierre semanal
--   21 Pedidos DIDI     -> 33, de ReporteSemanalDIDI
--
-- El de id BAJO lo calculaba ReporteConsolidacionRentabilidad corriendo la
-- consulta_sql que el concepto tiene guardada: un calculo local contra la base
-- de la tienda. El de id ALTO lo escribe el proceso de conciliacion con la
-- cifra que de verdad liquido la plataforma o el recaudador.
--
-- Manda el ALTO. El bajo es lo que la tienda cree que vendio; el alto es lo
-- que la plataforma efectivamente pago.
--
-- EL CASO DE RAPPI, QUE ES EL QUE MAS ASUSTA Y EL MENOS GRAVE
--
-- El concepto 3 difiere del 34 en 427 de 429 semanas-tienda, por 807 millones
-- en 2026. No es que las dos fuentes no cuadren: su consulta dice
--
--     WHERE a.estacion = 'RAPPI'
--
-- y las estaciones hoy se llaman RAPPI DOMI-Plat y RAPPI DOMI-Prop. La
-- consulta dejo de encontrar nada hace tiempo y nadie se entero, porque un
-- cero no da error. Una fuente esta muerta y la otra viva.
--
-- La consulta NO se arregla a proposito. Arreglarla dejaria lista una trampa:
-- el dia que alguien vuelva a poner activo = 1, Rappi se contaria dos veces.
-- Queda rota y con el nombre que dice por que.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LOS NOMBRES, PARA QUE NADIE LOS VUELVA A LEER POR ERROR
--
-- Se usa el mismo prefijo que ya existia en el 25 ("NO USAR - XL Promo
-- Deditos Santa"), para no inventar una convencion nueva.
--
-- Los lectores de nombre_gasto son dos y ninguno toca estos: el correo de
-- ReporteConsolidacionRentabilidad solo nombra los de activo = 1, y el detalle
-- del tablero solo lee los activos y las cuatro comisiones.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Pago Virtual (lo escribe el 26, de Wompi)'
 WHERE idgasto_conf = 2 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Pedidos Rappi (consulta rota; lo escribe el 34)'
 WHERE idgasto_conf = 3 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Pagos PAYU (lo escribe el 32, de PayU)'
 WHERE idgasto_conf = 4 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Pago Datafono (lo escribe el 24, de Wompi)'
 WHERE idgasto_conf = 12 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Descuento con Reintegro (lo escribe el 35, del POS)'
 WHERE idgasto_conf = 13 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Descuento sin Reintegro (lo escribe el 36, del POS)'
 WHERE idgasto_conf = 14 AND nombre_gasto NOT LIKE 'NO USAR%';

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = 'NO USAR - Pedidos DIDI (lo escribe el 33, de DiDi)'
 WHERE idgasto_conf = 21 AND nombre_gasto NOT LIKE 'NO USAR%';

-- ===========================================================================
-- 2. DE DONDE SALE CADA CONCEPTO VIVO
--
-- Para que quien abra la tabla no tenga que leerse cuatro procesos de Java
-- para saber quien escribe que.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET nombre_gasto = CASE idgasto_conf
       WHEN 24 THEN 'Pago Datafono (Wompi)'
       WHEN 25 THEN 'QR Bancolombia (Wompi)'
       WHEN 26 THEN 'Pago Virtual (Wompi)'
       WHEN 32 THEN 'Pagos PAYU (PayU)'
       WHEN 33 THEN 'Pedidos DIDI (DiDi)'
       WHEN 34 THEN 'Pedidos Rappi (Rappi)'
       WHEN 35 THEN 'Descuentos con Reintegro (POS, cierre semanal)'
       WHEN 36 THEN 'Descuentos sin Reintegro (POS, cierre semanal)'
       ELSE nombre_gasto END
 WHERE idgasto_conf IN (24,25,26,32,33,34,35,36)
   AND nombre_gasto NOT LIKE '%(%';

-- ===========================================================================
-- 3. QUE SIGNIFICA activo, QUE HOY ENGANA
--
-- La bandera NO dice si el concepto esta en uso. Dice si
-- ReporteConsolidacionRentabilidad debe correr su consulta_sql. Las comisiones
-- -16, 17, 18, 20- tienen activo = 0 y sin embargo se escriben todas las
-- semanas, porque las escriben sus propios procesos de conciliacion, que no
-- miran esta bandera.
--
-- Quien mire la tabla sin saber esto concluye que las comisiones estan
-- apagadas, y son entre el 3% y el 10% de la venta.
-- ===========================================================================

ALTER TABLE inventarioamericana.gasto_configuracion
  MODIFY COLUMN activo INT DEFAULT 0
  COMMENT 'Si ReporteConsolidacionRentabilidad corre su consulta_sql. NO dice si el concepto esta en uso: las comisiones 16/17/18/20 van en 0 y se escriben igual, desde los procesos de conciliacion';

ALTER TABLE inventarioamericana.gasto_configuracion
  MODIFY COLUMN consulta_sql VARCHAR(5000)
  COMMENT 'Solo la usa ReporteConsolidacionRentabilidad, y solo cuando activo = 1. Corre contra la base de CADA tienda, con %idtienda%, %fechainferior% y %fechasuperior%';

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT idgasto_conf, nombre_gasto, activo,
       CASE WHEN nombre_gasto LIKE 'NO USAR%' THEN 'reemplazado'
            WHEN activo = 1 THEN 'lo calcula rentabilidad'
            ELSE 'lo escribe su propio proceso' END AS quien_lo_escribe
  FROM inventarioamericana.gasto_configuracion
 ORDER BY activo DESC, idgasto_conf;

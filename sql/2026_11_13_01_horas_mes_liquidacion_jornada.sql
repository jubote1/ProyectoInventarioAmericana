-- ============================================================================
-- 2026_11_13_01_horas_mes_liquidacion_jornada.sql
--
-- El divisor para sacar el valor de la hora estaba en 240, que es la jornada
-- VIEJA de 48 horas (8 x 30). La jornada de hoy son 42 horas.
--
-- LA REGLA, y asi queda documentada para que no se vuelva a desfasar:
--
--      HORAS_MES_LIQUIDACION = JORNADA_SEMANAL / 6 x 30 = JORNADA_SEMANAL x 5
--
--      46 h/semana -> 230     44 h/semana -> 220     42 h/semana -> 210
--
-- Es la misma cuenta del archivo HORAS.xlsx de Contabilidad (sueldo / 30 / 7
-- para la jornada de 42), y la que exige la Ley 2101 de 2021 al reducir la
-- jornada, junto con la Ley 2466 de 2025.
--
-- POR QUE PESA: el valor de la hora multiplica TODOS los recargos. Con 240 en
-- vez de 210 cada recargo salia 14,3% corto, y como la seguridad social y la
-- liquidacion se calculan sobre el sueldo variable, el costo tambien.
--
-- LAS VIGENCIAS QUEDAN PEGADAS A LAS DE JORNADA_SEMANAL, que ya estaban bien:
--      44 h: 2025-07-16 a 2026-07-14      42 h: desde 2026-07-15
-- Si algun dia cambia la jornada, hay que mover LAS DOS filas. Son la misma
-- norma contada de dos maneras.
--
-- Idempotente: se puede correr las veces que sea.
-- ============================================================================

-- Respaldo de lo que habia, por si hay que devolverse.
CREATE TABLE IF NOT EXISTS inventarioamericana.respaldo_param_horas_20261113 AS
SELECT * FROM inventarioamericana.gerencia_nomina_parametro
 WHERE codigo = 'HORAS_MES_LIQUIDACION';

-- Fuera la fila unica de 240. No se "corrige" el valor: esa fila nacio con una
-- vigencia que no corresponde a ninguna jornada real.
DELETE FROM inventarioamericana.gerencia_nomina_parametro
 WHERE codigo = 'HORAS_MES_LIQUIDACION'
   AND vigencia_desde = '2026-01-01';

-- Las dos que si corresponden.
INSERT INTO inventarioamericana.gerencia_nomina_parametro
       (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
VALUES
 ('HORAS_MES_LIQUIDACION', 'Divisor para sacar el valor de la hora', 220, 'HORA',
  '2025-07-16', '2026-07-14',
  'Jornada de 44 h/semana: 44/6 x 30 = 220. Ley 2101 de 2021.', 'jdbotero'),
 ('HORAS_MES_LIQUIDACION', 'Divisor para sacar el valor de la hora', 210, 'HORA',
  '2026-07-15', NULL,
  'Jornada de 42 h/semana: 42/6 x 30 = 210. Ley 2101 de 2021.', 'jdbotero')
ON DUPLICATE KEY UPDATE
       valor          = VALUES(valor),
       nombre         = VALUES(nombre),
       unidad         = VALUES(unidad),
       vigencia_hasta = VALUES(vigencia_hasta),
       observacion    = VALUES(observacion);

-- Comprobacion: las dos lineas tienen que quedar emparejadas, jornada x 5.
SELECT j.vigencia_desde, j.vigencia_hasta,
       j.valor AS jornada_semanal,
       h.valor AS horas_mes,
       (j.valor * 5) AS deberia_ser,
       IF(h.valor = j.valor * 5, 'OK', 'REVISAR') AS cuadra
  FROM inventarioamericana.gerencia_nomina_parametro j
  LEFT JOIN inventarioamericana.gerencia_nomina_parametro h
    ON h.codigo = 'HORAS_MES_LIQUIDACION'
   AND h.vigencia_desde = j.vigencia_desde
 WHERE j.codigo = 'JORNADA_SEMANAL'
 ORDER BY j.vigencia_desde;

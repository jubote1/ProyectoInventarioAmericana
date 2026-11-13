-- ============================================================================
-- 2026_11_13_02_extra_ordinaria_125.sql
--
-- LA HORA EXTRA SE PAGA COMPLETA, NO SOLO EL RECARGO.
--
-- Un RECARGO -nocturno 35%, dominical 90%- va sobre una hora que el sueldo
-- mensual YA cubrio: solo se debe el porcentaje. Una HORA EXTRA es una hora que
-- el sueldo NO cubrio, asi que se debe la hora entera mas su recargo: 125%.
--
-- El cuadro de Contabilidad (HORAS.xlsx) lo deja ver: con valor hora de
-- $10.476, HEO vale $13.095, que es la hora x 1,25 -no el 25% de la hora-.
--
-- Antes el calculo mezclaba EXTRA_DIURNA (25) y EXTRA_NOCTURNA (75) por
-- proporcion. Ahora va UNA SOLA TASA: en el modelo de la compania las horas de
-- noche ya cobran su 35% aparte, en el recargo nocturno, sean ordinarias o
-- extras. Los dos parametros viejos se dejan -no los borro- porque todavia
-- alimentan el calculo de la extra FESTIVA, que esta por definir.
--
-- Idempotente.
-- ============================================================================

INSERT INTO inventarioamericana.gerencia_nomina_parametro
       (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
VALUES
 ('EXTRA_ORDINARIA', 'Hora extra ordinaria, hora completa mas recargo', 125, 'PORCENTAJE',
  '2025-07-16', NULL,
  'La hora extra se paga completa: 100% de la hora + 25% de recargo. Cuadro de Contabilidad HORAS.xlsx, HEO = valor hora x 1,25.',
  'jdbotero')
ON DUPLICATE KEY UPDATE
       valor       = VALUES(valor),
       nombre      = VALUES(nombre),
       unidad      = VALUES(unidad),
       observacion = VALUES(observacion);

-- Comprobacion: los tres factores que Juan David dio por buenos el 2026-11-13.
SELECT codigo, valor, unidad, vigencia_desde, vigencia_hasta
  FROM inventarioamericana.gerencia_nomina_parametro
 WHERE codigo IN ('EXTRA_ORDINARIA', 'RECARGO_NOCTURNO', 'RECARGO_DOMINICAL', 'HORAS_MES_LIQUIDACION')
 ORDER BY codigo, vigencia_desde;

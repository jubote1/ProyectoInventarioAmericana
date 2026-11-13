-- ============================================================================
-- 2026_11_13_03_extra_festiva_215.sql
--
-- HORA EXTRA EN DOMINGO O FESTIVO: 215%. Definido por Contabilidad el
-- 2026-11-13, y se arma sumando:
--
--      100%   la hora, que el sueldo del mes no cubrio
--    +  90%   el recargo dominical vigente
--    +  25%   el recargo de extra
--    = 215%
--
-- Por eso las vigencias van pegadas a las de RECARGO_DOMINICAL: 205% cuando el
-- dominical estaba en 80%, 215% ahora, 225% cuando suba a 100% en julio de
-- 2027. Si alguien mueve RECARGO_DOMINICAL tiene que mover tambien estas.
--
-- LA OTRA MITAD DE LA CORRECCION, y no necesita SQL: la HORA FESTIVA (HF) es
-- el 90%, lo MISMO que el recargo dominical. El codigo ya lo hace bien:
-- RECARGO_FESTIVO no existe como fila y cae por defecto en RECARGO_DOMINICAL,
-- asi que un festivo paga igual que un domingo y sigue solo. Se deja asi a
-- proposito: una fila propia se desfasaria el dia que cambie el dominical.
--
-- Se CIERRAN los cuatro parametros del reparto diurno/nocturno, que ya nadie
-- lee. No se borran: son historia de como se calculaba antes.
--
-- Idempotente.
-- ============================================================================

INSERT INTO inventarioamericana.gerencia_nomina_parametro
       (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
VALUES
 ('EXTRA_FESTIVA', 'Hora extra en domingo o festivo', 205, 'PORCENTAJE',
  '2025-07-01', '2026-06-30', '100% la hora + 80% dominical + 25% extra', 'jdbotero'),
 ('EXTRA_FESTIVA', 'Hora extra en domingo o festivo', 215, 'PORCENTAJE',
  '2026-07-01', '2027-06-30', '100% la hora + 90% dominical + 25% extra', 'jdbotero'),
 ('EXTRA_FESTIVA', 'Hora extra en domingo o festivo', 225, 'PORCENTAJE',
  '2027-07-01', NULL, '100% la hora + 100% dominical + 25% extra', 'jdbotero')
ON DUPLICATE KEY UPDATE
       valor          = VALUES(valor),
       nombre         = VALUES(nombre),
       unidad         = VALUES(unidad),
       vigencia_hasta = VALUES(vigencia_hasta),
       observacion    = VALUES(observacion);

UPDATE inventarioamericana.gerencia_nomina_parametro
   SET vigencia_hasta = '2026-11-12',
       observacion = 'Sin uso desde el 2026-11-13: las extras van a una sola tasa (EXTRA_ORDINARIA / EXTRA_FESTIVA) y la noche cobra aparte su recargo nocturno.'
 WHERE codigo IN ('EXTRA_DIURNA', 'EXTRA_NOCTURNA', 'EXTRA_FESTIVA_DIURNA', 'EXTRA_FESTIVA_NOCTURNA')
   AND vigencia_hasta IS NULL;

-- Comprobacion: los cinco factores de la tabla de Contabilidad, al dia de hoy.
SELECT 'HEO  hora extra ordinaria'   AS concepto, valor, '100 + 25'      AS se_arma FROM inventarioamericana.gerencia_nomina_parametro WHERE codigo='EXTRA_ORDINARIA'   AND CURDATE() BETWEEN vigencia_desde AND IFNULL(vigencia_hasta,'9999-12-31')
UNION ALL
SELECT 'RN   recargo nocturno',             valor, 'solo el recargo'     FROM inventarioamericana.gerencia_nomina_parametro WHERE codigo='RECARGO_NOCTURNO'  AND CURDATE() BETWEEN vigencia_desde AND IFNULL(vigencia_hasta,'9999-12-31')
UNION ALL
SELECT 'RD   recargo dominical',            valor, 'solo el recargo'     FROM inventarioamericana.gerencia_nomina_parametro WHERE codigo='RECARGO_DOMINICAL' AND CURDATE() BETWEEN vigencia_desde AND IFNULL(vigencia_hasta,'9999-12-31')
UNION ALL
SELECT 'HF   hora festiva',                 valor, 'hereda el dominical' FROM inventarioamericana.gerencia_nomina_parametro WHERE codigo='RECARGO_DOMINICAL' AND CURDATE() BETWEEN vigencia_desde AND IFNULL(vigencia_hasta,'9999-12-31')
UNION ALL
SELECT 'HEFD hora extra dom/festivo',       valor, '100 + 90 + 25'       FROM inventarioamericana.gerencia_nomina_parametro WHERE codigo='EXTRA_FESTIVA'     AND CURDATE() BETWEEN vigencia_desde AND IFNULL(vigencia_hasta,'9999-12-31');

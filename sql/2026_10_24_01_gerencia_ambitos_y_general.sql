-- ---------------------------------------------------------------------------
-- Gerencia: ambitos de los conceptos, y el valor "todas las tiendas"
--
-- 2026-10-24. Se corre en el CENTRAL. Idempotente.
--
-- 1. Seguridad Acropolis esta marcado como de TIENDA pero tiene valor cargado
--    en el area ADMINISTRATIVA (1.500.000) y tambien en una tienda
--    (2.567.000). Son los dos reales -la bodega y el punto de venta- asi que
--    el concepto aplica a los dos lados: AMBOS.
--
--    Mientras decia TIENDA, la pantalla de areas lo habria escondido y ese
--    gasto habria seguido sumando en el cierre sin que nadie pudiera tocarlo.
--
-- 2. Administracion (17) igual: hay uno de tienda y puede haber de area.
--
-- 3. Publicista (12) pasa a AMBOS: en el Excel aparece como linea de cada
--    tienda, y el costo del publicista tambien vive en la hoja de publicidad.
-- ---------------------------------------------------------------------------

UPDATE inventarioamericana.gerencia_concepto_gasto
   SET ambito = 'AMBOS'
 WHERE idconcepto IN (12, 16, 17);

-- ---------------------------------------------------------------------------
-- Como quedo el reparto de ambitos
-- ---------------------------------------------------------------------------
SELECT ambito, COUNT(*) AS conceptos,
       GROUP_CONCAT(nombre ORDER BY orden SEPARATOR ', ') AS cuales
  FROM inventarioamericana.gerencia_concepto_gasto
 WHERE activo = 'S'
 GROUP BY ambito ORDER BY ambito;

-- ---------------------------------------------------------------------------
-- Candidatos a pasar a valor general (tienda 0): mismo valor en TODAS
--
-- No se convierten aqui. Se listan para que gerencia decida: convertirlos
-- cambia de donde sale el numero y eso se hace mirando, no por script.
-- ---------------------------------------------------------------------------
SELECT c.nombre AS concepto,
       COUNT(DISTINCT g.idtienda) AS tiendas,
       COUNT(DISTINCT g.valor_mensual) AS valores_distintos,
       MAX(g.valor_mensual) AS valor
  FROM inventarioamericana.gerencia_gasto_fijo_tienda g
  JOIN inventarioamericana.gerencia_concepto_gasto c ON c.idconcepto = g.idconcepto
 WHERE g.vigencia_hasta IS NULL AND g.idtienda > 0
 GROUP BY c.idconcepto, c.nombre
HAVING COUNT(DISTINCT g.valor_mensual) = 1
 ORDER BY c.orden;

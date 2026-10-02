-- ---------------------------------------------------------------------------
-- COSTOS SIMULADOS DE AGOSTO Y SEPTIEMBRE, COPIADOS DE JULIO
--
-- Se corre en el CENTRAL (172.19.0.25). Idempotente.
--
-- PARA QUE ES ESTO, Y PARA QUE NO
--
-- Julio es el unico mes cargado de verdad. El tablero de rentabilidad necesita
-- costos en los meses que tienen venta cerrada para poderse probar y mirar, y
-- la nomina de verdad se definira cuando se revise la carga -SIIGO o
-- liquidacion aproximada-. Mientras tanto se copia julio a agosto y a
-- septiembre.
--
-- NO se copia a enero..junio. Nueve meses inventados dejarian de verse como
-- una simulacion y empezarian a verse como historia. Esos meses quedan vacios
-- y el tablero lo dice.
--
-- TODO QUEDA MARCADO
--
-- Los servicios entran con origen ESTIMADO, que es la marca que ya existe y
-- que la pantalla pinta distinto; ademas, cuando llegue la factura real, el
-- guardar normal pisa al estimado sin preguntar.
--
-- La nomina y la estructura no tienen columna de origen, asi que se firman con
-- usuario = 'SIMULACION'. Esa firma es la que lee el tablero para avisar en
-- pantalla que ese mes no es real. Si manana se carga la nomina de verdad, se
-- sobrescribe y la firma cambia sola.
--
-- LOS GASTOS FIJOS NO SE COPIAN, A PROPOSITO
--
-- Las 77 filas de gerencia_gasto_fijo_tienda rigen desde 2026-07-01 y estan
-- abiertas (vigencia_hasta NULL), asi que ya aplican a agosto y a septiembre.
-- Copiarlas crearia duplicados y el arriendo se cobraria dos veces.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. SERVICIOS PUBLICOS
-- ===========================================================================

INSERT INTO inventarioamericana.gerencia_gasto_servicio
  (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT s.idtienda, s.idconcepto, 2026, m.mes, s.valor, 'ESTIMADO', 1, 'SIMULACION', NOW()
  FROM inventarioamericana.gerencia_gasto_servicio s
 CROSS JOIN (SELECT 8 AS mes UNION ALL SELECT 9) m
 WHERE s.anio = 2026 AND s.mes = 7
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = s.idtienda AND ya.idconcepto = s.idconcepto
                      AND ya.anio = 2026 AND ya.mes = m.mes);

-- ===========================================================================
-- 2. NOMINA AGREGADA POR TIENDA
-- ===========================================================================

INSERT INTO inventarioamericana.gerencia_nomina_tienda
  (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social,
   liquidacion, usuario, fecha_registro)
SELECT n.idtienda, 2026, m.mes, n.empleados, n.sueldo_basico, n.sueldo_variable,
       n.seguridad_social, n.liquidacion, 'SIMULACION', NOW()
  FROM inventarioamericana.gerencia_nomina_tienda n
 CROSS JOIN (SELECT 8 AS mes UNION ALL SELECT 9) m
 WHERE n.anio = 2026 AND n.mes = 7
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_nomina_tienda ya
                    WHERE ya.idtienda = n.idtienda AND ya.anio = 2026 AND ya.mes = m.mes);

-- ===========================================================================
-- 3. LAS CUATRO BOLSAS DE ESTRUCTURA
-- ===========================================================================

INSERT INTO inventarioamericana.gerencia_pool_estructura
  (anio, mes, tipo, valor, usuario, fecha_registro)
SELECT 2026, m.mes, p.tipo, p.valor, 'SIMULACION', NOW()
  FROM inventarioamericana.gerencia_pool_estructura p
 CROSS JOIN (SELECT 8 AS mes UNION ALL SELECT 9) m
 WHERE p.anio = 2026 AND p.mes = 7
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_pool_estructura ya
                    WHERE ya.anio = 2026 AND ya.mes = m.mes AND ya.tipo = p.tipo);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT mes,
       SUM(que = 'servicio')  AS servicios,
       SUM(que = 'nomina')    AS nominas,
       SUM(que = 'pool')      AS bolsas,
       GROUP_CONCAT(DISTINCT firma) AS firmado_por
  FROM (
    SELECT mes, 'servicio' AS que, usuario AS firma FROM inventarioamericana.gerencia_gasto_servicio WHERE anio = 2026
    UNION ALL
    SELECT mes, 'nomina', usuario FROM inventarioamericana.gerencia_nomina_tienda WHERE anio = 2026
    UNION ALL
    SELECT mes, 'pool', usuario FROM inventarioamericana.gerencia_pool_estructura WHERE anio = 2026
  ) t GROUP BY mes ORDER BY mes;

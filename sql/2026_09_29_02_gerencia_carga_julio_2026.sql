-- ---------------------------------------------------------------------------
-- CARGA INICIAL DEL MENU GERENCIA: julio de 2026
--
-- Se corre en el CENTRAL (172.19.0.25). Idempotente: correrlo dos veces no
-- duplica nada.
-- Requiere 2026_09_29_01_gerencia_calendario_gastos_y_acceso.sql.
--
-- Trae a la base lo que hoy vive en GASTOS_FIJOS_TIENDAS_JULIO.xlsx: los gastos
-- fijos de las once tiendas, los servicios publicos de julio, las cuatro bolsas
-- de estructura y la nomina agregada por tienda.
--
-- ESTE ARCHIVO NO SE ESCRIBIO A MANO
--
-- Lo genero un script que lee las hojas del Excel, para que ninguna cifra
-- dependa de que alguien la copie bien. Si algo esta mal, esta mal en la hoja o
-- en el mapeo de nombres, que se puede revisar, y no en un dedo.
--
-- LAS HOJAS SE LLAMAN DISTINTO QUE LAS TIENDAS
--
-- La hoja CABANAS es Bello (tienda 2), confirmado con el usuario. Las demas
-- coinciden. La hoja PORCENTAJE ya la llama BELLO.
--
-- ===========================================================================
-- DOS COSAS QUE NO CUADRAN EN EL EXCEL, Y QUE HAY QUE MIRAR
-- ===========================================================================
--
-- Al sumar el detalle persona por persona y compararlo contra la fila TOTAL de
-- cada hoja, dos tiendas no dan. Las dos fallas son del mismo tipo: el rango de
-- la formula SUM empieza una fila mas abajo de donde debia, y se deja por fuera
-- a la primera persona de la lista. Solo en UNA columna cada vez, por eso pasa
-- desapercibido: las otras columnas de esa misma tienda si cuadran.
--
--   BELLO, columna SEGURIDAD SOCIAL
--     La hoja totaliza    8.634.750
--     El detalle suma     9.072.500
--     Diferencia            437.750  (una persona de salario 1.751.000)
--
--   LA MOTA, columna SUELDO VARIABLE
--     La hoja totaliza   18.049.000
--     El detalle suma    21.225.000
--     Diferencia          3.176.000  (Katerin Johana Garcia, la primera fila)
--
-- Esto NO es un problema de esta carga: es que el Excel de hoy subestima la
-- nomina de esas dos tiendas, y como la hoja PORCENTAJE se alimenta de esos
-- totales, el porcentaje de nomina de La Mota sale casi dos puntos por debajo
-- de lo que es.
--
-- SE CARGA EL DETALLE, NO EL TOTAL DE LA HOJA. El detalle es auditable persona
-- por persona; el total es una formula que se puede probar que deja gente por
-- fuera. Si resulta que esas personas se excluian a proposito, se corrige desde
-- la pantalla de Estructura y Nomina en un minuto.
--
-- EL CONTEO DE EMPLEADOS TAMPOCO SIRVE
--
-- El rotulo "NOMINA (n)" de cada hoja esta viejo y nadie lo actualiza: Envigado
-- dice 5 y tiene 10 personas listadas; Niquia dice 3 y tiene 6. Se carga la
-- cantidad de filas con salario, que es la que se puede contar.
--
-- LOS CEROS NO SE CARGAN
--
-- Datafono, comisariato, reteica, publicista y tiempos estan en cero en todas
-- las tiendas. Un gasto fijo en cero no aporta nada al tablero y llena la
-- pantalla de filas que hay que leer para descubrir que no dicen nada. El dia
-- que dejen de ser cero se agregan desde la pantalla.
--
-- Niquia no tiene seguro de tienda -la celda esta vacia, no en cero- ni
-- industria y comercio. Se cargan tal cual: sin fila.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. DOS CONCEPTOS QUE NO ESTABAN EN EL CATALOGO
--
-- Aparecieron leyendo las hojas y NO son menores: la seguridad de Acropolis le
-- cuesta a Calasanz 2.567.000 al mes, que es el 22% de todo su gasto fijo.
-- Dejarlos por fuera habria hecho que Calasanz se viera 22% mas barata de lo
-- que es, sin que nada avisara.
-- ===========================================================================

INSERT INTO inventarioamericana.gerencia_concepto_gasto (nombre, tipo, linea, orden, activo)
SELECT * FROM (
          SELECT 'Seguridad Acropolis' AS n, 'FIJO' AS t, 'CUOTA PUNTO DE VENTA' AS l, 130 AS o, 'S' AS a
UNION ALL SELECT 'Administracion',           'FIJO',     'CUOTA PUNTO DE VENTA', 140, 'S'
) nuevos
WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_concepto_gasto ya
                   WHERE ya.nombre = nuevos.n);

-- ===========================================================================
-- 2. LAS CUATRO BOLSAS DE ESTRUCTURA DE JULIO
--
-- Son el total de cada hoja, verificado sumando sus renglones:
--
--   ADMINISTRATIVA  133.797.880   nomina 106.177.180 mas gasolina, servicios,
--                                 arriendo, celulares, seguridad, transportes
--   LOGISTICA        33.379.760   solo nomina
--   CONTACT          39.489.280   solo nomina
--   PUBLICIDAD        9.587.100   agencia, publicista y pautas
--
-- No se guarda ningun porcentaje de reparto. El reparto es por participacion en
-- la venta y se calcula al mostrar, contra la venta real de las semanas que el
-- calendario le asigno al mes.
-- ===========================================================================

INSERT INTO inventarioamericana.gerencia_pool_estructura (anio, mes, tipo, valor, usuario, fecha_registro)
SELECT * FROM (
          SELECT 2026 AS a, 7 AS m, 'ADMINISTRATIVA' AS t, 133797880 AS v, 'carga_julio' AS u, NOW() AS f
UNION ALL SELECT 2026, 7, 'LOGISTICA',   33379760, 'carga_julio', NOW()
UNION ALL SELECT 2026, 7, 'CONTACT',     39489280, 'carga_julio', NOW()
UNION ALL SELECT 2026, 7, 'PUBLICIDAD',   9587100, 'carga_julio', NOW()
) nuevos
WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_pool_estructura ya
                   WHERE ya.anio = nuevos.a AND ya.mes = nuevos.m AND ya.tipo = nuevos.t);

-- ===========================================================================
-- 3. GASTOS FIJOS Y SERVICIOS DE LAS ONCE TIENDAS
--
-- Los fijos entran con vigencia desde el 1 de julio de 2026 y sin fecha de
-- cierre, o sea vigentes. Cuando alguno cambie, la pantalla le cierra la
-- vigencia a este y abre uno nuevo: julio va a seguir mostrando lo de julio.
--
-- Los servicios entran como REAL de julio: son la factura, no un estimado.
-- ===========================================================================

-- GASTOS FIJOS Y SERVICIOS
-- ---- Manrique (tienda 1)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 1, c.idconcepto, 2026, 7, 2765900, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 2000000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 1, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 1 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Bello (tienda 2)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 2, c.idconcepto, 2026, 7, 2739100, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 2650000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 2, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 2 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- America (tienda 3)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 3, c.idconcepto, 2026, 7, 3138600, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 3885000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 3, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 3 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Calasanz (tienda 4)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 4, c.idconcepto, 2026, 7, 2799700, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 4100000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 220000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 2567000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguridad Acropolis'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 4, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 4 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Itagui (tienda 5)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 5, c.idconcepto, 2026, 7, 3389300, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 3300000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 220000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 390000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Administracion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 5, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 5 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- La Mota (tienda 7)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 7, c.idconcepto, 2026, 7, 2599000, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 4486000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 7, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 7 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Envigado (tienda 8)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 8, c.idconcepto, 2026, 7, 2421200, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 1950000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 8, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 8 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Pilarica (tienda 9)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 9, c.idconcepto, 2026, 7, 2808900, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 3890000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 9, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 9 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- San Antonio (tienda 10)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 10, c.idconcepto, 2026, 7, 3535100, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 3260000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 10, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 10 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Piloto (tienda 11)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 11, c.idconcepto, 2026, 7, 2897100, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 2300000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Seguro tienda'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 11, c.idconcepto, 500000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Industria y comercio'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 11 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
-- ---- Niquia (tienda 13)
INSERT INTO inventarioamericana.gerencia_gasto_servicio
       (idtienda, idconcepto, anio, mes, valor, origen, meses_promediados, usuario, fecha_registro)
SELECT 13, c.idconcepto, 2026, 7, 3404000, 'REAL', 0, 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Servicios publicos'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_servicio ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.anio = 2026 AND ya.mes = 7);
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 13, c.idconcepto, 3050000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Arriendo'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 13, c.idconcepto, 150000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fumigacion'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 13, c.idconcepto, 200000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Alarma'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 13, c.idconcepto, 350200, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Contador y revisor fiscal'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');
INSERT INTO inventarioamericana.gerencia_gasto_fijo_tienda
       (idtienda, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario, fecha_registro)
SELECT 13, c.idconcepto, 550000, '2026-07-01', NULL, 'Carga inicial desde GASTOS_FIJOS_TIENDAS_JULIO', 'carga_julio', NOW()
  FROM inventarioamericana.gerencia_concepto_gasto c
 WHERE c.nombre = 'Fiesta de navidad'
   AND NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_gasto_fijo_tienda ya
                    WHERE ya.idtienda = 13 AND ya.idconcepto = c.idconcepto
                      AND ya.vigencia_desde = '2026-07-01');

-- NOMINA AGREGADA POR TIENDA
-- Manrique: 10 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (1, 2026, 7, 10, 18157000, 26390000, 6664000, 5864320, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 10, sueldo_basico = 18157000, sueldo_variable = 26390000,
       seguridad_social = 6664000, liquidacion = 5864320, usuario = 'carga_julio', fecha_registro = NOW();
-- Bello: 11 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (2, 2026, 7, 11, 20007000, 29390000, 9072500, 6530040, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 11, sueldo_basico = 20007000, sueldo_variable = 29390000,
       seguridad_social = 9072500, liquidacion = 6530040, usuario = 'carga_julio', fecha_registro = NOW();
-- America: 10 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (3, 2026, 7, 10, 18356000, 26336000, 6714500, 5960240, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 10, sueldo_basico = 18356000, sueldo_variable = 26336000,
       seguridad_social = 6714500, liquidacion = 5960240, usuario = 'carga_julio', fecha_registro = NOW();
-- Calasanz: 10 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (4, 2026, 7, 10, 18206000, 21411000, 5570500, 4710420, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 10, sueldo_basico = 18206000, sueldo_variable = 21411000,
       seguridad_social = 5570500, liquidacion = 4710420, usuario = 'carga_julio', fecha_registro = NOW();
-- Itagui: 8 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (5, 2026, 7, 8, 14206000, 22295000, 5573750, 4572700, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 8, sueldo_basico = 14206000, sueldo_variable = 22295000,
       seguridad_social = 5573750, liquidacion = 4572700, usuario = 'carga_julio', fecha_registro = NOW();
-- La Mota: 9 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (7, 2026, 7, 9, 16056000, 21225000, 5306250, 4669500, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 9, sueldo_basico = 16056000, sueldo_variable = 21225000,
       seguridad_social = 5306250, liquidacion = 4669500, usuario = 'carga_julio', fecha_registro = NOW();
-- Envigado: 10 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (8, 2026, 7, 10, 18206000, 21680000, 5435000, 4828560, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 10, sueldo_basico = 18206000, sueldo_variable = 21680000,
       seguridad_social = 5435000, liquidacion = 4828560, usuario = 'carga_julio', fecha_registro = NOW();
-- Pilarica: 9 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (9, 2026, 7, 9, 15858000, 25086000, 5986750, 5007640, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 9, sueldo_basico = 15858000, sueldo_variable = 25086000,
       seguridad_social = 5986750, liquidacion = 5007640, usuario = 'carga_julio', fecha_registro = NOW();
-- San Antonio: 9 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (10, 2026, 7, 9, 16505000, 22627000, 5755750, 4977940, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 9, sueldo_basico = 16505000, sueldo_variable = 22627000,
       seguridad_social = 5755750, liquidacion = 4977940, usuario = 'carga_julio', fecha_registro = NOW();
-- Piloto: 7 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (11, 2026, 7, 7, 12904000, 19874000, 4968500, 4372280, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 7, sueldo_basico = 12904000, sueldo_variable = 19874000,
       seguridad_social = 4968500, liquidacion = 4372280, usuario = 'carga_julio', fecha_registro = NOW();
-- Niquia: 6 personas contadas en el detalle
INSERT INTO inventarioamericana.gerencia_nomina_tienda
       (idtienda, anio, mes, empleados, sueldo_basico, sueldo_variable, seguridad_social, liquidacion, usuario, fecha_registro)
VALUES (13, 2026, 7, 6, 11103000, 16984000, 4349500, 3827560, 'carga_julio', NOW())
ON DUPLICATE KEY UPDATE empleados = 6, sueldo_basico = 11103000, sueldo_variable = 16984000,
       seguridad_social = 4349500, liquidacion = 3827560, usuario = 'carga_julio', fecha_registro = NOW();

-- ===========================================================================
-- COMO QUEDO, Y LA PRUEBA DE QUE CUADRA
-- ===========================================================================

SELECT 'conceptos' AS que, COUNT(*) AS n FROM inventarioamericana.gerencia_concepto_gasto
UNION ALL SELECT 'bolsas de julio', COUNT(*) FROM inventarioamericana.gerencia_pool_estructura WHERE anio=2026 AND mes=7
UNION ALL SELECT 'gastos fijos', COUNT(*) FROM inventarioamericana.gerencia_gasto_fijo_tienda
UNION ALL SELECT 'servicios de julio', COUNT(*) FROM inventarioamericana.gerencia_gasto_servicio WHERE anio=2026 AND mes=7
UNION ALL SELECT 'nominas de julio', COUNT(*) FROM inventarioamericana.gerencia_nomina_tienda WHERE anio=2026 AND mes=7;

-- El gasto fijo mas los servicios de cada tienda tiene que dar el mismo TOTAL
-- que la hoja de esa tienda. La columna diferencia debe ser cero en las once.
SELECT t.nombre AS tienda,
       FORMAT(SUM(x.valor), 0) AS cargado,
       FORMAT(esperado.total, 0) AS hoja,
       FORMAT(SUM(x.valor) - esperado.total, 0) AS diferencia
  FROM (SELECT idtienda, valor_mensual AS valor FROM inventarioamericana.gerencia_gasto_fijo_tienda
         WHERE vigencia_desde = '2026-07-01'
        UNION ALL
        SELECT idtienda, valor FROM inventarioamericana.gerencia_gasto_servicio
         WHERE anio = 2026 AND mes = 7) x
  JOIN inventarioamericana.tienda t ON t.idtienda = x.idtienda
  JOIN (          SELECT 1 AS idtienda,  6716100 AS total
        UNION ALL SELECT 2,  7339300
        UNION ALL SELECT 3,  8973800
        UNION ALL SELECT 4, 11436900
        UNION ALL SELECT 5,  9049500
        UNION ALL SELECT 7,  9035200
        UNION ALL SELECT 8,  6321400
        UNION ALL SELECT 9,  8649100
        UNION ALL SELECT 10, 8745300
        UNION ALL SELECT 11, 7147300
        UNION ALL SELECT 13, 7704200) esperado ON esperado.idtienda = x.idtienda
 GROUP BY t.nombre, esperado.total
 ORDER BY t.nombre;

-- La nomina cargada, con el costo del mes -variable mas seguridad social mas
-- liquidacion- que es el que va al tablero.
SELECT t.nombre AS tienda, n.empleados,
       FORMAT(n.sueldo_basico, 0) AS basico,
       FORMAT(n.sueldo_variable, 0) AS variable,
       FORMAT(n.seguridad_social, 0) AS seguridad,
       FORMAT(n.liquidacion, 0) AS liquidacion,
       FORMAT(n.sueldo_variable + n.seguridad_social + n.liquidacion, 0) AS costo_mes
  FROM inventarioamericana.gerencia_nomina_tienda n
  JOIN inventarioamericana.tienda t ON t.idtienda = n.idtienda
 WHERE n.anio = 2026 AND n.mes = 7
 ORDER BY t.nombre;

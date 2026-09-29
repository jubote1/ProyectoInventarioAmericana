-- ---------------------------------------------------------------------------
-- GASTO SEMANAL: una sola fuente por concepto
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente: correrlo dos veces no cambia nada la segunda.
--
-- ORDEN DE DESPLIEGUE: primero ESTE script, despues el jar de Servicios.
--
-- Al reves no: el jar nuevo filtra por activo = 1, y hoy los quince conceptos
-- que de verdad corren estan marcados activo = 0. Si el jar sube antes que el
-- script, el proceso no calcularia NADA. Corriendo el script primero no pasa
-- nada malo: el jar viejo ignora la columna activo, asi que mientras tanto
-- todo sigue igual.
--
-- ===========================================================================
-- QUE SE ENCONTRO
-- ===========================================================================
--
-- gasto_semanal tiene 69.146 filas desde 2021 y NADIE LAS LEE. No hay reporte,
-- pantalla ni proceso que las consulte. Por eso nada de lo de abajo se habia
-- notado, y por eso hay que arreglarlo ahora: el tablero de rentabilidad va a
-- ser el primer lector.
--
-- 1. LA TABLA LA ESCRIBEN DOCE PROCESOS, NO UNO
--
-- gasto_configuracion solo configura a ReporteConsolidacionRentabilidad. Los
-- demas llevan el numero de concepto QUEMADO en el codigo:
--
--     16, 24, 25, 26   ReporteConciliacionWompi
--     17, 32           ReporteConciliacionPAYU
--     18, 23, 34       ReporteSemanalRappi y ReporteSemanalPagoRappi
--     20, 22, 33       ReporteSemanalDIDI
--     27 a 31          ReporteSemanalContactCenter
--     1 a 15, 21       ReporteConsolidacionRentabilidad (por esta tabla)
--
-- 2. SIETE CONCEPTOS ESTAN DOS VECES, Y NO DICEN LO MISMO
--
-- La semana del 2026-09-27, sumando las once tiendas:
--
--     Pedidos Rappi     concepto  3 =          0    concepto 34 = 25.886.300
--     Pedidos DIDI      concepto 21 = 49.929.484    concepto 33 = 50.456.947
--     Pago Virtual      concepto  2 = 49.271.670    concepto 26 = 49.281.670
--     Pagos PAYU        concepto  4 = 2.340.870     concepto 32 = 2.340.870
--     Pago Datafono     concepto 12 = 56.776.640    concepto 24 = 56.776.640
--     Descuento c/rein  concepto 13 = 23.196.993    concepto 35 = 23.196.993
--     Descuento s/rein  concepto 14 = 3.204.950     concepto 36 = 3.204.950
--
-- 3. LA LINEA DE RAPPI LLEVA CUATRO ANOS EN CERO
--
-- La consulta del concepto 3 filtra estacion = 'RAPPI' exacto. En las tiendas
-- la estacion se llama 'RAPPI DOMI-Plat'. Nunca coincide. El ultimo valor real
-- es de 2022; desde 2023 son 2.401 filas en cero. DiDi si funciona, y solo
-- porque su consulta usa like '%DIDI%' en vez de igual.
--
-- 4. LA COLUMNA activo DICE LO CONTRARIO DE LO QUE PASA
--
-- Los quince conceptos que se ejecutan estan en activo = 0. Los veinte que
-- estan en activo = 1 no tienen consulta. Y el DAO hace SELECT * sin filtrar,
-- asi que la columna hoy no hace nada. Quien lea la tabla para entender el
-- sistema concluye exactamente al reves.
--
-- ===========================================================================
-- QUE SE DECIDIO
-- ===========================================================================
--
-- MANDA LA FUENTE CONCILIADA. Cuando la base de la tienda y el archivo del
-- operador no coinciden, vale el archivo: es la plata que de verdad entro.
-- Confirmado con el usuario el 2026-09-29.
--
-- Entonces rentabilidad deja de calcular los siete conceptos que ya calcula un
-- proceso especializado, y se queda con los que solo ella hace: el conteo de
-- domicilios, los egresos de caja por tipo y los desechos.
--
-- NO SE BORRA LA CONSULTA, SE APAGA EL CONCEPTO. El texto SQL queda guardado:
-- sirve para saber que se hacia antes, y el dia que haya que revivir uno no
-- hay que reescribirlo. Lo que decide es activo.
--
-- LO QUE ESTE SCRIPT NO TOCA
--
-- Las filas ya escritas. Los cuatro anos de ceros de Rappi siguen ahi, y esta
-- bien: son lo que paso. El tablero tiene que leer por concepto -34, no 3- y
-- no por nombre, que es lo que haria contar doble.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LO QUE RENTABILIDAD SIGUE CALCULANDO
--
-- Son los que ningun otro proceso hace: cuantos domicilios hubo, los egresos
-- de caja por tipo -que salen de la caja de la tienda y de ningun otro lado- y
-- los desechos.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET activo = 1
 WHERE idgasto_conf IN (
        1,   -- Total domicilios
        5,   -- Pago Muchachos          egreso tipo 1, domiciliarios externos
        6,   -- Pago Postobon           egreso tipo 9
        7,   -- Pago alimentos          egreso tipo 4
        8,   -- Pago Vigilancia         egreso tipo 11
        9,   -- Pago Domiciliarios fijos egreso tipo 2
        10,  -- Propina datafonos       egreso tipo 3
        11,  -- Gastos insumos papeleria egreso tipo 6
        15   -- Desechos
       )
   AND consulta_sql <> 'NA'
   AND activo <> 1;

-- ===========================================================================
-- 2. LO QUE RENTABILIDAD DEJA DE CALCULAR
--
-- Cada uno tiene su reemplazo, que es la fuente conciliada.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET activo = 0
 WHERE idgasto_conf IN (
        2,   -- Pago Virtual            lo calcula el 26, ReporteConciliacionWompi
        3,   -- Pedidos Rappi           lo calcula el 34, ReporteSemanalRappi
             --                         ADEMAS esta consulta da cero desde 2023
        4,   -- Pagos PAYU              lo calcula el 32, ReporteConciliacionPAYU
        12,  -- Pago Datafono           lo calcula el 24, ReporteConciliacionWompi
        13,  -- Descuento con Reintegro lo calcula el 35
        14,  -- Descuento sin Reintegro lo calcula el 36
        21   -- Pedidos DIDI            lo calcula el 33, ReporteSemanalDIDI
       )
   AND activo <> 0;

-- ===========================================================================
-- 3. LOS QUE NO SON ASUNTO DE RENTABILIDAD
--
-- Los escribe otro proceso con el id quemado. Estaban en activo = 1, lo que
-- hacia pensar que esta tabla los gobierna. No los gobierna.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET activo = 0
 WHERE consulta_sql = 'NA'
   AND activo <> 0;

-- ===========================================================================
-- 4. EL 5% DEL DATAFONO ERA LETRA MUERTA
--
-- El concepto 24 tenia porcentaje_gasto = 5, pero quien lo escribe es
-- ReporteConciliacionWompi, que ni siquiera lee esta tabla: guarda valor_gasto
-- igual a valor_calculo. Ese 5% nunca se aplico.
--
-- Se pone en cero para que la tabla deje de prometer un calculo que no ocurre.
-- Si la comision del datafono hay que calcularla, se hace donde se escribe el
-- concepto, no aqui.
-- ===========================================================================

UPDATE inventarioamericana.gasto_configuracion
   SET porcentaje_gasto = 0
 WHERE idgasto_conf = 24
   AND porcentaje_gasto <> 0;

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT 'lo que rentabilidad va a calcular' AS que, COUNT(*) AS conceptos,
       GROUP_CONCAT(idgasto_conf ORDER BY idgasto_conf) AS ids
  FROM inventarioamericana.gasto_configuracion WHERE activo = 1
UNION ALL
SELECT 'apagados', COUNT(*), GROUP_CONCAT(idgasto_conf ORDER BY idgasto_conf)
  FROM inventarioamericana.gasto_configuracion WHERE activo = 0;

SELECT idgasto_conf, nombre_gasto, origen, porcentaje_gasto, activo,
       IF(consulta_sql = 'NA', 'sin consulta', 'con consulta') AS sql_
  FROM inventarioamericana.gasto_configuracion
 ORDER BY activo DESC, idgasto_conf;

-- Ningun concepto activo puede quedar sin consulta, y ninguno apagado con
-- consulta deberia ser de los que solo hace rentabilidad. Las dos cuentas
-- tienen que dar cero.
SELECT SUM(activo = 1 AND consulta_sql = 'NA') AS activos_sin_consulta,
       SUM(activo = 1 AND idgasto_conf IN (2,3,4,12,13,14,21)) AS duplicados_todavia_activos
  FROM inventarioamericana.gasto_configuracion;

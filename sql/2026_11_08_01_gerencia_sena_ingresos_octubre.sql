-- ===========================================================================
-- MENU GERENCIA: APRENDICES SENA, LIQUIDACION PROPORCIONAL DE INGRESOS (OCTUBRE)
-- Y PARAMETROS DE LIQUIDACION
--
-- Fecha: 2026-11-08
-- Se corre en el CENTRAL (172.19.0.25). Idempotente.
-- Esquemas: general e inventarioamericana.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 1. MARCA ES_SENA Y FECHA_INGRESO EN general.empleado
--
-- es_sena: 1 = Aprendiz/practicante SENA. Cotiza seguridad social al 15% sobre
--          el basico y 0% en liquidacion de prestaciones.
-- fecha_ingreso: Permite liquidar dias proporcionales si ingresa durante el mes
--                (como Marina y Hermes el 28 de octubre -> 3 dias).
-- ---------------------------------------------------------------------------
SET @col_sena := (SELECT COUNT(*) FROM information_schema.columns
                   WHERE table_schema = 'general'
                     AND table_name = 'empleado'
                     AND column_name = 'es_sena');
SET @sql_sena := IF(@col_sena = 0,
  'ALTER TABLE general.empleado ADD COLUMN es_sena TINYINT(1) NOT NULL DEFAULT 0 AFTER supernumerario',
  'DO 0');
PREPARE p1 FROM @sql_sena; EXECUTE p1; DEALLOCATE PREPARE p1;

SET @col_ingreso := (SELECT COUNT(*) FROM information_schema.columns
                      WHERE table_schema = 'general'
                        AND table_name = 'empleado'
                        AND column_name = 'fecha_ingreso');
SET @sql_ingreso := IF(@col_ingreso = 0,
  'ALTER TABLE general.empleado ADD COLUMN fecha_ingreso DATE NULL AFTER salario',
  'DO 0');
PREPARE p2 FROM @sql_ingreso; EXECUTE p2; DEALLOCATE PREPARE p2;

-- ---------------------------------------------------------------------------
-- 2. ROL 'Aprendiz SENA' EN general.tipo_empleado Y gerencia_nomina_cargo
-- ---------------------------------------------------------------------------
INSERT INTO general.tipo_empleado (idtipoempleado, descripcion, es_administrador)
VALUES (38, 'Aprendiz SENA', 0)
ON DUPLICATE KEY UPDATE descripcion = 'Aprendiz SENA';

INSERT INTO inventarioamericana.gerencia_nomina_cargo
  (idtipoempleado, nombre, arl_porcentaje, entra_tienda, calcula, observacion, usuario, area)
VALUES
  (38, 'Aprendiz SENA', 0.522, 'N', 'S',
   'Aprendiz SENA etapa lectiva o practica. Nomina administrativa, 15% SS, 0% prestaciones',
   'gerencia', 'ADMINISTRATIVA')
ON DUPLICATE KEY UPDATE
  area = 'ADMINISTRATIVA',
  entra_tienda = 'N',
  observacion = VALUES(observacion);

-- Asegurar que los demas cargos afines de practicantes tambien apunten a ADMINISTRATIVA
UPDATE inventarioamericana.gerencia_nomina_cargo
   SET area = 'ADMINISTRATIVA'
 WHERE idtipoempleado IN (22, 28, 30, 31) AND area <> 'ADMINISTRATIVA';

-- ---------------------------------------------------------------------------
-- 3. PARAMETROS DE LIQUIDACION PARA APRENDICES SENA
--
-- Seguridad social: 15% (lo definio gerencia para aprendices que no laboran en planta)
-- Liquidacion: 0% (no tienen prestaciones sociales de ley)
-- ---------------------------------------------------------------------------
INSERT INTO inventarioamericana.gerencia_nomina_parametro
  (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
SELECT 'SEGURIDAD_SOCIAL_SENA', 'Seguridad social aprendices SENA', 15, 'PORCENTAJE',
       '2026-01-01', NULL, '15% sobre sueldo basico definido por gerencia', 'carga_sena'
 WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_nomina_parametro
                    WHERE codigo = 'SEGURIDAD_SOCIAL_SENA');

INSERT INTO inventarioamericana.gerencia_nomina_parametro
  (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
SELECT 'LIQUIDACION_SENA', 'Liquidacion mensual aprendices SENA', 0, 'PORCENTAJE',
       '2026-01-01', NULL, '0% prestaciones sociales definido por gerencia', 'carga_sena'
 WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_nomina_parametro
                    WHERE codigo = 'LIQUIDACION_SENA');

-- ---------------------------------------------------------------------------
-- 4. CASO MARINA Y HERMES: REGISTRO DE FECHA DE INGRESO (28 DE OCTUBRE)
--
-- Marina Elena Henao Hernandez (id: 806)
-- Hermes Arley Rojas Peralta    (id: 807)
-- ---------------------------------------------------------------------------
UPDATE general.empleado
   SET fecha_ingreso = '2026-10-28'
 WHERE id IN (806, 807);

-- ---------------------------------------------------------------------------
-- 5. CREACION Y MARCA DE LOS APRENDICES SENA DEL EXCEL DE OCTUBRE
--
-- 1. Juan Andres Muñoz Acevedo (id: 770 ya existe) -> marcar es_sena = 1
-- 2. Kelly Johana Arredondo Gaviria (cedula 1034917545)
-- 3. Sofia Valderrama Guisao (cedula 1020428901)
-- 4. Angel Eduardo Quiñonez (cedula 90000001 si no tiene)
-- 5. Isabella Moreno Calle (cedula 90000002 si no tiene)
-- ---------------------------------------------------------------------------

-- Actualizar Juan Andres Muñoz
UPDATE general.empleado
   SET es_sena = 1
 WHERE id = 770;

-- Kelly Johana Arredondo Gaviria
INSERT INTO general.empleado
  (nombre, password, nombre_largo, administrador, idtipoempleado, idtipoempleado2,
   tipoinicio, claverapida, es_empleado, activo, autorizo_redes, email,
   rotacion, salario, idgrupo, medio_tiempo, supernumerario, es_sena,
   idtienda_reportes, excepcion_horario_pv)
SELECT '1034917545', '1034917545', 'Kelly Johana Arredondo Gaviria', 'N',
       38, 0, 'NORMAL', '7545', 1, 1, 0, 'kelly.arredondo@sena.edu.co',
       0, 1751000, 0, 'N', 0, 1, 0, 'N'
 WHERE NOT EXISTS (SELECT 1 FROM general.empleado WHERE nombre = '1034917545');

-- Sofia Valderrama Guisao
INSERT INTO general.empleado
  (nombre, password, nombre_largo, administrador, idtipoempleado, idtipoempleado2,
   tipoinicio, claverapida, es_empleado, activo, autorizo_redes, email,
   rotacion, salario, idgrupo, medio_tiempo, supernumerario, es_sena,
   idtienda_reportes, excepcion_horario_pv)
SELECT '1020428901', '1020428901', 'Sofia Valderrama Guisao', 'N',
       38, 0, 'NORMAL', '8901', 1, 1, 0, 'sofia.valderrama@sena.edu.co',
       0, 1751000, 0, 'N', 0, 1, 0, 'N'
 WHERE NOT EXISTS (SELECT 1 FROM general.empleado WHERE nombre = '1020428901');

-- Angel Eduardo Quiñonez
INSERT INTO general.empleado
  (nombre, password, nombre_largo, administrador, idtipoempleado, idtipoempleado2,
   tipoinicio, claverapida, es_empleado, activo, autorizo_redes, email,
   rotacion, salario, idgrupo, medio_tiempo, supernumerario, es_sena,
   idtienda_reportes, excepcion_horario_pv)
SELECT '90000001', '90000001', 'Angel Eduardo Quiñonez', 'N',
       38, 0, 'NORMAL', '0001', 1, 1, 0, 'angel.quinonez@sena.edu.co',
       0, 1751000, 0, 'N', 0, 1, 0, 'N'
 WHERE NOT EXISTS (SELECT 1 FROM general.empleado WHERE nombre_largo LIKE '%Angel Eduardo Qui%onez%');

-- Isabella Moreno Calle
INSERT INTO general.empleado
  (nombre, password, nombre_largo, administrador, idtipoempleado, idtipoempleado2,
   tipoinicio, claverapida, es_empleado, activo, autorizo_redes, email,
   rotacion, salario, idgrupo, medio_tiempo, supernumerario, es_sena,
   idtienda_reportes, excepcion_horario_pv)
SELECT '90000002', '90000002', 'Isabella Moreno Calle', 'N',
       38, 0, 'NORMAL', '0002', 1, 1, 0, 'isabella.moreno@sena.edu.co',
       0, 1751000, 0, 'N', 0, 1, 0, 'N'
 WHERE NOT EXISTS (SELECT 1 FROM general.empleado WHERE nombre_largo LIKE '%Isabella Moreno Calle%');

-- ---------------------------------------------------------------------------
-- 6. ASIGNACION EN gerencia_persona_area (AREA = 'ADMINISTRATIVA')
-- ---------------------------------------------------------------------------
INSERT INTO inventarioamericana.gerencia_persona_area (id, area, observacion, usuario)
SELECT e.id, 'ADMINISTRATIVA', 'Aprendiz SENA - Nomina Administrativa', 'carga_sena'
  FROM general.empleado e
 WHERE e.es_sena = 1
ON DUPLICATE KEY UPDATE area = 'ADMINISTRATIVA', observacion = VALUES(observacion);

-- ---------------------------------------------------------------------------
-- 7. REVISION FINAL
-- ---------------------------------------------------------------------------
SELECT id, nombre, nombre_largo, salario, fecha_ingreso, es_sena+0 AS sena, activo+0 AS act
  FROM general.empleado
 WHERE id IN (770, 806, 807)
    OR es_sena = 1;

-- =====================================================================
-- Gerencia: las areas de estructura, y la pestana ADMINISTRATIVA
--
-- 2026-10-23
--
-- QUE SE QUIERE
--
-- Reemplazar el Excel GASTOS_FIJOS_TIENDAS por una pantalla con las
-- mismas pestanas: ADMINISTRATIVA, LOGISTICA Y PRODUCCION, CONTACT
-- CENTER, cada tienda, PUBLICIDAD y PORCENTAJE. Se conservan los
-- CONCEPTOS del Excel; lo que se reconstruye es de donde sale cada uno.
--
-- Esta entrega arma la primera: ADMINISTRATIVA.
--
-- NO SE TOCA NADA DEL MUNDO VIEJO
--
-- gasto_configuracion y gasto_semanal se leen y nunca se escriben. Todo
-- lo de aqui son tablas y columnas nuevas, en el espacio de Gerencia.
-- Es el mismo patron de rentabilidad_calcula, que funciono justamente
-- porque ningun proceso viejo la conoce.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. A que area pertenece cada cargo.
--
--    La columna va en gerencia_nomina_cargo, que ya existe y ya es de
--    Gerencia, en vez de una tabla aparte: el cargo ya esta ahi con su
--    ARL y su bandera de tienda, y partirlo en dos tablas obliga a
--    acordarse de las dos cada vez que entra un cargo nuevo.
--
--    TIENDA son los cinco cargos de punto de venta que indico gerencia:
--    administrador, auxiliar administrador, pizzero, domiciliario y
--    servicio al cliente. Esos NO entran en ninguna pestana de
--    estructura; su nomina va en la pestana de su tienda.
-- ---------------------------------------------------------------------
SET @hay := (SELECT COUNT(*) FROM information_schema.columns
              WHERE table_schema = 'inventarioamericana'
                AND table_name = 'gerencia_nomina_cargo'
                AND column_name = 'area');
SET @sql := IF(@hay = 0,
  'ALTER TABLE inventarioamericana.gerencia_nomina_cargo ADD COLUMN area VARCHAR(20) NOT NULL DEFAULT "SIN DEFINIR"',
  'DO 0');
PREPARE p FROM @sql; EXECUTE p; DEALLOCATE PREPARE p;

-- Punto de venta: los cinco que nombro gerencia.
UPDATE inventarioamericana.gerencia_nomina_cargo
   SET area = 'TIENDA'
 WHERE idtipoempleado IN (1,2,3,4,5) AND area = 'SIN DEFINIR';

-- Contact center.
UPDATE inventarioamericana.gerencia_nomina_cargo
   SET area = 'CONTACT'
 WHERE idtipoempleado IN (7,18) AND area = 'SIN DEFINIR';

-- Logistica y produccion: bodega, produccion, calidad en planta,
-- conductor y el lider del area.
UPDATE inventarioamericana.gerencia_nomina_cargo
   SET area = 'LOGISTICA'
 WHERE idtipoempleado IN (8,9,10,12,14,33) AND area = 'SIN DEFINIR';

-- Todo lo demas es administrativa. Se escribe explicito y no como
-- "lo que sobra", para que un cargo nuevo caiga en SIN DEFINIR y se
-- vea, en vez de entrar callado a administrativa.
UPDATE inventarioamericana.gerencia_nomina_cargo
   SET area = 'ADMINISTRATIVA'
 WHERE idtipoempleado IN (6,11,13,15,16,17,19,20,21,22,23,24,25,26,27,28,29,30,31,32,34,35)
   AND area = 'SIN DEFINIR';

-- ---------------------------------------------------------------------
-- 2. Los dos porcentajes de la nomina de estructura.
--
--    Gerencia los dio asi: seguridad social 25% y liquidacion 22%, los
--    dos sobre el sueldo variable. Van como parametro con vigencia, no
--    quemados, porque son los que mas se mueven.
--
--    OJO: en el Excel de julio hay cuatro personas marcadas (ELEC) y
--    (PRACT) que llevan 15% del BASICO en seguridad social, y cero o
--    22% en liquidacion. Aprendices y practicantes cotizan distinto.
--    Queda pendiente definirlo; por ahora a todos se les aplica 25/22.
-- ---------------------------------------------------------------------
INSERT INTO inventarioamericana.gerencia_nomina_parametro
      (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
SELECT 'SEGURIDAD_SOCIAL_ESTRUCTURA', 'Seguridad social sobre el sueldo variable',
       25, 'PORCENTAJE', '2026-01-01', NULL,
       'Lo definio gerencia el 2026-10-23. Aprendices y practicantes cotizan 15% del basico: pendiente.',
       'carga_areas'
 WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_nomina_parametro
                    WHERE codigo = 'SEGURIDAD_SOCIAL_ESTRUCTURA');

INSERT INTO inventarioamericana.gerencia_nomina_parametro
      (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
SELECT 'LIQUIDACION_ESTRUCTURA', 'Liquidacion mensual sobre el sueldo variable',
       22, 'PORCENTAJE', '2026-01-01', NULL,
       'Lo definio gerencia el 2026-10-23.',
       'carga_areas'
 WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_nomina_parametro
                    WHERE codigo = 'LIQUIDACION_ESTRUCTURA');

-- ---------------------------------------------------------------------
-- 3. Los conceptos de gasto saben a quien aplican.
--
--    Gerencia lo dijo asi: "hay gastos que hay que decir si son de
--    tienda o administrativos", y "fumigacion es para todos".
--
--    AMBOS no significa que se reparta: significa que ese concepto
--    existe tanto en las tiendas como en las areas, y cada una carga su
--    propio valor. En el Excel la fumigacion aparece con 150.000 en
--    ADMINISTRATIVA y otros 150.000 en MANRIQUE.
-- ---------------------------------------------------------------------
SET @hay := (SELECT COUNT(*) FROM information_schema.columns
              WHERE table_schema = 'inventarioamericana'
                AND table_name = 'gerencia_concepto_gasto'
                AND column_name = 'ambito');
SET @sql := IF(@hay = 0,
  'ALTER TABLE inventarioamericana.gerencia_concepto_gasto ADD COLUMN ambito VARCHAR(10) NOT NULL DEFAULT "TIENDA"',
  'DO 0');
PREPARE p FROM @sql; EXECUTE p; DEALLOCATE PREPARE p;

-- Los que en el Excel aparecen en los dos lados.
UPDATE inventarioamericana.gerencia_concepto_gasto
   SET ambito = 'AMBOS'
 WHERE idconcepto IN (1,2,3,4,5);

-- Los conceptos que solo existen en las areas y que el Excel trae en la
-- hoja ADMINISTRATIVA. Van con idconcepto alto para no chocar con los
-- que ya estan.
INSERT INTO inventarioamericana.gerencia_concepto_gasto
      (idconcepto, nombre, tipo, linea, orden, activo, ambito)
VALUES
  (50, 'Gasolina',                       'FIJO',     'CUOTA ADMINISTRATIVA', 200, 'S', 'AREA'),
  (51, 'Seguridad e impuesto seguridad', 'SERVICIO', 'CUOTA ADMINISTRATIVA', 210, 'S', 'AREA'),
  (52, 'Celulares',                      'FIJO',     'CUOTA ADMINISTRATIVA', 220, 'S', 'AREA'),
  (53, 'Arriendo pieza de archivo',      'FIJO',     'CUOTA ADMINISTRATIVA', 230, 'S', 'AREA'),
  (54, 'Psicologa',                      'FIJO',     'CUOTA ADMINISTRATIVA', 240, 'S', 'AREA'),
  (55, 'Planillas',                      'FIJO',     'CUOTA ADMINISTRATIVA', 250, 'S', 'AREA'),
  (56, 'Prestacion de servicios',        'FIJO',     'CUOTA ADMINISTRATIVA', 260, 'S', 'AREA'),
  (57, 'Transporte',                     'FIJO',     'CUOTA ADMINISTRATIVA', 270, 'S', 'AREA'),
  (58, 'Seguro de vida empleados',       'FIJO',     'CUOTA ADMINISTRATIVA', 280, 'S', 'AREA')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), ambito = VALUES(ambito),
                        linea = VALUES(linea), orden = VALUES(orden);

-- ---------------------------------------------------------------------
-- 4. Los gastos fijos de un area.
--
--    Misma forma que gerencia_gasto_fijo_tienda -valor mensual con
--    vigencia- pero la llave es el area y no la tienda. Un arriendo
--    vale lo que dice el contrato hasta que alguien lo cambie; no se
--    vuelve a teclear cada mes ni se estima con promedios.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_gasto_area (
  idgasto INT NOT NULL AUTO_INCREMENT,
  area VARCHAR(20) NOT NULL,
  idconcepto INT NOT NULL,
  valor_mensual DECIMAL(14,2) NOT NULL DEFAULT 0,
  vigencia_desde DATE NOT NULL,
  vigencia_hasta DATE NULL,
  observacion VARCHAR(200) NULL,
  usuario VARCHAR(100) NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idgasto),
  UNIQUE KEY uk_area_concepto_desde (area, idconcepto, vigencia_desde),
  KEY ix_area (area, vigencia_desde)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. Los gastos de ADMINISTRATIVA de julio 2026, del Excel.
--
--    Suman 27.620.700, que es el total de la hoja (133.797.880) menos
--    la nomina (106.177.180). Verificado.
-- ---------------------------------------------------------------------
INSERT INTO inventarioamericana.gerencia_gasto_area
      (area, idconcepto, valor_mensual, vigencia_desde, observacion, usuario)
VALUES
  ('ADMINISTRATIVA', 50,  1100000.00, '2026-07-01', 'Excel julio 2026: GASOLINA',                     'carga_areas'),
  ('ADMINISTRATIVA', 51, 11611700.00, '2026-07-01', 'Excel julio 2026: SERVICIOS E IMPU SEGURID',     'carga_areas'),
  ('ADMINISTRATIVA',  1,  3300000.00, '2026-07-01', 'Excel julio 2026: ARRIENDO',                     'carga_areas'),
  ('ADMINISTRATIVA',  3,   150000.00, '2026-07-01', 'Excel julio 2026: FUMIGACION',                   'carga_areas'),
  ('ADMINISTRATIVA',  4,   200000.00, '2026-07-01', 'Excel julio 2026: ALARMAR',                      'carga_areas'),
  ('ADMINISTRATIVA',  5,   200000.00, '2026-07-01', 'Excel julio 2026: SEGURO TIENDA',                'carga_areas'),
  ('ADMINISTRATIVA', 52,   645000.00, '2026-07-01', 'Excel julio 2026: CELULARES',                    'carga_areas'),
  ('ADMINISTRATIVA', 53,   600000.00, '2026-07-01', 'Excel julio 2026: ARRIENDO PIEZA ARCHIVO',       'carga_areas'),
  ('ADMINISTRATIVA', 16,  1500000.00, '2026-07-01', 'Excel julio 2026: SEGURIDAD ACROPOLIS',          'carga_areas'),
  ('ADMINISTRATIVA', 54,  1600000.00, '2026-07-01', 'Excel julio 2026: ADRIANA SICOLOGA',             'carga_areas'),
  ('ADMINISTRATIVA', 55,  1000000.00, '2026-07-01', 'Excel julio 2026: BEATRIZ (PLANILLAS)',          'carga_areas'),
  ('ADMINISTRATIVA', 56,  2800000.00, '2026-07-01', 'Excel julio 2026: ANDRES, prestacion servicios', 'carga_areas'),
  ('ADMINISTRATIVA', 57,  2090000.00, '2026-07-01', 'Excel julio 2026: TRANSPORTE',                   'carga_areas'),
  ('ADMINISTRATIVA', 58,   824000.00, '2026-07-01', 'Excel julio 2026: SEGURO VIDA EMPLEADOS',        'carga_areas')
ON DUPLICATE KEY UPDATE valor_mensual = VALUES(valor_mensual),
                        observacion = VALUES(observacion);

-- ---------------------------------------------------------------------
-- 6. Revision
-- ---------------------------------------------------------------------
SELECT area, COUNT(*) AS cargos
  FROM inventarioamericana.gerencia_nomina_cargo
 GROUP BY area ORDER BY area;

SELECT c.nombre AS concepto, g.valor_mensual
  FROM inventarioamericana.gerencia_gasto_area g
  JOIN inventarioamericana.gerencia_concepto_gasto c ON c.idconcepto = g.idconcepto
 WHERE g.area = 'ADMINISTRATIVA'
 ORDER BY c.orden;

SELECT 'gastos de administrativa' AS revision,
       ROUND(SUM(valor_mensual)) AS total,
       'el Excel dice 27.620.700' AS contra
  FROM inventarioamericana.gerencia_gasto_area
 WHERE area = 'ADMINISTRATIVA';

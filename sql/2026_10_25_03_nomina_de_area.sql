-- ---------------------------------------------------------------------------
-- LA NOMINA DE LAS AREAS: SALARIOS, EXCEPCIONES POR PERSONA Y EL VARIABLE
--
-- Se corre en el CENTRAL (172.19.0.25). Idempotente.
--
-- ===========================================================================
-- 1. DOS SALARIOS DESACTUALIZADOS
--
-- Emparejando los 22 sueldos de la pestana ADMINISTRATIVA del libro contra los
-- del sistema, dos no coincidian. Confirmados por Juan David:
--
--   Alexander Vigoya Murcia  gerente general   5.170.000 -> 8.500.000
--   Juan David Botero Duque                    5.170.000 -> 7.000.000
--
-- No se les toca el CARGO. El de Alexander es "Superusuario" (rol 6), que esta
-- en tipo_empleado.es_administrador y de ahi salen sus permisos en el POS;
-- cambiarselo por "Gerente" le moveria el acceso sin que nadie lo pida. El
-- area no cambia: los dos roles son ADMINISTRATIVA.
-- ===========================================================================
UPDATE general.empleado SET salario = 8500000 WHERE id = 303 AND salario <> 8500000;
UPDATE general.empleado SET salario = 7000000 WHERE id = 135 AND salario <> 7000000;

-- ===========================================================================
-- 2. LAS EXCEPCIONES POR PERSONA
--
-- POR QUE NO SE USA idtipoempleado2, QUE ERA LO PEDIDO
--
-- La idea era marcar en idtipoempleado2 que Vanessa y Mayra son supernumerarias
-- y que el reporte leyera de ahi. Ese campo YA TIENE DUENIO y escribirle encima
-- rompe cosas que estan andando:
--
--   - UsuarioDAO.tipoEmpleadoParaPOS lo usa para mandarle a la tienda el rol
--     ADMINISTRATIVO de quien ademas apoya como administrador del punto. Vanessa
--     tiene ahi el rol 1, "Auxiliar Administrador Punto de Venta", y de ahi
--     salen sus permisos al entrar al POS. Pisarlo la deja con permisos de
--     artesana: es exactamente el caso de Saray Rojas que ya se corrigio una vez.
--   - EmpleadoEventoDAO cuenta los pizzeros del dia por idtipoempleado O
--     idtipoempleado2.
--   - DesempenoDomiciliarioDAO mira los dos roles.
--
-- Es la misma leccion del 2026-09-29 con la columna `activo`: lo nuevo no se
-- marca en una columna que lo viejo ya usa. Por eso la marca va en una tabla
-- propia del modulo de Gerencia, que nadie mas lee.
--
-- Igual pasa con SuperUsuario. En el POS, es_empleado = 0 significa "es
-- temporal" y lo manda por el camino de ingreso de temporales; apagarselo le
-- tumbaria el login. Se excluye aqui, no alla.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS gerencia_persona_area (
  id          INT         NOT NULL COMMENT 'general.empleado.id',
  -- ADMINISTRATIVA, LOGISTICA, CONTACT, TIENDA, o NINGUNA para que no cuente.
  area        VARCHAR(20) NOT NULL,
  observacion VARCHAR(200) NULL,
  usuario     VARCHAR(50)  NULL,
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Donde cuenta una persona cuando su cargo dice otra cosa. Solo la lee Gerencia';

INSERT INTO gerencia_persona_area (id, area, observacion, usuario) VALUES
  (209, 'ADMINISTRATIVA',
   'Supernumeraria: su cargo es Artesano pero hoy apoya administrativa. Asi la cuenta el Excel',
   'carga_areas'),
  (631, 'ADMINISTRATIVA',
   'Supernumeraria: su cargo es Administrador Punto de Venta pero hoy apoya administrativa',
   'carga_areas'),
  (182, 'NINGUNA',
   'Cuenta tecnica del sistema, no es de nomina. No se le toca es_empleado porque en el POS un 0 significa temporal',
   'carga_areas')
ON DUPLICATE KEY UPDATE area = VALUES(area), observacion = VALUES(observacion);

-- ===========================================================================
-- 3. LA NOMINA DEL AREA, POR MES
--
-- El total que el libro reporta como "NOMINA (n)" de cada area es la columna
-- SUELDO VARIABLE, no la basica: lo que de verdad se pago, con recargos y
-- extras. Comprobado exacto en logistica (22.425.000) y contact (26.652.000).
--
-- El sistema no tiene de donde sacar el variable: GerenciaCierreDAO.cargarPersonas
-- hace p.variable = p.basico y lo dice en un comentario. Entre el 6% y el 33%
-- de la nomina de un area se pierde por ahi.
--
-- Asi que se digita, igual que ya se digitan los servicios y la nomina de las
-- tiendas. La misma forma de gerencia_nomina_tienda, para que las dos se lean
-- igual. Cuando el mes tiene fila, el cierre usa ESTA; cuando no, sigue con lo
-- que calcula desde general.empleado y lo dice en pantalla.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS gerencia_nomina_area (
  area             VARCHAR(20)    NOT NULL,
  anio             SMALLINT       NOT NULL,
  mes              TINYINT        NOT NULL,
  sueldo_basico    DECIMAL(14,2)  NOT NULL DEFAULT 0,
  sueldo_variable  DECIMAL(14,2)  NOT NULL DEFAULT 0,
  seguridad_social DECIMAL(14,2)  NOT NULL DEFAULT 0,
  liquidacion      DECIMAL(14,2)  NOT NULL DEFAULT 0,
  personas         SMALLINT       NOT NULL DEFAULT 0,
  observacion      VARCHAR(200)   NULL,
  usuario          VARCHAR(50)    NULL,
  fecha_registro   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (area, anio, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Julio de 2026, que es el mes del libro. Las cifras son las que la hoja misma
-- totaliza en cada pestana.
INSERT INTO gerencia_nomina_area
       (area, anio, mes, sueldo_basico, sueldo_variable, seguridad_social, liquidacion,
        personas, observacion, usuario) VALUES
  ('ADMINISTRATIVA', 2026, 7, 67502000, 72344000, 18254100, 15579080, 22,
   'Excel GASTOS_FIJOS_TIENDAS_JULIO, pestana ADMINISTRATIVA', 'carga_areas'),
  ('LOGISTICA',      2026, 7, 20257000, 22425000,  5827000,  5127760, 12,
   'Excel GASTOS_FIJOS_TIENDAS_JULIO, pestana LOGISTICA Y PRODUCCION', 'carga_areas'),
  ('CONTACT',        2026, 7, 19962000, 26652000,  6952500,  5884780, 14,
   'Excel GASTOS_FIJOS_TIENDAS_JULIO, pestana CONTAC CENTER', 'carga_areas')
ON DUPLICATE KEY UPDATE
  sueldo_basico = VALUES(sueldo_basico), sueldo_variable = VALUES(sueldo_variable),
  seguridad_social = VALUES(seguridad_social), liquidacion = VALUES(liquidacion),
  personas = VALUES(personas), observacion = VALUES(observacion);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================
SELECT IFNULL(x.area, c.area) AS area, COUNT(*) AS personas,
       ROUND(SUM(e.salario)) AS basico_calculado
  FROM general.empleado e
  JOIN gerencia_nomina_cargo c ON c.idtipoempleado = e.idtipoempleado
  LEFT JOIN gerencia_persona_area x ON x.id = e.id
 WHERE e.activo = 1 AND e.es_empleado = 1
   AND IFNULL(x.area, c.area) NOT IN ('TIENDA', 'NINGUNA')
 GROUP BY IFNULL(x.area, c.area);

SELECT area, anio, mes, sueldo_basico, sueldo_variable FROM gerencia_nomina_area ORDER BY area;

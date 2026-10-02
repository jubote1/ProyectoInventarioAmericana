-- ---------------------------------------------------------------------------
-- NOMINA POR EMPLEADO, REPARTIDA ENTRE TIENDAS SEGUN BIOMETRIA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente: correrlo dos veces no cambia nada la segunda.
--
-- Reemplaza, para las hojas compartidas del Excel (ADMINISTRATIVA, LOGISTICA
-- Y PRODUCCION, CONTAC CENTER), el reparto parejo de hoy (Contac Center
-- literalmente /6) por un reparto real: cuantos minutos marco cada empleado
-- en cada tienda esa semana, segun general.empleado_evento.
--
-- Se aplica a TODOS los empleados, no solo a los compartidos: uno de tienda
-- propia deberia salir ~100% en su tienda por biometria, y eso sirve de
-- chequeo cruzado gratis contra lo que ya esta en gerencia_nomina_tienda.
--
-- gerencia_nomina_empleado_semana   lo que se carga: el costo del empleado esa
--                                    semana (basico/variable/seg.social/
--                                    liquidacion), a mano hoy, con el campo
--                                    origen listo para cuando exista una
--                                    integracion.
-- gerencia_nomina_empleado_tienda_semana   lo que se calcula: el reparto por
--                                    tienda, con los minutos y el porcentaje
--                                    de respaldo -para poder auditar por que
--                                    un empleado quedo repartido asi-.
--
-- Ver capaDAOINV.GerenciaNominaEmpleadoDAO para el detalle del emparejamiento
-- de biometria y por que un empleado sin marcaciones esa semana no genera
-- ninguna fila aqui (no se inventa un reparto).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS gerencia_nomina_empleado_semana (
  idnomina INT AUTO_INCREMENT PRIMARY KEY,
  idempleado INT NOT NULL COMMENT 'general.empleado.id',
  semana DATE NOT NULL COMMENT 'domingo de cierre, igual que gerencia_semana.fecha_fin',
  sueldo_basico DOUBLE NOT NULL DEFAULT 0,
  sueldo_variable DOUBLE NOT NULL DEFAULT 0,
  seguridad_social DOUBLE NOT NULL DEFAULT 0,
  liquidacion DOUBLE NOT NULL DEFAULT 0,
  origen VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL hoy; API cuando exista la integracion',
  usuario VARCHAR(50),
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_nomina_empleado_semana (idempleado, semana)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gerencia_nomina_empleado_tienda_semana (
  idempleado INT NOT NULL,
  semana DATE NOT NULL,
  idtienda INT NOT NULL,
  minutos INT NOT NULL DEFAULT 0 COMMENT 'minutos marcados en esa tienda esa semana, para auditar',
  porcentaje DOUBLE NOT NULL DEFAULT 0 COMMENT 'minutos de esta tienda / minutos totales del empleado esa semana',
  sueldo_basico DOUBLE NOT NULL DEFAULT 0 COMMENT 'ya multiplicado por porcentaje',
  sueldo_variable DOUBLE NOT NULL DEFAULT 0,
  seguridad_social DOUBLE NOT NULL DEFAULT 0,
  liquidacion DOUBLE NOT NULL DEFAULT 0,
  fecha_calculo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idempleado, semana, idtienda)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================
SELECT TABLE_NAME, TABLE_ROWS
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'inventarioamericana'
   AND TABLE_NAME IN ('gerencia_nomina_empleado_semana', 'gerencia_nomina_empleado_tienda_semana');

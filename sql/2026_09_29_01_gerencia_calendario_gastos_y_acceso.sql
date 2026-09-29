-- ---------------------------------------------------------------------------
-- MENU GERENCIA: el calendario de semanas, los gastos parametrizables y quien
-- puede verlos.
--
-- Se corre UNA VEZ en el CENTRAL (172.19.0.25). No toca ninguna tienda.
-- Es idempotente: correrlo dos veces no cambia nada la segunda.
--
-- Toca dos esquemas y a proposito:
--   inventarioamericana  las tablas nuevas del menu Gerencia
--   pizzaamericana       el catalogo de seguridad que YA existe (rol, pantalla,
--                        menu_modulo, rol_pantalla). No se crea uno nuevo.
--
-- POR QUE EL CALENDARIO SE GUARDA Y NO SE CALCULA
--
-- Una semana de lunes a domingo casi nunca cabe dentro de un mes. La del 28 de
-- julio al 3 de agosto tiene cuatro dias en julio y tres en agosto. Si cada
-- reporte la asigna por su cuenta, el mismo mes da dos numeros distintos. Se
-- define una vez, queda guardada, y todos los reportes leen de aqui.
--
-- LO QUE NO SE VUELVE A CONSTRUIR
--
-- gasto_configuracion, gasto_semanal y gasto_empleado_temporal ya existen en
-- inventarioamericana y estan vivas: gasto_semanal tiene 42.400 filas de
-- catorce tiendas, desde 2021-04-25 y al dia -la ultima fecha es 2026-09-27-.
-- El proceso semanal ReporteConsolidacionRentabilidad ya baja comisiones,
-- egresos por tipo y desechos. Este script NO las toca. Lo que agrega es lo
-- que ese proceso no puede saber solo: cuanto vale el arriendo, de cuando a
-- cuando va cada semana, y cuanto pesa la estructura.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. EL ANO Y SUS SEMANAS
--
-- gerencia_anio existe para poder CERRAR un ano. Cerrado, el calendario no se
-- edita: un mes que ya se reporto no puede cambiar de tamano por detras, o los
-- historicos dejan de ser comparables entre si.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_anio (
  anio           SMALLINT     NOT NULL,
  estado         VARCHAR(10)  NOT NULL DEFAULT 'ABIERTO' COMMENT 'ABIERTO o CERRADO',
  usuario        VARCHAR(100) NULL,
  fecha_registro DATETIME     NULL,
  PRIMARY KEY (anio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Anos del calendario de gerencia. Cerrado = calendario congelado';

-- fecha_inicio es unica en TODA la tabla, no por ano. Un lunes pertenece a una
-- sola semana: si al generar 2027 su primer lunes ya lo cubre la ultima semana
-- de 2026, el INSERT falla en vez de dejar dos semanas montadas una sobre otra
-- y que despues nadie entienda por que un dia se cuenta dos veces.
CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_semana (
  idsemana     INT      NOT NULL AUTO_INCREMENT,
  anio         SMALLINT NOT NULL,
  numero       SMALLINT NOT NULL COMMENT 'Semana 1..53 dentro del ano',
  fecha_inicio DATE     NOT NULL COMMENT 'Siempre lunes',
  fecha_fin    DATE     NOT NULL COMMENT 'Siempre domingo',
  mes          TINYINT  NOT NULL COMMENT 'Mes al que se le carga la semana, 1..12',
  PRIMARY KEY (idsemana),
  UNIQUE KEY uk_gerencia_semana_anio_numero (anio, numero),
  UNIQUE KEY uk_gerencia_semana_inicio (fecha_inicio),
  KEY idx_gerencia_semana_anio_mes (anio, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='De cuando a cuando va cada semana y a que mes se le carga';

-- ===========================================================================
-- 2. EL CATALOGO DE CONCEPTOS
--
-- Dos naturalezas distintas y por eso la columna tipo:
--
--   FIJO      no cambia mes a mes. Se define con vigencia y se queda quieto.
--   SERVICIO  cambia y la factura llega despues de cerrado el periodo. Mientras
--             no llegue se estima con el promedio de los ultimos meses.
--
-- La columna linea dice a que renglon del tablero suma cada concepto, para que
-- manana se pueda agregar uno nuevo sin tocar codigo.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_concepto_gasto (
  idconcepto INT         NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(80) NOT NULL,
  tipo       VARCHAR(10) NOT NULL COMMENT 'FIJO o SERVICIO',
  linea      VARCHAR(40) NOT NULL COMMENT 'Renglon del tablero al que suma',
  orden      SMALLINT    NOT NULL DEFAULT 0,
  activo     CHAR(1)     NOT NULL DEFAULT 'S',
  PRIMARY KEY (idconcepto),
  UNIQUE KEY uk_gerencia_concepto_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Conceptos de gasto parametrizable por tienda';

-- Los nombres van SIN tildes a proposito, igual que el maestro de insumos de
-- este mismo esquema, para que el script se pueda correr desde cualquier
-- cliente sin depender de como quede codificado el archivo.
INSERT INTO inventarioamericana.gerencia_concepto_gasto (nombre, tipo, linea, orden, activo)
SELECT * FROM (
          SELECT 'Arriendo'                  AS n, 'FIJO'     AS t, 'CUOTA PUNTO DE VENTA' AS l, 10 AS o, 'S' AS a
UNION ALL SELECT 'Servicios publicos',           'SERVICIO', 'CUOTA PUNTO DE VENTA', 20, 'S'
UNION ALL SELECT 'Fumigacion',                   'FIJO',     'CUOTA PUNTO DE VENTA', 30, 'S'
UNION ALL SELECT 'Alarma',                       'FIJO',     'CUOTA PUNTO DE VENTA', 40, 'S'
UNION ALL SELECT 'Seguro tienda',                'FIJO',     'CUOTA PUNTO DE VENTA', 50, 'S'
UNION ALL SELECT 'Contador y revisor fiscal',    'FIJO',     'CUOTA PUNTO DE VENTA', 60, 'S'
UNION ALL SELECT 'Industria y comercio',         'FIJO',     'CUOTA PUNTO DE VENTA', 70, 'S'
UNION ALL SELECT 'Reteica',                      'FIJO',     'CUOTA PUNTO DE VENTA', 80, 'S'
UNION ALL SELECT 'Fiesta de navidad',            'FIJO',     'CUOTA PUNTO DE VENTA', 90, 'S'
UNION ALL SELECT 'Comisariato',                  'FIJO',     'CUOTA PUNTO DE VENTA', 100, 'S'
UNION ALL SELECT 'Datafono',                     'FIJO',     'CUOTA PUNTO DE VENTA', 110, 'S'
UNION ALL SELECT 'Publicista',                   'FIJO',     'CUOTA PUNTO DE VENTA', 120, 'S'
) nuevos
WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.gerencia_concepto_gasto ya
                   WHERE ya.nombre = nuevos.n);

-- ===========================================================================
-- 3. LOS GASTOS FIJOS, CON VIGENCIA
--
-- No se sobrescribe un valor: si el arriendo sube en marzo se le pone
-- vigencia_hasta al registro viejo y se abre uno nuevo. Asi un mes ya
-- reportado sigue mostrando lo que de verdad se pago, y se puede explicar por
-- que subio. Un UPDATE sobre el valor borraria esa historia para siempre.
--
-- vigencia_hasta NULL significa "vigente hoy".
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_gasto_fijo_tienda (
  idgasto        INT           NOT NULL AUTO_INCREMENT,
  idtienda       INT           NOT NULL,
  idconcepto     INT           NOT NULL,
  valor_mensual  DECIMAL(14,2) NOT NULL DEFAULT 0,
  vigencia_desde DATE          NOT NULL,
  vigencia_hasta DATE          NULL COMMENT 'NULL = vigente',
  observacion    VARCHAR(200)  NULL,
  usuario        VARCHAR(100)  NULL,
  fecha_registro DATETIME      NULL,
  PRIMARY KEY (idgasto),
  KEY idx_gerencia_fijo_tienda (idtienda, idconcepto, vigencia_desde),
  KEY idx_gerencia_fijo_vigente (idconcepto, vigencia_hasta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Gasto fijo por tienda y concepto, con historia por vigencia';

-- ===========================================================================
-- 4. LOS SERVICIOS, CON MARCA DE REAL O ESTIMADO
--
-- origen dice si el valor es la factura o un promedio, y meses_promediados
-- dice sobre cuantos meses se saco. Van juntos a proposito: un estimado hecho
-- con un solo mes de historia no vale lo mismo que uno hecho con tres, y quien
-- mira la pantalla tiene derecho a saber la diferencia.
--
-- Nunca se guarda un cero cuando no hay con que estimar. Un cero se suma y
-- ensucia el total sin que nadie se entere; una fila que no existe se ve.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_gasto_servicio (
  idservicio        INT           NOT NULL AUTO_INCREMENT,
  idtienda          INT           NOT NULL,
  idconcepto        INT           NOT NULL,
  anio              SMALLINT      NOT NULL,
  mes               TINYINT       NOT NULL,
  valor             DECIMAL(14,2) NOT NULL DEFAULT 0,
  origen            VARCHAR(10)   NOT NULL DEFAULT 'REAL' COMMENT 'REAL o ESTIMADO',
  meses_promediados TINYINT       NOT NULL DEFAULT 0 COMMENT 'Sobre cuantos meses se estimo',
  usuario           VARCHAR(100)  NULL,
  fecha_registro    DATETIME      NULL,
  PRIMARY KEY (idservicio),
  UNIQUE KEY uk_gerencia_servicio (idtienda, idconcepto, anio, mes),
  KEY idx_gerencia_servicio_periodo (anio, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Servicios publicos por tienda y mes, reales o estimados';

-- ===========================================================================
-- 5. LAS CUATRO BOLSAS DE ESTRUCTURA
--
-- Administrativa, logistica y produccion, contact center y publicidad. Son
-- costos que no pertenecen a ninguna tienda y se reparten entre todas.
--
-- EL REPARTO ES POR PARTICIPACION EN LA VENTA. Se suma la venta de todas las
-- tiendas del periodo y a cada una le toca su porcentaje. Ese calculo NO se
-- guarda aca: se hace al mostrar el tablero, contra la venta real del periodo
-- que se este mirando. Guardar el porcentaje lo volveria a congelar, que es
-- justo el problema que tiene hoy el Excel -sus pesos estan repartidos en
-- puntos enteros (12, 12, 12, 10, 9, 9, 9, 9, 8, 7, 3) que suman 100 y que
-- salieron de una venta vieja: hoy a Manrique le carga 12% cuando vende 10,5%,
-- y a Niquia 3% cuando vende 5,9%-.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_pool_estructura (
  idpool         INT           NOT NULL AUTO_INCREMENT,
  anio           SMALLINT      NOT NULL,
  mes            TINYINT       NOT NULL,
  tipo           VARCHAR(20)   NOT NULL COMMENT 'ADMINISTRATIVA, LOGISTICA, CONTACT o PUBLICIDAD',
  valor          DECIMAL(14,2) NOT NULL DEFAULT 0,
  usuario        VARCHAR(100)  NULL,
  fecha_registro DATETIME      NULL,
  PRIMARY KEY (idpool),
  UNIQUE KEY uk_gerencia_pool (anio, mes, tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Bolsas de costo de estructura por mes, a repartir por venta';

-- ===========================================================================
-- 6. LA NOMINA, AGREGADA POR TIENDA
--
-- A PROPOSITO NO HAY UNA FILA POR PERSONA. El Excel de hoy trae sueldos con
-- nombre y apellido; llevarlos a una tabla consultable por una pantalla web
-- convierte un archivo que vive en un computador en un dato que cualquiera con
-- acceso al menu puede leer. Para el tablero de rentabilidad el total por
-- tienda basta, y lo que no se guarda no se filtra.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_nomina_tienda (
  idnomina         INT           NOT NULL AUTO_INCREMENT,
  idtienda         INT           NOT NULL,
  anio             SMALLINT      NOT NULL,
  mes              TINYINT       NOT NULL,
  empleados        SMALLINT      NOT NULL DEFAULT 0,
  sueldo_basico    DECIMAL(14,2) NOT NULL DEFAULT 0,
  sueldo_variable  DECIMAL(14,2) NOT NULL DEFAULT 0,
  seguridad_social DECIMAL(14,2) NOT NULL DEFAULT 0,
  liquidacion      DECIMAL(14,2) NOT NULL DEFAULT 0,
  usuario          VARCHAR(100)  NULL,
  fecha_registro   DATETIME      NULL,
  PRIMARY KEY (idnomina),
  UNIQUE KEY uk_gerencia_nomina (idtienda, anio, mes),
  KEY idx_gerencia_nomina_periodo (anio, mes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Nomina mensual por tienda, agregada. Nunca persona por persona';

-- ===========================================================================
-- 7. LA BITACORA
--
-- Esta informacion es sensible y hay que poder responder quien la cambio. Las
-- tablas de arriba guardan usuario y fecha del ultimo cambio; la bitacora
-- guarda los anteriores.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS inventarioamericana.gerencia_bitacora (
  idbitacora INT          NOT NULL AUTO_INCREMENT,
  usuario    VARCHAR(100) NOT NULL,
  accion     VARCHAR(40)  NOT NULL,
  detalle    VARCHAR(400) NOT NULL,
  fecha      DATETIME     NOT NULL,
  PRIMARY KEY (idbitacora),
  KEY idx_gerencia_bitacora_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='Quien cambio que en el menu Gerencia';

-- ===========================================================================
-- 8. QUIEN PUEDE ENTRAR
--
-- Se usa el catalogo de seguridad que YA existe en pizzaamericana -rol,
-- menu_modulo, pantalla, rol_pantalla, usuario_rol-, sembrado el 2026-09-15.
-- Hacer un segundo sistema de perfiles solo para esta aplicacion significaria
-- dos listas de personas que hay que acordarse de mantener iguales, y el dia
-- que alguien se va de la empresa se le quita el acceso en una y se le olvida
-- la otra.
--
-- OJO CON ALGO QUE HAY QUE SABER: inventarioamericana.usuario tiene UNA sola
-- fila -el usuario 'admin'-. Toda la aplicacion de inventarios, en las once
-- tiendas, entra con ese mismo usuario compartido. Por eso el menu Gerencia no
-- se apoya en esa sesion: pide identificarse aparte contra pizzaamericana.
-- usuario, que si tiene 127 personas de verdad, y guarda en cada cambio quien
-- lo hizo.
-- ===========================================================================

-- El modulo del menu
INSERT INTO pizzaamericana.menu_modulo (nombre, orden, idmodulo_padre, activo)
SELECT 'Gerencia', 50, NULL, 'S' FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.menu_modulo WHERE nombre = 'Gerencia');

-- El rol
INSERT INTO pizzaamericana.rol (nombre, descripcion, activo)
SELECT 'Gerencia', 'Acceso al menu Gerencia: calendario de semanas, gastos por tienda, estructura y nomina agregada', 'S' FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.rol WHERE nombre = 'Gerencia');

-- Las tres pantallas
INSERT INTO pizzaamericana.pantalla (nombre, idmodulo, url_html, orden, activo)
SELECT nuevas.n, m.idmodulo, nuevas.u, nuevas.o, 'S'
  FROM (          SELECT 'Calendario de Semanas'      AS n, 'CalendarioSemanas.html'  AS u, 10 AS o
        UNION ALL SELECT 'Gastos por Tienda',              'GastosTienda.html',           20
        UNION ALL SELECT 'Estructura y Nomina',            'EstructuraGerencia.html',     30) nuevas
 CROSS JOIN (SELECT idmodulo FROM pizzaamericana.menu_modulo WHERE nombre = 'Gerencia') m
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.pantalla ya WHERE ya.url_html = nuevas.u);

-- El permiso: el rol Gerencia sobre las tres pantallas
INSERT INTO pizzaamericana.rol_pantalla (idrol, idpantalla)
SELECT r.idrol, p.idpantalla
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.pantalla p
    ON p.url_html IN ('CalendarioSemanas.html', 'GastosTienda.html', 'EstructuraGerencia.html')
 WHERE r.nombre = 'Gerencia'
   AND NOT EXISTS (SELECT 1 FROM pizzaamericana.rol_pantalla ya
                    WHERE ya.idrol = r.idrol AND ya.idpantalla = p.idpantalla);

-- A PROPOSITO NO SE LE ASIGNA EL ROL A NADIE.
--
-- Quien entra a ver nomina es una decision de la gerencia, no de un script. Se
-- asigna desde AsignarRolUsuario.html del central, o con:
--
--   INSERT INTO pizzaamericana.usuario_rol (idusuario, idrol)
--   SELECT u.id, r.idrol FROM pizzaamericana.usuario u, pizzaamericana.rol r
--    WHERE u.nombre = 'elusuario' AND r.nombre = 'Gerencia';
--
-- Mientras nadie lo tenga, el menu Gerencia no se le abre a nadie. Eso es lo
-- correcto: falla cerrado.

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT TABLE_NAME AS tabla_creada, TABLE_ROWS AS filas
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'inventarioamericana' AND TABLE_NAME LIKE 'gerencia%'
 ORDER BY TABLE_NAME;

SELECT idconcepto, nombre, tipo, linea
  FROM inventarioamericana.gerencia_concepto_gasto
 ORDER BY orden;

SELECT r.nombre AS rol, p.nombre AS pantalla, p.url_html
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.rol_pantalla rp ON rp.idrol = r.idrol
  JOIN pizzaamericana.pantalla p ON p.idpantalla = rp.idpantalla
 WHERE r.nombre = 'Gerencia'
 ORDER BY p.orden;

SELECT COUNT(*) AS personas_con_rol_gerencia
  FROM pizzaamericana.usuario_rol ur
  JOIN pizzaamericana.rol r ON r.idrol = ur.idrol
 WHERE r.nombre = 'Gerencia';

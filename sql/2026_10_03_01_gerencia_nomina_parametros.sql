-- ---------------------------------------------------------------------------
-- LOS PARAMETROS PARA CALCULAR LA NOMINA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente.
--
-- POR QUE LA LEY VA EN UNA TABLA Y NO EN EL CODIGO
--
-- 2026 es un ano en el que la ley cambia TRES veces, y dos de ellas a mitad de
-- ano:
--
--   * El recargo nocturno arranca a las 19:00 y ya no a las 21:00 (Ley 2466 de
--     2025, art. 10). Medido sobre la biometria de la semana del 21 al 27 de
--     septiembre, eso llevo las horas con recargo del 18,4% al 41,9% del total.
--     No es un detalle: son 42 de cada 100 horas de la compania.
--   * El recargo dominical sube del 80% al 90% el 1 de julio de 2026, y al
--     100% el 1 de julio de 2027.
--   * La jornada baja de 44 a 42 horas semanales el 15 de julio de 2026, y lo
--     que exceda pasa a ser trabajo suplementario.
--
-- Con la ley escrita en el codigo, cada una de esas fechas seria un
-- despliegue. Con vigencia en la tabla, el calculo de una semana de junio
-- sigue dando lo de junio aunque hoy sea octubre, que es lo que hace que un
-- historico se pueda volver a correr y de lo mismo.
--
-- CADA PARAMETRO ES UNA FILA CON VIGENCIA, NO UNA COLUMNA
--
-- Igual que los gastos fijos: cuando un valor cambia no se sobrescribe, se le
-- cierra la vigencia al anterior y se abre uno nuevo. Un UPDATE sobre el valor
-- haria que una semana ya calculada cambiara por detras.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LOS PARAMETROS DE LEY
-- ===========================================================================

CREATE TABLE IF NOT EXISTS gerencia_nomina_parametro (
  idparametro    INT AUTO_INCREMENT PRIMARY KEY,
  codigo         VARCHAR(40)  NOT NULL,
  nombre         VARCHAR(120) NOT NULL,
  valor          DOUBLE       NOT NULL,
  unidad         VARCHAR(20)  NOT NULL DEFAULT 'PORCENTAJE'
                 COMMENT 'PORCENTAJE, PESOS, HORA, FACTOR o BOOLEANO',
  vigencia_desde DATE         NOT NULL,
  vigencia_hasta DATE         NULL COMMENT 'NULL = sigue rigiendo',
  observacion    VARCHAR(255) NULL COMMENT 'la norma que lo sustenta',
  usuario        VARCHAR(50)  NULL,
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_parametro_vigencia (codigo, vigencia_desde),
  KEY idx_parametro_codigo (codigo, vigencia_desde, vigencia_hasta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='La ley laboral con vigencia: lo que cambia cada enero y a mitad de 2026';

-- ===========================================================================
-- 2. LA CONFIGURACION POR CARGO
--
-- Dos cosas que no son de ley sino de la compania:
--
-- arl_porcentaje  La clase de riesgo no es la misma para todos. Un domiciliario
--                 en moto no cotiza lo que un auxiliar de cocina.
--
-- entra_tienda    SI EL CARGO SUMA A LA NOMINA DE TIENDA O A LA ESTRUCTURA.
--                 Aqui se resuelve la duda de los domiciliarios sin tocar una
--                 linea de codigo: hoy el tablero ya descuenta "Pago
--                 Domiciliarios fijos" como gasto de operacion -213 millones en
--                 2026-, asi que si ademas entraran por nomina se contarian dos
--                 veces. Queda en 'N' por precaucion y se prende cuando se
--                 confirme contra el Excel de julio. Apagarlo NO los borra: el
--                 estimado se calcula igual y queda visible, simplemente no
--                 suma al costo de la tienda.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS gerencia_nomina_cargo (
  idtipoempleado  INT          NOT NULL PRIMARY KEY COMMENT 'general.tipo_empleado',
  nombre          VARCHAR(120) NOT NULL,
  arl_porcentaje  DOUBLE       NOT NULL DEFAULT 0.522 COMMENT 'clase de riesgo, en porcentaje',
  entra_tienda    CHAR(1)      NOT NULL DEFAULT 'S'
                  COMMENT 'S suma a la nomina de tienda; N va a estructura o ya se paga por otro lado',
  calcula         CHAR(1)      NOT NULL DEFAULT 'S'
                  COMMENT 'N para cargos que no se estiman nunca',
  observacion     VARCHAR(255) NULL,
  usuario         VARCHAR(50)  NULL,
  fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 3. LOS FESTIVOS
--
-- Se podrian calcular -la Ley Emiliani corre casi todos al lunes siguiente y
-- las fechas moviles salen de la Pascua- pero un festivo mal calculado se paga
-- con un 90% de recargo equivocado en toda la compania y nadie lo nota. Una
-- tabla de dieciocho filas al ano se revisa de un vistazo.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS gerencia_festivo (
  fecha   DATE         NOT NULL PRIMARY KEY,
  nombre  VARCHAR(80)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 4. EL ESTIMADO, CON SU DESGLOSE
--
-- Se guarda el desglose de horas y no solo el total, por dos razones: para
-- poder explicarle a alguien por que su semana costo lo que costo, y para que
-- cuando el estimado no cuadre con SIIGO se pueda ver DONDE no cuadra -si son
-- las nocturnas, si son los domingos, si son las extras- en vez de solo saber
-- que fallo por un 7%.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS gerencia_nomina_estimado (
  idempleado         INT    NOT NULL,
  semana             DATE   NOT NULL COMMENT 'domingo de cierre, igual que gerencia_semana.fecha_fin',
  salario_base       DOUBLE NOT NULL DEFAULT 0 COMMENT 'el salario del empleado ese dia',
  horas_ordinarias   DOUBLE NOT NULL DEFAULT 0,
  horas_nocturnas    DOUBLE NOT NULL DEFAULT 0,
  horas_dominicales  DOUBLE NOT NULL DEFAULT 0 COMMENT 'domingo o festivo, diurnas',
  horas_dom_noct     DOUBLE NOT NULL DEFAULT 0 COMMENT 'domingo o festivo, nocturnas',
  horas_extra        DOUBLE NOT NULL DEFAULT 0 COMMENT 'lo que excede la jornada semanal',
  horas_totales      DOUBLE NOT NULL DEFAULT 0,
  turnos             INT    NOT NULL DEFAULT 0,
  sueldo_basico      DOUBLE NOT NULL DEFAULT 0,
  sueldo_variable    DOUBLE NOT NULL DEFAULT 0 COMMENT 'recargos y extras',
  auxilio_transporte DOUBLE NOT NULL DEFAULT 0,
  seguridad_social   DOUBLE NOT NULL DEFAULT 0 COMMENT 'lo que paga el empleador',
  liquidacion        DOUBLE NOT NULL DEFAULT 0 COMMENT 'provisiones: cesantias, intereses, prima, vacaciones',
  costo_total        DOUBLE NOT NULL DEFAULT 0,
  factor_correccion  DOUBLE NOT NULL DEFAULT 0
                     COMMENT 'porcentaje aplicado por desviacion historica; 0 = sin corregir',
  costo_corregido    DOUBLE NOT NULL DEFAULT 0
                     COMMENT 'costo_total mas el factor; es lo que se sugiere cargar',
  aviso              VARCHAR(255) NULL COMMENT 'por que este estimado puede estar mal',
  fecha_calculo      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idempleado, semana),
  KEY idx_estimado_semana (semana)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 5. LA DESVIACION: ESTIMADO CONTRA REAL
--
-- Se guarda como fila propia y no se calcula al vuelo contra
-- gerencia_nomina_empleado_semana, por una razon: el estimado se vuelve a
-- calcular cuando se corrigen parametros o biometria, y si la desviacion se
-- recalculara con el estimado nuevo, el historico de que tan bien veniamos
-- estimando se perderia cada vez que se arregla algo.
--
-- Y SE GUARDA EL ESTIMADO CRUDO, NO EL CORREGIDO
--
-- Esta es la trampa del bucle: si la desviacion se midiera contra un estimado
-- que ya trae la correccion, el calculo se perseguiria a si mismo -o converge
-- a cero y deja de aprender, o se pone a oscilar-. La desviacion SIEMPRE se
-- mide contra lo que el calculo dio antes de corregir.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS gerencia_nomina_desviacion (
  idempleado      INT    NOT NULL,
  semana          DATE   NOT NULL,
  idtienda        INT    NOT NULL DEFAULT 0 COMMENT 'tienda principal esa semana, para calibrar por tienda',
  estimado_crudo  DOUBLE NOT NULL COMMENT 'lo que dio el calculo ANTES de corregir',
  real_cargado    DOUBLE NOT NULL COMMENT 'lo que vino de SIIGO o se digito',
  desviacion_pct  DOUBLE NOT NULL COMMENT '(real - estimado) / estimado * 100',
  fecha_registro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idempleado, semana),
  KEY idx_desviacion_tienda (idtienda, semana)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 6. LOS VALORES DE 2026
-- ===========================================================================

INSERT INTO gerencia_nomina_parametro
  (codigo, nombre, valor, unidad, vigencia_desde, vigencia_hasta, observacion, usuario)
SELECT * FROM (
  -- --- Lo que cambia cada enero ---
  SELECT 'SMLV' c, 'Salario minimo mensual' n, 1750905 v, 'PESOS' u,
         '2026-01-01' d, CAST(NULL AS CHAR) h, 'Vigente 2026' o, 'carga_inicial' s
  UNION ALL SELECT 'AUX_TRANSPORTE', 'Auxilio de transporte mensual', 249095, 'PESOS',
         '2026-01-01', NULL, 'Vigente 2026', 'carga_inicial'
  UNION ALL SELECT 'TOPE_AUX_SMLV', 'Hasta cuantos salarios minimos da auxilio', 2, 'FACTOR',
         '2026-01-01', NULL, 'Quien gana mas de 2 SMLV no recibe auxilio', 'carga_inicial'

  -- --- La jornada nocturna: lo que mas nos pesa ---
  UNION ALL SELECT 'HORA_NOCTURNA_INICIO', 'Hora en que empieza el recargo nocturno', 19, 'HORA',
         '2025-07-01', NULL, 'Ley 2466 de 2025 art. 10; antes eran las 21:00', 'carga_inicial'
  UNION ALL SELECT 'HORA_NOCTURNA_FIN', 'Hora en que termina el recargo nocturno', 6, 'HORA',
         '2025-07-01', NULL, 'Ley 2466 de 2025 art. 10', 'carga_inicial'
  UNION ALL SELECT 'RECARGO_NOCTURNO', 'Recargo por trabajo nocturno', 35, 'PORCENTAJE',
         '2025-07-01', NULL, 'CST art. 168', 'carga_inicial'

  -- --- El dominical, en escalera ---
  UNION ALL SELECT 'RECARGO_DOMINICAL', 'Recargo por domingo o festivo', 80, 'PORCENTAJE',
         '2025-07-01', '2026-06-30', 'Ley 2466 de 2025, primer escalon', 'carga_inicial'
  UNION ALL SELECT 'RECARGO_DOMINICAL', 'Recargo por domingo o festivo', 90, 'PORCENTAJE',
         '2026-07-01', '2027-06-30', 'Ley 2466 de 2025, segundo escalon', 'carga_inicial'
  UNION ALL SELECT 'RECARGO_DOMINICAL', 'Recargo por domingo o festivo', 100, 'PORCENTAJE',
         '2027-07-01', NULL, 'Ley 2466 de 2025, escalon final', 'carga_inicial'

  -- --- La jornada, que baja a mitad de ano ---
  UNION ALL SELECT 'JORNADA_SEMANAL', 'Horas ordinarias por semana', 44, 'HORA',
         '2025-07-16', '2026-07-14', 'Ley 2101 de 2021', 'carga_inicial'
  UNION ALL SELECT 'JORNADA_SEMANAL', 'Horas ordinarias por semana', 42, 'HORA',
         '2026-07-15', NULL, 'Ley 2101 de 2021, escalon final', 'carga_inicial'
  UNION ALL SELECT 'HORAS_MES_LIQUIDACION', 'Divisor para sacar el valor de la hora', 240, 'HORA',
         '2026-01-01', NULL,
         'La rebaja de jornada no rebaja el salario, asi que el divisor se mantiene. Si la compania decide otro, se cambia aqui.',
         'carga_inicial'
  UNION ALL SELECT 'EXTRA_DIURNA', 'Recargo de hora extra diurna', 25, 'PORCENTAJE',
         '2026-01-01', NULL, 'CST art. 168', 'carga_inicial'
  UNION ALL SELECT 'EXTRA_NOCTURNA', 'Recargo de hora extra nocturna', 75, 'PORCENTAJE',
         '2026-01-01', NULL, 'CST art. 168', 'carga_inicial'

  -- --- Provisiones ---
  UNION ALL SELECT 'CESANTIAS', 'Provision de cesantias', 8.33, 'PORCENTAJE',
         '2026-01-01', NULL, 'Un mes de salario por ano', 'carga_inicial'
  UNION ALL SELECT 'INTERES_CESANTIAS', 'Intereses sobre cesantias', 1, 'PORCENTAJE',
         '2026-01-01', NULL, '12% anual sobre las cesantias', 'carga_inicial'
  UNION ALL SELECT 'PRIMA', 'Provision de prima de servicios', 8.33, 'PORCENTAJE',
         '2026-01-01', NULL, 'Un mes de salario por ano', 'carga_inicial'
  UNION ALL SELECT 'VACACIONES', 'Provision de vacaciones', 4.17, 'PORCENTAJE',
         '2026-01-01', NULL, '15 dias habiles por ano; no incluye auxilio de transporte', 'carga_inicial'

  -- --- Seguridad social y parafiscales que paga el empleador ---
  UNION ALL SELECT 'PENSION_EMPLEADOR', 'Pension a cargo del empleador', 12, 'PORCENTAJE',
         '2026-01-01', NULL, 'Ley 100', 'carga_inicial'
  UNION ALL SELECT 'SALUD_EMPLEADOR', 'Salud a cargo del empleador', 8.5, 'PORCENTAJE',
         '2026-01-01', NULL, 'Exonerado si aplica el art. 114-1 ET', 'carga_inicial'
  UNION ALL SELECT 'CAJA_COMPENSACION', 'Caja de compensacion', 4, 'PORCENTAJE',
         '2026-01-01', NULL, 'Nunca se exonera', 'carga_inicial'
  UNION ALL SELECT 'SENA', 'Aporte SENA', 2, 'PORCENTAJE',
         '2026-01-01', NULL, 'Exonerado si aplica el art. 114-1 ET', 'carga_inicial'
  UNION ALL SELECT 'ICBF', 'Aporte ICBF', 3, 'PORCENTAJE',
         '2026-01-01', NULL, 'Exonerado si aplica el art. 114-1 ET', 'carga_inicial'
  UNION ALL SELECT 'EXONERACION_114_1', 'Aplica la exoneracion del art. 114-1 ET', 1, 'BOOLEANO',
         '2026-01-01', NULL,
         'Con 1, salud, SENA e ICBF NO se cobran a quien gane menos de 10 SMLV. Son 13,5 puntos: CONFIRMAR con el contador antes de creerle al numero.',
         'carga_inicial'
  UNION ALL SELECT 'TOPE_EXONERACION_SMLV', 'Hasta cuantos SMLV aplica la exoneracion', 10, 'FACTOR',
         '2026-01-01', NULL, 'Art. 114-1 ET', 'carga_inicial'

  -- --- Y una bandera de la compania, no de la ley ---
  UNION ALL SELECT 'CORREGIR_DESDE_SEMANAS', 'Semanas de historia para empezar a corregir', 6, 'FACTOR',
         '2026-01-01', NULL,
         'Con menos historia que esta, el estimado NO se corrige: un promedio de dos semanas no es un sesgo, es ruido.',
         'carga_inicial'
  UNION ALL SELECT 'DISPERSION_MAXIMA', 'Dispersion sobre la que no se corrige', 5, 'PORCENTAJE',
         '2026-01-01', NULL,
         'Si la desviacion historica varia mas que esto, el promedio no significa nada y aplicarlo empeora el estimado.',
         'carga_inicial'
) nuevos
WHERE NOT EXISTS (SELECT 1 FROM gerencia_nomina_parametro ya
                   WHERE ya.codigo = nuevos.c AND ya.vigencia_desde = nuevos.d);

-- ===========================================================================
-- 7. LOS CARGOS, SEMBRADOS DESDE LO QUE YA EXISTE
--
-- Se siembran todos con la clase de riesgo mas baja, que es la que hay que
-- corregir, no la que hay que adivinar: si quedara en la mas alta, el estimado
-- saldria inflado y nadie lo revisaria porque "va con margen".
--
-- Los domiciliarios y el conductor arrancan en 'N' en entra_tienda, porque hoy
-- el tablero YA descuenta "Pago Domiciliarios fijos" y sumarlos tambien por
-- nomina los contaria dos veces. Es lo unico de esta carga que hay que
-- confirmar contra el Excel de julio.
-- ===========================================================================

INSERT INTO gerencia_nomina_cargo (idtipoempleado, nombre, arl_porcentaje, entra_tienda, calcula, observacion, usuario)
SELECT t.idtipoempleado, t.descripcion,
       CASE WHEN t.descripcion LIKE '%omiciliario%' OR t.descripcion LIKE '%onductor%'
            THEN 6.960 ELSE 0.522 END,
       CASE WHEN t.descripcion LIKE '%omiciliario%' OR t.descripcion LIKE '%onductor%'
            THEN 'N' ELSE 'S' END,
       'S',
       CASE WHEN t.descripcion LIKE '%omiciliario%' OR t.descripcion LIKE '%onductor%'
            THEN 'Riesgo V por la moto. entra_tienda en N hasta confirmar si ya se pagan por Pago Domiciliarios fijos'
            ELSE 'Riesgo I por defecto; revisar' END,
       'carga_inicial'
  FROM general.tipo_empleado t
 WHERE NOT EXISTS (SELECT 1 FROM gerencia_nomina_cargo ya
                    WHERE ya.idtipoempleado = t.idtipoempleado);

-- ===========================================================================
-- 8. LOS FESTIVOS DE 2026
-- ===========================================================================

INSERT INTO gerencia_festivo (fecha, nombre)
SELECT * FROM (
            SELECT '2026-01-01' f, 'Ano Nuevo' n
  UNION ALL SELECT '2026-01-12', 'Reyes Magos'
  UNION ALL SELECT '2026-03-23', 'San Jose'
  UNION ALL SELECT '2026-04-02', 'Jueves Santo'
  UNION ALL SELECT '2026-04-03', 'Viernes Santo'
  UNION ALL SELECT '2026-05-01', 'Dia del Trabajo'
  UNION ALL SELECT '2026-05-18', 'Ascension de Jesus'
  UNION ALL SELECT '2026-06-08', 'Corpus Christi'
  UNION ALL SELECT '2026-06-15', 'Sagrado Corazon'
  UNION ALL SELECT '2026-06-29', 'San Pedro y San Pablo'
  UNION ALL SELECT '2026-07-20', 'Independencia'
  UNION ALL SELECT '2026-08-07', 'Batalla de Boyaca'
  UNION ALL SELECT '2026-08-17', 'Asuncion de la Virgen'
  UNION ALL SELECT '2026-10-12', 'Dia de la Raza'
  UNION ALL SELECT '2026-11-02', 'Todos los Santos'
  UNION ALL SELECT '2026-11-16', 'Independencia de Cartagena'
  UNION ALL SELECT '2026-12-08', 'Inmaculada Concepcion'
  UNION ALL SELECT '2026-12-25', 'Navidad'
) nuevos
WHERE NOT EXISTS (SELECT 1 FROM gerencia_festivo ya WHERE ya.fecha = nuevos.f);

-- ===========================================================================
-- 9. LA PANTALLA
-- ===========================================================================

INSERT INTO pizzaamericana.pantalla (nombre, idmodulo, url_html, orden, activo)
SELECT 'Calculo de Nomina', m.idmodulo, 'NominaCalculo.html', 45, 'S'
  FROM (SELECT idmodulo FROM pizzaamericana.menu_modulo WHERE nombre = 'Gerencia') m
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.pantalla ya
                    WHERE ya.url_html = 'NominaCalculo.html');

INSERT INTO pizzaamericana.rol_pantalla (idrol, idpantalla)
SELECT r.idrol, p.idpantalla
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.pantalla p ON p.url_html = 'NominaCalculo.html'
 WHERE r.nombre = 'Gerencia'
   AND NOT EXISTS (SELECT 1 FROM pizzaamericana.rol_pantalla ya
                    WHERE ya.idrol = r.idrol AND ya.idpantalla = p.idpantalla);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT codigo, nombre, valor, unidad, vigencia_desde, IFNULL(vigencia_hasta,'abierta') AS hasta
  FROM gerencia_nomina_parametro ORDER BY codigo, vigencia_desde;

SELECT entra_tienda, COUNT(*) AS cargos, GROUP_CONCAT(DISTINCT arl_porcentaje) AS arl
  FROM gerencia_nomina_cargo GROUP BY entra_tienda;

SELECT COUNT(*) AS festivos_2026 FROM gerencia_festivo WHERE YEAR(fecha) = 2026;

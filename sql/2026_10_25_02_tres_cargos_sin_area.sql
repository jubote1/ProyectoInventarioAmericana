-- ---------------------------------------------------------------------------
-- LOS TRES CARGOS QUE NO TENIAN AREA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente.
--
-- QUE PASA
--
-- GerenciaCierreDAO.cargarPersonas arma la nomina de cada area cruzando
-- general.empleado con gerencia_nomina_cargo. Tres cargos activos no estaban
-- en ese cruce, asi que esas tres personas no sumaban en ninguna area:
--
--   34 Director General                 Ana Maria Foronda Gomez     5.250.000
--   35 Lider de Innovacion y Desarrollo Maria Isabel Lopera Gomez   3.000.000
--   37 Auxiliar de Calidad              Alahia Vasquez Restrepo     2.000.000
--
-- POR QUE CALIDAD VA A LOGISTICA, Y NO ES UNA CORAZONADA
--
-- Sumando las pestanas del libro, el sueldo basico de LOGISTICA da 20.257.000.
-- El sistema calcula 18.505.430. La diferencia, 1.751.570, es casi exactamente
-- el sueldo de esta persona: con ella adentro da 20.505.430, un 1,2% de
-- diferencia contra el Excel. Esa area queda cuadrada.
--
-- Los otros dos van a ADMINISTRATIVA. Director General no tiene discusion.
-- Lider de Innovacion y Desarrollo es el menos obvio de los tres: si resulta
-- que va en otra parte, es un UPDATE de una fila.
--
-- ARL: se deja en 0,522 -riesgo I- como las demas filas de carga_inicial, que
-- es lo que aplica a trabajo de oficina. entra_tienda en N porque ninguno de
-- los tres opera un punto de venta; calcula en S para que si sumen.
-- ---------------------------------------------------------------------------

INSERT INTO gerencia_nomina_cargo
       (idtipoempleado, nombre, arl_porcentaje, entra_tienda, calcula, observacion, usuario, area)
VALUES
  (34, 'Director General',                 0.522, 'N', 'S',
   'Sin area hasta el 2026-10-25', 'carga_areas', 'ADMINISTRATIVA'),
  (35, 'Lider de Innovacion y Desarrollo', 0.522, 'N', 'S',
   'Sin area hasta el 2026-10-25; confirmar si va en administrativa', 'carga_areas', 'ADMINISTRATIVA'),
  (37, 'Auxiliar de Calidad',              0.522, 'N', 'S',
   'Sin area hasta el 2026-10-25; a logistica porque es lo que cuadra el area con el Excel',
   'carga_areas', 'LOGISTICA')
ON DUPLICATE KEY UPDATE area = VALUES(area), calcula = VALUES(calcula);

-- ===========================================================================
-- COMO QUEDO: la nomina basica de cada area contra el Excel
--   ADMINISTRATIVA 67.502.000 | LOGISTICA 20.257.000 | CONTACT 19.962.000
-- ===========================================================================
SELECT c.area, COUNT(*) AS personas, SUM(e.salario <= 0) AS sin_salario,
       ROUND(SUM(e.salario)) AS basico
  FROM general.empleado e
  JOIN gerencia_nomina_cargo c ON c.idtipoempleado = e.idtipoempleado
 WHERE e.activo = 1 AND e.es_empleado = 1 AND c.area <> 'TIENDA'
 GROUP BY c.area ORDER BY c.area;

-- Y que no quede nadie por fuera. Tiene que dar cero filas.
SELECT e.id, e.nombre_largo, t.descripcion AS cargo
  FROM general.empleado e
  JOIN general.tipo_empleado t ON t.idtipoempleado = e.idtipoempleado
  LEFT JOIN gerencia_nomina_cargo c ON c.idtipoempleado = e.idtipoempleado
 WHERE e.activo = 1 AND e.es_empleado = 1 AND c.idtipoempleado IS NULL;

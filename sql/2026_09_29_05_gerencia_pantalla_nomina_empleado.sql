-- ---------------------------------------------------------------------------
-- CUARTA PANTALLA DE GERENCIA: Nomina por Empleado
--
-- Se corre en el CENTRAL (172.19.0.25), esquema pizzaamericana.
-- Idempotente: correrlo dos veces no duplica nada.
-- Requiere 2026_09_29_01_gerencia_calendario_gastos_y_acceso.sql (crea el
-- modulo 'Gerencia' y el rol 'Gerencia' que aqui se reutilizan).
--
-- Registra NominaEmpleado.html en el mismo catalogo de seguridad de las otras
-- tres pantallas de Gerencia, y le da el permiso al rol 'Gerencia' -no a
-- ninguna persona en particular: eso sigue siendo decision de negocio, no de
-- un script (ver la nota en 2026_09_29_01)-.
-- ---------------------------------------------------------------------------

INSERT INTO pizzaamericana.pantalla (nombre, idmodulo, url_html, orden, activo)
SELECT 'Nomina por Empleado', m.idmodulo, 'NominaEmpleado.html', 40, 'S'
  FROM pizzaamericana.menu_modulo m
 WHERE m.nombre = 'Gerencia'
   AND NOT EXISTS (SELECT 1 FROM pizzaamericana.pantalla ya WHERE ya.url_html = 'NominaEmpleado.html');

INSERT INTO pizzaamericana.rol_pantalla (idrol, idpantalla)
SELECT r.idrol, p.idpantalla
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.pantalla p ON p.url_html = 'NominaEmpleado.html'
 WHERE r.nombre = 'Gerencia'
   AND NOT EXISTS (SELECT 1 FROM pizzaamericana.rol_pantalla ya
                    WHERE ya.idrol = r.idrol AND ya.idpantalla = p.idpantalla);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================
SELECT p.idpantalla, p.nombre, p.url_html, m.nombre AS modulo, p.orden, p.activo
  FROM pizzaamericana.pantalla p
  JOIN pizzaamericana.menu_modulo m ON m.idmodulo = p.idmodulo
 WHERE m.nombre = 'Gerencia'
 ORDER BY p.orden;

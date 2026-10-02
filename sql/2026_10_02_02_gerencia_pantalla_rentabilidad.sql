-- ---------------------------------------------------------------------------
-- LA PANTALLA DEL TABLERO DE RENTABILIDAD
--
-- Se corre en el CENTRAL (172.19.0.25). Idempotente.
--
-- Entra al mismo catalogo de seguridad que ya usan las otras tres pantallas de
-- Gerencia -menu_modulo, pantalla, rol_pantalla- y al mismo rol Gerencia. Quien
-- ya tiene el rol la ve sin que haya que tocarle nada; quien no lo tiene sigue
-- sin verla, que es lo que se quiere: detras de esta pantalla hay nomina.
--
-- Va de ultima en el orden a proposito, en 50 y no en 40: en 40 ya hay una
-- pantalla registrada -NominaEmpleado.html- que no existe como archivo, y dos
-- con el mismo orden salen en el menu en cualquier orden. Las tres anteriores son de CARGA y
-- esta es de LECTURA: no se puede mirar un resultado antes de haber definido
-- las semanas y los gastos, y el orden del menu deberia decirlo.
-- ---------------------------------------------------------------------------

INSERT INTO pizzaamericana.pantalla (nombre, idmodulo, url_html, orden, activo)
SELECT 'Tablero de Rentabilidad', m.idmodulo, 'Rentabilidad.html', 50, 'S'
  FROM (SELECT idmodulo FROM pizzaamericana.menu_modulo WHERE nombre = 'Gerencia') m
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.pantalla ya
                    WHERE ya.url_html = 'Rentabilidad.html');

INSERT INTO pizzaamericana.rol_pantalla (idrol, idpantalla)
SELECT r.idrol, p.idpantalla
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.pantalla p ON p.url_html = 'Rentabilidad.html'
 WHERE r.nombre = 'Gerencia'
   AND NOT EXISTS (SELECT 1 FROM pizzaamericana.rol_pantalla ya
                    WHERE ya.idrol = r.idrol AND ya.idpantalla = p.idpantalla);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================

SELECT r.nombre AS rol, p.nombre AS pantalla, p.url_html, p.orden
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.rol_pantalla rp ON rp.idrol = r.idrol
  JOIN pizzaamericana.pantalla p ON p.idpantalla = rp.idpantalla
 WHERE r.nombre = 'Gerencia' ORDER BY p.orden;

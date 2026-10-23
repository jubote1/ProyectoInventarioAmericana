-- ---------------------------------------------------------------------------
-- LA PANTALLA DEL CIERRE DE GERENCIA
--
-- Se corre en el CENTRAL (172.19.0.25). Idempotente.
--
-- Entra al mismo catalogo de seguridad que las otras pantallas de Gerencia
-- -menu_modulo, pantalla, rol_pantalla- y al mismo rol Gerencia. Quien ya tiene
-- el rol la ve sin que haya que tocarle nada; quien no lo tiene sigue sin
-- verla, que es lo que se quiere: detras de esta pantalla hay nomina con nombre
-- propio.
--
-- Va en 60, despues del tablero de rentabilidad: es la que reemplaza al Excel
-- y la que mas abajo esta en la cadena. Primero se definen las semanas y los
-- gastos, despues se mira el resultado, y de ultimo se cierra el mes.
-- ---------------------------------------------------------------------------

INSERT INTO pizzaamericana.pantalla (nombre, idmodulo, url_html, orden, activo)
SELECT 'Cierre de Gerencia', m.idmodulo, 'CierreGerencia.html', 60, 'S'
  FROM (SELECT idmodulo FROM pizzaamericana.menu_modulo WHERE nombre = 'Gerencia') m
 WHERE NOT EXISTS (SELECT 1 FROM pizzaamericana.pantalla ya
                    WHERE ya.url_html = 'CierreGerencia.html');

INSERT INTO pizzaamericana.rol_pantalla (idrol, idpantalla)
SELECT r.idrol, p.idpantalla
  FROM pizzaamericana.rol r
  JOIN pizzaamericana.pantalla p ON p.url_html = 'CierreGerencia.html'
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

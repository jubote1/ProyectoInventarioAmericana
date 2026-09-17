-- ---------------------------------------------------------------------------
-- CON GUACAMOLE: EL PZ Y EL XG ESTABAN CORRIDOS
--
-- Se corre en la base de CADA UNA de las once tiendas (tiendaamericana).
-- Mismo archivo para todas. Borra antes de insertar: correrlo dos veces no
-- cambia nada.
--
-- COMO ESTABA            COMO QUEDA
--   GD   90                GD   90
--   MD   60                MD   60
--   PZ  125  <- mas que    PZ   25
--   XG  sin fila              XG  125
--
-- El PZ tenia el valor que le corresponde al XG y el XG no tenia nada: la
-- pizza pequena con guacamole descontaba cinco veces lo que debia y la
-- extragrande no descontaba nada.
--
-- Las cantidades salen de la familia Adicion Guacamole, que esta bien:
-- XL 125, GD 90, MD 60, PZ 25. Confirmado por Juan David el 2026-09-17.
--
-- Es el mismo tipo de error que ya se corrigio dos veces hoy: una fila
-- cargada contra el producto de al lado.
-- ---------------------------------------------------------------------------

START TRANSACTION;

DELETE FROM item_inventario_x_producto WHERE idproducto = 274 AND iditem = 68;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (274, 68, 25);

DELETE FROM item_inventario_x_producto WHERE idproducto = 275 AND iditem = 68;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (275, 68, 125);

-- Tiene que quedar: GD 90, MD 60, PZ 25, XG 125.
SELECT p.idproducto, p.descripcion, x.cantidad
  FROM producto p LEFT JOIN item_inventario_x_producto x
    ON x.idproducto = p.idproducto AND x.iditem = 68
 WHERE p.idproducto IN (272,273,274,275) ORDER BY p.idproducto;

COMMIT;

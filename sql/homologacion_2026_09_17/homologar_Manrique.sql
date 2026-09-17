-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE LA COMPOSICION DE PRODUCTOS - Manrique
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
--
-- Lleva item_inventario_x_producto al valor que tienen la mayoria de las once
-- tiendas. No se toma Manrique como referencia: Manrique tambien tiene errores
-- propios. Para cada receta se usa el valor en que coinciden mas tiendas.
--
-- Correcciones en esta tienda: 5
--
-- Donde la cantidad cambia se borra y se vuelve a insertar UNA fila, en vez de
-- actualizar: si el par venia repetido, un UPDATE dejaria las dos filas y el POS
-- seguiria descontando doble -recorre todas las filas del producto-.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- Grande Postobon Ifood / Caja Grande : 1 -> AUSENTE   (asi lo tienen 9 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 417 AND iditem = 35;

-- Lasag¤a Combo Insep / Lasagna Mixt : 2 -> 1   (asi lo tienen 9 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 447 AND iditem = 6;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (447, 6, 1);

-- Lasag¤a Combo Insep / Panes Lasagna Baguette : 4 -> 2   (asi lo tienen 9 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 447 AND iditem = 72;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (447, 72, 2);

-- BlueBerry / Bolas Gel Sandia : 0.023 -> 25   (asi lo tienen 9 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 513 AND iditem = 133;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (513, 133, 25);

-- Americana Premium GD / Queso : 430 -> 330   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 598 AND iditem = 28;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (598, 28, 330);

-- Revise el conteo antes de confirmar.
SELECT COUNT(*) AS filas_despues FROM item_inventario_x_producto;

COMMIT;

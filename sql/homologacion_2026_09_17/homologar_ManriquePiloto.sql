-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE LA COMPOSICION DE PRODUCTOS - ManriquePiloto
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
--
-- Lleva item_inventario_x_producto al valor que tienen la mayoria de las once
-- tiendas. No se toma Manrique como referencia: Manrique tambien tiene errores
-- propios. Para cada receta se usa el valor en que coinciden mas tiendas.
--
-- Correcciones en esta tienda: 1
--
-- Donde la cantidad cambia se borra y se vuelve a insertar UNA fila, en vez de
-- actualizar: si el par venia repetido, un UPDATE dejaria las dos filas y el POS
-- seguiria descontando doble -recorre todas las filas del producto-.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- Deditos Promo / Mantequilla : 3 -> 3.3   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 432 AND iditem = 70;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (432, 70, 3.3);

-- Revise el conteo antes de confirmar.
SELECT COUNT(*) AS filas_despues FROM item_inventario_x_producto;

COMMIT;

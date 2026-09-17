-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE LA COMPOSICION DE PRODUCTOS - Calasanz
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
--
-- Lleva item_inventario_x_producto al valor que tienen la mayoria de las once
-- tiendas. No se toma Manrique como referencia: Manrique tambien tiene errores
-- propios. Para cada receta se usa el valor en que coinciden mas tiendas.
--
-- Correcciones en esta tienda: 4
--
-- Donde la cantidad cambia se borra y se vuelve a insertar UNA fila, en vez de
-- actualizar: si el par venia repetido, un UPDATE dejaria las dos filas y el POS
-- seguiria descontando doble -recorre todas las filas del producto-.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- Lecherita(Obsequio) / Pasta : 85 -> AUSENTE   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 462 AND iditem = 26;

-- Lecherita(Obsequio) / Queso : 330 -> AUSENTE   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 462 AND iditem = 28;

-- Lecherita(Obsequio) / Caja Grande : 1 -> AUSENTE   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 462 AND iditem = 35;

-- Lecherita(Obsequio) / Masa Grande : 1 -> AUSENTE   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 462 AND iditem = 9;

-- Revise el conteo antes de confirmar.
SELECT COUNT(*) AS filas_despues FROM item_inventario_x_producto;

COMMIT;

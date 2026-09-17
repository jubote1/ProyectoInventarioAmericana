-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - Niquia
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
--
-- Solo cambia el TEXTO de producto.descripcion y de
-- item_inventario.nombre_item. No toca ids, ni precios, ni recetas.
--
-- El nombre se escribe con UNHEX de los bytes de la tienda mayoritaria.
-- Escribirlo como texto obligaria a que cliente, conexion y columna
-- coincidan en codificacion, y aqui no coinciden: la columna dice utf8 y
-- hay filas con bytes latin1 sueltos. Copiando los bytes no hay forma de
-- corromper una tilde.
--
-- Cambios en esta tienda: 2
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 2: 'Lasagna Mixta' -> 'Lasagña Mixta'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4C61736167C3B161204D69787461') WHERE idproducto = 2;

-- item_inventario 27: 'Piña' -> 'Pina'   (asi lo tienen 8 de 11)
UPDATE item_inventario SET nombre_item = UNHEX('50696E61') WHERE iditem = 27;

COMMIT;

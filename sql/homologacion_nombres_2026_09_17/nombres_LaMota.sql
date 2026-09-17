-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - LaMota
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
-- Cambios en esta tienda: 1
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 293: 'XL Combo Doble Contact' -> 'XL Combo Doble Conctact'   (asi lo tienen 10 de 11)
UPDATE producto SET descripcion = UNHEX('584C20436F6D626F20446F626C6520436F6E6374616374') WHERE idproducto = 293;

COMMIT;

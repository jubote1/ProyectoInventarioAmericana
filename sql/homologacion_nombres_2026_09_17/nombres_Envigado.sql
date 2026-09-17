-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - Envigado
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
-- Cambios en esta tienda: 3
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 39: 'Hit Frutas 500 mL' -> '"Hit Frutas 500 mL"'   (asi lo tienen 10 de 11)
UPDATE producto SET descripcion = UNHEX('224869742046727574617320353030206D4C22') WHERE idproducto = 39;

-- producto 116: 'Uva Pet 400mL' -> '"Uva Pet 400mL"'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('2255766120506574203430306D4C22') WHERE idproducto = 116;

-- producto 615: 'Soda Limon' -> 'Soda Miche Limón'   (asi lo tienen 10 de 11)
UPDATE producto SET descripcion = UNHEX('536F6461204D69636865204C696DC3B36E') WHERE idproducto = 615;

COMMIT;

-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - Pilarica
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
-- Solo cambia texto: no toca ids, ni precios, ni recetas.
--
-- Se homologa a la escritura CORRECTA, NO a la mayoritaria. Nueve tiendas
-- tienen la enye doblemente codificada -bytes C383C2B1, el UTF-8 de "A~"- y
-- solo dos la tienen bien: homologar por mayoria rompia las dos buenas.
--
-- El valor se escribe con UNHEX de los bytes buenos. Escribirlo como texto
-- obligaria a que cliente, conexion y columna coincidan en codificacion, y
-- es justo lo que esta mal aqui.
--
-- Cambios en esta tienda: 2
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 116 -> "Uva Pet 400mL"
UPDATE producto SET descripcion = UNHEX('2255766120506574203430306D4C22') WHERE idproducto = 116;

-- item_inventario 27 -> Piña
UPDATE item_inventario SET nombre_item = UNHEX('5069C3B161') WHERE iditem = 27;

COMMIT;

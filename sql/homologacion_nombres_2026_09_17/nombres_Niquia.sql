-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - Niquia
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
-- Cambios en esta tienda: 41
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 2 -> Lasagña Mixta
UPDATE producto SET descripcion = UNHEX('4C61736167C3B161204D69787461') WHERE idproducto = 2;

-- producto 16 -> Adición Champiñon XL
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C3B16F6E20584C') WHERE idproducto = 16;

-- producto 46 -> Adición Champiñon GD
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C3B16F6E204744') WHERE idproducto = 46;

-- producto 64 -> Adición Champiñon MD
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C3B16F6E204D44') WHERE idproducto = 64;

-- producto 82 -> Adición Champiñon PZ
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C3B16F6E20505A') WHERE idproducto = 82;

-- producto 134 -> Con Champiñon GD
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C3B16F6E204744') WHERE idproducto = 134;

-- producto 135 -> Con Champiñon MD
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C3B16F6E204D44') WHERE idproducto = 135;

-- producto 136 -> Con Champiñon PZ
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C3B16F6E20505A') WHERE idproducto = 136;

-- producto 137 -> Con Champiñon XG
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C3B16F6E205847') WHERE idproducto = 137;

-- producto 142 -> Con Maíz Tierno GD
UPDATE producto SET descripcion = UNHEX('436F6E204D61C3AD7A20546965726E6F204744') WHERE idproducto = 142;

-- producto 143 -> Con Maíz Tierno MD
UPDATE producto SET descripcion = UNHEX('436F6E204D61C3AD7A20546965726E6F204D44') WHERE idproducto = 143;

-- producto 144 -> Con Maíz Tierno PZ
UPDATE producto SET descripcion = UNHEX('436F6E204D61C3AD7A20546965726E6F20505A') WHERE idproducto = 144;

-- producto 145 -> Con Maíz Tierno XG
UPDATE producto SET descripcion = UNHEX('436F6E204D61C3AD7A20546965726E6F205847') WHERE idproducto = 145;

-- producto 199 -> Sin Champiñon GD
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C3B16F6E204744') WHERE idproducto = 199;

-- producto 200 -> Sin Champiñon MD
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C3B16F6E204D44') WHERE idproducto = 200;

-- producto 201 -> Sin Champiñon PZ
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C3B16F6E20505A') WHERE idproducto = 201;

-- producto 202 -> Sin Champiñon XG
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C3B16F6E205847') WHERE idproducto = 202;

-- producto 207 -> Sin Maíz Tierno GD
UPDATE producto SET descripcion = UNHEX('53696E204D61C3AD7A20546965726E6F204744') WHERE idproducto = 207;

-- producto 208 -> Sin Maíz Tierno MD
UPDATE producto SET descripcion = UNHEX('53696E204D61C3AD7A20546965726E6F204D44') WHERE idproducto = 208;

-- producto 209 -> Sin Maíz Tierno PZ
UPDATE producto SET descripcion = UNHEX('53696E204D61C3AD7A20546965726E6F20505A') WHERE idproducto = 209;

-- producto 210 -> Sin Maíz Tierno XG
UPDATE producto SET descripcion = UNHEX('53696E204D61C3AD7A20546965726E6F205847') WHERE idproducto = 210;

-- producto 307 -> Pollo Champiñon XL
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C3B16F6E20584C') WHERE idproducto = 307;

-- producto 312 -> Jamon y Champiñon XL
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C3B16F6E20584C') WHERE idproducto = 312;

-- producto 322 -> Montañera XL
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C3B165726120584C') WHERE idproducto = 322;

-- producto 327 -> Pepperoni Champiñon XL
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C3B16F6E20584C') WHERE idproducto = 327;

-- producto 334 -> Pollo Champiñon GD
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C3B16F6E204744') WHERE idproducto = 334;

-- producto 339 -> Jamon y Champiñon GD
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C3B16F6E204744') WHERE idproducto = 339;

-- producto 349 -> Montañera GD
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C3B1657261204744') WHERE idproducto = 349;

-- producto 354 -> Pepperoni Champiñon GD
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C3B16F6E204744') WHERE idproducto = 354;

-- producto 361 -> Pollo Champiñon MD
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C3B16F6E204D44') WHERE idproducto = 361;

-- producto 366 -> Jamon y Champiñon MD
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C3B16F6E204D44') WHERE idproducto = 366;

-- producto 376 -> Montañera MD
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C3B1657261204D44') WHERE idproducto = 376;

-- producto 381 -> Pepperoni Champiñon MD
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C3B16F6E204D44') WHERE idproducto = 381;

-- producto 388 -> Pollo Champiñon PZ
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C3B16F6E20505A') WHERE idproducto = 388;

-- producto 393 -> Jamon y Champiñon PZ
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C3B16F6E20505A') WHERE idproducto = 393;

-- producto 403 -> Montañera PZ
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C3B165726120505A') WHERE idproducto = 403;

-- producto 408 -> Pepperoni Champiñon PZ
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C3B16F6E20505A') WHERE idproducto = 408;

-- item_inventario 11 -> Masa Pequeña
UPDATE item_inventario SET nombre_item = UNHEX('4D617361205065717565C3B161') WHERE iditem = 11;

-- item_inventario 24 -> Champiñon
UPDATE item_inventario SET nombre_item = UNHEX('4368616D7069C3B16F6E') WHERE iditem = 24;

-- item_inventario 37 -> Caja Pequeña
UPDATE item_inventario SET nombre_item = UNHEX('43616A61205065717565C3B161') WHERE iditem = 37;

-- item_inventario 44 -> Papel Parafinado pequeño
UPDATE item_inventario SET nombre_item = UNHEX('506170656C205061726166696E61646F207065717565C3B16F') WHERE iditem = 44;

COMMIT;

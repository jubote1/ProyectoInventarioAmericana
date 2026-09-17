-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE NOMBRES - America
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
-- Cambios en esta tienda: 40
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- producto 16: 'Adición Champiñon XL' -> 'Adición ChampiÃ±on XL'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C383C2B16F6E20584C') WHERE idproducto = 16;

-- producto 46: 'Adición Champiñon GD' -> 'Adición ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C383C2B16F6E204744') WHERE idproducto = 46;

-- producto 64: 'Adición Champiñon MD' -> 'Adición ChampiÃ±on MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C383C2B16F6E204D44') WHERE idproducto = 64;

-- producto 82: 'Adición Champiñon PZ' -> 'Adición ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4164696369C3B36E204368616D7069C383C2B16F6E20505A') WHERE idproducto = 82;

-- producto 134: 'Con Champiñon GD' -> 'Con ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C383C2B16F6E204744') WHERE idproducto = 134;

-- producto 135: 'Con Champiñon MD' -> 'Con ChampiÃ±on MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C383C2B16F6E204D44') WHERE idproducto = 135;

-- producto 136: 'Con Champiñon PZ' -> 'Con ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C383C2B16F6E20505A') WHERE idproducto = 136;

-- producto 137: 'Con Champiñon XG' -> 'Con ChampiÃ±on XG'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204368616D7069C383C2B16F6E205847') WHERE idproducto = 137;

-- producto 142: 'Con Maíz Tierno GD' -> 'Con MaÃ­z Tierno GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204D61C383C2AD7A20546965726E6F204744') WHERE idproducto = 142;

-- producto 143: 'Con Maíz Tierno MD' -> 'Con MaÃ­z Tierno MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204D61C383C2AD7A20546965726E6F204D44') WHERE idproducto = 143;

-- producto 144: 'Con Maíz Tierno PZ' -> 'Con MaÃ­z Tierno PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204D61C383C2AD7A20546965726E6F20505A') WHERE idproducto = 144;

-- producto 145: 'Con Maíz Tierno XG' -> 'Con MaÃ­z Tierno XG'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('436F6E204D61C383C2AD7A20546965726E6F205847') WHERE idproducto = 145;

-- producto 199: 'Sin Champiñon GD' -> 'Sin ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C383C2B16F6E204744') WHERE idproducto = 199;

-- producto 200: 'Sin Champiñon MD' -> 'Sin ChampiÃ±on MD'   (asi lo tienen 8 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C383C2B16F6E204D44') WHERE idproducto = 200;

-- producto 201: 'Sin Champiñon PZ' -> 'Sin ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C383C2B16F6E20505A') WHERE idproducto = 201;

-- producto 202: 'Sin Champiñon XG' -> 'Sin ChampiÃ±on XG'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204368616D7069C383C2B16F6E205847') WHERE idproducto = 202;

-- producto 207: 'Sin Maíz Tierno GD' -> 'Sin MaÃ­z Tierno GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204D61C383C2AD7A20546965726E6F204744') WHERE idproducto = 207;

-- producto 208: 'Sin Maíz Tierno MD' -> 'Sin MaÃ­z Tierno MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204D61C383C2AD7A20546965726E6F204D44') WHERE idproducto = 208;

-- producto 209: 'Sin Maíz Tierno PZ' -> 'Sin MaÃ­z Tierno PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204D61C383C2AD7A20546965726E6F20505A') WHERE idproducto = 209;

-- producto 210: 'Sin Maíz Tierno XG' -> 'Sin MaÃ­z Tierno XG'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('53696E204D61C383C2AD7A20546965726E6F205847') WHERE idproducto = 210;

-- producto 307: 'Pollo Champiñon XL' -> 'Pollo ChampiÃ±on XL'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C383C2B16F6E20584C') WHERE idproducto = 307;

-- producto 312: 'Jamon y Champiñon XL' -> 'Jamon y ChampiÃ±on XL'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C383C2B16F6E20584C') WHERE idproducto = 312;

-- producto 322: 'Montañera XL' -> 'MontaÃ±era XL'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C383C2B165726120584C') WHERE idproducto = 322;

-- producto 327: 'Pepperoni Champiñon XL' -> 'Pepperoni ChampiÃ±on XL'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C383C2B16F6E20584C') WHERE idproducto = 327;

-- producto 334: 'Pollo Champiñon GD' -> 'Pollo ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C383C2B16F6E204744') WHERE idproducto = 334;

-- producto 339: 'Jamon y Champiñon GD' -> 'Jamon y ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C383C2B16F6E204744') WHERE idproducto = 339;

-- producto 349: 'Montañera GD' -> 'MontaÃ±era GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C383C2B1657261204744') WHERE idproducto = 349;

-- producto 354: 'Pepperoni Champiñon GD' -> 'Pepperoni ChampiÃ±on GD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C383C2B16F6E204744') WHERE idproducto = 354;

-- producto 361: 'Pollo Champiñon MD' -> 'Pollo ChampiÃ±on MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C383C2B16F6E204D44') WHERE idproducto = 361;

-- producto 366: 'Jamon y Champiñon MD' -> 'Jamon y ChampiÃ±on MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C383C2B16F6E204D44') WHERE idproducto = 366;

-- producto 376: 'Montañera MD' -> 'MontaÃ±era MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C383C2B1657261204D44') WHERE idproducto = 376;

-- producto 381: 'Pepperoni Champiñon MD' -> 'Pepperoni ChampiÃ±on MD'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C383C2B16F6E204D44') WHERE idproducto = 381;

-- producto 388: 'Pollo Champiñon PZ' -> 'Pollo ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('506F6C6C6F204368616D7069C383C2B16F6E20505A') WHERE idproducto = 388;

-- producto 393: 'Jamon y Champiñon PZ' -> 'Jamon y ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4A616D6F6E2079204368616D7069C383C2B16F6E20505A') WHERE idproducto = 393;

-- producto 403: 'Montañera PZ' -> 'MontaÃ±era PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('4D6F6E7461C383C2B165726120505A') WHERE idproducto = 403;

-- producto 408: 'Pepperoni Champiñon PZ' -> 'Pepperoni ChampiÃ±on PZ'   (asi lo tienen 9 de 11)
UPDATE producto SET descripcion = UNHEX('5065707065726F6E69204368616D7069C383C2B16F6E20505A') WHERE idproducto = 408;

-- item_inventario 11: 'Masa Pequeña' -> 'Masa PequeÃ±a'   (asi lo tienen 9 de 11)
UPDATE item_inventario SET nombre_item = UNHEX('4D617361205065717565C383C2B161') WHERE iditem = 11;

-- item_inventario 24: 'Champiñon' -> 'ChampiÃ±on'   (asi lo tienen 9 de 11)
UPDATE item_inventario SET nombre_item = UNHEX('4368616D7069C383C2B16F6E') WHERE iditem = 24;

-- item_inventario 37: 'Caja Pequeña' -> 'Caja PequeÃ±a'   (asi lo tienen 9 de 11)
UPDATE item_inventario SET nombre_item = UNHEX('43616A61205065717565C383C2B161') WHERE iditem = 37;

-- item_inventario 44: 'Papel Parafinado pequeño' -> 'Papel Parafinado pequeÃ±o'   (asi lo tienen 9 de 11)
UPDATE item_inventario SET nombre_item = UNHEX('506170656C205061726166696E61646F207065717565C383C2B16F') WHERE iditem = 44;

COMMIT;

-- ---------------------------------------------------------------------------
-- HOMOLOGACION DE LA COMPOSICION DE PRODUCTOS - LaMota
--
-- Se corre en la base de ESTA tienda (tiendaamericana).
--
-- Lleva item_inventario_x_producto al valor que tienen la mayoria de las once
-- tiendas. No se toma Manrique como referencia: Manrique tambien tiene errores
-- propios. Para cada receta se usa el valor en que coinciden mas tiendas.
--
-- Correcciones en esta tienda: 29
--
-- Donde la cantidad cambia se borra y se vuelve a insertar UNA fila, en vez de
-- actualizar: si el par venia repetido, un UPDATE dejaria las dos filas y el POS
-- seguiria descontando doble -recorre todas las filas del producto-.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- Americana Especial XL / Cabano : 70 -> 53   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 314 AND iditem = 13;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (314, 13, 53);

-- Americana Especial XL / ChampiÇñon : 85 -> 150   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 314 AND iditem = 24;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (314, 24, 150);

-- Americana Especial XL / Pasta : 100 -> 70   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 314 AND iditem = 26;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (314, 26, 70);

-- Americana Especial XL / Queso : 440 -> 430   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 314 AND iditem = 28;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (314, 28, 430);

-- Americana Especial GD / Cabano : 50 -> 45   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 341 AND iditem = 13;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (341, 13, 45);

-- Americana Especial GD / Salami : 70 -> 65   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 341 AND iditem = 19;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (341, 19, 65);

-- Americana Especial GD / ChampiÇñon : 75 -> 100   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 341 AND iditem = 24;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (341, 24, 100);

-- Americana Especial GD / Pasta : 85 -> 60   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 341 AND iditem = 26;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (341, 26, 60);

-- Americana Especial GD / Queso : 340 -> 330   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 341 AND iditem = 28;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (341, 28, 330);

-- Americana Especial MD / Cabano : 40 -> 35   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 368 AND iditem = 13;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (368, 13, 35);

-- Americana Especial MD / ChampiÇñon : 65 -> 90   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 368 AND iditem = 24;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (368, 24, 90);

-- Americana Especial MD / Pasta : 70 -> 40   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 368 AND iditem = 26;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (368, 26, 40);

-- Americana Especial PZ / Cabano : 25 -> 20   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 395 AND iditem = 13;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (395, 13, 20);

-- Americana Especial PZ / Salami : 40 -> 30   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 395 AND iditem = 19;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (395, 19, 30);

-- Americana Especial PZ / ChampiÇñon : 45 -> 50   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 395 AND iditem = 24;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (395, 24, 50);

-- Americana Especial PZ / Pasta : 50 -> 20   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 395 AND iditem = 26;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (395, 26, 20);

-- Americana Especial PZ / Queso : 105 -> 100   (asi lo tienen 10 de 11)
DELETE FROM item_inventario_x_producto WHERE idproducto = 395 AND iditem = 28;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (395, 28, 100);

-- Hawaiana Artesanal GD / Queso : AUSENTE -> 330   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (586, 28, 330);

-- Hawaiana Artesanal GD / Caja Grande : AUSENTE -> 1   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (586, 35, 1);

-- Hawaiana Artesanal MD / Pi¤a Artesanal : AUSENTE -> 110   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (587, 142, 110);

-- Hawaiana Artesanal MD / Jamon : AUSENTE -> 38   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (587, 16, 38);

-- Hawaiana Artesanal MD / Pasta : AUSENTE -> 40   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (587, 26, 40);

-- Hawaiana Artesanal MD / Queso : AUSENTE -> 230   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (587, 28, 230);

-- Hawaiana Artesanal MD / Caja Mediana : AUSENTE -> 1   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (587, 36, 1);

-- Hawaiana Artesanal PZ / Pi¤a Artesanal : AUSENTE -> 60   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (588, 142, 60);

-- Hawaiana Artesanal PZ / Jamon : AUSENTE -> 22   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (588, 16, 22);

-- Hawaiana Artesanal PZ / Pasta : AUSENTE -> 20   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (588, 26, 20);

-- Hawaiana Artesanal PZ / Queso : AUSENTE -> 100   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (588, 28, 100);

-- Hawaiana Artesanal PZ / Caja PequeÇña : AUSENTE -> 1   (asi lo tienen 10 de 11)
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (588, 37, 1);

-- Revise el conteo antes de confirmar.
SELECT COUNT(*) AS filas_despues FROM item_inventario_x_producto;

COMMIT;

-- ---------------------------------------------------------------------------
-- HOMOLOGACION: LOS CINCO CASOS QUE FALTABAN DECIDIR
--
-- Se corre en la base de CADA UNA de las once tiendas (tiendaamericana).
-- Es el mismo archivo para todas: el estado final debe ser identico, y cada
-- bloque borra antes de insertar, asi que correrlo dos veces no cambia nada.
--
-- Decidido por Juan David el 2026-09-17.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- ===========================================================================
-- 1. Vaso Gaseosa10oz descuenta 0,2 de la Gaseosa de 1.5 lts
--
-- Lo hacian 6 tiendas y 5 no. Se deja en las once.
-- Faltaba en: Manrique, Calasanz, San Antonio, Manrique Piloto y Niquia.
-- ===========================================================================
DELETE FROM item_inventario_x_producto WHERE idproducto = 413 AND iditem = 1;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (413, 1, 0.2);

-- ===========================================================================
-- 2 y 3. La fila de guacamole estaba colgada del producto equivocado
--
-- Adicion Guacamole GD (264) debia descontar 90 de Guacamole Kg, y esa fila
-- solo la tenian Manrique y Niquia. Las otras nueve tenian esos mismos 90 de
-- guacamole colgados de Adicion Champinon MD (64), donde no van.
--
-- No es coincidencia: las tiendas que tienen una NO tienen la otra. Alguien
-- cargo la fila contra el producto de al lado, y aqui se devuelve a su sitio.
--
-- Adicion Champinon MD ya descuenta su champinon -45, coherente con la
-- familia: XL 60, GD 50, MD 45, PZ 30-, asi que la fila de guacamole solo
-- sobra. Y 90 encaja en la familia del guacamole: XL 125, GD 90, MD 60, PZ 25.
-- ===========================================================================
DELETE FROM item_inventario_x_producto WHERE idproducto = 264 AND iditem = 68;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (264, 68, 90);

DELETE FROM item_inventario_x_producto WHERE idproducto = 64 AND iditem = 68;

-- ===========================================================================
-- 4. Las dos Sodas nuevas descuentan 1 de Soda
--
-- Solo las tenia Calasanz. Se dejan en las once.
-- ===========================================================================
DELETE FROM item_inventario_x_producto WHERE idproducto = 595 AND iditem = 127;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (595, 127, 1);

DELETE FROM item_inventario_x_producto WHERE idproducto = 596 AND iditem = 127;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (596, 127, 1);

-- ===========================================================================
-- REVISE ESTO ANTES DE CONFIRMAR
--
-- Los cinco pares tienen que quedar: 0.2, 90, sin fila, 1 y 1.
-- Los pares repetidos tienen que ser 1: el Guacamole Kg / Con Guacamole MD,
-- que viene de antes y esta en las once tiendas.
-- ===========================================================================
SELECT (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 413 AND iditem = 1)   AS vaso_gaseosa,
       (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 264 AND iditem = 68)  AS adic_guacamole_gd,
       (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 64 AND iditem = 68)   AS champinon_con_guacamole,
       (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 595 AND iditem = 127) AS soda_rojos,
       (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 596 AND iditem = 127) AS soda_amarillos,
       (SELECT COUNT(*) FROM item_inventario_x_producto)     AS filas,
       (SELECT COUNT(*) FROM (SELECT idproducto, iditem FROM item_inventario_x_producto
                               GROUP BY idproducto, iditem HAVING COUNT(*) > 1) d) AS pares_repetidos;

COMMIT;

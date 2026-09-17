-- ---------------------------------------------------------------------------
-- CON GUACAMOLE MD: UNA SOLA FILA, DE 60
--
-- Se corre en la base de CADA UNA de las once tiendas (tiendaamericana).
-- Mismo archivo para todas. Borra antes de insertar, asi que correrlo dos
-- veces no cambia nada.
--
-- POR QUE
--
-- El par (273, 68) estaba DOS veces en las once tiendas, con 25 y con 60.
-- ItemInventarioProductoDAO.obtenerItemsInventarioProducto recorre todas las
-- filas del producto y arma un descuento por cada una, asi que cada
-- "Con Guacamole MD" venia descontando 85 gramos.
--
-- Se deja 60 porque es el que encaja en la familia del guacamole: la Adicion
-- Guacamole va XL 125, GD 90, MD 60, PZ 25. Decidido por Juan David el
-- 2026-09-17.
--
-- Con esto no queda ni un par repetido en ninguna tienda.
-- ---------------------------------------------------------------------------

START TRANSACTION;

DELETE FROM item_inventario_x_producto WHERE idproducto = 273 AND iditem = 68;
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad) VALUES (273, 68, 60);

-- Tiene que quedar: 60, 1228 filas y CERO pares repetidos.
SELECT (SELECT IFNULL(GROUP_CONCAT(cantidad),'sin fila') FROM item_inventario_x_producto
         WHERE idproducto = 273 AND iditem = 68) AS con_guacamole_md,
       (SELECT COUNT(*) FROM item_inventario_x_producto) AS filas,
       (SELECT COUNT(*) FROM (SELECT idproducto, iditem FROM item_inventario_x_producto
                               GROUP BY idproducto, iditem HAVING COUNT(*) > 1) d) AS pares_repetidos;

COMMIT;

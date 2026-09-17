-- ---------------------------------------------------------------------------
-- LAS OCHO BASES QUE NO TIENEN CAJA
--
-- Se corre en el CENTRAL (172.19.0.25), sobre el maestro. Va DESPUES de
-- 2026_09_17_04_caja_doble_en_sabores.sql y es la otra mitad de ese cambio.
--
-- POR QUE
--
-- El 04 le quito la caja a los sabores porque la base siempre la trae. Ocho
-- bases NO la traen:
--
--   GD   417 Grande Postobon Ifood    470 Pizza GD Estofada
--        478 Pizza GD Estofada Empl   503 Pizza GD Estofada Plat
--   MD   469 Pizza MD Estofada        471 Pizza MD Esponjada
--        477 Pizza MD Estofada Empl   502 Pizza MD Estofada Plat
--
-- Una base normal es masa mas caja -Pizza GD: Masa Grande y Caja Grande-.
-- Estas traen masa y queso, pero ninguna caja. Hasta ahora no se notaba
-- porque la caja se la ponia el sabor; con el 04 solo, estas pizzas saldrian
-- sin descontar caja ninguna, que es cambiar un error por el contrario.
--
-- Y se venden: en Manrique, 30 dias, la GD Estofada 17, la MD Estofada 14, la
-- MD Estofada Plat 10 y la GD Estofada Plat 8. Las 49 llevan sabor.
--
-- QUE CAJA LE TOCA A CADA UNA
--
-- La del tamano, que es la misma que traia el sabor. Verificado contra la
-- receta de la tienda antes de decidir: los 24 sabores XL llevan la caja 34,
-- los 24 GD la 35, los 24 MD la 36 y los 24 PZ la 37, sin una sola excepcion.
-- Aca solo hay bases GD y MD, asi que van la 35 y la 36.
--
-- ES IDEMPOTENTE
--
-- El INSERT va con NOT EXISTS en vez de ON DUPLICATE KEY UPDATE. Correrlo
-- dos veces no inserta nada la segunda, y no depende de VALUES().
-- ---------------------------------------------------------------------------

USE inventarioamericana;

START TRANSACTION;

INSERT INTO producto_insumo (idproducto, iditem, cantidad)
SELECT c.idproducto,
       IF(c.tamano = 'GD', 35, 36),
       1
  FROM producto_catalogo c
 WHERE c.tipo_producto = 'P'
   AND c.tamano IN ('GD', 'MD')
   AND NOT EXISTS (SELECT 1 FROM producto_insumo pi
                    WHERE pi.idproducto = c.idproducto
                      AND pi.iditem IN (34, 35, 36, 37, 79));

COMMIT;

-- Como quedo: ninguna base de pizza deberia quedar sin caja
SELECT c.tamano, COUNT(*) AS bases_sin_caja
  FROM producto_catalogo c
 WHERE c.tipo_producto = 'P'
   AND NOT EXISTS (SELECT 1 FROM producto_insumo pi
                    WHERE pi.idproducto = c.idproducto
                      AND pi.iditem IN (34, 35, 36, 37, 79))
 GROUP BY c.tamano;

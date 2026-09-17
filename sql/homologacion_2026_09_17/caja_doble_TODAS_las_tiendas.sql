-- ---------------------------------------------------------------------------
-- LA CAJA QUE SE DESCUENTA DOS VECES - SE CORRE EN CADA TIENDA
--
-- Va en la base de la tienda (tiendaamericana), en las once. Es el mismo
-- cambio que ya quedo en el maestro del central con los scripts 04 y 06.
--
-- QUE PASABA
--
-- Una pizza se vende en dos lineas: la base -Pizza XL- y el sabor -Hawaiana
-- XL-. Las dos descontaban la caja del tamano, asi que cada pizza descontaba
-- DOS cajas.
--
-- Medido en Manrique, pedido 418114: dos pizzas, masa 1+1 = 2 correcto, y
-- caja 1+1+0,5x4 = cuatro. En 30 dias la caja extragrande salio 736 veces por
-- la base y 703 por el sabor: casi toda pizza pasaba por las dos. Al costo de
-- hoy son unos 3,9 millones al mes de consumo teorico inflado por tienda.
--
-- Y no se queda en el costeo: la varianza se mide contra el teorico, asi que
-- un teorico con el doble de cajas muestra un sobrante que no existe, y ese
-- sobrante entra al total tapando faltantes reales de otros insumos.
--
-- LOS DOS PASOS, QUE VAN JUNTOS
--
-- 1. Se le quita la caja a los 24 sabores de cada tamano -96 filas-. Se le
--    quita al sabor y no a la base porque la base siempre esta: de 5.220
--    lineas de sabor en 30 dias, las 5.220 cuelgan de una base, ninguna se
--    vende sola.
--
-- 2. Se le PONE la caja a las ocho bases que no la tienen. Las Estofada y la
--    Esponjada traen masa y queso pero ninguna caja, y hasta ahora se la
--    ponia el sabor. Con el paso 1 solo, esas pizzas quedarian sin descontar
--    caja, que es cambiar un error por el contrario. Se venden: 49 unidades
--    en 30 dias en Manrique, todas con sabor.
--
-- La caja que les toca es la del tamano, la misma que traia el sabor:
-- verificado que los 24 sabores XL llevan la 34, los GD la 35, los MD la 36
-- y los PZ la 37, sin excepcion. Aca solo hay bases GD y MD.
--
-- QUE NO SE TOCA
--
-- La caja pequena tambien la descuentan ocho productos que NO son sabores
-- -deditos y demas-. Por eso el borrado va restringido a tipo_producto = 'D'.
--
-- ES IDEMPOTENTE
--
-- El DELETE no encuentra nada la segunda vez y el INSERT va con NOT EXISTS.
-- Correrlo dos veces deja lo mismo.
--
-- COMO QUEDA
--
-- De 1.228 filas a 1.140: menos 96 de los sabores, mas 8 de las bases.
-- ---------------------------------------------------------------------------

USE tiendaamericana;

START TRANSACTION;

-- 1. Quitarle la caja a los sabores
DELETE x FROM item_inventario_x_producto x
  JOIN producto p ON p.idproducto = x.idproducto
 WHERE x.iditem IN (34, 35, 36, 37)
   AND p.tipo_producto = 'D';

-- 2. Ponersela a las bases que no la tienen
INSERT INTO item_inventario_x_producto (idproducto, iditem, cantidad)
SELECT p.idproducto,
       IF(p.tamano = 'GD', 35, 36),
       1
  FROM producto p
 WHERE p.tipo_producto = 'P'
   AND p.tamano IN ('GD', 'MD')
   AND NOT EXISTS (SELECT 1 FROM item_inventario_x_producto x
                    WHERE x.idproducto = p.idproducto
                      AND x.iditem IN (34, 35, 36, 37, 79));

COMMIT;

-- Como quedo
SELECT (SELECT COUNT(*) FROM item_inventario_x_producto) AS filas,
       (SELECT COUNT(*) FROM item_inventario_x_producto x
          JOIN producto p ON p.idproducto = x.idproducto
         WHERE x.iditem IN (34,35,36,37) AND p.tipo_producto = 'D') AS caja_en_sabores,
       (SELECT COUNT(*) FROM producto p
         WHERE p.tipo_producto = 'P' AND NOT EXISTS (
           SELECT 1 FROM item_inventario_x_producto x
            WHERE x.idproducto = p.idproducto AND x.iditem IN (34,35,36,37,79))) AS bases_sin_caja;

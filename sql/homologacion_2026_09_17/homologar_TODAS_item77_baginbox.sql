-- ---------------------------------------------------------------------------
-- BAG IN BOX (iditem 77) EN LAS ONCE TIENDAS
--
-- Se corre en la base de CADA UNA de las once tiendas (tiendaamericana).
-- Mismo archivo para todas.
--
-- POR QUE
--
-- El item 77 existia SOLO en Calasanz, que lo cuenta todos los dias: 373 dias
-- en item_inventario_varianza y 372 en item_inventario_historico. Por eso no
-- se podia resolver borrandolo -habria dejado 745 filas huerfanas- y se
-- resuelve al reves, agregandolo a las otras diez.
--
-- SE CORRIGE LA CATEGORIA DE PASO
--
-- En Calasanz la fila dice categoria 'Bebidass', con doble s. Es un error de
-- digitacion: es la unica fila con ese valor en toda la cadena, y las otras
-- seis bebidas dicen 'Bebidas'. Se inserta con 'Bebidas' y se corrige tambien
-- en Calasanz, para que las once queden iguales Y bien.
--
-- POR QUE ON DUPLICATE KEY UPDATE Y NO DELETE + INSERT
--
-- En Calasanz la fila YA existe y tiene 745 filas de historia colgando. Un
-- DELETE, aunque se volviera a insertar enseguida, es un riesgo que no hay que
-- correr por un cambio de categoria. Asi el script sirve para las once: donde
-- no esta la crea, y donde esta solo le corrige la categoria.
--
-- La cantidad NO se toca en la tienda que ya lo tiene: donde se crea entra en
-- cero, que es lo correcto para un item que se empieza a contar.
-- ---------------------------------------------------------------------------

START TRANSACTION;

INSERT INTO item_inventario
  (iditem, nombre_item, unidad_medida, cantidad, manejacanastas, cantidadxcanasta,
   nombrecontenedor, categoria, varianza_resumida, orden, orden_ingreso, orden_retiro,
   consumo_inv, habilitado, idgrupo, numero_fila, controla_cantidad)
VALUES
  (77, 'Bag In Box', 'unidad', 0, 'N', 0, 'SIN CONTENEDOR', 'Bebidas',
   _binary '0', 78, 78, 78, _binary '0', _binary '1', 0, NULL, _binary '0')
ON DUPLICATE KEY UPDATE categoria = 'Bebidas';

-- Tiene que quedar: 127 items, el 77 como Bag In Box / Bebidas, y ninguna
-- fila con la categoria mal escrita.
SELECT (SELECT COUNT(*) FROM item_inventario) AS items,
       (SELECT CONCAT(nombre_item, ' / ', categoria) FROM item_inventario WHERE iditem = 77) AS item77,
       (SELECT COUNT(*) FROM item_inventario WHERE categoria = 'Bebidass') AS con_error;

COMMIT;

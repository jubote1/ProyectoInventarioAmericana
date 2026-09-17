-- ---------------------------------------------------------------------------
-- LA CAJA SE DESCUENTA DOS VECES POR PIZZA
--
-- Se corre en el CENTRAL (172.19.0.25), sobre el maestro. NO toca ninguna
-- tienda: el cambio llega a las tiendas cuando alguien replique desde la
-- pantalla de Costeo y Composicion, y no antes.
--
-- ESTA PENDIENTE DE DECISION. No correr sin que operacion lo confirme.
--
-- QUE PASA HOY
--
-- Una pizza se vende en dos lineas: la base -Pizza XL, tipo P- y el sabor
-- -Hawaiana XL, tipo D-. Las dos descuentan la caja del tamano. Asi que cada
-- pizza descuenta DOS cajas.
--
-- Medido en Manrique, pedido 418114 del ultimo Hawaiana XL: dos pizzas,
-- masa 1+1 = 2 correcto, y caja 1+1+0,5+0,5+0,5+0,5 = 4. Deberian ser dos.
--
-- Y no es un caso suelto. En los ultimos 30 dias, en Manrique:
--
--   Caja Extra Grande   736 por la base   703 por el sabor
--   Caja Grande         704               690
--   Caja Mediana        587               560
--   Caja Pequena        584               570
--
-- Las dos columnas son casi iguales porque practicamente cada pizza pasa por
-- las dos. Al costo de hoy eso son unos 3,9 millones al mes de consumo
-- teorico inflado, en una sola tienda. Las once tienen la misma receta.
--
-- POR QUE IMPORTA MAS ALLA DEL COSTEO
--
-- La varianza se mide contra el consumo teorico. Si el teorico gasta el doble
-- de cajas, la varianza muestra un SOBRANTE de cajas que no existe, y ese
-- sobrante entra al total de la tienda tapando faltantes reales de otros
-- insumos. El reporte semanal de varianza lo viene arrastrando.
--
-- POR QUE SE LE QUITA AL SABOR Y NO A LA BASE
--
-- Porque la base siempre esta. Verificado en Manrique, ultimos 30 dias: de
-- 5.220 lineas de sabor, las 5.220 traen iddetalle_pedido_master apuntando a
-- una base. Ninguna se vende sola. Quitarle la caja a la base, en cambio,
-- dejaria sin caja cualquier pizza que se venda sin sabor.
--
-- Ademas la base es la que define el tamano, que es lo que decide cual caja.
--
-- QUE NO SE TOCA
--
-- La caja pequena tambien la descuentan ocho productos que NO son sabores
-- -deditos y demas, 504 unidades en el mes-. Esos llevan su caja de verdad y
-- por eso el borrado va restringido a tipo_producto = 'D'.
-- ---------------------------------------------------------------------------

USE inventarioamericana;

START TRANSACTION;

-- Quedan 96 filas por fuera: 24 sabores x 4 tamanos de caja.
DELETE pi FROM inventarioamericana.producto_insumo pi
  JOIN inventarioamericana.producto_catalogo c ON c.idproducto = pi.idproducto
 WHERE pi.iditem IN (34, 35, 36, 37)
   AND c.tipo_producto = 'D';

COMMIT;

-- Como quedo: por tamano, cuantos productos descuentan cada caja y de que tipo
SELECT pi.iditem,
       SUM(c.tipo_producto = 'P') AS en_bases,
       SUM(c.tipo_producto = 'D') AS en_sabores,
       SUM(c.tipo_producto NOT IN ('P','D')) AS en_otros
  FROM inventarioamericana.producto_insumo pi
  JOIN inventarioamericana.producto_catalogo c ON c.idproducto = pi.idproducto
 WHERE pi.iditem IN (34, 35, 36, 37)
 GROUP BY pi.iditem ORDER BY pi.iditem;

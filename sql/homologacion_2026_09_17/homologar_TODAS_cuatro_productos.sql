-- ---------------------------------------------------------------------------
-- LOS CUATRO PRODUCTOS QUE FALTABAN DECIDIR
--
-- Se corre en la base de CADA UNA de las once tiendas (tiendaamericana).
-- Mismo archivo para todas. Es idempotente.
--
-- Decidido por Juan David el 2026-09-17.
--
-- Los nombres se escriben con UNHEX de los bytes de la tienda de referencia,
-- no como texto: la codificacion de la conexion es justo lo que esta mal en
-- esta cadena y no hay que darle otra oportunidad.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- ===========================================================================
-- 411  Domicilio: queda en $3.000 en las once
--
-- Calasanz y Niquia lo tenian en $2.000, y el precio no esta solo en el
-- nombre: precio1 tambien decia 2000. Se cambian LOS DOS, porque cambiar solo
-- el texto dejaria el boton diciendo $3.000 y cobrando $2.000.
--
-- OJO: esto SUBE el domicilio 1.000 pesos en Calasanz y Niquia.
-- ===========================================================================
UPDATE producto
   SET descripcion = UNHEX('446F6D6963696C696F202824332E30303029'),
       precio1 = 3000
 WHERE idproducto = 411;

-- ===========================================================================
-- 429  Queda como "Grande DIDI" en las once
--
-- EL PRECIO NO SE TOCA, y hay que saberlo: las nueve tiendas que se renombran
-- lo tienen en 44.900 y las dos que ya se llamaban Grande DIDI lo tienen en
-- 37.500. Al renombrar, el mismo nombre queda con dos precios en la cadena.
--
-- No se unifico el precio porque eso no se pidio y son 7.400 pesos de
-- diferencia: si deben quedar todos en 37.500 -o en 44.900- es una decision
-- de plata aparte.
-- ===========================================================================
UPDATE producto
   SET descripcion = UNHEX('4772616E64652044494449')
 WHERE idproducto = 429;

-- ===========================================================================
-- 453  Queda como "Manzana Pet 250 mL" en las once
--
-- Solo Manrique Piloto decia "Pepsi Pet 250 mL". El precio es el mismo en las
-- once -1.500-, asi que aqui no hay nada de plata que decidir.
-- ===========================================================================
UPDATE producto
   SET descripcion = UNHEX('4D616E7A616E612050657420323530206D4C')
 WHERE idproducto = 453;

-- ===========================================================================
-- 507  Queda como "Soda Saborizada Segunda 50%" en las once
--
-- Niquia lo tenia como "Soda Saborizada2x1" a 8.000. Se deja como las otras,
-- que incluye el precio: son dos promociones distintas y dejar el nombre de
-- una con el precio de la otra seria peor que como estaba.
--
-- OJO: esto sube el producto de 8.000 a 15.000 en Niquia.
-- ===========================================================================
UPDATE producto
   SET descripcion = UNHEX('536F6461205361626F72697A61646120536567756E646120353025'),
       precio1 = 15000
 WHERE idproducto = 507;

-- Tiene que quedar igual en las once.
SELECT idproducto, descripcion, precio1 FROM producto
 WHERE idproducto IN (411,429,453,507) ORDER BY idproducto;

COMMIT;

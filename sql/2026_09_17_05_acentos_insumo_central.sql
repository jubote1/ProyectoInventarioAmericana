-- ---------------------------------------------------------------------------
-- LOS ACENTOS DE inventarioamericana.insumo
--
-- Se corre en el CENTRAL (172.19.0.25). Solo cambia texto: no toca costos, ni
-- ids, ni recetas, ni una sola tienda.
--
-- QUE PASA
--
-- El nombre del insumo se ve mal en la pantalla. Y se perdio de dos formas
-- distintas, que conviene no confundir:
--
--   "Pina Artesanal"  la enye se cambio por una ene
--   "Caja Peque?a"    la enye se volvio un signo de interrogacion, byte 3F
--
-- El segundo caso es una letra que ya se perdio AL GUARDAR. No es un problema
-- de como se muestra: el dato esta daÃ±ado en la tabla.
--
-- POR QUE PASA
--
-- inventarioamericana.insumo.nombre_insumo es latin1 y
-- tiendaamericana.item_inventario.nombre_item es utf8. Cuando el nombre viajo
-- de la tienda al central por una conexion que no declaraba codificacion,
-- MySQL no pudo representar la enye y la reemplazo por "?".
--
-- COMO SE CORRIGE
--
-- Se toma el nombre bueno de la tienda -Manrique, la de referencia-, se pasa
-- a latin1 y se escribe con UNHEX de esos bytes, con SET NAMES latin1 para
-- que la conexion no vuelva a convertir nada por el camino. Escribirlo como
-- texto es exactamente lo que causo el daÃ±o.
--
-- QUE NO SE TOCA
--
-- Solo entran los nombres que son EL MISMO nombre mal escrito. Se comparan
-- sin tildes y en minuscula, y el "?" se compara como comodin de una letra.
-- Si aun asi difieren, son dos nombres distintos y se dejan quietos.
--
-- ES IDEMPOTENTE: cada UPDATE deja un valor fijo. Correrlo dos veces da igual.
-- ---------------------------------------------------------------------------

SET NAMES latin1;

START TRANSACTION;

-- Champi?on  ->  Champiñon
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('4368616D7069F16F6E') WHERE idinsumo = 24;

-- Pina  ->  Piña
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('5069F161') WHERE idinsumo = 27;

-- Caja Peque?a  ->  Caja Pequeña
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('43616A61205065717565F161') WHERE idinsumo = 37;

-- Papel Parafinado peque?o  ->  Papel Parafinado pequeño
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('506170656C205061726166696E61646F207065717565F16F') WHERE idinsumo = 44;

-- Bolsa kilo  ->  Bolsa Kilo
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('426F6C7361204B696C6F') WHERE idinsumo = 82;

-- Pina Artesanal  ->  Piña Artesanal
UPDATE inventarioamericana.insumo SET nombre_insumo = UNHEX('5069F161204172746573616E616C') WHERE idinsumo = 142;

COMMIT;

SET NAMES utf8mb4;
SELECT idinsumo, nombre_insumo FROM inventarioamericana.insumo
 WHERE idinsumo IN (24,27,37,44,82,142) ORDER BY idinsumo;

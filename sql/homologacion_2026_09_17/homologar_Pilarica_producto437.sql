-- ---------------------------------------------------------------------------
-- PILARICA: LE FALTA EL PRODUCTO 437 Y TIENE 6.660 VENTAS CONTRA EL
--
-- Se corre SOLO en Pilarica (192.168.9.39, base tiendaamericana).
--
-- QUE PASA
--
-- "No desea Gaseosa" esta en el id 437 en las otras diez tiendas. En Pilarica
-- la fila de producto quedo en el id 439, y el 437 no existe.
--
-- Pero las ventas SI van por el 437: detalle_pedido tiene 6.660 filas con
-- idproducto = 437 y CERO con 439. O sea que en Pilarica esas 6.660 ventas
-- apuntan a un producto que no esta en su propio catalogo, y cualquier
-- consulta que cruce detalle_pedido con producto las pierde.
--
-- QUE HACE
--
-- 1. Inserta el 437 con la fila exacta de Itagui -sacada con mysqldump, sin
--    transcribir nada a mano, para que las tildes no se dañen-.
-- 2. Borra el 439, que no tiene ni una venta.
--
-- Con esto Pilarica queda igual a las otras diez y las 6.660 ventas dejan de
-- estar huerfanas.
-- ---------------------------------------------------------------------------

START TRANSACTION;

-- Por si se corre dos veces.
DELETE FROM producto WHERE idproducto = 437;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

INSERT INTO `producto` (`idproducto`, `descripcion`, `descripcion_corta`, `impresion`, `textoboton`, `colorboton`, `idpreguntaforzada1`, `idpreguntaforzada2`, `idpreguntaforzada3`, `idpreguntaforzada4`, `idpreguntaforzada5`, `precio1`, `precio2`, `precio3`, `precio4`, `precio5`, `precio6`, `precio7`, `precio8`, `precio9`, `precio10`, `precio_puntos`, `souvenir`, `impresion_comanda`, `tipo_producto`, `tamano`, `modificadorcon`, `modificadorsin`, `imagen`, `mod_con_pregunta`, `dev_inventario_siempre`, `genera_observacion`, `multiple`, `cantidad_con`, `empleado`, `no_venta`, `precio_pila`, `control_comercial`, `comision`, `control_diario`, `iddesecho`, `promocion`, `id_producto_depende`) VALUES (437,'No desea Gaseosa','No desea Gaseosa','No desea Gaseosa','No desea Gaseosa','',0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,'N','','G','ADI',0,0,NULL,0x30,'N','N',0,0,'N',0x30,0x30,0x30,0,'N',0,'N',0);
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- El 439 no tiene ventas. Se verifica y se borra.
DELETE FROM producto WHERE idproducto = 439;

-- Tiene que quedar: 437 con "No desea Gaseosa", 439 sin fila, 602 productos.
SELECT (SELECT IFNULL(GROUP_CONCAT(descripcion),'sin fila') FROM producto WHERE idproducto=437) AS p437,
       (SELECT IFNULL(GROUP_CONCAT(descripcion),'sin fila') FROM producto WHERE idproducto=439) AS p439,
       (SELECT COUNT(*) FROM producto) AS total_productos,
       (SELECT COUNT(*) FROM detalle_pedido d
         WHERE NOT EXISTS (SELECT 1 FROM producto p WHERE p.idproducto = d.idproducto)) AS ventas_huerfanas;

COMMIT;

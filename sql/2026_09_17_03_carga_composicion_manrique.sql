-- ---------------------------------------------------------------------------
-- CARGA INICIAL DE LA COMPOSICION, DESDE MANRIQUE
--
-- Se corre UNA VEZ en el CENTRAL (172.19.0.25), despues de
-- 2026_09_17_02_composicion_productos.sql. No toca ninguna tienda.
--
-- POR QUE MANRIQUE Y NO OTRA
--
-- Es la tienda de referencia con la que se homologaron las otras diez el
-- 2026-09-17. Al cierre de esa homologacion las once quedaron identicas en
-- producto -602 filas-, item_inventario -127- e item_inventario_x_producto
-- -1.228-, asi que cargar desde Manrique es cargar lo que tienen todas.
--
-- POR QUE UNHEX Y NO EL NOMBRE ESCRITO
--
-- La columna dice utf8 pero en las tiendas hay filas con bytes latin1 sueltos.
-- Escribir el nombre como texto obliga a que cliente, conexion y columna
-- coincidan en codificacion, y es justo lo que fallaba: nueve tiendas tenian
-- la enye doblemente codificada. Copiando los bytes no hay forma de corromper
-- una tilde. Verificado antes de generar esto: los 147 nombres con acento son
-- UTF-8 valido y ninguno trae la firma de doble codificacion.
--
-- ES IDEMPOTENTE
--
-- ON DUPLICATE KEY UPDATE en las dos. Correrlo dos veces deja lo mismo.
--
-- LOS PARES REPETIDOS
--
-- La composicion sale agrupada por (idproducto, iditem) sumando la cantidad,
-- que es lo que hoy hace el POS: recorre todas las filas y arma un descuento
-- por cada una. En Manrique hoy no queda ninguno repetido -se corrigieron en
-- la homologacion-, pero se deja el agrupado por si se recarga desde otra.
-- ---------------------------------------------------------------------------

SET NAMES utf8mb4;

START TRANSACTION;

-- 1. EL CATALOGO --------------------------------------------------------------
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (1, UNHEX('416C6974617320424251'), 'ALITAS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (2, UNHEX('4C61736167C3B161204D69787461'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (3, UNHEX('4E756767657473'), 'NUGGETS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (4, UNHEX('44656469746F7320416D65726963616E6F73'), 'DEDITOS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (6, UNHEX('50697A7A6120584C'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (7, UNHEX('50697A7A61204744'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (8, UNHEX('50697A7A61204D44'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (9, UNHEX('50697A7A6120505A'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (10, UNHEX('4164696369C3B36E204D61697A20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (11, UNHEX('4164696369C3B36E205069C3B16120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (12, UNHEX('4164696369C3B36E20517565736F20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (13, UNHEX('4164696369C3B36E204A616DC3B36E20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (14, UNHEX('4164696369C3B36E2053616C616D7920584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (15, UNHEX('4164696369C3B36E2043686F72697A6F20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (16, UNHEX('4164696369C3B36E204368616D7069C3B16F6E20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (17, UNHEX('4164696369C3B36E204365626F6C6C6120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (18, UNHEX('4164696369C3B36E20546F6D61746520584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (19, UNHEX('4164696369C3B36E20546F63696E65746120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (20, UNHEX('41646963696F6E20436162616E6F20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (21, UNHEX('4164696369C3B36E205065707065726F6E6920584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (22, UNHEX('4164696369C3B36E20506F6C6C6F20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (23, UNHEX('4164696369C3B36E20546F63696E6F20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (24, UNHEX('4164696369C3B36E20506173746120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (25, UNHEX('4164696369C3B36E2050696D656E746F6E20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (26, UNHEX('4164696369C3B36E204163656974756E617320584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (27, UNHEX('4164696369C3B36E204361726E65204D6F6C69646120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (28, UNHEX('506570736920312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (29, UNHEX('55766120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (30, UNHEX('4D616E7A616E6120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (31, UNHEX('436F6C6F6D6269616E6120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (32, UNHEX('3720757020312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (33, UNHEX('4E6172616E6A6120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (34, UNHEX('4D616E7A616E6120506574203430306D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (35, UNHEX('436F6C6F6D6269616E612050657420343030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (36, UNHEX('50657073692050657420343030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (37, UNHEX('372075702050657420343030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (38, UNHEX('4D722054656120447572617A6E6F20353030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (39, UNHEX('224869742046727574617320353030206D4C22'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (40, UNHEX('4164696369C3B36E204D61697A204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (41, UNHEX('4164696369C3B36E205069C3B161204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (42, UNHEX('4164696369C3B36E20517565736F204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (43, UNHEX('4164696369C3B36E204A616DC3B36E204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (44, UNHEX('4164696369C3B36E2053616C616D79204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (45, UNHEX('4164696369C3B36E2043686F72697A6F204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (46, UNHEX('4164696369C3B36E204368616D7069C3B16F6E204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (47, UNHEX('4164696369C3B36E204365626F6C6C61204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (48, UNHEX('4164696369C3B36E20546F6D617465204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (49, UNHEX('4164696369C3B36E20546F63696E657461204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (50, UNHEX('41646963696F6E20436162616E6F204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (51, UNHEX('4164696369C3B36E205065707065726F6E69204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (52, UNHEX('4164696369C3B36E20506F6C6C6F204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (53, UNHEX('4164696369C3B36E20546F63696E6F204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (54, UNHEX('4164696369C3B36E205061737461204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (55, UNHEX('4164696369C3B36E2050696D656E746F6E204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (56, UNHEX('4164696369C3B36E204163656974756E6173204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (57, UNHEX('4164696369C3B36E204361726E65204D6F6C696461204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (58, UNHEX('4164696369C3B36E204D61697A204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (59, UNHEX('4164696369C3B36E205069C3B161204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (60, UNHEX('4164696369C3B36E20517565736F204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (61, UNHEX('4164696369C3B36E204A616DC3B36E204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (62, UNHEX('4164696369C3B36E2053616C616D79204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (63, UNHEX('4164696369C3B36E2043686F72697A6F204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (64, UNHEX('4164696369C3B36E204368616D7069C3B16F6E204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (65, UNHEX('4164696369C3B36E204365626F6C6C61204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (66, UNHEX('4164696369C3B36E20546F6D617465204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (67, UNHEX('4164696369C3B36E20546F63696E657461204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (68, UNHEX('41646963696F6E20436162616E6F204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (69, UNHEX('4164696369C3B36E205065707065726F6E69204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (70, UNHEX('4164696369C3B36E20506F6C6C6F204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (71, UNHEX('4164696369C3B36E20546F63696E6F204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (72, UNHEX('4164696369C3B36E205061737461204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (73, UNHEX('4164696369C3B36E2050696D656E746F6E204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (74, UNHEX('4164696369C3B36E204163656974756E6173204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (75, UNHEX('4164696369C3B36E204361726E65204D6F6C696461204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (76, UNHEX('4164696369C3B36E204D61697A20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (77, UNHEX('4164696369C3B36E205069C3B16120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (78, UNHEX('4164696369C3B36E20517565736F20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (79, UNHEX('4164696369C3B36E204A616DC3B36E20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (80, UNHEX('4164696369C3B36E2053616C616D7920505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (81, UNHEX('4164696369C3B36E2043686F72697A6F20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (82, UNHEX('4164696369C3B36E204368616D7069C3B16F6E20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (83, UNHEX('4164696369C3B36E204365626F6C6C6120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (84, UNHEX('4164696369C3B36E20546F6D61746520505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (85, UNHEX('4164696369C3B36E20546F63696E65746120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (86, UNHEX('41646963696F6E20436162616E6F20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (87, UNHEX('4164696369C3B36E205065707065726F6E6920505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (88, UNHEX('4164696369C3B36E20506F6C6C6F20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (89, UNHEX('4164696369C3B36E20546F63696E6F20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (90, UNHEX('4164696369C3B36E20506173746120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (91, UNHEX('4164696369C3B36E2050696D656E746F6E20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (92, UNHEX('4164696369C3B36E204163656974756E617320505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (93, UNHEX('4164696369C3B36E204361726E65204D6F6C69646120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (100, UNHEX('436F6E204A616DC3B36E205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (101, UNHEX('436F6E205069C3B161205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (102, UNHEX('436F6E205069C3B161204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (103, UNHEX('436F6E204A616DC3B36E204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (104, UNHEX('436F6E205069C3B161204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (105, UNHEX('436F6E204A616DC3B36E204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (106, UNHEX('436F6E205069C3B16120505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (107, UNHEX('436F6E204A616DC3B36E20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (110, UNHEX('4E6172616E6A6120506574203430306D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (111, UNHEX('486974204C756C6F20353030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (112, UNHEX('486974204D616E676F20353030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (113, UNHEX('486974204D6F726120353030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (114, UNHEX('486974204E6172616E6A6120353030206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (115, UNHEX('4D7220546561204C696D6F6E203530306D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (116, UNHEX('2255766120506574203430306D4C22'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (118, UNHEX('436F6E204163656974756E6173204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (119, UNHEX('436F6E204163656974756E6173204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (120, UNHEX('436F6E204163656974756E617320505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (121, UNHEX('436F6E204163656974756E6173205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (122, UNHEX('436F6E20436162616E6F204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (123, UNHEX('436F6E20436162616E6F204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (124, UNHEX('436F6E20436162616E6F20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (125, UNHEX('436F6E20436162616E6F205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (126, UNHEX('436F6E204361726E65204D6F6C696461204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (127, UNHEX('436F6E204361726E65204D6F6C696461204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (128, UNHEX('436F6E204361726E65204D6F6C69646120505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (129, UNHEX('436F6E204361726E65204D6F6C696461205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (130, UNHEX('436F6E204365626F6C6C61204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (131, UNHEX('436F6E204365626F6C6C61204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (132, UNHEX('436F6E204365626F6C6C6120505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (133, UNHEX('436F6E204365626F6C6C61205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (134, UNHEX('436F6E204368616D7069C3B16F6E204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (135, UNHEX('436F6E204368616D7069C3B16F6E204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (136, UNHEX('436F6E204368616D7069C3B16F6E20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (137, UNHEX('436F6E204368616D7069C3B16F6E205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (138, UNHEX('436F6E2043686F72697A6F204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (139, UNHEX('436F6E2043686F72697A6F204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (140, UNHEX('436F6E2043686F72697A6F20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (141, UNHEX('436F6E2043686F72697A6F205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (142, UNHEX('436F6E204D61C3AD7A20546965726E6F204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (143, UNHEX('436F6E204D61C3AD7A20546965726E6F204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (144, UNHEX('436F6E204D61C3AD7A20546965726E6F20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (145, UNHEX('436F6E204D61C3AD7A20546965726E6F205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (146, UNHEX('436F6E205065707065726F6E69204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (147, UNHEX('436F6E205065707065726F6E69204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (148, UNHEX('436F6E205065707065726F6E6920505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (149, UNHEX('436F6E205065707065726F6E69205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (150, UNHEX('436F6E2050696D656E74C3B36E204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (151, UNHEX('436F6E2050696D656E74C3B36E204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (152, UNHEX('436F6E2050696D656E74C3B36E20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (153, UNHEX('436F6E2050696D656E74C3B36E205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (154, UNHEX('436F6E20506F6C6C6F204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (155, UNHEX('436F6E20506F6C6C6F204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (156, UNHEX('436F6E20506F6C6C6F20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (157, UNHEX('436F6E20506F6C6C6F205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (158, UNHEX('436F6E2053616C616D69204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (159, UNHEX('436F6E2053616C616D69204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (160, UNHEX('436F6E2053616C616D6920505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (161, UNHEX('436F6E2053616C616D69205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (162, UNHEX('436F6E20546F63696E657461204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (163, UNHEX('436F6E20546F63696E657461204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (164, UNHEX('436F6E20546F63696E65746120505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (165, UNHEX('436F6E20546F63696E657461205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (166, UNHEX('436F6E20546F63696E6F73204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (167, UNHEX('436F6E20546F63696E6F73204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (168, UNHEX('436F6E20546F63696E6F7320505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (169, UNHEX('436F6E20546F63696E6F73205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (170, UNHEX('436F6E20546F6D617465204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (171, UNHEX('436F6E20546F6D617465204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (172, UNHEX('436F6E20546F6D61746520505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (173, UNHEX('436F6E20546F6D617465205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (175, UNHEX('53696E204A616DC3B36E205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (176, UNHEX('53696E205069C3B161205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (177, UNHEX('53696E205069C3B161204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (178, UNHEX('53696E204A616DC3B36E204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (179, UNHEX('53696E205069C3B161204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (180, UNHEX('53696E204A616DC3B36E204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (181, UNHEX('53696E205069C3B16120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (182, UNHEX('53696E204A616DC3B36E20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (183, UNHEX('53696E204163656974756E6173204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (184, UNHEX('53696E204163656974756E6173204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (185, UNHEX('53696E204163656974756E617320505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (186, UNHEX('53696E204163656974756E6173205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (187, UNHEX('53696E20436162616E6F204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (188, UNHEX('53696E20436162616E6F204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (189, UNHEX('53696E20436162616E6F20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (190, UNHEX('53696E20436162616E6F205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (191, UNHEX('53696E20517565736F204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (192, UNHEX('53696E20517565736F204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (193, UNHEX('53696E20517565736F20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (194, UNHEX('53696E20517565736F205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (195, UNHEX('53696E204365626F6C6C61204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (196, UNHEX('53696E204365626F6C6C61204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (197, UNHEX('53696E204365626F6C6C6120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (198, UNHEX('53696E204365626F6C6C61205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (199, UNHEX('53696E204368616D7069C3B16F6E204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (200, UNHEX('53696E204368616D7069C3B16F6E204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (201, UNHEX('53696E204368616D7069C3B16F6E20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (202, UNHEX('53696E204368616D7069C3B16F6E205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (203, UNHEX('53696E2043686F72697A6F204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (204, UNHEX('53696E2043686F72697A6F204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (205, UNHEX('53696E2043686F72697A6F20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (206, UNHEX('53696E2043686F72697A6F205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (207, UNHEX('53696E204D61C3AD7A20546965726E6F204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (208, UNHEX('53696E204D61C3AD7A20546965726E6F204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (209, UNHEX('53696E204D61C3AD7A20546965726E6F20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (210, UNHEX('53696E204D61C3AD7A20546965726E6F205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (211, UNHEX('53696E205065707065726F6E69204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (212, UNHEX('53696E205065707065726F6E69204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (213, UNHEX('53696E205065707065726F6E6920505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (214, UNHEX('53696E205065707065726F6E69205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (215, UNHEX('53696E2050696D656E74C3B36E204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (216, UNHEX('53696E2050696D656E74C3B36E204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (217, UNHEX('53696E2050696D656E74C3B36E20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (218, UNHEX('53696E2050696D656E74C3B36E205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (219, UNHEX('53696E20506F6C6C6F204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (220, UNHEX('53696E20506F6C6C6F204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (221, UNHEX('53696E20506F6C6C6F20505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (222, UNHEX('53696E20506F6C6C6F205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (223, UNHEX('53696E2053616C616D69204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (224, UNHEX('53696E2053616C616D69204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (225, UNHEX('53696E2053616C616D6920505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (226, UNHEX('53696E2053616C616D69205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (227, UNHEX('53696E20546F63696E657461204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (228, UNHEX('53696E20546F63696E657461204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (229, UNHEX('53696E20546F63696E65746120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (230, UNHEX('53696E20546F63696E657461205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (231, UNHEX('53696E20546F63696E6F73204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (232, UNHEX('53696E20546F63696E6F73204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (233, UNHEX('53696E20546F63696E6F7320505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (234, UNHEX('53696E20546F63696E6F73205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (235, UNHEX('53696E20546F6D617465204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (236, UNHEX('53696E20546F6D617465204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (237, UNHEX('53696E20546F6D61746520505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (238, UNHEX('53696E20546F6D617465205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (239, UNHEX('53616C20646520416A6F'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (240, UNHEX('50696D69656E7461'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (241, UNHEX('4F726567616E6F'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (242, UNHEX('53657276696C6C657461'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (243, UNHEX('5661736F73'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (244, UNHEX('506F7263696F6E65726F73'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (245, UNHEX('4D61676E657469636F'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (246, UNHEX('446F6D6963696C696F2028243029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (247, UNHEX('446F6D6963696C696F202824312E35303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (248, UNHEX('4172657175697065'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (249, UNHEX('536F6C6F20517565736F205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (250, UNHEX('536F6C6F20517565736F20505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (251, UNHEX('536F6C6F20517565736F204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (252, UNHEX('536F6C6F20517565736F204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (253, UNHEX('53696E205061737461204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (254, UNHEX('53696E205061737461204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (255, UNHEX('53696E20506173746120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (256, UNHEX('53696E205061737461205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (257, UNHEX('4167756120363030206D6C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (258, UNHEX('436F6C6F6D6269616E61205A65726F20312E35'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (259, UNHEX('5065707369204C6967746820312E35'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (260, UNHEX('436F6C6F6D6269616E61204C6967687420312E35'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (261, UNHEX('4D616E7A616E61204C6967687420312E35'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (262, UNHEX('4D616E7A616E61205A65726F312E35'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (263, UNHEX('4D7220546561204C696D6F6E204C69677468203530306D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (264, UNHEX('4164696369C3B36E2047756163616D6F6C65204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (265, UNHEX('4164696369C3B36E2047756163616D6F6C65204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (266, UNHEX('4164696369C3B36E2047756163616D6F6C6520505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (267, UNHEX('4164696369C3B36E2047756163616D6F6C6520584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (268, UNHEX('4164696369C3B36E2053616C736120424251204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (269, UNHEX('4164696369C3B36E2053616C736120424251204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (270, UNHEX('4164696369C3B36E2053616C73612042425120505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (271, UNHEX('4164696369C3B36E2053616C73612042425120584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (272, UNHEX('436F6E2047756163616D6F6C65204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (273, UNHEX('436F6E2047756163616D6F6C65204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (274, UNHEX('436F6E2047756163616D6F6C6520505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (275, UNHEX('436F6E2047756163616D6F6C65205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (276, UNHEX('436F6E2053616C736120424251204744'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (277, UNHEX('436F6E2053616C736120424251204D44'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (278, UNHEX('436F6E2053616C73612042425120505A'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (279, UNHEX('436F6E2053616C736120424251205847'), 'NA', 'MC', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (280, UNHEX('53696E204361726E65204D6F6C696461204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (281, UNHEX('53696E204361726E65204D6F6C696461204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (282, UNHEX('53696E204361726E65204D6F6C69646120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (283, UNHEX('53696E204361726E65204D6F6C696461205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (284, UNHEX('53696E2047756163616D6F6C65204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (285, UNHEX('53696E2047756163616D6F6C65204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (286, UNHEX('53696E2047756163616D6F6C6520505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (287, UNHEX('53696E2047756163616D6F6C65205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (288, UNHEX('53696E2053616C736120424251204744'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (289, UNHEX('53696E2053616C736120424251204D44'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (290, UNHEX('53696E2053616C73612042425120505A'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (291, UNHEX('53696E2053616C736120424251205847'), 'NA', 'MS', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (292, UNHEX('584C204469726563746F72696F'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (293, UNHEX('584C20436F6D626F20446F626C6520436F6E6374616374'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (294, UNHEX('4744204469726563746F72696F'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (295, UNHEX('4D44204469726563746F72696F'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (296, UNHEX('584C20566F6C616E74652032303234'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (297, UNHEX('474420566F6C616E74652032303234'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (298, UNHEX('4D656469616E61204D4158'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (299, UNHEX('474420436F6D626F20446F626C65'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (300, UNHEX('4D4420556E20496E6772656469656E7465'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (301, UNHEX('506C616E20506F727465726F'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (302, UNHEX('474420556E20496E6772656469656E746520566F6C616E7465'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (303, UNHEX('4861776169616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (304, UNHEX('416D65726963616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (305, UNHEX('4361726E697A20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (306, UNHEX('506F6C6C6F2042425120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (307, UNHEX('506F6C6C6F204368616D7069C3B16F6E20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (308, UNHEX('54686520576F726B7320584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (309, UNHEX('52616E63686572697320584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (310, UNHEX('4D65786963616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (311, UNHEX('4A616D6F6E207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (312, UNHEX('4A616D6F6E2079204368616D7069C3B16F6E20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (313, UNHEX('4974616C69616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (314, UNHEX('416D65726963616E6120457370656369616C20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (315, UNHEX('4E61706F6C6974616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (316, UNHEX('5665676574617269616E6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (317, UNHEX('506169736120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (318, UNHEX('4D61697A65746120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (319, UNHEX('506F6C6C6F20457370656369616C20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (320, UNHEX('50657065726F6E69207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (321, UNHEX('416D65726963616E204D455820584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (322, UNHEX('4D6F6E7461C3B165726120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (323, UNHEX('53616C616D69207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (324, UNHEX('537570657220584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (325, UNHEX('5065707065726F6E69204368696320584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (326, UNHEX('41726D612074752050697A7A6120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (327, UNHEX('5065707065726F6E69204368616D7069C3B16F6E20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (328, UNHEX('436F6E20546F646F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (329, UNHEX('44656C697A696120584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (330, UNHEX('4861776169616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (331, UNHEX('416D65726963616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (332, UNHEX('4361726E697A204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (333, UNHEX('506F6C6C6F20424251204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (334, UNHEX('506F6C6C6F204368616D7069C3B16F6E204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (335, UNHEX('54686520576F726B73204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (336, UNHEX('52616E636865726973204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (337, UNHEX('4D65786963616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (338, UNHEX('4A616D6F6E207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (339, UNHEX('4A616D6F6E2079204368616D7069C3B16F6E204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (340, UNHEX('4974616C69616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (341, UNHEX('416D65726963616E6120457370656369616C204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (342, UNHEX('4E61706F6C6974616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (343, UNHEX('5665676574617269616E61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (344, UNHEX('5061697361204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (345, UNHEX('4D61697A657461204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (346, UNHEX('506F6C6C6F20457370656369616C204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (347, UNHEX('50657065726F6E69207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (348, UNHEX('416D65726963616E204D4558204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (349, UNHEX('4D6F6E7461C3B1657261204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (350, UNHEX('53616C616D69207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (351, UNHEX('5375706572204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (352, UNHEX('5065707065726F6E692043686963204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (353, UNHEX('41726D612074752050697A7A61204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (354, UNHEX('5065707065726F6E69204368616D7069C3B16F6E204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (355, UNHEX('436F6E20546F646F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (356, UNHEX('44656C697A6961204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (357, UNHEX('4861776169616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (358, UNHEX('416D65726963616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (359, UNHEX('4361726E697A204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (360, UNHEX('506F6C6C6F20424251204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (361, UNHEX('506F6C6C6F204368616D7069C3B16F6E204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (362, UNHEX('54686520576F726B73204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (363, UNHEX('52616E636865726973204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (364, UNHEX('4D65786963616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (365, UNHEX('4A616D6F6E207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (366, UNHEX('4A616D6F6E2079204368616D7069C3B16F6E204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (367, UNHEX('4974616C69616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (368, UNHEX('416D65726963616E6120457370656369616C204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (369, UNHEX('4E61706F6C6974616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (370, UNHEX('5665676574617269616E61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (371, UNHEX('5061697361204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (372, UNHEX('4D61697A657461204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (373, UNHEX('506F6C6C6F20457370656369616C204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (374, UNHEX('50657065726F6E69207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (375, UNHEX('416D65726963616E204D4558204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (376, UNHEX('4D6F6E7461C3B1657261204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (377, UNHEX('53616C616D69207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (378, UNHEX('5375706572204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (379, UNHEX('5065707065726F6E692043686963204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (380, UNHEX('41726D612074752050697A7A61204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (381, UNHEX('5065707065726F6E69204368616D7069C3B16F6E204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (382, UNHEX('436F6E20546F646F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (383, UNHEX('44656C697A6961204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (384, UNHEX('4861776169616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (385, UNHEX('416D65726963616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (386, UNHEX('4361726E697A20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (387, UNHEX('506F6C6C6F2042425120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (388, UNHEX('506F6C6C6F204368616D7069C3B16F6E20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (389, UNHEX('54686520576F726B7320505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (390, UNHEX('52616E63686572697320505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (391, UNHEX('4D65786963616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (392, UNHEX('4A616D6F6E207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (393, UNHEX('4A616D6F6E2079204368616D7069C3B16F6E20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (394, UNHEX('4974616C69616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (395, UNHEX('416D65726963616E6120457370656369616C20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (396, UNHEX('4E61706F6C6974616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (397, UNHEX('5665676574617269616E6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (398, UNHEX('506169736120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (399, UNHEX('4D61697A65746120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (400, UNHEX('506F6C6C6F20457370656369616C20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (401, UNHEX('50657065726F6E69207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (402, UNHEX('416D65726963616E204D455820505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (403, UNHEX('4D6F6E7461C3B165726120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (404, UNHEX('53616C616D69207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (405, UNHEX('537570657220505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (406, UNHEX('5065707065726F6E69204368696320505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (407, UNHEX('41726D612074752050697A7A6120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (408, UNHEX('5065707065726F6E69204368616D7069C3B16F6E20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (409, UNHEX('436F6E20546F646F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (410, UNHEX('44656C697A696120505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (411, UNHEX('446F6D6963696C696F202824332E30303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (412, UNHEX('446F6D6963696C696F205041202824352E30303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (413, UNHEX('5661736F20476173656F736131306F7A'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (414, UNHEX('506F7263696F6E2050697A7A61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (415, UNHEX('47442050726F6D6F20456E76696761646F'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (416, UNHEX('506F7263696F6E2034303030202D20456D702054656D706F72616C'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (417, UNHEX('4772616E646520506F73746F626F6E2049666F6F64'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (418, UNHEX('584C20436F6D626F20446F626C65'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (419, UNHEX('474420436F6D626F20446F626C65'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (420, UNHEX('506F7263696F6E2050697A7A6120456D706C6561646F'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (421, UNHEX('506F7263696F6E204465736563686F'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (422, UNHEX('436F6D626F2032204D656469616E6173'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (423, UNHEX('436F6D626F2032204D656469616E6173'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (424, UNHEX('48617761696E61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (425, UNHEX('416D65726963616E61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (426, UNHEX('4D616E7A616E61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (427, UNHEX('5065707369'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (428, UNHEX('436F6C6F6D6269616E61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (429, UNHEX('4772616E64652044494449'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (430, UNHEX('4772616E64652044494449'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (431, UNHEX('584C2050726F6D6F2044656469746F73'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (432, UNHEX('44656469746F732050726F6D6F'), 'DEDITOS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (433, UNHEX('50726F6D6F2032204C61736167202B204D616475726F'), 'LASAG-MADU', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (434, UNHEX('4D6164757269746F'), 'MADURITO', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (435, UNHEX('584C20446F6D692E636F6D2023496D7065726469626C6573'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (436, UNHEX('4772616E6465204D6178'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (437, UNHEX('4E6F20646573656120476173656F7361'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (441, UNHEX('4D4420535550455220444F4D4943494C494F'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (442, UNHEX('505A2032207831392E393930'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (443, UNHEX('505A2032207831392E39393020434F4E54414354'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (444, UNHEX('584C20436F6D626F20467574626F6C65726F'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (445, UNHEX('4D6164757269746F2050726F6D6F'), 'MADURITO', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (446, UNHEX('50697A7A65746120436F6D626F20496E736570'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (447, UNHEX('4C61736167C3B16120436F6D626F20496E736570'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (448, UNHEX('584C2053656D204D61647265'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (449, UNHEX('47442053656D204D61647265'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (450, UNHEX('4D4420446961204D756A6572'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (451, UNHEX('4D616E7A616E612050657420323530206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (452, UNHEX('436F6C6F6D6269616E612050657420323530206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (453, UNHEX('4D616E7A616E612050657420323530206D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (454, UNHEX('43686F636F6C617465'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (455, UNHEX('43686F636F6C617465283130303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (456, UNHEX('4172657175697065283130303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (457, UNHEX('584C205261707069'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (458, UNHEX('4744205261707069'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (459, UNHEX('4D44205261707069'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (460, UNHEX('4C6563686572697461283130303029'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (461, UNHEX('536F6272652053686F7779'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (462, UNHEX('4C6563686572697461284F6273657175696F29'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (463, UNHEX('4D4420566F6C616E74652032303234'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (464, UNHEX('584C20426C61636B20436F6D626F'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (465, UNHEX('436869706F746C65284F6273657175696F29'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (466, UNHEX('50697A7A6120427572676572'), 'BURGER', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (467, UNHEX('47442063C3B36469676F20666C617368'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (468, UNHEX('50726F6D6F206D656469616E6120706C7573'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (469, UNHEX('50697A7A61204D44204573746F66616461'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (470, UNHEX('50697A7A61204744204573746F66616461'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (471, UNHEX('50697A7A61204D44204573706F6E6A616461'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (472, UNHEX('50697A7A6120584C20456D706C6561646F'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (473, UNHEX('50697A7A6120474420456D706C6561646F'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (474, UNHEX('50697A7A61204D4420456D706C6561646F'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (475, UNHEX('4C61736167C3B161204D6978746120456D706C6561646F'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (476, UNHEX('50697A7A6120505A20456D706C6561646F'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (477, UNHEX('50697A7A61204D44204573746F6661646120456D706C'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (478, UNHEX('50697A7A61204744204573746F6661646120456D706C'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (479, UNHEX('4D616E7A616E61204C6967687420506574203430306D4C'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (480, UNHEX('436F6D626F20496E737570657261626C6520584C'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (481, UNHEX('436F6D626F20496E737570657261626C65204744'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (482, UNHEX('436F6D626F20496E737570657261626C65204D44'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (483, UNHEX('50726F6D206D656469616E6120566F6C2032494E47'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (484, UNHEX('50697A7A612050726F6D6F20505A'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (485, UNHEX('5461726A65746120526567616C6F2035302E303030'), 'NA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (486, UNHEX('5461726A65746120526567616C6F203130302E303030'), 'NA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (487, UNHEX('54617269666120536572766963696F205241505049'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (488, UNHEX('41646963696F6E616C205241505049'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (489, UNHEX('506570736920312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (490, UNHEX('4D616E7A616E6120312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (491, UNHEX('436F6C6F6D6269616E6120312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (492, UNHEX('4D616E7A616E6120506574203430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (493, UNHEX('436F6C6F6D6269616E612050657420343030206D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (494, UNHEX('50657073692050657420343030206D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (495, UNHEX('4772616E64652044494449'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (496, UNHEX('50697A7A6574612044494449'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (497, UNHEX('50697A7A6574612044494449'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (498, UNHEX('50697A7A6120584C20506C6174'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (499, UNHEX('50697A7A6120474420506C6174'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (500, UNHEX('50697A7A61204D4420506C6174'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (501, UNHEX('50697A7A6120505A20506C6174'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (502, UNHEX('50697A7A61204D44204573746F6661646120506C6174'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (503, UNHEX('50697A7A61204744204573746F6661646120506C6174'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (504, UNHEX('4D61726163756D616E676F'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (505, UNHEX('436572657A61'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (506, UNHEX('536F6461205361626F72697A616461'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (507, UNHEX('536F6461205361626F72697A61646120536567756E646120353025'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (508, UNHEX('536F6461205361626F72697A6164612050697A7A6120456E74657261'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (509, UNHEX('4D656469616E61204449444920436F6E20476173656F7361'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (510, UNHEX('44656469746F732043756D706C65'), 'DEDITOS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (511, UNHEX('426F726465204D696368656C61646F'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (512, UNHEX('426F7264652053696E204D696368656C6172'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (513, UNHEX('426C75654265727279'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (514, UNHEX('4D61726163757961'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (515, UNHEX('436F636120436F6C6120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (516, UNHEX('436F636120436F6C61205A65726F20312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (517, UNHEX('5072656D696F20312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (518, UNHEX('51756174726F20312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (519, UNHEX('53707269746520312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (520, UNHEX('537072697465205A65726F20312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (521, UNHEX('41677561204272697361204C696D6F6E20312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (522, UNHEX('41677561204272697361204D616E7A616E6120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (523, UNHEX('41677561204272697361204D6172616375796120312E35204C7473'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (524, UNHEX('436F636120436F6C612050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (525, UNHEX('5072656D696F2050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (526, UNHEX('51756174726F2050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (527, UNHEX('5370726974652050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (528, UNHEX('4672657368204D616E646172696E612050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (529, UNHEX('436F636120436F6C61205A65726F2050343030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (530, UNHEX('41677561204D616E616E7469616C2050363030'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (531, UNHEX('41677561204272697361204C696D6F6E2050323830'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (532, UNHEX('41677561204272697361204D616E7A616E612050323830'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (533, UNHEX('41677561204272697361204D617261637579612050323830'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (534, UNHEX('51756174726F2050323530'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (535, UNHEX('5370726974652050323530'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (536, UNHEX('436F636120436F6C6120312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (537, UNHEX('436F636120436F6C61205A65726F20312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (538, UNHEX('5072656D696F20312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (539, UNHEX('51756174726F20312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (540, UNHEX('53707269746520312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (541, UNHEX('41677561204272697361204C696D6F6E20312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (542, UNHEX('41677561204272697361204D616E7A616E6120312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (543, UNHEX('41677561204272697361204D6172616375796120312E35204C74732044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (544, UNHEX('436F636120436F6C6120503430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (545, UNHEX('5072656D696F20503430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (546, UNHEX('51756174726F20503430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (547, UNHEX('53707269746520503430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (548, UNHEX('4672657368204D616E646172696E6120503430306D4C2044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (549, UNHEX('436F636120436F6C61205A65726F20503430302044494449'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (550, UNHEX('50697A7A61204D65642B2047617320312E356C20524150'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (551, UNHEX('50697A7A61204772616E202B2047617320312E356C20524150'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (552, UNHEX('50697A7A6120427572676572204C616E7A616D69656E746F'), 'BURGER', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (553, UNHEX('5061706173204D61796F6E657361'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (554, UNHEX('546F646F73206C6F7320436F6E64696D656E746F73'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (555, UNHEX('43686F72697A6F207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (556, UNHEX('43686F72697A6F207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (557, UNHEX('43686F72697A6F207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (558, UNHEX('43686F72697A6F207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (559, UNHEX('546F63696E657461207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (560, UNHEX('546F63696E657461207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (561, UNHEX('546F63696E657461207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (562, UNHEX('546F63696E657461207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (563, UNHEX('50696E61207920517565736F20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (564, UNHEX('50696E61207920517565736F204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (565, UNHEX('50696E61207920517565736F204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (566, UNHEX('50696E61207920517565736F20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (567, UNHEX('50697A7A61204861776169616E6120586772616E6465202B2047617320312E356C205241505049'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (568, UNHEX('436F6D626F2046616D696C6961722047442044494449'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (569, UNHEX('50697A7A6120416D65726963616E61204772616E6465202B2047617320312E356C205241505049'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (570, UNHEX('50697A7A6120584C20353025'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (571, UNHEX('50697A7A6120474420353025'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (572, UNHEX('50697A7A61204275726765722050726F6D6F'), 'BURGER', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (573, UNHEX('44656469746F732050756E746F732832303029'), 'DEDITOS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (574, UNHEX('50697A7A61204D44204372756E6368'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (575, UNHEX('436F6D626F20474420476F6C6561646F72'), 'GD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (576, UNHEX('4D6164757269746F205065717565C3B16F'), 'MADURITO', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (577, UNHEX('50697A7A61204275726765722044694469'), 'BURGER', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (578, UNHEX('44656469746F7320506C6174'), 'DEDITOS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (579, UNHEX('4D6164757269746F20506C6174'), 'MADURITO', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (580, UNHEX('476173656F73612050756E746F732831303029'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (581, UNHEX('50697A7A61204D442050756E746F732833303029'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (582, UNHEX('4D6F7272616C2050756E746F732835303029'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (583, UNHEX('584C20436F6D626F2050756E746F732835303029'), 'XL', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (584, UNHEX('50697A7A61204D442035302520486167616D6F732056616361'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (585, UNHEX('4861776169616E61204172746573616E616C20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (586, UNHEX('4861776169616E61204172746573616E616C204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (587, UNHEX('4861776169616E61204172746573616E616C204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (588, UNHEX('4861776169616E61204172746573616E616C20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (589, UNHEX('50697A7A61204D44204D6173746572'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (590, UNHEX('50697A7A61204D44205065707065202B2042656269646120312E356C'), 'MD', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (591, UNHEX('4164696369C3B36E205069C3B16120417274657320584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (592, UNHEX('4164696369C3B36E205069C3B161204172746573204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (593, UNHEX('4164696369C3B36E205069C3B161204172746573204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (594, UNHEX('4164696369C3B36E205069C3B16120417274657320505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (595, UNHEX('4E7565766120536F646120467275746F7320526F6A6F73'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (596, UNHEX('4E7565766120536F646120467275746F7320416D6172696C6C6F73'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (597, UNHEX('416D65726963616E61205072656D69756D20584C'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (598, UNHEX('416D65726963616E61205072656D69756D204744'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (599, UNHEX('416D65726963616E61205072656D69756D204D44'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (600, UNHEX('416D65726963616E61205072656D69756D20505A'), 'NA', 'D', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (601, UNHEX('4164696369C3B36E2053616C61205072656D20584C'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (602, UNHEX('4164696369C3B36E2053616C61205072656D204744'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (603, UNHEX('4164696369C3B36E2053616C61205072656D204D44'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (604, UNHEX('4164696369C3B36E2053616C61205072656D20505A'), 'NA', 'A', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (605, UNHEX('42616C6F6E2050756E746F732833303029'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (606, UNHEX('5661736F2050756E746F732832303029'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (607, UNHEX('50697A7A6574612050726F6D6F2044494449'), 'PZ', 'P', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (608, UNHEX('53494E20454D5041515545'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (609, UNHEX('434F4E20454D5041515545'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (610, UNHEX('42616C6F6E2050697A7A61'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (611, UNHEX('42616C6F6E2050697A7A612046756C6C'), 'ADI', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (612, UNHEX('4C61736167C3B16120506C6174'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (613, UNHEX('50524F4D4F2032204C617361676E6173202B20426562'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (614, UNHEX('50524F4D4F2032204C61736167202B204265622044494449'), 'LASAG MIXTA', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (615, UNHEX('536F6461204D69636865204C696DC3B36E'), 'ADI', 'G', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);
INSERT INTO inventarioamericana.producto_catalogo
  (idproducto, descripcion, tamano, tipo_producto, idtienda_origen)
  VALUES (616, UNHEX('4E7567676574732847726174697329'), 'NUGGETS', 'O', 1)
  ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion),
    tamano = VALUES(tamano), tipo_producto = VALUES(tipo_producto),
    idtienda_origen = VALUES(idtienda_origen);

-- 2. LA COMPOSICION ----------------------------------------------------------
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (1, 4, 8)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (2, 6, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (2, 72, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (3, 12, 6)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (4, 7, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (4, 28, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (4, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (4, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (6, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (6, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (7, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (7, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (8, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (8, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (9, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (9, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (10, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (11, 142, 280)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (12, 28, 200)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (13, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (14, 19, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (15, 15, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (16, 24, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (19, 20, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (20, 13, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (21, 17, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (22, 18, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (23, 21, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (24, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (25, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (26, 22, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (27, 14, 125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (28, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (29, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (30, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (31, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (32, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (33, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (34, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (35, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (36, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (37, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (38, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (39, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (40, 25, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (41, 142, 240)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (42, 28, 130)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (43, 16, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (44, 19, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (45, 15, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (46, 24, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (49, 20, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (50, 13, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (51, 17, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (52, 18, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (53, 21, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (54, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (55, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (56, 22, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (57, 14, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (58, 25, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (59, 142, 195)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (60, 28, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (61, 16, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (62, 19, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (63, 15, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (64, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (67, 20, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (68, 13, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (69, 17, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (70, 18, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (71, 21, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (72, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (73, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (74, 22, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (75, 14, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (76, 25, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (77, 142, 120)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (78, 28, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (79, 16, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (80, 19, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (81, 15, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (82, 24, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (85, 20, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (86, 13, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (87, 17, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (88, 18, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (89, 21, 28)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (90, 26, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (91, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (92, 22, 13)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (93, 14, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (100, 16, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (101, 142, 410)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (102, 142, 350)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (103, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (104, 142, 280)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (105, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (106, 142, 180)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (107, 16, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (110, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (111, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (112, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (113, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (114, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (115, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (116, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (118, 22, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (119, 22, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (120, 22, 13)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (121, 22, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (122, 13, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (123, 13, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (124, 13, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (125, 13, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (126, 14, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (127, 14, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (128, 14, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (129, 14, 125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (134, 24, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (135, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (136, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (137, 24, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (138, 15, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (139, 15, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (140, 15, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (141, 15, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (142, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (143, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (144, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (145, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (146, 17, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (147, 17, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (148, 17, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (149, 17, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (150, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (151, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (152, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (153, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (154, 18, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (155, 18, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (156, 18, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (157, 18, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (158, 19, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (159, 19, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (160, 19, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (161, 19, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (162, 20, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (163, 20, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (164, 20, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (165, 20, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (166, 21, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (167, 21, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (168, 21, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (169, 21, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (175, 16, -80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (176, 142, -410)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (177, 142, -350)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (178, 16, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (179, 142, -280)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (180, 16, -60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (181, 142, -180)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (182, 16, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (187, 13, -50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (188, 13, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (189, 13, -25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (190, 13, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (191, 28, -340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (192, 28, -230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (193, 28, -105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (194, 28, -440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (199, 24, -75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (200, 24, -65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (201, 24, -45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (202, 24, -85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (203, 15, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (204, 15, -35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (205, 15, -20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (206, 15, -50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (207, 25, -45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (208, 25, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (209, 25, -25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (210, 25, -60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (211, 17, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (212, 17, -60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (213, 17, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (214, 17, -80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (219, 18, -90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (220, 18, -75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (221, 18, -45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (222, 18, -90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (223, 19, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (224, 19, -60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (225, 19, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (226, 19, -80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (227, 20, -40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (228, 20, -35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (229, 20, -20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (230, 20, -50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (231, 21, -85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (232, 21, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (233, 21, -45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (234, 21, -100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (248, 5, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (249, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (250, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (251, 28, 240)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (252, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (253, 26, -85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (254, 26, -70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (255, 26, -50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (256, 26, -100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (257, 94, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (258, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (259, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (260, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (261, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (262, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (263, 87, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (264, 68, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (265, 68, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (266, 68, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (267, 68, 125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (268, 66, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (269, 66, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (270, 66, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (271, 66, 140)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (272, 68, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (273, 68, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (274, 68, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (275, 68, 125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (276, 66, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (277, 66, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (278, 66, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (279, 66, 140)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (292, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (292, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (293, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (293, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (294, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (294, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (295, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (295, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (296, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (296, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (297, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (297, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (298, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (298, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (298, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (298, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (299, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (299, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (300, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (300, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (301, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (301, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (302, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (302, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (303, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (303, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (303, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (303, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (303, 142, 260)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (304, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (304, 19, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (304, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (304, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (304, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 15, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 16, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 19, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 20, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (305, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (306, 18, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (306, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (306, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (306, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (306, 66, 140)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (307, 18, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (307, 24, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (307, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (307, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (307, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 15, 53)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 16, 56)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 19, 33)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 20, 27)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 22, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 24, 84)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (308, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 13, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 15, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (309, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 21, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (310, 68, 125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (311, 16, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (311, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (311, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (311, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (312, 16, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (312, 24, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (312, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (312, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (312, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 16, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 23, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 29, 213)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (313, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 13, 53)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 19, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 24, 150)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (314, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (315, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (315, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (315, 29, 213)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (315, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 23, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 24, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 29, 213)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (316, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 15, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 20, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (317, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (318, 20, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (318, 25, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (318, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (318, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (318, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 18, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 24, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (319, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (320, 17, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (320, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (320, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (320, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 17, 63)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 24, 150)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 25, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (321, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 21, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 23, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 25, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (322, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (323, 19, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (323, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (323, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (323, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (324, 14, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (324, 17, 56)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (324, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (324, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (324, 29, 213)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (325, 17, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (325, 21, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (325, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (325, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (325, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (326, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (326, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (327, 17, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (327, 24, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (327, 26, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (327, 28, 440)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (327, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 13, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 14, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 15, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 16, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 17, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 18, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 19, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 20, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 21, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 23, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 24, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 25, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 28, 320)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (328, 142, 200)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (329, 17, 56)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (329, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (329, 29, 213)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (330, 16, 44)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (330, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (330, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (330, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (330, 142, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (331, 16, 44)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (331, 19, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (331, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (331, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (331, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 15, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 19, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 20, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (332, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (333, 18, 95)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (333, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (333, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (333, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (333, 66, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (334, 18, 95)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (334, 24, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (334, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (334, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (334, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 15, 47)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 16, 49)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 19, 29)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 20, 23)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 22, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 24, 74)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (335, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 13, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 15, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (336, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 21, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (337, 68, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (338, 16, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (338, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (338, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (338, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (339, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (339, 24, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (339, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (339, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (339, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 23, 64)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 29, 187)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (340, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 13, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 19, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 24, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (341, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (342, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (342, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (342, 29, 187)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (342, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 23, 64)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 24, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 29, 187)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (343, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 15, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 20, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (344, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (345, 20, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (345, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (345, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (345, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (345, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 18, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 24, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (346, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (347, 17, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (347, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (347, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (347, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 17, 53)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 24, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 25, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (348, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 21, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 23, 64)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 25, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (349, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (350, 19, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (350, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (350, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (350, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (351, 14, 71)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (351, 17, 49)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (351, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (351, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (351, 29, 187)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (352, 17, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (352, 21, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (352, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (352, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (352, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (353, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (353, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (354, 17, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (354, 24, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (354, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (354, 28, 340)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (354, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 13, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 14, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 15, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 16, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 17, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 18, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 19, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 20, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 21, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 23, 64)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 24, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 25, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 28, 245)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (355, 142, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (356, 17, 49)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (356, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (356, 29, 187)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (357, 16, 38)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (357, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (357, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (357, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (357, 142, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (358, 16, 38)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (358, 19, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (358, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (358, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (358, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 15, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 19, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 20, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (359, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (360, 18, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (360, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (360, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (360, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (360, 66, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (361, 18, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (361, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (361, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (361, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (361, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 15, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 16, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 19, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 20, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 22, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 24, 63)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (362, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 13, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 15, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (363, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 21, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (364, 68, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (365, 16, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (365, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (365, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (365, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (366, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (366, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (366, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (366, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (366, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 23, 55)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 29, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (367, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 13, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 19, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 24, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (368, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (369, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (369, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (369, 29, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (369, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 23, 55)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 29, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (370, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 15, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 20, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (371, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (372, 20, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (372, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (372, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (372, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (372, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 18, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (373, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (374, 17, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (374, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (374, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (374, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 17, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 24, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 25, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (375, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 21, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 23, 55)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 25, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (376, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (377, 19, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (377, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (377, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (377, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (378, 14, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (378, 17, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (378, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (378, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (378, 29, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (379, 17, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (379, 21, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (379, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (379, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (379, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (380, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (380, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (381, 17, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (381, 24, 65)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (381, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (381, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (381, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 13, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 14, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 15, 17)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 16, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 17, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 18, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 19, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 20, 17)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 21, 35)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 23, 55)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 24, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 25, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 28, 173)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (382, 142, 120)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (383, 17, 42)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (383, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (383, 29, 160)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (384, 16, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (384, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (384, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (384, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (384, 142, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (385, 16, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (385, 19, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (385, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (385, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (385, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 15, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 16, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 19, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 20, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (386, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (387, 18, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (387, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (387, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (387, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (387, 66, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (388, 18, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (388, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (388, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (388, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (388, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 15, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 16, 27)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 19, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 20, 14)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 22, 13)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 24, 44)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (389, 95, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 13, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 15, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (390, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 21, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (391, 68, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (392, 16, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (392, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (392, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (392, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (393, 16, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (393, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (393, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (393, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (393, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 16, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 23, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 29, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (394, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 13, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 19, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 24, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (395, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (396, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (396, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (396, 29, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (396, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 23, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 29, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (397, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 15, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 20, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (398, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (399, 20, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (399, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (399, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (399, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (399, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 18, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (400, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (401, 17, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (401, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (401, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (401, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 17, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 24, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 25, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (402, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 21, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 23, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 25, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (403, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (404, 19, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (404, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (404, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (404, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (405, 14, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (405, 17, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (405, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (405, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (405, 29, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (406, 17, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (406, 21, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (406, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (406, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (406, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (407, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (407, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (408, 17, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (408, 24, 45)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (408, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (408, 28, 95)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (408, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 13, 12)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 14, 10)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 15, 10)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 16, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 17, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 18, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 19, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 20, 10)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 21, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 23, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 24, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 25, 12)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 28, 75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (409, 142, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (410, 17, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (410, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (410, 29, 73)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (413, 1, 0.2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (413, 3, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (414, 8, 0.125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (414, 26, 8.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (414, 28, 53.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (415, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (415, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (416, 8, 0.125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (416, 26, 8.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (416, 28, 53.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (417, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (417, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (418, 8, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (418, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (419, 9, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (419, 35, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (420, 8, 0.125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (420, 26, 8.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (420, 28, 53.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (421, 8, 0.125)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (421, 26, 8.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (421, 28, 53.75)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (422, 10, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (422, 36, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (423, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (423, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (429, 9, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (429, 35, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (430, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (430, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (431, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (431, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (432, 7, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (432, 28, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (432, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (432, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (433, 5, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (433, 6, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (433, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (433, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (433, 78, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (434, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (434, 78, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (434, 79, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (435, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (435, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (436, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (436, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (436, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (436, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (441, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (441, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (442, 11, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (442, 37, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (443, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (443, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (444, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (444, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (445, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (445, 78, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (445, 79, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (446, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (446, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (447, 6, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (447, 72, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (448, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (448, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (449, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (449, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (450, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (450, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (451, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (452, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (453, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (454, 92, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (455, 92, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (456, 5, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (457, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (457, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (458, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (458, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (459, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (459, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (460, 93, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (461, 76, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (462, 93, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (463, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (463, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (464, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (464, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (465, 96, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (467, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (467, 26, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (467, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (467, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (468, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (468, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (468, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (468, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (469, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (469, 28, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (469, 115, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (470, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (470, 28, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (470, 116, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (471, 117, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (472, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (472, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (473, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (473, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (474, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (474, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (475, 6, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (475, 72, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (476, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (476, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (477, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (477, 28, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (477, 115, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (478, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (478, 28, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (478, 116, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (480, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (480, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (481, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (481, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (482, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (482, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (483, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (483, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (483, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (483, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (484, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (484, 26, 50)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (484, 28, 105)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (484, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (495, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (495, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (496, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (496, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (497, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (497, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (498, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (498, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (499, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (499, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (500, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (500, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (501, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (501, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (502, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (502, 28, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (502, 115, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (503, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (503, 28, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (503, 116, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (504, 121, 0.015)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (504, 122, 0.015)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (504, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (504, 131, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (505, 120, 0.03)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (505, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (505, 128, 4)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (505, 131, 1.5)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (506, 125, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (506, 126, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (507, 125, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (507, 126, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (509, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (509, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (510, 7, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (510, 28, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (510, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (510, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (513, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (513, 131, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (513, 132, 0.03)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (513, 133, 25)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (514, 121, 0.03)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (514, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (514, 131, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (515, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (516, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (517, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (518, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (519, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (520, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (521, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (522, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (523, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (524, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (525, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (526, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (527, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (528, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (529, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (530, 94, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (531, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (532, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (533, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (534, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (535, 86, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (536, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (537, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (538, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (539, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (540, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (541, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (542, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (543, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (544, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (545, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (546, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (547, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (548, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (549, 2, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (550, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (550, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (551, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (551, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 20, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 23, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 29, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 66, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 84, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 135, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 136, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 138, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (552, 139, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (553, 134, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (567, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (567, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (568, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (568, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (569, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (569, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (570, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (570, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (571, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (571, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 20, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 23, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 29, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 66, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 84, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 135, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 136, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 138, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (572, 139, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (573, 7, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (573, 28, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (573, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (573, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (574, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (574, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (575, 9, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (575, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (576, 141, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 20, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 23, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 29, 80)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 66, 15)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 84, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 135, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 136, 30)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 138, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (577, 139, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (578, 7, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (578, 28, 90)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (578, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (578, 70, 3.3)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (579, 28, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (579, 78, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (579, 79, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (580, 1, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (581, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (581, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (583, 8, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (583, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (584, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (584, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (585, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (585, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (585, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (585, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (585, 142, 260)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (586, 16, 44)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (586, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (586, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (586, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (586, 142, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (587, 16, 38)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (587, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (587, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (587, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (587, 142, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (588, 16, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (588, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (588, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (588, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (588, 142, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (589, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (589, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (590, 10, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (590, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (591, 142, 280)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (592, 142, 240)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (593, 142, 195)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (594, 142, 120)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (595, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (596, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (597, 16, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (597, 26, 70)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (597, 28, 430)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (597, 34, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (597, 143, 130)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (598, 16, 44)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (598, 26, 60)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (598, 28, 330)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (598, 35, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (598, 143, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (599, 16, 38)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (599, 26, 40)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (599, 28, 230)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (599, 36, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (599, 143, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (600, 16, 22)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (600, 26, 20)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (600, 28, 100)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (600, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (600, 143, 58)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (601, 143, 130)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (602, 143, 110)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (603, 143, 85)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (604, 143, 58)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (607, 11, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (607, 37, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (610, 146, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (611, 146, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (612, 6, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (612, 72, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (613, 6, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (613, 72, 4)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (614, 6, 2)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (614, 72, 4)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (615, 125, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (615, 126, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (615, 127, 1)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);
INSERT INTO inventarioamericana.producto_insumo (idproducto, iditem, cantidad)
  VALUES (616, 12, 6)
  ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad);

COMMIT;

SELECT 'producto_catalogo' AS tabla, COUNT(*) AS filas FROM inventarioamericana.producto_catalogo
UNION ALL SELECT 'producto_insumo', COUNT(*) FROM inventarioamericana.producto_insumo;

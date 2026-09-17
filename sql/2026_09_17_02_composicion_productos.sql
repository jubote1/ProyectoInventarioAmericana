-- ---------------------------------------------------------------------------
-- COMPOSICION DE PRODUCTOS CENTRALIZADA
--
-- Se corre UNA VEZ en el CENTRAL (172.19.0.25). No toca ninguna tienda.
-- Es idempotente: correrlo dos veces no cambia nada la segunda.
--
-- POR QUE
--
-- item_inventario_x_producto dice cuanto insumo descuenta cada producto, y
-- vive SOLO en cada tienda. Verificado el 2026-09-17 contra las once: no hay
-- dos iguales. Nueve huellas distintas, y las diferencias no son de forma:
--
--   La Mota, Americana Especial XL:  Cabano 53 -> 70, Pasta 70 -> 100,
--                                    Champinon 150 -> 85, Queso 430 -> 440
--
-- Eso importa mas de lo que parece. La varianza se calcula contra el consumo
-- teorico, y el consumo teorico sale de esta tabla. Mientras las recetas
-- difieran, comparar la varianza entre tiendas es comparar dos cosas medidas
-- con reglas distintas. Y La Mota es justo la que mas varianza reporta.
--
-- Lo que SI esta alineado, y es lo que hace viable centralizar: de 602
-- productos solo 9 ids difieren de verdad entre tiendas, y de 126 items de
-- inventario solo uno cambia de nombre. Los ids significan lo mismo en todas.
--
-- QUE SE CREA
--
-- Dos tablas en inventarioamericana, que pasa a ser la fuente de verdad. Las
-- tiendas siguen teniendo la suya y se les replica desde aca.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. EL CATALOGO DE PRODUCTOS
--
-- El nombre del producto vive en la tienda. Se trae una copia al central para
-- que la pantalla de costeo pueda armar el selector sin salir a preguntarle a
-- una tienda cada vez que alguien la abre.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inventarioamericana.producto_catalogo (
	idproducto      INT         NOT NULL,
	descripcion     VARCHAR(100)    NULL,
	tamano          VARCHAR(20)     NULL,
	tipo_producto   VARCHAR(2)      NULL,
	idtienda_origen INT             NULL COMMENT 'De cual tienda se trajo',
	actualizado_en  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (idproducto),
	KEY idx_producto_catalogo_desc (descripcion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Copia central del catalogo de productos de las tiendas';

-- ===========================================================================
-- 2. LA COMPOSICION
--
-- La llave es (idproducto, iditem) y NO un consecutivo como en la tienda. Eso
-- es deliberado: en la tienda la llave es iditem_producto, un autonumerico, y
-- por eso se pudieron colar filas repetidas del mismo par.
--
-- Estan repetidas hoy, en produccion:
--
--   Guacamole Kg / Con Guacamole MD  ->  25 y 60, en LAS ONCE tiendas
--   Lasagna Mixt / Lasagna Combo     ->  1 y 1, en Manrique y Niquia
--   Panes Lasagna / Lasagna Combo    ->  2 y 2, en Manrique y Niquia
--
-- Y no son inofensivas: ItemInventarioProductoDAO.obtenerItemsInventarioProducto
-- recorre TODAS las filas del producto y arma un descuento por cada una. O sea
-- que cada "Con Guacamole MD" descuenta 85 gramos, no 25 ni 60.
--
-- Con esta llave el problema no se puede repetir.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inventarioamericana.producto_insumo (
	idproducto      INT         NOT NULL,
	iditem          INT         NOT NULL,
	cantidad        DOUBLE      NOT NULL DEFAULT 0,
	actualizado_en  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (idproducto, iditem),
	KEY idx_producto_insumo_item (iditem)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Composicion maestra: cuanto insumo descuenta cada producto';

-- ===========================================================================
-- 3. LA BITACORA DE REPLICAS
--
-- Replicar pisa la tabla de una tienda. Sin un registro de quien lo hizo y
-- cuando, una receta que cambie sin explicacion no se puede rastrear, y esto
-- mueve el consumo teorico de toda una tienda.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS inventarioamericana.producto_insumo_replica (
	idreplica       INT         NOT NULL AUTO_INCREMENT,
	idtienda        INT         NOT NULL,
	usuario         VARCHAR(100)    NULL,
	filas_enviadas  INT         NOT NULL DEFAULT 0,
	filas_antes     INT         NOT NULL DEFAULT 0,
	resultado       VARCHAR(20)     NULL,
	detalle         VARCHAR(500)    NULL,
	fecha           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (idreplica),
	KEY idx_replica_tienda_fecha (idtienda, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Quien replico la composicion a que tienda y cuando';

-- ===========================================================================
-- COMO QUEDO
--
-- Las tres quedan VACIAS. El catalogo y la composicion se llenan desde la
-- pantalla, con el boton que trae los datos de una tienda de referencia: se
-- hace desde alli y no aca porque el central no puede leer la base de una
-- tienda dentro de una consulta.
-- ===========================================================================
SELECT 'producto_catalogo'       AS tabla, COUNT(*) AS filas FROM inventarioamericana.producto_catalogo
UNION ALL
SELECT 'producto_insumo',        COUNT(*) FROM inventarioamericana.producto_insumo
UNION ALL
SELECT 'producto_insumo_replica', COUNT(*) FROM inventarioamericana.producto_insumo_replica;

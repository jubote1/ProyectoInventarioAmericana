-- ---------------------------------------------------------------------------
-- PUBLICIDAD SE PASA A gerencia_gasto_area, QUE ES LA TABLA BUENA
--
-- Se corre en el CENTRAL (172.19.0.25), esquema inventarioamericana.
-- Idempotente.
--
-- POR QUE
--
-- gerencia_gasto_area existe desde el 2026-10-23 con su catalogo
-- gerencia_concepto_gasto, y la leen GerenciaCierreDAO y GerenciaGastoDAO: es
-- la tabla del cierre gerencial. Un dia despues yo cree
-- pizzaamericana.gasto_area para lo mismo, sin saber que esta ya estaba. Dos
-- tablas para el mismo dato terminan diciendo cosas distintas, asi que la mia
-- se retira y publicidad se pasa aca.
--
-- EL GASTO VARIABLE MES A MES
--
-- Lo unico que mi tabla tenia y esta no es gasto_area_mes, el valor de cada
-- mes de un gasto variable. Aqui eso se expresa con la vigencia: una fila por
-- periodo, con vigencia_desde y vigencia_hasta. Es mas trabajo de digitar pero
-- tiene la misma propiedad que importa, que es la que me hizo separar la otra
-- tabla: un mes ya repartido entre las tiendas NO cambia porque alguien digite
-- el siguiente, porque cada mes tiene su propia fila.
--
-- "Pautas y temas de publicidad" es el unico variable hoy. Su fila de octubre
-- queda cerrada el 31 para que al digitar noviembre no se reescriba octubre.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LOS CONCEPTOS DE PUBLICIDAD
--
-- No se reusa el 12 "Publicista" a proposito: ese tiene linea CUOTA PUNTO DE
-- VENTA y ambito AMBOS, o sea que es un gasto de tienda. Meter la publicidad
-- del area ahi la sumaria en la linea equivocada.
-- ===========================================================================
INSERT INTO gerencia_concepto_gasto (idconcepto, nombre, tipo, linea, orden, ambito, activo) VALUES
  (62, 'Agencia de publicidad', 'FIJO', 'PUBLICIDAD Y MERCADEO', 300, 'AREA', 'S'),
  (63, 'Pautas y publicidad',   'FIJO', 'PUBLICIDAD Y MERCADEO', 310, 'AREA', 'S'),
  (64, 'Asesor de publicidad',  'FIJO', 'PUBLICIDAD Y MERCADEO', 320, 'AREA', 'S')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre), linea = VALUES(linea), orden = VALUES(orden), ambito = VALUES(ambito);

-- ===========================================================================
-- 2. LOS TRES GASTOS, CON LAS CIFRAS DEL EXCEL
--
-- Se borra y se vuelve a poner para que correr esto dos veces no duplique.
-- Solo toca estos tres conceptos: lo demas del area no se mueve.
--
-- La vigencia arranca el 2026-10-01 y no en julio como el resto, porque el
-- libro no dice desde cuando corre cada contrato de publicidad. Si se sabe,
-- se corrige la fecha: mientras tanto los periodos anteriores a octubre salen
-- SIN publicidad, y eso hay que tenerlo presente al mirar un mes viejo.
-- ===========================================================================
DELETE FROM gerencia_gasto_area WHERE idconcepto IN (62, 63, 64);

INSERT INTO gerencia_gasto_area
       (area, idconcepto, valor_mensual, vigencia_desde, vigencia_hasta, observacion, usuario) VALUES
  ('PUBLICIDAD', 62, 5287100, '2026-10-01', NULL,
   'Excel: Cclick Action (2.674.700 + 2.612.400)', 'carga_areas'),
  ('PUBLICIDAD', 64, 1000000, '2026-10-01', NULL,
   'Excel: Sebastian Botero', 'carga_areas'),
  -- El variable: su fila se cierra con el mes para que noviembre no pise octubre.
  ('PUBLICIDAD', 63, 3300000, '2026-10-01', '2026-10-31',
   'Excel: Pautas y temas de publicidad, octubre', 'carga_areas');

-- ===========================================================================
-- COMO QUEDO: publicidad tiene que dar 9.587.100 en octubre
-- ===========================================================================
SELECT area, COUNT(*) AS conceptos, ROUND(SUM(valor_mensual)) AS total_mes
  FROM gerencia_gasto_area
 WHERE vigencia_desde <= '2026-10-31'
   AND (vigencia_hasta IS NULL OR vigencia_hasta >= '2026-10-01')
 GROUP BY area WITH ROLLUP;

-- ---------------------------------------------------------------------------
-- MONITOREO DE VARIANZAS: lo que la pantalla necesita en la base
--
-- Se corre UNA VEZ en el CENTRAL (172.19.0.25). No toca ninguna tienda.
-- Es idempotente: correrlo dos veces no cambia nada la segunda.
--
-- La varianza YA llega a bodega: ServicioDiarioReplicaVarianza la trae cada
-- noche a datamart.varianza_resumen_historico -283.929 filas, 11 tiendas,
-- desde 2025-03-19-. La pantalla lee de ahi y no sale a consultar tiendas.
--
-- Faltan tres cosas: con que agrupar los insumos, un indice para que la
-- consulta por rango de fechas no barra la tabla entera, y la homologacion
-- del Agua 600 ml, que existe en las once tiendas y no esta en el maestro.
-- ---------------------------------------------------------------------------

-- ===========================================================================
-- 1. LA MARCACION DE GRUPO
--
-- Los grupos que se pidieron son dos -los caros y las carnes-; "todos" no es
-- una marca sino la ausencia de filtro. Va como columna y no como tabla
-- aparte porque un insumo pertenece a un solo grupo y se administra desde la
-- pantalla de insumos.
-- ===========================================================================
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA='inventarioamericana' AND TABLE_NAME='insumo'
                   AND COLUMN_NAME='grupo_varianza');
SET @sql := IF(@existe = 0,
  'ALTER TABLE inventarioamericana.insumo
     ADD COLUMN grupo_varianza VARCHAR(20)
       CHARACTER SET latin1 COLLATE latin1_spanish_ci NULL
       COMMENT ''CAROS, CARNES o NULL. Agrupa el monitoreo de varianzas''',
  'SELECT ''la columna grupo_varianza ya existe'' AS paso1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- El indice se guarda por COLUMN_NAME y no por INDEX_NAME: preguntando por el
-- nombre del indice, una segunda corrida con el indice ya creado con otro
-- nombre lo volveria a crear duplicado.
SET @existe := (SELECT COUNT(*) FROM information_schema.STATISTICS
                 WHERE TABLE_SCHEMA='inventarioamericana' AND TABLE_NAME='insumo'
                   AND COLUMN_NAME='grupo_varianza' AND SEQ_IN_INDEX=1);
SET @sql := IF(@existe = 0,
  'ALTER TABLE inventarioamericana.insumo ADD INDEX idx_insumo_grupo_varianza (grupo_varianza)',
  'SELECT ''el indice de grupo_varianza ya existe'' AS paso1b');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ===========================================================================
-- 2. LA MARCACION INICIAL
--
-- Solo se marca lo que este SIN marcar. Asi esto no pisa lo que se cambie
-- despues desde la pantalla, y se puede volver a correr sin miedo.
--
-- El orden importa: primero los caros y despues las carnes. Masa Baguette
-- esta clasificada como carnico en el maestro -parece un error viejo-, y
-- marcando primero los caros queda donde debe, con las otras masas.
-- ===========================================================================

-- Los caros: queso, pina, todas las masas y la pasta de tomate.
-- Los ids van explicitos porque los nombres en esta tabla son latin1 y estan
-- sin tildes ni enes: buscar 'Pina' por texto es fragil.
UPDATE inventarioamericana.insumo
   SET grupo_varianza = 'CAROS'
 WHERE grupo_varianza IS NULL
   AND (idinsumo IN (26, 27, 28, 142)      -- Pasta, Pina, Queso, Pina Artesanal
        OR nombre_insumo LIKE 'Masa%');    -- las once masas

-- Las carnes, tal como ya estan clasificadas en el maestro.
UPDATE inventarioamericana.insumo
   SET grupo_varianza = 'CARNES'
 WHERE grupo_varianza IS NULL
   AND categoria = 'Insumos Carnicos';

-- ===========================================================================
-- 3. EL INDICE DE LA TABLA DE HISTORIA
--
-- La pantalla siempre filtra por rango de fechas y casi siempre por tienda.
-- La llave primaria es (iditem, fecha, idtienda), que no sirve para eso: sin
-- este indice toda consulta barre las 283.929 filas.
-- ===========================================================================
SET @existe := (SELECT COUNT(*) FROM information_schema.STATISTICS
                 WHERE TABLE_SCHEMA='datamart' AND TABLE_NAME='varianza_resumen_historico'
                   AND COLUMN_NAME='fecha' AND SEQ_IN_INDEX=1);
SET @sql := IF(@existe = 0,
  'ALTER TABLE datamart.varianza_resumen_historico ADD INDEX idx_varianza_fecha_tienda (fecha, idtienda)',
  'SELECT ''el indice de fecha ya existe'' AS paso3');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ===========================================================================
-- 4. EL AGUA QUE NADIE VE
--
-- El insumo 94 -Agua 600 ml- existe en el inventario de las once tiendas y
-- lleva 526 dias de varianza guardada, pero no esta en insumo_homologacion_
-- tienda. Como todo informe cruza por esa tabla, hoy el agua no aparece en
-- ninguno: no es que no tenga varianza, es que se cae del JOIN.
--
-- Se homologa igual que los demas -el id de tienda es el mismo del maestro,
-- asi esta en las 1.717 filas que ya existen-. El INSERT IGNORE hace que una
-- segunda corrida no haga nada.
-- ===========================================================================
INSERT IGNORE INTO inventarioamericana.insumo_homologacion_tienda (idinsumo, idtienda, insumotienda)
SELECT DISTINCT 94, h.idtienda, 94
  FROM inventarioamericana.insumo_homologacion_tienda h
 WHERE EXISTS (SELECT 1 FROM datamart.varianza_resumen_historico v
                WHERE v.idtienda = h.idtienda AND v.iditem = 94);

-- ===========================================================================
-- COMO QUEDO
-- ===========================================================================
SELECT IFNULL(grupo_varianza, '(sin grupo)') AS grupo, COUNT(*) AS insumos
  FROM inventarioamericana.insumo
 GROUP BY grupo_varianza
 ORDER BY insumos DESC;

SELECT COUNT(*) AS combinaciones_tienda_item_sin_homologar
  FROM (SELECT DISTINCT v.idtienda, v.iditem
          FROM datamart.varianza_resumen_historico v
         WHERE NOT EXISTS (SELECT 1 FROM inventarioamericana.insumo_homologacion_tienda h
                            WHERE h.idtienda = v.idtienda AND h.insumotienda = v.iditem)) x;

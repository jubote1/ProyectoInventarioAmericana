-- Devolver Niquia a los ceros que tenia antes del 2026-11-09.
UPDATE inventarioamericana.gasto_semanal SET valor_calculo=0, valor_gasto=0
 WHERE idgasto_conf=24 AND idtienda=13 AND fecha='2026-11-08';
UPDATE inventarioamericana.gasto_semanal SET valor_calculo=0, valor_gasto=0
 WHERE idgasto_conf=25 AND idtienda=13 AND fecha='2026-11-08';

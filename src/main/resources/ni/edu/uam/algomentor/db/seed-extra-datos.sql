-- =============================================================
-- SEED EXTRA: +10 intentos por algoritmo en todos los perfiles
-- Perfiles: 2=Claudio, 4=Gean, 5=Jeremy, 6=Anthony, 10=Freddy, 11=caludio123
-- Algoritmos: 1=Bubble Sort, 2=Insertion Sort, 3=Gauss, 4=Gauss-Jordan,
--             8=QuickSort, 9=Merge Sort, 10=Counting Sort, 11=Radix Sort,
--             12=Bucket Sort, 64=Quick Sort
-- Habilidades: 1=Signos, 2=Fracciones, 3=Aritmetica, 4=Procedimiento
-- =============================================================

-- -------------------------
-- 1. EJERCICIOS FALTANTES
-- -------------------------
-- QuickSort (8) - necesita ejercicios
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (8,  '5,3,8,1,9,2,7,4,6',       1, 'MANUAL'),
  (8,  '12,4,7,15,1,9,3,11,6,8',  2, 'MANUAL'),
  (8,  '-5,10,-3,8,0,-1,7,-9,4',  3, 'MANUAL'),
  (8,  '23,7,-4,15,0,11,-8,19,2,-6,13', 4, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Counting Sort (10)
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (10, '4,2,2,8,3,3,1',              1, 'MANUAL'),
  (10, '10,7,5,3,8,2,9,1,4,6',      2, 'MANUAL'),
  (10, '0,5,2,8,1,3,7,4,6,9,2,5',   3, 'MANUAL'),
  (10, '15,3,8,1,10,4,7,2,9,6,11,5,13,0', 4, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Radix Sort (11)
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (11, '170,45,75,90,802,24,2,66',               1, 'MANUAL'),
  (11, '329,457,657,839,436,720,355',             2, 'MANUAL'),
  (11, '1234,567,890,123,456,789,321,654',        3, 'MANUAL'),
  (11, '5678,1234,9012,3456,7890,2345,6789,4321', 4, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Gauss (3) - agregar más variedad
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (3, '2,1,-1,8' || chr(10) || '-3,-1,2,-11' || chr(10) || '-2,1,2,-3', 2, 'MANUAL'),
  (3, '1,2,1,0' || chr(10) || '2,3,0,-7' || chr(10) || '1,-1,-3,-7',   3, 'MANUAL'),
  (3, '3,2,-1,5' || chr(10) || '2,-2,4,-2' || chr(10) || '-1,0.5,-1,0', 4, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Bubble Sort (1) - más variedad
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (1, '3,1,4,1,5,9,2,6',    1, 'MANUAL'),
  (1, '10,9,8,7,6,5,4,3,2,1', 2, 'MANUAL'),
  (1, '-3,5,-7,2,0,-1,8,-4', 3, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Insertion Sort (2) - más variedad
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (2, '5,2,4,6,1,3',              1, 'MANUAL'),
  (2, '31,41,59,26,41,58',        2, 'MANUAL'),
  (2, '-5,3,-2,8,0,-9,4,7,-1,6',  3, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Merge Sort (9)
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (9, '5,2,4,6,1,3',              1, 'MANUAL'),
  (9, '38,27,43,3,9,82,10',       2, 'MANUAL'),
  (9, '14,7,3,12,9,11,6,2,8,15',  3, 'MANUAL')
ON CONFLICT DO NOTHING;

-- Bucket Sort (12)
INSERT INTO ejercicio (algoritmo_id, datos, dificultad, origen) VALUES
  (12, '0.78,0.17,0.39,0.26,0.72,0.94,0.21,0.12', 2, 'MANUAL'),
  (12, '-15,8,3,-7,22,0,-4,11,19,-2',              3, 'MANUAL')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- HELPER: usar CTEs temporales para obtener IDs de ejercicios por algoritmo
-- =============================================================================

-- ============================================================
-- 2. INTENTOS - CLAUDIO (perfil_id=2)
-- ============================================================

-- Claudio: Bubble Sort (mix aciertos/fallos)
WITH ej AS (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad)
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, id, 'PRACTICA', correcto, dur,
       now() - (rn * INTERVAL '3 days')
FROM (
  SELECT id, correcto, dur, rn FROM (VALUES
    (1, true,  85, 1),(2, true,  72, 2),(3, false, 110, 3),(4, true,  65, 4),
    (5, false, 95, 5),(6, true,  58, 6),(7, true,  80, 7),(8, false, 120, 8),
    (9, true,  70, 9),(10,true,  60,10),(11,false, 100,11),(12,true,  75,12)
  ) AS t(rn, correcto, dur, rn2)
) sub(id, correcto, dur, rn)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e
ON CONFLICT DO NOTHING;

-- Vamos con INSERT directo usando subqueries para los IDs
-- Claudio - Insertion Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA', true,  70,  1),
  ('PRACTICA', true,  65,  2),
  ('PRACTICA', false, 90,  4),
  ('SIMULACION', true, 80, 6),
  ('PRACTICA', true,  55,  8),
  ('PRACTICA', false, 110, 10),
  ('SIMULACION', false,95, 12),
  ('PRACTICA', true,  60,  14),
  ('PRACTICA', true,  75,  16),
  ('SIMULACION', true, 85, 18),
  ('PRACTICA', false, 100, 20),
  ('PRACTICA', true,  58,  22)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=2 ORDER BY dificultad LIMIT 1) e;

-- Claudio - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  90,  2),
  ('PRACTICA',   false, 130, 4),
  ('SIMULACION', true,  100, 5),
  ('PRACTICA',   true,  85,  7),
  ('PRACTICA',   false, 115, 9),
  ('SIMULACION', true,  95,  11),
  ('PRACTICA',   true,  70,  13),
  ('PRACTICA',   true,  80,  15),
  ('SIMULACION', false, 140, 17),
  ('PRACTICA',   true,  75,  19),
  ('PRACTICA',   true,  65,  21),
  ('PRACTICA',   false, 120, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- Claudio - Gauss
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  120, 1),
  ('PRACTICA',   false, 180, 3),
  ('SIMULACION', true,  150, 5),
  ('PRACTICA',   true,  130, 7),
  ('PRACTICA',   false, 200, 9),
  ('PRACTICA',   true,  140, 11),
  ('SIMULACION', true,  125, 13),
  ('PRACTICA',   false, 170, 15),
  ('PRACTICA',   true,  135, 17),
  ('PRACTICA',   true,  115, 19),
  ('SIMULACION', false, 190, 21),
  ('PRACTICA',   true,  145, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=3 ORDER BY dificultad LIMIT 1) e;

-- Claudio - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  60,  1),('PRACTICA',   true,  55,  3),
  ('PRACTICA',   false, 80,  5),('SIMULACION', true,  70,  7),
  ('PRACTICA',   true,  65,  9),('PRACTICA',   false, 90,  11),
  ('SIMULACION', true,  75,  13),('PRACTICA',  true,  58,  15),
  ('PRACTICA',   false, 85,  17),('PRACTICA',  true,  62,  19),
  ('SIMULACION', true,  68,  21),('PRACTICA',  true,  54,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- Claudio - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  75,  2),('PRACTICA',   false, 100, 4),
  ('SIMULACION', true,  85,  6),('PRACTICA',   true,  70,  8),
  ('PRACTICA',   false, 110, 10),('PRACTICA',  true,  80,  12),
  ('SIMULACION', true,  90,  14),('PRACTICA',  false, 95,  16),
  ('PRACTICA',   true,  72,  18),('PRACTICA',  true,  68,  20),
  ('SIMULACION', true,  88,  22),('PRACTICA',  false, 105, 24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- Claudio - Merge Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 2, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  80,  1),('PRACTICA',   true,  75,  3),
  ('PRACTICA',   false, 100, 5),('SIMULACION', true,  90,  7),
  ('PRACTICA',   true,  70,  9),('PRACTICA',   true,  85,  11),
  ('SIMULACION', false, 110, 13),('PRACTICA',  true,  78,  15),
  ('PRACTICA',   false, 95,  17),('PRACTICA',  true,  73,  19),
  ('SIMULACION', true,  88,  21),('PRACTICA',  true,  65,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=9 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 3. INTENTOS - GEAN (perfil_id=4)
-- ============================================================

-- Gean - Bubble Sort (muchos aciertos, es bueno)
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  50,  1),('PRACTICA',   true,  48,  2),
  ('SIMULACION', true,  55,  3),('PRACTICA',   true,  52,  4),
  ('PRACTICA',   true,  47,  5),('PRACTICA',   false, 70,  6),
  ('SIMULACION', true,  58,  7),('PRACTICA',   true,  51,  8),
  ('PRACTICA',   true,  49,  9),('SIMULACION', true,  53,  10),
  ('PRACTICA',   true,  46,  11),('PRACTICA',  true,  60,  12),
  ('PRACTICA',   false, 75,  13)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e;

-- Gean - Gauss-Jordan
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  140, 1),('PRACTICA',   true,  130, 3),
  ('SIMULACION', false, 180, 5),('PRACTICA',   true,  150, 7),
  ('PRACTICA',   true,  120, 9),('PRACTICA',   false, 200, 11),
  ('SIMULACION', true,  160, 13),('PRACTICA',  true,  135, 15),
  ('PRACTICA',   true,  125, 17),('SIMULACION', true, 145, 19),
  ('PRACTICA',   false, 190, 21),('PRACTICA',  true,  128, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=4 ORDER BY dificultad LIMIT 1) e;

-- Gean - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  80,  1),('PRACTICA',   true,  75,  2),
  ('SIMULACION', true,  90,  3),('PRACTICA',   false, 110, 4),
  ('PRACTICA',   true,  70,  5),('PRACTICA',   true,  65,  6),
  ('SIMULACION', true,  85,  7),('PRACTICA',   false, 100, 8),
  ('PRACTICA',   true,  72,  9),('PRACTICA',   true,  68,  10),
  ('SIMULACION', true,  95,  11),('PRACTICA',  true,  60,  12)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- Gean - Merge Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  90,  2),('PRACTICA',   false, 120, 4),
  ('SIMULACION', true,  100, 6),('PRACTICA',   true,  85,  8),
  ('PRACTICA',   true,  80,  10),('PRACTICA',  false, 115, 12),
  ('SIMULACION', true,  95,  14),('PRACTICA',  true,  88,  16),
  ('PRACTICA',   true,  75,  18),('SIMULACION', true, 105, 20),
  ('PRACTICA',   false, 130, 22),('PRACTICA',  true,  82,  24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=9 ORDER BY dificultad LIMIT 1) e;

-- Gean - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  55,  1),('PRACTICA',   true,  50,  3),
  ('SIMULACION', true,  60,  5),('PRACTICA',   true,  52,  7),
  ('PRACTICA',   false, 75,  9),('PRACTICA',   true,  58,  11),
  ('SIMULACION', true,  65,  13),('PRACTICA',  true,  48,  15),
  ('PRACTICA',   true,  54,  17),('SIMULACION', false, 80, 19),
  ('PRACTICA',   true,  56,  21),('PRACTICA',  true,  51,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- Gean - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 4, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  70,  1),('PRACTICA',   true,  65,  3),
  ('SIMULACION', true,  80,  5),('PRACTICA',   false, 95,  7),
  ('PRACTICA',   true,  68,  9),('PRACTICA',   true,  72,  11),
  ('SIMULACION', true,  85,  13),('PRACTICA',  true,  60,  15),
  ('PRACTICA',   false, 100, 17),('PRACTICA',  true,  75,  19),
  ('SIMULACION', true,  78,  21),('PRACTICA',  true,  63,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 4. INTENTOS - JEREMY (perfil_id=5) - Muchos fallos (es el que tiene más errores)
-- ============================================================

-- Jeremy - Bubble Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 120, 1),('PRACTICA',   true,  90,  3),
  ('PRACTICA',   false, 140, 5),('SIMULACION', false, 150, 7),
  ('PRACTICA',   true,  100, 9),('PRACTICA',   false, 130, 11),
  ('SIMULACION', false, 160, 13),('PRACTICA',  true,  85,  15),
  ('PRACTICA',   false, 145, 17),('PRACTICA',  false, 135, 19),
  ('SIMULACION', true,  95,  21),('PRACTICA',  false, 125, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e;

-- Jeremy - Insertion Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 110, 2),('PRACTICA',   false, 130, 4),
  ('SIMULACION', true,  95,  6),('PRACTICA',   false, 140, 8),
  ('PRACTICA',   true,  80,  10),('PRACTICA',  false, 125, 12),
  ('SIMULACION', false, 150, 14),('PRACTICA',  true,  90,  16),
  ('PRACTICA',   false, 135, 18),('PRACTICA',  false, 120, 20),
  ('SIMULACION', true,  100, 22),('PRACTICA',  false, 115, 24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=2 ORDER BY dificultad LIMIT 1) e;

-- Jeremy - Gauss
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 200, 1),('PRACTICA',   false, 220, 3),
  ('SIMULACION', false, 250, 5),('PRACTICA',   true,  180, 7),
  ('PRACTICA',   false, 230, 9),('PRACTICA',   false, 210, 11),
  ('SIMULACION', true,  190, 13),('PRACTICA',  false, 240, 15),
  ('PRACTICA',   false, 215, 17),('PRACTICA',  true,  175, 19),
  ('SIMULACION', false, 235, 21),('PRACTICA',  false, 205, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=3 ORDER BY dificultad LIMIT 1) e;

-- Jeremy - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 140, 1),('PRACTICA',   true,  110, 3),
  ('SIMULACION', false, 160, 5),('PRACTICA',   false, 150, 7),
  ('PRACTICA',   true,  100, 9),('PRACTICA',   false, 145, 11),
  ('SIMULACION', false, 170, 13),('PRACTICA',  false, 135, 15),
  ('PRACTICA',   true,  105, 17),('PRACTICA',  false, 155, 19),
  ('SIMULACION', true,  115, 21),('PRACTICA',  false, 148, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- Jeremy - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 95,  1),('PRACTICA',   false, 110, 3),
  ('SIMULACION', true,  80,  5),('PRACTICA',   false, 100, 7),
  ('PRACTICA',   false, 105, 9),('PRACTICA',   true,  75,  11),
  ('SIMULACION', false, 115, 13),('PRACTICA',  false, 98,  15),
  ('PRACTICA',   true,  82,  17),('PRACTICA',  false, 108, 19),
  ('SIMULACION', false, 120, 21),('PRACTICA',  false, 92,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- Jeremy - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 5, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 115, 2),('PRACTICA',   false, 130, 4),
  ('SIMULACION', true,  100, 6),('PRACTICA',   false, 120, 8),
  ('PRACTICA',   false, 125, 10),('PRACTICA',  true,  95,  12),
  ('SIMULACION', false, 140, 14),('PRACTICA',  false, 118, 16),
  ('PRACTICA',   false, 135, 18),('PRACTICA',  true,  105, 20),
  ('SIMULACION', false, 145, 22),('PRACTICA',  false, 112, 24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 5. INTENTOS - ANTHONY (perfil_id=6) - Mitad aciertos, mitad fallos
-- ============================================================

-- Anthony - Bubble Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  70,  1),('PRACTICA',   false, 95,  2),
  ('SIMULACION', true,  80,  3),('PRACTICA',   false, 100, 4),
  ('PRACTICA',   true,  65,  5),('PRACTICA',   false, 90,  6),
  ('SIMULACION', true,  75,  7),('PRACTICA',   true,  68,  8),
  ('PRACTICA',   false, 98,  9),('PRACTICA',   true,  72,  10),
  ('SIMULACION', false, 105, 11),('PRACTICA',  true,  62,  12)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e;

-- Anthony - Gauss-Jordan
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  150, 1),('PRACTICA',   false, 190, 3),
  ('SIMULACION', true,  160, 5),('PRACTICA',   false, 200, 7),
  ('PRACTICA',   true,  145, 9),('PRACTICA',   false, 185, 11),
  ('SIMULACION', true,  170, 13),('PRACTICA',  true,  155, 15),
  ('PRACTICA',   false, 195, 17),('PRACTICA',  true,  140, 19),
  ('SIMULACION', false, 210, 21),('PRACTICA',  true,  165, 23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=4 ORDER BY dificultad LIMIT 1) e;

-- Anthony - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  85,  1),('PRACTICA',   false, 110, 3),
  ('SIMULACION', true,  95,  5),('PRACTICA',   false, 115, 7),
  ('PRACTICA',   true,  80,  9),('PRACTICA',   false, 105, 11),
  ('SIMULACION', true,  90,  13),('PRACTICA',  true,  78,  15),
  ('PRACTICA',   false, 120, 17),('PRACTICA',  true,  82,  19),
  ('SIMULACION', false, 125, 21),('PRACTICA',  true,  75,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- Anthony - Merge Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 100, 2),('PRACTICA',   true,  80,  4),
  ('SIMULACION', false, 110, 6),('PRACTICA',   true,  85,  8),
  ('PRACTICA',   false, 105, 10),('PRACTICA',  true,  75,  12),
  ('SIMULACION', true,  90,  14),('PRACTICA',  false, 115, 16),
  ('PRACTICA',   true,  78,  18),('PRACTICA',  false, 108, 20),
  ('SIMULACION', true,  88,  22),('PRACTICA',  true,  72,  24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=9 ORDER BY dificultad LIMIT 1) e;

-- Anthony - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  60,  1),('PRACTICA',   false, 80,  3),
  ('SIMULACION', true,  70,  5),('PRACTICA',   false, 85,  7),
  ('PRACTICA',   true,  58,  9),('PRACTICA',   false, 75,  11),
  ('SIMULACION', true,  65,  13),('PRACTICA',  true,  55,  15),
  ('PRACTICA',   false, 88,  17),('PRACTICA',  true,  62,  19),
  ('SIMULACION', false, 90,  21),('PRACTICA',  true,  57,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- Anthony - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 6, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 90,  1),('PRACTICA',   true,  72,  3),
  ('SIMULACION', false, 100, 5),('PRACTICA',   true,  68,  7),
  ('PRACTICA',   false, 95,  9),('PRACTICA',   true,  75,  11),
  ('SIMULACION', false, 105, 13),('PRACTICA',  true,  70,  15),
  ('PRACTICA',   false, 98,  17),('PRACTICA',  true,  65,  19),
  ('SIMULACION', true,  82,  21),('PRACTICA',  false, 92,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 6. INTENTOS - FREDDY (perfil_id=10) - Principalmente aciertos
-- ============================================================

-- Freddy - Bubble Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  55,  1),('PRACTICA',   true,  52,  2),
  ('SIMULACION', true,  60,  3),('PRACTICA',   true,  50,  4),
  ('PRACTICA',   false, 72,  5),('PRACTICA',   true,  48,  6),
  ('SIMULACION', true,  58,  7),('PRACTICA',   true,  54,  8),
  ('PRACTICA',   true,  51,  9),('PRACTICA',   false, 75,  10),
  ('SIMULACION', true,  56,  11),('PRACTICA',  true,  49,  12)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e;

-- Freddy - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  75,  1),('PRACTICA',   true,  70,  3),
  ('SIMULACION', true,  80,  5),('PRACTICA',   false, 95,  7),
  ('PRACTICA',   true,  68,  9),('PRACTICA',   true,  72,  11),
  ('SIMULACION', true,  85,  13),('PRACTICA',  true,  65,  15),
  ('PRACTICA',   false, 98,  17),('PRACTICA',  true,  73,  19),
  ('SIMULACION', true,  78,  21),('PRACTICA',  true,  60,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- Freddy - Gauss
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  130, 2),('PRACTICA',   true,  120, 4),
  ('SIMULACION', true,  140, 6),('PRACTICA',   false, 165, 8),
  ('PRACTICA',   true,  125, 10),('PRACTICA',  true,  118, 12),
  ('SIMULACION', true,  135, 14),('PRACTICA',  false, 170, 16),
  ('PRACTICA',   true,  128, 18),('PRACTICA',  true,  122, 20),
  ('SIMULACION', true,  142, 22),('PRACTICA',  true,  115, 24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=3 ORDER BY dificultad LIMIT 1) e;

-- Freddy - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  52,  1),('PRACTICA',   true,  48,  3),
  ('SIMULACION', true,  58,  5),('PRACTICA',   true,  50,  7),
  ('PRACTICA',   false, 70,  9),('PRACTICA',   true,  54,  11),
  ('SIMULACION', true,  60,  13),('PRACTICA',  true,  46,  15),
  ('PRACTICA',   true,  55,  17),('PRACTICA',  false, 72,  19),
  ('SIMULACION', true,  62,  21),('PRACTICA',  true,  47,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- Freddy - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  68,  1),('PRACTICA',   true,  65,  3),
  ('SIMULACION', true,  75,  5),('PRACTICA',   false, 88,  7),
  ('PRACTICA',   true,  62,  9),('PRACTICA',   true,  70,  11),
  ('SIMULACION', true,  80,  13),('PRACTICA',  true,  60,  15),
  ('PRACTICA',   false, 90,  17),('PRACTICA',  true,  67,  19),
  ('SIMULACION', true,  72,  21),('PRACTICA',  true,  58,  23)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- Freddy - Merge Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 10, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   true,  82,  2),('PRACTICA',   true,  78,  4),
  ('SIMULACION', true,  88,  6),('PRACTICA',   false, 100, 8),
  ('PRACTICA',   true,  75,  10),('PRACTICA',  true,  80,  12),
  ('SIMULACION', true,  92,  14),('PRACTICA',  true,  72,  16),
  ('PRACTICA',   false, 105, 18),('PRACTICA',  true,  85,  20),
  ('SIMULACION', true,  90,  22),('PRACTICA',  true,  70,  24)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=9 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 7. INTENTOS - CALUDIO123 (perfil_id=11) - Mixto, mejorando
-- ============================================================

-- caludio123 - Bubble Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 11, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 100, 20),('PRACTICA',   false, 95,  18),
  ('PRACTICA',   false, 110, 16),('SIMULACION', true,  85,  14),
  ('PRACTICA',   true,  80,  12),('PRACTICA',   false, 90,  10),
  ('SIMULACION', true,  75,  8),('PRACTICA',   true,  70,  6),
  ('PRACTICA',   true,  65,  4),('SIMULACION', true,  72,  2),
  ('PRACTICA',   true,  68,  1),('PRACTICA',  false, 88,  3)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=1 ORDER BY dificultad LIMIT 1) e;

-- caludio123 - QuickSort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 11, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 130, 15),('PRACTICA',   false, 120, 12),
  ('SIMULACION', false, 140, 10),('PRACTICA',   true,  105, 8),
  ('PRACTICA',   false, 115, 6),('PRACTICA',   true,  100, 4),
  ('SIMULACION', true,  110, 2),('PRACTICA',   true,  95,  1),
  ('PRACTICA',   true,  90,  3),('SIMULACION', true,  98,  5),
  ('PRACTICA',   false, 125, 7),('PRACTICA',  true,  88,  9)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=8 ORDER BY dificultad LIMIT 1) e;

-- caludio123 - Gauss-Jordan
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 11, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 200, 18),('PRACTICA',   false, 190, 15),
  ('SIMULACION', true,  170, 12),('PRACTICA',   false, 185, 10),
  ('PRACTICA',   true,  160, 8),('PRACTICA',   false, 180, 6),
  ('SIMULACION', true,  155, 4),('PRACTICA',   true,  150, 2),
  ('PRACTICA',   true,  145, 1),('SIMULACION', true,  158, 3),
  ('PRACTICA',   true,  142, 5),('PRACTICA',  false, 175, 7)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=4 ORDER BY dificultad LIMIT 1) e;

-- caludio123 - Counting Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 11, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 85,  12),('PRACTICA',   false, 90,  10),
  ('SIMULACION', true,  72,  8),('PRACTICA',   false, 80,  6),
  ('PRACTICA',   true,  68,  4),('PRACTICA',   true,  65,  2),
  ('SIMULACION', true,  70,  1),('PRACTICA',   true,  62,  3),
  ('PRACTICA',   false, 78,  5),('SIMULACION', true,  67,  7),
  ('PRACTICA',   true,  60,  9),('PRACTICA',  false, 82,  11)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=10 ORDER BY dificultad LIMIT 1) e;

-- caludio123 - Radix Sort
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT 11, e.id, m.modo, m.correcto, m.dur, now() - (m.dias * INTERVAL '1 day')
FROM (VALUES
  ('PRACTICA',   false, 110, 10),('PRACTICA',   false, 105, 8),
  ('SIMULACION', true,  90,  6),('PRACTICA',   false, 100, 4),
  ('PRACTICA',   true,  85,  2),('PRACTICA',   true,  80,  1),
  ('SIMULACION', true,  88,  3),('PRACTICA',   true,  82,  5),
  ('PRACTICA',   false, 98,  7),('SIMULACION', true,  86,  9),
  ('PRACTICA',   true,  78,  11),('PRACTICA',  false, 102, 12)
) AS m(modo, correcto, dur, dias)
CROSS JOIN (SELECT id FROM ejercicio WHERE algoritmo_id=11 ORDER BY dificultad LIMIT 1) e;

-- ============================================================
-- 8. PROGRESO DE HABILIDADES (actualizar según intentos reales)
-- ============================================================

-- Claudio (2): buen promedio general
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (2, 1, 38, 12), -- Signos
  (2, 2, 29, 15), -- Fracciones
  (2, 3, 42, 8),  -- Aritmetica
  (2, 4, 35, 10)  -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- Gean (4): el mejor del grupo
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (4, 1, 55, 5),  -- Signos
  (4, 2, 48, 8),  -- Fracciones
  (4, 3, 60, 4),  -- Aritmetica
  (4, 4, 52, 6)   -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- Jeremy (5): muchos errores
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (5, 1, 10, 42), -- Signos
  (5, 2, 8,  38), -- Fracciones
  (5, 3, 15, 35), -- Aritmetica
  (5, 4, 5,  48)  -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- Anthony (6): 50/50
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (6, 1, 25, 22), -- Signos
  (6, 2, 20, 25), -- Fracciones
  (6, 3, 28, 18), -- Aritmetica
  (6, 4, 22, 24)  -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- Freddy (10): muy bueno
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (10, 1, 50, 6),  -- Signos
  (10, 2, 44, 10), -- Fracciones
  (10, 3, 56, 4),  -- Aritmetica
  (10, 4, 48, 8)   -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- caludio123 (11): mejorando con el tiempo
INSERT INTO progreso_habilidad (perfil_id, habilidad_id, aciertos, errores)
VALUES
  (11, 1, 18, 28), -- Signos
  (11, 2, 15, 30), -- Fracciones
  (11, 3, 22, 24), -- Aritmetica
  (11, 4, 12, 32)  -- Procedimiento
ON CONFLICT (perfil_id, habilidad_id)
DO UPDATE SET
  aciertos = progreso_habilidad.aciertos + EXCLUDED.aciertos,
  errores  = progreso_habilidad.errores  + EXCLUDED.errores,
  ultima_actualizacion = now();

-- ============================================================
-- 9. PASOS DE INTENTO Y ERRORES (para los intentos fallidos)
-- ============================================================

-- Insertar pasos para intentos fallidos de Bubble Sort de Jeremy (perfil 5)
-- (Solo insertamos pasos para algunos intentos recientes para simular errores detectados)
INSERT INTO paso_intento (intento_id, numero_paso, respuesta, correcto)
SELECT i.id, 1, 'incorrecto', false
FROM intento i
WHERE i.perfil_id = 5 AND i.correcto = false
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id = 1)
  AND NOT EXISTS (SELECT 1 FROM paso_intento p WHERE p.intento_id = i.id)
LIMIT 8;

INSERT INTO paso_intento (intento_id, numero_paso, respuesta, correcto)
SELECT i.id, 1, 'incorrecto', false
FROM intento i
WHERE i.perfil_id = 5 AND i.correcto = false
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id IN (3, 8))
  AND NOT EXISTS (SELECT 1 FROM paso_intento p WHERE p.intento_id = i.id)
LIMIT 10;

-- Pasos fallidos para Anthony (perfil 6)
INSERT INTO paso_intento (intento_id, numero_paso, respuesta, correcto)
SELECT i.id, 1, 'incorrecto', false
FROM intento i
WHERE i.perfil_id = 6 AND i.correcto = false
  AND NOT EXISTS (SELECT 1 FROM paso_intento p WHERE p.intento_id = i.id)
LIMIT 12;

-- Pasos fallidos para caludio123 (perfil 11)
INSERT INTO paso_intento (intento_id, numero_paso, respuesta, correcto)
SELECT i.id, 1, 'incorrecto', false
FROM intento i
WHERE i.perfil_id = 11 AND i.correcto = false
  AND NOT EXISTS (SELECT 1 FROM paso_intento p WHERE p.intento_id = i.id)
LIMIT 12;

-- ============================================================
-- 10. ERRORES DETECTADOS en pasos fallidos
-- ============================================================

-- Jeremy - errores de SIGNO y ARITMETICO en Bubble Sort
INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id, 1, 'SIGNO', 'Signo invertido en comparacion de elementos'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 5
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id = 1)
  AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 5;

INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id, 3, 'ARITMETICO', 'Error en calculo de posicion durante ordenamiento'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 5
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id = 1)
  AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 3;

-- Jeremy - errores en Gauss y QuickSort
INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id, 2, 'FRACCION', 'Error al operar con fracciones en eliminacion gaussiana'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 5
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id IN (3, 8))
  AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 6;

INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id, 4, 'OPERACION_INVALIDA', 'Fila modificada incorrectamente fuera de la operacion'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 5
  AND i.ejercicio_id IN (SELECT id FROM ejercicio WHERE algoritmo_id IN (3, 8))
  AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 4;

-- Anthony - mezcla de errores
INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id,
  CASE ROW_NUMBER() OVER () % 4
    WHEN 0 THEN 1 WHEN 1 THEN 2 WHEN 2 THEN 3 ELSE 4
  END,
  CASE ROW_NUMBER() OVER () % 4
    WHEN 0 THEN 'SIGNO' WHEN 1 THEN 'FRACCION' WHEN 2 THEN 'ARITMETICO' ELSE 'OPERACION_INVALIDA'
  END,
  'Error detectado automaticamente'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 6 AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 10;

-- caludio123 - errores con patron de mejora
INSERT INTO error_detectado (paso_intento_id, habilidad_id, tipo_error, descripcion)
SELECT p.id,
  CASE ROW_NUMBER() OVER (ORDER BY i.fecha_inicio DESC) % 3
    WHEN 0 THEN 1 WHEN 1 THEN 2 ELSE 3
  END,
  CASE ROW_NUMBER() OVER (ORDER BY i.fecha_inicio DESC) % 3
    WHEN 0 THEN 'SIGNO' WHEN 1 THEN 'ARITMETICO' ELSE 'FRACCION'
  END,
  'Error detectado en paso de intento'
FROM paso_intento p
JOIN intento i ON p.intento_id = i.id
WHERE i.perfil_id = 11 AND p.correcto = false
  AND NOT EXISTS (SELECT 1 FROM error_detectado ed WHERE ed.paso_intento_id = p.id)
LIMIT 10;

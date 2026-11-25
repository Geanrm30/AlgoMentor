-- Bucket Sort intentos para todos los perfiles
INSERT INTO intento (perfil_id, ejercicio_id, modo, correcto, duracion_segundos, fecha_inicio)
SELECT p.perfil_id, e.id, p.modo, p.correcto, p.dur, now() - (p.dias * INTERVAL '1 day')
FROM (SELECT id FROM ejercicio WHERE algoritmo_id = 12 ORDER BY dificultad LIMIT 1) e,
(VALUES
  (2, 'PRACTICA',   true,  80, 1),(2, 'PRACTICA',   false, 110, 3),(2, 'SIMULACION', true,  90, 5),
  (2, 'PRACTICA',   true,  75, 7),(2, 'PRACTICA',   false, 100, 9),(2, 'PRACTICA',   true,  85, 11),
  (2, 'SIMULACION', true,  70, 13),(2, 'PRACTICA',  false, 95, 15),(2, 'PRACTICA',   true,  78, 17),
  (2, 'PRACTICA',   true,  72, 19),(2, 'SIMULACION', false, 105, 21),(2, 'PRACTICA', true,  68, 23),
  (4, 'PRACTICA',   true,  65, 1),(4, 'PRACTICA',   true,  60, 3),(4, 'SIMULACION', true,  70, 5),
  (4, 'PRACTICA',   true,  58, 7),(4, 'PRACTICA',   false, 80, 9),(4, 'PRACTICA',   true,  62, 11),
  (4, 'SIMULACION', true,  68, 13),(4, 'PRACTICA',  true,  55, 15),(4, 'PRACTICA',   true,  63, 17),
  (4, 'PRACTICA',   false, 82, 19),(4, 'SIMULACION', true, 66, 21),(4, 'PRACTICA',   true,  57, 23),
  (5, 'PRACTICA',   false, 130, 1),(5, 'PRACTICA',  false, 140, 3),(5, 'SIMULACION', true,  110, 5),
  (5, 'PRACTICA',   false, 135, 7),(5, 'PRACTICA',  false, 125, 9),(5, 'PRACTICA',   true,  105, 11),
  (5, 'SIMULACION', false, 145, 13),(5, 'PRACTICA', false, 138, 15),(5, 'PRACTICA',  true,  108, 17),
  (5, 'PRACTICA',   false, 132, 19),(5, 'SIMULACION', true, 112, 21),(5, 'PRACTICA', false, 128, 23),
  (6, 'PRACTICA',   true,  88, 2),(6, 'PRACTICA',   false, 110, 4),(6, 'SIMULACION', true,  95, 6),
  (6, 'PRACTICA',   false, 105, 8),(6, 'PRACTICA',  true,  82, 10),(6, 'PRACTICA',   false, 100, 12),
  (6, 'SIMULACION', true,  90, 14),(6, 'PRACTICA',  true,  85, 16),(6, 'PRACTICA',   false, 108, 18),
  (6, 'PRACTICA',   true,  80, 20),(6, 'SIMULACION', false, 115, 22),(6, 'PRACTICA', true,  78, 24),
  (10,'PRACTICA',   true,  60, 1),(10,'PRACTICA',   true,  55, 3),(10,'SIMULACION', true,  65, 5),
  (10,'PRACTICA',   false, 75, 7),(10,'PRACTICA',   true,  58, 9),(10,'PRACTICA',   true,  62, 11),
  (10,'SIMULACION', true,  70, 13),(10,'PRACTICA',  true,  52, 15),(10,'PRACTICA',   false, 78, 17),
  (10,'PRACTICA',   true,  56, 19),(10,'SIMULACION', true, 68, 21),(10,'PRACTICA',   true,  50, 23),
  (11,'PRACTICA',   false, 120, 15),(11,'PRACTICA', false, 110, 12),(11,'SIMULACION', true, 95, 10),
  (11,'PRACTICA',   false, 105, 8),(11,'PRACTICA',  true,  90, 6),(11,'PRACTICA',   true,  85, 4),
  (11,'SIMULACION', true,  92, 2),(11,'PRACTICA',   false, 100, 1),(11,'PRACTICA',   true,  88, 3),
  (11,'PRACTICA',   true,  82, 5),(11,'SIMULACION', false, 108, 7),(11,'PRACTICA',   true,  80, 9)
) AS p(perfil_id, modo, correcto, dur, dias);

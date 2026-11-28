-- Ejecutar sobre una base AlgoMentor existente. No borra ni modifica registros existentes.
BEGIN;

INSERT INTO algoritmo (nombre, categoria, complejidad, descripcion) VALUES
    ('Quick Sort', 'ORDENAMIENTO', 'O(n log n); peor O(n^2)', 'Particiones en tres vias con pivote y manejo de repetidos'),
    ('Merge Sort', 'ORDENAMIENTO', 'O(n log n)', 'Divide subarreglos y los mezcla de forma estable'),
    ('Counting Sort', 'ORDENAMIENTO', 'O(n+k); disperso O(n log u)', 'Cuenta frecuencias con desplazamiento; usa conteo disperso para rangos grandes'),
    ('Radix Sort', 'ORDENAMIENTO', 'O(d(n+b))', 'Diez pasadas decimales estables sobre claves long desplazadas'),
    ('Bucket Sort', 'ORDENAMIENTO', 'Prom. O(n+k); peor O(n^2)', 'Distribuye en cubetas, ordena por insercion y concatena'),
    ('Selection Sort', 'ORDENAMIENTO', 'O(n^2)', 'Busca el minimo y lo coloca al inicio del tramo sin ordenar'),
    ('Shell Sort', 'ORDENAMIENTO', 'O(n log n) a O(n^2)', 'Insercion con saltos decrecientes'),
    ('Heap Sort', 'ORDENAMIENTO', 'O(n log n)', 'Monticulo maximo; extrae la raiz hacia el final'),
    ('Cocktail Sort', 'ORDENAMIENTO', 'O(n^2)', 'Burbuja bidireccional'),
    ('Comb Sort', 'ORDENAMIENTO', 'O(n^2); promedio mejor', 'Burbuja con brecha decreciente (factor 1.3)'),
    ('Gnome Sort', 'ORDENAMIENTO', 'O(n^2)', 'Avanza si el par esta en orden; si no, intercambia y retrocede')
ON CONFLICT (nombre) DO NOTHING;

COMMIT;

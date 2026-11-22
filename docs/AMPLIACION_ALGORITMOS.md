# Informe de ampliación de AlgoMentor

Fecha de verificación: 17 de noviembre de 2026.
Rama: `feature/nuevos-algoritmos`, creada desde `main` sincronizado con `origin/main` (base `a7c2e57`).

## Resultado

Se implementaron Quick Sort, Merge Sort, Counting Sort, Radix Sort y Bucket Sort sobre
`AlgoritmoOrdenamiento`. El catálogo contiene nueve algoritmos: siete ordenamientos y
los dos métodos matriciales existentes. Ningún ordenamiento nuevo modifica la entrada.

`TrazaOrdenamiento` conserva instantáneas continuas, índices relevantes y explicaciones
en español. `Paso` conserva el constructor previo y añade un índice opcional de pivote.
Los tipos nuevos distinguen particiones, divisiones, mezclas, escrituras, conteos,
distribuciones, pasadas y ordenamiento de cubetas. Las operaciones matriciales previas
siguen utilizando sus tipos originales.

## Funcionalidades integradas

- Simulador con nueve opciones, generación de entradas, navegación completa en ambos sentidos,
  pivote violeta y desplazamiento horizontal para arreglos anchos.
- Práctica reconoce los siete ordenamientos del catálogo y evalúa datos simples o versionados.
- Biblioteca genera ejercicios por algoritmo y dificultad; el usuario los guarda mediante Hibernate.
- Validación compartida con la importación: formato, límites visuales, origen y dificultad.
- Comparador de siete resultados con comparaciones, intercambios, escrituras del arreglo,
  escrituras internas de cubetas, conteos, distribuciones y pasos educativos independientes.
- Terminal con los comandos `quick`, `merge`, `counting`, `radix` y `bucket`, además de los previos.
- JSON de intercambio versión 1 compatible con los nombres nuevos, sin IDs locales.
- Esquema inicial y migración transaccional idempotente para instalaciones existentes.
- Entidades, habilidades, guardado de intentos, estadísticas y servicios de persistencia conservados.

## Decisiones y problemas resueltos

Quick Sort usa tres vías para no volver a particionar los iguales al pivote. Los intervalos
se procesan por tamaño para acotar la pila auxiliar y el pivote se rastrea incluso al intercambiarlo.
Merge Sort mezcla de forma estable desde un arreglo auxiliar y registra cada escritura real.
Counting Sort calcula el rango con `long`, limita el conteo denso a 65 536 casillas y usa
un árbol de frecuencias dispersas para rangos mayores. Sus comparaciones reales se registran;
la variante densa no inventa comparaciones de orden.

Radix Sort desplaza las claves firmadas en `long` y realiza diez pasadas decimales estables,
sin negar `Integer.MIN_VALUE`. Bucket Sort usa `n` cubetas, inserción estable y aritmética
de rango en `long`, incluyendo negativos, duplicados y extremos.

Las escrituras de mezcla, conteo y radix actualizan celdas en su posición; ya no se animan
como intercambios ficticios. Se contempla también la transición inversa al retroceder.
Bubble e Insertion registran explícitamente la comparación antes del intercambio, evitando
atribuir comparaciones a todos los intercambios de Quick Sort. La validación detecta textos
no numéricos, arreglos demasiado largos y selecciones vacías antes del guardado/importación.

Las complejidades, la memoria adicional de las trazas y las definiciones de las métricas
están detalladas en el README. No se añadieron dependencias.

## Verificación ejecutada

- Referencia inicial: 153 pruebas, cero fallos o errores.
- Verificación conjunta: 194 pruebas, cero fallos, errores u omisiones
  (`.\mvnw.cmd test "-Dtest=*Test,InterfazAlgoritmosIT"`).
- Verificación final desde compilación limpia: 190 pruebas unitarias, cero fallos o errores
  (`.\mvnw.cmd clean test`).
- Las cuatro pruebas reales de JavaFX comprueban carga FXML, nueve opciones,
  simulación y retroceso de los siete ordenamientos, generación para los nueve métodos,
  siete filas y nueve columnas del comparador y marcado del pivote.
- Cada motor nuevo ejecuta el contrato compartido con entradas ordenadas, inversas,
  repetidas, negativas, cero, un elemento, vacías/nulas y extremos de enteros.
  Cada implementación también compara 120 arreglos de semilla fija con `Arrays.sort`.
  Se verifican conservación de la entrada, numeración, continuidad e índices, y que cada
  escritura o intercambio afecte únicamente las posiciones declaradas.
- Se comprueban estabilidad de cada pasada de Radix, límite de rango de Counting,
  seguimiento del pivote, generación, evaluación, terminal, métricas y JSON portable.
- Se abrió la aplicación con `javafx:run`; se observaron las nueve opciones, la entrada
  generada, el pivote violeta y la tabla de comparación. El control de escritorio se
  detuvo por Escape durante la comprobación manual final; no se reanudó.
- `git diff --check` sin errores. Los commits se realizaron después de pruebas pertinentes
  y revisión del diff. El estado final se verifica también después del commit documental.

## PostgreSQL: preparado, verificación pendiente

No se creó ni modificó `db.local.properties` ni se publicaron credenciales.
La sesión no proporciona configuración de conexión y el usuario pidió dejarla intacta.
Por ello no se ejecutaron `PersistenciaAprendizajeIT` ni `NuevosAlgoritmosPostgresIT`;
no se declaran aprobadas. Ambas clases compilan con la suite.

La prueba nueva usa PostgreSQL real: ejecuta dos veces la migración y revierte esa transacción;
comprueba importación/deduplicación, recuperación y exportación de cinco ejercicios,
evaluación, diez intentos y acumulados de Procedimiento. Sus datos temporales usan UUID
y se limpian al terminar. No hay H2, SQLite ni bases simuladas.

Antes de presentar, aplicar `src/main/resources/ni/edu/uam/algomentor/db/actualizar-nuevos-algoritmos.sql`
en la base existente y ejecutar las pruebas PostgreSQL indicadas en el README.
El SQL conserva los registros existentes con `ON CONFLICT (nombre) DO NOTHING`.
Los límites previos de importación (500 ejercicios y 1 MB) se mantienen.

## Commits locales

1. `be6fc64` — feat: implementar Quick Sort y sus pruebas
2. `2ecfa33` — feat: implementar Merge Sort y sus pruebas
3. `ac7d39b` — feat: implementar Counting Sort y sus pruebas
4. `88d6663` — feat: implementar Radix Sort y sus pruebas
5. `fffad10` — feat: implementar Bucket Sort y sus pruebas
6. `0b300d4` — feat: integrar nuevos algoritmos en el simulador
7. `a7edb77` — feat: integrar algoritmos en practica y comparacion
8. `8d5d221` — feat: registrar nuevos algoritmos en PostgreSQL
9. `2012290` — test: verificar integracion de nuevos algoritmos
10. Commit documental que contiene este informe: `docs: documentar los nuevos algoritmos`.

No se hizo push, merge ni se abrió un Pull Request. La carpeta `.codex/` ya existía
como cambio sin seguimiento antes del trabajo; se conservó y quedó fuera de los commits.

## Archivos creados y modificados

Rutas relativas al repositorio. `A` indica creado y `M` modificado.

```text
M	README.md
A	docs/AMPLIACION_ALGORITMOS.md
M	src/main/java/ni/edu/uam/algomentor/controller/ComparadorController.java
M	src/main/java/ni/edu/uam/algomentor/controller/GestionContenidoController.java
M	src/main/java/ni/edu/uam/algomentor/controller/PracticaController.java
M	src/main/java/ni/edu/uam/algomentor/controller/SimuladorController.java
M	src/main/java/ni/edu/uam/algomentor/engine/BubbleSort.java
A	src/main/java/ni/edu/uam/algomentor/engine/BucketSort.java
A	src/main/java/ni/edu/uam/algomentor/engine/CatalogoAlgoritmos.java
A	src/main/java/ni/edu/uam/algomentor/engine/CountingSort.java
M	src/main/java/ni/edu/uam/algomentor/engine/GeneradorEjercicios.java
M	src/main/java/ni/edu/uam/algomentor/engine/InsertionSort.java
A	src/main/java/ni/edu/uam/algomentor/engine/MergeSort.java
M	src/main/java/ni/edu/uam/algomentor/engine/Paso.java
A	src/main/java/ni/edu/uam/algomentor/engine/QuickSort.java
A	src/main/java/ni/edu/uam/algomentor/engine/RadixSort.java
M	src/main/java/ni/edu/uam/algomentor/engine/TipoOperacion.java
A	src/main/java/ni/edu/uam/algomentor/engine/TrazaOrdenamiento.java
M	src/main/java/ni/edu/uam/algomentor/service/ComandosTerminal.java
M	src/main/java/ni/edu/uam/algomentor/service/ComparadorOrdenamientos.java
M	src/main/java/ni/edu/uam/algomentor/service/EvaluadorPractica.java
M	src/main/java/ni/edu/uam/algomentor/util/EntradaAprendizaje.java
M	src/main/java/ni/edu/uam/algomentor/util/MovimientoCeldas.java
M	src/main/java/ni/edu/uam/algomentor/util/TextoInterfaz.java
M	src/main/java/ni/edu/uam/algomentor/util/ValidacionesFormulario.java
M	src/main/resources/ni/edu/uam/algomentor/css/algomentor.css
A	src/main/resources/ni/edu/uam/algomentor/db/actualizar-nuevos-algoritmos.sql
M	src/main/resources/ni/edu/uam/algomentor/db/schema.sql
M	src/main/resources/ni/edu/uam/algomentor/view/comparador.fxml
M	src/main/resources/ni/edu/uam/algomentor/view/gestion-contenido.fxml
M	src/main/resources/ni/edu/uam/algomentor/view/simulador.fxml
A	src/test/java/ni/edu/uam/algomentor/api/PaqueteNuevosAlgoritmosTest.java
A	src/test/java/ni/edu/uam/algomentor/controller/InterfazAlgoritmosIT.java
A	src/test/java/ni/edu/uam/algomentor/engine/BucketSortTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/CatalogoAlgoritmosTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/CatalogoSqlTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/ContratoOrdenamiento.java
A	src/test/java/ni/edu/uam/algomentor/engine/CountingSortTest.java
M	src/test/java/ni/edu/uam/algomentor/engine/GeneradorEjerciciosTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/MergeSortTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/QuickSortTest.java
A	src/test/java/ni/edu/uam/algomentor/engine/RadixSortTest.java
M	src/test/java/ni/edu/uam/algomentor/service/ComandosTerminalTest.java
M	src/test/java/ni/edu/uam/algomentor/service/ComparadorOrdenamientosTest.java
M	src/test/java/ni/edu/uam/algomentor/service/EvaluadorPracticaTest.java
A	src/test/java/ni/edu/uam/algomentor/service/NuevosAlgoritmosPostgresIT.java
M	src/test/java/ni/edu/uam/algomentor/util/MovimientoCeldasTest.java
A	src/test/java/ni/edu/uam/algomentor/util/ValidacionesFormularioTest.java
```

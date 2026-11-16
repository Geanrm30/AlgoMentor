# AlgoMentor

Proyecto final de Programación de Aplicaciones de Escritorio (UAM).

## Instalación de la base de datos

### 1. Instalar PostgreSQL

Instalar PostgreSQL 14 o superior (incluye pgAdmin). Durante la instalación anotar la contraseña del usuario `postgres`
y dejar el puerto por defecto, `5432`.

### 2. Crear la base de datos

En pgAdmin: clic derecho en *Databases* > *Create* > *Database...* y usar el nombre `algomentor`.

O desde SQL:

```sql
CREATE DATABASE algomentor;
```

### 3. Configurar la conexión

1. En la raíz del proyecto, copiar `db.local.properties.example` como `db.local.properties`.
2. Editar `db.local.properties` con los datos propios:

   ```properties
   db.url=jdbc:postgresql://localhost:5432/algomentor
   db.user=postgres
   db.password=LA_CONTRASEÑA_DE_POSTGRES
   ```

`db.local.properties` está en `.gitignore`: no se sube a Git. Nunca publicar contraseñas ni credenciales.

### 4. Abrir y ejecutar la aplicación

Abrir la carpeta del proyecto en IntelliJ IDEA como proyecto Maven y utilizar JDK 21.
Desde una terminal en la raíz del proyecto:

```powershell
.\mvnw.cmd javafx:run
```

La vista de gestión de contenido permite crear, consultar, actualizar y eliminar algoritmos
y ejercicios. La pestaña Consultas muestra ejercicios por algoritmo e historial de intentos
por perfil mediante HQL. Para consultar un historial debe existir al menos un perfil y un
intento en la base. Si falla la conexión, la aplicación muestra el problema en la barra
inferior y registra el detalle técnico en consola.

### 5. Preparar el esquema del laboratorio

Ejecutar en la base `algomentor` el archivo
`src/main/resources/ni/edu/uam/algomentor/db/schema.sql`.
El script define las ocho tablas y sus relaciones, y agrega algoritmos de ejemplo.

### 6. Verificar la persistencia

En la pestaña Algoritmos, guardar un algoritmo y comprobar que aparece en la tabla.
Después actualizarlo y eliminarlo. En Ejercicios, seleccionar un algoritmo, indicar
origen y dificultad, ingresar datos, y repetir las operaciones. Por último, seleccionar
un algoritmo en Consultas y pulsar **Buscar ejercicios**. No se puede eliminar un
algoritmo que tenga ejercicios relacionados; primero deben eliminarse esos ejercicios.

## Aprendizaje

- **Simulador:** elige Bubble Sort, Insertion Sort, Gauss o Gauss-Jordan. Escribe enteros separados por comas, o una matriz ampliada con una ecuación por línea y celdas separadas por comas. Usa **Anterior** y **Siguiente** para recorrer los pasos.
- **Audio:** en un paso puedes cargar un MP3 o WAV local y reproducirlo. El texto del paso permanece visible. Los audios no se guardan en la base de datos ni se incluyen en el repositorio.
- **Práctica:** crea un perfil y selecciona un ejercicio guardado. Para ordenamiento responde con el arreglo final. Para Gauss y Gauss-Jordan selecciona una operación de filas, indica las filas con números desde 1 y el factor cuando aplique, y escribe la matriz resultante completa. Verifica y luego guarda el intento. Los errores muestran fila y columna y se asocian a Signos, Fracciones, Aritmetica o Procedimiento; los pasos correctos cuentan en Procedimiento.
- **Terminal:** escribe `ayuda` para consultar los comandos educativos. No ejecuta comandos del sistema operativo.
- **Comparar:** usa la misma entrada para medir comparaciones, intercambios y pasos de ambos métodos de ordenamiento.
- **Progreso:** selecciona un perfil para consultar sus intentos y aciertos por habilidad. Pulsa **Exportar PDF** para guardar un informe con el resumen de intentos y los resultados por habilidad. El informe se genera usando JasperReports y los datos consultados mediante Hibernate.
- **Intercambio API:** exporta ejercicios a JSON e importa lotes compartidos por otros equipos mediante una API HTTP local. Los ejercicios se relacionan por nombre de algoritmo, sin compartir identificadores de la base de datos.

### Interfaz y animaciones

- **Tema oscuro:** la hoja de estilos `algomentor.css` define la paleta (cian y violeta), las tarjetas, los botones, las tablas y los diálogos. Los cuadros de alerta usan el mismo tema.
- **Animaciones:** `Animaciones` reúne las transiciones de JavaFX. Las pestañas y las tarjetas aparecen de forma escalonada, los botones reaccionan al pasar el cursor y al pulsarlos, y la retroalimentación de Práctica e Intercambio API aparece con un estado correcto o de error.
- **Simulador:** las celdas aparecen con rebote al simular. Cuando cambia el estado entre pasos, las celdas de un arreglo se deslizan de su posición anterior a la nueva (`MovimientoCeldas` calcula ese desplazamiento) y las celdas activas pulsan. En las matrices se resaltan las celdas que cambiaron.
- **Progreso:** cada habilidad muestra una barra animada. Verde desde 70 %, ámbar desde 40 % y roja por debajo.

### Informe PDF de progreso

1. Configura PostgreSQL, ejecuta `schema.sql` e inicia la aplicación.
2. Abre **Aprender > Progreso** y selecciona un perfil existente.
3. Pulsa **Exportar PDF**, elige una ubicación y confirma el nombre del archivo.
4. El informe incluye la fecha de generación, total de intentos, correctos, incorrectos y sin calificar, además de aciertos, errores y porcentaje por habilidad.

Si el perfil no tiene progreso por habilidad, se genera el PDF con el resumen de intentos y un mensaje indicando que no hay datos de habilidades. Si no existen perfiles o la base de datos no está disponible, no se puede generar el informe. Para probar JasperReports sin PostgreSQL:

```powershell
.\mvnw.cmd "-Dtest=ReporteProgresoTemplateTest,ReporteProgresoServiceTest" test
```

JasperReports compila la plantilla JRXML al generar el informe. La aplicación requiere Internet solo para descargar dependencias Maven la primera vez que se compila; la generación del PDF no utiliza la API de ejercicios.

### Intercambio de ejercicios por API

La pestaña **Intercambio API** inicia un servidor HTTP local en `127.0.0.1:8765` y lo consume
desde JavaFX. Requiere PostgreSQL configurado y el esquema cargado; no necesita Internet.
**Exportar JSON** guarda los ejercicios en un archivo portable. **Importar JSON** lee un archivo
de hasta 1 MB y carga como máximo 500 ejercicios por lote. El algoritmo de cada ejercicio debe
existir previamente con el mismo nombre en la base de datos receptora. La importación es una
sola transacción: si hay un dato inválido, no se guarda ningún ejercicio del lote. Los ejercicios
idénticos que ya existan se omiten.

Los endpoints son `GET http://127.0.0.1:8765/api/ejercicios/exportar` y
`POST http://127.0.0.1:8765/api/ejercicios/importar` con `Content-Type: application/json`.
El cuerpo usa este formato:

```json
{"version":1,"ejercicios":[{"algoritmo":"Bubble Sort","datos":"8,3,6,1","dificultad":2,"origen":"MANUAL"}]}
```

El servidor escucha solo en la computadora local. Para compartir entre equipos, se envía el
archivo JSON exportado y el otro equipo lo importa desde su propia instalación.

En Gestión de contenido, crea los ejercicios con el mismo nombre de algoritmo que aparece en el simulador. Los datos pueden ser simples (`8,3,6,1` o `2,1,5` seguido de otra fila) o usar el formato versionado `v1;ARREGLO;8,3,6,1` y `v1;MATRIZ;2x3;2,1,5|1,-1,1`. Ejecuta `schema.sql` antes de guardar intentos, pues crea las cuatro habilidades iniciales.

## Trabajo en equipo

Cada integrante desarrolla en su rama y abre un Pull Request revisado por otra persona.
La rama `main` debe compilar y ejecutarse después de cada integración.


## Persistencia del aprendizaje

AlgoMentor utiliza Hibernate ORM y PostgreSQL para
almacenar el historial de aprendizaje de los estudiantes.

El sistema permite:

- Registrar perfiles de estudiantes.
- Guardar intentos y sus pasos.
- Asociar errores detectados con habilidades.
- Actualizar aciertos y errores por habilidad.
- Consultar estadísticas mediante HQL.

### Transacciones

El registro de intentos, pasos, errores y progreso
utiliza una única transacción de Hibernate.

Si alguna operación falla, se ejecuta rollback
para evitar registros parcialmente guardados.

### Ejecución de pruebas

Para ejecutar las pruebas unitarias:

```powershell
.\mvnw.cmd clean test
```

Para ejecutar la prueba de integración con PostgreSQL:

```powershell
.\mvnw.cmd "-Dtest=PersistenciaAprendizajeIT" test
```

La prueba de integración requiere PostgreSQL configurado
y el esquema de AlgoMentor creado.

La clase PersistenciaAprendizajeIT no se ejecuta
automáticamente mediante mvn test debido a su nombre.

### Correspondencia de habilidades

Los errores del motor se relacionan con las habilidades
mediante HabilidadErrorResolver:

- SIGNO: Signos
- FRACCION: Fracciones
- ARITMETICO: Aritmetica
- OPERACION_INVALIDA: Procedimiento

Los identificadores se consultan mediante Hibernate,
sin depender de IDs fijos.

### Concurrencia

IntentoService utiliza un bloqueo pesimista sobre
el perfil durante el registro del intento.

Las actualizaciones de progreso se realizan en un
orden consistente de identificadores de habilidades.

Estas medidas reducen los conflictos de concurrencia
cuando las operaciones utilizan IntentoService.

Las actualizaciones realizadas por otros servicios
deben respetar la misma estrategia de bloqueo.

La concurrencia real y el rollback deben comprobarse
mediante pruebas de integración con PostgreSQL.

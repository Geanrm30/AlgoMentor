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

### 4. Preparar el esquema del laboratorio

Ejecutar en la base `algomentor` el archivo
`src/main/resources/ni/edu/uam/algomentor/db/schema.sql`.
El script define las tablas y sus relaciones, y agrega algoritmos de ejemplo. También
crea la tabla `cuenta` que necesita el inicio de sesión.

### 5. Abrir y ejecutar la aplicación

Abrir la carpeta del proyecto en IntelliJ IDEA como proyecto Maven y utilizar JDK 21.
Desde una terminal en la raíz del proyecto:

```powershell
.\mvnw.cmd javafx:run
```

La aplicación inicia en la pantalla de acceso. Crea una cuenta con un usuario de 3 a 40
caracteres y una contraseña de 8 a 128 caracteres, o inicia sesión. Cada cuenta queda
vinculada a un perfil; al registrar el nombre de un perfil previo se conserva su progreso.
Las contraseñas se almacenan con PBKDF2 y no en texto plano. Usa **Cerrar sesión** en la
barra superior para salir. La gestión de contenido permite crear, consultar, actualizar y
eliminar algoritmos y ejercicios. La pestaña **Resumen** muestra los totales y listados de
algoritmos, ejercicios y perfiles, además del total de intentos.

### 6. Verificar la persistencia

Registra una cuenta e inicia sesión para guardar intentos en el perfil propio. En la pestaña
Algoritmos, guardar un algoritmo y comprobar que aparece en la tabla. Después actualizarlo
y eliminarlo. En Ejercicios, seleccionar un algoritmo, indicar origen y dificultad, ingresar
datos, y repetir las operaciones. Por último, seleccionar
un algoritmo en Consultas y pulsar **Buscar ejercicios**. No se puede eliminar un
algoritmo que tenga ejercicios relacionados; primero deben eliminarse esos ejercicios.

## Aprendizaje

- La aplicación inicia con el tema oscuro. Usa el botón **Tema claro** de la barra superior para alternar entre ambos temas.
- **Simulador:** elige Bubble Sort, Insertion Sort, Gauss o Gauss-Jordan. Escribe enteros separados por comas, o una matriz ampliada con una ecuación por línea y celdas separadas por comas. Usa **Anterior** y **Siguiente** para recorrer los pasos.
- **Audio:** en un paso puedes cargar un MP3 o WAV local y reproducirlo. El texto del paso permanece visible. Los audios no se guardan en la base de datos ni se incluyen en el repositorio.
- **Práctica:** usa el perfil creado para tu cuenta y selecciona un ejercicio guardado. Para ordenamiento responde con el arreglo final. Para Gauss y Gauss-Jordan selecciona una operación de filas, indica las filas con números desde 1 y el factor cuando aplique, y escribe la matriz resultante completa. Verifica y luego guarda el intento. Los errores muestran fila y columna y se asocian a Signos, Fracciones, Aritmetica o Procedimiento; los pasos correctos cuentan en Procedimiento.
- **Terminal:** escribe `ayuda` para consultar los comandos educativos. No ejecuta comandos del sistema operativo.
- **Comparar:** usa la misma entrada para medir comparaciones, intercambios y pasos de ambos métodos de ordenamiento.
- **Progreso:** consulta los intentos y aciertos por habilidad de tu cuenta. Pulsa **Vista previa** para abrir el informe en el visor de JasperReports, o **Exportar** para guardar el informe PDF. El informe incluye el resumen de intentos y los resultados por habilidad, consultados mediante Hibernate.
- **Repaso API:** carga preguntas de opción múltiple de computación desde Open Trivia DB, permite seleccionar una respuesta y muestra el resultado. Esta función no guarda respuestas en PostgreSQL.

### Informe PDF de progreso

1. Configura PostgreSQL, ejecuta `schema.sql` e inicia la aplicación.
2. Inicia sesión y abre **Aprender > Progreso**.
3. Pulsa **Vista previa** para revisar el informe, o **Exportar** para guardarlo como PDF en la ubicación elegida.
4. El informe incluye la fecha de generación, total de intentos, correctos, incorrectos y sin calificar, además de aciertos, errores y porcentaje por habilidad.

Si tu cuenta no tiene progreso por habilidad, se genera el PDF con el resumen de intentos y un mensaje indicando que no hay datos de habilidades. Si la base de datos no está disponible, no se puede generar el informe. Para probar JasperReports sin PostgreSQL:

```powershell
.\mvnw.cmd "-Dtest=ReporteProgresoTemplateTest,ReporteProgresoServiceTest" test
```

JasperReports compila la plantilla JRXML al generar el informe. La aplicación requiere Internet solo para descargar dependencias Maven la primera vez que se compila; la generación del PDF no consulta la API externa.

### Repaso conceptual con Open Trivia DB

La pestaña **Repaso API** requiere conexión a Internet. La aplicación consulta Open Trivia DB
sin necesitar una clave de API y limita las solicitudes a una por cada cinco segundos para
respetar el límite del servicio. Si la API no está disponible, devuelve un error o no tiene
preguntas disponibles, la aplicación informa el problema y permite volver a intentarlo.

Las preguntas provienen de un servicio externo y son un recurso complementario de repaso;
no sustituyen las explicaciones ni la verificación de ejercicios del simulador. Para ejecutar
las pruebas del cliente y del mapeo JSON:

```powershell
.\mvnw.cmd "-Dtest=OpenTriviaClientTest,OpenTriviaResponseTest" test
```

En Gestión de contenido, crea los ejercicios con el mismo nombre de algoritmo que aparece en el simulador. Los datos pueden ser simples (`8,3,6,1` o `2,1,5` seguido de otra fila) o usar el formato versionado `v1;ARREGLO;8,3,6,1` y `v1;MATRIZ;2x3;2,1,5|1,-1,1`. Ejecuta `schema.sql` antes de guardar intentos, pues crea las cuatro habilidades iniciales.

## Trabajo en equipo

Cada integrante desarrolla en su rama y abre un Pull Request revisado por otra persona.
La rama `main` debe compilar y ejecutarse después de cada integración.


## Persistencia del aprendizaje

AlgoMentor utiliza Hibernate ORM y PostgreSQL para
almacenar el historial de aprendizaje de los estudiantes.

El sistema permite:

- Registrar cuentas vinculadas a perfiles de estudiantes.
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

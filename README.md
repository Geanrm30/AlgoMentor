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

## Trabajo en equipo

Cada integrante desarrolla en su rama y abre un Pull Request revisado por otra persona.
La rama `main` debe compilar y ejecutarse después de cada integración.

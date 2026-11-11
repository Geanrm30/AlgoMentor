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

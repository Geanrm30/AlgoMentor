# Relevo para Jeremy

Esta rama contiene la experiencia JavaFX, práctica, terminal, comparación y progreso. Los algoritmos están en `engine/`; las entidades, DAO y servicios de persistencia son de Gean y Anthony. La interfaz no contiene consumo de API ni JasperReports: esas integraciones son el trabajo siguiente.

## Puntos de integración

1. **API de ejercicios:** `Ejercicio` usa `origen` (`ALEATORIO`, `MANUAL` o `PERSONALIZADO`) y `datos`. `PracticaController` lista los ejercicios de `EjercicioDAO.listarConAlgoritmo()` y `EvaluadorPractica` acepta datos simples y versionados. Si se agregan ejercicios remotos, convertirlos al mismo modelo y formato antes de persistirlos; conservar el manejo de fallos de red fuera del hilo JavaFX.
2. **Reporte JasperReports:** `EstadisticasDAO.obtenerResumenIntentos(perfilId)` y `listarProgresoPorHabilidad(perfilId)` entregan los datos que muestra `ProgresoController`. Se puede añadir allí una acción de exportación para el perfil elegido, con una plantilla propia y manejo explícito de errores.
3. **Ejecución:** `MainApp` carga `experiencia.fxml` y `gestion-contenido.fxml`. Las nuevas vistas del alumno se incluyen como pestañas de `experiencia.fxml`. Las consultas de PostgreSQL van en hilos daemon y la interfaz se actualiza con `Platform.runLater`.

## Verificación previa a integrar

Ejecutar `./mvnw.cmd test` y `./mvnw.cmd javafx:run` con JDK 21. Para comprobar práctica y progreso hace falta `db.local.properties`, la base `algomentor` y `schema.sql`. Esta copia no incluye credenciales.

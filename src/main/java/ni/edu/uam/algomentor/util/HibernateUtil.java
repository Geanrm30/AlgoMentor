package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HibernateUtil {

    private static final Logger LOG = Logger.getLogger(HibernateUtil.class.getName());
    private static final Path LOCAL_CONFIG = Path.of("db.local.properties");
    private static final String MIGRACION_CUENTAS = "/ni/edu/uam/algomentor/db/actualizar-cuentas.sql";
    private static SessionFactory sessionFactory;

    static {
        ConfiguracionLogs.aplicar();
    }

    private HibernateUtil() { }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = build();
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    private static SessionFactory build() {
        Properties local = loadLocalConfig();

        String url = value(local, "db.url", "DB_URL").strip();
        String usuario = value(local, "db.user", "DB_USER").strip();
        String contrasena = value(local, "db.password", "DB_PASSWORD");
        asegurarTablaCuenta(url, usuario, contrasena);

        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", url);
        cfg.setProperty("hibernate.connection.username", usuario);
        cfg.setProperty("hibernate.connection.password", contrasena);
        cfg.setProperty("hibernate.connection.pool_size", "5");
        cfg.setProperty("hibernate.hbm2ddl.auto", "validate");
        cfg.setProperty("hibernate.show_sql", "false");

        cfg.addAnnotatedClass(Perfil.class);
        cfg.addAnnotatedClass(Cuenta.class);
        cfg.addAnnotatedClass(Algoritmo.class);
        cfg.addAnnotatedClass(Habilidad.class);
        cfg.addAnnotatedClass(Ejercicio.class);
        cfg.addAnnotatedClass(Intento.class);
        cfg.addAnnotatedClass(PasoIntento.class);
        cfg.addAnnotatedClass(ErrorDetectado.class);
        cfg.addAnnotatedClass(ProgresoHabilidad.class);
        return cfg.buildSessionFactory();
    }

    /**
     * Las bases creadas antes del inicio de sesión no tienen la tabla {@code cuenta}, y con
     * {@code hbm2ddl.auto=validate} Hibernate no arrancaría. El script es idempotente: no
     * modifica ni borra datos. Si falla, se registra y la validación de Hibernate informa el problema.
     */
    private static void asegurarTablaCuenta(String url, String usuario, String contrasena) {
        try (InputStream recurso = HibernateUtil.class.getResourceAsStream(MIGRACION_CUENTAS)) {
            if (recurso == null) {
                LOG.warning("No se encontró el script de migración de cuentas.");
                return;
            }
            String script = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
                 Statement sentencia = conexion.createStatement()) {
                sentencia.execute(script);
            }
        } catch (IOException | SQLException e) {
            LOG.log(Level.WARNING, "No se pudo verificar la tabla de cuentas", e);
        }
    }

    private static Properties loadLocalConfig() {
        Properties props = new Properties();
        if (Files.isRegularFile(LOCAL_CONFIG)) {
            try (BufferedReader reader = Files.newBufferedReader(LOCAL_CONFIG)) {
                props.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer db.local.properties.", e);
            }
        }
        return props;
    }

    private static String value(Properties local, String key, String envKey) {
        String value = local.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta '" + key + "' en db.local.properties o la variable " + envKey + ".");
        }
        return value;
    }
}
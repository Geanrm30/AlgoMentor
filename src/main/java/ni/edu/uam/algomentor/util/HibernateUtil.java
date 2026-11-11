package ni.edu.uam.algomentor.util;

import ni.edu.uam.algomentor.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HibernateUtil {

    private static final Path LOCAL_CONFIG = Path.of("db.local.properties");
    private static SessionFactory sessionFactory;

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

        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", value(local, "db.url", "DB_URL").strip());
        cfg.setProperty("hibernate.connection.username", value(local, "db.user", "DB_USER").strip());
        cfg.setProperty("hibernate.connection.password", value(local, "db.password", "DB_PASSWORD"));
        cfg.setProperty("hibernate.connection.pool_size", "5");
        cfg.setProperty("hibernate.hbm2ddl.auto", "validate");
        cfg.setProperty("hibernate.show_sql", "false");

        cfg.addAnnotatedClass(Perfil.class);
        cfg.addAnnotatedClass(Algoritmo.class);
        cfg.addAnnotatedClass(Habilidad.class);
        cfg.addAnnotatedClass(Ejercicio.class);
        cfg.addAnnotatedClass(Intento.class);
        cfg.addAnnotatedClass(PasoIntento.class);
        cfg.addAnnotatedClass(ErrorDetectado.class);
        cfg.addAnnotatedClass(ProgresoHabilidad.class);
        return cfg.buildSessionFactory();
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
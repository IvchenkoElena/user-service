package aston.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration().configure();
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // метод для создания тестовой SessionFactory с параметрами от Testcontainers
    public static SessionFactory createTestSessionFactory(
            String jdbcUrl, String username, String password) {
        Configuration configuration = new Configuration().configure();


        // Основные настройки подключения
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.setProperty("hibernate.connection.url", jdbcUrl);
        props.setProperty("hibernate.connection.username", username);
        props.setProperty("hibernate.connection.password", password);

        // Настройки для тестов
        props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.use_sql_comments", "true");

        // Оптимизации для тестов
        props.setProperty("hibernate.generate_statistics", "false");
        props.setProperty("hibernate.order_inserts", "true");
        props.setProperty("hibernate.order_updates", "true");

        configuration.addProperties(props);

        try {
            SessionFactory testSessionFactory = configuration.buildSessionFactory();
            logger.info("Test SessionFactory created successfully for URL: {}", jdbcUrl);
            return testSessionFactory;
        } catch (Exception e) {
            logger.error("Failed to create test SessionFactory", e);
            throw e;
        }
    }

    // Корректное закрытие SessionFactory

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("SessionFactory closed successfully");
        }
    }
}

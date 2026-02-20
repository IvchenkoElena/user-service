package aston;

import aston.ui.ConsoleUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceApp {
    private final static Logger logger = LoggerFactory.getLogger(UserServiceApp.class);

    public static void main(String[] args) {
        logger.info("Запуск приложения");

        try (ConsoleUI consoleUI = new ConsoleUI()) {
            logger.info("Приложение стартовало");
            consoleUI.start();
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в главном потоке приложения: {}", e.getMessage(), e);
            System.err.println("Произошла критическая ошибка. Приложение завершает работу.");
            System.exit(1);
        }

        logger.info("Приложение user-service завершено.");
    }
}

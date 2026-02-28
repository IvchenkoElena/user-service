package aston;

import aston.ui.ConsoleUI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@AllArgsConstructor
public class UserServiceApp implements CommandLineRunner {
    private final ConsoleUI consoleUI;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApp.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Запуск приложения");
        try {
            log.info("Приложение стартовало");
            consoleUI.start();
        } catch (Exception e) {
            log.error("Неожиданная ошибка в главном потоке приложения: {}", e.getMessage(), e);
            System.err.println("Произошла критическая ошибка. Приложение завершает работу.");
            throw e;
        } finally {
            log.info("Приложение user-service завершено.");
        }
    }
}

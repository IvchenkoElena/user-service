package aston;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class UserServiceApp {
    public static void main(String[] args) {
        log.info("Запуск user-service...");
        SpringApplication.run(UserServiceApp.class, args);
        log.info("user-service запущен и готов к работе!");
    }
}

package aston.ui;

import aston.dao.UserDao;
import aston.dao.UserDaoImpl;
import aston.model.User;
import aston.service.UserService;
import aston.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService;
    private final UserDao userDao = new UserDaoImpl();

    private boolean running = true;

    public ConsoleUI(UserService userService) {
        this.userService = userService;
    }

    public void start() throws Exception {
        logger.info("Запуск консольного интерфейса user-service");
        System.out.println("Добро пожаловать в приложение User Service! Выберите необходимый пункт меню:");

        while (running) {
            showMenu();
            int choice = readChoice();
            executeChoice(choice);
        }
        close();
    }

    private int readChoice() {
        while (true) {
            try {
                System.out.println("Ваш выбор:");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > 7) {
                    System.out.println("Введите число от 1 до 7");
                    logger.error("Введено некорректное число");
                } else {
                    return choice;
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Введите целое число");
                logger.error("Введен неверный символ");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n---Menu---");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Найти пользователя по email");
        System.out.println("4. Показать всех пользователей");
        System.out.println("5. Обновить данные пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("7. Выход");
    }

    private void executeChoice(int choice) {
        try {
            switch (choice) {
                case 1 -> createNewUser();
                case 2 -> findUserById();
                case 3 -> findUserByEmail();
                case 4 -> findAllUsers();
                case 5 -> updateUser();
                case 6 -> deleteUser();
                case 7 -> exitApp();
                default -> System.out.println("Неизвестный пункт меню");
            }
        } catch (RuntimeException e) {
            // Обрабатываем исключения, выброшенные из DAO
            handleUserFriendlyError(e);
        }
    }

    private void createNewUser() {
        System.out.println("--Создание пользователя--");
        logger.info("Начинаем создание нового пользователя");
        System.out.println("Введите имя:");
        String name = scanner.nextLine().trim();
        System.out.println("Введите email:");
        String email = scanner.nextLine().trim();
        System.out.println("Введите возраст:");
        int age = Integer.parseInt(scanner.nextLine());
        try {
            User user = userService.createUser(name, email, age);
            System.out.println("Новый пользователь с ID = " + user.getId() + " успешно создан");
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
            logger.error("Ошибка создания пользователя: {}", e.getMessage());
        }
    }

    private void findUserById() {
        System.out.println("--Поиск пользователя по ID--");
        logger.info("Начинаем поиск пользователя по ID");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            Optional<User> mayBeUser = userService.findUserById(id);
            if (mayBeUser.isPresent()) {
                User user = mayBeUser.get();
                System.out.println("Пользователь найден: " + user);
            } else {
                System.out.println("Пользователь с ID " + id + " не найден");
            }
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
        }
    }

    private void findUserByEmail() {
        System.out.println("--Поиск пользователя по Email--");
        logger.info("Начинаем поиск пользователя по Email");
        System.out.println("Введите Email:");
        String email = scanner.nextLine().trim();
        try {
            Optional<User> mayBeUser = userService.findUserByEmail(email);
            if (mayBeUser.isPresent()) {
                User user = mayBeUser.get();
                System.out.println("Пользователь найден: " + user);
            } else {
                System.out.println("Пользователь с Email " + email + " не найден");
            }
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
        }

    }

    private void findAllUsers() {
        System.out.println("--Вывод всех пользователей--");
        logger.info("Начинаем вывод всех пользователей");

        try {
            List<User> users = userService.findAllUsers();
            System.out.println("Список пользователей:");
            if (users.isEmpty()) {
                System.out.println("пуст");
                logger.info("Пустой список пользователей");
            } else {
                System.out.println(users);
                logger.info("Список пользователей выведен");
            }
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
        }
    }

    private void updateUser() {
        System.out.println("--Обновление пользователя--");
        logger.info("Начинаем обновление пользователя");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());

        try {
            System.out.println("Введите имя. Оставьте пустым, чтобы оставить без изменений");
            String name = scanner.nextLine().trim();

            System.out.println("Введите email. Оставьте пустым, чтобы оставить без изменений");
            String email = scanner.nextLine().trim();

            System.out.println("Введите возраст. Оставьте пустым, чтобы оставить без изменений");
            String ageInput = scanner.nextLine().trim();

            userService.updateUser(id, name, email, ageInput);
            System.out.println("Пользователь с ID = " + id + " успешно обновлен");
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
        }
    }

    private void deleteUser() {
        System.out.println("--Удаление пользователя по ID--");
        logger.info("Начинаем удаление пользователя по ID");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            userService.deleteUser(id);
            System.out.println("Удаление завершено");
        } catch (RuntimeException e) {
            handleUserFriendlyError(e);
        }
    }

    private void exitApp() {
        running = false;
        logger.info("Выбран пункт меню выход");
        System.out.println("До свидания!");
    }

    @Override
    public void close() throws Exception {
        HibernateUtil.shutdown();
        scanner.close();
        logger.info("Консольный интерфейс ConsoleUI закрыт.");
    }

    private static void handleUserFriendlyError(RuntimeException e) {
        Throwable cause = e.getCause();

        String message;

        if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
            message = "Ошибка: не удалось сохранить пользователя — нарушено ограничение БД. " +
                    "Возможные причины: email уже существует или пропущено обязательное поле.";
        }
        else if (cause instanceof org.hibernate.exception.JDBCConnectionException) {
            message = "Ошибка: потеряно соединение с базой данных. " +
                    "Проверьте, запущен ли PostgreSQL и верны ли учётные данные.";
        }
        else if (cause instanceof org.hibernate.exception.SQLGrammarException) {
            message = "Ошибка: синтаксическая ошибка SQL. " +
                    "Возможные причины: таблица не найдена или ошибка в маппинге.";
        }
        else if (cause instanceof org.hibernate.StaleObjectStateException) {
            message = "Ошибка: конфликт оптимистической блокировки. " +
                    "Запись была изменена другой транзакцией. Обновите данные и повторите попытку.";
        }
        else if (e.getMessage() != null && e.getMessage().contains("нарушение ограничения БД")) {
            message = "Ошибка: нарушено ограничение базы данных (например, уникальное поле).";
        }
        else if (e.getMessage() != null && e.getMessage().contains("Ошибка соединения с БД")) {
            message = "Ошибка: не удаётся подключиться к базе данных. Проверьте сеть и статус сервера.";
        }
        else if (e.getMessage() != null && e.getMessage().contains("Синтаксическая ошибка SQL")) {
            message = "Ошибка SQL: проверьте корректность запросов и структуру БД.";
        }
        else if (e.getMessage() != null && e.getMessage().contains("Ошибка транзакции")) {
            message = "Ошибка: транзакция не выполнена. Попробуйте ещё раз.";
        }
        else {
            // Общий случай — неизвестная ошибка
            message = "Произошла непредвиденная ошибка: " + e.getMessage() +
                    ". Подробности записаны в лог.";
            logger.error("Unhandled exception in user interface", e);
        }

        System.out.println("\n!!!" + message + "!!!\n");
    }
}

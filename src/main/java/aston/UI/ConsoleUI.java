package aston.UI;

import aston.DAO.UserDao;
import aston.DAO.UserDaoImpl;
import aston.model.User;
import aston.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDaoImpl();

    private boolean running = true;


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
        User user = new User(name, email, age);
        try {
            userDao.create(user);
            System.out.println("Новый пользователь с ID = " + user.getId() + " успешно создан");
            logger.info("Новый пользователь с ID = {} успешно создан", user.getId());
        } catch (Exception e) {
            System.out.println("Ошибка создания пользователя");
            logger.error("Ошибка создания пользователя: {}", e.getMessage());
        }
    }

    private void findUserById() {
        System.out.println("--Поиск пользователя по ID--");
        logger.info("Начинаем поиск пользователя по ID");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isPresent()) {
            User user = mayBeUser.get();
            System.out.println("Пользователь найден: " + user);
            logger.info("Пользователь с ID {} найден", id);
        } else {
            System.out.println("Пользователь с ID " + id + " не найден");
            logger.info("Пользователь с ID {} не найден", id);
        }
    }

    private void findUserByEmail() {
        System.out.println("--Поиск пользователя по Email--");
        logger.info("Начинаем поиск пользователя по Email");
        System.out.println("Введите Email:");
        String email = scanner.nextLine().trim();
        Optional<User> mayBeUser = userDao.findByEmail(email);
        if (mayBeUser.isPresent()) {
            User user = mayBeUser.get();
            System.out.println("Пользователь найден: " + user);
            logger.info("Пользователь с Email {} найден", email);
        } else {
            System.out.println("Пользователь с Email " + email + " не найден");
            logger.info("Пользователь с Email {} не найден", email);
        }
    }

    private void findAllUsers() {
        System.out.println("--Вывод всех пользователей--");
        logger.info("Начинаем вывод всех пользователей");

        List<User> users = userDao.findAll();
        System.out.println("Список пользователей:");
        if (users.isEmpty()) {
            System.out.println("пуст");
            logger.info("Пустой список пользователей");
        } else {
            System.out.println(users);
            logger.info("Список пользователей выведен");
        }
    }

    private void updateUser() {
        System.out.println("--Обновление пользователя--");
        logger.info("Начинаем обновление пользователя");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());

        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден");
            logger.info("Пользователь с ID {} не найден", id);
            return;
        }

        User user = mayBeUser.get();
        System.out.println("Текущий пользователь: " + user);

        System.out.println("Введите имя. Оставьте пустым, чтобы оставить без изменений");
        String name = scanner.nextLine().trim();
        if(!name.isEmpty()) {
            user.setName(name);
        }

        System.out.println("Введите email. Оставьте пустым, чтобы оставить без изменений");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        System.out.println("Введите возраст:");
        int age = Integer.parseInt(scanner.nextLine());

        if (age != 0) {
            user.setAge(age);
        }

        try {
            userDao.update(user);
            System.out.println("Пользователь с ID = " + id + " успешно обновлен");
            logger.info("Пользователь с ID = {} успешно обновлен", id);
        } catch (Exception e) {
            System.out.println("Ошибка обновления пользователя");
            logger.error("Ошибка обновления пользователя: {}", e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.println("--Удаление пользователя по ID--");
        logger.info("Начинаем удаление пользователя по ID");
        System.out.println("Введите ID:");
        Long id = Long.parseLong(scanner.nextLine());
        userDao.delete(id);
        logger.info("Удаление завершено");
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
}

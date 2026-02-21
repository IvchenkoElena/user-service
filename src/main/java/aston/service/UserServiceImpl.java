package aston.service;

import aston.dao.UserDao;
import aston.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void close() throws Exception {
        logger.info("UserService закрыт");
    }

    @Override
    public User createUser(String name, String email, int age) {
        // Валидация аргументов
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Имя должно быть от 2 до 100 символов");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Некорректный email");
        }
        if (age < 3 || age > 120) {
            throw new IllegalArgumentException("Возраст должен быть от 3 до 120 лет");
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        logger.info("Создание пользователя: name={}, email={}", name, email);
        User user = new User(name, email, age);
        userDao.create(user);
        logger.info("Новый пользователь с ID = {} успешно создан", user.getId());
        return user;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    @Override
    public Optional<User> findUserById(Long id) {
        logger.info("Поиск пользователя по Id={}", id);
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isPresent()) {
            logger.info("Пользователь с ID {} найден", id);
        } else {
            logger.info("Пользователь с ID {} не найден", id);
        }
        return mayBeUser;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        logger.info("Поиск пользователя по Email={}", email);
        Optional<User> mayBeUser = userDao.findByEmail(email);
        if (mayBeUser.isPresent()) {
            logger.info("Пользователь с Email {} найден", email);
        } else {
            logger.info("Пользователь с Email {} не найден", email);
        }
        return mayBeUser;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> users = userDao.findAll();
        logger.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public void updateUser(Long id, String name, String email, String ageInput) {
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден");
            logger.info("Пользователь с ID {} не найден", id);
            return;
        }

        User user = mayBeUser.get();
        System.out.println("Старые данные пользователя: " + user);

        if(!name.isEmpty()) {
            user.setName(name);
        }

        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        if (!ageInput.isEmpty()) {
            user.setAge(Integer.parseInt(ageInput));
        }

        userDao.update(user);
        System.out.println("Новые данные пользователя: " + user);
        logger.info("Пользователь с ID = {} успешно обновлен", id);
    }

    @Override
    public void deleteUser(Long id) {
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден");
            logger.info("Пользователь с ID {} не найден", id);
            return;
        }
        userDao.delete(id);
        logger.info("Пользователь с ID {} удалён", id);
    }
}

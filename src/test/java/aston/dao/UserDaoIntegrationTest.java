package aston.dao;

import aston.model.User;
import aston.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
class UserDaoIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(UserDaoIntegrationTest.class);

    @Container

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserDao userDao;
    private SessionFactory testSessionFactory;

    @BeforeEach
    void setUp() {
        logger.info("Вошли в метод SetUp");
        if (!postgres.isRunning()) {
            postgres.start();
        }

        logger.info("PostgreSQL container is running. JDBC URL: {}", postgres.getJdbcUrl());

        // Создаём тестовую SessionFactory с подключением к контейнеру
        testSessionFactory = HibernateUtil.createTestSessionFactory(
                postgres.getJdbcUrl(), postgres.getUsername(),postgres.getPassword());

        // Инициализация DAO с тестовой SessionFactory
        userDao = new UserDaoImpl(testSessionFactory);
    }

    @AfterEach
    void tearDown() {
        if (testSessionFactory != null) {
        testSessionFactory.close();
        }
    }

    @Test
    @DisplayName("Создание пользователя и проверка сохранения в БД")
    void shouldCreateUserAndPersistInDatabase() {
        // Arrange
        User user = new User("John Doe", "john@example.com", 30);

        // Act
        userDao.create(user);

        // Assert
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);

        Optional<User> found = userDao.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john@example.com", found.get().getEmail());
        assertEquals(30, found.get().getAge());
        assertEquals(user, found.get());
    }

    @ParameterizedTest
    @MethodSource("provideUsersForCreation")
    @DisplayName("Параметризованный тест создания пользователей с разными данными")
    void shouldCreateUsersWithDifferentData(String name, String email, int age) {
        // Arrange
        User user = new User(name, email, age);

        // Act
        userDao.create(user);

        // Assert
        assertNotNull(user.getId());

        Optional<User> found = userDao.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals(name, found.get().getName());
        assertEquals(email, found.get().getEmail());
        assertEquals(age, found.get().getAge());
    }

    static List<Arguments> provideUsersForCreation() {
        return List.of(
                Arguments.of("Alice", "alice@example.com", 25),
                Arguments.of("Bob", "bob@example.com", 35),
                Arguments.of("Charlie", "charlie@example.com", 40)
        );
    }

    @Test
    @DisplayName("Поиск пользователя по ID — пользователь существует")
    void shouldFindUserByIdWhenExists() {
        // Arrange
        User user = new User("Existing User", "existing@example.com", 28);
        userDao.create(user);

        // Act
        Optional<User> result = userDao.findById(user.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user.getName(), result.get().getName());
        assertEquals(user, result.get());
    }

    @Test
    @DisplayName("Поиск пользователя по ID — пользователь не существует")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Act
        Optional<User> result = userDao.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Параметризованный поиск пользователей по ID")
    void shouldFindUsersById(Long id) {
        // Arrange — создаём пользователя с конкретным ID
        User user = new User("Test User " + id, "test" + id + "@example.com", (int) (20 + id));
        userDao.create(user);
        Long factId = user.getId();

        // Act
        Optional<User> result = userDao.findById(factId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(factId, result.get().getId());
    }

    @Test
    @DisplayName("Поиск пользователя по email — пользователь существует")
    void shouldFindUserByEmailWhenExists() {
        // Arrange
        User user = new User("Email User", "email@example.com", 32);
        userDao.create(user);

        // Act
        Optional<User> result = userDao.findByEmail("email@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Email User", result.get().getName());
    }

    @Test
    @DisplayName("Поиск пользователя по email — пользователь не существует")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Act
        Optional<User> result = userDao.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void shouldFindAllUsers() {
        // Arrange
        User user1 = new User("User One", "one@example.com", 25);
        User user2 = new User("User Two", "two@example.com", 30);
        userDao.create(user1);
        userDao.create(user2);

        // Act
        List<User> users = userDao.findAll();

        // Assert
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void shouldUpdateUser() {
        // Arrange
        User user = new User("Old Name", "update@example.com", 25);
        userDao.create(user);

        // Модифицируем пользователя
        user.setName("Updated Name");
        user.setAge(35);

        // Act
        userDao.update(user);

        // Assert
        Optional<User> updated = userDao.findById(user.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals(35, updated.get().getAge());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void shouldDeleteUser() {
        // Arrange
        User user = new User("To Delete", "delete@example.com", 40);
        userDao.create(user);

        // Act
        userDao.delete(user.getId());

        // Assert
        Optional<User> deleted = userDao.findById(user.getId());
        assertFalse(deleted.isPresent());
    }
}
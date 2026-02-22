package aston.service;

import aston.dao.UserDao;
import aston.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceUnitTest {
    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("Yuriy Gagarin", "gagarin@mail.ru", 35);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void shouldCreateUserSuccessfully() {
        // Arrange
        doNothing().when(userDao).create(any(User.class));

        // Act
        User result = userService.createUser("Yuriy Gagarin", "gagarin@mail.ru", 35);

        // Assert
        assertNotNull(result);
        assertEquals("Yuriy Gagarin", result.getName());
        assertEquals("gagarin@mail.ru", result.getEmail());
        assertEquals(35, result.getAge());
        verify(userDao, times(1)).create(argThat(user ->
                "Yuriy Gagarin".equals(user.getName()) &&
                "gagarin@mail.ru".equals(user.getEmail()) &&
                35 == user.getAge()));
    }

    @ParameterizedTest
    @DisplayName("Проверка создания пользователей с разными данными")
    @CsvSource({
            "Valentina Tereshkova, valya@mail.ru, 50",
            "Belka, belka@mail.ru, 3",
            "Strelka, strelka@mail.ru, 4"
    })
    void shouldCreateUsersWithValidData(String name, String email, int age){
        // Arrange
        doNothing().when(userDao).create(any(User.class));

        // Act
        User result = userService.createUser(name, email, age);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(age, result.getAge());
        verify(userDao, times(1)).create(argThat(user ->
                name.equals(user.getName()) &&
                        email.equals(user.getEmail()) &&
                        age == user.getAge()));
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь существует")
    void shouldFindUserByIdWhenExist() {
        // Arrange
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не существует")
    void shouldFindUserByIdWhenNotExist() {
        // Arrange
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 4L})
    @DisplayName("Параметризованный поиск пользователей по ID")
    void shouldFindUsersById(Long id) {
        // Arrange
        User user = new User("Test user " + id, "testmail" + id + "@mail.ru", (int) (id + 20));
        user.setId(id);
        when(userDao.findById(id)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findUserById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь существует")
    void shouldFindUserByEmailWhenExist() {
        // Arrange
        when(userDao.findByEmail("gagarin@mail.ru")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findUserByEmail("gagarin@mail.ru");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не существует")
    void shouldFindUserByEmailWhenNotExist() {
        // Arrange
        when(userDao.findByEmail("test@mail.ru")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserByEmail("test@mail.ru");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void shouldFindAllUsers() {
        // Arrange
        when(userDao.findAll()).thenReturn(List.of(testUser));

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.getFirst());
    }

    @Test
    @DisplayName("Обновление пользователя - успешный сценарий")
    void shouldUpdateUserWhenExist() {
        // Arrange
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDao).update(any(User.class));

        // Act
        userService.updateUser(1L, "New name", "", "50");

        // Assert
        verify(userDao, times(1)).update(argThat(user ->
                user.getId().equals(1L) &&
                user.getName().equals("New name") &&
                user.getEmail().equals("gagarin@mail.ru") &&
                user.getAge() == 50));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void shouldThrowExceptionWhenUpdatingNotExistingUser() {
        // Arrange
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // Act
        userService.updateUser(999L, "New name", "", "50");

        // Assert
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Удаление существующего пользователя")
    void shouldDeleteUserWhenExist() {
        // Arrange
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDao).delete(any(Long.class));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userDao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void shouldThrowExceptionWhenDeletingNotExistingUser() {
        // Arrange
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // Act
        userService.deleteUser(999L);

        // Assert
        verify(userDao, never()).delete(999L);
    }

    @Test
    @DisplayName("Метод close() — успешное закрытие")
    void shouldCloseSuccessfully() throws Exception {
        // Act & Assert
        assertDoesNotThrow(() -> userService.close());
        verify(userDao, atMost(1)).close();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserData")
    @DisplayName("Параметризованный тест обработки некорректных данных при создании")
    void shouldHandleInvalidDataOnCreate(String name, String email, int age, Class<Exception> expectedException) {
        // Act & Assert
        assertThrows(expectedException,
                () -> userService.createUser(name, email, age));
    }

    static List<Arguments> provideInvalidUserData() {
        return List.of(
                Arguments.of("", "valid@example.com", 25, IllegalArgumentException.class),
                Arguments.of("Valid Name", "invalid-email", 25, IllegalArgumentException.class),
                Arguments.of("Valid Name", "valid@example.com", -5, IllegalArgumentException.class)
        );
    }
}
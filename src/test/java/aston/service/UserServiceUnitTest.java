package aston.service;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;
import aston.dto.UserResponseDto;
import aston.exception.UserNotFoundException;
import aston.mapper.UserMapper;
import aston.model.User;
import aston.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setName("Yuriy Gagarin");
        testUser.setEmail("gagarin@mail.ru");
        testUser.setAge(35);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void shouldCreateUserSuccessfully() {
        when(userMapper.toEntity(any(CreateUserRequestDto.class))).thenReturn(testUser);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("Yuriy Gagarin");
        expectedResponse.setEmail("gagarin@mail.ru");
        expectedResponse.setAge(35);

        when(userMapper.toDTO(any(User.class))).thenReturn(expectedResponse);

        CreateUserRequestDto requestDto = new CreateUserRequestDto("Yuriy Gagarin", "gagarin@mail.ru", 35);
        UserResponseDto result = userService.createUser(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Yuriy Gagarin", result.getName());
        assertEquals("gagarin@mail.ru", result.getEmail());
        assertEquals(35, result.getAge());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("Yuriy Gagarin", capturedUser.getName());
        assertEquals("gagarin@mail.ru", capturedUser.getEmail());
        assertEquals(35, capturedUser.getAge());
    }

    @ParameterizedTest
    @DisplayName("Проверка создания пользователей с разными данными")
    @CsvSource({
            "Valentina Tereshkova, valya@mail.ru, 50",
            "Belka, belka@mail.ru, 3",
            "Strelka, strelka@mail.ru, 4"
    })
    void shouldCreateUsersWithValidData(String name, String email, int age){
        User testUserForCase = new User();
        testUserForCase.setName(name);
        testUserForCase.setEmail(email);
        testUserForCase.setAge(age);
        testUserForCase.setId(1L);

        when(userMapper.toEntity(any(CreateUserRequestDto.class)))
                .thenReturn(testUserForCase);


        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName(name);
        expectedResponse.setEmail(email);
        expectedResponse.setAge(age);

        when(userMapper.toDTO(any(User.class))).thenReturn(expectedResponse);

        CreateUserRequestDto requestDto = new CreateUserRequestDto(name, email, age);
        UserResponseDto result = userService.createUser(requestDto);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(age, result.getAge());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(name, capturedUser.getName());
        assertEquals(email, capturedUser.getEmail());
        assertEquals(age, capturedUser.getAge());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь существует")
    void shouldFindUserByIdWhenExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("Yuriy Gagarin");
        expectedResponse.setEmail("gagarin@mail.ru");
        expectedResponse.setAge(35);
        expectedResponse.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        when(userMapper.toDTO(testUser)).thenReturn(expectedResponse);

        UserResponseDto result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getAge(), result.getAge());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не существует")
    void shouldFindUserByIdWhenNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(999L));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 4L})
    @DisplayName("Параметризованный поиск пользователей по ID")
    void shouldFindUsersById(Long id) {
        User user = new User();
        user.setName("Test user " + id);
        user.setEmail("testmail" + id + "@mail.ru");
        user.setAge((int) (id + 20));
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(id);
        expectedResponse.setName(user.getName());
        expectedResponse.setEmail(user.getEmail());
        expectedResponse.setAge(user.getAge());

        when(userMapper.toDTO(user)).thenReturn(expectedResponse);

        UserResponseDto result = userService.findUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getAge(), result.getAge());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь существует")
    void shouldFindUserByEmailWhenExist() {
        when(userRepository.findByEmail("gagarin@mail.ru")).thenReturn(Optional.of(testUser));

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("Yuriy Gagarin");
        expectedResponse.setEmail("gagarin@mail.ru");
        expectedResponse.setAge(35);

        when(userMapper.toDTO(testUser)).thenReturn(expectedResponse);

        UserResponseDto result = userService.findUserByEmail("gagarin@mail.ru");

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getAge(), result.getAge());
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не существует")
    void shouldFindUserByEmailWhenNotExist() {
        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findUserByEmail("test@mail.ru"));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void shouldFindAllUsers() {
        List<User> users = List.of(testUser);

        when(userRepository.findAll()).thenReturn(users);

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("Yuriy Gagarin");
        expectedResponse.setEmail("gagarin@mail.ru");
        expectedResponse.setAge(35);
        expectedResponse.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        when(userMapper.toDTO(testUser)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.getFirst().getId());
        assertEquals(testUser.getName(), result.getFirst().getName());
        assertEquals(testUser.getEmail(), result.getFirst().getEmail());
        assertEquals(testUser.getAge(), result.getFirst().getAge());
    }

    @Test
    @DisplayName("Обновление пользователя - успешный сценарий")
    void shouldUpdateUserWhenExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User updatedUserExpected  = new User();
        updatedUserExpected .setId(1L);
        updatedUserExpected .setName("New name");
        updatedUserExpected .setEmail("gagarin@mail.ru");
        updatedUserExpected .setAge(50);
        updatedUserExpected.setCreatedAt(testUser.getCreatedAt());

        doAnswer(invocation -> {
            UpdateUserRequestDto dto = invocation.getArgument(0);
            User user = invocation.getArgument(1);

            if (dto.getName() != null) user.setName(dto.getName());
            if (dto.getEmail() != null) user.setEmail(dto.getEmail());
            if (dto.getAge() != null) user.setAge(dto.getAge());

            return null;
        }).when(userMapper).updateEntityFromRequest(any(UpdateUserRequestDto.class), eq(testUser));

        when(userRepository.save(any(User.class))).thenReturn(updatedUserExpected );

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("New name");
        expectedResponse.setEmail("gagarin@mail.ru");
        expectedResponse.setAge(50);
        expectedResponse.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        when(userMapper.toDTO(testUser)).thenReturn(expectedResponse);

        UpdateUserRequestDto requestDto = new UpdateUserRequestDto("New name", null, 50);
        userService.updateUser(1L, requestDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals(1L, updatedUser.getId());
        assertEquals("New name", updatedUser.getName());
        assertEquals("gagarin@mail.ru", updatedUser.getEmail());
        assertEquals(50, updatedUser.getAge());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void shouldThrowExceptionWhenUpdatingNotExistingUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateUserRequestDto requestDto = new UpdateUserRequestDto("New name", null, 50);
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(999L, requestDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Удаление существующего пользователя")
    void shouldDeleteUserWhenExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void shouldThrowExceptionWhenDeletingNotExistingUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(999L));

        verify(userRepository, never()).deleteById(999L);
    }

    @Test
    @DisplayName("Метод close() — успешное закрытие")
    void shouldCloseSuccessfully() throws Exception {
        assertDoesNotThrow(() -> userService.close());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserData")
    @DisplayName("Параметризованный тест обработки некорректных данных при создании")
    void shouldHandleInvalidDataOnCreate(String name, String email, Integer age,
                                         Class<? extends Exception> expectedException, String errorMessageContains) {
        CreateUserRequestDto requestDto = new CreateUserRequestDto(name, email, age);

        Throwable thrown = assertThrows(expectedException,
                () -> userService.createUser(requestDto),
                "Ожидалось исключение типа " + expectedException.getSimpleName()
        );

        if (errorMessageContains != null) {
            assertTrue(thrown.getMessage().contains(errorMessageContains),
                    "Сообщение об ошибке должно содержать '" + errorMessageContains + "', но было: " + thrown.getMessage());
        }
    }

    static List<Arguments> provideInvalidUserData() {
        return List.of(
                Arguments.of("", "valid@example.com", 25,
                        IllegalArgumentException.class, "Name cannot be empty or null"),
                Arguments.of(null, "valid@example.com", 25,
                        IllegalArgumentException.class, "Name cannot be empty or null"),
                Arguments.of("Valid Name", "invalid-email", 25,
                        IllegalArgumentException.class, "Invalid email format"),
                Arguments.of("Valid Name", null, 25,
                        IllegalArgumentException.class, "Invalid email format"),
                Arguments.of("Valid Name", "valid@example.com", -5,
                        IllegalArgumentException.class, "Age must be between 0 and 120"),
                Arguments.of("Valid Name", "valid@example.com", 150,
                        IllegalArgumentException.class, "Age must be between 0 and 120")
        );
    }
}
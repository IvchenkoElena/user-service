package aston.service;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;

import aston.dto.UserResponseDto;
import aston.exception.UserNotFoundException;
import aston.mapper.UserMapper;
import aston.model.User;
import aston.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    private final UserMapper userMapper;

    @Override
    public void close() throws Exception {
        log.info("UserService закрыт");
    }

    @Override
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto requestDto) {
        validateRequestData(requestDto);
        validateUniqueEmail(requestDto.getEmail());
        log.info("Создание пользователя: name={}, email={}", requestDto.getName(), requestDto.getEmail());
        User user = userMapper.toEntity(requestDto);
        User savedUser = repository.save(user);
        log.info("Новый пользователь с ID = {} успешно создан", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    private void validateRequestData(CreateUserRequestDto requestDto) {
        if (requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty or null");
        }
        if (requestDto.getEmail() == null || !requestDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (requestDto.getAge() < 0 || requestDto.getAge() > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }
    }

    private void validateUniqueEmail(String email) {
        if (repository.findByEmail(email).isPresent()) {
            log.warn("Попытка создать пользователя с существующим email: {}", email);
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserById(Long id) {
        log.info("Поиск пользователя по Id={}", id);
        Optional<User> mayBeUser = repository.findById(id);
        if (mayBeUser.isPresent()) {
            log.info("Пользователь с ID {} найден", id);
            return userMapper.toDTO(mayBeUser.get());
        } else {
            log.info("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserByEmail(String email) {
        log.info("Поиск пользователя по Email={}", email);
        Optional<User> mayBeUser = repository.findByEmail(email);
        if (mayBeUser.isPresent()) {
            log.info("Пользователь с Email {} найден", email);
            return userMapper.toDTO(mayBeUser.get());
        } else {
            log.info("Пользователь с Email {} не найден", email);
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        List<User> users = repository.findAll();
        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto requestDto) {
        Optional<User> mayBeUser = repository.findById(id);
        if (mayBeUser.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        User user = mayBeUser.get();
        log.info("Старые данные пользователя: " + user);
        userMapper.updateEntityFromRequest(requestDto, user);
        User updatedUser = repository.save(user);
        System.out.println("Новые данные пользователя: " + updatedUser);
        log.info("Пользователь с ID = {} успешно обновлен", id);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        Optional<User> mayBeUser = repository.findById(id);
        if (mayBeUser.isEmpty()) {
            log.info("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        repository.deleteById(id);
        log.info("Пользователь с ID {} удалён", id);
    }
}

package aston.service;

import aston.dao.UserDao;
import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;

import aston.exception.UserNotFoundException;
import aston.mapper.UserMapper;
import aston.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final UserMapper userMapper;

    @Override
    public void close() throws Exception {
        log.info("UserService закрыт");
    }

    @Override
    public User createUser(CreateUserRequestDto requestDto) {
        validateUniqueEmail(requestDto.getEmail());
        log.info("Создание пользователя: name={}, email={}", requestDto.getName(), requestDto.getEmail());
        User user = userMapper.toEntity(requestDto);
        userDao.create(user);
        log.info("Новый пользователь с ID = {} успешно создан", user.getId());
        return user;
    }

    private void validateUniqueEmail(String email) {
        if (userDao.findByEmail(email).isPresent()) {
            log.warn("Попытка создать пользователя с существующим email: {}", email);
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
    }

    @Override
    public Optional<User> findUserById(Long id) {
        log.info("Поиск пользователя по Id={}", id);
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isPresent()) {
            log.info("Пользователь с ID {} найден", id);
        } else {
            log.info("Пользователь с ID {} не найден", id);
        }
        return mayBeUser;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        log.info("Поиск пользователя по Email={}", email);
        Optional<User> mayBeUser = userDao.findByEmail(email);
        if (mayBeUser.isPresent()) {
            log.info("Пользователь с Email {} найден", email);
        } else {
            log.info("Пользователь с Email {} не найден", email);
        }
        return mayBeUser;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> users = userDao.findAll();
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public void updateUser(Long id, UpdateUserRequestDto requestDto) {
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        User user = mayBeUser.get();
        log.info("Старые данные пользователя: " + user);
        userMapper.updateEntityFromRequest(requestDto, user);
        userDao.update(user);
        System.out.println("Новые данные пользователя: " + user);
        log.info("Пользователь с ID = {} успешно обновлен", id);
    }

    @Override
    public void deleteUser(Long id) {
        Optional<User> mayBeUser = userDao.findById(id);
        if (mayBeUser.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден");
            log.info("Пользователь с ID {} не найден", id);
            return;
        }
        userDao.delete(id);
        log.info("Пользователь с ID {} удалён", id);
    }
}

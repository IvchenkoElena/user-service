package aston.service;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;
import aston.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends AutoCloseable {
    User createUser(CreateUserRequestDto requestDto);
    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    void updateUser(Long id, UpdateUserRequestDto requestDto);
    void deleteUser(Long id);

}

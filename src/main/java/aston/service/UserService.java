package aston.service;

import aston.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends AutoCloseable {
    User createUser(String name, String email, int age);
    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    void updateUser(Long id, String name, String email, String ageInput);
    void deleteUser(Long id);

}

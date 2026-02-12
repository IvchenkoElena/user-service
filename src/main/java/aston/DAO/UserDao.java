package aston.DAO;

import aston.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user);
    Optional<User> findById(Long id);
    void update(User user);
    void delete(Long id);
    List<User> findAll();
}

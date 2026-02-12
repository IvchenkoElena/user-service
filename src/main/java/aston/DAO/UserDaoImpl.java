package aston.DAO;

import aston.model.User;
import aston.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void create(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("User успешно создан. ID: {}, Email: {}", user.getId(), user.getEmail());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при создании пользователя. Данные: {}, Сообщение: {}", user, e.getMessage(), e);
            throw e; // Перебрасываем исключение для обработки на верхнем уровне
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;

        try {
            user = session.find(User.class, id);
            if (user != null) {
                logger.info("Пользователь найден по ID={}. Имя: {}, Email: {}", id, user.getName(), user.getEmail());
                return Optional.of(user);
            } else {
                logger.warn("Пользователь с ID={} не найден в базе данных.", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID={}. Сообщение: {}", id, e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.info("Пользователь успешно обновлён. ID: {}, Email: {}", user.getId(), user.getEmail());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при обновлении пользователя с ID={}. Данные: {}, Сообщение: {}",
                    user.getId(), user, e.getMessage(), e);
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            User user = session.find(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("Пользователь удалён. ID: {}, Email: {}", id, user.getEmail());
            } else {
                logger.warn("Попытка удаления пользователя с ID={}, но запись не найдена.", id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении пользователя с ID={}. Сообщение: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<User> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> users;

        try {
            users = session.createQuery("FROM User", User.class).list();
            logger.info("Получено {} записей из таблицы users.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка всех пользователей. Сообщение: {}", e.getMessage(), e);
            return List.of(); // Возвращаем пустой список при ошибке
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}

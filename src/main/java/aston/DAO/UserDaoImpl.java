package aston.DAO;

import aston.model.User;
import aston.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.*;


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
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Constraint violation when creating user: {}", e.getSQLException().getMessage());
            throw new RuntimeException("Нарушение ограничения БД: " + e.getSQLException().getMessage(), e);
        } catch (JDBCConnectionException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
        } catch (SQLGrammarException e) {
            if (transaction != null) transaction.rollback();
            logger.error("SQL syntax error: {}", e.getMessage());
            throw new RuntimeException("Синтаксическая ошибка SQL", e);
        } catch (TransactionException e) {
            logger.error("Transaction failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка транзакции", e);
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
        } catch (ObjectNotFoundException e) {
            logger.warn("User with id {} not found", id);
            return Optional.empty();
        } catch (JDBCConnectionException e) {
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
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
    public Optional<User> findByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;

        try {
            // Используем HQL-запрос с параметром
            user = session.createQuery(
                            "FROM User WHERE email = :email",
                            User.class
                    )
                    .setParameter("email", email)
                    .uniqueResult();
            if (user != null) {
                logger.info("Пользователь найден по Email={}. Имя: {}, ID: {}", email, user.getName(), user.getId());
                return Optional.of(user);
            } else {
                logger.warn("Пользователь с Email={} не найден в базе данных.", email);
                return Optional.empty();
            }
        } catch (JDBCConnectionException e) {
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по Email={}. Сообщение: {}", email, e.getMessage(), e);
            return Optional.empty();
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
        } catch (JDBCConnectionException e) {
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка всех пользователей. Сообщение: {}", e.getMessage(), e);
            return List.of(); // Возвращаем пустой список при ошибке
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
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Constraint violation when updating user: {}", e.getSQLException().getMessage());
            throw new RuntimeException("Нарушение ограничения БД при обновлении", e);
        } catch (StaleObjectStateException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Stale object (optimistic lock): {}", e.getMessage());
            throw new RuntimeException("Данные устарели (оптимистическая блокировка)", e);
        } catch (JDBCConnectionException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
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
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Constraint violation when deleting user: {}", e.getSQLException().getMessage());
            throw new RuntimeException("Нарушение ограничения БД при удалении", e);
        } catch (JDBCConnectionException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Database connection failed: {}", e.getMessage());
            throw new RuntimeException("Ошибка соединения с БД", e);
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
}

package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.UserDAO;
import com.pinguela.rentexpres.dao.impl.UserDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link UserService}.
 * Manages business logic and transaction control for user operations.
 * Delegates data access to {@link UserDAO}.
 * 
 * All transactions are handled manually using {@link JDBCUtils}.
 */
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public UserDTO findById(Integer id) throws RentexpresException {
        Connection connection = null;
        UserDTO user = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            user = userDAO.findById(connection, id);
            if (user != null) {
                user.setPassword(null); // Never expose password
            }

            JDBCUtils.commitTransaction(connection);
            logger.info("findById completed successfully. ID: {}", id);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findById for User", e);
            throw new RentexpresException("Error in findById for User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return user;
    }

    @Override
    public List<UserDTO> findAll() throws RentexpresException {
        Connection connection = null;
        List<UserDTO> users = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            users = userDAO.findAll(connection);
            if (users != null) {
                for (UserDTO u : users) {
                    u.setPassword(null);
                }
            }

            JDBCUtils.commitTransaction(connection);
            logger.info("findAll completed successfully. Total users: {}", (users != null ? users.size() : 0));
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findAll for User", e);
            throw new RentexpresException("Error in findAll for User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return users;
    }

    @Override
    public boolean create(UserDTO user) throws RentexpresException {
        Connection connection = null;
        boolean created = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            created = userDAO.create(connection, user);
            if (created) {
                JDBCUtils.commitTransaction(connection);
                logger.info("User created successfully. ID: {}", user.getUserId());
                user.setPassword(null);
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("User creation failed.");
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error creating User", e);
            throw new RentexpresException("Error creating User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return created;
    }

    @Override
    public boolean update(UserDTO user) throws RentexpresException {
        Connection connection = null;
        boolean updated = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            updated = userDAO.update(connection, user);
            if (updated) {
                JDBCUtils.commitTransaction(connection);
                logger.info("User updated successfully. ID: {}", user.getUserId());
                user.setPassword(null);
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("User update failed. ID: {}", user.getUserId());
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error updating User", e);
            throw new RentexpresException("Error updating User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return updated;
    }

    @Override
    public boolean delete(Integer id) throws RentexpresException {
        Connection connection = null;
        boolean deleted = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            deleted = userDAO.delete(connection, id);
            if (deleted) {
                JDBCUtils.commitTransaction(connection);
                logger.info("User deleted (deactivated) successfully. ID: {}", id);
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("User deletion failed. ID: {}", id);
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error deleting User", e);
            throw new RentexpresException("Error deleting User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return deleted;
    }

    @Override
    public Results<UserDTO> findByCriteria(UserCriteria criteria) throws RentexpresException {
        Connection connection = null;
        Results<UserDTO> results = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            results = userDAO.findByCriteria(connection, criteria);
            if (results != null && results.getResults() != null) {
                for (UserDTO u : results.getResults()) {
                    u.setPassword(null);
                }
            }

            JDBCUtils.commitTransaction(connection);
            logger.info("findByCriteria completed successfully. Page {} (Size: {}), Total: {}", 
                        criteria.getPageNumber(), criteria.getPageSize(), 
                        results != null ? results.getTotalRecords() : 0);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findByCriteria for User", e);
            throw new RentexpresException("Error in findByCriteria for User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return results;
    }
    @Override
    public boolean activate(Integer id) throws RentexpresException {
        Connection connection = null;
        boolean activated = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            activated = userDAO.activate(connection, id);

            if (activated) {
                JDBCUtils.commitTransaction(connection);
                logger.info("User reactivated successfully. ID: {}", id);
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("User reactivation failed. ID: {}", id);
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in activate for User: ", e);
            throw new RentexpresException("Error activating User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return activated;
    }


    @Override
    public UserDTO authenticate(String login, String password) throws RentexpresException {
        Connection connection = null;
        UserDTO user = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            user = userDAO.authenticate(connection, login, password);
            if (user != null) {
                user.setPassword(null);
            }

            JDBCUtils.commitTransaction(connection);
            logger.info("Authentication successful for login: {}", login);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error during authentication for User", e);
            throw new RentexpresException("Error during authentication for User", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return user;
    }
}

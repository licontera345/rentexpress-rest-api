package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;

/**
 * Service interface for managing users.
 * Provides CRUD operations, authentication, and filtered searches.
 */
public interface UserService {

    /**
     * Finds a user by their ID.
     *
     * @param id User ID
     * @return {@link UserDTO} if found, null otherwise
     * @throws RentexpresException On data or logic errors
     */
    UserDTO findById(Integer id) throws RentexpresException;

    /**
     * Retrieves all users.
     *
     * @return List of {@link UserDTO}
     * @throws RentexpresException On data or logic errors
     */
    List<UserDTO> findAll() throws RentexpresException;

    /**
     * Creates a new user.
     *
     * @param user User data to create
     * @return true if creation was successful
     * @throws RentexpresException On data or logic errors
     */
    boolean create(UserDTO user) throws RentexpresException;

    /**
     * Updates an existing user.
     *
     * @param user User data to update (must include ID)
     * @return true if update was successful
     * @throws RentexpresException On data or logic errors
     */
    boolean update(UserDTO user) throws RentexpresException;

    /**
     * Deletes (deactivates) a user by ID.
     *
     * @param id User ID
     * @return true if deletion was successful
     * @throws RentexpresException On data or logic errors
     */
    boolean delete(Integer id) throws RentexpresException;

    /**
     * Finds users by specific criteria (paginated).
     *
     * @param criteria User search filter
     * @return {@link Results}&lt;{@link UserDTO}&gt;
     * @throws RentexpresException On data or logic errors
     */
    Results<UserDTO> findByCriteria(UserCriteria criteria) throws RentexpresException;

    /**
     * Authenticates a user with username/email and password.
     *
     * @param login Username or email
     * @param password Plain text password
     * @return Authenticated {@link UserDTO} if credentials are valid, null otherwise
     * @throws RentexpresException On data or logic errors
     */
    UserDTO authenticate(String login, String password) throws RentexpresException;

    /**
     * Reactivates (restores) a deactivated user by their ID.
     *
     * @param id User ID
     * @return true if activation was successful
     * @throws RentexpresException On data or logic errors
     */
    boolean activate(Integer id) throws RentexpresException;

}

package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.Results;

/**
 * DAO interface for the 'employee' table.
 * Provides CRUD operations, authentication and search by criteria.
 */
public interface EmployeeDAO {

    /**
     * Finds an employee by its ID.
     *
     * @param connection Active SQL connection
     * @param id Employee ID
     * @return EmployeeDTO if found, null otherwise
     * @throws DataException On SQL or data access error
     */
    EmployeeDTO findById(Connection connection, Integer id) throws DataException;

    /**
     * Creates a new employee.
     *
     * @param connection Active SQL connection
     * @param employee Employee to insert
     * @return true if successfully created
     * @throws DataException On SQL or data access error
     */
    boolean create(Connection connection, EmployeeDTO employee) throws DataException;

    /**
     * Updates an existing employee.
     *
     * @param connection Active SQL connection
     * @param employee Employee to update
     * @return true if successfully updated
     * @throws DataException On SQL or data access error
     */
    boolean update(Connection connection, EmployeeDTO employee) throws DataException;

    /**
     * Deletes an employee by ID.
     *
     * @param connection Active SQL connection
     * @param id Employee ID to delete
     * @return true if successfully deleted
     * @throws DataException On SQL or data access error
     */
    boolean delete(Connection connection, Integer id) throws DataException;

    /**
     * Returns all employees.
     *
     * @param connection Active SQL connection
     * @return List of all employees
     * @throws DataException On SQL or data access error
     */
    List<EmployeeDTO> findAll(Connection connection) throws DataException;

    /**
     * Authenticates an employee using email or username and password.
     *
     * @param connection Active SQL connection
     * @param login Username or email
     * @param clearPassword Plain text password
     * @return EmployeeDTO if authentication succeeds, null otherwise
     * @throws DataException On SQL or data access error
     */
    EmployeeDTO authenticate(Connection connection, String login, String clearPassword) throws DataException;

    /**
     * Returns a paginated list of employees matching search criteria.
     *
     * @param connection Active SQL connection
     * @param criteria Employee search filter
     * @return Paginated results
     * @throws DataException On SQL or data access error
     */
    Results<EmployeeDTO> findByCriteria(Connection connection, EmployeeCriteria criteria) throws DataException;

	boolean activate(Connection connection, Integer employeeId) throws DataException;
}

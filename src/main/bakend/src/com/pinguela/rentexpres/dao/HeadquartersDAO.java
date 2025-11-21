package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;

/**
 * DAO interface for the 'headquarters' table.
 * Provides CRUD operations.
 */
public interface HeadquartersDAO {

    /**
     * Finds a headquarters by its ID.
     *
     * @param connection Active SQL connection
     * @param id Headquarters ID
     * @return HeadquartersDTO if found, null otherwise
     * @throws DataException On SQL or data access error
     */
    HeadquartersDTO findById(Connection connection, Integer id) throws DataException;

    /**
     * Inserts a new headquarters record.
     *
     * @param connection Active SQL connection
     * @param headquarters Headquarters object to insert
     * @return true if successfully created
     * @throws DataException On SQL or data access error
     */
    boolean create(Connection connection, HeadquartersDTO headquarters) throws DataException;

    /**
     * Updates an existing headquarters record.
     *
     * @param connection Active SQL connection
     * @param headquarters Headquarters object to update
     * @return true if successfully updated
     * @throws DataException On SQL or data access error
     */
    boolean update(Connection connection, HeadquartersDTO headquarters) throws DataException;

    /**
     * Deletes a headquarters by ID.
     *
     * @param connection Active SQL connection
     * @param id Headquarters ID to delete
     * @return true if successfully deleted
     * @throws DataException On SQL or data access error
     */
    boolean delete(Connection connection, Integer id) throws DataException;

    /**
     * Returns all headquarters records.
     *
     * @param connection Active SQL connection
     * @return List of all headquarters
     * @throws DataException On SQL or data access error
     */
    List<HeadquartersDTO> findAll(Connection connection) throws DataException;
}

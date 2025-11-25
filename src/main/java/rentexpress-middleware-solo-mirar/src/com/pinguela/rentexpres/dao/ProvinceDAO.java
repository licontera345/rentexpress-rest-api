package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ProvinceDTO;

/**
 * DAO interface for the 'province' table. Provides basic CRUD operations.
 */
public interface ProvinceDAO {

	/**
	 * Finds a province by its ID.
	 *
	 * @param connection Active SQL connection
	 * @param id         Province ID
	 * @return ProvinceDTO if found, null otherwise
	 * @throws DataException On SQL or data access error
	 */
	ProvinceDTO findById(Connection connection, Integer id) throws DataException;

	/**
	 * Inserts a new province record.
	 *
	 * @param connection Active SQL connection
	 * @param province   Province object to insert
	 * @return true if successfully created
	 * @throws DataException On SQL or data access error
	 */
	boolean create(Connection connection, ProvinceDTO province) throws DataException;

	/**
	 * Updates an existing province.
	 *
	 * @param connection Active SQL connection
	 * @param province   Province object to update
	 * @return true if successfully updated
	 * @throws DataException On SQL or data access error
	 */
	boolean update(Connection connection, ProvinceDTO province) throws DataException;

	/**
	 * Deletes a province by ID.
	 *
	 * @param connection Active SQL connection
	 * @param id         Province ID to delete
	 * @return true if successfully deleted
	 * @throws DataException On SQL or data access error
	 */
	boolean delete(Connection connection, Integer id) throws DataException;

	/**
	 * Returns all provinces.
	 *
	 * @param connection Active SQL connection
	 * @return List of all provinces
	 * @throws DataException On SQL or data access error
	 */
	List<ProvinceDTO> findAll(Connection connection) throws DataException;
}

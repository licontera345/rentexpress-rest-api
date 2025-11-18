package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.Results;

/**
 * DAO interface for the 'rental' table. Provides CRUD operations and search by
 * criteria.
 */
public interface RentalDAO {

	RentalDTO findById(Connection connection, Integer id) throws DataException;

	List<RentalDTO> findAll(Connection connection) throws DataException;

	boolean create(Connection connection, RentalDTO rental) throws DataException;

	boolean update(Connection connection, RentalDTO rental) throws DataException;

	boolean delete(Connection connection, Integer id) throws DataException;

	Results<RentalDTO> findByCriteria(Connection connection, RentalCriteria criteria) throws DataException;

	boolean existsByReservation(Integer reservationId) throws DataException;
}

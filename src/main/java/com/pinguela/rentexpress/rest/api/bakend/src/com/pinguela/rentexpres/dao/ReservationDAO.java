package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;

/**
 * DAO interface for the 'reservation' table. Provides CRUD operations and
 * criteria-based search.
 */
public interface ReservationDAO {

	ReservationDTO findById(Connection connection, Integer id) throws DataException;

	List<ReservationDTO> findAll(Connection connection) throws DataException;

	boolean create(Connection connection, ReservationDTO reservation) throws DataException;

	boolean update(Connection connection, ReservationDTO reservation) throws DataException;

	boolean delete(Connection connection, Integer id) throws DataException;

	Results<ReservationDTO> findByCriteria(Connection connection, ReservationCriteria criteria) throws DataException;

	void updateStatus(Connection connection, Integer reservationId, Integer statusId) throws DataException;

}

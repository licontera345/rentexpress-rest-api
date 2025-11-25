package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;

public interface VehicleDAO {

	VehicleDTO findById(Connection connection, Integer id) throws DataException;

	List<VehicleDTO> findAll(Connection connection) throws DataException;

	boolean create(Connection connection, VehicleDTO vehicle) throws DataException;

	boolean update(Connection connection, VehicleDTO vehicle) throws DataException;

	boolean delete(Connection connection, Integer id) throws DataException;

	Results<VehicleDTO> findByCriteria(Connection connection, VehicleCriteria criteria) throws DataException;

	void updateStatus(Connection connection, Integer vehicleId, Integer statusId) throws DataException;

}

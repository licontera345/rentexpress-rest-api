package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.VehicleDAO;
import com.pinguela.rentexpres.dao.impl.VehicleDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.service.VehicleService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link VehicleService}. Handles all vehicle-related logic,
 * including file management for images.
 */
public class VehicleServiceImpl implements VehicleService {

	private static final Logger logger = LogManager.getLogger(VehicleServiceImpl.class);

	private final VehicleDAO vehicleDAO;

	public VehicleServiceImpl() {
		this.vehicleDAO = new VehicleDAOImpl();
	}

	@Override
	public VehicleDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);
			VehicleDTO vehicle = vehicleDAO.findById(connection, id);
			JDBCUtils.commitTransaction(connection);
			logger.info("Vehicle found with ID: {}", id);
			return vehicle;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error finding vehicle by ID: {}", id, e);
			throw new RentexpresException("Error finding vehicle by ID: " + id, e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public List<VehicleDTO> findAll() throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);
			List<VehicleDTO> vehicles = vehicleDAO.findAll(connection);
			JDBCUtils.commitTransaction(connection);
			logger.info("findAll completed successfully. Total vehicles: {}", vehicles.size());
			return vehicles;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error retrieving all vehicles", e);
			throw new RentexpresException("Error retrieving all vehicles", e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean create(VehicleDTO vehicle) throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			boolean created = vehicleDAO.create(connection, vehicle);
			if (!created) {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Failed to create vehicle in database");
				return false;
			}
			JDBCUtils.commitTransaction(connection);
			logger.info("Vehicle created successfully with ID: {}", vehicle.getVehicleId());
			return true;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error creating vehicle", e);
			throw new RentexpresException("Error creating vehicle", e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean update(VehicleDTO vehicle) throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			boolean updated = vehicleDAO.update(connection, vehicle);
			if (!updated) {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Failed to update vehicle. ID: {}", vehicle.getVehicleId());
				return false;
			}
			JDBCUtils.commitTransaction(connection);
			logger.info("Vehicle updated successfully. ID: {}", vehicle.getVehicleId());
			return true;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error updating vehicle ID: {}", vehicle.getVehicleId(), e);
			throw new RentexpresException("Error updating vehicle", e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean delete(Integer id) throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			VehicleDTO vehicle = vehicleDAO.findById(connection, id);
			if (vehicle == null) {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("No vehicle found with ID: {} to delete", id);
				return false;
			}

			boolean deleted = vehicleDAO.delete(connection, id);
			if (!deleted) {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Failed to delete vehicle ID: {}", id);
				return false;
			}

			JDBCUtils.commitTransaction(connection);
			logger.info("Vehicle deleted successfully. ID: {}", id);
			return true;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error deleting vehicle ID: {}", id, e);
			throw new RentexpresException("Error deleting vehicle", e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public Results<VehicleDTO> findByCriteria(VehicleCriteria criteria) throws RentexpresException {
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			if (criteria == null) {
				criteria = new VehicleCriteria();
			}
			if (criteria.getPageNumber() == null || criteria.getPageNumber() < 1) {
				criteria.setPageNumber(1);
			}
			if (criteria.getPageSize() == null || criteria.getPageSize() < 1) {
				criteria.setPageSize(10);
			}

			Results<VehicleDTO> results = vehicleDAO.findByCriteria(connection, criteria);
			JDBCUtils.commitTransaction(connection);
			logger.info("findByCriteria completed. Found {} vehicles",
					(results != null && results.getResults() != null) ? results.getResults().size() : 0);
			return results;
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error during vehicle search by criteria", e);
			throw new RentexpresException("Error during vehicle search by criteria", e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

}

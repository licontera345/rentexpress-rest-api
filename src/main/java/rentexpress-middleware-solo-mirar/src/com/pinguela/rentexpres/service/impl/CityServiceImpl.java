package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.CityDAO;
import com.pinguela.rentexpres.dao.impl.CityDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CityDTO;
import com.pinguela.rentexpres.service.CityService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Service implementation for {@link CityService}. Handles business logic and
 * transaction management for City operations, delegating data access to
 * {@link CityDAO}.
 * 
 * @author
 */
public class CityServiceImpl implements CityService {

	private static final Logger logger = LogManager.getLogger(CityServiceImpl.class);
	private final CityDAO cityDAO;

	public CityServiceImpl() {
		this.cityDAO = new CityDAOImpl();
	}

	@Override
	public CityDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		CityDTO city = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			city = cityDAO.findById(connection, id);

			JDBCUtils.commitTransaction(connection);
			logger.info("findById completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for City", e);
			throw new RentexpresException("Error in findById for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return city;
	}

	@Override
	public List<CityDTO> findAll() throws RentexpresException {
		Connection connection = null;
		List<CityDTO> cities = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			cities = cityDAO.findAll(connection);

			JDBCUtils.commitTransaction(connection);
			logger.info("findAll completed successfully. Total cities: {}", (cities != null ? cities.size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findAll for City", e);
			throw new RentexpresException("Error in findAll for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return cities;
	}

	@Override
	public List<CityDTO> findByProvinceId(Integer provinceId) throws RentexpresException {
		Connection connection = null;
		List<CityDTO> cities = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			cities = cityDAO.findByProvinceId(connection, provinceId);

			JDBCUtils.commitTransaction(connection);
			logger.info("findByProvinceId completed successfully. Province ID: {}", provinceId);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findByProvinceId for City", e);
			throw new RentexpresException("Error in findByProvinceId for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return cities;
	}

	@Override
	public boolean create(CityDTO city) throws RentexpresException {
		Connection connection = null;
		boolean created = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			created = cityDAO.create(connection, city);
			if (created) {
				JDBCUtils.commitTransaction(connection);
				logger.info("City created successfully. ID: {}", city.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("City could not be created.");
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in create for City", e);
			throw new RentexpresException("Error in create for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return created;
	}

	@Override
	public boolean update(CityDTO city) throws RentexpresException {
		Connection connection = null;
		boolean updated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			updated = cityDAO.update(connection, city);
			if (updated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("City updated successfully. ID: {}", city.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("City could not be updated. ID: {}", city.getId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in update for City", e);
			throw new RentexpresException("Error in update for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return updated;
	}

	@Override
	public boolean delete(CityDTO city) throws RentexpresException {
		Connection connection = null;
		boolean deleted = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			deleted = cityDAO.delete(connection, city);
			if (deleted) {
				JDBCUtils.commitTransaction(connection);
				logger.info("City deleted successfully. ID: {}", city.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("City could not be deleted. ID: {}", city.getId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in delete for City", e);
			throw new RentexpresException("Error in delete for City", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return deleted;
	}
}

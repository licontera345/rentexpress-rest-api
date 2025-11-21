package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.AddressDAO;
import com.pinguela.rentexpres.dao.impl.AddressDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.service.AddressService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Service implementation for {@link AddressService}. Handles business logic for
 * Address operations and transaction management. Delegates data access to
 * {@link AddressDAO}.
 *
 * @author
 */
public class AddressServiceImpl implements AddressService {

	private static final Logger logger = LogManager.getLogger(AddressServiceImpl.class);
	private final AddressDAO addressDAO;

	public AddressServiceImpl() {
		this.addressDAO = new AddressDAOImpl();
	}

	@Override
	public AddressDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		AddressDTO address = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			address = addressDAO.findById(connection, id);

			JDBCUtils.commitTransaction(connection);
			logger.info("findById completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for Address", e);
			throw new RentexpresException("Error in findById for Address", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return address;
	}

	@Override
	public boolean create(AddressDTO address) throws RentexpresException {
		Connection connection = null;
		boolean created = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			created = addressDAO.create(connection, address);
			if (created) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Address created successfully. ID: {}", address.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Address could not be created.");
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in create for Address", e);
			throw new RentexpresException("Error in create for Address", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return created;
	}

	@Override
	public boolean update(AddressDTO address) throws RentexpresException {
		Connection connection = null;
		boolean updated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			updated = addressDAO.update(connection, address);
			if (updated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Address updated successfully. ID: {}", address.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Address could not be updated. ID: {}", address.getId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in update for Address", e);
			throw new RentexpresException("Error in update for Address", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return updated;
	}

	@Override
	public boolean delete(AddressDTO address) throws RentexpresException {
		Connection connection = null;
		boolean deleted = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			deleted = addressDAO.delete(connection, address, address.getId());
			if (deleted) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Address deleted successfully. ID: {}", address.getId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Address could not be deleted. ID: {}", address.getId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in delete for Address", e);
			throw new RentexpresException("Error in delete for Address", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return deleted;
	}
}

package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.HeadquartersDAO;
import com.pinguela.rentexpres.dao.impl.HeadquartersDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.service.HeadquartersService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of HeadquartersService responsible for business logic and
 * transaction management.
 */
public class HeadquartersServiceImpl implements HeadquartersService {

	private static final Logger logger = LogManager.getLogger(HeadquartersServiceImpl.class);
	private HeadquartersDAO headquartersDAO = new HeadquartersDAOImpl();

	@Override
	public HeadquartersDTO findById(Integer id) throws DataException {
		final String method = "findById";
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			HeadquartersDTO dto = headquartersDAO.findById(connection, id);
			return dto;
		} catch (SQLException e) {
			logger.error("[{}] Database error finding headquarters by id {}", method, id, e);
			throw new DataException(e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean create(HeadquartersDTO headquarters) throws DataException {
		final String method = "create";
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			connection.setAutoCommit(false);
			boolean success = headquartersDAO.create(connection, headquarters);
			connection.commit();
			return success;
		} catch (SQLException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("[{}] Error creating headquarters", method, e);
			throw new DataException(e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean update(HeadquartersDTO headquarters) throws DataException {
		final String method = "update";
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			connection.setAutoCommit(false);
			boolean success = headquartersDAO.update(connection, headquarters);
			connection.commit();
			return success;
		} catch (SQLException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("[{}] Error updating headquarters {}", method, headquarters.getId(), e);
			throw new DataException(e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public boolean delete(Integer id) throws DataException {
		final String method = "delete";
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			connection.setAutoCommit(false);
			boolean success = headquartersDAO.delete(connection, id);
			connection.commit();
			return success;
		} catch (SQLException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("[{}] Error deleting headquarters {}", method, id, e);
			throw new DataException(e);
		} finally {
			JDBCUtils.close(connection);
		}
	}

	@Override
	public List<HeadquartersDTO> findAll() throws DataException {
		final String method = "findAll";
		Connection connection = null;
		try {
			connection = JDBCUtils.getConnection();
			List<HeadquartersDTO> list = headquartersDAO.findAll(connection);
			return list;
		} catch (SQLException e) {
			logger.error("[{}] Error retrieving all headquarters", method, e);
			throw new DataException(e);
		} finally {
			JDBCUtils.close(connection);
		}
	}
}

package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RoleDAO;
import com.pinguela.rentexpres.dao.impl.RoleDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link RoleService}. Handles business logic and transaction
 * control for Role operations, delegating data access to {@link RoleDAO}.
 */
public class RoleServiceImpl implements RoleService {

	private static final Logger logger = LogManager.getLogger(RoleServiceImpl.class);
	private final RoleDAO roleDAO;

	public RoleServiceImpl() {
		this.roleDAO = new RoleDAOImpl();
	}

	@Override
	public RoleDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		RoleDTO role = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			role = roleDAO.findById(connection, id);

			JDBCUtils.commitTransaction(connection);
			logger.info("findById for Role completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for Role", e);
			throw new RentexpresException("Error in findById for Role", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return role;
	}

	@Override
	public List<RoleDTO> findAll() throws RentexpresException {
		Connection connection = null;
		List<RoleDTO> list = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			list = roleDAO.findAll(connection);

			JDBCUtils.commitTransaction(connection);
			logger.info("findAll for Role completed successfully. Total: {}", (list != null ? list.size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findAll for Role", e);
			throw new RentexpresException("Error in findAll for Role", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return list;
	}
}

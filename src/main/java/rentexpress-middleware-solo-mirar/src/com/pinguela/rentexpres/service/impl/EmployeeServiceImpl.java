package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.EmployeeDAO;
import com.pinguela.rentexpres.dao.impl.EmployeeDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link EmployeeService} responsible for handling business
 * logic related to CRUD operations and authentication of employees. Delegates
 * data access to {@link EmployeeDAO} and manages JDBC transactions.
 *
 * @author
 * @version 1.0
 */
public class EmployeeServiceImpl implements EmployeeService {

	private static final Logger logger = LogManager.getLogger(EmployeeServiceImpl.class);

	private final EmployeeDAO employeeDAO;

	public EmployeeServiceImpl() {
		this.employeeDAO = new EmployeeDAOImpl();
	}

	@Override
	public EmployeeDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		EmployeeDTO employee = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			employee = employeeDAO.findById(connection, id);
			if (employee != null) {
				employee.setPassword(null);
			}

			JDBCUtils.commitTransaction(connection);
			logger.info("findById completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for Employee", e);
			throw new RentexpresException("Error in findById for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return employee;
	}

	@Override
	public List<EmployeeDTO> findAll() throws RentexpresException {
		Connection connection = null;
		List<EmployeeDTO> list = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			list = employeeDAO.findAll(connection);
			if (list != null) {
				for (EmployeeDTO e : list) {
					e.setPassword(null);
				}
			}

			JDBCUtils.commitTransaction(connection);
			logger.info("findAll completed successfully. Total employees: {}", (list != null ? list.size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findAll for Employee", e);
			throw new RentexpresException("Error in findAll for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return list;
	}

	@Override
	public boolean create(EmployeeDTO employee) throws RentexpresException {
		Connection connection = null;
		boolean created = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			created = employeeDAO.create(connection, employee);
			if (created) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Employee created successfully. ID: {}", employee.getId());

				// Welcome email
				MailServiceImpl mailService = new MailServiceImpl();
				String subject = "Welcome to RentExpress";
				String message = "Dear " + employee.getFirstName() + ", welcome to RentExpress.";
				mailService.send(employee.getEmail(), subject, message);

				// Clear password before returning
				employee.setPassword(null);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Employee could not be created.");
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in create for Employee", e);
			throw new RentexpresException("Error in create for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return created;
	}

	@Override
	public boolean update(EmployeeDTO employee) throws RentexpresException {
		Connection connection = null;
		boolean updated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			updated = employeeDAO.update(connection, employee);
			if (updated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Employee updated successfully. ID: {}", employee.getId());
				employee.setPassword(null);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Employee could not be updated. ID: {}", employee.getId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in update for Employee", e);
			throw new RentexpresException("Error in update for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return updated;
	}

	@Override
	public boolean delete(EmployeeDTO employee, Integer id) throws RentexpresException {
		Connection connection = null;
		boolean deleted = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			deleted = employeeDAO.delete(connection, id);
			if (deleted) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Employee deleted successfully. ID: {}", id);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Employee could not be deleted. ID: {}", id);
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in delete for Employee", e);
			throw new RentexpresException("Error in delete for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return deleted;
	}

	@Override
	public EmployeeDTO autenticar(String login, String clearPassword) throws RentexpresException {
		Connection connection = null;
		EmployeeDTO employee = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			employee = employeeDAO.authenticate(connection, login, clearPassword);
			if (employee != null) {
				employee.setPassword(null);
			}

			JDBCUtils.commitTransaction(connection);
			logger.info("Authentication completed for: {}", login);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error during authentication of Employee", e);
			throw new RentexpresException("Error during authentication of Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return employee;
	}

	@Override
	public boolean activate(Integer id) throws RentexpresException {
		Connection connection = null;
		boolean activated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			activated = employeeDAO.activate(connection, id);

			if (activated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Employee reactivated successfully. ID: {}", id);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Employee reactivation failed. ID: {}", id);
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in activate for Employee: ", e);
			throw new RentexpresException("Error activating Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return activated;
	}

	@Override
	public Results<EmployeeDTO> findByCriteria(EmployeeCriteria criteria) throws RentexpresException {
		Connection connection = null;
		Results<EmployeeDTO> results = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			results = employeeDAO.findByCriteria(connection, criteria);
			if (results != null && results.getResults() != null) {
				for (EmployeeDTO e : results.getResults()) {
					e.setPassword(null);
				}
			}

			JDBCUtils.commitTransaction(connection);
			logger.info("findByCriteria completed. Page {} (Size: {}), Total: {}", criteria.getPageNumber(),
					criteria.getPageSize(), results != null ? results.getTotalRecords() : 0);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findByCriteria for Employee", e);
			throw new RentexpresException("Error in findByCriteria for Employee", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return results;
	}
}

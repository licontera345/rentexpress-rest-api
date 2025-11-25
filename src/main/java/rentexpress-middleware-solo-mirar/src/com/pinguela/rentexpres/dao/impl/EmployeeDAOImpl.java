package com.pinguela.rentexpres.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;

import com.pinguela.rentexpres.dao.EmployeeDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.EmployeeDTO;
import com.pinguela.rentexpres.model.EmployeeCriteria;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * DAO implementation for the 'employee' table. Mirrors the design of
 * UsuarioDAOImpl, adapted to the employee schema.
 */
public class EmployeeDAOImpl implements EmployeeDAO {

	private static final Logger logger = LogManager.getLogger(EmployeeDAOImpl.class);

        private static final String EMPLOYEE_SELECT_BASE = String.join(" ",
                        "SELECT e.employee_id, e.employee_name, e.password,",
                        "e.role_id, e.headquarters_id, e.first_name, e.last_name1, e.last_name2,",
                        "e.phone, e.email",
                        "FROM employee e");

	@Override
	public EmployeeDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return null;
		}

                String sql = EMPLOYEE_SELECT_BASE + " WHERE e.employee_id = ? AND e.active_status = 1";
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				logger.info("[{}] Employee found with id: {}", method, id);
				return loadEmployee(rs, false);
			}
		} catch (SQLException e) {
			logger.error("[{}] Error finding Employee by ID: {}", method, id, e);
			throw new DataException("Error finding Employee by ID: " + id, e);
		} finally {
			JDBCUtils.close(ps, rs);
		}
		return null;
	}

	@Override
	public boolean create(Connection connection, EmployeeDTO employee) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (employee == null) {
			logger.warn("[{}] called with null employee.", method);
			return false;
		}

                String sql = "INSERT INTO employee (employee_name, password, role_id, headquarters_id, first_name, "
                                + "last_name1, last_name2, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setEmployeeParameters(ps, employee, false);

			if (ps.executeUpdate() > 0) {
				try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						employee.setId(generatedKeys.getInt(1));
					}
				}
				logger.info("[{}] Employee created successfully: {}", method, employee.getEmployeeName());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error creating Employee: {}", method, employee.getEmployeeName(), e);
			throw new DataException("Error creating Employee: " + employee.getEmployeeName(), e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	@Override
	public boolean update(Connection connection, EmployeeDTO employee) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (employee == null || employee.getId() == null) {
			logger.warn("[{}] called with null employee or id.", method);
			return false;
		}

                StringBuilder sql = new StringBuilder("UPDATE employee SET employee_name = ?, password = ?, "
                                + "role_id = ?, headquarters_id = ?, first_name = ?, last_name1 = ?, last_name2 = ?, "
                                + "phone = ?, email = ? WHERE employee_id = ?");

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql.toString());
			setEmployeeParameters(ps, employee, true);

			if (ps.executeUpdate() > 0) {
				logger.info("[{}] Employee updated successfully, id: {}", method, employee.getId());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error updating Employee: {}", method, employee.getId(), e);
			throw new DataException("Error updating Employee: " + employee.getId(), e);
		} finally {
			JDBCUtils.close(ps, null);
		}
		return false;
	}

	@Override
	public boolean delete(Connection connection, Integer userId) throws DataException {

		PreparedStatement ps = null;
		boolean deleted = false;

		String sql = "UPDATE user " + "SET active_status = 0, updated_at = NOW() " + "WHERE user_id = ?";

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);

			int rows = ps.executeUpdate();
			deleted = (rows > 0);

			if (deleted) {
				logger.info("[delete] User successfully marked as inactive. user_id={}", userId);
			} else {
				logger.warn("[delete] The user to be deactivated was not found. user_id={}", userId);
			}

		} catch (SQLException e) {
			logger.error("[delete] Error deactivating user withid={}: {}", userId, e.getMessage(), e);
			throw new DataException("[UserDAOImpl] Error deactivating user", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return deleted;
	}

	@Override
	public EmployeeDTO authenticate(Connection connection, String login, String clearPassword) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (login == null || clearPassword == null) {
			logger.warn("[{}] called with null parameters.", method);
			return null;
		}

		login = login.trim();
		clearPassword = clearPassword.trim();

		boolean isEmail = login.contains("@");

                String sql = EMPLOYEE_SELECT_BASE + " WHERE e.active_status = 1 AND "
                                + (isEmail ? "LOWER(e.email) = LOWER(?)" : "e.employee_name = ?");

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, login);
			rs = ps.executeQuery();

			if (rs.next()) {
				String storedHash = rs.getString("password");
				StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

				if (encryptor.checkPassword(clearPassword, storedHash)) {
					logger.info("[{}] Employee authenticated successfully: {}", method, login);
					return loadEmployee(rs, true);
				} else {
					logger.debug("[{}] Incorrect password for {}", method, login);
				}
			} else {
				logger.debug("[{}] Employee not found: {}", method, login);
			}

		} catch (SQLException e) {
			logger.error("[{}] SQL error authenticating {}", method, login, e);
			throw new DataException("Error authenticating: " + login, e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return null;
	}

	@Override
	public List<EmployeeDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		List<EmployeeDTO> employees = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
                        ps = connection.prepareStatement(EMPLOYEE_SELECT_BASE + " WHERE e.active_status = 1");
			rs = ps.executeQuery();

			while (rs.next()) {
				employees.add(loadEmployee(rs, false));
			}

		} catch (SQLException e) {
			logger.error("[{}] Error fetching all Employees", method, e);
			throw new DataException("Error fetching all Employees", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return employees;
	}

	@Override
	public Results<EmployeeDTO> findByCriteria(Connection connection, EmployeeCriteria criteria) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		Results<EmployeeDTO> results = new Results<>();

                StringBuilder sql = new StringBuilder(EMPLOYEE_SELECT_BASE);
                sql.append(" WHERE 1=1");
                sql.append(" AND e.active_status = 1");

		if (criteria.getEmployeeName() != null && !criteria.getEmployeeName().isEmpty()) {
                        sql.append(" AND e.employee_name LIKE ? ");
		}
		if (criteria.getFirstName() != null && !criteria.getFirstName().isEmpty()) {
                        sql.append(" AND e.first_name LIKE ? ");
		}
		if (criteria.getLastName1() != null && !criteria.getLastName1().isEmpty()) {
                        sql.append(" AND e.last_name1 LIKE ? ");
		}
		if (criteria.getLastName2() != null && !criteria.getLastName2().isEmpty()) {
                        sql.append(" AND e.last_name2 LIKE ? ");
		}
		if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
                        sql.append(" AND e.email LIKE ? ");
		}
		if (criteria.getPhone() != null && !criteria.getPhone().isEmpty()) {
                        sql.append(" AND e.phone LIKE ? ");
		}

		String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS total";

		int page = criteria.getPageNumber() <= 0 ? 1 : criteria.getPageNumber();
		int size = criteria.getPageSize() <= 0 ? 25 : criteria.getPageSize();
		int offset = (page - 1) * size;

                sql.append(" ORDER BY e.employee_id LIMIT ? OFFSET ? ");

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			// total count
			ps = connection.prepareStatement(countSql);
			int idx = bindCriteriaParameters(ps, criteria, 1);
			rs = ps.executeQuery();
			int totalRecords = 0;
			if (rs.next())
				totalRecords = rs.getInt(1);
			JDBCUtils.close(ps, rs);

			// paginated query
			ps = connection.prepareStatement(sql.toString());
			idx = bindCriteriaParameters(ps, criteria, 1);
			ps.setInt(idx++, size);
			ps.setInt(idx++, offset);

			rs = ps.executeQuery();
			List<EmployeeDTO> employees = new ArrayList<>();
			while (rs.next()) {
				employees.add(loadEmployee(rs, false));
			}

			results.setResults(employees);
			results.setPageNumber(page);
			results.setPageSize(size);
			results.setTotalRecords(totalRecords);

			logger.info("[{}] findByCriteria Employee paginated: page {} of {} ({} total)", method, page,
					(int) Math.ceil((double) totalRecords / size), totalRecords);

		} catch (SQLException e) {
			logger.error("[{}] Error in findByCriteria Employee", method, e);
			throw new DataException("Error in findByCriteria Employee", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return results;
	}

	@Override
	public boolean activate(Connection connection, Integer employeeId) throws DataException {

	    PreparedStatement ps = null;
	    boolean activated = false;

	    String sql = "UPDATE employee "
	               + "SET active_status = 1, updated_at = NOW() "
	               + "WHERE employee_id = ?";

	    try {
	        ps = connection.prepareStatement(sql);
	        ps.setInt(1, employeeId);

	        int rows = ps.executeUpdate();
	        activated = (rows > 0);

	        if (activated) {
	            logger.info("[activate] Empleado reactivado correctamente. employee_id={}", employeeId);
	        } else {
	            logger.warn("[activate] No se encontró el empleado para reactivar. employee_id={}", employeeId);
	        }

	    } catch (SQLException e) {
	        logger.error("[activate] Error al reactivar empleado con id={}: {}", employeeId, e.getMessage(), e);
	        throw new DataException("[EmployeeDAOImpl] Error al reactivar empleado", e);
	    } finally {
	        JDBCUtils.close(ps,  null);
	    }

	    return activated;
	}

	private EmployeeDTO loadEmployee(ResultSet rs, boolean authenticated) throws SQLException {
		EmployeeDTO e = new EmployeeDTO();
		e.setId(rs.getInt("employee_id"));
		e.setEmployeeName(rs.getString("employee_name"));
		e.setRoleId(rs.getInt("role_id"));
		e.setHeadquartersId(rs.getInt("headquarters_id"));
		e.setFirstName(rs.getString("first_name"));
		e.setLastName1(rs.getString("last_name1"));
		e.setLastName2(rs.getString("last_name2"));
		e.setPhone(rs.getString("phone"));
		e.setEmail(rs.getString("email"));
		e.setPassword(null);
		return e;
	}

	private void setEmployeeParameters(PreparedStatement ps, EmployeeDTO e, boolean isUpdate) throws SQLException {
		ps.setString(1, e.getEmployeeName());

		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
		String encrypted = encryptor.encryptPassword(e.getPassword());
		ps.setString(2, encrypted);

		ps.setInt(3, e.getRoleId());
		ps.setInt(4, e.getHeadquartersId());
		ps.setString(5, e.getFirstName());
		ps.setString(6, e.getLastName1());
		ps.setString(7, e.getLastName2());
		ps.setString(8, e.getPhone());
		ps.setString(9, e.getEmail());

		if (isUpdate) {
			ps.setInt(10, e.getId());
		}
	}

	private int bindCriteriaParameters(PreparedStatement ps, EmployeeCriteria c, int idx) throws SQLException {
		if (c.getEmployeeName() != null && !c.getEmployeeName().isEmpty()) {
			ps.setString(idx++, "%" + c.getEmployeeName() + "%");
		}
		if (c.getFirstName() != null && !c.getFirstName().isEmpty()) {
			ps.setString(idx++, "%" + c.getFirstName() + "%");
		}
		if (c.getLastName1() != null && !c.getLastName1().isEmpty()) {
			ps.setString(idx++, "%" + c.getLastName1() + "%");
		}
		if (c.getLastName2() != null && !c.getLastName2().isEmpty()) {
			ps.setString(idx++, "%" + c.getLastName2() + "%");
		}
		if (c.getEmail() != null && !c.getEmail().isEmpty()) {
			ps.setString(idx++, "%" + c.getEmail() + "%");
		}
		if (c.getPhone() != null && !c.getPhone().isEmpty()) {
			ps.setString(idx++, "%" + c.getPhone() + "%");
		}
		return idx;
	}
}
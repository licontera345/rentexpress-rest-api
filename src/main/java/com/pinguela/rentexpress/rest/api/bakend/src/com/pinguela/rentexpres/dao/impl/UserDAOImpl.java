package com.pinguela.rentexpres.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;

import com.pinguela.rentexpres.dao.UserDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

public class UserDAOImpl implements UserDAO {

	private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);

	private static final String USER_SELECT_BASE = String.join(" ",
			"SELECT u.user_id, u.username, u.first_name, u.last_name1, u.last_name2,",
			"u.birth_date, u.email, u.password, u.phone, u.role_id, u.address_id,",
			"u.active_status, u.created_at, u.updated_at, r.role_name, a.street, a.number", "FROM user u",
			"LEFT JOIN role r ON u.role_id = r.role_id", "LEFT JOIN address a ON u.address_id = a.address_id");

	// --------------------- AUTHENTICATE ---------------------
	@Override
	public UserDTO authenticate(Connection connection, String login, String password) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		UserDTO user = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

		try {
			String sql = USER_SELECT_BASE + " WHERE (u.username = ? OR u.email = ?) AND u.active_status = 1";

			ps = connection.prepareStatement(sql);
			ps.setString(1, login);
			ps.setString(2, login);
			rs = ps.executeQuery();

			if (rs.next()) {
				String encryptedPassword = rs.getString("password");
				if (encryptor.checkPassword(password, encryptedPassword)) {
					user = loadUser(rs);
					logger.info("[{}] Successful authentication for login: {}", method, login);
				} else {
					logger.warn("[{}] Invalid password for login: {}", method, login);
				}
			} else {
				logger.warn("[{}] No user found for login: {}", method, login);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error during authentication for login: {}", method, login, e);
			throw new DataException("Error authenticating user", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return user;
	}

	// --------------------- FIND BY ID ---------------------
	@Override
	public UserDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return null;
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		UserDTO user = null;

		try {
			String sql = USER_SELECT_BASE + " WHERE u.user_id = ? AND u.active_status = 1";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				user = loadUser(rs);
				logger.info("[{}] User found with id: {}", method, id);
			} else {
				logger.warn("[{}] No User found with id: {}", method, id);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error finding User by id: {}", method, id, e);
			throw new DataException("Error finding User by id", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return user;
	}

	// --------------------- FIND ALL ---------------------
	@Override
	public List<UserDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		List<UserDTO> list = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(USER_SELECT_BASE + " WHERE u.active_status = 1");
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(loadUser(rs));
			}
			logger.info("[{}] Total Users found: {}", method, list.size());
		} catch (SQLException e) {
			logger.error("[{}] Error in findAll User", method, e);
			throw new DataException("Error in findAll User", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}
		return list;
	}

	// --------------------- CREATE ---------------------
	@Override
	public boolean create(Connection connection, UserDTO user) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (user == null) {
			logger.warn("[{}] called with null User.", method);
			return false;
		}

		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

		try {
			String sql = "INSERT INTO user (username, first_name, last_name1, last_name2, birth_date, "
					+ "email, password, phone, role_id, address_id, active_status, created_at) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				String encrypted = encryptor.encryptPassword(user.getPassword());
				user.setPassword(encrypted);
			}

			ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			setUserParameters(ps, user, false);
			int affected = ps.executeUpdate();

			if (affected > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					user.setUserId(generatedKeys.getInt(1));
				}
				logger.info("[{}] User created successfully, id: {}", method, user.getUserId());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error creating User", method, e);
			throw new DataException("Error creating User", e);
		} finally {
			JDBCUtils.close(ps, generatedKeys);
		}
		return false;
	}

	// --------------------- UPDATE ---------------------
	@Override
	public boolean update(Connection connection, UserDTO user) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (user == null || user.getUserId() == null) {
			logger.warn("[{}] called with null user or missing id.", method);
			return false;
		}

		PreparedStatement ps = null;
		StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

		try {
			String sql = "UPDATE user SET username = ?, first_name = ?, last_name1 = ?, last_name2 = ?, birth_date = ?, "
					+ "email = ?, password = ?, phone = ?, role_id = ?, address_id = ?, active_status = ?, updated_at = NOW() "
					+ "WHERE user_id = ?";

			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				String encrypted = encryptor.encryptPassword(user.getPassword());
				user.setPassword(encrypted);
			}

			ps = connection.prepareStatement(sql);
			setUserParameters(ps, user, true);
			int rows = ps.executeUpdate();

			if (rows > 0) {
				logger.info("[{}] User updated successfully, id: {}", method, user.getUserId());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error updating User", method, e);
			throw new DataException("Error updating User", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	// --------------------- DELETE ---------------------
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
				logger.info("[delete] Usuario marcado como inactivo correctamente. user_id={}", userId);
			} else {
				logger.warn("[delete] No se encontró el usuario a desactivar. user_id={}", userId);
			}

		} catch (SQLException e) {
			logger.error("[delete] Error al desactivar usuario con id={}: {}", userId, e.getMessage(), e);
			throw new DataException("[UserDAOImpl] Error al desactivar usuario", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return deleted;
	}

	// --------------------- FIND BY CRITERIA ---------------------
	@Override
	public Results<UserDTO> findByCriteria(Connection connection, UserCriteria criteria) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		Results<UserDTO> results = new Results<>();
		List<UserDTO> list = new ArrayList<>();

		int pageNumber = criteria.getPageNumber();
		int pageSize = criteria.getPageSize();
		int offset = (pageNumber - 1) * pageSize;

		StringBuilder sql = new StringBuilder(USER_SELECT_BASE);
		sql.append(" WHERE 1=1");
		sql.append(" AND u.active_status = 1");

		if (criteria.getUserId() != null)
			sql.append(" AND u.user_id = ? ");
		if (criteria.getUsername() != null && !criteria.getUsername().isEmpty())
			sql.append(" AND u.username LIKE ? ");
		if (criteria.getFirstName() != null && !criteria.getFirstName().isEmpty())
			sql.append(" AND u.first_name LIKE ? ");
		if (criteria.getLastName1() != null && !criteria.getLastName1().isEmpty())
			sql.append(" AND u.last_name1 LIKE ? ");
		if (criteria.getLastName2() != null && !criteria.getLastName2().isEmpty())
			sql.append(" AND u.last_name2 LIKE ? ");
		if (criteria.getEmail() != null && !criteria.getEmail().isEmpty())
			sql.append(" AND u.email LIKE ? ");
		if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
			sql.append(" AND u.phone LIKE ? ");
		if (criteria.getRoleId() != null)
			sql.append(" AND u.role_id = ? ");
		if (criteria.getAddressId() != null)
			sql.append(" AND u.address_id = ? ");
		if (criteria.getActiveStatus() != null)
			sql.append(" AND u.active_status = ? ");

		sql.append(" ORDER BY u.first_name, u.last_name1 ");

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			int idx = 1;

			if (criteria.getUserId() != null)
				ps.setInt(idx++, criteria.getUserId());
			if (criteria.getUsername() != null && !criteria.getUsername().isEmpty())
				ps.setString(idx++, "%" + criteria.getUsername() + "%");
			if (criteria.getFirstName() != null && !criteria.getFirstName().isEmpty())
				ps.setString(idx++, "%" + criteria.getFirstName() + "%");
			if (criteria.getLastName1() != null && !criteria.getLastName1().isEmpty())
				ps.setString(idx++, "%" + criteria.getLastName1() + "%");
			if (criteria.getLastName2() != null && !criteria.getLastName2().isEmpty())
				ps.setString(idx++, "%" + criteria.getLastName2() + "%");
			if (criteria.getEmail() != null && !criteria.getEmail().isEmpty())
				ps.setString(idx++, "%" + criteria.getEmail() + "%");
			if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
				ps.setString(idx++, "%" + criteria.getPhone() + "%");
			if (criteria.getRoleId() != null)
				ps.setInt(idx++, criteria.getRoleId());
			if (criteria.getAddressId() != null)
				ps.setInt(idx++, criteria.getAddressId());
			if (criteria.getActiveStatus() != null)
				ps.setBoolean(idx++, criteria.getActiveStatus());

			rs = ps.executeQuery();

			if (rs.absolute(offset + 1)) {
				int count = 0;
				do {
					list.add(loadUser(rs));
					count++;
				} while (count < pageSize && rs.next());
			}

			rs.last();
			int totalRecords = rs.getRow();

			results.setResults(list);
			results.setPageNumber(pageNumber);
			results.setPageSize(pageSize);
			results.setTotalRecords(totalRecords);

			logger.info("[{}] findByCriteria completed: Page {} (Size {}), Total: {}", method, pageNumber, pageSize,
					totalRecords);

		} catch (SQLException e) {
			logger.error("[{}] Error in findByCriteria User", method, e);
			throw new DataException("Error in findByCriteria User", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}
		return results;
	}

	@Override
	public boolean activate(Connection connection, Integer userId) throws DataException {

		PreparedStatement ps = null;
		boolean activated = false;

		String sql = "UPDATE user " + "SET active_status = 1, updated_at = NOW() " + "WHERE user_id = ?";

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);

			int rows = ps.executeUpdate();
			activated = (rows > 0);

			if (activated) {
				logger.info("[activate] Usuario reactivado correctamente. user_id={}", userId);
			} else {
				logger.warn("[activate] No se encontró el usuario para reactivar. user_id={}", userId);
			}

		} catch (SQLException e) {
			logger.error("[activate] Error al reactivar usuario con id={}: {}", userId, e.getMessage(), e);
			throw new DataException("[UserDAOImpl] Error al reactivar usuario", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return activated;
	}

	// --------------------- PRIVATE HELPERS ---------------------
	private UserDTO loadUser(ResultSet rs) throws SQLException {
		UserDTO u = new UserDTO();
		u.setUserId(rs.getInt("user_id"));
		u.setUsername(rs.getString("username"));
		u.setFirstName(rs.getString("first_name"));
		u.setLastName1(rs.getString("last_name1"));
		u.setLastName2(rs.getString("last_name2"));
		u.setBirthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null);
		u.setEmail(rs.getString("email"));
		u.setPhone(rs.getString("phone"));
		u.setRoleId(rs.getInt("role_id"));
		u.setAddressId(rs.getInt("address_id"));
		u.setActiveStatus(rs.getBoolean("active_status"));
		u.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
		u.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
		u.setPassword(null); // No exponer password
		return u;
	}

	private void setUserParameters(PreparedStatement ps, UserDTO u, boolean isUpdate) throws SQLException {
		ps.setString(1, u.getUsername());
		ps.setString(2, u.getFirstName());
		ps.setString(3, u.getLastName1());
		ps.setString(4, u.getLastName2());
		ps.setObject(5, u.getBirthDate());
		ps.setString(6, u.getEmail());
		ps.setString(7, u.getPassword());
		ps.setString(8, u.getPhone());
		ps.setInt(9, u.getRoleId());
		ps.setInt(10, u.getAddressId());
		ps.setBoolean(11, u.getActiveStatus() != null ? u.getActiveStatus() : true);
		if (isUpdate) {
			ps.setInt(12, u.getUserId());
		}
	}
}
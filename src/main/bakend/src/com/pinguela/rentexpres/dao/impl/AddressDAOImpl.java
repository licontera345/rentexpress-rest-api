package com.pinguela.rentexpres.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.AddressDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.AddressDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

public class AddressDAOImpl implements AddressDAO {

	private static final Logger logger = LogManager.getLogger(AddressDAOImpl.class);

	@Override
	public AddressDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		AddressDTO address = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT a.address_id, a.city_id, a.street, a.number, " + "c.city_name, p.province_name "
					+ "FROM address a " + "JOIN city c ON a.city_id = c.city_id "
					+ "JOIN province p ON c.province_id = p.province_id " + "WHERE a.address_id = ?";

			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				address = loadAddress(rs);
				logger.info("[{}] Address found with id: {}", method, id);
			} else {
				logger.warn("[{}] No address found with id: {}", method, id);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error finding address by id: {}", method, id, e);
			throw new DataException("Error finding address by id: " + id, e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return address;
	}

	@Override
	public boolean create(Connection connection, AddressDTO address) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (address == null) {
			logger.warn("[{}] Called with null AddressDTO.", method);
			return false;
		}

		String sql = "INSERT INTO address (city_id, street, number) VALUES (?, ?, ?)";
		PreparedStatement ps = null;
		ResultSet generatedKeys = null;

		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setAddressParameters(ps, address, false);

			int affectedRows = ps.executeUpdate();
			if (affectedRows > 0) {
				generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					address.setId(generatedKeys.getInt(1));
				}
				logger.info("[{}] Address created successfully with id: {}", method, address.getId());
				return true;
			} else {
				logger.warn("[{}] No address created.", method);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error creating address: {}", method, e.getMessage(), e);
			throw new DataException("Error creating address", e);
		} finally {
			JDBCUtils.close(ps, generatedKeys);
		}

		return false;
	}

	@Override
	public boolean update(Connection connection, AddressDTO address) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (address == null || address.getId() == null) {
			logger.warn("[{}] Called with null address or missing id.", method);
			return false;
		}

		String sql = "UPDATE address SET city_id = ?, street = ?, number = ? WHERE address_id = ?";
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql);
			setAddressParameters(ps, address, true);

			int rows = ps.executeUpdate();
			if (rows > 0) {
				logger.info("[{}] Address updated successfully (id: {}).", method, address.getId());
				return true;
			} else {
				logger.warn("[{}] No rows updated for id: {}", method, address.getId());
			}

		} catch (SQLException e) {
			logger.error("[{}] Error updating address: {}", method, e.getMessage(), e);
			throw new DataException("Error updating address", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	@Override
	public boolean delete(Connection connection, AddressDTO address, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (id == null) {
			logger.warn("[{}] Called with null id.", method);
			return false;
		}

		String sql = "DELETE FROM address WHERE address_id = ?";
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			int rows = ps.executeUpdate();
			if (rows > 0) {
				logger.info("[{}] Address deleted (id: {}).", method, id);
				return true;
			} else {
				logger.warn("[{}] No address deleted for id: {}", method, id);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error deleting address: {}", method, e.getMessage(), e);
			throw new DataException("Error deleting address", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	private AddressDTO loadAddress(ResultSet rs) throws SQLException {
		AddressDTO a = new AddressDTO();
		a.setId(rs.getInt("address_id"));
		a.setCityId(rs.getInt("city_id"));
		a.setStreet(rs.getString("street"));
		a.setNumber(rs.getString("number"));
		a.setCityName(rs.getString("city_name"));
		a.setProvinceName(rs.getString("province_name"));
		return a;
	}

	private void setAddressParameters(PreparedStatement ps, AddressDTO address, boolean isUpdate) throws SQLException {
		ps.setInt(1, address.getCityId());
		ps.setString(2, address.getStreet());
		ps.setString(3, address.getNumber());
		if (isUpdate) {
			ps.setInt(4, address.getId());
		}
	}
}

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

import com.pinguela.rentexpres.dao.ProvinceDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ProvinceDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * DAO implementation for the 'province' table. Compatible with Java 1.8 and
 * RentExpress structure.
 */
public class ProvinceDAOImpl implements ProvinceDAO {

	private static final Logger logger = LogManager.getLogger(ProvinceDAOImpl.class);

	private static final String PROVINCE_SELECT_BASE = "SELECT province_id, province_name FROM province";

	@Override
	public ProvinceDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return null;
		}

		String sql = PROVINCE_SELECT_BASE + " WHERE province_id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				logger.info("[{}] Province found with id: {}", method, id);
				return loadProvince(rs);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error finding Province by ID: {}", method, id, e);
			throw new DataException("Error finding Province by ID: " + id, e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return null;
	}

	@Override
	public boolean create(Connection connection, ProvinceDTO province) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (province == null) {
			logger.warn("[{}] called with null province.", method);
			return false;
		}

		String sql = "INSERT INTO province (province_name) VALUES (?)";
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, province.getProvinceName());

			if (ps.executeUpdate() > 0) {
				try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						province.setProvinceId(generatedKeys.getInt(1));
					}
				}
				logger.info("[{}] Province created successfully: {}", method, province.getProvinceName());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error creating Province: {}", method, province.getProvinceName(), e);
			throw new DataException("Error creating Province: " + province.getProvinceName(), e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	@Override
	public boolean update(Connection connection, ProvinceDTO province) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (province == null || province.getProvinceId() == null) {
			logger.warn("[{}] called with null province or id.", method);
			return false;
		}

		String sql = "UPDATE province SET province_name = ? WHERE province_id = ?";
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, province.getProvinceName());
			ps.setInt(2, province.getProvinceId());

			if (ps.executeUpdate() > 0) {
				logger.info("[{}] Province updated successfully, id: {}", method, province.getProvinceId());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error updating Province: {}", method, province.getProvinceId(), e);
			throw new DataException("Error updating Province: " + province.getProvinceId(), e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	@Override
	public boolean delete(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return false;
		}

		String sql = "DELETE FROM province WHERE province_id = ?";
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);

			if (ps.executeUpdate() > 0) {
				logger.info("[{}] Province deleted, id: {}", method, id);
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error deleting Province: {}", method, id, e);
			throw new DataException("Error deleting Province: " + id, e);
		} finally {
			JDBCUtils.close(ps, null);
		}

		return false;
	}

	@Override
	public List<ProvinceDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		List<ProvinceDTO> provinces = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement(PROVINCE_SELECT_BASE + " ORDER BY province_name");
			rs = ps.executeQuery();

			while (rs.next()) {
				provinces.add(loadProvince(rs));
			}

			logger.info("[{}] {} provinces found.", method, provinces.size());

		} catch (SQLException e) {
			logger.error("[{}] Error fetching all Provinces", method, e);
			throw new DataException("Error fetching all Provinces", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return provinces;
	}

	private ProvinceDTO loadProvince(ResultSet rs) throws SQLException {
		ProvinceDTO p = new ProvinceDTO();
		p.setProvinceId(rs.getInt("province_id"));
		p.setProvinceName(rs.getString("province_name"));
		return p;
	}
}

package com.pinguela.rentexpres.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RoleDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.RoleDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

public class RoleDAOImpl implements RoleDAO {

	private static final Logger logger = LogManager.getLogger(RoleDAOImpl.class);

	@Override
	public RoleDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		RoleDTO role = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT role_id, role_name FROM role WHERE role_id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				role = loadRole(rs);
				logger.info("[{}] Role found with id: {}", method, id);
			} else {
				logger.warn("[{}] No Role found with id: {}", method, id);
			}

		} catch (SQLException e) {
			logger.error("[{}] Error finding Role by id: {}", method, id, e);
			throw new DataException("Error finding Role by id: " + id, e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return role;
	}

	@Override
	public List<RoleDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		List<RoleDTO> list = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT role_id, role_name FROM role";
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				list.add(loadRole(rs));
			}

			logger.info("[{}] Total Roles found: {}", method, list.size());

		} catch (SQLException e) {
			logger.error("[{}] Error retrieving all Roles", method, e);
			throw new DataException("Error retrieving all Roles", e);
		} finally {
			JDBCUtils.close(ps, rs);
		}

		return list;
	}

	private RoleDTO loadRole(ResultSet rs) throws SQLException {
		RoleDTO r = new RoleDTO();
		r.setRoleId(rs.getInt("role_id"));
		r.setRoleName(rs.getString("role_name"));
		return r;
	}
}

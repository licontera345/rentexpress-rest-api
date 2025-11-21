package com.pinguela.rentexpres.dao.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.VehicleCategoryDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;

/**
 * DAO i18n para vehicle_category. DDL: - vehicle_category(category_id,
 * category_name) - vehicle_category_language(category_id, language_id,
 * translated_name) - language(language_id, iso_code UNIQUE)
 */
public class VehicleCategoryDAOImpl implements VehicleCategoryDAO {

	private static final Logger logger = LogManager.getLogger(VehicleCategoryDAOImpl.class);

	// Resolvemos primero el idioma y luego unimos la traducción. Si isoCode==null →
	// no hay match → fallback.
	private static final String SELECT_I18N = "SELECT vc.category_id, "
			+ "       COALESCE(vcl.translated_name, vc.category_name) AS category_name " + "FROM vehicle_category vc "
			+ "LEFT JOIN language l ON l.iso_code = ? " + "LEFT JOIN vehicle_category_language vcl "
			+ "  ON vcl.category_id = vc.category_id AND vcl.language_id = l.language_id ";

	@Override
	public VehicleCategoryDTO findById(Connection c, Integer id, String isoCode) throws DataException {
		if (id == null)
			return null;
		final String sql = SELECT_I18N + "WHERE vc.category_id = ?";
		try (PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, normalizeIso(isoCode)); // null → no match → COALESCE usa base
			ps.setInt(2, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? load(rs) : null;
			}
		} catch (SQLException e) {
			logger.error("[VehicleCategoryDAO.findById] id={}, iso={}", id, isoCode, e);
			throw new DataException("Error finding VehicleCategory by id (i18n)", e);
		}
	}

	@Override
	public List<VehicleCategoryDTO> findAll(Connection c, String isoCode) throws DataException {
		final String sql = SELECT_I18N + "ORDER BY vc.category_id";
		List<VehicleCategoryDTO> out = new ArrayList<>();
		try (PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, normalizeIso(isoCode));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					out.add(load(rs));
			}
			return out;
		} catch (SQLException e) {
			logger.error("[VehicleCategoryDAO.findAll] iso={}", isoCode, e);
			throw new DataException("Error listing VehicleCategory (i18n)", e);
		}
	}

	// ---- helpers ----
	private VehicleCategoryDTO load(ResultSet rs) throws SQLException {
		VehicleCategoryDTO v = new VehicleCategoryDTO();
		v.setCategoryId(rs.getInt("category_id"));
		v.setCategoryName(rs.getString("category_name"));
		return v;
	}

	// "es-ES" → "es"
	private String normalizeIso(String iso) {
		if (iso == null || iso.isEmpty())
			return null;
		String v = iso.trim();
		int p = v.indexOf('-');
		return (p > 0 ? v.substring(0, p) : v).toLowerCase();
	}
}

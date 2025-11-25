package com.pinguela.rentexpres.dao.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RentalStatusDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.RentalStatusDTO;

/**
 * DAO i18n para rental_status.
 * Tablas (DDL):
 *  - rental_status(rental_status_id, status_name)
 *  - rental_status_language(rental_status_id, language_id, translated_name)
 *  - language(language_id, iso_code UNIQUE)
 */
public class RentalStatusDAOImpl implements RentalStatusDAO {

    private static final Logger logger = LogManager.getLogger(RentalStatusDAOImpl.class);

    // ¡OJO al orden del JOIN! Primero resolvemos el idioma y luego unimos la traducción de ese idioma.
    private static final String SELECT_I18N =
        "SELECT rs.rental_status_id, " +
        "       COALESCE(rsl.translated_name, rs.status_name) AS status_name " +
        "FROM rental_status rs " +
        "LEFT JOIN language l ON l.iso_code = ? " +
        "LEFT JOIN rental_status_language rsl " +
        "  ON rsl.rental_status_id = rs.rental_status_id AND rsl.language_id = l.language_id ";

    @Override
    public RentalStatusDTO findById(Connection connection, Integer id, String isoCode) throws DataException {
        if (id == null) return null;
        final String sql = SELECT_I18N + "WHERE rs.rental_status_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode)); // null → no hay match de idioma → fallback a base
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? load(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("[RentalStatusDAO.findById] id={}, iso={}", id, isoCode, e);
            throw new DataException("Error finding RentalStatus by id (i18n)", e);
        }
    }

    @Override
    public List<RentalStatusDTO> findAll(Connection connection, String isoCode) throws DataException {
        final String sql = SELECT_I18N + "ORDER BY rs.rental_status_id";
        List<RentalStatusDTO> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(load(rs));
            }
            return out;
        } catch (SQLException e) {
            logger.error("[RentalStatusDAO.findAll] iso={}", isoCode, e);
            throw new DataException("Error listing RentalStatus (i18n)", e);
        }
    }

    // --------- helpers ---------
    private RentalStatusDTO load(ResultSet rs) throws SQLException {
        RentalStatusDTO dto = new RentalStatusDTO();
        dto.setRentalStatusId(rs.getInt("rental_status_id"));
        dto.setStatusName(rs.getString("status_name"));
        return dto;
    }

    // "es-ES" → "es"
    private String normalizeIso(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        String v = iso.trim();
        int p = v.indexOf('-');
        return (p > 0 ? v.substring(0, p) : v).toLowerCase();
    }
}

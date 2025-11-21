package com.pinguela.rentexpres.dao.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.VehicleStatusDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.VehicleStatusDTO;

/**
 * DAO i18n para vehicle_status.
 * Tablas esperadas (según DDL):
 *  - vehicle_status(vehicle_status_id, status_name)
 *  - vehicle_status_language(vehicle_status_id, language_id, translated_name)
 *  - language(language_id, iso_code UNIQUE)
 */
public class VehicleStatusDAOImpl implements VehicleStatusDAO {

    private static final Logger logger = LogManager.getLogger(VehicleStatusDAOImpl.class);

    // Primero resolvemos el idioma (language) y luego unimos su traducción.
    private static final String SELECT_I18N =
        "SELECT vs.vehicle_status_id, " +
        "       COALESCE(vsl.translated_name, vs.status_name) AS status_name " +
        "FROM vehicle_status vs " +
        "LEFT JOIN language l ON l.iso_code = ? " +
        "LEFT JOIN vehicle_status_language vsl " +
        "  ON vsl.vehicle_status_id = vs.vehicle_status_id AND vsl.language_id = l.language_id ";

    @Override
    public VehicleStatusDTO findById(Connection connection, Integer id, String isoCode) throws DataException {
        if (id == null) return null;
        final String sql = SELECT_I18N + "WHERE vs.vehicle_status_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode));     
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? load(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("[VehicleStatusDAO.findById] id={}, iso={}", id, isoCode, e);
            throw new DataException("Error finding VehicleStatus by id (i18n)", e);
        }
    }

    @Override
    public List<VehicleStatusDTO> findAll(Connection connection, String isoCode) throws DataException {
        final String sql = SELECT_I18N + "ORDER BY vs.vehicle_status_id";
        List<VehicleStatusDTO> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(load(rs));
            }
            return out;
        } catch (SQLException e) {
            logger.error("[VehicleStatusDAO.findAll] iso={}", isoCode, e);
            throw new DataException("Error listing VehicleStatus (i18n)", e);
        }
    }


    private VehicleStatusDTO load(ResultSet rs) throws SQLException {
        VehicleStatusDTO dto = new VehicleStatusDTO();
        dto.setVehicleStatusId(rs.getInt("vehicle_status_id"));
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

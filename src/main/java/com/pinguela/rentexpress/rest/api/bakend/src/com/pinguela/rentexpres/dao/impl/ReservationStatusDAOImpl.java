package com.pinguela.rentexpres.dao.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.ReservationStatusDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ReservationStatusDTO;

public class ReservationStatusDAOImpl implements ReservationStatusDAO {

    private static final Logger logger = LogManager.getLogger(ReservationStatusDAOImpl.class);

    // LEFT JOIN con filtro por ISO en la condición del JOIN (no en WHERE) → permite fallback
    private static final String SELECT_I18N =
        "SELECT rs.reservation_status_id, " +
        "       COALESCE(rsl.translated_name, rs.status_name) AS status_name " +
        "FROM reservation_status rs " +
        "LEFT JOIN reservation_status_language rsl " +
        "       ON rsl.reservation_status_id = rs.reservation_status_id " +
        "LEFT JOIN language l " +
        "       ON l.language_id = rsl.language_id AND l.iso_code = ? ";

    @Override
    public ReservationStatusDTO findById(Connection c, Integer id, String isoCode) throws DataException {
        final String sql = SELECT_I18N + "WHERE rs.reservation_status_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode));  // si llega null → JOIN no matchea → fallback
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? load(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("[ReservationStatusDAO.findById] id={}, iso={}", id, isoCode, e);
            throw new DataException("Error finding ReservationStatus by id (i18n)", e);
        }
    }

    @Override
    public List<ReservationStatusDTO> findAll(Connection c, String isoCode) throws DataException {
        final String sql = SELECT_I18N + "ORDER BY rs.reservation_status_id";
        List<ReservationStatusDTO> out = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normalizeIso(isoCode));  // null → base.status_name
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(load(rs));
            }
            return out;
        } catch (SQLException e) {
            logger.error("[ReservationStatusDAO.findAll] iso={}", isoCode, e);
            throw new DataException("Error listing ReservationStatus (i18n)", e);
        }
    }

    // ---- helpers ----
    private ReservationStatusDTO load(ResultSet rs) throws SQLException {
        ReservationStatusDTO dto = new ReservationStatusDTO();
        dto.setReservationStatusId(rs.getInt("reservation_status_id"));
        dto.setStatusName(rs.getString("status_name"));
        return dto;
    }

    private String normalizeIso(String iso) {
        if (iso == null || iso.isEmpty()) return null; // así forzamos fallback si no viene ISO
        String v = iso.trim();
        int p = v.indexOf('-');
        return (p > 0 ? v.substring(0, p) : v).toLowerCase();
    }
}

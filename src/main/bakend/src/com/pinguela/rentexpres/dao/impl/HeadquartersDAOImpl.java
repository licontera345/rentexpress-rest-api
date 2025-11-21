package com.pinguela.rentexpres.dao.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.HeadquartersDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * DAO implementation for the 'headquarters' table.
 * Compatible with Java 1.8 and RentExpress pattern.
 */
public class HeadquartersDAOImpl implements HeadquartersDAO {

    private static final Logger logger = LogManager.getLogger(HeadquartersDAOImpl.class);

    private static final String HQ_SELECT_BASE = 
        "SELECT headquarters_id, name, phone, email, address_id, created_at, updated_at FROM headquarters";

    @Override
    public HeadquartersDTO findById(Connection connection, Integer id) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();

        if (id == null) {
            logger.warn("[{}] called with null id.", method);
            return null;
        }

        String sql = HQ_SELECT_BASE + " WHERE headquarters_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("[{}] Headquarters found with id: {}", method, id);
                return loadHeadquarters(rs);
            }

        } catch (SQLException e) {
            logger.error("[{}] Error finding Headquarters by ID: {}", method, id, e);
            throw new DataException("Error finding Headquarters by ID: " + id, e);
        } finally {
            JDBCUtils.close(ps, rs);
        }

        return null;
    }
    @Override
    public boolean create(Connection connection, HeadquartersDTO headquarters) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();

        if (headquarters == null) {
            logger.warn("[{}] called with null headquarters.", method);
            return false;
        }

        String sql = "INSERT INTO headquarters (name, phone, email, address_id) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setHeadquartersParameters(ps, headquarters, false);

            if (ps.executeUpdate() > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        headquarters.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("[{}] Headquarters created successfully: {}", method, headquarters.getName());
                return true;
            }

        } catch (SQLException e) {
            logger.error("[{}] Error creating Headquarters: {}", method, headquarters.getName(), e);
            throw new DataException("Error creating Headquarters: " + headquarters.getName(), e);
        } finally {
            JDBCUtils.close(ps, null);
        }

        return false;
    }

    @Override
    public boolean update(Connection connection, HeadquartersDTO headquarters) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();

        if (headquarters == null || headquarters.getId() == null) {
            logger.warn("[{}] called with null headquarters or id.", method);
            return false;
        }

        String sql = "UPDATE headquarters SET name = ?, phone = ?, email = ?, address_id = ? WHERE headquarters_id = ?";
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(sql);
            setHeadquartersParameters(ps, headquarters, true);

            if (ps.executeUpdate() > 0) {
                logger.info("[{}] Headquarters updated successfully, id: {}", method, headquarters.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.error("[{}] Error updating Headquarters: {}", method, headquarters.getId(), e);
            throw new DataException("Error updating Headquarters: " + headquarters.getId(), e);
        } finally {
            JDBCUtils.close(ps, null);
        }

        return false;
    }
    
    @Override
    public boolean delete(Connection connection, Integer id) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();

        if (id == null) {
            logger.warn("[{}] called with null id.", method);
            return false;
        }

        String sql = "DELETE FROM headquarters WHERE headquarters_id = ?";
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                logger.info("[{}] Headquarters deleted, id: {}", method, id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("[{}] Error deleting Headquarters: {}", method, id, e);
            throw new DataException("Error deleting Headquarters: " + id, e);
        } finally {
            JDBCUtils.close(ps, null);
        }

        return false;
    }

    @Override
    public List<HeadquartersDTO> findAll(Connection connection) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();

        List<HeadquartersDTO> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(HQ_SELECT_BASE + " ORDER BY headquarters_id");
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(loadHeadquarters(rs));
            }

            logger.info("[{}] {} headquarters found.", method, list.size());

        } catch (SQLException e) {
            logger.error("[{}] Error fetching all Headquarters", method, e);
            throw new DataException("Error fetching all Headquarters", e);
        } finally {
            JDBCUtils.close(ps, rs);
        }

        return list;
    }

    private HeadquartersDTO loadHeadquarters(ResultSet rs) throws SQLException {
        HeadquartersDTO h = new HeadquartersDTO();
        h.setId(rs.getInt("headquarters_id"));
        h.setName(rs.getString("name"));
        h.setPhone(rs.getString("phone"));
        h.setEmail(rs.getString("email"));
        h.setAddressId(rs.getInt("address_id"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            h.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            h.setUpdatedAt(updated.toLocalDateTime());
        }

        return h;
    }


    private void setHeadquartersParameters(PreparedStatement ps, HeadquartersDTO h, boolean isUpdate)
            throws SQLException {
        ps.setString(1, h.getName());
        ps.setString(2, h.getPhone());
        ps.setString(3, h.getEmail());
        ps.setInt(4, h.getAddressId());
        if (isUpdate) {
            ps.setInt(5, h.getId());
        }
    }
}

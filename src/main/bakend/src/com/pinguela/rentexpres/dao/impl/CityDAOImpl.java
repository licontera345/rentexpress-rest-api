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

import com.pinguela.rentexpres.dao.CityDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.CityDTO;

public class CityDAOImpl implements CityDAO {

    private static final Logger logger = LogManager.getLogger(CityDAOImpl.class);

    private static final String BASE_SELECT = "SELECT city_id, city_name, province_id FROM city";

    @Override
    public CityDTO findById(Connection c, Integer id) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        String sql = BASE_SELECT + " WHERE city_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CityDTO city = loadCity(rs);
                    logger.info("[{}] City found with id: {}", method, id);
                    return city;
                } else {
                    logger.warn("[{}] No city found with id: {}", method, id);
                    return null;
                }
            }
        } catch (SQLException ex) {
            logger.error("[{}] Error finding city by id: {}", method, id, ex);
            throw new DataException("Error finding city by id", ex);
        }
    }

    @Override
    public List<CityDTO> findAll(Connection c) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        List<CityDTO> cities = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(BASE_SELECT);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cities.add(loadCity(rs));
            }
            logger.info("[{}] {} cities found.", method, cities.size());
            return cities;

        } catch (SQLException ex) {
            logger.error("[{}] Error listing cities", method, ex);
            throw new DataException("Error listing cities", ex);
        }
    }

    @Override
    public List<CityDTO> findByProvinceId(Connection c, Integer provinceId) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        String sql = BASE_SELECT + " WHERE province_id = ? ORDER BY city_name";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, provinceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CityDTO> cities = new ArrayList<>();
                while (rs.next()) {
                    cities.add(loadCity(rs));
                }
                logger.info("[{}] {} cities found for province id: {}", method, cities.size(), provinceId);
                return cities;
            }
        } catch (SQLException ex) {
            logger.error("[{}] Error finding cities by province id: {}", method, provinceId, ex);
            throw new DataException("Error finding cities by province id", ex);
        }
    }

    @Override
    public boolean create(Connection c, CityDTO city) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        String sql = "INSERT INTO city (city_name, province_id) VALUES (?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setCityParameters(ps, city, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        city.setId(keys.getInt(1));
                    }
                }
                logger.info("[{}] City created successfully with id: {}", method, city.getId());
                return true;
            } else {
                logger.warn("[{}] No city created.", method);
                return false;
            }
        } catch (SQLException ex) {
            logger.error("[{}] Error creating city: {}", method, ex.getMessage(), ex);
            throw new DataException("Error creating city", ex);
        }
    }

    @Override
    public boolean update(Connection c, CityDTO city) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        String sql = "UPDATE city SET city_name = ?, province_id = ? WHERE city_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setCityParameters(ps, city, true);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("[{}] City updated successfully (id: {}).", method, city.getId());
                return true;
            } else {
                logger.warn("[{}] No city updated for id: {}", method, city.getId());
                return false;
            }
        } catch (SQLException ex) {
            logger.error("[{}] Error updating city: {}", method, ex.getMessage(), ex);
            throw new DataException("Error updating city", ex);
        }
    }

    @Override
    public boolean delete(Connection c, CityDTO city) throws DataException {
        final String method = new Object(){}.getClass().getEnclosingMethod().getName();
        String sql = "DELETE FROM city WHERE city_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, city.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("[{}] City deleted successfully (id: {}).", method, city.getId());
                return true;
            } else {
                logger.warn("[{}] No city deleted for id: {}", method, city.getId());
                return false;
            }
        } catch (SQLException ex) {
            logger.error("[{}] Error deleting city: {}", method, ex.getMessage(), ex);
            throw new DataException("Error deleting city", ex);
        }
    }

    // ---------- Private helpers ----------
    private CityDTO loadCity(ResultSet rs) throws SQLException {
        CityDTO c = new CityDTO();
        c.setId(rs.getInt("city_id"));
        c.setCityName(rs.getString("city_name"));
        c.setProvinceId(rs.getInt("province_id"));
        return c;
    }

    private void setCityParameters(PreparedStatement ps, CityDTO city, boolean isUpdate) throws SQLException {
        ps.setString(1, city.getCityName());
        ps.setInt(2, city.getProvinceId());
        if (isUpdate) {
            ps.setInt(3, city.getId());
        }
    }
}

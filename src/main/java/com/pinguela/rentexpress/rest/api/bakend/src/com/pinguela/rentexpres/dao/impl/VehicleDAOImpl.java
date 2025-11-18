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

import com.pinguela.rentexpres.dao.VehicleDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.util.JDBCUtils;

public class VehicleDAOImpl implements VehicleDAO {

	private static final Logger logger = LogManager.getLogger(VehicleDAOImpl.class);

	private static final String VEHICLE_SELECT_BASE = String.join(" ",
			"SELECT v.vehicle_id, v.brand, v.model, v.manufacture_year, v.daily_price,",
			"v.license_plate, v.vin_number, v.current_mileage, v.vehicle_status_id,",
			"v.category_id, v.current_headquarters_id, v.created_at, v.updated_at,",
			"vc.category_name AS category_name", "FROM vehicle v",
			"LEFT JOIN vehicle_category vc ON v.category_id = vc.category_id");

	// ---------------- FIND BY ID ----------------
	@Override
	public VehicleDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		VehicleDTO vehicle = null;

		try (PreparedStatement ps = connection.prepareStatement(VEHICLE_SELECT_BASE + " WHERE v.vehicle_id = ?")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					vehicle = loadVehicle(rs);
					logger.info("[{}] Vehicle found with id: {}", method, id);
				} else {
					logger.warn("[{}] No Vehicle found with id: {}", method, id);
				}
			}
		} catch (SQLException e) {
			logger.error("[{}] Error finding Vehicle by id: {}", method, id, e);
			throw new DataException("Error finding Vehicle by id", e);
		}

		return vehicle;
	}

	// ---------------- FIND ALL ----------------
	@Override
	public List<VehicleDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		List<VehicleDTO> list = new ArrayList<>();

		try (PreparedStatement ps = connection.prepareStatement(VEHICLE_SELECT_BASE);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(loadVehicle(rs));
			}
			logger.info("[{}] Total vehicles found: {}", method, list.size());
		} catch (SQLException e) {
			logger.error("[{}] Error retrieving all vehicles", method, e);
			throw new DataException("Error retrieving all vehicles", e);
		}

		return list;
	}

	// ---------------- CREATE ----------------
	@Override
	public boolean create(Connection connection, VehicleDTO v) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (v == null) {
			logger.warn("[{}] called with null Vehicle.", method);
			return false;
		}

		String sql = "INSERT INTO vehicle (brand, model, manufacture_year, daily_price, "
				+ "license_plate, vin_number, current_mileage, vehicle_status_id, "
				+ "category_id, current_headquarters_id, created_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			setVehicleParameters(ps, v, false);
			int affected = ps.executeUpdate();

			if (affected > 0) {
				try (ResultSet keys = ps.getGeneratedKeys()) {
					if (keys.next()) {
						v.setVehicleId(keys.getInt(1));
					}
				}
				logger.info("[{}] Vehicle created successfully, id: {}", method, v.getVehicleId());
				return true;
			}

		} catch (SQLException e) {
			logger.error("[{}] Error creating vehicle", method, e);
			throw new DataException("Error creating vehicle", e);
		}

		return false;
	}

	// ---------------- UPDATE ----------------
	@Override
	public boolean update(Connection connection, VehicleDTO v) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (v == null || v.getVehicleId() == null) {
			logger.warn("[{}] called with null vehicle or missing id.", method);
			return false;
		}

		String sql = "UPDATE vehicle SET brand=?, model=?, manufacture_year=?, daily_price=?, "
				+ "license_plate=?, vin_number=?, current_mileage=?, vehicle_status_id=?, "
				+ "category_id=?, current_headquarters_id=?, updated_at=NOW() " + "WHERE vehicle_id=?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			setVehicleParameters(ps, v, true);
			int rows = ps.executeUpdate();
			if (rows > 0) {
				logger.info("[{}] Vehicle updated successfully, id: {}", method, v.getVehicleId());
				return true;
			}
		} catch (SQLException e) {
			logger.error("[{}] Error updating vehicle", method, e);
			throw new DataException("Error updating vehicle", e);
		}

		return false;
	}

	// ---------------- DELETE ----------------
	@Override
	public boolean delete(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		String sql = "DELETE FROM vehicle WHERE vehicle_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			int rows = ps.executeUpdate();
			if (rows > 0) {
				logger.info("[{}] Vehicle deleted, id: {}", method, id);
				return true;
			}
		} catch (SQLException e) {
			logger.error("[{}] Error deleting vehicle", method, e);
			throw new DataException("Error deleting vehicle", e);
		}
		return false;
	}

	// ---------------- FIND BY CRITERIA ----------------
	@Override
	public Results<VehicleDTO> findByCriteria(Connection connection, VehicleCriteria criteria) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		Results<VehicleDTO> results = new Results<>();
		List<VehicleDTO> list = new ArrayList<>();

		int pageNumber = criteria.getPageNumber();
		int pageSize = criteria.getPageSize();
		int offset = (pageNumber - 1) * pageSize;

		StringBuilder sql = new StringBuilder(VEHICLE_SELECT_BASE);
		sql.append(" WHERE 1=1 ");

		if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
			sql.append(" AND v.brand LIKE ? ");
		if (criteria.getModel() != null && !criteria.getModel().isEmpty())
			sql.append(" AND v.model LIKE ? ");
		if (criteria.getManufactureYearFrom() != null)
			sql.append(" AND v.manufacture_year >= ? ");
		if (criteria.getManufactureYearTo() != null)
			sql.append(" AND v.manufacture_year <= ? ");
		if (criteria.getDailyPriceMin() != null)
			sql.append(" AND v.daily_price >= ? ");
		if (criteria.getDailyPriceMax() != null)
			sql.append(" AND v.daily_price <= ? ");
		if (criteria.getVehicleStatusId() != null)
			sql.append(" AND v.vehicle_status_id = ? ");
		if (criteria.getCategoryId() != null)
			sql.append(" AND v.category_id = ? ");
		if (criteria.getCurrentHeadquartersId() != null)
			sql.append(" AND v.current_headquarters_id = ? ");

		sql.append(" ORDER BY v.brand, v.model ");

		try (PreparedStatement ps = connection.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY)) {

			int idx = 1;
			if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
				ps.setString(idx++, "%" + criteria.getBrand() + "%");
			if (criteria.getModel() != null && !criteria.getModel().isEmpty())
				ps.setString(idx++, "%" + criteria.getModel() + "%");
			if (criteria.getManufactureYearFrom() != null)
				ps.setInt(idx++, criteria.getManufactureYearFrom());
			if (criteria.getManufactureYearTo() != null)
				ps.setInt(idx++, criteria.getManufactureYearTo());
			if (criteria.getDailyPriceMin() != null)
				ps.setBigDecimal(idx++, criteria.getDailyPriceMin());
			if (criteria.getDailyPriceMax() != null)
				ps.setBigDecimal(idx++, criteria.getDailyPriceMax());
			if (criteria.getVehicleStatusId() != null)
				ps.setInt(idx++, criteria.getVehicleStatusId());
			if (criteria.getCategoryId() != null)
				ps.setInt(idx++, criteria.getCategoryId());
			if (criteria.getCurrentHeadquartersId() != null)
				ps.setInt(idx++, criteria.getCurrentHeadquartersId());

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.absolute(offset + 1)) {
					int count = 0;
					do {
						list.add(loadVehicle(rs));
						count++;
					} while (count < pageSize && rs.next());
				}
				rs.last();
				results.setTotalRecords(rs.getRow());
			}

			results.setResults(list);
			results.setPageNumber(pageNumber);
			results.setPageSize(pageSize);
			logger.info("[{}] findByCriteria completed: Page {} (Size {}), Total: {}", method, pageNumber, pageSize,
					results.getTotalRecords());

		} catch (SQLException e) {
			logger.error("[{}] Error in findByCriteria Vehicle", method, e);
			throw new DataException("Error in findByCriteria Vehicle", e);
		}

		return results;
	}

	// ---------------- PRIVATE HELPERS ----------------
	private VehicleDTO loadVehicle(ResultSet rs) throws SQLException {
		VehicleDTO v = new VehicleDTO();
		v.setVehicleId(rs.getInt("vehicle_id"));
		v.setBrand(rs.getString("brand"));
		v.setModel(rs.getString("model"));
		v.setManufactureYear(rs.getInt("manufacture_year"));
		v.setDailyPrice(rs.getBigDecimal("daily_price"));
		v.setLicensePlate(rs.getString("license_plate"));
		v.setVinNumber(rs.getString("vin_number"));
		v.setCurrentMileage(rs.getInt("current_mileage"));
		v.setVehicleStatusId(rs.getInt("vehicle_status_id"));
		v.setCategoryId(rs.getInt("category_id"));
		v.setCurrentHeadquartersId(rs.getInt("current_headquarters_id"));
		v.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
		v.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
		return v;
	}

	private void setVehicleParameters(PreparedStatement ps, VehicleDTO v, boolean isUpdate) throws SQLException {
		ps.setString(1, v.getBrand());
		ps.setString(2, v.getModel());
		ps.setInt(3, v.getManufactureYear());
		ps.setBigDecimal(4, v.getDailyPrice());
		ps.setString(5, v.getLicensePlate());
		ps.setString(6, v.getVinNumber());
		ps.setInt(7, v.getCurrentMileage());
		ps.setInt(8, v.getVehicleStatusId());
		ps.setInt(9, v.getCategoryId());
		ps.setInt(10, v.getCurrentHeadquartersId());
		if (isUpdate)
			ps.setInt(11, v.getVehicleId());
	}

	@Override
	public void updateStatus(Connection connection, Integer vehicleId, Integer statusId) throws DataException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("UPDATE vehicle SET vehicle_status_id = ? WHERE vehicle_id = ?");
			ps.setInt(1, statusId);
			ps.setInt(2, vehicleId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DataException("Error actualizando estado del vehículo", e);
		} finally {
			JDBCUtils.close(ps, null);
		}

	}
}
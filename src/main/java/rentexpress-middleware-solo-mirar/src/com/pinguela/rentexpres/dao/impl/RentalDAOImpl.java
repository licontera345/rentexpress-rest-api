package com.pinguela.rentexpres.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RentalDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.util.JDBCUtils;

public class RentalDAOImpl implements RentalDAO {

	private static final Logger logger = LogManager.getLogger(RentalDAOImpl.class);

	private static final String SELECT_BASE = "SELECT r.rental_id, r.reservation_id, r.start_date_effective, r.end_date_effective, "
			+ "r.initial_km, r.final_km, r.total_cost, r.rental_status_id, s.status_name AS rentalStatusName, "
			+ "res.vehicle_id, v.license_plate, v.brand, v.model, "
			+ "res.user_id, u.first_name AS userFirstName, u.last_name1 AS userLastName1, u.phone, "
			+ "r.pickup_headquarters_id, h1.name AS pickupHeadquarters, "
			+ "r.return_headquarters_id, h2.name AS returnHeadquarters, " + "r.created_at, r.updated_at "
			+ "FROM rental r " + "INNER JOIN rental_status s ON r.rental_status_id = s.rental_status_id "
			+ "INNER JOIN reservation res ON r.reservation_id = res.reservation_id "
			+ "INNER JOIN vehicle v ON res.vehicle_id = v.vehicle_id " + "INNER JOIN user u ON res.user_id = u.user_id "
			+ "INNER JOIN headquarters h1 ON r.pickup_headquarters_id = h1.headquarters_id "
			+ "INNER JOIN headquarters h2 ON r.return_headquarters_id = h2.headquarters_id";

	@Override
	public RentalDTO findById(Connection connection, Integer id) throws DataException {
		if (id == null) {
			logger.warn("findById called with null id.");
			return null;
		}
		String sql = SELECT_BASE + " WHERE r.rental_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					logger.info("Rental found, id: {}", id);
					return loadRental(rs);
				} else {
					logger.warn("No rental found with id: {}", id);
				}
			}
		} catch (SQLException e) {
			logger.error("Error in findById for rental id: {}", id, e);
			throw new DataException("Error finding rental by ID", e);
		}
		return null;
	}

	@Override
	public List<RentalDTO> findAll(Connection connection) throws DataException {
		List<RentalDTO> list = new ArrayList<>();
		String sql = SELECT_BASE + " ORDER BY r.start_date_effective DESC";
		try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(loadRental(rs));
			}
			logger.info("Total rentals found: {}", list.size());
		} catch (SQLException e) {
			logger.error("Error in findAll rental", e);
			throw new DataException("Error in findAll rental", e);
		}
		return list;
	}

	@Override
	public boolean create(Connection connection, RentalDTO rental) throws DataException {
		if (rental == null) {
			logger.warn("create called with null rental.");
			return false;
		}
		String sql = "INSERT INTO rental (reservation_id, start_date_effective, end_date_effective, initial_km, final_km, rental_status_id, total_cost, pickup_headquarters_id, return_headquarters_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			setRentalParameters(ps, rental, false);
			if (ps.executeUpdate() > 0) {
				try (ResultSet gen = ps.getGeneratedKeys()) {
					if (gen.next()) {
						rental.setRentalId(gen.getInt(1));
					}
				}
				logger.info("Rental created successfully. New ID: {}", rental.getRentalId());
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error creating rental", e);
			throw new DataException("Error creating rental", e);
		}
		return false;
	}

	@Override
	public boolean update(Connection connection, RentalDTO rental) throws DataException {
		if (rental == null || rental.getRentalId() == null) {
			logger.warn("update called with null rental or null id.");
			return false;
		}
		String sql = "UPDATE rental SET start_date_effective = ?, end_date_effective = ?, initial_km = ?, final_km = ?, rental_status_id = ?, total_cost = ?, pickup_headquarters_id = ?, return_headquarters_id = ? "
				+ "WHERE rental_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			setRentalParameters(ps, rental, true);
			if (ps.executeUpdate() > 0) {
				logger.info("Rental updated successfully. ID: {}", rental.getRentalId());
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error updating rental", e);
			throw new DataException("Error updating rental", e);
		}
		return false;
	}

	@Override
	public boolean delete(Connection connection, Integer id) throws DataException {
		if (id == null) {
			logger.warn("delete called with null id.");
			return false;
		}
		String sql = "DELETE FROM rental WHERE rental_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			if (ps.executeUpdate() > 0) {
				logger.info("Rental deleted. ID: {}", id);
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error deleting rental with ID: {}", id, e);
			throw new DataException("Error deleting rental with ID: " + id, e);
		}
		return false;
	}

	@Override
	public Results<RentalDTO> findByCriteria(Connection connection, RentalCriteria criteria) throws DataException {
		Results<RentalDTO> results = new Results<>();
		List<RentalDTO> list = new ArrayList<>();
		int pageNumber = criteria.getPageNumber();
		int pageSize = criteria.getPageSize();
		int offset = (pageNumber - 1) * pageSize;

		StringBuilder sql = new StringBuilder(SELECT_BASE);
		sql.append(" WHERE 1=1 ");
		if (criteria.getRentalId() != null)
			sql.append(" AND r.rental_id = ? ");
		if (criteria.getReservationId() != null)
			sql.append(" AND r.reservation_id = ? ");
		if (criteria.getStartDateEffective() != null)
			sql.append(" AND r.start_date_effective >= ? ");
		if (criteria.getEndDateEffective() != null)
			sql.append(" AND r.end_date_effective <= ? ");
		if (criteria.getInitialKm() != null)
			sql.append(" AND r.initial_km >= ? ");
		if (criteria.getFinalKm() != null)
			sql.append(" AND r.final_km <= ? ");
		if (criteria.getRentalStatusId() != null)
			sql.append(" AND r.rental_status_id = ? ");
		if (criteria.getTotalCost() != null)
			sql.append(" AND r.total_cost = ? ");

		if (criteria.getUserId() != null)
			sql.append(" AND res.user_id = ? ");
		if (criteria.getUserFirstName() != null && !criteria.getUserFirstName().isEmpty())
			sql.append(" AND u.first_name LIKE ? ");
		if (criteria.getUserLastName1() != null && !criteria.getUserLastName1().isEmpty())
			sql.append(" AND u.last_name1 LIKE ? ");
		if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
			sql.append(" AND u.phone LIKE ? ");

		if (criteria.getVehicleId() != null)
			sql.append(" AND res.vehicle_id = ? ");
		if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
			sql.append(" AND v.brand LIKE ? ");
		if (criteria.getLicensePlate() != null && !criteria.getLicensePlate().isEmpty())
			sql.append(" AND v.license_plate LIKE ? ");
		if (criteria.getModel() != null && !criteria.getModel().isEmpty())
			sql.append(" AND v.model LIKE ? ");

		sql.append(" ORDER BY r.start_date_effective DESC LIMIT ? OFFSET ?");

		try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
			int idx = 1;
			if (criteria.getRentalId() != null)
				ps.setInt(idx++, criteria.getRentalId());
			if (criteria.getReservationId() != null)
				ps.setInt(idx++, criteria.getReservationId());
			if (criteria.getStartDateEffective() != null)
				ps.setTimestamp(idx++, Timestamp.valueOf(criteria.getStartDateEffective()));
			if (criteria.getEndDateEffective() != null)
				ps.setTimestamp(idx++, Timestamp.valueOf(criteria.getEndDateEffective()));
			if (criteria.getInitialKm() != null)
				ps.setInt(idx++, criteria.getInitialKm());
			if (criteria.getFinalKm() != null)
				ps.setInt(idx++, criteria.getFinalKm());
			if (criteria.getRentalStatusId() != null)
				ps.setInt(idx++, criteria.getRentalStatusId());
			if (criteria.getTotalCost() != null)
				ps.setBigDecimal(idx++, criteria.getTotalCost());

			if (criteria.getUserId() != null)
				ps.setInt(idx++, criteria.getUserId());
			if (criteria.getUserFirstName() != null && !criteria.getUserFirstName().isEmpty())
				ps.setString(idx++, "%" + criteria.getUserFirstName() + "%");
			if (criteria.getUserLastName1() != null && !criteria.getUserLastName1().isEmpty())
				ps.setString(idx++, "%" + criteria.getUserLastName1() + "%");
			if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
				ps.setString(idx++, "%" + criteria.getPhone() + "%");

			if (criteria.getVehicleId() != null)
				ps.setInt(idx++, criteria.getVehicleId());
			if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
				ps.setString(idx++, "%" + criteria.getBrand() + "%");
			if (criteria.getLicensePlate() != null && !criteria.getLicensePlate().isEmpty())
				ps.setString(idx++, "%" + criteria.getLicensePlate() + "%");
			if (criteria.getModel() != null && !criteria.getModel().isEmpty())
				ps.setString(idx++, "%" + criteria.getModel() + "%");

			ps.setInt(idx++, pageSize);
			ps.setInt(idx++, offset);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(loadRental(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error executing paginated rental search", e);
			throw new DataException("Error executing search", e);
		}

		int totalRecords = 0;
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM rental r ");
		countSql.append("INNER JOIN rental_status s ON r.rental_status_id = s.rental_status_id ");
		countSql.append("INNER JOIN reservation res ON r.reservation_id = res.reservation_id ");
		countSql.append("INNER JOIN vehicle v ON res.vehicle_id = v.vehicle_id ");
		countSql.append("INNER JOIN user u ON res.user_id = u.user_id ");
		countSql.append(" WHERE 1=1 ");

		if (criteria.getRentalId() != null)
			countSql.append(" AND r.rental_id = ? ");
		if (criteria.getReservationId() != null)
			countSql.append(" AND r.reservation_id = ? ");
		if (criteria.getStartDateEffective() != null)
			countSql.append(" AND r.start_date_effective >= ? ");
		if (criteria.getEndDateEffective() != null)
			countSql.append(" AND r.end_date_effective <= ? ");
		if (criteria.getInitialKm() != null)
			countSql.append(" AND r.initial_km >= ? ");
		if (criteria.getFinalKm() != null)
			countSql.append(" AND r.final_km <= ? ");
		if (criteria.getRentalStatusId() != null)
			countSql.append(" AND r.rental_status_id = ? ");
		if (criteria.getTotalCost() != null)
			countSql.append(" AND r.total_cost = ? ");

		if (criteria.getUserId() != null)
			countSql.append(" AND res.user_id = ? ");
		if (criteria.getUserFirstName() != null && !criteria.getUserFirstName().isEmpty())
			countSql.append(" AND u.first_name LIKE ? ");
		if (criteria.getUserLastName1() != null && !criteria.getUserLastName1().isEmpty())
			countSql.append(" AND u.last_name1 LIKE ? ");
		if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
			countSql.append(" AND u.phone LIKE ? ");

		if (criteria.getVehicleId() != null)
			countSql.append(" AND res.vehicle_id = ? ");
		if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
			countSql.append(" AND v.brand LIKE ? ");
		if (criteria.getLicensePlate() != null && !criteria.getLicensePlate().isEmpty())
			countSql.append(" AND v.license_plate LIKE ? ");
		if (criteria.getModel() != null && !criteria.getModel().isEmpty())
			countSql.append(" AND v.model LIKE ? ");

		try (PreparedStatement countPs = connection.prepareStatement(countSql.toString())) {
			int idx = 1;
			if (criteria.getRentalId() != null)
				countPs.setInt(idx++, criteria.getRentalId());
			if (criteria.getReservationId() != null)
				countPs.setInt(idx++, criteria.getReservationId());
			if (criteria.getStartDateEffective() != null)
				countPs.setTimestamp(idx++, Timestamp.valueOf(criteria.getStartDateEffective()));
			if (criteria.getEndDateEffective() != null)
				countPs.setTimestamp(idx++, Timestamp.valueOf(criteria.getEndDateEffective()));
			if (criteria.getInitialKm() != null)
				countPs.setInt(idx++, criteria.getInitialKm());
			if (criteria.getFinalKm() != null)
				countPs.setInt(idx++, criteria.getFinalKm());
			if (criteria.getRentalStatusId() != null)
				countPs.setInt(idx++, criteria.getRentalStatusId());
			if (criteria.getTotalCost() != null)
				countPs.setBigDecimal(idx++, criteria.getTotalCost());

			if (criteria.getUserId() != null)
				countPs.setInt(idx++, criteria.getUserId());
			if (criteria.getUserFirstName() != null && !criteria.getUserFirstName().isEmpty())
				countPs.setString(idx++, "%" + criteria.getUserFirstName() + "%");
			if (criteria.getUserLastName1() != null && !criteria.getUserLastName1().isEmpty())
				countPs.setString(idx++, "%" + criteria.getUserLastName1() + "%");
			if (criteria.getPhone() != null && !criteria.getPhone().isEmpty())
				countPs.setString(idx++, "%" + criteria.getPhone() + "%");

			if (criteria.getVehicleId() != null)
				countPs.setInt(idx++, criteria.getVehicleId());
			if (criteria.getBrand() != null && !criteria.getBrand().isEmpty())
				countPs.setString(idx++, "%" + criteria.getBrand() + "%");
			if (criteria.getLicensePlate() != null && !criteria.getLicensePlate().isEmpty())
				countPs.setString(idx++, "%" + criteria.getLicensePlate() + "%");
			if (criteria.getModel() != null && !criteria.getModel().isEmpty())
				countPs.setString(idx++, "%" + criteria.getModel() + "%");

			try (ResultSet rs = countPs.executeQuery()) {
				if (rs.next()) {
					totalRecords = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.error("Error executing rental total count", e);
			throw new DataException("Error executing total count", e);
		}

		results.setResults(list);
		results.setPageNumber(pageNumber);
		results.setPageSize(pageSize);
		results.setTotalRecords(totalRecords);
		return results;
	}

	private void setRentalParameters(PreparedStatement ps, RentalDTO rental, boolean isUpdate) throws SQLException {
		if (!isUpdate) {
			ps.setInt(1, rental.getReservationId());
			ps.setTimestamp(2, Timestamp.valueOf(rental.getStartDateEffective()));
			ps.setTimestamp(3, Timestamp.valueOf(rental.getEndDateEffective()));
			ps.setInt(4, rental.getInitialKm());
			ps.setInt(5, rental.getFinalKm());
			ps.setInt(6, rental.getRentalStatusId());
			ps.setBigDecimal(7, rental.getTotalCost() != null ? rental.getTotalCost() : BigDecimal.ZERO);
			ps.setInt(8, rental.getPickupHeadquartersId());
			ps.setInt(9, rental.getReturnHeadquartersId());
		} else {
			ps.setTimestamp(1, Timestamp.valueOf(rental.getStartDateEffective()));
			ps.setTimestamp(2, Timestamp.valueOf(rental.getEndDateEffective()));
			ps.setInt(3, rental.getInitialKm());
			ps.setInt(4, rental.getFinalKm());
			ps.setInt(5, rental.getRentalStatusId());
			ps.setBigDecimal(6, rental.getTotalCost() != null ? rental.getTotalCost() : BigDecimal.ZERO);
			ps.setInt(7, rental.getPickupHeadquartersId());
			ps.setInt(8, rental.getReturnHeadquartersId());
			ps.setInt(9, rental.getRentalId());
		}
	}

	private RentalDTO loadRental(ResultSet rs) throws SQLException {
		RentalDTO dto = new RentalDTO();
		dto.setRentalId(rs.getInt("rental_id"));
		dto.setReservationId(rs.getInt("reservation_id"));
		dto.setStartDateEffective(rs.getTimestamp("start_date_effective").toLocalDateTime());
		dto.setEndDateEffective(rs.getTimestamp("end_date_effective").toLocalDateTime());
		dto.setInitialKm(rs.getInt("initial_km"));
		dto.setFinalKm(rs.getInt("final_km"));
		dto.setRentalStatusId(rs.getInt("rental_status_id"));
		dto.setTotalCost(rs.getBigDecimal("total_cost"));
		dto.setRentalStatusName(rs.getString("rentalStatusName"));

		dto.setVehicleId(rs.getInt("vehicle_id"));
		dto.setLicensePlate(rs.getString("license_plate"));
		dto.setBrand(rs.getString("brand"));
		dto.setModel(rs.getString("model"));

		dto.setUserId(rs.getInt("user_id"));
		dto.setUserFirstName(rs.getString("userFirstName"));
		dto.setUserLastName1(rs.getString("userLastName1"));
		dto.setPhone(rs.getString("phone"));

		dto.setPickupHeadquartersId(rs.getInt("pickup_headquarters_id"));
		dto.setPickupHeadquartersName(rs.getString("pickupHeadquarters"));
		dto.setReturnHeadquartersId(rs.getInt("return_headquarters_id"));
		dto.setPickupHeadquartersName(rs.getString("returnHeadquarters"));

		Timestamp created = rs.getTimestamp("created_at");
		if (created != null) {
			dto.setCreatedAt(created.toLocalDateTime());
		}
		Timestamp updated = rs.getTimestamp("updated_at");
		if (updated != null) {
			dto.setUpdatedAt(updated.toLocalDateTime());
		}
		return dto;
	}

	@Override
	public boolean existsByReservation(Integer reservationId) throws DataException {
		final String sql = "SELECT COUNT(*) FROM rental WHERE reservation_id = ?";
		try (Connection conn = JDBCUtils.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, reservationId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
			return false;
		} catch (SQLException e) {
			logger.error("Error checking rental existence by reservation", e);
			throw new DataException(e);
		}
	}
}

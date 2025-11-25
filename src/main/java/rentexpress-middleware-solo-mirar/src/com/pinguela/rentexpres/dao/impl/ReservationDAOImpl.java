package com.pinguela.rentexpres.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.ReservationDAO;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * DAO implementation for the 'reservation' table. - Java 1.8 compatible - Uses
 * LocalDateTime for date-time fields - Includes full JOINs (vehicle, user/user,
 * employee, reservation_status, headquarters) - Logs include the current method
 * name
 */
public class ReservationDAOImpl implements ReservationDAO {

	private static final Logger logger = LogManager.getLogger(ReservationDAOImpl.class);

	private static final String RESERVATION_SELECT_BASE = "SELECT r.reservation_id, r.vehicle_id, r.user_id, r.employee_id, r.reservation_status_id, "
			+ "       r.pickup_headquarters_id, r.return_headquarters_id, "
			+ "       r.start_date, r.end_date, r.created_at, r.updated_at, " +
			// Useful aliases from joins (por si tus DTOs los usan después)
			"       v.brand AS vehicle_brand, v.model AS vehicle_model, v.license_plate AS vehicle_plate, "
			+ "       u.first_name AS user_first_name, u.last_name AS user_last_name, u.phone AS user_phone, "
			+ "       e.first_name AS employee_first_name, e.last_name AS employee_last_name, "
			+ "       rs.status_name AS reservation_status_name, "
			+ "       h1.name AS pickup_headquarters_name, h2.name AS return_headquarters_name " + "FROM reservation r "
			+ "INNER JOIN vehicle v ON r.vehicle_id = v.vehicle_id " + "INNER JOIN user u ON r.user_id = u.user_id "
			+ "LEFT JOIN employee e ON r.employee_id = e.employee_id " + // puede ser null
			"INNER JOIN reservation_status rs ON r.reservation_status_id = rs.reservation_status_id "
			+ "INNER JOIN headquarters h1 ON r.pickup_headquarters_id = h1.headquarters_id "
			+ "INNER JOIN headquarters h2 ON r.return_headquarters_id = h2.headquarters_id";

	// ============================================================
	// == FIND BY ID ==============================================
	// ============================================================

	@Override
	public ReservationDTO findById(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return null;
		}

		String sql = RESERVATION_SELECT_BASE + " WHERE r.reservation_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					logger.info("[{}] Reservation found, id: {}", method, id);
					return loadReservation(rs);
				} else {
					logger.warn("[{}] No reservation found with id: {}", method, id);
				}
			}
		} catch (SQLException e) {
			logger.error("[{}] Error finding reservation by ID: {}", method, id, e);
			throw new DataException("Error finding reservation by ID: " + id, e);
		}
		return null;
	}

	// ============================================================
	// == FIND ALL ================================================
	// ============================================================

	@Override
	public List<ReservationDTO> findAll(Connection connection) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		List<ReservationDTO> list = new ArrayList<>();

		String sql = RESERVATION_SELECT_BASE + " ORDER BY r.start_date DESC";
		try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(loadReservation(rs));
			}
			logger.info("[{}] Total reservations found: {}", method, list.size());
		} catch (SQLException e) {
			logger.error("[{}] Error listing reservations", method, e);
			throw new DataException("Error listing reservations", e);
		}
		return list;
	}

	// ============================================================
	// == CREATE ==================================================
	// ============================================================

	@Override
	public boolean create(Connection connection, ReservationDTO r) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (r == null) {
			logger.warn("[{}] called with null reservation.", method);
			return false;
		}

		String sql = "INSERT INTO reservation " + "(vehicle_id, user_id, employee_id, reservation_status_id, "
				+ " pickup_headquarters_id, return_headquarters_id, start_date, end_date) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			setReservationParameters(ps, r, false);
			if (ps.executeUpdate() > 0) {
				try (ResultSet gen = ps.getGeneratedKeys()) {
					if (gen.next()) {
						r.setReservationId(gen.getInt(1));
					}
				}
				logger.info("[{}] Reservation created successfully. ID: {}", method, r.getReservationId());
				return true;
			}
		} catch (SQLException e) {
			logger.error("[{}] Error creating reservation", method, e);
			throw new DataException("Error creating reservation", e);
		}
		return false;
	}

	// ============================================================
	// == UPDATE ==================================================
	// ============================================================

	@Override
	public boolean update(Connection connection, ReservationDTO r) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (r == null || r.getReservationId() == null) {
			logger.warn("[{}] called with null reservation or id.", method);
			return false;
		}

		String sql = "UPDATE reservation SET vehicle_id = ?, user_id = ?, employee_id = ?, reservation_status_id = ?, "
				+ "pickup_headquarters_id = ?, return_headquarters_id = ?, start_date = ?, end_date = ? "
				+ "WHERE reservation_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			setReservationParameters(ps, r, true);
			if (ps.executeUpdate() > 0) {
				logger.info("[{}] Reservation updated successfully. ID: {}", method, r.getReservationId());
				return true;
			}
		} catch (SQLException e) {
			logger.error("[{}] Error updating reservation", method, e);
			throw new DataException("Error updating reservation", e);
		}
		return false;
	}

	// ============================================================
	// == DELETE ==================================================
	// ============================================================

	@Override
	public boolean delete(Connection connection, Integer id) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();
		if (id == null) {
			logger.warn("[{}] called with null id.", method);
			return false;
		}

		String sql = "DELETE FROM reservation WHERE reservation_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, id);
			if (ps.executeUpdate() > 0) {
				logger.info("[{}] Reservation deleted. ID: {}", method, id);
				return true;
			}
		} catch (SQLException e) {
			logger.error("[{}] Error deleting reservation ID: {}", method, id, e);
			throw new DataException("Error deleting reservation ID: " + id, e);
		}
		return false;
	}

	// ============================================================
	// == FIND BY CRITERIA ========================================
	// ============================================================

	@Override
	public Results<ReservationDTO> findByCriteria(Connection connection, ReservationCriteria c) throws DataException {
		final String method = new Object() {
		}.getClass().getEnclosingMethod().getName();

		Results<ReservationDTO> results = new Results<>();
		List<ReservationDTO> list = new ArrayList<>();

		int page = c.getPageNumber() == null || c.getPageNumber() <= 0 ? 1 : c.getPageNumber();
		int size = c.getPageSize() == null || c.getPageSize() <= 0 ? 25 : c.getPageSize();
		int offset = (page - 1) * size;

		StringBuilder sql = new StringBuilder(RESERVATION_SELECT_BASE);
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) " + "FROM reservation r "
				+ "INNER JOIN vehicle v ON r.vehicle_id = v.vehicle_id " + "INNER JOIN user u ON r.user_id = u.user_id "
				+ "LEFT JOIN employee e ON r.employee_id = e.employee_id "
				+ "INNER JOIN reservation_status rs ON r.reservation_status_id = rs.reservation_status_id "
				+ "INNER JOIN headquarters h1 ON r.pickup_headquarters_id = h1.headquarters_id "
				+ "INNER JOIN headquarters h2 ON r.return_headquarters_id = h2.headquarters_id ");

		sql.append(" WHERE 1=1 ");
		countSql.append(" WHERE 1=1 ");

		// --- filtros exactos por ids ---
		if (c.getReservationId() != null) {
			sql.append(" AND r.reservation_id = ? ");
			countSql.append(" AND r.reservation_id = ? ");
		}
		if (c.getVehicleId() != null) {
			sql.append(" AND r.vehicle_id = ? ");
			countSql.append(" AND r.vehicle_id = ? ");
		}
		if (c.getUserId() != null) {
			sql.append(" AND r.user_id = ? ");
			countSql.append(" AND r.user_id = ? ");
		}
		if (c.getEmployeeId() != null) {
			sql.append(" AND r.employee_id = ? ");
			countSql.append(" AND r.employee_id = ? ");
		}
		if (c.getReservationStatusId() != null) {
			sql.append(" AND r.reservation_status_id = ? ");
			countSql.append(" AND r.reservation_status_id = ? ");
		}
		if (c.getPickupHeadquartersId() != null) {
			sql.append(" AND r.pickup_headquarters_id = ? ");
			countSql.append(" AND r.pickup_headquarters_id = ? ");
		}
		if (c.getReturnHeadquartersId() != null) {
			sql.append(" AND r.return_headquarters_id = ? ");
			countSql.append(" AND r.return_headquarters_id = ? ");
		}

		// --- rangos de fechas ---
		if (c.getStartDateFrom() != null) {
			sql.append(" AND r.start_date >= ? ");
			countSql.append(" AND r.start_date >= ? ");
		}
		if (c.getStartDateTo() != null) {
			sql.append(" AND r.start_date <= ? ");
			countSql.append(" AND r.start_date <= ? ");
		}

		if (c.getEndDateFrom() != null) {
			sql.append(" AND r.end_date >= ? ");
			countSql.append(" AND r.end_date >= ? ");
		}
		if (c.getEndDateTo() != null) {
			sql.append(" AND r.end_date <= ? ");
			countSql.append(" AND r.end_date <= ? ");
		}

		if (c.getCreatedAtFrom() != null) {
			sql.append(" AND r.created_at >= ? ");
			countSql.append(" AND r.created_at >= ? ");
		}
		if (c.getCreatedAtTo() != null) {
			sql.append(" AND r.created_at <= ? ");
			countSql.append(" AND r.created_at <= ? ");
		}

		if (c.getUpdatedAtFrom() != null) {
			sql.append(" AND r.updated_at >= ? ");
			countSql.append(" AND r.updated_at >= ? ");
		}
		if (c.getUpdatedAtTo() != null) {
			sql.append(" AND r.updated_at <= ? ");
			countSql.append(" AND r.updated_at <= ? ");
		}

		sql.append(" ORDER BY r.start_date DESC LIMIT ? OFFSET ?");

		// --- consulta paginada ---
		try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
			int idx = 1;

			// ids exactos
			if (c.getReservationId() != null)
				ps.setInt(idx++, c.getReservationId());
			if (c.getVehicleId() != null)
				ps.setInt(idx++, c.getVehicleId());
			if (c.getUserId() != null)
				ps.setInt(idx++, c.getUserId());
			if (c.getEmployeeId() != null)
				ps.setInt(idx++, c.getEmployeeId());
			if (c.getReservationStatusId() != null)
				ps.setInt(idx++, c.getReservationStatusId());
			if (c.getPickupHeadquartersId() != null)
				ps.setInt(idx++, c.getPickupHeadquartersId());
			if (c.getReturnHeadquartersId() != null)
				ps.setInt(idx++, c.getReturnHeadquartersId());

			// rangos
			if (c.getStartDateFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getStartDateFrom()));
			if (c.getStartDateTo() != null)
				ps.setTimestamp(idx++, toTs(c.getStartDateTo()));
			if (c.getEndDateFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getEndDateFrom()));
			if (c.getEndDateTo() != null)
				ps.setTimestamp(idx++, toTs(c.getEndDateTo()));
			if (c.getCreatedAtFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getCreatedAtFrom()));
			if (c.getCreatedAtTo() != null)
				ps.setTimestamp(idx++, toTs(c.getCreatedAtTo()));
			if (c.getUpdatedAtFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getUpdatedAtFrom()));
			if (c.getUpdatedAtTo() != null)
				ps.setTimestamp(idx++, toTs(c.getUpdatedAtTo()));

			// paginación
			ps.setInt(idx++, size);
			ps.setInt(idx++, offset);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(loadReservation(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("[{}] Error executing paginated reservation search", method, e);
			throw new DataException("Error executing reservation search", e);
		}

		// --- consulta de conteo (mismo binding, sin LIMIT/OFFSET) ---
		int totalRecords = 0;
		try (PreparedStatement ps = connection.prepareStatement(countSql.toString())) {
			int idx = 1;

			if (c.getReservationId() != null)
				ps.setInt(idx++, c.getReservationId());
			if (c.getVehicleId() != null)
				ps.setInt(idx++, c.getVehicleId());
			if (c.getUserId() != null)
				ps.setInt(idx++, c.getUserId());
			if (c.getEmployeeId() != null)
				ps.setInt(idx++, c.getEmployeeId());
			if (c.getReservationStatusId() != null)
				ps.setInt(idx++, c.getReservationStatusId());
			if (c.getPickupHeadquartersId() != null)
				ps.setInt(idx++, c.getPickupHeadquartersId());
			if (c.getReturnHeadquartersId() != null)
				ps.setInt(idx++, c.getReturnHeadquartersId());

			if (c.getStartDateFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getStartDateFrom()));
			if (c.getStartDateTo() != null)
				ps.setTimestamp(idx++, toTs(c.getStartDateTo()));
			if (c.getEndDateFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getEndDateFrom()));
			if (c.getEndDateTo() != null)
				ps.setTimestamp(idx++, toTs(c.getEndDateTo()));
			if (c.getCreatedAtFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getCreatedAtFrom()));
			if (c.getCreatedAtTo() != null)
				ps.setTimestamp(idx++, toTs(c.getCreatedAtTo()));
			if (c.getUpdatedAtFrom() != null)
				ps.setTimestamp(idx++, toTs(c.getUpdatedAtFrom()));
			if (c.getUpdatedAtTo() != null)
				ps.setTimestamp(idx++, toTs(c.getUpdatedAtTo()));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					totalRecords = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("[{}] Error executing reservation total count", method, e);
			throw new DataException("Error executing reservation total count", e);
		}

		results.setResults(list);
		results.setPageNumber(page);
		results.setPageSize(size);
		results.setTotalRecords(totalRecords);

		logger.info("[{}] findByCriteria reservation: page {} (size {}), total {}", method, page, size, totalRecords);
		return results;
	}

	// ============================================================
	// == PRIVATE HELPERS =========================================
	// ============================================================

	private static Timestamp toTs(LocalDateTime dt) {
		return dt == null ? null : Timestamp.valueOf(dt);
	}

	private void setReservationParameters(PreparedStatement ps, ReservationDTO r, boolean isUpdate)
			throws SQLException {
		ps.setInt(1, r.getVehicleId());
		ps.setInt(2, r.getUserId());
		if (r.getEmployeeId() != null) {
			ps.setInt(3, r.getEmployeeId());
		} else {
			ps.setNull(3, Types.INTEGER);
		}
		ps.setInt(4, r.getReservationStatusId());
		ps.setInt(5, r.getPickupHeadquartersId());
		ps.setInt(6, r.getReturnHeadquartersId());
		ps.setTimestamp(7, toTs(r.getStartDate()));
		ps.setTimestamp(8, toTs(r.getEndDate()));

		if (isUpdate) {
			ps.setInt(9, r.getReservationId());
		}
	}

	private ReservationDTO loadReservation(ResultSet rs) throws SQLException {
		ReservationDTO dto = new ReservationDTO();

		dto.setReservationId(rs.getInt("reservation_id"));
		dto.setVehicleId(rs.getInt("vehicle_id"));
		dto.setUserId(rs.getInt("user_id"));

		int empId = rs.getInt("employee_id");
		dto.setEmployeeId(rs.wasNull() ? null : empId);

		dto.setReservationStatusId(rs.getInt("reservation_status_id"));
		dto.setPickupHeadquartersId(rs.getInt("pickup_headquarters_id"));
		dto.setReturnHeadquartersId(rs.getInt("return_headquarters_id"));

		Timestamp s = rs.getTimestamp("start_date");
		if (s != null)
			dto.setStartDate(s.toLocalDateTime());

		Timestamp e = rs.getTimestamp("end_date");
		if (e != null)
			dto.setEndDate(e.toLocalDateTime());

		Timestamp cAt = rs.getTimestamp("created_at");
		if (cAt != null)
			dto.setCreatedAt(cAt.toLocalDateTime());

		Timestamp uAt = rs.getTimestamp("updated_at");
		if (uAt != null)
			dto.setUpdatedAt(uAt.toLocalDateTime());

		// Nota: dejé los campos “relational objects (for joins)” del DTO como listas
		// vacías
		// (se suelen poblar desde servicios o mapeadores especializados).
		// Aquí igualmente traemos todos los JOINs para futuras ampliaciones.

		return dto;
	}

	@Override
	public void updateStatus(Connection connection, Integer reservationId, Integer statusId) throws DataException {
	    PreparedStatement ps = null;
	    try {
	        ps = connection.prepareStatement(
	            "UPDATE reservation SET reservation_status_id = ? WHERE reservation_id = ?");
	        ps.setInt(1, statusId);
	        ps.setInt(2, reservationId);
	        ps.executeUpdate();
	    } catch (SQLException e) {
	        throw new DataException("Error actualizando estado de la reserva", e);
	    } finally {
	        JDBCUtils.close(ps, null);
	    }
	}
}

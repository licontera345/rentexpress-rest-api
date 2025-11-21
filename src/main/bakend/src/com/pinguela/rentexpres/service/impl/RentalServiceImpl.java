package com.pinguela.rentexpres.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RentalDAO;
import com.pinguela.rentexpres.dao.ReservationDAO;
import com.pinguela.rentexpres.dao.VehicleDAO;
import com.pinguela.rentexpres.dao.impl.RentalDAOImpl;
import com.pinguela.rentexpres.dao.impl.ReservationDAOImpl;
import com.pinguela.rentexpres.dao.impl.VehicleDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleDTO;
import com.pinguela.rentexpres.service.RentalService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link RentalService}. Handles business logic for CRUD and
 * search operations on rentals, delegating data access to {@link RentalDAO} and
 * managing JDBC transactions.
 * 
 * @author
 */
public class RentalServiceImpl implements RentalService {

	private static final Logger logger = LogManager.getLogger(RentalServiceImpl.class);
	private final RentalDAO rentalDAO;
	private Connection connection = null;
	private final ReservationDAO reservationDAO;
	private final VehicleDAO vehicleDAO;

	public RentalServiceImpl() {
		this.rentalDAO = new RentalDAOImpl();
		this.reservationDAO =  new ReservationDAOImpl();
		this.vehicleDAO = new VehicleDAOImpl();
	}

	@Override
	public RentalDTO findById(Integer id) throws RentexpresException {
		RentalDTO rental = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			rental = rentalDAO.findById(connection, id);

			JDBCUtils.commitTransaction(connection);
			logger.info("findById transaction completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for rental", e);
			throw new RentexpresException("Error in findById for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return rental;
	}

	@Override
	public List<RentalDTO> findAll() throws RentexpresException {
		List<RentalDTO> list = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			list = rentalDAO.findAll(connection);

			JDBCUtils.commitTransaction(connection);
			logger.info("findAll transaction completed successfully. Total rentals: {}",
					(list != null ? list.size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findAll for rental", e);
			throw new RentexpresException("Error in findAll for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return list;
	}

	@Override
	public boolean create(RentalDTO rental) throws RentexpresException {
		boolean created = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			created = rentalDAO.create(connection, rental);
			if (created) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Rental created successfully. ID: {}", rental.getRentalId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Rental could not be created.");
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in create for rental", e);
			throw new RentexpresException("Error in create for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return created;
	}

	@Override
	public boolean update(RentalDTO rental) throws RentexpresException {
		boolean updated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			updated = rentalDAO.update(connection, rental);
			if (updated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Rental updated successfully. ID: {}", rental.getRentalId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Rental could not be updated. ID: {}", rental.getRentalId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in update for rental", e);
			throw new RentexpresException("Error in update for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return updated;
	}

	@Override
	public boolean delete(Integer id) throws RentexpresException {
		boolean deleted = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			deleted = rentalDAO.delete(connection, id);
			if (deleted) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Rental deleted successfully. ID: {}", id);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Rental could not be deleted. ID: {}", id);
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in delete for rental", e);
			throw new RentexpresException("Error in delete for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return deleted;
	}

	@Override
	public Results<RentalDTO> findByCriteria(RentalCriteria criteria) throws RentexpresException {
		Results<RentalDTO> results = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			results = rentalDAO.findByCriteria(connection, criteria);

			JDBCUtils.commitTransaction(connection);
			logger.info("findByCriteria for rental completed successfully. Results: {}",
					(results != null && results.getResults() != null ? results.getResults().size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findByCriteria for rental", e);
			throw new RentexpresException("Error in findByCriteria for rental", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return results;
	}

	@Override
	public boolean existsByReservation(Integer reservationId) throws RentexpresException {
		try {
			return rentalDAO.existsByReservation(reservationId);
		} catch (DataException e) {
			throw new RentexpresException("Error checking rental existence for reservation ID " + reservationId, e);
		}
	}

	@Override
	public void createFromReservation(ReservationDTO reservation) throws RentexpresException {
		Connection conn = null;
		try {
			conn = JDBCUtils.getConnection();
			conn.setAutoCommit(false);

			Integer reservationId = reservation.getReservationId();
			Integer vehicleId = reservation.getVehicleId();

			// 1️⃣ Buscar vehículo y su precio diario
			VehicleDTO vehicle = vehicleDAO.findById(conn, vehicleId);
			if (vehicle == null) {
				throw new RentexpresException("Vehículo no encontrado para la reserva ID=" + reservationId);
			}

			// 2️⃣ Calcular días y coste total
			long days = ChronoUnit.DAYS.between(reservation.getStartDate().toLocalDate(),
					reservation.getEndDate().toLocalDate()) + 1;
			if (days < 1)
				days = 1;
			BigDecimal costTotal = vehicle.getDailyPrice().multiply(BigDecimal.valueOf(days));

			// 3️⃣ Crear alquiler (rental)
			RentalDTO rental = new RentalDTO();
			rental.setReservationId(reservationId);
			rental.setVehicleId(vehicleId);
			rental.setStartDateEffective(reservation.getStartDate());
			rental.setEndDateEffective(reservation.getEndDate());
			rental.setTotalCost(costTotal);
			rental.setRentalStatusId(1); // "Activo"

			rentalDAO.create(conn, rental);

			// 4️⃣ Cambiar estado del vehículo a “Alquilado”
			vehicleDAO.updateStatus(conn, vehicleId, 2); // 2 = Alquilado

			// 5️⃣ Actualizar estado de la reserva
			reservationDAO.updateStatus(conn, reservationId, 2); // 2 = Convertida/Completada

			conn.commit();

			logger.info("Reserva {} convertida en alquiler (ID alquiler generado)", reservationId);

		} catch (Exception e) {
			JDBCUtils.rollbackTransaction(conn);
			logger.error("Error convirtiendo reserva a alquiler", e);
			throw new RentexpresException("Error creando alquiler desde reserva.", e);
		} finally {

			JDBCUtils.close(conn);
		}
	}

	@Override
	public int autoConvertReservations() throws RentexpresException {
		Connection conn = null;
		int converted = 0;
		try {
			conn = JDBCUtils.getConnection();
			conn.setAutoCommit(false);

			// Buscar todas las reservas cuyo start_date <= NOW() y status = “Reservado”
			String sql = "SELECT * FROM reservation WHERE reservation_status_id = 1 AND start_date <= NOW()";
			try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					ReservationDTO reservation = new ReservationDTO();
					reservation.setReservationId(rs.getInt("reservation_id"));
					reservation.setVehicleId(rs.getInt("vehicle_id"));
					reservation.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
					reservation.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
					createFromReservation(reservation);
					converted++;
				}
			}
			conn.commit();
			logger.info("Conversión automática completada: {} reservas convertidas.", converted);
		} catch (Exception e) {
			JDBCUtils.rollbackTransaction(conn);
			throw new RentexpresException("Error en conversión automática de reservas", e);
		} finally {
			JDBCUtils.close(conn);
		}
		return converted;
	}

}
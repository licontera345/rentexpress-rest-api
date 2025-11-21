package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.ReservationDAO;
import com.pinguela.rentexpres.dao.impl.ReservationDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.service.ReservationService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link ReservationService}.
 * Handles business logic and transaction control for Reservation operations,
 * delegating data access to {@link ReservationDAO}.
 * 
 * @author 
 */
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LogManager.getLogger(ReservationServiceImpl.class);
    private final ReservationDAO reservationDAO;

    public ReservationServiceImpl() {
        this.reservationDAO = new ReservationDAOImpl();
    }

    @Override
    public ReservationDTO findById(Integer id) throws RentexpresException {
        Connection connection = null;
        ReservationDTO reservation = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            reservation = reservationDAO.findById(connection, id);

            JDBCUtils.commitTransaction(connection);
            logger.info("findById transaction completed successfully. ID: {}", id);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findById for Reservation", e);
            throw new RentexpresException("Error in findById for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return reservation;
    }

    @Override
    public List<ReservationDTO> findAll() throws RentexpresException {
        Connection connection = null;
        List<ReservationDTO> reservations = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            reservations = reservationDAO.findAll(connection);

            JDBCUtils.commitTransaction(connection);
            logger.info("findAll transaction completed successfully. Total reservations: {}", 
                        reservations != null ? reservations.size() : 0);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findAll for Reservation", e);
            throw new RentexpresException("Error in findAll for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return reservations;
    }

    @Override
    public boolean create(ReservationDTO reservation) throws RentexpresException {
        Connection connection = null;
        boolean created = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            created = reservationDAO.create(connection, reservation);
            if (created) {
                JDBCUtils.commitTransaction(connection);
                logger.info("Reservation created successfully. ID: {}", reservation.getReservationId());
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("Reservation could not be created.");
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in create for Reservation", e);
            throw new RentexpresException("Error in create for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return created;
    }

    @Override
    public boolean update(ReservationDTO reservation) throws RentexpresException {
        Connection connection = null;
        boolean updated = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            updated = reservationDAO.update(connection, reservation);
            if (updated) {
                JDBCUtils.commitTransaction(connection);
                logger.info("Reservation updated successfully. ID: {}", reservation.getReservationId());
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("Reservation could not be updated. ID: {}", reservation.getReservationId());
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in update for Reservation", e);
            throw new RentexpresException("Error in update for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return updated;
    }

    @Override
    public boolean delete(Integer id) throws RentexpresException {
        Connection connection = null;
        boolean deleted = false;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            deleted = reservationDAO.delete(connection, id);
            if (deleted) {
                JDBCUtils.commitTransaction(connection);
                logger.info("Reservation deleted successfully. ID: {}", id);
            } else {
                JDBCUtils.rollbackTransaction(connection);
                logger.warn("Reservation could not be deleted. ID: {}", id);
            }
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in delete for Reservation", e);
            throw new RentexpresException("Error in delete for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return deleted;
    }

    @Override
    public Results<ReservationDTO> findByCriteria(ReservationCriteria criteria) throws RentexpresException {
        Connection connection = null;
        Results<ReservationDTO> results = null;
        try {
            connection = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(connection);

            results = reservationDAO.findByCriteria(connection, criteria);

            JDBCUtils.commitTransaction(connection);
            logger.info("findByCriteria for Reservation completed successfully. Page {} (Size: {}), Total records: {}",
                    criteria.getPageNumber(), criteria.getPageSize(),
                    results != null ? results.getTotalRecords() : 0);
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(connection);
            logger.error("Error in findByCriteria for Reservation", e);
            throw new RentexpresException("Error in findByCriteria for Reservation", e);
        } finally {
            JDBCUtils.close(connection);
        }
        return results;
    }
}

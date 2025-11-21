package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.ReservationStatusDAO;
import com.pinguela.rentexpres.dao.impl.ReservationStatusDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationStatusDTO;
import com.pinguela.rentexpres.service.ReservationStatusService;
import com.pinguela.rentexpres.util.JDBCUtils;

public class ReservationStatusServiceImpl implements ReservationStatusService {

    private static final Logger logger = LogManager.getLogger(ReservationStatusServiceImpl.class);

    private final ReservationStatusDAO reservationStatusDAO = new ReservationStatusDAOImpl();

    @Override
    public ReservationStatusDTO findById(Integer id, String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            ReservationStatusDTO dto = reservationStatusDAO.findById(c, id, isoCode);
            JDBCUtils.commitTransaction(c);
            return dto;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[ReservationStatusService.findById] id={}, iso={}", id, isoCode, e);
            throw new RentexpresException("Error in ReservationStatusService.findById (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }

    @Override
    public List<ReservationStatusDTO> findAll(String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            List<ReservationStatusDTO> list = reservationStatusDAO.findAll(c, isoCode);
            JDBCUtils.commitTransaction(c);
            return list;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[ReservationStatusService.findAll] iso={}", isoCode, e);
            throw new RentexpresException("Error in ReservationStatusService.findAll (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }
}

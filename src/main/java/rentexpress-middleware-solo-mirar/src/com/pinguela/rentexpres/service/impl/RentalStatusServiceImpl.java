package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.RentalStatusDAO;
import com.pinguela.rentexpres.dao.impl.RentalStatusDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalStatusDTO;
import com.pinguela.rentexpres.service.RentalStatusService;
import com.pinguela.rentexpres.util.JDBCUtils;

public class RentalStatusServiceImpl implements RentalStatusService {

    private static final Logger logger = LogManager.getLogger(RentalStatusServiceImpl.class);

    private final RentalStatusDAO rentalStatusDAO = new RentalStatusDAOImpl();

    @Override
    public RentalStatusDTO findById(Integer id, String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            RentalStatusDTO dto = rentalStatusDAO.findById(c, id, isoCode);
            JDBCUtils.commitTransaction(c);
            return dto;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[RentalStatusService.findById] id={}, iso={}", id, isoCode, e);
            throw new RentexpresException("Error in RentalStatusService.findById (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }

    @Override
    public List<RentalStatusDTO> findAll(String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            List<RentalStatusDTO> list = rentalStatusDAO.findAll(c, isoCode);
            JDBCUtils.commitTransaction(c);
            return list;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[RentalStatusService.findAll] iso={}", isoCode, e);
            throw new RentexpresException("Error in RentalStatusService.findAll (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }
}

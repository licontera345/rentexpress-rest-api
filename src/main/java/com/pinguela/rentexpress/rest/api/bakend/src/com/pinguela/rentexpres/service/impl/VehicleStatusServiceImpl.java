package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.VehicleStatusDAO;
import com.pinguela.rentexpres.dao.impl.VehicleStatusDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleStatusDTO;
import com.pinguela.rentexpres.service.VehicleStatusService;
import com.pinguela.rentexpres.util.JDBCUtils;

public class VehicleStatusServiceImpl implements VehicleStatusService {

    private static final Logger logger = LogManager.getLogger(VehicleStatusServiceImpl.class);

    private final VehicleStatusDAO vehicleStatusDAO = new VehicleStatusDAOImpl();

    @Override
    public VehicleStatusDTO findById(Integer id, String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            VehicleStatusDTO dto = vehicleStatusDAO.findById(c, id, isoCode);
            JDBCUtils.commitTransaction(c);
            return dto;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[VehicleStatusService.findById] id={}, iso={}", id, isoCode, e);
            throw new RentexpresException("Error in VehicleStatusService.findById (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }

    @Override
    public List<VehicleStatusDTO> findAll(String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            List<VehicleStatusDTO> list = vehicleStatusDAO.findAll(c, isoCode);
            JDBCUtils.commitTransaction(c);
            return list;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[VehicleStatusService.findAll] iso={}", isoCode, e);
            throw new RentexpresException("Error in VehicleStatusService.findAll (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }
}

package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.VehicleCategoryDAO;
import com.pinguela.rentexpres.dao.impl.VehicleCategoryDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpres.util.JDBCUtils;

public class VehicleCategoryServiceImpl implements VehicleCategoryService {

    private static final Logger logger = LogManager.getLogger(VehicleCategoryServiceImpl.class);

    private final VehicleCategoryDAO vehicleCategoryDAO = new VehicleCategoryDAOImpl();

    @Override
    public VehicleCategoryDTO findById(Integer id, String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            VehicleCategoryDTO dto = vehicleCategoryDAO.findById(c, id, isoCode);
            JDBCUtils.commitTransaction(c);
            return dto;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[VehicleCategoryService.findById] id={}, iso={}", id, isoCode, e);
            throw new RentexpresException("Error in VehicleCategoryService.findById (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }

    @Override
    public List<VehicleCategoryDTO> findAll(String isoCode) throws RentexpresException {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            JDBCUtils.beginTransaction(c);
            List<VehicleCategoryDTO> list = vehicleCategoryDAO.findAll(c, isoCode);
            JDBCUtils.commitTransaction(c);
            return list;
        } catch (SQLException | DataException e) {
            JDBCUtils.rollbackTransaction(c);
            logger.error("[VehicleCategoryService.findAll] iso={}", isoCode, e);
            throw new RentexpresException("Error in VehicleCategoryService.findAll (i18n)", e);
        } finally {
            JDBCUtils.close(c);
        }
    }
}

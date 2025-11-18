package com.pinguela.rentexpres.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.dao.ProvinceDAO;
import com.pinguela.rentexpres.dao.impl.ProvinceDAOImpl;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ProvinceDTO;
import com.pinguela.rentexpres.service.ProvinceService;
import com.pinguela.rentexpres.util.JDBCUtils;

/**
 * Implementation of {@link ProvinceService}. Handles business logic for CRUD
 * operations on provinces, delegating data access to {@link ProvinceDAO}.
 * 
 * @author
 */
public class ProvinceServiceImpl implements ProvinceService {

	private static final Logger logger = LogManager.getLogger(ProvinceServiceImpl.class);
	private final ProvinceDAO provinceDAO;

	public ProvinceServiceImpl() {
		this.provinceDAO = new ProvinceDAOImpl();
	}

	@Override
	public ProvinceDTO findById(Integer id) throws RentexpresException {
		Connection connection = null;
		ProvinceDTO province = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			province = provinceDAO.findById(connection, id);

			JDBCUtils.commitTransaction(connection);
			logger.info("findById completed successfully. ID: {}", id);
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findById for Province", e);
			throw new RentexpresException("Error in findById for Province", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return province;
	}

	@Override
	public List<ProvinceDTO> findAll() throws RentexpresException {
		Connection connection = null;
		List<ProvinceDTO> list = null;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			list = provinceDAO.findAll(connection);

			JDBCUtils.commitTransaction(connection);
			logger.info("findAll completed successfully. Total provinces: {}", (list != null ? list.size() : 0));
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in findAll for Province", e);
			throw new RentexpresException("Error in findAll for Province", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return list;
	}

	@Override
	public boolean create(ProvinceDTO province) throws RentexpresException {
		Connection connection = null;
		boolean created = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			created = provinceDAO.create(connection, province);
			if (created) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Province created successfully. ID: {}", province.getProvinceId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Province could not be created.");
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in create for Province", e);
			throw new RentexpresException("Error in create for Province", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return created;
	}

	@Override
	public boolean update(ProvinceDTO province) throws RentexpresException {
		Connection connection = null;
		boolean updated = false;
		try {
			connection = JDBCUtils.getConnection();
			JDBCUtils.beginTransaction(connection);

			updated = provinceDAO.update(connection, province);
			if (updated) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Province updated successfully. ID: {}", province.getProvinceId());
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Province could not be updated. ID: {}", province.getProvinceId());
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in update for Province", e);
			throw new RentexpresException("Error in update for Province", e);
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

			deleted = provinceDAO.delete(connection, id);
			if (deleted) {
				JDBCUtils.commitTransaction(connection);
				logger.info("Province deleted successfully. ID: {}", id);
			} else {
				JDBCUtils.rollbackTransaction(connection);
				logger.warn("Province could not be deleted. ID: {}", id);
			}
		} catch (SQLException | DataException e) {
			JDBCUtils.rollbackTransaction(connection);
			logger.error("Error in delete for Province", e);
			throw new RentexpresException("Error in delete for Province", e);
		} finally {
			JDBCUtils.close(connection);
		}
		return deleted;
	}
}

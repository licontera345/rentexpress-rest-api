package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.CityDTO;

public interface CityDAO {

	CityDTO findById(Connection connection, Integer id) throws DataException;

	List<CityDTO> findAll(Connection connection) throws DataException;

	List<CityDTO> findByProvinceId(Connection connection, Integer provinceId) throws DataException;

	boolean create(Connection connection, CityDTO city) throws DataException;

	boolean update(Connection connection, CityDTO city) throws DataException;

	boolean delete(Connection connection, CityDTO city) throws DataException;
}

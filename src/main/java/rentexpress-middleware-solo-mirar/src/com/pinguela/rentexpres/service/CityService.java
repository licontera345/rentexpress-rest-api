package com.pinguela.rentexpres.service;

import java.util.List;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.CityDTO;

public interface CityService {

	CityDTO findById(Integer id) throws RentexpresException;

	List<CityDTO> findAll() throws RentexpresException;

	List<CityDTO> findByProvinceId(Integer provinceId) throws RentexpresException;

	boolean create(CityDTO city) throws RentexpresException;

	boolean update(CityDTO city) throws RentexpresException;

	boolean delete(CityDTO city) throws RentexpresException;
}

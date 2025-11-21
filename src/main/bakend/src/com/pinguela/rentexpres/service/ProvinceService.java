package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ProvinceDTO;

/**
 * Service interface for managing provinces. Provides CRUD operations and
 * handles business logic.
 */
public interface ProvinceService {

	ProvinceDTO findById(Integer id) throws RentexpresException;

	List<ProvinceDTO> findAll() throws RentexpresException;

	boolean create(ProvinceDTO province) throws RentexpresException;

	boolean update(ProvinceDTO province) throws RentexpresException;

	boolean delete(Integer id) throws RentexpresException;
}

package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.VehicleCriteria;
import com.pinguela.rentexpres.model.VehicleDTO;

/**
 * Service interface for managing vehicles and their images.
 */
public interface VehicleService {

	VehicleDTO findById(Integer id) throws RentexpresException;

	List<VehicleDTO> findAll() throws RentexpresException;

	boolean create(VehicleDTO vehicle) throws RentexpresException;

	boolean update(VehicleDTO vehicle) throws RentexpresException;

	boolean delete(Integer id) throws RentexpresException;

	Results<VehicleDTO> findByCriteria(VehicleCriteria criteria) throws RentexpresException;

}

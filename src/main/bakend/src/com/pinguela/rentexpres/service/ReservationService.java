package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationCriteria;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;

/**
 * Service interface for managing reservations. Provides CRUD operations and
 * criteria-based search.
 */
public interface ReservationService {

	ReservationDTO findById(Integer id) throws RentexpresException;

	List<ReservationDTO> findAll() throws RentexpresException;

	boolean create(ReservationDTO reservation) throws RentexpresException;

	boolean update(ReservationDTO reservation) throws RentexpresException;

	boolean delete(Integer id) throws RentexpresException;

	Results<ReservationDTO> findByCriteria(ReservationCriteria criteria) throws RentexpresException;
}

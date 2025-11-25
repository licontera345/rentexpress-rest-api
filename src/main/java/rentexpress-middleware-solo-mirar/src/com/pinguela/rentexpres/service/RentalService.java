package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalCriteria;
import com.pinguela.rentexpres.model.RentalDTO;
import com.pinguela.rentexpres.model.ReservationDTO;
import com.pinguela.rentexpres.model.Results;

public interface RentalService {

	public RentalDTO findById(Integer id) throws RentexpresException;

	public List<RentalDTO> findAll() throws RentexpresException;;

	public boolean create(RentalDTO alquiler) throws RentexpresException;

	public boolean update(RentalDTO alquiler) throws RentexpresException;

	public boolean delete(Integer id) throws RentexpresException;

	public Results<RentalDTO> findByCriteria(RentalCriteria criteria) throws RentexpresException;

	boolean existsByReservation(Integer idReserva) throws RentexpresException;

	void createFromReservation(ReservationDTO reservation) throws RentexpresException;

	int autoConvertReservations() throws RentexpresException;
}

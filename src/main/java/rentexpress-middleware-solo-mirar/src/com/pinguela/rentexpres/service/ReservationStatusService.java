package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.ReservationStatusDTO;

public interface ReservationStatusService {

    ReservationStatusDTO findById(Integer id, String isoCode) throws RentexpresException;

    List<ReservationStatusDTO> findAll(String isoCode) throws RentexpresException;
}

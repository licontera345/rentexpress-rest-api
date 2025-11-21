package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RentalStatusDTO;

public interface RentalStatusService {

    RentalStatusDTO findById(Integer id, String isoCode) throws RentexpresException;

    List<RentalStatusDTO> findAll(String isoCode) throws RentexpresException;
}

package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleStatusDTO;

public interface VehicleStatusService {

    VehicleStatusDTO findById(Integer id, String isoCode) throws RentexpresException;

    List<VehicleStatusDTO> findAll(String isoCode) throws RentexpresException;
}

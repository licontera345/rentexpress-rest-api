package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.VehicleCategoryDTO;

public interface VehicleCategoryService {

    VehicleCategoryDTO findById(Integer id, String isoCode) throws RentexpresException;

    List<VehicleCategoryDTO> findAll(String isoCode) throws RentexpresException;
}

package com.pinguela.rentexpres.service;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.AddressDTO;

public interface AddressService {

	public AddressDTO findById(Integer id) throws RentexpresException;

	public boolean create(AddressDTO address) throws RentexpresException;

	public boolean update(AddressDTO address) throws RentexpresException;

	public boolean delete(AddressDTO address) throws RentexpresException;
}

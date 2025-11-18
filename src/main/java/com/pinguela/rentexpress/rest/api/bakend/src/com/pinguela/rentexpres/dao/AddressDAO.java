package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.AddressDTO;

public interface AddressDAO {

	public AddressDTO findById(Connection connection, Integer id) throws DataException;

	public boolean create(Connection connection, AddressDTO address) throws DataException;

	public boolean update(Connection connection, AddressDTO address) throws DataException;

	public boolean delete(Connection connection, AddressDTO address, Integer id) throws DataException;
}

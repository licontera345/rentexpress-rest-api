package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.RoleDTO;

public interface RoleDAO {

	public RoleDTO findById(Connection connection, Integer id) throws DataException;

	public List<RoleDTO> findAll(Connection connection) throws DataException;
}

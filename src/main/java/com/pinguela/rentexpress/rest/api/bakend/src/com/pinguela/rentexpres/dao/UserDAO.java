package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserCriteria;
import com.pinguela.rentexpres.model.UserDTO;

public interface UserDAO {

	public UserDTO findById(Connection connection, Integer id) throws DataException;

	public List<UserDTO> findAll(Connection connection) throws DataException;

	public boolean create(Connection connection, UserDTO user) throws DataException;

	public boolean update(Connection connection, UserDTO user) throws DataException;

	public boolean delete(Connection connection, Integer id) throws DataException;

	public Results<UserDTO> findByCriteria(Connection connection, UserCriteria criteria) throws DataException;

	public UserDTO authenticate(Connection connection, String username, String password) throws DataException;

	boolean activate(Connection connection, Integer userId) throws DataException;
}

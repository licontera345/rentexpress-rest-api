package com.pinguela.rentexpres.service;

import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.model.RoleDTO;

/**
 * Service interface for managing user roles. Provides read operations for
 * roles.
 */
public interface RoleService {

	/**
	 * Finds a role by its ID.
	 *
	 * @param id Role ID
	 * @return {@link RoleDTO} if found, null otherwise
	 * @throws RentexpresException On data or logic errors
	 */
	RoleDTO findById(Integer id) throws RentexpresException;

	/**
	 * Retrieves all roles.
	 *
	 * @return List of {@link RoleDTO}
	 * @throws RentexpresException On data or logic errors
	 */
	List<RoleDTO> findAll() throws RentexpresException;
}

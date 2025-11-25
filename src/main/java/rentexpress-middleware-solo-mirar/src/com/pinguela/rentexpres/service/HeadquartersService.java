package com.pinguela.rentexpres.service;

import java.util.List;
import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.HeadquartersDTO;

/**
 * Service interface for headquarters entity.
 * Encapsulates business logic related to headquarters management.
 */
public interface HeadquartersService {

    HeadquartersDTO findById(Integer id) throws DataException;

    boolean create(HeadquartersDTO headquarters) throws DataException;

    boolean update(HeadquartersDTO headquarters) throws DataException;

    boolean delete(Integer id) throws DataException;

    List<HeadquartersDTO> findAll() throws DataException;
}

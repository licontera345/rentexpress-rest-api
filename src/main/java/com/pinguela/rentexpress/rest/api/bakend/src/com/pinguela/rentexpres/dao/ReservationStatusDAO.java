package com.pinguela.rentexpres.dao;

import java.sql.Connection;
import java.util.List;

import com.pinguela.rentexpres.exception.DataException;
import com.pinguela.rentexpres.model.ReservationStatusDTO;

public interface ReservationStatusDAO {

    ReservationStatusDTO findById(Connection connection, Integer id, String isoCode) throws DataException;

    List<ReservationStatusDTO> findAll(Connection connection, String isoCode) throws DataException;
}

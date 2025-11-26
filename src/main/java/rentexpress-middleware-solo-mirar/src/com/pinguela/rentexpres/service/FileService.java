//package com.pinguela.rentexpres.service;
//
//import java.util.List;
//
//import com.pinguela.rentexpres.exception.RentexpresException;
//
//public interface FileService {
//
//    List<String> listVehicleImages(Integer vehicleId) throws RentexpresException;
//
//    byte[] getVehicleImage(Integer vehicleId, String imageName) throws RentexpresException;
//
//    void saveVehicleImage(Integer vehicleId, String imageName, byte[] data) throws RentexpresException;
//
//    void deleteVehicleImage(Integer vehicleId, String imageName) throws RentexpresException;
//
//    byte[] getUserAvatar(Integer userId) throws RentexpresException;
//
//    void saveUserAvatar(Integer userId, byte[] data) throws RentexpresException;
//
//    byte[] getEmployeeAvatar(Integer employeeId) throws RentexpresException;
//
//    void saveEmployeeAvatar(Integer employeeId, byte[] data) throws RentexpresException;
//}
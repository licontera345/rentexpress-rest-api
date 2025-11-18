package com.pinguela.rentexpres.service;

import java.io.File;
import java.util.List;

import com.pinguela.rentexpres.exception.RentexpresException;

public interface FileService {

    // VEHICLE (galería: varias imágenes numeradas 1.jpg, 2.jpg, ...)

    List<File> getImagesByVehicleId(Integer vehicleId) throws RentexpresException;

    /**
     * Sincroniza la galería del vehículo:
     * - Crea la carpeta si no existe.
     * - Borra ficheros que ya no estén en "imagenes".
     * - Copia/actualiza los ficheros de "imagenes" (añade nuevos con nombre secuencial).
     */
    void uploadImagesByVehicleId(List<File> imagenes, Integer vehicleId) throws RentexpresException;


    // USER (avatar único: profile.jpg)

    File getImageByUserId(Integer userId) throws RentexpresException;

    /**
     * Obtiene la URL pública del avatar de un usuario concreto.
     */
    String getUserAvatarUrl(Integer userId);

    /**
     * Guarda avatar del usuario como profile.jpg. Si es nulo o inváido,
     * usa la imagen por defecto configurada o de classpath.
     */
    void uploadImageByUserId(File imagen, Integer userId) throws RentexpresException;


    // EMPLOYEE (avatar único: profile.jpg)

    File getImageByEmployeeId(Integer employeeId) throws RentexpresException;

    void uploadImageByEmployeeId(File imagen, Integer employeeId) throws RentexpresException;

    /**
     * Obtiene la URL pública del avatar de un empleado concreto.
     */
    String getEmployeeAvatarUrl(Integer employeeId);

    /**
     * Construye la URL pública de una imagen de vehículo específica.
     */
    String getImageUrl(Integer vehicleId, String imageName);
}
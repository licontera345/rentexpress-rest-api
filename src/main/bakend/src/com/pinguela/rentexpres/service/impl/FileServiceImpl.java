package com.pinguela.rentexpres.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;

public class FileServiceImpl implements FileService {

    private static final Logger logger = LogManager.getLogger(FileServiceImpl.class);

    private static final String BASE_DIR_IMAGES_VEHICLE = "base.image.path.vehicle";
    private static final String BASE_DIR_IMAGES_USER = "base.image.path.user";
    private static final String BASE_DIR_IMAGES_EMPLOYEE = "base.image.path.employee";
    private static final String AVATAR_NAME = "profile.jpg";

    @Override
    public List<String> listVehicleImages(Integer vehicleId) throws RentexpresException {
        if (vehicleId == null) {
            return Collections.emptyList();
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_VEHICLE) + "/" + vehicleId);
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        String[] names = folder.list((dir, name) -> isImageName(name));
        if (names == null || names.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(names).sorted().collect(Collectors.toList());
    }

    @Override
    public byte[] getVehicleImage(Integer vehicleId, String imageName) throws RentexpresException {
        if (vehicleId == null || imageName == null || imageName.isEmpty()) {
            throw new RentexpresException("Vehicle id and image name are required");
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_VEHICLE) + "/" + vehicleId);
        File file = new File(folder, imageName);

        if (!file.exists() || !file.isFile()) {
            throw new RentexpresException("Vehicle image not found");
        }

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.error("Error reading vehicle image {}", file.getAbsolutePath(), e);
            throw new RentexpresException("Error reading vehicle image", e);
        }
    }

    @Override
    public void saveVehicleImage(Integer vehicleId, String imageName, byte[] data) throws RentexpresException {
        if (vehicleId == null || imageName == null || imageName.isEmpty()) {
            throw new RentexpresException("Vehicle id and image name are required");
        }

        if (data == null) {
            throw new RentexpresException("Image data is required");
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_VEHICLE) + "/" + vehicleId);
        ensureDirectory(folder);

        String sequentialName = getNextSequentialName(folder, imageName);
        File target = new File(folder, sequentialName);
        try {
            Files.write(target.toPath(), data);
        } catch (IOException e) {
            logger.error("Error saving vehicle image {}", target.getAbsolutePath(), e);
            throw new RentexpresException("Error saving vehicle image", e);
        }
    }

    @Override
    public void deleteVehicleImage(Integer vehicleId, String imageName) throws RentexpresException {
        if (vehicleId == null || imageName == null || imageName.isEmpty()) {
            return;
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_VEHICLE) + "/" + vehicleId);
        File target = new File(folder, imageName);
        if (target.exists() && !target.delete()) {
            logger.warn("Vehicle image could not be deleted: {}", target.getAbsolutePath());
        }
    }

    @Override
    public byte[] getUserAvatar(Integer userId) throws RentexpresException {
        if (userId == null) {
            return null;
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_USER) + "/" + userId);
        File avatar = new File(folder, AVATAR_NAME);

        if (!avatar.exists() || !avatar.isFile()) {
            return null;
        }

        try {
            return Files.readAllBytes(avatar.toPath());
        } catch (IOException e) {
            logger.error("Error reading user avatar {}", avatar.getAbsolutePath(), e);
            throw new RentexpresException("Error reading user avatar", e);
        }
    }

    @Override
    public void saveUserAvatar(Integer userId, byte[] data) throws RentexpresException {
        if (userId == null) {
            throw new RentexpresException("User id is required");
        }

        if (data == null) {
            throw new RentexpresException("Avatar data is required");
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_USER) + "/" + userId);
        ensureDirectory(folder);

        File target = new File(folder, AVATAR_NAME);
        try {
            Files.write(target.toPath(), data);
        } catch (IOException e) {
            logger.error("Error saving user avatar {}", target.getAbsolutePath(), e);
            throw new RentexpresException("Error saving user avatar", e);
        }
    }

    @Override
    public byte[] getEmployeeAvatar(Integer employeeId) throws RentexpresException {
        if (employeeId == null) {
            return null;
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_EMPLOYEE) + "/" + employeeId);
        File avatar = new File(folder, AVATAR_NAME);

        if (!avatar.exists() || !avatar.isFile()) {
            return null;
        }

        try {
            return Files.readAllBytes(avatar.toPath());
        } catch (IOException e) {
            logger.error("Error reading employee avatar {}", avatar.getAbsolutePath(), e);
            throw new RentexpresException("Error reading employee avatar", e);
        }
    }

    @Override
    public void saveEmployeeAvatar(Integer employeeId, byte[] data) throws RentexpresException {
        if (employeeId == null) {
            throw new RentexpresException("Employee id is required");
        }

        if (data == null) {
            throw new RentexpresException("Avatar data is required");
        }

        File folder = new File(getRequired(BASE_DIR_IMAGES_EMPLOYEE) + "/" + employeeId);
        ensureDirectory(folder);

        File target = new File(folder, AVATAR_NAME);
        try {
            Files.write(target.toPath(), data);
        } catch (IOException e) {
            logger.error("Error saving employee avatar {}", target.getAbsolutePath(), e);
            throw new RentexpresException("Error saving employee avatar", e);
        }
    }

    private String getRequired(String key) throws RentexpresException {
        String value = ConfigManager.getValue(key);
        if (value == null || value.isEmpty()) {
            throw new RentexpresException("Config key missing: " + key);
        }
        return value;
    }

    private void ensureDirectory(File folder) throws RentexpresException {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RentexpresException("Unable to create directory: " + folder.getAbsolutePath());
        }
        if (!folder.isDirectory()) {
            throw new RentexpresException("Path is not a directory: " + folder.getAbsolutePath());
        }
    }

    private String getNextSequentialName(File folder, String originalName) {
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        int maxNumber = 0;
        File[] files = folder.listFiles((dir, name) -> isImageName(name));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                if (dot <= 0) {
                    continue;
                }

                String numericPart = name.substring(0, dot);
                try {
                    int number = Integer.parseInt(numericPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // ignore non-numeric names
                }
            }
        }

        return (maxNumber + 1) + extension;
    }

    private boolean isImageName(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif");
    }
}

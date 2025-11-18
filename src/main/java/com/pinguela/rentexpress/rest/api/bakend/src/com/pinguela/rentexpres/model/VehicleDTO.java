package com.pinguela.rentexpres.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VehicleDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer vehicleId; // vehicle.vehicle_id
	private String brand; // vehicle.brand
	private String model; // vehicle.model
	private Integer manufactureYear; // vehicle.manufacture_year
	private BigDecimal dailyPrice; // vehicle.daily_price
	private String licensePlate; // vehicle.license_plate
	private String vinNumber; // vehicle.vin_number
	private Integer currentMileage; // vehicle.current_mileage
	private Integer vehicleStatusId;
	private Integer categoryId;
	private Integer currentHeadquartersId;
	private LocalDateTime createdAt; // vehicle.created_at
	private LocalDateTime updatedAt; // vehicle.updated_at

	// para inner join
	private List<VehicleStatusDTO> vehicleStatus;
	private List<VehicleCategoryDTO> vehicleCategory;
	private List<HeadquartersDTO> currentHeadquarters;

	public VehicleDTO() {
		super();
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getManufactureYear() {
		return manufactureYear;
	}

	public void setManufactureYear(Integer manufactureYear) {
		this.manufactureYear = manufactureYear;
	}

	public BigDecimal getDailyPrice() {
		return dailyPrice;
	}

	public void setDailyPrice(BigDecimal dailyPrice) {
		this.dailyPrice = dailyPrice;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public String getVinNumber() {
		return vinNumber;
	}

	public void setVinNumber(String vinNumber) {
		this.vinNumber = vinNumber;
	}

	public Integer getCurrentMileage() {
		return currentMileage;
	}

	public void setCurrentMileage(Integer currentMileage) {
		this.currentMileage = currentMileage;
	}

	public Integer getVehicleStatusId() {
		return vehicleStatusId;
	}

	public void setVehicleStatusId(Integer vehicleStatusId) {
		this.vehicleStatusId = vehicleStatusId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getCurrentHeadquartersId() {
		return currentHeadquartersId;
	}

	public void setCurrentHeadquartersId(Integer currentHeadquartersId) {
		this.currentHeadquartersId = currentHeadquartersId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<VehicleStatusDTO> getVehicleStatus() {
		return vehicleStatus;
	}

	public void setVehicleStatus(List<VehicleStatusDTO> vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}

	public List<VehicleCategoryDTO> getVehicleCategory() {
		return vehicleCategory;
	}

	public void setVehicleCategory(List<VehicleCategoryDTO> vehicleCategory) {
		this.vehicleCategory = vehicleCategory;
	}

	public List<HeadquartersDTO> getCurrentHeadquarters() {
		return currentHeadquarters;
	}

	public void setCurrentHeadquarters(List<HeadquartersDTO> currentHeadquarters) {
		this.currentHeadquarters = currentHeadquarters;
	}


}

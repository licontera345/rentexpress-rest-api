package com.pinguela.rentexpres.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Criteria class to dynamically filter vehicles. Each non-null field will
 * generate a WHERE condition in DAO queries.
 */
public class VehicleCriteria extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer vehicleId; // vehicle.vehicle_id
	private Integer vehicleStatusId; // FK → vehicle_status.vehicle_status_id
	private Integer categoryId; // FK → vehicle_category.category_id
	private Integer currentHeadquartersId; // FK → headquarters.headquarters_id

	private String brand; // vehicle.brand
	private String model; // vehicle.model
	private String licensePlate; // vehicle.license_plate
	private String vinNumber; // vehicle.vin_number

	private Integer manufactureYearFrom; // vehicle.manufacture_year >= ...
	private Integer manufactureYearTo; // vehicle.manufacture_year <= ...
	private BigDecimal dailyPriceMin; // vehicle.daily_price >= ...
	private BigDecimal dailyPriceMax; // vehicle.daily_price <= ...
	private Integer currentMileageMin; // vehicle.current_mileage >= ...
	private Integer currentMileageMax; // vehicle.current_mileage <= ...

	private Boolean activeStatus; // optional, if you add logical state

	private Integer pageNumber;
	private Integer pageSize;

	private LocalDateTime createdAtFrom;
	private LocalDateTime createdAtTo;
	private LocalDateTime updatedAtFrom;
	private LocalDateTime updatedAtTo;

	public VehicleCriteria() {
		super();
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
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

	public Integer getManufactureYearFrom() {
		return manufactureYearFrom;
	}

	public void setManufactureYearFrom(Integer manufactureYearFrom) {
		this.manufactureYearFrom = manufactureYearFrom;
	}

	public Integer getManufactureYearTo() {
		return manufactureYearTo;
	}

	public void setManufactureYearTo(Integer manufactureYearTo) {
		this.manufactureYearTo = manufactureYearTo;
	}

	public BigDecimal getDailyPriceMin() {
		return dailyPriceMin;
	}

	public void setDailyPriceMin(BigDecimal dailyPriceMin) {
		this.dailyPriceMin = dailyPriceMin;
	}

	public BigDecimal getDailyPriceMax() {
		return dailyPriceMax;
	}

	public void setDailyPriceMax(BigDecimal dailyPriceMax) {
		this.dailyPriceMax = dailyPriceMax;
	}

	public Integer getCurrentMileageMin() {
		return currentMileageMin;
	}

	public void setCurrentMileageMin(Integer currentMileageMin) {
		this.currentMileageMin = currentMileageMin;
	}

	public Integer getCurrentMileageMax() {
		return currentMileageMax;
	}

	public void setCurrentMileageMax(Integer currentMileageMax) {
		this.currentMileageMax = currentMileageMax;
	}

	public Boolean getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(Boolean activeStatus) {
		this.activeStatus = activeStatus;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public LocalDateTime getCreatedAtFrom() {
		return createdAtFrom;
	}

	public void setCreatedAtFrom(LocalDateTime createdAtFrom) {
		this.createdAtFrom = createdAtFrom;
	}

	public LocalDateTime getCreatedAtTo() {
		return createdAtTo;
	}

	public void setCreatedAtTo(LocalDateTime createdAtTo) {
		this.createdAtTo = createdAtTo;
	}

	public LocalDateTime getUpdatedAtFrom() {
		return updatedAtFrom;
	}

	public void setUpdatedAtFrom(LocalDateTime updatedAtFrom) {
		this.updatedAtFrom = updatedAtFrom;
	}

	public LocalDateTime getUpdatedAtTo() {
		return updatedAtTo;
	}

	public void setUpdatedAtTo(LocalDateTime updatedAtTo) {
		this.updatedAtTo = updatedAtTo;
	}

}

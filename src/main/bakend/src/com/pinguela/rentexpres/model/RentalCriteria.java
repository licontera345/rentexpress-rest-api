package com.pinguela.rentexpres.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentalCriteria extends ValueObject {
	/**
	  * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer rentalId;
	private Integer rentalStatusId;
	private Integer reservationId;
	private Integer userId;
	private Integer employeeId;
	private Integer vehicleId;
	private Integer pickupHeadquartersId;
	private Integer returnHeadquartersId;

	private LocalDateTime startDateEffectiveFrom;
	private LocalDateTime startDateEffectiveTo;
	private LocalDateTime endDateEffectiveFrom;
	private LocalDateTime endDateEffectiveTo;
	private LocalDateTime createdAtFrom;
	private LocalDateTime createdAtTo;
	private LocalDateTime updatedAtFrom;
	private LocalDateTime updatedAtTo;

	private Integer initialKmMin;
	private Integer initialKmMax;
	private Integer finalKmMin;
	private Integer finalKmMax;

	private BigDecimal totalCostMin;
	private BigDecimal totalCostMax;

	private LocalDateTime startDateEffective;
	private LocalDateTime endDateEffective;
	private Integer initialKm;
	private Integer finalKm;
	private BigDecimal totalCost;
	private String userFirstName;
	private String userLastName1;
	private String phone;
	private String licensePlate;
	private String brand;
	private String model;

	private Integer pageNumber;
	private Integer pageSize;

	public RentalCriteria() {
		super();
	}

	public Integer getRentalId() {
		return rentalId;
	}

	public void setRentalId(Integer rentalId) {
		this.rentalId = rentalId;
	}

	public Integer getRentalStatusId() {
		return rentalStatusId;
	}

	public void setRentalStatusId(Integer rentalStatusId) {
		this.rentalStatusId = rentalStatusId;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}

	public Integer getPickupHeadquartersId() {
		return pickupHeadquartersId;
	}

	public void setPickupHeadquartersId(Integer pickupHeadquartersId) {
		this.pickupHeadquartersId = pickupHeadquartersId;
	}

	public Integer getReturnHeadquartersId() {
		return returnHeadquartersId;
	}

	public void setReturnHeadquartersId(Integer returnHeadquartersId) {
		this.returnHeadquartersId = returnHeadquartersId;
	}

	public LocalDateTime getStartDateEffectiveFrom() {
		return startDateEffectiveFrom;
	}

	public void setStartDateEffectiveFrom(LocalDateTime startDateEffectiveFrom) {
		this.startDateEffectiveFrom = startDateEffectiveFrom;
	}

	public LocalDateTime getStartDateEffectiveTo() {
		return startDateEffectiveTo;
	}

	public void setStartDateEffectiveTo(LocalDateTime startDateEffectiveTo) {
		this.startDateEffectiveTo = startDateEffectiveTo;
	}

	public LocalDateTime getEndDateEffectiveFrom() {
		return endDateEffectiveFrom;
	}

	public void setEndDateEffectiveFrom(LocalDateTime endDateEffectiveFrom) {
		this.endDateEffectiveFrom = endDateEffectiveFrom;
	}

	public LocalDateTime getEndDateEffectiveTo() {
		return endDateEffectiveTo;
	}

	public void setEndDateEffectiveTo(LocalDateTime endDateEffectiveTo) {
		this.endDateEffectiveTo = endDateEffectiveTo;
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

	public Integer getInitialKmMin() {
		return initialKmMin;
	}

	public void setInitialKmMin(Integer initialKmMin) {
		this.initialKmMin = initialKmMin;
	}

	public Integer getInitialKmMax() {
		return initialKmMax;
	}

	public void setInitialKmMax(Integer initialKmMax) {
		this.initialKmMax = initialKmMax;
	}

	public Integer getFinalKmMin() {
		return finalKmMin;
	}

	public void setFinalKmMin(Integer finalKmMin) {
		this.finalKmMin = finalKmMin;
	}

	public Integer getFinalKmMax() {
		return finalKmMax;
	}

	public void setFinalKmMax(Integer finalKmMax) {
		this.finalKmMax = finalKmMax;
	}

	public BigDecimal getTotalCostMin() {
		return totalCostMin;
	}

	public void setTotalCostMin(BigDecimal totalCostMin) {
		this.totalCostMin = totalCostMin;
	}

	public BigDecimal getTotalCostMax() {
		return totalCostMax;
	}

	public void setTotalCostMax(BigDecimal totalCostMax) {
		this.totalCostMax = totalCostMax;
	}

	public LocalDateTime getStartDateEffective() {
		return startDateEffective;
	}

	public void setStartDateEffective(LocalDateTime startDateEffective) {
		this.startDateEffective = startDateEffective;
	}

	public LocalDateTime getEndDateEffective() {
		return endDateEffective;
	}

	public void setEndDateEffective(LocalDateTime endDateEffective) {
		this.endDateEffective = endDateEffective;
	}

	public Integer getInitialKm() {
		return initialKm;
	}

	public void setInitialKm(Integer initialKm) {
		this.initialKm = initialKm;
	}

	public Integer getFinalKm() {
		return finalKm;
	}

	public void setFinalKm(Integer finalKm) {
		this.finalKm = finalKm;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName1() {
		return userLastName1;
	}

	public void setUserLastName1(String userLastName1) {
		this.userLastName1 = userLastName1;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
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


}

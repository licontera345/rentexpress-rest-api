package com.pinguela.rentexpres.model;

import java.time.LocalDateTime;

/**
 * Criteria class used to dynamically filter reservations. Each non-null
 * attribute will be added as a WHERE condition in DAO.
 */
public class ReservationCriteria extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer reservationId;
	private Integer vehicleId;
	private Integer userId;
	private Integer employeeId;
	private Integer reservationStatusId;
	private Integer pickupHeadquartersId;
	private Integer returnHeadquartersId;

	private LocalDateTime startDateFrom;
	private LocalDateTime startDateTo;
	private LocalDateTime endDateFrom;
	private LocalDateTime endDateTo;
	private LocalDateTime createdAtFrom;
	private LocalDateTime createdAtTo;
	private LocalDateTime updatedAtFrom;
	private LocalDateTime updatedAtTo;

	private Integer pageNumber;
	private Integer pageSize;

	public ReservationCriteria() {
		super();
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public Integer getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
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

	public Integer getReservationStatusId() {
		return reservationStatusId;
	}

	public void setReservationStatusId(Integer reservationStatusId) {
		this.reservationStatusId = reservationStatusId;
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

	public LocalDateTime getStartDateFrom() {
		return startDateFrom;
	}

	public void setStartDateFrom(LocalDateTime startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	public LocalDateTime getStartDateTo() {
		return startDateTo;
	}

	public void setStartDateTo(LocalDateTime startDateTo) {
		this.startDateTo = startDateTo;
	}

	public LocalDateTime getEndDateFrom() {
		return endDateFrom;
	}

	public void setEndDateFrom(LocalDateTime endDateFrom) {
		this.endDateFrom = endDateFrom;
	}

	public LocalDateTime getEndDateTo() {
		return endDateTo;
	}

	public void setEndDateTo(LocalDateTime endDateTo) {
		this.endDateTo = endDateTo;
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

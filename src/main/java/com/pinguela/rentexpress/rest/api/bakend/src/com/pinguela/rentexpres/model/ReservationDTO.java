package com.pinguela.rentexpres.model;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer reservationId; // reservation.reservation_id
	private Integer vehicleId; // FK → vehicle.vehicle_id
	private Integer userId; // FK → user.user_id
	private Integer employeeId; // FK → employee.employee_id
	private Integer reservationStatusId; // FK → reservation_status.reservation_status_id
	private Integer pickupHeadquartersId; // FK → headquarters.headquarters_id
	private Integer returnHeadquartersId; // FK → headquarters.headquarters_id
	private LocalDateTime startDate; // reservation.start_date
	private LocalDateTime endDate; // reservation.end_date
	private LocalDateTime createdAt; // reservation.created_at
	private LocalDateTime updatedAt; // reservation.updated_at

	// relational objects (for joins)
	private List<VehicleDTO> vehicle;
	private List<EmployeeDTO> employee;
	private ReservationStatusDTO reservationStatus;

	private List<HeadquartersDTO> pickupHeadquarters;
	private List<HeadquartersDTO> returnHeadquarters;

	public ReservationDTO() {
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

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
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

	public List<VehicleDTO> getVehicle() {
		return vehicle;
	}

	public void setVehicle(List<VehicleDTO> vehicle) {
		this.vehicle = vehicle;
	}

	public List<EmployeeDTO> getEmployee() {
		return employee;
	}

	public void setEmployee(List<EmployeeDTO> employee) {
		this.employee = employee;
	}

	public ReservationStatusDTO getReservationStatus() {
		return reservationStatus;
	}

	public void setReservationStatus(ReservationStatusDTO reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	public List<HeadquartersDTO> getPickupHeadquarters() {
		return pickupHeadquarters;
	}

	public void setPickupHeadquarters(List<HeadquartersDTO> pickupHeadquarters) {
		this.pickupHeadquarters = pickupHeadquarters;
	}

	public List<HeadquartersDTO> getReturnHeadquarters() {
		return returnHeadquarters;
	}

	public void setReturnHeadquarters(List<HeadquartersDTO> returnHeadquarters) {
		this.returnHeadquarters = returnHeadquarters;
	}


}

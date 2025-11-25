package com.pinguela.rentexpres.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Criteria class to dynamically filter system users (clients). Each non-null
 * field will generate a WHERE condition in DAO queries.
 */
public class UserCriteria extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer userId; // user.user_id
	private Integer roleId; // FK → role.role_id
	private Integer addressId; // FK → address.address_id

	private String username; // user.username (login)
	private String firstName; // user.first_name
	private String lastName1; // user.last_name1
	private String lastName2; // user.last_name2
	private String email; // user.email
	private String phone; // user.phone

	private LocalDate birthDateFrom; // user.birth_date >= ...
	private LocalDate birthDateTo; // user.birth_date <= ...

	private Boolean activeStatus; // user.active_status (tinyint(1))

	private Integer pageNumber;
	private Integer pageSize;

	private LocalDateTime createdAtFrom;
	private LocalDateTime createdAtTo;
	private LocalDateTime updatedAtFrom;
	private LocalDateTime updatedAtTo;

	public UserCriteria() {
		super();
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName1() {
		return lastName1;
	}

	public void setLastName1(String lastName1) {
		this.lastName1 = lastName1;
	}

	public String getLastName2() {
		return lastName2;
	}

	public void setLastName2(String lastName2) {
		this.lastName2 = lastName2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDate getBirthDateFrom() {
		return birthDateFrom;
	}

	public void setBirthDateFrom(LocalDate birthDateFrom) {
		this.birthDateFrom = birthDateFrom;
	}

	public LocalDate getBirthDateTo() {
		return birthDateTo;
	}

	public void setBirthDateTo(LocalDate birthDateTo) {
		this.birthDateTo = birthDateTo;
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

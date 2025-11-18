package com.pinguela.rentexpres.model;

import java.time.LocalDateTime;

public class EmployeeDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer Id; // employee.employee_id
	private String employeeName; // employee.employee_name
	private String password; // employee.password
	private Integer roleId;
	private Integer headquartersId;
	private String firstName; // employee.first_name
	private String lastName1; // employee.last_name1
	private String lastName2; // employee.last_name2
	private String email; // employee.email
	private String phone; // employee.phone
	private Boolean activeStatus; // employee.active_status (tinyint 1/0)
	private LocalDateTime createdAt; // employee.created_at
	private LocalDateTime updatedAt; // employee.updated_at

	private HeadquartersDTO headquarters;
	private RoleDTO role;

	public EmployeeDTO() {
		super();
	}

	
	public RoleDTO getRole() {
		return role;
	}


	public void setRole(RoleDTO role) {
		this.role = role;
	}


	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public Integer getHeadquartersId() {
		return headquartersId;
	}

	public void setHeadquartersId(Integer headquartersId) {
		this.headquartersId = headquartersId;
	}

	public Integer getId() {
		return Id;
	}

	public void setId(Integer Id) {
		this.Id = Id;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Boolean getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(Boolean activeStatus) {
		this.activeStatus = activeStatus;
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

	public HeadquartersDTO getHeadquarters() {
		return headquarters;
	}

	public void setHeadquarters(HeadquartersDTO headquarters) {
		this.headquarters = headquarters;
	}

}

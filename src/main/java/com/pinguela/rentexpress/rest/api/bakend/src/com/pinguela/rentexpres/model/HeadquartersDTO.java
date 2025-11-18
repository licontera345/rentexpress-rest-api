package com.pinguela.rentexpres.model;

import java.time.LocalDateTime;
import java.util.List;

public class HeadquartersDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer Id; // headquarters.headquarters_id
	private String name; // headquarters.name
	private String phone; // headquarters.phone
	private String email; // headquarters.email
	private Integer addressId;
	private LocalDateTime createdAt; // headquarters.created_at
	private LocalDateTime updatedAt; // headquarters.updated_at

	// addressDTO
	private List<AddressDTO> addresses;
	private CityDTO city;
	private ProvinceDTO province;

	public HeadquartersDTO() {
		super();
	}

	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public List<AddressDTO> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<AddressDTO> addresses) {
		this.addresses = addresses;
	}

	public CityDTO getCity() {
		return city;
	}

	public void setCity(CityDTO city) {
		this.city = city;
	}

	public ProvinceDTO getProvince() {
		return province;
	}

	public void setProvince(ProvinceDTO province) {
		this.province = province;
	}

}

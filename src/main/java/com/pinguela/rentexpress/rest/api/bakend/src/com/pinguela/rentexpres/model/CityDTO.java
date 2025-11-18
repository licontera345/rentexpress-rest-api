package com.pinguela.rentexpres.model;

public class CityDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer Id; // city.city_id
	private String cityName; // city.city_name
	private Integer provinceId;

	private ProvinceDTO province;

	public CityDTO() {
		super();
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getId() {
		return Id;
	}

	public void setId(Integer Id) {
		this.Id = Id;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public ProvinceDTO getProvince() {
		return province;
	}

	public void setProvince(ProvinceDTO province) {
		this.province = province;
	}

}

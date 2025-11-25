package com.pinguela.rentexpres.model;

public class ProvinceDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer provinceId;
	private String provinceName;

	public ProvinceDTO() {
		super();
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

}

package com.pinguela.rentexpres.model;

import java.util.List;

public class VehicleStatusDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer vehicleStatusId; // vehicle_status.vehicle_status_id
	private String statusName; // vehicle_status.status_name

	private List<LanguageDTO> language;

	public VehicleStatusDTO() {
		super();
	}

	public Integer getVehicleStatusId() {
		return vehicleStatusId;
	}

	public void setVehicleStatusId(Integer vehicleStatusId) {
		this.vehicleStatusId = vehicleStatusId;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public List<LanguageDTO> getLanguage() {
		return language;
	}

	public void setLanguage(List<LanguageDTO> language) {
		this.language = language;
	}

}

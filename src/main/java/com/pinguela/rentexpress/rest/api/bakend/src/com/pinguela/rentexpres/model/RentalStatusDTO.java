package com.pinguela.rentexpres.model;

import java.util.List;

public class RentalStatusDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer rentalStatusId; // rental_status.rental_status_id
	private String statusName; // rental_status.status_name

	private List<LanguageDTO> language;

	public RentalStatusDTO() {
		super();
	}

	public Integer getRentalStatusId() {
		return rentalStatusId;
	}

	public void setRentalStatusId(Integer rentalStatusId) {
		this.rentalStatusId = rentalStatusId;
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

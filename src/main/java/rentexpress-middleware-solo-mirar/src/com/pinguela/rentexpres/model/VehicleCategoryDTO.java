package com.pinguela.rentexpres.model;

import java.util.List;

public class VehicleCategoryDTO extends ValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer categoryId; // vehicle_category.category_id
	private String categoryName; // vehicle_category.category_name

	private List<LanguageDTO> language;

	public VehicleCategoryDTO() {
		super();
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<LanguageDTO> getLanguage() {
		return language;
	}

	public void setLanguage(List<LanguageDTO> language) {
		this.language = language;
	}

}

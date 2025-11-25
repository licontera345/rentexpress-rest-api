package com.pinguela.rentexpres.model;

public class LanguageDTO extends ValueObject {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer languageId;
    private String isoCode;
    private String name;

    public LanguageDTO() { 
    	
    	super();
    }

	public Integer getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
    
}

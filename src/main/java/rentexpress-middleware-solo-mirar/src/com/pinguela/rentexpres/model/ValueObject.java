package com.pinguela.rentexpres.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ValueObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValueObject() {
		super();
	}

	@SuppressWarnings("static-access")
  public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE.MULTI_LINE_STYLE);
	}

}

package com.pinguela.rentexpres.model;

import java.util.List;

public class Results<T> extends ValueObject {
	/**
	 *  
	 */
	private static final long serialVersionUID = 1L;

	private List<T> results;
	private Integer pageNumber;
	private Integer pageSize;
	private Integer totalRecords;
	private Integer totalPages;

	public Results() {
		super();
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
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

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public Integer getTotalPages() {
		if (totalPages != null) {
			return totalPages;
		}

		if (pageSize == null || pageSize <= 0 || totalRecords == null) {
			return 0;
		}

		int pages = totalRecords / pageSize;
		if (totalRecords % pageSize != 0) {
			pages++;
		}
		return pages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

}
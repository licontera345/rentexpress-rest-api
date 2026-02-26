package com.pinguela.rentexpress.rest.api.dto;

public class FinalizarMantenimiento {
	private String matricula;
	private String descripcion;

	public FinalizarMantenimiento() {
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
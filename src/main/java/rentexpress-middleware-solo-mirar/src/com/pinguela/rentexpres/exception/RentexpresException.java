package com.pinguela.rentexpres.exception;

/**
 * Excepción para violaciones de reglas de negocio.
 */
public class RentexpresException extends Exception {

	public RentexpresException() {
		super();
	}

	public RentexpresException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RentexpresException(String message, Throwable cause) {
		super(message, cause);
	}

	public RentexpresException(String message) {
		super(message);
	}

	public RentexpresException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}

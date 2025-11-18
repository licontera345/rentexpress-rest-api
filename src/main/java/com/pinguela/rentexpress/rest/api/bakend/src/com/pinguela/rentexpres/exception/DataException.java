package com.pinguela.rentexpres.exception;

/**
 * Excepción para errores relacionados con el acceso a datos.
 */
public class DataException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataException(Object object) {
        super();
    }
    
    public DataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataException(Throwable cause) {
        super(cause);
    }
}

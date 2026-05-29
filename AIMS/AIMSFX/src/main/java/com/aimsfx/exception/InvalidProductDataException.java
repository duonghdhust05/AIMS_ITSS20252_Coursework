package com.aimsfx.exception;

public class InvalidProductDataException extends Exception {
    
    private static final long serialVersionUID = 1L;

	public InvalidProductDataException() {
        super();
    }
    
    public InvalidProductDataException(String message) {
        super(message);
    }
    
    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidProductDataException(Throwable cause) {
        super(cause);
    }
}

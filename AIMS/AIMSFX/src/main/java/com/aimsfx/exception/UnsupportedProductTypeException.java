package com.aimsfx.exception;

public class UnsupportedProductTypeException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

	public UnsupportedProductTypeException() {
        super();
    }
    
    public UnsupportedProductTypeException(String message) {
        super(message);
    }
    
    public UnsupportedProductTypeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnsupportedProductTypeException(Throwable cause) {
        super(cause);
    }
}

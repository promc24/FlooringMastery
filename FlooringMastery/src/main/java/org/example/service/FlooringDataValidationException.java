package org.example.service;

public class FlooringDataValidationException extends Exception{

    public FlooringDataValidationException(String message) {
        super(message);
    }

    public FlooringDataValidationException(String message,
                                              Throwable cause) {
        super(message, cause);
    }
}

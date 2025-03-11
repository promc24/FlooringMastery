package org.example.service;

public class FlooringDuplicateIdException extends Exception{

    public FlooringDuplicateIdException(String message) {
        super(message);
    }

    public FlooringDuplicateIdException(String message, Throwable cause) {
        super(message, cause);
    }
}

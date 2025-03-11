package org.example.service;

public class FlooringPersistenceException extends RuntimeException {
    public FlooringPersistenceException(String message) {
        super(message);
    }

    public FlooringPersistenceException(String message, Throwable cause){
        super(message, cause);
    }
}

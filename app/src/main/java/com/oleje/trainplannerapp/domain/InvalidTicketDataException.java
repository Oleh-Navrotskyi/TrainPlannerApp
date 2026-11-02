package com.oleje.trainplannerapp.domain;

/**
 * Custom exception thrown when ticket parameters are invalid (e.g. negative price, empty city).
 */
public class InvalidTicketDataException extends Exception {
    public InvalidTicketDataException(String message) {
        super(message);
    }

    public InvalidTicketDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

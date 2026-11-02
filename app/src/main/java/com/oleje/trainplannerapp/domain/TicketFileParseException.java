package com.oleje.trainplannerapp.domain;

/**
 * Custom exception thrown when parsing a formatted ticket .txt file fails.
 */
public class TicketFileParseException extends Exception {
    public TicketFileParseException(String message) {
        super(message);
    }

    public TicketFileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

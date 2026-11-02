package com.oleje.trainplannerapp.domain;

/**
 * Custom exception thrown when no route exists between origin and destination stations.
 */
public class RouteNotFoundException extends Exception {
    public RouteNotFoundException(String message) {
        super(message);
    }
}

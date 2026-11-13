package com.group12.parking.exception;

/**
 * Custom checked exception thrown when no parking slots of the requested type are available.
 * Demonstrates Unit 3: Custom checked exception handling.
 */
public class ParkingFullException extends Exception { // Unit 3: Custom checked exception
    public ParkingFullException(String message) {
        super(message);
    }
}

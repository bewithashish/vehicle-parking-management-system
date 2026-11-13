package com.group12.parking.exception;

/**
 * General checked exception for parking business rule violations (e.g., duplicate vehicle, ticket not found).
 * Demonstrates Unit 3: Checked exception hierarchies.
 */
public class ParkingException extends Exception { // Unit 3: Checked exception
    public ParkingException(String message) {
        super(message);
    }

    public ParkingException(String message, Throwable cause) {
        super(message, cause);
    }
}

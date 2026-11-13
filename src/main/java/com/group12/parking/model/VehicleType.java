package com.group12.parking.model;

/**
 * Enumeration of supported vehicle types.
 * Demonstrates Unit 1: Enum types with custom properties and methods.
 */
public enum VehicleType {
    BIKE("Bike", "B"),
    CAR("Car", "C"),
    TRUCK("Truck", "T");

    private final String displayName;
    private final String prefix;

    VehicleType(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}

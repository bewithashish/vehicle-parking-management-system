package com.group12.parking.model;

import com.group12.parking.util.Constants;

/**
 * Concrete Vehicle subclass representing a Truck (Heavy vehicle).
 * Demonstrates Unit 2: Inheritance (extends Vehicle) and method overriding.
 */
public class Truck extends Vehicle { // Unit 2: Inheritance

    // Unit 1: Constructor calling super constructor
    public Truck(String vehicleNumber, String ownerName) {
        super(vehicleNumber, ownerName); // Unit 2: super keyword usage
    }

    // Unit 1: Overloaded constructor with ID
    public Truck(int vehicleId, String vehicleNumber, String ownerName) {
        super(vehicleId, vehicleNumber, ownerName); // Unit 2: super keyword usage
    }

    // Unit 2: Method overriding for rate
    @Override
    public double getRatePerHour() {
        return Constants.TRUCK_RATE_PER_HOUR;
    }

    // Unit 2: Method overriding for vehicle type
    @Override
    public VehicleType getType() {
        return VehicleType.TRUCK;
    }
}

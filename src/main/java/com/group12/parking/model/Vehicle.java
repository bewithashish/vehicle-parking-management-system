package com.group12.parking.model;

/**
 * Abstract base class representing a generic vehicle in the parking system.
 * Demonstrates Unit 1 (Encapsulation, constructors, getters/setters, toString)
 * and Unit 2 (Abstract classes, interface implementation, factory pattern, polymorphism).
 */
public abstract class Vehicle implements Chargeable { // Unit 2: Abstract class implementing interface
    // Unit 1: Private encapsulated fields
    private int vehicleId;
    private String vehicleNumber;
    private String ownerName;

    // Unit 1: Constructor for new vehicle
    public Vehicle(String vehicleNumber, String ownerName) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
    }

    // Unit 1: Overloaded constructor with vehicleId (from DB)
    public Vehicle(int vehicleId, String vehicleNumber, String ownerName) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
    }

    // Unit 2: Abstract methods to be implemented by concrete vehicle subclasses
    public abstract double getRatePerHour();
    public abstract VehicleType getType();

    // Unit 2: Implementation of Chargeable interface using polymorphic getRatePerHour()
    @Override
    public double calculateCharge(long minutes) {
        // Billing rule: billable_hours = max(1, ceil(minutes / 60.0))
        long billableHours = Math.max(1, (long) Math.ceil(minutes / 60.0));
        return billableHours * getRatePerHour(); // Unit 2: Polymorphism in action
    }

    // Unit 2: Factory method returning polymorphic Vehicle instances
    public static Vehicle create(VehicleType type, String vehicleNumber, String ownerName) {
        return switch (type) {
            case BIKE -> new Bike(vehicleNumber, ownerName);
            case CAR -> new Car(vehicleNumber, ownerName);
            case TRUCK -> new Truck(vehicleNumber, ownerName);
        };
    }

    // Unit 2: Overloaded factory method with ID
    public static Vehicle create(int vehicleId, VehicleType type, String vehicleNumber, String ownerName) {
        return switch (type) {
            case BIKE -> new Bike(vehicleId, vehicleNumber, ownerName);
            case CAR -> new Car(vehicleId, vehicleNumber, ownerName);
            case TRUCK -> new Truck(vehicleId, vehicleNumber, ownerName);
        };
    }

    // Unit 1: Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    // Unit 1: toString method
    @Override
    public String toString() {
        return getType() + " [Number=" + vehicleNumber + ", Owner=" + ownerName + "]";
    }
}

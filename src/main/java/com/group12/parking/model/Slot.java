package com.group12.parking.model;

/**
 * Model class representing a parking slot.
 * Demonstrates Unit 1: Class design, encapsulation, constructors, getters/setters, and toString.
 */
public class Slot {
    // Unit 1: Encapsulated fields
    private int slotId;
    private String slotNumber;
    private VehicleType vehicleType;
    private boolean occupied;

    // Unit 1: Constructor for new slot before DB insertion
    public Slot(String slotNumber, VehicleType vehicleType, boolean occupied) {
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.occupied = occupied;
    }

    // Unit 1: Overloaded constructor with slotId from DB
    public Slot(int slotId, String slotNumber, VehicleType vehicleType, boolean occupied) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.occupied = occupied;
    }

    // Unit 1: Getters and Setters
    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    // Unit 1: toString method
    @Override
    public String toString() {
        return "Slot{" +
                "slotId=" + slotId +
                ", slotNumber='" + slotNumber + '\'' +
                ", vehicleType=" + vehicleType +
                ", occupied=" + occupied +
                '}';
    }
}

package com.group12.parking.model;

import java.time.LocalDateTime;

/**
 * Model class representing a parking ticket issued at entry and settled at exit.
 * Demonstrates Unit 1: Class design, constructors, method overloading, encapsulation, and toString.
 */
public class Ticket {
    // Unit 1: Encapsulated private attributes
    private int ticketId;
    private int vehicleId;
    private int slotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double fee;
    private String status; // "ACTIVE" or "COMPLETED"

    // Convenience display fields populated during query joins
    private String vehicleNumber;
    private String ownerName;
    private VehicleType vehicleType;
    private String slotNumber;

    // Unit 1: Overloaded constructor 1 - New active ticket being created at entry
    public Ticket(int vehicleId, int slotId, LocalDateTime entryTime) {
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.status = "ACTIVE";
    }

    // Unit 1: Overloaded constructor 2 - Active ticket with generated ticketId
    public Ticket(int ticketId, int vehicleId, int slotId, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.status = "ACTIVE";
    }

    // Unit 1: Overloaded constructor 3 - Complete ticket loaded from database (with exit details)
    public Ticket(int ticketId, int vehicleId, int slotId, LocalDateTime entryTime,
                  LocalDateTime exitTime, Double fee, String status) {
        this.ticketId = ticketId;
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = status;
    }

    // Unit 1: Overloaded constructor 4 - Full details including joined vehicle and slot display info
    public Ticket(int ticketId, int vehicleId, int slotId, LocalDateTime entryTime,
                  LocalDateTime exitTime, Double fee, String status,
                  String vehicleNumber, String ownerName, VehicleType vehicleType, String slotNumber) {
        this(ticketId, vehicleId, slotId, entryTime, exitTime, fee, status);
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
    }

    // Unit 1: Standard Getters and Setters
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    // Unit 1: toString method
    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", slotNumber='" + slotNumber + '\'' +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", fee=" + fee +
                ", status='" + status + '\'' +
                '}';
    }
}

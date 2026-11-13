package com.group12.parking.service;

import com.group12.parking.model.VehicleType;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) containing receipt details generated when a vehicle exits.
 * Demonstrates Unit 1: Encapsulated data container and formatted string presentation.
 */
public class ReceiptInfo {
    private final int ticketId;
    private final String vehicleNumber;
    private final String ownerName;
    private final VehicleType vehicleType;
    private final String slotNumber;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final long durationMinutes;
    private final long billableHours;
    private final double hourlyRate;
    private final double discountAmount;
    private final double totalFee;
    private final boolean isMember;

    public ReceiptInfo(int ticketId, String vehicleNumber, String ownerName, VehicleType vehicleType,
                       String slotNumber, LocalDateTime entryTime, LocalDateTime exitTime,
                       long durationMinutes, long billableHours, double hourlyRate,
                       double discountAmount, double totalFee, boolean isMember) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationMinutes = durationMinutes;
        this.billableHours = billableHours;
        this.hourlyRate = hourlyRate;
        this.discountAmount = discountAmount;
        this.totalFee = totalFee;
        this.isMember = isMember;
    }

    public int getTicketId() { return ticketId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getSlotNumber() { return slotNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public long getDurationMinutes() { return durationMinutes; }
    public long getBillableHours() { return billableHours; }
    public double getHourlyRate() { return hourlyRate; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotalFee() { return totalFee; }
    public boolean isMember() { return isMember; }
}

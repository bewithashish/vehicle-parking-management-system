package com.group12.parking.service;

import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;
import com.group12.parking.util.Constants;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service calculating parking charges based on vehicle type, parked duration, and membership.
 * Demonstrates Unit 1: Method Overloading (compile-time polymorphism) and encapsulation.
 */
public class FeeCalculator {

    /**
     * Unit 1: Overloaded method 1 - Calculates standard parking fee based on minutes and vehicle type.
     * Billing rule: billable_hours = max(1, ceil(minutes / 60.0)). Fee = billable_hours * rate.
     */
    public double calculateFee(long minutes, VehicleType type) { // Unit 1: Method Overloading
        long billableHours = calculateBillableHours(minutes);
        double rate = getRate(type);
        return billableHours * rate;
    }

    /**
     * Unit 1: Overloaded method 2 - Calculates fee applying member discount (10%) if applicable.
     */
    public double calculateFee(long minutes, VehicleType type, boolean isMember) { // Unit 1: Method Overloading
        double baseFee = calculateFee(minutes, type); // Call overloaded method
        if (isMember) {
            double discount = baseFee * Constants.MEMBER_DISCOUNT_PERCENTAGE;
            return baseFee - discount;
        }
        return baseFee;
    }

    /**
     * Unit 1: Overloaded method 3 - Calculates fee for a Ticket using its entry time, exit time (or now), and type.
     */
    public double calculateFee(Ticket ticket) { // Unit 1: Method Overloading
        return calculateFee(ticket, false);
    }

    /**
     * Unit 1: Overloaded method 4 - Calculates fee for a Ticket with membership consideration.
     */
    public double calculateFee(Ticket ticket, boolean isMember) { // Unit 1: Method Overloading
        LocalDateTime start = ticket.getEntryTime();
        LocalDateTime end = ticket.getExitTime() != null ? ticket.getExitTime() : LocalDateTime.now();
        long minutes = Math.max(0, Duration.between(start, end).toMinutes());
        return calculateFee(minutes, ticket.getVehicleType(), isMember);
    }

    /**
     * Helper to compute billable hours: ceiling of minutes divided by 60, minimum 1 hour.
     */
    public static long calculateBillableHours(long minutes) {
        if (minutes <= 0) {
            return 1;
        }
        return Math.max(1, (long) Math.ceil(minutes / 60.0));
    }

    /**
     * Helper to fetch hourly rate by vehicle type.
     */
    public static double getRate(VehicleType type) {
        return switch (type) {
            case BIKE -> Constants.BIKE_RATE_PER_HOUR;
            case CAR -> Constants.CAR_RATE_PER_HOUR;
            case TRUCK -> Constants.TRUCK_RATE_PER_HOUR;
        };
    }
}

package com.group12.parking.service;

import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for FeeCalculator.
 * Demonstrates Unit 1: Method Overloading testing and fee calculation rules.
 */
class FeeCalculatorTest {
    private FeeCalculator feeCalculator;

    @BeforeEach
    void setUp() {
        feeCalculator = new FeeCalculator();
    }

    @Test
    @DisplayName("1 minute duration bills for 1 hour minimum")
    void testOneMinuteBillsForOneHour() {
        assertEquals(10.0, feeCalculator.calculateFee(1, VehicleType.BIKE));
        assertEquals(20.0, feeCalculator.calculateFee(1, VehicleType.CAR));
        assertEquals(40.0, feeCalculator.calculateFee(1, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("60 minutes duration bills for exactly 1 hour")
    void testSixtyMinutesBillsForOneHour() {
        assertEquals(10.0, feeCalculator.calculateFee(60, VehicleType.BIKE));
        assertEquals(20.0, feeCalculator.calculateFee(60, VehicleType.CAR));
        assertEquals(40.0, feeCalculator.calculateFee(60, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("61 minutes duration bills for 2 hours (ceiling)")
    void testSixtyOneMinutesBillsForTwoHours() {
        assertEquals(20.0, feeCalculator.calculateFee(61, VehicleType.BIKE));
        assertEquals(40.0, feeCalculator.calculateFee(61, VehicleType.CAR));
        assertEquals(80.0, feeCalculator.calculateFee(61, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Each vehicle type uses its specific hourly rate")
    void testHourlyRateForEachType() {
        // 3 hours: Bike = 30, Car = 60, Truck = 120
        assertEquals(30.0, feeCalculator.calculateFee(180, VehicleType.BIKE));
        assertEquals(60.0, feeCalculator.calculateFee(180, VehicleType.CAR));
        assertEquals(120.0, feeCalculator.calculateFee(180, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Member receives 10% discount on calculated fee")
    void testMemberTenPercentDiscount() {
        // Standard 1 hr Car is 20.0 -> with 10% discount = 18.0
        assertEquals(18.0, feeCalculator.calculateFee(60, VehicleType.CAR, true));
        // Standard 2 hr Bike is 20.0 -> with 10% discount = 18.0
        assertEquals(18.0, feeCalculator.calculateFee(75, VehicleType.BIKE, true));
        // Standard 1 hr Truck is 40.0 -> with 10% discount = 36.0
        assertEquals(36.0, feeCalculator.calculateFee(15, VehicleType.TRUCK, true));
    }

    @Test
    @DisplayName("calculateFee(Ticket) calculates fee correctly from Ticket timestamps")
    void testCalculateFeeWithTicket() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 11, 13, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 11, 13, 12, 15); // 135 minutes -> 3 billable hours

        Ticket ticket = new Ticket(1, 1, 1, entryTime, exitTime, null, "ACTIVE",
                "DL01AB1234", "John Doe", VehicleType.CAR, "C-01");

        double regularFee = feeCalculator.calculateFee(ticket);
        assertEquals(60.0, regularFee); // 3 hrs * 20 = 60.0

        double memberFee = feeCalculator.calculateFee(ticket, true);
        assertEquals(54.0, memberFee); // 60.0 - 10% = 54.0
    }
}

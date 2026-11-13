package com.group12.parking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests demonstrating Object-Oriented Polymorphism (Unit 2).
 * Verifies polymorphic dynamic method dispatch through abstract Vehicle and Chargeable interface references.
 */
class PolymorphismTest {

    @Test
    @DisplayName("Vehicle references to Car, Bike, and Truck return the correct rate and charge")
    void testPolymorphicChargeCalculation() {
        // Unit 2: Polymorphism - holding subclass instances via abstract Vehicle superclass references
        Vehicle bike = new Bike("PB65AB1234", "Ashish");
        Vehicle car = new Car("DL01C1234", "Samar");
        Vehicle truck = new Truck("HR26DQ9999", "Lakshay");

        assertEquals(10.0, bike.getRatePerHour());
        assertEquals(20.0, car.getRatePerHour());
        assertEquals(40.0, truck.getRatePerHour());

        // 120 minutes = 2 hours
        // Bike: 2 * 10 = 20.0, Car: 2 * 20 = 40.0, Truck: 2 * 40 = 80.0
        assertEquals(20.0, bike.calculateCharge(120));
        assertEquals(40.0, car.calculateCharge(120));
        assertEquals(80.0, truck.calculateCharge(120));

        // Chargeable interface reference polymorphism
        Chargeable chargeableCar = car; // Unit 2: Interface polymorphism
        assertEquals(40.0, chargeableCar.calculateCharge(120));
    }

    @Test
    @DisplayName("Factory method Vehicle.create produces correct polymorphic subclasses")
    void testFactoryCreation() {
        Vehicle v1 = Vehicle.create(VehicleType.BIKE, "PB65AB1234", "Ashish");
        Vehicle v2 = Vehicle.create(VehicleType.CAR, "DL01C1234", "Samar");
        Vehicle v3 = Vehicle.create(VehicleType.TRUCK, "HR26DQ9999", "Lakshay");

        assertTrue(v1 instanceof Bike);
        assertTrue(v2 instanceof Car);
        assertTrue(v3 instanceof Truck);

        assertEquals(VehicleType.BIKE, v1.getType());
        assertEquals(VehicleType.CAR, v2.getType());
        assertEquals(VehicleType.TRUCK, v3.getType());
    }
}

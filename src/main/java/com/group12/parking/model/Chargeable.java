package com.group12.parking.model;

/**
 * Interface representing any entity capable of incurring parking charges.
 * Demonstrates Unit 2: Interfaces and polymorphic contracts.
 */
public interface Chargeable {
    // Unit 2: Interface method specification for fee calculation
    double calculateCharge(long minutes);
}

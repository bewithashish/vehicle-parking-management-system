package com.group12.parking.util;

/**
 * Constants used throughout the Vehicle Parking Management System.
 * Demonstrates Unit 1: Static final constants and encapsulated utility class.
 */
public final class Constants {
    // Unit 1: Private constructor to prevent instantiation of utility class
    private Constants() {}

    // Unit 1: Pricing rates in Rupees per hour
    public static final double BIKE_RATE_PER_HOUR = 10.0;
    public static final double CAR_RATE_PER_HOUR = 20.0;
    public static final double TRUCK_RATE_PER_HOUR = 40.0;

    // Unit 1: Member discount rate (10%)
    public static final double MEMBER_DISCOUNT_PERCENTAGE = 0.10;

    // Default slot counts for seeding
    public static final int DEFAULT_BIKE_SLOTS = 10;
    public static final int DEFAULT_CAR_SLOTS = 10;
    public static final int DEFAULT_TRUCK_SLOTS = 5;

    // Unit 3: Background thread refresh interval
    public static final int REFRESH_INTERVAL_SECONDS = 3;

    // Registration plate format validation regex (e.g., PB65AB1234)
    public static final String VEHICLE_PLATE_REGEX = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$";
}

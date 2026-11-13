package com.group12.parking.util;

import com.group12.parking.exception.ParkingException;

import java.util.regex.Pattern;

/**
 * Utility class providing validation and normalization for vehicle numbers and owner names.
 * Demonstrates Unit 1: Static utility methods and Unit 3: Exception throwing on invalid input.
 */
public final class Validator {
    // Unit 1: Private constructor for utility class
    private Validator() {}

    private static final Pattern PLATE_PATTERN = Pattern.compile(Constants.VEHICLE_PLATE_REGEX);
    private static final Pattern OWNER_PATTERN = Pattern.compile("^[a-zA-Z. ]{2,50}$");

    /**
     * Normalizes vehicle registration number by trimming, converting to uppercase,
     * and removing all spaces and hyphens.
     */
    public static String normalizeVehicleNumber(String rawNumber) {
        if (rawNumber == null) {
            return "";
        }
        return rawNumber.trim().toUpperCase().replaceAll("[\\s-]+", "");
    }

    /**
     * Validates normalized vehicle number against Indian standard format regex.
     * Throws ParkingException with descriptive message and example on failure.
     */
    public static String validateAndNormalizeVehicleNumber(String rawNumber) throws ParkingException {
        String normalized = normalizeVehicleNumber(rawNumber);
        if (normalized.isEmpty() || !PLATE_PATTERN.matcher(normalized).matches()) {
            // Unit 3: Throwing custom checked exception
            throw new ParkingException("Invalid vehicle number '" + (rawNumber != null ? rawNumber.trim() : "") +
                    "'. Must match format: 2-letter state code + 1-2 digit district + 1-3 letters + 4 digits (e.g., PB65AB1234, DL01C1234).");
        }
        return normalized;
    }

    /**
     * Validates owner name (2 to 50 characters, letters, dots, and spaces only).
     */
    public static String validateOwnerName(String rawName) throws ParkingException {
        if (rawName == null) {
            throw new ParkingException("Owner name cannot be empty.");
        }
        String trimmed = rawName.trim();
        if (!OWNER_PATTERN.matcher(trimmed).matches()) {
            // Unit 3: Throwing custom checked exception
            throw new ParkingException("Invalid owner name '" + trimmed +
                    "'. Must be 2 to 50 characters long and contain only letters, dots, and spaces.");
        }
        return trimmed;
    }

    /**
     * Validates slot count for slot configuration (0 to 100).
     */
    public static void validateSlotCount(int count, String typeName) throws ParkingException {
        if (count < 0 || count > 100) {
            throw new ParkingException(typeName + " slot count must be between 0 and 100 (got " + count + ").");
        }
    }
}

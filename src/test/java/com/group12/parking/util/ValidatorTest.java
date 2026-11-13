package com.group12.parking.util;

import com.group12.parking.exception.ParkingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Validator utility class.
 * Demonstrates Unit 3: Exception testing on invalid inputs.
 */
class ValidatorTest {

    @Test
    @DisplayName("Valid number plates are normalized and accepted")
    void testValidPlates() {
        assertDoesNotThrow(() -> {
            assertEquals("PB65AB1234", Validator.validateAndNormalizeVehicleNumber("PB65AB1234"));
            assertEquals("DL01C1234", Validator.validateAndNormalizeVehicleNumber("dl-01-c-1234"));
            assertEquals("MH12DE5678", Validator.validateAndNormalizeVehicleNumber("  mh 12 de 5678  "));
            assertEquals("HR26DQ9999", Validator.validateAndNormalizeVehicleNumber("hr-26-dq-9999"));
            assertEquals("KA03AAA1111", Validator.validateAndNormalizeVehicleNumber("ka03aaa1111"));
        });
    }

    @Test
    @DisplayName("Invalid number plates throw ParkingException with descriptive message")
    void testInvalidPlates() {
        // Too short
        ParkingException ex1 = assertThrows(ParkingException.class, () ->
                Validator.validateAndNormalizeVehicleNumber("PB1"));
        assertTrue(ex1.getMessage().contains("Must match format"));

        // Missing digits
        assertThrows(ParkingException.class, () ->
                Validator.validateAndNormalizeVehicleNumber("PB65ABCD"));

        // Special characters
        assertThrows(ParkingException.class, () ->
                Validator.validateAndNormalizeVehicleNumber("PB@65AB1234"));

        // Empty or null
        assertThrows(ParkingException.class, () ->
                Validator.validateAndNormalizeVehicleNumber(""));
        assertThrows(ParkingException.class, () ->
                Validator.validateAndNormalizeVehicleNumber(null));
    }

    @Test
    @DisplayName("Normalization strips spaces and hyphens and converts to uppercase")
    void testNormalization() {
        assertEquals("PB65AB1234", Validator.normalizeVehicleNumber(" pb - 65 - ab - 1234 "));
        assertEquals("HR01A9999", Validator.normalizeVehicleNumber("hr 01 a 9999"));
    }

    @Test
    @DisplayName("Valid owner names are accepted")
    void testValidOwnerNames() {
        assertDoesNotThrow(() -> {
            assertEquals("John Doe", Validator.validateOwnerName("John Doe"));
            assertEquals("Dr. A. Sharma", Validator.validateOwnerName("Dr. A. Sharma"));
            assertEquals("Ashish", Validator.validateOwnerName("Ashish"));
        });
    }

    @Test
    @DisplayName("Invalid owner names throw ParkingException")
    void testInvalidOwnerNames() {
        // Less than 2 characters
        assertThrows(ParkingException.class, () -> Validator.validateOwnerName("A"));
        // Null or blank
        assertThrows(ParkingException.class, () -> Validator.validateOwnerName(""));
        assertThrows(ParkingException.class, () -> Validator.validateOwnerName(null));
        // Numbers or special characters not allowed
        assertThrows(ParkingException.class, () -> Validator.validateOwnerName("John123"));
        assertThrows(ParkingException.class, () -> Validator.validateOwnerName("John @ Doe"));
    }
}

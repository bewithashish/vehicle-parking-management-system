package com.group12.parking.dao;

import com.group12.parking.model.Slot;
import com.group12.parking.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Slot entities.
 * Demonstrates Unit 4: JDBC CRUD operations, query filtering, aggregation,
 * and slot state management with PreparedStatement and ResultSet.
 */
public class SlotDAO { // Unit 4: DAO Pattern

    /**
     * Finds the free slot with the lowest slot_id for the given vehicle type.
     */
    public Optional<Slot> findLowestFreeSlot(Connection conn, VehicleType type) throws SQLException {
        // Business Rule: Allot lowest slot_id free slot of matching type
        String sql = """
                SELECT slot_id, slot_number, vehicle_type, is_occupied
                FROM slots
                WHERE vehicle_type = ? AND is_occupied = 0
                ORDER BY slot_id ASC
                LIMIT 1;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Unit 4: PreparedStatement
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) { // Unit 4: ResultSet
                if (rs.next()) {
                    return Optional.of(mapRowToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates the occupancy status (0 for free, 1 for occupied) of a slot.
     */
    public boolean updateOccupancy(Connection conn, int slotId, boolean occupied) throws SQLException {
        String sql = "UPDATE slots SET is_occupied = ? WHERE slot_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, occupied ? 1 : 0);
            pstmt.setInt(2, slotId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all parking slots ordered by vehicle type and slot ID.
     */
    public List<Slot> getAllSlots(Connection conn) throws SQLException {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT slot_id, slot_number, vehicle_type, is_occupied FROM slots ORDER BY slot_id ASC;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                slots.add(mapRowToSlot(rs));
            }
        }
        return slots;
    }

    /**
     * Retrieves all slots for a specific vehicle type.
     */
    public List<Slot> getSlotsByType(Connection conn, VehicleType type) throws SQLException {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT slot_id, slot_number, vehicle_type, is_occupied FROM slots WHERE vehicle_type = ? ORDER BY slot_id ASC;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapRowToSlot(rs));
                }
            }
        }
        return slots;
    }

    /**
     * Finds a slot by slotId.
     */
    public Optional<Slot> findById(Connection conn, int slotId) throws SQLException {
        String sql = "SELECT slot_id, slot_number, vehicle_type, is_occupied FROM slots WHERE slot_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a slot by slotNumber (e.g. 'C-01').
     */
    public Optional<Slot> findBySlotNumber(Connection conn, String slotNumber) throws SQLException {
        String sql = "SELECT slot_id, slot_number, vehicle_type, is_occupied FROM slots WHERE slot_number = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, slotNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Counts total slots of a specific type.
     */
    public int countSlotsByType(Connection conn, VehicleType type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM slots WHERE vehicle_type = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Inserts a new slot.
     */
    public void insertSlot(Connection conn, Slot slot) throws SQLException {
        String sql = "INSERT INTO slots (slot_number, vehicle_type, is_occupied) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, slot.getSlotNumber());
            pstmt.setString(2, slot.getVehicleType().name());
            pstmt.setInt(3, slot.isOccupied() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    /**
     * Checks if a slot has ever been referenced in parking_tickets table.
     */
    public boolean hasTicketHistory(Connection conn, int slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_tickets WHERE slot_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a slot by slotId.
     */
    public boolean deleteSlot(Connection conn, int slotId) throws SQLException {
        String sql = "DELETE FROM slots WHERE slot_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Slot mapRowToSlot(ResultSet rs) throws SQLException {
        int id = rs.getInt("slot_id");
        String number = rs.getString("slot_number");
        VehicleType type = VehicleType.valueOf(rs.getString("vehicle_type"));
        boolean occupied = rs.getInt("is_occupied") == 1;
        return new Slot(id, number, type, occupied);
    }
}

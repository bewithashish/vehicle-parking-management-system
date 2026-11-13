package com.group12.parking.dao;

import com.group12.parking.model.Vehicle;
import com.group12.parking.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for Vehicle entities.
 * Demonstrates Unit 4: JDBC CRUD operations, PreparedStatement, ResultSet mapping,
 * and parameter binding to prevent SQL injection.
 */
public class VehicleDAO { // Unit 4: DAO Pattern

    /**
     * Finds a vehicle by its unique normalized vehicle registration number.
     */
    public Optional<Vehicle> findByVehicleNumber(Connection conn, String vehicleNumber) throws SQLException {
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_type, owner_name FROM vehicles WHERE vehicle_number = ?;";
        // Unit 4: PreparedStatement with parameterized query
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            try (ResultSet rs = pstmt.executeQuery()) { // Unit 4: ResultSet traversal
                if (rs.next()) {
                    int id = rs.getInt("vehicle_id");
                    String number = rs.getString("vehicle_number");
                    VehicleType type = VehicleType.valueOf(rs.getString("vehicle_type"));
                    String owner = rs.getString("owner_name");
                    // Unit 2: Polymorphic vehicle creation via factory method
                    return Optional.of(Vehicle.create(id, type, number, owner));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a vehicle by its primary key ID.
     */
    public Optional<Vehicle> findById(Connection conn, int vehicleId) throws SQLException {
        String sql = "SELECT vehicle_id, vehicle_number, vehicle_type, owner_name FROM vehicles WHERE vehicle_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("vehicle_id");
                    String number = rs.getString("vehicle_number");
                    VehicleType type = VehicleType.valueOf(rs.getString("vehicle_type"));
                    String owner = rs.getString("owner_name");
                    return Optional.of(Vehicle.create(id, type, number, owner));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new vehicle or updates owner_name if vehicle_number already exists.
     * Sets and returns the updated vehicle with its generated/existing vehicleId.
     */
    public Vehicle saveOrUpdate(Connection conn, Vehicle vehicle) throws SQLException {
        // Unit 4: SQLite upsert with parameterized query
        String upsertSql = """
                INSERT INTO vehicles (vehicle_number, vehicle_type, owner_name)
                VALUES (?, ?, ?)
                ON CONFLICT(vehicle_number) DO UPDATE SET
                    owner_name = excluded.owner_name,
                    vehicle_type = excluded.vehicle_type;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(upsertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, vehicle.getVehicleNumber());
            pstmt.setString(2, vehicle.getType().name());
            pstmt.setString(3, vehicle.getOwnerName());
            pstmt.executeUpdate();
        }

        // Fetch vehicle_id
        String selectIdSql = "SELECT vehicle_id FROM vehicles WHERE vehicle_number = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(selectIdSql)) {
            pstmt.setString(1, vehicle.getVehicleNumber());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    vehicle.setVehicleId(rs.getInt(1));
                }
            }
        }
        return vehicle;
    }
}

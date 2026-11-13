package com.group12.parking.dao;

import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Parking Ticket entities.
 * Demonstrates Unit 4: JDBC database queries, transaction participation,
 * complex SQL joins across multiple tables, and PreparedStatement date formatting.
 */
public class TicketDAO { // Unit 4: DAO Pattern
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Checks if a vehicle has an ACTIVE ticket currently.
     */
    public boolean hasActiveTicket(Connection conn, int vehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_tickets WHERE vehicle_id = ? AND status = 'ACTIVE';";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Finds active ticket by vehicle registration number with joined vehicle and slot details.
     */
    public Optional<Ticket> findActiveTicketByVehicleNumber(Connection conn, String vehicleNumber) throws SQLException {
        String sql = """
                SELECT t.ticket_id, t.vehicle_id, t.slot_id, t.entry_time, t.exit_time, t.fee, t.status,
                       v.vehicle_number, v.owner_name, v.vehicle_type, s.slot_number
                FROM parking_tickets t
                JOIN vehicles v ON t.vehicle_id = v.vehicle_id
                JOIN slots s ON t.slot_id = s.slot_id
                WHERE v.vehicle_number = ? AND t.status = 'ACTIVE'
                ORDER BY t.ticket_id DESC
                LIMIT 1;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFullTicket(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds active ticket by numeric ticketId with joined details.
     */
    public Optional<Ticket> findActiveTicketById(Connection conn, int ticketId) throws SQLException {
        String sql = """
                SELECT t.ticket_id, t.vehicle_id, t.slot_id, t.entry_time, t.exit_time, t.fee, t.status,
                       v.vehicle_number, v.owner_name, v.vehicle_type, s.slot_number
                FROM parking_tickets t
                JOIN vehicles v ON t.vehicle_id = v.vehicle_id
                JOIN slots s ON t.slot_id = s.slot_id
                WHERE t.ticket_id = ? AND t.status = 'ACTIVE';
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ticketId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFullTicket(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds active ticket for a given slotId with joined details (for slot click inspection).
     */
    public Optional<Ticket> findActiveTicketBySlotId(Connection conn, int slotId) throws SQLException {
        String sql = """
                SELECT t.ticket_id, t.vehicle_id, t.slot_id, t.entry_time, t.exit_time, t.fee, t.status,
                       v.vehicle_number, v.owner_name, v.vehicle_type, s.slot_number
                FROM parking_tickets t
                JOIN vehicles v ON t.vehicle_id = v.vehicle_id
                JOIN slots s ON t.slot_id = s.slot_id
                WHERE t.slot_id = ? AND t.status = 'ACTIVE'
                ORDER BY t.ticket_id DESC
                LIMIT 1;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFullTicket(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts a new active ticket during vehicle entry.
     */
    public Ticket insertTicket(Connection conn, Ticket ticket) throws SQLException {
        String sql = "INSERT INTO parking_tickets (vehicle_id, slot_id, entry_time, exit_time, fee, status) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, ticket.getVehicleId());
            pstmt.setInt(2, ticket.getSlotId());
            pstmt.setString(3, ticket.getEntryTime().format(ISO_FORMATTER));
            pstmt.setNull(4, java.sql.Types.VARCHAR);
            pstmt.setNull(5, java.sql.Types.REAL);
            pstmt.setString(6, ticket.getStatus());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setTicketId(generatedKeys.getInt(1));
                }
            }
        }
        return ticket;
    }

    /**
     * Completes a ticket during vehicle exit by recording exit time, calculated fee, and COMPLETED status.
     */
    public boolean completeTicket(Connection conn, int ticketId, LocalDateTime exitTime, double fee) throws SQLException {
        String sql = "UPDATE parking_tickets SET exit_time = ?, fee = ?, status = 'COMPLETED' WHERE ticket_id = ? AND status = 'ACTIVE';";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exitTime.format(ISO_FORMATTER));
            pstmt.setDouble(2, fee);
            pstmt.setInt(3, ticketId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves tickets entered or exited today for report generation.
     */
    public List<Ticket> getTodayTickets(Connection conn, LocalDate today) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String datePrefix = today.toString(); // YYYY-MM-DD
        String sql = """
                SELECT t.ticket_id, t.vehicle_id, t.slot_id, t.entry_time, t.exit_time, t.fee, t.status,
                       v.vehicle_number, v.owner_name, v.vehicle_type, s.slot_number
                FROM parking_tickets t
                JOIN vehicles v ON t.vehicle_id = v.vehicle_id
                JOIN slots s ON t.slot_id = s.slot_id
                WHERE t.entry_time LIKE ? OR t.exit_time LIKE ?
                ORDER BY t.ticket_id DESC;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, datePrefix + "%");
            pstmt.setString(2, datePrefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRowToFullTicket(rs));
                }
            }
        }
        return tickets;
    }

    private Ticket mapRowToFullTicket(ResultSet rs) throws SQLException {
        int id = rs.getInt("ticket_id");
        int vehicleId = rs.getInt("vehicle_id");
        int slotId = rs.getInt("slot_id");
        LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"), ISO_FORMATTER);

        String exitTimeStr = rs.getString("exit_time");
        LocalDateTime exitTime = (exitTimeStr != null && !exitTimeStr.isBlank())
                ? LocalDateTime.parse(exitTimeStr, ISO_FORMATTER) : null;

        double feeObj = rs.getDouble("fee");
        Double fee = rs.wasNull() ? null : feeObj;

        String status = rs.getString("status");
        String vehicleNumber = rs.getString("vehicle_number");
        String ownerName = rs.getString("owner_name");
        VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
        String slotNumber = rs.getString("slot_number");

        return new Ticket(id, vehicleId, slotId, entryTime, exitTime, fee, status,
                vehicleNumber, ownerName, vehicleType, slotNumber);
    }
}

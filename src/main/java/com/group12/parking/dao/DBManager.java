package com.group12.parking.dao;

import com.group12.parking.model.VehicleType;
import com.group12.parking.util.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class managing SQLite database connections and schema initialization.
 * Demonstrates Unit 4: JDBC architecture, DriverManager, Statement/PreparedStatement,
 * transactions, and singleton pattern with try-with-resources.
 */
public class DBManager { // Unit 4: JDBC Manager Singleton
    private static DBManager instance;
    private static String dbPath = "parking.db";

    // Unit 4: Private constructor for singleton
    private DBManager() {
        try {
            // Load SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath", e);
        }
    }

    // Unit 4: Thread-safe singleton instance accessor
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    // Unit 4: Dynamic database path setter for tests
    public static synchronized void setDbPath(String path) {
        dbPath = path;
    }

    public static synchronized String getDbPath() {
        return dbPath;
    }

    /**
     * Creates and returns a fresh JDBC Connection with foreign keys and busy timeout configured.
     * Always used with try-with-resources by callers so threads never share connections.
     */
    public Connection getConnection() throws SQLException { // Unit 4: JDBC Connection factory
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        // Unit 4: Executing PRAGMA commands via JDBC Statement
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return connection;
    }

    /**
     * Initializes database tables and seeds default parking slots if the slots table is empty.
     */
    public void initializeDatabase() throws SQLException {
        // Unit 4: Creating tables with DDL statements via Statement
        String createSlotsTable = """
                CREATE TABLE IF NOT EXISTS slots (
                  slot_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  slot_number TEXT NOT NULL UNIQUE,
                  vehicle_type TEXT NOT NULL CHECK(vehicle_type IN ('BIKE','CAR','TRUCK')),
                  is_occupied INTEGER NOT NULL DEFAULT 0
                );
                """;

        String createVehiclesTable = """
                CREATE TABLE IF NOT EXISTS vehicles (
                  vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  vehicle_number TEXT NOT NULL UNIQUE,
                  vehicle_type TEXT NOT NULL CHECK(vehicle_type IN ('BIKE','CAR','TRUCK')),
                  owner_name TEXT NOT NULL
                );
                """;

        String createTicketsTable = """
                CREATE TABLE IF NOT EXISTS parking_tickets (
                  ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  vehicle_id INTEGER NOT NULL REFERENCES vehicles(vehicle_id),
                  slot_id INTEGER NOT NULL REFERENCES slots(slot_id),
                  entry_time TEXT NOT NULL,
                  exit_time TEXT,
                  fee REAL,
                  status TEXT NOT NULL CHECK(status IN ('ACTIVE','COMPLETED'))
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) { // Unit 4: try-with-resources
            stmt.execute(createSlotsTable);
            stmt.execute(createVehiclesTable);
            stmt.execute(createTicketsTable);
        }

        // Seed default slots if empty
        seedSlotsIfEmpty();
    }

    /**
     * Seeds initial slots: 10 BIKE, 10 CAR, 5 TRUCK on first run.
     */
    private void seedSlotsIfEmpty() throws SQLException {
        try (Connection conn = getConnection()) {
            // Unit 4: Querying with PreparedStatement and ResultSet
            String checkCountSql = "SELECT COUNT(*) FROM slots;";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkCountSql);
                 ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Seed slots inside a single transaction
                    conn.setAutoCommit(false); // Unit 4: JDBC Transaction management
                    try {
                        insertSlotsForType(conn, VehicleType.BIKE, Constants.DEFAULT_BIKE_SLOTS, 1);
                        insertSlotsForType(conn, VehicleType.CAR, Constants.DEFAULT_CAR_SLOTS, 1);
                        insertSlotsForType(conn, VehicleType.TRUCK, Constants.DEFAULT_TRUCK_SLOTS, 1);
                        conn.commit(); // Unit 4: Transaction commit
                    } catch (SQLException e) {
                        conn.rollback(); // Unit 4: Transaction rollback
                        throw e;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            }
        }
    }

    private void insertSlotsForType(Connection conn, VehicleType type, int count, int startIndex) throws SQLException {
        String insertSql = "INSERT INTO slots (slot_number, vehicle_type, is_occupied) VALUES (?, ?, 0);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) { // Unit 4: PreparedStatement
            for (int i = 0; i < count; i++) {
                int index = startIndex + i;
                String slotNumber = String.format("%s-%02d", type.getPrefix(), index);
                pstmt.setString(1, slotNumber);
                pstmt.setString(2, type.name());
                pstmt.executeUpdate();
            }
        }
    }
}

package com.group12.parking.service;

import com.group12.parking.dao.DBManager;
import com.group12.parking.exception.ParkingException;
import com.group12.parking.exception.ParkingFullException;
import com.group12.parking.model.Slot;
import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration and business rule tests for ParkingService.
 * Demonstrates Unit 3: Checked exceptions (ParkingFullException, ParkingException)
 * and Unit 4: JDBC database operations with an isolated SQLite temp database.
 */
class ParkingServiceTest {
    private ParkingService parkingService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws SQLException {
        // Point DBManager to isolated temporary database file for each test
        String tempDbPath = tempDir.resolve("test_parking.db").toAbsolutePath().toString();
        DBManager.setDbPath(tempDbPath);
        DBManager.getInstance().initializeDatabase();
        parkingService = new ParkingService();
    }

    @Test
    @DisplayName("Initial seeding creates exactly 25 slots (10 Bike, 10 Car, 5 Truck)")
    void testInitialSeeding() throws ParkingException {
        List<Slot> allSlots = parkingService.getAllSlots();
        assertEquals(25, allSlots.size());

        assertEquals(10, parkingService.getSlotCountByType(VehicleType.BIKE));
        assertEquals(10, parkingService.getSlotCountByType(VehicleType.CAR));
        assertEquals(5, parkingService.getSlotCountByType(VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Entry allots lowest slot_id free slot of matching vehicle type")
    void testEntryAllotsLowestSlot() throws ParkingException, ParkingFullException {
        Ticket ticket1 = parkingService.parkVehicle("PB65AB1234", "Ashish", VehicleType.CAR);
        assertEquals("C-01", ticket1.getSlotNumber());

        Ticket ticket2 = parkingService.parkVehicle("PB65AB5678", "Samar", VehicleType.CAR);
        assertEquals("C-02", ticket2.getSlotNumber());

        Ticket bikeTicket = parkingService.parkVehicle("DL01C1111", "Chandni", VehicleType.BIKE);
        assertEquals("B-01", bikeTicket.getSlotNumber());
    }

    @Test
    @DisplayName("Duplicate entry for vehicle with active ticket is rejected")
    void testDuplicateActiveVehicleRejected() throws ParkingException, ParkingFullException {
        parkingService.parkVehicle("PB65AB1234", "Ashish", VehicleType.CAR);

        ParkingException ex = assertThrows(ParkingException.class, () ->
                parkingService.parkVehicle("pb-65-ab-1234", "Ashish", VehicleType.CAR));

        assertTrue(ex.getMessage().contains("already parked with an active ticket"));
    }

    @Test
    @DisplayName("ParkingFullException thrown when slots of requested type are exhausted")
    void testParkingFullException() throws ParkingException, ParkingFullException {
        // Reconfigure to only 1 bike slot
        parkingService.configureSlots(1, 10, 5);

        // Park 1 bike -> fills the slot B-01
        parkingService.parkVehicle("PB65AB1111", "Rider One", VehicleType.BIKE);

        // Park 2nd bike -> must throw ParkingFullException
        ParkingFullException ex = assertThrows(ParkingFullException.class, () ->
                parkingService.parkVehicle("PB65AB2222", "Rider Two", VehicleType.BIKE));

        assertTrue(ex.getMessage().contains("No free Bike slot available"));
    }

    @Test
    @DisplayName("Exit frees slot and updates ticket fee and status")
    void testExitFreesSlot() throws ParkingException, ParkingFullException {
        Ticket ticket = parkingService.parkVehicle("PB65AB1234", "Ashish", VehicleType.CAR);

        ReceiptInfo receipt = parkingService.exitVehicle("PB65AB1234", false);
        assertNotNull(receipt);
        assertEquals("C-01", receipt.getSlotNumber());
        assertEquals(20.0, receipt.getTotalFee()); // 1 hr minimum * 20

        // Slot C-01 should now be free again
        List<Slot> slots = parkingService.getAllSlots();
        Slot slotC1 = slots.stream().filter(s -> s.getSlotNumber().equals("C-01")).findFirst().orElseThrow();
        assertFalse(slotC1.isOccupied());

        // Subsequent vehicle can now park in C-01
        Ticket nextTicket = parkingService.parkVehicle("HR26DQ9999", "New Driver", VehicleType.CAR);
        assertEquals("C-01", nextTicket.getSlotNumber());
    }

    @Test
    @DisplayName("Exit works by numeric ticket ID as well as vehicle number")
    void testExitByTicketIdAndVehicleNumber() throws ParkingException, ParkingFullException {
        Ticket ticket1 = parkingService.parkVehicle("PB65AB1234", "Ashish", VehicleType.CAR);
        Ticket ticket2 = parkingService.parkVehicle("HR26DQ9999", "Lakshay", VehicleType.BIKE);

        // Exit first by ticket ID
        ReceiptInfo receipt1 = parkingService.exitVehicle(String.valueOf(ticket1.getTicketId()), false);
        assertEquals(ticket1.getTicketId(), receipt1.getTicketId());

        // Exit second by vehicle registration number
        ReceiptInfo receipt2 = parkingService.exitVehicle("hr-26-dq-9999", true); // Member discount
        assertEquals(ticket2.getTicketId(), receipt2.getTicketId());
        assertEquals(9.0, receipt2.getTotalFee()); // 10 - 10% = 9.0
    }

    @Test
    @DisplayName("Slot configuration increases and decreases slots correctly")
    void testSlotConfigurationRules() throws ParkingException {
        // Increase slots: 12 bikes, 11 cars, 6 trucks
        parkingService.configureSlots(12, 11, 6);
        assertEquals(12, parkingService.getSlotCountByType(VehicleType.BIKE));
        assertEquals(11, parkingService.getSlotCountByType(VehicleType.CAR));
        assertEquals(6, parkingService.getSlotCountByType(VehicleType.TRUCK));

        // Decrease slots where slots are free
        parkingService.configureSlots(10, 10, 5);
        assertEquals(10, parkingService.getSlotCountByType(VehicleType.BIKE));
        assertEquals(10, parkingService.getSlotCountByType(VehicleType.CAR));
        assertEquals(5, parkingService.getSlotCountByType(VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Slot configuration refuses deletion if slot has past ticket history")
    void testSlotConfigRefusesSlotWithTicketHistory() throws ParkingException, ParkingFullException {
        // Reconfigure to 1 truck
        parkingService.configureSlots(10, 10, 1);

        // Park truck in T-01
        Ticket ticket = parkingService.parkVehicle("PB65AB9999", "Heavy Hauler", VehicleType.TRUCK);
        parkingService.exitVehicle(String.valueOf(ticket.getTicketId()), false);

        // T-01 is free now, but has ticket history. Attempting to reduce Truck slots to 0 should fail!
        ParkingException ex = assertThrows(ParkingException.class, () ->
                parkingService.configureSlots(10, 10, 0));

        assertTrue(ex.getMessage().contains("past parking ticket history") ||
                ex.getMessage().contains("free slot(s) without ticket history"));
    }

    @Test
    @DisplayName("Report returns correct totals for vehicles parked today and revenue")
    void testReportTotals() throws ParkingException, ParkingFullException {
        Ticket t1 = parkingService.parkVehicle("PB65AB1111", "Ashish", VehicleType.CAR);
        Ticket t2 = parkingService.parkVehicle("PB65AB2222", "Samar", VehicleType.BIKE);
        parkingService.parkVehicle("PB65AB3333", "Lakshay", VehicleType.TRUCK);

        // Exit t1 and t2
        parkingService.exitVehicle(String.valueOf(t1.getTicketId()), false); // Car = 20
        parkingService.exitVehicle(String.valueOf(t2.getTicketId()), true);  // Bike = 9 (member)

        ReportData report = parkingService.getTodayReport();
        assertEquals(3, report.getTotalVehiclesParkedToday());
        assertEquals(29.0, report.getTotalRevenueToday());
        assertEquals(1, report.getCurrentlyActiveCount()); // Truck is still parked
    }
}

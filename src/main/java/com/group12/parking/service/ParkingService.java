package com.group12.parking.service;

import com.group12.parking.dao.DBManager;
import com.group12.parking.dao.SlotDAO;
import com.group12.parking.dao.TicketDAO;
import com.group12.parking.dao.VehicleDAO;
import com.group12.parking.exception.ParkingException;
import com.group12.parking.exception.ParkingFullException;
import com.group12.parking.model.Slot;
import com.group12.parking.model.Ticket;
import com.group12.parking.model.Vehicle;
import com.group12.parking.model.VehicleType;
import com.group12.parking.util.Constants;
import com.group12.parking.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core business service handling vehicle entry, slot allotment, vehicle exit,
 * slot capacity configuration, and reporting.
 * Demonstrates Unit 3 (Multithreading synchronization, checked exceptions)
 * and Unit 4 (Database transactions, atomic rollback/commit with JDBC).
 */
public class ParkingService {
    private final DBManager dbManager;
    private final SlotDAO slotDAO;
    private final VehicleDAO vehicleDAO;
    private final TicketDAO ticketDAO;
    private final FeeCalculator feeCalculator;

    public ParkingService() {
        this(DBManager.getInstance(), new SlotDAO(), new VehicleDAO(), new TicketDAO(), new FeeCalculator());
    }

    public ParkingService(DBManager dbManager, SlotDAO slotDAO, VehicleDAO vehicleDAO,
                          TicketDAO ticketDAO, FeeCalculator feeCalculator) {
        this.dbManager = dbManager;
        this.slotDAO = slotDAO;
        this.vehicleDAO = vehicleDAO;
        this.ticketDAO = ticketDAO;
        this.feeCalculator = feeCalculator;
    }

    /**
     * Unit 3: Synchronized entry method ensuring atomic slot allotment.
     * Prevents race conditions when multiple threads/requests attempt to park simultaneously.
     */
    public synchronized Ticket parkVehicle(String rawVehicleNumber, String rawOwnerName, VehicleType type)
            throws ParkingException, ParkingFullException { // Unit 3: Exception declarations
        // Step 1: Validate input
        String normalizedPlate = Validator.validateAndNormalizeVehicleNumber(rawVehicleNumber);
        String validatedOwner = Validator.validateOwnerName(rawOwnerName);

        // Step 2: Atomic DB Transaction
        try (Connection conn = dbManager.getConnection()) { // Unit 4: try-with-resources
            conn.setAutoCommit(false); // Unit 4: Begin JDBC Transaction
            try {
                // Rule: Reject if vehicle already has an ACTIVE ticket
                Optional<Vehicle> existingVehicle = vehicleDAO.findByVehicleNumber(conn, normalizedPlate);
                if (existingVehicle.isPresent() && ticketDAO.hasActiveTicket(conn, existingVehicle.get().getVehicleId())) {
                    throw new ParkingException("Vehicle " + normalizedPlate + " is already parked with an active ticket.");
                }

                // Rule: Allot lowest slot_id free slot of matching type
                Optional<Slot> freeSlot = slotDAO.findLowestFreeSlot(conn, type);
                if (freeSlot.isEmpty()) {
                    // Unit 3: Throw custom checked ParkingFullException
                    throw new ParkingFullException("No free " + type.getDisplayName() + " slot available.");
                }
                Slot allocatedSlot = freeSlot.get();

                // Rule: Create polymorphic vehicle and upsert in database
                Vehicle vehicle = Vehicle.create(type, normalizedPlate, validatedOwner); // Unit 2: Polymorphism
                vehicle = vehicleDAO.saveOrUpdate(conn, vehicle);

                // Update slot occupancy to 1
                slotDAO.updateOccupancy(conn, allocatedSlot.getSlotId(), true);

                // Create and insert active parking ticket
                LocalDateTime entryTime = LocalDateTime.now();
                Ticket ticket = new Ticket(vehicle.getVehicleId(), allocatedSlot.getSlotId(), entryTime);
                ticket = ticketDAO.insertTicket(conn, ticket);

                // Attach display info to ticket
                ticket.setVehicleNumber(vehicle.getVehicleNumber());
                ticket.setOwnerName(vehicle.getOwnerName());
                ticket.setVehicleType(type);
                ticket.setSlotNumber(allocatedSlot.getSlotNumber());

                conn.commit(); // Unit 4: Commit transaction
                return ticket;
            } catch (SQLException | ParkingException | ParkingFullException ex) {
                conn.rollback(); // Unit 4: Rollback transaction on failure
                if (ex instanceof ParkingException pe) throw pe;
                if (ex instanceof ParkingFullException pfe) throw pfe;
                throw new ParkingException("Database error during parking: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ParkingException("Database connection error: " + e.getMessage(), e);
        }
    }

    /**
     * Unit 3: Synchronized exit method processing departure and fee collection.
     * Accepts either vehicle registration number or numeric ticket ID.
     */
    public synchronized ReceiptInfo exitVehicle(String input, boolean isMember) throws ParkingException {
        if (input == null || input.trim().isEmpty()) {
            throw new ParkingException("Please provide a vehicle number or ticket ID.");
        }
        String cleanInput = input.trim();

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Unit 4: Transaction start
            try {
                Optional<Ticket> ticketOpt;
                // Check if numeric ticket ID was entered
                if (cleanInput.matches("^[0-9]+$")) {
                    int ticketId = Integer.parseInt(cleanInput);
                    ticketOpt = ticketDAO.findActiveTicketById(conn, ticketId);
                } else {
                    // Treat as vehicle number
                    String normalizedPlate = Validator.normalizeVehicleNumber(cleanInput);
                    ticketOpt = ticketDAO.findActiveTicketByVehicleNumber(conn, normalizedPlate);
                }

                if (ticketOpt.isEmpty()) {
                    throw new ParkingException("No active parking ticket found for: '" + cleanInput + "'.");
                }

                Ticket ticket = ticketOpt.get();
                LocalDateTime exitTime = LocalDateTime.now();
                long durationMinutes = Math.max(0, Duration.between(ticket.getEntryTime(), exitTime).toMinutes());
                long billableHours = FeeCalculator.calculateBillableHours(durationMinutes);
                double hourlyRate = FeeCalculator.getRate(ticket.getVehicleType());
                double baseFee = billableHours * hourlyRate;
                double totalFee = feeCalculator.calculateFee(durationMinutes, ticket.getVehicleType(), isMember);
                double discountAmount = baseFee - totalFee;

                // Mark ticket completed
                ticketDAO.completeTicket(conn, ticket.getTicketId(), exitTime, totalFee);

                // Free the slot
                slotDAO.updateOccupancy(conn, ticket.getSlotId(), false);

                conn.commit(); // Unit 4: Commit transaction

                return new ReceiptInfo(
                        ticket.getTicketId(),
                        ticket.getVehicleNumber(),
                        ticket.getOwnerName(),
                        ticket.getVehicleType(),
                        ticket.getSlotNumber(),
                        ticket.getEntryTime(),
                        exitTime,
                        durationMinutes,
                        billableHours,
                        hourlyRate,
                        discountAmount,
                        totalFee,
                        isMember
                );
            } catch (SQLException ex) {
                conn.rollback(); // Unit 4: Rollback transaction
                throw new ParkingException("Database error during exit processing: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ParkingException("Database connection error: " + e.getMessage(), e);
        }
    }

    /**
     * Unit 3: Synchronized slot configuration method.
     * Admin sets desired count (0-100) per vehicle type.
     */
    public synchronized void configureSlots(int targetBikes, int targetCars, int targetTrucks) throws ParkingException {
        Validator.validateSlotCount(targetBikes, "Bike");
        Validator.validateSlotCount(targetCars, "Car");
        Validator.validateSlotCount(targetTrucks, "Truck");

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Unit 4: Atomic slot reallocation
            try {
                adjustSlotCountForType(conn, VehicleType.BIKE, targetBikes);
                adjustSlotCountForType(conn, VehicleType.CAR, targetCars);
                adjustSlotCountForType(conn, VehicleType.TRUCK, targetTrucks);
                conn.commit();
            } catch (SQLException | ParkingException e) {
                conn.rollback();
                if (e instanceof ParkingException pe) throw pe;
                throw new ParkingException("Failed to update slot configuration: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ParkingException("Database connection error: " + e.getMessage(), e);
        }
    }

    private void adjustSlotCountForType(Connection conn, VehicleType type, int targetCount)
            throws SQLException, ParkingException {
        List<Slot> currentSlots = slotDAO.getSlotsByType(conn, type);
        int currentCount = currentSlots.size();

        if (targetCount == currentCount) {
            return; // No change needed
        }

        if (targetCount > currentCount) {
            // Add new slots
            int slotsToAdd = targetCount - currentCount;
            int maxIndex = 0;
            for (Slot s : currentSlots) {
                String numStr = s.getSlotNumber().replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    maxIndex = Math.max(maxIndex, Integer.parseInt(numStr));
                }
            }

            for (int i = 1; i <= slotsToAdd; i++) {
                int nextIndex = maxIndex + i;
                String slotNumber = String.format("%s-%02d", type.getPrefix(), nextIndex);
                slotDAO.insertSlot(conn, new Slot(slotNumber, type, false));
            }
        } else {
            // Decrease slots: delete only highest-numbered FREE slots without ticket history
            int slotsToRemove = currentCount - targetCount;

            // Sort current slots descending by slot number index
            List<Slot> sortedDesc = new ArrayList<>(currentSlots);
            sortedDesc.sort(Comparator.comparingInt(this::extractSlotIndex).reversed());

            List<Slot> toDelete = new ArrayList<>();
            for (Slot s : sortedDesc) {
                if (toDelete.size() == slotsToRemove) {
                    break;
                }
                if (s.isOccupied()) {
                    continue; // Skip occupied slots
                }
                if (slotDAO.hasTicketHistory(conn, s.getSlotId())) {
                    throw new ParkingException("Cannot delete slot " + s.getSlotNumber() +
                            " because it has past parking ticket history. Slot configuration unchanged.");
                }
                toDelete.add(s);
            }

            if (toDelete.size() < slotsToRemove) {
                throw new ParkingException("Cannot reduce " + type.getDisplayName() +
                        " slots from " + currentCount + " to " + targetCount +
                        ": only " + toDelete.size() + " free slot(s) without ticket history available for removal.");
            }

            for (Slot s : toDelete) {
                slotDAO.deleteSlot(conn, s.getSlotId());
            }
        }
    }

    private int extractSlotIndex(Slot slot) {
        String digits = slot.getSlotNumber().replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    /**
     * Generates comprehensive report data for today.
     */
    public ReportData getTodayReport() throws ParkingException {
        LocalDate today = LocalDate.now();
        try (Connection conn = dbManager.getConnection()) {
            List<Ticket> allTodayTickets = ticketDAO.getTodayTickets(conn, today);
            List<Slot> allSlots = slotDAO.getAllSlots(conn);

            int activeCount = 0;
            for (Slot s : allSlots) {
                if (s.isOccupied()) {
                    activeCount++;
                }
            }

            int parkedTodayCount = 0;
            double totalRevenueToday = 0.0;
            Map<VehicleType, Integer> parkedCountByType = new EnumMap<>(VehicleType.class);
            Map<VehicleType, Double> revenueByType = new EnumMap<>(VehicleType.class);

            for (VehicleType vt : VehicleType.values()) {
                parkedCountByType.put(vt, 0);
                revenueByType.put(vt, 0.0);
            }

            for (Ticket t : allTodayTickets) {
                // Total vehicles parked today based on entry_time date
                if (t.getEntryTime() != null && t.getEntryTime().toLocalDate().equals(today)) {
                    parkedTodayCount++;
                    parkedCountByType.put(t.getVehicleType(), parkedCountByType.get(t.getVehicleType()) + 1);
                }
                // Revenue based on exit_time date
                if ("COMPLETED".equals(t.getStatus()) && t.getExitTime() != null &&
                        t.getExitTime().toLocalDate().equals(today) && t.getFee() != null) {
                    totalRevenueToday += t.getFee();
                    revenueByType.put(t.getVehicleType(), revenueByType.get(t.getVehicleType()) + t.getFee());
                }
            }

            return new ReportData(today, parkedTodayCount, totalRevenueToday, activeCount,
                    parkedCountByType, revenueByType, allTodayTickets);
        } catch (SQLException e) {
            throw new ParkingException("Failed to generate today's report: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all parking slots for grid mapping.
     */
    public List<Slot> getAllSlots() throws ParkingException {
        try (Connection conn = dbManager.getConnection()) {
            return slotDAO.getAllSlots(conn);
        } catch (SQLException e) {
            throw new ParkingException("Failed to retrieve slots: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches active ticket details for a given slot ID.
     */
    public Optional<Ticket> getActiveTicketForSlot(int slotId) throws ParkingException {
        try (Connection conn = dbManager.getConnection()) {
            return ticketDAO.findActiveTicketBySlotId(conn, slotId);
        } catch (SQLException e) {
            throw new ParkingException("Failed to retrieve ticket for slot " + slotId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns total count of slots of a specific type.
     */
    public int getSlotCountByType(VehicleType type) throws ParkingException {
        try (Connection conn = dbManager.getConnection()) {
            return slotDAO.countSlotsByType(conn, type);
        } catch (SQLException e) {
            throw new ParkingException("Failed to count slots for " + type + ": " + e.getMessage(), e);
        }
    }
}

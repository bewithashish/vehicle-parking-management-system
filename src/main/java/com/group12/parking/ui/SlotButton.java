package com.group12.parking.ui;

import com.group12.parking.model.Slot;
import com.group12.parking.model.Ticket;
import com.group12.parking.service.FeeCalculator;
import com.group12.parking.service.ParkingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Custom interactive button representing a parking slot in the visual grid.
 * Demonstrates Unit 5: Swing components, MouseAdapter event handling,
 * mouseEntered/mouseExited hover effects, single/double click detection, and tooltips.
 */
public class SlotButton extends JButton { // Unit 5: Custom Swing Component extending JButton
    // Required specific color specifications
    public static final Color FREE_COLOR = new Color(0x2E, 0x7D, 0x32);       // #2E7D32 Green
    public static final Color FREE_HOVER = new Color(0x38, 0x8E, 0x3C);       // Brighter Green
    public static final Color OCCUPIED_COLOR = new Color(0xC6, 0x28, 0x28);   // #C62828 Red
    public static final Color OCCUPIED_HOVER = new Color(0xD3, 0x2F, 0x2F);   // Brighter Red

    private final Slot slot;
    private final ParkingService parkingService;
    private final Consumer<String> onNavigateToExit;

    public SlotButton(Slot slot, ParkingService parkingService, Consumer<String> onNavigateToExit) {
        this.slot = slot;
        this.parkingService = parkingService;
        this.onNavigateToExit = onNavigateToExit;

        setText(slot.getSlotNumber());
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setPreferredSize(new Dimension(80, 50));
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        applySlotState();

        // Unit 5: MouseAdapter handling mouse events (hover, single-click, double-click)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { // Unit 5: Mouse event - hover enter
                setBackground(slot.isOccupied() ? OCCUPIED_HOVER : FREE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) { // Unit 5: Mouse event - hover exit
                setBackground(slot.isOccupied() ? OCCUPIED_COLOR : FREE_COLOR);
            }

            @Override
            public void mouseClicked(MouseEvent e) { // Unit 5: Mouse event - click handling
                if (e.getClickCount() == 2 && slot.isOccupied()) {
                    // Double-click on an occupied slot switches to Exit tab with vehicle pre-filled
                    handleDoubleClick();
                } else if (e.getClickCount() == 1) {
                    // Single click shows slot details dialog
                    handleSingleClick();
                }
            }
        });
    }

    private void applySlotState() {
        if (slot.isOccupied()) {
            setBackground(OCCUPIED_COLOR);
            setToolTipText("Slot " + slot.getSlotNumber() + " (" + slot.getVehicleType() + ") - OCCUPIED (Double-click to Exit)");
        } else {
            setBackground(FREE_COLOR);
            setToolTipText("Slot " + slot.getSlotNumber() + " (" + slot.getVehicleType() + ") - AVAILABLE");
        }
    }

    private void handleSingleClick() {
        if (!slot.isOccupied()) {
            JOptionPane.showMessageDialog(this,
                    "Slot: " + slot.getSlotNumber() + "\n" +
                    "Type: " + slot.getVehicleType().getDisplayName() + "\n" +
                    "Status: AVAILABLE\n" +
                    "Hourly Rate: " + UiUtils.formatCurrency(FeeCalculator.getRate(slot.getVehicleType())),
                    "Slot Details - " + slot.getSlotNumber(),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Fetch active ticket details
        try {
            Optional<Ticket> ticketOpt = parkingService.getActiveTicketForSlot(slot.getSlotId());
            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                LocalDateTime now = LocalDateTime.now();
                long minutes = Math.max(0, Duration.between(ticket.getEntryTime(), now).toMinutes());
                long hours = FeeCalculator.calculateBillableHours(minutes);
                double runningFee = hours * FeeCalculator.getRate(slot.getVehicleType());

                String details = String.format("""
                        Slot Number: %s
                        Vehicle Type: %s
                        Status: OCCUPIED
                        
                        Vehicle Number: %s
                        Owner Name: %s
                        Entry Time: %s
                        Parked Duration: %d min (%d billable hr)
                        Current Running Fee: %s
                        """,
                        slot.getSlotNumber(),
                        slot.getVehicleType().getDisplayName(),
                        ticket.getVehicleNumber(),
                        ticket.getOwnerName(),
                        UiUtils.formatDateTime(ticket.getEntryTime()),
                        minutes, hours,
                        UiUtils.formatCurrency(runningFee));

                JOptionPane.showMessageDialog(this, details, "Slot Details - " + slot.getSlotNumber(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Slot " + slot.getSlotNumber() + " is marked occupied.", "Slot Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "Error", "Failed to fetch slot details: " + ex.getMessage());
        }
    }

    private void handleDoubleClick() {
        try {
            Optional<Ticket> ticketOpt = parkingService.getActiveTicketForSlot(slot.getSlotId());
            if (ticketOpt.isPresent() && onNavigateToExit != null) {
                onNavigateToExit.accept(ticketOpt.get().getVehicleNumber());
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "Navigation Error", "Could not load vehicle details: " + ex.getMessage());
        }
    }

    public Slot getSlot() {
        return slot;
    }
}

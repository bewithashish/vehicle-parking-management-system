package com.group12.parking.ui;

import com.group12.parking.model.Slot;
import com.group12.parking.model.VehicleType;
import com.group12.parking.service.ParkingService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Visual Dashboard panel showing the parking layout for Bikes, Cars, and Trucks.
 * Demonstrates Unit 5: Swing Layout Managers (BorderLayout, GridLayout, FlowLayout, BoxLayout),
 * JScrollPane, JPanels, custom color badges, and interactive slot mapping.
 */
public class DashboardPanel extends JPanel { // Unit 5: Swing JPanel
    private final ParkingService parkingService;
    private final Consumer<String> onNavigateToExit;
    private final Runnable onDataChanged;

    private final JPanel bikeSlotsGrid;
    private final JPanel carSlotsGrid;
    private final JPanel truckSlotsGrid;

    public DashboardPanel(ParkingService parkingService, Consumer<String> onNavigateToExit, Runnable onDataChanged) {
        this.parkingService = parkingService;
        this.onNavigateToExit = onNavigateToExit;
        this.onDataChanged = onDataChanged;

        // Unit 5: BorderLayout
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Legend Panel (Unit 5: FlowLayout)
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.NORTH);

        // Center Container holding the 3 vehicle categories (Unit 5: BoxLayout)
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        bikeSlotsGrid = new JPanel();
        carSlotsGrid = new JPanel();
        truckSlotsGrid = new JPanel();

        centerContainer.add(createSectionPanel("Bike Slots (Rs. 10/hr)", bikeSlotsGrid));
        centerContainer.add(Box.createVerticalStrut(10));
        centerContainer.add(createSectionPanel("Car Slots (Rs. 20/hr)", carSlotsGrid));
        centerContainer.add(Box.createVerticalStrut(10));
        centerContainer.add(createSectionPanel("Truck Slots (Rs. 40/hr)", truckSlotsGrid));

        // Wrap the slot map in a scroll pane (Unit 5: JScrollPane)
        JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        refreshSlots();
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6)); // Unit 5: FlowLayout
        panel.setBorder(BorderFactory.createTitledBorder("Legend & Instructions"));

        JLabel freeBox = createColorBadge(SlotButton.FREE_COLOR, "  Available  ");
        JLabel occupiedBox = createColorBadge(SlotButton.OCCUPIED_COLOR, "  Occupied  ");
        JLabel hintLabel = new JLabel("<html><i>Click slot for info | Double-click occupied slot to exit</i></html>");
        hintLabel.setFont(UiUtils.REGULAR_FONT);

        panel.add(freeBox);
        panel.add(occupiedBox);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(hintLabel);

        return panel;
    }

    private JLabel createColorBadge(Color bg, String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiUtils.BOLD_FONT);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return label;
    }

    private JPanel createSectionPanel(String title, JPanel gridContainer) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, 0, 0, UiUtils.HEADER_FONT));
        section.add(gridContainer, BorderLayout.CENTER);
        return section;
    }

    /**
     * Refreshes the visual slot map from the database on the Event Dispatch Thread (EDT).
     */
    public void refreshSlots() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshSlots);
            return;
        }

        try {
            List<Slot> allSlots = parkingService.getAllSlots();

            List<Slot> bikeSlots = new ArrayList<>();
            List<Slot> carSlots = new ArrayList<>();
            List<Slot> truckSlots = new ArrayList<>();

            for (Slot s : allSlots) {
                switch (s.getVehicleType()) {
                    case BIKE -> bikeSlots.add(s);
                    case CAR -> carSlots.add(s);
                    case TRUCK -> truckSlots.add(s);
                }
            }

            populateGrid(bikeSlotsGrid, bikeSlots);
            populateGrid(carSlotsGrid, carSlots);
            populateGrid(truckSlotsGrid, truckSlots);

            revalidate();
            repaint();

            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (Exception e) {
            // Log quietly during background poll
            System.err.println("Notice: Slot refresh exception: " + e.getMessage());
        }
    }

    private void populateGrid(JPanel container, List<Slot> slots) {
        container.removeAll();
        int columns = 5; // Required: 5 columns
        int rows = Math.max(1, (int) Math.ceil((double) slots.size() / columns)); // Unit 5: Computed rows

        // Unit 5: GridLayout for the slot map (columns = 5, rows computed)
        container.setLayout(new GridLayout(rows, columns, 8, 8));
        container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Slot slot : slots) {
            SlotButton btn = new SlotButton(slot, parkingService, onNavigateToExit);
            container.add(btn);
        }

        // Fill remaining cells in grid if any to keep button sizes uniform
        int totalCells = rows * columns;
        for (int i = slots.size(); i < totalCells; i++) {
            JPanel placeholder = new JPanel();
            placeholder.setPreferredSize(new Dimension(80, 50));
            placeholder.setOpaque(false);
            container.add(placeholder);
        }
    }
}

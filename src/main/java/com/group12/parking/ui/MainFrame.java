package com.group12.parking.ui;

import com.group12.parking.model.Slot;
import com.group12.parking.service.ParkingService;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Main application window for Vehicle Parking Management System.
 * Demonstrates Unit 5: Swing JFrame, JTabbedPane, WindowListener, BorderLayout,
 * and integration of all functional panels.
 */
public class MainFrame extends JFrame { // Unit 5: JFrame
    private final ParkingService parkingService;

    private final JLabel headerStatsLabel;
    private final JTabbedPane tabbedPane;
    private final DashboardPanel dashboardPanel;
    private final EntryPanel entryPanel;
    private final ExitPanel exitPanel;
    private final SlotConfigPanel slotConfigPanel;
    private final ReportPanel reportPanel;

    private Runnable onWindowClosingCallback;

    public MainFrame(ParkingService parkingService) {
        this.parkingService = parkingService;

        // Frame initialization (1000x700, centered, title)
        setTitle("Vehicle Parking Management System - Group 12");
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout()); // Unit 5: BorderLayout

        // Top Header Bar with Live Occupancy Counters
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x1E, 0x29, 0x3B)); // Dark slate header
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("Vehicle Parking Management System");
        titleLabel.setFont(UiUtils.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        headerStatsLabel = new JLabel("Free 0 / Occupied 0 / Total 0");
        headerStatsLabel.setFont(UiUtils.HEADER_FONT);
        headerStatsLabel.setForeground(Color.WHITE);
        headerPanel.add(headerStatsLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Functional Panels
        dashboardPanel = new DashboardPanel(parkingService, this::navigateToExitWithVehicle, this::updateHeaderStats);
        entryPanel = new EntryPanel(parkingService, this::onDataChanged);
        exitPanel = new ExitPanel(parkingService, this::onDataChanged);
        slotConfigPanel = new SlotConfigPanel(parkingService, this::onDataChanged);
        reportPanel = new ReportPanel(parkingService);

        // JTabbedPane in EXACT required order: Dashboard, Entry, Exit, Slot Config, Report
        tabbedPane = new JTabbedPane(); // Unit 5: JTabbedPane
        tabbedPane.setFont(UiUtils.BOLD_FONT);
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Entry", entryPanel);
        tabbedPane.addTab("Exit", exitPanel);
        tabbedPane.addTab("Slot Config", slotConfigPanel);
        tabbedPane.addTab("Report", reportPanel);

        // Unit 5: ChangeListener for tab selection
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                dashboardPanel.refreshSlots();
            } else if (selectedIndex == 3) {
                slotConfigPanel.loadCurrentCounts();
            } else if (selectedIndex == 4) {
                reportPanel.refreshReport();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Unit 5: WindowListener (WindowAdapter) to stop background threads cleanly on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { // Unit 5: WindowEvent handling
                if (onWindowClosingCallback != null) {
                    onWindowClosingCallback.run();
                }
            }
        });

        updateHeaderStats();
    }

    /**
     * Navigates to Exit tab (index 2) and pre-fills the search field with the given vehicle number.
     */
    public void navigateToExitWithVehicle(String vehicleNumber) {
        tabbedPane.setSelectedIndex(2); // Switch to Exit tab
        exitPanel.setVehicleSearchQuery(vehicleNumber);
    }

    /**
     * Callback when parking data changes (entry, exit, config).
     */
    public void onDataChanged() {
        dashboardPanel.refreshSlots();
        updateHeaderStats();
        slotConfigPanel.loadCurrentCounts();
        reportPanel.refreshReport();
    }

    /**
     * Updates header label showing live counts: "Free X / Occupied Y / Total Z"
     */
    public void updateHeaderStats() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateHeaderStats);
            return;
        }

        try {
            List<Slot> slots = parkingService.getAllSlots();
            int total = slots.size();
            int occupied = 0;
            for (Slot s : slots) {
                if (s.isOccupied()) {
                    occupied++;
                }
            }
            int free = total - occupied;
            headerStatsLabel.setText(String.format("Free %d / Occupied %d / Total %d", free, occupied, total));
        } catch (Exception e) {
            System.err.println("Notice: Could not update header counts: " + e.getMessage());
        }
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public void setOnWindowClosingCallback(Runnable callback) {
        this.onWindowClosingCallback = callback;
    }
}

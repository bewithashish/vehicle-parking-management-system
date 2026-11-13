package com.group12.parking;

import com.formdev.flatlaf.FlatLightLaf;
import com.group12.parking.dao.DBManager;
import com.group12.parking.service.ParkingService;
import com.group12.parking.thread.SlotStatusUpdater;
import com.group12.parking.ui.MainFrame;
import com.group12.parking.ui.UiUtils;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Application Entry Point for the Vehicle Parking Management System.
 * Demonstrates Unit 1 (Static main method), Unit 3 (Multithreading & Daemon thread setup),
 * Unit 4 (Database initialization), and Unit 5 (FlatLaf theme and Event Dispatch Thread creation).
 */
public class Main {

    public static void main(String[] args) { // Unit 1: Main execution entry point
        try {
            // Unit 5: FlatLaf Modern Look and Feel setup before UI creation
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("Notice: FlatLaf setup warning: " + e.getMessage());
        }

        // Unit 4: Initialize SQLite schema and seed initial slots
        try {
            DBManager.getInstance().initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal: Database initialization failed: " + e.getMessage());
            if (!GraphicsEnvironment.isHeadless()) {
                UiUtils.showError(null, "Database Error", "Failed to initialize database: " + e.getMessage());
            }
            return;
        }

        // Check headless mode for CI/server environments
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment detected. Database initialized and system ready (GUI suppressed).");
            return;
        }

        // Unit 3 & Unit 5: Launch GUI strictly on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                ParkingService parkingService = new ParkingService();
                MainFrame frame = new MainFrame(parkingService);

                // Unit 3: Initialize and start background SlotStatusUpdater daemon thread
                SlotStatusUpdater updater = new SlotStatusUpdater(frame.getDashboardPanel(), frame);
                updater.start();

                // Cleanly terminate daemon thread when main window closes
                frame.setOnWindowClosingCallback(updater::stop);

                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace(); // Never crash silently
                UiUtils.showError(null, "System Error", "Failed to launch application: " + e.getMessage());
            }
        });
    }
}

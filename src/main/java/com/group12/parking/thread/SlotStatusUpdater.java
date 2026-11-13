package com.group12.parking.thread;

import com.group12.parking.ui.DashboardPanel;
import com.group12.parking.ui.MainFrame;
import com.group12.parking.util.Constants;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background daemon worker thread that periodically polls slot occupancy
 * from the database and refreshes the Swing UI.
 * Demonstrates Unit 3: Multithreading, Runnable interface, ScheduledExecutorService,
 * daemon threads, and thread-safe UI updates via SwingUtilities.invokeLater.
 */
public class SlotStatusUpdater implements Runnable { // Unit 3: Multithreading & Runnable interface
    private final DashboardPanel dashboardPanel;
    private final MainFrame mainFrame;
    private ScheduledExecutorService scheduler;

    public SlotStatusUpdater(DashboardPanel dashboardPanel, MainFrame mainFrame) {
        this.dashboardPanel = dashboardPanel;
        this.mainFrame = mainFrame;
    }

    /**
     * Starts the periodic daemon scheduler executing every 3 seconds.
     */
    public synchronized void start() {
        if (scheduler == null || scheduler.isShutdown()) {
            // Unit 3: Daemon thread creation using ScheduledExecutorService
            scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "SlotStatusUpdater-Thread");
                t.setDaemon(true); // Unit 3: Daemon thread
                return t;
            });
            scheduler.scheduleWithFixedDelay(this,
                    Constants.REFRESH_INTERVAL_SECONDS,
                    Constants.REFRESH_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the background thread cleanly when the window is closed.
     */
    public synchronized void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public void run() { // Unit 3: Background thread execution logic
        try {
            // Unit 3 & Unit 5: Thread-safe UI update strictly via SwingUtilities.invokeLater on EDT
            SwingUtilities.invokeLater(() -> {
                dashboardPanel.refreshSlots();
                mainFrame.updateHeaderStats();
            });
        } catch (Exception e) {
            System.err.println("Notice: Exception in SlotStatusUpdater background thread: " + e.getMessage());
        }
    }
}

package com.group12.parking.ui;

import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;
import com.group12.parking.service.ParkingService;
import com.group12.parking.service.ReportData;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Report panel showing real-time statistics and a tabular audit log of today's tickets.
 * Demonstrates Unit 5: Swing JTable with custom non-editable DefaultTableModel,
 * TableRowSorter, JScrollPane, and summary dashboard panels.
 */
public class ReportPanel extends JPanel { // Unit 5: Swing JPanel
    private final ParkingService parkingService;

    private final JLabel totalParkedLabel;
    private final JLabel totalRevenueLabel;
    private final JLabel activeVehiclesLabel;

    private final DefaultTableModel breakdownTableModel;
    private final DefaultTableModel ticketsTableModel;
    private final JTable ticketsTable;

    public ReportPanel(ParkingService parkingService) {
        this.parkingService = parkingService;

        setLayout(new BorderLayout(10, 10)); // Unit 5: BorderLayout
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Summary Cards Panel
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 12)); // Unit 5: GridLayout
        totalParkedLabel = createSummaryCard(statsRow, "Vehicles Parked Today", "0");
        totalRevenueLabel = createSummaryCard(statsRow, "Total Revenue Today", "Rs. 0.00");
        activeVehiclesLabel = createSummaryCard(statsRow, "Currently Parked Active", "0");
        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(10));

        // Breakdown Table by Vehicle Type
        String[] breakdownCols = {"Vehicle Type", "Vehicles Count", "Revenue Collected"};
        breakdownTableModel = new DefaultTableModel(breakdownCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Non-editable table
            }
        };
        JTable breakdownTable = new JTable(breakdownTableModel); // Unit 5: JTable
        breakdownTable.getTableHeader().setFont(UiUtils.BOLD_FONT);
        breakdownTable.setRowHeight(24);
        JScrollPane breakdownScroll = new JScrollPane(breakdownTable);
        breakdownScroll.setPreferredSize(new Dimension(800, 95));
        breakdownScroll.setBorder(BorderFactory.createTitledBorder("Breakdown by Vehicle Type"));
        topContainer.add(breakdownScroll);

        add(topContainer, BorderLayout.NORTH);

        // Center: Today's Tickets Table
        String[] ticketCols = {"Ticket ID", "Vehicle No", "Owner", "Type", "Slot", "Entry Time", "Exit Time", "Fee", "Status"};
        ticketsTableModel = new DefaultTableModel(ticketCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Non-editable table
            }
        };
        ticketsTable = new JTable(ticketsTableModel); // Unit 5: JTable
        ticketsTable.getTableHeader().setFont(UiUtils.BOLD_FONT);
        ticketsTable.setRowHeight(22);

        // Unit 5: TableRowSorter enabling column sorting by clicking table header
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(ticketsTableModel);
        ticketsTable.setRowSorter(sorter);

        JScrollPane ticketsScroll = new JScrollPane(ticketsTable); // Unit 5: JScrollPane
        ticketsScroll.setBorder(BorderFactory.createTitledBorder("Today's Tickets Audit Log (Click headers to sort)"));
        add(ticketsScroll, BorderLayout.CENTER);

        // Bottom Controls Panel
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh Report"); // Unit 5: JButton
        refreshBtn.setFont(UiUtils.BOLD_FONT);
        refreshBtn.addActionListener(e -> refreshReport());
        bottomBar.add(refreshBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshReport();
    }

    private JLabel createSummaryCard(JPanel parent, String title, String initialVal) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UiUtils.REGULAR_FONT);
        JLabel valueLbl = new JLabel(initialVal);
        valueLbl.setFont(UiUtils.TITLE_FONT);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        parent.add(card);
        return valueLbl;
    }

    /**
     * Refreshes the metrics, breakdown, and ticket table on the EDT.
     */
    public void refreshReport() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshReport);
            return;
        }

        try {
            ReportData report = parkingService.getTodayReport();

            totalParkedLabel.setText(String.valueOf(report.getTotalVehiclesParkedToday()));
            totalRevenueLabel.setText(UiUtils.formatCurrency(report.getTotalRevenueToday()));
            activeVehiclesLabel.setText(String.valueOf(report.getCurrentlyActiveCount()));

            // Update breakdown table
            breakdownTableModel.setRowCount(0);
            for (VehicleType vt : VehicleType.values()) {
                int count = report.getParkedCountByType().getOrDefault(vt, 0);
                double revenue = report.getRevenueByType().getOrDefault(vt, 0.0);
                breakdownTableModel.addRow(new Object[]{
                        vt.getDisplayName(),
                        count,
                        UiUtils.formatCurrency(revenue)
                });
            }

            // Update tickets table
            ticketsTableModel.setRowCount(0);
            List<Ticket> tickets = report.getTodayTickets();
            for (Ticket t : tickets) {
                ticketsTableModel.addRow(new Object[]{
                        "#" + t.getTicketId(),
                        t.getVehicleNumber(),
                        t.getOwnerName(),
                        t.getVehicleType().getDisplayName(),
                        t.getSlotNumber(),
                        UiUtils.formatDateTime(t.getEntryTime()),
                        t.getExitTime() != null ? UiUtils.formatDateTime(t.getExitTime()) : "-- Parked --",
                        t.getFee() != null ? UiUtils.formatCurrency(t.getFee()) : "--",
                        t.getStatus()
                });
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "Report Error", "Failed to load report: " + ex.getMessage());
        }
    }
}

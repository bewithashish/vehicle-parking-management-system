package com.group12.parking.ui;

import com.group12.parking.exception.ParkingException;
import com.group12.parking.service.ParkingService;
import com.group12.parking.service.ReceiptInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Exit panel to process vehicle check-out, fee calculation, and receipt generation.
 * Demonstrates Unit 3: Exception handling and Unit 5: Swing components (JTextField, JCheckBox, JButton, Event Handling).
 */
public class ExitPanel extends JPanel { // Unit 5: Swing JPanel
    private final ParkingService parkingService;
    private final Runnable onExitSuccess;

    private final JTextField searchField;
    private final JCheckBox memberCheckBox;
    private final JButton exitButton;

    public ExitPanel(ParkingService parkingService, Runnable onExitSuccess) {
        this.parkingService = parkingService;
        this.onExitSuccess = onExitSuccess;

        setLayout(new GridBagLayout()); // Unit 5: GridBagLayout
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vehicle Check-Out / Exit", 0, 0, UiUtils.HEADER_FONT),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel titleLabel = new JLabel("Process Vehicle Departure");
        titleLabel.setFont(UiUtils.TITLE_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(titleLabel, gbc);

        // Search Field (Vehicle number or Ticket ID)
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel searchLabel = new JLabel("Vehicle Number or Ticket ID:");
        searchLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(searchLabel, gbc);

        gbc.gridx = 1;
        searchField = new JTextField(15);
        searchField.setFont(UiUtils.REGULAR_FONT);
        searchField.setToolTipText("Enter plate number (e.g. PB65AB1234) or numeric ticket ID (e.g. 1)");
        formCard.add(searchField, gbc);

        // Member Checkbox (10% discount)
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        memberCheckBox = new JCheckBox("Member (10% discount)"); // Unit 5: JCheckBox
        memberCheckBox.setFont(UiUtils.BOLD_FONT);
        formCard.add(memberCheckBox, gbc);

        // Exit & Pay Button
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        exitButton = new JButton("Calculate & Exit"); // Unit 5: JButton
        exitButton.setFont(UiUtils.HEADER_FONT);
        exitButton.setPreferredSize(new Dimension(180, 38));
        // Unit 5: ActionListener handling departure
        exitButton.addActionListener(e -> handleExit());
        formCard.add(exitButton, gbc);

        add(formCard);
    }

    /**
     * Programmatically populates search field when user double-clicks an occupied slot on Dashboard.
     */
    public void setVehicleSearchQuery(String query) {
        searchField.setText(query);
        memberCheckBox.setSelected(false);
        searchField.requestFocusInWindow();
    }

    private void handleExit() {
        String input = searchField.getText();
        boolean isMember = memberCheckBox.isSelected();

        try {
            // Unit 3: Exception-safe service call
            ReceiptInfo receipt = parkingService.exitVehicle(input, isMember);

            // Detailed Receipt Dialog
            String receiptMessage = String.format("""
                    ================ PARKING RECEIPT ================
                    Ticket ID:           #%d
                    Vehicle Number:      %s
                    Owner Name:          %s
                    Vehicle Type:        %s
                    Slot Allocated:      %s
                    -------------------------------------------------
                    Entry Time:          %s
                    Exit Time:           %s
                    Duration:            %d minute(s)
                    Billable Hours:      %d hour(s)
                    Hourly Rate:         %s
                    Discount:            %s (%s)
                    -------------------------------------------------
                    TOTAL AMOUNT DUE:    %s
                    =================================================
                    """,
                    receipt.getTicketId(),
                    receipt.getVehicleNumber(),
                    receipt.getOwnerName(),
                    receipt.getVehicleType().getDisplayName(),
                    receipt.getSlotNumber(),
                    UiUtils.formatDateTime(receipt.getEntryTime()),
                    UiUtils.formatDateTime(receipt.getExitTime()),
                    receipt.getDurationMinutes(),
                    receipt.getBillableHours(),
                    UiUtils.formatCurrency(receipt.getHourlyRate()),
                    UiUtils.formatCurrency(receipt.getDiscountAmount()),
                    receipt.isMember() ? "Member 10%" : "None",
                    UiUtils.formatCurrency(receipt.getTotalFee())
            );

            JOptionPane.showMessageDialog(this, receiptMessage, "Departure Receipt - Payment Settled", JOptionPane.INFORMATION_MESSAGE);

            searchField.setText("");
            memberCheckBox.setSelected(false);

            if (onExitSuccess != null) {
                onExitSuccess.run();
            }
        } catch (ParkingException ex) {
            UiUtils.showError(this, "Exit Failed", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            UiUtils.showError(this, "System Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }
}

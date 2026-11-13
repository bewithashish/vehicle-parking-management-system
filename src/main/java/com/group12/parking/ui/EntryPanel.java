package com.group12.parking.ui;

import com.group12.parking.exception.ParkingException;
import com.group12.parking.exception.ParkingFullException;
import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;
import com.group12.parking.service.ParkingService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Entry panel to record vehicle entry, allocate slots, and issue tickets.
 * Demonstrates Unit 3: Exception handling (ParkingFullException, ParkingException)
 * and Unit 5: Swing form components (JTextField, JComboBox, JButton, GridBagLayout).
 */
public class EntryPanel extends JPanel { // Unit 5: Swing JPanel
    private final ParkingService parkingService;
    private final Runnable onParkSuccess;

    private final JTextField vehicleNumberField;
    private final JTextField ownerNameField;
    private final JComboBox<VehicleType> vehicleTypeCombo;
    private final JButton parkButton;

    public EntryPanel(ParkingService parkingService, Runnable onParkSuccess) {
        this.parkingService = parkingService;
        this.onParkSuccess = onParkSuccess;

        setLayout(new GridBagLayout()); // Unit 5: GridBagLayout for aligned form layout
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vehicle Check-In / Entry", 0, 0, UiUtils.HEADER_FONT),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header Title
        JLabel titleLabel = new JLabel("Park a New Vehicle");
        titleLabel.setFont(UiUtils.TITLE_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(titleLabel, gbc);

        // Vehicle Number Field
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel numLabel = new JLabel("Vehicle Registration Number:");
        numLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(numLabel, gbc);

        gbc.gridx = 1;
        vehicleNumberField = new JTextField(15);
        vehicleNumberField.setFont(UiUtils.REGULAR_FONT);
        vehicleNumberField.setToolTipText("e.g. PB65AB1234, DL01C1234");
        formCard.add(vehicleNumberField, gbc);

        // Owner Name Field
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel ownerLabel = new JLabel("Vehicle Owner Name:");
        ownerLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(ownerLabel, gbc);

        gbc.gridx = 1;
        ownerNameField = new JTextField(15);
        ownerNameField.setFont(UiUtils.REGULAR_FONT);
        formCard.add(ownerNameField, gbc);

        // Vehicle Type ComboBox
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel typeLabel = new JLabel("Vehicle Type:");
        typeLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(typeLabel, gbc);

        gbc.gridx = 1;
        vehicleTypeCombo = new JComboBox<>(VehicleType.values()); // Unit 5: JComboBox
        vehicleTypeCombo.setFont(UiUtils.REGULAR_FONT);
        vehicleTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VehicleType vt) {
                    setText(vt.getDisplayName() + " (" + vt.name() + ")");
                }
                return this;
            }
        });
        formCard.add(vehicleTypeCombo, gbc);

        // Park Vehicle Button
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        parkButton = new JButton("Park Vehicle"); // Unit 5: JButton
        parkButton.setFont(UiUtils.HEADER_FONT);
        parkButton.setPreferredSize(new Dimension(180, 38));
        // Unit 5: ActionListener event handling via lambda
        parkButton.addActionListener(e -> handleParkVehicle());
        formCard.add(parkButton, gbc);

        add(formCard);
    }

    private void handleParkVehicle() {
        String vehicleNumber = vehicleNumberField.getText();
        String ownerName = ownerNameField.getText();
        VehicleType type = (VehicleType) vehicleTypeCombo.getSelectedItem();

        try {
            // Unit 3: Method call declaring checked exceptions
            Ticket ticket = parkingService.parkVehicle(vehicleNumber, ownerName, type);

            // Success ticket dialog
            String message = String.format("""
                    Parking Ticket Issued Successfully!
                    
                    Ticket ID: #%d
                    Vehicle Number: %s
                    Owner Name: %s
                    Vehicle Type: %s
                    Assigned Slot: %s
                    Entry Time: %s
                    """,
                    ticket.getTicketId(),
                    ticket.getVehicleNumber(),
                    ticket.getOwnerName(),
                    type.getDisplayName(),
                    ticket.getSlotNumber(),
                    UiUtils.formatDateTime(ticket.getEntryTime()));

            JOptionPane.showMessageDialog(this, message, "Parking Ticket - Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            vehicleNumberField.setText("");
            ownerNameField.setText("");
            vehicleTypeCombo.setSelectedIndex(0);

            // Notify parent to refresh slot grid and header counts
            if (onParkSuccess != null) {
                onParkSuccess.run();
            }
        } catch (ParkingFullException ex) {
            // Unit 3: Catching custom checked ParkingFullException
            JOptionPane.showMessageDialog(this,
                    "Sorry, parking is currently full for " + type.getDisplayName() + " vehicles.\n" +
                    "Please wait for a slot to become available or adjust slot capacity.",
                    "Parking Full", JOptionPane.WARNING_MESSAGE);
        } catch (ParkingException ex) {
            // Unit 3: Catching business rule ParkingException
            UiUtils.showError(this, "Entry Rejected", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            UiUtils.showError(this, "System Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }
}

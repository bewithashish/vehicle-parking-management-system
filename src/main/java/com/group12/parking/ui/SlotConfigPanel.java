package com.group12.parking.ui;

import com.group12.parking.exception.ParkingException;
import com.group12.parking.model.VehicleType;
import com.group12.parking.service.ParkingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Slot Configuration panel allowing administrators to scale parking slot capacity (0 - 100).
 * Demonstrates Unit 3: Exception handling and Unit 5: Swing JSpinner components and form layout.
 */
public class SlotConfigPanel extends JPanel { // Unit 5: Swing JPanel
    private final ParkingService parkingService;
    private final Runnable onConfigApplied;

    private final JSpinner bikeCountSpinner;
    private final JSpinner carCountSpinner;
    private final JSpinner truckCountSpinner;
    private final JButton applyButton;

    public SlotConfigPanel(ParkingService parkingService, Runnable onConfigApplied) {
        this.parkingService = parkingService;
        this.onConfigApplied = onConfigApplied;

        setLayout(new GridBagLayout()); // Unit 5: GridBagLayout
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Parking Slot Capacity Configuration", 0, 0, UiUtils.HEADER_FONT),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel titleLabel = new JLabel("Adjust Slot Counts (0 to 100)");
        titleLabel.setFont(UiUtils.TITLE_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(titleLabel, gbc);

        // Bike Slots Spinner
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel bikeLabel = new JLabel("Bike Slots Capacity:");
        bikeLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(bikeLabel, gbc);

        gbc.gridx = 1;
        bikeCountSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1)); // Unit 5: JSpinner
        bikeCountSpinner.setFont(UiUtils.REGULAR_FONT);
        formCard.add(bikeCountSpinner, gbc);

        // Car Slots Spinner
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel carLabel = new JLabel("Car Slots Capacity:");
        carLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(carLabel, gbc);

        gbc.gridx = 1;
        carCountSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        carCountSpinner.setFont(UiUtils.REGULAR_FONT);
        formCard.add(carCountSpinner, gbc);

        // Truck Slots Spinner
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel truckLabel = new JLabel("Truck Slots Capacity:");
        truckLabel.setFont(UiUtils.BOLD_FONT);
        formCard.add(truckLabel, gbc);

        gbc.gridx = 1;
        truckCountSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        truckCountSpinner.setFont(UiUtils.REGULAR_FONT);
        formCard.add(truckCountSpinner, gbc);

        // Apply Configuration Button
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        applyButton = new JButton("Apply Configuration"); // Unit 5: JButton
        applyButton.setFont(UiUtils.HEADER_FONT);
        applyButton.setPreferredSize(new Dimension(200, 38));
        applyButton.addActionListener(e -> handleApplyConfig());
        formCard.add(applyButton, gbc);

        add(formCard);

        loadCurrentCounts();
    }

    /**
     * Loads current counts from the database to prefill spinners.
     */
    public void loadCurrentCounts() {
        try {
            int bikeCount = parkingService.getSlotCountByType(VehicleType.BIKE);
            int carCount = parkingService.getSlotCountByType(VehicleType.CAR);
            int truckCount = parkingService.getSlotCountByType(VehicleType.TRUCK);

            bikeCountSpinner.setValue(bikeCount);
            carCountSpinner.setValue(carCount);
            truckCountSpinner.setValue(truckCount);
        } catch (Exception e) {
            System.err.println("Notice: Could not load slot counts: " + e.getMessage());
        }
    }

    private void handleApplyConfig() {
        int bikes = (int) bikeCountSpinner.getValue();
        int cars = (int) carCountSpinner.getValue();
        int trucks = (int) truckCountSpinner.getValue();

        try {
            // Unit 3: Exception handling during transactional slot adjustment
            parkingService.configureSlots(bikes, cars, trucks);

            JOptionPane.showMessageDialog(this,
                    "Slot configuration applied successfully!\n" +
                    "Bikes: " + bikes + " | Cars: " + cars + " | Trucks: " + trucks,
                    "Configuration Updated", JOptionPane.INFORMATION_MESSAGE);

            if (onConfigApplied != null) {
                onConfigApplied.run();
            }
        } catch (ParkingException ex) {
            UiUtils.showError(this, "Configuration Refused", ex.getMessage());
            loadCurrentCounts(); // Reset spinners to actual DB values
        } catch (Exception ex) {
            ex.printStackTrace();
            UiUtils.showError(this, "System Error", "Failed to update slots: " + ex.getMessage());
            loadCurrentCounts();
        }
    }
}

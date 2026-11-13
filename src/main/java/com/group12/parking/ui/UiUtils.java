package com.group12.parking.ui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for UI styling, standard dialogs, and formatting.
 * Demonstrates Unit 5: Swing UI helper utilities and JOptionPane message dialogs.
 */
public final class UiUtils {
    private UiUtils() {}

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public static String formatDateTime(LocalDateTime ldt) {
        if (ldt == null) return "N/A";
        return ldt.format(TIME_FORMATTER);
    }

    public static String formatCurrency(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    // Unit 5: JOptionPane error dialog with title
    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Unit 5: JOptionPane information dialog with title
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Unit 5: JOptionPane warning dialog with title
    public static void showWarning(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
}

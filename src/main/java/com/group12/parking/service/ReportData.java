package com.group12.parking.service;

import com.group12.parking.model.Ticket;
import com.group12.parking.model.VehicleType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) holding aggregated parking report metrics for a specific day.
 * Demonstrates Unit 1: Encapsulation and collection management.
 */
public class ReportData {
    private final LocalDate reportDate;
    private final int totalVehiclesParkedToday;
    private final double totalRevenueToday;
    private final int currentlyActiveCount;
    private final Map<VehicleType, Integer> parkedCountByType;
    private final Map<VehicleType, Double> revenueByType;
    private final List<Ticket> todayTickets;

    public ReportData(LocalDate reportDate, int totalVehiclesParkedToday, double totalRevenueToday,
                      int currentlyActiveCount, Map<VehicleType, Integer> parkedCountByType,
                      Map<VehicleType, Double> revenueByType, List<Ticket> todayTickets) {
        this.reportDate = reportDate;
        this.totalVehiclesParkedToday = totalVehiclesParkedToday;
        this.totalRevenueToday = totalRevenueToday;
        this.currentlyActiveCount = currentlyActiveCount;
        this.parkedCountByType = parkedCountByType;
        this.revenueByType = revenueByType;
        this.todayTickets = todayTickets;
    }

    public LocalDate getReportDate() { return reportDate; }
    public int getTotalVehiclesParkedToday() { return totalVehiclesParkedToday; }
    public double getTotalRevenueToday() { return totalRevenueToday; }
    public int getCurrentlyActiveCount() { return currentlyActiveCount; }
    public Map<VehicleType, Integer> getParkedCountByType() { return Collections.unmodifiableMap(parkedCountByType); }
    public Map<VehicleType, Double> getRevenueByType() { return Collections.unmodifiableMap(revenueByType); }
    public List<Ticket> getTodayTickets() { return Collections.unmodifiableList(todayTickets); }
}

package com.homeservice.ui.admin;

import com.homeservice.service.StatisticsService;
import com.homeservice.ui.common.GradientCard;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class AdminDashboardPanel extends JPanel {
    private final StatisticsService statsService = new StatisticsService();

    private JPanel metricsRow;
    private JTabbedPane tabbedPane;

    private JPanel overviewPanel;
    private AdminUsersPanel usersPanel;
    private AdminServicesPanel servicesPanel;
    private AdminBookingsPanel bookingsPanel;
    private AdminReviewsPanel reviewsPanel;

    private JPanel statusCardsContainer;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // Top 5 Key Metric Cards
        metricsRow = new JPanel(new GridLayout(1, 5, 12, 12));
        metricsRow.setOpaque(false);
        add(metricsRow, BorderLayout.NORTH);

        // Subtabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        Runnable refreshStats = this::updateMetrics;

        // 1. Overview Tab
        overviewPanel = createOverviewTab();
        usersPanel = new AdminUsersPanel(refreshStats);
        servicesPanel = new AdminServicesPanel(refreshStats);
        bookingsPanel = new AdminBookingsPanel(refreshStats);
        reviewsPanel = new AdminReviewsPanel(refreshStats);

        tabbedPane.addTab("  📊 System Overview  ", overviewPanel);
        tabbedPane.addTab("  👥 User Management  ", usersPanel);
        tabbedPane.addTab("  🛠️ Services & Pricing  ", servicesPanel);
        tabbedPane.addTab("  📋 Master Bookings  ", bookingsPanel);
        tabbedPane.addTab("  ⭐ Reviews & Ratings  ", reviewsPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) updateMetrics();
            else if (idx == 1) usersPanel.loadUsers();
            else if (idx == 2) servicesPanel.loadCategories();
            else if (idx == 3) bookingsPanel.loadBookings();
            else if (idx == 4) reviewsPanel.loadReviews();
            updateMetrics();
        });

        add(tabbedPane, BorderLayout.CENTER);
        updateMetrics();
    }

    public void setSelectedIndex(int idx) {
        if (idx >= 0 && idx < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(idx);
        }
    }

    private JPanel createOverviewTab() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBackground(UITheme.BG_MAIN);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Status Breakdown Panel
        JPanel statusCard = UITheme.createCardPanel();
        statusCard.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Booking Lifecycle Status Breakdown");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);
        statusCard.add(title, BorderLayout.NORTH);

        statusCardsContainer = new JPanel(new GridLayout(2, 3, 12, 12));
        statusCardsContainer.setOpaque(false);
        statusCard.add(statusCardsContainer, BorderLayout.CENTER);

        // Bottom Academic Info Banner
        JPanel infoBanner = UITheme.createCardPanel();
        infoBanner.setLayout(new BorderLayout(10, 10));
        JLabel lblHeader = new JLabel("Home Service Booking & Management System — Academic Project");
        lblHeader.setFont(UITheme.FONT_SUBHEADER);
        lblHeader.setForeground(UITheme.PRIMARY);

        JLabel lblAuthors = new JLabel("Submitted by: Abhinav Sajeev (25CS009) | Alan Jojo (25CS032) | Allan Peter (25CS038) | Sebin Raju (25CS187)");
        lblAuthors.setFont(UITheme.FONT_BODY);
        lblAuthors.setForeground(UITheme.TEXT_MUTED);

        infoBanner.add(lblHeader, BorderLayout.NORTH);
        infoBanner.add(lblAuthors, BorderLayout.CENTER);

        p.add(statusCard, BorderLayout.CENTER);
        p.add(infoBanner, BorderLayout.SOUTH);
        return p;
    }

    public void updateMetrics() {
        metricsRow.removeAll();

        int totalBookings = statsService.getTotalBookings();
        int customers = statsService.getTotalCustomers();
        int providers = statsService.getTotalProviders();
        int categories = statsService.getTotalCategories();
        double revenue = statsService.getTotalRevenue();

        metricsRow.add(new GradientCard("📊", "Total Bookings", String.valueOf(totalBookings), "All-time verified orders", UITheme.GRADIENT_BLUE_START, UITheme.GRADIENT_BLUE_END));
        metricsRow.add(new GradientCard("💰", "Gross Revenue", "₹" + String.format("%.0f", revenue), "Completed service volume", UITheme.GRADIENT_PURPLE_START, UITheme.GRADIENT_PURPLE_END));
        metricsRow.add(new GradientCard("👥", "Platform Users", String.valueOf(customers + providers), customers + " Customers · " + providers + " Providers", UITheme.GRADIENT_GOLD_START, UITheme.GRADIENT_GOLD_END));
        metricsRow.add(new GradientCard("🛠️", "Active Categories", String.valueOf(categories) + " Types", "Household services offered", UITheme.GRADIENT_TEAL_START, UITheme.GRADIENT_TEAL_END));

        metricsRow.revalidate();
        metricsRow.repaint();

        // Update Status cards
        if (statusCardsContainer != null) {
            statusCardsContainer.removeAll();
            Map<String, Integer> dist = statsService.getBookingStatusDistribution();

            statusCardsContainer.add(UITheme.createMetricCard("PENDING", String.valueOf(dist.getOrDefault("PENDING", 0)), "Awaiting provider response", UITheme.STATUS_PENDING));
            statusCardsContainer.add(UITheme.createMetricCard("ACCEPTED", String.valueOf(dist.getOrDefault("ACCEPTED", 0)), "Confirmed by provider", UITheme.STATUS_ACCEPTED));
            statusCardsContainer.add(UITheme.createMetricCard("IN PROGRESS", String.valueOf(dist.getOrDefault("IN_PROGRESS", 0)), "Service actively executing", UITheme.STATUS_IN_PROGRESS));
            statusCardsContainer.add(UITheme.createMetricCard("COMPLETED", String.valueOf(dist.getOrDefault("COMPLETED", 0)), "Finished & billed", UITheme.STATUS_COMPLETED));
            statusCardsContainer.add(UITheme.createMetricCard("CANCELLED", String.valueOf(dist.getOrDefault("CANCELLED", 0)), "Cancelled by user/admin", UITheme.STATUS_CANCELLED));
            statusCardsContainer.add(UITheme.createMetricCard("REJECTED", String.valueOf(dist.getOrDefault("REJECTED", 0)), "Declined requests", UITheme.STATUS_REJECTED));

            statusCardsContainer.revalidate();
            statusCardsContainer.repaint();
        }
    }

    public void refreshAll() {
        updateMetrics();
        usersPanel.loadUsers();
        servicesPanel.loadCategories();
        bookingsPanel.loadBookings();
        reviewsPanel.loadReviews();
    }
}

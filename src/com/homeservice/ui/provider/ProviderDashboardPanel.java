package com.homeservice.ui.provider;

import com.homeservice.dao.BookingDAO;
import com.homeservice.dao.ServiceProviderDAO;
import com.homeservice.model.Booking;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.common.GradientCard;
import com.homeservice.ui.common.MiniBarChart;
import com.homeservice.ui.common.RingProgressChart;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProviderDashboardPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();

    private JPanel statsPanel;
    private RingProgressChart completionRingChart;
    private MiniBarChart statusChart;
    private JTabbedPane tabbedPane;
    private ProviderRequestsPanel requestsPanel;
    private ProviderActiveJobsPanel activeJobsPanel;
    private ProviderReviewsPanel reviewsPanel;
    private ProviderProfilePanel profilePanel;

    public ProviderDashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // TOP SECTION: Metrics Cards + Visual Analytics
        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);

        // Top 5 Metric Cards
        statsPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        statsPanel.setOpaque(false);
        topWrapper.add(statsPanel, BorderLayout.NORTH);

        // Suggestion #7: Visual Analytics Banner Card (Donut Chart + Mini Bar Chart)
        JPanel analyticsCard = UITheme.createCardPanel();
        analyticsCard.setLayout(new BorderLayout(16, 0));
        analyticsCard.setPreferredSize(new Dimension(0, 165));

        JPanel leftTitleBox = new JPanel(new BorderLayout());
        leftTitleBox.setOpaque(false);
        leftTitleBox.setPreferredSize(new Dimension(200, 0));

        JLabel lblAnalyticsTitle = new JLabel("Performance & Analytics");
        lblAnalyticsTitle.setFont(UITheme.FONT_HEADER);
        lblAnalyticsTitle.setForeground(UITheme.getTextDark());

        JLabel lblAnalyticsSubtitle = new JLabel("<html>Real-time completion rate and order pipeline status metrics.</html>");
        lblAnalyticsSubtitle.setFont(UITheme.FONT_SMALL);
        lblAnalyticsSubtitle.setForeground(UITheme.getTextMuted());

        leftTitleBox.add(lblAnalyticsTitle, BorderLayout.NORTH);
        leftTitleBox.add(lblAnalyticsSubtitle, BorderLayout.CENTER);

        JPanel chartsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0));
        chartsRow.setOpaque(false);

        completionRingChart = new RingProgressChart("Completion Rate", 100, "Delivered on Schedule", UITheme.BRAND_GREEN, UITheme.STATUS_ACCEPTED);
        completionRingChart.setPreferredSize(new Dimension(170, 140));

        statusChart = new MiniBarChart("Orders Pipeline Distribution");
        statusChart.setPreferredSize(new Dimension(340, 140));

        chartsRow.add(completionRingChart);
        chartsRow.add(statusChart);

        analyticsCard.add(leftTitleBox, BorderLayout.WEST);
        analyticsCard.add(chartsRow, BorderLayout.CENTER);

        topWrapper.add(analyticsCard, BorderLayout.CENTER);
        add(topWrapper, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        Runnable refreshMetrics = this::updateMetrics;

        requestsPanel = new ProviderRequestsPanel(refreshMetrics);
        activeJobsPanel = new ProviderActiveJobsPanel(refreshMetrics);
        reviewsPanel = new ProviderReviewsPanel();
        profilePanel = new ProviderProfilePanel(refreshMetrics);

        tabbedPane.addTab("  📥 Booking Requests  ", requestsPanel);
        tabbedPane.addTab("  ⚡ Active Jobs & Schedule  ", activeJobsPanel);
        tabbedPane.addTab("  ⭐ Reviews & Ratings  ", reviewsPanel);
        tabbedPane.addTab("  ⚙️ Profile & Availability  ", profilePanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) requestsPanel.loadRequests();
            else if (idx == 1) activeJobsPanel.loadJobs();
            else if (idx == 2) reviewsPanel.loadReviews();
            else if (idx == 3) profilePanel.loadProfile();
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

    public void updateMetrics() {
        statsPanel.removeAll();
        User provider = AuthService.getCurrentUser();
        if (provider == null) return;

        List<Booking> bookings = bookingDAO.getBookingsByProvider(provider.getId());
        int pending = 0;
        int active = 0;
        int completed = 0;
        int cancelled = 0;
        double earnings = 0.0;

        for (Booking b : bookings) {
            String s = b.getStatus().toUpperCase();
            if ("PENDING".equals(s)) pending++;
            else if ("ACCEPTED".equals(s) || "IN_PROGRESS".equals(s)) active++;
            else if ("COMPLETED".equals(s)) {
                completed++;
                earnings += b.getTotalCost();
            } else if ("CANCELLED".equals(s) || "REJECTED".equals(s)) {
                cancelled++;
            }
        }

        ServiceProviderProfile profile = providerDAO.findByUserId(provider.getId());
        double rating = profile != null ? profile.getRatingAvg() : 5.0;
        int reviewCount = profile != null ? profile.getTotalReviews() : 0;

        statsPanel.add(new GradientCard("📥", "New Requests", String.valueOf(pending), "Awaiting response", UITheme.GRADIENT_PURPLE_START, UITheme.GRADIENT_PURPLE_END));
        statsPanel.add(new GradientCard("⚡", "Active Jobs", String.valueOf(active), "In progress", UITheme.GRADIENT_BLUE_START, UITheme.GRADIENT_BLUE_END));
        statsPanel.add(new GradientCard("✓", "Completed Jobs", String.valueOf(completed), "Delivered", UITheme.GRADIENT_TEAL_START, UITheme.GRADIENT_TEAL_END));
        statsPanel.add(new GradientCard("💰", "Total Earnings", "₹" + String.format("%.0f", earnings), "Finished orders", UITheme.GRADIENT_GOLD_START, UITheme.GRADIENT_GOLD_END));
        statsPanel.add(new GradientCard("⭐", "Avg Rating", String.format("%.1f", rating) + " ★", reviewCount + " reviews", UITheme.GRADIENT_BLUE_START, UITheme.GRADIENT_PURPLE_START));

        // Update charts
        int totalValid = completed + active + pending;
        int compPct = totalValid > 0 ? (int) Math.round(((double) completed / totalValid) * 100.0) : 100;
        completionRingChart.setPercentage(compPct);
        completionRingChart.setSubtitle(completed + " of " + (totalValid + cancelled) + " total jobs");

        statusChart.clear();
        statusChart.addBar("Requests", pending, UITheme.STATUS_PENDING);
        statusChart.addBar("Active", active, UITheme.STATUS_ACCEPTED);
        statusChart.addBar("Completed", completed, UITheme.STATUS_COMPLETED);
        statusChart.addBar("Rejected", cancelled, UITheme.STATUS_CANCELLED);

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    public void refreshAll() {
        updateMetrics();
        requestsPanel.loadRequests();
        activeJobsPanel.loadJobs();
        reviewsPanel.loadReviews();
        profilePanel.loadProfile();
    }
}

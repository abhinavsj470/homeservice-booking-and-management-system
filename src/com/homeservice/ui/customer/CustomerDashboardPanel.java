package com.homeservice.ui.customer;

import com.homeservice.dao.BookingDAO;
import com.homeservice.model.Booking;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.common.EmptyStatePanel;
import com.homeservice.ui.common.GradientCard;
import com.homeservice.ui.common.MiniBarChart;
import com.homeservice.ui.common.RingProgressChart;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CustomerDashboardPanel extends JPanel {
    private final JTabbedPane tabbedPane;
    private final BrowseServicesPanel browsePanel;
    private final MyBookingsPanel bookingsPanel;
    private final CustomerProfilePanel profilePanel;
    private final BookingDAO bookingDAO = new BookingDAO();

    // Overview widgets
    private JPanel overviewPanel;
    private JPanel gradientCardsRow;
    private JTable overviewTable;
    private DefaultTableModel overviewTableModel;
    private TableRowSorter<DefaultTableModel> overviewSorter;
    private RingProgressChart ringChart;
    private MiniBarChart barChart;
    private JPanel tableCard;
    private JPanel centerContentPanel;
    private CardLayout centerCardLayout;
    private EmptyStatePanel emptyPanel;

    public CustomerDashboardPanel(Frame parentFrame) {
        setLayout(new BorderLayout());
        setBackground(UITheme.getBgMain());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        overviewPanel = createOverviewPanel();
        browsePanel = new BrowseServicesPanel(parentFrame);
        bookingsPanel = new MyBookingsPanel(parentFrame);
        profilePanel = new CustomerProfilePanel(() -> {
            if (parentFrame instanceof com.homeservice.ui.MainFrame) {
                ((com.homeservice.ui.MainFrame) parentFrame).updateActiveUserView();
            }
        });

        tabbedPane.addTab("  📊 Dashboard  ", overviewPanel);
        tabbedPane.addTab("  🔍 Browse Services  ", browsePanel);
        tabbedPane.addTab("  📅 My Bookings  ", bookingsPanel);
        tabbedPane.addTab("  👤 Profile & Settings  ", profilePanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) {
                refreshOverview();
            } else if (idx == 1) {
                browsePanel.reloadCategories();
            } else if (idx == 2) {
                bookingsPanel.loadBookings();
            } else if (idx == 3) {
                profilePanel.loadUserData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setSelectedIndex(int idx) {
        if (idx >= 0 && idx < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(idx);
        }
    }

    private JPanel createOverviewPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBackground(UITheme.getBgMain());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top 3 Gradient Cards row
        gradientCardsRow = new JPanel(new GridLayout(1, 3, 16, 16));
        gradientCardsRow.setOpaque(false);
        p.add(gradientCardsRow, BorderLayout.NORTH);

        // Center Split: Left Side Table Card + Right Side Visual Analytics (Suggestion #7)
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setOpaque(false);

        // Table Card
        tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(12, 12));

        JPanel tableHeaderRow = new JPanel(new BorderLayout());
        tableHeaderRow.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Recent Bookings & Activity");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.getTextDark());

        JButton btnQuickBook = UITheme.createPrimaryButton("+ Book New Service");
        btnQuickBook.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        tableHeaderRow.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderRow.add(btnQuickBook, BorderLayout.EAST);
        tableCard.add(tableHeaderRow, BorderLayout.NORTH);

        String[] cols = {"Provider Name", "Service Category", "Scheduled Date", "Status"};
        overviewTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        overviewTable = new JTable(overviewTableModel);
        overviewSorter = new TableRowSorter<>(overviewTableModel);
        overviewTable.setRowSorter(overviewSorter);
        overviewTable.setRowHeight(38);
        overviewTable.setShowVerticalLines(false);
        overviewTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.getStatusBadgeRenderer());

        overviewTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        overviewTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        overviewTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        overviewTable.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane sp = new JScrollPane(overviewTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(sp, BorderLayout.CENTER);

        // Suggestion #7: Visual Analytics Card (Donut Chart + Bar Chart)
        JPanel analyticsCard = UITheme.createCardPanel();
        analyticsCard.setLayout(new BoxLayout(analyticsCard, BoxLayout.Y_AXIS));
        analyticsCard.setPreferredSize(new Dimension(360, 0));

        JLabel lblAnalyticsTitle = new JLabel("Service Fulfillment & Status");
        lblAnalyticsTitle.setFont(UITheme.FONT_HEADER);
        lblAnalyticsTitle.setForeground(UITheme.getTextDark());
        lblAnalyticsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        analyticsCard.add(lblAnalyticsTitle);
        analyticsCard.add(Box.createVerticalStrut(8));

        ringChart = new RingProgressChart("Fulfillment Rate", 100, "Completed on Schedule", UITheme.PRIMARY, UITheme.SECONDARY);
        ringChart.setAlignmentX(Component.CENTER_ALIGNMENT);
        analyticsCard.add(ringChart);

        barChart = new MiniBarChart("Bookings by Status");
        barChart.setAlignmentX(Component.CENTER_ALIGNMENT);
        analyticsCard.add(barChart);

        mainContent.add(tableCard, BorderLayout.CENTER);
        mainContent.add(analyticsCard, BorderLayout.EAST);

        // Empty state container switcher
        centerCardLayout = new CardLayout();
        centerContentPanel = new JPanel(centerCardLayout);
        centerContentPanel.setOpaque(false);

        JButton btnEmptyBook = UITheme.createPrimaryButton("+ Explore & Book Services");
        btnEmptyBook.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        emptyPanel = new EmptyStatePanel(
            "📊",
            "Welcome to HomeService!",
            "You haven't scheduled any service bookings yet. Explore our top verified professionals in Plumbing, Electrical, Cleaning, and more to see your live analytics here.",
            btnEmptyBook
        );

        centerContentPanel.add(mainContent, "CONTENT");
        centerContentPanel.add(emptyPanel, "EMPTY");

        p.add(centerContentPanel, BorderLayout.CENTER);
        return p;
    }

    public void refreshOverview() {
        gradientCardsRow.removeAll();
        overviewTableModel.setRowCount(0);

        User user = AuthService.getCurrentUser();
        if (user == null) return;

        List<Booking> bookings = bookingDAO.getBookingsByCustomer(user.getId());
        int total = bookings.size();
        int pending = 0;
        int active = 0;
        int completed = 0;
        int cancelled = 0;
        double totalSpent = 0.0;

        for (Booking b : bookings) {
            String s = b.getStatus().toUpperCase();
            if (s.equals("PENDING")) {
                pending++;
            } else if (s.equals("ACCEPTED") || s.equals("IN_PROGRESS")) {
                active++;
            } else if (s.equals("COMPLETED")) {
                completed++;
                totalSpent += b.getTotalCost();
            } else if (s.equals("CANCELLED") || s.equals("REJECTED")) {
                cancelled++;
            }

            overviewTableModel.addRow(new Object[]{
                b.getProviderName(),
                b.getCategoryName(),
                b.getServiceDate() != null ? b.getServiceDate().toString() : "",
                b.getStatus()
            });
        }

        // Add 3 gradient cards
        gradientCardsRow.add(new GradientCard("📊", "Total Bookings", String.valueOf(total), "All recorded requests", UITheme.GRADIENT_BLUE_START, UITheme.GRADIENT_BLUE_END));
        gradientCardsRow.add(new GradientCard("💰", "Total Spent", "₹" + String.format("%.0f", totalSpent), "Delivered services", UITheme.GRADIENT_PURPLE_START, UITheme.GRADIENT_PURPLE_END));
        gradientCardsRow.add(new GradientCard("⚡", "Active Requests", String.valueOf(active + pending), "In schedule / pending", UITheme.GRADIENT_GOLD_START, UITheme.GRADIENT_GOLD_END));

        // Update Suggestion #7 charts
        int fulfillPct = total > 0 ? (int) Math.round(((double) completed / (double) (total - cancelled > 0 ? total - cancelled : total)) * 100.0) : 100;
        ringChart.setPercentage(fulfillPct);
        ringChart.setSubtitle(completed + " of " + total + " completed");

        barChart.clear();
        barChart.addBar("Pending", pending, UITheme.STATUS_PENDING);
        barChart.addBar("Active", active, UITheme.STATUS_ACCEPTED);
        barChart.addBar("Completed", completed, UITheme.STATUS_COMPLETED);
        barChart.addBar("Cancelled", cancelled, UITheme.STATUS_CANCELLED);

        if (total == 0) {
            centerCardLayout.show(centerContentPanel, "EMPTY");
        } else {
            centerCardLayout.show(centerContentPanel, "CONTENT");
        }

        gradientCardsRow.revalidate();
        gradientCardsRow.repaint();
    }

    public void filterContent(String query) {
        if (query == null) return;
        String q = query.trim().toLowerCase();
        if (q.equals("search in active view...") || q.isEmpty()) {
            overviewSorter.setRowFilter(null);
            return;
        }
        overviewSorter.setRowFilter(RowFilter.regexFilter("(?i)" + q));
    }

    public void refreshAll() {
        refreshOverview();
        browsePanel.reloadCategories();
        bookingsPanel.loadBookings();
        profilePanel.loadUserData();
    }
}

package com.homeservice.ui.provider;

import com.homeservice.dao.BookingDAO;
import com.homeservice.model.Booking;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.service.BookingService;
import com.homeservice.ui.common.EmptyStatePanel;
import com.homeservice.ui.common.ToastNotification;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ProviderActiveJobsPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService = new BookingService();
    private final Runnable onUpdateCallback;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cmbFilter;
    private List<Booking> currentList = new ArrayList<>();
    private JPanel centerCardContainer;
    private CardLayout centerCardLayout;
    private JScrollPane tableScrollPane;
    private EmptyStatePanel emptyPanel;

    public ProviderActiveJobsPanel(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout(12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Provider Jobs & Schedule");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.getTextDark());

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(UITheme.FONT_BODY);
        lblFilter.setForeground(UITheme.getTextMuted());

        cmbFilter = new JComboBox<>(new String[]{"Active Jobs (Accepted & In Progress)", "Completed Jobs", "All Statuses"});
        cmbFilter.addActionListener(e -> loadJobs());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadJobs());

        left.add(title);
        left.add(Box.createHorizontalStrut(12));
        left.add(lblFilter);
        left.add(cmbFilter);
        left.add(btnRefresh);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnDetails = UITheme.createSecondaryButton("View Details");
        btnDetails.addActionListener(e -> viewDetails());

        JButton btnStart = UITheme.createSecondaryButton("▶ Start Work");
        btnStart.addActionListener(e -> startJob());

        JButton btnComplete = UITheme.createSuccessButton("✓ Mark as Completed");
        btnComplete.addActionListener(e -> completeJob());

        right.add(btnDetails);
        right.add(btnStart);
        right.add(btnComplete);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center Switcher (Table vs Empty State)
        centerCardLayout = new CardLayout();
        centerCardContainer = new JPanel(centerCardLayout);
        centerCardContainer.setOpaque(false);

        // Table
        String[] columns = {"ID", "Customer Name", "Phone", "Date", "Time Slot", "Address", "Problem", "Status", "Fee (₹)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(7).setCellRenderer(UITheme.getStatusBadgeRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDetails();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(180);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(85);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.getCardBorder(), 1));

        // Suggestion #4: Rich Empty State
        emptyPanel = new EmptyStatePanel(
            "⚡",
            "No Jobs Matching Filter",
            "You currently have no active or completed assignments under this filter. Check your incoming requests tab to accept new service bookings."
        );

        centerCardContainer.add(tableScrollPane, "TABLE");
        centerCardContainer.add(emptyPanel, "EMPTY");
        add(centerCardContainer, BorderLayout.CENTER);

        loadJobs();
    }

    public void loadJobs() {
        tableModel.setRowCount(0);
        User provider = AuthService.getCurrentUser();
        if (provider == null) return;

        List<Booking> all = bookingDAO.getBookingsByProvider(provider.getId());
        String filter = (String) cmbFilter.getSelectedItem();
        currentList.clear();

        for (Booking b : all) {
            String s = b.getStatus().toUpperCase();
            boolean match = false;
            if (filter.startsWith("Active")) {
                match = s.equals("ACCEPTED") || s.equals("IN_PROGRESS");
            } else if (filter.startsWith("Completed")) {
                match = s.equals("COMPLETED");
            } else {
                match = true;
            }

            if (match) {
                currentList.add(b);
                tableModel.addRow(new Object[]{
                    "#" + b.getId(),
                    b.getCustomerName(),
                    b.getCustomerPhone(),
                    b.getServiceDate(),
                    b.getTimeSlot(),
                    b.getAddress(),
                    b.getProblemDescription(),
                    b.getStatus(),
                    "₹" + String.format("%.2f", b.getTotalCost())
                });
            }
        }

        if (currentList.isEmpty()) {
            centerCardLayout.show(centerCardContainer, "EMPTY");
        } else {
            centerCardLayout.show(centerCardContainer, "TABLE");
        }
    }

    private Booking getSelectedJob() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            ToastNotification.showWarning(this, "Please select a job from the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentList.size()) {
            return null;
        }
        return currentList.get(modelRow);
    }

    private void viewDetails() {
        Booking b = getSelectedJob();
        if (b == null) return;

        String msg = "Job Reference: #" + b.getId() + "\n" +
                     "Customer: " + b.getCustomerName() + " (" + b.getCustomerPhone() + ")\n" +
                     "Service Category: " + b.getCategoryName() + "\n" +
                     "Service Location: " + b.getAddress() + "\n" +
                     "Scheduled Date: " + b.getServiceDate() + " [" + b.getTimeSlot() + "]\n" +
                     "Problem Description:\n" + b.getProblemDescription() + "\n\n" +
                     "Status: " + b.getStatus() + "\n" +
                     "Agreed Rate / Fee: ₹" + String.format("%.2f", b.getTotalCost());

        JTextArea ta = new JTextArea(msg, 9, 35);
        ta.setFont(UITheme.FONT_BODY);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Job Details #" + b.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void startJob() {
        Booking b = getSelectedJob();
        if (b == null) return;

        if (!"ACCEPTED".equalsIgnoreCase(b.getStatus())) {
            ToastNotification.showWarning(this, "Only 'ACCEPTED' jobs can be transitioned to 'IN_PROGRESS'. Current status is " + b.getStatus());
            return;
        }

        bookingService.startBooking(b.getId());
        ToastNotification.showInfo(this, "▶ Job #" + b.getId() + " is now IN PROGRESS.");
        loadJobs();
        if (onUpdateCallback != null) onUpdateCallback.run();
    }

    private void completeJob() {
        Booking b = getSelectedJob();
        if (b == null) return;

        if ("COMPLETED".equalsIgnoreCase(b.getStatus())) {
            ToastNotification.showInfo(this, "This job is already marked as completed.");
            return;
        }

        if (!"IN_PROGRESS".equalsIgnoreCase(b.getStatus()) && !"ACCEPTED".equalsIgnoreCase(b.getStatus())) {
            ToastNotification.showWarning(this, "Cannot complete a booking with status: " + b.getStatus());
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Mark booking #" + b.getId() + " as COMPLETED?\nThis confirms service delivery.", "Confirm Completion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            bookingService.completeBooking(b.getId());
            ToastNotification.showSuccess(this, "✓ Service #" + b.getId() + " COMPLETED! Payment earnings recorded.");
            loadJobs();
            if (onUpdateCallback != null) onUpdateCallback.run();
        }
    }
}

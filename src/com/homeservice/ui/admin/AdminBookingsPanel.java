package com.homeservice.ui.admin;

import com.homeservice.dao.BookingDAO;
import com.homeservice.model.Booking;
import com.homeservice.service.BookingService;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminBookingsPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService = new BookingService();
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbStatusFilter;
    private JTextField txtSearch;
    private List<Booking> currentList = new ArrayList<>();

    public AdminBookingsPanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout(12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("All System Bookings");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BODY);
        String[] statuses = {"All Statuses", "PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.addActionListener(e -> loadBookings());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY);
        txtSearch = new JTextField(12);
        txtSearch.addActionListener(e -> loadBookings());

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> loadBookings());

        left.add(title);
        left.add(Box.createHorizontalStrut(10));
        left.add(lblStatus);
        left.add(cmbStatusFilter);
        left.add(lblSearch);
        left.add(txtSearch);
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnDetails = UITheme.createSecondaryButton("View Details");
        btnDetails.addActionListener(e -> viewDetails());

        JButton btnForceCancel = UITheme.createDangerButton("Admin Cancel Booking");
        btnForceCancel.addActionListener(e -> forceCancelBooking());

        right.add(btnDetails);
        right.add(btnForceCancel);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Customer", "Provider", "Service", "Date", "Time Slot", "Fee (₹)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(7).setCellRenderer(UITheme.getStatusBadgeRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        add(sp, BorderLayout.CENTER);

        loadBookings();
    }

    public void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> all = bookingDAO.getAllBookings();
        String filter = (String) cmbStatusFilter.getSelectedItem();
        String query = txtSearch.getText().trim().toLowerCase();

        currentList.clear();
        for (Booking b : all) {
            boolean matchStatus = "All Statuses".equals(filter) || b.getStatus().equalsIgnoreCase(filter);
            boolean matchQuery = query.isEmpty() ||
                                 String.valueOf(b.getId()).contains(query) ||
                                 b.getCustomerName().toLowerCase().contains(query) ||
                                 b.getProviderName().toLowerCase().contains(query) ||
                                 b.getCategoryName().toLowerCase().contains(query);

            if (matchStatus && matchQuery) {
                currentList.add(b);
                tableModel.addRow(new Object[]{
                    "#" + b.getId(),
                    b.getCustomerName(),
                    b.getProviderName(),
                    b.getCategoryName(),
                    b.getServiceDate(),
                    b.getTimeSlot(),
                    "₹" + String.format("%.2f", b.getTotalCost()),
                    b.getStatus()
                });
            }
        }
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void viewDetails() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        String msg = "Booking ID: #" + b.getId() + "\n" +
                     "Customer: " + b.getCustomerName() + " (" + b.getCustomerEmail() + ", " + b.getCustomerPhone() + ")\n" +
                     "Provider: " + b.getProviderName() + " (" + b.getProviderPhone() + ")\n" +
                     "Category: " + b.getCategoryName() + "\n" +
                     "Service Date: " + b.getServiceDate() + " [" + b.getTimeSlot() + "]\n" +
                     "Address: " + b.getAddress() + "\n" +
                     "Problem Description:\n" + b.getProblemDescription() + "\n\n" +
                     "Status: " + b.getStatus() + "\n" +
                     "Total Fee: ₹" + String.format("%.2f", b.getTotalCost()) + "\n" +
                     "Created: " + b.getCreatedAt();

        JTextArea ta = new JTextArea(msg, 11, 38);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "System Booking Details #" + b.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void forceCancelBooking() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if ("COMPLETED".equalsIgnoreCase(b.getStatus()) || "CANCELLED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot cancel a booking that is already " + b.getStatus() + ".", "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Admin override: Cancel booking #" + b.getId() + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            bookingService.cancelBooking(b.getId());
            JOptionPane.showMessageDialog(this, "Booking cancelled by admin.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
            if (onDataChanged != null) onDataChanged.run();
        }
    }
}

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
import java.util.ArrayList;
import java.util.List;

public class ProviderRequestsPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService = new BookingService();
    private final Runnable onUpdateCallback;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Booking> pendingList = new ArrayList<>();
    private JPanel centerCardContainer;
    private CardLayout centerCardLayout;
    private JScrollPane tableScrollPane;
    private EmptyStatePanel emptyPanel;

    public ProviderRequestsPanel(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // Toolbar
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Incoming Booking Requests");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.getTextDark());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadRequests());

        JButton btnAccept = UITheme.createSuccessButton("✓ Accept Booking");
        btnAccept.addActionListener(e -> acceptBooking());

        JButton btnReject = UITheme.createDangerButton("✕ Reject Booking");
        btnReject.addActionListener(e -> rejectBooking());

        actions.add(btnRefresh);
        actions.add(btnReject);
        actions.add(btnAccept);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center Switcher (Table vs Empty State)
        centerCardLayout = new CardLayout();
        centerCardContainer = new JPanel(centerCardLayout);
        centerCardContainer.setOpaque(false);

        // Table
        String[] columns = {"ID", "Customer Name", "Phone", "Scheduled Date", "Time Slot", "Service Address", "Problem Description", "Earnings (₹)"};
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

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(220);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.getCardBorder(), 1));

        // Suggestion #4: Rich Empty State
        JButton btnEmptyRefresh = UITheme.createPrimaryButton("Check Again");
        btnEmptyRefresh.addActionListener(e -> loadRequests());
        emptyPanel = new EmptyStatePanel(
            "🎉",
            "All Caught Up! No Pending Requests",
            "You have no incoming pending booking requests at this moment. When customers request your service, notifications and details will instantly show up here.",
            btnEmptyRefresh
        );

        centerCardContainer.add(tableScrollPane, "TABLE");
        centerCardContainer.add(emptyPanel, "EMPTY");
        add(centerCardContainer, BorderLayout.CENTER);

        loadRequests();
    }

    public void loadRequests() {
        tableModel.setRowCount(0);
        User provider = AuthService.getCurrentUser();
        if (provider == null) return;

        List<Booking> all = bookingDAO.getBookingsByProvider(provider.getId());
        pendingList.clear();

        for (Booking b : all) {
            if ("PENDING".equalsIgnoreCase(b.getStatus())) {
                pendingList.add(b);
                tableModel.addRow(new Object[]{
                    "#" + b.getId(),
                    b.getCustomerName(),
                    b.getCustomerPhone(),
                    b.getServiceDate(),
                    b.getTimeSlot(),
                    b.getAddress(),
                    b.getProblemDescription(),
                    "₹" + String.format("%.2f", b.getTotalCost())
                });
            }
        }

        if (pendingList.isEmpty()) {
            centerCardLayout.show(centerCardContainer, "EMPTY");
        } else {
            centerCardLayout.show(centerCardContainer, "TABLE");
        }
    }

    private Booking getSelectedRequest() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            ToastNotification.showWarning(this, "Please select an incoming booking request from the table.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= pendingList.size()) {
            return null;
        }
        return pendingList.get(modelRow);
    }

    private void acceptBooking() {
        Booking b = getSelectedRequest();
        if (b == null) return;

        int choice = JOptionPane.showConfirmDialog(this, "Accept booking #" + b.getId() + " for " + b.getCustomerName() + "?", "Confirm Acceptance", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            bookingService.acceptBooking(b.getId());
            // Suggestion #6: Toast Notification
            ToastNotification.showSuccess(this, "✓ Booking #" + b.getId() + " accepted! Moved to Active Jobs.");
            loadRequests();
            if (onUpdateCallback != null) onUpdateCallback.run();
        }
    }

    private void rejectBooking() {
        Booking b = getSelectedRequest();
        if (b == null) return;

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to decline / reject booking #" + b.getId() + "?", "Confirm Rejection", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            bookingService.rejectBooking(b.getId());
            // Suggestion #6: Toast Notification
            ToastNotification.showInfo(this, "ℹ Booking request #" + b.getId() + " has been rejected.");
            loadRequests();
            if (onUpdateCallback != null) onUpdateCallback.run();
        }
    }
}

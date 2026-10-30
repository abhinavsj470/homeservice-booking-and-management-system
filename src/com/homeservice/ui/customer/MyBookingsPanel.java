package com.homeservice.ui.customer;

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

public class MyBookingsPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService = new BookingService();
    private final Frame parentFrame;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> cmbStatusFilter;
    private List<Booking> currentList = new ArrayList<>();
    private JPanel centerCardContainer;
    private CardLayout centerCardLayout;
    private JScrollPane tableScrollPane;
    private EmptyStatePanel emptyPanel;

    public MyBookingsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // Controls Toolbar
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new BorderLayout(12, 12));

        JPanel leftFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFlow.setOpaque(false);

        JLabel lblTitle = new JLabel("My Service Bookings & History");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.getTextDark());

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(UITheme.FONT_BODY);
        lblFilter.setForeground(UITheme.getTextMuted());

        String[] statuses = {"All Statuses", "PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.setFont(UITheme.FONT_BODY);
        cmbStatusFilter.addActionListener(e -> loadBookings());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadBookings());

        leftFlow.add(lblTitle);
        leftFlow.add(Box.createHorizontalStrut(12));
        leftFlow.add(lblFilter);
        leftFlow.add(cmbStatusFilter);
        leftFlow.add(btnRefresh);

        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightFlow.setOpaque(false);

        JButton btnViewDetails = UITheme.createSecondaryButton("View Details");
        btnViewDetails.addActionListener(e -> viewSelectedBookingDetails());

        JButton btnCancel = UITheme.createDangerButton("Cancel Booking");
        btnCancel.addActionListener(e -> cancelSelectedBooking());

        JButton btnReview = UITheme.createSuccessButton("★ Rate & Review");
        btnReview.addActionListener(e -> reviewSelectedBooking());

        rightFlow.add(btnViewDetails);
        rightFlow.add(btnCancel);
        rightFlow.add(btnReview);

        topPanel.add(leftFlow, BorderLayout.WEST);
        topPanel.add(rightFlow, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Switcher (Table vs Empty State)
        centerCardLayout = new CardLayout();
        centerCardContainer = new JPanel(centerCardLayout);
        centerCardContainer.setOpaque(false);

        // Table
        String[] columns = {"ID", "Service Category", "Service Provider", "Contact", "Date", "Time Slot", "Total (₹)", "Status", "Review"};
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

        // Suggestion #3: Double click on row opens details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedBookingDetails();
                }
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.getCardBorder(), 1));

        // Suggestion #4: Rich Empty State
        emptyPanel = new EmptyStatePanel(
            "📅",
            "No Bookings Found",
            "You don't have any bookings matching this filter. Browse our service categories to schedule your first maintenance or repair!"
        );

        centerCardContainer.add(tableScrollPane, "TABLE");
        centerCardContainer.add(emptyPanel, "EMPTY");
        add(centerCardContainer, BorderLayout.CENTER);

        loadBookings();
    }

    public void loadBookings() {
        tableModel.setRowCount(0);
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        List<Booking> all = bookingDAO.getBookingsByCustomer(user.getId());
        String filter = (String) cmbStatusFilter.getSelectedItem();

        currentList.clear();
        for (Booking b : all) {
            if ("All Statuses".equals(filter) || b.getStatus().equalsIgnoreCase(filter)) {
                currentList.add(b);
                tableModel.addRow(new Object[]{
                    "#" + b.getId(),
                    b.getCategoryName(),
                    b.getProviderName(),
                    b.getProviderPhone(),
                    b.getServiceDate(),
                    b.getTimeSlot(),
                    "₹" + String.format("%.2f", b.getTotalCost()),
                    b.getStatus(),
                    b.isHasReview() ? "Reviewed" : ("COMPLETED".equals(b.getStatus()) ? "Pending Review" : "—")
                });
            }
        }

        if (currentList.isEmpty()) {
            centerCardLayout.show(centerCardContainer, "EMPTY");
        } else {
            centerCardLayout.show(centerCardContainer, "TABLE");
        }
    }

    private Booking getSelectedBooking() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            ToastNotification.showWarning(this, "Please select a booking from the table first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentList.size()) {
            return null;
        }
        return currentList.get(modelRow);
    }

    private void viewSelectedBookingDetails() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        String details = "Booking Reference: #" + b.getId() + "\n" +
                         "Service: " + b.getCategoryName() + "\n" +
                         "Provider: " + b.getProviderName() + " (Phone: " + b.getProviderPhone() + ")\n" +
                         "Scheduled Date: " + b.getServiceDate() + " (" + b.getTimeSlot() + ")\n" +
                         "Service Location: " + b.getAddress() + "\n" +
                         "Reported Issue: " + b.getProblemDescription() + "\n" +
                         "Current Status: " + b.getStatus() + "\n" +
                         "Total Amount: ₹" + String.format("%.2f", b.getTotalCost());

        JTextArea ta = new JTextArea(details, 9, 36);
        ta.setFont(UITheme.FONT_BODY);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Booking Details #" + b.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelectedBooking() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if (!"PENDING".equalsIgnoreCase(b.getStatus())) {
            ToastNotification.showWarning(this, "Only 'PENDING' bookings can be cancelled. Current status is " + b.getStatus() + ".");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel booking #" + b.getId() + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            bookingService.cancelBooking(b.getId());
            loadBookings();
            ToastNotification.showWarning(parentFrame, "⚠ Booking #" + b.getId() + " has been cancelled.");
        }
    }

    private void reviewSelectedBooking() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if (!"COMPLETED".equalsIgnoreCase(b.getStatus())) {
            ToastNotification.showWarning(this, "You can only rate services that have been marked as 'COMPLETED'.");
            return;
        }

        if (b.isHasReview()) {
            ToastNotification.showInfo(this, "You have already submitted a review for this booking.");
            return;
        }

        AddReviewDialog dlg = new AddReviewDialog(parentFrame, b);
        dlg.setVisible(true);
        if (dlg.isReviewSubmitted()) {
            loadBookings();
        }
    }
}

package com.homeservice.ui.customer;

import com.homeservice.model.ServiceCategory;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.service.BookingService;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;

public class BookServiceDialog extends JDialog {
    private final ServiceProviderProfile provider;
    private final ServiceCategory category;
    private final BookingService bookingService = new BookingService();
    private boolean bookingSuccessful = false;

    private JTextField txtDate;
    private JComboBox<String> cmbTimeSlot;
    private JTextField txtAddress;
    private JTextArea txtProblem;

    public BookServiceDialog(Frame parent, ServiceProviderProfile provider, ServiceCategory category) {
        super(parent, "Schedule Service - " + category.getName(), true);
        this.provider = provider;
        this.category = category;
        setSize(520, 640);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Schedule " + category.getName() + " Service");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Service Provider: " + provider.getProviderName() + " | Rate: ₹" + provider.getHourlyRate() + "/hr");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(224, 231, 255));
        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        // Provider Summary Card
        JPanel cardProv = new JPanel(new GridLayout(4, 1, 4, 4));
        cardProv.setBackground(new Color(248, 250, 252));
        cardProv.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        cardProv.add(new JLabel("Expert: " + provider.getProviderName() + " (" + provider.getExperienceYears() + " yrs exp)"));
        cardProv.add(new JLabel("Contact Email: " + (provider.getProviderEmail() != null ? provider.getProviderEmail() : "") + " | Phone: " + (provider.getProviderPhone() != null ? provider.getProviderPhone() : "")));
        cardProv.add(new JLabel("Rating: ⭐ " + String.format("%.1f", provider.getRatingAvg()) + " (" + provider.getTotalReviews() + " verified reviews)"));
        cardProv.add(new JLabel("Estimated Cost: ₹" + provider.getHourlyRate() + " base inspection & 1st hour"));
        form.add(cardProv);
        form.add(Box.createVerticalStrut(14));

        // Date selection
        form.add(createFieldLabel("Preferred Service Date (YYYY-MM-DD) *"));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        txtDate = new JTextField(tomorrow.toString());
        txtDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtDate);
        form.add(Box.createVerticalStrut(10));

        // Time slot selection
        form.add(createFieldLabel("Preferred Time Slot *"));
        String[] slots = {
            "09:00 AM - 11:00 AM",
            "11:00 AM - 01:00 PM",
            "02:00 PM - 04:00 PM",
            "04:00 PM - 06:00 PM",
            "06:00 PM - 08:00 PM"
        };
        cmbTimeSlot = new JComboBox<>(slots);
        cmbTimeSlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(cmbTimeSlot);
        form.add(Box.createVerticalStrut(10));

        // Address
        form.add(createFieldLabel("Service Address *"));
        User currentUser = AuthService.getCurrentUser();
        String initialAddr = currentUser != null && currentUser.getAddress() != null ? currentUser.getAddress() : "";
        txtAddress = new JTextField(initialAddr);
        txtAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtAddress);
        form.add(Box.createVerticalStrut(10));

        // Problem Description
        form.add(createFieldLabel("Problem Details / Specific Requirements *"));
        txtProblem = new JTextArea(4, 20);
        txtProblem.setLineWrap(true);
        txtProblem.setWrapStyleWord(true);
        JScrollPane spProb = new JScrollPane(txtProblem);
        spProb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        form.add(spProb);
        form.add(Box.createVerticalStrut(10));

        root.add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(4, 16, 12, 16));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = UITheme.createPrimaryButton("Confirm & Schedule Booking");
        btnConfirm.addActionListener(e -> submitBooking());

        footer.add(btnCancel);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void submitBooking() {
        String dateStr = txtDate.getText().trim();
        String timeSlot = (String) cmbTimeSlot.getSelectedItem();
        String addr = txtAddress.getText().trim();
        String problem = txtProblem.getText().trim();

        if (dateStr.isEmpty() || addr.isEmpty() || problem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all details including service date, address, and problem description.", "Required Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date sqlDate;
        try {
            sqlDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = AuthService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Not Logged In", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalCost = provider.getHourlyRate();
        int bookingId = bookingService.createBooking(user.getId(), provider.getUserId(), category.getId(), sqlDate, timeSlot, addr, problem, totalCost);
        if (bookingId > 0) {
            bookingSuccessful = true;
            JOptionPane.showMessageDialog(this, "Service request booked successfully! Booking Reference: #" + bookingId + "\nThe provider has been notified.", "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create booking. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isBookingSuccessful() {
        return bookingSuccessful;
    }
}

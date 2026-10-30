package com.homeservice.ui.admin;

import com.homeservice.dao.ReviewDAO;
import com.homeservice.model.Review;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminReviewsPanel extends JPanel {
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Review> currentList = new ArrayList<>();

    public AdminReviewsPanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Customer Reviews & Moderation");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadReviews());

        JButton btnDelete = UITheme.createDangerButton("Delete / Moderate Review");
        btnDelete.addActionListener(e -> deleteSelectedReview());

        actions.add(btnRefresh);
        actions.add(btnDelete);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Booking ID", "Customer Name", "Provider Name", "Rating", "Customer Review Comments", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(350);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        add(sp, BorderLayout.CENTER);

        loadReviews();
    }

    public void loadReviews() {
        tableModel.setRowCount(0);
        currentList = reviewDAO.getAllReviews();
        for (Review r : currentList) {
            String stars = "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
            tableModel.addRow(new Object[]{
                "#" + r.getId(),
                "#" + r.getBookingId(),
                r.getCustomerName(),
                r.getProviderName(),
                stars + " (" + r.getRating() + "/5)",
                r.getComment(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString().substring(0, 10) : ""
            });
        }
    }

    private void deleteSelectedReview() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a review from the table.", "Select Review", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Review r = currentList.get(row);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete review #" + r.getId() + " by " + r.getCustomerName() + "?\nThis will recompute the provider's average rating.", "Confirm Review Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            reviewDAO.deleteReview(r.getId());
            JOptionPane.showMessageDialog(this, "Review deleted and provider rating re-calculated.", "Review Moderated", JOptionPane.INFORMATION_MESSAGE);
            loadReviews();
            if (onDataChanged != null) onDataChanged.run();
        }
    }
}

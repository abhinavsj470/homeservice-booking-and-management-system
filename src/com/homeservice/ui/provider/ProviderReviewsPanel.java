package com.homeservice.ui.provider;

import com.homeservice.dao.ReviewDAO;
import com.homeservice.dao.ServiceProviderDAO;
import com.homeservice.model.Review;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.common.EmptyStatePanel;
import com.homeservice.ui.common.RatingBreakdownPanel;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ProviderReviewsPanel extends JPanel {
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private RatingBreakdownPanel breakdownPanel;
    private JPanel centerCardContainer;
    private CardLayout centerCardLayout;
    private JScrollPane tableScrollPane;
    private EmptyStatePanel emptyPanel;

    public ProviderReviewsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // Top Toolbar + Breakdown Card
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JPanel toolbar = UITheme.createCardPanel();
        toolbar.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Client Feedback & Reputation Breakdown");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.getTextDark());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Feedback");
        btnRefresh.addActionListener(e -> loadReviews());

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRefresh, BorderLayout.EAST);
        topContainer.add(toolbar, BorderLayout.NORTH);

        // Suggestion #5: Rating Breakdown Bars Card
        breakdownPanel = new RatingBreakdownPanel();
        topContainer.add(breakdownPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // Center Area (Switches between Table and Empty State)
        centerCardLayout = new CardLayout();
        centerCardContainer = new JPanel(centerCardLayout);
        centerCardContainer.setOpaque(false);

        // Table
        String[] columns = {"ID", "Customer Name", "Rating", "Review Comments", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(450);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.getCardBorder(), 1));

        // Suggestion #4: Rich Empty State
        emptyPanel = new EmptyStatePanel(
            "⭐",
            "No Customer Reviews Yet",
            "When clients complete service bookings with you, their feedback and ratings will be automatically analyzed and displayed here."
        );

        centerCardContainer.add(tableScrollPane, "TABLE");
        centerCardContainer.add(emptyPanel, "EMPTY");
        add(centerCardContainer, BorderLayout.CENTER);

        loadReviews();
    }

    public void loadReviews() {
        tableModel.setRowCount(0);
        User provider = AuthService.getCurrentUser();
        if (provider == null) return;

        List<Review> list = reviewDAO.getReviewsByProvider(provider.getId());

        int[] starCounts = new int[5]; // index 0 = 1 star, 4 = 5 stars
        double totalRating = 0.0;

        for (Review r : list) {
            int rating = Math.max(1, Math.min(5, r.getRating()));
            starCounts[rating - 1]++;
            totalRating += rating;

            String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
            tableModel.addRow(new Object[]{
                "#" + r.getId(),
                r.getCustomerName(),
                stars + " (" + rating + "/5)",
                r.getComment(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString().substring(0, 10) : ""
            });
        }

        double avg = list.isEmpty() ? 5.0 : (totalRating / list.size());
        breakdownPanel.updateData(avg, list.size(), starCounts);

        if (list.isEmpty()) {
            centerCardLayout.show(centerCardContainer, "EMPTY");
        } else {
            centerCardLayout.show(centerCardContainer, "TABLE");
        }
    }
}

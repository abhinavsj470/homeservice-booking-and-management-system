package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * Modern Amazon/App Store style Rating Breakdown card with progress bars for 5 to 1 star ratings.
 */
public class RatingBreakdownPanel extends JPanel {
    private final JLabel lblAvgScore;
    private final JLabel lblStars;
    private final JLabel lblTotalCount;
    private final JPanel barsContainer;

    public RatingBreakdownPanel() {
        setLayout(new BorderLayout(24, 0));
        setBackground(UITheme.getCardBg());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.getCardBorder(), 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        // Left Side: Overall Score & Stars
        JPanel leftSummary = new JPanel();
        leftSummary.setLayout(new BoxLayout(leftSummary, BoxLayout.Y_AXIS));
        leftSummary.setOpaque(false);
        leftSummary.setPreferredSize(new Dimension(140, 100));

        lblAvgScore = new JLabel("5.0");
        lblAvgScore.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblAvgScore.setForeground(UITheme.getTextDark());
        lblAvgScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStars = new JLabel("★★★★★");
        lblStars.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        lblStars.setForeground(new Color(245, 158, 11));
        lblStars.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTotalCount = new JLabel("Based on 0 reviews");
        lblTotalCount.setFont(UITheme.FONT_SMALL);
        lblTotalCount.setForeground(UITheme.getTextMuted());
        lblTotalCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftSummary.add(Box.createVerticalGlue());
        leftSummary.add(lblAvgScore);
        leftSummary.add(Box.createVerticalStrut(2));
        leftSummary.add(lblStars);
        leftSummary.add(Box.createVerticalStrut(4));
        leftSummary.add(lblTotalCount);
        leftSummary.add(Box.createVerticalGlue());

        add(leftSummary, BorderLayout.WEST);

        // Right Side: 5 Breakdown Bars
        barsContainer = new JPanel(new GridLayout(5, 1, 0, 4));
        barsContainer.setOpaque(false);
        add(barsContainer, BorderLayout.CENTER);

        updateData(5.0, 0, new int[]{0, 0, 0, 0, 0});
    }

    public void updateData(double avgRating, int totalReviews, int[] starCounts) {
        lblAvgScore.setText(String.format("%.1f", avgRating));
        lblAvgScore.setForeground(UITheme.getTextDark());

        int fullStars = (int) Math.round(avgRating);
        lblStars.setText("★".repeat(Math.max(0, Math.min(5, fullStars))) + "☆".repeat(Math.max(0, 5 - fullStars)));
        lblTotalCount.setText("Based on " + totalReviews + " reviews");
        lblTotalCount.setForeground(UITheme.getTextMuted());

        setBackground(UITheme.getCardBg());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.getCardBorder(), 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        barsContainer.removeAll();

        // 5 stars down to 1 star
        for (int i = 5; i >= 1; i--) {
            int count = (starCounts != null && starCounts.length >= i) ? starCounts[i - 1] : 0;
            double pct = totalReviews > 0 ? ((double) count / totalReviews) * 100.0 : 0.0;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);

            JLabel lblStarNum = new JLabel(i + " ★");
            lblStarNum.setFont(UITheme.FONT_BOLD);
            lblStarNum.setForeground(UITheme.getTextDark());
            lblStarNum.setPreferredSize(new Dimension(30, 18));

            JProgressBar pb = new JProgressBar(0, 100);
            pb.setValue((int) pct);
            pb.setPreferredSize(new Dimension(180, 10));
            pb.setStringPainted(false);
            pb.setBorderPainted(false);
            pb.setBackground(new Color(241, 245, 249));

            // Color bar dynamically
            if (i >= 4) {
                pb.setForeground(new Color(16, 185, 129)); // Emerald
            } else if (i == 3) {
                pb.setForeground(new Color(245, 158, 11)); // Amber
            } else {
                pb.setForeground(new Color(239, 68, 68));  // Red
            }

            JLabel lblInfo = new JLabel(String.format("%.0f%% (%d)", pct, count));
            lblInfo.setFont(UITheme.FONT_SMALL);
            lblInfo.setForeground(UITheme.getTextMuted());
            lblInfo.setPreferredSize(new Dimension(65, 18));

            row.add(lblStarNum, BorderLayout.WEST);
            row.add(pb, BorderLayout.CENTER);
            row.add(lblInfo, BorderLayout.EAST);

            barsContainer.add(row);
        }

        barsContainer.revalidate();
        barsContainer.repaint();
        revalidate();
        repaint();
    }
}

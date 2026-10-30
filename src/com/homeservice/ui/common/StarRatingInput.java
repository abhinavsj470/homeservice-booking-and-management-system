package com.homeservice.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive 5-star rating input with smooth rollover and dynamic feedback label.
 */
public class StarRatingInput extends JPanel {
    private int currentRating = 5;
    private int hoveredRating = -1;
    private final JLabel[] starLabels = new JLabel[5];
    private final JLabel lblFeedback;
    private final List<Runnable> changeListeners = new ArrayList<>();

    private static final Color GOLD = new Color(245, 158, 11);
    private static final Color GOLD_HOVER = new Color(251, 191, 36);
    private static final Color GRAY_UNLIT_LIGHT = new Color(203, 213, 225);
    private static final Color GRAY_UNLIT_DARK = new Color(71, 85, 105);

    public StarRatingInput() {
        this(5);
    }

    public StarRatingInput(int initialRating) {
        this.currentRating = Math.max(1, Math.min(5, initialRating));
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false);

        // 5 Star Buttons
        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            JLabel star = new JLabel("★");
            star.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 28));
            star.setCursor(new Cursor(Cursor.HAND_CURSOR));

            star.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hoveredRating = starIndex;
                    updateStarVisuals();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredRating = -1;
                    updateStarVisuals();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    currentRating = starIndex;
                    updateStarVisuals();
                    for (Runnable l : changeListeners) {
                        l.run();
                    }
                }
            });

            starLabels[i] = star;
            add(star);
        }

        lblFeedback = new JLabel();
        lblFeedback.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFeedback.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        add(lblFeedback);

        updateStarVisuals();
    }

    private void updateStarVisuals() {
        int displayRating = (hoveredRating > 0) ? hoveredRating : currentRating;
        Color unlit = GRAY_UNLIT_LIGHT;
        Color activeGold = (hoveredRating > 0) ? GOLD_HOVER : GOLD;

        for (int i = 0; i < 5; i++) {
            if (i < displayRating) {
                starLabels[i].setForeground(activeGold);
            } else {
                starLabels[i].setForeground(unlit);
            }
        }

        String feedbackText;
        switch (displayRating) {
            case 1: feedbackText = "1.0 - Poor Experience"; break;
            case 2: feedbackText = "2.0 - Below Expectations"; break;
            case 3: feedbackText = "3.0 - Average Service"; break;
            case 4: feedbackText = "4.0 - Very Good Experience"; break;
            case 5: feedbackText = "5.0 - Exceptional Service!"; break;
            default: feedbackText = ""; break;
        }
        lblFeedback.setText(feedbackText);
        lblFeedback.setForeground(UITheme.getTextDark());
    }

    public int getRating() {
        return currentRating;
    }

    public void setRating(int rating) {
        this.currentRating = Math.max(1, Math.min(5, rating));
        updateStarVisuals();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }
}

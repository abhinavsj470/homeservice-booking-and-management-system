package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable illustrated empty state panel when a list, table, or search has 0 items.
 */
public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String emojiIcon, String title, String description) {
        this(emojiIcon, title, description, null);
    }

    public EmptyStatePanel(String emojiIcon, String title, String description, JButton actionButton) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(40, 24, 40, 24));

        // Emoji / Icon Circle
        JLabel lblIcon = new JLabel(emojiIcon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(226, 232, 240));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcon.setPreferredSize(new Dimension(84, 84));
        lblIcon.setMaximumSize(new Dimension(84, 84));

        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(UITheme.getTextDark());
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        JLabel lblDesc = new JLabel("<html><div style='text-align: center; max-width: 380px;'>" + description + "</div></html>");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.getTextMuted());
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(lblIcon);
        add(Box.createVerticalStrut(16));
        add(lblTitle);
        add(Box.createVerticalStrut(6));
        add(lblDesc);

        if (actionButton != null) {
            actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(18));
            add(actionButton);
        }
    }
}

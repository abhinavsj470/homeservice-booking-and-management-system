package com.homeservice.ui.common;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class UITheme {
    public static final Color PRIMARY = new Color(37, 99, 235);       // Royal Blue #2563EB
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);  // #1D4ED8
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255); // #EFF6FF
    public static final Color SECONDARY = new Color(79, 70, 229);    // Indigo #4F46E5

    // Forest / Spruce Green Theme (Matching Modern Login UI)
    public static final Color BRAND_GREEN = new Color(40, 90, 72);          // #285A48
    public static final Color BRAND_GREEN_DARK = new Color(28, 68, 54);     // #1C4436
    public static final Color BRAND_GREEN_LIGHT = new Color(240, 248, 244); // #F0F8F4
    public static final Color BRAND_GREEN_BORDER = new Color(64, 120, 100); // #407864

    public static final Color BG_MAIN = new Color(248, 250, 252);     // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = new Color(226, 232, 240); // Slate 200

    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500

    // Status Colors
    public static final Color STATUS_PENDING = new Color(245, 158, 11);    // Amber
    public static final Color STATUS_ACCEPTED = new Color(14, 165, 233);   // Sky Blue
    public static final Color STATUS_IN_PROGRESS = new Color(139, 92, 246); // Purple
    public static final Color STATUS_COMPLETED = new Color(16, 185, 129);  // Emerald
    public static final Color STATUS_CANCELLED = new Color(239, 68, 68);   // Red
    public static final Color STATUS_REJECTED = new Color(156, 163, 175);  // Gray

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static Color getBgMain() {
        return BG_MAIN;
    }

    public static Color getCardBg() {
        return CARD_BG;
    }

    public static Color getCardBorder() {
        return CARD_BORDER;
    }

    public static Color getTextDark() {
        return TEXT_DARK;
    }

    public static Color getTextMuted() {
        return TEXT_MUTED;
    }

    public static boolean isDarkMode() {
        return false;
    }

    public static void setupLaf() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", PRIMARY_DARK);
            UIManager.put("Table.rowHeight", 38);
            UIManager.put("TableHeader.font", FONT_BOLD);
            UIManager.put("TableHeader.height", 38);
        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf: " + e.getMessage());
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(new Color(254, 226, 226));
        btn.setForeground(new Color(185, 28, 28));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(new Color(220, 252, 231));
        btn.setForeground(new Color(21, 128, 61));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createGreenButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(BRAND_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JPanel createMetricCard(String title, String value, String subtitle, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
            )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    public static final Color GRADIENT_BLUE_START = new Color(99, 102, 241);   // Indigo #6366F1
    public static final Color GRADIENT_BLUE_END = new Color(139, 92, 246);    // Violet #8B5CF6

    public static final Color GRADIENT_PURPLE_START = new Color(184, 41, 247); // Magenta-Purple
    public static final Color GRADIENT_PURPLE_END = new Color(117, 121, 255);  // Soft Purple

    public static final Color GRADIENT_GOLD_START = new Color(245, 158, 11);   // Amber
    public static final Color GRADIENT_GOLD_END = new Color(251, 191, 36);    // Warm Gold

    public static final Color GRADIENT_TEAL_START = new Color(16, 185, 129);   // Emerald
    public static final Color GRADIENT_TEAL_END = new Color(6, 182, 212);     // Cyan

    public static DefaultTableCellRenderer getStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            private String currentText = "";
            private Color pillBg = new Color(139, 92, 246);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                currentText = value != null ? value.toString() : "";
                setText(""); // Draw pill text in paintComponent
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(false);

                switch (currentText.toUpperCase()) {
                    case "PENDING":
                        pillBg = new Color(168, 85, 247); // Vibrant Purple like reference screenshot
                        break;
                    case "ACCEPTED":
                    case "APPROVED":
                        pillBg = new Color(59, 130, 246);  // Electric Blue like reference screenshot
                        break;
                    case "IN_PROGRESS":
                        pillBg = new Color(14, 165, 233);  // Sky Blue
                        break;
                    case "COMPLETED":
                        pillBg = new Color(16, 185, 129);  // Emerald Green
                        break;
                    case "CANCELLED":
                    case "REJECTED":
                    case "REJECT":
                        pillBg = new Color(234, 179, 8);   // Yellow/Gold like reference screenshot
                        break;
                    case "ACTIVE":
                        pillBg = new Color(16, 185, 129);  // Green
                        break;
                    case "INACTIVE":
                        pillBg = new Color(148, 163, 184); // Gray
                        break;
                    default:
                        pillBg = new Color(100, 116, 139);
                        break;
                }
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int pillWidth = Math.min(w - 12, 94);
                int pillHeight = 24;
                int x = (w - pillWidth) / 2;
                int y = (h - pillHeight) / 2;

                // Draw rounded pill
                g2.setColor(pillBg);
                g2.fillRoundRect(x, y, pillWidth, pillHeight, 12, 12);

                // Draw pill text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (pillWidth - fm.stringWidth(currentText)) / 2;
                int textY = y + ((pillHeight - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(currentText, textX, textY);

                g2.dispose();
            }
        };
    }
}

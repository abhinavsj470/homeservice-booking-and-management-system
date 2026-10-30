package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern floating glassmorphic toast notification banner.
 * Displays non-blocking animated notifications (Success, Error, Info, Warning).
 */
public class ToastNotification extends JWindow {
    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    private float opacity = 0f;
    private Timer fadeInTimer;
    private Timer stayTimer;
    private Timer fadeOutTimer;

    public static void showSuccess(Component parent, String message) {
        show(parent, message, Type.SUCCESS);
    }

    public static void showError(Component parent, String message) {
        show(parent, message, Type.ERROR);
    }

    public static void showInfo(Component parent, String message) {
        show(parent, message, Type.INFO);
    }

    public static void showWarning(Component parent, String message) {
        show(parent, message, Type.WARNING);
    }

    public static void show(Component parent, String message, Type type) {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
            ToastNotification toast = new ToastNotification(parentWindow, message, type);
            toast.display();
        });
    }

    private ToastNotification(Window parent, String message, Type type) {
        super(parent);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        Color bgColor;
        Color fgColor = Color.WHITE;
        Color borderColor;
        String iconText;

        switch (type) {
            case SUCCESS:
                bgColor = new Color(16, 185, 129, 240); // Emerald
                borderColor = new Color(5, 150, 105);
                iconText = "✓";
                break;
            case ERROR:
                bgColor = new Color(239, 68, 68, 240);  // Red
                borderColor = new Color(220, 38, 38);
                iconText = "✕";
                break;
            case WARNING:
                bgColor = new Color(245, 158, 11, 240); // Amber
                borderColor = new Color(217, 119, 6);
                iconText = "⚠";
                break;
            case INFO:
            default:
                bgColor = new Color(37, 99, 235, 240);  // Royal Blue
                borderColor = new Color(29, 78, 216);
                iconText = "ℹ";
                break;
        }

        JPanel toastPanel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                // Draw border
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.dispose();
            }
        };
        toastPanel.setOpaque(false);
        toastPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Icon badge
        JLabel lblIcon = new JLabel(iconText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIcon.setForeground(fgColor);
        lblIcon.setPreferredSize(new Dimension(28, 28));

        // Message text
        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMsg.setForeground(fgColor);

        // Close button (x)
        JLabel lblClose = new JLabel("✕");
        lblClose.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClose.setForeground(new Color(255, 255, 255, 180));
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                closeImmediate();
            }
        });

        toastPanel.add(lblIcon, BorderLayout.WEST);
        toastPanel.add(lblMsg, BorderLayout.CENTER);
        toastPanel.add(lblClose, BorderLayout.EAST);

        // Click to dismiss
        toastPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toastPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                closeImmediate();
            }
        });

        setContentPane(toastPanel);
        pack();

        // Position at top center or bottom right of parent window
        if (parent != null && parent.isShowing()) {
            Point p = parent.getLocationOnScreen();
            int x = p.x + (parent.getWidth() - getWidth()) / 2;
            int y = p.y + 45; // Below the top navigation bar
            setLocation(x, y);
        } else {
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((scr.width - getWidth()) / 2, 60);
        }
    }

    private void display() {
        setVisible(true);
        try {
            setOpacity(0f);
        } catch (Exception ignored) {}

        fadeInTimer = new Timer(20, e -> {
            opacity += 0.1f;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                fadeInTimer.stop();
                startStayTimer();
            }
            try {
                setOpacity(opacity);
            } catch (Exception ignored) {}
        });
        fadeInTimer.start();
    }

    private void startStayTimer() {
        stayTimer = new Timer(2800, e -> {
            stayTimer.stop();
            startFadeOut();
        });
        stayTimer.setRepeats(false);
        stayTimer.start();
    }

    private void startFadeOut() {
        fadeOutTimer = new Timer(25, e -> {
            opacity -= 0.1f;
            if (opacity <= 0.05f) {
                opacity = 0f;
                fadeOutTimer.stop();
                dispose();
            } else {
                try {
                    setOpacity(opacity);
                } catch (Exception ignored) {}
            }
        });
        fadeOutTimer.start();
    }

    private void closeImmediate() {
        if (fadeInTimer != null) fadeInTimer.stop();
        if (stayTimer != null) stayTimer.stop();
        if (fadeOutTimer != null) fadeOutTimer.stop();
        dispose();
    }
}

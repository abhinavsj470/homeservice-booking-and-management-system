package com.homeservice.ui.common;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Utility for loading, scaling, and rendering app icons (Sidebar Toggle & Notification Bell)
 * with high-quality anti-aliased vector fallbacks.
 */
public class AppIcons {
    private static BufferedImage imgNotificationBell;
    private static BufferedImage imgSidebarToggle;

    static {
        imgNotificationBell = loadImage("icons/notification_bell.png");
        imgSidebarToggle = loadImage("icons/sidebar_toggle.png");
    }

    private static BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                return ImageIO.read(f);
            }
            // Classpath fallback
            InputStream is = AppIcons.class.getResourceAsStream("/" + path);
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception e) {
            System.err.println("Note: Icon " + path + " could not be loaded from file, will use vector renderer: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates an Icon for the Sidebar Toggle navigation button.
     */
    public static Icon getSidebarToggleIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (imgSidebarToggle != null) {
                    // Draw tintable or transparent PNG directly
                    g2.drawImage(imgSidebarToggle, x, y, size, size, null);
                } else {
                    // Vector drawing of the exact sidebar toggle symbol (rounded rect with left vertical divider)
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 6, 6);
                    int dividerX = x + (int) Math.round(size * 0.35);
                    g2.drawLine(dividerX, y + 2, dividerX, y + size - 2);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates an Icon for the System Notifications and Alerts bell button.
     */
    public static Icon getNotificationBellIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (imgNotificationBell != null) {
                    g2.drawImage(imgNotificationBell, x, y, size, size, null);
                } else {
                    // Vector drawing of the exact outline bell symbol
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    // Top handle loop
                    g2.drawArc(x + size / 2 - 3, y + 1, 6, 5, 0, 180);

                    // Bell body
                    Path2D.Double bell = new Path2D.Double();
                    bell.moveTo(x + 3, y + size - 5);
                    bell.lineTo(x + size - 3, y + size - 5);
                    bell.curveTo(x + size - 4, y + size * 0.55, x + size - 5, y + 5, x + size / 2.0, y + 5);
                    bell.curveTo(x + 5, y + 5, x + 4, y + size * 0.55, x + 3, y + size - 5);
                    g2.draw(bell);

                    // Clapper
                    g2.drawArc(x + size / 2 - 3, y + size - 5, 6, 4, 180, 180);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
}

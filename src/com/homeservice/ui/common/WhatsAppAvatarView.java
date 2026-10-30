package com.homeservice.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * WhatsApp-style profile avatar component.
 * Displays uploaded profile photo inside a smooth circular crop with a floating camera badge.
 * If no photo is uploaded, displays the first letter of the user's name on a rich gradient circle.
 */
public class WhatsAppAvatarView extends JComponent {
    private BufferedImage avatarImage;
    private String firstLetter = "U";
    private boolean isHovered = false;
    private Runnable onUploadRequested;

    private static final int COMP_SIZE = 132;
    private static final int AVATAR_SIZE = 120;
    private static final int OFFSET = 6;

    public WhatsAppAvatarView() {
        setPreferredSize(new Dimension(COMP_SIZE, COMP_SIZE));
        setMinimumSize(new Dimension(COMP_SIZE, COMP_SIZE));
        setMaximumSize(new Dimension(COMP_SIZE, COMP_SIZE));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Click to upload or change profile photo");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onUploadRequested != null) {
                    onUploadRequested.run();
                }
            }
        });
    }

    public void setAvatarData(BufferedImage img, String letter) {
        this.avatarImage = img;
        this.firstLetter = (letter != null && !letter.trim().isEmpty()) ? letter.trim().toUpperCase() : "U";
        repaint();
    }

    public void setOnUploadRequested(Runnable onUploadRequested) {
        this.onUploadRequested = onUploadRequested;
    }

    public boolean hasCustomImage() {
        return avatarImage != null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int x = OFFSET;
        int y = OFFSET;
        int d = AVATAR_SIZE;

        // Subtle drop shadow behind avatar
        g2.setColor(new Color(0, 0, 0, isHovered ? 35 : 18));
        g2.fillOval(x, y + 3, d, d);

        if (avatarImage != null) {
            // 1. Render uploaded photo clipped to circle
            Shape circleClip = new Ellipse2D.Double(x, y, d, d);
            Shape oldClip = g2.getClip();
            g2.clip(circleClip);
            g2.drawImage(avatarImage, x, y, d, d, null);
            g2.setClip(oldClip);

            // Subtle inner border ring
            g2.setStroke(new BasicStroke(isHovered ? 3.5f : 2.5f));
            g2.setColor(isHovered ? new Color(37, 99, 235) : new Color(226, 232, 240));
            g2.drawOval(x, y, d, d);

            if (isHovered) {
                // Semi-transparent hover tint
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillOval(x, y, d, d);
            }
        } else {
            // 2. No photo uploaded -> WhatsApp-style gradient circle with First Letter
            Color c1 = isHovered ? new Color(29, 78, 216) : new Color(37, 99, 235);
            Color c2 = isHovered ? new Color(67, 56, 202) : new Color(79, 70, 229);
            GradientPaint gp = new GradientPaint(x, y, c1, x + d, y + d, c2);
            g2.setPaint(gp);
            g2.fillOval(x, y, d, d);

            // Inner subtle specular highlight
            g2.setColor(new Color(255, 255, 255, 50));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x + 2, y + 2, d - 4, d - 4);

            // Large, bold, centered initial
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (d - fm.stringWidth(firstLetter)) / 2;
            int ty = y + (d - fm.getHeight()) / 2 + fm.getAscent() - 2;
            g2.drawString(firstLetter, tx, ty);
        }

        // 3. Floating WhatsApp Camera Badge at bottom-right
        int badgeD = 36;
        int bx = x + d - badgeD + 2;
        int by = y + d - badgeD + 2;

        // Badge shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(bx, by + 2, badgeD, badgeD);

        // Badge background (WhatsApp emerald green or theme blue)
        Color badgeBg = isHovered ? new Color(5, 150, 105) : new Color(16, 185, 129); // WhatsApp Emerald
        g2.setColor(badgeBg);
        g2.fillOval(bx, by, badgeD, badgeD);

        // Solid white ring border around camera badge
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.8f));
        g2.drawOval(bx, by, badgeD, badgeD);

        // Draw crisp vector camera icon
        drawCameraIcon(g2, bx + badgeD / 2, by + badgeD / 2);

        g2.dispose();
    }

    private void drawCameraIcon(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.WHITE);

        // Camera body
        int bodyW = 16;
        int bodyH = 11;
        int bodyX = cx - bodyW / 2;
        int bodyY = cy - bodyH / 2 + 1;
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 3, 3);

        // Top flash / pentaprism bump
        g2.fillRoundRect(cx - 3, bodyY - 2, 6, 3, 2, 2);

        // Lens ring
        g2.setColor(new Color(16, 185, 129));
        g2.fillOval(cx - 3, cy + 1, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawOval(cx - 3, cy + 1, 6, 6);

        // Small flash dot
        g2.fillOval(bodyX + bodyW - 4, bodyY + 2, 2, 2);
    }
}

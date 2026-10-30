package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Modern circular donut progress indicator with gradient stroke and centered metrics.
 */
public class RingProgressChart extends JPanel {
    private int percentage = 0; // 0 to 100
    private String title = "";
    private String subtitle = "";
    private Color startColor = new Color(37, 99, 235);
    private Color endColor = new Color(139, 92, 246);

    public RingProgressChart(String title, int percentage, String subtitle, Color startColor, Color endColor) {
        this.title = title;
        this.percentage = Math.max(0, Math.min(100, percentage));
        this.subtitle = subtitle;
        this.startColor = startColor;
        this.endColor = endColor;

        setPreferredSize(new Dimension(180, 160));
        setMinimumSize(new Dimension(140, 140));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public void setPercentage(int pct) {
        this.percentage = Math.max(0, Math.min(100, pct));
        repaint();
    }

    public void setSubtitle(String sub) {
        this.subtitle = sub;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();

        // Title at top
        g2.setFont(UITheme.FONT_SMALL);
        g2.setColor(UITheme.getTextMuted());
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = (w - fmTitle.stringWidth(title.toUpperCase())) / 2;
        g2.drawString(title.toUpperCase(), titleX, 16);

        // Ring dimensions
        int ringSize = Math.min(w - 30, h - 50);
        int ringThickness = 12;
        int ringX = (w - ringSize) / 2;
        int ringY = 22 + (h - 50 - ringSize) / 2;

        // Background track ring
        g2.setColor(new Color(241, 245, 249));
        g2.setStroke(new BasicStroke(ringThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(ringX + ringThickness / 2, ringY + ringThickness / 2, ringSize - ringThickness, ringSize - ringThickness);

        // Foreground progress ring (gradient)
        if (percentage > 0) {
            GradientPaint gp = new GradientPaint(ringX, ringY, startColor, ringX + ringSize, ringY + ringSize, endColor);
            g2.setPaint(gp);

            double angle = (percentage / 100.0) * 360.0;
            // Swing arc starts at 90 (top) and goes counter-clockwise with positive angles, or clockwise with negative
            Arc2D.Double progressArc = new Arc2D.Double(
                ringX + ringThickness / 2.0,
                ringY + ringThickness / 2.0,
                ringSize - ringThickness,
                ringSize - ringThickness,
                90,
                -angle,
                Arc2D.OPEN
            );
            g2.draw(progressArc);
        }

        // Center Percentage
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(UITheme.getTextDark());
        FontMetrics fmPct = g2.getFontMetrics();
        String pctStr = percentage + "%";
        int pctX = (w - fmPct.stringWidth(pctStr)) / 2;
        int pctY = ringY + (ringSize / 2) + (fmPct.getAscent() / 3);
        g2.drawString(pctStr, pctX, pctY);

        // Subtitle at bottom
        if (subtitle != null && !subtitle.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(UITheme.getTextMuted());
            FontMetrics fmSub = g2.getFontMetrics();
            int subX = (w - fmSub.stringWidth(subtitle)) / 2;
            g2.drawString(subtitle, subX, h - 4);
        }

        g2.dispose();
    }
}

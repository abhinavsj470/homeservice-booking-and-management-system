package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern mini bar chart component for dashboards displaying categorical or status distribution.
 */
public class MiniBarChart extends JPanel {
    public static class BarItem {
        public String label;
        public double value;
        public Color barColor;

        public BarItem(String label, double value, Color barColor) {
            this.label = label;
            this.value = value;
            this.barColor = barColor;
        }
    }

    private String chartTitle = "Activity Breakdown";
    private final List<BarItem> items = new ArrayList<>();

    public MiniBarChart(String title) {
        this.chartTitle = title;
        setPreferredSize(new Dimension(320, 160));
        setMinimumSize(new Dimension(200, 140));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 12, 10, 12));
    }

    public void clear() {
        items.clear();
        repaint();
    }

    public void addBar(String label, double value, Color barColor) {
        items.add(new BarItem(label, value, barColor));
        repaint();
    }

    public void setData(List<BarItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Chart Title
        g2.setFont(UITheme.FONT_SMALL);
        g2.setColor(UITheme.getTextMuted());
        g2.drawString(chartTitle.toUpperCase(), 12, 16);

        if (items.isEmpty()) {
            g2.setFont(UITheme.FONT_BODY);
            g2.drawString("No data available", w / 2 - 40, h / 2);
            g2.dispose();
            return;
        }

        double maxVal = 1.0;
        for (BarItem it : items) {
            if (it.value > maxVal) maxVal = it.value;
        }

        int bottomPad = 26;
        int topPad = 32;
        int chartH = h - bottomPad - topPad;
        int baselineY = h - bottomPad;

        // Baseline
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(12, baselineY, w - 12, baselineY);

        int count = items.size();
        int totalWidth = w - 24;
        int slotWidth = totalWidth / count;
        int barWidth = Math.max(16, Math.min(38, slotWidth - 16));

        for (int i = 0; i < count; i++) {
            BarItem it = items.get(i);
            int slotX = 12 + (i * slotWidth);
            int barX = slotX + (slotWidth - barWidth) / 2;

            int barHeight = (int) Math.round((it.value / maxVal) * (chartH - 14));
            if (it.value > 0 && barHeight < 6) barHeight = 6;
            int barY = baselineY - barHeight;

            // Draw Bar
            if (barHeight > 0) {
                g2.setColor(it.barColor);
                RoundRectangle2D.Double barRect = new RoundRectangle2D.Double(barX, barY, barWidth, barHeight, 8, 8);
                g2.fill(barRect);
            }

            // Draw Value above Bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(UITheme.getTextDark());
            String valStr = (it.value == (long) it.value) ? String.format("%d", (long) it.value) : String.format("%.1f", it.value);
            FontMetrics fmV = g2.getFontMetrics();
            int valX = barX + (barWidth - fmV.stringWidth(valStr)) / 2;
            int valY = Math.max(topPad - 2, barY - 4);
            g2.drawString(valStr, valX, valY);

            // Draw Label below Baseline
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(UITheme.getTextMuted());
            FontMetrics fmL = g2.getFontMetrics();
            String labelStr = it.label;
            if (fmL.stringWidth(labelStr) > slotWidth && labelStr.length() > 6) {
                labelStr = labelStr.substring(0, 5) + "..";
            }
            int lblX = slotX + (slotWidth - fmL.stringWidth(labelStr)) / 2;
            g2.drawString(labelStr, lblX, baselineY + 16);
        }

        g2.dispose();
    }
}

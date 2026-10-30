package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GradientCard extends JPanel {
    private final Color colorStart;
    private final Color colorEnd;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;

    public GradientCard(String icon, String title, String value, String subtitle, Color colorStart, Color colorEnd) {
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;
        setOpaque(false);
        setLayout(new BorderLayout(6, 6));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Row: Icon + Title
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(255, 255, 255, 230));

        topRow.add(lblIcon);
        topRow.add(lblTitle);

        // Center: Bold Value
        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(Color.WHITE);
        lblValue.setBorder(new EmptyBorder(4, 4, 2, 4));

        // Bottom: Subtitle / Growth
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(255, 255, 255, 210));
        lblSubtitle.setBorder(new EmptyBorder(0, 4, 0, 4));

        add(topRow, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void update(String value, String subtitle) {
        lblValue.setText(value);
        lblSubtitle.setText(subtitle);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 20;

        Shape shape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
        g2.clip(shape);

        // Smooth Linear Gradient
        GradientPaint gp = new GradientPaint(0, 0, colorStart, w, h, colorEnd);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Layered decorative translucent circles matching reference design
        g2.setColor(new Color(255, 255, 255, 42));
        g2.fillOval(w - 75, -25, 115, 115);

        g2.setColor(new Color(255, 255, 255, 28));
        g2.fillOval(w - 45, h - 55, 95, 95);

        g2.dispose();
        super.paintComponent(g);
    }
}

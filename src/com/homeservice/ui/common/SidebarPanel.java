package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    private final Color gradientTop = new Color(14, 165, 233);   // Vibrant Sky/Cyan #0EA5E9
    private final Color gradientBottom = new Color(15, 23, 42);  // Deep Midnight Navy #0F172A

    private final JPanel menuContainer;
    private final List<SidebarMenuItem> menuItems = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<Integer> onSelectionChanged;
    private boolean isCollapsed = false;
    private final JLabel lblTitle;
    private final JLabel lblSub;
    private final JLabel lblEmblem;

    public SidebarPanel(String brandTitle, String brandSubtitle) {
        setPreferredSize(new Dimension(225, 0));
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header with Logo
        JPanel brandHeader = new JPanel();
        brandHeader.setLayout(new BoxLayout(brandHeader, BoxLayout.Y_AXIS));
        brandHeader.setOpaque(false);
        brandHeader.setBorder(new EmptyBorder(22, 16, 18, 16));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        // Emblem
        lblEmblem = new JLabel("🛠️");
        lblEmblem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblEmblem.setOpaque(false);

        JPanel textCol = new JPanel(new GridLayout(2, 1, 2, 2));
        textCol.setOpaque(false);

        lblTitle = new JLabel(brandTitle);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        lblSub = new JLabel(brandSubtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(255, 255, 255, 180));

        textCol.add(lblTitle);
        textCol.add(lblSub);

        logoRow.add(lblEmblem);
        logoRow.add(textCol);
        brandHeader.add(logoRow);

        add(brandHeader, BorderLayout.NORTH);

        // Menu Container
        menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(new EmptyBorder(8, 10, 12, 10));

        JScrollPane scrollPane = new JScrollPane(menuContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void toggleCollapse() {
        isCollapsed = !isCollapsed;
        int targetWidth = isCollapsed ? 68 : 225;
        setPreferredSize(new Dimension(targetWidth, 0));
        lblTitle.setVisible(!isCollapsed);
        lblSub.setVisible(!isCollapsed);
        for (SidebarMenuItem item : menuItems) {
            item.setCollapsed(isCollapsed);
        }
        revalidate();
        repaint();
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setOnSelectionChanged(Consumer<Integer> callback) {
        this.onSelectionChanged = callback;
    }

    public void clearMenu() {
        menuItems.clear();
        menuContainer.removeAll();
        selectedIndex = 0;
    }

    public void addSectionHeader(String title) {
        if (!isCollapsed) {
            JLabel lbl = new JLabel(title.toUpperCase());
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(new Color(255, 255, 255, 140));
            lbl.setBorder(new EmptyBorder(14, 12, 6, 12));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuContainer.add(lbl);
        }
    }

    public void addMenuItem(String icon, String label) {
        addMenuItem(icon, label, null);
    }

    public void addMenuItem(String icon, String label, String badgeText) {
        int index = menuItems.size();
        SidebarMenuItem item = new SidebarMenuItem(icon, label, badgeText, index == selectedIndex, isCollapsed);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedIndex(index);
                if (onSelectionChanged != null) {
                    onSelectionChanged.accept(index);
                }
            }
        });
        menuItems.add(item);
        menuContainer.add(item);
        menuContainer.add(Box.createVerticalStrut(4));
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
        for (int i = 0; i < menuItems.size(); i++) {
            menuItems.get(i).setSelected(i == index);
        }
        repaint();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Sleek Vertical Cyan to Deep Midnight Gradient
        GradientPaint gp = new GradientPaint(0, 0, gradientTop, 0, h, gradientBottom);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
        super.paintComponent(g);
    }

    // Individual Menu Item Component
    private static class SidebarMenuItem extends JPanel {
        private final JLabel lblIcon;
        private final JLabel lblText;
        private final JLabel lblBadge;
        private boolean isSelected;
        private boolean isHovered;

        public SidebarMenuItem(String icon, String text, String badgeText, boolean isSelected, boolean collapsed) {
            this.isSelected = isSelected;
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            lblIcon = new JLabel(icon);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            lblIcon.setForeground(Color.WHITE);
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            lblIcon.setPreferredSize(new Dimension(24, 24));

            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblText.setForeground(isSelected ? Color.WHITE : new Color(255, 255, 255, 210));
            lblText.setVisible(!collapsed);

            lblBadge = new JLabel(badgeText != null ? badgeText : "");
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblBadge.setForeground(Color.WHITE);
            lblBadge.setOpaque(true);
            lblBadge.setBackground(new Color(239, 68, 68));
            lblBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
            lblBadge.setVisible(badgeText != null && !badgeText.isEmpty() && !collapsed);

            add(lblIcon, BorderLayout.WEST);
            add(lblText, BorderLayout.CENTER);
            add(lblBadge, BorderLayout.EAST);

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
            });
        }

        public void setCollapsed(boolean collapsed) {
            lblText.setVisible(!collapsed);
            lblBadge.setVisible(!collapsed && !lblBadge.getText().isEmpty());
            revalidate();
            repaint();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            lblText.setForeground(selected ? Color.WHITE : new Color(255, 255, 255, 210));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelected || isHovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (isSelected) {
                    g2.setColor(new Color(255, 255, 255, 52));
                    Shape pill = new RoundRectangle2D.Float(4, 2, w - 8, h - 4, 12, 12);
                    g2.fill(pill);

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 8, 4, h - 16, 4, 4);
                } else if (isHovered) {
                    g2.setColor(new Color(255, 255, 255, 28));
                    Shape pill = new RoundRectangle2D.Float(4, 2, w - 8, h - 4, 12, 12);
                    g2.fill(pill);
                }
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}

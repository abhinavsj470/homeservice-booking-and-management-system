package com.homeservice.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame parent) {
        super(parent, "About Project - Home Service Booking System", true);
        setSize(520, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header Title
        JLabel title = new JLabel("Home Service Booking & Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Second-Year Academic Project in Java & OOP");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Team Box
        JPanel teamPanel = new JPanel();
        teamPanel.setLayout(new BoxLayout(teamPanel, BoxLayout.Y_AXIS));
        teamPanel.setBackground(new Color(248, 250, 252));
        teamPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel teamHeader = new JLabel("SUBMITTED BY:");
        teamHeader.setFont(UITheme.FONT_BOLD);
        teamHeader.setForeground(UITheme.TEXT_DARK);
        teamPanel.add(teamHeader);
        teamPanel.add(Box.createVerticalStrut(10));

        String[][] members = {
            {"Abhinav Sajeev", "25CS009"},
            {"Alan Jojo", "25CS032"},
            {"Allan Peter", "25CS038"},
            {"Sebin Raju", "25CS187"}
        };

        for (String[] m : members) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(new Color(248, 250, 252));
            JLabel name = new JLabel(m[0]);
            name.setFont(UITheme.FONT_BODY);
            JLabel roll = new JLabel(m[1]);
            roll.setFont(UITheme.FONT_BOLD);
            roll.setForeground(UITheme.PRIMARY);
            row.add(name, BorderLayout.WEST);
            row.add(roll, BorderLayout.EAST);
            teamPanel.add(row);
            teamPanel.add(Box.createVerticalStrut(6));
        }

        // Tech Info
        JPanel techPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        techPanel.setBackground(Color.WHITE);
        techPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        techPanel.add(createBadge("Platform: Java SE 25"));
        techPanel.add(createBadge("GUI: Java Swing + FlatLaf"));
        techPanel.add(createBadge("Database: MySQL 8.0 via JDBC"));
        techPanel.add(createBadge("Architecture: MVC + DAO + OOP"));

        JButton btnClose = UITheme.createPrimaryButton("Close");
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dispose());

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(18));
        content.add(teamPanel);
        content.add(Box.createVerticalStrut(12));
        content.add(techPanel);
        content.add(Box.createVerticalStrut(16));
        content.add(btnClose);

        setContentPane(content);
    }

    private JLabel createBadge(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        return lbl;
    }
}

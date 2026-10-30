package com.homeservice;

import com.homeservice.db.DatabaseInitializer;
import com.homeservice.ui.MainFrame;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Set modern UI Theme
        UITheme.setupLaf();

        // Initialize Database Schema & Seed Data in background or before UI
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Database initialization warning: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Notice: Could not automatically connect to MySQL database.\nPlease verify MySQL is running on localhost:3306 with credentials in db.properties.\nError: " + e.getMessage(),
                "Database Notice", JOptionPane.WARNING_MESSAGE);
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            mainFrame.toFront();
            mainFrame.requestFocus();
            mainFrame.promptLogin();
        });
    }
}

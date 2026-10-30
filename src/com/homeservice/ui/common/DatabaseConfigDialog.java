package com.homeservice.ui.common;

import com.homeservice.config.DBConnection;
import com.homeservice.config.DatabaseConfig;
import com.homeservice.db.DatabaseInitializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * In-App Database Configuration Dialog.
 * Allows users on secondary laptops to easily point the application
 * to a shared Cloud MySQL instance (e.g. Aiven) or local LAN server IP.
 */
public class DatabaseConfigDialog extends JDialog {
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDbName;
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JCheckBox chkSsl;
    private JLabel lblStatus;
    private boolean saved = false;

    public DatabaseConfigDialog(Window parent) {
        super(parent, "Database Connection Settings", ModalityType.APPLICATION_MODAL);
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Database Connection Settings");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("<html>Connect to your <b>Cloud Database</b> (Supabase PostgreSQL / Cloud MySQL) or local server.</html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        root.add(pnlHeader, BorderLayout.NORTH);

        // Form fields
        JPanel pnlForm = new JPanel(new GridLayout(6, 1, 0, 10));
        pnlForm.setOpaque(false);

        txtHost = new JTextField(DatabaseConfig.getHost());
        txtPort = new JTextField(String.valueOf(DatabaseConfig.getPort()));
        txtDbName = new JTextField(DatabaseConfig.getDbName());
        txtUser = new JTextField(DatabaseConfig.getUser());
        txtPassword = new JPasswordField(DatabaseConfig.getPassword());
        chkSsl = new JCheckBox("Enable SSL (Required for Cloud providers like Supabase / Aiven)", DatabaseConfig.isSsl());
        chkSsl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSsl.setOpaque(false);

        pnlForm.add(createFieldPanel("Database Host (Cloud host or server IP):", txtHost));
        pnlForm.add(createFieldPanel("Port:", txtPort));
        pnlForm.add(createFieldPanel("Database Name:", txtDbName));
        pnlForm.add(createFieldPanel("Username:", txtUser));
        pnlForm.add(createFieldPanel("Password:", txtPassword));
        pnlForm.add(chkSsl);

        root.add(pnlForm, BorderLayout.CENTER);

        // Footer with status and action buttons
        JPanel pnlFooter = new JPanel(new BorderLayout(0, 12));
        pnlFooter.setOpaque(false);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFooter.add(lblStatus, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnTest = UITheme.createSecondaryButton("Test Connection");
        btnTest.addActionListener(e -> runTestConnection());

        JButton btnSave = UITheme.createPrimaryButton("Save & Connect");
        btnSave.addActionListener(e -> saveAndConnect());

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        pnlButtons.add(btnTest);
        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        pnlFooter.add(pnlButtons, BorderLayout.SOUTH);

        root.add(pnlFooter, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(51, 65, 85));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private boolean runTestConnection() {
        lblStatus.setText("Testing connection...");
        lblStatus.setForeground(new Color(37, 99, 235));
        lblStatus.paintImmediately(lblStatus.getVisibleRect());

        String h = txtHost.getText().trim();
        int p;
        try {
            p = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            lblStatus.setText("❌ Invalid port number.");
            lblStatus.setForeground(new Color(220, 38, 38));
            return false;
        }
        String db = txtDbName.getText().trim();
        String u = txtUser.getText().trim();
        String pass = new String(txtPassword.getPassword());
        boolean ssl = chkSsl.isSelected();

        try {
            if (p == 33060) {
                lblStatus.setText("❌ Port 33060 is MySQL X Protocol. Please use port 3306 for Java JDBC.");
                lblStatus.setForeground(new Color(220, 38, 38));
                return false;
            }
            boolean ok = DBConnection.testConnection(h, p, db, u, pass, ssl);
            if (ok) {
                lblStatus.setText("✅ Connection Successful!");
                lblStatus.setForeground(new Color(16, 185, 129));
                return true;
            } else {
                lblStatus.setText("❌ Connection refused or invalid.");
                lblStatus.setForeground(new Color(220, 38, 38));
                return false;
            }
        } catch (SQLException ex) {
            String msg = ex.getMessage();
            if (msg != null && (msg.toLowerCase().contains("x protocol") || msg.toLowerCase().contains("max_allowed_packet") || p == 33060)) {
                lblStatus.setText("❌ Error: Port " + p + " is running MySQL X Protocol. Please change Port to 3306!");
            } else {
                lblStatus.setText("❌ Error: " + ex.getMessage());
            }
            lblStatus.setForeground(new Color(220, 38, 38));
            return false;
        }
    }

    private void saveAndConnect() {
        String h = txtHost.getText().trim();
        int p;
        try {
            p = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a valid number.", "Invalid Port", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (p == 33060) {
            int fix = JOptionPane.showConfirmDialog(this,
                "Port 33060 is the MySQL X Protocol port, which causes handshake errors with Java JDBC.\n\n" +
                "Would you like to automatically switch to the standard port 3306?",
                "MySQL X Protocol Port Detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (fix == JOptionPane.YES_OPTION) {
                p = 3306;
                txtPort.setText("3306");
            }
        }
        String db = txtDbName.getText().trim();
        String u = txtUser.getText().trim();
        String pass = new String(txtPassword.getPassword());
        boolean ssl = chkSsl.isSelected();

        // Save configuration
        DatabaseConfig.updateAndSaveConfig(h, p, db, u, pass, ssl);

        // Test and initialize
        try {
            DatabaseInitializer.initialize();
            saved = true;
            JOptionPane.showMessageDialog(this,
                "Database configuration saved and verified!\nConnected to: " + h + ":" + p,
                "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Settings saved, but could not connect immediately:\n" + ex.getMessage(),
                "Connection Warning", JOptionPane.WARNING_MESSAGE);
            dispose();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

package com.homeservice.ui.common;

import com.homeservice.config.DBConnection;
import com.homeservice.config.DatabaseConfig;
import com.homeservice.dao.ServiceCategoryDAO;
import com.homeservice.model.ServiceCategory;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RegisterDialog extends JDialog {
    private final AuthService authService = new AuthService();
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();
    private boolean registered = false;

    // Fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JComboBox<String> cmbRole;

    // Provider specific fields
    private JPanel providerDetailsPanel;
    private JComboBox<ServiceCategory> cmbCategory;
    private JSpinner spnrExperience;
    private JTextField txtHourlyRate;
    private JTextArea txtBio;

    public RegisterDialog(Frame parent) {
        super(parent, "Create New Account - Home Service System", true);
        setSize(480, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Join Home Service Platform");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Register as a Customer or Service Provider");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(239, 246, 255));
        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        form.add(createFieldLabel("Registering As"));
        cmbRole = new JComboBox<>(new String[]{"Customer", "Service Provider"});
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbRole.addActionListener(e -> toggleProviderFields());
        form.add(cmbRole);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Full Name *"));
        txtName = new JTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtName);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Email Address *"));
        txtEmail = new JTextField();
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtEmail);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Password *"));
        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Phone Number *"));
        txtPhone = new JTextField();
        txtPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtPhone);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Service Address / Location *"));
        txtAddress = new JTextField();
        txtAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(txtAddress);
        form.add(Box.createVerticalStrut(10));

        // Provider Details Panel
        providerDetailsPanel = new JPanel();
        providerDetailsPanel.setLayout(new BoxLayout(providerDetailsPanel, BoxLayout.Y_AXIS));
        providerDetailsPanel.setBackground(new Color(248, 250, 252));
        providerDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));
        providerDetailsPanel.setVisible(false);

        providerDetailsPanel.add(createFieldLabel("Service Category / Skill"));
        List<ServiceCategory> categories = categoryDAO.getAllCategories();
        cmbCategory = new JComboBox<>(categories.toArray(new ServiceCategory[0]));
        cmbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        providerDetailsPanel.add(cmbCategory);
        providerDetailsPanel.add(Box.createVerticalStrut(8));

        JPanel provGrid = new JPanel(new GridLayout(1, 2, 8, 8));
        provGrid.setOpaque(false);
        JPanel pExp = new JPanel(new BorderLayout(4, 4));
        pExp.setOpaque(false);
        pExp.add(new JLabel("Experience (Yrs):"), BorderLayout.NORTH);
        spnrExperience = new JSpinner(new SpinnerNumberModel(2, 0, 50, 1));
        pExp.add(spnrExperience, BorderLayout.CENTER);

        JPanel pRate = new JPanel(new BorderLayout(4, 4));
        pRate.setOpaque(false);
        pRate.add(new JLabel("Hourly Rate (₹):"), BorderLayout.NORTH);
        txtHourlyRate = new JTextField("400.00");
        pRate.add(txtHourlyRate, BorderLayout.CENTER);

        provGrid.add(pExp);
        provGrid.add(pRate);
        providerDetailsPanel.add(provGrid);
        providerDetailsPanel.add(Box.createVerticalStrut(8));

        providerDetailsPanel.add(createFieldLabel("Professional Bio / Description"));
        txtBio = new JTextArea(3, 20);
        txtBio.setLineWrap(true);
        txtBio.setWrapStyleWord(true);
        JScrollPane spBio = new JScrollPane(txtBio);
        spBio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        providerDetailsPanel.add(spBio);

        form.add(providerDetailsPanel);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        root.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(6, 16, 12, 16));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnRegister = UITheme.createPrimaryButton("Create Account");
        btnRegister.addActionListener(e -> performRegistration());

        footer.add(btnCancel);
        footer.add(btnRegister);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void toggleProviderFields() {
        boolean isProvider = "Service Provider".equals(cmbRole.getSelectedItem());
        providerDetailsPanel.setVisible(isProvider);
        revalidate();
        repaint();
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void performRegistration() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verify Database Connectivity first
        try {
            DBConnection.getConnection();
        } catch (SQLException dbEx) {
            JOptionPane.showMessageDialog(this,
                "Unable to connect to MySQL database at: " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "\n\n" +
                "Error: " + dbEx.getMessage() + "\n\n" +
                "Please configure your database connection settings first.",
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if ("Service Provider".equals(cmbRole.getSelectedItem())) {
                ServiceCategory cat = (ServiceCategory) cmbCategory.getSelectedItem();
                if (cat == null) {
                    JOptionPane.showMessageDialog(this, "Please select a service category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int exp = (Integer) spnrExperience.getValue();
                double rate = Double.parseDouble(txtHourlyRate.getText().trim());
                String bio = txtBio.getText().trim();

                User user = authService.registerProvider(name, email, password, phone, address, cat.getId(), exp, rate, bio);
                if (user != null) {
                    registered = true;
                    JOptionPane.showMessageDialog(this, "Service Provider account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not create account. Please verify database connection and try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                User user = authService.registerCustomer(name, email, password, phone, address);
                if (user != null) {
                    registered = true;
                    JOptionPane.showMessageDialog(this, "Customer account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not create account. Please verify database connection and try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }
}

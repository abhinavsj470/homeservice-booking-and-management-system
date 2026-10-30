package com.homeservice.ui.provider;

import com.homeservice.dao.ServiceCategoryDAO;
import com.homeservice.dao.ServiceProviderDAO;
import com.homeservice.dao.UserDAO;
import com.homeservice.model.ServiceCategory;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProviderProfilePanel extends JPanel {
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Runnable onUpdateCallback;

    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JComboBox<ServiceCategory> cmbCategory;
    private JSpinner spnrExperience;
    private JTextField txtHourlyRate;
    private JCheckBox chkAvailable;
    private JTextArea txtBio;
    private JPasswordField txtNewPassword;

    public ProviderProfilePanel(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(620, 720));

        JLabel title = new JLabel("Provider Profile & Availability Settings");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        card.add(title);

        JLabel sub = new JLabel("Keep your service pricing, skill details, and availability status current");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        card.add(sub);
        card.add(Box.createVerticalStrut(16));

        // Availability status toggle
        JPanel availPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        availPanel.setOpaque(false);
        chkAvailable = new JCheckBox("Available for New Customer Service Bookings");
        chkAvailable.setFont(UITheme.FONT_BOLD);
        chkAvailable.setForeground(UITheme.PRIMARY);
        availPanel.add(chkAvailable);
        card.add(availPanel);
        card.add(Box.createVerticalStrut(12));

        // Form Fields
        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(createFieldBox("Full Name", txtName = new JTextField()));
        txtEmail = new JTextField();
        txtEmail.setEditable(false);
        txtEmail.setBackground(new Color(241, 245, 249));
        grid.add(createFieldBox("Email (Fixed)", txtEmail));

        grid.add(createFieldBox("Phone Number", txtPhone = new JTextField()));
        grid.add(createFieldBox("Service Location / City", txtAddress = new JTextField()));

        cmbCategory = new JComboBox<>();
        grid.add(createFieldBox("Primary Service Category", cmbCategory));

        spnrExperience = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
        grid.add(createFieldBox("Experience (Years)", spnrExperience));

        grid.add(createFieldBox("Hourly Service Rate (₹)", txtHourlyRate = new JTextField()));

        txtNewPassword = new JPasswordField();
        grid.add(createFieldBox("New Password (optional)", txtNewPassword));

        card.add(grid);
        card.add(Box.createVerticalStrut(12));

        card.add(createFieldLabel("Professional Bio & Expertise Summary"));
        txtBio = new JTextArea(3, 20);
        txtBio.setLineWrap(true);
        txtBio.setWrapStyleWord(true);
        JScrollPane spBio = new JScrollPane(txtBio);
        spBio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.add(spBio);
        card.add(Box.createVerticalStrut(18));

        JButton btnSave = UITheme.createPrimaryButton("Save Profile & Rates");
        btnSave.addActionListener(e -> saveProfile());
        card.add(btnSave);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);

        JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outer.setOpaque(false);
        outer.add(card);

        add(outer, BorderLayout.CENTER);
        loadCategories();
        loadProfile();
    }

    private JPanel createFieldBox(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        return lbl;
    }

    private void loadCategories() {
        cmbCategory.removeAllItems();
        List<ServiceCategory> list = categoryDAO.getAllCategories();
        for (ServiceCategory c : list) {
            cmbCategory.addItem(c);
        }
    }

    public void loadProfile() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        txtName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        txtAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        txtNewPassword.setText("");

        ServiceProviderProfile profile = providerDAO.findByUserId(user.getId());
        if (profile != null) {
            chkAvailable.setSelected(profile.isAvailable());
            spnrExperience.setValue(profile.getExperienceYears());
            txtHourlyRate.setText(String.format("%.2f", profile.getHourlyRate()));
            txtBio.setText(profile.getBio() != null ? profile.getBio() : "");

            for (int i = 0; i < cmbCategory.getItemCount(); i++) {
                if (cmbCategory.getItemAt(i).getId() == profile.getCategoryId()) {
                    cmbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveProfile() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String rateStr = txtHourlyRate.getText().trim();
        String bio = txtBio.getText().trim();
        String newPass = new String(txtNewPassword.getPassword()).trim();
        ServiceCategory cat = (ServiceCategory) cmbCategory.getSelectedItem();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || rateStr.isEmpty() || cat == null) {
            JOptionPane.showMessageDialog(this, "Please complete all required profile fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double rate;
        try {
            rate = Double.parseDouble(rateStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric hourly rate.", "Invalid Rate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setFullName(name);
        user.setPhone(phone);
        user.setAddress(address);
        userDAO.update(user);

        if (!newPass.isEmpty()) {
            userDAO.updatePassword(user.getId(), newPass);
            user.setPassword(newPass);
        }

        ServiceProviderProfile profile = providerDAO.findByUserId(user.getId());
        if (profile != null) {
            profile.setCategoryId(cat.getId());
            profile.setExperienceYears((Integer) spnrExperience.getValue());
            profile.setHourlyRate(rate);
            profile.setBio(bio);
            profile.setAvailable(chkAvailable.isSelected());
            providerDAO.updateProfile(profile);
        }

        JOptionPane.showMessageDialog(this, "Provider profile successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        if (onUpdateCallback != null) onUpdateCallback.run();
    }
}

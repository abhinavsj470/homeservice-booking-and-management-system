package com.homeservice.ui.customer;

import com.homeservice.dao.UserDAO;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.common.UITheme;
import com.homeservice.ui.common.WhatsAppAvatarView;
import com.homeservice.util.AvatarManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * WhatsApp-style Profile and Settings Panel for Customers.
 * Features:
 * - Circular WhatsApp profile avatar with floating camera badge.
 * - Photo upload supporting PNG, JPG, and JPEG with automatic center-crop and scaling.
 * - Dynamic fallback: displays the first letter of the user's name on a rich gradient circle when no photo is uploaded.
 * - Real-time avatar update as the user edits their full name.
 * - WhatsApp-inspired profile sections: Name, About/Status, Phone, Address, Email, and Password security.
 * - Photo removal capability to easily restore the initial letter avatar.
 */
public class CustomerProfilePanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final Runnable onProfileUpdated;

    // Avatar & Hero
    private WhatsAppAvatarView avatarView;
    private JLabel lblHeroName;
    private JLabel lblPhotoStatus;
    private JButton btnUploadPhoto;
    private JButton btnRemovePhoto;

    // Fields
    private JTextField txtName;
    private JTextField txtAbout;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JPasswordField txtNewPassword;
    private JButton btnTogglePass;
    private boolean isPassVisible = false;

    // Feedback
    private JLabel lblSaveStatus;

    public CustomerProfilePanel() {
        this(null);
    }

    public CustomerProfilePanel(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        scrollContent.setOpaque(false);

        // Main WhatsApp Profile Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(28, 36, 28, 36)
        ));
        card.setPreferredSize(new Dimension(600, 860));
        card.setMaximumSize(new Dimension(640, 920));

        // 1. Header Row
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Profile & Settings");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Personalize your profile photo, contact details, and account settings");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        card.add(headerPanel);
        card.add(Box.createVerticalStrut(20));

        // 2. WhatsApp Avatar Hero Section
        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setOpaque(false);
        heroPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarView = new WhatsAppAvatarView();
        avatarView.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarView.setOnUploadRequested(this::chooseAndUploadPhoto);
        heroPanel.add(avatarView);
        heroPanel.add(Box.createVerticalStrut(10));

        lblHeroName = new JLabel("Abhinav Customer");
        lblHeroName.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblHeroName.setForeground(new Color(15, 23, 42));
        lblHeroName.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(lblHeroName);
        heroPanel.add(Box.createVerticalStrut(4));

        JLabel lblBadge = new JLabel("🟢 Verified Customer Account");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(new Color(4, 120, 87));
        lblBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(lblBadge);
        heroPanel.add(Box.createVerticalStrut(10));

        // Photo Action Buttons
        JPanel photoButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        photoButtons.setOpaque(false);

        btnUploadPhoto = new JButton("📷 Change Photo");
        btnUploadPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUploadPhoto.setForeground(Color.WHITE);
        btnUploadPhoto.setBackground(new Color(37, 99, 235));
        btnUploadPhoto.setFocusPainted(false);
        btnUploadPhoto.setBorder(new EmptyBorder(7, 14, 7, 14));
        btnUploadPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUploadPhoto.addActionListener(e -> chooseAndUploadPhoto());

        btnRemovePhoto = new JButton("🗑️ Remove Photo");
        btnRemovePhoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRemovePhoto.setForeground(new Color(185, 28, 28));
        btnRemovePhoto.setBackground(new Color(254, 242, 242));
        btnRemovePhoto.setFocusPainted(false);
        btnRemovePhoto.setBorder(new EmptyBorder(7, 12, 7, 12));
        btnRemovePhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemovePhoto.addActionListener(e -> removePhoto());

        photoButtons.add(btnUploadPhoto);
        photoButtons.add(btnRemovePhoto);
        photoButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(photoButtons);
        heroPanel.add(Box.createVerticalStrut(6));

        lblPhotoStatus = new JLabel("Click photo or camera to upload (JPG or PNG)");
        lblPhotoStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblPhotoStatus.setForeground(new Color(148, 163, 184));
        lblPhotoStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(lblPhotoStatus);

        card.add(heroPanel);
        card.add(Box.createVerticalStrut(18));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        // 3. Profile Information Fields
        // Name
        card.add(createFieldHeader("👤", "Your Name", "This name is visible to service professionals when bookings are made."));
        txtName = createStyledTextField();
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateAvatarLetter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateAvatarLetter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateAvatarLetter(); }
        });
        card.add(txtName);
        card.add(Box.createVerticalStrut(12));

        // WhatsApp About / Status
        card.add(createFieldHeader("💬", "About / Status", "Short status or customer note visible on your booking receipts."));
        txtAbout = createStyledTextField();
        card.add(txtAbout);
        card.add(Box.createVerticalStrut(12));

        // Phone & Email Row
        JPanel contactRow = new JPanel(new GridLayout(1, 2, 12, 0));
        contactRow.setOpaque(false);
        contactRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel phoneCol = new JPanel(new BorderLayout(0, 4));
        phoneCol.setOpaque(false);
        JLabel lblPhone = new JLabel("📞  Phone Number");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPhone.setForeground(new Color(51, 65, 85));
        txtPhone = createStyledTextField();
        phoneCol.add(lblPhone, BorderLayout.NORTH);
        phoneCol.add(txtPhone, BorderLayout.CENTER);

        JPanel emailCol = new JPanel(new BorderLayout(0, 4));
        emailCol.setOpaque(false);
        JLabel lblEmail = new JLabel("✉️  Email (Primary Account ID)");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(new Color(51, 65, 85));
        txtEmail = createStyledTextField();
        txtEmail.setEditable(false);
        txtEmail.setBackground(new Color(241, 245, 249));
        txtEmail.setToolTipText("Email address is your unique system login and cannot be altered.");
        emailCol.add(lblEmail, BorderLayout.NORTH);
        emailCol.add(txtEmail, BorderLayout.CENTER);

        contactRow.add(phoneCol);
        contactRow.add(emailCol);
        card.add(contactRow);
        card.add(Box.createVerticalStrut(12));

        // Address
        card.add(createFieldHeader("📍", "Default Service Address", "Default location pre-filled when you schedule a new service request."));
        txtAddress = createStyledTextField();
        card.add(txtAddress);
        card.add(Box.createVerticalStrut(12));

        // Password Security Section
        card.add(createFieldHeader("🔒", "Security & Password", "Leave blank to keep your current password."));
        JPanel passRow = new JPanel(new BorderLayout(6, 0));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNewPassword.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        btnTogglePass = new JButton("👁️");
        btnTogglePass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnTogglePass.setPreferredSize(new Dimension(42, 36));
        btnTogglePass.setBackground(new Color(241, 245, 249));
        btnTogglePass.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        btnTogglePass.setFocusPainted(false);
        btnTogglePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePass.setToolTipText("Show/hide password");
        btnTogglePass.addActionListener(e -> togglePasswordVisibility());

        passRow.add(txtNewPassword, BorderLayout.CENTER);
        passRow.add(btnTogglePass, BorderLayout.EAST);
        card.add(passRow);
        card.add(Box.createVerticalStrut(20));

        // 4. Save Changes Action Row
        JPanel actionRow = new JPanel(new BorderLayout(12, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnSave = new JButton("💾  Save Profile Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(37, 99, 235)); // Royal Blue
        btnSave.setBorder(new EmptyBorder(10, 24, 10, 24));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());

        lblSaveStatus = new JLabel("");
        lblSaveStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSaveStatus.setForeground(new Color(16, 185, 129));

        actionRow.add(btnSave, BorderLayout.WEST);
        actionRow.add(lblSaveStatus, BorderLayout.CENTER);

        card.add(actionRow);

        scrollContent.add(card);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadUserData();
    }

    private JPanel createFieldHeader(String icon, String labelText, String helperText) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(icon + "  " + labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(30, 41, 59));

        JLabel sub = new JLabel(helperText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(148, 163, 184));

        p.add(lbl, BorderLayout.NORTH);
        p.add(sub, BorderLayout.SOUTH);
        p.setBorder(new EmptyBorder(0, 0, 4, 0));
        return p;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(new Color(15, 23, 42));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setPreferredSize(new Dimension(200, 36));
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private void togglePasswordVisibility() {
        isPassVisible = !isPassVisible;
        txtNewPassword.setEchoChar(isPassVisible ? (char) 0 : '•');
        btnTogglePass.setText(isPassVisible ? "🔒" : "👁️");
    }

    public void loadUserData() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        txtName.setText(user.getFullName());
        lblHeroName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        txtAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        txtAbout.setText(AvatarManager.getUserAbout(user.getId()));
        txtNewPassword.setText("");
        lblSaveStatus.setText("");

        refreshAvatarDisplay(user);
    }

    private void refreshAvatarDisplay(User user) {
        if (user == null) return;
        BufferedImage img = AvatarManager.getAvatarImage(user.getId());
        String letter = AvatarManager.getFirstLetter(user.getFullName());
        avatarView.setAvatarData(img, letter);

        boolean hasCustom = (img != null);
        btnRemovePhoto.setVisible(hasCustom);
        if (hasCustom) {
            lblPhotoStatus.setText("✓ Custom profile photo active");
            lblPhotoStatus.setForeground(new Color(16, 185, 129));
        } else {
            lblPhotoStatus.setText("Showing first letter '" + letter + "'. Click to upload a picture.");
            lblPhotoStatus.setForeground(new Color(100, 116, 139));
        }
    }

    private void updateAvatarLetter() {
        if (!avatarView.hasCustomImage()) {
            String name = txtName.getText().trim();
            String letter = AvatarManager.getFirstLetter(name);
            avatarView.setAvatarData(null, letter);
            lblHeroName.setText(name.isEmpty() ? "Customer Profile" : name);
            lblPhotoStatus.setText("Showing first letter '" + letter + "'. Click to upload a picture.");
        } else {
            String name = txtName.getText().trim();
            lblHeroName.setText(name.isEmpty() ? "Customer Profile" : name);
        }
    }

    private void chooseAndUploadPhoto() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Picture (WhatsApp Style)");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png, *.webp)", "jpg", "jpeg", "png", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                boolean ok = AvatarManager.saveAvatar(user.getId(), selectedFile);
                if (ok) {
                    refreshAvatarDisplay(user);
                    lblSaveStatus.setText("✓ Profile photo updated!");
                    lblSaveStatus.setForeground(new Color(16, 185, 129));

                    if (onProfileUpdated != null) {
                        onProfileUpdated.run();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to process image: " + ex.getMessage(),
                    "Image Upload Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removePhoto() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove custom profile picture and show your first letter avatar?",
            "Remove Profile Photo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            AvatarManager.removeAvatar(user.getId());
            refreshAvatarDisplay(user);
            lblSaveStatus.setText("✓ Profile photo removed. Initial restored.");
            lblSaveStatus.setForeground(new Color(59, 130, 246));

            if (onProfileUpdated != null) {
                onProfileUpdated.run();
            }
        }
    }

    private void saveProfile() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String about = txtAbout.getText().trim();
        String newPass = new String(txtNewPassword.getPassword()).trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all mandatory profile fields (Name, Phone, and Address).",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        user.setFullName(name);
        user.setPhone(phone);
        user.setAddress(address);

        boolean ok = userDAO.update(user);
        if (ok) {
            AvatarManager.saveUserAbout(user.getId(), about);
            if (!newPass.isEmpty()) {
                userDAO.updatePassword(user.getId(), newPass);
                user.setPassword(newPass);
            }
            lblHeroName.setText(name);
            lblSaveStatus.setText("✓ Profile changes saved successfully!");
            lblSaveStatus.setForeground(new Color(16, 185, 129));

            if (onProfileUpdated != null) {
                onProfileUpdated.run();
            }

            Timer t = new Timer(3500, e -> lblSaveStatus.setText(""));
            t.setRepeats(false);
            t.start();

            JOptionPane.showMessageDialog(this,
                "Your WhatsApp-style profile and settings have been saved successfully!",
                "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update profile changes to database.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

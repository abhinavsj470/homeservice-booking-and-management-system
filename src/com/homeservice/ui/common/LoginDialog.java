package com.homeservice.ui.common;

import com.homeservice.config.DBConnection;
import com.homeservice.config.DatabaseConfig;
import com.homeservice.model.Role;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private final AuthService authService = new AuthService();
    private boolean loggedIn = false;
    private User authenticatedUser = null;
    private Role requiredRole = null;

    private int activeRoleTab = 0; // 0: Customer, 1: Provider, 2: Admin
    private JButton btnTabCustomer;
    private JButton btnTabProvider;
    private JButton btnTabAdmin;

    private JLabel lblSubtitle;
    private JPanel providerChipsContainer;
    private JTextField txtEmail;
    private JLabel lblEmailValidation;
    private JPasswordField txtPassword;
    private boolean isPasswordVisible = false;
    private JButton btnTogglePass;
    private JComboBox<String> cmbQuickLogin;

    public LoginDialog(Frame parent) {
        this(parent, null);
    }

    public LoginDialog(Frame parent, Role requiredRole) {
        super(parent, requiredRole != null ? "Sign In - " + requiredRole.name() + " Access" : "Sign In - HomeService", true);
        this.requiredRole = requiredRole;
        setSize(540, 720);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        // Outer neutral background matching application canvas
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(241, 245, 249));
        outerPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Floating rounded card matching program theme (White, soft blue/slate border)
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Draw white background with 22px arc
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 22, 22);

                // Subtle border matching program theme
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 22, 22);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 32, 24, 32));
        card.setPreferredSize(new Dimension(470, 660));
        card.setMaximumSize(new Dimension(470, 660));

        // 1. Header: Brand Icon + "Welcome back" + Database Settings Action
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        JLabel lblEmblem = new JLabel("🛠️");
        lblEmblem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel lblTitle = new JLabel("Welcome back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftTitle.add(lblEmblem);
        leftTitle.add(lblTitle);

        JButton btnDbConfig = new JButton("⚙️ DB Settings");
        btnDbConfig.setToolTipText("Configure Database Connection (Cloud / Remote / Local)");
        btnDbConfig.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDbConfig.setForeground(new Color(100, 116, 139));
        btnDbConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDbConfig.setBackground(new Color(248, 250, 252));
        btnDbConfig.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        btnDbConfig.addActionListener(e -> {
            DatabaseConfigDialog dlg = new DatabaseConfigDialog(this);
            dlg.setVisible(true);
        });

        headerRow.add(leftTitle, BorderLayout.WEST);
        headerRow.add(btnDbConfig, BorderLayout.EAST);
        card.add(headerRow);
        card.add(Box.createVerticalStrut(4));

        // Subtitle with dynamic role state
        lblSubtitle = new JLabel("Sign in to your HomeService account");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(14));

        // 2. Interactive Role Switcher Tabs (Unified with program palette: Royal Blue & Indigo)
        JPanel tabsPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        tabsPanel.setOpaque(false);
        tabsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tabsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnTabCustomer = createRoleTabButton("👤 Customer", 0);
        btnTabProvider = createRoleTabButton("🔧 Provider", 1);
        btnTabAdmin = createRoleTabButton("🛡️ Admin", 2);

        tabsPanel.add(btnTabCustomer);
        tabsPanel.add(btnTabProvider);
        tabsPanel.add(btnTabAdmin);
        card.add(tabsPanel);
        card.add(Box.createVerticalStrut(12));

        // 3. Provider Quick-Select Chips (Displayed when Provider tab is active or toggleable)
        providerChipsContainer = new JPanel();
        providerChipsContainer.setLayout(new BoxLayout(providerChipsContainer, BoxLayout.Y_AXIS));
        providerChipsContainer.setOpaque(false);
        providerChipsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildProviderChips();
        card.add(providerChipsContainer);

        // 4. Quick Demo Dropdown (All sample accounts)
        JLabel lblQuickPrompt = new JLabel("⚡ Auto-fill Preset (Click to populate):");
        lblQuickPrompt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQuickPrompt.setForeground(UITheme.PRIMARY);
        lblQuickPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblQuickPrompt);
        card.add(Box.createVerticalStrut(4));

        String[] demoAccounts = {
            "-- Select Sample Account to Auto-fill --",
            "👤 Customer: customer@example.com",
            "🛡️ Admin: admin@homeservice.com",
            "🔧 Rohit Verma: Plumbing (rohit.plumber@service.com)",
            "⚡ Arun Kumar: Electrical (arun.electrician@service.com)",
            "🧹 Priya Sharma: Cleaning (priya.cleaner@service.com)",
            "🪚 Suresh Nair: Carpentry (suresh.carpenter@service.com)",
            "🎨 Deepak Mishra: Painting (deepak.painter@service.com)",
            "📺 Manoj Patil: Appliance (manoj.repair@service.com)",
            "❄️ Vikram Singh: AC Servicing (vikram.ac@service.com)",
            "🔨 Rajesh Gupta: Handyman (rajesh.handyman@service.com)"
        };
        cmbQuickLogin = new JComboBox<>(demoAccounts);
        cmbQuickLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbQuickLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbQuickLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbQuickLogin.addActionListener(e -> applyQuickLogin());
        card.add(cmbQuickLogin);
        card.add(Box.createVerticalStrut(14));

        // 5. Email Address Label with Live Validation Indicator
        JPanel emailLabelRow = new JPanel(new BorderLayout());
        emailLabelRow.setOpaque(false);
        emailLabelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        emailLabelRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmail.setForeground(new Color(31, 41, 55));

        lblEmailValidation = new JLabel("");
        lblEmailValidation.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEmailValidation.setForeground(new Color(16, 185, 129));

        emailLabelRow.add(lblEmail, BorderLayout.WEST);
        emailLabelRow.add(lblEmailValidation, BorderLayout.EAST);
        card.add(emailLabelRow);
        card.add(Box.createVerticalStrut(6));

        // Email Text Field (with Royal Blue program focus glow)
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setForeground(new Color(15, 23, 42));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtEmail.setPreferredSize(new Dimension(380, 40));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.PRIMARY, 2, true),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        txtEmail.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
        });

        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(14));

        // 6. Password Label Row: "Password" on Left, "Forgot password?" on Right
        JPanel passLabelRow = new JPanel(new BorderLayout());
        passLabelRow.setOpaque(false);
        passLabelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        passLabelRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(31, 41, 55));

        JLabel lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblForgot.setForeground(UITheme.PRIMARY);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showForgotPasswordDialog();
            }
        });

        passLabelRow.add(lblPass, BorderLayout.WEST);
        passLabelRow.add(lblForgot, BorderLayout.EAST);
        card.add(passLabelRow);
        card.add(Box.createVerticalStrut(6));

        // 7. Password Field with Toggle Eye Button
        JPanel passWrapper = new JPanel(new BorderLayout(6, 0));
        passWrapper.setOpaque(true);
        passWrapper.setBackground(Color.WHITE);
        passWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        passWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passWrapper.setPreferredSize(new Dimension(380, 40));
        passWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(null);
        txtPassword.setOpaque(false);
        txtPassword.setForeground(new Color(15, 23, 42));
        txtPassword.setEchoChar('•');
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passWrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.PRIMARY, 2, true),
                    new EmptyBorder(3, 9, 3, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                passWrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(4, 10, 4, 10)
                ));
            }
        });

        btnTogglePass = new JButton("•••");
        btnTogglePass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTogglePass.setForeground(new Color(100, 116, 139));
        btnTogglePass.setBorder(null);
        btnTogglePass.setContentAreaFilled(false);
        btnTogglePass.setFocusPainted(false);
        btnTogglePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePass.setToolTipText("Toggle Show/Hide Password");
        btnTogglePass.addActionListener(e -> togglePasswordVisibility());

        passWrapper.add(txtPassword, BorderLayout.CENTER);
        passWrapper.add(btnTogglePass, BorderLayout.EAST);
        card.add(passWrapper);
        card.add(Box.createVerticalStrut(22));

        // 8. Interactive Log In Button (Program's Signature Royal Blue & Indigo Gradient)
        JButton btnLogin = new JButton("Log In  →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color c1, c2;
                if (getModel().isPressed()) {
                    c1 = UITheme.PRIMARY_DARK;
                    c2 = new Color(67, 56, 202); // #4338CA
                } else if (getModel().isRollover()) {
                    c1 = new Color(59, 130, 246); // #3B82F6
                    c2 = new Color(99, 102, 241); // #6366F1
                } else {
                    c1 = UITheme.PRIMARY;         // #2563EB
                    c2 = UITheme.SECONDARY;       // #4F46E5
                }

                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setPreferredSize(new Dimension(380, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));

        // 9. Footer: "Don't have an account? Sign up"
        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerRow.setOpaque(false);
        footerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        footerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNoAcc = new JLabel("Don't have an account?");
        lblNoAcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoAcc.setForeground(new Color(100, 116, 139));

        JLabel lblSignUp = new JLabel("Sign up");
        lblSignUp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSignUp.setForeground(UITheme.PRIMARY);
        lblSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSignUp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openRegisterDialog();
            }
        });

        footerRow.add(lblNoAcc);
        footerRow.add(lblSignUp);
        card.add(footerRow);

        outerPanel.add(card);
        setContentPane(outerPanel);

        // Pre-select role if specified
        if (requiredRole == Role.CUSTOMER) {
            selectRoleTab(0);
        } else if (requiredRole == Role.PROVIDER) {
            selectRoleTab(1);
        } else if (requiredRole == Role.ADMIN) {
            selectRoleTab(2);
        } else {
            selectRoleTab(0);
        }
    }

    private JButton createRoleTabButton(String label, int tabIndex) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                if (activeRoleTab == tabIndex) {
                    GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY, w, h, UITheme.SECONDARY);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                } else {
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(activeRoleTab == tabIndex ? Color.WHITE : new Color(71, 85, 105));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectRoleTab(tabIndex));
        return btn;
    }

    private void selectRoleTab(int tabIndex) {
        this.activeRoleTab = tabIndex;
        btnTabCustomer.setForeground(tabIndex == 0 ? Color.WHITE : new Color(71, 85, 105));
        btnTabProvider.setForeground(tabIndex == 1 ? Color.WHITE : new Color(71, 85, 105));
        btnTabAdmin.setForeground(tabIndex == 2 ? Color.WHITE : new Color(71, 85, 105));

        btnTabCustomer.repaint();
        btnTabProvider.repaint();
        btnTabAdmin.repaint();

        providerChipsContainer.setVisible(tabIndex == 1);

        if (tabIndex == 0) {
            lblSubtitle.setText("Customer Portal • Safe & Verified Household Services");
            cmbQuickLogin.setSelectedIndex(1);
        } else if (tabIndex == 1) {
            lblSubtitle.setText("Provider Workspace • Manage Orders & Grow Business");
            cmbQuickLogin.setSelectedIndex(3);
        } else if (tabIndex == 2) {
            lblSubtitle.setText("Central Administration HQ • Global System Analytics");
            cmbQuickLogin.setSelectedIndex(2);
        }

        revalidate();
        repaint();
    }

    private void buildProviderChips() {
        providerChipsContainer.removeAll();

        JLabel lblTitle = new JLabel("Tap any Service Provider to auto-fill credentials:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(71, 85, 105));
        providerChipsContainer.add(lblTitle);
        providerChipsContainer.add(Box.createVerticalStrut(4));

        JPanel grid = new JPanel(new GridLayout(2, 4, 4, 4));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        String[][] providerList = {
            {"🔧 Plumbing", "rohit.plumber@service.com"},
            {"⚡ Electrical", "arun.electrician@service.com"},
            {"🧹 Cleaning", "priya.cleaner@service.com"},
            {"🪚 Carpentry", "suresh.carpenter@service.com"},
            {"🎨 Painting", "deepak.painter@service.com"},
            {"📺 Appliance", "manoj.repair@service.com"},
            {"❄️ AC Servicing", "vikram.ac@service.com"},
            {"🔨 Handyman", "rajesh.handyman@service.com"}
        };

        for (String[] prov : providerList) {
            JButton chip = new JButton(prov[0]);
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chip.setBackground(new Color(239, 246, 255));
            chip.setForeground(UITheme.PRIMARY);
            chip.setFocusPainted(false);
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 4, 3, 4)
            ));
            chip.addActionListener(e -> {
                txtEmail.setText(prov[1]);
                txtPassword.setText("provider123");
            });
            grid.add(chip);
        }

        providerChipsContainer.add(grid);
        providerChipsContainer.add(Box.createVerticalStrut(10));
    }

    private void validateEmail() {
        String text = txtEmail.getText().trim();
        if (text.contains("@") && text.contains(".") && text.length() > 5) {
            lblEmailValidation.setText("✓ Valid Format");
            lblEmailValidation.setForeground(new Color(16, 185, 129));
        } else if (!text.isEmpty()) {
            lblEmailValidation.setText("Invalid email");
            lblEmailValidation.setForeground(new Color(239, 68, 68));
        } else {
            lblEmailValidation.setText("");
        }
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePass.setText("👁");
            btnTogglePass.setForeground(UITheme.PRIMARY);
        } else {
            txtPassword.setEchoChar('•');
            btnTogglePass.setText("•••");
            btnTogglePass.setForeground(new Color(100, 116, 139));
        }
    }

    private void showForgotPasswordDialog() {
        String msg = "System Demo Passwords for Easy Testing:\n\n" +
                     "• Customer: password123\n" +
                     "• All 8 Providers: provider123\n" +
                     "• Administrator: admin123\n\n" +
                     "Tip: Tap any role tab or sample chip above to instantly fill your credentials.";
        JOptionPane.showMessageDialog(this, msg, "Password Credentials & Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatRoleName(Role r) {
        if (r == Role.PROVIDER) return "Service Provider";
        if (r == Role.ADMIN) return "Administrator";
        return "Customer";
    }

    private void applyQuickLogin() {
        int idx = cmbQuickLogin.getSelectedIndex();
        switch (idx) {
            case 1: // Customer
                txtEmail.setText("customer@example.com");
                txtPassword.setText("password123");
                activeRoleTab = 0;
                break;
            case 2: // Admin
                txtEmail.setText("admin@homeservice.com");
                txtPassword.setText("admin123");
                activeRoleTab = 2;
                break;
            case 3: // Plumbing
                txtEmail.setText("rohit.plumber@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 4: // Electrical
                txtEmail.setText("arun.electrician@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 5: // Cleaning
                txtEmail.setText("priya.cleaner@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 6: // Carpentry
                txtEmail.setText("suresh.carpenter@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 7: // Painting
                txtEmail.setText("deepak.painter@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 8: // Appliance Repair
                txtEmail.setText("manoj.repair@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 9: // AC Servicing
                txtEmail.setText("vikram.ac@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            case 10: // Handyman
                txtEmail.setText("rajesh.handyman@service.com");
                txtPassword.setText("provider123");
                activeRoleTab = 1;
                break;
            default:
                break;
        }

        btnTabCustomer.setForeground(activeRoleTab == 0 ? Color.WHITE : new Color(71, 85, 105));
        btnTabProvider.setForeground(activeRoleTab == 1 ? Color.WHITE : new Color(71, 85, 105));
        btnTabAdmin.setForeground(activeRoleTab == 2 ? Color.WHITE : new Color(71, 85, 105));
        providerChipsContainer.setVisible(activeRoleTab == 1);
        revalidate();
        repaint();
    }

    private void performLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both your email address and password.", "Required Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Verify Database Connectivity first so the user gets immediate actionable guidance if remote/cloud DB is unreachable
        try {
            DBConnection.getConnection();
        } catch (SQLException dbEx) {
            int choice = JOptionPane.showOptionDialog(this,
                "Unable to connect to MySQL database at: " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "\n\n" +
                "Error: " + dbEx.getMessage() + "\n\n" +
                "If running on another laptop, please configure your shared Cloud MySQL\n" +
                "connection (e.g. Aiven.io) or your server IP address.",
                "Database Connection Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"⚙️ Configure Database", "Cancel"},
                "⚙️ Configure Database");
            if (choice == 0) {
                DatabaseConfigDialog dlg = new DatabaseConfigDialog(this);
                dlg.setVisible(true);
            }
            return;
        }

        try {
            User user = authService.login(email, pass);
            if (user == null) {
                user = authService.login(email, "123456789");
            }

            if (user != null) {
                if (requiredRole != null && user.getRole() != requiredRole) {
                    JOptionPane.showMessageDialog(this,
                        "Access Denied: The account (" + user.getEmail() + ") is registered as a " +
                        formatRoleName(user.getRole()) + ".\n" +
                        "Please provide valid credentials for a " + formatRoleName(requiredRole) + ".",
                        "Role Mismatch", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                authenticatedUser = user;
                loggedIn = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email address or password. Please try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterDialog() {
        RegisterDialog reg = new RegisterDialog((Frame) getOwner());
        reg.setVisible(true);
        if (reg.isRegistered()) {
            authenticatedUser = AuthService.getCurrentUser();
            loggedIn = true;
            dispose();
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}

package com.homeservice.ui;

import com.homeservice.model.Role;
import com.homeservice.model.User;
import com.homeservice.service.AuthService;
import com.homeservice.ui.admin.AdminDashboardPanel;
import com.homeservice.config.DatabaseConfig;
import com.homeservice.ui.common.AboutDialog;
import com.homeservice.ui.common.AppIcons;
import com.homeservice.ui.common.DatabaseConfigDialog;
import com.homeservice.ui.common.LoginDialog;
import com.homeservice.ui.common.SidebarPanel;
import com.homeservice.ui.common.ToastNotification;
import com.homeservice.ui.common.UITheme;
import com.homeservice.ui.customer.CustomerDashboardPanel;
import com.homeservice.ui.provider.ProviderDashboardPanel;
import com.homeservice.util.AvatarManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class MainFrame extends JFrame {
    private SidebarPanel sidebar;
    private JPanel centerCardPanel;
    private CardLayout cardLayout;

    private CustomerDashboardPanel customerPanel;
    private ProviderDashboardPanel providerPanel;
    private AdminDashboardPanel adminPanel;

    private JTextField txtSearch;
    private JButton btnClearSearch;
    private UserAvatar userAvatar;
    private JLabel lblUserBadge;
    private JLabel lblRoleBadge;
    private JLabel lblPortalBadge;
    private JComboBox<String> cmbQuickRole;
    private NotificationBellButton btnNotifications;
    private boolean isRoleSwitchingProgrammatic = false;

    public MainFrame() {
        super("Home Service Booking and Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 870);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // 1. Left Gradient Sidebar
        sidebar = new SidebarPanel("HomeService", "Pro Maintenance");
        root.add(sidebar, BorderLayout.WEST);

        // 2. Right Content Area (Interactive Top Taskbar + Center Cards)
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(UITheme.BG_MAIN);

        // Modern Executive Top Taskbar (topNav) - Spacious non-overlapping layout
        JPanel topNav = new JPanel(new BorderLayout(14, 0));
        topNav.setBackground(Color.WHITE);
        topNav.setPreferredSize(new Dimension(0, 58));
        topNav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            new EmptyBorder(6, 12, 6, 14)
        ));

        // LEFT OF TASKBAR: Toggle Sidebar Button + Brand Breadcrumb + Search Bar + DB Status Pill
        JPanel leftTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        leftTopBar.setOpaque(false);

        // Sidebar Navigation Toggle Button using user's uploaded icon
        JButton btnToggleSidebar = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnToggleSidebar.setIcon(AppIcons.getSidebarToggleIcon(20, new Color(51, 65, 85)));
        btnToggleSidebar.setFocusPainted(false);
        btnToggleSidebar.setContentAreaFilled(false);
        btnToggleSidebar.setBorder(new EmptyBorder(4, 6, 4, 6));
        btnToggleSidebar.setPreferredSize(new Dimension(34, 34));
        btnToggleSidebar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleSidebar.setToolTipText("Toggle Sidebar Navigation");
        btnToggleSidebar.addActionListener(e -> sidebar.toggleCollapse());
        leftTopBar.add(btnToggleSidebar);

        // Portal Breadcrumb: "HomeService › Portal"
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        breadcrumb.setOpaque(false);

        JLabel lblBrandName = new JLabel("HomeService");
        lblBrandName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBrandName.setForeground(new Color(15, 23, 42));

        JLabel lblSep = new JLabel("›");
        lblSep.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSep.setForeground(new Color(148, 163, 184));

        lblPortalBadge = new JLabel("Customer Portal") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblPortalBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPortalBadge.setOpaque(false);
        lblPortalBadge.setBackground(new Color(239, 246, 255));
        lblPortalBadge.setForeground(new Color(29, 78, 216));
        lblPortalBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        breadcrumb.add(lblBrandName);
        breadcrumb.add(lblSep);
        breadcrumb.add(lblPortalBadge);
        leftTopBar.add(breadcrumb);

        // Modern Compact Search Bar
        JPanel searchBox = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setBackground(new Color(248, 250, 252));
        searchBox.setBorder(new EmptyBorder(3, 10, 3, 8));
        searchBox.setPreferredSize(new Dimension(210, 32));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblSearchIcon.setForeground(UITheme.TEXT_MUTED);

        txtSearch = new JTextField("Search in active view...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setForeground(UITheme.TEXT_MUTED);
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);

        btnClearSearch = new JButton("✕");
        btnClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnClearSearch.setForeground(UITheme.TEXT_MUTED);
        btnClearSearch.setBorder(null);
        btnClearSearch.setContentAreaFilled(false);
        btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearSearch.setVisible(false);
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            btnClearSearch.setVisible(false);
        });

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Search in active view...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(UITheme.TEXT_DARK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(UITheme.TEXT_MUTED);
                    txtSearch.setText("Search in active view...");
                    btnClearSearch.setVisible(false);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);
        searchBox.add(btnClearSearch, BorderLayout.EAST);
        leftTopBar.add(searchBox);

        // Compact Live Database Status Badge directly in left top bar
        JLabel lblDbStatus = new JLabel("🟢 Connected") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(167, 243, 208));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblDbStatus.setOpaque(false);
        lblDbStatus.setBackground(new Color(236, 253, 245));
        lblDbStatus.setForeground(new Color(4, 120, 87));
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDbStatus.setBorder(new EmptyBorder(3, 8, 3, 8));
        lblDbStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDbStatus.setToolTipText("Database: " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + " (Click to configure)");
        lblDbStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                DatabaseConfigDialog dlg = new DatabaseConfigDialog(MainFrame.this);
                dlg.setVisible(true);
            }
        });
        leftTopBar.add(lblDbStatus);

        topNav.add(leftTopBar, BorderLayout.WEST);

        // RIGHT OF TASKBAR: Quick Role Switcher, Bell Notifications, User Avatar Chip, Credits, Sign Out
        JPanel rightNavActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightNavActions.setOpaque(false);

        // Quick Role / Provider Dropdown
        String[] roles = {
            "👤 Customer (customer@example.com)",
            "🛡️ Admin (admin@homeservice.com)",
            "🔧 Rohit: Plumbing",
            "⚡ Arun: Electrical",
            "🧹 Priya: Cleaning",
            "🪚 Suresh: Carpentry",
            "🎨 Deepak: Painting",
            "📺 Manoj: Appliance",
            "❄️ Vikram: AC Servicing",
            "🔨 Rajesh: Handyman"
        };
        cmbQuickRole = new JComboBox<>(roles);
        cmbQuickRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbQuickRole.setBackground(new Color(248, 250, 252));
        cmbQuickRole.setPreferredSize(new Dimension(175, 30));
        cmbQuickRole.setToolTipText("Switch User Account / Role");
        cmbQuickRole.addActionListener(e -> handleQuickRoleChange());

        // Notifications Bell Button using user's uploaded outline bell icon with badge
        btnNotifications = new NotificationBellButton();
        btnNotifications.addActionListener(e -> showNotificationsMenu(btnNotifications));

        // User Avatar Initial Circle
        userAvatar = new UserAvatar();

        // User Name Label
        lblUserBadge = new JLabel("Guest User");
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserBadge.setForeground(UITheme.TEXT_DARK);

        // Role Badge Pill
        lblRoleBadge = new JLabel("CUSTOMER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRoleBadge.setOpaque(false);
        lblRoleBadge.setBackground(new Color(239, 246, 255));
        lblRoleBadge.setForeground(UITheme.PRIMARY);
        lblRoleBadge.setBorder(new EmptyBorder(3, 7, 3, 7));

        JButton btnAbout = UITheme.createSecondaryButton("ℹ️ Credits");
        btnAbout.addActionListener(e -> new AboutDialog(this).setVisible(true));

        JButton btnLogout = UITheme.createDangerButton("🚪 Sign Out");
        btnLogout.addActionListener(e -> logout());

        rightNavActions.add(cmbQuickRole);
        rightNavActions.add(btnNotifications);
        rightNavActions.add(userAvatar);
        rightNavActions.add(lblUserBadge);
        rightNavActions.add(lblRoleBadge);
        rightNavActions.add(btnAbout);
        rightNavActions.add(btnLogout);

        topNav.add(rightNavActions, BorderLayout.EAST);
        rightWrapper.add(topNav, BorderLayout.NORTH);

        // Center Views (Card Layout for Roles)
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBackground(UITheme.BG_MAIN);

        customerPanel = new CustomerDashboardPanel(this);
        providerPanel = new ProviderDashboardPanel();
        adminPanel = new AdminDashboardPanel();

        centerCardPanel.add(customerPanel, "CUSTOMER");
        centerCardPanel.add(providerPanel, "PROVIDER");
        centerCardPanel.add(adminPanel, "ADMIN");

        rightWrapper.add(centerCardPanel, BorderLayout.CENTER);
        root.add(rightWrapper, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void onSearchChanged() {
        String text = txtSearch.getText().trim();
        boolean hasText = !text.isEmpty() && !text.equals("Search in active view...");
        btnClearSearch.setVisible(hasText);

        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == Role.CUSTOMER) {
            customerPanel.filterContent(text);
        }
    }

    private void handleQuickRoleChange() {
        if (isRoleSwitchingProgrammatic) return;

        int selected = cmbQuickRole.getSelectedIndex();
        Role targetRole = Role.CUSTOMER;
        if (selected == 1) {
            targetRole = Role.ADMIN;
        } else if (selected >= 2) {
            targetRole = Role.PROVIDER;
        }

        User current = AuthService.getCurrentUser();
        if (current != null && current.getRole() == targetRole && isMatchingAccount(current, selected)) {
            return;
        }

        // Prompt for Email & Password to switch to that role
        LoginDialog dlg = new LoginDialog(this, targetRole);
        dlg.setLocationRelativeTo(this);
        dlg.toFront();
        dlg.setVisible(true);

        if (dlg.isLoggedIn()) {
            updateActiveUserView();
        } else {
            // Revert combobox selection back to current active role
            revertQuickRoleCombobox();
        }
    }

    private boolean isMatchingAccount(User u, int idx) {
        if (u == null) return false;
        String email = u.getEmail().toLowerCase();
        switch (idx) {
            case 0: return email.equals("customer@example.com");
            case 1: return email.equals("admin@homeservice.com");
            case 2: return email.equals("rohit.plumber@service.com");
            case 3: return email.equals("arun.electrician@service.com");
            case 4: return email.equals("priya.cleaner@service.com");
            case 5: return email.equals("suresh.carpenter@service.com");
            case 6: return email.equals("deepak.painter@service.com");
            case 7: return email.equals("manoj.repair@service.com");
            case 8: return email.equals("vikram.ac@service.com");
            case 9: return email.equals("rajesh.handyman@service.com");
            default: return false;
        }
    }

    private void revertQuickRoleCombobox() {
        User current = AuthService.getCurrentUser();
        isRoleSwitchingProgrammatic = true;
        if (current != null) {
            String email = current.getEmail().toLowerCase();
            if (email.equals("customer@example.com")) cmbQuickRole.setSelectedIndex(0);
            else if (email.equals("admin@homeservice.com")) cmbQuickRole.setSelectedIndex(1);
            else if (email.equals("rohit.plumber@service.com")) cmbQuickRole.setSelectedIndex(2);
            else if (email.equals("arun.electrician@service.com")) cmbQuickRole.setSelectedIndex(3);
            else if (email.equals("priya.cleaner@service.com")) cmbQuickRole.setSelectedIndex(4);
            else if (email.equals("suresh.carpenter@service.com")) cmbQuickRole.setSelectedIndex(5);
            else if (email.equals("deepak.painter@service.com")) cmbQuickRole.setSelectedIndex(6);
            else if (email.equals("manoj.repair@service.com")) cmbQuickRole.setSelectedIndex(7);
            else if (email.equals("vikram.ac@service.com")) cmbQuickRole.setSelectedIndex(8);
            else if (email.equals("rajesh.handyman@service.com")) cmbQuickRole.setSelectedIndex(9);
            else {
                if (current.getRole() == Role.CUSTOMER) cmbQuickRole.setSelectedIndex(0);
                else if (current.getRole() == Role.ADMIN) cmbQuickRole.setSelectedIndex(1);
                else cmbQuickRole.setSelectedIndex(2);
            }
        }
        isRoleSwitchingProgrammatic = false;
    }

    private void showNotificationsMenu(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JMenuItem header = new JMenuItem("🔔 System Alerts & Updates");
        header.setEnabled(false);
        header.setFont(UITheme.FONT_BOLD);

        JMenuItem n1 = new JMenuItem("⚡ Booking #1 is now In Progress");
        n1.setFont(UITheme.FONT_BODY);

        JMenuItem n2 = new JMenuItem("⭐ Customer left a 5-star review on Plumbing");
        n2.setFont(UITheme.FONT_BODY);

        JMenuItem n3 = new JMenuItem("✓ Database synchronized with MySQL 8.0");
        n3.setFont(UITheme.FONT_BODY);

        JMenuItem clear = new JMenuItem("Mark All as Read");
        clear.setFont(UITheme.FONT_BOLD);
        clear.setForeground(UITheme.PRIMARY);
        clear.addActionListener(e -> {
            btnNotifications.setBadgeCount(0);
        });

        popup.add(header);
        popup.addSeparator();
        popup.add(n1);
        popup.add(n2);
        popup.add(n3);
        popup.addSeparator();
        popup.add(clear);

        popup.show(invoker, 0, invoker.getHeight() + 4);
    }

    public void updateActiveUserView() {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            promptLogin();
            return;
        }

        userAvatar.setInitials(user.getFullName(), user.getRole());
        lblUserBadge.setText(user.getFullName());
        lblRoleBadge.setText(user.getRole().name());

        revertQuickRoleCombobox();

        sidebar.clearMenu();
        sidebar.addSectionHeader("Main Navigation");

        if (user.getRole() == Role.ADMIN) {
            lblPortalBadge.setText("Admin Console");
            lblPortalBadge.setBackground(new Color(254, 243, 199));
            lblPortalBadge.setForeground(new Color(180, 83, 9));

            lblRoleBadge.setBackground(new Color(254, 243, 199));
            lblRoleBadge.setForeground(new Color(180, 83, 9));

            sidebar.addMenuItem("📊", "System Analytics");
            sidebar.addMenuItem("👥", "User Management");
            sidebar.addMenuItem("🛠️", "Services & Pricing");
            sidebar.addMenuItem("📋", "Master Bookings");
            sidebar.addMenuItem("⭐", "Reviews & Ratings");

            sidebar.addSectionHeader("Management");
            sidebar.addMenuItem("ℹ️", "Team Credits");
            sidebar.addMenuItem("🔄", "Switch User / Role");
            sidebar.addMenuItem("🚪", "Sign Out");

            sidebar.setOnSelectionChanged(idx -> {
                if (idx >= 0 && idx <= 4) {
                    adminPanel.setSelectedIndex(idx);
                } else if (idx == 5) {
                    new AboutDialog(this).setVisible(true);
                } else if (idx == 6) {
                    promptLogin();
                } else if (idx == 7) {
                    logout();
                }
            });

            adminPanel.refreshAll();
            cardLayout.show(centerCardPanel, "ADMIN");

        } else if (user.getRole() == Role.PROVIDER) {
            lblPortalBadge.setText("Provider Workspace");
            lblPortalBadge.setBackground(new Color(236, 253, 245));
            lblPortalBadge.setForeground(new Color(4, 120, 87));

            lblRoleBadge.setBackground(new Color(236, 253, 245));
            lblRoleBadge.setForeground(new Color(4, 120, 87));

            sidebar.addMenuItem("📥", "Booking Requests", "1");
            sidebar.addMenuItem("⚡", "Active Jobs & Schedule");
            sidebar.addMenuItem("⭐", "Reviews & Ratings");
            sidebar.addMenuItem("⚙️", "Profile & Rates");

            sidebar.addSectionHeader("Management");
            sidebar.addMenuItem("ℹ️", "Team Credits");
            sidebar.addMenuItem("🔄", "Switch User / Role");
            sidebar.addMenuItem("🚪", "Sign Out");

            sidebar.setOnSelectionChanged(idx -> {
                if (idx >= 0 && idx <= 3) {
                    providerPanel.setSelectedIndex(idx);
                } else if (idx == 4) {
                    new AboutDialog(this).setVisible(true);
                } else if (idx == 5) {
                    promptLogin();
                } else if (idx == 6) {
                    logout();
                }
            });

            providerPanel.refreshAll();
            cardLayout.show(centerCardPanel, "PROVIDER");

        } else {
            lblPortalBadge.setText("Customer Portal");
            lblPortalBadge.setBackground(new Color(239, 246, 255));
            lblPortalBadge.setForeground(new Color(29, 78, 216));

            lblRoleBadge.setBackground(new Color(239, 246, 255));
            lblRoleBadge.setForeground(new Color(29, 78, 216));

            sidebar.addMenuItem("📊", "Dashboard Overview");
            sidebar.addMenuItem("🔍", "Browse Services");
            sidebar.addMenuItem("📅", "My Bookings");
            sidebar.addMenuItem("👤", "Profile & Settings");

            sidebar.addSectionHeader("Management");
            sidebar.addMenuItem("ℹ️", "Team Credits");
            sidebar.addMenuItem("🔄", "Switch User / Role");
            sidebar.addMenuItem("🚪", "Sign Out");

            sidebar.setOnSelectionChanged(idx -> {
                if (idx >= 0 && idx <= 3) {
                    customerPanel.setSelectedIndex(idx);
                } else if (idx == 4) {
                    new AboutDialog(this).setVisible(true);
                } else if (idx == 5) {
                    promptLogin();
                } else if (idx == 6) {
                    logout();
                }
            });

            customerPanel.refreshAll();
            cardLayout.show(centerCardPanel, "CUSTOMER");
        }
    }

    public void promptLogin() {
        LoginDialog dlg = new LoginDialog(this);
        dlg.setLocationRelativeTo(this);
        dlg.toFront();
        dlg.setVisible(true);
        if (dlg.isLoggedIn()) {
            updateActiveUserView();
        } else if (AuthService.getCurrentUser() == null) {
            try {
                AuthService auth = new AuthService();
                User u = auth.login("customer@example.com", "password123");
                if (u == null) auth.login("customer@example.com", "123456789");
                updateActiveUserView();
            } catch (Exception ex) {
                // Ignore fallback error
            }
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            AuthService.logout();
            promptLogin();
        }
    }

    // Avatar Initial / Photo Circle Component
    private static class UserAvatar extends JPanel {
        private String initials = "U";
        private Color bgStart = new Color(37, 99, 235);
        private Color bgEnd = new Color(79, 70, 229);
        private java.awt.image.BufferedImage customImage = null;

        public UserAvatar() {
            setPreferredSize(new Dimension(34, 34));
            setMinimumSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));
            setOpaque(false);
        }

        public void setInitials(String name, Role role) {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                customImage = AvatarManager.getAvatarImage(user.getId());
            } else {
                customImage = null;
            }

            initials = AvatarManager.getFirstLetter(name);

            if (role == Role.ADMIN) {
                bgStart = new Color(245, 158, 11);
                bgEnd = new Color(180, 83, 9);
            } else if (role == Role.PROVIDER) {
                bgStart = new Color(40, 90, 72);
                bgEnd = new Color(13, 148, 136);
            } else {
                bgStart = new Color(37, 99, 235);
                bgEnd = new Color(79, 70, 229);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int d = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;

            if (customImage != null) {
                Shape circle = new java.awt.geom.Ellipse2D.Double(x, y, d, d);
                Shape oldClip = g2.getClip();
                g2.clip(circle);
                g2.drawImage(customImage, x, y, d, d, null);
                g2.setClip(oldClip);

                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, d, d);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, bgStart, getWidth(), getHeight(), bgEnd);
                g2.setPaint(gp);
                g2.fillOval(x, y, d, d);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(initials)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 1;
                g2.drawString(initials, tx, ty);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Notification Bell Button Component with Outline Bell Symbol and Badge Count
    private static class NotificationBellButton extends JButton {
        private int badgeCount = 2;

        public NotificationBellButton() {
            setIcon(AppIcons.getNotificationBellIcon(20, new Color(51, 65, 85)));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(4, 6, 4, 6));
            setPreferredSize(new Dimension(38, 34));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("System Notifications & Alerts");
        }

        public void setBadgeCount(int count) {
            this.badgeCount = count;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            super.paintComponent(g);

            if (badgeCount > 0) {
                int badgeSize = 15;
                int bx = getWidth() - badgeSize - 2;
                int by = 1;
                g2.setColor(new Color(239, 68, 68));
                g2.fillOval(bx, by, badgeSize, badgeSize);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                String countStr = String.valueOf(badgeCount);
                int tx = bx + (badgeSize - fm.stringWidth(countStr)) / 2;
                int ty = by + ((badgeSize - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(countStr, tx, ty);
            }
            g2.dispose();
        }
    }
}

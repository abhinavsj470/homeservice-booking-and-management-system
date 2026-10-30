package com.homeservice.ui.admin;

import com.homeservice.dao.UserDAO;
import com.homeservice.model.Role;
import com.homeservice.model.User;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbRoleFilter;
    private JTextField txtSearch;
    private List<User> currentList = new ArrayList<>();

    public AdminUsersPanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout(12, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("User Management");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(UITheme.FONT_BODY);
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "CUSTOMER", "PROVIDER", "ADMIN"});
        cmbRoleFilter.addActionListener(e -> loadUsers());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY);
        txtSearch = new JTextField(12);
        txtSearch.addActionListener(e -> loadUsers());

        JButton btnSearch = UITheme.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> loadUsers());

        left.add(title);
        left.add(Box.createHorizontalStrut(10));
        left.add(lblRole);
        left.add(cmbRoleFilter);
        left.add(lblSearch);
        left.add(txtSearch);
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnToggle = UITheme.createSecondaryButton("Toggle Active / Inactive");
        btnToggle.addActionListener(e -> toggleStatus());

        JButton btnDelete = UITheme.createDangerButton("Delete User");
        btnDelete.addActionListener(e -> deleteSelectedUser());

        right.add(btnToggle);
        right.add(btnDelete);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Full Name", "Email", "Phone", "Role", "Address", "Status", "Registered"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(6).setCellRenderer(UITheme.getStatusBadgeRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        add(sp, BorderLayout.CENTER);

        loadUsers();
    }

    public void loadUsers() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.getAllUsers();
        String roleFilter = (String) cmbRoleFilter.getSelectedItem();
        String query = txtSearch.getText().trim().toLowerCase();

        currentList.clear();
        for (User u : list) {
            boolean matchRole = "All Roles".equals(roleFilter) || u.getRole().name().equalsIgnoreCase(roleFilter);
            boolean matchQuery = query.isEmpty() ||
                                 u.getFullName().toLowerCase().contains(query) ||
                                 u.getEmail().toLowerCase().contains(query) ||
                                 (u.getPhone() != null && u.getPhone().contains(query));

            if (matchRole && matchQuery) {
                currentList.add(u);
                tableModel.addRow(new Object[]{
                    "#" + u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone() != null ? u.getPhone() : "—",
                    u.getRole().name(),
                    u.getAddress() != null ? u.getAddress() : "—",
                    u.getStatus(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0, 10) : ""
                });
            }
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table.", "Select User", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void toggleStatus() {
        User u = getSelectedUser();
        if (u == null) return;

        String nextStatus = "ACTIVE".equalsIgnoreCase(u.getStatus()) ? "INACTIVE" : "ACTIVE";
        userDAO.toggleUserStatus(u.getId(), nextStatus);
        JOptionPane.showMessageDialog(this, "User #" + u.getId() + " status changed to " + nextStatus + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        loadUsers();
        if (onDataChanged != null) onDataChanged.run();
    }

    private void deleteSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;

        if (u.getRole() == Role.ADMIN) {
            JOptionPane.showMessageDialog(this, "Cannot delete an administrator account.", "Action Forbidden", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete user #" + u.getId() + " (" + u.getFullName() + ")?\nThis will remove all related profile data and records.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            userDAO.deleteUser(u.getId());
            JOptionPane.showMessageDialog(this, "User deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
            if (onDataChanged != null) onDataChanged.run();
        }
    }
}

package com.homeservice.ui.admin;

import com.homeservice.dao.ServiceCategoryDAO;
import com.homeservice.model.ServiceCategory;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminServicesPanel extends JPanel {
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<ServiceCategory> currentList = new ArrayList<>();

    public AdminServicesPanel(Runnable onDataChanged) {
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

        JLabel title = new JLabel("Service Categories & Pricing Management");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadCategories());

        JButton btnAdd = UITheme.createPrimaryButton("+ Add New Service");
        btnAdd.addActionListener(e -> showAddCategoryDialog());

        JButton btnEdit = UITheme.createSecondaryButton("Edit Service");
        btnEdit.addActionListener(e -> showEditCategoryDialog());

        JButton btnDelete = UITheme.createDangerButton("Delete Service");
        btnDelete.addActionListener(e -> deleteSelectedCategory());

        actions.add(btnRefresh);
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Service Category Name", "Base Price (₹)", "Icon Identifier", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(450);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        add(sp, BorderLayout.CENTER);

        loadCategories();
    }

    public void loadCategories() {
        tableModel.setRowCount(0);
        currentList = categoryDAO.getAllCategories();
        for (ServiceCategory c : currentList) {
            tableModel.addRow(new Object[]{
                "#" + c.getId(),
                c.getName(),
                "₹" + String.format("%.2f", c.getBasePrice()),
                c.getIconName(),
                c.getDescription()
            });
        }
    }

    private ServiceCategory getSelectedCategory() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a service category from the table.", "Select Category", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void showAddCategoryDialog() {
        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField("499.00");
        JTextField txtIcon = new JTextField("wrench");
        JTextArea txtDesc = new JTextArea(3, 20);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Service Name:"));
        p.add(txtName);
        p.add(new JLabel("Base Rate (₹):"));
        p.add(txtPrice);
        p.add(new JLabel("Icon Identifier:"));
        p.add(txtIcon);
        p.add(new JLabel("Description:"));
        p.add(new JScrollPane(txtDesc));

        int res = JOptionPane.showConfirmDialog(this, p, "Create New Service Category", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String icon = txtIcon.getText().trim();
            String desc = txtDesc.getText().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Base Rate are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                ServiceCategory cat = new ServiceCategory();
                cat.setName(name);
                cat.setBasePrice(price);
                cat.setIconName(icon.isEmpty() ? "wrench" : icon);
                cat.setDescription(desc);

                categoryDAO.create(cat);
                JOptionPane.showMessageDialog(this, "Category created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditCategoryDialog() {
        ServiceCategory cat = getSelectedCategory();
        if (cat == null) return;

        JTextField txtName = new JTextField(cat.getName());
        JTextField txtPrice = new JTextField(String.valueOf(cat.getBasePrice()));
        JTextField txtIcon = new JTextField(cat.getIconName());
        JTextArea txtDesc = new JTextArea(cat.getDescription(), 3, 20);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Service Name:"));
        p.add(txtName);
        p.add(new JLabel("Base Rate (₹):"));
        p.add(txtPrice);
        p.add(new JLabel("Icon Identifier:"));
        p.add(txtIcon);
        p.add(new JLabel("Description:"));
        p.add(new JScrollPane(txtDesc));

        int res = JOptionPane.showConfirmDialog(this, p, "Edit Service Category #" + cat.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                cat.setName(txtName.getText().trim());
                cat.setBasePrice(Double.parseDouble(txtPrice.getText().trim()));
                cat.setIconName(txtIcon.getText().trim());
                cat.setDescription(txtDesc.getText().trim());

                categoryDAO.update(cat);
                JOptionPane.showMessageDialog(this, "Category updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCategory() {
        ServiceCategory cat = getSelectedCategory();
        if (cat == null) return;

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete category '" + cat.getName() + "'?\n(Notice: Cannot delete if providers or bookings reference it).", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = categoryDAO.delete(cat.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Service category deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
                if (onDataChanged != null) onDataChanged.run();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete this service category as active providers or bookings are linked to it.", "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

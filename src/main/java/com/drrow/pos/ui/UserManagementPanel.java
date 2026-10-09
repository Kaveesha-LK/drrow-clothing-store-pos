package com.drrow.pos.ui;

import com.drrow.pos.dao.RoleDAO;
import com.drrow.pos.dao.UserDAO;
import com.drrow.pos.model.Role;
import com.drrow.pos.model.User;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"ID", "Username", "Full Name", "Role", "Email", "Phone", "Active", "Must Change Pass"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable userTable = new ModernTable(tableModel);

    public UserManagementPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel("EMPLOYEE USER ACCOUNTS & ROLE PERMISSIONS");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("+ Add Employee User", ModernButton.ButtonType.GOLD);
        ModernButton btnEdit = new ModernButton("Edit User", ModernButton.ButtonType.SECONDARY);
        ModernButton btnResetPass = new ModernButton("Reset Password", ModernButton.ButtonType.WARNING);
        ModernButton btnToggleActive = new ModernButton("Toggle Active / Inactive", ModernButton.ButtonType.DANGER);
        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnNew.addActionListener(e -> openUserEditor(null));
        btnEdit.addActionListener(e -> editSelectedUser());
        btnResetPass.addActionListener(e -> resetSelectedUserPassword());
        btnToggleActive.addActionListener(e -> toggleUserActiveStatus());
        btnRefresh.addActionListener(e -> refreshUsers());

        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnResetPass);
        btnPanel.add(btnToggleActive);
        btnPanel.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        add(new JScrollPane(userTable), BorderLayout.CENTER);

        refreshUsers();
    }

    private void refreshUsers() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.findAll();
            for (User u : users) {
                tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRoleName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.isActive() ? "Active" : "Inactive",
                    u.isRequirePasswordChange() ? "Yes" : "No"
                });
            }
        } catch (Exception ex) {
            System.err.println("User refresh error: " + ex.getMessage());
        }
    }

    private void editSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            int id = (Integer) tableModel.getValueAt(row, 0);
            try {
                User u = userDAO.findById(id);
                if (u != null) openUserEditor(u);
            } catch (Exception ignored) {}
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openUserEditor(User existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), existing == null ? "Add Employee Account" : "Edit User Account", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        JTextField txtUser = new JTextField(existing != null ? existing.getUsername() : "");
        if (existing != null) txtUser.setEditable(false);
        JPasswordField txtPass = new JPasswordField();
        JTextField txtFull = new JTextField(existing != null ? existing.getFullName() : "");
        JTextField txtEmail = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField txtPhone = new JTextField(existing != null ? existing.getPhone() : "");

        JComboBox<Role> cmbRole = new JComboBox<>();
        try {
            for (Role r : roleDAO.findAll()) cmbRole.addItem(r);
        } catch (Exception ignored) {}

        form.add(new JLabel("Username:")); form.add(txtUser);
        if (existing == null) {
            form.add(new JLabel("Initial Password:")); form.add(txtPass);
        }
        form.add(new JLabel("Full Staff Name:")); form.add(txtFull);
        form.add(new JLabel("Assigned Role:")); form.add(cmbRole);
        form.add(new JLabel("Email Address:")); form.add(txtEmail);
        form.add(new JLabel("Telephone:")); form.add(txtPhone);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.COLOR_HEADER_BG);
        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Save User", ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String fullname = txtFull.getText().trim();
            Role role = (Role) cmbRole.getSelectedItem();

            if (username.isEmpty() || fullname.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(dlg, "Username, Full Name, and Role are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (existing == null) {
                    String pass = new String(txtPass.getPassword()).trim();
                    if (pass.length() < 6) {
                        JOptionPane.showMessageDialog(dlg, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    User u = new User(0, username, fullname, role.getRoleId(), role.getRoleName(), true);
                    u.setEmail(txtEmail.getText().trim());
                    u.setPhone(txtPhone.getText().trim());
                    u.setRequirePasswordChange(true); // Force password change on first login
                    userDAO.create(u, pass);
                } else {
                    existing.setFullName(fullname);
                    existing.setRoleId(role.getRoleId());
                    existing.setEmail(txtEmail.getText().trim());
                    existing.setPhone(txtPhone.getText().trim());
                    userDAO.update(existing);
                }
                JOptionPane.showMessageDialog(dlg, "User account successfully saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshUsers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error saving user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void resetSelectedUserPassword() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            int id = (Integer) tableModel.getValueAt(row, 0);
            String name = (String) tableModel.getValueAt(row, 1);
            String newPass = JOptionPane.showInputDialog(this, "Enter new temporary password for user '" + name + "':");
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (newPass.trim().length() < 6) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Invalid Password", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    userDAO.updatePassword(id, newPass.trim());
                    JOptionPane.showMessageDialog(this, "Password for '" + name + "' reset successfully.", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
                    refreshUsers();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleUserActiveStatus() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            int id = (Integer) tableModel.getValueAt(row, 0);
            try {
                User u = userDAO.findById(id);
                if (u != null) {
                    if ("admin".equalsIgnoreCase(u.getUsername())) {
                        JOptionPane.showMessageDialog(this, "Cannot deactivate primary system administrator.", "Action Denied", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    u.setActive(!u.isActive());
                    userDAO.update(u);
                    refreshUsers();
                }
            } catch (Exception ignored) {}
        }
    }
}

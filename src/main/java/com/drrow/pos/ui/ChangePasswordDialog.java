package com.drrow.pos.ui;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.service.AuthService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final JPasswordField txtOldPassword = new JPasswordField(15);
    private final JPasswordField txtNewPassword = new JPasswordField(15);
    private final JPasswordField txtConfirmPassword = new JPasswordField(15);
    private final AuthService authService = new AuthService();
    private boolean passwordSuccessfullyChanged = false;

    public ChangePasswordDialog(Frame parent, boolean forceFirstChange) {
        super(parent, "D’RROW POS - Security Password Update", true);
        setSize(440, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_CARD_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UITheme.COLOR_CARD_BG);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel(forceFirstChange ? "Password Change Required" : "Update Password");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);
        JLabel lblSubtitle = new JLabel(forceFirstChange ?
                "First-time login detected. Please create a new secure password." :
                "Ensure your new password has at least 6 characters.");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.COLOR_TEXT_MUTED);
        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 12));
        formPanel.setOpaque(false);

        JLabel lblOld = new JLabel("Current Password:");
        lblOld.setFont(UITheme.FONT_BOLD);
        lblOld.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblNew = new JLabel("New Password:");
        lblNew.setFont(UITheme.FONT_BOLD);
        lblNew.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblConfirm = new JLabel("Confirm Password:");
        lblConfirm.setFont(UITheme.FONT_BOLD);
        lblConfirm.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        stylePasswordField(txtOldPassword);
        stylePasswordField(txtNewPassword);
        stylePasswordField(txtConfirmPassword);

        formPanel.add(lblOld);
        formPanel.add(txtOldPassword);
        formPanel.add(lblNew);
        formPanel.add(txtNewPassword);
        formPanel.add(lblConfirm);
        formPanel.add(txtConfirmPassword);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Update Password", ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> executeChangePassword());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setBackground(UITheme.COLOR_INPUT_BG);
        pf.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void executeChangePassword() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation password do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
            boolean success = authService.changePassword(userId, oldPass, newPass);
            if (success) {
                passwordSuccessfullyChanged = true;
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPasswordSuccessfullyChanged() {
        return passwordSuccessfullyChanged;
    }
}

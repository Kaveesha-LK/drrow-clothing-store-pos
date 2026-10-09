package com.drrow.pos.ui;

import com.drrow.pos.model.User;
import com.drrow.pos.service.AuthService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final JCheckBox chkShowPassword = new JCheckBox("Show password");
    private final JLabel lblStatus = new JLabel(" ");
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("D’RROW Clothing Store - POS Terminal Login");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BG_DARK);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(UITheme.COLOR_BG_DARK);
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.COLOR_CARD_BG);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1, true),
            new EmptyBorder(30, 36, 30, 36)
        ));
        cardPanel.setPreferredSize(new Dimension(400, 440));

        // Brand Logo / Header
        JLabel lblBrand = new JLabel("D’RROW");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblBrand.setForeground(UITheme.COLOR_GOLD);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel("CLOTHING STORE POS");
        lblTagline.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTagline.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInstruction = new JLabel("Sign in to your cash terminal or manager desk");
        lblInstruction.setFont(UITheme.FONT_SMALL);
        lblInstruction.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(lblBrand);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblTagline);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(lblInstruction);
        cardPanel.add(Box.createVerticalStrut(24));

        // Form Fields
        JLabel lblUser = new JLabel("Username / Cashier ID");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleTextField(txtUsername);
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setMaximumSize(new Dimension(340, 36));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleTextField(txtPassword);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(340, 36));

        // Show/Hide password toggle
        chkShowPassword.setFont(UITheme.FONT_SMALL);
        chkShowPassword.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShowPassword.setBackground(UITheme.COLOR_CARD_BG);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        // Key listeners for Enter key submission
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterSubmit);
        txtPassword.addKeyListener(enterSubmit);

        // Status Label
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.COLOR_DANGER);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(340, 40));

        ModernButton btnExit = new ModernButton("Exit", ModernButton.ButtonType.SECONDARY);
        ModernButton btnLogin = new ModernButton("Sign In", ModernButton.ButtonType.GOLD);

        btnExit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> performLogin());

        btnPanel.add(btnExit);
        btnPanel.add(btnLogin);

        cardPanel.add(lblUser);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(txtUsername);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(lblPass);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(txtPassword);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(chkShowPassword);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(btnPanel);

        contentPanel.add(cardPanel);
        add(contentPanel);

        // Quick focus on username
        SwingUtilities.invokeLater(txtUsername::requestFocusInWindow);
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setBackground(UITheme.COLOR_INPUT_BG);
        tf.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblStatus.setText("Verifying credentials...");

        SwingUtilities.invokeLater(() -> {
            try {
                User user = authService.login(username, password);
                lblStatus.setText(" ");

                // Check mandatory first-run password change
                if (user.isRequirePasswordChange()) {
                    ChangePasswordDialog pwdDialog = new ChangePasswordDialog(this, true);
                    pwdDialog.setVisible(true);
                }

                // Launch Main Dashboard Frame
                dispose();
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

            } catch (Exception ex) {
                lblStatus.setForeground(UITheme.COLOR_DANGER);
                lblStatus.setText(ex.getMessage());
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
            }
        });
    }
}

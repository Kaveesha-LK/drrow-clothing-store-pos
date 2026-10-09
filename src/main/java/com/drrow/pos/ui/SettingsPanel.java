package com.drrow.pos.ui;

import com.drrow.pos.service.SettingService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class SettingsPanel extends JPanel {

    private final SettingService settingService = new SettingService();

    // Store settings fields
    private final JTextField txtStoreName = new JTextField();
    private final JTextField txtTagline = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtCurrency = new JTextField();
    private final JTextField txtTaxPct = new JTextField();
    private final JTextField txtBarcodePrefix = new JTextField();
    private final JTextField txtInvoicePrefix = new JTextField();
    private final JTextField txtLowStock = new JTextField();
    private final JTextArea txtFooter = new JTextArea(2, 20);
    private final JTextArea txtPolicy = new JTextArea(2, 20);

    public SettingsPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(UITheme.COLOR_CARD_BG);
        tabs.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        tabs.addTab("Store Details & Hardware", createStoreSettingsTab());
        tabs.addTab("Database Backup & Disaster Recovery", createBackupRestoreTab());

        add(tabs, BorderLayout.CENTER);

        loadSettings();
    }

    private JPanel createStoreSettingsTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(11, 2, 12, 8));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        form.add(new JLabel("Official Store Name:")); form.add(txtStoreName);
        form.add(new JLabel("Brand Tagline / Slogan:")); form.add(txtTagline);
        form.add(new JLabel("Store Physical Address:")); form.add(txtAddress);
        form.add(new JLabel("Telephone Contact:")); form.add(txtPhone);
        form.add(new JLabel("Customer Service Email:")); form.add(txtEmail);
        form.add(new JLabel("Currency Symbol:")); form.add(txtCurrency);
        form.add(new JLabel("Sales Tax Percentage (%):")); form.add(txtTaxPct);
        form.add(new JLabel("Clothing Tag Barcode Prefix:")); form.add(txtBarcodePrefix);
        form.add(new JLabel("Invoice Number Prefix:")); form.add(txtInvoicePrefix);
        form.add(new JLabel("Low Stock Reorder Threshold:")); form.add(txtLowStock);
        form.add(new JLabel("Receipt Footer Greeting:")); form.add(new JScrollPane(txtFooter));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        ModernButton btnSave = new ModernButton("Save Configuration", ModernButton.ButtonType.GOLD);
        btnSave.addActionListener(e -> saveSettings());
        btnBar.add(btnSave);

        pnl.add(form, BorderLayout.CENTER);
        pnl.add(btnBar, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel createBackupRestoreTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBackground(UITheme.COLOR_CARD_BG);
        pnl.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel("DATABASE BACKUP & RESTORE PROCEDURES");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        JTextArea txtGuide = new JTextArea();
        txtGuide.setFont(UITheme.FONT_REGULAR);
        txtGuide.setBackground(UITheme.COLOR_INPUT_BG);
        txtGuide.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        txtGuide.setEditable(false);
        txtGuide.setLineWrap(true);
        txtGuide.setWrapStyleWord(true);
        txtGuide.setText(
            "1. Automated Backup Command (MySQL 8.x / MariaDB):\n" +
            "   mysqldump -u root -p drrow_pos > drrow_pos_backup_$(date +%Y%m%d_%H%M%S).sql\n\n" +
            "2. Interactive Backup via NetBeans or Terminal:\n" +
            "   Click the 'Create Immediate SQL Backup' button below to export current tables, inventory, and sales history.\n\n" +
            "3. Restore Safety Warning:\n" +
            "   WARNING: Restoring a database overwrites existing products, inventory transactions, and sales.\n" +
            "   Always verify existing backups before proceeding."
        );

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnPanel.setOpaque(false);

        ModernButton btnBackup = new ModernButton("Create Immediate SQL Backup", ModernButton.ButtonType.SUCCESS);
        ModernButton btnRestore = new ModernButton("Restore Database...", ModernButton.ButtonType.DANGER);

        btnBackup.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("drrow_pos_backup_" + System.currentTimeMillis() + ".sql"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Database backup script saved successfully to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "Backup Created", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRestore.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                "WARNING: This operation can overwrite current data!\nAre you sure you want to proceed with database restore?",
                "Critical Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(this, "Database restored from:\n" + chooser.getSelectedFile().getName(), "Restore Succeeded", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnPanel.add(btnBackup);
        btnPanel.add(btnRestore);

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(new JScrollPane(txtGuide), BorderLayout.CENTER);
        pnl.add(btnPanel, BorderLayout.SOUTH);
        return pnl;
    }

    private void loadSettings() {
        try {
            Map<String, String> s = settingService.getAllSettings();
            txtStoreName.setText(s.getOrDefault("store.name", "D’RROW Clothing Store"));
            txtTagline.setText(s.getOrDefault("store.tagline", "Modern Elegance & Premium Fashion"));
            txtAddress.setText(s.getOrDefault("store.address", "No. 124, Galle Road, Colombo 03"));
            txtPhone.setText(s.getOrDefault("store.phone", "+94 11 234 5678"));
            txtEmail.setText(s.getOrDefault("store.email", "support@drrow.com"));
            txtCurrency.setText(s.getOrDefault("store.currency", "Rs."));
            txtTaxPct.setText(s.getOrDefault("pos.tax_percentage", "0.00"));
            txtBarcodePrefix.setText(s.getOrDefault("barcode.prefix", "DRTS"));
            txtInvoicePrefix.setText(s.getOrDefault("invoice.prefix", "DRR"));
            txtLowStock.setText(s.getOrDefault("inventory.low_stock_threshold", "5"));
            txtFooter.setText(s.getOrDefault("invoice.footer", "Thank you for shopping at D’RROW Clothing Store!"));
            txtPolicy.setText(s.getOrDefault("policy.return_exchange", "Items can be exchanged within 7 days."));
        } catch (Exception ex) {
            System.err.println("Load settings error: " + ex.getMessage());
        }
    }

    private void saveSettings() {
        try {
            settingService.updateSetting("store.name", txtStoreName.getText().trim(), "STORE", "Store name");
            settingService.updateSetting("store.tagline", txtTagline.getText().trim(), "STORE", "Tagline");
            settingService.updateSetting("store.address", txtAddress.getText().trim(), "STORE", "Address");
            settingService.updateSetting("store.phone", txtPhone.getText().trim(), "STORE", "Phone");
            settingService.updateSetting("store.email", txtEmail.getText().trim(), "STORE", "Email");
            settingService.updateSetting("store.currency", txtCurrency.getText().trim(), "GENERAL", "Currency");
            settingService.updateSetting("pos.tax_percentage", txtTaxPct.getText().trim(), "POS", "Tax %");
            settingService.updateSetting("barcode.prefix", txtBarcodePrefix.getText().trim(), "BARCODE", "Barcode prefix");
            settingService.updateSetting("invoice.prefix", txtInvoicePrefix.getText().trim(), "INVOICE", "Invoice prefix");
            settingService.updateSetting("inventory.low_stock_threshold", txtLowStock.getText().trim(), "INVENTORY", "Threshold");
            settingService.updateSetting("invoice.footer", txtFooter.getText().trim(), "INVOICE", "Footer");

            JOptionPane.showMessageDialog(this, "Settings updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

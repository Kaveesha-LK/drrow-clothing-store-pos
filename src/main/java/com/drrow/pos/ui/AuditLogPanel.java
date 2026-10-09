package com.drrow.pos.ui;

import com.drrow.pos.dao.AuditLogDAO;
import com.drrow.pos.model.AuditLog;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.theme.UITheme;
import com.drrow.pos.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditLogPanel extends JPanel {

    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final JComboBox<String> cmbActionFilter = new JComboBox<>(new String[]{
        "ALL", "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT", "SALE_COMPLETE", "RETURN_COMPLETE",
        "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE", "STOCK_RECEIVE", "STOCK_ADJUSTMENT",
        "PASSWORD_CHANGE", "SETTINGS_CHANGE"
    });

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Log ID", "Date / Time", "User", "Action", "Entity", "Entity ID", "Description"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable auditTable = new ModernTable(tableModel);

    public AuditLogPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel("SECURITY & AUDIT TRAILS LOG VIEWER");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        cmbActionFilter.setFont(UITheme.FONT_REGULAR);
        cmbActionFilter.setBackground(UITheme.COLOR_INPUT_BG);
        cmbActionFilter.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        cmbActionFilter.addActionListener(e -> refreshLogs());

        ModernButton btnRefresh = new ModernButton("Refresh Logs", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshLogs());

        filterPanel.add(new JLabel("Action Filter:"));
        filterPanel.add(cmbActionFilter);
        filterPanel.add(btnRefresh);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        add(new JScrollPane(auditTable), BorderLayout.CENTER);

        refreshLogs();
    }

    public void refreshLogs() {
        tableModel.setRowCount(0);
        try {
            String filter = (String) cmbActionFilter.getSelectedItem();
            List<AuditLog> logs = auditDAO.searchLogs(filter, null, null, null);
            for (AuditLog l : logs) {
                tableModel.addRow(new Object[]{
                    l.getLogId(),
                    FormatUtil.formatDateTime(l.getCreatedAt()),
                    l.getUsername(),
                    l.getAction(),
                    l.getEntityType(),
                    l.getEntityId(),
                    l.getDescription()
                });
            }
        } catch (Exception ex) {
            System.err.println("Audit log error: " + ex.getMessage());
        }
    }
}

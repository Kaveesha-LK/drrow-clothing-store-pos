package com.drrow.pos.ui;

import com.drrow.pos.model.Customer;
import com.drrow.pos.service.SalesService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.components.SearchField;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerManagementPanel extends JPanel {

    private final SalesService salesService = new SalesService();

    private final SearchField txtSearch = new SearchField("Search customer by name or telephone...");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"ID", "Customer Name", "Telephone", "Email", "Address", "Loyalty Notes"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable customerTable = new ModernTable(tableModel);

    public CustomerManagementPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCustomers(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCustomers(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCustomers(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        ModernButton btnNew = new ModernButton("+ New Customer", ModernButton.ButtonType.GOLD);
        ModernButton btnEdit = new ModernButton("Edit Customer", ModernButton.ButtonType.SECONDARY);
        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnNew.addActionListener(e -> openCustomerEditor(null));
        btnEdit.addActionListener(e -> editSelectedCustomer());
        btnRefresh.addActionListener(e -> refreshCustomers());

        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnRefresh);

        topBar.add(txtSearch, BorderLayout.CENTER);
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        refreshCustomers();
    }

    private void refreshCustomers() {
        tableModel.setRowCount(0);
        try {
            List<Customer> list = salesService.getAllCustomers();
            String query = txtSearch.getText().trim().toLowerCase();
            for (Customer c : list) {
                if (query.isEmpty() || c.getName().toLowerCase().contains(query) ||
                    (c.getPhone() != null && c.getPhone().contains(query))) {
                    tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getPhone(),
                        c.getEmail(),
                        c.getAddress(),
                        c.getNotes()
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    private void editSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row >= 0) {
            int id = (Integer) tableModel.getValueAt(row, 0);
            try {
                Customer c = salesService.getCustomerById(id);
                if (c != null) openCustomerEditor(c);
            } catch (Exception ignored) {}
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openCustomerEditor(Customer existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), existing == null ? "Add New Customer" : "Edit Customer", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 360);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        JTextField txtName = new JTextField(existing != null ? existing.getName() : "");
        JTextField txtPhone = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField txtEmail = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField txtAddress = new JTextField(existing != null ? existing.getAddress() : "");
        JTextField txtNotes = new JTextField(existing != null ? existing.getNotes() : "");

        form.add(new JLabel("Full Name:")); form.add(txtName);
        form.add(new JLabel("Telephone Number:")); form.add(txtPhone);
        form.add(new JLabel("Email Address:")); form.add(txtEmail);
        form.add(new JLabel("Street Address:")); form.add(txtAddress);
        form.add(new JLabel("Loyalty / Notes:")); form.add(txtNotes);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.COLOR_HEADER_BG);
        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Save Customer", ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Customer name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Customer c = existing != null ? existing : new Customer();
            c.setName(name);
            c.setPhone(txtPhone.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setNotes(txtNotes.getText().trim());

            try {
                salesService.saveCustomer(c);
                JOptionPane.showMessageDialog(dlg, "Customer profile saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshCustomers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}

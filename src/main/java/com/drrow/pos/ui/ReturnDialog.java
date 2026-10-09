package com.drrow.pos.ui;

import com.drrow.pos.model.ReturnItem;
import com.drrow.pos.model.Sale;
import com.drrow.pos.model.SaleItem;
import com.drrow.pos.service.ReturnService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.theme.UITheme;
import com.drrow.pos.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReturnDialog extends JDialog {

    private final Sale sale;
    private final ReturnService returnService = new ReturnService();

    private final DefaultTableModel itemTableModel = new DefaultTableModel(
        new String[]{"Item ID", "Product", "Variant", "Sold Qty", "Unit Price", "Return Qty", "Refund Amt", "Restock Action"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5 || column == 7; // Only return quantity and restock action are editable
        }
    };
    private final ModernTable itemsTable = new ModernTable(itemTableModel);

    private final JComboBox<String> cmbAction = new JComboBox<>(new String[]{"REFUND", "EXCHANGE"});
    private final JTextArea txtReason = new JTextArea(2, 20);
    private final JLabel lblTotalRefund = new JLabel("Rs. 0.00");
    private boolean returnCompleted = false;

    public ReturnDialog(Window parent, Sale sale) {
        super(parent, "D’RROW POS - Process Return / Exchange for Invoice [" + sale.getInvoiceNumber() + "]", ModalityType.APPLICATION_MODAL);
        this.sale = sale;
        initUI();
        loadSaleItems();
    }

    private void initUI() {
        setSize(780, 520);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setBackground(UITheme.COLOR_HEADER_BG);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("INVOICE RETURN / EXCHANGE DISPATCH");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        String subInfo = "Invoice: " + sale.getInvoiceNumber() + " | Date: " + FormatUtil.formatDate(sale.getSaleDate()) +
                         " | Original Grand Total: " + FormatUtil.formatCurrency(sale.getGrandTotal()) +
                         " | Customer: " + (sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-In");
        JLabel lblSub = new JLabel(subInfo);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        topPanel.add(lblTitle);
        topPanel.add(lblSub);
        add(topPanel, BorderLayout.NORTH);

        // Center: Items table
        itemsTable.getModel().addTableModelListener(e -> calculateTotalRefund());
        JComboBox<String> cmbRestock = new JComboBox<>(new String[]{"RESTOCKED", "DAMAGED_DISCARD"});
        itemsTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(cmbRestock));

        JScrollPane scroll = new JScrollPane(itemsTable);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Sold Line Items (Enter return quantity in table)", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));
        add(scroll, BorderLayout.CENTER);

        // Bottom Configuration
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 8));
        bottomContainer.setBackground(UITheme.COLOR_CARD_BG);
        bottomContainer.setBorder(UITheme.createCardBorder());

        JPanel formRow = new JPanel(new GridLayout(2, 2, 12, 8));
        formRow.setOpaque(false);

        cmbAction.setFont(UITheme.FONT_REGULAR);
        cmbAction.setBackground(UITheme.COLOR_INPUT_BG);
        cmbAction.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        lblTotalRefund.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotalRefund.setForeground(UITheme.COLOR_DANGER);

        JPanel p1 = new JPanel(new BorderLayout()); p1.setOpaque(false);
        p1.add(new JLabel("Return Action:"), BorderLayout.NORTH);
        p1.add(cmbAction, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout()); p2.setOpaque(false);
        p2.add(new JLabel("Total Calculated Refund:"), BorderLayout.NORTH);
        p2.add(lblTotalRefund, BorderLayout.CENTER);

        JPanel p3 = new JPanel(new BorderLayout()); p3.setOpaque(false);
        p3.add(new JLabel("Reason for Return / Exchange:"), BorderLayout.NORTH);
        txtReason.setBackground(UITheme.COLOR_INPUT_BG);
        txtReason.setForeground(Color.WHITE);
        p3.add(new JScrollPane(txtReason), BorderLayout.CENTER);

        formRow.add(p1);
        formRow.add(p2);
        formRow.add(p3);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnRow.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnConfirm = new ModernButton("Process Return & Restore Stock", ModernButton.ButtonType.DANGER);

        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> executeReturn());

        btnRow.add(btnCancel);
        btnRow.add(btnConfirm);

        bottomContainer.add(formRow, BorderLayout.CENTER);
        bottomContainer.add(btnRow, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);
    }

    private void loadSaleItems() {
        itemTableModel.setRowCount(0);
        if (sale.getItems() != null) {
            for (SaleItem item : sale.getItems()) {
                itemTableModel.addRow(new Object[]{
                    item.getItemId(),
                    item.getProductName(),
                    item.getSizeName() + " / " + item.getColourName(),
                    item.getQuantity(),
                    FormatUtil.formatDecimal(item.getUnitPrice()),
                    0, // Default return quantity
                    "0.00",
                    "RESTOCKED"
                });
            }
        }
    }

    private void calculateTotalRefund() {
        double total = 0.00;
        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            try {
                Object qtyObj = itemTableModel.getValueAt(i, 5);
                int returnQty = Integer.parseInt(qtyObj.toString());
                int soldQty = (Integer) itemTableModel.getValueAt(i, 3);
                double unitPrice = FormatUtil.parseAmount(itemTableModel.getValueAt(i, 4).toString());

                if (returnQty > soldQty) {
                    returnQty = soldQty;
                    itemTableModel.setValueAt(returnQty, i, 5);
                }
                if (returnQty < 0) {
                    returnQty = 0;
                    itemTableModel.setValueAt(returnQty, i, 5);
                }

                double refundLine = returnQty * unitPrice;
                itemTableModel.setValueAt(FormatUtil.formatDecimal(refundLine), i, 6);
                total += refundLine;
            } catch (Exception ignored) {}
        }
        lblTotalRefund.setText(FormatUtil.formatCurrency(total));
    }

    private void executeReturn() {
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a reason for this return/exchange.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ReturnItem> returnItems = new ArrayList<>();
        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            int returnQty = 0;
            try {
                returnQty = Integer.parseInt(itemTableModel.getValueAt(i, 5).toString());
            } catch (Exception ignored) {}

            if (returnQty > 0) {
                int saleItemId = (Integer) itemTableModel.getValueAt(i, 0);
                SaleItem origItem = null;
                for (SaleItem si : sale.getItems()) {
                    if (si.getItemId() == saleItemId) {
                        origItem = si;
                        break;
                    }
                }
                if (origItem != null) {
                    double refundLine = returnQty * origItem.getUnitPrice();
                    String restock = (String) itemTableModel.getValueAt(i, 7);
                    returnItems.add(new ReturnItem(
                        origItem.getItemId(),
                        origItem.getVariantId(),
                        origItem.getProductName(),
                        origItem.getSku(),
                        origItem.getSizeName(),
                        origItem.getColourName(),
                        returnQty,
                        origItem.getUnitPrice(),
                        refundLine,
                        restock
                    ));
                }
            }
        }

        if (returnItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items have a return quantity greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String action = (String) cmbAction.getSelectedItem();
        try {
            boolean success = returnService.processReturn(sale.getSaleId(), sale.getInvoiceNumber(), returnItems, reason, action);
            if (success) {
                returnCompleted = true;
                JOptionPane.showMessageDialog(this, "Return transaction processed successfully! Stock levels updated.", "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to process return: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isReturnCompleted() {
        return returnCompleted;
    }
}

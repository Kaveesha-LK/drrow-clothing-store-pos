package com.drrow.pos.ui;

import com.drrow.pos.model.Sale;
import com.drrow.pos.model.SaleItem;
import com.drrow.pos.service.SalesService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.components.SearchField;
import com.drrow.pos.ui.theme.UITheme;
import com.drrow.pos.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesHistoryPanel extends JPanel {

    private final SalesService salesService = new SalesService();

    private final SearchField txtSearch = new SearchField("Search invoice number, customer...");
    private final DefaultTableModel salesTableModel = new DefaultTableModel(
        new String[]{"ID", "Invoice #", "Date / Time", "Cashier", "Customer", "Subtotal", "Discount", "Grand Total", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable salesTable = new ModernTable(salesTableModel);

    private final DefaultTableModel itemsTableModel = new DefaultTableModel(
        new String[]{"Barcode", "Item Name", "Size", "Colour", "Qty", "Unit Price", "Discount", "Line Total"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable itemsTable = new ModernTable(itemsTableModel);

    private Sale selectedSale = null;

    public SalesHistoryPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshSales(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshSales(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshSales(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        ModernButton btnReprint = new ModernButton("Reprint Bill", ModernButton.ButtonType.GOLD);
        ModernButton btnReturn = new ModernButton("Return / Exchange", ModernButton.ButtonType.WARNING);
        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnReprint.addActionListener(e -> reprintSelectedSale());
        btnReturn.addActionListener(e -> openReturnDialog());
        btnRefresh.addActionListener(e -> refreshSales());

        btnPanel.add(btnReprint);
        btnPanel.add(btnReturn);
        btnPanel.add(btnRefresh);

        topBar.add(txtSearch, BorderLayout.CENTER);
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center split pane: Sales on top, Items on bottom
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedSaleItems();
            }
        });

        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Invoices Archive", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Purchased Line Items", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        split.setTopComponent(salesScroll);
        split.setBottomComponent(itemsScroll);

        add(split, BorderLayout.CENTER);

        refreshSales();
    }

    public void refreshSales() {
        salesTableModel.setRowCount(0);
        try {
            String query = txtSearch.getText().trim();
            List<Sale> sales = salesService.searchSales(query, null, null, null);
            for (Sale s : sales) {
                salesTableModel.addRow(new Object[]{
                    s.getSaleId(),
                    s.getInvoiceNumber(),
                    FormatUtil.formatDateTime(s.getSaleDate()),
                    s.getCashierName(),
                    s.getCustomerName() != null ? s.getCustomerName() : "Walk-In Customer",
                    FormatUtil.formatDecimal(s.getSubtotal()),
                    FormatUtil.formatDecimal(s.getDiscountAmount()),
                    FormatUtil.formatDecimal(s.getGrandTotal()),
                    s.getPaymentStatus()
                });
            }
        } catch (Exception ex) {
            System.err.println("Sales history error: " + ex.getMessage());
        }
    }

    private void updateSelectedSaleItems() {
        itemsTableModel.setRowCount(0);
        int row = salesTable.getSelectedRow();
        if (row >= 0) {
            String invoiceNum = (String) salesTableModel.getValueAt(row, 1);
            try {
                this.selectedSale = salesService.getSaleByInvoiceNumber(invoiceNum);
                if (selectedSale != null && selectedSale.getItems() != null) {
                    for (SaleItem item : selectedSale.getItems()) {
                        itemsTableModel.addRow(new Object[]{
                            item.getBarcode(),
                            item.getProductName(),
                            item.getSizeName(),
                            item.getColourName(),
                            item.getQuantity(),
                            FormatUtil.formatDecimal(item.getUnitPrice()),
                            FormatUtil.formatDecimal(item.getDiscountAmount()),
                            FormatUtil.formatDecimal(item.getLineTotal())
                        });
                    }
                }
            } catch (Exception ignored) {}
        } else {
            this.selectedSale = null;
        }
    }

    private void reprintSelectedSale() {
        if (selectedSale == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ReceiptPreviewDialog dlg = new ReceiptPreviewDialog(SwingUtilities.getWindowAncestor(this), selectedSale);
        dlg.setVisible(true);
    }

    private void openReturnDialog() {
        if (selectedSale == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ReturnDialog dlg = new ReturnDialog(SwingUtilities.getWindowAncestor(this), selectedSale);
        dlg.setVisible(true);
        if (dlg.isReturnCompleted()) {
            refreshSales();
        }
    }
}

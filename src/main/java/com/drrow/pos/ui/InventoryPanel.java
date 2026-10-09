package com.drrow.pos.ui;

import com.drrow.pos.model.*;
import com.drrow.pos.service.InventoryService;
import com.drrow.pos.service.ProductService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService = new InventoryService();
    private final ProductService productService = new ProductService();

    private final JTabbedPane tabbedPane = new JTabbedPane();

    // Tab 1: Current Stock & Alerts
    private final SearchField txtStockSearch = new SearchField("Search variant name, barcode, SKU...");
    private final JComboBox<String> cmbStockFilter = new JComboBox<>(new String[]{"All Items", "Low Stock Alert (<= 5)", "Out of Stock (0)"});
    private final DefaultTableModel stockTableModel = new DefaultTableModel(
        new String[]{"Variant ID", "Barcode", "Product Name", "SKU", "Size", "Colour", "Current Stock", "Reorder Level", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable stockTable = new ModernTable(stockTableModel);

    // Tab 2: Stock Receiving
    private final DefaultTableModel receiptTableModel = new DefaultTableModel(
        new String[]{"Receipt #", "Supplier", "Date", "Items Count", "Total Cost", "PO Reference", "Received By"}, 0
    );
    private final ModernTable receiptTable = new ModernTable(receiptTableModel);

    // Tab 3: Movement History
    private final DefaultTableModel movementTableModel = new DefaultTableModel(
        new String[]{"ID", "Date/Time", "Type", "Barcode", "Item Name", "Change Qty", "Before", "After", "Reference", "User", "Notes"}, 0
    );
    private final ModernTable movementTable = new ModernTable(movementTableModel);

    public InventoryPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.COLOR_CARD_BG);
        tabbedPane.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        initCurrentStockTab();
        initReceivingTab();
        initMovementHistoryTab();

        add(tabbedPane, BorderLayout.CENTER);

        refreshStock();
    }

    private void initCurrentStockTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        txtStockSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshStock(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshStock(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshStock(); }
        });

        cmbStockFilter.setFont(UITheme.FONT_REGULAR);
        cmbStockFilter.setBackground(UITheme.COLOR_INPUT_BG);
        cmbStockFilter.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        cmbStockFilter.addActionListener(e -> refreshStock());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(txtStockSearch);
        filterPanel.add(cmbStockFilter);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        ModernButton btnAdjust = new ModernButton("Stock Adjustment", ModernButton.ButtonType.WARNING);
        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnAdjust.addActionListener(e -> openStockAdjustmentDialog());
        btnRefresh.addActionListener(e -> refreshStock());

        btnPanel.add(btnAdjust);
        btnPanel.add(btnRefresh);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        pnl.add(topBar, BorderLayout.NORTH);

        pnl.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        tabbedPane.addTab("Current Inventory & Alerts", pnl);
    }

    private void refreshStock() {
        stockTableModel.setRowCount(0);
        try {
            String query = txtStockSearch.getText().trim();
            int filterIdx = cmbStockFilter.getSelectedIndex();

            List<ProductVariant> list;
            if (filterIdx == 1) {
                list = inventoryService.getLowStockAlerts(5);
            } else if (filterIdx == 2) {
                list = inventoryService.getOutOfStockAlerts();
            } else {
                list = productService.searchVariants(query);
            }

            for (ProductVariant v : list) {
                String status = "NORMAL";
                if (v.getCurrentStock() <= 0) {
                    status = "OUT OF STOCK";
                } else if (v.isLowStock()) {
                    status = "LOW STOCK";
                }

                stockTableModel.addRow(new Object[]{
                    v.getVariantId(),
                    v.getBarcode(),
                    v.getProductName(),
                    v.getSku(),
                    v.getSizeName(),
                    v.getColourName(),
                    v.getCurrentStock(),
                    v.getReorderLevel(),
                    status
                });
            }
        } catch (Exception ex) {
            System.err.println("Stock refresh error: " + ex.getMessage());
        }
    }

    private void initReceivingTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        ModernButton btnNewReceipt = new ModernButton("+ Receive Inward Stock", ModernButton.ButtonType.GOLD);
        btnNewReceipt.addActionListener(e -> openStockReceivingDialog());
        topBar.add(btnNewReceipt);
        pnl.add(topBar, BorderLayout.NORTH);

        pnl.add(new JScrollPane(receiptTable), BorderLayout.CENTER);
        tabbedPane.addTab("Supplier Stock Receiving", pnl);
        refreshReceipts();
    }

    private void refreshReceipts() {
        receiptTableModel.setRowCount(0);
        try {
            List<StockReceipt> receipts = inventoryService.getAllReceipts();
            for (StockReceipt r : receipts) {
                receiptTableModel.addRow(new Object[]{
                    r.getReceiptNumber(),
                    r.getSupplierName() != null ? r.getSupplierName() : "Apex Garments Lanka",
                    FormatUtil.formatDate(r.getReceiptDate()),
                    "-",
                    FormatUtil.formatCurrency(r.getTotalAmount()),
                    r.getReferenceNumber(),
                    r.getUsername()
                });
            }
        } catch (Exception ignored) {}
    }

    private void initMovementHistoryTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        ModernButton btnRefresh = new ModernButton("Refresh Movements", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshMovements());
        topBar.add(btnRefresh);
        pnl.add(topBar, BorderLayout.NORTH);

        pnl.add(new JScrollPane(movementTable), BorderLayout.CENTER);
        tabbedPane.addTab("Inventory Audit Trails & Movements", pnl);
        refreshMovements();
    }

    private void refreshMovements() {
        movementTableModel.setRowCount(0);
        try {
            List<StockMovement> list = inventoryService.getRecentMovements(100);
            for (StockMovement sm : list) {
                movementTableModel.addRow(new Object[]{
                    sm.getMovementId(),
                    FormatUtil.formatDateTime(sm.getCreatedAt()),
                    sm.getMovementType(),
                    sm.getBarcode(),
                    sm.getProductName(),
                    (sm.getQuantity() > 0 ? "+" : "") + sm.getQuantity(),
                    sm.getBeforeQuantity(),
                    sm.getAfterQuantity(),
                    sm.getReferenceId(),
                    sm.getUsername(),
                    sm.getNotes()
                });
            }
        } catch (Exception ignored) {}
    }

    /**
     * Requirement 20: Stock Adjustment Dialog with reasons:
     * Damage, Lost, Correction, Found, Manual adjustment.
     * Records User, Date/time, Product, Quantity, Before quantity, After quantity, Reason.
     */
    private void openStockAdjustmentDialog() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the stock table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int variantId = (Integer) stockTableModel.getValueAt(selectedRow, 0);
        String pName = (String) stockTableModel.getValueAt(selectedRow, 2);
        String size = (String) stockTableModel.getValueAt(selectedRow, 4);
        String colour = (String) stockTableModel.getValueAt(selectedRow, 5);
        int curStock = (Integer) stockTableModel.getValueAt(selectedRow, 6);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Inventory Stock Adjustment", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 12));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        JComboBox<String> cmbType = new JComboBox<>(new String[]{
            "ADJUSTMENT_DAMAGE", "ADJUSTMENT_LOST", "ADJUSTMENT_CORRECTION", "ADJUSTMENT_FOUND", "ADJUSTMENT_MANUAL"
        });
        JTextField txtDelta = new JTextField("0");
        JTextArea txtReason = new JTextArea(3, 15);
        txtReason.setLineWrap(true);

        form.add(new JLabel("Product Variant:"));
        form.add(new JLabel(pName + " [" + size + "/" + colour + "]"));
        form.add(new JLabel("Current Stock Level:"));
        form.add(new JLabel(String.valueOf(curStock)));
        form.add(new JLabel("Adjustment Type:"));
        form.add(cmbType);
        form.add(new JLabel("Quantity Change (+ or -):"));
        form.add(txtDelta);
        form.add(new JLabel("Mandatory Reason:"));
        form.add(new JScrollPane(txtReason));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.COLOR_HEADER_BG);
        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Save Adjustment", ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String reason = txtReason.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "A reason must be provided for stock adjustments.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int delta;
            try {
                delta = Integer.parseInt(txtDelta.getText().trim());
                if (delta == 0) throw new IllegalArgumentException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter a valid non-zero integer quantity change.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String type = (String) cmbType.getSelectedItem();
                inventoryService.adjustStock(variantId, type, delta, reason);
                JOptionPane.showMessageDialog(dlg, "Stock successfully adjusted.", "Adjustment Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshStock();
                refreshMovements();
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

    /**
     * Requirement 19: Stock Receiving Dialog
     * Fields: Supplier, Date, Reference number, Product, Variant, Quantity, Purchase price, Notes.
     * When stock receiving is saved: Increase inventory, Record a stock movement.
     */
    private void openStockReceivingDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Stock Receiving - Supplier Inward Batch", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        JComboBox<Supplier> cmbSupplier = new JComboBox<>();
        try {
            for (Supplier s : inventoryService.getAllSuppliers()) if (s.isActive()) cmbSupplier.addItem(s);
        } catch (Exception ignored) {}

        JTextField txtRef = new JTextField("PO-" + System.currentTimeMillis() % 100000);
        JComboBox<ProductVariant> cmbVariants = new JComboBox<>();
        try {
            for (ProductVariant v : productService.searchVariants("")) cmbVariants.addItem(v);
        } catch (Exception ignored) {}

        JTextField txtQty = new JTextField("10");
        JTextField txtCost = new JTextField("2500.00");
        JTextField txtNotes = new JTextField("Inward delivery batch inspection passed");

        form.add(new JLabel("Supplier:")); form.add(cmbSupplier);
        form.add(new JLabel("Purchase Order / Ref #:")); form.add(txtRef);
        form.add(new JLabel("Apparel Item & Variant:")); form.add(cmbVariants);
        form.add(new JLabel("Receiving Quantity:")); form.add(txtQty);
        form.add(new JLabel("Unit Cost Price (Rs.):")); form.add(txtCost);
        form.add(new JLabel("Receiving Notes:")); form.add(txtNotes);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.COLOR_HEADER_BG);
        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Confirm Receipt & Add Stock", ModernButton.ButtonType.SUCCESS);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            ProductVariant selected = (ProductVariant) cmbVariants.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dlg, "Please select an apparel variant.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int qty;
            double cost;
            try {
                qty = Integer.parseInt(txtQty.getText().trim());
                cost = Double.parseDouble(txtCost.getText().trim());
                if (qty <= 0) throw new IllegalArgumentException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter valid quantity and cost values.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                StockReceipt receipt = new StockReceipt();
                receipt.setReceiptNumber("REC-" + FormatUtil.getInvoiceDateCode(new Date()) + "-" + (System.currentTimeMillis() % 10000));
                Supplier s = (Supplier) cmbSupplier.getSelectedItem();
                if (s != null) receipt.setSupplierId(s.getSupplierId());
                receipt.setReceiptDate(new Date());
                receipt.setTotalAmount(qty * cost);
                receipt.setReferenceNumber(txtRef.getText().trim());
                receipt.setNotes(txtNotes.getText().trim());

                List<StockReceiptItem> items = new ArrayList<>();
                items.add(new StockReceiptItem(selected.getVariantId(), qty, cost, qty * cost));

                inventoryService.processStockReceipt(receipt, items);
                JOptionPane.showMessageDialog(dlg, "Stock receiving saved! " + qty + " units added to inventory.", "Inward Stock Received", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshStock();
                refreshReceipts();
                refreshMovements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error saving stock receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}

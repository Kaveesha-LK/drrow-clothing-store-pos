package com.drrow.pos.ui;

import com.drrow.pos.controller.POSController;
import com.drrow.pos.model.*;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class POSPanel extends JPanel implements POSController.CartChangeListener {

    private final POSController controller = new POSController();
    private final ProductService productService = new ProductService();

    // Barcode & Product Search controls
    private final JTextField txtBarcode = new JTextField();
    private final SearchField txtSearch = new SearchField("Search item, name, or SKU...");
    private final JComboBox<Category> cmbCategory = new JComboBox<>();

    // Catalog & Cart Tables
    private final DefaultTableModel catalogTableModel = new DefaultTableModel(
        new String[]{"Barcode", "Item Name", "Size", "Colour", "Price", "Stock"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final ModernTable catalogTable = new ModernTable(catalogTableModel);

    private final DefaultTableModel cartTableModel = new DefaultTableModel(
        new String[]{"Barcode", "Item", "Size", "Colour", "Qty", "Unit Price", "Discount", "Total"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final ModernTable cartTable = new ModernTable(cartTableModel);

    // Totals & Payment UI
    private final JLabel lblSubtotalVal = new JLabel("Rs. 0.00");
    private final JLabel lblDiscountVal = new JLabel("-Rs. 0.00");
    private final JLabel lblTaxVal = new JLabel("Rs. 0.00");
    private final JLabel lblGrandTotalVal = new JLabel("Rs. 0.00");

    private final JComboBox<String> cmbPaymentMethod = new JComboBox<>(new String[]{"CASH", "CARD", "BANK_TRANSFER", "OTHER"});
    private final JTextField txtPaidAmount = new JTextField("0.00", 8);
    private final JLabel lblChangeVal = new JLabel("Rs. 0.00");
    private final JLabel lblCustomerBadge = new JLabel("Customer: Walk-In Customer");

    private Sale lastCompletedSale = null;

    public POSPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        controller.addCartChangeListener(this);

        initLeftCatalogPanel();
        initRightCartPanel();

        loadCategories();
        refreshCatalog(null);

        // Ensure barcode input is focused
        SwingUtilities.invokeLater(this::focusBarcodeInput);
    }

    private void focusBarcodeInput() {
        txtBarcode.requestFocusInWindow();
    }

    private void initLeftCatalogPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(UITheme.COLOR_BG_DARK);
        leftPanel.setPreferredSize(new Dimension(500, 700));

        // Scan & Search Box
        JPanel searchCard = new JPanel(new BorderLayout(8, 8));
        searchCard.setBackground(UITheme.COLOR_CARD_BG);
        searchCard.setBorder(UITheme.createCardBorder());

        // Barcode Scanner Input Row (Top Priority)
        JPanel barcodeRow = new JPanel(new BorderLayout(8, 0));
        barcodeRow.setOpaque(false);
        JLabel lblBarcodeIcon = new JLabel("SCAN BARCODE:");
        lblBarcodeIcon.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBarcodeIcon.setForeground(UITheme.COLOR_GOLD);

        txtBarcode.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtBarcode.setBackground(UITheme.COLOR_INPUT_BG);
        txtBarcode.setForeground(Color.WHITE);
        txtBarcode.setCaretColor(Color.WHITE);
        txtBarcode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_GOLD, 2),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // USB HID Scanner Enter Key Listener
        txtBarcode.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processBarcodeScan();
                }
            }
        });

        barcodeRow.add(lblBarcodeIcon, BorderLayout.WEST);
        barcodeRow.add(txtBarcode, BorderLayout.CENTER);

        // Search & Category Filter Row
        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setOpaque(false);

        cmbCategory.setFont(UITheme.FONT_REGULAR);
        cmbCategory.setBackground(UITheme.COLOR_INPUT_BG);
        cmbCategory.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        cmbCategory.setPreferredSize(new Dimension(140, 36));

        cmbCategory.addActionListener(e -> {
            Category selected = (Category) cmbCategory.getSelectedItem();
            refreshCatalog(selected != null && selected.getCategoryId() > 0 ? selected.getCategoryId() : null);
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { doSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { doSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { doSearch(); }
            private void doSearch() {
                Category selected = (Category) cmbCategory.getSelectedItem();
                refreshCatalog(selected != null && selected.getCategoryId() > 0 ? selected.getCategoryId() : null);
            }
        });

        filterRow.add(txtSearch, BorderLayout.CENTER);
        filterRow.add(cmbCategory, BorderLayout.EAST);

        searchCard.add(barcodeRow, BorderLayout.NORTH);
        searchCard.add(filterRow, BorderLayout.SOUTH);
        leftPanel.add(searchCard, BorderLayout.NORTH);

        // Catalog Table
        catalogTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedCatalogItemToCart();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Quick Add Button
        JPanel bottomCatalog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomCatalog.setOpaque(false);
        ModernButton btnAddToCart = new ModernButton("+ Add Selected to Cart", ModernButton.ButtonType.GOLD);
        btnAddToCart.addActionListener(e -> addSelectedCatalogItemToCart());
        bottomCatalog.add(btnAddToCart);
        leftPanel.add(bottomCatalog, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
    }

    private void initRightCartPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(UITheme.COLOR_BG_DARK);

        // Header with Customer Badge and Cart Actions
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setBackground(UITheme.COLOR_CARD_BG);
        cartHeader.setBorder(UITheme.createCardBorder());

        JLabel lblCartTitle = new JLabel("SHOPPING CART");
        lblCartTitle.setFont(UITheme.FONT_SUBTITLE);
        lblCartTitle.setForeground(UITheme.COLOR_GOLD);

        lblCustomerBadge.setFont(UITheme.FONT_BOLD);
        lblCustomerBadge.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JPanel custPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        custPanel.setOpaque(false);
        custPanel.add(lblCustomerBadge);

        ModernButton btnSelectCustomer = new ModernButton("Select Customer", ModernButton.ButtonType.SECONDARY);
        btnSelectCustomer.addActionListener(e -> selectCustomerDialog());
        custPanel.add(btnSelectCustomer);

        cartHeader.add(lblCartTitle, BorderLayout.WEST);
        cartHeader.add(custPanel, BorderLayout.EAST);
        rightPanel.add(cartHeader, BorderLayout.NORTH);

        // Cart Table with line items
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        rightPanel.add(cartScroll, BorderLayout.CENTER);

        // Bottom Payment & Checkout Control Panel
        JPanel checkoutContainer = new JPanel(new BorderLayout(0, 8));
        checkoutContainer.setBackground(UITheme.COLOR_CARD_BG);
        checkoutContainer.setBorder(UITheme.createCardBorder());

        // Cart Operations Row: (+ Qty, - Qty, Remove, Clear, Discount, Hold, Resume)
        JPanel cartOpsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cartOpsRow.setOpaque(false);

        ModernButton btnInc = new ModernButton("+ Qty", ModernButton.ButtonType.SECONDARY);
        ModernButton btnDec = new ModernButton("- Qty", ModernButton.ButtonType.SECONDARY);
        ModernButton btnRemove = new ModernButton("Remove Item", ModernButton.ButtonType.DANGER);
        ModernButton btnClear = new ModernButton("Clear Cart", ModernButton.ButtonType.SECONDARY);
        ModernButton btnDiscount = new ModernButton("Apply Discount", ModernButton.ButtonType.WARNING);
        ModernButton btnHold = new ModernButton("Hold Sale", ModernButton.ButtonType.SECONDARY);
        ModernButton btnResume = new ModernButton("Resume Sale", ModernButton.ButtonType.SECONDARY);

        btnInc.addActionListener(e -> modifySelectedCartQuantity(1));
        btnDec.addActionListener(e -> modifySelectedCartQuantity(-1));
        btnRemove.addActionListener(e -> removeSelectedCartItem());
        btnClear.addActionListener(e -> controller.clearCart());
        btnDiscount.addActionListener(e -> promptDiscountDialog());
        btnHold.addActionListener(e -> holdCurrentSale());
        btnResume.addActionListener(e -> resumeHeldSale());

        cartOpsRow.add(btnInc);
        cartOpsRow.add(btnDec);
        cartOpsRow.add(btnRemove);
        cartOpsRow.add(btnClear);
        cartOpsRow.add(btnDiscount);
        cartOpsRow.add(btnHold);
        cartOpsRow.add(btnResume);

        // Summary & Payment Row
        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 12, 6));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(8, 4, 8, 4));

        lblSubtotalVal.setFont(UITheme.FONT_SUBTITLE);
        lblSubtotalVal.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        lblDiscountVal.setFont(UITheme.FONT_SUBTITLE);
        lblDiscountVal.setForeground(UITheme.COLOR_DANGER);

        lblTaxVal.setFont(UITheme.FONT_SUBTITLE);
        lblTaxVal.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        lblGrandTotalVal.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblGrandTotalVal.setForeground(UITheme.COLOR_GOLD);

        summaryPanel.add(createSummaryField("Subtotal", lblSubtotalVal));
        summaryPanel.add(createSummaryField("Discount", lblDiscountVal));
        summaryPanel.add(createSummaryField("Tax", lblTaxVal));
        summaryPanel.add(createSummaryField("GRAND TOTAL", lblGrandTotalVal));

        // Payment input row
        styleTextField(txtPaidAmount);
        txtPaidAmount.setFont(new Font("SansSerif", Font.BOLD, 16));
        txtPaidAmount.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateChange(); }
            @Override public void removeUpdate(DocumentEvent e) { updateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { updateChange(); }
        });

        lblChangeVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblChangeVal.setForeground(UITheme.COLOR_SUCCESS);

        cmbPaymentMethod.setFont(UITheme.FONT_BOLD);
        cmbPaymentMethod.setBackground(UITheme.COLOR_INPUT_BG);
        cmbPaymentMethod.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        summaryPanel.add(createSummaryComponent("Payment Method", cmbPaymentMethod));
        summaryPanel.add(createSummaryComponent("Paid Amount", txtPaidAmount));
        summaryPanel.add(createSummaryField("Change Given", lblChangeVal));

        // Action Buttons Row: Complete Sale, Print Bill, Cancel
        JPanel checkoutActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        checkoutActions.setOpaque(false);

        ModernButton btnNewSale = new ModernButton("New Sale", ModernButton.ButtonType.SECONDARY);
        ModernButton btnReprint = new ModernButton("Print Last Bill", ModernButton.ButtonType.SECONDARY);
        ModernButton btnComplete = new ModernButton("Complete Sale & Print", ModernButton.ButtonType.SUCCESS);
        btnComplete.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnNewSale.addActionListener(e -> controller.clearCart());
        btnReprint.addActionListener(e -> reprintLastBill());
        btnComplete.addActionListener(e -> completeCheckout());

        checkoutActions.add(btnNewSale);
        checkoutActions.add(btnReprint);
        checkoutActions.add(btnComplete);

        checkoutContainer.add(cartOpsRow, BorderLayout.NORTH);
        checkoutContainer.add(summaryPanel, BorderLayout.CENTER);
        checkoutContainer.add(checkoutActions, BorderLayout.SOUTH);

        rightPanel.add(checkoutContainer, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryField(String label, JLabel valLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.COLOR_TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSummaryComponent(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.COLOR_TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(UITheme.COLOR_INPUT_BG);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void loadCategories() {
        try {
            cmbCategory.removeAllItems();
            cmbCategory.addItem(new Category(0, "All Categories", "", true));
            List<Category> cats = productService.getAllCategories();
            for (Category c : cats) {
                if (c.isActive()) {
                    cmbCategory.addItem(c);
                }
            }
        } catch (Exception ignored) {}
    }

    private void refreshCatalog(Integer categoryId) {
        catalogTableModel.setRowCount(0);
        try {
            String query = txtSearch.getText().trim();
            List<ProductVariant> variants = productService.searchVariants(query);
            for (ProductVariant v : variants) {
                catalogTableModel.addRow(new Object[]{
                    v.getBarcode(),
                    v.getProductName(),
                    v.getSizeName(),
                    v.getColourName(),
                    FormatUtil.formatDecimal(v.getSellingPrice()),
                    v.getCurrentStock()
                });
            }
        } catch (Exception ex) {
            System.err.println("Catalog refresh error: " + ex.getMessage());
        }
    }

    /**
     * Requirement 12: Handles scanning a barcode into the POS input box.
     */
    private void processBarcodeScan() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        try {
            ProductVariant v = controller.handleBarcodeScan(barcode);
            txtBarcode.setText("");
            focusBarcodeInput();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Product Not Found", JOptionPane.WARNING_MESSAGE);
            txtBarcode.selectAll();
            focusBarcodeInput();
        }
    }

    private void addSelectedCatalogItemToCart() {
        int row = catalogTable.getSelectedRow();
        if (row >= 0) {
            String barcode = (String) catalogTableModel.getValueAt(row, 0);
            try {
                controller.handleBarcodeScan(barcode);
                focusBarcodeInput();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modifySelectedCartQuantity(int delta) {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            CartItem ci = controller.getCartItems().get(row);
            controller.updateQuantity(ci.getVariantId(), ci.getQuantity() + delta);
            focusBarcodeInput();
        }
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            CartItem ci = controller.getCartItems().get(row);
            controller.removeItem(ci.getVariantId());
            focusBarcodeInput();
        }
    }

    private void promptDiscountDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter overall discount amount (Rs.):",
                String.valueOf(controller.getDiscountAmount()));
        if (input != null && !input.trim().isEmpty()) {
            try {
                double discount = Double.parseDouble(input.trim());
                controller.applyDiscount(discount);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric discount.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
            }
        }
        focusBarcodeInput();
    }

    private void holdCurrentSale() {
        if (controller.getCartItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Nothing to hold.", "Hold Sale", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String key = "HOLD-" + System.currentTimeMillis() % 10000;
        controller.holdCart(key);
        JOptionPane.showMessageDialog(this, "Sale parked as reference: " + key, "Sale Held", JOptionPane.INFORMATION_MESSAGE);
        focusBarcodeInput();
    }

    private void resumeHeldSale() {
        java.util.Set<String> keys = controller.getCartItems().isEmpty() ? null : null;
        // Let user choose from active held sales
        java.util.Map<String, List<CartItem>> held = new com.drrow.pos.service.SalesService().getHeldSales();
        if (held.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No held sales found.", "Resume Sale", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] options = held.keySet().toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this, "Select held sale to resume:", "Resume Sale",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (selected != null) {
            controller.resumeCart(selected);
        }
        focusBarcodeInput();
    }

    private void selectCustomerDialog() {
        try {
            List<Customer> customers = productService.searchProducts("", 0, 0) != null ?
                    new com.drrow.pos.dao.CustomerDAO().findAll() : null;
            if (customers != null && !customers.isEmpty()) {
                Customer selected = (Customer) JOptionPane.showInputDialog(this, "Select customer for this sale:",
                        "Customer Assignment", JOptionPane.PLAIN_MESSAGE, null, customers.toArray(), customers.get(0));
                if (selected != null) {
                    controller.setCustomer(selected.getCustomerId(), selected.getName());
                    lblCustomerBadge.setText("Customer: " + selected.getName());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load customer list: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        focusBarcodeInput();
    }

    private void updateChange() {
        try {
            double paid = Double.parseDouble(txtPaidAmount.getText().trim());
            double change = controller.calculateChange(paid);
            lblChangeVal.setText(FormatUtil.formatCurrency(change));
        } catch (NumberFormatException e) {
            lblChangeVal.setText("Rs. 0.00");
        }
    }

    private void completeCheckout() {
        if (controller.getCartItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot complete sale. The cart is empty.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            focusBarcodeInput();
            return;
        }

        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
        double paidAmount = 0.00;
        try {
            paidAmount = Double.parseDouble(txtPaidAmount.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric payment amount.", "Invalid Payment", JOptionPane.WARNING_MESSAGE);
            txtPaidAmount.requestFocusInWindow();
            return;
        }

        double grandTotal = controller.calculateGrandTotal();
        if ("CASH".equalsIgnoreCase(paymentMethod) && paidAmount < grandTotal) {
            JOptionPane.showMessageDialog(this, String.format("Insufficient payment amount!\nReceived: Rs. %.2f\nRequired: Rs. %.2f", paidAmount, grandTotal),
                    "Payment Error", JOptionPane.ERROR_MESSAGE);
            txtPaidAmount.requestFocusInWindow();
            return;
        }

        try {
            Sale completed = controller.completeSale(paymentMethod, paidAmount, "");
            this.lastCompletedSale = completed;

            // Show thermal bill preview and print dialog
            ReceiptPreviewDialog previewDialog = new ReceiptPreviewDialog(SwingUtilities.getWindowAncestor(this), completed);
            previewDialog.setVisible(true);

            // Reset UI
            txtPaidAmount.setText("0.00");
            refreshCatalog(null);
            focusBarcodeInput();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Checkout Failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            focusBarcodeInput();
        }
    }

    private void reprintLastBill() {
        if (lastCompletedSale != null) {
            ReceiptPreviewDialog previewDialog = new ReceiptPreviewDialog(SwingUtilities.getWindowAncestor(this), lastCompletedSale);
            previewDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No recent sale in current session to reprint.", "Reprint Bill", JOptionPane.INFORMATION_MESSAGE);
        }
        focusBarcodeInput();
    }

    @Override
    public void onCartChanged() {
        cartTableModel.setRowCount(0);
        for (CartItem ci : controller.getCartItems()) {
            cartTableModel.addRow(new Object[]{
                ci.getBarcode(),
                ci.getProductName(),
                ci.getSizeName(),
                ci.getColourName(),
                ci.getQuantity(),
                FormatUtil.formatDecimal(ci.getUnitPrice()),
                FormatUtil.formatDecimal(ci.getDiscountAmount()),
                FormatUtil.formatDecimal(ci.getLineTotal())
            });
        }

        lblSubtotalVal.setText(FormatUtil.formatCurrency(controller.calculateSubtotal()));
        lblDiscountVal.setText("-" + FormatUtil.formatCurrency(controller.getDiscountAmount()));
        lblTaxVal.setText(FormatUtil.formatCurrency(controller.calculateTax()));

        double grandTotal = controller.calculateGrandTotal();
        lblGrandTotalVal.setText(FormatUtil.formatCurrency(grandTotal));

        // Auto default paid amount to grand total if cash wasn't manually typed
        if ("0.00".equals(txtPaidAmount.getText().trim()) || txtPaidAmount.getText().trim().isEmpty()) {
            txtPaidAmount.setText(String.format("%.2f", grandTotal));
        }
        updateChange();
    }
}

package com.drrow.pos.ui;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductManagementPanel extends JPanel {

    private final ProductService productService = new ProductService();

    private final JTabbedPane tabbedPane = new JTabbedPane();

    // --- Tab 1: Products & Variants ---
    private final SearchField txtProdSearch = new SearchField("Search product name, item code...");
    private final DefaultTableModel productTableModel = new DefaultTableModel(
        new String[]{"ID", "Item Code", "Product Name", "Category", "Brand", "Cost Price", "Retail Price", "Variants", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable productTable = new ModernTable(productTableModel);

    private final DefaultTableModel variantTableModel = new DefaultTableModel(
        new String[]{"Variant ID", "Barcode", "SKU", "Size", "Colour", "Retail Price", "Stock"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable variantTable = new ModernTable(variantTableModel);

    // --- Tab 2: Categories ---
    private final DefaultTableModel categoryTableModel = new DefaultTableModel(new String[]{"ID", "Category Name", "Description", "Active"}, 0);
    private final ModernTable categoryTable = new ModernTable(categoryTableModel);

    // --- Tab 3: Brands ---
    private final DefaultTableModel brandTableModel = new DefaultTableModel(new String[]{"ID", "Brand Name", "Description", "Active"}, 0);
    private final ModernTable brandTable = new ModernTable(brandTableModel);

    // --- Tab 4: Sizes ---
    private final DefaultTableModel sizeTableModel = new DefaultTableModel(new String[]{"ID", "Size Name", "Display Order", "Active"}, 0);
    private final ModernTable sizeTable = new ModernTable(sizeTableModel);

    // --- Tab 5: Colours ---
    private final DefaultTableModel colourTableModel = new DefaultTableModel(new String[]{"ID", "Colour Name", "Hex Code", "Active"}, 0);
    private final ModernTable colourTable = new ModernTable(colourTableModel);

    public ProductManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.COLOR_CARD_BG);
        tabbedPane.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        initProductsTab();
        initCategoriesTab();
        initBrandsTab();
        initSizesTab();
        initColoursTab();

        add(tabbedPane, BorderLayout.CENTER);

        refreshProducts();
    }

    private void initProductsTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        txtProdSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshProducts(); }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);
        ModernButton btnNewProd = new ModernButton("+ New Clothing Product", ModernButton.ButtonType.GOLD);
        ModernButton btnEditProd = new ModernButton("Edit Product", ModernButton.ButtonType.SECONDARY);
        ModernButton btnDeleteProd = new ModernButton("Delete Product", ModernButton.ButtonType.DANGER);

        btnNewProd.addActionListener(e -> openProductEditorDialog(null));
        btnEditProd.addActionListener(e -> editSelectedProduct());
        btnDeleteProd.addActionListener(e -> deleteSelectedProduct());

        btnBar.add(btnNewProd);
        btnBar.add(btnEditProd);
        btnBar.add(btnDeleteProd);

        topBar.add(txtProdSearch, BorderLayout.CENTER);
        topBar.add(btnBar, BorderLayout.EAST);
        pnl.add(topBar, BorderLayout.NORTH);

        // Center split pane: Upper Products, Lower Variants
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshVariantsForSelectedProduct();
            }
        });

        JScrollPane prodScroll = new JScrollPane(productTable);
        prodScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Products Catalogue", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        JScrollPane varScroll = new JScrollPane(variantTable);
        varScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Apparel Variants (Size & Colour Matrix)", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        splitPane.setTopComponent(prodScroll);
        splitPane.setBottomComponent(varScroll);

        pnl.add(splitPane, BorderLayout.CENTER);
        tabbedPane.addTab("Products & Variants", pnl);
    }

    private void refreshProducts() {
        productTableModel.setRowCount(0);
        try {
            String query = txtProdSearch.getText().trim();
            List<Product> products = productService.searchProducts(query, null, null);
            for (Product p : products) {
                List<ProductVariant> vars = productService.getVariantsForProduct(p.getProductId());
                productTableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getItemCode(),
                    p.getName(),
                    p.getCategoryName(),
                    p.getBrandName(),
                    FormatUtil.formatDecimal(p.getPurchasePrice()),
                    FormatUtil.formatDecimal(p.getSellingPrice()),
                    vars.size() + " variants",
                    p.isActive() ? "Active" : "Inactive"
                });
            }
        } catch (Exception ex) {
            System.err.println("Error refreshing products: " + ex.getMessage());
        }
    }

    private void refreshVariantsForSelectedProduct() {
        variantTableModel.setRowCount(0);
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int prodId = (Integer) productTableModel.getValueAt(row, 0);
            try {
                List<ProductVariant> vars = productService.getVariantsForProduct(prodId);
                for (ProductVariant v : vars) {
                    variantTableModel.addRow(new Object[]{
                        v.getVariantId(),
                        v.getBarcode(),
                        v.getSku(),
                        v.getSizeName(),
                        v.getColourName(),
                        FormatUtil.formatDecimal(v.getSellingPrice()),
                        v.getCurrentStock()
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private void editSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int prodId = (Integer) productTableModel.getValueAt(row, 0);
            try {
                Product p = productService.getProductById(prodId);
                if (p != null) {
                    openProductEditorDialog(p);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int prodId = (Integer) productTableModel.getValueAt(row, 0);
            String name = (String) productTableModel.getValueAt(row, 2);
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete '" + name + "' and its variants?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    productService.deleteProduct(prodId);
                    refreshProducts();
                    variantTableModel.setRowCount(0);
                    JOptionPane.showMessageDialog(this, "Product deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Complete Product & Variant Creation Modal Dialog.
     * Generates clothing variants with unique barcodes and sets initial inventory stock!
     */
    private void openProductEditorDialog(Product existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Create New D’RROW Product & Variants" : "Edit Product", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(680, 620);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 8));
        form.setBackground(UITheme.COLOR_CARD_BG);
        form.setBorder(UITheme.createCardBorder());

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JComboBox<Category> cmbCat = new JComboBox<>();
        JComboBox<Brand> cmbBr = new JComboBox<>();
        JTextField txtCost = new JTextField("0.00");
        JTextField txtRetail = new JTextField("0.00");
        JTextField txtReorder = new JTextField("5");
        JTextField txtInitialStock = new JTextField("20");

        try {
            for (Category c : productService.getAllCategories()) if (c.isActive()) cmbCat.addItem(c);
            for (Brand b : productService.getAllBrands()) if (b.isActive()) cmbBr.addItem(b);
        } catch (Exception ignored) {}

        if (existing == null) {
            try {
                txtCode.setText(productService.generateNextItemCode("DRTS"));
            } catch (Exception ignored) {}
        } else {
            txtCode.setText(existing.getItemCode());
            txtName.setText(existing.getName());
            txtCost.setText(String.valueOf(existing.getPurchasePrice()));
            txtRetail.setText(String.valueOf(existing.getSellingPrice()));
            txtReorder.setText(String.valueOf(existing.getReorderLevel()));
        }

        form.add(new JLabel("Item Code / SKU Prefix:")); form.add(txtCode);
        form.add(new JLabel("Product Name:")); form.add(txtName);
        form.add(new JLabel("Category:")); form.add(cmbCat);
        form.add(new JLabel("Brand:")); form.add(cmbBr);
        form.add(new JLabel("Cost / Purchase Price (Rs.):")); form.add(txtCost);
        form.add(new JLabel("Selling Retail Price (Rs.):")); form.add(txtRetail);

        // Variant Matrix Options Panel
        JPanel matrixPanel = new JPanel(new BorderLayout(8, 8));
        matrixPanel.setBackground(UITheme.COLOR_CARD_BG);
        matrixPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Select Sizes & Colours for Variant Generation", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        JPanel sizeChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sizeChecks.setOpaque(false);
        List<JCheckBox> sizeBoxes = new ArrayList<>();
        try {
            for (Size s : productService.getAllSizes()) {
                JCheckBox cb = new JCheckBox(s.getSizeName(), s.getSizeName().equals("M") || s.getSizeName().equals("L"));
                cb.putClientProperty("sizeId", s.getSizeId());
                cb.setForeground(Color.WHITE);
                cb.setBackground(UITheme.COLOR_CARD_BG);
                sizeBoxes.add(cb);
                sizeChecks.add(cb);
            }
        } catch (Exception ignored) {}

        JPanel colourChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        colourChecks.setOpaque(false);
        List<JCheckBox> colourBoxes = new ArrayList<>();
        try {
            for (Colour c : productService.getAllColours()) {
                JCheckBox cb = new JCheckBox(c.getColourName(), c.getColourName().equals("Black") || c.getColourName().equals("White"));
                cb.putClientProperty("colourId", c.getColourId());
                cb.setForeground(Color.WHITE);
                cb.setBackground(UITheme.COLOR_CARD_BG);
                colourBoxes.add(cb);
                colourChecks.add(cb);
            }
        } catch (Exception ignored) {}

        JPanel checkContainer = new JPanel(new GridLayout(2, 1));
        checkContainer.setOpaque(false);
        checkContainer.add(sizeChecks);
        checkContainer.add(colourChecks);

        JPanel stockRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        stockRow.setOpaque(false);
        stockRow.add(new JLabel("Initial Stock Per Variant:"));
        stockRow.add(txtInitialStock);
        txtInitialStock.setPreferredSize(new Dimension(80, 28));

        matrixPanel.add(checkContainer, BorderLayout.CENTER);
        matrixPanel.add(stockRow, BorderLayout.SOUTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.COLOR_HEADER_BG);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Save & Generate Barcodes", ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String code = txtCode.getText().trim();
            if (name.isEmpty() || code.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Product Name and Item Code are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double cost = FormatUtil.parseAmount(txtCost.getText());
            double retail = FormatUtil.parseAmount(txtRetail.getText());
            int initStock = 0;
            try { initStock = Integer.parseInt(txtInitialStock.getText().trim()); } catch (Exception ignored) {}

            Product p = existing != null ? existing : new Product();
            p.setName(name);
            p.setItemCode(code);
            Category cat = (Category) cmbCat.getSelectedItem();
            if (cat != null) p.setCategoryId(cat.getCategoryId());
            Brand br = (Brand) cmbBr.getSelectedItem();
            if (br != null) p.setBrandId(br.getBrandId());
            p.setPurchasePrice(cost);
            p.setSellingPrice(retail);
            p.setActive(true);

            // Generate variants from selected sizes and colours
            List<ProductVariant> variants = new ArrayList<>();
            try {
                for (JCheckBox sb : sizeBoxes) {
                    if (sb.isSelected()) {
                        int sizeId = (Integer) sb.getClientProperty("sizeId");
                        String sizeName = sb.getText();
                        for (JCheckBox cb : colourBoxes) {
                            if (cb.isSelected()) {
                                int colourId = (Integer) cb.getClientProperty("colourId");
                                String colName = cb.getText();

                                ProductVariant v = new ProductVariant();
                                v.setSizeId(sizeId);
                                v.setColourId(colourId);
                                v.setPurchasePrice(cost);
                                v.setSellingPrice(retail);
                                v.setActive(true);

                                String colCode = colName.length() >= 3 ? colName.substring(0, 3).toUpperCase() : colName.toUpperCase();
                                v.setSku(code + "-" + colCode + "-" + sizeName);
                                v.setBarcode(productService.generateNextBarcode("DRTS"));
                                variants.add(v);
                            }
                        }
                    }
                }

                if (variants.isEmpty() && existing == null) {
                    JOptionPane.showMessageDialog(dlg, "Please select at least one Size and one Colour to generate variants.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                productService.saveProductWithVariants(p, variants, initStock);
                JOptionPane.showMessageDialog(dlg, "Product and " + variants.size() + " variants saved with unique barcodes!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshProducts();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Failed to save product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        if (existing == null) {
            centerPanel.add(matrixPanel, BorderLayout.CENTER);
        }

        dlg.add(centerPanel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // --- Tab 2: Categories Tab ---
    private void initCategoriesTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        ModernButton btnAdd = new ModernButton("+ Add Category", ModernButton.ButtonType.GOLD);
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Category Name:");
            if (name != null && !name.trim().isEmpty()) {
                try {
                    productService.saveCategory(new Category(0, name.trim(), "", true));
                    refreshCategories();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        top.add(btnAdd);
        pnl.add(top, BorderLayout.NORTH);
        pnl.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        tabbedPane.addTab("Categories", pnl);
        refreshCategories();
    }

    private void refreshCategories() {
        categoryTableModel.setRowCount(0);
        try {
            for (Category c : productService.getAllCategories()) {
                categoryTableModel.addRow(new Object[]{c.getCategoryId(), c.getName(), c.getDescription(), c.isActive() ? "Yes" : "No"});
            }
        } catch (Exception ignored) {}
    }

    // --- Tab 3: Brands Tab ---
    private void initBrandsTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        ModernButton btnAdd = new ModernButton("+ Add Brand", ModernButton.ButtonType.GOLD);
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Brand Name:");
            if (name != null && !name.trim().isEmpty()) {
                try {
                    productService.saveBrand(new Brand(0, name.trim(), "", true));
                    refreshBrands();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        top.add(btnAdd);
        pnl.add(top, BorderLayout.NORTH);
        pnl.add(new JScrollPane(brandTable), BorderLayout.CENTER);
        tabbedPane.addTab("Brands", pnl);
        refreshBrands();
    }

    private void refreshBrands() {
        brandTableModel.setRowCount(0);
        try {
            for (Brand b : productService.getAllBrands()) {
                brandTableModel.addRow(new Object[]{b.getBrandId(), b.getName(), b.getDescription(), b.isActive() ? "Yes" : "No"});
            }
        } catch (Exception ignored) {}
    }

    // --- Tab 4: Sizes Tab ---
    private void initSizesTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        ModernButton btnAdd = new ModernButton("+ Add Size", ModernButton.ButtonType.GOLD);
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Size Label (e.g. 4XL, 36, Slim):");
            if (name != null && !name.trim().isEmpty()) {
                try {
                    productService.saveSize(new Size(0, name.trim(), 90, true));
                    refreshSizes();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        top.add(btnAdd);
        pnl.add(top, BorderLayout.NORTH);
        pnl.add(new JScrollPane(sizeTable), BorderLayout.CENTER);
        tabbedPane.addTab("Sizes", pnl);
        refreshSizes();
    }

    private void refreshSizes() {
        sizeTableModel.setRowCount(0);
        try {
            for (Size s : productService.getAllSizes()) {
                sizeTableModel.addRow(new Object[]{s.getSizeId(), s.getSizeName(), s.getDisplayOrder(), s.isActive() ? "Yes" : "No"});
            }
        } catch (Exception ignored) {}
    }

    // --- Tab 5: Colours Tab ---
    private void initColoursTab() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(UITheme.COLOR_BG_DARK);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        ModernButton btnAdd = new ModernButton("+ Add Colour", ModernButton.ButtonType.GOLD);
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Colour Name (e.g. Maroon, Charcoal):");
            if (name != null && !name.trim().isEmpty()) {
                try {
                    productService.saveColour(new Colour(0, name.trim(), "#000000", true));
                    refreshColours();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        top.add(btnAdd);
        pnl.add(top, BorderLayout.NORTH);
        pnl.add(new JScrollPane(colourTable), BorderLayout.CENTER);
        tabbedPane.addTab("Colours", pnl);
        refreshColours();
    }

    private void refreshColours() {
        colourTableModel.setRowCount(0);
        try {
            for (Colour c : productService.getAllColours()) {
                colourTableModel.addRow(new Object[]{c.getColourId(), c.getColourName(), c.getHexCode(), c.isActive() ? "Yes" : "No"});
            }
        } catch (Exception ignored) {}
    }
}

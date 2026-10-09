package com.drrow.pos.ui;

import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.service.BarcodeService;
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
import java.awt.image.BufferedImage;
import java.util.List;

public class BarcodePrintPanel extends JPanel {

    private final ProductService productService = new ProductService();
    private final BarcodeService barcodeService = new BarcodeService();

    private final SearchField txtSearch = new SearchField("Search item by name, SKU, or barcode...");
    private final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JComboBox<String> cmbPrintMode = new JComboBox<>(new String[]{"Roll Label (50x30mm)", "A4 Sheet (21 Tags/Sheet)"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Barcode", "Item Name", "SKU", "Size", "Colour", "Selling Price", "Stock"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final ModernTable variantTable = new ModernTable(tableModel);

    // Live preview canvas
    private ProductVariant selectedVariant = null;
    private BufferedImage currentPreviewImage = null;
    private final JPanel previewCanvas = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentPreviewImage != null) {
                int x = (getWidth() - currentPreviewImage.getWidth()) / 2;
                int y = (getHeight() - currentPreviewImage.getHeight()) / 2;
                g.drawImage(currentPreviewImage, Math.max(0, x), Math.max(0, y), null);
            } else {
                g.setColor(UITheme.COLOR_TEXT_MUTED);
                g.setFont(UITheme.FONT_REGULAR);
                String msg = "Select an apparel variant from the table to preview tag";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            }
        }
    };

    public BarcodePrintPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        initLeftTablePanel();
        initRightPreviewPanel();

        refreshVariantList();
    }

    private void initLeftTablePanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(UITheme.COLOR_BG_DARK);

        // Header and Search bar
        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setBackground(UITheme.COLOR_CARD_BG);
        headerPanel.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel("BARCODE TAG PRINTING MODULE");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshVariantList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshVariantList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshVariantList(); }
        });

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(txtSearch, BorderLayout.CENTER);
        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        variantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedVariantPreview();
            }
        });

        JScrollPane scroll = new JScrollPane(variantTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        leftPanel.add(scroll, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);
    }

    private void initRightPreviewPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
        rightPanel.setBackground(UITheme.COLOR_CARD_BG);
        rightPanel.setBorder(UITheme.createCardBorder());
        rightPanel.setPreferredSize(new Dimension(380, 600));

        JLabel lblPreviewTitle = new JLabel("TAG PREVIEW");
        lblPreviewTitle.setFont(UITheme.FONT_SUBTITLE);
        lblPreviewTitle.setForeground(UITheme.COLOR_GOLD);
        rightPanel.add(lblPreviewTitle, BorderLayout.NORTH);

        // Canvas inside bordered panel
        previewCanvas.setBackground(new Color(243, 244, 246));
        previewCanvas.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        rightPanel.add(previewCanvas, BorderLayout.CENTER);

        // Configuration Form (Quantity, Sheet mode, Print button)
        JPanel controlPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        controlPanel.setOpaque(false);

        JLabel lblQty = new JLabel("Number of Tags:");
        lblQty.setFont(UITheme.FONT_BOLD);
        lblQty.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblMode = new JLabel("Output Medium:");
        lblMode.setFont(UITheme.FONT_BOLD);
        lblMode.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        spnQuantity.setFont(UITheme.FONT_BOLD);
        cmbPrintMode.setFont(UITheme.FONT_REGULAR);
        cmbPrintMode.setBackground(UITheme.COLOR_INPUT_BG);
        cmbPrintMode.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        controlPanel.add(lblQty);
        controlPanel.add(spnQuantity);
        controlPanel.add(lblMode);
        controlPanel.add(cmbPrintMode);

        ModernButton btnPrint = new ModernButton("Print Tags", ModernButton.ButtonType.GOLD);
        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPrint.addActionListener(e -> executePrint());

        ModernButton btnDialogPreview = new ModernButton("Open Print Dialog", ModernButton.ButtonType.SECONDARY);
        btnDialogPreview.addActionListener(e -> openFullPreviewDialog());

        controlPanel.add(btnDialogPreview);
        controlPanel.add(btnPrint);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);
    }

    private void refreshVariantList() {
        tableModel.setRowCount(0);
        try {
            String query = txtSearch.getText().trim();
            List<ProductVariant> variants = productService.searchVariants(query);
            for (ProductVariant v : variants) {
                tableModel.addRow(new Object[]{
                    v.getBarcode(),
                    v.getProductName(),
                    v.getSku(),
                    v.getSizeName(),
                    v.getColourName(),
                    FormatUtil.formatDecimal(v.getSellingPrice()),
                    v.getCurrentStock()
                });
            }
        } catch (Exception ex) {
            System.err.println("Barcode panel error: " + ex.getMessage());
        }
    }

    private void updateSelectedVariantPreview() {
        int row = variantTable.getSelectedRow();
        if (row >= 0) {
            String barcode = (String) tableModel.getValueAt(row, 0);
            try {
                this.selectedVariant = productService.getVariantByBarcode(barcode);
                if (selectedVariant != null) {
                    this.currentPreviewImage = barcodeService.renderClothingTagPreview(selectedVariant, selectedVariant.getBrandName());
                } else {
                    this.currentPreviewImage = null;
                }
            } catch (Exception ex) {
                this.currentPreviewImage = null;
            }
        } else {
            this.selectedVariant = null;
            this.currentPreviewImage = null;
        }
        previewCanvas.repaint();
    }

    private void executePrint() {
        if (selectedVariant == null) {
            JOptionPane.showMessageDialog(this, "Please select an apparel variant from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (Integer) spnQuantity.getValue();
        boolean a4Mode = cmbPrintMode.getSelectedIndex() == 1;

        try {
            boolean ok = barcodeService.printClothingTags(selectedVariant, selectedVariant.getBrandName(), qty, a4Mode);
            if (ok) {
                JOptionPane.showMessageDialog(this, qty + " barcode label tags sent to printer.", "Print Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFullPreviewDialog() {
        if (selectedVariant == null) {
            JOptionPane.showMessageDialog(this, "Please select an apparel variant first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty = (Integer) spnQuantity.getValue();
        boolean a4Mode = cmbPrintMode.getSelectedIndex() == 1;
        BarcodeLabelPreviewDialog dlg = new BarcodeLabelPreviewDialog(SwingUtilities.getWindowAncestor(this), selectedVariant, qty, a4Mode);
        dlg.setVisible(true);
    }
}

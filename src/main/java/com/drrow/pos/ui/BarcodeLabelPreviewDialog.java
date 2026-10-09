package com.drrow.pos.ui;

import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.service.PrintService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BarcodeLabelPreviewDialog extends JDialog {

    private final ProductVariant variant;
    private final int quantity;
    private final boolean a4Mode;
    private final PrintService printService = new PrintService();

    public BarcodeLabelPreviewDialog(Window parent, ProductVariant variant, int quantity, boolean a4Mode) {
        super(parent, "D’RROW Clothing Tag Preview - " + variant.getBarcode(), ModalityType.APPLICATION_MODAL);
        this.variant = variant;
        this.quantity = quantity;
        this.a4Mode = a4Mode;
        initUI();
    }

    private void initUI() {
        setSize(420, 360);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_HEADER_BG);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("Clothing Tag Label Preview (" + (a4Mode ? "A4 Sheet Grid" : "Roll Label 50x30mm") + ")");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center preview
        BufferedImage tagImage = null;
        try {
            tagImage = printService.getClothingTagPreview(variant);
        } catch (Exception ignored) {}

        final BufferedImage finalImg = tagImage;
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalImg != null) {
                    int x = (getWidth() - finalImg.getWidth()) / 2;
                    int y = (getHeight() - finalImg.getHeight()) / 2;
                    g.drawImage(finalImg, x, y, null);
                }
            }
        };
        canvas.setBackground(new Color(240, 242, 245));
        add(canvas, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(UITheme.COLOR_HEADER_BG);

        ModernButton btnCancel = new ModernButton("Close", ModernButton.ButtonType.SECONDARY);
        ModernButton btnPrint = new ModernButton("Print " + quantity + " Label" + (quantity > 1 ? "s" : ""), ModernButton.ButtonType.GOLD);

        btnCancel.addActionListener(e -> dispose());
        btnPrint.addActionListener(e -> {
            try {
                boolean printed = printService.printClothingTags(variant, quantity, a4Mode);
                if (printed) {
                    JOptionPane.showMessageDialog(this, "Barcode tags sent to printer.", "Print Successful", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Printing error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnPrint);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}

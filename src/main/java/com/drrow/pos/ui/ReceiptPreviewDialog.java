package com.drrow.pos.ui;

import com.drrow.pos.model.Sale;
import com.drrow.pos.service.PrintService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ReceiptPreviewDialog extends JDialog {

    private final PrintService printService = new PrintService();
    private final Sale sale;

    public ReceiptPreviewDialog(Window parent, Sale sale) {
        super(parent, "D’RROW POS - Receipt Preview [" + sale.getInvoiceNumber() + "]", ModalityType.APPLICATION_MODAL);
        this.sale = sale;
        initUI();
    }

    private void initUI() {
        setSize(380, 680);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // Top Banner
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_HEADER_BG);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("Thermal Receipt (80mm)");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Scrollable Receipt Render
        BufferedImage receiptImage = null;
        try {
            receiptImage = printService.getReceiptPreview(sale);
        } catch (Exception e) {
            System.err.println("Preview error: " + e.getMessage());
        }

        final BufferedImage finalImg = receiptImage;
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalImg != null) {
                    int x = Math.max(0, (getWidth() - finalImg.getWidth()) / 2);
                    g.drawImage(finalImg, x, 10, null);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                if (finalImg != null) {
                    return new Dimension(finalImg.getWidth() + 30, finalImg.getHeight() + 30);
                }
                return new Dimension(320, 500);
            }
        };
        canvas.setBackground(new Color(240, 242, 245));

        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(UITheme.COLOR_HEADER_BG);

        ModernButton btnClose = new ModernButton("Close", ModernButton.ButtonType.SECONDARY);
        ModernButton btnPrint = new ModernButton("Print Bill", ModernButton.ButtonType.SUCCESS);

        btnClose.addActionListener(e -> dispose());
        btnPrint.addActionListener(e -> {
            try {
                boolean printed = printService.printReceipt(sale);
                if (printed) {
                    JOptionPane.showMessageDialog(this, "Receipt sent to printer successfully.", "Printed", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Printing Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnClose);
        bottomPanel.add(btnPrint);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}

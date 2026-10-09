package com.drrow.pos.util;

import com.drrow.pos.model.Sale;
import com.drrow.pos.model.SaleItem;
import com.drrow.pos.model.Payment;

import java.awt.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Java Printable implementation for printing 80mm thermal receipts and full bills.
 * Formatted with D'RROW branding, detailed line items, payments, and store policies.
 */
public class ReceiptPrinter implements Printable {

    private final Sale sale;
    private final Map<String, String> settings;
    private static final int RECEIPT_WIDTH = 220; // 80mm standard printable point width ~ 220pt

    public ReceiptPrinter(Sale sale, Map<String, String> settings) {
        this.sale = sale;
        this.settings = settings;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        drawReceipt(g2, RECEIPT_WIDTH);

        return PAGE_EXISTS;
    }

    /**
     * Renders receipt directly onto a Graphics2D context (for printing or preview rendering).
     */
    public int drawReceipt(Graphics2D g2, int width) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 15;
        int leftMargin = 8;
        int rightMargin = width - 8;
        int usableWidth = rightMargin - leftMargin;

        // Brand Title
        String storeName = getSetting("store.name", "D’RROW Clothing Store");
        String tagline = getSetting("store.tagline", "Modern Elegance & Premium Fashion");
        String address = getSetting("store.address", "No. 124, Galle Road, Colombo 03");
        String phone = getSetting("store.phone", "+94 11 234 5678");

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        drawCenteredString(g2, storeName, width, y);
        y += 14;

        g2.setFont(new Font("Monospaced", Font.ITALIC, 8));
        drawCenteredString(g2, tagline, width, y);
        y += 12;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        drawCenteredString(g2, address, width, y);
        y += 10;
        drawCenteredString(g2, "Tel: " + phone, width, y);
        y += 14;

        // Divider
        drawDashedLine(g2, leftMargin, rightMargin, y);
        y += 12;

        // Invoice Metadata
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.drawString("INVOICE: " + sale.getInvoiceNumber(), leftMargin, y);
        y += 11;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        String dateStr = FormatUtil.formatDate(sale.getSaleDate());
        String timeStr = FormatUtil.formatTime(sale.getSaleDate());
        g2.drawString("Date: " + dateStr + "  Time: " + timeStr, leftMargin, y);
        y += 10;

        String cashier = sale.getCashierName() != null ? sale.getCashierName() : "Cashier";
        g2.drawString("Cashier: " + cashier, leftMargin, y);
        y += 10;

        if (sale.getCustomerName() != null && !sale.getCustomerName().trim().isEmpty()) {
            g2.drawString("Customer: " + sale.getCustomerName(), leftMargin, y);
            y += 10;
        }

        drawDashedLine(g2, leftMargin, rightMargin, y);
        y += 12;

        // Table Header: Item | Qty | Price | Total
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("ITEM", leftMargin, y);
        g2.drawString("QTY", leftMargin + 95, y);
        g2.drawString("PRICE", leftMargin + 125, y);
        drawStringRight(g2, "TOTAL", rightMargin, y);
        y += 6;

        g2.drawLine(leftMargin, y, rightMargin, y);
        y += 10;

        // Line Items
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        if (sale.getItems() != null) {
            for (SaleItem item : sale.getItems()) {
                String name = item.getProductName();
                if (name.length() > 16) {
                    name = name.substring(0, 14) + "..";
                }
                g2.drawString(name, leftMargin, y);
                g2.drawString(String.valueOf(item.getQuantity()), leftMargin + 100, y);
                g2.drawString(FormatUtil.formatDecimal(item.getUnitPrice()), leftMargin + 125, y);
                drawStringRight(g2, FormatUtil.formatDecimal(item.getLineTotal()), rightMargin, y);
                y += 10;

                // Sub-line with Variant size & colour
                String varDesc = " [" + item.getSizeName() + " / " + item.getColourName() + "]";
                if (item.getDiscountAmount() > 0) {
                    varDesc += " (Disc: -" + FormatUtil.formatDecimal(item.getDiscountAmount()) + ")";
                }
                g2.setFont(new Font("Monospaced", Font.ITALIC, 7));
                g2.drawString(varDesc, leftMargin + 4, y);
                g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
                y += 10;
            }
        }

        drawDashedLine(g2, leftMargin, rightMargin, y);
        y += 12;

        // Totals Section
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("Subtotal:", leftMargin + 60, y);
        drawStringRight(g2, FormatUtil.formatCurrency(sale.getSubtotal()), rightMargin, y);
        y += 11;

        if (sale.getDiscountAmount() > 0) {
            g2.drawString("Discount:", leftMargin + 60, y);
            drawStringRight(g2, "-" + FormatUtil.formatCurrency(sale.getDiscountAmount()), rightMargin, y);
            y += 11;
        }

        if (sale.getTaxAmount() > 0) {
            g2.drawString("Tax (VAT):", leftMargin + 60, y);
            drawStringRight(g2, FormatUtil.formatCurrency(sale.getTaxAmount()), rightMargin, y);
            y += 11;
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString("GRAND TOTAL:", leftMargin + 40, y);
        drawStringRight(g2, FormatUtil.formatCurrency(sale.getGrandTotal()), rightMargin, y);
        y += 13;

        drawDashedLine(g2, leftMargin, rightMargin, y);
        y += 11;

        // Payments
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        if (sale.getPayments() != null && !sale.getPayments().isEmpty()) {
            for (Payment p : sale.getPayments()) {
                g2.drawString("Payment (" + p.getPaymentMethod() + "):", leftMargin + 40, y);
                drawStringRight(g2, FormatUtil.formatCurrency(p.getAmount()), rightMargin, y);
                y += 10;
            }
        } else {
            g2.drawString("Paid Amount:", leftMargin + 40, y);
            drawStringRight(g2, FormatUtil.formatCurrency(sale.getPaidAmount()), rightMargin, y);
            y += 10;
        }

        g2.drawString("Change Given:", leftMargin + 40, y);
        drawStringRight(g2, FormatUtil.formatCurrency(sale.getChangeAmount()), rightMargin, y);
        y += 14;

        drawDashedLine(g2, leftMargin, rightMargin, y);
        y += 12;

        // Barcode for quick invoice retrieval / scanning
        try {
            int bcWidth = usableWidth - 20;
            BufferedImage bc = BarcodeUtil.generateCode128(sale.getInvoiceNumber(), bcWidth, 32);
            g2.drawImage(bc, (width - bcWidth) / 2, y, null);
            y += 38;
        } catch (Exception ignored) {}

        // Footer & Store Policy
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        String footerMsg = getSetting("invoice.footer", "Thank you for shopping at D’RROW Clothing Store! Please visit again.");
        String policyMsg = getSetting("policy.return_exchange", "Items can be exchanged within 7 days with original tag attached and receipt. No cash refunds.");

        y = drawWrappedText(g2, footerMsg, width, y);
        y += 6;
        y = drawWrappedText(g2, policyMsg, width, y);
        y += 15;

        return y;
    }

    /**
     * Renders receipt into a BufferedImage for high-resolution visual preview dialogs.
     */
    public BufferedImage generatePreviewImage() {
        // Calculate estimated height
        int itemCount = sale.getItems() != null ? sale.getItems().size() : 1;
        int estimatedHeight = 360 + (itemCount * 22);

        BufferedImage img = new BufferedImage(RECEIPT_WIDTH, estimatedHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, RECEIPT_WIDTH, estimatedHeight);

        int actualHeight = drawReceipt(g2, RECEIPT_WIDTH);
        g2.dispose();

        // Crop to exact drawn height if different
        if (actualHeight > 0 && actualHeight != estimatedHeight) {
            BufferedImage cropped = new BufferedImage(RECEIPT_WIDTH, actualHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gCrop = cropped.createGraphics();
            gCrop.drawImage(img, 0, 0, null);
            gCrop.dispose();
            return cropped;
        }
        return img;
    }

    private String getSetting(String key, String def) {
        if (settings != null && settings.containsKey(key)) {
            return settings.get(key);
        }
        return def;
    }

    private void drawCenteredString(Graphics2D g2, String text, int width, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawStringRight(Graphics2D g2, String text, int rightX, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rightX - fm.stringWidth(text);
        g2.drawString(text, x, y);
    }

    private void drawDashedLine(Graphics2D g2, int x1, int x2, int y) {
        Stroke oldStroke = g2.getStroke();
        float[] dash = {3.0f, 3.0f};
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawLine(x1, y, x2, y);
        g2.setStroke(oldStroke);
    }

    private int drawWrappedText(Graphics2D g2, String text, int width, int startY) {
        FontMetrics fm = g2.getFontMetrics();
        int margin = 12;
        int maxWidth = width - (margin * 2);
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = startY;

        for (String word : words) {
            if (fm.stringWidth(line + " " + word) > maxWidth) {
                drawCenteredString(g2, line.toString(), width, curY);
                curY += fm.getHeight();
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            drawCenteredString(g2, line.toString(), width, curY);
            curY += fm.getHeight();
        }
        return curY;
    }
}

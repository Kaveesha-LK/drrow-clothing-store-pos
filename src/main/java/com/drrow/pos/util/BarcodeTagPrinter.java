package com.drrow.pos.util;

import com.drrow.pos.model.ProductVariant;

import java.awt.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;

/**
 * Java Printable implementation for printing clothing barcode tags on
 * roll label printers (50x30mm) or multi-tag A4 sheets.
 */
public class BarcodeTagPrinter implements Printable {

    private final ProductVariant variant;
    private final String brandName;
    private final int quantity;
    private final boolean a4SheetMode;

    // Single tag dimensions in standard 72 DPI points: 50mm ~ 142pt, 30mm ~ 85pt
    public static final int TAG_WIDTH_PT = 142;
    public static final int TAG_HEIGHT_PT = 85;

    // Pixel dimensions for on-screen preview:
    public static final int PREVIEW_TAG_WIDTH = 260;
    public static final int PREVIEW_TAG_HEIGHT = 160;

    public BarcodeTagPrinter(ProductVariant variant, String brandName, int quantity, boolean a4SheetMode) {
        this.variant = variant;
        this.brandName = brandName != null ? brandName : "D’RROW";
        this.quantity = Math.max(1, quantity);
        this.a4SheetMode = a4SheetMode;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (!a4SheetMode) {
            // Roll label printer: 1 tag per page
            if (pageIndex >= quantity) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            drawSingleTag(g2, (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
            return PAGE_EXISTS;
        } else {
            // A4 sheet mode: 3 cols x 7 rows = 21 labels per page
            int labelsPerPage = 21;
            int totalPages = (int) Math.ceil((double) quantity / labelsPerPage);
            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int startIdx = pageIndex * labelsPerPage;
            int endIdx = Math.min(startIdx + labelsPerPage, quantity);

            int cols = 3;
            int rows = 7;
            int cellW = (int) pageFormat.getImageableWidth() / cols;
            int cellH = (int) pageFormat.getImageableHeight() / rows;

            for (int i = startIdx; i < endIdx; i++) {
                int localIdx = i - startIdx;
                int c = localIdx % cols;
                int r = localIdx / cols;

                int x = c * cellW + 4;
                int y = r * cellH + 4;

                Graphics2D gCell = (Graphics2D) g2.create(x, y, cellW - 8, cellH - 8);
                drawSingleTag(gCell, cellW - 8, cellH - 8);
                gCell.dispose();
            }

            return PAGE_EXISTS;
        }
    }

    public void drawSingleTag(Graphics2D g2, int width, int height) {
        BufferedImage tagImg = BarcodeUtil.renderClothingTag(
            brandName,
            variant.getProductName(),
            variant.getSku(),
            variant.getSizeName(),
            variant.getColourName(),
            variant.getSellingPrice(),
            variant.getBarcode(),
            width,
            height
        );
        g2.drawImage(tagImg, 0, 0, null);
    }

    /**
     * Generates a high-definition preview of a single tag.
     */
    public BufferedImage generatePreviewImage() {
        return BarcodeUtil.renderClothingTag(
            brandName,
            variant.getProductName(),
            variant.getSku(),
            variant.getSizeName(),
            variant.getColourName(),
            variant.getSellingPrice(),
            variant.getBarcode(),
            PREVIEW_TAG_WIDTH,
            PREVIEW_TAG_HEIGHT
        );
    }
}

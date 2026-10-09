package com.drrow.pos.service;

import com.drrow.pos.dao.ProductVariantDAO;
import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.util.BarcodeTagPrinter;
import com.drrow.pos.util.BarcodeUtil;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.sql.SQLException;

public class BarcodeService {

    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    public BufferedImage generateBarcodeImage(String barcode, int width, int height) {
        return BarcodeUtil.generateCode128(barcode, width, height);
    }

    public BufferedImage renderClothingTagPreview(ProductVariant variant, String brandName) {
        BarcodeTagPrinter tagPrinter = new BarcodeTagPrinter(variant, brandName, 1, false);
        return tagPrinter.generatePreviewImage();
    }

    public String generateUniqueBarcode(String prefix) throws SQLException {
        return variantDAO.generateNextBarcode(prefix);
    }

    public boolean isBarcodeUnique(String barcode, Integer excludeVariantId) throws SQLException {
        return !variantDAO.isBarcodeExists(barcode, excludeVariantId);
    }

    public boolean printClothingTags(ProductVariant variant, String brandName, int quantity, boolean a4SheetMode) throws Exception {
        BarcodeTagPrinter tagPrinter = new BarcodeTagPrinter(variant, brandName, quantity, a4SheetMode);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(tagPrinter);
        if (job.printDialog()) {
            job.print();
            return true;
        }
        return false;
    }
}

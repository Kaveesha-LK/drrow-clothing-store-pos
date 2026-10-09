package com.drrow.pos.service;

import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.model.Sale;
import com.drrow.pos.util.BarcodeTagPrinter;
import com.drrow.pos.util.ReceiptPrinter;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.util.Map;

public class PrintService {

    private final SettingService settingService = new SettingService();

    public BufferedImage getReceiptPreview(Sale sale) throws Exception {
        Map<String, String> settings = settingService.getAllSettings();
        ReceiptPrinter printer = new ReceiptPrinter(sale, settings);
        return printer.generatePreviewImage();
    }

    public boolean printReceipt(Sale sale) throws Exception {
        Map<String, String> settings = settingService.getAllSettings();
        ReceiptPrinter printer = new ReceiptPrinter(sale, settings);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(printer);
        if (job.printDialog()) {
            job.print();
            return true;
        }
        return false;
    }

    public BufferedImage getClothingTagPreview(ProductVariant variant) throws Exception {
        String brand = variant.getBrandName() != null ? variant.getBrandName() : "D’RROW";
        BarcodeTagPrinter printer = new BarcodeTagPrinter(variant, brand, 1, false);
        return printer.generatePreviewImage();
    }

    public boolean printClothingTags(ProductVariant variant, int quantity, boolean a4Mode) throws Exception {
        String brand = variant.getBrandName() != null ? variant.getBrandName() : "D’RROW";
        BarcodeTagPrinter printer = new BarcodeTagPrinter(variant, brand, quantity, a4Mode);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(printer);
        if (job.printDialog()) {
            job.print();
            return true;
        }
        return false;
    }
}

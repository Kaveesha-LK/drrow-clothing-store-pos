package com.drrow.pos.test;

import com.drrow.pos.controller.POSController;
import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.DatabaseConnection;
import com.drrow.pos.dao.ReportDAO;
import com.drrow.pos.model.*;
import com.drrow.pos.service.*;
import com.drrow.pos.util.BarcodeUtil;
import com.drrow.pos.util.FormatUtil;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Automated End-to-End Workflow Verification Test according to Requirement #39:
 * 1. Login as Admin
 * 2. Create category 'T-Shirts'
 * 3. Create product 'Classic T-Shirt'
 * 4. Create variant 'Black / M', Price Rs. 2,500, Initial Stock 20
 * 5. Generate Code 128 unique barcode and render clothing tag
 * 6. Scan barcode via POS scanner input workflow
 * 7. Add quantity 1 to shopping cart
 * 8. Complete cash sale (paid Rs. 3,000, grand total Rs. 2,500, change Rs. 500)
 * 9. Verify stock becomes 19
 * 10. Generate and reprint invoice
 * 11. Process return of 1 item
 * 12. Verify stock returns to 20
 * 13. Verify sales and return reporting
 */
public class WorkflowIntegrationTest {

    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println("   D’RROW CLOTHING STORE POS - END-TO-END WORKFLOW INTEGRATION TEST  ");
        System.out.println("=====================================================================");

        try {
            // 0. Initialize Database
            System.out.println("\n[STEP 0] Initializing database in test mode...");
            DatabaseConnection.setEmbeddedMode("drrow_test_db");

            // Execute seed scripts
            try (InputStream seedIn = WorkflowIntegrationTest.class.getResourceAsStream("/database/seed.sql")) {
                if (seedIn != null) DatabaseConnection.executeSqlScript(seedIn);
            }
            try (InputStream sampleIn = WorkflowIntegrationTest.class.getResourceAsStream("/database/sample_data.sql")) {
                if (sampleIn != null) DatabaseConnection.executeSqlScript(sampleIn);
            }
            System.out.println("-> Database schema, roles, and sample data initialized successfully.");

            // 1. Login as Admin
            System.out.println("\n[STEP 1] Login as Admin...");
            AuthService authService = new AuthService();
            User adminUser = authService.login("admin", "Admin@123");
            assertNotNull(adminUser, "Admin login returned null");
            assertEquals("ADMIN", adminUser.getRoleName(), "Role is not ADMIN");
            System.out.println("-> Logged in successfully as: " + adminUser.getFullName() + " (Role: " + adminUser.getRoleName() + ")");

            // 2. Create Category "T-Shirts"
            System.out.println("\n[STEP 2] Verifying/Creating category 'T-Shirts'...");
            ProductService productService = new ProductService();
            Category targetCategory = null;
            for (Category c : productService.getAllCategories()) {
                if ("T-Shirts".equalsIgnoreCase(c.getName())) {
                    targetCategory = c;
                    break;
                }
            }
            if (targetCategory == null) {
                targetCategory = new Category(0, "T-Shirts", "Crew neck and graphic tees", true);
                productService.saveCategory(targetCategory);
                System.out.println("-> Created category: " + targetCategory.getName());
            } else {
                System.out.println("-> Found existing category: " + targetCategory.getName() + " (ID: " + targetCategory.getCategoryId() + ")");
            }

            // Get Brand, Size 'M', and Colour 'Black'
            Brand targetBrand = productService.getAllBrands().get(0);
            Size sizeM = null;
            for (Size s : productService.getAllSizes()) {
                if ("M".equalsIgnoreCase(s.getSizeName())) { sizeM = s; break; }
            }
            Colour colBlack = null;
            for (Colour c : productService.getAllColours()) {
                if ("Black".equalsIgnoreCase(c.getColourName())) { colBlack = c; break; }
            }
            assertNotNull(sizeM, "Size M must exist");
            assertNotNull(colBlack, "Colour Black must exist");

            // 3. Create Product "Classic T-Shirt"
            System.out.println("\n[STEP 3] Creating product 'Classic T-Shirt'...");
            Product product = new Product();
            product.setName("Classic T-Shirt");
            product.setItemCode("DRTS-TEST-01");
            product.setCategoryId(targetCategory.getCategoryId());
            product.setBrandId(targetBrand.getBrandId());
            product.setDescription("100% premium bio-washed cotton daily wear crew-neck t-shirt");
            product.setPurchasePrice(1200.00);
            product.setSellingPrice(2500.00);
            product.setReorderLevel(5);
            product.setActive(true);

            // 4. Create Variant: Black / M with Selling Price Rs. 2,500 and Add Stock = 20
            System.out.println("\n[STEP 4] Creating Variant 'Black / M' with Price Rs. 2,500 and Initial Stock = 20...");
            String generatedBarcode = productService.generateNextBarcode("DRTS");
            System.out.println("-> Generated Unique Barcode: " + generatedBarcode);

            ProductVariant variant = new ProductVariant();
            variant.setSizeId(sizeM.getSizeId());
            variant.setSizeName(sizeM.getSizeName());
            variant.setColourId(colBlack.getColourId());
            variant.setColourName(colBlack.getColourName());
            variant.setPurchasePrice(1200.00);
            variant.setSellingPrice(2500.00);
            variant.setSku("DRTS-TEST-01-BLK-M");
            variant.setBarcode(generatedBarcode);
            variant.setActive(true);

            List<ProductVariant> variants = new ArrayList<>();
            variants.add(variant);

            productService.saveProductWithVariants(product, variants, 20);
            System.out.println("-> Product & Variant saved. Product ID: " + product.getProductId() + ", Variant ID: " + variant.getVariantId());

            // Verify initial stock is 20
            InventoryService inventoryService = new InventoryService();
            Stock initialStock = inventoryService.getStock(variant.getVariantId());
            assertNotNull(initialStock, "Stock record must exist");
            assertEquals(20, initialStock.getCurrentStock(), "Initial stock must be exactly 20");
            System.out.println("-> Initial Stock Verified: " + initialStock.getCurrentStock() + " units");

            // 5. Print Barcode Tag & Verify Label Layout
            System.out.println("\n[STEP 5] Generating Barcode Tag preview...");
            BarcodeService barcodeService = new BarcodeService();
            BufferedImage tagImg = barcodeService.renderClothingTagPreview(variant, targetBrand.getName());
            assertNotNull(tagImg, "Tag preview image must not be null");
            assertTrue(tagImg.getWidth() > 0 && tagImg.getHeight() > 0, "Tag preview must have non-zero dimensions");
            System.out.println("-> Tag image rendered successfully (Width: " + tagImg.getWidth() + "px, Height: " + tagImg.getHeight() + "px)");

            // 6. Scan Barcode using USB Scanner Simulation
            System.out.println("\n[STEP 6] Scanning barcode via POS Scanner (" + generatedBarcode + ")...");
            SalesService salesService = new SalesService();
            ProductVariant scannedVariant = salesService.scanBarcode(generatedBarcode);
            assertNotNull(scannedVariant, "Product must be found by scanned barcode");
            assertEquals("Classic T-Shirt", scannedVariant.getProductName(), "Product name must match");
            assertEquals("M", scannedVariant.getSizeName(), "Size must be M");
            assertEquals("Black", scannedVariant.getColourName(), "Colour must be Black");
            System.out.println("-> Scanner identified: " + scannedVariant.getVariantDisplayName() + " @ Rs. " + scannedVariant.getSellingPrice());

            // 7. Add to POS Shopping Cart
            System.out.println("\n[STEP 7] Adding 1 unit to POS cart...");
            POSController pos = new POSController();
            pos.handleBarcodeScan(generatedBarcode);
            assertEquals(1, pos.getCartItems().size(), "Cart must contain 1 item");
            assertEquals(1, pos.getCartItems().get(0).getQuantity(), "Item quantity must be 1");
            assertEquals(2500.00, pos.calculateGrandTotal(), "Grand total must be Rs. 2,500.00");
            System.out.println("-> Cart Item: " + pos.getCartItems().get(0).getProductName() + ", Qty: " + pos.getCartItems().get(0).getQuantity() + ", Total: Rs. " + pos.calculateGrandTotal());

            // 8. Complete Cash Sale
            System.out.println("\n[STEP 8] Completing Cash Sale: Tendered Rs. 3,000.00...");
            double paid = 3000.00;
            double grandTotal = pos.calculateGrandTotal();
            double change = pos.calculateChange(paid);
            assertEquals(500.00, change, "Change must be exactly Rs. 500.00");

            Sale completedSale = pos.completeSale("CASH", paid, "Test cashier sale");
            assertNotNull(completedSale, "Sale must be created successfully");
            assertNotNull(completedSale.getInvoiceNumber(), "Invoice number must be generated");
            System.out.println("-> Sale Completed! Invoice #: " + completedSale.getInvoiceNumber() + ", Grand Total: Rs. " + completedSale.getGrandTotal() + ", Change Given: Rs. " + completedSale.getChangeAmount());

            // 9. Verify Stock Becomes 19
            System.out.println("\n[STEP 9] Verifying stock deduction...");
            Stock stockAfterSale = inventoryService.getStock(variant.getVariantId());
            assertEquals(19, stockAfterSale.getCurrentStock(), "Stock after 1 sale must be exactly 19");
            System.out.println("-> Stock Level Verified: " + stockAfterSale.getCurrentStock() + " units (Decreased from 20 to 19)");

            // 10. Open Sales History & Reprint Invoice
            System.out.println("\n[STEP 10] Retrieving invoice from Sales Archive & generating reprint preview...");
            Sale fetchedSale = salesService.getSaleByInvoiceNumber(completedSale.getInvoiceNumber());
            assertNotNull(fetchedSale, "Sale must be retrievable by invoice number");
            assertEquals(1, fetchedSale.getItems().size(), "Sale must have 1 line item");

            PrintService printService = new PrintService();
            BufferedImage receiptImg = printService.getReceiptPreview(fetchedSale);
            assertNotNull(receiptImg, "Receipt image must render properly");
            System.out.println("-> Invoice reprint preview verified (" + receiptImg.getWidth() + "x" + receiptImg.getHeight() + "px)");

            // 11. Process Return of 1 Item
            System.out.println("\n[STEP 11] Processing customer return of 1 item with reason 'Customer exchange'...");
            ReturnService returnService = new ReturnService();
            SaleItem itemToReturn = fetchedSale.getItems().get(0);

            List<ReturnItem> returnItems = new ArrayList<>();
            returnItems.add(new ReturnItem(
                itemToReturn.getItemId(),
                itemToReturn.getVariantId(),
                itemToReturn.getProductName(),
                itemToReturn.getSku(),
                itemToReturn.getSizeName(),
                itemToReturn.getColourName(),
                1, // Quantity 1
                itemToReturn.getUnitPrice(),
                itemToReturn.getUnitPrice() * 1, // Rs. 2500 refund
                "RESTOCKED" // Restock item back to inventory
            ));

            boolean returnSuccess = returnService.processReturn(
                fetchedSale.getSaleId(),
                fetchedSale.getInvoiceNumber(),
                returnItems,
                "Customer requested size exchange",
                "EXCHANGE"
            );
            assertTrue(returnSuccess, "Return transaction must succeed");
            System.out.println("-> Return processed successfully. Original sale preserved intact.");

            // 12. Verify Stock Returns to 20
            System.out.println("\n[STEP 12] Verifying stock restoration after return...");
            Stock stockAfterReturn = inventoryService.getStock(variant.getVariantId());
            assertEquals(20, stockAfterReturn.getCurrentStock(), "Stock after return of 1 item must return to 20");
            System.out.println("-> Stock Level Verified: " + stockAfterReturn.getCurrentStock() + " units (Restored from 19 back to 20)");

            // 13. Verify Sales & Return Reports
            System.out.println("\n[STEP 13] Verifying Reports...");
            ReportService reportService = new ReportService();
            ReportDAO.ReportResult dailyReport = reportService.getDailySalesReport(new Date());
            assertNotNull(dailyReport, "Daily sales report must not be null");
            assertTrue(dailyReport.getRows().size() > 0, "Daily report should include completed sales");
            System.out.println("-> Daily Sales Report: " + dailyReport.getRows().size() + " invoice records verified.");

            ReportDAO.ReportResult returnsReport = reportService.getReturnsReport(new Date(System.currentTimeMillis() - 86400000), new Date(System.currentTimeMillis() + 86400000));
            assertNotNull(returnsReport, "Returns report must not be null");
            assertTrue(returnsReport.getRows().size() > 0, "Returns report should include processed return");
            System.out.println("-> Returns Report: " + returnsReport.getRows().size() + " return records verified.");

            System.out.println("\n=====================================================================");
            System.out.println("   >>> ALL 13 TEST WORKFLOW VERIFICATIONS PASSED SUCCESSFULLY! <<<   ");
            System.out.println("=====================================================================");

        } catch (Throwable t) {
            System.err.println("\nTEST FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " - Expected: [" + expected + "], Got: [" + actual + "]");
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(message + " - Expected: [" + expected + "], Got: [" + actual + "]");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message + " - Expected non-null value");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

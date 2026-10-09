package com.drrow.pos.service;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.*;
import com.drrow.pos.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class InventoryService {

    private final StockDAO stockDAO = new StockDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final StockReceiptDAO receiptDAO = new StockReceiptDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    public Stock getStock(int variantId) throws SQLException {
        return stockDAO.findByVariantId(variantId, null);
    }

    public List<ProductVariant> getLowStockAlerts(int threshold) throws SQLException {
        return stockDAO.getLowStockVariants(threshold);
    }

    public List<ProductVariant> getOutOfStockAlerts() throws SQLException {
        return stockDAO.getOutOfStockVariants();
    }

    public int getTotalStockQuantity() throws SQLException {
        return stockDAO.getTotalStockQuantity();
    }

    public List<StockMovement> getRecentMovements(int limit) throws SQLException {
        return movementDAO.getRecentMovements(limit);
    }

    public List<StockMovement> getMovementsByDateRange(Date start, Date end) throws SQLException {
        return movementDAO.getMovementsByDateRange(start, end);
    }

    /**
     * Executes manual stock adjustment with required reason code:
     * ADJUSTMENT_DAMAGE, ADJUSTMENT_LOST, ADJUSTMENT_CORRECTION, ADJUSTMENT_FOUND, ADJUSTMENT_MANUAL
     */
    public boolean adjustStock(int variantId, String adjustmentType, int quantityDelta, String reason) throws SQLException {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided for manual stock adjustment.");
        }
        if (quantityDelta == 0) {
            throw new IllegalArgumentException("Adjustment quantity cannot be zero.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Stock stock = stockDAO.findByVariantId(variantId, conn);
            int beforeQty = stock != null ? stock.getCurrentStock() : 0;
            int afterQty = beforeQty + quantityDelta;

            if (afterQty < 0) {
                throw new SQLException("Adjustment would result in negative stock (" + afterQty + "). Operation disallowed.");
            }

            stockDAO.upsertStock(variantId, afterQty, afterQty, conn);

            int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
            String username = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "system";

            StockMovement sm = new StockMovement(
                variantId,
                adjustmentType,
                quantityDelta,
                beforeQty,
                afterQty,
                "ADJ-" + System.currentTimeMillis() % 1000000,
                "MANUAL",
                reason,
                userId
            );
            movementDAO.recordMovement(sm, conn);

            auditDAO.log(new AuditLog(
                userId,
                username,
                "STOCK_ADJUSTMENT",
                "STOCK",
                String.valueOf(variantId),
                "Adjusted variant ID " + variantId + " (" + adjustmentType + "): delta " + quantityDelta + ", new stock: " + afterQty + ". Reason: " + reason
            ), conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean processStockReceipt(StockReceipt receipt, List<StockReceiptItem> items) throws SQLException {
        int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
        receipt.setUserId(userId);
        receipt.setUsername(SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "system");
        return receiptDAO.processStockReceipt(receipt, items, userId);
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.findAll();
    }

    public boolean saveSupplier(Supplier s) throws SQLException {
        return s.getSupplierId() == 0 ? supplierDAO.create(s) : supplierDAO.update(s);
    }

    public List<StockReceipt> getAllReceipts() throws SQLException {
        return receiptDAO.findAll();
    }
}

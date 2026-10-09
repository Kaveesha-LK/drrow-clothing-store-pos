package com.drrow.pos.dao;

import com.drrow.pos.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockReceiptDAO {

    public boolean processStockReceipt(StockReceipt receipt, List<StockReceiptItem> items, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String receiptSql = "INSERT INTO stock_receipts (receipt_number, supplier_id, user_id, receipt_date, total_amount, reference_number, notes) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            int receiptId = 0;
            try (PreparedStatement ps = DatabaseConnection.prepareInsert(conn, receiptSql)) {
                ps.setString(1, receipt.getReceiptNumber());
                if (receipt.getSupplierId() > 0) {
                    ps.setInt(2, receipt.getSupplierId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, receipt.getUserId());
                ps.setDate(4, new java.sql.Date(receipt.getReceiptDate().getTime()));
                ps.setDouble(5, receipt.getTotalAmount());
                ps.setString(6, receipt.getReferenceNumber());
                ps.setString(7, receipt.getNotes());

                ps.executeUpdate();
                receiptId = DatabaseConnection.getGeneratedKey(ps, conn);
                if (receiptId == 0) {
                    try (PreparedStatement psSel = conn.prepareStatement("SELECT receipt_id FROM stock_receipts WHERE receipt_number = ?")) {
                        psSel.setString(1, receipt.getReceiptNumber());
                        try (ResultSet rs = psSel.executeQuery()) {
                            if (rs.next()) receiptId = rs.getInt(1);
                        }
                    }
                }
                receipt.setReceiptId(receiptId);
            }

            StockDAO stockDao = new StockDAO();
            StockMovementDAO movementDao = new StockMovementDAO();

            String itemSql = "INSERT INTO stock_receipt_items (receipt_id, variant_id, quantity, unit_cost, total_cost) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = DatabaseConnection.prepareInsert(conn, itemSql)) {
                for (StockReceiptItem item : items) {
                    psItem.setInt(1, receiptId);
                    psItem.setInt(2, item.getVariantId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitCost());
                    psItem.setDouble(5, item.getTotalCost());
                    psItem.executeUpdate();

                    // Increase inventory stock
                    Stock currentStock = stockDao.findByVariantId(item.getVariantId(), conn);
                    int beforeQty = currentStock != null ? currentStock.getCurrentStock() : 0;
                    int afterQty = beforeQty + item.getQuantity();
                    stockDao.upsertStock(item.getVariantId(), afterQty, afterQty, conn);

                    // Record movement
                    StockMovement sm = new StockMovement(
                        item.getVariantId(),
                        "RECEIVE",
                        item.getQuantity(),
                        beforeQty,
                        afterQty,
                        receipt.getReceiptNumber(),
                        "RECEIPT",
                        "Supplier stock intake (Ref: " + receipt.getReferenceNumber() + ")",
                        userId
                    );
                    movementDao.recordMovement(sm, conn);
                }
            }

            // Audit log
            AuditLogDAO auditDao = new AuditLogDAO();
            auditDao.log(new AuditLog(
                userId,
                receipt.getUsername(),
                "STOCK_RECEIVE",
                "RECEIPT",
                String.valueOf(receiptId),
                "Processed stock receipt " + receipt.getReceiptNumber() + " with " + items.size() + " items"
            ), conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
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

    public List<StockReceipt> findAll() throws SQLException {
        List<StockReceipt> list = new ArrayList<>();
        String sql = "SELECT sr.receipt_id, sr.receipt_number, sr.supplier_id, s.name AS supplier_name, " +
                     "sr.user_id, u.username, sr.receipt_date, sr.total_amount, sr.reference_number, sr.notes, sr.created_at " +
                     "FROM stock_receipts sr " +
                     "LEFT JOIN suppliers s ON sr.supplier_id = s.supplier_id " +
                     "JOIN users u ON sr.user_id = u.user_id " +
                     "ORDER BY sr.receipt_date DESC, sr.receipt_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StockReceipt sr = new StockReceipt();
                sr.setReceiptId(rs.getInt("receipt_id"));
                sr.setReceiptNumber(rs.getString("receipt_number"));
                sr.setSupplierId(rs.getInt("supplier_id"));
                sr.setSupplierName(rs.getString("supplier_name"));
                sr.setUserId(rs.getInt("user_id"));
                sr.setUsername(rs.getString("username"));
                sr.setReceiptDate(rs.getDate("receipt_date"));
                sr.setTotalAmount(rs.getDouble("total_amount"));
                sr.setReferenceNumber(rs.getString("reference_number"));
                sr.setNotes(rs.getString("notes"));
                Timestamp ct = rs.getTimestamp("created_at");
                if (ct != null) sr.setCreatedAt(new java.util.Date(ct.getTime()));
                list.add(sr);
            }
        }
        return list;
    }
}

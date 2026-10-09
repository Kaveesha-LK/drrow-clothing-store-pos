package com.drrow.pos.dao;

import com.drrow.pos.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReturnDAO {

    public boolean processReturnTransaction(Return ret, List<ReturnItem> items, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String retSql = "INSERT INTO returns (return_number, sale_id, user_id, return_date, total_refund_amount, return_reason, action_taken) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
            int returnId = 0;
            try (PreparedStatement ps = DatabaseConnection.prepareInsert(conn, retSql)) {
                ps.setString(1, ret.getReturnNumber());
                ps.setInt(2, ret.getSaleId());
                ps.setInt(3, ret.getUserId());
                ps.setTimestamp(4, new Timestamp(ret.getReturnDate().getTime()));
                ps.setDouble(5, ret.getTotalRefundAmount());
                ps.setString(6, ret.getReturnReason());
                ps.setString(7, ret.getActionTaken());

                ps.executeUpdate();
                returnId = DatabaseConnection.getGeneratedKey(ps, conn);
                if (returnId == 0) {
                    try (PreparedStatement psSel = conn.prepareStatement("SELECT return_id FROM returns WHERE return_number = ?")) {
                        psSel.setString(1, ret.getReturnNumber());
                        try (ResultSet rs = psSel.executeQuery()) {
                            if (rs.next()) returnId = rs.getInt(1);
                        }
                    }
                }
                ret.setReturnId(returnId);
            }

            StockDAO stockDao = new StockDAO();
            StockMovementDAO movementDao = new StockMovementDAO();

            String itemSql = "INSERT INTO return_items (return_id, sale_item_id, variant_id, quantity, unit_price, refund_amount, restock_status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = DatabaseConnection.prepareInsert(conn, itemSql)) {
                for (ReturnItem ri : items) {
                    psItem.setInt(1, returnId);
                    psItem.setInt(2, ri.getSaleItemId());
                    psItem.setInt(3, ri.getVariantId());
                    psItem.setInt(4, ri.getQuantity());
                    psItem.setDouble(5, ri.getUnitPrice());
                    psItem.setDouble(6, ri.getRefundAmount());
                    psItem.setString(7, ri.getRestockStatus());
                    psItem.executeUpdate();

                    // Restore stock if marked RESTOCKED
                    if ("RESTOCKED".equalsIgnoreCase(ri.getRestockStatus())) {
                        Stock stock = stockDao.findByVariantId(ri.getVariantId(), conn);
                        int beforeQty = stock != null ? stock.getCurrentStock() : 0;
                        int afterQty = beforeQty + ri.getQuantity();
                        stockDao.upsertStock(ri.getVariantId(), afterQty, afterQty, conn);

                        StockMovement sm = new StockMovement(
                            ri.getVariantId(),
                            "RETURN",
                            ri.getQuantity(),
                            beforeQty,
                            afterQty,
                            ret.getReturnNumber(),
                            "RETURN",
                            "Returned from invoice: " + ret.getInvoiceNumber() + ". Reason: " + ret.getReturnReason(),
                            userId
                        );
                        movementDao.recordMovement(sm, conn);
                    }
                }
            }

            // Audit log
            AuditLogDAO auditDao = new AuditLogDAO();
            auditDao.log(new AuditLog(
                userId,
                ret.getCashierName(),
                "RETURN_COMPLETE",
                "RETURN",
                String.valueOf(returnId),
                "Processed return " + ret.getReturnNumber() + " against invoice " + ret.getInvoiceNumber() + ", refund amount: " + ret.getTotalRefundAmount()
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

    public List<Return> getReturnsForSale(int saleId) throws SQLException {
        List<Return> list = new ArrayList<>();
        String sql = "SELECT r.return_id, r.return_number, r.sale_id, s.invoice_number, r.user_id, u.username AS cashier_name, " +
                     "r.return_date, r.total_refund_amount, r.return_reason, r.action_taken, r.created_at " +
                     "FROM returns r " +
                     "JOIN sales s ON r.sale_id = s.sale_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.sale_id = ? ORDER BY r.return_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Return r = mapRow(rs);
                    r.setItems(getItemsForReturn(r.getReturnId(), conn));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public List<Return> searchReturns(String keyword, Date from, Date to) throws SQLException {
        List<Return> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.return_id, r.return_number, r.sale_id, s.invoice_number, r.user_id, u.username AS cashier_name, " +
            "r.return_date, r.total_refund_amount, r.return_reason, r.action_taken, r.created_at " +
            "FROM returns r " +
            "JOIN sales s ON r.sale_id = s.sale_id " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(r.return_number) LIKE ? OR LOWER(s.invoice_number) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }
        if (from != null) {
            sql.append("AND r.return_date >= ? ");
            params.add(new Timestamp(from.getTime()));
        }
        if (to != null) {
            sql.append("AND r.return_date <= ? ");
            params.add(new Timestamp(to.getTime()));
        }
        sql.append("ORDER BY r.return_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Return r = mapRow(rs);
                    r.setItems(getItemsForReturn(r.getReturnId(), conn));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public List<ReturnItem> getItemsForReturn(int returnId, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        List<ReturnItem> list = new ArrayList<>();
        String sql = "SELECT ri.item_id, ri.return_id, ri.sale_item_id, ri.variant_id, p.name AS product_name, " +
                     "pv.sku, pv.barcode, s.size_name, col.colour_name, ri.quantity, ri.unit_price, ri.refund_amount, ri.restock_status " +
                     "FROM return_items ri " +
                     "JOIN product_variants pv ON ri.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours col ON pv.colour_id = col.colour_id " +
                     "WHERE ri.return_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, returnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnItem item = new ReturnItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setReturnId(rs.getInt("return_id"));
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setBarcode(rs.getString("barcode"));
                    item.setSizeName(rs.getString("size_name"));
                    item.setColourName(rs.getString("colour_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setRefundAmount(rs.getDouble("refund_amount"));
                    item.setRestockStatus(rs.getString("restock_status"));
                    list.add(item);
                }
            }
        } finally {
            if (manageConn) c.close();
        }
        return list;
    }

    private Return mapRow(ResultSet rs) throws SQLException {
        Return r = new Return();
        r.setReturnId(rs.getInt("return_id"));
        r.setReturnNumber(rs.getString("return_number"));
        r.setSaleId(rs.getInt("sale_id"));
        r.setInvoiceNumber(rs.getString("invoice_number"));
        r.setUserId(rs.getInt("user_id"));
        r.setCashierName(rs.getString("cashier_name"));
        Timestamp rd = rs.getTimestamp("return_date");
        if (rd != null) r.setReturnDate(new java.util.Date(rd.getTime()));
        r.setTotalRefundAmount(rs.getDouble("total_refund_amount"));
        r.setReturnReason(rs.getString("return_reason"));
        r.setActionTaken(rs.getString("action_taken"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) r.setCreatedAt(new java.util.Date(ct.getTime()));
        return r;
    }
}

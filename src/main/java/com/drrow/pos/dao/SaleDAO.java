package com.drrow.pos.dao;

import com.drrow.pos.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleDAO {

    public boolean processSaleTransaction(Sale sale, List<CartItem> cartItems, List<Payment> payments, int userId, boolean allowNegativeStock) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Validate and lock stock for each cart item
            StockDAO stockDao = new StockDAO();
            StockMovementDAO movementDao = new StockMovementDAO();

            for (CartItem item : cartItems) {
                Stock stock = stockDao.findByVariantId(item.getVariantId(), conn);
                int currentAvailable = stock != null ? stock.getAvailableStock() : 0;
                if (!allowNegativeStock && currentAvailable < item.getQuantity()) {
                    throw new SQLException("Insufficient stock for " + item.getProductName() +
                            " [" + item.getSizeName() + " / " + item.getColourName() + "]. Available: " + currentAvailable + ", Requested: " + item.getQuantity());
                }
            }

            // 2. Insert Sale header
            String saleSql = "INSERT INTO sales (invoice_number, user_id, customer_id, sale_date, subtotal, discount_amount, " +
                             "discount_percentage, tax_amount, grand_total, paid_amount, change_amount, payment_status, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int saleId = 0;
            try (PreparedStatement psSale = DatabaseConnection.prepareInsert(conn, saleSql)) {
                psSale.setString(1, sale.getInvoiceNumber());
                psSale.setInt(2, sale.getUserId());
                if (sale.getCustomerId() != null && sale.getCustomerId() > 0) {
                    psSale.setInt(3, sale.getCustomerId());
                } else {
                    psSale.setNull(3, Types.INTEGER);
                }
                psSale.setTimestamp(4, new Timestamp(sale.getSaleDate().getTime()));
                psSale.setDouble(5, sale.getSubtotal());
                psSale.setDouble(6, sale.getDiscountAmount());
                psSale.setDouble(7, sale.getDiscountPercentage());
                psSale.setDouble(8, sale.getTaxAmount());
                psSale.setDouble(9, sale.getGrandTotal());
                psSale.setDouble(10, sale.getPaidAmount());
                psSale.setDouble(11, sale.getChangeAmount());
                psSale.setString(12, sale.getPaymentStatus());
                psSale.setString(13, sale.getNotes());

                int affected = psSale.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Failed to insert sale record");
                }
                saleId = DatabaseConnection.getGeneratedKey(psSale, conn);
                if (saleId == 0) {
                    // Try select by invoice_number
                    try (PreparedStatement psSel = conn.prepareStatement("SELECT sale_id FROM sales WHERE invoice_number = ?")) {
                        psSel.setString(1, sale.getInvoiceNumber());
                        try (ResultSet rs = psSel.executeQuery()) {
                            if (rs.next()) saleId = rs.getInt(1);
                        }
                    }
                }
                sale.setSaleId(saleId);
            }

            // 3. Insert Sale Items & Deduct Stock
            String itemSql = "INSERT INTO sale_items (sale_id, variant_id, quantity, unit_price, discount_amount, line_total) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = DatabaseConnection.prepareInsert(conn, itemSql)) {
                for (CartItem ci : cartItems) {
                    psItem.setInt(1, saleId);
                    psItem.setInt(2, ci.getVariantId());
                    psItem.setInt(3, ci.getQuantity());
                    psItem.setDouble(4, ci.getUnitPrice());
                    psItem.setDouble(5, ci.getDiscountAmount());
                    psItem.setDouble(6, ci.getLineTotal());
                    psItem.executeUpdate();

                    // Deduct stock
                    Stock stock = stockDao.findByVariantId(ci.getVariantId(), conn);
                    int beforeQty = stock != null ? stock.getCurrentStock() : 0;
                    int afterQty = beforeQty - ci.getQuantity();
                    stockDao.upsertStock(ci.getVariantId(), afterQty, afterQty, conn);

                    // Record Stock Movement
                    StockMovement sm = new StockMovement(
                        ci.getVariantId(),
                        "SALE",
                        -ci.getQuantity(),
                        beforeQty,
                        afterQty,
                        sale.getInvoiceNumber(),
                        "INVOICE",
                        "POS checkout sale: " + sale.getInvoiceNumber(),
                        userId
                    );
                    movementDao.recordMovement(sm, conn);
                }
            }

            // 4. Insert Payments
            String paySql = "INSERT INTO payments (sale_id, payment_method, amount, reference_number, notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psPay = DatabaseConnection.prepareInsert(conn, paySql)) {
                for (Payment p : payments) {
                    psPay.setInt(1, saleId);
                    psPay.setString(2, p.getPaymentMethod());
                    psPay.setDouble(3, p.getAmount());
                    psPay.setString(4, p.getReferenceNumber());
                    psPay.setString(5, p.getNotes());
                    psPay.executeUpdate();
                }
            }

            // 5. Audit Log
            AuditLogDAO auditDao = new AuditLogDAO();
            auditDao.log(new AuditLog(
                userId,
                sale.getCashierName(),
                "SALE_COMPLETE",
                "SALE",
                String.valueOf(saleId),
                "Completed sale " + sale.getInvoiceNumber() + " with grand total " + sale.getGrandTotal()
            ), conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
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

    public Sale findById(int saleId) throws SQLException {
        String sql = "SELECT s.sale_id, s.invoice_number, s.user_id, u.username AS cashier_name, " +
                     "s.customer_id, c.name AS customer_name, s.sale_date, s.subtotal, s.discount_amount, " +
                     "s.discount_percentage, s.tax_amount, s.grand_total, s.paid_amount, s.change_amount, " +
                     "s.payment_status, s.notes, s.created_at " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "WHERE s.sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapRow(rs);
                    sale.setItems(getItemsForSale(saleId, conn));
                    sale.setPayments(getPaymentsForSale(saleId, conn));
                    return sale;
                }
            }
        }
        return null;
    }

    public Sale findByInvoiceNumber(String invoiceNumber) throws SQLException {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) return null;
        String sql = "SELECT s.sale_id, s.invoice_number, s.user_id, u.username AS cashier_name, " +
                     "s.customer_id, c.name AS customer_name, s.sale_date, s.subtotal, s.discount_amount, " +
                     "s.discount_percentage, s.tax_amount, s.grand_total, s.paid_amount, s.change_amount, " +
                     "s.payment_status, s.notes, s.created_at " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "WHERE LOWER(s.invoice_number) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapRow(rs);
                    sale.setItems(getItemsForSale(sale.getSaleId(), conn));
                    sale.setPayments(getPaymentsForSale(sale.getSaleId(), conn));
                    return sale;
                }
            }
        }
        return null;
    }

    public List<Sale> searchSales(String keyword, Date fromDate, Date toDate, Integer cashierId) throws SQLException {
        List<Sale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT s.sale_id, s.invoice_number, s.user_id, u.username AS cashier_name, " +
            "s.customer_id, c.name AS customer_name, s.sale_date, s.subtotal, s.discount_amount, " +
            "s.discount_percentage, s.tax_amount, s.grand_total, s.paid_amount, s.change_amount, " +
            "s.payment_status, s.notes, s.created_at " +
            "FROM sales s " +
            "JOIN users u ON s.user_id = u.user_id " +
            "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(s.invoice_number) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(c.phone) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (fromDate != null) {
            sql.append("AND s.sale_date >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            sql.append("AND s.sale_date <= ? ");
            params.add(new Timestamp(toDate.getTime()));
        }
        if (cashierId != null && cashierId > 0) {
            sql.append("AND s.user_id = ? ");
            params.add(cashierId);
        }
        sql.append("ORDER BY s.sale_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapRow(rs);
                    list.add(sale);
                }
            }
        }
        return list;
    }

    public List<SaleItem> getItemsForSale(int saleId, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        List<SaleItem> list = new ArrayList<>();
        String sql = "SELECT si.item_id, si.sale_id, si.variant_id, p.name AS product_name, pv.sku, pv.barcode, " +
                     "s.size_name, col.colour_name, si.quantity, si.unit_price, si.discount_amount, si.line_total " +
                     "FROM sale_items si " +
                     "JOIN product_variants pv ON si.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours col ON pv.colour_id = col.colour_id " +
                     "WHERE si.sale_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setVariantId(rs.getInt("variant_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setBarcode(rs.getString("barcode"));
                    item.setSizeName(rs.getString("size_name"));
                    item.setColourName(rs.getString("colour_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setDiscountAmount(rs.getDouble("discount_amount"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    list.add(item);
                }
            }
        } finally {
            if (manageConn) c.close();
        }
        return list;
    }

    public List<Payment> getPaymentsForSale(int saleId, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, sale_id, payment_method, amount, reference_number, notes, created_at " +
                     "FROM payments WHERE sale_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setSaleId(rs.getInt("sale_id"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setReferenceNumber(rs.getString("reference_number"));
                    p.setNotes(rs.getString("notes"));
                    Timestamp ct = rs.getTimestamp("created_at");
                    if (ct != null) p.setCreatedAt(new java.util.Date(ct.getTime()));
                    list.add(p);
                }
            }
        } finally {
            if (manageConn) c.close();
        }
        return list;
    }

    public List<Sale> getRecentSales(int limit) throws SQLException {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.invoice_number, s.user_id, u.username AS cashier_name, " +
                     "s.customer_id, c.name AS customer_name, s.sale_date, s.subtotal, s.discount_amount, " +
                     "s.discount_percentage, s.tax_amount, s.grand_total, s.paid_amount, s.change_amount, " +
                     "s.payment_status, s.notes, s.created_at " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        java.time.LocalDate today = java.time.LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(today.atStartOfDay());
        Timestamp endOfDay = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        String todaySql = "SELECT COUNT(*) AS txn_count, COALESCE(SUM(grand_total), 0) AS total_sales " +
                          "FROM sales WHERE sale_date >= ? AND sale_date < ? AND payment_status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(todaySql)) {
                ps.setTimestamp(1, startOfDay);
                ps.setTimestamp(2, endOfDay);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setTodayTransactions(rs.getInt("txn_count"));
                        stats.setTodaySales(rs.getDouble("total_sales"));
                    }
                }
            }

            // Estimate profit
            String profitSql = "SELECT COALESCE(SUM((si.unit_price - pv.purchase_price) * si.quantity), 0) AS profit " +
                               "FROM sale_items si " +
                               "JOIN sales s ON si.sale_id = s.sale_id " +
                               "JOIN product_variants pv ON si.variant_id = pv.variant_id " +
                               "WHERE s.sale_date >= ? AND s.sale_date < ? AND s.payment_status = 'COMPLETED'";
            try (PreparedStatement ps = conn.prepareStatement(profitSql)) {
                ps.setTimestamp(1, startOfDay);
                ps.setTimestamp(2, endOfDay);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setTodayProfit(rs.getDouble("profit"));
                    }
                }
            }

            // Total products count
            String prodCountSql = "SELECT COUNT(*) FROM products WHERE is_active = 1";
            try (PreparedStatement ps = conn.prepareStatement(prodCountSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalProducts(rs.getInt(1));
                }
            }

            // Total stock quantity
            String stockQtySql = "SELECT COALESCE(SUM(current_stock), 0) FROM stock";
            try (PreparedStatement ps = conn.prepareStatement(stockQtySql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalStockQuantity(rs.getInt(1));
                }
            }

            // Low stock and out of stock counts
            StockDAO stockDao = new StockDAO();
            List<ProductVariant> lowVariants = stockDao.getLowStockVariants(5);
            stats.setLowStockCount(lowVariants.size());
            stats.setLowStockVariants(lowVariants);

            List<ProductVariant> outVariants = stockDao.getOutOfStockVariants();
            stats.setOutOfStockCount(outVariants.size());

            // Recent sales
            stats.setRecentSales(getRecentSales(8));
        }

        return stats;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setSaleId(rs.getInt("sale_id"));
        s.setInvoiceNumber(rs.getString("invoice_number"));
        s.setUserId(rs.getInt("user_id"));
        s.setCashierName(rs.getString("cashier_name"));
        int cid = rs.getInt("customer_id");
        if (!rs.wasNull()) s.setCustomerId(cid);
        s.setCustomerName(rs.getString("customer_name"));
        Timestamp sd = rs.getTimestamp("sale_date");
        if (sd != null) s.setSaleDate(new java.util.Date(sd.getTime()));
        s.setSubtotal(rs.getDouble("subtotal"));
        s.setDiscountAmount(rs.getDouble("discount_amount"));
        s.setDiscountPercentage(rs.getDouble("discount_percentage"));
        s.setTaxAmount(rs.getDouble("tax_amount"));
        s.setGrandTotal(rs.getDouble("grand_total"));
        s.setPaidAmount(rs.getDouble("paid_amount"));
        s.setChangeAmount(rs.getDouble("change_amount"));
        s.setPaymentStatus(rs.getString("payment_status"));
        s.setNotes(rs.getString("notes"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) s.setCreatedAt(new java.util.Date(ct.getTime()));
        return s;
    }
}

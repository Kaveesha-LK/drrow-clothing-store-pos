package com.drrow.pos.dao;

import com.drrow.pos.model.Stock;
import com.drrow.pos.model.ProductVariant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public Stock findByVariantId(int variantId, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "SELECT stock_id, variant_id, current_stock, available_stock, reserved_stock, last_updated " +
                     "FROM stock WHERE variant_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Stock s = new Stock(
                        rs.getInt("stock_id"),
                        rs.getInt("variant_id"),
                        rs.getInt("current_stock"),
                        rs.getInt("available_stock"),
                        rs.getInt("reserved_stock")
                    );
                    Timestamp lu = rs.getTimestamp("last_updated");
                    if (lu != null) s.setLastUpdated(new java.util.Date(lu.getTime()));
                    return s;
                }
            }
        } finally {
            if (manageConn) c.close();
        }
        return null;
    }

    public boolean upsertStock(int variantId, int newCurrentStock, int newAvailableStock, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;

        Stock existing = findByVariantId(variantId, c);
        if (existing == null) {
            String insertSql = "INSERT INTO stock (variant_id, current_stock, available_stock, reserved_stock) VALUES (?, ?, ?, 0)";
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                ps.setInt(1, variantId);
                ps.setInt(2, newCurrentStock);
                ps.setInt(3, newAvailableStock);
                return ps.executeUpdate() > 0;
            } finally {
                if (manageConn) c.close();
            }
        } else {
            String updateSql = "UPDATE stock SET current_stock = ?, available_stock = ?, last_updated = CURRENT_TIMESTAMP WHERE variant_id = ?";
            try (PreparedStatement ps = c.prepareStatement(updateSql)) {
                ps.setInt(1, newCurrentStock);
                ps.setInt(2, newAvailableStock);
                ps.setInt(3, variantId);
                return ps.executeUpdate() > 0;
            } finally {
                if (manageConn) c.close();
            }
        }
    }

    public boolean adjustStock(int variantId, int delta, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;

        try {
            Stock current = findByVariantId(variantId, c);
            int curStock = current != null ? current.getCurrentStock() : 0;
            int newStock = curStock + delta;
            return upsertStock(variantId, newStock, newStock, c);
        } finally {
            if (manageConn) c.close();
        }
    }

    public List<ProductVariant> getLowStockVariants(int defaultThreshold) throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        String sql = "SELECT pv.variant_id, pv.product_id, p.name AS product_name, p.item_code, pv.sku, pv.barcode, " +
                     "pv.size_id, s.size_name, pv.colour_id, c.colour_name, pv.purchase_price, pv.selling_price, " +
                     "pv.is_active, pv.created_at, b.name AS brand_name, cat.name AS category_name, " +
                     "COALESCE(st.current_stock, 0) AS current_stock, COALESCE(st.available_stock, 0) AS available_stock, " +
                     "p.reorder_level " +
                     "FROM product_variants pv " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN categories cat ON p.category_id = cat.category_id " +
                     "JOIN brands b ON p.brand_id = b.brand_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "LEFT JOIN stock st ON pv.variant_id = st.variant_id " +
                     "WHERE pv.is_active = 1 AND COALESCE(st.current_stock, 0) > 0 AND COALESCE(st.current_stock, 0) <= COALESCE(p.reorder_level, ?) " +
                     "ORDER BY st.current_stock ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, defaultThreshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariant pv = new ProductVariant();
                    pv.setVariantId(rs.getInt("variant_id"));
                    pv.setProductId(rs.getInt("product_id"));
                    pv.setProductName(rs.getString("product_name"));
                    pv.setItemCode(rs.getString("item_code"));
                    pv.setSku(rs.getString("sku"));
                    pv.setBarcode(rs.getString("barcode"));
                    pv.setSizeName(rs.getString("size_name"));
                    pv.setColourName(rs.getString("colour_name"));
                    pv.setSellingPrice(rs.getDouble("selling_price"));
                    pv.setCurrentStock(rs.getInt("current_stock"));
                    pv.setAvailableStock(rs.getInt("available_stock"));
                    pv.setReorderLevel(rs.getInt("reorder_level"));
                    pv.setBrandName(rs.getString("brand_name"));
                    pv.setCategoryName(rs.getString("category_name"));
                    list.add(pv);
                }
            }
        }
        return list;
    }

    public List<ProductVariant> getOutOfStockVariants() throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        String sql = "SELECT pv.variant_id, pv.product_id, p.name AS product_name, p.item_code, pv.sku, pv.barcode, " +
                     "pv.size_id, s.size_name, pv.colour_id, c.colour_name, pv.purchase_price, pv.selling_price, " +
                     "pv.is_active, pv.created_at, b.name AS brand_name, cat.name AS category_name, " +
                     "COALESCE(st.current_stock, 0) AS current_stock, COALESCE(st.available_stock, 0) AS available_stock, " +
                     "p.reorder_level " +
                     "FROM product_variants pv " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN categories cat ON p.category_id = cat.category_id " +
                     "JOIN brands b ON p.brand_id = b.brand_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "LEFT JOIN stock st ON pv.variant_id = st.variant_id " +
                     "WHERE pv.is_active = 1 AND COALESCE(st.current_stock, 0) <= 0 " +
                     "ORDER BY p.name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductVariant pv = new ProductVariant();
                pv.setVariantId(rs.getInt("variant_id"));
                pv.setProductId(rs.getInt("product_id"));
                pv.setProductName(rs.getString("product_name"));
                pv.setItemCode(rs.getString("item_code"));
                pv.setSku(rs.getString("sku"));
                pv.setBarcode(rs.getString("barcode"));
                pv.setSizeName(rs.getString("size_name"));
                pv.setColourName(rs.getString("colour_name"));
                pv.setSellingPrice(rs.getDouble("selling_price"));
                pv.setCurrentStock(rs.getInt("current_stock"));
                pv.setAvailableStock(rs.getInt("available_stock"));
                pv.setReorderLevel(rs.getInt("reorder_level"));
                pv.setBrandName(rs.getString("brand_name"));
                pv.setCategoryName(rs.getString("category_name"));
                list.add(pv);
            }
        }
        return list;
    }

    public int getTotalStockQuantity() throws SQLException {
        String sql = "SELECT COALESCE(SUM(current_stock), 0) FROM stock";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}

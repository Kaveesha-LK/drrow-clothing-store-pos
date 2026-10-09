package com.drrow.pos.dao;

import com.drrow.pos.model.ProductVariant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO {

    private static final String BASE_SELECT =
        "SELECT pv.variant_id, pv.product_id, p.name AS product_name, p.item_code, pv.sku, pv.barcode, " +
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
        "LEFT JOIN stock st ON pv.variant_id = st.variant_id ";

    public ProductVariant findById(int variantId) throws SQLException {
        String sql = BASE_SELECT + "WHERE pv.variant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ProductVariant findByBarcode(String barcode) throws SQLException {
        if (barcode == null || barcode.trim().isEmpty()) return null;
        String sql = BASE_SELECT + "WHERE pv.barcode = ? AND pv.is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ProductVariant findBySku(String sku) throws SQLException {
        if (sku == null || sku.trim().isEmpty()) return null;
        String sql = BASE_SELECT + "WHERE pv.sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ProductVariant> findByProductId(int productId) throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE pv.product_id = ? ORDER BY s.display_order ASC, c.colour_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ProductVariant> search(String keyword) throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE pv.is_active = 1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(pv.barcode) LIKE ? OR LOWER(pv.sku) LIKE ? OR LOWER(p.name) LIKE ? OR LOWER(p.item_code) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        sql.append("ORDER BY p.name ASC, s.display_order ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean create(ProductVariant v, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "INSERT INTO product_variants (product_id, sku, barcode, size_id, colour_id, purchase_price, selling_price, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.prepareInsert(c, sql)) {
            ps.setInt(1, v.getProductId());
            ps.setString(2, v.getSku());
            ps.setString(3, v.getBarcode());
            ps.setInt(4, v.getSizeId());
            ps.setInt(5, v.getColourId());
            ps.setDouble(6, v.getPurchasePrice());
            ps.setDouble(7, v.getSellingPrice());
            ps.setInt(8, v.isActive() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, c);
                if (key > 0) {
                    v.setVariantId(key);
                }
                return true;
            }
            return false;
        } finally {
            if (manageConn) c.close();
        }
    }

    public boolean update(ProductVariant v, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "UPDATE product_variants SET sku = ?, barcode = ?, size_id = ?, colour_id = ?, " +
                     "purchase_price = ?, selling_price = ?, is_active = ? WHERE variant_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v.getSku());
            ps.setString(2, v.getBarcode());
            ps.setInt(3, v.getSizeId());
            ps.setInt(4, v.getColourId());
            ps.setDouble(5, v.getPurchasePrice());
            ps.setDouble(6, v.getSellingPrice());
            ps.setInt(7, v.isActive() ? 1 : 0);
            ps.setInt(8, v.getVariantId());
            return ps.executeUpdate() > 0;
        } finally {
            if (manageConn) c.close();
        }
    }

    public boolean delete(int variantId) throws SQLException {
        String sql = "DELETE FROM product_variants WHERE variant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isBarcodeExists(String barcode, Integer excludeVariantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE barcode = ?" +
                     (excludeVariantId != null ? " AND variant_id != ?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            if (excludeVariantId != null) {
                ps.setInt(2, excludeVariantId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean isSkuExists(String sku, Integer excludeVariantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE sku = ?" +
                     (excludeVariantId != null ? " AND variant_id != ?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            if (excludeVariantId != null) {
                ps.setInt(2, excludeVariantId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public synchronized String generateNextBarcode(String prefix) throws SQLException {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "DRTS";
        }
        String sql = "SELECT COUNT(*) FROM product_variants";
        int nextId = 1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
        }
        String candidate = String.format("%s%06d", prefix, nextId);
        while (isBarcodeExists(candidate, null)) {
            nextId++;
            candidate = String.format("%s%06d", prefix, nextId);
        }
        return candidate;
    }

    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        ProductVariant pv = new ProductVariant();
        pv.setVariantId(rs.getInt("variant_id"));
        pv.setProductId(rs.getInt("product_id"));
        pv.setProductName(rs.getString("product_name"));
        pv.setItemCode(rs.getString("item_code"));
        pv.setSku(rs.getString("sku"));
        pv.setBarcode(rs.getString("barcode"));
        pv.setSizeId(rs.getInt("size_id"));
        pv.setSizeName(rs.getString("size_name"));
        pv.setColourId(rs.getInt("colour_id"));
        pv.setColourName(rs.getString("colour_name"));
        pv.setPurchasePrice(rs.getDouble("purchase_price"));
        pv.setSellingPrice(rs.getDouble("selling_price"));
        pv.setActive(rs.getInt("is_active") == 1);
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) pv.setCreatedAt(new java.util.Date(ct.getTime()));

        pv.setCurrentStock(rs.getInt("current_stock"));
        pv.setAvailableStock(rs.getInt("available_stock"));
        pv.setReorderLevel(rs.getInt("reorder_level"));
        pv.setBrandName(rs.getString("brand_name"));
        pv.setCategoryName(rs.getString("category_name"));
        return pv;
    }
}

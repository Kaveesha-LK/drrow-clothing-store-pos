package com.drrow.pos.dao;

import com.drrow.pos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.item_code, p.name, p.category_id, c.name AS category_name, " +
                     "p.brand_id, b.name AS brand_name, p.description, p.purchase_price, p.selling_price, " +
                     "p.reorder_level, p.is_active, p.created_at, p.updated_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN brands b ON p.brand_id = b.brand_id " +
                     "ORDER BY p.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Product findById(int productId) throws SQLException {
        String sql = "SELECT p.product_id, p.item_code, p.name, p.category_id, c.name AS category_name, " +
                     "p.brand_id, b.name AS brand_name, p.description, p.purchase_price, p.selling_price, " +
                     "p.reorder_level, p.is_active, p.created_at, p.updated_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN brands b ON p.brand_id = b.brand_id " +
                     "WHERE p.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Product findByItemCode(String itemCode) throws SQLException {
        String sql = "SELECT p.product_id, p.item_code, p.name, p.category_id, c.name AS category_name, " +
                     "p.brand_id, b.name AS brand_name, p.description, p.purchase_price, p.selling_price, " +
                     "p.reorder_level, p.is_active, p.created_at, p.updated_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN brands b ON p.brand_id = b.brand_id " +
                     "WHERE p.item_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Product> search(String keyword, Integer categoryId, Integer brandId) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.product_id, p.item_code, p.name, p.category_id, c.name AS category_name, " +
            "p.brand_id, b.name AS brand_name, p.description, p.purchase_price, p.selling_price, " +
            "p.reorder_level, p.is_active, p.created_at, p.updated_at " +
            "FROM products p " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.item_code) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (brandId != null && brandId > 0) {
            sql.append("AND p.brand_id = ? ");
            params.add(brandId);
        }
        sql.append("ORDER BY p.name ASC");

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

    public boolean create(Product p, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "INSERT INTO products (item_code, name, category_id, brand_id, description, purchase_price, selling_price, reorder_level, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.prepareInsert(c, sql)) {
            ps.setString(1, p.getItemCode());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getCategoryId());
            ps.setInt(4, p.getBrandId());
            ps.setString(5, p.getDescription());
            ps.setDouble(6, p.getPurchasePrice());
            ps.setDouble(7, p.getSellingPrice());
            ps.setInt(8, p.getReorderLevel());
            ps.setInt(9, p.isActive() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, c);
                if (key > 0) {
                    p.setProductId(key);
                }
                return true;
            }
            return false;
        } finally {
            if (manageConn) c.close();
        }
    }

    public boolean update(Product p, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "UPDATE products SET item_code = ?, name = ?, category_id = ?, brand_id = ?, description = ?, " +
                     "purchase_price = ?, selling_price = ?, reorder_level = ?, is_active = ? WHERE product_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getItemCode());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getCategoryId());
            ps.setInt(4, p.getBrandId());
            ps.setString(5, p.getDescription());
            ps.setDouble(6, p.getPurchasePrice());
            ps.setDouble(7, p.getSellingPrice());
            ps.setInt(8, p.getReorderLevel());
            ps.setInt(9, p.isActive() ? 1 : 0);
            ps.setInt(10, p.getProductId());
            return ps.executeUpdate() > 0;
        } finally {
            if (manageConn) c.close();
        }
    }

    public boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getNextProductSequence() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        }
        return 1;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setItemCode(rs.getString("item_code"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setBrandId(rs.getInt("brand_id"));
        p.setBrandName(rs.getString("brand_name"));
        p.setDescription(rs.getString("description"));
        p.setPurchasePrice(rs.getDouble("purchase_price"));
        p.setSellingPrice(rs.getDouble("selling_price"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setActive(rs.getInt("is_active") == 1);
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(new java.util.Date(ct.getTime()));
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) p.setUpdatedAt(new java.util.Date(ut.getTime()));
        return p;
    }
}

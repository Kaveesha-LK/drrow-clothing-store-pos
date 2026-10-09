package com.drrow.pos.dao;

import com.drrow.pos.model.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    public List<Brand> findAll() throws SQLException {
        List<Brand> list = new ArrayList<>();
        String sql = "SELECT brand_id, name, description, is_active FROM brands ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Brand(
                    rs.getInt("brand_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("is_active") == 1
                ));
            }
        }
        return list;
    }

    public Brand findById(int id) throws SQLException {
        String sql = "SELECT brand_id, name, description, is_active FROM brands WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Brand(
                        rs.getInt("brand_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    public boolean create(Brand b) throws SQLException {
        String sql = "INSERT INTO brands (name, description, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = DatabaseConnection.prepareInsert(conn, sql)) {
            ps.setString(1, b.getName());
            ps.setString(2, b.getDescription());
            ps.setInt(3, b.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, conn);
                if (key > 0) b.setBrandId(key);
                return true;
            }
        }
        return false;
    }

    public boolean update(Brand b) throws SQLException {
        String sql = "UPDATE brands SET name = ?, description = ?, is_active = ? WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getName());
            ps.setString(2, b.getDescription());
            ps.setInt(3, b.isActive() ? 1 : 0);
            ps.setInt(4, b.getBrandId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM brands WHERE brand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}

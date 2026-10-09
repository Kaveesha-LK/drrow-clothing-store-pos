package com.drrow.pos.dao;

import com.drrow.pos.model.Size;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SizeDAO {

    public List<Size> findAll() throws SQLException {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT size_id, size_name, display_order, is_active FROM sizes ORDER BY display_order ASC, size_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Size(
                    rs.getInt("size_id"),
                    rs.getString("size_name"),
                    rs.getInt("display_order"),
                    rs.getInt("is_active") == 1
                ));
            }
        }
        return list;
    }

    public Size findById(int id) throws SQLException {
        String sql = "SELECT size_id, size_name, display_order, is_active FROM sizes WHERE size_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Size(
                        rs.getInt("size_id"),
                        rs.getString("size_name"),
                        rs.getInt("display_order"),
                        rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    public boolean create(Size s) throws SQLException {
        String sql = "INSERT INTO sizes (size_name, display_order, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = DatabaseConnection.prepareInsert(conn, sql)) {
            ps.setString(1, s.getSizeName());
            ps.setInt(2, s.getDisplayOrder());
            ps.setInt(3, s.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, conn);
                if (key > 0) s.setSizeId(key);
                return true;
            }
        }
        return false;
    }

    public boolean update(Size s) throws SQLException {
        String sql = "UPDATE sizes SET size_name = ?, display_order = ?, is_active = ? WHERE size_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSizeName());
            ps.setInt(2, s.getDisplayOrder());
            ps.setInt(3, s.isActive() ? 1 : 0);
            ps.setInt(4, s.getSizeId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM sizes WHERE size_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}

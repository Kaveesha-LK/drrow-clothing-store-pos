package com.drrow.pos.dao;

import com.drrow.pos.model.Colour;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColourDAO {

    public List<Colour> findAll() throws SQLException {
        List<Colour> list = new ArrayList<>();
        String sql = "SELECT colour_id, colour_name, hex_code, is_active FROM colours ORDER BY colour_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Colour(
                    rs.getInt("colour_id"),
                    rs.getString("colour_name"),
                    rs.getString("hex_code"),
                    rs.getInt("is_active") == 1
                ));
            }
        }
        return list;
    }

    public Colour findById(int id) throws SQLException {
        String sql = "SELECT colour_id, colour_name, hex_code, is_active FROM colours WHERE colour_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Colour(
                        rs.getInt("colour_id"),
                        rs.getString("colour_name"),
                        rs.getString("hex_code"),
                        rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    public boolean create(Colour c) throws SQLException {
        String sql = "INSERT INTO colours (colour_name, hex_code, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = DatabaseConnection.prepareInsert(conn, sql)) {
            ps.setString(1, c.getColourName());
            ps.setString(2, c.getHexCode());
            ps.setInt(3, c.isActive() ? 1 : 0);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, conn);
                if (key > 0) c.setColourId(key);
                return true;
            }
        }
        return false;
    }

    public boolean update(Colour c) throws SQLException {
        String sql = "UPDATE colours SET colour_name = ?, hex_code = ?, is_active = ? WHERE colour_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getColourName());
            ps.setString(2, c.getHexCode());
            ps.setInt(3, c.isActive() ? 1 : 0);
            ps.setInt(4, c.getColourId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM colours WHERE colour_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}

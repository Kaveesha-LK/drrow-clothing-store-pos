package com.drrow.pos.dao;

import com.drrow.pos.model.Setting;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SettingDAO {

    public Map<String, String> getAllSettings() throws SQLException {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return map;
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException ignored) {}
        return defaultValue;
    }

    public boolean saveOrUpdate(String key, String value, String group, String description) throws SQLException {
        String selectSql = "SELECT COUNT(*) FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }

            if (exists) {
                String updateSql = "UPDATE settings SET setting_value = ? WHERE setting_key = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, value);
                    ps.setString(2, key);
                    return ps.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO settings (setting_key, setting_value, setting_group, description) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, key);
                    ps.setString(2, value);
                    ps.setString(3, group != null ? group : "GENERAL");
                    ps.setString(4, description != null ? description : "");
                    return ps.executeUpdate() > 0;
                }
            }
        }
    }
}

package com.drrow.pos.dao;

import com.drrow.pos.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditLogDAO {

    public void log(AuditLog log, Connection conn) {
        boolean manageConn = (conn == null);
        Connection c = null;
        try {
            c = manageConn ? DatabaseConnection.getConnection() : conn;
            String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, description, ip_address) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (log.getUserId() != null && log.getUserId() > 0) {
                    ps.setInt(1, log.getUserId());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setString(2, log.getUsername());
                ps.setString(3, log.getAction());
                ps.setString(4, log.getEntityType());
                ps.setString(5, log.getEntityId());
                ps.setString(6, log.getDescription());
                ps.setString(7, log.getIpAddress());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Audit log error: " + e.getMessage());
        } finally {
            if (manageConn && c != null) {
                try { c.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<AuditLog> getRecentLogs(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT log_id, user_id, username, action, entity_type, entity_id, description, ip_address, created_at " +
                     "FROM audit_logs ORDER BY created_at DESC";
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

    public List<AuditLog> searchLogs(String action, Integer userId, Date from, Date to) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT log_id, user_id, username, action, entity_type, entity_id, description, ip_address, created_at " +
            "FROM audit_logs WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (action != null && !action.trim().isEmpty() && !"ALL".equalsIgnoreCase(action)) {
            sql.append("AND action = ? ");
            params.add(action.trim());
        }
        if (userId != null && userId > 0) {
            sql.append("AND user_id = ? ");
            params.add(userId);
        }
        if (from != null) {
            sql.append("AND created_at >= ? ");
            params.add(new Timestamp(from.getTime()));
        }
        if (to != null) {
            sql.append("AND created_at <= ? ");
            params.add(new Timestamp(to.getTime()));
        }
        sql.append("ORDER BY created_at DESC");

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

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog al = new AuditLog();
        al.setLogId(rs.getInt("log_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) al.setUserId(uid);
        al.setUsername(rs.getString("username"));
        al.setAction(rs.getString("action"));
        al.setEntityType(rs.getString("entity_type"));
        al.setEntityId(rs.getString("entity_id"));
        al.setDescription(rs.getString("description"));
        al.setIpAddress(rs.getString("ip_address"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) al.setCreatedAt(new java.util.Date(ct.getTime()));
        return al;
    }
}

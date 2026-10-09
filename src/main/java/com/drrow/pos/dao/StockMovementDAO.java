package com.drrow.pos.dao;

import com.drrow.pos.model.StockMovement;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockMovementDAO {

    public boolean recordMovement(StockMovement sm, Connection conn) throws SQLException {
        boolean manageConn = (conn == null);
        Connection c = manageConn ? DatabaseConnection.getConnection() : conn;
        String sql = "INSERT INTO stock_movements (variant_id, movement_type, quantity, before_quantity, after_quantity, " +
                     "reference_id, reference_type, notes, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.prepareInsert(c, sql)) {
            ps.setInt(1, sm.getVariantId());
            ps.setString(2, sm.getMovementType());
            ps.setInt(3, sm.getQuantity());
            ps.setInt(4, sm.getBeforeQuantity());
            ps.setInt(5, sm.getAfterQuantity());
            ps.setString(6, sm.getReferenceId());
            ps.setString(7, sm.getReferenceType());
            ps.setString(8, sm.getNotes());
            ps.setInt(9, sm.getUserId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, c);
                if (key > 0) sm.setMovementId(key);
                return true;
            }
            return false;
        } finally {
            if (manageConn) c.close();
        }
    }

    public List<StockMovement> getRecentMovements(int limit) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.movement_id, sm.variant_id, p.name AS product_name, pv.sku, pv.barcode, " +
                     "s.size_name, c.colour_name, sm.movement_type, sm.quantity, sm.before_quantity, sm.after_quantity, " +
                     "sm.reference_id, sm.reference_type, sm.notes, sm.user_id, u.username, sm.created_at " +
                     "FROM stock_movements sm " +
                     "JOIN product_variants pv ON sm.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "JOIN users u ON sm.user_id = u.user_id " +
                     "ORDER BY sm.created_at DESC";

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

    public List<StockMovement> getMovementsByDateRange(Date start, Date end) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.movement_id, sm.variant_id, p.name AS product_name, pv.sku, pv.barcode, " +
                     "s.size_name, c.colour_name, sm.movement_type, sm.quantity, sm.before_quantity, sm.after_quantity, " +
                     "sm.reference_id, sm.reference_type, sm.notes, sm.user_id, u.username, sm.created_at " +
                     "FROM stock_movements sm " +
                     "JOIN product_variants pv ON sm.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "JOIN users u ON sm.user_id = u.user_id " +
                     "WHERE sm.created_at >= ? AND sm.created_at <= ? " +
                     "ORDER BY sm.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private StockMovement mapRow(ResultSet rs) throws SQLException {
        StockMovement sm = new StockMovement();
        sm.setMovementId(rs.getInt("movement_id"));
        sm.setVariantId(rs.getInt("variant_id"));
        sm.setProductName(rs.getString("product_name"));
        sm.setVariantDetails(rs.getString("size_name") + " / " + rs.getString("colour_name") + " (" + rs.getString("sku") + ")");
        sm.setBarcode(rs.getString("barcode"));
        sm.setMovementType(rs.getString("movement_type"));
        sm.setQuantity(rs.getInt("quantity"));
        sm.setBeforeQuantity(rs.getInt("before_quantity"));
        sm.setAfterQuantity(rs.getInt("after_quantity"));
        sm.setReferenceId(rs.getString("reference_id"));
        sm.setReferenceType(rs.getString("reference_type"));
        sm.setNotes(rs.getString("notes"));
        sm.setUserId(rs.getInt("user_id"));
        sm.setUsername(rs.getString("username"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) sm.setCreatedAt(new java.util.Date(ct.getTime()));
        return sm;
    }
}

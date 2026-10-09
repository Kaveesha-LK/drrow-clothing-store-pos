package com.drrow.pos.dao;

import com.drrow.pos.model.User;
import com.drrow.pos.util.SecurityUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.full_name, u.email, u.phone, " +
                     "u.role_id, r.role_name, u.is_active, u.require_password_change, u.last_login, u.created_at " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean active = rs.getInt("is_active") == 1;
                    if (!active) {
                        return null; // Inactive user
                    }
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtil.checkPassword(password, storedHash)) {
                        User user = mapRow(rs);
                        RoleDAO roleDao = new RoleDAO();
                        user.setPermissions(roleDao.getPermissionCodesForRole(user.getRoleId()));
                        updateLastLogin(user.getUserId());
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.full_name, u.email, u.phone, " +
                     "u.role_id, r.role_name, u.is_active, u.require_password_change, u.last_login, u.created_at " +
                     "FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    RoleDAO roleDao = new RoleDAO();
                    user.setPermissions(roleDao.getPermissionCodesForRole(user.getRoleId()));
                    return user;
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.full_name, u.email, u.phone, " +
                     "u.role_id, r.role_name, u.is_active, u.require_password_change, u.last_login, u.created_at " +
                     "FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.full_name, u.email, u.phone, " +
                     "u.role_id, r.role_name, u.is_active, u.require_password_change, u.last_login, u.created_at " +
                     "FROM users u JOIN roles r ON u.role_id = r.role_id ORDER BY u.user_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean create(User user, String rawPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role_id, is_active, require_password_change) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String hash = SecurityUtil.hashPassword(rawPassword);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = DatabaseConnection.prepareInsert(conn, sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, hash);
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setInt(6, user.getRoleId());
            ps.setInt(7, user.isActive() ? 1 : 0);
            ps.setInt(8, user.isRequirePasswordChange() ? 1 : 0);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                int key = DatabaseConnection.getGeneratedKey(ps, conn);
                if (key > 0) user.setUserId(key);
                return true;
            }
        }
        return false;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role_id = ?, is_active = ?, require_password_change = ? " +
                     "WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setInt(4, user.getRoleId());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setInt(6, user.isRequirePasswordChange() ? 1 : 0);
            ps.setInt(7, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newRawPassword) throws SQLException {
        String hash = SecurityUtil.hashPassword(newRawPassword);
        String sql = "UPDATE users SET password_hash = ?, require_password_change = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRoleId(rs.getInt("role_id"));
        user.setRoleName(rs.getString("role_name"));
        user.setActive(rs.getInt("is_active") == 1);
        user.setRequirePasswordChange(rs.getInt("require_password_change") == 1);
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(new java.util.Date(lastLogin.getTime()));
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(new java.util.Date(created.getTime()));
        }
        return user;
    }
}

package com.drrow.pos.dao;

import com.drrow.pos.model.Role;
import com.drrow.pos.model.Permission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    public List<Role> findAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT role_id, role_name, description FROM roles ORDER BY role_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description")));
            }
        }
        return list;
    }

    public Role findById(int roleId) throws SQLException {
        String sql = "SELECT role_id, role_name, description FROM roles WHERE role_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description"));
                    role.setPermissions(getPermissionsForRole(roleId));
                    return role;
                }
            }
        }
        return null;
    }

    public List<Permission> getPermissionsForRole(int roleId) throws SQLException {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT p.permission_id, p.permission_code, p.description " +
                     "FROM permissions p " +
                     "JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                     "WHERE rp.role_id = ? ORDER BY p.permission_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Permission(rs.getInt("permission_id"), rs.getString("permission_code"), rs.getString("description")));
                }
            }
        }
        return list;
    }

    public List<String> getPermissionCodesForRole(int roleId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT p.permission_code " +
                     "FROM permissions p " +
                     "JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                     "WHERE rp.role_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("permission_code"));
                }
            }
        }
        return list;
    }
}

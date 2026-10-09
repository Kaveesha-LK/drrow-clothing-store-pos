package com.drrow.pos.model;

import java.util.Date;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private int roleId;
    private String roleName;
    private boolean active;
    private boolean requirePasswordChange;
    private Date lastLogin;
    private Date createdAt;
    private List<String> permissions;

    public User() {}

    public User(int userId, String username, String fullName, int roleId, String roleName, boolean active) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.active = active;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isRequirePasswordChange() { return requirePasswordChange; }
    public void setRequirePasswordChange(boolean requirePasswordChange) { this.requirePasswordChange = requirePasswordChange; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public boolean hasPermission(String permissionCode) {
        if ("ADMIN".equalsIgnoreCase(roleName)) return true;
        return permissions != null && permissions.contains(permissionCode);
    }
}

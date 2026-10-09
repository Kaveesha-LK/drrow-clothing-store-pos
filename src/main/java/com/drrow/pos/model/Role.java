package com.drrow.pos.model;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private int roleId;
    private String roleName;
    private String description;
    private List<Permission> permissions = new ArrayList<>();

    public Role() {}

    public Role(int roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }

    @Override
    public String toString() {
        return roleName;
    }
}

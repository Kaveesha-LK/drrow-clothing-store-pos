package com.drrow.pos.model;

public class Permission {
    private int permissionId;
    private String permissionCode;
    private String description;

    public Permission() {}

    public Permission(int permissionId, String permissionCode, String description) {
        this.permissionId = permissionId;
        this.permissionCode = permissionCode;
        this.description = description;
    }

    public int getPermissionId() { return permissionId; }
    public void setPermissionId(int permissionId) { this.permissionId = permissionId; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return permissionCode;
    }
}

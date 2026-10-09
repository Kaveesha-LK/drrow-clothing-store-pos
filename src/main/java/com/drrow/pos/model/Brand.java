package com.drrow.pos.model;

public class Brand {
    private int brandId;
    private String name;
    private String description;
    private boolean active = true;

    public Brand() {}

    public Brand(int brandId, String name, String description, boolean active) {
        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;
    }
}

package com.drrow.pos.model;

public class Category {
    private int categoryId;
    private String name;
    private String description;
    private boolean active = true;

    public Category() {}

    public Category(int categoryId, String name, String description, boolean active) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

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

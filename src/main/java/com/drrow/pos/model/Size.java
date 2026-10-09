package com.drrow.pos.model;

public class Size {
    private int sizeId;
    private String sizeName;
    private int displayOrder;
    private boolean active = true;

    public Size() {}

    public Size(int sizeId, String sizeName, int displayOrder, boolean active) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public int getSizeId() { return sizeId; }
    public void setSizeId(int sizeId) { this.sizeId = sizeId; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return sizeName;
    }
}

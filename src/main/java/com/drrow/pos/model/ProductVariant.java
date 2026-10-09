package com.drrow.pos.model;

import java.util.Date;

public class ProductVariant {
    private int variantId;
    private int productId;
    private String productName;
    private String itemCode;
    private String sku;
    private String barcode;
    private int sizeId;
    private String sizeName;
    private int colourId;
    private String colourName;
    private double purchasePrice;
    private double sellingPrice;
    private boolean active = true;
    private Date createdAt;

    // Joined inventory attributes
    private int currentStock;
    private int availableStock;
    private int reorderLevel = 5;
    private String brandName;
    private String categoryName;

    public ProductVariant() {}

    public ProductVariant(int variantId, int productId, String sku, String barcode, int sizeId, int colourId, double purchasePrice, double sellingPrice) {
        this.variantId = variantId;
        this.productId = productId;
        this.sku = sku;
        this.barcode = barcode;
        this.sizeId = sizeId;
        this.colourId = colourId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public int getSizeId() { return sizeId; }
    public void setSizeId(int sizeId) { this.sizeId = sizeId; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public int getColourId() { return colourId; }
    public void setColourId(int colourId) { this.colourId = colourId; }

    public String getColourName() { return colourName; }
    public void setColourName(String colourName) { this.colourName = colourName; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isLowStock() {
        return currentStock > 0 && currentStock <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return currentStock <= 0;
    }

    public String getVariantDisplayName() {
        return (productName != null ? productName : "") + " [" +
               (sizeName != null ? sizeName : "") + " / " +
               (colourName != null ? colourName : "") + "]";
    }

    @Override
    public String toString() {
        return getVariantDisplayName() + " - " + barcode;
    }
}

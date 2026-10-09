package com.drrow.pos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product {
    private int productId;
    private String itemCode;
    private String name;
    private int categoryId;
    private String categoryName;
    private int brandId;
    private String brandName;
    private String description;
    private double purchasePrice;
    private double sellingPrice;
    private int reorderLevel = 5;
    private boolean active = true;
    private Date createdAt;
    private Date updatedAt;
    private List<ProductVariant> variants = new ArrayList<>();

    public Product() {}

    public Product(int productId, String itemCode, String name, int categoryId, int brandId, double purchasePrice, double sellingPrice) {
        this.productId = productId;
        this.itemCode = itemCode;
        this.name = name;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    @Override
    public String toString() {
        return name + " (" + itemCode + ")";
    }
}

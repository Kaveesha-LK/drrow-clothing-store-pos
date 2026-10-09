package com.drrow.pos.model;

public class CartItem {
    private int variantId;
    private int productId;
    private String productName;
    private String itemCode;
    private String sku;
    private String barcode;
    private String sizeName;
    private String colourName;
    private double unitPrice;
    private double purchasePrice;
    private int quantity = 1;
    private double discountAmount = 0.00;
    private int availableStock;

    public CartItem() {}

    public CartItem(ProductVariant variant) {
        this.variantId = variant.getVariantId();
        this.productId = variant.getProductId();
        this.productName = variant.getProductName();
        this.itemCode = variant.getItemCode();
        this.sku = variant.getSku();
        this.barcode = variant.getBarcode();
        this.sizeName = variant.getSizeName();
        this.colourName = variant.getColourName();
        this.unitPrice = variant.getSellingPrice();
        this.purchasePrice = variant.getPurchasePrice();
        this.availableStock = variant.getAvailableStock();
        this.quantity = 1;
        this.discountAmount = 0.00;
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

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public String getColourName() { return colourName; }
    public void setColourName(String colourName) { this.colourName = colourName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public double getLineTotal() {
        double subtotal = unitPrice * quantity;
        double total = subtotal - discountAmount;
        return Math.max(0.00, total);
    }
}

package com.drrow.pos.model;

public class ReturnItem {
    private int itemId;
    private int returnId;
    private int saleItemId;
    private int variantId;
    private String productName;
    private String sku;
    private String barcode;
    private String sizeName;
    private String colourName;
    private int quantity;
    private double unitPrice;
    private double refundAmount;
    private String restockStatus = "RESTOCKED"; // RESTOCKED, DAMAGED_DISCARD

    public ReturnItem() {}

    public ReturnItem(int saleItemId, int variantId, String productName, String sku, String sizeName, String colourName, int quantity, double unitPrice, double refundAmount, String restockStatus) {
        this.saleItemId = saleItemId;
        this.variantId = variantId;
        this.productName = productName;
        this.sku = sku;
        this.sizeName = sizeName;
        this.colourName = colourName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.refundAmount = refundAmount;
        this.restockStatus = restockStatus;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getSaleItemId() { return saleItemId; }
    public void setSaleItemId(int saleItemId) { this.saleItemId = saleItemId; }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public String getColourName() { return colourName; }
    public void setColourName(String colourName) { this.colourName = colourName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }

    public String getRestockStatus() { return restockStatus; }
    public void setRestockStatus(String restockStatus) { this.restockStatus = restockStatus; }
}

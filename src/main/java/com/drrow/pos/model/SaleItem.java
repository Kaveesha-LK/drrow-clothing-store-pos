package com.drrow.pos.model;

public class SaleItem {
    private int itemId;
    private int saleId;
    private int variantId;
    private String productName;
    private String sku;
    private String barcode;
    private String sizeName;
    private String colourName;
    private int quantity;
    private double unitPrice;
    private double discountAmount;
    private double lineTotal;

    public SaleItem() {}

    public SaleItem(int variantId, String productName, String sku, String barcode, String sizeName, String colourName, int quantity, double unitPrice, double discountAmount, double lineTotal) {
        this.variantId = variantId;
        this.productName = productName;
        this.sku = sku;
        this.barcode = barcode;
        this.sizeName = sizeName;
        this.colourName = colourName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
        this.lineTotal = lineTotal;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

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

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}

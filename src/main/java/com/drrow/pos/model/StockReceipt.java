package com.drrow.pos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockReceipt {
    private int receiptId;
    private String receiptNumber;
    private int supplierId;
    private String supplierName;
    private int userId;
    private String username;
    private Date receiptDate;
    private double totalAmount;
    private String referenceNumber;
    private String notes;
    private Date createdAt;
    private List<StockReceiptItem> items = new ArrayList<>();

    public StockReceipt() {}

    public StockReceipt(int receiptId, String receiptNumber, int supplierId, int userId, Date receiptDate, double totalAmount, String referenceNumber, String notes) {
        this.receiptId = receiptId;
        this.receiptNumber = receiptNumber;
        this.supplierId = supplierId;
        this.userId = userId;
        this.receiptDate = receiptDate;
        this.totalAmount = totalAmount;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
    }

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Date receiptDate) { this.receiptDate = receiptDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<StockReceiptItem> getItems() { return items; }
    public void setItems(List<StockReceiptItem> items) { this.items = items; }
}

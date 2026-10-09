package com.drrow.pos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Return {
    private int returnId;
    private String returnNumber;
    private int saleId;
    private String invoiceNumber;
    private int userId;
    private String cashierName;
    private Date returnDate;
    private double totalRefundAmount;
    private String returnReason;
    private String actionTaken = "REFUND"; // REFUND, EXCHANGE
    private Date createdAt;
    private List<ReturnItem> items = new ArrayList<>();

    public Return() {}

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public double getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(double totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<ReturnItem> getItems() { return items; }
    public void setItems(List<ReturnItem> items) { this.items = items; }
}

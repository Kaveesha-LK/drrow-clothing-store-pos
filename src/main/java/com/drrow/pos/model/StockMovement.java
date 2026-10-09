package com.drrow.pos.model;

import java.util.Date;

public class StockMovement {
    private int movementId;
    private int variantId;
    private String productName;
    private String variantDetails;
    private String barcode;
    private String movementType; // RECEIVE, SALE, RETURN, ADJUSTMENT_DAMAGE, ADJUSTMENT_LOST, ADJUSTMENT_CORRECTION, ADJUSTMENT_FOUND, ADJUSTMENT_MANUAL
    private int quantity;
    private int beforeQuantity;
    private int afterQuantity;
    private String referenceId;
    private String referenceType;
    private String notes;
    private int userId;
    private String username;
    private Date createdAt;

    public StockMovement() {}

    public StockMovement(int variantId, String movementType, int quantity, int beforeQuantity, int afterQuantity, String referenceId, String referenceType, String notes, int userId) {
        this.variantId = variantId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.notes = notes;
        this.userId = userId;
    }

    public int getMovementId() { return movementId; }
    public void setMovementId(int movementId) { this.movementId = movementId; }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantDetails() { return variantDetails; }
    public void setVariantDetails(String variantDetails) { this.variantDetails = variantDetails; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getBeforeQuantity() { return beforeQuantity; }
    public void setBeforeQuantity(int beforeQuantity) { this.beforeQuantity = beforeQuantity; }

    public int getAfterQuantity() { return afterQuantity; }
    public void setAfterQuantity(int afterQuantity) { this.afterQuantity = afterQuantity; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

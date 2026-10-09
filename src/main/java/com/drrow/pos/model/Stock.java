package com.drrow.pos.model;

import java.util.Date;

public class Stock {
    private int stockId;
    private int variantId;
    private int currentStock;
    private int availableStock;
    private int reservedStock;
    private Date lastUpdated;

    public Stock() {}

    public Stock(int stockId, int variantId, int currentStock, int availableStock, int reservedStock) {
        this.stockId = stockId;
        this.variantId = variantId;
        this.currentStock = currentStock;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
    }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
}

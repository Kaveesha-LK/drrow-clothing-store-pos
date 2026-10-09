package com.drrow.pos.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardStats {
    private double todaySales;
    private int todayTransactions;
    private double todayProfit;
    private int lowStockCount;
    private int outOfStockCount;
    private int totalProducts;
    private int totalStockQuantity;
    private List<Sale> recentSales = new ArrayList<>();
    private List<ProductVariant> lowStockVariants = new ArrayList<>();

    public DashboardStats() {}

    public double getTodaySales() { return todaySales; }
    public void setTodaySales(double todaySales) { this.todaySales = todaySales; }

    public int getTodayTransactions() { return todayTransactions; }
    public void setTodayTransactions(int todayTransactions) { this.todayTransactions = todayTransactions; }

    public double getTodayProfit() { return todayProfit; }
    public void setTodayProfit(double todayProfit) { this.todayProfit = todayProfit; }

    public int getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(int lowStockCount) { this.lowStockCount = lowStockCount; }

    public int getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(int outOfStockCount) { this.outOfStockCount = outOfStockCount; }

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getTotalStockQuantity() { return totalStockQuantity; }
    public void setTotalStockQuantity(int totalStockQuantity) { this.totalStockQuantity = totalStockQuantity; }

    public List<Sale> getRecentSales() { return recentSales; }
    public void setRecentSales(List<Sale> recentSales) { this.recentSales = recentSales; }

    public List<ProductVariant> getLowStockVariants() { return lowStockVariants; }
    public void setLowStockVariants(List<ProductVariant> lowStockVariants) { this.lowStockVariants = lowStockVariants; }
}

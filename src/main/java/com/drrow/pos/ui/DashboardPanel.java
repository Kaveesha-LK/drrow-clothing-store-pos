package com.drrow.pos.ui;

import com.drrow.pos.model.DashboardStats;
import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.model.Sale;
import com.drrow.pos.service.SalesService;
import com.drrow.pos.ui.components.KpiCard;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.theme.UITheme;
import com.drrow.pos.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final SalesService salesService = new SalesService();

    // KPI Cards
    private final KpiCard cardTodaySales = new KpiCard("Today's Gross Sales", "Rs. 0.00", "Updated real-time", UITheme.COLOR_GOLD);
    private final KpiCard cardTransactions = new KpiCard("Today's Transactions", "0 Invoices", "Completed checkouts", UITheme.COLOR_PRIMARY);
    private final KpiCard cardProfit = new KpiCard("Est. Gross Margin", "Rs. 0.00", "Based on cost of goods", UITheme.COLOR_SUCCESS);
    private final KpiCard cardLowStock = new KpiCard("Low Stock Alerts", "0 Items", "Stock <= 5 units", UITheme.COLOR_WARNING);
    private final KpiCard cardTotalStock = new KpiCard("Total Inventory Units", "0 Units", "Warehouse & floor total", new Color(139, 92, 246));

    // Tables
    private final DefaultTableModel recentSalesModel = new DefaultTableModel(
        new String[]{"Invoice #", "Time", "Customer", "Subtotal", "Grand Total", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable recentSalesTable = new ModernTable(recentSalesModel);

    private final DefaultTableModel lowStockModel = new DefaultTableModel(
        new String[]{"Barcode", "Item Name", "Size", "Colour", "Current Stock", "Reorder Level"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final ModernTable lowStockTable = new ModernTable(lowStockModel);

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("D’RROW EXECUTIVE DASHBOARD");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_GOLD);

        ModernButton btnRefresh = new ModernButton("Refresh Dashboard", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshData());

        topHeader.add(lblTitle, BorderLayout.WEST);
        topHeader.add(btnRefresh, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // KPI Cards Row
        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 100));

        kpiPanel.add(cardTodaySales);
        kpiPanel.add(cardTransactions);
        kpiPanel.add(cardProfit);
        kpiPanel.add(cardLowStock);
        kpiPanel.add(cardTotalStock);

        // Center Tables: Left Recent Sales, Right Low Stock Alerts
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        tablesPanel.setOpaque(false);

        JScrollPane recentScroll = new JScrollPane(recentSalesTable);
        recentScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Recent Sales Transactions", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_GOLD));

        JScrollPane lowScroll = new JScrollPane(lowStockTable);
        lowScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER), "Urgent Reorder Alerts (Low Stock)", 0, 0, UITheme.FONT_BOLD, UITheme.COLOR_DANGER));

        tablesPanel.add(recentScroll);
        tablesPanel.add(lowScroll);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(kpiPanel, BorderLayout.NORTH);
        centerContainer.add(tablesPanel, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        try {
            DashboardStats stats = salesService.getDashboardStats();
            cardTodaySales.setValue(FormatUtil.formatCurrency(stats.getTodaySales()));
            cardTransactions.setValue(stats.getTodayTransactions() + " Invoices");
            cardProfit.setValue(FormatUtil.formatCurrency(stats.getTodayProfit()));
            cardLowStock.setValue(stats.getLowStockCount() + " Items");
            cardTotalStock.setValue(stats.getTotalStockQuantity() + " Units");

            // Populate Recent Sales
            recentSalesModel.setRowCount(0);
            for (Sale s : stats.getRecentSales()) {
                recentSalesModel.addRow(new Object[]{
                    s.getInvoiceNumber(),
                    FormatUtil.formatTime(s.getSaleDate()),
                    s.getCustomerName() != null ? s.getCustomerName() : "Walk-In Customer",
                    FormatUtil.formatDecimal(s.getSubtotal()),
                    FormatUtil.formatDecimal(s.getGrandTotal()),
                    s.getPaymentStatus()
                });
            }

            // Populate Low Stock
            lowStockModel.setRowCount(0);
            for (ProductVariant v : stats.getLowStockVariants()) {
                lowStockModel.addRow(new Object[]{
                    v.getBarcode(),
                    v.getProductName(),
                    v.getSizeName(),
                    v.getColourName(),
                    v.getCurrentStock(),
                    v.getReorderLevel()
                });
            }

        } catch (Exception ex) {
            System.err.println("Dashboard refresh error: " + ex.getMessage());
        }
    }
}

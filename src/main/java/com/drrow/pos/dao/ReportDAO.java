package com.drrow.pos.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportDAO {

    public static class ReportRow {
        private final List<Object> values = new ArrayList<>();

        public void add(Object v) { values.add(v); }
        public List<Object> getValues() { return values; }
        public Object get(int index) { return values.get(index); }
    }

    public static class ReportResult {
        private final List<String> columns = new ArrayList<>();
        private final List<ReportRow> rows = new ArrayList<>();

        public List<String> getColumns() { return columns; }
        public List<ReportRow> getRows() { return rows; }
    }

    public ReportResult getDailySalesReport(Date day) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Invoice #");
        result.columns.add("Time");
        result.columns.add("Cashier");
        result.columns.add("Customer");
        result.columns.add("Subtotal");
        result.columns.add("Discount");
        result.columns.add("Grand Total");
        result.columns.add("Status");

        java.time.LocalDate d = new java.sql.Date(day.getTime()).toLocalDate();
        Timestamp start = Timestamp.valueOf(d.atStartOfDay());
        Timestamp end = Timestamp.valueOf(d.plusDays(1).atStartOfDay());

        String sql = "SELECT s.invoice_number, s.sale_date, u.username, COALESCE(c.name, 'Walk-In') AS customer_name, " +
                     "s.subtotal, s.discount_amount, s.grand_total, s.payment_status " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "WHERE s.sale_date >= ? AND s.sale_date < ? ORDER BY s.sale_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getString("invoice_number"));
                    row.add(rs.getTime("sale_date"));
                    row.add(rs.getString("username"));
                    row.add(rs.getString("customer_name"));
                    row.add(rs.getDouble("subtotal"));
                    row.add(rs.getDouble("discount_amount"));
                    row.add(rs.getDouble("grand_total"));
                    row.add(rs.getString("payment_status"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getMonthlySalesReport(int year, int month) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Date");
        result.columns.add("Invoices");
        result.columns.add("Items Sold");
        result.columns.add("Total Sales");
        result.columns.add("Total Discounts");
        result.columns.add("Net Revenue");

        java.time.LocalDate start = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate end = start.plusMonths(1);

        String sql = "SELECT DATE(s.sale_date) AS s_date, COUNT(DISTINCT s.sale_id) AS inv_count, " +
                     "COALESCE(SUM(si.quantity), 0) AS total_items, " +
                     "COALESCE(SUM(s.subtotal), 0) AS total_subtotal, " +
                     "COALESCE(SUM(s.discount_amount), 0) AS total_discount, " +
                     "COALESCE(SUM(s.grand_total), 0) AS net_revenue " +
                     "FROM sales s " +
                     "LEFT JOIN sale_items si ON s.sale_id = si.sale_id " +
                     "WHERE s.sale_date >= ? AND s.sale_date < ? AND s.payment_status = 'COMPLETED' " +
                     "GROUP BY DATE(s.sale_date) ORDER BY s_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getDate("s_date"));
                    row.add(rs.getInt("inv_count"));
                    row.add(rs.getInt("total_items"));
                    row.add(rs.getDouble("total_subtotal"));
                    row.add(rs.getDouble("total_discount"));
                    row.add(rs.getDouble("net_revenue"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getSalesByCashier(Date start, Date end) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Cashier Username");
        result.columns.add("Staff Name");
        result.columns.add("Transactions");
        result.columns.add("Gross Sales");
        result.columns.add("Total Discounts");
        result.columns.add("Net Revenue");

        String sql = "SELECT u.username, u.full_name, COUNT(s.sale_id) AS txn_count, " +
                     "COALESCE(SUM(s.subtotal), 0) AS gross_sales, " +
                     "COALESCE(SUM(s.discount_amount), 0) AS total_discounts, " +
                     "COALESCE(SUM(s.grand_total), 0) AS net_revenue " +
                     "FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "WHERE s.sale_date >= ? AND s.sale_date <= ? AND s.payment_status = 'COMPLETED' " +
                     "GROUP BY u.username, u.full_name ORDER BY net_revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getString("username"));
                    row.add(rs.getString("full_name"));
                    row.add(rs.getInt("txn_count"));
                    row.add(rs.getDouble("gross_sales"));
                    row.add(rs.getDouble("total_discounts"));
                    row.add(rs.getDouble("net_revenue"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getProductSalesReport(Date start, Date end) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Barcode");
        result.columns.add("Product Name");
        result.columns.add("Category");
        result.columns.add("Size");
        result.columns.add("Colour");
        result.columns.add("Quantity Sold");
        result.columns.add("Total Revenue");

        String sql = "SELECT pv.barcode, p.name AS product_name, cat.name AS category_name, " +
                     "s.size_name, c.colour_name, " +
                     "SUM(si.quantity) AS qty_sold, SUM(si.line_total) AS total_revenue " +
                     "FROM sale_items si " +
                     "JOIN sales sl ON si.sale_id = sl.sale_id " +
                     "JOIN product_variants pv ON si.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN categories cat ON p.category_id = cat.category_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "WHERE sl.sale_date >= ? AND sl.sale_date <= ? AND sl.payment_status = 'COMPLETED' " +
                     "GROUP BY pv.barcode, p.name, cat.name, s.size_name, c.colour_name " +
                     "ORDER BY total_revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getString("barcode"));
                    row.add(rs.getString("product_name"));
                    row.add(rs.getString("category_name"));
                    row.add(rs.getString("size_name"));
                    row.add(rs.getString("colour_name"));
                    row.add(rs.getInt("qty_sold"));
                    row.add(rs.getDouble("total_revenue"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getCategorySalesReport(Date start, Date end) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Category Name");
        result.columns.add("Units Sold");
        result.columns.add("Total Sales");

        String sql = "SELECT cat.name AS category_name, SUM(si.quantity) AS units_sold, SUM(si.line_total) AS total_sales " +
                     "FROM sale_items si " +
                     "JOIN sales sl ON si.sale_id = sl.sale_id " +
                     "JOIN product_variants pv ON si.variant_id = pv.variant_id " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN categories cat ON p.category_id = cat.category_id " +
                     "WHERE sl.sale_date >= ? AND sl.sale_date <= ? AND sl.payment_status = 'COMPLETED' " +
                     "GROUP BY cat.name ORDER BY total_sales DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getString("category_name"));
                    row.add(rs.getInt("units_sold"));
                    row.add(rs.getDouble("total_sales"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getStockValuationReport() throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Barcode");
        result.columns.add("Product Name");
        result.columns.add("SKU");
        result.columns.add("Size");
        result.columns.add("Colour");
        result.columns.add("Current Stock");
        result.columns.add("Cost Price");
        result.columns.add("Selling Price");
        result.columns.add("Total Cost Value");
        result.columns.add("Total Retail Value");

        String sql = "SELECT pv.barcode, p.name AS product_name, pv.sku, s.size_name, c.colour_name, " +
                     "COALESCE(st.current_stock, 0) AS current_stock, pv.purchase_price, pv.selling_price " +
                     "FROM product_variants pv " +
                     "JOIN products p ON pv.product_id = p.product_id " +
                     "JOIN sizes s ON pv.size_id = s.size_id " +
                     "JOIN colours c ON pv.colour_id = c.colour_id " +
                     "LEFT JOIN stock st ON pv.variant_id = st.variant_id " +
                     "WHERE pv.is_active = 1 ORDER BY p.name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stock = rs.getInt("current_stock");
                double cost = rs.getDouble("purchase_price");
                double retail = rs.getDouble("selling_price");

                ReportRow row = new ReportRow();
                row.add(rs.getString("barcode"));
                row.add(rs.getString("product_name"));
                row.add(rs.getString("sku"));
                row.add(rs.getString("size_name"));
                row.add(rs.getString("colour_name"));
                row.add(stock);
                row.add(cost);
                row.add(retail);
                row.add(stock * cost);
                row.add(stock * retail);
                result.rows.add(row);
            }
        }
        return result;
    }

    public ReportResult getProfitEstimateReport(Date start, Date end) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Invoice #");
        result.columns.add("Date");
        result.columns.add("Gross Revenue");
        result.columns.add("Cost of Goods");
        result.columns.add("Estimated Gross Profit");
        result.columns.add("Margin %");

        String sql = "SELECT s.invoice_number, s.sale_date, s.grand_total, " +
                     "SUM(pv.purchase_price * si.quantity) AS total_cost " +
                     "FROM sales s " +
                     "JOIN sale_items si ON s.sale_id = si.sale_id " +
                     "JOIN product_variants pv ON si.variant_id = pv.variant_id " +
                     "WHERE s.sale_date >= ? AND s.sale_date <= ? AND s.payment_status = 'COMPLETED' " +
                     "GROUP BY s.invoice_number, s.sale_date, s.grand_total " +
                     "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double revenue = rs.getDouble("grand_total");
                    double cost = rs.getDouble("total_cost");
                    double profit = revenue - cost;
                    double margin = revenue > 0 ? (profit / revenue) * 100 : 0.0;

                    ReportRow row = new ReportRow();
                    row.add(rs.getString("invoice_number"));
                    row.add(rs.getTimestamp("sale_date"));
                    row.add(revenue);
                    row.add(cost);
                    row.add(profit);
                    row.add(Math.round(margin * 10.0) / 10.0);
                    result.rows.add(row);
                }
            }
        }
        return result;
    }

    public ReportResult getReturnsReport(Date start, Date end) throws SQLException {
        ReportResult result = new ReportResult();
        result.columns.add("Return #");
        result.columns.add("Invoice #");
        result.columns.add("Date");
        result.columns.add("Cashier");
        result.columns.add("Refund Amount");
        result.columns.add("Reason");
        result.columns.add("Action");

        String sql = "SELECT r.return_number, s.invoice_number, r.return_date, u.username, " +
                     "r.total_refund_amount, r.return_reason, r.action_taken " +
                     "FROM returns r " +
                     "JOIN sales s ON r.sale_id = s.sale_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.return_date >= ? AND r.return_date <= ? " +
                     "ORDER BY r.return_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.add(rs.getString("return_number"));
                    row.add(rs.getString("invoice_number"));
                    row.add(rs.getTimestamp("return_date"));
                    row.add(rs.getString("username"));
                    row.add(rs.getDouble("total_refund_amount"));
                    row.add(rs.getString("return_reason"));
                    row.add(rs.getString("action_taken"));
                    result.rows.add(row);
                }
            }
        }
        return result;
    }
}

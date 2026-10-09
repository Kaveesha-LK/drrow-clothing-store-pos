package com.drrow.pos.service;

import com.drrow.pos.config.AppConfig;
import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.*;
import com.drrow.pos.model.*;
import com.drrow.pos.util.InvoiceNumberGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class SalesService {

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final SettingDAO settingDAO = new SettingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    // In-memory held carts for quick park/resume
    private static final Map<String, List<CartItem>> heldCarts = new HashMap<>();

    public ProductVariant scanBarcode(String rawBarcode) throws SQLException {
        if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
            return null;
        }
        String clean = rawBarcode.trim();
        ProductVariant variant = variantDAO.findByBarcode(clean);
        if (variant == null) {
            // Try SKU lookup as fallback
            variant = variantDAO.findBySku(clean);
        }
        return variant;
    }

    public String generateNextInvoiceNumber() throws SQLException {
        String prefix = settingDAO.getSetting("invoice.prefix", AppConfig.getInvoicePrefix());
        String format = settingDAO.getSetting("invoice.format", "PREFIX-YYYYMMDD-SEQ");
        try (Connection conn = DatabaseConnection.getConnection()) {
            return InvoiceNumberGenerator.generateInvoiceNumber(conn, prefix, format);
        }
    }

    public Sale completeSale(List<CartItem> cartItems, double discountAmount, double taxPercentage,
                             String paymentMethod, double paidAmount, Integer customerId, String notes) throws Exception {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout.");
        }

        double subtotal = 0.00;
        for (CartItem ci : cartItems) {
            if (ci.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero: " + ci.getProductName());
            }
            subtotal += (ci.getUnitPrice() * ci.getQuantity());
        }

        double taxAmount = (subtotal - discountAmount) * (taxPercentage / 100.0);
        if (taxAmount < 0) taxAmount = 0.00;

        double grandTotal = (subtotal - discountAmount) + taxAmount;
        grandTotal = Math.max(0.00, Math.round(grandTotal * 100.0) / 100.0);

        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            if (paidAmount < grandTotal) {
                throw new IllegalArgumentException(String.format("Insufficient payment. Received: Rs. %.2f, Required: Rs. %.2f", paidAmount, grandTotal));
            }
        } else {
            // For card/bank transfer, paid amount equals total
            if (paidAmount <= 0) {
                paidAmount = grandTotal;
            }
        }

        double changeAmount = Math.max(0.00, paidAmount - grandTotal);

        Sale sale = new Sale();
        sale.setInvoiceNumber(generateNextInvoiceNumber());
        int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
        sale.setUserId(userId);
        sale.setCashierName(SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "Cashier");
        sale.setCustomerId(customerId);
        if (customerId != null && customerId > 0) {
            Customer cust = customerDAO.findById(customerId);
            if (cust != null) sale.setCustomerName(cust.getName());
        }
        sale.setSaleDate(new Date());
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discountAmount);
        sale.setDiscountPercentage(subtotal > 0 ? (discountAmount / subtotal) * 100.0 : 0.0);
        sale.setTaxAmount(taxAmount);
        sale.setGrandTotal(grandTotal);
        sale.setPaidAmount(paidAmount);
        sale.setChangeAmount(changeAmount);
        sale.setPaymentStatus("COMPLETED");
        sale.setNotes(notes);

        List<Payment> payments = new ArrayList<>();
        payments.add(new Payment(paymentMethod, paidAmount, null, notes));

        boolean allowNegative = "true".equalsIgnoreCase(settingDAO.getSetting("pos.allow_negative_stock", "false"));

        // Process atomic database transaction
        saleDAO.processSaleTransaction(sale, cartItems, payments, userId, allowNegative);

        // Populate items in returned sale object for printing
        List<SaleItem> items = new ArrayList<>();
        for (CartItem ci : cartItems) {
            items.add(new SaleItem(
                ci.getVariantId(),
                ci.getProductName(),
                ci.getSku(),
                ci.getBarcode(),
                ci.getSizeName(),
                ci.getColourName(),
                ci.getQuantity(),
                ci.getUnitPrice(),
                ci.getDiscountAmount(),
                ci.getLineTotal()
            ));
        }
        sale.setItems(items);
        sale.setPayments(payments);

        return sale;
    }

    public Sale getSaleByInvoiceNumber(String invoiceNumber) throws SQLException {
        return saleDAO.findByInvoiceNumber(invoiceNumber);
    }

    public List<Sale> searchSales(String keyword, Date from, Date to, Integer cashierId) throws SQLException {
        return saleDAO.searchSales(keyword, from, to, cashierId);
    }

    public DashboardStats getDashboardStats() throws SQLException {
        return saleDAO.getDashboardStats();
    }

    public void holdSale(String holdKey, List<CartItem> items) {
        if (items != null && !items.isEmpty()) {
            heldCarts.put(holdKey, new ArrayList<>(items));
        }
    }

    public List<CartItem> resumeSale(String holdKey) {
        return heldCarts.remove(holdKey);
    }

    public Map<String, List<CartItem>> getHeldSales() {
        return heldCarts;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public Customer getCustomerById(int id) throws SQLException {
        return customerDAO.findById(id);
    }

    public boolean saveCustomer(Customer c) throws SQLException {
        return c.getCustomerId() == 0 ? customerDAO.create(c) : customerDAO.update(c);
    }
}

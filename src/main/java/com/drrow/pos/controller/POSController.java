package com.drrow.pos.controller;

import com.drrow.pos.model.CartItem;
import com.drrow.pos.model.ProductVariant;
import com.drrow.pos.model.Sale;
import com.drrow.pos.service.SalesService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class POSController {

    public interface CartChangeListener {
        void onCartChanged();
    }

    private final SalesService salesService = new SalesService();
    private final List<CartItem> cartItems = new ArrayList<>();
    private final List<CartChangeListener> listeners = new ArrayList<>();

    private double discountAmount = 0.00;
    private double taxPercentage = 0.00;
    private Integer selectedCustomerId = null;
    private String customerDisplayName = "Walk-In Customer";

    public void addCartChangeListener(CartChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (CartChangeListener listener : listeners) {
            listener.onCartChanged();
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    /**
     * Handles USB HID barcode scanner input.
     * Searches database by barcode or SKU, adds to cart if found, or throws exception.
     */
    public ProductVariant handleBarcodeScan(String rawBarcode) throws Exception {
        if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
            return null;
        }

        ProductVariant variant = salesService.scanBarcode(rawBarcode.trim());
        if (variant == null) {
            throw new IllegalArgumentException("Product not found for barcode: " + rawBarcode.trim());
        }

        addVariantToCart(variant);
        return variant;
    }

    public void addVariantToCart(ProductVariant variant) {
        // Check if already in cart
        for (CartItem item : cartItems) {
            if (item.getVariantId() == variant.getVariantId()) {
                item.setQuantity(item.getQuantity() + 1);
                notifyListeners();
                return;
            }
        }

        // Add as new item
        CartItem newItem = new CartItem(variant);
        newItem.setQuantity(1);
        cartItems.add(newItem);
        notifyListeners();
    }

    public void updateQuantity(int variantId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(variantId);
            return;
        }
        for (CartItem item : cartItems) {
            if (item.getVariantId() == variantId) {
                item.setQuantity(newQuantity);
                notifyListeners();
                return;
            }
        }
    }

    public void removeItem(int variantId) {
        cartItems.removeIf(item -> item.getVariantId() == variantId);
        notifyListeners();
    }

    public void clearCart() {
        cartItems.clear();
        discountAmount = 0.00;
        selectedCustomerId = null;
        customerDisplayName = "Walk-In Customer";
        notifyListeners();
    }

    public void applyDiscount(double amount) {
        this.discountAmount = Math.max(0.00, amount);
        notifyListeners();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setTaxPercentage(double taxPercentage) {
        this.taxPercentage = Math.max(0.00, taxPercentage);
        notifyListeners();
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public void setCustomer(Integer customerId, String displayName) {
        this.selectedCustomerId = customerId;
        this.customerDisplayName = displayName != null ? displayName : "Walk-In Customer";
        notifyListeners();
    }

    public Integer getSelectedCustomerId() {
        return selectedCustomerId;
    }

    public String getCustomerDisplayName() {
        return customerDisplayName;
    }

    public double calculateSubtotal() {
        double subtotal = 0.00;
        for (CartItem item : cartItems) {
            subtotal += (item.getUnitPrice() * item.getQuantity());
        }
        return subtotal;
    }

    public double calculateTax() {
        double taxable = calculateSubtotal() - discountAmount;
        if (taxable <= 0) return 0.00;
        return taxable * (taxPercentage / 100.0);
    }

    public double calculateGrandTotal() {
        double total = (calculateSubtotal() - discountAmount) + calculateTax();
        return Math.max(0.00, Math.round(total * 100.0) / 100.0);
    }

    public double calculateChange(double paidAmount) {
        return Math.max(0.00, paidAmount - calculateGrandTotal());
    }

    public Sale completeSale(String paymentMethod, double paidAmount, String notes) throws Exception {
        Sale completed = salesService.completeSale(
            cartItems,
            discountAmount,
            taxPercentage,
            paymentMethod,
            paidAmount,
            selectedCustomerId,
            notes
        );
        clearCart();
        return completed;
    }

    public void holdCart(String key) {
        salesService.holdSale(key, cartItems);
        clearCart();
    }

    public boolean resumeCart(String key) {
        List<CartItem> resumed = salesService.resumeSale(key);
        if (resumed != null && !resumed.isEmpty()) {
            cartItems.clear();
            cartItems.addAll(resumed);
            notifyListeners();
            return true;
        }
        return false;
    }
}

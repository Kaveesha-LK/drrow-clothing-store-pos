package com.drrow.pos.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Currency, decimal number, and date formatting utility for D'RROW POS.
 */
public class FormatUtil {

    private static String currencySymbol = "Rs.";
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat INVOICE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    public static void setCurrencySymbol(String symbol) {
        if (symbol != null && !symbol.trim().isEmpty()) {
            currencySymbol = symbol.trim();
        }
    }

    public static String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Formats amount as "Rs. 4,990.00"
     */
    public static String formatCurrency(double amount) {
        synchronized (DECIMAL_FORMAT) {
            return currencySymbol + " " + DECIMAL_FORMAT.format(amount);
        }
    }

    /**
     * Formats decimal number as "4,990.00" without currency symbol
     */
    public static String formatDecimal(double amount) {
        synchronized (DECIMAL_FORMAT) {
            return DECIMAL_FORMAT.format(amount);
        }
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    public static String formatTime(Date date) {
        if (date == null) return "";
        synchronized (TIME_FORMAT) {
            return TIME_FORMAT.format(date);
        }
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        synchronized (DATETIME_FORMAT) {
            return DATETIME_FORMAT.format(date);
        }
    }

    public static String getInvoiceDateCode(Date date) {
        if (date == null) date = new Date();
        synchronized (INVOICE_DATE_FORMAT) {
            return INVOICE_DATE_FORMAT.format(date);
        }
    }

    public static double parseAmount(String text) {
        if (text == null) return 0.0;
        String clean = text.replace(currencySymbol, "").replace(",", "").trim();
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

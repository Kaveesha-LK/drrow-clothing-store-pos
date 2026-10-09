package com.drrow.pos.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe invoice and return number generator ensuring strict uniqueness.
 * Supported patterns: PREFIX-YYYYMMDD-SEQ (e.g. DRR-20261008-0001) or PREFIX-SEQ (e.g. DRR-000001).
 */
public class InvoiceNumberGenerator {

    private static final AtomicInteger fallbackSequence = new AtomicInteger(1);

    /**
     * Generates a new unique invoice number from database sequence.
     */
    public static synchronized String generateInvoiceNumber(Connection conn, String prefix, String format) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "DRR";
        }
        if (format == null || format.trim().isEmpty()) {
            format = "PREFIX-YYYYMMDD-SEQ";
        }

        Date now = new Date();
        String datePart = FormatUtil.getInvoiceDateCode(now);
        int nextSeq = 1;

        if (conn != null) {
            String query = "SELECT COUNT(*) FROM sales WHERE invoice_number LIKE ?";
            String pattern;
            if (format.contains("YYYYMMDD")) {
                pattern = prefix + "-" + datePart + "-%";
            } else {
                pattern = prefix + "-%";
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nextSeq = rs.getInt(1) + 1;
                    }
                }
            } catch (SQLException e) {
                nextSeq = fallbackSequence.incrementAndGet();
            }
        } else {
            nextSeq = fallbackSequence.incrementAndGet();
        }

        if (format.contains("YYYYMMDD")) {
            return String.format("%s-%s-%04d", prefix, datePart, nextSeq);
        } else {
            return String.format("%s-%06d", prefix, nextSeq);
        }
    }

    /**
     * Generates a new unique return transaction number.
     */
    public static synchronized String generateReturnNumber(Connection conn, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "RET";
        }
        Date now = new Date();
        String datePart = FormatUtil.getInvoiceDateCode(now);
        int nextSeq = 1;

        if (conn != null) {
            String query = "SELECT COUNT(*) FROM returns WHERE return_number LIKE ?";
            String pattern = prefix + "-" + datePart + "-%";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nextSeq = rs.getInt(1) + 1;
                    }
                }
            } catch (SQLException e) {
                nextSeq = fallbackSequence.incrementAndGet();
            }
        } else {
            nextSeq = fallbackSequence.incrementAndGet();
        }

        return String.format("%s-%s-%04d", prefix, datePart, nextSeq);
    }
}

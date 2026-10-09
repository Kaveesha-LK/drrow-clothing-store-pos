package com.drrow.pos.reports;

import com.drrow.pos.model.Sale;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * JasperReports integration bridge for compiling and generating PDF invoices and reports.
 * Uses dynamic reflection so that the application compiles and runs smoothly even if
 * JasperReports jars are optional in a given lightweight deployment.
 */
public class JasperReportGenerator {

    public static boolean isJasperAvailable() {
        try {
            Class.forName("net.sf.jasperreports.engine.JasperReport");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Generates a JasperPrint object or PDF export for an invoice.
     */
    public static boolean generateInvoicePdf(Sale sale, Connection conn, String outputPdfPath) {
        if (!isJasperAvailable()) {
            System.out.println("JasperReports engine not detected on classpath. Using native D'RROW high-fidelity PDF/Print engine.");
            return false;
        }

        try {
            InputStream reportStream = JasperReportGenerator.class.getResourceAsStream("/reports/invoice_receipt.jrxml");
            if (reportStream == null) {
                System.err.println("invoice_receipt.jrxml not found in classpath");
                return false;
            }

            Class<?> compileManager = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
            Method compileReport = compileManager.getMethod("compileReport", InputStream.class);
            Object jasperReport = compileReport.invoke(null, reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("INVOICE_NUMBER", sale.getInvoiceNumber());
            parameters.put("SALE_ID", sale.getSaleId());

            Class<?> fillManager = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
            Method fillReport = fillManager.getMethod("fillReport", jasperReport.getClass(), Map.class, Connection.class);
            Object jasperPrint = fillReport.invoke(null, jasperReport, parameters, conn);

            Class<?> exportManager = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
            Method exportToPdfFile = exportManager.getMethod("exportReportToPdfFile", jasperPrint.getClass(), String.class);
            exportToPdfFile.invoke(null, jasperPrint, outputPdfPath);

            return true;
        } catch (Throwable t) {
            System.err.println("JasperReports export error: " + t.getMessage());
            return false;
        }
    }
}

package com.drrow.pos.service;

import com.drrow.pos.dao.AuditLogDAO;
import com.drrow.pos.dao.ReportDAO;
import com.drrow.pos.dao.StockMovementDAO;
import com.drrow.pos.model.AuditLog;
import com.drrow.pos.model.StockMovement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    public ReportDAO.ReportResult getDailySalesReport(Date day) throws SQLException {
        return reportDAO.getDailySalesReport(day);
    }

    public ReportDAO.ReportResult getMonthlySalesReport(int year, int month) throws SQLException {
        return reportDAO.getMonthlySalesReport(year, month);
    }

    public ReportDAO.ReportResult getSalesByCashier(Date start, Date end) throws SQLException {
        return reportDAO.getSalesByCashier(start, end);
    }

    public ReportDAO.ReportResult getProductSalesReport(Date start, Date end) throws SQLException {
        return reportDAO.getProductSalesReport(start, end);
    }

    public ReportDAO.ReportResult getCategorySalesReport(Date start, Date end) throws SQLException {
        return reportDAO.getCategorySalesReport(start, end);
    }

    public ReportDAO.ReportResult getStockValuationReport() throws SQLException {
        return reportDAO.getStockValuationReport();
    }

    public ReportDAO.ReportResult getProfitEstimateReport(Date start, Date end) throws SQLException {
        return reportDAO.getProfitEstimateReport(start, end);
    }

    public ReportDAO.ReportResult getReturnsReport(Date start, Date end) throws SQLException {
        return reportDAO.getReturnsReport(start, end);
    }

    public List<StockMovement> getStockMovementHistory(Date start, Date end) throws SQLException {
        return movementDAO.getMovementsByDateRange(start, end);
    }

    public List<AuditLog> getAuditActivityReport(String action, Integer userId, Date from, Date to) throws SQLException {
        return auditDAO.searchLogs(action, userId, from, to);
    }

    public File exportResultToCSV(ReportDAO.ReportResult result, String reportTitle, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("# D'RROW Clothing Store - " + reportTitle);
            writer.println("# Generated on: " + new Date());
            writer.println();

            // Headers
            for (int i = 0; i < result.getColumns().size(); i++) {
                writer.print("\"" + result.getColumns().get(i) + "\"");
                if (i < result.getColumns().size() - 1) writer.print(",");
            }
            writer.println();

            // Rows
            for (ReportDAO.ReportRow row : result.getRows()) {
                for (int i = 0; i < row.getValues().size(); i++) {
                    Object val = row.get(i);
                    writer.print("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                    if (i < row.getValues().size() - 1) writer.print(",");
                }
                writer.println();
            }
        }
        return targetFile;
    }
}

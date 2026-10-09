package com.drrow.pos.ui;

import com.drrow.pos.dao.ReportDAO;
import com.drrow.pos.service.ReportService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.components.ModernTable;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    private final JComboBox<String> cmbReportType = new JComboBox<>(new String[]{
        "Daily Sales Report",
        "Monthly Sales Report",
        "Sales by Cashier",
        "Product Sales Report",
        "Category Sales Report",
        "Stock Valuation Report",
        "Profit Estimate Report",
        "Returns & Exchanges Report"
    });

    private final JSpinner spnDate = new JSpinner(new SpinnerDateModel());
    private final DefaultTableModel reportTableModel = new DefaultTableModel();
    private final ModernTable reportTable = new ModernTable(reportTableModel);
    private ReportDAO.ReportResult currentResult = null;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.COLOR_BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Control Bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(UITheme.COLOR_CARD_BG);
        topBar.setBorder(UITheme.createCardBorder());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblType = new JLabel("REPORT TYPE:");
        lblType.setFont(UITheme.FONT_BOLD);
        lblType.setForeground(UITheme.COLOR_GOLD);

        cmbReportType.setFont(UITheme.FONT_REGULAR);
        cmbReportType.setBackground(UITheme.COLOR_INPUT_BG);
        cmbReportType.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnDate, "yyyy-MM-dd");
        spnDate.setEditor(dateEditor);
        spnDate.setFont(UITheme.FONT_REGULAR);

        filterPanel.add(lblType);
        filterPanel.add(cmbReportType);
        filterPanel.add(new JLabel("Target Date:"));
        filterPanel.add(spnDate);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton btnRun = new ModernButton("Generate Report", ModernButton.ButtonType.GOLD);
        ModernButton btnExport = new ModernButton("Export CSV", ModernButton.ButtonType.SECONDARY);
        ModernButton btnPrint = new ModernButton("Print Table", ModernButton.ButtonType.SECONDARY);

        btnRun.addActionListener(e -> runReport());
        btnExport.addActionListener(e -> exportCSV());
        btnPrint.addActionListener(e -> printTable());

        btnPanel.add(btnRun);
        btnPanel.add(btnExport);
        btnPanel.add(btnPrint);

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        add(scroll, BorderLayout.CENTER);

        // Initial run
        runReport();
    }

    private void runReport() {
        int idx = cmbReportType.getSelectedIndex();
        Date targetDate = (Date) spnDate.getValue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(targetDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date endOfMonth = cal.getTime();

        try {
            switch (idx) {
                case 0: // Daily Sales
                    currentResult = reportService.getDailySalesReport(targetDate);
                    break;
                case 1: // Monthly Sales
                    currentResult = reportService.getMonthlySalesReport(year, month);
                    break;
                case 2: // Cashier
                    currentResult = reportService.getSalesByCashier(startOfMonth, endOfMonth);
                    break;
                case 3: // Product Sales
                    currentResult = reportService.getProductSalesReport(startOfMonth, endOfMonth);
                    break;
                case 4: // Category Sales
                    currentResult = reportService.getCategorySalesReport(startOfMonth, endOfMonth);
                    break;
                case 5: // Stock Valuation
                    currentResult = reportService.getStockValuationReport();
                    break;
                case 6: // Profit Estimate
                    currentResult = reportService.getProfitEstimateReport(startOfMonth, endOfMonth);
                    break;
                case 7: // Returns Report
                    currentResult = reportService.getReturnsReport(startOfMonth, endOfMonth);
                    break;
                default:
                    currentResult = reportService.getDailySalesReport(targetDate);
                    break;
            }
            displayResult(currentResult);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResult(ReportDAO.ReportResult result) {
        reportTableModel.setRowCount(0);
        reportTableModel.setColumnCount(0);

        if (result != null) {
            for (String col : result.getColumns()) {
                reportTableModel.addColumn(col);
            }
            for (ReportDAO.ReportRow row : result.getRows()) {
                reportTableModel.addRow(row.getValues().toArray());
            }
        }
    }

    private void exportCSV() {
        if (currentResult == null || currentResult.getRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "Empty Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String reportName = ((String) cmbReportType.getSelectedItem()).replaceAll("\\s+", "_").toLowerCase() + ".csv";
        chooser.setSelectedFile(new File(reportName));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                reportService.exportResultToCSV(currentResult, (String) cmbReportType.getSelectedItem(), target);
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + target.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printTable() {
        try {
            boolean complete = reportTable.print(JTable.PrintMode.FIT_WIDTH,
                new java.text.MessageFormat("D’RROW Clothing Store - " + cmbReportType.getSelectedItem()),
                new java.text.MessageFormat("Page - {0}"));
            if (complete) {
                JOptionPane.showMessageDialog(this, "Print complete.", "Printed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

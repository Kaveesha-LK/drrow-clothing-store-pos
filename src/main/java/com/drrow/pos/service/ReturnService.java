package com.drrow.pos.service;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.ReturnDAO;
import com.drrow.pos.dao.SaleDAO;
import com.drrow.pos.dao.SettingDAO;
import com.drrow.pos.model.*;
import com.drrow.pos.util.InvoiceNumberGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReturnService {

    private final ReturnDAO returnDAO = new ReturnDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final SettingDAO settingDAO = new SettingDAO();

    public Sale findSaleForReturn(String invoiceNumber) throws SQLException {
        return saleDAO.findByInvoiceNumber(invoiceNumber);
    }

    public String generateNextReturnNumber() throws SQLException {
        String prefix = settingDAO.getSetting("return.prefix", "RET");
        try (Connection conn = com.drrow.pos.dao.DatabaseConnection.getConnection()) {
            return InvoiceNumberGenerator.generateReturnNumber(conn, prefix);
        }
    }

    public boolean processReturn(int saleId, String invoiceNumber, List<ReturnItem> items,
                                 String reason, String actionTaken) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No items selected for return/exchange.");
        }

        double totalRefund = 0.00;
        for (ReturnItem ri : items) {
            if (ri.getQuantity() <= 0) {
                throw new IllegalArgumentException("Return quantity must be greater than zero for " + ri.getProductName());
            }
            totalRefund += ri.getRefundAmount();
        }

        Return ret = new Return();
        ret.setReturnNumber(generateNextReturnNumber());
        ret.setSaleId(saleId);
        ret.setInvoiceNumber(invoiceNumber);
        int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
        ret.setUserId(userId);
        ret.setCashierName(SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "Cashier");
        ret.setReturnDate(new Date());
        ret.setTotalRefundAmount(totalRefund);
        ret.setReturnReason(reason);
        ret.setActionTaken(actionTaken != null ? actionTaken : "REFUND");

        return returnDAO.processReturnTransaction(ret, items, userId);
    }

    public List<Return> searchReturns(String keyword, Date from, Date to) throws SQLException {
        return returnDAO.searchReturns(keyword, from, to);
    }

    public List<Return> getReturnsForSale(int saleId) throws SQLException {
        return returnDAO.getReturnsForSale(saleId);
    }
}

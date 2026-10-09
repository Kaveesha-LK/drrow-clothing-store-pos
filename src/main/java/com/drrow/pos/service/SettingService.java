package com.drrow.pos.service;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.AuditLogDAO;
import com.drrow.pos.dao.SettingDAO;
import com.drrow.pos.model.AuditLog;
import com.drrow.pos.util.FormatUtil;

import java.sql.SQLException;
import java.util.Map;

public class SettingService {

    private final SettingDAO settingDAO = new SettingDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    public Map<String, String> getAllSettings() throws SQLException {
        Map<String, String> settings = settingDAO.getAllSettings();
        // Update FormatUtil currency if set
        if (settings.containsKey("store.currency")) {
            FormatUtil.setCurrencySymbol(settings.get("store.currency"));
        }
        return settings;
    }

    public String getSetting(String key, String defaultValue) {
        return settingDAO.getSetting(key, defaultValue);
    }

    public boolean updateSetting(String key, String value, String group, String description) throws SQLException {
        boolean ok = settingDAO.saveOrUpdate(key, value, group, description);
        if (ok) {
            if ("store.currency".equals(key)) {
                FormatUtil.setCurrencySymbol(value);
            }
            int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
            String username = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "system";
            auditDAO.log(new AuditLog(userId, username, "SETTINGS_CHANGE", "SETTING", key, "Updated setting " + key + " = " + value), null);
        }
        return ok;
    }
}

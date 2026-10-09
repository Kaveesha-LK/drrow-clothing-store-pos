package com.drrow.pos.service;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.AuditLogDAO;
import com.drrow.pos.dao.UserDAO;
import com.drrow.pos.model.AuditLog;
import com.drrow.pos.model.User;
import com.drrow.pos.util.SecurityUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    public User login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User user = userDAO.authenticate(username.trim(), password);
        if (user == null) {
            auditDAO.log(new AuditLog(null, username, "LOGIN_FAILED", "USER", null, "Failed login attempt for user: " + username), null);
            throw new IllegalArgumentException("Invalid username or password, or account is disabled.");
        }

        SessionContext.setCurrentUser(user);
        auditDAO.log(new AuditLog(user.getUserId(), user.getUsername(), "LOGIN_SUCCESS", "USER", String.valueOf(user.getUserId()), "Successful login as " + user.getRoleName()), null);

        return user;
    }

    public void logout() {
        User user = SessionContext.getCurrentUser();
        if (user != null) {
            auditDAO.log(new AuditLog(user.getUserId(), user.getUsername(), "LOGOUT", "USER", String.valueOf(user.getUserId()), "User logged out"), null);
            SessionContext.clear();
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!SecurityUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        boolean updated = userDAO.updatePassword(userId, newPassword);
        if (updated) {
            auditDAO.log(new AuditLog(userId, user.getUsername(), "PASSWORD_CHANGE", "USER", String.valueOf(userId), "User changed password"), null);
            if (SessionContext.getCurrentUser() != null && SessionContext.getCurrentUser().getUserId() == userId) {
                SessionContext.getCurrentUser().setRequirePasswordChange(false);
            }
        }
        return updated;
    }
}

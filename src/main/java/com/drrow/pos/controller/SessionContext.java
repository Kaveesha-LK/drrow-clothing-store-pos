package com.drrow.pos.controller;

import com.drrow.pos.model.User;

/**
 * Thread-safe global session context tracking the currently logged-in user.
 */
public class SessionContext {

    private static User currentUser = null;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean hasPermission(String permissionCode) {
        if (currentUser == null) return false;
        return currentUser.hasPermission(permissionCode);
    }

    public static synchronized String getRoleName() {
        if (currentUser == null) return "";
        return currentUser.getRoleName() != null ? currentUser.getRoleName() : "";
    }

    public static synchronized void clear() {
        currentUser = null;
    }
}

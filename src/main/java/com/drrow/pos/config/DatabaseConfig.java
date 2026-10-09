package com.drrow.pos.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central database configuration loader reading from db.properties or external overrides.
 */
public class DatabaseConfig {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;

        // 1. Try external db.properties in working directory first (allows easy user reconfiguration)
        File externalFile = new File("db.properties");
        if (externalFile.exists()) {
            try (InputStream in = new FileInputStream(externalFile)) {
                props.load(in);
                loaded = true;
                return;
            } catch (Exception ignored) {}
        }

        // 2. Try classpath resource config/db.properties
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/config/db.properties")) {
            if (in != null) {
                props.load(in);
                loaded = true;
                return;
            }
        } catch (Exception ignored) {}

        // 3. Fallback defaults
        props.setProperty("db.type", "mysql");
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db.name", "drrow_pos");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "root123");
        loaded = true;
    }

    public static String getProperty(String key, String defaultValue) {
        load();
        return props.getProperty(key, defaultValue);
    }

    public static String getDbType() {
        return getProperty("db.type", "mysql").trim().toLowerCase();
    }

    public static String getDbHost() {
        return getProperty("db.host", "localhost").trim();
    }

    public static int getDbPort() {
        try {
            return Integer.parseInt(getProperty("db.port", "3306").trim());
        } catch (NumberFormatException e) {
            return 3306;
        }
    }

    public static String getDbName() {
        return getProperty("db.name", "drrow_pos").trim();
    }

    public static String getDbUser() {
        return getProperty("db.user", "root").trim();
    }

    public static String getDbPassword() {
        return getProperty("db.password", "root123");
    }

    public static String buildJdbcUrl() {
        String type = getDbType();
        if ("mariadb".equals(type)) {
            return String.format("jdbc:mariadb://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=%s&autoReconnect=%s",
                    getDbHost(), getDbPort(), getDbName(),
                    getProperty("db.useSSL", "false"),
                    getProperty("db.allowPublicKeyRetrieval", "true"),
                    getProperty("db.autoReconnect", "true"));
        } else if ("embedded".equals(type) || "hsqldb".equals(type)) {
            return "jdbc:hsqldb:mem:drrow_pos;sql.syntax_mys=true";
        } else {
            // Default MySQL 8.x
            return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=%s&autoReconnect=%s&characterEncoding=%s",
                    getDbHost(), getDbPort(), getDbName(),
                    getProperty("db.useSSL", "false"),
                    getProperty("db.serverTimezone", "UTC"),
                    getProperty("db.allowPublicKeyRetrieval", "true"),
                    getProperty("db.autoReconnect", "true"),
                    getProperty("db.characterEncoding", "UTF-8"));
        }
    }
}

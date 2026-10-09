package com.drrow.pos.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Global application metadata and configuration loader.
 */
public class AppConfig {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;

        File externalFile = new File("app.properties");
        if (externalFile.exists()) {
            try (InputStream in = new FileInputStream(externalFile)) {
                props.load(in);
                loaded = true;
                return;
            } catch (Exception ignored) {}
        }

        try (InputStream in = AppConfig.class.getResourceAsStream("/config/app.properties")) {
            if (in != null) {
                props.load(in);
                loaded = true;
                return;
            }
        } catch (Exception ignored) {}

        props.setProperty("app.name", "D’RROW Clothing Store POS");
        props.setProperty("app.version", "1.0.0");
        props.setProperty("app.currency", "Rs.");
        props.setProperty("app.barcode.prefix", "DRTS");
        props.setProperty("app.invoice.prefix", "DRR");
        loaded = true;
    }

    public static String getProperty(String key, String defaultValue) {
        load();
        return props.getProperty(key, defaultValue);
    }

    public static String getAppName() {
        return getProperty("app.name", "D’RROW Clothing Store POS");
    }

    public static String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }

    public static String getCurrency() {
        return getProperty("app.currency", "Rs.");
    }

    public static String getBarcodePrefix() {
        return getProperty("app.barcode.prefix", "DRTS");
    }

    public static String getInvoicePrefix() {
        return getProperty("app.invoice.prefix", "DRR");
    }
}

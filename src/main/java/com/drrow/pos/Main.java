package com.drrow.pos;

import com.drrow.pos.dao.DatabaseConnection;
import com.drrow.pos.ui.LoginFrame;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Main application entry point for D’RROW Clothing Store POS.
 * Initializes theme, verifies database connection, checks first-run schema, and launches the login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Apply modern retail UI theme
        UITheme.applyGlobalTheme();

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database
                DatabaseConnection.initialize();

                // If embedded fallback mode or empty database, automatically apply schema and seed scripts
                if (DatabaseConnection.isEmbeddedMode() || isDatabaseEmpty()) {
                    System.out.println("Initializing D’RROW database schema and default master data...");
                    try (InputStream schemaIn = Main.class.getResourceAsStream("/database/schema.sql")) {
                        if (schemaIn != null) DatabaseConnection.executeSqlScript(schemaIn);
                    }
                    try (InputStream seedIn = Main.class.getResourceAsStream("/database/seed.sql")) {
                        if (seedIn != null) DatabaseConnection.executeSqlScript(seedIn);
                    }
                    try (InputStream sampleIn = Main.class.getResourceAsStream("/database/sample_data.sql")) {
                        if (sampleIn != null) DatabaseConnection.executeSqlScript(sampleIn);
                    }
                }

                // Launch Login Frame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

            } catch (Exception ex) {
                System.err.println("Startup failure: " + ex.getMessage());
                ex.printStackTrace();

                String message = "Unable to connect to the database.\n\n" +
                        "Technical details: " + ex.getMessage() + "\n\n" +
                        "Please check the following:\n" +
                        "1. Ensure MySQL / MariaDB service is started and running.\n" +
                        "2. Ensure the 'drrow_pos' database is created using database/schema.sql.\n" +
                        "3. Check your database credentials in 'config/db.properties'.";

                JOptionPane.showMessageDialog(null, message, "D’RROW POS - Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static boolean isDatabaseEmpty() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}

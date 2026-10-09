package com.drrow.pos.dao;

import com.drrow.pos.config.DatabaseConfig;
import com.drrow.pos.util.SecurityUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * Central Database Connection Manager for D'RROW POS.
 * Supports MySQL 8.x, MariaDB, and standalone fallback mode with transaction control.
 */
public class DatabaseConnection {

    private static String activeUrl = null;
    private static String activeUser = null;
    private static String activePassword = null;
    private static boolean isInitialized = false;
    private static boolean isEmbedded = false;

    static {
        registerDrivers();
    }

    private static void registerDrivers() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException ignored) {}
    }

    public static synchronized void initialize() throws SQLException {
        if (isInitialized) return;

        activeUrl = DatabaseConfig.buildJdbcUrl();
        activeUser = DatabaseConfig.getDbUser();
        activePassword = DatabaseConfig.getDbPassword();

        // Test primary connection
        try {
            Connection testConn = DriverManager.getConnection(activeUrl, activeUser, activePassword);
            testConn.close();
            isInitialized = true;
            isEmbedded = "embedded".equalsIgnoreCase(DatabaseConfig.getDbType()) || "hsqldb".equalsIgnoreCase(DatabaseConfig.getDbType());
            return;
        } catch (SQLException ex) {
            // If primary connection fails, check if HSQLDB is present for offline/testing mode
            try {
                Class.forName("org.hsqldb.jdbcDriver");
                activeUrl = "jdbc:hsqldb:mem:drrow_pos;sql.syntax_mys=true";
                activeUser = "sa";
                activePassword = "";
                isEmbedded = true;
                isInitialized = true;
                initEmbeddedSchema();
                return;
            } catch (Throwable t) {
                throw new SQLException("Database connection failed for URL: " + activeUrl +
                        "\nError: " + ex.getMessage() +
                        "\nPlease ensure MySQL/MariaDB server is running and database 'drrow_pos' exists.", ex);
            }
        }
    }

    public static synchronized void setEmbeddedMode(String memDbName) throws SQLException {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("HSQLDB driver not found for embedded mode", e);
        }
        activeUrl = "jdbc:hsqldb:mem:" + (memDbName != null ? memDbName : "drrow_pos") + ";sql.syntax_mys=true";
        activeUser = "sa";
        activePassword = "";
        isEmbedded = true;
        isInitialized = true;
        initEmbeddedSchema();
    }

    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initialize();
        }
        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }

    public static boolean isEmbeddedMode() {
        return isEmbedded;
    }

    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public static void commit(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Commit failed: " + e.getMessage());
            }
        }
    }

    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Rollback failed: " + e.getMessage());
            }
        }
    }

    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Universally prepares an INSERT statement that can retrieve auto-generated IDs across MySQL, MariaDB, and HSQLDB.
     */
    public static PreparedStatement prepareInsert(Connection conn, String sql) throws SQLException {
        try {
            return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException ex) {
            return conn.prepareStatement(sql);
        }
    }

    /**
     * Retrieves the auto-generated primary key ID after an INSERT statement execution across all databases.
     */
    public static int getGeneratedKey(PreparedStatement ps, Connection conn) {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}

        // Fallback for HSQLDB / SQLite
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("CALL IDENTITY()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}

        return 0;
    }

    /**
     * Executes SQL scripts (e.g. schema.sql, seed.sql) against current connection.
     */
    public static void executeSqlScript(InputStream inputStream) throws SQLException {
        if (inputStream == null) return;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1).trim();
                    }
                    if (isEmbedded) {
                        sql = sql.replace("`", "");
                    }
                    if (!sql.isEmpty() && !sql.toLowerCase().startsWith("use ") &&
                        !sql.toLowerCase().startsWith("create database") &&
                        !sql.toLowerCase().startsWith("set foreign_key_checks") &&
                        !sql.toLowerCase().startsWith("drop table")) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            // If table already exists or minor dialect variation, continue
                        }
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed executing SQL script: " + e.getMessage(), e);
        }
    }

    private static void initEmbeddedSchema() {
        try (Connection conn = DriverManager.getConnection(activeUrl, activeUser, activePassword);
             Statement stmt = conn.createStatement()) {

            // HSQLDB 1.8 Compatible DDL
            String[] ddl = {
                "CREATE TABLE roles (role_id INTEGER IDENTITY PRIMARY KEY, role_name VARCHAR(50) NOT NULL, description VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE permissions (permission_id INTEGER IDENTITY PRIMARY KEY, permission_code VARCHAR(100) NOT NULL, description VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE role_permissions (role_id INT NOT NULL, permission_id INT NOT NULL, PRIMARY KEY (role_id, permission_id))",
                "CREATE TABLE users (user_id INTEGER IDENTITY PRIMARY KEY, username VARCHAR(50) NOT NULL, password_hash VARCHAR(255) NOT NULL, full_name VARCHAR(100) NOT NULL, email VARCHAR(100), phone VARCHAR(30), role_id INT NOT NULL, is_active INT DEFAULT 1, require_password_change INT DEFAULT 0, last_login TIMESTAMP, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE categories (category_id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(100) NOT NULL, description VARCHAR(255), is_active INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE brands (brand_id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(100) NOT NULL, description VARCHAR(255), is_active INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE sizes (size_id INTEGER IDENTITY PRIMARY KEY, size_name VARCHAR(20) NOT NULL, display_order INT DEFAULT 0, is_active INT DEFAULT 1)",
                "CREATE TABLE colours (colour_id INTEGER IDENTITY PRIMARY KEY, colour_name VARCHAR(50) NOT NULL, hex_code VARCHAR(10), is_active INT DEFAULT 1)",
                "CREATE TABLE products (product_id INTEGER IDENTITY PRIMARY KEY, item_code VARCHAR(50) NOT NULL, name VARCHAR(150) NOT NULL, category_id INT NOT NULL, brand_id INT NOT NULL, description VARCHAR(500), purchase_price DECIMAL(12,2) DEFAULT 0.00, selling_price DECIMAL(12,2) DEFAULT 0.00, reorder_level INT DEFAULT 5, is_active INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE product_variants (variant_id INTEGER IDENTITY PRIMARY KEY, product_id INT NOT NULL, sku VARCHAR(60) NOT NULL, barcode VARCHAR(50) NOT NULL, size_id INT NOT NULL, colour_id INT NOT NULL, purchase_price DECIMAL(12,2) DEFAULT 0.00, selling_price DECIMAL(12,2) DEFAULT 0.00, is_active INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE stock (stock_id INTEGER IDENTITY PRIMARY KEY, variant_id INT NOT NULL, current_stock INT DEFAULT 0, available_stock INT DEFAULT 0, reserved_stock INT DEFAULT 0, last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE stock_movements (movement_id INTEGER IDENTITY PRIMARY KEY, variant_id INT NOT NULL, movement_type VARCHAR(30) NOT NULL, quantity INT NOT NULL, before_quantity INT NOT NULL, after_quantity INT NOT NULL, reference_id VARCHAR(50), reference_type VARCHAR(30), notes VARCHAR(255), user_id INT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE customers (customer_id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(30), email VARCHAR(100), address VARCHAR(255), notes VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE suppliers (supplier_id INTEGER IDENTITY PRIMARY KEY, name VARCHAR(120) NOT NULL, contact_person VARCHAR(100), phone VARCHAR(30), email VARCHAR(100), address VARCHAR(255), is_active INT DEFAULT 1, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE stock_receipts (receipt_id INTEGER IDENTITY PRIMARY KEY, receipt_number VARCHAR(50) NOT NULL, supplier_id INT, user_id INT NOT NULL, receipt_date DATE NOT NULL, total_amount DECIMAL(12,2) DEFAULT 0.00, reference_number VARCHAR(60), notes VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE stock_receipt_items (item_id INTEGER IDENTITY PRIMARY KEY, receipt_id INT NOT NULL, variant_id INT NOT NULL, quantity INT NOT NULL, unit_cost DECIMAL(12,2) DEFAULT 0.00, total_cost DECIMAL(12,2) DEFAULT 0.00)",
                "CREATE TABLE sales (sale_id INTEGER IDENTITY PRIMARY KEY, invoice_number VARCHAR(50) NOT NULL, user_id INT NOT NULL, customer_id INT, sale_date TIMESTAMP NOT NULL, subtotal DECIMAL(12,2) DEFAULT 0.00, discount_amount DECIMAL(12,2) DEFAULT 0.00, discount_percentage DECIMAL(5,2) DEFAULT 0.00, tax_amount DECIMAL(12,2) DEFAULT 0.00, grand_total DECIMAL(12,2) DEFAULT 0.00, paid_amount DECIMAL(12,2) DEFAULT 0.00, change_amount DECIMAL(12,2) DEFAULT 0.00, payment_status VARCHAR(20) DEFAULT 'COMPLETED', notes VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE sale_items (item_id INTEGER IDENTITY PRIMARY KEY, sale_id INT NOT NULL, variant_id INT NOT NULL, quantity INT NOT NULL, unit_price DECIMAL(12,2) DEFAULT 0.00, discount_amount DECIMAL(12,2) DEFAULT 0.00, line_total DECIMAL(12,2) DEFAULT 0.00)",
                "CREATE TABLE payments (payment_id INTEGER IDENTITY PRIMARY KEY, sale_id INT NOT NULL, payment_method VARCHAR(30) NOT NULL, amount DECIMAL(12,2) DEFAULT 0.00, reference_number VARCHAR(60), notes VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE returns (return_id INTEGER IDENTITY PRIMARY KEY, return_number VARCHAR(50) NOT NULL, sale_id INT NOT NULL, user_id INT NOT NULL, return_date TIMESTAMP NOT NULL, total_refund_amount DECIMAL(12,2) DEFAULT 0.00, return_reason VARCHAR(255), action_taken VARCHAR(30) DEFAULT 'REFUND', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE return_items (item_id INTEGER IDENTITY PRIMARY KEY, return_id INT NOT NULL, sale_item_id INT NOT NULL, variant_id INT NOT NULL, quantity INT NOT NULL, unit_price DECIMAL(12,2) DEFAULT 0.00, refund_amount DECIMAL(12,2) DEFAULT 0.00, restock_status VARCHAR(30) DEFAULT 'RESTOCKED')",
                "CREATE TABLE settings (setting_key VARCHAR(60) PRIMARY KEY, setting_value VARCHAR(255) NOT NULL, setting_group VARCHAR(50) DEFAULT 'GENERAL', description VARCHAR(255), updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE audit_logs (log_id INTEGER IDENTITY PRIMARY KEY, user_id INT, username VARCHAR(50), action VARCHAR(50) NOT NULL, entity_type VARCHAR(50), entity_id VARCHAR(50), description VARCHAR(500), ip_address VARCHAR(45), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            };

            for (String sql : ddl) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    if (!ex.getMessage().contains("already exists")) {
                        System.err.println("DDL note: " + ex.getMessage());
                    }
                }
            }

            // Create Unique Indexes
            String[] indexes = {
                "CREATE UNIQUE INDEX idx_role_name ON roles(role_name)",
                "CREATE UNIQUE INDEX idx_perm_code ON permissions(permission_code)",
                "CREATE UNIQUE INDEX idx_user_name ON users(username)",
                "CREATE UNIQUE INDEX idx_cat_name ON categories(name)",
                "CREATE UNIQUE INDEX idx_brand_name ON brands(name)",
                "CREATE UNIQUE INDEX idx_size_name ON sizes(size_name)",
                "CREATE UNIQUE INDEX idx_col_name ON colours(colour_name)",
                "CREATE UNIQUE INDEX idx_prod_code ON products(item_code)",
                "CREATE UNIQUE INDEX idx_pv_sku ON product_variants(sku)",
                "CREATE UNIQUE INDEX idx_pv_bc ON product_variants(barcode)",
                "CREATE UNIQUE INDEX idx_stock_var ON stock(variant_id)",
                "CREATE UNIQUE INDEX idx_sr_num ON stock_receipts(receipt_number)",
                "CREATE UNIQUE INDEX idx_sale_inv ON sales(invoice_number)",
                "CREATE UNIQUE INDEX idx_ret_num ON returns(return_number)"
            };

            for (String idxSql : indexes) {
                try {
                    stmt.execute(idxSql);
                } catch (SQLException ignored) {}
            }

            // Seed initial roles & admin user in embedded mode
            String adminHash = SecurityUtil.hashPassword("Admin@123");
            String[] seedStatements = {
                "INSERT INTO roles (role_id, role_name, description) VALUES (1, 'ADMIN', 'System Administrator')",
                "INSERT INTO roles (role_id, role_name, description) VALUES (2, 'MANAGER', 'Store Manager')",
                "INSERT INTO roles (role_id, role_name, description) VALUES (3, 'CASHIER', 'Lead Cashier')",
                "INSERT INTO roles (role_id, role_name, description) VALUES (4, 'STOCK_STAFF', 'Stock Staff')",
                "INSERT INTO permissions (permission_id, permission_code, description) VALUES (1, 'DASHBOARD_VIEW', 'View dashboard')",
                "INSERT INTO permissions (permission_id, permission_code, description) VALUES (2, 'POS_ACCESS', 'Access POS')",
                "INSERT INTO permissions (permission_id, permission_code, description) VALUES (3, 'SALES_PROCESS', 'Complete sales')",
                "INSERT INTO permissions (permission_id, permission_code, description) VALUES (4, 'PRODUCTS_MANAGE', 'Manage products')",
                "INSERT INTO permissions (permission_id, permission_code, description) VALUES (5, 'INVENTORY_VIEW', 'View inventory')",
                "INSERT INTO users (user_id, username, password_hash, full_name, email, phone, role_id, is_active, require_password_change) VALUES (1, 'admin', '" + adminHash + "', 'System Administrator', 'admin@drrow.com', '+94 77 123 4567', 1, 1, 1)",
                "INSERT INTO sizes (size_id, size_name, display_order, is_active) VALUES (1, 'XS', 10, 1)",
                "INSERT INTO sizes (size_id, size_name, display_order, is_active) VALUES (2, 'S', 20, 1)",
                "INSERT INTO sizes (size_id, size_name, display_order, is_active) VALUES (3, 'M', 30, 1)",
                "INSERT INTO sizes (size_id, size_name, display_order, is_active) VALUES (4, 'L', 40, 1)",
                "INSERT INTO sizes (size_id, size_name, display_order, is_active) VALUES (5, 'XL', 50, 1)",
                "INSERT INTO colours (colour_id, colour_name, hex_code, is_active) VALUES (1, 'Black', '#000000', 1)",
                "INSERT INTO colours (colour_id, colour_name, hex_code, is_active) VALUES (2, 'White', '#FFFFFF', 1)",
                "INSERT INTO colours (colour_id, colour_name, hex_code, is_active) VALUES (3, 'Navy Blue', '#000080', 1)",
                "INSERT INTO categories (category_id, name, description, is_active) VALUES (1, 'T-Shirts', 'Casual T-Shirts', 1)",
                "INSERT INTO categories (category_id, name, description, is_active) VALUES (2, 'Shirts', 'Formal and casual shirts', 1)",
                "INSERT INTO categories (category_id, name, description, is_active) VALUES (3, 'Jeans', 'Denim jeans', 1)",
                "INSERT INTO brands (brand_id, name, description, is_active) VALUES (1, 'D’RROW Signature', 'D’RROW luxury flagship line', 1)",
                "INSERT INTO brands (brand_id, name, description, is_active) VALUES (2, 'D’RROW Urban', 'Streetwear line', 1)",
                "INSERT INTO customers (customer_id, name, phone, email, address, notes) VALUES (1, 'Walk-In Customer', '0000000000', 'walkin@drrow.com', 'In-Store', 'Default guest customer')",
                "INSERT INTO settings (setting_key, setting_value, setting_group, description) VALUES ('store.name', 'D’RROW Clothing Store', 'STORE', 'Official store name')",
                "INSERT INTO settings (setting_key, setting_value, setting_group, description) VALUES ('store.currency', 'Rs.', 'GENERAL', 'Currency symbol')",
                "INSERT INTO settings (setting_key, setting_value, setting_group, description) VALUES ('barcode.prefix', 'DRTS', 'BARCODE', 'Barcode prefix')",
                "INSERT INTO settings (setting_key, setting_value, setting_group, description) VALUES ('invoice.prefix', 'DRR', 'INVOICE', 'Invoice prefix')"
            };

            for (String sql : seedStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ignored) {}
            }

        } catch (Exception e) {
            System.err.println("Note on embedded schema initialization: " + e.getMessage());
        }
    }
}

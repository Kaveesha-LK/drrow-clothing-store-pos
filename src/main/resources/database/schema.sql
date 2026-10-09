-- =====================================================================
-- D’RROW Clothing Store POS and Inventory Management System
-- Normalized Database Schema (MySQL 8.x / MariaDB compatible)
-- Character Set: utf8mb4, Engine: InnoDB
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `drrow_pos` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `drrow_pos`;

-- Disable foreign key checks during creation
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- Table: roles
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `role_id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: permissions
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
    `permission_id` INT AUTO_INCREMENT PRIMARY KEY,
    `permission_code` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: role_permissions
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
    `role_id` INT NOT NULL,
    `permission_id` INT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`permission_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: users
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NULL,
    `phone` VARCHAR(30) NULL,
    `role_id` INT NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `require_password_change` TINYINT(1) NOT NULL DEFAULT 0,
    `last_login` DATETIME NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_users_username` ON `users` (`username`);
CREATE INDEX `idx_users_role` ON `users` (`role_id`);

-- ---------------------------------------------------------------------
-- Table: categories
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
    `category_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: brands
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `brands`;
CREATE TABLE `brands` (
    `brand_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: sizes
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sizes`;
CREATE TABLE `sizes` (
    `size_id` INT AUTO_INCREMENT PRIMARY KEY,
    `size_name` VARCHAR(20) NOT NULL UNIQUE,
    `display_order` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: colours
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `colours`;
CREATE TABLE `colours` (
    `colour_id` INT AUTO_INCREMENT PRIMARY KEY,
    `colour_name` VARCHAR(50) NOT NULL UNIQUE,
    `hex_code` VARCHAR(10) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: products
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
    `product_id` INT AUTO_INCREMENT PRIMARY KEY,
    `item_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `category_id` INT NOT NULL,
    `brand_id` INT NOT NULL,
    `description` TEXT NULL,
    `purchase_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `selling_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `reorder_level` INT NOT NULL DEFAULT 5,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_prod_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`),
    CONSTRAINT `fk_prod_brand` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_prod_item_code` ON `products` (`item_code`);
CREATE INDEX `idx_prod_name` ON `products` (`name`);
CREATE INDEX `idx_prod_category` ON `products` (`category_id`);
CREATE INDEX `idx_prod_brand` ON `products` (`brand_id`);

-- ---------------------------------------------------------------------
-- Table: product_variants
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `product_variants`;
CREATE TABLE `product_variants` (
    `variant_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,
    `sku` VARCHAR(60) NOT NULL UNIQUE,
    `barcode` VARCHAR(50) NOT NULL UNIQUE,
    `size_id` INT NOT NULL,
    `colour_id` INT NOT NULL,
    `purchase_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `selling_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pv_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pv_size` FOREIGN KEY (`size_id`) REFERENCES `sizes` (`size_id`),
    CONSTRAINT `fk_pv_colour` FOREIGN KEY (`colour_id`) REFERENCES `colours` (`colour_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `idx_pv_barcode` ON `product_variants` (`barcode`);
CREATE UNIQUE INDEX `idx_pv_sku` ON `product_variants` (`sku`);
CREATE INDEX `idx_pv_product` ON `product_variants` (`product_id`);

-- ---------------------------------------------------------------------
-- Table: stock
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
    `stock_id` INT AUTO_INCREMENT PRIMARY KEY,
    `variant_id` INT NOT NULL UNIQUE,
    `current_stock` INT NOT NULL DEFAULT 0,
    `available_stock` INT NOT NULL DEFAULT 0,
    `reserved_stock` INT NOT NULL DEFAULT 0,
    `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_stock_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_stock_variant` ON `stock` (`variant_id`);

-- ---------------------------------------------------------------------
-- Table: stock_movements
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `stock_movements`;
CREATE TABLE `stock_movements` (
    `movement_id` INT AUTO_INCREMENT PRIMARY KEY,
    `variant_id` INT NOT NULL,
    `movement_type` VARCHAR(30) NOT NULL, -- RECEIVE, SALE, RETURN, ADJUSTMENT_DAMAGE, ADJUSTMENT_LOST, ADJUSTMENT_CORRECTION, ADJUSTMENT_FOUND, ADJUSTMENT_MANUAL
    `quantity` INT NOT NULL,
    `before_quantity` INT NOT NULL,
    `after_quantity` INT NOT NULL,
    `reference_id` VARCHAR(50) NULL,
    `reference_type` VARCHAR(30) NULL, -- INVOICE, RECEIPT, MANUAL
    `notes` VARCHAR(255) NULL,
    `user_id` INT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sm_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`),
    CONSTRAINT `fk_sm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_sm_variant` ON `stock_movements` (`variant_id`);
CREATE INDEX `idx_sm_created` ON `stock_movements` (`created_at`);

-- ---------------------------------------------------------------------
-- Table: customers
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
    `customer_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NULL,
    `email` VARCHAR(100) NULL,
    `address` TEXT NULL,
    `notes` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_customers_phone` ON `customers` (`phone`);

-- ---------------------------------------------------------------------
-- Table: suppliers
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers` (
    `supplier_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(120) NOT NULL,
    `contact_person` VARCHAR(100) NULL,
    `phone` VARCHAR(30) NULL,
    `email` VARCHAR(100) NULL,
    `address` TEXT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: stock_receipts
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `stock_receipts`;
CREATE TABLE `stock_receipts` (
    `receipt_id` INT AUTO_INCREMENT PRIMARY KEY,
    `receipt_number` VARCHAR(50) NOT NULL UNIQUE,
    `supplier_id` INT NULL,
    `user_id` INT NOT NULL,
    `receipt_date` DATE NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `reference_number` VARCHAR(60) NULL,
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sr_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`),
    CONSTRAINT `fk_sr_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_sr_number` ON `stock_receipts` (`receipt_number`);

-- ---------------------------------------------------------------------
-- Table: stock_receipt_items
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `stock_receipt_items`;
CREATE TABLE `stock_receipt_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `receipt_id` INT NOT NULL,
    `variant_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_cost` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total_cost` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT `fk_sri_receipt` FOREIGN KEY (`receipt_id`) REFERENCES `stock_receipts` (`receipt_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sri_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: sales
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sales`;
CREATE TABLE `sales` (
    `sale_id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_number` VARCHAR(50) NOT NULL UNIQUE,
    `user_id` INT NOT NULL,
    `customer_id` INT NULL,
    `sale_date` DATETIME NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_percentage` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `tax_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `grand_total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `paid_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `change_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, HELD, CANCELLED
    `notes` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sales_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT `fk_sales_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_sales_invoice` ON `sales` (`invoice_number`);
CREATE INDEX `idx_sales_date` ON `sales` (`sale_date`);
CREATE INDEX `idx_sales_user` ON `sales` (`user_id`);

-- ---------------------------------------------------------------------
-- Table: sale_items
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sale_items`;
CREATE TABLE `sale_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `sale_id` INT NOT NULL,
    `variant_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `line_total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT `fk_si_sale` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_si_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_si_sale` ON `sale_items` (`sale_id`);
CREATE INDEX `idx_si_variant` ON `sale_items` (`variant_id`);

-- ---------------------------------------------------------------------
-- Table: payments
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `sale_id` INT NOT NULL,
    `payment_method` VARCHAR(30) NOT NULL, -- CASH, CARD, BANK_TRANSFER, OTHER
    `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `reference_number` VARCHAR(60) NULL,
    `notes` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pay_sale` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_pay_sale` ON `payments` (`sale_id`);

-- ---------------------------------------------------------------------
-- Table: returns
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `returns`;
CREATE TABLE `returns` (
    `return_id` INT AUTO_INCREMENT PRIMARY KEY,
    `return_number` VARCHAR(50) NOT NULL UNIQUE,
    `sale_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `return_date` DATETIME NOT NULL,
    `total_refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `return_reason` VARCHAR(255) NULL,
    `action_taken` VARCHAR(30) NOT NULL DEFAULT 'REFUND', -- REFUND, EXCHANGE
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ret_sale` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`),
    CONSTRAINT `fk_ret_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_returns_number` ON `returns` (`return_number`);
CREATE INDEX `idx_returns_sale` ON `returns` (`sale_id`);

-- ---------------------------------------------------------------------
-- Table: return_items
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `return_items`;
CREATE TABLE `return_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `return_id` INT NOT NULL,
    `sale_item_id` INT NOT NULL,
    `variant_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `restock_status` VARCHAR(30) NOT NULL DEFAULT 'RESTOCKED', -- RESTOCKED, DAMAGED_DISCARD
    CONSTRAINT `fk_ri_return` FOREIGN KEY (`return_id`) REFERENCES `returns` (`return_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ri_sale_item` FOREIGN KEY (`sale_item_id`) REFERENCES `sale_items` (`item_id`),
    CONSTRAINT `fk_ri_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: settings
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `settings`;
CREATE TABLE `settings` (
    `setting_key` VARCHAR(60) PRIMARY KEY,
    `setting_value` TEXT NOT NULL,
    `setting_group` VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    `description` VARCHAR(255) NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: audit_logs
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `username` VARCHAR(50) NULL,
    `action` VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, PRODUCT_CREATE, SALE_COMPLETE, etc.
    `entity_type` VARCHAR(50) NULL,
    `entity_id` VARCHAR(50) NULL,
    `description` TEXT NOT NULL,
    `ip_address` VARCHAR(45) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_audit_user` ON `audit_logs` (`user_id`);
CREATE INDEX `idx_audit_action` ON `audit_logs` (`action`);
CREATE INDEX `idx_audit_created` ON `audit_logs` (`created_at`);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

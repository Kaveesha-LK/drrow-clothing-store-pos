-- =====================================================================
-- D’RROW Clothing Store POS and Inventory Management System
-- Seed Data Script (Roles, Permissions, Users, Core Master Data, Settings)
-- =====================================================================

USE `drrow_pos`;

-- ---------------------------------------------------------------------
-- 1. Roles
-- ---------------------------------------------------------------------
INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES
(1, 'ADMIN', 'System Administrator with full access to all system features'),
(2, 'MANAGER', 'Store Manager with access to products, inventory, sales, returns, and reports'),
(3, 'CASHIER', 'Cashier with access to POS billing, sales processing, and receipt reprinting'),
(4, 'STOCK_STAFF', 'Stock Staff with access to inventory, receiving, adjustments, and barcode tags');

-- ---------------------------------------------------------------------
-- 2. Permissions
-- ---------------------------------------------------------------------
INSERT INTO `permissions` (`permission_id`, `permission_code`, `description`) VALUES
(1, 'DASHBOARD_VIEW', 'View dashboard metrics and KPIs'),
(2, 'USERS_MANAGE', 'Create, edit, and deactivate system users'),
(3, 'ROLES_MANAGE', 'Configure roles and security permissions'),
(4, 'PRODUCTS_VIEW', 'View product catalogue and variants'),
(5, 'PRODUCTS_MANAGE', 'Add, edit, or deactivate products and variants'),
(6, 'PRODUCTS_DELETE', 'Delete or permanently retire products'),
(7, 'PURCHASE_PRICE_VIEW', 'View cost/purchase prices of clothing items'),
(8, 'PURCHASE_PRICE_EDIT', 'Change cost/purchase prices of clothing items'),
(9, 'INVENTORY_VIEW', 'View current stock levels and reorder alerts'),
(10, 'STOCK_RECEIVE', 'Process incoming stock from suppliers'),
(11, 'STOCK_ADJUST', 'Perform stock adjustments (damage, lost, count corrections)'),
(12, 'BARCODE_GENERATE', 'Generate unique Code 128 barcodes for variants'),
(13, 'BARCODE_PRINT', 'Print clothing tag barcode labels'),
(14, 'POS_ACCESS', 'Access POS billing terminal and scan barcodes'),
(15, 'SALES_PROCESS', 'Complete sales, collect payments, and print invoices'),
(16, 'INVOICE_REPRINT', 'Reprint existing customer invoices'),
(17, 'RETURNS_PROCESS', 'Process customer returns and exchange transactions'),
(18, 'REPORTS_VIEW', 'View and export sales, inventory, and profit reports'),
(19, 'SETTINGS_MANAGE', 'Configure store details, tax, invoice formats, and hardware'),
(20, 'BACKUP_RESTORE', 'Execute database backup and restore operations'),
(21, 'AUDIT_LOG_VIEW', 'Inspect system audit trails and security activity logs');

-- ---------------------------------------------------------------------
-- 3. Role Permissions Mapping
-- ---------------------------------------------------------------------
-- ADMIN (All 21 permissions)
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 1, `permission_id` FROM `permissions`;

-- MANAGER
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(2, 1),  -- DASHBOARD_VIEW
(2, 4),  -- PRODUCTS_VIEW
(2, 5),  -- PRODUCTS_MANAGE
(2, 7),  -- PURCHASE_PRICE_VIEW
(2, 8),  -- PURCHASE_PRICE_EDIT
(2, 9),  -- INVENTORY_VIEW
(2, 10), -- STOCK_RECEIVE
(2, 11), -- STOCK_ADJUST
(2, 12), -- BARCODE_GENERATE
(2, 13), -- BARCODE_PRINT
(2, 14), -- POS_ACCESS
(2, 15), -- SALES_PROCESS
(2, 16), -- INVOICE_REPRINT
(2, 17), -- RETURNS_PROCESS
(2, 18); -- REPORTS_VIEW

-- CASHIER
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(3, 1),  -- DASHBOARD_VIEW (restricted)
(3, 4),  -- PRODUCTS_VIEW
(3, 13), -- BARCODE_PRINT
(3, 14), -- POS_ACCESS
(3, 15), -- SALES_PROCESS
(3, 16), -- INVOICE_REPRINT
(3, 17); -- RETURNS_PROCESS (only if explicitly assigned)

-- STOCK_STAFF
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(4, 4),  -- PRODUCTS_VIEW
(4, 5),  -- PRODUCTS_MANAGE
(4, 7),  -- PURCHASE_PRICE_VIEW
(4, 9),  -- INVENTORY_VIEW
(4, 10), -- STOCK_RECEIVE
(4, 11), -- STOCK_ADJUST
(4, 12), -- BARCODE_GENERATE
(4, 13); -- BARCODE_PRINT

-- ---------------------------------------------------------------------
-- 4. Initial Users
-- BCrypt Hashed Passwords:
-- admin   / Admin@123   (require_password_change = 1 for security)
-- manager / Manager@123
-- cashier / Cashier@123
-- stock   / Stock@123
-- ---------------------------------------------------------------------
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `full_name`, `email`, `phone`, `role_id`, `is_active`, `require_password_change`) VALUES
(1, 'admin', '$2a$10$wK1F5N8bO/lPZ0E.E8cQeOKpB3rB.5X5d0hE3h1u5k1wB8s2gB7Ce', 'System Administrator', 'admin@drrow.com', '+94 77 123 4567', 1, 1, 1),
(2, 'manager', '$2a$10$u8L0k3qB6gG1v5t.L7aJeOCqC5sC.6Y6e1iF4i2v6l2xC9t3hC8De', 'Store Manager', 'manager@drrow.com', '+94 77 234 5678', 2, 1, 0),
(3, 'cashier', '$2a$10$t7K9j2pA5fF0u4s.K6zIdNBpB4rB.5X5d0hE3h1u5k1wB8s2gB7Ce', 'Lead Cashier', 'cashier@drrow.com', '+94 77 345 6789', 3, 1, 0),
(4, 'stock', '$2a$10$s6J8i1oZ4eE9t3r.J5yHcMAoA3qA.4W4c9gD2g0t4j0vA7r1fA6Bd', 'Warehouse Specialist', 'stock@drrow.com', '+94 77 456 7890', 4, 1, 0);

-- ---------------------------------------------------------------------
-- 5. Standard Clothing Sizes
-- ---------------------------------------------------------------------
INSERT INTO `sizes` (`size_id`, `size_name`, `display_order`, `is_active`) VALUES
(1, 'XS', 10, 1),
(2, 'S', 20, 1),
(3, 'M', 30, 1),
(4, 'L', 40, 1),
(5, 'XL', 50, 1),
(6, 'XXL', 60, 1),
(7, '3XL', 70, 1),
(8, 'Free Size', 80, 1);

-- ---------------------------------------------------------------------
-- 6. Standard Clothing Colours
-- ---------------------------------------------------------------------
INSERT INTO `colours` (`colour_id`, `colour_name`, `hex_code`, `is_active`) VALUES
(1, 'Black', '#000000', 1),
(2, 'White', '#FFFFFF', 1),
(3, 'Navy Blue', '#000080', 1),
(4, 'Red', '#FF0000', 1),
(5, 'Royal Blue', '#4169E1', 1),
(6, 'Grey', '#808080', 1),
(7, 'Olive Green', '#556B2F', 1),
(8, 'Burgundy', '#800020', 1),
(9, 'Beige', '#F5F5DC', 1),
(10, 'Pastel Pink', '#FFD1DC', 1);

-- ---------------------------------------------------------------------
-- 7. Clothing Categories
-- ---------------------------------------------------------------------
INSERT INTO `categories` (`category_id`, `name`, `description`, `is_active`) VALUES
(1, 'T-Shirts', 'Casual and graphic crew-neck and v-neck t-shirts', 1),
(2, 'Shirts', 'Formal and casual button-down shirts', 1),
(3, 'Jeans', 'Denim jeans, regular, slim and relaxed fit', 1),
(4, 'Trousers', 'Chinos, formal trousers and pleated pants', 1),
(5, 'Dresses', 'Casual, party and summer dresses', 1),
(6, 'Jackets', 'Bomber jackets, blazers, and winter coats', 1),
(7, 'Skirts', 'Mini, midi and maxi skirts', 1),
(8, 'Kids Wear', 'Children clothing for all age groups', 1),
(9, 'Accessories', 'Belts, caps, scarves, and fashion accessories', 1),
(10, 'Footwear', 'Casual shoes, sneakers, and sandals', 1);

-- ---------------------------------------------------------------------
-- 8. Clothing Brands
-- ---------------------------------------------------------------------
INSERT INTO `brands` (`brand_id`, `name`, `description`, `is_active`) VALUES
(1, 'D’RROW Signature', 'D’RROW luxury flagship fashion line', 1),
(2, 'D’RROW Urban', 'Contemporary streetwear and casual wear', 1),
(3, 'D’RROW Essentials', 'Everyday basic cottons and comfort wear', 1),
(4, 'LuxeTailor', 'Premium bespoke and executive tailored formal wear', 1),
(5, 'DenimCraft', 'Raw selvedge and stretch premium denim', 1);

-- ---------------------------------------------------------------------
-- 9. System Settings
-- ---------------------------------------------------------------------
INSERT INTO `settings` (`setting_key`, `setting_value`, `setting_group`, `description`) VALUES
('store.name', 'D’RROW Clothing Store', 'STORE', 'Official store business name'),
('store.tagline', 'Modern Elegance & Premium Fashion', 'STORE', 'Brand subtitle/tagline on receipts'),
('store.address', 'No. 124, Galle Road, Colombo 03, Sri Lanka', 'STORE', 'Store physical address'),
('store.phone', '+94 11 234 5678 / +94 77 987 6543', 'STORE', 'Store contact telephone numbers'),
('store.email', 'support@drrow.com', 'STORE', 'Store customer service email'),
('store.currency', 'Rs.', 'GENERAL', 'Currency symbol or abbreviation'),
('store.currency_code', 'LKR', 'GENERAL', 'ISO currency code'),
('pos.tax_enabled', 'false', 'POS', 'Whether sales tax calculation is active'),
('pos.tax_percentage', '0.00', 'POS', 'Sales tax percentage'),
('pos.default_discount', '0.00', 'POS', 'Default discount percentage'),
('pos.allow_negative_stock', 'false', 'INVENTORY', 'Allow billing when available stock is 0'),
('barcode.prefix', 'DRTS', 'BARCODE', 'Barcode prefix for clothing tags'),
('barcode.symbology', 'CODE_128', 'BARCODE', 'Preferred barcode format (Code 128)'),
('invoice.prefix', 'DRR', 'INVOICE', 'Invoice number prefix'),
('invoice.format', 'PREFIX-YYYYMMDD-SEQ', 'INVOICE', 'Invoice sequence pattern'),
('invoice.footer', 'Thank you for shopping at D’RROW Clothing Store! Please visit again.', 'INVOICE', 'Receipt footer greeting message'),
('policy.return_exchange', 'Items can be exchanged within 7 days with original tag attached and receipt. No cash refunds.', 'INVOICE', 'Store return and exchange policy message'),
('inventory.low_stock_threshold', '5', 'INVENTORY', 'Default low-stock warning threshold');

-- ---------------------------------------------------------------------
-- 10. Sample Suppliers
-- ---------------------------------------------------------------------
INSERT INTO `suppliers` (`supplier_id`, `name`, `contact_person`, `phone`, `email`, `address`, `is_active`) VALUES
(1, 'Apex Garments Lanka (Pvt) Ltd', 'Rohan Perera', '+94 11 765 4321', 'sales@apexgarments.lk', 'Biyagama Free Trade Zone, Sri Lanka', 1),
(2, 'SilkRoute Textiles International', 'Kavinda Silva', '+94 71 888 9900', 'orders@silkroutetextiles.com', 'Katunayake EPZ, Sri Lanka', 1),
(3, 'Denim Mills Global', 'Nimal Fernando', '+94 77 333 4455', 'supply@denimmills.com', 'Ratmalana Industrial Estate, Colombo', 1);

-- ---------------------------------------------------------------------
-- 11. Initial Customers (Walk-In and VIP Members)
-- ---------------------------------------------------------------------
INSERT INTO `customers` (`customer_id`, `name`, `phone`, `email`, `address`, `notes`) VALUES
(1, 'Walk-In Customer', '0000000000', 'walkin@drrow.com', 'In-Store', 'Default guest customer for counter sales'),
(2, 'Dilshan Senanayake', '+94 77 111 2233', 'dilshan.s@gmail.com', 'No. 45, Alfred Place, Colombo 03', 'Loyalty Tier: Gold'),
(3, 'Ananya Wickramasinghe', '+94 71 444 5566', 'ananya.w@yahoo.com', 'No. 12/B, Flower Road, Colombo 07', 'Prefers D’RROW Signature collection');

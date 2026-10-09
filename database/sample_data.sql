-- =====================================================================
-- D’RROW Clothing Store POS and Inventory Management System
-- Sample Products, Variants, Barcodes, Inventory Stock, and Receipts
-- =====================================================================

USE `drrow_pos`;

-- ---------------------------------------------------------------------
-- 1. Clothing Products
-- ---------------------------------------------------------------------
INSERT INTO `products` (`product_id`, `item_code`, `name`, `category_id`, `brand_id`, `description`, `purchase_price`, `selling_price`, `reorder_level`, `is_active`) VALUES
(1, 'DRTS0001', 'Classic Polo Shirt', 1, 3, '100% combed cotton pique polo shirt with embroidered D’RROW insignia', 2200.00, 4990.00, 5, 1),
(2, 'DRSH0002', 'Slim Fit Cotton Shirt', 2, 4, 'Formal long-sleeve luxury twill cotton shirt with spread collar', 3200.00, 6990.00, 4, 1),
(3, 'DRJN0003', 'Premium Denim Jeans', 3, 5, 'Classic 5-pocket stretch denim with authentic whiskering and copper hardware', 4100.00, 8990.00, 6, 1),
(4, 'DRHD0004', 'Urban Streetwear Hoodie', 6, 2, 'Heavyweight 400gsm brushed fleece oversized drop-shoulder pullover hoodie', 4800.00, 9990.00, 3, 1),
(5, 'DRDR0005', 'Ladies Casual Wrap Dress', 5, 1, 'Breathable rayon A-line wrap silhouette dress with adjustable waist sash', 3800.00, 7990.00, 4, 1),
(6, 'DRKD0006', 'Kids Cotton Graphic Tee', 8, 3, 'Soft hypoallergenic organic cotton children t-shirt with playful graphic print', 1200.00, 2490.00, 8, 1),
(7, 'DRTR0007', 'Formal Pleated Trouser', 4, 4, 'Wrinkle-resistant poly-viscose blend tailored dress pants with tapered hem', 3500.00, 7490.00, 5, 1);

-- ---------------------------------------------------------------------
-- 2. Product Variants with Unique Code 128 Barcodes
-- ---------------------------------------------------------------------
INSERT INTO `product_variants` (`variant_id`, `product_id`, `sku`, `barcode`, `size_id`, `colour_id`, `purchase_price`, `selling_price`, `is_active`) VALUES
-- Classic Polo Shirt (Variants 1-10)
(1, 1, 'DRTS0001-BLK-S', 'DRTS000001', 2, 1, 2200.00, 4990.00, 1),
(2, 1, 'DRTS0001-BLK-M', 'DRTS000002', 3, 1, 2200.00, 4990.00, 1),
(3, 1, 'DRTS0001-BLK-L', 'DRTS000003', 4, 1, 2200.00, 4990.00, 1),
(4, 1, 'DRTS0001-BLK-XL', 'DRTS000004', 5, 1, 2200.00, 4990.00, 1),
(5, 1, 'DRTS0001-WHT-S', 'DRTS000005', 2, 2, 2200.00, 4990.00, 1),
(6, 1, 'DRTS0001-WHT-M', 'DRTS000006', 3, 2, 2200.00, 4990.00, 1),
(7, 1, 'DRTS0001-WHT-L', 'DRTS000007', 4, 2, 2200.00, 4990.00, 1),
(8, 1, 'DRTS0001-WHT-XL', 'DRTS000008', 5, 2, 2200.00, 4990.00, 1),
(9, 1, 'DRTS0001-NVY-M', 'DRTS000009', 3, 3, 2200.00, 4990.00, 1),
(10, 1, 'DRTS0001-NVY-L', 'DRTS000010', 4, 3, 2200.00, 4990.00, 1),

-- Slim Fit Cotton Shirt (Variants 11-14)
(11, 2, 'DRSH0002-WHT-M', 'DRSH000011', 3, 2, 3200.00, 6990.00, 1),
(12, 2, 'DRSH0002-WHT-L', 'DRSH000012', 4, 2, 3200.00, 6990.00, 1),
(13, 2, 'DRSH0002-RBL-M', 'DRSH000013', 3, 5, 3200.00, 6990.00, 1),
(14, 2, 'DRSH0002-RBL-L', 'DRSH000014', 4, 5, 3200.00, 6990.00, 1),

-- Premium Denim Jeans (Variants 15-18)
(15, 3, 'DRJN0003-NVY-S', 'DRJN000015', 2, 3, 4100.00, 8990.00, 1),
(16, 3, 'DRJN0003-NVY-M', 'DRJN000016', 3, 3, 4100.00, 8990.00, 1),
(17, 3, 'DRJN0003-NVY-L', 'DRJN000017', 4, 3, 4100.00, 8990.00, 1),
(18, 3, 'DRJN0003-BLK-M', 'DRJN000018', 3, 1, 4100.00, 8990.00, 1),

-- Urban Streetwear Hoodie (Variants 19-22)
(19, 4, 'DRHD0004-BLK-M', 'DRHD000019', 3, 1, 4800.00, 9990.00, 1),
(20, 4, 'DRHD0004-BLK-L', 'DRHD000020', 4, 1, 4800.00, 9990.00, 1),
(21, 4, 'DRHD0004-GRY-M', 'DRHD000021', 3, 6, 4800.00, 9990.00, 1),
(22, 4, 'DRHD0004-GRY-XL', 'DRHD000022', 5, 6, 4800.00, 9990.00, 1),

-- Ladies Casual Wrap Dress (Variants 23-25)
(23, 5, 'DRDR0005-PNK-S', 'DRDR000023', 2, 10, 3800.00, 7990.00, 1),
(24, 5, 'DRDR0005-PNK-M', 'DRDR000024', 3, 10, 3800.00, 7990.00, 1),
(25, 5, 'DRDR0005-RED-M', 'DRDR000025', 3, 4, 3800.00, 7990.00, 1),

-- Kids Cotton Graphic Tee (Variants 26-27)
(26, 6, 'DRKD0006-RED-XS', 'DRKD000026', 1, 4, 1200.00, 2490.00, 1),
(27, 6, 'DRKD0006-RBL-S', 'DRKD000027', 2, 5, 1200.00, 2490.00, 1),

-- Formal Pleated Trouser (Variants 28-29)
(28, 7, 'DRTR0007-BLK-M', 'DRTR000028', 3, 1, 3500.00, 7490.00, 1),
(29, 7, 'DRTR0007-GRY-L', 'DRTR000029', 4, 6, 3500.00, 7490.00, 1);

-- ---------------------------------------------------------------------
-- 3. Initial Inventory Stock Levels
-- ---------------------------------------------------------------------
INSERT INTO `stock` (`stock_id`, `variant_id`, `current_stock`, `available_stock`, `reserved_stock`) VALUES
(1, 1, 25, 25, 0),
(2, 2, 35, 35, 0), -- Classic Polo Black / M (Key test item)
(3, 3, 20, 20, 0),
(4, 4, 15, 15, 0),
(5, 5, 18, 18, 0),
(6, 6, 30, 30, 0),
(7, 7, 22, 22, 0),
(8, 8, 12, 12, 0),
(9, 9, 28, 28, 0),
(10, 10, 16, 16, 0),
(11, 11, 20, 20, 0),
(12, 12, 15, 15, 0),
(13, 13, 18, 18, 0),
(14, 14, 14, 14, 0),
(15, 15, 22, 22, 0),
(16, 16, 28, 28, 0),
(17, 17, 18, 18, 0),
(18, 18, 24, 24, 0),
(19, 19, 12, 12, 0),
(20, 20, 10, 10, 0),
(21, 21, 14, 14, 0),
(22, 22, 8, 8, 0),
(23, 23, 15, 15, 0),
(24, 24, 20, 20, 0),
(25, 25, 12, 12, 0),
(26, 26, 30, 30, 0),
(27, 27, 25, 25, 0),
(28, 28, 18, 18, 0),
(29, 29, 16, 16, 0);

-- ---------------------------------------------------------------------
-- 4. Initial Stock Receipts & Audit Log
-- ---------------------------------------------------------------------
INSERT INTO `stock_receipts` (`receipt_id`, `receipt_number`, `supplier_id`, `user_id`, `receipt_date`, `total_amount`, `reference_number`, `notes`) VALUES
(1, 'REC-20261001-0001', 1, 1, '2026-10-01', 250000.00, 'PO-APEX-9821', 'Opening inventory stock receipt from Apex Garments');

INSERT INTO `stock_movements` (`variant_id`, `movement_type`, `quantity`, `before_quantity`, `after_quantity`, `reference_id`, `reference_type`, `notes`, `user_id`)
SELECT `variant_id`, 'RECEIVE', `current_stock`, 0, `current_stock`, 'REC-20261001-0001', 'RECEIPT', 'Initial inventory batch intake', 1
FROM `stock`;

INSERT INTO `audit_logs` (`user_id`, `username`, `action`, `entity_type`, `entity_id`, `description`) VALUES
(1, 'admin', 'INITIAL_IMPORT', 'DATABASE', 'ALL', 'Initial D’RROW sample inventory and product variants loaded into system');

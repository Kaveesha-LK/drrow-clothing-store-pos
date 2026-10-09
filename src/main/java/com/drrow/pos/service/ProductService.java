package com.drrow.pos.service;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.dao.*;
import com.drrow.pos.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final ColourDAO colourDAO = new ColourDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public Product getProductById(int productId) throws SQLException {
        Product p = productDAO.findById(productId);
        if (p != null) {
            p.setVariants(variantDAO.findByProductId(productId));
        }
        return p;
    }

    public List<Product> searchProducts(String keyword, Integer categoryId, Integer brandId) throws SQLException {
        return productDAO.search(keyword, categoryId, brandId);
    }

    public List<ProductVariant> getVariantsForProduct(int productId) throws SQLException {
        return variantDAO.findByProductId(productId);
    }

    public ProductVariant getVariantByBarcode(String barcode) throws SQLException {
        return variantDAO.findByBarcode(barcode);
    }

    public ProductVariant getVariantById(int variantId) throws SQLException {
        return variantDAO.findById(variantId);
    }

    public List<ProductVariant> searchVariants(String keyword) throws SQLException {
        return variantDAO.search(keyword);
    }

    /**
     * Saves a product along with variants and initial stock inside an atomic transaction.
     */
    public boolean saveProductWithVariants(Product product, List<ProductVariant> variants, int initialStockPerVariant) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            boolean isNew = (product.getProductId() == 0);
            if (isNew) {
                productDAO.create(product, conn);
            } else {
                productDAO.update(product, conn);
            }

            int currentUserId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
            String username = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "system";

            for (ProductVariant v : variants) {
                v.setProductId(product.getProductId());
                if (v.getVariantId() == 0) {
                    // Check duplicate barcode
                    if (variantDAO.isBarcodeExists(v.getBarcode(), null)) {
                        throw new SQLException("Barcode already exists: " + v.getBarcode());
                    }
                    variantDAO.create(v, conn);

                    // Initialize stock
                    int stockQty = Math.max(0, initialStockPerVariant);
                    stockDAO.upsertStock(v.getVariantId(), stockQty, stockQty, conn);

                    if (stockQty > 0) {
                        StockMovement sm = new StockMovement(
                            v.getVariantId(),
                            "RECEIVE",
                            stockQty,
                            0,
                            stockQty,
                            "INIT-" + product.getItemCode(),
                            "MANUAL",
                            "Initial stock assignment on product creation",
                            currentUserId
                        );
                        movementDAO.recordMovement(sm, conn);
                    }
                } else {
                    if (variantDAO.isBarcodeExists(v.getBarcode(), v.getVariantId())) {
                        throw new SQLException("Barcode already exists for another variant: " + v.getBarcode());
                    }
                    variantDAO.update(v, conn);
                }
            }

            auditDAO.log(new AuditLog(
                currentUserId,
                username,
                isNew ? "PRODUCT_CREATE" : "PRODUCT_UPDATE",
                "PRODUCT",
                String.valueOf(product.getProductId()),
                (isNew ? "Created" : "Updated") + " product " + product.getName() + " (" + product.getItemCode() + ") with " + variants.size() + " variants"
            ), conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        Product p = productDAO.findById(productId);
        boolean deleted = productDAO.delete(productId);
        if (deleted && p != null) {
            int uid = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
            String un = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUsername() : "system";
            auditDAO.log(new AuditLog(uid, un, "PRODUCT_DELETE", "PRODUCT", String.valueOf(productId), "Deleted product " + p.getName()), null);
        }
        return deleted;
    }

    public String generateNextBarcode(String prefix) throws SQLException {
        return variantDAO.generateNextBarcode(prefix);
    }

    public String generateNextItemCode(String prefix) throws SQLException {
        int seq = productDAO.getNextProductSequence();
        return String.format("%s%04d", prefix, seq);
    }

    // Master data lookups
    public List<Category> getAllCategories() throws SQLException { return categoryDAO.findAll(); }
    public boolean saveCategory(Category c) throws SQLException {
        return c.getCategoryId() == 0 ? categoryDAO.create(c) : categoryDAO.update(c);
    }
    public boolean deleteCategory(int id) throws SQLException { return categoryDAO.delete(id); }

    public List<Brand> getAllBrands() throws SQLException { return brandDAO.findAll(); }
    public boolean saveBrand(Brand b) throws SQLException {
        return b.getBrandId() == 0 ? brandDAO.create(b) : brandDAO.update(b);
    }
    public boolean deleteBrand(int id) throws SQLException { return brandDAO.delete(id); }

    public List<Size> getAllSizes() throws SQLException { return sizeDAO.findAll(); }
    public boolean saveSize(Size s) throws SQLException {
        return s.getSizeId() == 0 ? sizeDAO.create(s) : sizeDAO.update(s);
    }
    public boolean deleteSize(int id) throws SQLException { return sizeDAO.delete(id); }

    public List<Colour> getAllColours() throws SQLException { return colourDAO.findAll(); }
    public boolean saveColour(Colour c) throws SQLException {
        return c.getColourId() == 0 ? colourDAO.create(c) : colourDAO.update(c);
    }
    public boolean deleteColour(int id) throws SQLException { return colourDAO.delete(id); }
}

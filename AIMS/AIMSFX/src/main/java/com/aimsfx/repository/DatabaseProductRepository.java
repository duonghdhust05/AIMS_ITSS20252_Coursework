package com.aimsfx.repository;

import com.aimsfx.utils.DatabaseConnection;
import com.aimsfx.model.*;
import com.aimsfx.factory.ProductFactory;
import com.aimsfx.factory.ProductFactoryRegistry;
import com.aimsfx.repository.mapper.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DatabaseProductRepository - Database implementation of ProductRepository
 *
 * DESIGN PATTERN: Repository Pattern (Concrete Implementation)
 * STORAGE: PostgreSQL database using JDBC with SINGLE TABLE INHERITANCE
 *
 * ARCHITECTURE:
 * Single table inheritance - all product types in one table
 * Database auto-generates IDs (BIGSERIAL)
 *
 */
public class DatabaseProductRepository implements ProductRepository {

    private final DatabaseConnection dbConnection;

    private static final String INSERT_SQL = "INSERT INTO products (" +
            "barcode, title, category, original_price, current_price, description, " +
            "weight, dimensions, stock, status, vat_rate, product_type, " +
            "is_current, expired_date, " +
            // Book columns
            "author, publisher, publication_date, pages, language, cover_type, genre, " +
            // CD columns (artist, record_label, genre already in Book, track_count,
            // release_date)
            "artist, record_label, track_count, release_date, " +
            // DVD columns (director, studio, subtitle, disc_type, duration, genre,
            // release_date already added)
            "director, studio, subtitle, disc_type, duration, " +
            // Newspaper columns (issn, frequency, editor_in_chief,
            // publisher/publication_date/language already added, section)
            "issn, frequency, editor_in_chief, section" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            +
            "RETURNING product_id";

    public DatabaseProductRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
        System.out.println("DatabaseProductRepository initialized (hardcoded mode)");
    }

    @Override
    public Product save(Product product) {
        if (product.getProductId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    /**
     * Insert new product into database
     * Hardcoded mapping for each product type
     */
    private Product insert(Product product) {
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            int idx = 1;

            // Common columns (14 columns)
            stmt.setString(idx++, product.getBarcode());
            stmt.setString(idx++, product.getTitle());
            stmt.setString(idx++, product.getCategory());
            stmt.setDouble(idx++, product.getOriginalPrice());
            stmt.setDouble(idx++, product.getCurrentPrice());
            stmt.setString(idx++, product.getDescription());
            // Handle nullable physical attributes (null for digital products)
            if (product.getWeight() != null) {
                stmt.setDouble(idx++, product.getWeight());
            } else {
                stmt.setNull(idx++, Types.DOUBLE);
            }
            stmt.setString(idx++, product.getDimensions()); // setString handles null
            stmt.setInt(idx++, product.getStock());
            stmt.setString(idx++, product.getStatus());
            stmt.setDouble(idx++, product.getVatRate());
            stmt.setString(idx++, getProductTypeString(product));
            stmt.setBoolean(idx++, true); // is_current
            stmt.setNull(idx++, Types.TIMESTAMP); // expired_date

            // Type-specific columns using helper method
            idx = setTypeSpecificColumns(stmt, idx, product);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long productId = rs.getLong("product_id");
                product.setProductId(productId);
                return product;
            } else {
                throw new RuntimeException("Failed to insert product: No ID returned");
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to insert product: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to insert product", e);
        }
    }

    /**
     * Update existing product in database (creates new version with history
     * tracking)
     * 0. Check if product has actually changed - if not, skip update
     * 1. Expire the current version (set is_current=false, expired_date=NOW())
     * 2. Insert a new version with the updated data (is_current=true)
     */
    private Product update(Product product) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Step 0: Check if product has actually changed
                Product existingProduct = findById(product.getProductId()).orElse(null);
                if (existingProduct != null && areProductsEqual(existingProduct, product)) {
                    // No changes detected, return existing product without creating new version
                    conn.rollback();
                    return existingProduct;
                }

                // Step 1: Expire the current version
                String expireSql = "UPDATE products SET is_current = false, expired_date = NOW() " +
                        "WHERE product_id = ? AND is_current = true";

                try (PreparedStatement expireStmt = conn.prepareStatement(expireSql)) {
                    expireStmt.setLong(1, product.getProductId());
                    int rowsAffected = expireStmt.executeUpdate();

                    if (rowsAffected == 0) {
                        throw new SQLException("Product not found or already expired: " + product.getProductId());
                    }
                }

                product.setProductId(null);

                try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
                    int idx = 1;

                    // Common columns (14 columns)
                    stmt.setString(idx++, product.getBarcode());
                    stmt.setString(idx++, product.getTitle());
                    stmt.setString(idx++, product.getCategory());
                    stmt.setDouble(idx++, product.getOriginalPrice());
                    stmt.setDouble(idx++, product.getCurrentPrice());
                    stmt.setString(idx++, product.getDescription());
                    // Handle nullable physical attributes (null for digital products)
                    if (product.getWeight() != null) {
                        stmt.setDouble(idx++, product.getWeight());
                    } else {
                        stmt.setNull(idx++, Types.DOUBLE);
                    }
                    stmt.setString(idx++, product.getDimensions()); // setString handles null
                    stmt.setInt(idx++, product.getStock());
                    stmt.setString(idx++, product.getStatus());
                    stmt.setDouble(idx++, product.getVatRate());
                    stmt.setString(idx++, getProductTypeString(product));
                    stmt.setBoolean(idx++, true); // is_current
                    stmt.setNull(idx++, Types.TIMESTAMP); // expired_date

                    // Type-specific columns based on product type
                    idx = setTypeSpecificColumns(stmt, idx, product);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        Long newProductId = rs.getLong("product_id");
                        product.setProductId(newProductId);
                    } else {
                        throw new SQLException("Failed to insert new version: No ID returned");
                    }
                }

                conn.commit();
                return product;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update product: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update product", e);
        }
    }

    /**
     * Helper method to set type-specific columns for PreparedStatement
     * Returns the next parameter index after setting all columns
     * 
     * REFACTORED: Uses Strategy Pattern via ProductMapperRegistry
     * OCP COMPLIANT: Adding new product types doesn't require modifying this method
     * 
     * Each mapper handles ALL 20 type-specific columns:
     * - Sets its own type's columns with actual values
     * - Sets other types' columns with NULL
     * 
     */
    private int setTypeSpecificColumns(PreparedStatement stmt, int startIdx, Product product) throws SQLException {
        // TRUE STRATEGY PATTERN: Delegate entirely to the mapper
        // No if-else chains, no switch statements
        // OCP: To add new product type, just create new mapper - this method stays
        // unchanged!
        ProductJdbcMapper<Product> mapper = (ProductJdbcMapper<Product>) ProductMapperRegistry
                .getMapperForProduct(product);
        return mapper.setAllTypeSpecificColumns(stmt, startIdx, product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE product_id = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = createProductFromResultSet(rs);
                return Optional.of(product);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to find product by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Product> findCurrentByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM products WHERE barcode = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = createProductFromResultSet(rs);
                return Optional.of(product);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to find product by barcode: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_current = true ORDER BY product_id";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to fetch all products: " + e.getMessage());
        }

        return products;
    }

    @Override
    public boolean deleteById(Long id) {
        // Soft delete: Mark as expired instead of physically deleting
        // Set is_current = false and expired_date = NOW()
        String sql = "UPDATE products SET is_current = false, expired_date = NOW() WHERE product_id = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to delete product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to check product existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Compare two products to check if they are equal (ignoring ID and timestamps)
     * Used to avoid creating unnecessary versions when nothing has changed
     * 
     * OCP SOLUTION: Uses polymorphic equals() method
     * - Product.equals() compares common fields
     * - Subclass equals() compares type-specific fields
     * - No instanceof chains needed - just call p1.equals(p2)
     */
    private boolean areProductsEqual(Product p1, Product p2) {
        if (p1 == null || p2 == null)
            return false;
        return p1.equals(p2);
    }

    @Override
    public List<Product> findHistoryByProductId(Long productId) {
        List<Product> history = new ArrayList<>();

        // First, get the barcode of the product (to find all versions with same
        // barcode)
        String barcode = null;
        String getBarcodeSql = "SELECT barcode FROM products WHERE product_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(getBarcodeSql)) {

            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                barcode = rs.getString("barcode");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get barcode: " + e.getMessage());
            return history;
        }

        if (barcode == null) {
            return history; // Product not found
        }

        // Now get all versions with this barcode, ordered by created_at DESC (newest
        // first)
        String historySql = "SELECT * FROM products WHERE barcode = ? ORDER BY created_at DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(historySql)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                history.add(product);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to fetch product history: " + e.getMessage());
        }

        return history;
    }

    /**
     * OCP SOLUTION: Uses Factory + Mapper pattern to reconstruct Product from
     * ResultSet
     * 
     * HOW IT WORKS:
     * 1. Get product type from DB (e.g., "BOOK")
     * 2. Factory.createEmptyProduct() creates empty instance (e.g., new Book())
     * 3. Mapper.populateFromResultSet() fills type-specific fields
     * 4. setCommonAttributes() fills common fields
     * 
     * BENEFITS:
     * - No switch statement needed
     * - Adding new type: just add new Factory + Mapper
     * - Existing code unchanged (OCP)
     */
    private Product createProductFromResultSet(ResultSet rs) throws SQLException {
        String productTypeStr = rs.getString("product_type");

        // Use Factory to create empty product instance
        ProductFactory factory = ProductFactoryRegistry.getFactory(productTypeStr);
        Product product = factory.createEmptyProduct();

        // Use Mapper to populate type-specific fields from ResultSet
        ProductJdbcMapper<Product> mapper = ProductMapperRegistry.getMapper(productTypeStr);
        mapper.populateFromResultSet(rs, product);

        // Set common attributes
        setCommonAttributes(product, rs);

        return product;
    }

    private void setCommonAttributes(Product product, ResultSet rs) throws SQLException {
        product.setProductId(rs.getLong("product_id"));
        product.setBarcode(rs.getString("barcode"));
        product.setTitle(rs.getString("title"));
        product.setCategory(rs.getString("category"));
        product.setOriginalPrice(rs.getDouble("original_price"));
        product.setCurrentPrice(rs.getDouble("current_price"));
        product.setDescription(rs.getString("description"));
        // Handle nullable physical attributes (null for digital products)
        double weight = rs.getDouble("weight");
        if (!rs.wasNull()) {
            product.setWeight(weight);
        }
        product.setDimensions(rs.getString("dimensions"));
        product.setStock(rs.getInt("stock"));
        product.setStatus(rs.getString("status"));
        product.setVatRate(rs.getDouble("vat_rate"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            product.setCreatedAt(createdAt.toLocalDateTime());

        // History tracking fields
        product.setIsCurrent(rs.getBoolean("is_current"));
        Timestamp expiredDate = rs.getTimestamp("expired_date");
        if (expiredDate != null)
            product.setExpiredDate(expiredDate.toLocalDateTime());
    }

    /**
     * OCP SOLUTION: Uses polymorphic getProductType() method
     * Each Product subclass returns its own ProductType via polymorphism
     * No instanceof checks needed!
     */
    private String getProductTypeString(Product product) {
        return product.getProductType().toString();
    }

    /**
     * Update product stock after order is paid
     * Updates only the current version of the product (is_current = true)
     * 
     * @param productId Product ID
     * @param newStock  New stock quantity
     * @return true if update successful, false otherwise
     */
    @Override
    public boolean updateStock(Long productId, Integer newStock) {
        String sql = "UPDATE products SET stock = ? " +
                "WHERE product_id = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setLong(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(String.format("✅ Stock updated in database: Product ID=%d, New Stock=%d",
                        productId, newStock));
                return true;
            } else {
                System.err.println(String.format("⚠️ No product found to update: Product ID=%d", productId));
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to update stock in database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Atomically deducts stock from the database (solves Race Condition).
     * The condition 'stock >= ?' ensures stock never goes negative.
     */
    @Override
    public boolean deductStockAtomically(Long productId, int quantity) {
        String sql = "UPDATE products SET stock = stock - ? " +
                "WHERE product_id = ? AND is_current = true AND stock >= ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setInt(3, quantity);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(String.format("✅ Stock atomically deducted: Product ID=%d, Quantity=%d",
                        productId, quantity));
                return true;
            } else {
                System.err.println(String.format(
                        "⚠️ Failed to deduct stock (Out of stock or product not found): Product ID=%d", productId));
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database error during atomic stock deduction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Atomically restores stock to the database (used for Compensating
     * Transactions).
     */
    @Override
    public boolean restoreStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock = stock + ? " +
                "WHERE product_id = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(String.format("✅ Stock atomically restored: Product ID=%d, Quantity=%d",
                        productId, quantity));
                return true;
            } else {
                System.err.println(
                        String.format("⚠️ Failed to restore stock (product not found): Product ID=%d", productId));
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database error during stock restoration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deductStockForOrder(List<com.aimsfx.model.OrderItem> items) {
        if (items == null || items.isEmpty())
            return true;

        String sql = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND is_current = true AND stock >= ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (com.aimsfx.model.OrderItem item : items) {
                    Product product = item.getProduct();
                    if (product == null || product.getProductId() == null)
                        continue;

                    stmt.setInt(1, item.getQuantity());
                    stmt.setLong(2, product.getProductId());
                    stmt.setInt(3, item.getQuantity());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        // Out of stock or product not found. Rollback all previous deductions.
                        conn.rollback();
                        System.err.println(
                                String.format("Failed to deduct stock for Product ID=%d. Rolling back transaction.",
                                        product.getProductId()));
                        return false;
                    }
                }
            }

            conn.commit();
            System.out.println("All stock deductions completed successfully in a single transaction.");
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("ERROR: Failed to rollback stock deduction: " + ex.getMessage());
                }
            }
            System.err.println("Database error during transactional stock deduction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("ERROR: Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean restoreStockForOrder(List<com.aimsfx.model.OrderItem> items) {
        if (items == null || items.isEmpty())
            return true;

        String sql = "UPDATE products SET stock = stock + ? WHERE product_id = ? AND is_current = true";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (com.aimsfx.model.OrderItem item : items) {
                    Product product = item.getProduct();
                    if (product == null || product.getProductId() == null)
                        continue;

                    stmt.setInt(1, item.getQuantity());
                    stmt.setLong(2, product.getProductId());

                    stmt.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("All stock restorations completed successfully in a single transaction.");
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("ERROR: Failed to rollback stock restoration: " + ex.getMessage());
                }
            }
            System.err.println("Database error during transactional stock restoration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("ERROR: Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Get product details as Map for Update form
     * Returns all common and specific fields with "specific_" prefix
     * 
     * OCP SOLUTION: Uses createProductFromResultSet() + getSpecificDetail()
     * - No switch statement needed
     * - Type-specific fields come from Product.getSpecificDetail() (polymorphism)
     */
    @Override
    public java.util.Map<String, Object> getProductDetails(Long productId) {
        String sql = "SELECT * FROM products WHERE product_id = ? AND is_current = true";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create product using OCP-compliant method
                Product product = createProductFromResultSet(rs);

                java.util.Map<String, Object> details = new java.util.HashMap<>();

                // Common fields
                details.put("product_id", product.getProductId());
                details.put("barcode", product.getBarcode());
                details.put("title", product.getTitle());
                details.put("category", product.getCategory());
                details.put("original_price", product.getOriginalPrice());
                details.put("current_price", product.getCurrentPrice());
                details.put("description", product.getDescription());
                details.put("weight", product.getWeight());
                details.put("dimensions", product.getDimensions());
                details.put("stock", product.getStock());
                details.put("status", product.getStatus());
                details.put("vat_rate", product.getVatRate());
                details.put("product_type", product.getProductType().toString());

                // Specific fields from polymorphic method with "specific_" prefix
                java.util.Map<String, Object> specificDetails = product.getSpecificDetail();
                for (java.util.Map.Entry<String, Object> entry : specificDetails.entrySet()) {
                    details.put("specific_" + entry.getKey(), entry.getValue());
                }

                return details;
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get product details: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update product stock and log the change
     * 
     * @param barcode  Product barcode
     * @param newStock New stock value
     * @param reason   Reason for stock change
     * @return true if updated successfully
     */
    @Override
    public boolean updateStock(String barcode, Integer newStock, String reason) {
        String getStockSql = "SELECT stock FROM products WHERE barcode = ? AND is_current = true";
        String updateStockSql = "UPDATE products SET stock = ? WHERE barcode = ? AND is_current = true";
        String insertLogSql = "INSERT INTO stock_change_log (barcode, from_stock, to_stock, change_reason) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Get current stock
            Integer currentStock;
            try (PreparedStatement stmt = conn.prepareStatement(getStockSql)) {
                stmt.setString(1, barcode);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentStock = rs.getInt("stock");
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Update stock
            try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                stmt.setInt(1, newStock);
                stmt.setString(2, barcode);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Log the change
            try (PreparedStatement stmt = conn.prepareStatement(insertLogSql)) {
                stmt.setString(1, barcode);
                stmt.setInt(2, currentStock);
                stmt.setInt(3, newStock);
                stmt.setString(4, reason);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("ERROR: Failed to rollback: " + ex.getMessage());
                }
            }
            System.err.println("ERROR: Failed to update stock: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("ERROR: Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Get stock change history for a product
     * 
     * @param barcode Product barcode
     * @return List of stock change logs
     */
    @Override
    public List<StockChangeLog> getStockChangeHistory(String barcode) {
        String sql = "SELECT * FROM stock_change_log WHERE barcode = ? ORDER BY changed_at DESC";
        List<StockChangeLog> logs = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockChangeLog log = new StockChangeLog();
                log.setId(rs.getLong("id"));
                log.setBarcode(rs.getString("barcode"));
                log.setFromStock(rs.getInt("from_stock"));
                log.setToStock(rs.getInt("to_stock"));
                log.setChangeReason(rs.getString("change_reason"));
                log.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
                logs.add(log);
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get stock change history: " + e.getMessage());
        }

        return logs;
    }
}

package com.aimsfx.repository;

import com.aimsfx.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseProductRepository
 * Maps to Phase 3 of Test Plan
 */
class DatabaseProductRepositoryTest {

    private DatabaseProductRepository repository;
    private Connection connection;
    private org.mockito.MockedStatic<com.aimsfx.utils.DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() throws Exception {
        // Setup an in-memory SQLite database for testing
        Connection realConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection = org.mockito.Mockito.spy(realConnection);
        org.mockito.Mockito.doAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("CAST(? AS JSONB)")) {
                sql = sql.replace("CAST(? AS JSONB)", "?");
            }
            return realConnection.prepareStatement(sql);
        }).when(connection).prepareStatement(org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.doNothing().when(connection).close();
        
        // Setup table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE products (" +
                    "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "barcode TEXT, " +
                    "title TEXT, " +
                    "category TEXT, " +
                    "original_price REAL, " +
                    "current_price REAL, " +
                    "description TEXT, " +
                    "weight REAL, " +
                    "dimensions TEXT, " +
                    "stock INTEGER, " +
                    "status TEXT, " +
                    "vat_rate REAL, " +
                    "product_type TEXT, " +
                    "is_current BOOLEAN, " +
                    "expired_date TIMESTAMP, " +
                    "attributes TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP)");
        }
        
        com.aimsfx.utils.DatabaseConnection mockDb = org.mockito.Mockito.mock(com.aimsfx.utils.DatabaseConnection.class);
        org.mockito.Mockito.when(mockDb.getConnection()).thenReturn(connection);
        
        mockedDbConnection = org.mockito.Mockito.mockStatic(com.aimsfx.utils.DatabaseConnection.class);
        mockedDbConnection.when(com.aimsfx.utils.DatabaseConnection::getInstance).thenReturn(mockDb);
        
        repository = new DatabaseProductRepository();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedDbConnection != null) {
            mockedDbConnection.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("[IT_PROD_005] DB Insert & Retrieve")
    void testSaveAndFindById() {
        // Arrange
        Product product = new com.aimsfx.model.Book();
        product.setBarcode("BARCODE-123");
        product.setTitle("Test Insert Product");
        product.setCategory("Books");
        product.setOriginalPrice(100.0);
        product.setCurrentPrice(100.0);
        product.setWeight(1.5);
        product.setDimensions("10x10x10");
        product.setStock(5);
        product.setStatus("available");
        product.setVatRate(10.0);

        // Act
        Product savedProduct = repository.save(product);

        // Assert
        assertNotNull(savedProduct.getProductId());
        
        Optional<Product> retrieved = repository.findById(savedProduct.getProductId());
        assertTrue(retrieved.isPresent());
        assertEquals("BARCODE-123", retrieved.get().getBarcode());
        assertEquals("Test Insert Product", retrieved.get().getTitle());
    }

    @Test
    @DisplayName("[IT_PROD_006] DB Delete Product")
    void testDeleteById() {
        // Arrange
        Product product = new com.aimsfx.model.Book();
        product.setOriginalPrice(100.0);
        product.setCategory("Books");
        product.setCurrentPrice(100.0);
        product.setWeight(1.0);
        product.setStock(1);
        product.setStatus("available");
        product.setVatRate(10.0);
        product.setBarcode("BARCODE-DEL");
        product.setTitle("Product to Delete");
        Product savedProduct = repository.save(product);
        
        Long id = savedProduct.getProductId();

        // Act
        boolean isDeleted = repository.deleteById(id);

        // Assert
        assertTrue(isDeleted);
        Optional<Product> retrieved = repository.findById(id);
        assertFalse(retrieved.isPresent(), "Deleted product should not be found (is_current = false)");
    }
}

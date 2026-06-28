package com.aimsfx.repository.mapper;

import com.aimsfx.model.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ProductJdbcMapper - Strategy Pattern for Database Mapping
 * 
 * DESIGN PATTERN: Strategy Pattern
 * PURPOSE: Solves OCP violation in DatabaseProductRepository
 * 
 * PROBLEM SOLVED:
 * Before: DatabaseProductRepository had huge if-else chains for each product type
 * After: Each product type has its own mapper, registered in ProductMapperRegistry
 * 
 * BENEFITS:
 * - Open/Closed Principle: Add new product types without modifying existing code
 * - Single Responsibility: Each mapper handles one product type
 * - Testability: Each mapper can be tested independently
 * - Maintainability: Changes to one type don't affect others
 * 
 * USAGE:
 * ProductJdbcMapper<Book> mapper = ProductMapperRegistry.getMapper(ProductType.BOOK);
 * mapper.setAllTypeSpecificColumns(stmt, startIndex, book);
 * Book book = mapper.mapRow(rs);
 * 
 * @param <T> The specific product type this mapper handles
 */
public interface ProductJdbcMapper<T extends Product> {
    
    int TOTAL_TYPE_SPECIFIC_COLUMNS = 1; // It is just 1 column now (JSONB attributes)
    
    /**
     * Set the attributes column in PreparedStatement as JSONB
     * 
     * This method serializes the product's type-specific details into a JSON string
     * and sets it to the PreparedStatement.
     * 
     * This is the main Strategy method - each mapper serializes its own specific fields.
     * 
     * @param stmt The PreparedStatement to set parameters on
     * @param startIndex The starting parameter index (1-based)
     * @param product The product to extract data from
     * @return The next parameter index after setting ALL type-specific columns
     * @throws SQLException if database access error occurs
     */
    int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, T product) throws SQLException;
    
    /**
     * Create a product instance from ResultSet with type-specific fields
     * Only populates type-specific fields, common fields are handled separately
     * 
     * @param rs The ResultSet positioned at the current row
     * @return A new product instance with type-specific fields populated
     * @throws SQLException if database access error occurs
     */
    T mapRow(ResultSet rs) throws SQLException;
    
    /**
     * Populate type-specific fields from ResultSet into existing product
     * Used for reconstructing product from database without creating new instance
     * 
     * OCP PRINCIPLE: Repository uses this method without knowing product types
     * Each mapper populates its own fields, no switch-case needed in Repository
     * 
     * @param rs The ResultSet positioned at the current row
     * @param product The product instance to populate (must be correct type)
     * @throws SQLException if database access error occurs
     */
    void populateFromResultSet(ResultSet rs, T product) throws SQLException;
}

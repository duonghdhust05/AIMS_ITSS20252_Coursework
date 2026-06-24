package com.aims.product.repository;

import com.aims.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CONCEPT: Spring Data JpaRepository (@Repository)
 * ROLE (Local): Handles all database operations for the Product entity.
 * ROLE (Global): Acts as the Data Access Object (DAO) for the Microservice.
 * 
 * MEANING: By extending JpaRepository<Product, Long>, Spring Data JPA automatically 
 * generates the implementation for basic CRUD operations at runtime (save, findById, findAll, delete).
 * We NO LONGER need to write SQL queries or JDBC PreparedStatement code.
 * 
 * For custom queries, we can just declare the method name (e.g., findByBarcodeAndIsCurrentTrue) 
 * and Spring writes the SQL for us (Query Derivation).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByBarcodeAndIsCurrentTrue(String barcode);
}

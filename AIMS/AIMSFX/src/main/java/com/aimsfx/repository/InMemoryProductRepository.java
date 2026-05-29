//package com.aimsfx.repository;

//import com.aimsfx.model.Product;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;

//import java.util.Optional;

/**
 * InMemoryProductRepository - In-memory implementation of ProductRepository
 * 
 * DESIGN PATTERN: Repository Pattern (Concrete Implementation)
 * STORAGE: In-memory using ObservableList (suitable for JavaFX applications)
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only manages product data storage in memory
 * - LSP: Can be used wherever ProductRepository is expected
 * - DIP: Implements abstraction, can be replaced with database implementation
 * 
 * THREAD SAFETY: Not thread-safe (suitable for single-threaded JavaFX)
 * 
 * FUTURE: Can replace with DatabaseProductRepository without changing business logic
 */
//public class InMemoryProductRepository implements ProductRepository {
//
//    private final ObservableList<Product> products = FXCollections.observableArrayList();
//    private Long nextId = 1L;
//
//    @Override
//    public Product save(Product product) {
//        if (product.getProductId() == null) {
//            // New product - assign ID
//            product.setProductId(nextId++);
//            products.add(product);
//        } else {
//            // Update existing - replace in list
//            Optional<Product> existing = findById(product.getProductId());
//            if (existing.isPresent()) {
//                int index = products.indexOf(existing.get());
//                products.set(index, product);
//            } else {
//                // Product has ID but doesn't exist - add as new
//                products.add(product);
//                if (product.getProductId() >= nextId) {
//                    nextId = product.getProductId() + 1;
//                }
//            }
//        }
//        return product;
//    }
//
//    @Override
//    public Optional<Product> findById(Long id) {
//        return products.stream()
//                .filter(p -> p.getProductId() != null && p.getProductId().equals(id))
//                .findFirst();
//    }
//
//    @Override
//    public ObservableList<Product> findAll() {
//        return products;
//    }
//
//    @Override
//    public boolean deleteById(Long id) {
//        return products.removeIf(p -> p.getProductId() != null && p.getProductId().equals(id));
//    }
//
//    @Override
//    public boolean existsById(Long id) {
//        return products.stream()
//                .anyMatch(p -> p.getProductId() != null && p.getProductId().equals(id));
//    }
//
//    @Override
//    public ObservableList<Product> findHistoryByProductId(Long productId) {
//        // In-memory implementation doesn't support history tracking
//        // Returns only the current version if it exists
//        ObservableList<Product> history = FXCollections.observableArrayList();
//        findById(productId).ifPresent(history::add);
//        return history;
//    }
//
//    @Override
//    public boolean updateStock(Long productId, Integer newStock) {
//        Optional<Product> productOpt = findById(productId);
//        if (productOpt.isPresent()) {
//            Product product = productOpt.get();
//            product.setStock(newStock);
//            System.out.println(String.format("✅ Stock updated in memory: Product ID=%d, New Stock=%d",
//                    productId, newStock));
//            return true;
//        }
//        System.err.println(String.format("⚠️ No product found to update: Product ID=%d", productId));
//        return false;
//    }
//}

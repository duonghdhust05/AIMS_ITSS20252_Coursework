package com.aims.product.service;

import com.aims.product.model.Product;
import com.aims.product.model.StockUpdateRequest;
import com.aims.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Deduct stock atomically
     */
    @Transactional
    public boolean deductStockAtomically(Long productId, int quantity) {
        Product product = getProductById(productId);
        if (product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    /**
     * Restore stock atomically
     */
    @Transactional
    public boolean restoreStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        return true;
    }

    /**
     * Batch deduct stock for an order
     */
    @Transactional
    public boolean deductStockForOrder(List<StockUpdateRequest> items) {
        for (StockUpdateRequest item : items) {
            boolean success = deductStockAtomically(item.getProductId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("Insufficient stock for product " + item.getProductId());
            }
        }
        return true;
    }

    /**
     * Batch restore stock for an order
     */
    @Transactional
    public boolean restoreStockForOrder(List<StockUpdateRequest> items) {
        for (StockUpdateRequest item : items) {
            restoreStock(item.getProductId(), item.getQuantity());
        }
        return true;
    }
}

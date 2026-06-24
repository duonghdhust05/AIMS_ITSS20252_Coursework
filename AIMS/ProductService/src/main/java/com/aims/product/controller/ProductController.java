package com.aims.product.controller;

import com.aims.product.model.Product;
import com.aims.product.model.StockUpdateRequest;
import com.aims.product.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.existsById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteById(id));
    }

    @PostMapping("/deduct-stock")
    public ResponseEntity<Boolean> deductStock(@RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(productService.deductStockAtomically(request.getProductId(), request.getQuantity()));
    }

    @PostMapping("/restore-stock")
    public ResponseEntity<Boolean> restoreStock(@RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(productService.restoreStock(request.getProductId(), request.getQuantity()));
    }

    @PostMapping("/batch-deduct")
    public ResponseEntity<Boolean> batchDeductStock(@RequestBody List<StockUpdateRequest> requests) {
        try {
            boolean result = productService.deductStockForOrder(requests);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PostMapping("/batch-restore")
    public ResponseEntity<Boolean> batchRestoreStock(@RequestBody List<StockUpdateRequest> requests) {
        return ResponseEntity.ok(productService.restoreStockForOrder(requests));
    }
}

package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.model.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ViewProductController - READ operations for product data
 * Singleton Pattern, CQS (Query only)
 */
public class ViewProductController {

    private static ViewProductController instance;
    private IProductDataProvider dataProvider;

    private ViewProductController() {
        this.dataProvider = ProductController.getInstance();
    }
    
    public ViewProductController(IProductDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public static ViewProductController getInstance() {
        if (instance == null) {
            instance = new ViewProductController();
        }
        return instance;
    }
    
    public void setDataProvider(IProductDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public Map<String, Object> getProductDetail(String id) throws ProductNotFoundException {
        if (id == null || id.isBlank()) {
            throw new ProductNotFoundException("Product ID cannot be null or empty");
        }

        Long productId;
        try {
            productId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ProductNotFoundException("Invalid product ID format: " + id);
        }

        Product product = dataProvider.findById(productId);
        
        if (product == null) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }

        if (!validateData(product)) {
            throw new ProductNotFoundException("Product data is invalid for ID: " + id);
        }

        Map<String, Object> commonInfo = product.getCommonProductInfo();
        Map<String, Object> specificInfo = product.getSpecificDetail();
        String productType = checkProductType(product);

        Map<String, Object> result = mergeProductData(commonInfo, specificInfo);
        result.put("productType", productType);

        return result;
    }

    private String checkProductType(Product product) {
        return product.getProductType().name();
    }

    private boolean validateData(Product product) {
        if (product == null) {
            return false;
        }

        if (product.getTitle() == null || product.getTitle().isBlank()) {
            return false;
        }

        if (product.getBarcode() == null || product.getBarcode().isBlank()) {
            return false;
        }

        if (product.getCurrentPrice() != null && product.getCurrentPrice() < 0) {
            return false;
        }

        if (product.getOriginalPrice() != null && product.getOriginalPrice() < 0) {
            return false;
        }

        return true;
    }

    private Map<String, Object> mergeProductData(Map<String, Object> commonData,
            Map<String, Object> specificData) {
        Map<String, Object> merged = new HashMap<>();

        if (commonData != null) {
            merged.putAll(commonData);
        }

        if (specificData != null) {
            for (Map.Entry<String, Object> entry : specificData.entrySet()) {
                merged.put("specific_" + entry.getKey(), entry.getValue());
            }
        }

        return merged;
    }

    public Product findProductById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        Long productId;
        try {
            productId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }

        return dataProvider.findById(productId);
    }

    public String getProductTypeDisplay(Product product) {
        if (product == null) {
            return "Unknown";
        }

        ProductType type = product.getProductType();
        return switch (type) {
            case BOOK -> "Book";
            case CD -> "Compact Disc (CD)";
            case DVD -> "Digital Video Disc (DVD)";
            case NEWSPAPER -> "Newspaper";
        };
    }
}

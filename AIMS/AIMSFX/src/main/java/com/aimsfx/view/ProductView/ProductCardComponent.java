package com.aimsfx.view.ProductView;

import com.aimsfx.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.function.Consumer;

public class ProductCardComponent {
    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Button viewDetailsBtn;
    @FXML
    private Button addToCartBtn;

    @FXML
    private Label typeLabel;
    @FXML
    private Label stockLabel;

    public void setProductData(java.util.Map<String, Object> productData, java.util.function.Consumer<java.util.Map<String, Object>> onViewDetails, java.util.function.Consumer<java.util.Map<String, Object>> onAddToCart) {
        titleLabel.setText((String) productData.get("title"));
        
        Double currentPrice = (Double) productData.get("currentPrice");
        String priceText = currentPrice != null ? String.format("%,.0f VND", currentPrice) : "N/A";
        priceLabel.setText(priceText);

        String productType = (String) productData.getOrDefault("productType", "Unknown");
        String category = (String) productData.getOrDefault("category", "N/A");
        typeLabel.setText("Type: " + productType + " | Category: " + category);

        Integer stock = (Integer) productData.get("stock");
        stockLabel.setText(stock != null ? "Stock: " + stock : "Stock: N/A");

        viewDetailsBtn.setOnAction(e -> onViewDetails.accept(productData));
        addToCartBtn.setOnAction(e -> onAddToCart.accept(productData));
    }

    public void setProductData(Product product, Consumer<Product> onViewDetails, Consumer<Product> onAddToCart) {
        titleLabel.setText(product.getTitle() != null ? product.getTitle() : "Unknown");
        
        Double currentPrice = product.getCurrentPrice();
        String priceText = currentPrice != null ? String.format("%,.0f VND", currentPrice) : "N/A";
        priceLabel.setText(priceText);

        String productType = product.getClass().getSimpleName();
        String category = product.getCategory() != null ? product.getCategory() : "N/A";
        typeLabel.setText("Type: " + productType + " | Category: " + category);

        Integer stock = product.getStock();
        stockLabel.setText(stock != null ? "Stock: " + stock : "Stock: N/A");

        viewDetailsBtn.setOnAction(e -> onViewDetails.accept(product));
        addToCartBtn.setOnAction(e -> onAddToCart.accept(product));
    }
}
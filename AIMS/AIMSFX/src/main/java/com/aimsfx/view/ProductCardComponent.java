package com.aimsfx.view;

import com.aimsfx.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.function.Consumer;

public class ProductCardComponent {
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Button viewDetailsBtn;
    @FXML private Button addToCartBtn;

    // Pouring data and event handlers from Presenter
    public void setProductData(Product product, Consumer<Product> onViewDetails, Consumer<Product> onAddToCart) {
        titleLabel.setText(product.getTitle());
        priceLabel.setText(String.format("%,.0f VND", product.getCurrentPrice()));

        // Give permission to call Presenter methods when buttons are clicked
        viewDetailsBtn.setOnAction(e -> onViewDetails.accept(product));
        addToCartBtn.setOnAction(e -> onAddToCart.accept(product));
    }
}
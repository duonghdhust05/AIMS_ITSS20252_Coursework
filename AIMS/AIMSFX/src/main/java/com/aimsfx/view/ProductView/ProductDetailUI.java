package com.aimsfx.view.ProductView;

import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.model.*;
import com.aimsfx.view.BaseView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * ProductDetailUI displays detailed information about a single product
 */
public class ProductDetailUI extends BaseView {

    private final Stage parentStage;
    private Map<String, Object> productData;

    @javafx.fxml.FXML
    private Label titleLabel;
    @javafx.fxml.FXML
    private Label categoryLabel;
    @javafx.fxml.FXML
    private Label priceLabel;
    @javafx.fxml.FXML
    private Label weightLabel;
    @javafx.fxml.FXML
    private Label barcodeLabel;
    @javafx.fxml.FXML
    private Label statusLabel;
    @javafx.fxml.FXML
    private Label stockLabel;
    @javafx.fxml.FXML
    private Label dimensionsLabel;
    @javafx.fxml.FXML
    private GridPane specificDetailsPanel;
    @javafx.fxml.FXML
    private Label descriptionLabel;
    @javafx.fxml.FXML
    private Label vatRateLabel;
    @javafx.fxml.FXML
    private Button closeButton;
    @javafx.fxml.FXML
    private Button addToCartButton;

    private ICartManager cartManager = CartManager.getInstance();

    public void setCartManager(ICartManager cartManager) {
        this.cartManager = cartManager;
    }

    public ProductDetailUI(ViewProductController controller, Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void displayProduct(Map<String, Object> productData) {
        if (productData == null || productData.isEmpty()) {
            displayError("No product data to display");
            return;
        }

        this.productData = productData;

        Stage stage = new Stage();
        com.aimsfx.utils.UIUtils.applyAppIcon(stage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Product Details");

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/aimsfx/product-detail-ui-view.fxml"));
            loader.setController(this);
            BorderPane root = loader.load();

            populateProductData();

            if (closeButton != null) {
                closeButton.setOnAction(e -> stage.close());
            }
            if (addToCartButton != null) {
                addToCartButton.setOnAction(e -> handleAddToCart());
            }

            Scene scene = new Scene(root, 800, 700);
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            displayError("Failed to load product details UI.");
        }
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private void populateProductData() {
        if (productData == null) {
            return;
        }

        titleLabel.setText(getStringValue("title", "Untitled Product"));

        String productType = getStringValue("productType", "Unknown");
        String category = getStringValue("category", "N/A");
        categoryLabel.setText("Type: " + productType + " | Category: " + category);
        barcodeLabel.setText(getStringValue("barcode", "N/A"));

        Double currentPrice = (Double) productData.get("currentPrice");
        Double originalPrice = (Double) productData.get("originalPrice");
        String priceText = currentPrice != null ? String.format("%,.0f VND", currentPrice) : "N/A";
        if (originalPrice != null && !originalPrice.equals(currentPrice)) {
            priceText += String.format(" (Original: %,.0f VND)", originalPrice);
        }
        priceLabel.setText(priceText);
        Integer stock = (Integer) productData.get("stock");
        stockLabel.setText(stock != null ? stock.toString() : "N/A");

        statusLabel.setText(getStringValue("status", "Available"));

        Double weight = (Double) productData.get("weight");
        weightLabel.setText(weight != null ? weight + " kg" : "N/A");

        dimensionsLabel.setText(getStringValue("dimensions", "N/A"));

        Double vatRate = (Double) productData.get("vatRate");
        vatRateLabel.setText(vatRate != null ? (vatRate * 100) + "%" : "N/A");

        descriptionLabel.setText(getStringValue("description", "No description available"));

        populateSpecificDetails();
    }

    private void populateSpecificDetails() {
        specificDetailsPanel.getChildren().clear();
        int row = 0;

        String productType = getStringValue("productType", "UNKNOWN");

        switch (productType) {
            case "BOOK" -> {
                addSpecificField("Author:", "specific_author", row++);
                addSpecificField("Publisher:", "specific_publisher", row++);
                addSpecificField("Publication Date:", "specific_publicationDate", row++);
                addSpecificField("Number of Pages:", "specific_pages", row++);
                addSpecificField("Language:", "specific_language", row++);
                addSpecificField("Cover Type:", "specific_coverType", row++);
                addSpecificField("Genre:", "specific_genre", row++);
            }
            case "CD" -> {
                addSpecificField("Artist:", "specific_artist", row++);
                addSpecificField("Record Label:", "specific_recordLabel", row++);
                addSpecificField("Genre:", "specific_genre", row++);
                addSpecificField("Track Count:", "specific_trackCount", row++);
                addSpecificDateField("Release Date:", "specific_releaseDate", row++);
            }
            case "DVD" -> {
                addSpecificField("Director:", "specific_director", row++);
                addSpecificField("Studio:", "specific_studio", row++);
                addSpecificField("Genre:", "specific_genre", row++);
                addSpecificField("Duration (minutes):", "specific_duration", row++);
                addSpecificField("Disc Type:", "specific_discType", row++);
                addSpecificField("Subtitle:", "specific_subtitle", row++);
                addSpecificDateField("Release Date:", "specific_releaseDate", row++);
            }
            case "NEWSPAPER" -> {
                addSpecificField("ISSN:", "specific_issn", row++);
                addSpecificField("Publisher:", "specific_publisher", row++);
                addSpecificField("Editor-in-Chief:", "specific_editorInChief", row++);
                addSpecificDateField("Publication Date:", "specific_publicationDate", row++);
                addSpecificField("Frequency:", "specific_frequency", row++);
                addSpecificField("Language:", "specific_language", row++);
                addSpecificField("Section:", "specific_section", row++);
            }
            default -> {
                Label noDetailsLabel = new Label("No specific details available for this product type");
                noDetailsLabel.setStyle("-fx-text-fill: gray;");
                specificDetailsPanel.add(noDetailsLabel, 0, 0, 2, 1);
            }
        }
    }

    private void addSpecificField(String labelText, String dataKey, int row) {
        Label label = createFieldLabel(labelText);
        Label valueLabel = new Label(getStringValue(dataKey, "N/A"));

        valueLabel.setMaxWidth(400);

        specificDetailsPanel.add(label, 0, row);
        specificDetailsPanel.add(valueLabel, 1, row);
    }

    private void addSpecificDateField(String labelText, String dataKey, int row) {
        Label label = createFieldLabel(labelText);
        Object dateValue = productData.get(dataKey);
        String formattedDate = formatDate(dateValue);

        Label valueLabel = new Label(formattedDate);

        specificDetailsPanel.add(label, 0, row);
        specificDetailsPanel.add(valueLabel, 1, row);
    }

    private String getStringValue(String key, String defaultValue) {
        Object value = productData.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "N/A";
        }

        try

        {
            if (dateObj instanceof Date) {
                return new java.text.SimpleDateFormat("MMM dd, yyyy").format((Date) dateObj);
            } else if (dateObj instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                return ((LocalDateTime) dateObj).format(formatter);
            }
            return dateObj.toString();
        } catch (Exception e) {
            return dateObj.toString();
        }
    }

    private void handleAddToCart() {
        try {
            Object productId = productData.get("productId");
            if (productId == null) {
                displayError("Cannot add product: Product ID not found");
                return;
            }

            String idStr = productId.toString();
            Product product = ViewProductController.getInstance().findProductById(idStr);
            if (product == null) {
                displayError("Cannot add product: Product not found");
                return;
            }

            if (!product.checkAvailability(1)) {
                displayError("Product is out of stock");
                return;
            }

            Product cartProduct = product.copy();
            if (cartProduct == null) {
                displayError("Cannot add product: Unknown product type");
                return;
            }

            boolean added = cartManager.addProduct(cartProduct, 1);

            if (added) {
                CartEvents.notifyCartUpdated();
                displayInfo("Added to cart");
            } else {
                displayError("Failed to add product to cart");
            }
        } catch (Exception e) {
            displayError("Failed to add product to cart: " + e.getMessage());
        }
    }

}

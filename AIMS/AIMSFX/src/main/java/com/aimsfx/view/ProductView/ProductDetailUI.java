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

    private Label titleLabel;
    private Label categoryLabel;
    private Label priceLabel;
    private Label weightLabel;
    private Label barcodeLabel;
    private Label statusLabel;
    private Label stockLabel;
    private Label dimensionsLabel;
    private GridPane specificDetailsPanel;
    private Label descriptionLabel;

    private Label vatRateLabel;

    private ICartManager cartManager = CartManager.getInstance();

    public void setCartManager(ICartManager cartManager) {
        this.cartManager = cartManager;
    }

    public ProductDetailUI(ViewProductController controller, Stage parentStage) {
        this.parentStage = parentStage;
        initializeComponents();
    }

    private void initializeComponents() {
        titleLabel = new Label();
        categoryLabel = new Label();
        priceLabel = new Label();
        weightLabel = new Label();
        barcodeLabel = new Label();
        statusLabel = new Label();
        stockLabel = new Label();
        descriptionLabel = new Label();
        dimensionsLabel = new Label();

        vatRateLabel = new Label();

        specificDetailsPanel = new GridPane();
        specificDetailsPanel.setHgap(15);
        specificDetailsPanel.setVgap(10);
        specificDetailsPanel.setPadding(new Insets(10));
    }

    public void displayProduct(Map<String, Object> productData) {
        if (productData == null || productData.isEmpty()) {
            displayError("No product data to display");
            return;
        }

        this.productData = productData;

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Product Details");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox headerBox = createHeaderSection();
        root.setTop(headerBox);

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = createContentSection();
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        HBox buttonBox = createButtonSection(stage);
        root.setBottom(buttonBox);

        populateProductData();

        Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        headerBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20px;");

        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        HBox typeAndCategoryBox = new HBox(20);
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
        typeAndCategoryBox.getChildren().addAll(categoryLabel);

        headerBox.getChildren().addAll(titleLabel, typeAndCategoryBox, new Separator());
        return headerBox;
    }

    private VBox createContentSection() {
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));

        VBox commonSection = new VBox(10);

        Label commonTitle = new Label("Common Specifications");
        commonTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane commonGrid = createCommonInfoGrid();

        commonSection.getChildren().addAll(commonTitle, new Separator(), commonGrid);

        VBox specificSection = new VBox(10);

        Label specificTitle = new Label("Specific Specifications");
        specificTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        specificSection.getChildren().addAll(specificTitle, new Separator(), specificDetailsPanel);

        contentBox.getChildren().addAll(commonSection, specificSection);
        return contentBox;
    }

    private GridPane createCommonInfoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int row = 0;

        grid.add(createFieldLabel("Barcode:"), 0, row);
        grid.add(barcodeLabel, 1, row++);

        grid.add(createFieldLabel("Price:"), 0, row);
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        grid.add(priceLabel, 1, row++);

        grid.add(createFieldLabel("Stock:"), 0, row);
        grid.add(stockLabel, 1, row++);

        grid.add(createFieldLabel("Status:"), 0, row);
        grid.add(statusLabel, 1, row++);

        grid.add(createFieldLabel("Weight:"), 0, row);
        grid.add(weightLabel, 1, row++);

        grid.add(createFieldLabel("Dimensions:"), 0, row);
        grid.add(dimensionsLabel, 1, row++);

        grid.add(createFieldLabel("VAT Rate:"), 0, row);
        grid.add(vatRateLabel, 1, row++);

        grid.add(createFieldLabel("Description:"), 0, row);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        grid.add(descriptionLabel, 1, row++);

        return grid;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private HBox createButtonSection(Stage stage) {
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-padding: 10 30; -fx-font-size: 14px;");
        closeButton.setOnAction(e -> stage.close());

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; " +
                "-fx-padding: 10 30; -fx-font-size: 14px;");
        addToCartButton.setOnAction(e -> handleAddToCart());

        buttonBox.getChildren().addAll(addToCartButton, closeButton);
        return buttonBox;
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
            priceText += String.format(" (Original: %,.0f đ)", originalPrice);
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

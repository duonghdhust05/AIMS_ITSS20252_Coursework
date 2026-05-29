package com.aimsfx.view;

import com.aimsfx.controller.ViewProductController;
import com.aimsfx.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProductListUI provides a user interface for displaying and searching products
 * Extends BaseView for consistent error/info messaging
 */
public class ProductListUI extends BaseView {

    private final ViewProductController controller;
    private final Stage parentStage;
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private VBox productListPanel;
    private ObservableList<Product> displayedProducts;
    private Label productCountLabel;

    /**
     * Creates a ProductListUI with specified controller and parent stage
     * @param controller ViewProductController instance
     * @param parentStage Parent stage for modal dialog
     */
    public ProductListUI(ViewProductController controller, Stage parentStage) {
        this.controller = controller;
        this.parentStage = parentStage;
        this.displayedProducts = FXCollections.observableArrayList();
    }

    /**
     * Shows the product list window
     */
    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Product List");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top: Search and filter controls
        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Center: Product list
        ScrollPane scrollPane = new ScrollPane();
        productListPanel = new VBox(10);
        productListPanel.setPadding(new Insets(10));
        scrollPane.setContent(productListPanel);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Bottom: Product count
        HBox bottomPanel = new HBox();
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        productCountLabel = new Label("Products: 0");
        productCountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        bottomPanel.getChildren().add(productCountLabel);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        
        // Load initial products
        refreshProductList();
        
        stage.show();
    }

    /**
     * Creates the top panel with search and filter controls
     * @return VBox containing search and filter UI
     */
    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(0, 0, 15, 0));

        // Title
        Label titleLabel = new Label("Browse Products");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Enter product title or barcode...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> filterProducts());

        searchBox.getChildren().addAll(searchLabel, searchField, searchButton);

        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Type:");
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All", "Book", "CD", "DVD", "Newspaper");
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> filterProducts());

        Button clearButton = new Button("Clear Filters");
        clearButton.setOnAction(e -> clearFilters());

        filterBox.getChildren().addAll(filterLabel, filterComboBox, clearButton);

        topPanel.getChildren().addAll(titleLabel, searchBox, filterBox, new Separator());
        return topPanel;
    }

    /**
     * Displays a list of products in the UI
     * @param products List of maps containing product data
     */
    public void displayProductList(List<Map<String, Object>> products) {
        productListPanel.getChildren().clear();

        if (products == null || products.isEmpty()) {
            Label emptyLabel = new Label("No products found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            productListPanel.getChildren().add(emptyLabel);
            updateProductCount(0);
            return;
        }

        for (Map<String, Object> productData : products) {
            HBox productCard = createProductCard(productData);
            productListPanel.getChildren().add(productCard);
        }

        updateProductCount(products.size());
    }

    /**
     * Creates a product card UI component
     * @param productData Map containing product information
     * @return HBox representing a product card
     */
    private HBox createProductCard(Map<String, Object> productData) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; " +
                      "-fx-background-color: white; -fx-background-radius: 5; -fx-border-radius: 5;");

        // Product info section
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label((String) productData.get("title"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String productType = (String) productData.getOrDefault("productType", "Unknown");
        String category = (String) productData.getOrDefault("category", "N/A");
        Label typeLabel = new Label("Type: " + productType + " | Category: " + category);
        typeLabel.setStyle("-fx-text-fill: #666666;");

        Double currentPrice = (Double) productData.get("currentPrice");
        String priceText = currentPrice != null ? String.format("$%.2f", currentPrice) : "N/A";
        Label priceLabel = new Label("Price: " + priceText);
        priceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        Integer stock = (Integer) productData.get("stock");
        String stockText = stock != null ? "Stock: " + stock : "Stock: N/A";
        Label stockLabel = new Label(stockText);

        infoBox.getChildren().addAll(titleLabel, typeLabel, priceLabel, stockLabel);

        // Action buttons section
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewDetailButton = new Button("View Details");
        viewDetailButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; " +
                                  "-fx-padding: 10 20; -fx-font-size: 14px;");
        viewDetailButton.setOnAction(e -> {
            Object productId = productData.get("productId");
            if (productId != null) {
                handleViewDetailClick(productId.toString());
            }
        });

        buttonBox.getChildren().add(viewDetailButton);

        card.getChildren().addAll(infoBox, buttonBox);
        return card;
    }

    /**
     * Handles click event to view product details
     * @param productId The ID of the product to view
     */
    public void handleViewDetailClick(String productId) {
        try {
            Map<String, Object> productData = controller.getProductDetail(productId);
            ProductDetailUI detailUI = new ProductDetailUI(controller, 
                    (Stage) productListPanel.getScene().getWindow());
            detailUI.displayProduct(productData);
        } catch (Exception e) {
            displayError("Failed to load product details: " + e.getMessage());
        }
    }

    /**
     * Refreshes the product list from the controller
     */
    private void refreshProductList() {
        try {
            ObservableList<Product> allProducts = 
                    com.aimsfx.controller.ProductController.getInstance().getProducts();
            displayedProducts.setAll(allProducts);
            displayProducts(displayedProducts);
        } catch (Exception e) {
            displayError("Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Filters products based on search and filter criteria
     */
    private void filterProducts() {
        ObservableList<Product> allProducts = 
                com.aimsfx.controller.ProductController.getInstance().getProducts();
        
        String searchText = searchField.getText().toLowerCase().trim();
        String filterType = filterComboBox.getValue();

        List<Product> filtered = allProducts.stream()
                .filter(product -> {
                    // Filter by search text
                    boolean matchesSearch = searchText.isEmpty() ||
                            (product.getTitle() != null && 
                             product.getTitle().toLowerCase().contains(searchText)) ||
                            (product.getBarcode() != null && 
                             product.getBarcode().toLowerCase().contains(searchText));

                    // Filter by type
                    boolean matchesType = filterType.equals("All") ||
                            controller.getProductTypeDisplay(product).contains(filterType);

                    return matchesSearch && matchesType;
                })
                .collect(Collectors.toList());

        displayedProducts.setAll(filtered);
        displayProducts(displayedProducts);
    }

    /**
     * Clears all filters and search criteria
     */
    private void clearFilters() {
        searchField.clear();
        filterComboBox.setValue("All");
        refreshProductList();
    }

    /**
     * Displays products in the UI
     * @param products ObservableList of products to display
     */
    private void displayProducts(ObservableList<Product> products) {
        List<Map<String, Object>> productDataList = products.stream()
                .map(product -> {
                    try {
                        return controller.getProductDetail(product.getProductId().toString());
                    } catch (Exception e) {
                        // Return minimal data if detail fetch fails
                        Map<String, Object> basicData = product.getCommonProductInfo();
                        basicData.put("productType", controller.getProductTypeDisplay(product));
                        return basicData;
                    }
                })
                .collect(Collectors.toList());

        displayProductList(productDataList);
    }

    /**
     * Updates the product count label
     * @param count Number of products displayed
     */
    private void updateProductCount(int count) {
        productCountLabel.setText("Products: " + count);
    }
}

package com.aimsfx.view.ProductView;

import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.model.Product;
import com.aimsfx.view.BaseView;

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
    @javafx.fxml.FXML
    private TextField searchField;
    @javafx.fxml.FXML
    private ComboBox<String> filterComboBox;
    @javafx.fxml.FXML
    private javafx.scene.layout.FlowPane productListPanel;
    private ObservableList<Product> displayedProducts;
    @javafx.fxml.FXML
    private Label productCountLabel;
    @javafx.fxml.FXML
    private Button clearFilterButton;
    
    @javafx.fxml.FXML
    private Label refreshNotificationLabel;
    @javafx.fxml.FXML
    private Label lastUpdatedLabel;
    @javafx.fxml.FXML
    private Button refreshBtn;

    private java.util.concurrent.ScheduledExecutorService scheduler;
    private int lastLoadedCount = -1;
    private final java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
    private List<Product> allProductsCache = new java.util.ArrayList<>();

    public ProductListUI(ViewProductController controller, Stage parentStage) {
        this.controller = controller;
        this.parentStage = parentStage;
        this.displayedProducts = FXCollections.observableArrayList();
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Product List");

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/aimsfx/product-list-ui-view.fxml"));
            loader.setController(this);
            BorderPane root = loader.load();

            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());
            filterComboBox.setOnAction(e -> filterProducts());
            
            startBackgroundPolling();
            
            stage.setOnCloseRequest(e -> {
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdownNow();
                }
            });
            if (clearFilterButton != null) {
                clearFilterButton.setOnAction(e -> clearFilters());
            }

            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);

            refreshProductList();
            
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            displayError("Failed to load product list UI.");
        }
    }

    @javafx.fxml.FXML
    private void handleRefresh() {
        if (refreshNotificationLabel != null) {
            refreshNotificationLabel.setVisible(false);
            refreshNotificationLabel.setManaged(false);
        }
        refreshProductList();
    }

    private void startBackgroundPolling() {
        scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Here we just check the count of products in DB
                int currentCount = com.aimsfx.controller.ProductManagerController.ProductController.getInstance().getProducts().size(); // Or a specific count method if available
                if (lastLoadedCount != -1 && currentCount != lastLoadedCount) {
                    javafx.application.Platform.runLater(() -> {
                        if (refreshNotificationLabel != null) {
                            refreshNotificationLabel.setVisible(true);
                            refreshNotificationLabel.setManaged(true);
                        }
                    });
                }
            } catch (Exception e) {
                // Ignore background polling errors
            }
        }, 15, 15, java.util.concurrent.TimeUnit.SECONDS);
    }

    /* 
     * 
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
            javafx.scene.Node productCard = createProductCard(productData);
            productListPanel.getChildren().add(productCard);
        }

        updateProductCount(products.size());
    }

    /* 
     * 
     * Creates a product card UI component
     * @param productData Map containing product information
     * @return HBox representing a product card
     */
    private javafx.scene.Node createProductCard(Map<String, Object> productData) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/aimsfx/product-card.fxml"));
            javafx.scene.layout.VBox card = loader.load();
            ProductCardComponent cardComponent = loader.getController();

            cardComponent.setProductData(
                productData,
                (pd) -> {
                    Object productId = pd.get("productId");
                    if (productId != null) {
                        handleViewDetailClick(productId.toString());
                    }
                },
                (pd) -> {
                    // Logic to add to cart from list (if needed)
                    // Currently ProductListUI doesn't implement add to cart directly, so we can leave it empty or trigger an event
                }
            );

            return card;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            displayError("Failed to load product card");
            return new VBox();
        }
    }

    /* 
     * 
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
    public void refreshProductList() {
        try {
            List<Product> products = com.aimsfx.controller.ProductManagerController.ProductController.getInstance().getProducts();
            this.lastLoadedCount = products.size();
            this.allProductsCache = new java.util.ArrayList<>(products);
            
            if (lastUpdatedLabel != null) {
                javafx.application.Platform.runLater(() -> {
                    lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().format(timeFmt));
                });
            }
            
            displayedProducts.setAll(products);
            filterProducts(); // Re-apply existing filters
        } catch (Exception e) {
            displayError("Failed to refresh product list: " + e.getMessage());
        }
    }

    /**
     * Filters products based on search and filter criteria
     */
    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterType = filterComboBox.getValue();

        List<Product> filtered = allProductsCache.stream()
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

    /* 
     * 
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

    /* 
     * 
     * Updates the product count label
     * @param count Number of products displayed
     */
    private void updateProductCount(int count) {
        productCountLabel.setText("Products: " + count);
    }
}

package com.aimsfx.view;

import com.aimsfx.model.*;
import com.aimsfx.controller.ProductController;
import com.aimsfx.controller.ViewProductController;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.utils.SessionManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ProductListView implements Initializable {

    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, Boolean> selectColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> titleColumn;
    @FXML private TableColumn<Product, String> typeColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    @FXML private Button addProductButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Label selectionCountLabel;

    private static final int MAX_SELECTION = 10;
    private ProductController productController;
    private ViewProductController viewProductController;
    private Runnable onProductUpdatedCallback;
    
    // Track selected products
    private Map<Long, SimpleBooleanProperty> selectionMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productController = ProductController.getInstance();
        viewProductController = ViewProductController.getInstance();
        setupTableColumns();
        setupSelectColumn();
        setupActionsColumn();
        loadProducts();
    }

    private void setupTableColumns() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        // Custom cell value factory for product type
        typeColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String type;
            if (product instanceof Book) {
                type = "Book";
            } else if (product instanceof CD) {
                type = "CD";
            } else if (product instanceof DVD) {
                type = "DVD";
            } else if (product instanceof Newspaper) {
                type = "Newspaper";
            } else {
                type = "Unknown";
            }
            return new SimpleStringProperty(type);
        });
    }
    
    private void setupSelectColumn() {
        selectColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            Long productId = product.getProductId();
            
            // Create or get existing property for this product
            if (!selectionMap.containsKey(productId)) {
                SimpleBooleanProperty prop = new SimpleBooleanProperty(false);
                prop.addListener((obs, wasSelected, isSelected) -> {
                    handleSelectionChange(productId, isSelected);
                });
                selectionMap.put(productId, prop);
            }
            return selectionMap.get(productId);
        });
        
        selectColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        productTableView.setEditable(true);
        selectColumn.setEditable(true);
    }
    
    private void handleSelectionChange(Long productId, boolean isSelected) {
        int selectedCount = getSelectedCount();
        
        // Prevent selecting more than MAX_SELECTION
        if (isSelected && selectedCount > MAX_SELECTION) {
            // Revert the selection
            SimpleBooleanProperty prop = selectionMap.get(productId);
            if (prop != null) {
                Platform.runLater(() -> prop.set(false));
            }
            showAlert("Selection Limit", "You can only select up to " + MAX_SELECTION + " products at a time.");
            return;
        }
        
        updateSelectionUI();
    }
    
    private int getSelectedCount() {
        return (int) selectionMap.values().stream().filter(SimpleBooleanProperty::get).count();
    }
    
    private List<Product> getSelectedProducts() {
        List<Product> selected = new ArrayList<>();
        for (Product product : productTableView.getItems()) {
            SimpleBooleanProperty prop = selectionMap.get(product.getProductId());
            if (prop != null && prop.get()) {
                selected.add(product);
            }
        }
        return selected;
    }
    
    private void updateSelectionUI() {
        int count = getSelectedCount();
        selectionCountLabel.setText("Selected: " + count + "/" + MAX_SELECTION);
        deleteSelectedButton.setDisable(count == 0);
    }
    
    private void clearSelections() {
        selectionMap.values().forEach(prop -> prop.set(false));
        updateSelectionUI();
    }

    private void setupActionsColumn() {
        if (actionsColumn != null) {
            actionsColumn.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
                private final javafx.scene.control.Button viewDetailsBtn = new javafx.scene.control.Button("View Details");
                private final javafx.scene.control.Button updateBtn = new javafx.scene.control.Button("Update");
                private final javafx.scene.control.Button historyBtn = new javafx.scene.control.Button("History");
                private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5);
                
                {
                    viewDetailsBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 12;");
                    updateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12;");
                    historyBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 12;");
                    
                    viewDetailsBtn.setOnAction(event -> {
                        Product product = getTableView().getItems().get(getIndex());
                        if (product != null && product.getProductId() != null) {
                            handleViewDetailClick(product.getProductId().toString());
                        }
                    });
                    
                    updateBtn.setOnAction(event -> {
                        Product product = getTableView().getItems().get(getIndex());
                        if (product != null && product.getProductId() != null) {
                            handleUpdateProduct(product.getProductId());
                        }
                    });
                    
                    historyBtn.setOnAction(event -> {
                        Product product = getTableView().getItems().get(getIndex());
                        if (product != null && product.getProductId() != null) {
                            handleViewHistory(product.getProductId());
                        }
                    });
                    
                    buttonBox.getChildren().addAll(viewDetailsBtn, updateBtn, historyBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });
        }
    }
    
    private void loadProducts() {
        productTableView.setItems(productController.getProducts());
        productTableView.refresh(); // Force UI refresh to show updated data
    }

    /**
     * Handles click event to view product details
     * @param productId The ID of the product to view
     */
    public void handleViewDetailClick(String productId) {
        try {
            java.util.Map<String, Object> productData = viewProductController.getProductDetail(productId);
            
            Stage currentStage = (Stage) productTableView.getScene().getWindow();
            ProductDetailUI detailUI = new ProductDetailUI(viewProductController, currentStage);
            detailUI.displayProduct(productData);
        } catch (ProductNotFoundException e) {
            showAlert("Error", "Product not found: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to load product details: " + e.getMessage());
        }
    }
    
    // Add new method for Add Product functionality
    @FXML
    private void onAddProduct() {
        // Check if user is logged in and has permission
        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isLoggedIn() || !sessionManager.canManageProducts()) {
            showAlert("Access Denied", "You need Product Manager role to manage products");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.initModality(Modality.WINDOW_MODAL);
            
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(700);

            // Get controller and set callback
            ProductFormView controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setOnProductAdded(() -> {
          	refreshProductList(); // Let the success dialog handle closing the add product form window	
				
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open product creation dialog.");
        }
    }

    /**
     * Handles update product action
     * @param productId The ID of the product to update
     */
    public void handleUpdateProduct(Long productId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-update-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(productTableView.getScene().getWindow());
            stage.setScene(new Scene(root, 900, 650));
            stage.setResizable(true);
            stage.setMinWidth(850);
            stage.setMinHeight(600);

            // Get controller and set product data
            UpdateProductFormView controller = loader.getController();
            controller.setProductData(productId);
            controller.setOnProductUpdated(() -> {
                refreshProductList();
                // Let the success dialog handle closing the update form window
                Platform.runLater(() -> {
                    stage.close();
                });
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open product update dialog: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update product: " + e.getMessage());
        }
    }
    
    /**
     * Handle view history button click - opens history dialog
     */
    private void handleViewHistory(Long productId) {
        try {
            ProductHistoryView historyView = new ProductHistoryView();
            Stage ownerStage = (Stage) productTableView.getScene().getWindow();
            historyView.show(productId, ownerStage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load product history: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete selected products button
     * Delegates validation to service layer
     */
    @FXML
    private void handleDeleteSelected() {
        List<Product> selectedProducts = getSelectedProducts();
        
        if (selectedProducts.isEmpty()) {
            showAlert("No Selection", "Please select at least one product to delete.");
            return;
        }
        
        // Check if user is logged in
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in.");
            return;
        }
        
        Long userId = currentUser.getUserId();
        
        try {
            // Call service layer for validation and deletion
            int deletedCount = productController.deleteMultipleProducts(selectedProducts, userId);
            
            // Get remaining quota after deletion
            int remainingQuota = productController.getRemainingDeletionQuota(userId);
            
            // Show result with remaining quota and refresh
            clearSelections();
            refreshProductList();
            showSuccess("Deletion Successful", 
                    "Successfully processed " + deletedCount + " product(s).\n" +
                    "Products with stock = 0 were deleted. Products with stock > 0 were deactivated.\n\n" +
                    "Remaining deletion quota for today: " + remainingQuota + "/20");
            
        } catch (com.aimsfx.exception.BulkDeleteValidationException e) {
            if (e.getErrorType() == com.aimsfx.exception.BulkDeleteValidationException.ErrorType.QUOTA_EXCEEDED) {
                String message = "Cannot delete " + e.getRequestedCount() + " products.\n\n"
                        + "Your remaining daily deletion quota is: " + e.getRemainingQuota() + "\n"
                        + "Daily limit: 20 products per day.\n\n"
                        + "Please try again tomorrow or select fewer products.";
                showWarning("Deletion Limit Exceeded", message);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to delete products: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void refreshProductList() {
        selectionMap.clear(); // Clear old selections when refreshing
        loadProducts();
        updateSelectionUI();
        // Trigger callback to notify parent that products have been updated
        if (onProductUpdatedCallback != null) {
            onProductUpdatedCallback.run();
        }
    }
    
    /**
     * Sets the callback to be executed when a product is updated
     * @param callback The callback to execute
     */
    public void setOnProductUpdated(Runnable callback) {
        this.onProductUpdatedCallback = callback;
    }
}
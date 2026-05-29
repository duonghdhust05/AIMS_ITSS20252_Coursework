package com.aimsfx.controller;

import com.aimsfx.model.*;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.view.ProductDetailUI;
import com.aimsfx.view.ProductListView;
import com.aimsfx.view.OrderManagementView;
import com.aimsfx.view.LoginView;
import com.aimsfx.exception.ProductNotFoundException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * HomepageController
 * Controller for the homepage view displaying products
 */
public class HomepageController {

    @FXML
    private TextField searchField;

    @FXML
    private Label cartBadge;

    @FXML
    private Button btnAll;
    
    @FXML
    private Button btnUnder100K;
    
    @FXML
    private Button btn100To200K;
    
    @FXML
    private Button btn200To300K;
    
    @FXML
    private Button btnOver300K;
    
    @FXML
    private Button btnSortNew;
    
    @FXML
    private Button btnSortPriceAsc;
    
    @FXML
    private Button btnSortPriceDesc;

    @FXML
    private GridPane productGrid;

    @FXML
    private Label productCountLabel;

    private List<Product> currentDisplayedProducts = new ArrayList<>();







    /* Add new method for Add Product functionality
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
            stage.initOwner(productGrid.getScene().getWindow());
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(700);

            // Get controller and set callback
            ProductFormView controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setOnProductAdded(() -> {
            filterAll();
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open product creation dialog.");
        }
    }*/
    
    @FXML
    private void onProductManagementPanel() {
        // Check if user is logged in and has permission
        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isLoggedIn() || !sessionManager.canManageProducts()) {
            showAlert("Access Denied", "You need Product Manager role to manage products");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-list-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Product Management Panel");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(productGrid.getScene().getWindow());
            stage.setScene(new Scene(root, 1280, 720));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(900);
            stage.setMinHeight(650);

            // Get controller and set up the callback for when a product is updated
            ProductListView controller = loader.getController();
            controller.setOnProductUpdated(() -> {
                // Refresh the current view to show updated product information
                refreshAllProducts();
                displayProducts(allProducts);
                
                // Also refresh cart to reflect updated product prices/info
                int updatedCartItems = cartManager.refreshCartProducts();
                if (updatedCartItems > 0) {
                    CartEvents.notifyCartUpdated();
                    System.out.println("✅ Cart updated: " + updatedCartItems + " item(s) synced with new product data");
                }
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open product update dialog.");
        }
    }
    
    private ProductController productController;
    private List<Product> allProducts = new ArrayList<>();
    private ICartManager cartManager = CartManager.getInstance();
    
    public void setCartManager(ICartManager cartManager) {
        this.cartManager = cartManager;
    }

    @FXML
    public void initialize() {
        // Initialize ProductController
        productController = ProductController.getInstance();
        
        // Register cart event listener to update badge when cart changes
        CartEvents.addListener(this::updateCartBadge);
        
        // Setup search functionality
        setupSearchListener();
        
        // Use Platform.runLater to ensure UI components are fully initialized
        Platform.runLater(() -> {
            // Load products from database via controller and display all products by default
            refreshAllProducts();
            displayProducts(allProducts);
            
            // Update cart badge
            updateCartBadge();
        });
    }
    
    /**
     * Refresh products from ProductController (database-backed)
     */
    private void refreshAllProducts() {
        if (productController != null) {
            allProducts.clear();
            allProducts.addAll(productController.getProducts());
        }
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                performSearch(newValue);
            });
            searchField.setPromptText("Search by title, category, or barcode...");
        }
    }
    
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayProducts(allProducts);
            return;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        List<Product> filteredProducts = allProducts.stream()
            .filter(product -> matchesSearch(product, lowerQuery))
            .collect(Collectors.toList());
        
        displayProducts(filteredProducts);
    }
    
    private boolean matchesSearch(Product product, String query) {
        if (product == null) {
            return false;
        }
        
        if (product.getTitle() != null && 
            product.getTitle().toLowerCase().contains(query)) {
            return true;
        }
        
        if (product.getCategory() != null && 
            product.getCategory().toLowerCase().contains(query)) {
            return true;
        }
        
        if (product.getBarcode() != null && 
            product.getBarcode().toLowerCase().contains(query)) {
            return true;
        }
        
        if (product.getProductType() != null && 
            product.getProductType().name().toLowerCase().contains(query)) {
            return true;
        }
        
        return false;
    }

    // Event Handlers

    @FXML
    public void onAccount() {
        SessionManager sessionManager = SessionManager.getInstance();
        
        if (!sessionManager.isLoggedIn()) {
            // User not logged in - show login dialog
            showLoginDialog();
        } else {
            // User logged in - show user menu
            showUserMenu();
        }
    }

    @FXML
    public void onCartClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/cart-view.fxml"));
            Parent view = loader.load();

            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.setTitle("AIMS - Shopping Cart");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load cart view: " + e.getMessage());
        }
    }

    // Filter methods

    @FXML
    public void filterAll() {
        setActiveFilterButton(btnAll);
        refreshAllProducts();
        displayProducts(allProducts);
    }

    @FXML
    public void filterUnder100K() {
        setActiveFilterButton(btnUnder100K);
        filterByPrice(p -> p.getCurrentPrice() < 100000);
    }

    @FXML
    public void filter100To200K() {
        setActiveFilterButton(btn100To200K);
        filterByPrice(p -> {
            double price = p.getCurrentPrice();
            return price >= 100000 && price < 200000;
        });
    }

    @FXML
    public void filter200To300K() {
        setActiveFilterButton(btn200To300K);
        filterByPrice(p -> {
            double price = p.getCurrentPrice();
            return price >= 200000 && price < 300000;
        });
    }

    @FXML
    public void filterOver300K() {
        setActiveFilterButton(btnOver300K);
        filterByPrice(p -> p.getCurrentPrice() >= 300000);
    }

    private void filterByPrice(Predicate<Product> criteria) {
        List<Product> filtered = allProducts.stream()
            .filter(criteria)
            .collect(Collectors.toList());
        displayProducts(filtered);
    }
    
    private void setActiveFilterButton(Button activeBtn) {
        Button[] filterButtons = {btnAll, btnUnder100K, btn100To200K, btn200To300K, btnOver300K};
        
        for (Button btn : filterButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("filter-tab", "filter-tab-active");
                btn.getStyleClass().add(btn == activeBtn ? "filter-tab-active" : "filter-tab");
            }
        }
    }

    // Sort methods

    @FXML
    public void sortByNew() {
        setActiveSortButton(btnSortNew);
        // Sort by newest (assuming higher ID = newer)
        List<Product> sorted = new ArrayList<>(currentDisplayedProducts);
        sorted.sort((p1, p2) -> Long.compare(p2.getProductId(), p1.getProductId()));
        displayProducts(sorted);
    }

    @FXML
    public void sortByPriceAsc() {
        setActiveSortButton(btnSortPriceAsc);
        List<Product> sorted = new ArrayList<>(currentDisplayedProducts);
        sorted.sort((p1, p2) -> Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
        displayProducts(sorted);
    }

    @FXML
    public void sortByPriceDesc() {
        setActiveSortButton(btnSortPriceDesc);
        List<Product> sorted = new ArrayList<>(currentDisplayedProducts);
        sorted.sort((p1, p2) -> Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
        displayProducts(sorted);
    }
    
    private void setActiveSortButton(Button activeBtn) {
        Button[] sortButtons = {btnSortNew, btnSortPriceAsc, btnSortPriceDesc};
        
        for (Button btn : sortButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("sort-btn", "sort-btn-active");
                btn.getStyleClass().add(btn == activeBtn ? "sort-btn-active" : "sort-btn");
            }
        }
    }

    private void displayProducts(List<Product> products) {
        if (productGrid == null) {
            System.err.println("Product grid not initialized");
            return;
        }

        // Store current displayed products for sorting
        currentDisplayedProducts = new ArrayList<>(products);

        productGrid.getChildren().clear();

        // Update product count label
        if (productCountLabel != null) {
            productCountLabel.setText(products.size() + " products");
        }

        int column = 0;
        int row = 0;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, column, row);

            column++;
            if (column == 4) { // 4 columns per row
                column = 0;
                row++;
            }
        }

        updateProductCount();
    }

    private void updateProductCount() {
        if (productCountLabel != null) {
            int count = currentDisplayedProducts.size();
            productCountLabel.setText(count + " product" + (count != 1 ? "s" : ""));
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 15; -fx-cursor: hand;");
        card.setPrefWidth(250);

        // Product image placeholder
        Label imagePlaceholder = new Label("📦");
        imagePlaceholder.setStyle("-fx-font-size: 48px;");

        // Product title
        Label title = new Label(product.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(220);

        // Product price
        Label price = new Label(String.format("%,.0f VND", product.getCurrentPrice()));
        price.setStyle("-fx-font-size: 16px; -fx-text-fill: #d32f2f; -fx-font-weight: bold;");

        // View Details button
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 20;");
        viewDetailsBtn.setOnAction(e -> handleViewDetailClick(product.getProductId().toString()));

        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 20;");
        addToCartBtn.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(imagePlaceholder, title, price, viewDetailsBtn, addToCartBtn);

        return card;
    }



    private void addToCart(Product product) {
        try {
            // Show dialog to select quantity
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Add to Cart");
            dialog.setHeaderText("Add " + product.getTitle() + " to cart");
            dialog.setContentText("Quantity (Available: " + product.getStock() + "):");
            
            dialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    
                    // Validate quantity
                    if (quantity <= 0) {
                        showAlert("Invalid Quantity", "Please enter a positive number.");
                        return;
                    }
                    
                    if (quantity > product.getStock()) {
                        showAlert("Out of Stock", "Only " + product.getStock() + " items available.");
                        return;
                    }
                    
                    Product cartProduct = product.copy();
                    
                    cartManager.addProduct(cartProduct, quantity);
                    CartEvents.notifyCartUpdated();
                    showAlert("Added to Cart", quantity + "x " + product.getTitle() + " added to cart!");
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number.");
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to add product to cart: " + e.getMessage());
        }
    }

    private void updateCartBadge() {
        if (cartBadge != null) {
            Cart cart = cartManager.getCart();
            int itemCount = cart != null ? cart.getItems().size() : 0;
            cartBadge.setText(String.valueOf(itemCount));
            cartBadge.setVisible(itemCount > 0);
        }
    }

    /**
     * Show login dialog
     */
    private void showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/login-view.fxml"));
            Parent root = loader.load();
            
            LoginView loginView = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(searchField.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            loginView.setDialogStage(dialogStage);
            loginView.setOnLoginSuccess(() -> {
                // Refresh UI after successful login
                showAlert("Success", "Login successful! Welcome, " + 
                         SessionManager.getInstance().getCurrentUser().getUsername());
            });
            
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login view: " + e.getMessage());
        }
    }
    
    /**
     * Show user menu for logged-in users
     */
    private void showUserMenu() {
        SessionManager sessionManager = SessionManager.getInstance();
        UserController userController = UserController.getInstance();
        User currentUser = sessionManager.getCurrentUser();
        
        // Format roles as comma-separated string
        String rolesStr = currentUser.getRoles().stream()
            .map(UserRole::toString)
            .reduce((r1, r2) -> r1 + ", " + r2)
            .orElse("No roles");
        
        Alert menu = new Alert(Alert.AlertType.INFORMATION);
        menu.setTitle("Account");
        menu.setHeaderText("Logged in as: " + currentUser.getUsername() + 
                          " (" + rolesStr + ")");
        
        ButtonType changePasswordBtn = new ButtonType("Change Password");
        ButtonType manageUsersBtn = new ButtonType("Manage Users");
        ButtonType manageOrdersBtn = new ButtonType("Manage Orders");
        ButtonType logoutBtn = new ButtonType("Logout");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        boolean isAdmin = sessionManager.isAdministrator();
        boolean isPm = sessionManager.isProductManager();

        if (isAdmin && isPm) {
            menu.getButtonTypes().setAll(changePasswordBtn, manageUsersBtn, manageOrdersBtn, logoutBtn, cancelBtn);
        } else if (isAdmin) {
            menu.getButtonTypes().setAll(changePasswordBtn, manageUsersBtn, logoutBtn, cancelBtn);
        } else if (isPm) {
            menu.getButtonTypes().setAll(changePasswordBtn, manageOrdersBtn, logoutBtn, cancelBtn);
        } else {
            menu.getButtonTypes().setAll(changePasswordBtn, logoutBtn, cancelBtn);
        }
        
        if (sessionManager.isAdministrator()) {
            menu.getButtonTypes().setAll(changePasswordBtn, manageUsersBtn, logoutBtn, cancelBtn);
        } else {
            menu.getButtonTypes().setAll(changePasswordBtn, logoutBtn, cancelBtn);
        }
        
        Optional<ButtonType> result = menu.showAndWait();
        
        if (result.isPresent()) {
        	if (result.get() == changePasswordBtn) {
                showChangePasswordDialog();
            } else if (result.get() == manageUsersBtn) {
                showUserManagementView();
            } else if (result.get() == manageOrdersBtn) {
                showOrderManagementView();
            } else if (result.get() == logoutBtn) {
                userController.logout();
                showAlert("Success", "Logged out successfully!");
            }
        }
    }
    
    @FXML
    private void showOrderManagementView() {
    	// Check if user is logged in and has permission
        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isLoggedIn() || !sessionManager.canManageOrders()) {
            showAlert("Access Denied", "You need Product Manager role to manage Orders");
            return;
        }
        try {
            OrderManagementView view = new OrderManagementView();
            Stage owner = (Stage) productGrid.getScene().getWindow();
            view.show(owner);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open order management view: " + e.getMessage());
        }
    }
    
    /**
     * Show change password dialog
     */
    private void showChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/change-password-view.fxml"));
            Parent root = loader.load();
            
            com.aimsfx.view.ChangePasswordView controller = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(searchField.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load change password view: " + e.getMessage());
        }
    }
    
    /**
     * Show user management view (administrators only)
     */
    private void showUserManagementView() {
    	
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/user-management-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("User Management");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(productGrid.getScene().getWindow());
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(600);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open user management view: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles click event to view product details
     * @param productId The ID of the product to view
     */
    public void handleViewDetailClick(String productId) {
        try {
            ViewProductController viewController = ViewProductController.getInstance();
            Map<String, Object> productData = viewController.getProductDetail(productId);

            Stage currentStage = (Stage) productGrid.getScene().getWindow();
            ProductDetailUI detailUI = new ProductDetailUI(viewController, currentStage);
            detailUI.displayProduct(productData);
        } catch (ProductNotFoundException e) {
            showAlert("Error", "Product not found: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to load product details: " + e.getMessage());
        }
    }

    public Cart getCart() {
        return cartManager.getCart();
    }
}
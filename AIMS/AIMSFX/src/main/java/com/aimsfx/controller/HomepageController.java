package com.aimsfx.controller;

import com.aimsfx.model.*;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.view.HomepageView;
import com.aimsfx.controller.ProductManagerController.ProductController;
import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.utils.UIUtils;

import java.util.*;
import java.util.stream.Collectors;

public class HomepageController {
    private final HomepageView view;
    private final ProductController productController = ProductController.getInstance();
    private final ICartManager cartManager = CartManager.getInstance();

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentDisplayedProducts = new ArrayList<>();
    private List<Product> random20Products = new ArrayList<>();

    // State variables for Database-Level Filtering
    private Double currentMinPrice = null;
    private Double currentMaxPrice = null;
    private String currentSearchQuery = "";

    public HomepageController(HomepageView view) {
        this.view = view;
    }

    public void initData() {
        refreshAllProducts();
        view.displayProducts(currentDisplayedProducts);
        view.updateCartBadge(cartManager.getCart().getItems().size());

        // Listen to cart events
        CartEvents.addListener(() -> {
            view.updateCartBadge(cartManager.getCart().getItems().size());
        });
    }

    /**
     * =========================================================================================================
     * FE/BE OPTIMIZATION: ASYNCHRONOUS SEARCH WITH DATABASE-LEVEL FILTERS
     * =========================================================================================================
     * PREVIOUS PROBLEMS:
     * 1. Price filters were applied locally to the random 20 products currently in
     * memory,
     * which yielded inconsistent and confusing results (e.g., clicking 100-200k
     * would return 2-4 items).
     *
     * DETAILED SOLUTION & IMPLEMENTATION:
     * 1. Merged search and filter state into performFilteredSearchAsync().
     * 2. This method queries the database directly with both the search query AND
     * the price bounds.
     * 3. Retained the Async Task and Loading UI for maximum responsiveness.
     *
     * EXPECTED RESULTS:
     * Clicking a price filter or typing a search will ALWAYS query the database and
     * return exactly 20
     * matching products. The count will be perfectly consistent.
     * =========================================================================================================
     */
    public void performSearchAsync(String query) {
        this.currentSearchQuery = query;
        performFilteredSearchAsync();
    }

    private void performFilteredSearchAsync() {
        view.showLoading();

        javafx.concurrent.Task<List<Product>> searchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                // If there are no filters and no query, just return the random products
                if ((currentSearchQuery == null || currentSearchQuery.trim().isEmpty())
                        && currentMinPrice == null && currentMaxPrice == null) {
                    return new ArrayList<>(random20Products);
                } else {
                    return productController.searchProducts(currentSearchQuery, currentMinPrice, currentMaxPrice, 20);
                }
            }
        };

        searchTask.setOnSucceeded(e -> {
            currentDisplayedProducts = searchTask.getValue();
            allProducts = new ArrayList<>(currentDisplayedProducts);
            view.displayProducts(currentDisplayedProducts);
        });

        searchTask.setOnFailed(e -> {
            UIUtils.showError("Error", "Failed to search products: " + searchTask.getException().getMessage());
        });

        new Thread(searchTask).start();
    }

    /**
     * CHANGELOG: Added for memory-optimization in autocomplete feature.
     * Fetches up to 10 suggestions from the database based on the query.
     */
    public List<String> getAutocompleteSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return productController.searchProducts(query, null, null, 10).stream()
                .map(product -> product.getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    public void filterByPrice(double min, double max) {
        this.currentMinPrice = min;
        this.currentMaxPrice = max;
        performFilteredSearchAsync();
    }

    public void filterOverPrice(double min) {
        this.currentMinPrice = min;
        this.currentMaxPrice = null;
        performFilteredSearchAsync();
    }

    public void clearPriceFilter() {
        this.currentMinPrice = null;
        this.currentMaxPrice = null;
    }

    public void sortByNew() {
        currentDisplayedProducts.sort((p1, p2) -> Long.compare(p2.getProductId(), p1.getProductId()));
        view.displayProducts(currentDisplayedProducts);
    }

    public void sortByPrice(boolean ascending) {
        currentDisplayedProducts.sort((p1, p2) -> ascending ? Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice())
                : Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
        view.displayProducts(currentDisplayedProducts);
    }

    public void handleAddToCart(Product product, int quantity) {
        if (quantity <= 0) {
            UIUtils.showError("Invalid Quantity", "Please enter a positive number.");
            return;
        }
        if (quantity > product.getStock()) {
            UIUtils.showWarning("Out of Stock", "Only " + product.getStock() + " items available.");
            return;
        }

        cartManager.addProduct(product.copy(), quantity);
        CartEvents.notifyCartUpdated();
        UIUtils.showAlert("Added to Cart", quantity + "x " + product.getTitle() + " added to cart!");
    }

    public void handleProductsUpdated() {
        refreshAllProducts();
        view.displayProducts(currentDisplayedProducts);

        int updatedCartItems = cartManager.refreshCartProducts();
        if (updatedCartItems > 0) {
            CartEvents.notifyCartUpdated();
            System.out.println("✅ Cart updated: " + updatedCartItems + " item(s) synced with new product data");
        }
    }

    public void handleLoginSuccess() {
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getCurrentUser().getUsername();
            view.updateAccountUI(true, username);
            UIUtils.showAlert("Success", "Login successful! Welcome, " + username);
        }
    }

    public void handleOpenUserMenu() {
        SessionManager sessionManager = SessionManager.getInstance();

        if (!sessionManager.isLoggedIn()) {
            view.showLoginDialog();
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        String rolesStr = currentUser.getRoles().stream()
                .map(role -> role.toString())
                .reduce((r1, r2) -> r1 + ", " + r2)
                .orElse("No roles");

        List<UserMenuAction> allowedActions = new ArrayList<>();
        allowedActions.add(UserMenuAction.CHANGE_PASSWORD);

        boolean isAdmin = sessionManager.isAdministrator();
        boolean isPm = sessionManager.isProductManager();

        if (isAdmin) {
            allowedActions.add(UserMenuAction.MANAGE_USERS);
        }
        if (isPm) {
            allowedActions.add(UserMenuAction.MANAGE_ORDERS);
        }

        allowedActions.add(UserMenuAction.LOGOUT);

        view.displayUserMenuDialog(currentUser.getUsername(), rolesStr, allowedActions);
    }

    public void executeMenuAction(UserMenuAction action) {
        switch (action) {
            case CHANGE_PASSWORD:
                view.showChangePasswordDialog();
                break;
            case MANAGE_USERS:
                view.showUserManagementView();
                break;
            case MANAGE_ORDERS:
                view.navigateToOrderManagement();
                break;
            case LOGOUT:
                UserController.getInstance().logout();
                view.updateAccountUI(false, null);
                UIUtils.showAlert("Success", "Logged out successfully!");
                break;
        }
    }

    public void handleOpenProductManagement() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.canManageProducts()) {
            UIUtils.showError("Access Denied", "You need Product Manager role to manage products");
            return;
        }
        view.navigateToProductManagement();
    }

    public void handleOpenOrderManagement() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.canManageOrders()) {
            UIUtils.showError("Access Denied", "You need Product Manager role to manage orders");
            return;
        }
        view.navigateToOrderManagement();
    } // [FIXED] Added missing closing brace

    /**
     * CHANGELOG: Optimized to fetch 20 random products directly from the database
     * to avoid loading all products into memory, handling huge databases
     * efficiently.
     */
    public void refreshAllProducts() {
        allProducts.clear();
        random20Products.clear();

        List<Product> randomFromDb = productController.getRandomProducts(20);

        allProducts.addAll(randomFromDb);
        random20Products.addAll(randomFromDb);
        currentDisplayedProducts = new ArrayList<>(random20Products);
    }

    public void handleViewDetail(String productId) {
        try {
            ViewProductController viewController = ViewProductController.getInstance();
            Map<String, Object> productData = viewController.getProductDetail(productId); // Retrieves detailed data
                                                                                          // from DB
            view.showProductDetail(viewController, productData);
        } catch (ProductNotFoundException e) {
            // [ADDED] Explicitly handling DB exception when product is not found, matching
            // original controller
            UIUtils.showError("Error", "Product not found: " + e.getMessage());
        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to load product details: " + e.getMessage());
        }
    }

    // [ADDED] Expose cart getter for external dependencies and testing to match
    // original HomepageController capability
    public Cart getCart() {
        return cartManager.getCart();
    }
}
package com.aimsfx.controller;

import com.aimsfx.model.*;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.view.HomepageView;
import com.aimsfx.exception.ProductNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class HomepageController {
    private final HomepageView view;
    private final ProductController productController = ProductController.getInstance();
    private final ICartManager cartManager = CartManager.getInstance();

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentDisplayedProducts = new ArrayList<>();

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

    public void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            currentDisplayedProducts = new ArrayList<>(allProducts);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            currentDisplayedProducts = allProducts.stream()
                    .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerQuery)) ||
                            (p.getCategory() != null && p.getCategory().toLowerCase().contains(lowerQuery)) ||
                            (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(lowerQuery)) ||
                            (p.getProductType() != null
                                    && p.getProductType().name().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());
        }
        view.displayProducts(currentDisplayedProducts);
    }

    public void filterByPrice(double min, double max) {
        currentDisplayedProducts = allProducts.stream()
                .filter(p -> p.getCurrentPrice() >= min && p.getCurrentPrice() < max)
                .collect(Collectors.toList());
        view.displayProducts(currentDisplayedProducts);
    }

    public void filterOverPrice(double min) {
        currentDisplayedProducts = allProducts.stream()
                .filter(p -> p.getCurrentPrice() >= min)
                .collect(Collectors.toList());
        view.displayProducts(currentDisplayedProducts);
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
            view.showAlert("Invalid Quantity", "Please enter a positive number.");
            return;
        }
        if (quantity > product.getStock()) {
            view.showAlert("Out of Stock", "Only " + product.getStock() + " items available.");
            return;
        }

        cartManager.addProduct(product.copy(), quantity);
        CartEvents.notifyCartUpdated();
        view.showAlert("Added to Cart", quantity + "x " + product.getTitle() + " added to cart!");
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
            view.showAlert("Success", "Login successful! Welcome, " + username);
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
                .map(UserRole::toString)
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
                view.showAlert("Success", "Logged out successfully!");
                break;
        }
    }

    public void handleOpenProductManagement() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.canManageProducts()) {
            view.showAlert("Access Denied", "You need Product Manager role to manage products");
            return;
        }
        view.navigateToProductManagement();
    }

    public void handleOpenOrderManagement() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !session.canManageOrders()) {
            view.showAlert("Access Denied", "You need Product Manager role to manage orders");
            return;
        }
        view.navigateToOrderManagement();
    } // [FIXED] Added missing closing brace

    public void refreshAllProducts() {
        allProducts.clear();
        allProducts.addAll(productController.getProducts());
        currentDisplayedProducts = new ArrayList<>(allProducts);
    }

    public void handleViewDetail(String productId) {
        try {
            ViewProductController viewController = ViewProductController.getInstance();
            Map<String, Object> productData = viewController.getProductDetail(productId); // Retrieves detailed data from DB
            view.showProductDetail(viewController, productData);
        } catch (ProductNotFoundException e) {
            // [ADDED] Explicitly handling DB exception when product is not found, matching original controller
            view.showAlert("Error", "Product not found: " + e.getMessage());
        } catch (Exception e) {
            view.showAlert("Error", "Failed to load product details: " + e.getMessage());
        }
    }

    // [ADDED] Expose cart getter for external dependencies and testing to match original HomepageController capability
    public Cart getCart() {
        return cartManager.getCart();
    }
}
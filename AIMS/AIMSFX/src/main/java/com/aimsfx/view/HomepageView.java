package com.aimsfx.view;

import com.aimsfx.controller.HomepageController;
import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.model.Product;
import com.aimsfx.model.UserMenuAction;
import com.aimsfx.router.OrderManagementRouter;
import com.aimsfx.view.ProductView.ProductCardComponent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import com.aimsfx.router.AdministratorRouter;
import com.aimsfx.router.PlaceOrderRouter;
import com.aimsfx.utils.UIUtils;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.aimsfx.router.ProductRouter;

public class HomepageView {
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

    @FXML
    private Button btnAccount;

    private HomepageController controller;

    @FXML
    public void initialize() {
        this.controller = new HomepageController(this);

        /**
         * =========================================================================================================
         * FE OPTIMIZATION: DEBOUNCE & EXPLICIT SEARCH TRIGGERS
         * =========================================================================================================
         * PREVIOUS PROBLEMS:
         * 1. The textProperty().addListener called performSearch() on EVERY keystroke,
         * causing severe UI stutter.
         * 2. ControlsFX bindAutoCompletion fired immediately without debounce, flooding
         * the DB with LIKE queries.
         *
         * DETAILED SOLUTION & IMPLEMENTATION:
         * 1. Removed the real-time search listener. Main grid only updates on ENTER or
         * selecting a suggestion.
         * 2. Added a 300ms debounce (setDelay) to AutoCompletionBinding to wait until
         * the user stops typing.
         * 3. Added an event handler for suggestion selection to trigger the
         * asynchronous main search.
         *
         * EXPECTED RESULTS:
         * Typing will feel perfectly smooth. Database queries are reduced by 90%. UI
         * only updates when necessary.
         * =========================================================================================================
         */

        // Trigger search when ENTER is pressed
        searchField.setOnAction(e -> controller.performSearchAsync(searchField.getText()));

        // Bind Autocomplete with a 300ms delay to prevent DB spamming
        org.controlsfx.control.textfield.AutoCompletionBinding<String> binding = org.controlsfx.control.textfield.TextFields
                .bindAutoCompletion(searchField, request -> {
                    return controller.getAutocompleteSuggestions(request.getUserText());
                });

        binding.setDelay(300); // 300ms Debounce

        // When user clicks a suggestion, automatically search for it
        binding.setOnAutoCompleted(event -> {
            controller.performSearchAsync(event.getCompletion());
        });

        Platform.runLater(() -> {
            controller.initData();
            if (com.aimsfx.utils.SessionManager.getInstance().isLoggedIn()) {
                updateAccountUI(true, com.aimsfx.utils.SessionManager.getInstance().getCurrentUser().getUsername());
            }
        });
    }

    public void updateAccountUI(boolean isLoggedIn, String username) {
        if (btnAccount != null) {
            if (isLoggedIn && username != null) {
                btnAccount.setText("👤 " + username);
            } else {
                btnAccount.setText("👤 Login");
            }
        }
    }

    // Filter buttons state
    private void setActiveFilterButton(Button activeBtn) {
        Button[] filterButtons = { btnAll, btnUnder100K, btn100To200K, btn200To300K, btnOver300K };

        for (Button btn : filterButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("filter-tab", "filter-tab-active");
                btn.getStyleClass().add(btn == activeBtn ? "filter-tab-active" : "filter-tab");
            }
        }
    }

    @FXML
    public void filterUnder100K() {
        setActiveFilterButton(btnUnder100K);
        controller.filterByPrice(0, 100000);
    }

    @FXML
    public void filter100To200K() {
        setActiveFilterButton(btn100To200K);
        controller.filterByPrice(100000, 200000);
    }

    @FXML
    public void filter200To300K() {
        setActiveFilterButton(btn200To300K);
        controller.filterByPrice(200000, 300000);
    }

    @FXML
    public void filterOver300K() {
        setActiveFilterButton(btnOver300K);
        controller.filterOverPrice(300000);
    }

    @FXML
    public void filterAll() {
        setActiveFilterButton(btnAll);
        controller.clearPriceFilter();
        controller.refreshAllProducts();
        controller.performSearchAsync(searchField.getText());
    }

    // Sort buttons state
    private void setActiveSortButton(Button activeBtn) {
        Button[] sortButtons = { btnSortNew, btnSortPriceAsc, btnSortPriceDesc };

        for (Button btn : sortButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("sort-btn", "sort-btn-active");
                btn.getStyleClass().add(btn == activeBtn ? "sort-btn-active" : "sort-btn");
            }
        }
    }

    @FXML
    public void sortByNew() {
        setActiveSortButton(btnSortNew);
        controller.sortByNew();
    }

    @FXML
    public void sortByPriceAsc() {
        setActiveSortButton(btnSortPriceAsc);
        controller.sortByPrice(true);
    }

    @FXML
    public void sortByPriceDesc() {
        setActiveSortButton(btnSortPriceDesc);
        controller.sortByPrice(false);
    }

    @FXML
    public void onProductManagementPanel() {
        controller.handleOpenProductManagement();
    }

    @FXML
    public void showOrderManagementView() {
        controller.handleOpenOrderManagement();
    }

    // Render Methods

    public void showLoading() {
        Platform.runLater(() -> {
            productGrid.getChildren().clear();
            productCountLabel.setText("Searching...");
            ProgressIndicator loadingIndicator = new ProgressIndicator();
            loadingIndicator.setPrefSize(50, 50);

            // Add loading spinner to the center of the grid area
            GridPane.setHalignment(loadingIndicator, javafx.geometry.HPos.CENTER);
            GridPane.setValignment(loadingIndicator, javafx.geometry.VPos.CENTER);
            productGrid.add(loadingIndicator, 1, 0);
        });
    }

    public void displayProducts(List<Product> products) {
        productGrid.getChildren().clear();
        productCountLabel.setText(products.size() + " product" + (products.size() != 1 ? "s" : ""));

        int column = 0, row = 0;
        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-card.fxml"));
                VBox productCard = loader.load();

                ProductCardComponent cardComponent = loader.getController();
                cardComponent.setProductData(product,
                        this::openProductDetail,
                        this::promptQuantityAndAdd);

                productGrid.add(productCard, column, row);
                column++;
                if (column == 6) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new animatefx.animation.FadeInUp(productGrid).play();
    }

    @FXML
    public void onCartClick() {
        Stage stage = getMainWindow();
        PlaceOrderRouter.getInstance().navigateToCart(stage);
    }

    // [ADDED] Helper method to safely retrieve the current main window stage to act
    // as owner for modals, preventing UI null pointers
    private Stage getMainWindow() {
        if (productGrid != null && productGrid.getScene() != null) {
            return (Stage) productGrid.getScene().getWindow();
        }
        return null;
    }

    public void updateCartBadge(int count) {
        if (cartBadge != null) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }

    // Navigation UI

    private void promptQuantityAndAdd(Product product) {
        ProductRouter.getInstance().showQuantityPromptDialog(product, quantity -> {
            controller.handleAddToCart(product, quantity);
        });
    }

    public void navigateToProductManagement() {
        ProductRouter.getInstance().showProductList(getMainWindow(), () -> {
            if (controller != null) {
                controller.handleProductsUpdated();
            }
        });
    }

    public void navigateToOrderManagement() {
        try {
            OrderManagementRouter router = new OrderManagementRouter();
            Stage owner = getMainWindow(); // [ADDED] Safe owner retrieval
            router.show(owner);
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open order management view: " + e.getMessage());
        }
    }

    @FXML
    public void onAccount() {
        controller.handleOpenUserMenu();
    }

    public void displayUserMenuDialog(String username, String rolesStr, List<UserMenuAction> availableActions) {
        AdministratorRouter.getInstance().showUserMenuDialog(username, rolesStr, availableActions, action -> {
            controller.executeMenuAction(action);
        });
    }

    public void showUserManagementView() {
        Stage owner = getMainWindow();
        AdministratorRouter.getInstance().showUserManagementView(owner);
    }

    public void showLoginDialog() {
        Stage owner = getMainWindow();
        AdministratorRouter.getInstance().showLoginDialog(owner, () -> {
            if (controller != null) {
                controller.handleLoginSuccess();
            }
        });
    }

    public void showChangePasswordDialog() {
        Stage owner = getMainWindow();
        AdministratorRouter.getInstance().showChangePasswordDialog(owner);
    }

    private void openProductDetail(Product product) {
        controller.handleViewDetail(product.getProductId().toString());
    }

    public void showProductDetail(ViewProductController viewController, Map<String, Object> productData) {
        Stage currentStage = getMainWindow(); // [ADDED] Safe owner retrieval for product details view
        ProductRouter.getInstance().showProductDetail(String.valueOf(productData.get("productId")), currentStage);
    }
}
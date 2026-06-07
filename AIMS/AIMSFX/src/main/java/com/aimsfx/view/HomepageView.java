package com.aimsfx.view;

import com.aimsfx.controller.HomepageController;
import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.model.Product;
import com.aimsfx.model.UserMenuAction;
import com.aimsfx.view.ProductView.ProductCardComponent;
import com.aimsfx.view.ProductView.ProductDetailUI;
import com.aimsfx.view.ProductView.ProductListView;
import com.aimsfx.view.UserView.LoginView;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        searchField.textProperty().addListener((obs, oldVal, newVal) -> controller.performSearch(newVal));

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
        controller.refreshAllProducts();
        controller.performSearch(searchField.getText());
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
                if (column == 4) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onCartClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/cart-view.fxml"));
            Parent view = loader.load();

            Stage stage = (Stage) searchField.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(view);
            } else {
                stage.setScene(new Scene(view));
            }
            stage.setTitle("AIMS - Shopping Cart");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load cart view: " + e.getMessage());
        }
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

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Navigation UI

    private void promptQuantityAndAdd(Product product) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + product.getTitle() + " to cart");
        dialog.setContentText("Quantity (Available: " + product.getStock() + "):");

        dialog.showAndWait().ifPresent(qty -> {
            try {
                int quantity = Integer.parseInt(qty);
                controller.handleAddToCart(product, quantity);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    public void navigateToProductManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-list-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Product Management Panel");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMainWindow()); // [ADDED] Safe owner retrieval
            stage.setScene(new Scene(root, 1600, 900));
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(650);

            ProductListView subcontroller = loader.getController();
            subcontroller.setOnProductUpdated(() -> {
                if (controller != null) {
                    controller.handleProductsUpdated();
                }
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open product update dialog.");
        }
    }

    public void navigateToOrderManagement() {
        try {
            OrderManagementView view = new OrderManagementView();
            Stage owner = getMainWindow(); // [ADDED] Safe owner retrieval
            view.show(owner);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open order management view: " + e.getMessage());
        }
    }

    @FXML
    public void onAccount() {
        controller.handleOpenUserMenu();
    }

    public void displayUserMenuDialog(String username, String rolesStr, List<UserMenuAction> availableActions) {
        Alert menu = new Alert(Alert.AlertType.INFORMATION);
        menu.setTitle("Account");
        menu.setHeaderText("Logged in as: " + username + " (" + rolesStr + ")");

        Map<ButtonType, UserMenuAction> buttonMap = new HashMap<>();
        List<ButtonType> buttonTypes = new ArrayList<>();

        for (UserMenuAction action : availableActions) {
            ButtonType btn = new ButtonType(action.getLabel());
            buttonTypes.add(btn);
            buttonMap.put(btn, action);
        }

        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonTypes.add(cancelBtn);

        menu.getButtonTypes().setAll(buttonTypes);

        Optional<ButtonType> result = menu.showAndWait();

        if (result.isPresent() && result.get() != cancelBtn) {
            UserMenuAction selectedAction = buttonMap.get(result.get());
            if (selectedAction != null) {
                controller.executeMenuAction(selectedAction);
            }
        }
    }

    public void showUserManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/user-management-view.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("User Management");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getMainWindow()); // [ADDED] Safe owner retrieval
            dialogStage.setScene(new Scene(root, 1600, 900));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(900);
            dialogStage.setMinHeight(600);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open user management view: " + e.getMessage());
        }
    }

    public void showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/login-view.fxml"));
            Parent root = loader.load();

            LoginView loginView = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getMainWindow()); // [ADDED] Safe owner retrieval
            dialogStage.setScene(new Scene(root));

            loginView.setDialogStage(dialogStage);

            loginView.setOnLoginSuccess(() -> {
                if (controller != null) {
                    controller.handleLoginSuccess();
                }
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login view: " + e.getMessage());
        }
    }

    public void showChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/change-password-view.fxml"));
            Parent root = loader.load();

            com.aimsfx.view.UserView.ChangePasswordView controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getMainWindow()); // [ADDED] Safe owner retrieval
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load change password view: " + e.getMessage());
        }
    }

    private void openProductDetail(Product product) {
        controller.handleViewDetail(product.getProductId().toString());
    }

    public void showProductDetail(ViewProductController viewController, Map<String, Object> productData) {
        Stage currentStage = getMainWindow(); // [ADDED] Safe owner retrieval for product details view
        ProductDetailUI detailUI = new ProductDetailUI(viewController, currentStage);
        detailUI.displayProduct(productData);
    }
}
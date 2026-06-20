package com.aimsfx.router;

import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.ProductView.ProductDetailUI;
import com.aimsfx.view.ProductView.ProductFormView;
import com.aimsfx.view.ProductView.ProductHistoryView;
import com.aimsfx.view.ProductView.ProductListView;
import com.aimsfx.view.ProductView.UpdateProductFormView;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import animatefx.animation.FadeIn;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import com.aimsfx.model.Product;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

/**
 * ProductRouter handles navigation and dialog creation for Product Management views.
 * Implements the Singleton pattern.
 */
public class ProductRouter {

    private static ProductRouter instance;

    private ProductRouter() {
    }

    public static ProductRouter getInstance() {
        if (instance == null) {
            instance = new ProductRouter();
        }
        return instance;
    }

    /**
     * Shows the Product Management List Panel.
     */
    public void showProductList(Stage owner, Runnable onProductUpdated) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-list-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            UIUtils.applyAppIcon(stage);
            stage.setTitle("Product Management Panel");
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root, 1280, 720));
            stage.setResizable(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);

            new FadeIn(root).play();

            ProductListView controller = loader.getController();
            controller.setOnProductUpdated(onProductUpdated);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open product management panel.");
        }
    }

    /**
     * Shows the Add Product Form Dialog.
     */
    public void showAddProductForm(Stage owner, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            UIUtils.applyAppIcon(stage);
            stage.setTitle("Add Product");
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }

            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            new FadeIn(root).play();

            ProductFormView controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setOnProductAdded(onSuccess);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open product creation dialog.");
        }
    }

    /**
     * Shows the Update Product Form Dialog.
     */
    public void showUpdateProductForm(Long productId, Stage owner, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-update-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            UIUtils.applyAppIcon(stage);
            stage.setTitle("Update Product");
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root, 900, 650));
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            new FadeIn(root).play();

            UpdateProductFormView controller = loader.getController();
            controller.setProductData(productId);
            controller.setOnProductUpdated(() -> {
                if (onSuccess != null) onSuccess.run();
                Platform.runLater(stage::close);
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open product update dialog: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to update product: " + e.getMessage());
        }
    }

    /**
     * Shows the Product Details Dialog.
     */
    public void showProductDetail(String productId, Stage owner) {
        try {
            ViewProductController viewProductController = ViewProductController.getInstance();
            Map<String, Object> productData = viewProductController.getProductDetail(productId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-detail-ui-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            UIUtils.applyAppIcon(stage);
            stage.setTitle("Product Details");
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root, 800, 700));

            ProductDetailUI controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setProductData(productData);

            stage.show();
        } catch (ProductNotFoundException e) {
            UIUtils.showError("Error", "Product not found: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to load product details UI.");
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to load product details: " + e.getMessage());
        }
    }

    /**
     * Shows the Product History Dialog.
     */
    public void showProductHistory(Long productId, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/product-history-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            UIUtils.applyAppIcon(stage);
            stage.setTitle("Product History - ID: " + productId);
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root, 1000, 600));

            ProductHistoryView controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setProductHistory(productId);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to load product history.");
        }
    }

    /**
     * Shows a dialog to prompt the user for product quantity when adding to cart.
     */
    public void showQuantityPromptDialog(Product product, Consumer<Integer> onQuantityEntered) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + product.getTitle() + " to cart");
        dialog.setContentText("Quantity (Available: " + product.getStock() + "):");

        dialog.showAndWait().ifPresent(qty -> {
            try {
                int quantity = Integer.parseInt(qty);
                if (onQuantityEntered != null) {
                    onQuantityEntered.accept(quantity);
                }
            } catch (NumberFormatException e) {
                UIUtils.showError("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    /**
     * Shows a dialog to update the stock of a product.
     */
    public void showUpdateStockDialog(String currentStockText, BiConsumer<String, String> onStockEntered) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update Stock for Product");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label currentStockLabel = new Label("Current Stock:");
        Label currentStockValue = new Label(currentStockText);
        currentStockValue.setStyle("-fx-font-weight: bold;");

        Label newStockLabel = new Label("New Stock:");
        TextField newStockField = new TextField();
        newStockField.setPromptText("Enter new stock quantity");

        Label reasonLabel = new Label("Reason:");
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Enter reason for stock change (required)");
        reasonField.setPrefRowCount(3);

        grid.add(currentStockLabel, 0, 0);
        grid.add(currentStockValue, 1, 0);
        grid.add(newStockLabel, 0, 1);
        grid.add(newStockField, 1, 1);
        grid.add(reasonLabel, 0, 2);
        grid.add(reasonField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newStockText = newStockField.getText().trim();
            String reason = reasonField.getText().trim();
            if (onStockEntered != null) {
                onStockEntered.accept(newStockText, reason);
            }
        }
    }
}

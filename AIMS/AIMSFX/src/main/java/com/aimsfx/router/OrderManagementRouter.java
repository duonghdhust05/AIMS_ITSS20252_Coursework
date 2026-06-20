package com.aimsfx.router;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.utils.UIUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import animatefx.animation.FadeIn;

/**
 * Router class for Order Management.
 * Handles loading FXML files and opening new stages or dialogs.
 */
public class OrderManagementRouter {

    /**
     * Shows the main order management view.
     */
    public void show(Stage owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-management-view.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        UIUtils.applyAppIcon(stage);
        stage.setTitle("Order Management");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setScene(new Scene(root, 1600, 900));
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        new FadeIn(root).play();

        stage.showAndWait();
    }

    /**
     * Shows the order detail dialog for a specific order.
     */
    public void showDetailDialog(OrderDetail detail) {
        if (detail == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-detail-dialog.fxml"));
            javafx.scene.control.ScrollPane scrollPane = loader.load();
            com.aimsfx.view.OrderView.OrderDetailDialogUI controller = loader.getController();
            controller.setOrderDetail(detail);

            Dialog<Void> dialog = new Dialog<>();
            if (dialog.getDialogPane() != null && dialog.getDialogPane().getScene() != null) {
                javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage) {
                    UIUtils.applyAppIcon((Stage) window);
                }
            }
            dialog.setTitle("Order Detail");
            dialog.getDialogPane().setContent(scrollPane);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load order details view.");
        }
    }

    /**
     * Shows the reject reason dialog and returns the entered reason.
     * Returns null if cancelled.
     */
    public String showRejectReasonDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-reject-dialog.fxml"));
            javafx.scene.layout.VBox vbox = loader.load();
            com.aimsfx.view.OrderView.OrderRejectDialogUI controller = loader.getController();

            Dialog<Void> dialog = new Dialog<>();
            if (dialog.getDialogPane() != null && dialog.getDialogPane().getScene() != null) {
                javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage) {
                    UIUtils.applyAppIcon((Stage) window);
                }
            }
            dialog.setTitle("Reject Order");
            dialog.getDialogPane().setContent(vbox);

            // Dummy button to prevent Dialog from crashing
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);

            dialog.showAndWait();

            if (controller.isConfirmed()) {
                return controller.getReason();
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load reject dialog.");
        }
        return null;
    }
}

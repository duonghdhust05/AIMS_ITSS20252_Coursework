package com.aimsfx.view.OrderView;

import com.aimsfx.model.OrderDetail;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.aimsfx.utils.UIUtils;

/**
 * OrderManagementView - Pure View Layer for Product Manager order review
 * screen.
 */
public class OrderManagementView {

    public void show(Stage owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-management-view.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        com.aimsfx.utils.UIUtils.applyAppIcon(stage);
        stage.setTitle("Order Management");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setScene(new Scene(root, 1280, 720));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.showAndWait();
    }

    public void showDetailDialog(OrderDetail detail) {
        if (detail == null)
            return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/order-detail-dialog.fxml"));
            javafx.scene.control.ScrollPane scrollPane = loader.load();
            com.aimsfx.view.OrderView.OrderDetailDialogUI controller = loader
                    .getController();
            controller.setOrderDetail(detail);

            Dialog<Void> dialog = new Dialog<>();
            if (dialog != null && dialog.getDialogPane() != null && dialog.getDialogPane().getScene() != null) {
                javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage) {
                    com.aimsfx.utils.UIUtils.applyAppIcon((Stage) window);
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

    public String showRejectReasonDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/order-reject-dialog.fxml"));
            javafx.scene.layout.VBox vbox = loader.load();
            com.aimsfx.view.OrderView.OrderRejectDialogUI controller = loader
                    .getController();

            Dialog<Void> dialog = new Dialog<>();
            if (dialog != null && dialog.getDialogPane() != null && dialog.getDialogPane().getScene() != null) {
                javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage) {
                    com.aimsfx.utils.UIUtils.applyAppIcon((Stage) window);
                }
            }
            dialog.setTitle("Reject Order");
            dialog.getDialogPane().setContent(vbox);

            // Note: The new OrderRejectDialogUI uses its own buttons and calls
            // close() on the Stage.
            // But we need a dummy button type so Dialog doesn't crash if X is pressed
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            // Hide the default close button
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
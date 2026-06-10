package com.aimsfx.view;

import com.aimsfx.model.OrderDetail;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * OrderManagementView - Pure View Layer for Product Manager order review screen.
 */
public class OrderManagementView {

    public void show(Stage owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-management-view.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
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
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(detail.getSummary().getOrderId()).append("\n");
        sb.append("Customer: ").append(nullToEmpty(detail.getSummary().getCustomerName())).append("\n");
        sb.append("Total: ").append(detail.getSummary().getTotalAmount()).append(" VND").append("\n");
        sb.append("Payment: ").append(nullToEmpty(detail.getSummary().getPaymentMethod()))
                .append(" / ").append(nullToEmpty(detail.getSummary().getPaymentStatus())).append("\n");
        if (detail == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/aimsfx/order-detail-dialog.fxml"));
            javafx.scene.control.ScrollPane scrollPane = loader.load();
            com.aimsfx.controller.ProductManagerController.OrderDetailDialogController controller = loader.getController();
            controller.setOrderDetail(detail);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Detail");
            dialog.getDialogPane().setContent(scrollPane);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not load order details view.");
        }
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Warning: This action cannot be undone.");
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public String showRejectReasonDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/aimsfx/order-reject-dialog.fxml"));
            javafx.scene.layout.VBox vbox = loader.load();
            com.aimsfx.controller.ProductManagerController.OrderRejectDialogController controller = loader.getController();

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Reject Order");
            dialog.getDialogPane().setContent(vbox);
            
            // Note: The new OrderRejectDialogController uses its own buttons and calls close() on the Stage.
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
            showError("Error", "Could not load reject dialog.");
        }
        return null;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
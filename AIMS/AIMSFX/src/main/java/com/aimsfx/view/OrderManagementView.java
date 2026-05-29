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
        stage.setScene(new Scene(root, 1600, 900));
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
        sb.append("Status: ").append(detail.getSummary().getOrderStatus()).append("\n\n");
        sb.append("Delivery:\n");
        sb.append("- Email: ").append(nullToEmpty(detail.getDeliveryEmail())).append("\n");
        sb.append("- Phone: ").append(nullToEmpty(detail.getDeliveryPhone())).append("\n");
        sb.append("- Address: ").append(nullToEmpty(detail.getDeliveryAddress())).append("\n");
        sb.append("- Province: ").append(nullToEmpty(detail.getDeliveryProvince())).append("\n");
        sb.append("- Ward: ").append(nullToEmpty(detail.getDeliveryWard())).append("\n");
        sb.append("- Instructions: ").append(nullToEmpty(detail.getDeliveryInstructions())).append("\n\n");
        sb.append("Items:\n");
        detail.getLines().forEach(line -> sb.append("- ")
                .append(nullToEmpty(line.getProductTitle()))
                .append(" x").append(line.getQuantity())
                .append(" (").append(line.getUnitPrice()).append(")\n"));

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(400, 300);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Detail");
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
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
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reject Order");
        dialog.setHeaderText("Please select or enter a reason for rejection.");

        ButtonType rejectButtonType = new ButtonType("Reject", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rejectButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll(
                "Undelivered items",
                "Out of stock while customer is paying",
                "Cannot find the item in stock",
                "Other (specify below)"
        );
        reasonComboBox.setValue("Out of stock while customer is paying");

        TextField customReasonField = new TextField();
        customReasonField.setPromptText("Custom Reason");
        customReasonField.setDisable(true);

        reasonComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Other (specify below)".equals(newVal)) {
                customReasonField.setDisable(false);
            } else {
                customReasonField.setDisable(true);
                customReasonField.clear();
            }
        });

        grid.add(new Label("Reason:"), 0, 0);
        grid.add(reasonComboBox, 1, 0);
        grid.add(new Label("Custom:"), 0, 1);
        grid.add(customReasonField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rejectButtonType) {
                if ("Other (specify below)".equals(reasonComboBox.getValue())) {
                    return customReasonField.getText();
                }
                return reasonComboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
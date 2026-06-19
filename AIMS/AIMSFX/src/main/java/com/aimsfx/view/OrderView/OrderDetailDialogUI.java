package com.aimsfx.view.OrderView;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderLine;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class OrderDetailDialogUI {

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label paymentMethodLabel;

    @FXML
    private Label deliveryNameLabel;
    @FXML
    private Label deliveryPhoneLabel;
    @FXML
    private Label deliveryEmailLabel;
    @FXML
    private Label deliveryAddressLabel;
    @FXML
    private Label deliveryInstructionsLabel;

    @FXML
    private TableView<OrderLine> itemsTable;
    @FXML
    private TableColumn<OrderLine, String> colProduct;
    @FXML
    private TableColumn<OrderLine, Integer> colQuantity;
    @FXML
    private TableColumn<OrderLine, Double> colUnitPrice;
    @FXML
    private TableColumn<OrderLine, Double> colTotal;

    @FXML
    private Button closeButton;

    @FXML
    public void initialize() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productTitle"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Format columns
        colUnitPrice.setCellFactory(column -> new javafx.scene.control.TableCell<OrderLine, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(UIUtils.formatPrice(item) + " VND");
                }
            }
        });

        colTotal.setCellFactory(column -> new javafx.scene.control.TableCell<OrderLine, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(UIUtils.formatPrice(item) + " VND");
                }
            }
        });
    }

    public void setOrderDetail(OrderDetail detail) {
        if (detail == null)
            return;

        OrderSummary summary = detail.getSummary();
        if (summary != null) {
            orderIdLabel.setText(String.valueOf(summary.getOrderId()));
            statusLabel.setText(summary.getOrderStatus() != null ? summary.getOrderStatus().name() : "");
            totalAmountLabel.setText(UIUtils.formatPrice(summary.getTotalAmount()) + " VND");
            paymentMethodLabel.setText(summary.getPaymentMethod() + " (" + summary.getPaymentStatus() + ")");

            deliveryNameLabel.setText(nullToEmpty(summary.getCustomerName()));
        }

        deliveryPhoneLabel.setText(nullToEmpty(detail.getDeliveryPhone()));
        deliveryEmailLabel.setText(nullToEmpty(detail.getDeliveryEmail()));

        String address = nullToEmpty(detail.getDeliveryAddress());
        if (detail.getDeliveryWard() != null && !detail.getDeliveryWard().isEmpty()) {
            address += ", " + detail.getDeliveryWard();
        }
        if (detail.getDeliveryProvince() != null && !detail.getDeliveryProvince().isEmpty()) {
            address += ", " + detail.getDeliveryProvince();
        }
        deliveryAddressLabel.setText(address);

        deliveryInstructionsLabel.setText(nullToEmpty(detail.getDeliveryInstructions()));

        itemsTable.setItems(FXCollections.observableArrayList(detail.getLines()));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

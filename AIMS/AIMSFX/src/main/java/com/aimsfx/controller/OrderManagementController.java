package com.aimsfx.controller;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OrderManagementController - Product Manager UI controller for reviewing pending orders.
 *
 * Skeleton implementation:
 * - List pending orders (30/page)
 * - View order detail (read-only dialog)
 * - Approve / Reject (status update only; refund excluded)
 */
public class OrderManagementController {

    @FXML private TableView<OrderSummary> ordersTable;
    @FXML private TableColumn<OrderSummary, Integer> orderIdColumn;
    @FXML private TableColumn<OrderSummary, String> createdAtColumn;
    @FXML private TableColumn<OrderSummary, String> customerColumn;
    @FXML private TableColumn<OrderSummary, Double> totalColumn;
    @FXML private TableColumn<OrderSummary, String> statusColumn;
    @FXML private TableColumn<OrderSummary, Void> actionsColumn;
    @FXML private Pagination pagination;
    @FXML private Label totalPendingLabel;

    private final OrderReviewService orderReviewService = new OrderReviewService();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().canManageOrders()) {
            disableUi("Access denied: Product Manager role required.");
            return;
        }

        setupColumns();
        setupActions();
        loadFirstPage();
    }

    private void setupColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        createdAtColumn.setCellValueFactory(cell -> {
            if (cell.getValue() == null || cell.getValue().getCreatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            return new javafx.beans.property.SimpleStringProperty(cell.getValue().getCreatedAt().format(dateFmt));
        });
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(cell -> {
            String status = cell.getValue() != null && cell.getValue().getOrderStatus() != null
                    ? cell.getValue().getOrderStatus().name()
                    : "";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void setupActions() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, viewBtn, approveBtn, rejectBtn);

            {
                viewBtn.setOnAction(e -> onView(getCurrentOrderId()));
                approveBtn.setOnAction(e -> onApprove(getCurrentOrderId()));
                rejectBtn.setOnAction(e -> onReject(getCurrentOrderId()));
            }

            private Integer getCurrentOrderId() {
                OrderSummary row = getTableView().getItems().get(getIndex());
                return row != null ? row.getOrderId() : null;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadFirstPage() {
        try {
            int total = orderReviewService.countPendingReviewOrders();
            if (totalPendingLabel != null) {
                totalPendingLabel.setText("Pending: " + total);
            }

            int pageSize = OrderReviewService.DEFAULT_PAGE_SIZE;
            int pageCount = (int) Math.ceil(total / (double) pageSize);
            pagination.setPageCount(Math.max(1, pageCount));
            pagination.setCurrentPageIndex(0);
            pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> loadPage(newV.intValue()));

            loadPage(0);
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            disableUi("Failed to load orders.");
        }
    }

    private void loadPage(int pageIndex) {
        Platform.runLater(() -> {
            try {
                List<OrderSummary> page = orderReviewService.listPendingReviewOrders(pageIndex, OrderReviewService.DEFAULT_PAGE_SIZE);
                ordersTable.getItems().setAll(page);
            } catch (SQLException e) {
                showError("Database Error", e.getMessage());
            }
        });
    }

    private void onView(Integer orderId) {
        if (orderId == null) return;
        try {
            OrderDetail detail = orderReviewService.getOrderDetail(orderId);
            if (detail == null) {
                showError("Not Found", "Order not found: " + orderId);
                return;
            }
            showDetailDialog(detail);
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private void onApprove(Integer orderId) {
        if (orderId == null) return;
        if (!confirm("Approve Order", "Approve order #" + orderId + "?")) return;
        try {
            orderReviewService.approve(orderId);
            refreshCurrentPage();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private void onReject(Integer orderId) {
        if (orderId == null) return;
        if (!confirm("Reject Order", "Reject order #" + orderId + "?")) return;
        try {
            orderReviewService.reject(orderId);
            refreshCurrentPage();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private void refreshCurrentPage() {
        int idx = pagination.getCurrentPageIndex();
        loadPage(idx);
    }

    private void showDetailDialog(OrderDetail detail) {
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

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Detail");
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void disableUi(String message) {
        if (ordersTable != null) ordersTable.setDisable(true);
        if (pagination != null) pagination.setDisable(true);
        if (totalPendingLabel != null) totalPendingLabel.setText(message);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
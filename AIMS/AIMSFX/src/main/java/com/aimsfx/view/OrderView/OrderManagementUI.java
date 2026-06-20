package com.aimsfx.view.OrderView;

import com.aimsfx.controller.ProductManagerController.OrderReviewController;
import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.router.OrderManagementRouter;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.utils.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OrderManagementUI - FXML Controller for reviewing pending orders.
 * Now acts as a Passive View.
 */
public class OrderManagementUI {

    @FXML
    private TableView<OrderSummary> ordersTable;
    @FXML
    private TableColumn<OrderSummary, Integer> orderIdColumn;
    @FXML
    private TableColumn<OrderSummary, String> createdAtColumn;
    @FXML
    private TableColumn<OrderSummary, String> customerColumn;
    @FXML
    private TableColumn<OrderSummary, Double> totalColumn;
    @FXML
    private TableColumn<OrderSummary, String> statusColumn;
    @FXML
    private TableColumn<OrderSummary, Void> actionsColumn;
    @FXML
    private Pagination pagination;
    @FXML
    private Label totalPendingLabel;
    @FXML
    private Label refreshNotificationLabel;
    @FXML
    private Label lastUpdatedLabel;
    @FXML
    private Button refreshBtn;

    private final OrderReviewController controller = new OrderReviewController();
    private final OrderManagementRouter router = new OrderManagementRouter();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        if (!controller.canManageOrders()) {
            disableUi("Access denied: Product Manager role required.");
            return;
        }

        setupColumns();
        setupActions();
        loadFirstPage();

        // Register background polling callback
        controller.startBackgroundPolling(currentCount -> {
            Platform.runLater(() -> {
                if (refreshNotificationLabel != null) {
                    refreshNotificationLabel.setVisible(true);
                    refreshNotificationLabel.setManaged(true);
                }
            });
        });
    }

    @FXML
    private void handleRefresh() {
        if (refreshNotificationLabel != null) {
            refreshNotificationLabel.setVisible(false);
            refreshNotificationLabel.setManaged(false);
        }
        refreshCurrentPage();
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
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, viewBtn, approveBtn,
                    rejectBtn);

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
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderSummary row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        String status = row.getOrderStatus() != null ? row.getOrderStatus().name() : "";
                        boolean canApprove = "PENDING_REVIEW".equals(status) || "PENDING".equals(status);
                        boolean canReject = "PENDING_REVIEW".equals(status) || "PENDING".equals(status)
                                || "REFUND_REQUEST".equals(status);

                        approveBtn.setVisible(canApprove);
                        approveBtn.setManaged(canApprove);
                        rejectBtn.setVisible(canReject);
                        rejectBtn.setManaged(canReject);
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void loadFirstPage() {
        try {
            int total = controller.countAllOrders();
            updateTotalLabels(total);

            int pageSize = OrderReviewService.DEFAULT_PAGE_SIZE;
            int pageCount = (int) Math.ceil(total / (double) pageSize);
            pagination.setPageCount(Math.max(1, pageCount));
            pagination.setCurrentPageIndex(0);
            pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> loadPage(newV.intValue()));

            loadPage(0);
        } catch (SQLException e) {
            UIUtils.showError("Database Error", e.getMessage());
            disableUi("Failed to load orders.");
        }
    }

    private void loadPage(int pageIndex) {
        Platform.runLater(() -> {
            try {
                int total = controller.countAllOrders();
                updateTotalLabels(total);

                List<OrderSummary> page = controller.loadOrders(pageIndex, OrderReviewService.DEFAULT_PAGE_SIZE);
                ordersTable.getItems().setAll(page);
            } catch (SQLException e) {
                UIUtils.showError("Database Error", e.getMessage());
            }
        });
    }

    private void updateTotalLabels(int total) {
        if (totalPendingLabel != null) {
            totalPendingLabel.setText("Total Orders: " + total);
        }
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().format(timeFmt));
        }
    }

    private void onView(Integer orderId) {
        if (orderId == null)
            return;
        try {
            OrderDetail detail = controller.getOrderDetail(orderId);
            if (detail == null) {
                UIUtils.showError("Not Found", "Order not found: " + orderId);
                return;
            }
            router.showDetailDialog(detail);
        } catch (SQLException e) {
            UIUtils.showError("Database Error", e.getMessage());
        }
    }

    private void onApprove(Integer orderId) {
        if (orderId == null)
            return;
        if (!UIUtils.showConfirmation("Approve Order", "Are you sure you want to approve order #" + orderId + "?"))
            return;
        try {
            controller.approveOrder(orderId);
            refreshCurrentPage();
        } catch (SQLException e) {
            UIUtils.showError("Database Error", e.getMessage());
        }
    }

    private void onReject(Integer orderId) {
        if (orderId == null)
            return;
        if (!UIUtils.showConfirmation("Reject Order", "Are you sure you want to reject order #" + orderId + "?"))
            return;

        String reason = router.showRejectReasonDialog();
        if (reason == null || reason.trim().isEmpty()) {
            return; // Cancelled or empty reason
        }

        try {
            controller.rejectOrder(orderId, reason);
            refreshCurrentPage();
        } catch (SQLException e) {
            UIUtils.showError("Database Error", e.getMessage());
        }
    }

    private void refreshCurrentPage() {
        int idx = pagination.getCurrentPageIndex();
        loadPage(idx);
    }

    private void disableUi(String message) {
        if (ordersTable != null)
            ordersTable.setDisable(true);
        if (pagination != null)
            pagination.setDisable(true);
        if (totalPendingLabel != null)
            totalPendingLabel.setText(message);
    }
}
package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.model.OrderDetail;
import com.aimsfx.model.OrderSummary;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.utils.SessionManager;
import com.aimsfx.view.OrderManagementView;
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
    @FXML private Label refreshNotificationLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Button refreshBtn;

    private final OrderReviewService orderReviewService = new OrderReviewService();
    private final OrderManagementView view = new OrderManagementView();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    private java.util.concurrent.ScheduledExecutorService scheduler;
    private int lastLoadedCount = -1;

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().canManageOrders()) {
            disableUi("Access denied: Product Manager role required.");
            return;
        }

        setupColumns();
        setupActions();
        loadFirstPage();
        startBackgroundPolling();
    }

    @FXML
    private void handleRefresh() {
        if (refreshNotificationLabel != null) {
            refreshNotificationLabel.setVisible(false);
            refreshNotificationLabel.setManaged(false);
        }
        refreshCurrentPage();
    }

    private void startBackgroundPolling() {
        scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int currentCount = orderReviewService.countAllOrders();
                if (lastLoadedCount != -1 && currentCount != lastLoadedCount) {
                    Platform.runLater(() -> {
                        if (refreshNotificationLabel != null) {
                            refreshNotificationLabel.setVisible(true);
                            refreshNotificationLabel.setManaged(true);
                        }
                    });
                }
            } catch (SQLException e) {
                // Ignore background polling errors
            }
        }, 15, 15, java.util.concurrent.TimeUnit.SECONDS);
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
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderSummary row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        String status = row.getOrderStatus() != null ? row.getOrderStatus().name() : "";
                        boolean canApprove = "PENDING_REVIEW".equals(status) || "PENDING".equals(status);
                        boolean canReject = "PENDING_REVIEW".equals(status) || "PENDING".equals(status) || "REFUND_REQUEST".equals(status);
                        
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
            int total = orderReviewService.countAllOrders();
            this.lastLoadedCount = total;
            
            if (totalPendingLabel != null) {
                totalPendingLabel.setText("Total Orders: " + total);
            }
            if (lastUpdatedLabel != null) {
                lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().format(timeFmt));
            }

            int pageSize = OrderReviewService.DEFAULT_PAGE_SIZE;
            int pageCount = (int) Math.ceil(total / (double) pageSize);
            pagination.setPageCount(Math.max(1, pageCount));
            pagination.setCurrentPageIndex(0);
            pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> loadPage(newV.intValue()));

            loadPage(0);
        } catch (SQLException e) {
            view.showError("Database Error", e.getMessage());
            disableUi("Failed to load orders.");
        }
    }

    private void loadPage(int pageIndex) {
        Platform.runLater(() -> {
            try {
                int total = orderReviewService.countAllOrders();
                this.lastLoadedCount = total;
                if (totalPendingLabel != null) {
                    totalPendingLabel.setText("Total Orders: " + total);
                }
                if (lastUpdatedLabel != null) {
                    lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().format(timeFmt));
                }

                List<OrderSummary> page = orderReviewService.listAllOrders(pageIndex, OrderReviewService.DEFAULT_PAGE_SIZE);
                ordersTable.getItems().setAll(page);
            } catch (SQLException e) {
                view.showError("Database Error", e.getMessage());
            }
        });
    }

    private void onView(Integer orderId) {
        if (orderId == null) return;
        try {
            OrderDetail detail = orderReviewService.getOrderDetail(orderId);
            if (detail == null) {
                view.showError("Not Found", "Order not found: " + orderId);
                return;
            }
            view.showDetailDialog(detail);
        } catch (SQLException e) {
            view.showError("Database Error", e.getMessage());
        }
    }

    private void onApprove(Integer orderId) {
        if (orderId == null) return;
        if (!view.confirmAction("Approve Order", "Are you sure you want to approve order #" + orderId + "?")) return;
        try {
            orderReviewService.approve(orderId);
            refreshCurrentPage();
        } catch (SQLException e) {
            view.showError("Database Error", e.getMessage());
        }
    }

    private void onReject(Integer orderId) {
        if (orderId == null) return;
        if (!view.confirmAction("Reject Order", "Are you sure you want to reject order #" + orderId + "?")) return;
        
        String reason = view.showRejectReasonDialog();
        if (reason == null || reason.trim().isEmpty()) {
            return; // Cancelled or empty reason
        }

        try {
            orderReviewService.reject(orderId, reason);
            refreshCurrentPage();
        } catch (SQLException e) {
            view.showError("Database Error", e.getMessage());
        }
    }

    private void refreshCurrentPage() {
        int idx = pagination.getCurrentPageIndex();
        loadPage(idx);
    }

    private void disableUi(String message) {
        if (ordersTable != null) ordersTable.setDisable(true);
        if (pagination != null) pagination.setDisable(true);
        if (totalPendingLabel != null) totalPendingLabel.setText(message);
    }
}
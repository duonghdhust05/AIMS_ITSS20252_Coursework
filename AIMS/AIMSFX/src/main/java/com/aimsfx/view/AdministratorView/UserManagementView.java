package com.aimsfx.view.AdministratorView;

import com.aimsfx.controller.UserController;
import com.aimsfx.exception.*;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * UserManagementView - UI Controller for user management screen
 * 
 * RESPONSIBILITIES (UI only):
 * - Display user list in table format
 * - Handle UI events (button clicks, filter changes)
 * - Show dialogs and alerts
 * - Navigate to sub-dialogs (create/edit user)
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles UI logic
 * - Delegates business logic to UserController
 * - No validation logic (handled by Validator)
 * - No business rules (handled by Service)
 */
public class UserManagementView implements Initializable {

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> rolesColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, String> createdAtColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private Button addUserButton;
    @FXML
    private Label userCountLabel;
    @FXML
    private ComboBox<String> filterComboBox;

    private UserController userController;
    private SessionManager sessionManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userController = UserController.getInstance();
        sessionManager = SessionManager.getInstance();

        setupTableColumns();
        setupActionsColumn();
        setupFilterComboBox();
        loadUsers();
    }

    // ==================== TABLE SETUP (UI) ====================

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        rolesColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String roles = user.getRoles().stream()
                    .map(UserRole::toString)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(roles);
        });

        statusColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("ACTIVE".equals(item)
                            ? "-fx-text-fill: #4CAF50; -fx-font-weight: bold;"
                            : "-fx-text-fill: #f44336; -fx-font-weight: bold;");
                }
            }
        });

        createdAtColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String dateStr = user.getCreatedAt() != null
                    ? user.getCreatedAt().format(DATE_FORMATTER)
                    : "";
            return new SimpleStringProperty(dateStr);
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new ActionButtonCell());
    }

    private void setupFilterComboBox() {
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll(
                    "All Users", "Active", "Blocked", "Administrators", "Product Managers");
            filterComboBox.setValue("All Users");
            filterComboBox.setOnAction(e -> applyFilter());
        }
    }

    // ==================== DATA LOADING (delegates to Controller)
    // ====================

    private void loadUsers() {
        try {
            ObservableList<User> users = userController.getAllUsers();
            userTableView.setItems(users);
            updateUserCount();
        } catch (UnauthorizedAccessException e) {
            showErrorAlert("Access Denied", e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = filterComboBox.getValue();
        try {
            ObservableList<User> allUsers = userController.getAllUsers();
            ObservableList<User> filteredUsers = allUsers.filtered(user -> matchesFilter(user, filter));
            userTableView.setItems(filteredUsers);
            updateUserCount();
        } catch (UnauthorizedAccessException e) {
            showErrorAlert("Access Denied", e.getMessage());
        }
    }

    private boolean matchesFilter(User user, String filter) {
        return switch (filter) {
            case "Active" -> user.isActive();
            case "Blocked" -> user.isBlocked();
            case "Administrators" -> user.hasRole(UserRole.ADMINISTRATOR);
            case "Product Managers" -> user.hasRole(UserRole.PRODUCT_MANAGER);
            default -> true;
        };
    }

    private void updateUserCount() {
        if (userCountLabel != null) {
            int count = userTableView.getItems().size();
            userCountLabel.setText(count + " user" + (count != 1 ? "s" : ""));
        }
    }

    // ==================== EVENT HANDLERS (UI) ====================

    @FXML
    private void handleAddUser() {
        openUserFormDialog(null);
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        applyFilter();
    }

    @FXML
    private void handleClose() {
        ((Stage) userTableView.getScene().getWindow()).close();
    }

    // ==================== DIALOG NAVIGATION (UI) ====================

    private void openUserFormDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/aimsfx/user-form-view.fxml"));
            Parent root = loader.load();

            UserFormView controller = loader.getController();
            controller.setUser(user);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(user == null ? "Add New User" : "Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userTableView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            controller.setOnSaveSuccess(this::refreshAfterChange);

            dialogStage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to open user form: " + e.getMessage());
        }
    }

    private void openResetPasswordDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + user.getUsername());
        dialog.setContentText("New Password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                boolean success = userController.resetPassword(user.getUserId(), newPassword);
                if (success) {
                    showSuccessAlert("Success", "Password reset successfully for " + user.getUsername());
                } else {
                    showErrorAlert("Error", "Failed to reset password!");
                }
            } catch (InvalidPasswordException e) {
                showErrorAlert("Validation Error", e.getMessage());
            } catch (UnauthorizedAccessException e) {
                showErrorAlert("Access Denied", e.getMessage());
            } catch (UserNotFoundException e) {
                showErrorAlert("Error", e.getMessage());
            }
        });
    }

    private void confirmAndToggleBlock(User user) {
        String action = user.isBlocked() ? "unblock" : "block";

        if (!showConfirmDialog(
                "Confirm " + (user.isBlocked() ? "Unblock" : "Block"),
                "Are you sure you want to " + action + " this user?",
                "User: " + user.getUsername() + " (" + user.getFullName() + ")")) {
            return;
        }

        try {
            boolean success = user.isBlocked()
                    ? userController.unblockUser(user.getUserId())
                    : userController.blockUser(user.getUserId());

            if (success) {
                showSuccessAlert("Success", "User " + action + "ed successfully!");
                refreshAfterChange();
            } else {
                showErrorAlert("Error", "Failed to " + action + " user!");
            }
        } catch (UnauthorizedAccessException e) {
            showErrorAlert("Access Denied", e.getMessage());
        } catch (SelfOperationException e) {
            showErrorAlert("Error", e.getMessage());
        } catch (UserNotFoundException e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    private void confirmAndDeleteUser(User user) {
        if (!showConfirmDialog(
                "Confirm Delete",
                "Are you sure you want to delete this user?",
                "User: " + user.getUsername() + " (" + user.getFullName() + ")\n\nThis action cannot be undone!")) {
            return;
        }

        try {
            boolean success = userController.deleteUser(user.getUserId());
            if (success) {
                showSuccessAlert("Success", "User deleted successfully!");
                refreshAfterChange();
            } else {
                showErrorAlert("Error", "Failed to delete user!");
            }
        } catch (UnauthorizedAccessException e) {
            showErrorAlert("Access Denied", e.getMessage());
        } catch (SelfOperationException e) {
            showErrorAlert("Error", e.getMessage());
        } catch (UserNotFoundException e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    private void refreshAfterChange() {
        loadUsers();
        applyFilter();
    }

    // ==================== ALERT HELPERS (UI) ====================

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ==================== INNER CLASS: Action Button Cell ====================

    private class ActionButtonCell extends TableCell<User, Void> {
        private final Button editBtn = new Button("Edit");
        private final Button resetPwdBtn = new Button("Reset Pwd");
        private final Button blockBtn = new Button("Block");
        private final Button deleteBtn = new Button("Delete");
        private final HBox buttonBox = new HBox(5);

        ActionButtonCell() {
            styleButtons();
            setupButtonActions();
            buttonBox.getChildren().addAll(editBtn, resetPwdBtn, blockBtn, deleteBtn);
        }

        private void styleButtons() {
            editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11;");
            resetPwdBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 11;");
            blockBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 11;");
            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11;");
        }

        private void setupButtonActions() {
            editBtn.setOnAction(e -> openUserFormDialog(getCurrentUser()));
            resetPwdBtn.setOnAction(e -> openResetPasswordDialog(getCurrentUser()));
            blockBtn.setOnAction(e -> confirmAndToggleBlock(getCurrentUser()));
            deleteBtn.setOnAction(e -> confirmAndDeleteUser(getCurrentUser()));
        }

        private User getCurrentUser() {
            return getTableView().getItems().get(getIndex());
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                User user = getCurrentUser();
                updateBlockButton(user);
                updateButtonStates(user);
                setGraphic(buttonBox);
            }
        }

        private void updateBlockButton(User user) {
            if (user.isBlocked()) {
                blockBtn.setText("Unblock");
                blockBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11;");
            } else {
                blockBtn.setText("Block");
                blockBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 11;");
            }
        }

        private void updateButtonStates(User user) {
            boolean isCurrentUser = user.getUserId().equals(
                    sessionManager.getCurrentUser().getUserId());
            blockBtn.setDisable(isCurrentUser);
            deleteBtn.setDisable(isCurrentUser);
        }
    }
}

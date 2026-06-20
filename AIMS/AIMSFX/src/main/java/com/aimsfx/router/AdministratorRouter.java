package com.aimsfx.router;

import com.aimsfx.model.User;
import com.aimsfx.utils.UIUtils;
import com.aimsfx.view.AdministratorView.UserFormView;
import com.aimsfx.view.LoginView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.aimsfx.model.UserMenuAction;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextInputDialog;
import animatefx.animation.FadeIn;

/**
 * AdministratorRouter Class
 * Purpose: Singleton router to manage all navigation and UI transitions for the
 * Administrator/User module.
 * 
 * SOLID Compliance:
 * - SRP: Handles only Stage creation, Scene transitions, and Dialog popups.
 */
public class AdministratorRouter {

    private static AdministratorRouter instance;

    private AdministratorRouter() {
    }

    public static AdministratorRouter getInstance() {
        if (instance == null) {
            instance = new AdministratorRouter();
        }
        return instance;
    }

    /**
     * Shows the Login Dialog.
     */
    public void showLoginDialog(Stage owner, Runnable onLoginSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/login-view.fxml"));
            Parent root = loader.load();

            LoginView loginView = loader.getController();

            Stage dialogStage = new Stage();
            UIUtils.applyAppIcon(dialogStage);
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setScene(new Scene(root));

            loginView.setDialogStage(dialogStage);

            if (onLoginSuccess != null) {
                loginView.setOnLoginSuccess(onLoginSuccess);
            }

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load login view: " + e.getMessage());
        }
    }

    /**
     * Shows the Change Password Dialog.
     */
    public void showChangePasswordDialog(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/auth-change-password-view.fxml"));
            VBox root = loader.load();

            Stage dialogStage = new Stage();
            UIUtils.applyAppIcon(dialogStage);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setTitle("Change Password");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            // Play FadeIn
            new FadeIn(root).play();

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Cannot display Change Password form.");
        }
    }

    /**
     * Shows a dialog to prompt for a new password when resetting a user's password.
     */
    public void showResetPasswordDialog(User user, Consumer<String> onPasswordEntered) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset Password for User: " + user.getUsername());
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newPassword = result.get().trim();
            if (!newPassword.isEmpty()) {
                if (onPasswordEntered != null) {
                    onPasswordEntered.accept(newPassword);
                }
            } else {
                UIUtils.showError("Error", "Password cannot be empty.");
            }
        }
    }

    /**
     * Shows a user menu dialog with dynamic actions based on roles.
     */
    public void showUserMenuDialog(String username, String rolesStr, List<UserMenuAction> availableActions,
            Consumer<UserMenuAction> onSelect) {
        Alert menu = new Alert(Alert.AlertType.INFORMATION);
        menu.setTitle("Account");
        menu.setHeaderText("Logged in as: " + username + " (" + rolesStr + ")");

        Map<ButtonType, UserMenuAction> buttonMap = new HashMap<>();
        List<ButtonType> buttonTypes = new ArrayList<>();

        for (UserMenuAction action : availableActions) {
            ButtonType btn = new ButtonType(action.getLabel());
            buttonTypes.add(btn);
            buttonMap.put(btn, action);
        }

        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonTypes.add(cancelBtn);

        menu.getButtonTypes().setAll(buttonTypes);

        Optional<ButtonType> result = menu.showAndWait();

        if (result.isPresent() && result.get() != cancelBtn) {
            UserMenuAction selectedAction = buttonMap.get(result.get());
            if (selectedAction != null && onSelect != null) {
                onSelect.accept(selectedAction);
            }
        }
    }

    /**
     * Shows the User Management View.
     */
    public void showUserManagementView(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/user-management-view.fxml"));
            Parent root = loader.load();

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(root);
            } else {
                currentStage.setScene(new Scene(root, 1600, 900));
            }
            currentStage.setTitle("AIMS - User Management");

            new FadeIn(root).play();

            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open user management view: " + e.getMessage());
        }
    }

    /**
     * Shows the User Form Dialog (Add/Edit User).
     */
    public void showUserFormDialog(Stage owner, User user, Runnable onSaveSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/user-form-view.fxml"));
            Parent root = loader.load();

            UserFormView controller = loader.getController();
            controller.setUser(user);

            Stage dialogStage = new Stage();
            UIUtils.applyAppIcon(dialogStage);
            dialogStage.setTitle(user == null ? "Add New User" : "Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            if (onSaveSuccess != null) {
                controller.setOnSaveSuccess(onSaveSuccess);
            }

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to open user form: " + e.getMessage());
        }
    }
}

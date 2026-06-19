package com.aimsfx.view.AdministratorView;

import com.aimsfx.controller.UserController;
import com.aimsfx.exception.InvalidPasswordException;
import com.aimsfx.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import com.aimsfx.utils.UIUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ChangePasswordView - UI Controller for change password dialog
 * 
 * RESPONSIBILITIES (UI only):
 * - Display password change form
 * - Collect password input from user
 * - Show validation error messages from exceptions
 * - Handle form submission UI
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles UI logic
 * - No validation logic (handled by Validator via Controller)
 * - Delegates all business operations to Controller
 */
public class ChangePasswordView implements Initializable {

    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label usernameLabel;

    private UserController userController;
    private SessionManager sessionManager;
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userController = UserController.getInstance();
        sessionManager = SessionManager.getInstance();

        displayCurrentUsername();
        setupInputListeners();
    }

    // ==================== SETUP METHODS ====================

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void displayCurrentUsername() {
        if (usernameLabel != null && sessionManager.isLoggedIn()) {
            usernameLabel.setText(" " + sessionManager.getCurrentUser().getUsername());
        }
    }

    private void setupInputListeners() {
        oldPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
        newPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleChangePassword() {
        clearError();

        // UI-level validation: password confirmation check
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords do not match!");
            confirmPasswordField.requestFocus();
            return;
        }

        try {
            String oldPassword = oldPasswordField.getText();
            boolean success = userController.changePassword(oldPassword, newPassword);

            if (success) {
                UIUtils.showAlert("Success", "Password changed successfully!");
                closeDialog();
            } else {
                showError("Current password is incorrect!");
                oldPasswordField.requestFocus();
            }
        } catch (InvalidPasswordException e) {
            showError(e.getMessage());
            focusFieldForError(e.getReason());
        } catch (Exception e) {
            showError("Failed to change password: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // ==================== UI HELPERS ====================

    private void focusFieldForError(InvalidPasswordException.Reason reason) {
        switch (reason) {
            case EMPTY:
            case TOO_SHORT:
                newPasswordField.requestFocus();
                break;
            case SAME_AS_OLD:
                newPasswordField.requestFocus();
                break;
            default:
                oldPasswordField.requestFocus();
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }
}

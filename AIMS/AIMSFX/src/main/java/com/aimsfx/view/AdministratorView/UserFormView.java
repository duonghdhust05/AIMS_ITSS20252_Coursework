package com.aimsfx.view.AdministratorView;

import com.aimsfx.controller.UserController;
import com.aimsfx.exception.*;
import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.aimsfx.utils.UIUtils;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * UserFormView - UI Controller for user create/edit form
 * 
 * RESPONSIBILITIES (UI only):
 * - Display user form fields
 * - Collect user input from form
 * - Show validation error messages from exceptions
 * - Handle form submission UI
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles UI logic
 * - No validation logic (handled by Validator via Controller)
 * - Delegates all business operations to Controller
 */
public class UserFormView implements Initializable {
    
    @FXML private Label formTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private CheckBox adminRoleCheckBox;
    @FXML private CheckBox pmRoleCheckBox;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Label passwordHintLabel;
    @FXML private Label errorLabel;
    
    private UserController userController;
    private Stage dialogStage;
    private User user;
    private Runnable onSaveSuccess;
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userController = UserController.getInstance();
        clearError();
        
        // Clear error on input change
        usernameField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
        fullNameField.textProperty().addListener((obs, old, newVal) -> clearError());
    }
    
    // ==================== PUBLIC SETUP METHODS ====================
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setOnSaveSuccess(Runnable callback) {
        this.onSaveSuccess = callback;
    }
    
    public void setUser(User user) {
        this.user = user;
        this.isEditMode = (user != null);
        configureFormForMode();
    }
    
    // ==================== UI CONFIGURATION ====================
    
    private void configureFormForMode() {
        if (isEditMode) {
            setupEditMode();
        } else {
            setupCreateMode();
        }
    }
    
    private void setupEditMode() {
        formTitleLabel.setText("Edit User");
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        
        adminRoleCheckBox.setSelected(user.hasRole(UserRole.ADMINISTRATOR));
        pmRoleCheckBox.setSelected(user.hasRole(UserRole.PRODUCT_MANAGER));
        
        // Hide password fields in edit mode
        setPasswordFieldsVisible(false);
    }
    
    private void setupCreateMode() {
        formTitleLabel.setText("Add New User");
        setPasswordFieldsVisible(true);
    }
    
    private void setPasswordFieldsVisible(boolean visible) {
        passwordField.setVisible(visible);
        passwordField.setManaged(visible);
        confirmPasswordField.setVisible(visible);
        confirmPasswordField.setManaged(visible);
        passwordLabel.setVisible(visible);
        passwordLabel.setManaged(visible);
        confirmPasswordLabel.setVisible(visible);
        confirmPasswordLabel.setManaged(visible);
        if (passwordHintLabel != null) {
            passwordHintLabel.setVisible(visible);
            passwordHintLabel.setManaged(visible);
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    @FXML
    private void handleSave() {
        clearError();
        
        try {
            // UI-level validation: password confirmation check
            if (!isEditMode && !passwordField.getText().equals(confirmPasswordField.getText())) {
                showError("Passwords do not match!");
                confirmPasswordField.requestFocus();
                return;
            }
            
            if (isEditMode) {
                updateExistingUser();
            } else {
                createNewUser();
            }
            
            if (onSaveSuccess != null) {
                onSaveSuccess.run();
            }
            closeDialog();
            
        } catch (UserValidationException e) {
            showError(e.getMessage());
            focusFieldForError(e.getMessage());
        } catch (DuplicateUsernameException e) {
            showError(e.getMessage());
            usernameField.requestFocus();
        } catch (UnauthorizedAccessException e) {
            UIUtils.showError("Access Denied", e.getMessage());
        } catch (UserNotFoundException e) {
            UIUtils.showError("Error", e.getMessage());
        } catch (InvalidPasswordException e) {
            showError(e.getMessage());
            passwordField.requestFocus();
        }
    }
    
    private void createNewUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fullName = fullNameField.getText().trim();
        Set<UserRole> roles = collectSelectedRoles();
        
        userController.createUser(username, password, roles, fullName);
        UIUtils.showAlert("Success", "User created successfully!");
    }
    
    private void updateExistingUser() {
        String fullName = fullNameField.getText().trim();
        Set<UserRole> roles = collectSelectedRoles();
        
        userController.updateUser(user.getUserId(), null, roles, fullName);
        UIUtils.showAlert("Success", "User updated successfully!");
    }
    
    private Set<UserRole> collectSelectedRoles() {
        Set<UserRole> roles = new HashSet<>();
        if (adminRoleCheckBox.isSelected()) {
            roles.add(UserRole.ADMINISTRATOR);
        }
        if (pmRoleCheckBox.isSelected()) {
            roles.add(UserRole.PRODUCT_MANAGER);
        }
        return roles;
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
    
    private void focusFieldForError(String errorMessage) {
        String lowerMsg = errorMessage.toLowerCase();
        if (lowerMsg.contains("username")) {
            usernameField.requestFocus();
        } else if (lowerMsg.contains("password")) {
            passwordField.requestFocus();
        } else if (lowerMsg.contains("full name") || lowerMsg.contains("fullname")) {
            fullNameField.requestFocus();
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
            errorLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }
}

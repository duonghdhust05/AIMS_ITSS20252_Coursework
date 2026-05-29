package com.aimsfx.view;

import com.aimsfx.controller.UserController;
import com.aimsfx.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginView - Controller for login UI
 * 
 * RESPONSIBILITIES:
 * - User authentication
 * - Input validation
 * - Error messaging
 */
public class LoginView implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private UserController userController;
    private Stage dialogStage;
    private Runnable onLoginSuccess;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userController = UserController.getInstance();
        SessionManager.getInstance();
        
        // Clear error on input
        usernameField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        passwordField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        
        // Login on Enter key
        passwordField.setOnAction(e -> handleLogin());
    }
    
    /**
     * Set the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Set callback for successful login
     */
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (username.isEmpty()) {
            errorLabel.setText("Username is required!");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            errorLabel.setText("Password is required!");
            passwordField.requestFocus();
            return;
        }
        
        // Attempt login
        boolean success = userController.login(username, password);
        
        if (success) {
            // Login successful
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            // Login failed
            errorLabel.setText("Invalid username or password, or account is blocked!");
            passwordField.clear();
            usernameField.requestFocus();
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}

package com.aimsfx.controller;

import com.aimsfx.model.Province;
import com.aimsfx.model.Ward;
import com.aimsfx.service.VietnamProvinceService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

public class DeliveryInfoDialogController {
    
    @FXML private TextField dialogNameField;
    @FXML private TextField dialogPhoneField;
    @FXML private TextField dialogEmailField;
    @FXML private ComboBox<Province> dialogProvinceComboBox;
    @FXML private ComboBox<Ward> dialogWardComboBox;
    @FXML private TextArea dialogAddressArea;
    @FXML private TextArea dialogDeliveryInstructionsArea;
    @FXML private Label dialogDeliveryFeeLabel;
    @FXML private Button dialogSaveButton;
    @FXML private Button dialogCancelButton;
    
    // Error labels
    @FXML private Label dialogNameError;
    @FXML private Label dialogPhoneError;
    @FXML private Label dialogEmailError;
    @FXML private Label dialogProvinceError;
    @FXML private Label dialogWardError;
    @FXML private Label dialogAddressError;
    
    private boolean saved = false;
    private double totalWeight;
    private double totalAmount;
    private VietnamProvinceService provinceService;
    private List<Province> allProvinces;
    
    public void initialize() {
        provinceService = VietnamProvinceService.getInstance();
        
        // Load provinces asynchronously
        loadProvincesAsync();
        
        // Setup cascade dropdowns
        setupProvinceComboBox();
        setupWardComboBox();
        
        // Cancel button handler
        dialogCancelButton.setOnAction(e -> {
            Stage stage = (Stage) dialogCancelButton.getScene().getWindow();
            stage.close();
        });
        
        // Save button handler
        dialogSaveButton.setOnAction(e -> handleSave());
    }
    
    public void setOrderData(double weight, double amount) {
        this.totalWeight = weight;
        this.totalAmount = amount;
    }
    
    /**
     * Set existing delivery data for editing
     */
    public void setExistingData(String name, String phone, String email, String province, 
                                String subDistrict, String address, String instructions) {
        if (name != null && !name.isEmpty()) {
            dialogNameField.setText(name);
        }
        if (phone != null && !phone.isEmpty()) {
            dialogPhoneField.setText(phone);
        }
        if (email != null && !email.isEmpty()) {
            dialogEmailField.setText(email);
        }
        if (address != null && !address.isEmpty()) {
            dialogAddressArea.setText(address);
        }
        if (instructions != null && !instructions.isEmpty()) {
            dialogDeliveryInstructionsArea.setText(instructions);
        }
        
        // For province and ward, need to select after provinces are loaded
        if (province != null && !province.isEmpty()) {
            // Use callback-based approach instead of polling
            waitForProvincesAndSelect(province, subDistrict);
        }
    }
    
    /**
     * Wait for provinces to load, then select matching province/ward
     * Uses callback approach instead of blocking loop
     */
    private void waitForProvincesAndSelect(String provinceName, String wardName) {
        if (allProvinces != null && !allProvinces.isEmpty()) {
            // Already loaded, select immediately
            selectProvinceAndWard(provinceName, wardName);
        } else {
            // Schedule retry in 100ms (non-blocking)
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                
                Platform.runLater(() -> waitForProvincesAndSelect(provinceName, wardName));
            }).start();
        }
    }
    
    /**
     * Select province and ward by name
     */
    private void selectProvinceAndWard(String provinceName, String wardName) {
        // Find and select province
        for (Province p : allProvinces) {
            if (p.getName().equals(provinceName)) {
                dialogProvinceComboBox.setValue(p);
                
                // If ward provided, wait for wards to load then select
                if (wardName != null && !wardName.isEmpty()) {
                    selectWardAfterLoad(p, wardName);
                }
                break;
            }
        }
    }
    
    /**
     * Select ward after wards are loaded for province
     */
    private void selectWardAfterLoad(Province province, String wardName) {
        if (province.getWards() != null && !province.getWards().isEmpty()) {
            // Wards already loaded
            for (Ward w : province.getWards()) {
                if (w.getName().equals(wardName)) {
                    dialogWardComboBox.setValue(w);
                    break;
                }
            }
        } else {
            // Wait for lazy loading to complete
            new Thread(() -> {
                try {
                    Thread.sleep(200);  // Wait for lazy load
                } catch (InterruptedException ignored) {}
                
                Platform.runLater(() -> selectWardAfterLoad(province, wardName));
            }).start();
        }
    }
    
    /**
     * Load provinces from API v2 asynchronously
     */
    private void loadProvincesAsync() {
        // Show loading state
        dialogProvinceComboBox.setPromptText("Loading list...");
        dialogProvinceComboBox.setDisable(true);
        
        new Thread(() -> {
            try {
                allProvinces = provinceService.getAllProvinces();
                
                Platform.runLater(() -> {
                    dialogProvinceComboBox.getItems().setAll(allProvinces);
                    dialogProvinceComboBox.setPromptText("Select province/city");
                    dialogProvinceComboBox.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    dialogProvinceComboBox.setPromptText("Failed to load provinces");
                    dialogProvinceComboBox.setDisable(false);
                    
                    // Show error
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cannot load province list");
                    alert.setContentText("Please check your internet connection and try again.");
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    /**
     * Setup province ComboBox with change listener
     * Lazy load wards when province is selected
     */
    private void setupProvinceComboBox() {
        dialogProvinceComboBox.setOnAction(e -> {
            Province selectedProvince = dialogProvinceComboBox.getValue();
            
            // Clear ward selection
            dialogWardComboBox.getItems().clear();
            dialogWardComboBox.setValue(null);
            dialogWardComboBox.setDisable(true);
            dialogWardComboBox.setPromptText("Select ward/commune");
            
            dialogDeliveryFeeLabel.setText("Delivery fee: Not calculated yet");
            
            if (selectedProvince != null) {
                // Check if wards already loaded
                if (selectedProvince.getWards() != null && !selectedProvince.getWards().isEmpty()) {
                    // Wards already cached
                    dialogWardComboBox.getItems().setAll(selectedProvince.getWards());
                    dialogWardComboBox.setDisable(false);
                } else {
                    // Lazy load wards from API
                    loadWardsForSelectedProvince(selectedProvince);
                }
            }
        });
    }
    
    /**
     * Lazy load wards for selected province
     */
    private void loadWardsForSelectedProvince(Province province) {
        dialogWardComboBox.setPromptText("Loading wards...");
        dialogWardComboBox.setDisable(true);
        
        new Thread(() -> {
            try {
                List<Ward> wards = provinceService.loadWardsForProvince(province.getCode());
                
                Platform.runLater(() -> {
                    dialogWardComboBox.getItems().setAll(wards);
                    dialogWardComboBox.setPromptText("Select ward/commune");
                    dialogWardComboBox.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    dialogWardComboBox.setPromptText("Failed to load wards");
                    dialogWardComboBox.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * Setup ward ComboBox with change listener
     */
    private void setupWardComboBox() {
        dialogWardComboBox.setOnAction(e -> {
            Ward selectedWard = dialogWardComboBox.getValue();
            
            if (selectedWard != null) {
                // Auto-calculate delivery fee when ward is selected
                calculateDeliveryFee();
            }
        });
    }
    
    private void calculateDeliveryFee() {
        Province province = dialogProvinceComboBox.getValue();
        if (province == null) {
            dialogDeliveryFeeLabel.setText("0 VND");
            return;
        }
        
        String provinceName = province.getName();
        
        // Use DeliveryInfo.calculateDeliveryFee() for consistent logic
        com.aimsfx.model.DeliveryInfo tempInfo = new com.aimsfx.model.DeliveryInfo();
        tempInfo.setProvince(provinceName);
        
        float deliveryFee = tempInfo.calculateDeliveryFee((float) totalWeight);
        
        // Apply free shipping discount if applicable
        if (totalAmount >= 100000) {
            float originalFee = deliveryFee;
            float discount = Math.min(deliveryFee, 25000f);
            deliveryFee = Math.max(0, deliveryFee - discount);
            
            // Show detailed breakdown with original fee and discount
            dialogDeliveryFeeLabel.setText(String.format(
                "%,.0f VND\n(Original: %,.0f VND - Discount: %,.0f VND)",
                deliveryFee, originalFee, discount));
            dialogDeliveryFeeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");
            
            System.out.println("[DEBUG] Dialog Fee Calculation:");
            System.out.println("  Original: " + originalFee + " VND");
            System.out.println("  Discount: " + discount + " VND");
            System.out.println("  Final: " + deliveryFee + " VND");
        } else {
            dialogDeliveryFeeLabel.setText(String.format("%,.0f VND", deliveryFee));
            dialogDeliveryFeeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        }
    }
    
    private void handleSave() {
        // Clear previous errors
        hideError(dialogNameError);
        hideError(dialogPhoneError);
        hideError(dialogEmailError);
        hideError(dialogProvinceError);
        hideError(dialogWardError);
        hideError(dialogAddressError);
        
        boolean valid = true;
        
        // Validate name
        String name = dialogNameField.getText().trim();
        if (name.isEmpty() || name.length() < 2) {
            showError(dialogNameError, "Please enter full name (at least 2 characters)");
            valid = false;
        }
        
        // Validate phone
        String phone = dialogPhoneField.getText().trim();
        if (!phone.matches("^0\\d{9}$")) {
            showError(dialogPhoneError, "Phone number must be 10 digits and start with 0");
            valid = false;
        }
        
        // Validate email (required and must be valid)
        String email = dialogEmailField.getText().trim();
        if (email.isEmpty()) {
            showError(dialogEmailError, "⚠️ Please enter your email address");
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(dialogEmailError, "❌ Invalid email format (e.g., user@example.com)");
            valid = false;
        }
        
        // Validate province
        if (dialogProvinceComboBox.getValue() == null) {
            showError(dialogProvinceError, "Please select province/city");
            valid = false;
        }
        
        // Validate ward
        if (dialogWardComboBox.getValue() == null) {
            showError(dialogWardError, "Please select ward/subdistrict");
            valid = false;
        }
        
        // Validate address
        String address = dialogAddressArea.getText().trim();
        if (address.isEmpty() || address.length() < 10) {
            showError(dialogAddressError, "Please enter detailed address (at least 10 characters)");
            valid = false;
        }
        
        if (valid) {
            saved = true;
            Stage stage = (Stage) dialogSaveButton.getScene().getWindow();
            stage.close();
        }
    }
    
    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }
    
    private void hideError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            errorLabel.setText("");
        }
    }
    
    // Getters for form data
    public boolean isSaved() {
        return saved;
    }
    
    public String getName() {
        return dialogNameField.getText().trim();
    }
    
    public String getPhone() {
        return dialogPhoneField.getText().trim();
    }
    
    public String getEmail() {
        return dialogEmailField.getText().trim();
    }
    
    public String getProvince() {
        Province p = dialogProvinceComboBox.getValue();
        return p != null ? p.getName() : "";
    }
    
    public String getSubDistrict() {
        Ward w = dialogWardComboBox.getValue();
        return w != null ? w.getName() : "";
    }
    
    public String getAddress() {
        return dialogAddressArea.getText().trim();
    }
    
    public String getDeliveryInstructions() {
        return dialogDeliveryInstructionsArea.getText().trim();
    }
    
    public String getDeliveryFee() {
        return dialogDeliveryFeeLabel.getText();
    }
}

package com.aimsfx.view;

import com.aimsfx.model.DeliveryInfo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Map;

/**
 * DeliveryForm View Class
 * Purpose: Render the delivery form UI (fields, validators, and action buttons)
 * and handle delivery information submission and updates.
 */
public class DeliveryForm extends BaseView {

    // FXML UI Components
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea addressArea;
    @FXML
    private ComboBox<String> provinceComboBox;
    @FXML
    private TextArea deliveryInstructionsArea;
    @FXML
    private Label deliveryFeeLabel;

    @FXML
    public void initialize() {
        // Initialize province combo box with available provinces
        if (provinceComboBox != null) {
            provinceComboBox.getItems().addAll(
                    "Hanoi", "Ho Chi Minh", "Da Nang", "Hai Phong", "Can Tho");
        }
    }

    /**
     * deliveryForm(): void
     * Purpose: Render delivery fields and prepare client-side validators
     * (email/phone format, required fields).
     * Usage: Instantiated when the user starts checkout or edits existing delivery
     * info.
     */
    public void deliveryForm() {
        // Render the delivery form UI
        // In JavaFX, this is typically handled by FXML loading and initialize()
        // This method can be used to reset/refresh the form
        clearForm();
        setupValidators();
    }

    /**
     * enterDeliveryInformation(form): void
     * Purpose: Submit a complete DeliveryInfo for the current order.
     * Processing (UI-side):
     * 1. Run client-side validation (non-empty, regex for email/phone).
     * 2. Build a DeliveryInfo DTO and send to
     * PlaceOrderController.submitDeliveryInfo().
     * 3. Handle response: on success, navigate to the next step (cart
     * review/payment);
     * on error, show messages per field.
     */
    public void enterDeliveryInformation() {
        try {
            // Step 1: Run client-side validation
            if (!validateForm()) {
                displayError("Please fill in all required fields correctly.");
                return;
            }

            // Step 2: Build DeliveryInfo DTO from form fields
            DeliveryInfo deliveryInfo = collectDeliveryInfoFromForm();

            if (deliveryInfo == null) {
                displayError("Failed to collect delivery information.");
                return;
            }

            // Step 3: Validate using model's validation method
            if (!deliveryInfo.checkValidityOfDeliveryInfo()) {
                displayError("Invalid delivery information. Please check all fields.");
                return;
            }

            // Step 4: Send to controller (would be called by controller in real
            // implementation)
            // controller.submitDeliveryInfo(deliveryInfo);
            displayInfo("Delivery information submitted successfully.");

        } catch (Exception e) {
            displayError("Error submitting delivery information: " + e.getMessage());
        }
    }

    /**
     * requestToUpdateInformation(updatedFields): void
     * Purpose: Allow the user to adjust fields after initial submission.
     * Processing (UI-side):
     * 1. Collect modified fields into updatedFields.
     * 2. Call PlaceOrderController to update and revalidate the underlying
     * DeliveryInfo.
     * 3. Refresh the view with updated values or highlight errors returned by the
     * controller.
     * 
     * @param updatedFields Map of field names to updated values
     */
    public void requestToUpdateInformation(Map<String, String> updatedFields) {
        try {
            if (updatedFields == null || updatedFields.isEmpty()) {
                displayError("No fields to update.");
                return;
            }

            // Validate updated fields
            if (!validateUpdatedFields(updatedFields)) {
                displayError("Invalid field values. Please check your input.");
                return;
            }

            // Apply updates to form fields
            applyUpdatesToForm(updatedFields);

            // Collect updated DeliveryInfo
            DeliveryInfo updatedInfo = collectDeliveryInfoFromForm();

            if (updatedInfo != null && updatedInfo.checkValidityOfDeliveryInfo()) {
                // Send to controller for revalidation and save
                // controller.updateDeliveryInfo(updatedInfo);
                displayInfo("Delivery information updated successfully.");
            } else {
                displayError("Updated information is invalid. Please correct the errors.");
            }

        } catch (Exception e) {
            displayError("Error updating delivery information: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Clear all form fields
     */
    private void clearForm() {
        if (nameField != null)
            nameField.clear();
        if (emailField != null)
            emailField.clear();
        if (phoneField != null)
            phoneField.clear();
        if (addressArea != null)
            addressArea.clear();
        if (provinceComboBox != null)
            provinceComboBox.setValue(null);
        if (deliveryInstructionsArea != null)
            deliveryInstructionsArea.clear();
        if (deliveryFeeLabel != null)
            deliveryFeeLabel.setText("0.00 VND");
    }

    /**
     * Setup client-side validators for form fields
     */
    private void setupValidators() {
        // Email format validator
        if (emailField != null) {
            emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    validateEmail(emailField.getText());
                }
            });
        }

        // Phone format validator
        if (phoneField != null) {
            phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    validatePhone(phoneField.getText());
                }
            });
        }
    }

    /**
     * Validate entire form
     * 
     * @return true if all fields are valid
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Required fields
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            highlightError(nameField);
            isValid = false;
        }

        if (emailField == null || !validateEmail(emailField.getText())) {
            highlightError(emailField);
            isValid = false;
        }

        if (phoneField == null || !validatePhone(phoneField.getText())) {
            highlightError(phoneField);
            isValid = false;
        }

        if (addressArea == null || addressArea.getText().trim().isEmpty()) {
            highlightError(addressArea);
            isValid = false;
        }

        if (provinceComboBox == null || provinceComboBox.getValue() == null) {
            highlightError(provinceComboBox);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if valid
     */
    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Validate phone number format
     * 
     * @param phone Phone number to validate
     * @return true if valid
     */
    private boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Vietnamese phone number: 10 digits starting with 0
        String phoneRegex = "^0\\d{9}$";
        return phone.matches(phoneRegex);
    }

    /**
     * Validate updated fields map
     * 
     * @param updatedFields Map of field names to values
     * @return true if all values are valid
     */
    private boolean validateUpdatedFields(Map<String, String> updatedFields) {
        for (Map.Entry<String, String> entry : updatedFields.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            switch (fieldName) {
                case "email":
                    if (!validateEmail(value))
                        return false;
                    break;
                case "phoneNumber":
                    if (!validatePhone(value))
                        return false;
                    break;
                case "recipientName":
                case "address":
                case "province":
                case "district":
                    if (value == null || value.trim().isEmpty())
                        return false;
                    break;
            }
        }
        return true;
    }

    /**
     * Apply updates from map to form fields
     * 
     * @param updatedFields Map of field names to values
     */
    private void applyUpdatesToForm(Map<String, String> updatedFields) {
        for (Map.Entry<String, String> entry : updatedFields.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            switch (fieldName) {
                case "recipientName":
                    if (nameField != null)
                        nameField.setText(value);
                    break;
                case "email":
                    if (emailField != null)
                        emailField.setText(value);
                    break;
                case "phoneNumber":
                    if (phoneField != null)
                        phoneField.setText(value);
                    break;
                case "address":
                    if (addressArea != null)
                        addressArea.setText(value);
                    break;
                case "province":
                    if (provinceComboBox != null)
                        provinceComboBox.setValue(value);
                    break;
                case "deliveryInstructions":
                    if (deliveryInstructionsArea != null)
                        deliveryInstructionsArea.setText(value);
                    break;
            }
        }
    }

    /**
     * Collect delivery information from form fields
     * 
     * @return DeliveryInfo object with form data
     */
    private DeliveryInfo collectDeliveryInfoFromForm() {
        try {
            DeliveryInfo info = new DeliveryInfo();

            if (nameField != null)
                info.setRecipientName(nameField.getText().trim());
            if (emailField != null)
                info.setEmail(emailField.getText().trim());
            if (phoneField != null)
                info.setPhoneNumber(phoneField.getText().trim());
            if (addressArea != null)
                info.setAddress(addressArea.getText().trim());
            if (provinceComboBox != null && provinceComboBox.getValue() != null) {
                info.setProvince(provinceComboBox.getValue());
            }
            if (deliveryInstructionsArea != null) {
                info.setDeliveryInstructions(deliveryInstructionsArea.getText().trim());
            }

            return info;

        } catch (Exception e) {
            displayError("Failed to collect delivery information: " + e.getMessage());
            return null;
        }
    }

    /**
     * Highlight field with error styling
     * 
     * @param control Control to highlight
     */
    private void highlightError(Control control) {
        if (control != null) {
            control.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    // ==================== Public Utility Methods (for Controller)
    // ====================

    // ==================== Public Utility Methods (for Controller)
    // ====================

    /**
     * Collect delivery information from form (for controller use)
     * 
     * @return DeliveryInfo object with form data
     */
    public DeliveryInfo collectDeliveryInfo() {
        return collectDeliveryInfoFromForm();
    }

    /**
     * Validate and collect delivery information
     * 
     * @return DeliveryInfo if valid, null otherwise
     */
    public DeliveryInfo getValidatedDeliveryInfo() {
        if (!validateForm()) {
            return null;
        }

        DeliveryInfo info = collectDeliveryInfoFromForm();

        if (info == null || !info.checkValidityOfDeliveryInfo()) {
            return null;
        }

        return info;
    }

    /**
     * Update delivery fee display
     * 
     * @param fee Delivery fee amount
     */
    public void updateDeliveryFeeDisplay(float fee) {
        if (deliveryFeeLabel != null) {
            deliveryFeeLabel.setText(String.format("%.2f VND", fee));
        }
    }

    /**
     * Show calculate delivery fee option
     * 
     * @return true if delivery fee can be calculated
     */
    public boolean showCalculateDeliveryFee() {
        DeliveryInfo info = collectDeliveryInfoFromForm();
        return info != null;
    }

    /**
     * Display delivery information
     * 
     * @param deliveryInfo Delivery information to display
     */
    public void displayDeliveryInfo(DeliveryInfo deliveryInfo) {
        if (deliveryInfo == null) {
            displayError("No delivery information available");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Delivery Information\n");
        info.append("=".repeat(50)).append("\n\n");
        info.append("Name: %s\n".formatted(deliveryInfo.getRecipientName()));
        info.append("Email: %s\n".formatted(deliveryInfo.getEmail()));
        info.append("Phone: %s\n".formatted(deliveryInfo.getPhoneNumber()));
        info.append("Address: %s\n".formatted(deliveryInfo.getAddress()));
        info.append("Province: %s\n".formatted(deliveryInfo.getProvince()));

        if (deliveryInfo.getDeliveryInstructions() != null && !deliveryInfo.getDeliveryInstructions().isEmpty()) {
            info.append("\nInstructions: %s\n".formatted(deliveryInfo.getDeliveryInstructions()));
        }

        displayInfo(info.toString());
    }
}

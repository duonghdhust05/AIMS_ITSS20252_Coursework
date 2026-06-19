package com.aimsfx.view;

import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.service.PlaceOrderService;
import com.aimsfx.utils.UIUtils;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class PlaceOrderDeliveryFormHandler {
    private final TextField nameField, phoneField, emailField, subDistrictField;
    private final ComboBox<String> provinceComboBox;
    private final TextArea addressArea, deliveryInstructionsArea;
    private final GridPane deliveryFormGrid;
    private final VBox deliveryInfoDisplay;
    private final Label displayNameLabel, displayPhoneLabel, displayEmailLabel;
    private final Label displayProvinceLabel, displaySubDistrictLabel, displayAddressLabel, displayInstructionsLabel;
    private final Button addDeliveryInfoButton;

    public PlaceOrderDeliveryFormHandler(TextField nameField, TextField phoneField, TextField emailField,
            TextField subDistrictField,
            ComboBox<String> provinceComboBox, TextArea addressArea, TextArea deliveryInstructionsArea,
            GridPane deliveryFormGrid, VBox deliveryInfoDisplay, Label displayNameLabel,
            Label displayPhoneLabel, Label displayEmailLabel, Label displayProvinceLabel,
            Label displaySubDistrictLabel, Label displayAddressLabel, Label displayInstructionsLabel,
            Button addDeliveryInfoButton) {
        this.nameField = nameField;
        this.phoneField = phoneField;
        this.emailField = emailField;
        this.subDistrictField = subDistrictField;
        this.provinceComboBox = provinceComboBox;
        this.addressArea = addressArea;
        this.deliveryInstructionsArea = deliveryInstructionsArea;
        this.deliveryFormGrid = deliveryFormGrid;
        this.deliveryInfoDisplay = deliveryInfoDisplay;
        this.displayNameLabel = displayNameLabel;
        this.displayPhoneLabel = displayPhoneLabel;
        this.displayEmailLabel = displayEmailLabel;
        this.displayProvinceLabel = displayProvinceLabel;
        this.displaySubDistrictLabel = displaySubDistrictLabel;
        this.displayAddressLabel = displayAddressLabel;
        this.displayInstructionsLabel = displayInstructionsLabel;
        this.addDeliveryInfoButton = addDeliveryInfoButton;
    }

    public void initProvinceComboBox() {
        if (provinceComboBox != null) {
            provinceComboBox.getItems().addAll("Hanoi", "Ho Chi Minh City", "Da Nang", "Hai Phong", "Can Tho",
                    "An Giang", "Ba Ria - Vung Tau", "Bac Giang", "Bac Kan", "Bac Lieu", "Bac Ninh", "Ben Tre",
                    "Binh Dinh");
        }
    }

    public void restoreDeliveryInfoToForm(DeliveryInfo deliveryInfo) {
        if (deliveryInfo == null)
            return;
        if (deliveryInfo.getRecipientName() != null && nameField != null)
            nameField.setText(deliveryInfo.getRecipientName());
        if (deliveryInfo.getPhoneNumber() != null && phoneField != null)
            phoneField.setText(deliveryInfo.getPhoneNumber());
        if (deliveryInfo.getEmail() != null && emailField != null)
            emailField.setText(deliveryInfo.getEmail());
        if (deliveryInfo.getProvince() != null && provinceComboBox != null)
            provinceComboBox.setValue(deliveryInfo.getProvince());
        if (deliveryInfo.getWard() != null && subDistrictField != null)
            subDistrictField.setText(deliveryInfo.getWard());
        if (deliveryInfo.getAddress() != null && addressArea != null)
            addressArea.setText(deliveryInfo.getAddress());
        if (deliveryInfo.getDeliveryInstructions() != null && deliveryInstructionsArea != null)
            deliveryInstructionsArea.setText(deliveryInfo.getDeliveryInstructions());

        if (deliveryInfo.getRecipientName() != null && !deliveryInfo.getRecipientName().trim().isEmpty()) {
            updateDeliveryDisplayAndSwitchMode(deliveryInfo);
        }
    }

    public void updateDeliveryDisplayAndSwitchMode(DeliveryInfo info) {
        if (info == null)
            return;
        if (displayNameLabel != null)
            displayNameLabel.setText(info.getRecipientName() != null ? info.getRecipientName() : "-");
        if (displayPhoneLabel != null)
            displayPhoneLabel.setText(info.getPhoneNumber() != null ? info.getPhoneNumber() : "-");
        if (displayEmailLabel != null)
            displayEmailLabel
                    .setText(info.getEmail() != null && !info.getEmail().trim().isEmpty() ? info.getEmail() : "-");
        if (displayProvinceLabel != null)
            displayProvinceLabel.setText(info.getProvince() != null ? info.getProvince() : "-");
        if (displaySubDistrictLabel != null)
            displaySubDistrictLabel
                    .setText(info.getWard() != null && !info.getWard().trim().isEmpty() ? info.getWard() : "-");
        if (displayAddressLabel != null)
            displayAddressLabel.setText(info.getAddress() != null ? info.getAddress() : "-");
        if (displayInstructionsLabel != null)
            displayInstructionsLabel
                    .setText(info.getDeliveryInstructions() != null && !info.getDeliveryInstructions().trim().isEmpty()
                            ? info.getDeliveryInstructions()
                            : "-");

        if (deliveryInfoDisplay != null) {
            deliveryInfoDisplay.setVisible(true);
            deliveryInfoDisplay.setManaged(true);
        }
        if (deliveryFormGrid != null) {
            deliveryFormGrid.setVisible(false);
            deliveryFormGrid.setManaged(false);
        }
        if (addDeliveryInfoButton != null) {
            addDeliveryInfoButton.setText("Edit Delivery Information");
        }
    }

    public boolean validateDeliveryInfo(PlaceOrderService placeOrderService, float originalDeliveryFee,
            String deliveryFeeText) {
        List<String> errors = placeOrderService.validateDeliveryInfo(
                nameField.getText(), phoneField.getText(), emailField.getText(),
                provinceComboBox.getValue(), addressArea.getText());

        if ((deliveryFeeText == null || deliveryFeeText.equals("0.00 VND"))
                && originalDeliveryFee == 0) {
            errors.add("Please click 'Calculate Delivery Fee' before placing order.");
        }

        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors)
                errorMsg.append("• ").append(error).append("\n");
            UIUtils.showAlert("Invalid Information", "Please check delivery information:\n\n" + errorMsg);
            return false;
        }
        return true;
    }

    public void updateFormFromDialog(DeliveryInfoDialogUI controller) {
        nameField.setText(controller.getName());
        phoneField.setText(controller.getPhone());
        emailField.setText(controller.getEmail());
        provinceComboBox.setValue(controller.getProvince());
        subDistrictField.setText(controller.getSubDistrict());
        addressArea.setText(controller.getAddress());
        deliveryInstructionsArea.setText(controller.getDeliveryInstructions());
    }

    public String getName() {
        return nameField.getText();
    }

    public String getPhone() {
        return phoneField.getText();
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getProvince() {
        return provinceComboBox.getValue();
    }

    public String getSubDistrict() {
        return subDistrictField.getText();
    }

    public String getAddress() {
        return addressArea.getText();
    }

    public String getDeliveryInstructions() {
        return deliveryInstructionsArea.getText();
    }
}

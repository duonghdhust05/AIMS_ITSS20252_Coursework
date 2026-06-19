package com.aimsfx.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class OrderRejectDialogUI {

    @FXML
    private ComboBox<String> reasonComboBox;

    @FXML
    private TextArea customReasonArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private boolean confirmed = false;
    private String finalReason = null;

    @FXML
    public void initialize() {
        reasonComboBox.setItems(FXCollections.observableArrayList(
                "Out of stock",
                "Cannot deliver to the specified address",
                "Customer requested cancellation",
                "Suspected fraud",
                "Other (specify below)"
        ));

        // When a reason is selected, clear custom reason if it's not "Other"
        reasonComboBox.setOnAction(e -> {
            String selected = reasonComboBox.getValue();
            if (selected != null && !selected.startsWith("Other")) {
                customReasonArea.clear();
            }
        });
    }

    @FXML
    private void handleConfirm() {
        String selected = reasonComboBox.getValue();
        String custom = customReasonArea.getText().trim();

        if (selected == null && custom.isEmpty()) {
            showError("Please select or enter a rejection reason.");
            return;
        }

        if (selected != null && selected.startsWith("Other") && custom.isEmpty()) {
            showError("Please specify the reason in the text area.");
            return;
        }

        finalReason = !custom.isEmpty() ? custom : selected;
        confirmed = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeDialog();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getReason() {
        return finalReason;
    }
}

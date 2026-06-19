package com.aimsfx.view.ProductView;

import com.aimsfx.controller.ProductManagerController.ProductController;
import com.aimsfx.model.Product;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.model.meta.InputType;
import com.aimsfx.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UpdateProductFormView - Update existing product form
 * 
 * REFACTORED: Now UI-only
 * - Collects user input from JavaFX controls
 * - Calls ONE controller method to submit (handleUpdateProduct /
 * handleUpdateStock)
 * - Does NOT parse numbers or construct ProductDTO
 * - Does NOT perform orchestrations
 */
public class UpdateProductFormView {

    // Common Fields
    @FXML
    private TextField productIdField;
    @FXML
    private TextField productTypeField;
    @FXML
    private TextField barcodeField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField originalPriceField;
    @FXML
    private TextField currentPriceField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField weightField;
    @FXML
    private TextField dimensionsField;
    @FXML
    private TextField stockField;
    @FXML
    private Button updateStockButton;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField vatRateField;

    // Specific Fields Section
    @FXML
    private VBox specificFieldsSection;
    @FXML
    private Label specificFieldsTitle;
    @FXML
    private GridPane specificFieldsGrid;

    // OCP SOLUTION: Dynamic fields storage by key (not by index)
    private Map<String, Control> dynamicControls = new HashMap<>();

    // Legacy support: Keep for backward compatibility
    private List<Control> specificFieldControls = new ArrayList<>();

    private ProductController controller;
    private String productType;
    private Runnable onProductUpdated;

    @FXML
    public void initialize() {
        controller = ProductController.getInstance();

        // Initialize status combo with only two valid values
        statusComboBox.getItems().addAll("available", "deactivated");

        // Make stock field read-only
        stockField.setEditable(false);
        stockField.setStyle("-fx-background-color: #f0f0f0;");

        // Setup update stock button
        if (updateStockButton != null) {
            updateStockButton.setOnAction(e -> handleUpdateStock());
        }
    }

    /**
     * Set callback to run after product is updated
     */
    public void setOnProductUpdated(Runnable callback) {
        this.onProductUpdated = callback;
    }

    /**
     * Set product data by ID (alias for loadProduct)
     */
    public void setProductData(Long productId) {
        loadProduct(productId);
    }

    /**
     * Load product data by ID
     */
    public void loadProduct(Long productId) {
        try {
            // Get product details from controller (not repository!)
            Map<String, Object> productData = controller.getProductDetails(productId);

            if (productData == null) {
                UIUtils.showError("Error", "Product not found!");
                handleCancel();
                return;
            }

            // Load product type
            this.productType = getString(productData, "product_type");

            // Populate common fields
            productIdField.setText(productId.toString());
            productTypeField.setText(productType);
            barcodeField.setText(getString(productData, "barcode"));
            titleField.setText(getString(productData, "title"));
            categoryField.setText(getString(productData, "category"));
            originalPriceField.setText(getString(productData, "original_price"));
            currentPriceField.setText(getString(productData, "current_price"));
            descriptionField.setText(getString(productData, "description"));
            weightField.setText(getString(productData, "weight"));
            dimensionsField.setText(getString(productData, "dimensions"));
            stockField.setText(getString(productData, "stock"));
            statusComboBox.setValue(getString(productData, "status"));
            vatRateField.setText(getString(productData, "vat_rate"));

            // Generate and populate specific fields
            generateSpecificFields();
            populateSpecificFields(productData);

        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to load product: " + e.getMessage());
            e.printStackTrace();
            handleCancel();
        }
    }

    /**
     * Generate specific fields based on product type
     * 
     * OCP SOLUTION: Uses metadata from Factory instead of hardcoded conditions
     */
    private void generateSpecificFields() {
        try {
            // Clear previous fields
            specificFieldsGrid.getChildren().clear();
            dynamicControls.clear();
            specificFieldControls.clear();

            // Get metadata configuration from Controller (not directly from Factory!)
            List<AttributeMeta> configs = controller.getAttributeConfig(productType);

            // OCP: Render fields based on metadata (no product-type checks)
            for (int i = 0; i < configs.size(); i++) {
                AttributeMeta meta = configs.get(i);

                Label label = new Label(meta.getLabel());
                label.setStyle("-fx-font-size: 13px;");

                // Create Input Control based on InputType
                Control inputControl = createInputControl(meta);

                // Store control by KEY (not by index)
                dynamicControls.put(meta.getKey(), inputControl);
                specificFieldControls.add(inputControl); // Legacy support

                specificFieldsGrid.add(label, 0, i);
                specificFieldsGrid.add(inputControl, 1, i);
            }

            // Update title
            specificFieldsTitle.setText(productType + " Specific Information");

        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to generate fields: " + e.getMessage());
        }
    }

    /**
     * Create input control based on AttributeMeta
     */
    private Control createInputControl(AttributeMeta meta) {
        return switch (meta.getInputType()) {
            case COMBOBOX -> {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(meta.getOptions()));
                cb.setPromptText(meta.getPlaceholder());
                cb.setPrefWidth(300);
                yield cb;
            }
            case NUMBER -> {
                TextField tf = new TextField();
                tf.setPromptText(meta.getPlaceholder());
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        tf.setText(oldVal);
                    }
                });
                yield tf;
            }
            case READONLY -> {
                TextField tf = new TextField();
                tf.setEditable(false);
                tf.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
                tf.setPromptText(meta.getPlaceholder());
                yield tf;
            }
            case DATE -> {
                TextField tf = new TextField();
                tf.setPromptText(meta.getPlaceholder());
                yield tf;
            }
            default -> { // TEXT
                TextField tf = new TextField();
                tf.setPromptText(meta.getPlaceholder());
                yield tf;
            }
        };
    }

    /**
     * Populate specific fields with existing data
     */
    /**
     * Populate specific fields with existing data
     * 
     * OCP SOLUTION: Uses key-based lookup from dynamicControls
     * BEFORE: switch (productType) { case "BOOK" -> setValue(0, ...); }
     * AFTER: for each key: setValueByKey(key, productData.get("specific_" + key))
     */
    private void populateSpecificFields(Map<String, Object> productData) {
        // Get attribute config from Controller (not directly from Factory!)
        List<AttributeMeta> configs = controller.getAttributeConfig(productType);

        for (AttributeMeta meta : configs) {
            String key = meta.getKey();
            String dataKey = "specific_" + key;

            // Special handling for CD trackCount - load from tracks table
            if ("CD".equals(productType) && "trackCount".equals(key)) {
                String barcode = getString(productData, "barcode");
                if (barcode != null && !barcode.isEmpty()) {
                    List<Track> tracks = controller.getTracksByBarcode(barcode);
                    setValueByKey(key, String.valueOf(tracks.size()));
                } else {
                    setValueByKey(key, getString(productData, dataKey));
                }
            }
            // Handle date fields
            else if (meta.getInputType() == InputType.DATE) {
                setValueByKey(key, getDateString(productData, dataKey));
            }
            // Handle all other fields
            else {
                setValueByKey(key, getString(productData, dataKey));
            }
        }
    }

    /**
     * Set value to a specific field control by key
     * OCP SOLUTION: Uses key-based lookup instead of index
     */
    private void setValueByKey(String key, String value) {
        Control control = dynamicControls.get(key);
        if (control == null)
            return;

        if (control instanceof TextField tf) {
            tf.setText(value != null ? value : "");
        } else if (control instanceof ComboBox<?>) {
            @SuppressWarnings("unchecked")
            ComboBox<String> cb = (ComboBox<String>) control;
            cb.setValue(value);
        }
    }

    /**
     * Set value to a specific field control by index
     * 
     * @deprecated Use setValueByKey() instead
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void setValue(int index, String value) {
        if (index < specificFieldControls.size()) {
            Control control = specificFieldControls.get(index);
            if (control instanceof TextField) {
                ((TextField) control).setText(value);
            } else if (control instanceof ComboBox) {
                @SuppressWarnings("unchecked")
                ComboBox<String> comboBox = (ComboBox<String>) control;
                comboBox.setValue(value);
            }
        }
    }

    /**
     * Handle update product button
     * 
     * REFACTORED: Now UI-only
     * - Collects raw input from UI controls into Maps
     * - Calls ONE controller method: handleUpdateProduct()
     * - Does NOT parse numbers or build ProductDTO
     * - Controller handles all parsing, validation, and orchestration
     */
    @FXML
    private void handleUpdate() {
        try {
            // Step 1: Collect common fields as raw strings (no parsing!)
            Map<String, String> commonFields = new HashMap<>();
            commonFields.put("barcode", barcodeField.getText().trim());
            commonFields.put("title", titleField.getText().trim());
            commonFields.put("category", categoryField.getText().trim());
            commonFields.put("originalPrice", originalPriceField.getText().trim());
            commonFields.put("currentPrice", currentPriceField.getText().trim());
            commonFields.put("description", descriptionField.getText().trim());
            commonFields.put("weight", weightField.getText().trim());
            commonFields.put("dimensions", dimensionsField.getText().trim());
            commonFields.put("status", statusComboBox.getValue());
            commonFields.put("vatRate", vatRateField.getText().trim());

            // Step 2: Collect specific attributes as Map
            Map<String, String> specificAttributes = collectSpecificAttributesAsMap();

            // Step 3: Call controller ONCE - controller handles everything
            Product product = controller.handleUpdateProduct(
                    productIdField.getText().trim(),
                    productType,
                    commonFields,
                    specificAttributes);

            // Step 4: Show success
            UIUtils.showAlert("Success", "Product updated successfully!\nBarcode: " + product.getBarcode());

            // Step 5: Trigger callback
            if (onProductUpdated != null) {
                onProductUpdated.run();
            }

            // Step 6: Close form
            handleCancel();

        } catch (IllegalArgumentException e) {
            // Controller throws IllegalArgumentException with user-friendly messages
            UIUtils.showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to update product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Collect specific attributes from dynamic fields as Map
     * OCP SOLUTION: Uses key-based lookup instead of index-based
     */
    private Map<String, String> collectSpecificAttributesAsMap() {
        Map<String, String> attributesMap = new HashMap<>();

        for (Map.Entry<String, Control> entry : dynamicControls.entrySet()) {
            String key = entry.getKey();
            Control control = entry.getValue();
            String value = extractValueFromControl(control);
            attributesMap.put(key, value);
        }

        return attributesMap;
    }

    /**
     * Extract value from any Control type
     */
    private String extractValueFromControl(Control control) {
        if (control instanceof TextField tf) {
            return tf.getText().trim();
        } else if (control instanceof ComboBox<?> cb) {
            Object value = cb.getValue();
            return value != null ? value.toString() : "";
        } else if (control instanceof DatePicker dp) {
            return dp.getValue() != null ? dp.getValue().toString() : "";
        }
        return "";
    }

    /**
     * Collect specific attributes from dynamic fields
     * 
     * @deprecated Use collectSpecificAttributesAsMap() instead
     */
    @SuppressWarnings("unused")
    @Deprecated
    private String[] collectSpecificAttributes() {
        String[] attributes = new String[specificFieldControls.size()];

        for (int i = 0; i < specificFieldControls.size(); i++) {
            Control control = specificFieldControls.get(i);

            if (control instanceof TextField) {
                attributes[i] = ((TextField) control).getText().trim();
            } else if (control instanceof ComboBox) {
                @SuppressWarnings("unchecked")
                ComboBox<String> comboBox = (ComboBox<String>) control;
                attributes[i] = comboBox.getValue() != null ? comboBox.getValue() : "";
            }
        }

        return attributes;
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    /**
     * Handle update stock button - opens dialog to update stock with reason
     * 
     * REFACTORED: Now UI-only
     * - Collects raw input from dialog
     * - Calls ONE controller method: handleUpdateStock()
     * - Does NOT parse numbers
     * - Controller handles all parsing and validation
     */
    private void handleUpdateStock() {
        try {
            // Create dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Update Stock");
            dialog.setHeaderText("Update Stock for Product");

            // Create form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            Label currentStockLabel = new Label("Current Stock:");
            Label currentStockValue = new Label(stockField.getText().trim());
            currentStockValue.setStyle("-fx-font-weight: bold;");

            Label newStockLabel = new Label("New Stock:");
            TextField newStockField = new TextField();
            newStockField.setPromptText("Enter new stock quantity");

            Label reasonLabel = new Label("Reason:");
            TextArea reasonField = new TextArea();
            reasonField.setPromptText("Enter reason for stock change (required)");
            reasonField.setPrefRowCount(3);

            grid.add(currentStockLabel, 0, 0);
            grid.add(currentStockValue, 1, 0);
            grid.add(newStockLabel, 0, 1);
            grid.add(newStockField, 1, 1);
            grid.add(reasonLabel, 0, 2);
            grid.add(reasonField, 1, 2);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Show dialog and process result
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Collect raw inputs (no parsing in View!)
                        String barcode = barcodeField.getText().trim();
                        String newStockRaw = newStockField.getText().trim();
                        String reason = reasonField.getText().trim();

                        // Call controller ONCE - controller handles parsing and validation
                        controller.handleUpdateStock(barcode, newStockRaw, reason);

                        // Update display - refetch the value to ensure consistency
                        stockField.setText(newStockRaw);

                        // Show success
                        UIUtils.showAlert("Success", "Stock updated successfully!");

                    } catch (IllegalArgumentException e) {
                        // Controller throws IllegalArgumentException with user-friendly messages
                        UIUtils.showError("Validation Error", e.getMessage());
                    } catch (Exception e) {
                        UIUtils.showError("Error", "Failed to update stock: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to open stock update dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility methods

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    private String getDateString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null)
            return "";

        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format((Date) value);
        }

        return value.toString();
    }


}

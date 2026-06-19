package com.aimsfx.view.ProductView;

import com.aimsfx.controller.ProductManagerController.ProductController;
import com.aimsfx.model.Product;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductFormView - Add new product form
 * 
 * REFACTORED: Now UI-only
 * - Collects user input from JavaFX controls
 * - Calls ONE controller method to submit
 * - Does NOT parse numbers or construct ProductDTO
 * - Does NOT call multiple controller methods for one use case
 */
public class ProductFormView {

    // Common Fields
    @FXML
    private ComboBox<String> productTypeComboBox;
    @FXML
    private TextField barcodeField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
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
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField vatRateField;
    @FXML
    private VBox commonFieldsContainer;

    // Specific Fields Section
    @FXML
    private VBox specificFieldsSection;
    @FXML
    private Label specificFieldsTitle;
    @FXML
    private GridPane specificFieldsGrid;
    @FXML
    private Button addProductButton;

    // CD Tracks Section
    @FXML
    private VBox tracksSection;
    @FXML
    private TextField trackTitleField;
    @FXML
    private TextField trackDurationField;
    @FXML
    private VBox tracksList;
    @FXML
    private Label tracksCountLabel;
    @FXML
    private VBox tracksListContainer;

    // OCP SOLUTION: Dynamic fields storage by key (not by index)
    // Key: attributeKey (e.g., "author", "coverType")
    // Value: Control (TextField, ComboBox, DatePicker)
    private Map<String, Control> dynamicControls = new HashMap<>();

    // Legacy support: Keep for backward compatibility during transition
    private List<Control> specificFieldControls = new ArrayList<>();

    private List<Track> cdTracks = new ArrayList<>();

    private ProductController controller;
    private Runnable onProductAdded;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        controller = ProductController.getInstance();

        // Initialize product type combo
        productTypeComboBox.getItems().addAll(controller.getSupportedTypes());
        productTypeComboBox.setOnAction(e -> handleProductTypeChange());

        // Hide status field - it will be set automatically to "available" when creating
        // product
        if (statusComboBox != null) {
            statusComboBox.setVisible(false);
            statusComboBox.setManaged(false);
        }

        // Set default values
        vatRateField.setText("10.0");

        // Start in type-selection state only
        showTypeSelectionState();
    }

    /**
     * Set product type (called from external view)
     */
    public void setProductType(String type) {
        productTypeComboBox.setValue(type);
        handleProductTypeChange();
    }

    /*
     * Set the owning dialog stage so this view can close itself reliably.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set callback to run after product is added
     */
    public void setOnProductAdded(Runnable callback) {
        this.onProductAdded = callback;
    }

    /**
     * Handle product type change - generate specific fields dynamically
     * 
     * OCP SOLUTION: Uses metadata from Factory instead of hardcoded conditions
     * BEFORE: if (selectedType.equals("BOOK") && i == 5) { ... } // Content
     * Coupling
     * AFTER: switch (meta.getInputType()) { ... } // OCP Compliant
     */
    private void handleProductTypeChange() {
        String selectedType = productTypeComboBox.getValue();
        if (selectedType == null) {
            showTypeSelectionState();
            return;
        }

        try {

            showFormSections();

            // Clear previous fields
            specificFieldsGrid.getChildren().clear();
            dynamicControls.clear();
            specificFieldControls.clear();
            cdTracks.clear();
            tracksList.getChildren().clear();
            updateTracksCount();

            // Get metadata configuration from Controller (not directly from Factory!)
            List<AttributeMeta> configs = controller.getAttributeConfig(selectedType);

            // OCP: Render fields based on metadata (no product-type checks)
            for (int i = 0; i < configs.size(); i++) {
                AttributeMeta meta = configs.get(i);

                // Create Label
                Label label = new Label(meta.getLabel());
                label.setStyle("-fx-font-size: 13px;");

                // Create Input Control based on InputType (OCP: no "BOOK", "CD" checks)
                Control inputControl = createInputControl(meta);

                // Store control by KEY (not by index)
                dynamicControls.put(meta.getKey(), inputControl);
                specificFieldControls.add(inputControl); // Legacy support

                // Add to Grid
                specificFieldsGrid.add(label, 0, i);
                specificFieldsGrid.add(inputControl, 1, i);
            }

            // Show specific fields section
            specificFieldsTitle.setText(selectedType + " Specific Information");
            specificFieldsSection.setVisible(true);
            specificFieldsSection.setManaged(true);

            // Show tracks section only for CD products
            if (selectedType.equals("CD")) {
                tracksSection.setVisible(true);
                tracksSection.setManaged(true);
            } else {
                tracksSection.setVisible(false);
                tracksSection.setManaged(false);
            }
            if (addProductButton != null) {
                addProductButton.setDisable(false);
            }

        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to load product fields: " + e.getMessage());
        }
    }

    /**
     * Create input control based on AttributeMeta
     * 
     * OCP SOLUTION: Switch on InputType enum, not product type strings
     * Adding new InputType = Add case here
     * Adding new ProductType = No changes needed here!
     */
    private Control createInputControl(AttributeMeta meta) {
        return switch (meta.getInputType()) {
            case COMBOBOX -> {
                ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(meta.getOptions()));
                cb.setPromptText(meta.getPlaceholder());
                cb.setPrefWidth(300);
                cb.getSelectionModel().selectFirst();
                yield cb;
            }
            case NUMBER -> {
                TextField tf = new TextField();
                tf.setPromptText(meta.getPlaceholder());
                // Restrict to numbers only
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        tf.setText(oldVal);
                    }
                });
                yield tf;
            }
            case READONLY -> {
                TextField tf = new TextField();
                tf.setText("0"); // Default value
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
     * Handle add product button
     * - Collects raw input from UI controls into Maps
     * - Calls ONE controller method: handleAddProduct()
     * - Does NOT parse numbers or build ProductDTO
     * - Controller handles all parsing, validation, and orchestration
     */
    @FXML
    private void handleAdd() {
        try {
            // Step 1: Collect product type
            String productType = productTypeComboBox.getValue();
            if (productType == null) {
                UIUtils.showError("Validation Error", "Please select a product type!");
                return;
            }

            // Step 2: Collect common fields as raw strings (no parsing!)
            Map<String, String> commonFields = new HashMap<>();
            commonFields.put("barcode", barcodeField.getText().trim());
            commonFields.put("title", titleField.getText().trim());
            commonFields.put("category", categoryField.getText().trim());
            commonFields.put("price", priceField.getText().trim());
            commonFields.put("description", descriptionField.getText().trim());
            commonFields.put("weight", weightField.getText().trim());
            commonFields.put("dimensions", dimensionsField.getText().trim());
            commonFields.put("stock", stockField.getText().trim());
            commonFields.put("vatRate", vatRateField.getText().trim());

            // Step 3: Collect specific attributes as Map
            Map<String, String> specificAttributes = collectSpecificAttributesAsMap();

            // Step 4: Collect tracks (for CD only)
            List<Track> tracks = new ArrayList<>(cdTracks);

            // Step 5: Call controller ONCE - controller handles everything
            Product product = controller.handleAddProduct(
                    productType,
                    commonFields,
                    specificAttributes,
                    tracks);

            // Step 6: Show success
            String successMessage = "Product added successfully!\nBarcode: " + product.getBarcode();
            if ("CD".equals(productType)) {
                successMessage += "\nTracks added: " + cdTracks.size();
            }
            UIUtils.showAlert("Success", successMessage);

            // Step 7: Trigger callback
            if (onProductAdded != null) {
                onProductAdded.run();
            }

            // Step 8: Close form
            handleCancel();

        } catch (IllegalArgumentException e) {
            // Controller throws IllegalArgumentException with user-friendly messages
            UIUtils.showError("Validation Error", e.getMessage());
        } catch (Exception e) {
            UIUtils.showError("Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Collect specific attributes from dynamic fields as Map
     * 
     * OCP SOLUTION: Uses key-based lookup instead of index-based
     * BEFORE: attributes[i] = ((TextField) specificFieldControls.get(i)).getText();
     * AFTER: attributesMap.put(key, getValue(control));
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
     * Handle add track button
     */
    @FXML
    private void handleAddTrack() {
        String title = trackTitleField.getText().trim();
        String durationStr = trackDurationField.getText().trim();

        // Validate track fields
        if (title.isEmpty()) {
            UIUtils.showError("Validation Error", "Track title is required!");
            trackTitleField.requestFocus();
            return;
        }

        if (durationStr.isEmpty()) {
            UIUtils.showError("Validation Error", "Track duration is required!");
            trackDurationField.requestFocus();
            return;
        }

        try {
            Integer duration = Integer.parseInt(durationStr);

            if (duration <= 0) {
                UIUtils.showError("Validation Error", "Duration must be greater than 0 seconds!");
                trackDurationField.requestFocus();
                return;
            }

            // Create track (barcode will be set when product is created)
            Track track = new Track(null, null, title, duration);
            cdTracks.add(track);

            // Add track to UI list
            addTrackToList(track, cdTracks.size() - 1);

            // Clear input fields
            trackTitleField.clear();
            trackDurationField.clear();
            trackTitleField.requestFocus();

            // Update tracks count
            updateTracksCount();

            // Update Track Count field (index 3 for CD products)
            updateCDTrackCountField();

        } catch (NumberFormatException e) {
            UIUtils.showError("Validation Error", "Duration must be a valid number (in seconds)!");
            trackDurationField.requestFocus();
        }
    }

    /**
     * Add track to the visual list
     */
    private void addTrackToList(Track track, int index) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/cd-track-item.fxml"));
            HBox trackItem = loader.load();
            com.aimsfx.controller.ProductManagerController.CDTrackItemController controller = loader.getController();

            controller.setTrackData(track, index, (idx) -> {
                cdTracks.remove(idx.intValue());
                refreshTracksList();
            });

            tracksList.getChildren().add(trackItem);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Could not load track item UI.");
        }
    }

    /**
     * Refresh tracks list display
     */
    private void refreshTracksList() {
        tracksList.getChildren().clear();
        for (int i = 0; i < cdTracks.size(); i++) {
            addTrackToList(cdTracks.get(i), i);
        }
        updateTracksCount();
        updateCDTrackCountField();
    }

    /**
     * Update tracks count label
     */
    private void updateTracksCount() {
        tracksCountLabel.setText("Tracks: " + cdTracks.size());
    }

    /**
     * Update CD Track Count field with current track count
     * 
     * OCP SOLUTION: Uses key-based lookup instead of magic index
     * BEFORE: specificFieldControls.get(3) // Magic number!
     * AFTER: dynamicControls.get("trackCount") // Clear intent
     */
    private void updateCDTrackCountField() {
        String productType = productTypeComboBox.getValue();
        if ("CD".equals(productType)) {
            Control trackCountControl = dynamicControls.get("trackCount");
            if (trackCountControl instanceof TextField tf) {
                tf.setText(String.valueOf(cdTracks.size()));
            }
        }
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
            return;
        }
        Stage stage = (Stage) barcodeField.getScene().getWindow();
        stage.close();
    }

    /**
     * Initial state: only product type selector is available.
     */
    private void showTypeSelectionState() {
        if (commonFieldsContainer != null) {
            commonFieldsContainer.setVisible(false);
            commonFieldsContainer.setManaged(false);
        }
        if (specificFieldsSection != null) {
            specificFieldsSection.setVisible(false);
            specificFieldsSection.setManaged(false);
        }
        if (tracksSection != null) {
            tracksSection.setVisible(false);
            tracksSection.setManaged(false);
        }
        if (addProductButton != null) {
            addProductButton.setDisable(true);
        }
    }

    /**
     * Form state after a product type has been chosen.
     */
    private void showFormSections() {
        if (commonFieldsContainer != null) {
            commonFieldsContainer.setVisible(true);
            commonFieldsContainer.setManaged(true);
        }
    }

    // Utility methods


}

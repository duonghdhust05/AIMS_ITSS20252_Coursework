package com.aimsfx.model.meta;

import java.util.Collections;
import java.util.List;

/**
 * AttributeMeta - Metadata class describing a product attribute
 * 
 * PURPOSE: Replaces hardcoded index-based logic with declarative configuration
 * 
 * BEFORE (Content Coupling - BAD):
 *   if (selectedType.equals("BOOK") && i == 5) {  // Magic number!
 *       ComboBox<String> comboBox = new ComboBox<>();
 *       comboBox.getItems().addAll("Paperback", "Hardcover");
 *   }
 * 
 * AFTER (OCP Compliant - GOOD):
 *   AttributeMeta meta = configs.get(i);
 *   switch (meta.getInputType()) {
 *       case COMBOBOX -> new ComboBox<>(meta.getOptions());
 *       case NUMBER -> createNumberField();
 *       default -> new TextField();
 *   }
 * 
 * BENEFITS:
 * - View doesn't know about specific product types (no "BOOK", "CD" checks)
 * - Factory owns all product metadata (Single Source of Truth)
 * - Adding new product types = Adding new Factory only
 * - Type-safe: InputType enum vs magic index numbers
 */
public class AttributeMeta {
    
    private final String key;           // ID for DTO mapping (e.g., "coverType")
    private final String label;         // Display label (e.g., "Cover Type:")
    private final InputType inputType;  // Type of UI control
    private final List<String> options; // Options for COMBOBOX type
    private final String placeholder;   // Placeholder/hint text
    
    /**
     * Constructor for TEXT, NUMBER, DATE, READONLY types
     * 
     * @param key       Unique identifier for DTO mapping
     * @param label     Display label for the field
     * @param inputType Type of input control
     */
    public AttributeMeta(String key, String label, InputType inputType) {
        this.key = key;
        this.label = label;
        this.inputType = inputType;
        this.options = Collections.emptyList();
        this.placeholder = "Enter " + label.replace(":", "").toLowerCase();
    }
    
    /**
     * Constructor for TEXT, NUMBER, DATE, READONLY types with custom placeholder
     * 
     * @param key         Unique identifier for DTO mapping
     * @param label       Display label for the field
     * @param inputType   Type of input control
     * @param placeholder Custom placeholder text
     */
    public AttributeMeta(String key, String label, InputType inputType, String placeholder) {
        this.key = key;
        this.label = label;
        this.inputType = inputType;
        this.options = Collections.emptyList();
        this.placeholder = placeholder;
    }
    
    /**
     * Constructor for COMBOBOX type
     * 
     * @param key     Unique identifier for DTO mapping
     * @param label   Display label for the field
     * @param options List of selectable options
     */
    public AttributeMeta(String key, String label, List<String> options) {
        this.key = key;
        this.label = label;
        this.inputType = InputType.COMBOBOX;
        this.options = options != null ? List.copyOf(options) : Collections.emptyList();
        this.placeholder = "Select " + label.replace(":", "").toLowerCase();
    }
    
    // ==================== Getters ====================
    
    /**
     * Get the unique key for DTO mapping
     * @return Key identifier (e.g., "author", "coverType")
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Get the display label
     * @return Label text (e.g., "Author:", "Cover Type:")
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Get the input type
     * @return InputType enum value
     */
    public InputType getInputType() {
        return inputType;
    }
    
    /**
     * Get the options for COMBOBOX type
     * @return Immutable list of options (empty for non-COMBOBOX types)
     */
    public List<String> getOptions() {
        return options;
    }
    
    /**
     * Get the placeholder text
     * @return Placeholder/hint text for the input
     */
    public String getPlaceholder() {
        return placeholder;
    }
    
    /**
     * Check if this attribute has selectable options
     * @return true if COMBOBOX type with options
     */
    public boolean hasOptions() {
        return inputType == InputType.COMBOBOX && !options.isEmpty();
    }
    
    @Override
    public String toString() {
        return "AttributeMeta{" +
               "key='" + key + '\'' +
               ", label='" + label + '\'' +
               ", inputType=" + inputType +
               ", options=" + options +
               '}';
    }
}

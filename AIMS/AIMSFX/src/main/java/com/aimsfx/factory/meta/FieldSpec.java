package com.aimsfx.factory.meta;

import java.util.function.Predicate;

/**
 * FieldSpec - Lightweight metadata for input field parsing and validation
 * 
 * DESIGN PATTERN: Specification Pattern (lightweight)
 * PURPOSE: Enables generic parsing/validation in Controller without hardcoded field logic
 * 
 * USAGE:
 * FieldSpec barcodeSpec = FieldSpec.string("barcode", "Barcode", true);
 * FieldSpec priceSpec = FieldSpec.doubleField("price", "Price", true);
 * FieldSpec weightSpec = FieldSpec.doubleField("weight", "Weight", true)
 *                                 .withConstraint(v -> (Double)v >= 0, "Weight cannot be negative!");
 * FieldSpec vatSpec = FieldSpec.doubleFieldWithDefault("vatRate", "VAT Rate", false, 10.0);
 * 
 * BENEFITS:
 * - Controller becomes generic - no per-field hardcoded validation
 * - Factories own their field requirements (OCP)
 * - PhysicalProductFactory adds weight/dimensions specs
 * - DigitalProductFactory doesn't include them
 * - Adding new product types = no controller changes
 */
public class FieldSpec {
    
    /**
     * Field data type for parsing
     */
    public enum Kind {
        STRING,
        INT,
        DOUBLE
    }
    
    private final String key;
    private final String label;
    private final Kind kind;
    private final boolean required;
    private final Object defaultValue;
    private final Predicate<Object> constraint;
    private final String constraintMessage;
    
    /**
     * Full constructor (internal use)
     */
    private FieldSpec(String key, String label, Kind kind, boolean required, 
                      Object defaultValue, Predicate<Object> constraint, String constraintMessage) {
        this.key = key;
        this.label = label;
        this.kind = kind;
        this.required = required;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.constraintMessage = constraintMessage;
    }
    
    // ==================== Static Factory Methods ====================
    
    /**
     * Create a STRING field spec
     * @param key Field key in the raw map
     * @param label Human-friendly label for error messages
     * @param required Whether field is required
     */
    public static FieldSpec string(String key, String label, boolean required) {
        return new FieldSpec(key, label, Kind.STRING, required, null, null, null);
    }
    
    /**
     * Create a STRING field spec with default value
     */
    public static FieldSpec stringWithDefault(String key, String label, boolean required, String defaultValue) {
        return new FieldSpec(key, label, Kind.STRING, required, defaultValue, null, null);
    }
    
    /**
     * Create an INT field spec
     * @param key Field key in the raw map
     * @param label Human-friendly label for error messages
     * @param required Whether field is required
     */
    public static FieldSpec intField(String key, String label, boolean required) {
        return new FieldSpec(key, label, Kind.INT, required, null, null, null);
    }
    
    /**
     * Create an INT field spec with default value
     */
    public static FieldSpec intFieldWithDefault(String key, String label, boolean required, Integer defaultValue) {
        return new FieldSpec(key, label, Kind.INT, required, defaultValue, null, null);
    }
    
    /**
     * Create a DOUBLE field spec
     * @param key Field key in the raw map
     * @param label Human-friendly label for error messages
     * @param required Whether field is required
     */
    public static FieldSpec doubleField(String key, String label, boolean required) {
        return new FieldSpec(key, label, Kind.DOUBLE, required, null, null, null);
    }
    
    /**
     * Create a DOUBLE field spec with default value
     * @param key Field key in the raw map
     * @param label Human-friendly label for error messages
     * @param required Whether field is required
     * @param defaultValue Default value when field is blank
     */
    public static FieldSpec doubleFieldWithDefault(String key, String label, boolean required, Double defaultValue) {
        return new FieldSpec(key, label, Kind.DOUBLE, required, defaultValue, null, null);
    }
    
    // ==================== Builder-style Methods ====================
    
    /**
     * Add a constraint to this field spec (returns new instance - immutable)
     * @param predicate Constraint predicate receiving the parsed value
     * @param message Error message when constraint fails
     * @return New FieldSpec with constraint attached
     */
    public FieldSpec withConstraint(Predicate<Object> predicate, String message) {
        return new FieldSpec(this.key, this.label, this.kind, this.required, 
                            this.defaultValue, predicate, message);
    }
    
    /**
     * Create a copy with different required flag
     */
    public FieldSpec withRequired(boolean required) {
        return new FieldSpec(this.key, this.label, this.kind, required, 
                            this.defaultValue, this.constraint, this.constraintMessage);
    }
    
    // ==================== Getters ====================
    
    public String getKey() {
        return key;
    }
    
    public String getLabel() {
        return label;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public Predicate<Object> getConstraint() {
        return constraint;
    }
    
    public String getConstraintMessage() {
        return constraintMessage;
    }
    
    public boolean hasConstraint() {
        return constraint != null;
    }
    
    @Override
    public String toString() {
        return "FieldSpec{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", kind=" + kind +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                ", hasConstraint=" + (constraint != null) +
                '}';
    }
}

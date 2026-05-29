package com.aimsfx.model.meta;

/**
 * InputType - Enum defining UI input control types
 * 
 * PURPOSE: Part of the metadata system for dynamic form generation
 * Used by AttributeMeta to specify how each field should be rendered
 * 
 * OCP BENEFIT: View doesn't need to know product types - just input types
 * Adding new product types doesn't require View modification
 */
public enum InputType {

    TEXT,
    NUMBER,
    COMBOBOX,
    DATE,
    READONLY
}

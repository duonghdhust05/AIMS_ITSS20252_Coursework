package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DateFormatValidator - Utility class for validating date formats
 * 
 * DESIGN PATTERN: Utility/Helper Class
 * PURPOSE: Provides reusable date validation logic
 * 
 * SOLID PRINCIPLES:
 * - SRP: Single responsibility = date format validation
 * - DRY: Avoid code duplication across validators
 * 
 * SUPPORTED FORMAT: YYYY-MM-DD (ISO 8601)
 * Examples: 2024-01-15, 2023-12-31
 */
public final class DateFormatValidator {
    
    /**
     * Expected date format: YYYY-MM-DD
     */
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    
    /**
     * Regex pattern for quick format check before parsing
     */
    private static final String DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    
    // Private constructor to prevent instantiation
    private DateFormatValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validate date string format (YYYY-MM-DD)
     * 
     * @param dateString The date string to validate
     * @param fieldName The name of the field for error messages
     * @throws InvalidProductDataException if date format is invalid
     */
    public static void validateDateFormat(String dateString, String fieldName) 
            throws InvalidProductDataException {
        
        if (dateString == null || dateString.isBlank()) {
            throw new InvalidProductDataException(fieldName + " is required");
        }
        
        String trimmedDate = dateString.trim();
        
        // Quick regex check for format
        if (!trimmedDate.matches(DATE_REGEX)) {
            throw new InvalidProductDataException(
                fieldName + " must be in format YYYY-MM-DD (e.g., 2024-01-15). Got: " + trimmedDate);
        }
        
        // Parse to validate actual date values (e.g., reject 2024-02-30)
        try {
            LocalDate.parse(trimmedDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidProductDataException(
                fieldName + " is not a valid date. Please use format YYYY-MM-DD with valid values. Got: " + trimmedDate);
        }
    }
    
    /**
     * Validate date string format (YYYY-MM-DD) - returns boolean instead of throwing
     * 
     * @param dateString The date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return false;
        }
        
        String trimmedDate = dateString.trim();
        
        if (!trimmedDate.matches(DATE_REGEX)) {
            return false;
        }
        
        try {
            LocalDate.parse(trimmedDate, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Parse date string to LocalDate
     * 
     * @param dateString The date string to parse (must be in YYYY-MM-DD format)
     * @param fieldName The name of the field for error messages
     * @return LocalDate object
     * @throws InvalidProductDataException if date format is invalid
     */
    public static LocalDate parseDate(String dateString, String fieldName) 
            throws InvalidProductDataException {
        validateDateFormat(dateString, fieldName);
        return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    }
}

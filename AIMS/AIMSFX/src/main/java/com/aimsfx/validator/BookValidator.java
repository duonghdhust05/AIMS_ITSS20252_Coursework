package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.model.ProductType;

/**
 * BookValidator - Validates book-specific attributes
 * 
 * STRATEGY PATTERN: Concrete strategy for Book validation
 * SRP: Only validates Book-specific fields
 * 
 * ALL FIELDS ARE REQUIRED:
 * - Author, Publisher, Publication Date, Pages, Language, Cover Type, Genre
 */
public class BookValidator implements ProductValidator {
    
    @Override
    public void validateSpecificAttributes(String... attributes) throws InvalidProductDataException {
        // Book attributes order (from BookFactory):
        // 0: Author, 1: Publisher, 2: Publication Date, 3: Pages, 4: Language, 5: Cover Type, 6: Genre
        
        // Validate Author (index 0) - REQUIRED
        if (attributes.length < 1 || attributes[0] == null || attributes[0].isBlank()) {
            throw new InvalidProductDataException("Author is required for books");
        }
        if (attributes[0].length() > 200) {
            throw new InvalidProductDataException("Author name too long (max 200 characters)");
        }
        
        // Validate Publisher (index 1) - REQUIRED
        if (attributes.length < 2 || attributes[1] == null || attributes[1].isBlank()) {
            throw new InvalidProductDataException("Publisher is required for books");
        }
        
        // Validate Publication Date (index 2) - REQUIRED with format YYYY-MM-DD
        if (attributes.length < 3 || attributes[2] == null || attributes[2].isBlank()) {
            throw new InvalidProductDataException("Publication date is required for books");
        }
        DateFormatValidator.validateDateFormat(attributes[2], "Publication date");
        
        // Validate Pages (index 3) - OPTIONAL
        if (attributes.length >= 4 && attributes[3] != null && !attributes[3].isBlank()) {
            try {
                int pages = Integer.parseInt(attributes[3]);
                if (pages <= 0) {
                    throw new InvalidProductDataException("Number of pages must be positive");
                }
            } catch (NumberFormatException e) {
                throw new InvalidProductDataException("Invalid number format for pages");
            }
        }
        
        // Validate Language (index 4) - OPTIONAL
        // if (attributes.length < 5 || attributes[4] == null || attributes[4].isBlank()) {
        //     throw new InvalidProductDataException("Language is required for books");
        // }
        
        // Validate Cover Type (index 5) - REQUIRED
        if (attributes.length < 6 || attributes[5] == null || attributes[5].isBlank()) {
            throw new InvalidProductDataException("Cover type is required for books");
        }
        if (!attributes[5].equalsIgnoreCase("Paperback") && 
            !attributes[5].equalsIgnoreCase("Hardcover")) {
            throw new InvalidProductDataException("Cover type must be 'Paperback' or 'Hardcover'");
        }
        
        // Validate Genre (index 6) - OPTIONAL
        // if (attributes.length < 7 || attributes[6] == null || attributes[6].isBlank()) {
        //     throw new InvalidProductDataException("Genre is required for books");
        // }
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.BOOK;
    }
}

package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.model.ProductType;

/**
 * CDValidator - Validates CD-specific attributes
 * 
 * ALL FIELDS ARE REQUIRED:
 * - Artist, Record Label, Genre, Track Count, Release Date
 */
public class CDValidator implements ProductValidator {
    
    @Override
    public void validateSpecificAttributes(String... attributes) throws InvalidProductDataException {
        // CD attributes order (from CDFactory):
        // 0: Artist, 1: Record Label, 2: Genre, 3: Track Count, 4: Release Date
        
        // Validate Artist (index 0) - REQUIRED
        if (attributes.length < 1 || attributes[0] == null || attributes[0].isBlank()) {
            throw new InvalidProductDataException("Artist is required for CDs");
        }
        
        // Validate Record Label (index 1) - REQUIRED
        if (attributes.length < 2 || attributes[1] == null || attributes[1].isBlank()) {
            throw new InvalidProductDataException("Record label is required for CDs");
        }
        
        // Validate Genre (index 2) - REQUIRED
        if (attributes.length < 3 || attributes[2] == null || attributes[2].isBlank()) {
            throw new InvalidProductDataException("Genre is required for CDs");
        }
        
        // Validate Track Count (index 3) - REQUIRED
        if (attributes.length < 4 || attributes[3] == null || attributes[3].isBlank()) {
            throw new InvalidProductDataException("Track count is required for CDs");
        }
        try {
            int trackCount = Integer.parseInt(attributes[3]);
            if (trackCount <= 0) {
                throw new InvalidProductDataException("Track count must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InvalidProductDataException("Invalid number format for track count");
        }
        
        // Validate Release Date (index 4) - OPTIONAL with format YYYY-MM-DD
        if (attributes.length >= 5 && attributes[4] != null && !attributes[4].isBlank()) {
            DateFormatValidator.validateDateFormat(attributes[4], "Release date");
        }
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.CD;
    }
}

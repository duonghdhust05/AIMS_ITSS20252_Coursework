package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.model.ProductType;

/**
 * DVDValidator - Validates DVD-specific attributes
 * 
 * ALL FIELDS ARE REQUIRED:
 * - Director, Studio, Subtitle, Disc Type, Duration, Genre, Release Date
 */
public class DVDValidator implements ProductValidator {
    
    @Override
    public void validateSpecificAttributes(String... attributes) throws InvalidProductDataException {
        // DVD attributes order (from DVDFactory):
        // 0: Director, 1: Studio, 2: Subtitle, 3: Disc Type, 4: Duration, 5: Genre, 6: Release Date
        
        // Validate Director (index 0) - REQUIRED
        if (attributes.length < 1 || attributes[0] == null || attributes[0].isBlank()) {
            throw new InvalidProductDataException("Director is required for DVDs");
        }
        
        // Validate Studio (index 1) - REQUIRED
        if (attributes.length < 2 || attributes[1] == null || attributes[1].isBlank()) {
            throw new InvalidProductDataException("Studio is required for DVDs");
        }
        
        // Validate Subtitle (index 2) - REQUIRED
        if (attributes.length < 3 || attributes[2] == null || attributes[2].isBlank()) {
            throw new InvalidProductDataException("Subtitle is required for DVDs");
        }
        
        // Validate Disc Type (index 3) - REQUIRED
        if (attributes.length < 4 || attributes[3] == null || attributes[3].isBlank()) {
            throw new InvalidProductDataException("Disc type is required for DVDs");
        }
        // Validate disc type value
        String discType = attributes[3];
        if (!discType.equalsIgnoreCase("DVD-5") && 
            !discType.equalsIgnoreCase("DVD-9") && 
            !discType.equalsIgnoreCase("Blu-ray") && 
            !discType.equalsIgnoreCase("4K UHD")) {
            throw new InvalidProductDataException("Disc type must be 'DVD-5', 'DVD-9', 'Blu-ray', or '4K UHD'");
        }
        
        // Validate Duration (index 4) - REQUIRED
        if (attributes.length < 5 || attributes[4] == null || attributes[4].isBlank()) {
            throw new InvalidProductDataException("Duration is required for DVDs");
        }
        try {
            int duration = Integer.parseInt(attributes[4]);
            if (duration <= 0) {
                throw new InvalidProductDataException("Duration must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InvalidProductDataException("Invalid number format for duration");
        }
        
        // Validate Genre (index 5) - OPTIONAL
        // if (attributes.length < 6 || attributes[5] == null || attributes[5].isBlank()) {
        //     throw new InvalidProductDataException("Genre is required for DVDs");
        // }
        
        // Validate Release Date (index 6) - OPTIONAL with format YYYY-MM-DD
        if (attributes.length >= 7 && attributes[6] != null && !attributes[6].isBlank()) {
            DateFormatValidator.validateDateFormat(attributes[6], "Release date");
        }
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.DVD;
    }
}

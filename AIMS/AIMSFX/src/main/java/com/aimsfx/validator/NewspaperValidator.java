package com.aimsfx.validator;

import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.model.ProductType;

/**
 * NewspaperValidator - Validates Newspaper-specific attributes
 * 
 * ALL FIELDS ARE REQUIRED:
 * - Issue Number, Frequency, Editor-in-Chief, Publisher, Publication Date, Language, Sections
 */
public class NewspaperValidator implements ProductValidator {
    
    @Override
    public void validateSpecificAttributes(String... attributes) throws InvalidProductDataException {
        // Newspaper attributes order (from NewspaperFactory):
        // 0: Issue Number, 1: Frequency, 2: Editor-in-Chief, 3: Publisher, 4: Publication Date, 5: Language, 6: Sections
        
        // Validate ISSN (index 0) - OPTIONAL
        // if (attributes.length < 1 || attributes[0] == null || attributes[0].isBlank()) {
        //     throw new InvalidProductDataException("ISSN is required for newspapers");
        // }
        
        // Validate Frequency (index 1) - OPTIONAL
        if (attributes.length >= 2 && attributes[1] != null && !attributes[1].isBlank()) {
            String frequency = attributes[1];
            if (!frequency.equalsIgnoreCase("Daily") && 
                !frequency.equalsIgnoreCase("Weekly") && 
                !frequency.equalsIgnoreCase("Bi-weekly") && 
                !frequency.equalsIgnoreCase("Monthly") && 
                !frequency.equalsIgnoreCase("Quarterly")) {
                throw new InvalidProductDataException(
                    "Frequency must be one of: Daily, Weekly, Bi-weekly, Monthly, Quarterly");
            }
        }
        
        // Validate Editor-in-Chief (index 2) - REQUIRED
        if (attributes.length < 3 || attributes[2] == null || attributes[2].isBlank()) {
            throw new InvalidProductDataException("Editor-in-Chief is required for newspapers");
        }
        
        // Validate Publisher (index 3) - REQUIRED
        if (attributes.length < 4 || attributes[3] == null || attributes[3].isBlank()) {
            throw new InvalidProductDataException("Publisher is required for newspapers");
        }
        
        // Validate Publication Date (index 4) - REQUIRED with format YYYY-MM-DD
        if (attributes.length < 5 || attributes[4] == null || attributes[4].isBlank()) {
            throw new InvalidProductDataException("Publication date is required for newspapers");
        }
        DateFormatValidator.validateDateFormat(attributes[4], "Publication date");
        
        // Validate Language (index 5) - OPTIONAL
        // if (attributes.length < 6 || attributes[5] == null || attributes[5].isBlank()) {
        //     throw new InvalidProductDataException("Language is required for newspapers");
        // }
        
        // Validate Sections (index 6) - OPTIONAL
        // if (attributes.length < 7 || attributes[6] == null || attributes[6].isBlank()) {
        //     throw new InvalidProductDataException("Sections is required for newspapers");
        // }
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.NEWSPAPER;
    }
}

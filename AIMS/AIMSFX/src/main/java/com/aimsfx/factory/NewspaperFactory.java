package com.aimsfx.factory;

import com.aimsfx.model.Newspaper;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.model.meta.InputType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * NewspaperFactory - Concrete Factory for creating Newspaper products
 */
public class NewspaperFactory implements PhysicalProductFactory {
    
    @Override
    public Product createPhysicalProduct(Long productId, String barcode, String title, String category,
                                Double originalPrice, Double currentPrice,
                                String description, Double weight, String dimensions,
                                Integer stock, String status, Double vatRate,
                                String... attributes) {
        
        String issn = attributes.length > 0 ? attributes[0] : null;
        String frequency = attributes.length > 1 ? attributes[1] : null;
        String editorInChief = attributes.length > 2 ? attributes[2] : null;
        String publisher = attributes.length > 3 ? attributes[3] : null;
        Date publicationDate = attributes.length > 4 ? parseDateOrNull(attributes[4]) : null;
        String language = attributes.length > 5 ? attributes[5] : null;
        String section = attributes.length > 6 ? attributes[6] : null;
        
        return new Newspaper(productId, barcode, title, category, originalPrice, currentPrice,
                            description, weight, dimensions, stock, status, vatRate,
                            issn, frequency, editorInChief, publisher, publicationDate, language, section);
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.NEWSPAPER;
    }
    
    @Override
    public String[] getAttributeLabels() {
        return new String[]{"ISSN:", "Frequency:", "Editor-in-Chief*:", "Publisher*:", "Publication Date* (yyyy-MM-dd):", "Language:", "Sections:"};
    }
    
    @Override
    public String[] getAttributeKeys() {
        return new String[]{"issn", "frequency", "editorInChief", "publisher", "publicationDate", "language", "section"};
    }
    
    @Override
    public List<AttributeMeta> getAttributeConfig() {
        return List.of(
            new AttributeMeta("issn", "ISSN:", InputType.TEXT),
            new AttributeMeta("frequency", "Frequency:", List.of("Daily", "Weekly", "Bi-weekly", "Monthly", "Quarterly")),
            new AttributeMeta("editorInChief", "Editor-in-Chief*:", InputType.TEXT),
            new AttributeMeta("publisher", "Publisher*:", InputType.TEXT),
            new AttributeMeta("publicationDate", "Publication Date*:", InputType.DATE, "yyyy-MM-dd"),
            new AttributeMeta("language", "Language:", InputType.TEXT),
            new AttributeMeta("section", "Sections:", InputType.TEXT)
        );
    }
    
    @Override
    public Product createEmptyProduct() {
        return new Newspaper();
    }
    
    private Date parseDateOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}

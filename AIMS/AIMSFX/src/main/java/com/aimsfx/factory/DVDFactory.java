package com.aimsfx.factory;

import com.aimsfx.model.DVD;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.model.meta.InputType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * DVDFactory - Concrete Factory for creating DVD products
 */
public class DVDFactory implements PhysicalProductFactory {
    
    @Override
    public Product createPhysicalProduct(Long productId, String barcode, String title, String category,
                                Double originalPrice, Double currentPrice,
                                String description, Double weight, String dimensions,
                                Integer stock, String status, Double vatRate,
                                String... attributes) {
        
        String director = attributes.length > 0 ? attributes[0] : null;
        String studio = attributes.length > 1 ? attributes[1] : null;
        String subtitle = attributes.length > 2 ? attributes[2] : null;
        String discType = attributes.length > 3 ? attributes[3] : null;
        Integer duration = attributes.length > 4 ? parseIntOrNull(attributes[4]) : null;
        String genre = attributes.length > 5 ? attributes[5] : null;
        Date releaseDate = attributes.length > 6 ? parseDateOrNull(attributes[6]) : null;
        
        return new DVD(productId, barcode, title, category, originalPrice, currentPrice,
                      description, weight, dimensions, stock, status, vatRate,
                      director, studio, subtitle, discType, duration, genre, releaseDate);
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.DVD;
    }
    
    @Override
    public String[] getAttributeLabels() {
        return new String[]{"Director*:", "Studio*:", "Subtitle*:", "Disc Type*:", "Duration* (minutes):", "Genre:", "Release Date (yyyy-MM-dd):"};
    }
    
    @Override
    public String[] getAttributeKeys() {
        return new String[]{"director", "studio", "subtitle", "discType", "duration", "genre", "releaseDate"};
    }
    
    @Override
    public List<AttributeMeta> getAttributeConfig() {
        return List.of(
            new AttributeMeta("director", "Director*:", InputType.TEXT),
            new AttributeMeta("studio", "Studio*:", InputType.TEXT),
            new AttributeMeta("subtitle", "Subtitle*:", InputType.TEXT),
            new AttributeMeta("discType", "Disc Type*:", List.of("DVD-5", "DVD-9", "Blu-ray", "4K UHD")),
            new AttributeMeta("duration", "Duration* (minutes):", InputType.NUMBER),
            new AttributeMeta("genre", "Genre:", InputType.TEXT),
            new AttributeMeta("releaseDate", "Release Date (yyyy-MM-dd):", InputType.DATE, "yyyy-MM-dd")
        );
    }
    
    @Override
    public Product createEmptyProduct() {
        return new DVD();
    }
    
    private Integer parseIntOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
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

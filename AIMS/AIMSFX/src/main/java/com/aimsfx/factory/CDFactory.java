package com.aimsfx.factory;

import com.aimsfx.model.CD;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.Track;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.model.meta.InputType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CDFactory - Concrete Factory for creating CD products
 */
public class CDFactory implements PhysicalProductFactory {
    
    @Override
    public Product createPhysicalProduct(Long productId, String barcode, String title, String category,
                                Double originalPrice, Double currentPrice,
                                String description, Double weight, String dimensions,
                                Integer stock, String status, Double vatRate,
                                String... attributes) {
        
        String artist = attributes.length > 0 ? attributes[0] : null;
        String recordLabel = attributes.length > 1 ? attributes[1] : null;
        String genre = attributes.length > 2 ? attributes[2] : null;
        Integer trackCount = attributes.length > 3 ? parseIntOrNull(attributes[3]) : null;
        Date releaseDate = attributes.length > 4 ? parseDateOrNull(attributes[4]) : null;
        
        return new CD(productId, barcode, title, category, originalPrice, currentPrice,
                     description, weight, dimensions, stock, status, vatRate,
                     artist, recordLabel, genre, trackCount, releaseDate);
    }
    
    @Override
    public ProductType getProductType() {
        return ProductType.CD;
    }
    
    @Override
    public String[] getAttributeLabels() {
        return new String[]{"Artist*:", "Record Label*:", "Genre*:", "Track Count*:", "Release Date (yyyy-MM-dd):"};
    }
    
    @Override
    public String[] getAttributeKeys() {
        return new String[]{"artist", "recordLabel", "genre", "trackCount", "releaseDate"};
    }
    
    @Override
    public List<AttributeMeta> getAttributeConfig() {
        return List.of(
            new AttributeMeta("artist", "Artist*:", InputType.TEXT),
            new AttributeMeta("recordLabel", "Record Label*:", InputType.TEXT),
            new AttributeMeta("genre", "Genre*:", InputType.TEXT),
            // OCP: READONLY replaces hardcoded `if (selectedType.equals("CD") && i == 3)`
            new AttributeMeta("trackCount", "Track Count*:", InputType.READONLY, "Auto-calculated from tracks"),
            new AttributeMeta("releaseDate", "Release Date (yyyy-MM-dd):", InputType.DATE, "yyyy-MM-dd")
        );
    }
    
    @Override
    public Product createEmptyProduct() {
        return new CD();
    }
    
    // ==================== Cross-Field Validation ====================
    
    /**
     * CD-specific cross-field validation: requires at least one track
     * 
     * OCP PRINCIPLE: CD track requirement is enforced here, not in Controller
     * - Controller calls factory.validateCrossFieldRules() generically
     * - Controller doesn't need to know "CD" or check product type
     * - Adding new cross-field rules = modify factory, not controller
     * 
     * @throws IllegalArgumentException if no tracks provided for CD
     */
    @Override
    public void validateCrossFieldRules(
            Map<String, Object> parsedCommon,
            Map<String, String> specificAttributes,
            List<Track> tracks) {
        
        if (tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("CD products must have at least 1 track!");
        }
    }
    
    // ==================== Private Helpers ====================
    
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

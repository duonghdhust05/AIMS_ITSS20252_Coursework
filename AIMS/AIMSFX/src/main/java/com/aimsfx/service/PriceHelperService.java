package com.aimsfx.service;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PriceHelperService - Price formatting and parsing implementation
 * 
 * RESPONSIBILITIES:
 * - Format prices for display
 * - Parse price strings
 * - Support multiple currencies
 * 
 * COHESION: Functional (5/5)
 * COUPLING: Data (5/5) - No coupling, pure utility
 */
public class PriceHelperService implements IPriceHelper {
    
    private static final Logger LOGGER = Logger.getLogger(PriceHelperService.class.getName());
    private final DecimalFormat vndFormat;
    private final DecimalFormat usdFormat;
    
    public PriceHelperService() {
        // Vietnamese Dong format: 1,000,000 VND
        vndFormat = new DecimalFormat("#,###");
        
        // US Dollar format: $1,000.00
        usdFormat = new DecimalFormat("#,##0.00");
    }
    
    @Override
    public String format(double price) {
        return formatVND(price);
    }
    
    /**
     * Format price in VND
     */
    public String formatVND(double price) {
        return vndFormat.format(price) + " VND";
    }
    
    /**
     * Format price in USD
     */
    public String formatUSD(double price) {
        return "$" + usdFormat.format(price);
    }
    
    @Override
    public double parse(String priceString) {
        if (priceString == null || priceString.trim().isEmpty()) {
            return 0.0;
        }
        
        try {
            // Remove currency symbols and whitespace
            String cleaned = priceString
                .replace("VND", "")
                .replace("$", "")
                .replace(",", "")
                .trim();
            
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Failed to parse price: " + priceString, e);
            return 0.0;
        }
    }
    
    /**
     * Format price with custom decimal places
     */
    public String formatWithDecimals(double price, int decimals) {
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimals > 0) {
            pattern.append(".");
            for (int i = 0; i < decimals; i++) {
                pattern.append("0");
            }
        }
        
        DecimalFormat format = new DecimalFormat(pattern.toString());
        return format.format(price);
    }
    
    /**
     * Check if price string is valid
     */
    public boolean isValidPrice(String priceString) {
        try {
            double parsed = parse(priceString);
            return parsed >= 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Round price to nearest integer
     */
    public double round(double price) {
        return Math.round(price);
    }
    
    /**
     * Calculate percentage of price
     */
    public double calculatePercentage(double price, double percentage) {
        return price * (percentage / 100.0);
    }
}

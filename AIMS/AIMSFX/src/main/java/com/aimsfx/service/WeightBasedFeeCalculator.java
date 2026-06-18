package com.aimsfx.service;

import com.aimsfx.model.DeliveryInfo;

/**
 * Weight-based delivery fee calculator
 * 
 * CURRENT STRATEGY: Calculate fee based on weight and province
 * FORMULA: weight (kg) × rate per kg (VND)
 * 
 * RATES (Based on current business rules):
 * - Tier 1 cities (Hanoi, Ho Chi Minh City): 10,000 VND/kg
 * - Tier 2 cities (Da Nang, Can Tho): 15,000 VND/kg
 * - Other provinces: 20,000 VND/kg
 * 
 * NOTE: This is the default and currently only implementation.
 *       The Strategy Pattern allows easy addition of new calculation methods
 *       in the future without modifying existing code.
 */
public class WeightBasedFeeCalculator implements IDeliveryFeeCalculator {
    
    // ===== RATE CONSTANTS =====
    private static final double TIER_1_RATE = 10000.0;  // VND per kg
    private static final double TIER_2_RATE = 15000.0;
    private static final double OTHER_RATE = 20000.0;
    
    @Override
    public double calculateFee(DeliveryInfo deliveryInfo, float weight) {
        if (deliveryInfo == null) {
            throw new IllegalArgumentException("DeliveryInfo cannot be null");
        }
        
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        
        String province = deliveryInfo.getProvince();
        double ratePerKg = getRateForProvince(province);
        
        double fee = weight * ratePerKg;
        
        System.out.println("📦 Weight-based calculation:");
        System.out.println("   Province: " + province);
        System.out.println("   Weight: " + weight + " kg");
        System.out.println("   Rate: " + ratePerKg + " VND/kg");
        System.out.println("   Fee: " + fee + " VND");
        
        return fee;
    }
    
    /**
     * Get rate per kg for given province
     */
    private double getRateForProvince(String province) {
        if (province == null || province.trim().isEmpty()) {
            return OTHER_RATE;
        }
        
        // Tier 1 cities
        if (province.equals("Hanoi") || province.equals("Ho Chi Minh City")) {
            return TIER_1_RATE;
        }
        
        // Tier 2 cities
        if (province.equals("Da Nang") || province.equals("Can Tho")) {
            return TIER_2_RATE;
        }
        
        // Other provinces
        return OTHER_RATE;
    }
    
    @Override
    public String getStrategyName() {
        return "Weight-Based Fee Calculator";
    }
}

package com.aimsfx.service;

import com.aimsfx.model.DeliveryInfo;

/**
 * Strategy interface for calculating delivery fees
 * 
 * DESIGN PATTERN: Strategy Pattern
 * PURPOSE: Allow different delivery fee calculation algorithms
 *          without modifying DeliveryInfo or PlaceOrderService (OCP compliance)
 * 
 * CURRENT IMPLEMENTATION:
 * - WeightBasedFeeCalculator: Fee based on weight and province (currently used)
 * 
 * FUTURE EXTENSIBILITY:
 * If business requirements change, can easily add new implementations:
 * - DistanceBasedFeeCalculator: Fee based on distance from warehouse
 * - FlatRateFeeCalculator: Fixed fee for promotions
 * - ExpressDeliveryFeeCalculator: Premium shipping with surcharge
 * 
 * WITHOUT modifying existing code (Open/Closed Principle)
 */
public interface IDeliveryFeeCalculator {
    
    /**
     * Calculate delivery fee for given delivery info and weight
     * 
     * @param deliveryInfo Delivery information (address, province, etc.)
     * @param weight Total weight of the order in kg
     * @return Delivery fee in VND
     */
    double calculateFee(DeliveryInfo deliveryInfo, float weight);
    
    /**
     * Get name of this calculation strategy
     * For display/logging purposes
     */
    String getStrategyName();
}

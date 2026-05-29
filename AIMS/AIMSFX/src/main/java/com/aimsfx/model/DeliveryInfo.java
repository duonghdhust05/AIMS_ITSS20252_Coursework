package com.aimsfx.model;

import java.util.HashMap;
import java.util.Map;

/**
 * DeliveryInfo model for managing delivery information
 * As per ClassDeliveryInfo.pdf specification
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods relate to delivery info: validation, fee calculation, data management
 * - Single responsibility: Manage and validate delivery information
 * - Clear separation: validation logic, calculation logic, data access
 * 
 * COUPLING ANALYSIS:
 * 1. NO External Coupling
 *    - Self-contained class
 *    - Only uses built-in Java types (String, Map, primitive types)
 *    - All calculations done internally based on own attributes
 * 
 * 2. calculateDeliveryFee(float totalWeight) method
 *    - Input: primitive float (totalWeight)
 *    - Type: Data coupling - simplest form, only primitive data
 *    - Returns: primitive float (fee)
 * 
 * Overall: ZERO COUPLING with other domain classes - Completely independent
 */
public class DeliveryInfo {
    // Attributes (as per specification)
    private String recipientName;
    private String email;
    private String phoneNumber;
    private String province;
    private String ward;  // Phường/Xã - Added for complete address
    private String address;
    private String deliveryInstructions;

    // Constructors
    /**
     * Default constructor for creating delivery information
     */
    public DeliveryInfo() {
    }

    /**
     * Constructor with basic delivery information
     */
    public DeliveryInfo(String recipientName, String phoneNumber, String address, String province) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.province = province;
    }

    // Getters and Setters
    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    // Business Methods (as per specification)
    
    /**
     * 2. Validates all required delivery information fields
     * @return true if all required fields are valid
     */
    public boolean checkValidityOfDeliveryInfo() {
        // Validate recipient name
        if (recipientName == null || recipientName.trim().isEmpty() || recipientName.length() < 2) {
            return false;
        }

        // Validate phone number (Vietnamese format: 10 digits starting with 0)
        if (phoneNumber == null || !phoneNumber.matches("^0\\d{9}$")) {
            return false;
        }

        // Validate email
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }

        // Validate address
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        // Validate province
        if (province == null || province.trim().isEmpty()) {
            return false;
        }

        return true;
    }
    
    /**
     * 3. Calculates shipping fee based on product weight and location
     * As per specification:
     * - Hanoi/HCM: 22,000 VND for first 3kg, then 2,500 VND per 0.5kg
     * - Other provinces: 30,000 VND for first 0.5kg, then 2,500 VND per 0.5kg
     * 
     * ⚠️ SOLID VIOLATION ALERT - Week 11 Audit ⚠️
     * 
     * ❌ OCP VIOLATION (Open/Closed Principle):
     *    Shipping calculation is HARD-CODED - NOT open for extension
     *    
     *    NEW REQUIREMENT: Calculate fee based on volumetric weight
     *    - Volumetric Weight = (Length × Width × Height) / 6000
     *    - Chargeable Weight = MAX(actual weight, volumetric weight)
     *    
     *    PROBLEM: To implement this, we must MODIFY this method (breaks "Closed for Modification")
     *    - Need to add parameters: length, width, height
     *    - Need to change method signature
     *    - Existing code depending on this method will break
     *    
     *    SOLUTION: Use Strategy Pattern
     *    - Create ShippingFeeCalculator interface
     *    - Implement WeightBasedShippingCalculator (current logic)
     *    - Implement VolumetricShippingCalculator (new requirement)
     *    - Implement ExpressShippingCalculator, WeekendShippingCalculator, etc. (future extensions)
     *    - DeliveryInfo delegates to strategy → Open for extension, Closed for modification
     *    
     *    See: SOLID_AUDIT_REPORT_PlaceOrder.md Section 2 for detailed refactoring
     * 
     * @param totalWeight Total weight of products in kg
     * @return Shipping fee in VND
     */
    public float calculateDeliveryFee(float totalWeight) {
        System.out.println("\n[DEBUG] ===== calculateDeliveryFee START =====");
        System.out.println("[DEBUG] Input totalWeight: " + totalWeight + " kg");
        
        float fee = 0f;
        
        // Check if in Hanoi or Ho Chi Minh City
        boolean isHanoiOrHCM = isInHanoiOrHCM();
        
        System.out.println("[DEBUG] isHanoiOrHCM result: " + isHanoiOrHCM);
        
        if (isHanoiOrHCM) {
            // Hanoi/HCM: 22,000 VND for first 3kg
            fee = 22000f;
            System.out.println("[DEBUG] HN/HCM: Base fee = 22,000 VND (first 3kg)");
            
            // Additional fee for weight over 3kg
            if (totalWeight > 3.0f) {
                float extraWeight = totalWeight - 3.0f;
                // 2,500 VND for every 0.5kg
                int extraUnits = (int) Math.ceil(extraWeight / 0.5);
                float extraFee = extraUnits * 2500f;
                fee += extraFee;
                System.out.println("[DEBUG] Extra weight: " + extraWeight + " kg");
                System.out.println("[DEBUG] Extra units (0.5kg each): " + extraUnits);
                System.out.println("[DEBUG] Extra fee: " + extraFee + " VND");
            }
        } else {
            // Other provinces: 30,000 VND for first 0.5kg
            fee = 30000f;
            System.out.println("[DEBUG] Other province: Base fee = 30,000 VND (first 0.5kg)");
            
            // Additional fee for weight over 0.5kg
            if (totalWeight > 0.5f) {
                float extraWeight = totalWeight - 0.5f;
                // 2,500 VND for every 0.5kg
                int extraUnits = (int) Math.ceil(extraWeight / 0.5);
                float extraFee = extraUnits * 2500f;
                fee += extraFee;
                System.out.println("[DEBUG] Extra weight: " + extraWeight + " kg");
                System.out.println("[DEBUG] Extra units (0.5kg each): " + extraUnits);
                System.out.println("[DEBUG] Extra fee: " + extraFee + " VND");
            }
        }
        
        System.out.println("[DEBUG] Final calculated fee: " + fee + " VND");
        System.out.println("[DEBUG] ===== calculateDeliveryFee END =====\n");
        
        return fee;
    }
    
    /**
     * 4. Returns all delivery information as a map
     * @return Map containing all delivery information
     */
    public Map<String, String> getDeliveryInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("recipientName", recipientName != null ? recipientName : "");
        info.put("email", email != null ? email : "");
        info.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
        info.put("province", province != null ? province : "");
        info.put("ward", ward != null ? ward : "");
        info.put("address", address != null ? address : "");
        info.put("deliveryInstructions", deliveryInstructions != null ? deliveryInstructions : "");
        return info;
    }
    
    /**
     * 5. Updates the delivery information with new values
     * @param newInfo Map containing updated delivery information
     * @return true if update successful
     */
    public boolean updateDeliveryInfo(Map<String, String> newInfo) {
        if (newInfo == null || newInfo.isEmpty()) {
            return false;
        }
        
        if (newInfo.containsKey("recipientName")) {
            this.recipientName = newInfo.get("recipientName");
        }
        if (newInfo.containsKey("email")) {
            this.email = newInfo.get("email");
        }
        if (newInfo.containsKey("phoneNumber")) {
            this.phoneNumber = newInfo.get("phoneNumber");
        }
        if (newInfo.containsKey("province")) {
            this.province = newInfo.get("province");
        }
        if (newInfo.containsKey("ward")) {
            this.ward = newInfo.get("ward");
        }
        if (newInfo.containsKey("address")) {
            this.address = newInfo.get("address");
        }
        if (newInfo.containsKey("deliveryInstructions")) {
            this.deliveryInstructions = newInfo.get("deliveryInstructions");
        }
        
        return true;
    }
    
    /**
     * 6. Check if province is Hanoi or Ho Chi Minh City
     * @return true if location is Hanoi or HCM
     */
    public boolean isInHanoiOrHCM() {
        if (province == null) {
            System.out.println("[DEBUG] isInHanoiOrHCM: province is NULL -> return false");
            return false;
        }
        
        String normalizedProvince = province.trim().toLowerCase();
        boolean result = normalizedProvince.contains("hà nội") || 
               normalizedProvince.contains("ha noi") ||
               normalizedProvince.contains("hanoi") ||
               normalizedProvince.contains("hồ chí minh") ||
               normalizedProvince.contains("ho chi minh") ||
               normalizedProvince.contains("tp. hồ chí minh") ||
               normalizedProvince.contains("tp hcm") ||
               normalizedProvince.contains("hcm");
        
        System.out.println("[DEBUG] isInHanoiOrHCM:");
        System.out.println("  - Original province: '" + province + "'");
        System.out.println("  - Normalized: '" + normalizedProvince + "'");
        System.out.println("  - Result: " + result);
        
        return result;
    }
    
    /**
     * Returns true if province is Hanoi or HCM (no district check since district field removed)
     * @return true if location is in Hanoi or HCM
     */
    public boolean isInHanoiOrHCMInnerCity() {
        return isInHanoiOrHCM();
    }

    @Override
    public String toString() {
        return "DeliveryInfo{" +
                "recipientName='" + recipientName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", province='" + province + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

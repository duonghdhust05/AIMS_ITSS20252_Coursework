package com.aimsfx.subsystem.paypal;

import java.util.function.Consumer;

/**
 * IPayPalView Interface
 * Purpose: Abstraction for PayPal approval page display
 * 
 * COHESION: HIGH - Functional Cohesion
 * - Single method with one well-defined purpose: display PayPal approval page
 * - All parameters work together for the single task of user approval flow
 * - Single responsibility: Define PayPal view contract
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with PayPalWebView (implementation)
 * - Type: Data coupling - passes String (approvalUrl) and callbacks
 * - Justification: Clean interface contract
 * 
 * 2. Data Coupling with PayOrderController (consumer)
 * - Uses: displayApprovalPage(approvalUrl, onSuccess, onCancel)
 * - Type: Data coupling - passes URL string and functional callbacks
 * - Justification: Controller only needs to provide data and handle results
 */
public interface IPayPalView {
    void displayApprovalPage(String approvalUrl, Consumer<String> onSuccess, Runnable onCancel);
}
package com.aimsfx.view.PaymentView;

import com.aimsfx.subsystem.paypal.IPayPalView;
import javafx.application.Platform;
import java.util.function.Consumer;
import com.aimsfx.router.PaymentRouter;

/**
 * PayPalWebView Class
 * Purpose: JavaFX WebView implementation for displaying PayPal approval page
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All attributes (SUCCESS_URL_MARKER, CANCEL_URL_MARKER, isFinished) support
 * one task
 * - Single method displayApprovalPage() performs one well-defined function
 * - All code elements work together to: display PayPal page and detect
 * approval/cancel
 * - Single responsibility: Render PayPal checkout in embedded browser
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with IPayPalView (interface implementation)
 * - Type: Data coupling - implements interface with String and callbacks
 * - Justification: Loose coupling through interface abstraction
 * 
 * 2. Data Coupling with PayOrderController (consumer via interface)
 * - Uses: displayApprovalPage(approvalUrl, onSuccess, onCancel)
 * - Type: Data coupling - receives String URL and callback functions
 * - Justification: Controller provides data, view handles display
 * 
 * 3. Control Coupling with JavaFX WebEngine
 * - Type: Control coupling - monitors URL changes and controls stage
 * - Justification: Necessary for detecting PayPal redirect responses
 */
public class PayPalWebView implements IPayPalView {

    @Override
    public void displayApprovalPage(String approvalUrl, Consumer<String> onSuccess, Runnable onCancel) {
        Platform.runLater(() -> {
            PaymentRouter.getInstance().showPayPalApprovalPage(approvalUrl, onSuccess, onCancel);
        });
    }
}
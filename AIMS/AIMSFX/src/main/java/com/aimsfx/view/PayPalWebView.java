package com.aimsfx.view;

import com.aimsfx.subsystem.paypal.IPayPalView;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.function.Consumer;

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

    private static final String SUCCESS_URL_MARKER = "google.com/search?q=success";
    private static final String CANCEL_URL_MARKER = "google.com/search?q=cancel";

    private boolean isFinished = false;

    @Override
    public void displayApprovalPage(String approvalUrl, Consumer<String> onSuccess, Runnable onCancel) {
        this.isFinished = false;

        // --- FIX 1: CẤU HÌNH COOKIE CHẤP NHẬN TẤT CẢ ---
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("PayPal Checkout");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        engine.getHistory().setMaxSize(0);

        engine.load(approvalUrl);

        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && !isFinished) {
                if (newUrl.contains(SUCCESS_URL_MARKER)) {
                    isFinished = true;
                    stage.close();
                    onSuccess.accept(newUrl);
                } else if (newUrl.contains(CANCEL_URL_MARKER)) {
                    isFinished = true;
                    stage.close();
                    onCancel.run();
                }
            }
        });

        stage.setScene(new Scene(webView, 1000, 700));
        stage.show();
    }
}
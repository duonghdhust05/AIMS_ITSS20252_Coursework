package com.aimsfx.router;

import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Invoice;
import com.aimsfx.model.Order;
import com.aimsfx.model.TransactionInfo;
import com.aimsfx.utils.UIUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.function.Consumer;
import animatefx.animation.FadeIn;

/**
 * PaymentRouter Class
 * Purpose: Singleton router to manage all navigation and UI transitions for the Payment module.
 * 
 * SOLID Compliance:
 * - SRP: Handles only Stage creation, Scene transitions, and Dialog popups.
 * 
 * COHESION: HIGH - All methods handle routing.
 */
public class PaymentRouter {
    
    private static PaymentRouter instance;
    
    private PaymentRouter() {}
    
    public static PaymentRouter getInstance() {
        if (instance == null) {
            instance = new PaymentRouter();
        }
        return instance;
    }

    /**
     * Navigates back to the Place Order screen
     */
    public void navigateToPlaceOrder(Stage currentStage) {
        // PlaceOrderRouter handles navigation to PlaceOrder, so delegate to it
        // and fetch current cart from CartManager
        PlaceOrderRouter.getInstance().navigateToPlaceOrder(
            com.aimsfx.model.CartManager.getInstance().getCart(), 
            currentStage
        );
    }

    /**
     * Navigates to the Order Success screen
     */
    public void navigateToOrderSuccess(Order order, Invoice invoice, TransactionInfo transactionInfo, DeliveryInfo deliveryInfo, Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-success-view.fxml"));
            Parent root = loader.load();

            com.aimsfx.view.OrderView.OrderSuccessScreen controller = loader.getController();
            controller.setSuccessData(order, invoice, transactionInfo, deliveryInfo);

            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(root);
            } else {
                currentStage.setScene(new Scene(root));
            }
            currentStage.setTitle("AIMS - Order Success");
            
            new FadeIn(root).play();

        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showError("Error", "Failed to load success screen: " + e.getMessage());
        }
    }

    /**
     * Shows the PayPal Approval popup in a WebView
     */
    public void showPayPalApprovalPage(String approvalUrl, Consumer<String> onSuccess, Runnable onCancel) {
        // Configure cookies to accept all for PayPal WebView
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        Stage stage = new Stage();
        UIUtils.applyAppIcon(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("PayPal Checkout");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        engine.getHistory().setMaxSize(0);

        engine.load(approvalUrl);

        // State holder to prevent multiple triggers
        final boolean[] isFinished = {false};
        
        final String SUCCESS_URL_MARKER = "google.com/search?q=success";
        final String CANCEL_URL_MARKER = "google.com/search?q=cancel";

        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && !isFinished[0]) {
                if (newUrl.contains(SUCCESS_URL_MARKER)) {
                    isFinished[0] = true;
                    stage.close();
                    onSuccess.accept(newUrl);
                } else if (newUrl.contains(CANCEL_URL_MARKER)) {
                    isFinished[0] = true;
                    stage.close();
                    onCancel.run();
                }
            }
        });

        stage.setScene(new Scene(webView, 1000, 700));
        
        new FadeIn(webView).play();
        
        stage.show();
    }
}

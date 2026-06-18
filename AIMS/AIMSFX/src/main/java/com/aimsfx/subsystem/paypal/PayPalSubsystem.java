package com.aimsfx.subsystem.paypal;

import com.aimsfx.exception.*;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.controllers.PaymentsController;
import com.paypal.sdk.models.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * PayPalSubsystem Class
 * Purpose: Implementation of IPaymentGateway for PayPal payment processing
 * 
 * SOLID Compliance:
 * - SRP: Handles only PayPal API operations
 * - OCP: Maps provider errors to semantic exception types
 * - LSP: Properly substitutable for IPaymentGateway
 * - DIP: Throws abstract PaymentException subclasses
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All methods work toward PayPal order management
 * 
 * COUPLING: LOW - Data Coupling
 * - Throws semantic exceptions (no provider-specific coupling)
 */
public class PayPalSubsystem implements IPaymentGateway {

    private final OrdersController ordersController;
    private final PaymentsController paymentsController;
    private final CurrencyConverter converter;

    public PayPalSubsystem(PaypalServerSdkClient client, CurrencyConverter converter) {
        this.ordersController = client.getOrdersController();
        this.paymentsController = client.getPaymentsController();
        this.converter = converter;
    }

    @Override
    public Map<String, String> createOrder(String internalOrderId, double amountVND) throws PaymentException {
        try {
            double amountUSD = converter.convertVndToUsd(amountVND);
            String amountString = String.format("%.2f", amountUSD).replace(",", ".");

            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest.Builder(
                    new AmountWithBreakdown.Builder("USD", amountString).build())
                    .referenceId(internalOrderId)
                    .description("Order #" + internalOrderId + " from AIMS")
                    .build();

            OrderRequest orderRequest = new OrderRequest.Builder(
                    CheckoutPaymentIntent.CAPTURE,
                    Arrays.asList(purchaseUnit))
                    .applicationContext(new OrderApplicationContext.Builder()
                            .returnUrl("https://www.google.com/search?q=success")
                            .cancelUrl("https://www.google.com/search?q=cancel")
                            .brandName("AIMS Store")
                            .landingPage(OrderApplicationContextLandingPage.LOGIN)
                            .userAction(OrderApplicationContextUserAction.PAY_NOW)
                            .build())
                    .build();

            CreateOrderInput input = new CreateOrderInput.Builder(null, orderRequest).build();
            Order order = ordersController.createOrder(input).getResult();

            String approveUrl = order.getLinks().stream()
                    .filter(link -> "approve".equals(link.getRel()))
                    .findFirst()
                    .orElseThrow(() -> new PaymentProcessingException("NO_APPROVAL_URL", "PAYPAL"))
                    .getHref();

            Map<String, String> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("approveUrl", approveUrl);

            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw mapToSemanticException(e, "CREATE_ORDER");
        }
    }

    @Override
    public boolean captureOrder(String paypalOrderId) throws PaymentException {
        try {
            CaptureOrderInput input = new CaptureOrderInput.Builder(paypalOrderId, null).build();
            Order result = ordersController.captureOrder(input).getResult();

            return "COMPLETED".equals(result.getStatus().value());
        } catch (Exception e) {
            throw mapToSemanticException(e, "CAPTURE");
        }
    }

    @Override
    public boolean refundOrder(String gatewayOrderId) throws PaymentException {
        try {
            GetOrderInput getOrderInput = new GetOrderInput.Builder(gatewayOrderId).build();
            Order order = ordersController.getOrder(getOrderInput).getResult();
            
            if (order.getPurchaseUnits() == null || order.getPurchaseUnits().isEmpty() ||
                order.getPurchaseUnits().get(0).getPayments() == null ||
                order.getPurchaseUnits().get(0).getPayments().getCaptures() == null ||
                order.getPurchaseUnits().get(0).getPayments().getCaptures().isEmpty()) {
                throw new PaymentProcessingException("NO_CAPTURE_FOUND", "PAYPAL");
            }
            
            String captureId = order.getPurchaseUnits().get(0).getPayments().getCaptures().get(0).getId();
            
            RefundRequest refundRequest = new RefundRequest.Builder().build();
            RefundCapturedPaymentInput refundInput = new RefundCapturedPaymentInput.Builder()
                    .captureId(captureId)
                    .body(refundRequest)
                    .build();
            
            Refund result = paymentsController.refundCapturedPayment(refundInput).getResult();
            
            String status = result.getStatus() != null ? result.getStatus().value() : "";
            return "COMPLETED".equals(status) || "PENDING".equals(status);
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw mapToSemanticException(e, "REFUND");
        }
    }

    private PaymentException mapToSemanticException(Exception e, String operation) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        System.err.println("[PayPalSubsystem] " + operation + " error: " + e.getMessage());

        if (message.contains("declined_by_payment_method") ||
                message.contains("payment_method_error") ||
                message.contains("invalid_expiry_date")) {
            return new PaymentDeclinedException(extractErrorCode(message), "PAYPAL", e);
        }

        if (message.contains("system_config_error") ||
                message.contains("payee_not_enabled_for_payment_method")) {
            return new PaymentAuthenticationException(extractErrorCode(message), "PAYPAL", e);
        }

        if (message.contains("internal_server_error") ||
                message.contains("service_unavailable") ||
                message.contains("order_completion_in_progress")) {
            return new PaymentTimeoutException(extractErrorCode(message), "PAYPAL", e);
        }

        return new PaymentProcessingException(extractErrorCode(message), "PAYPAL", e);
    }

    private String extractErrorCode(String message) {
        String[] knownCodes = {
                "order_not_confirmed", "system_config_error", "invalid_payment_method",
                "payee_not_enabled_for_payment_method", "payment_method_change_not_allowed",
                "processing_error", "min_amount_required_by_payment_method",
                "payment_method_error", "declined_by_payment_method",
                "currency_not_supported_by_payment_method", "country_not_supported_by_payment_method",
                "invalid_expiry_date", "unsupported_processing_instruction",
                "order_complete_on_payment_approval", "order_completion_in_progress",
                "internal_server_error", "payment_error"
        };

        for (String code : knownCodes) {
            if (message.contains(code)) {
                return code.toUpperCase();
            }
        }

        return "UNKNOWN";
    }
}
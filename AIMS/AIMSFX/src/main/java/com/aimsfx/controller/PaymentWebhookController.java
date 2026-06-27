package com.aimsfx.controller;

import com.aimsfx.service.webhook.IPaymentWebhookHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    private final List<IPaymentWebhookHandler> webhookHandlers;

    public PaymentWebhookController(List<IPaymentWebhookHandler> webhookHandlers) {
        this.webhookHandlers = webhookHandlers;
    }

    @PostMapping(value = "/{gatewayName}-callback")
    public ResponseEntity<?> handlePaymentCallback(
            @PathVariable String gatewayName,
            @RequestBody String rawPayload,
            HttpServletRequest request) {

        // Delegate to the appropriate handler based on gatewayName (Strategy Pattern)
        for (IPaymentWebhookHandler handler : webhookHandlers) {
            if (handler.supports(gatewayName)) {
                return handler.handleWebhook(rawPayload, request);
            }
        }

        return ResponseEntity.badRequest().body("Unsupported payment gateway: " + gatewayName);
    }
}
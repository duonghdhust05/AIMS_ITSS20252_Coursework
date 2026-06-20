package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.view.html.OrderHtmlView;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderReviewService orderReviewService = new OrderReviewService();

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String viewOrder(@PathVariable("id") int orderId) {
        try {
            Order order = orderReviewService.getOrderById(orderId);
            if (order == null) {
                return OrderHtmlView.renderError("Order Not Found");
            }

            String status = OrderStatus.fromDbValue(order.getStatus()).name();
            return OrderHtmlView.renderOrderDetail(order, status);

        } catch (SQLException e) {
            e.printStackTrace();
            return OrderHtmlView.renderError("Error loading order");
        }
    }

    @PostMapping(value = "/{id}/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public String cancelOrder(@PathVariable("id") int orderId) {
        try {
            Order order = orderReviewService.getOrderById(orderId);
            if (order == null) {
                return OrderHtmlView.renderError("Order Not Found");
            }

            OrderStatus status = OrderStatus.fromDbValue(order.getStatus());
            if (status != OrderStatus.PENDING_REVIEW && status != OrderStatus.PROCESSING) {
                return OrderHtmlView.renderError("Cannot cancel order. Current status: " + status.name());
            }

            orderReviewService.requestRefund(orderId);

            return OrderHtmlView.renderMessage("Order Cancelled Successfully!", 
                    "Your order has been updated to REFUND_REQUEST.");
        } catch (SQLException e) {
            e.printStackTrace();
            return OrderHtmlView.renderError("Error cancelling order");
        }
    }
}

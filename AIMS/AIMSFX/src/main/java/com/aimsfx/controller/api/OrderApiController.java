package com.aimsfx.controller.api;

import com.aimsfx.model.Order;
import com.aimsfx.model.OrderStatus;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.OrderReviewService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderReviewService orderReviewService = new OrderReviewService();
    private final OrderRepository orderRepository = new OrderRepository();

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String viewOrder(@PathVariable("id") int orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                return "<h1>Order Not Found</h1>";
            }

            String status = OrderStatus.fromDbValue(order.getStatus()).name();
            
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Order Detail</title>");
            html.append("<style>body{font-family:Arial;padding:20px;} .card{border:1px solid #ccc;padding:20px;border-radius:8px;} .btn{padding:10px 15px;background:#f44336;color:#fff;border:none;border-radius:4px;cursor:pointer;}</style>");
            html.append("</head><body><div class='card'>");
            html.append("<h2>Order #").append(order.getOrderId()).append("</h2>");
            html.append("<p><strong>Status:</strong> ").append(status).append("</p>");
            html.append("<p><strong>Total Amount:</strong> ").append(order.getTotalAmount()).append(" VND</p>");
            
            if ("PENDING_REVIEW".equals(status) || "PENDING".equals(status)) {
                html.append("<form method='POST' action='/api/orders/").append(order.getOrderId()).append("/cancel'>");
                html.append("<button type='submit' class='btn'>Cancel Order</button>");
                html.append("</form>");
            }
            
            html.append("</div></body></html>");
            return html.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "<h1>Error loading order</h1>";
        }
    }

    @PostMapping(value = "/{id}/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public String cancelOrder(@PathVariable("id") int orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                return "<h1>Order Not Found</h1>";
            }

            OrderStatus status = OrderStatus.fromDbValue(order.getStatus());
            if (status != OrderStatus.PENDING_REVIEW && status != OrderStatus.PROCESSING) {
                return "<h1>Cannot cancel order. Current status: " + status.name() + "</h1>";
            }

            orderReviewService.requestRefund(orderId);

            return "<html><body style='font-family:Arial;padding:20px;text-align:center;'>"
                    + "<h2>Order Cancelled Successfully!</h2>"
                    + "<p>Your order has been updated to REFUND_REQUEST.</p>"
                    + "</body></html>";
        } catch (SQLException e) {
            e.printStackTrace();
            return "<h1>Error cancelling order</h1>";
        }
    }
}

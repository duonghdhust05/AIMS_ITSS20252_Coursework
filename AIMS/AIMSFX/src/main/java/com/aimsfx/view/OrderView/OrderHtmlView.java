package com.aimsfx.view.html;

import com.aimsfx.model.Order;

public class OrderHtmlView {

    public static String renderOrderDetail(Order order, String status) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Order Detail</title>");
        html.append(
                "<style>body{font-family:Arial;padding:20px;} .card{border:1px solid #ccc;padding:20px;border-radius:8px;} .btn{padding:10px 15px;background:#f44336;color:#fff;border:none;border-radius:4px;cursor:pointer;}</style>");
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
    }

    public static String renderMessage(String title, String message) {
        return "<html><body style='font-family:Arial;padding:20px;text-align:center;'>"
                + "<h2>" + title + "</h2>"
                + "<p>" + message + "</p>"
                + "</body></html>";
    }

    public static String renderError(String errorMessage) {
        return "<h1>" + errorMessage + "</h1>";
    }
}

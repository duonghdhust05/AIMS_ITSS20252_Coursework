package com.aimsfx.service.webhook;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * Interface cho chiến lược xử lý Webhook (Strategy Pattern).
 * Đảm bảo OCP (Open/Closed Principle) khi thêm các cổng thanh toán mới.
 */
public interface IPaymentWebhookHandler {
    
    /**
     * Kiểm tra xem handler này có hỗ trợ xử lý callback từ gateway chỉ định không.
     * @param gatewayName tên của payment gateway (ví dụ: "vietqr", "paypal", "momo")
     * @return true nếu hỗ trợ
     */
    boolean supports(String gatewayName);

    /**
     * Thực thi logic xử lý payload callback từ cổng thanh toán.
     * @param payload Chuỗi JSON chứa dữ liệu callback
     * @param request HttpServletRequest gốc để lấy các thông tin như Headers (Authentication, Signature,...)
     * @return ResponseEntity kết quả trả về cho cổng thanh toán
     */
    ResponseEntity<?> handleWebhook(String payload, HttpServletRequest request);
}

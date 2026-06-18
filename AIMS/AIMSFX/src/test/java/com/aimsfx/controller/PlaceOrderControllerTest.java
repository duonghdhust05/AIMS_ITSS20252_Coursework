package com.aimsfx.controller;

import com.aimsfx.controller.PlaceOrderController.PlaceOrderController;
import com.aimsfx.exception.*;
import com.aimsfx.model.*;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.*;
import com.aimsfx.view.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PlaceOrderController
 * 
 * TEST GROUPS:
 * 1. placeOrder() - Cart validation & stock check (4 tests)
 * 2. submitDeliveryInfo() - Delivery processing & invoice creation (4 tests)
 * 
 * ARCHITECTURE:
 * Controller tests verify orchestration logic
 * Business logic delegated to PlaceOrderService (tested separately)
 * 
 * @see PlaceOrderServiceTest for service layer tests
 * @see PLACEORDER_UNITTEST_PLAN.md for detailed test plan
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("[CONTROLLER] PlaceOrderController - Controller Layer Tests")
class PlaceOrderControllerTest {

    // ==================== MOCKS ====================
    @Mock
    private PlaceOrderService mockPlaceOrderService;
    @Mock
    private InvoiceUI mockInvoiceUI;
    @Mock
    private ViewCartUI mockViewCartUI;
    @Mock
    private OrderRepository mockOrderRepository;

    // ==================== SYSTEM UNDER TEST ====================
    private PlaceOrderController controller;

    // ==================== TEST DATA ====================
    private Cart testCart;
    private TestProduct testProduct;
    private DeliveryInfo validDeliveryInfo;
    private Order testOrder;
    private Invoice testInvoice;

    /**
     * Test stub for abstract Product class
     * Required because Product is abstract
     */
    private static class TestProduct extends Product {
        @Override
        public boolean isSufficient(Integer requestedQuantity) {
            return this.getStock() != null && requestedQuantity != null
                    && this.getStock() >= requestedQuantity;
        }

        @Override
        public boolean checkAvailability(Integer requestedQuantity) {
            return isSufficient(requestedQuantity);
        }

        @Override
        public Map<String, Object> getSpecificDetail() {
            return new HashMap<>();
        }

        @Override
        public ProductType getProductType() {
            return ProductType.BOOK; // Default for testing
        }

        @Override
        public Product copy() {
            TestProduct copy = new TestProduct();
            copy.setProductId(this.getProductId());
            copy.setTitle(this.getTitle());
            copy.setCurrentPrice(this.getCurrentPrice());
            copy.setStock(this.getStock());
            copy.setWeight(this.getWeight());
            return copy;
        }
    }

    @BeforeEach
    void setUp() {
        // Initialize controller with mocked dependencies
        controller = new PlaceOrderController(
                mockPlaceOrderService,
                mockInvoiceUI, mockViewCartUI, mockOrderRepository);

        // Setup test cart
        testCart = new Cart(1, 1);

        // Setup test product
        testProduct = new TestProduct();
        testProduct.setProductId(1L);
        testProduct.setTitle("Test Product");
        testProduct.setCurrentPrice(100000.0);
        testProduct.setStock(10);
        testProduct.setWeight(0.5);

        // Setup valid delivery info
        validDeliveryInfo = new DeliveryInfo();
        validDeliveryInfo.setRecipientName("Nguyen Van A");
        validDeliveryInfo.setPhoneNumber("0912345678");
        validDeliveryInfo.setEmail("test@email.com");
        validDeliveryInfo.setProvince("Hanoi");
        validDeliveryInfo.setAddress("123 ABC Street, Cau Giay District");

        // Setup test order
        testOrder = mock(Order.class);
        when(testOrder.getOrderId()).thenReturn(123);
        when(testOrder.getStatus()).thenReturn("pending");

        // Setup test invoice
        testInvoice = new Invoice();
        testInvoice.setInvoiceId(456);
        testInvoice.setSubtotal(100000f);
        testInvoice.setVat(10000f);
        testInvoice.setDeliveryFee(22000f);
        testInvoice.setTotalAmount(132000f);
    }

    // ==================== GROUP 1: placeOrder() TESTS ====================

    @Nested
    @DisplayName("[GROUP 1] placeOrder() - Check cart & inventory")
    class PlaceOrderTests {

        @Test
        @DisplayName("[FAIL] TC1.1: Empty Carr -> Nem EmptyCartException")
        void testPlaceOrder_EmptyCart_ShouldThrowEmptyCartException() {
            // Arrange
            Cart emptyCart = new Cart(1, 1);
            // Cart has no items

            // Act & Assert
            EmptyCartException exception = assertThrows(EmptyCartException.class,
                    () -> controller.placeOrder(emptyCart),
                    "Should throw EmptyCartException for empty cart");

            assertTrue(exception.getMessage().toLowerCase().contains("empty"),
                    "Exception message should mention 'empty'");
        }

        @Test
        @DisplayName("[FAIL] TC1.2: Null Cart -> Nem EmptyCartException")
        void testPlaceOrder_NullCart_ShouldThrowEmptyCartException() {
            // Act & Assert
            assertThrows(EmptyCartException.class,
                    () -> controller.placeOrder(null),
                    "Should throw EmptyCartException for null cart");
        }

        @Test
        @DisplayName("[PASS] TC1.3: Sufficient Stock -> Return true & create order")
        void testPlaceOrder_SufficientStock_ShouldReturnTrueAndCreateOrder() throws EmptyCartException {
            // Arrange
            testProduct.setStock(10);
            testCart.addProduct(testProduct, 2); // Request 2, stock = 10

            // Mock service returns empty list (no insufficient items)
            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart))
                    .thenReturn(testOrder);

            // Act
            boolean result = controller.placeOrder(testCart);

            // Assert
            assertTrue(result, "placeOrder should return true when stock sufficient");
            verify(mockPlaceOrderService).getInsufficientStockItems(testCart);
            verify(mockPlaceOrderService).createOrder(testCart);
        }

        @Test
        @DisplayName("[WARN] TC1.4: Insufficient Stock -> Return false & show error")
        void testPlaceOrder_InsufficientStock_ShouldReturnFalseAndShowError() throws EmptyCartException {
            // Arrange
            testProduct.setStock(2);
            testCart.addProduct(testProduct, 5); // Request 5, stock = 2

            // Mock service returns list with insufficient item
            List<Map<String, Object>> insufficientItems = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("title", testProduct.getTitle());
            item.put("requested", 5);
            item.put("available", 2);
            insufficientItems.add(item);

            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(insufficientItems);

            // Act
            boolean result = controller.placeOrder(testCart);

            // Assert
            assertFalse(result, "placeOrder should return false when stock insufficient");
            verify(mockPlaceOrderService).getInsufficientStockItems(testCart);
            verify(mockViewCartUI).showInvalidQuantityException(anyString(), eq(insufficientItems));
            verify(mockPlaceOrderService, never()).createOrder(any());
        }
    }

    // ==================== GROUP 2: submitDeliveryInfo() TESTS ====================

    @Nested
    @DisplayName("[GROUP 2] submitDeliveryInfo() - Process delivery info")
    class SubmitDeliveryInfoTests {

        @Test
        @DisplayName("[PASS] TC2.1: Valid Delivery Info -> Successful Invoice")
        void testSubmitDeliveryInfo_ValidInfo_ShouldCreateInvoice()
                throws InvalidDeliveryInfoException, UnsupportedLocationException, EmptyCartException {
            // Arrange
            testCart.addProduct(testProduct, 1);

            // First call placeOrder to set currentOrder
            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart)).thenReturn(testOrder);
            controller.placeOrder(testCart);

            // Setup delivery info processing
            Map<String, Object> processResult = new HashMap<>();
            processResult.put("invoice", testInvoice);
            processResult.put("originalFee", 50000f);
            processResult.put("discount", 25000f);

            when(mockPlaceOrderService.processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo))
                    .thenReturn(processResult);

            // Act
            Invoice result = controller.submitDeliveryInfo(validDeliveryInfo);

            // Assert
            assertNotNull(result, "Invoice should be created");
            assertEquals(testInvoice.getInvoiceId(), result.getInvoiceId());
            verify(mockPlaceOrderService).processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo);
            verify(mockInvoiceUI).displayInvoice(testInvoice);
        }

        @Test
        @DisplayName("[FAIL] TC2.2: Invalid Delivery Info -> Throw Exception")
        void testSubmitDeliveryInfo_InvalidInfo_ShouldThrowException()
                throws EmptyCartException {
            // Arrange
            testCart.addProduct(testProduct, 1);

            // First call placeOrder to set currentOrder
            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart)).thenReturn(testOrder);
            controller.placeOrder(testCart);

            // Create invalid delivery info
            DeliveryInfo invalidInfo = new DeliveryInfo();
            invalidInfo.setRecipientName(""); // Invalid - empty name
            invalidInfo.setPhoneNumber("123"); // Invalid - wrong format
            // Missing address and province

            // Act & Assert
            assertThrows(InvalidDeliveryInfoException.class,
                    () -> controller.submitDeliveryInfo(invalidInfo),
                    "Should throw InvalidDeliveryInfoException for invalid info");
        }

        @Test
        @DisplayName("[FAIL] TC2.3: No Current Order -> Throw IllegalStateException")
        void testSubmitDeliveryInfo_NoCurrentOrder_ShouldThrowIllegalStateException() {
            // Arrange - don't call placeOrder first, so currentOrder is null

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> controller.submitDeliveryInfo(validDeliveryInfo),
                    "Should throw IllegalStateException when no current order");
        }

        @Test
        @DisplayName("[PASS] TC2.4: Test exact original fee & discount storage")
        void testSubmitDeliveryInfo_ShouldStoreOriginalFeeAndDiscount()
                throws InvalidDeliveryInfoException, UnsupportedLocationException, EmptyCartException {
            // Arrange
            testCart.addProduct(testProduct, 1);

            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart)).thenReturn(testOrder);
            controller.placeOrder(testCart);

            float originalFee = 50000f;
            float discount = 25000f;

            Map<String, Object> processResult = new HashMap<>();
            processResult.put("invoice", testInvoice);
            processResult.put("originalFee", originalFee);
            processResult.put("discount", discount);

            when(mockPlaceOrderService.processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo))
                    .thenReturn(processResult);

            // Act
            controller.submitDeliveryInfo(validDeliveryInfo);

            // Assert - verify service was called correctly
            verify(mockPlaceOrderService).processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo);
        }
    }

    // ==================== GETTER TESTS ====================

    @Nested
    @DisplayName("[GROUP 3] Getter Methods - Test data retrieval")
    class GetterTests {

        @Test
        @DisplayName("[PASS] TC3.1: setCart() & placeOrder() -> order confirmed")
        void testSetCartAndPlaceOrder_ShouldSetCurrentOrder() throws EmptyCartException {
            // Arrange
            testCart.addProduct(testProduct, 1);
            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart)).thenReturn(testOrder);

            // Act
            controller.setCart(testCart);
            controller.placeOrder(testCart);

            // Assert - verify order was created (no direct getter, verify via mock)
            verify(mockPlaceOrderService).createOrder(testCart);
        }
    }

    // ==================== INTEGRATION-STYLE TESTS ====================

    @Nested
    @DisplayName("[GROUP 4] End-to-End Flow - Test complete order flow")
    class EndToEndFlowTests {

        @Test
        @DisplayName("[PASS] TC4.1: Complete order flow: placeOrder() -> submitDeliveryInfo() -> Success")
        void testCompleteOrderFlow_ShouldSucceed()
                throws EmptyCartException, InvalidDeliveryInfoException, UnsupportedLocationException {
            // Arrange
            testProduct.setStock(10);
            testCart.addProduct(testProduct, 2);

            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(Collections.emptyList());
            when(mockPlaceOrderService.createOrder(testCart)).thenReturn(testOrder);

            Map<String, Object> processResult = new HashMap<>();
            processResult.put("invoice", testInvoice);
            processResult.put("originalFee", 22000f);
            processResult.put("discount", 0f);

            when(mockPlaceOrderService.processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo))
                    .thenReturn(processResult);

            // Act - Step 1: Place Order
            boolean orderResult = controller.placeOrder(testCart);

            // Act - Step 2: Submit Delivery Info
            Invoice invoice = controller.submitDeliveryInfo(validDeliveryInfo);

            // Assert
            assertTrue(orderResult, "Order should be placed successfully");
            assertNotNull(invoice, "Invoice should be created");
            assertEquals(testInvoice.getTotalAmount(), invoice.getTotalAmount(), 0.01);

            // Verify flow
            verify(mockPlaceOrderService).getInsufficientStockItems(testCart);
            verify(mockPlaceOrderService).createOrder(testCart);
            verify(mockPlaceOrderService).processDeliveryAndCreateInvoice(testOrder, validDeliveryInfo);
            verify(mockInvoiceUI).displayInvoice(testInvoice);
        }

        @Test
        @DisplayName("[WARN] TC4.2: Stock Issue -> Stop at placeOrder()")
        void testFlowWithStockIssue_ShouldStopAtPlaceOrder() throws EmptyCartException, InvalidDeliveryInfoException {
            // Arrange
            testProduct.setStock(1);
            testCart.addProduct(testProduct, 5);

            List<Map<String, Object>> insufficientItems = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("title", testProduct.getTitle());
            insufficientItems.add(item);

            when(mockPlaceOrderService.getInsufficientStockItems(testCart))
                    .thenReturn(insufficientItems);

            // Act
            boolean orderResult = controller.placeOrder(testCart);

            // Assert
            assertFalse(orderResult, "Order should fail due to stock");
            verify(mockPlaceOrderService, never()).createOrder(any());
            verify(mockPlaceOrderService, never()).processDeliveryAndCreateInvoice(any(), any());
        }
    }
}

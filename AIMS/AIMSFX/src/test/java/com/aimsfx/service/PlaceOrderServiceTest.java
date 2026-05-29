package com.aimsfx.service;

import com.aimsfx.exception.*;
import com.aimsfx.model.*;
import com.aimsfx.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PlaceOrderService
 * 
 * TEST GROUPS:
 * 1. Delivery Form Validation (4 tests)
 * 2. Delivery Logic - Fees, VAT, Promotion (4 tests)
 * 3. Stock/Inventory Validation (4 tests)
 * 4. Order Creation & Post-actions (4 tests)
 * 
 * SOLID VERIFICATION:
 * - Tests verify DIP compliance with mock dependency injection
 * - Tests ensure SRP (business logic only in service)
 * 
 * @see PLACEORDER_UNITTEST_PLAN.md for detailed test plan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("[SERVICE] PlaceOrderService - Business Logic Layer Tests")
class PlaceOrderServiceTest {

    // ==================== MOCKS ====================
    @Mock private IOrderStorage mockStorage;
    @Mock private IEmailSender mockEmailSender;
    @Mock private IPriceHelper mockPriceHelper;
    @Mock private IDeliveryFeeCalculator mockFeeCalculator;
    @Mock private ICartService mockCartService;
    @Mock private ProductRepository mockProductRepository;

    // ==================== SYSTEM UNDER TEST ====================
    private PlaceOrderService placeOrderService;

    // ==================== TEST DATA ====================
    private Cart testCart;
    private Book testBook;
    private CD testCD;
    private DeliveryInfo validDeliveryInfo;

    @BeforeEach
    void setUp() {
        // Initialize service with mocked dependencies
        placeOrderService = new PlaceOrderService(
            mockStorage, mockEmailSender, mockPriceHelper, 
            mockFeeCalculator, mockCartService);

        // Setup test cart
        testCart = new Cart(1, 1);

        // Setup test book
        testBook = new Book();
        testBook.setProductId(1L);
        testBook.setBarcode("BOOK-001");
        testBook.setTitle("Test Book");
        testBook.setCurrentPrice(100000.0);
        testBook.setStock(10);
        testBook.setWeight(0.5);

        // Setup test CD
        testCD = new CD();
        testCD.setProductId(2L);
        testCD.setBarcode("CD-001");
        testCD.setTitle("Test CD");
        testCD.setCurrentPrice(80000.0);
        testCD.setStock(5);
        testCD.setWeight(0.1);

        // Setup valid delivery info
        validDeliveryInfo = new DeliveryInfo();
        validDeliveryInfo.setRecipientName("Nguyen Van A");
        validDeliveryInfo.setPhoneNumber("0912345678");
        validDeliveryInfo.setEmail("test@email.com");
        validDeliveryInfo.setProvince("Hà Nội");
        validDeliveryInfo.setAddress("123 ABC Street, Cau Giay District");
    }

    // ==================== NHÓM 1: DELIVERY FORM VALIDATION (4 Tests) ====================
    
    @Nested
    @DisplayName("[NHOM 1] Delivery Form Validation - Kiem tra form giao hang")
    class DeliveryFormValidationTests {

        @Test
        @DisplayName("[FAIL] TC1.1: Thieu so dien thoai -> Tra ve loi validation")
        void testValidateDeliveryInfo_MissingPhone_ShouldReturnError() {
            // Arrange
            String name = "Nguyen Van A";
            String phone = null;  // Missing phone
            String email = "test@email.com";
            String province = "Hà Nội";
            String address = "123 ABC Street, Cau Giay District";

            // Act
            List<String> errors = placeOrderService.validateDeliveryInfo(name, phone, email, province, address);

            // Assert
            assertFalse(errors.isEmpty(), "Should return validation errors");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("phone")),
                    "Error list should contain phone-related error");
        }

        @Test
        @DisplayName("[FAIL] TC1.2: Email format khong hop le -> Tra ve loi email")
        void testValidateDeliveryInfo_InvalidEmail_ShouldReturnError() {
            // Arrange
            String name = "Nguyen Van A";
            String phone = "0912345678";
            String email = "invalid-email-format";  // Invalid email
            String province = "Hà Nội";
            String address = "123 ABC Street, Cau Giay District";

            // Act
            List<String> errors = placeOrderService.validateDeliveryInfo(name, phone, email, province, address);

            // Assert
            assertFalse(errors.isEmpty(), "Should return validation errors for invalid email");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("email")),
                    "Error list should contain email-related error");
        }

        @Test
        @DisplayName("[PASS] TC1.3: Tat ca fields hop le -> Tra ve danh sach rong (khong loi)")
        void testValidateDeliveryInfo_AllFieldsValid_ShouldReturnEmptyList() {
            // Arrange
            String name = "Nguyen Van A";
            String phone = "0912345678";
            String email = "test@email.com";
            String province = "Hà Nội";
            String address = "123 ABC Street, Cau Giay District";

            // Act
            List<String> errors = placeOrderService.validateDeliveryInfo(name, phone, email, province, address);

            // Assert
            assertTrue(errors.isEmpty(), "Should return no errors for valid input");
        }

        @Test
        @DisplayName("[FAIL] TC1.4: Thieu nhieu fields -> Tra ve tat ca loi")
        void testValidateDeliveryInfo_MissingMultipleFields_ShouldReturnAllErrors() {
            // Arrange - all fields are null/empty
            String name = null;
            String phone = null;
            String email = null;
            String province = null;
            String address = null;

            // Act
            List<String> errors = placeOrderService.validateDeliveryInfo(name, phone, email, province, address);

            // Assert
            assertTrue(errors.size() >= 4, "Should return at least 4 errors (name, phone, province, address)");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("name")),
                    "Should have name error");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("phone")),
                    "Should have phone error");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("province")),
                    "Should have province error");
            assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("address")),
                    "Should have address error");
        }
    }

    // ==================== NHÓM 2: DELIVERY LOGIC (4 Tests) ====================
    
    @Nested
    @DisplayName("[NHOM 2] Delivery Logic - Phi van chuyen, VAT, Khuyen mai")
    class DeliveryLogicTests {

        @Test
        @DisplayName("[CALC] TC2.1: Tinh phi van chuyen cho Ha Noi = 22,000 VND")
        void testCalculateDeliveryFee_HanoiLocation_ShouldReturnCorrectFee() {
            // Arrange
            DeliveryInfo hanoiDelivery = new DeliveryInfo();
            hanoiDelivery.setProvince("Hà Nội");
            
            testCart.addProduct(testBook, 2);  // 2 books = 1.0kg
            float totalWeight = 1.0f;
            
            // Mock fee calculator to return expected fee
            when(mockCartService.calculateTotalWeight(testCart)).thenReturn(totalWeight);
            when(mockFeeCalculator.calculateFee(any(DeliveryInfo.class), eq(totalWeight)))
                .thenReturn(22000.0);

            // Act
            double fee = placeOrderService.calculateDeliveryFee(hanoiDelivery, testCart);

            // Assert
            assertEquals(22000.0, fee, 0.01, "Delivery fee for Hanoi should be 22,000 VND for weight <= 3kg");
            verify(mockFeeCalculator).calculateFee(any(DeliveryInfo.class), eq(totalWeight));
        }

        @Test
        @DisplayName("[PROMO] TC2.2: Mien phi ship (toi da 25K) khi subtotal > 100,000 VND")
        void testProcessDeliveryFeeWithDiscount_SubtotalOver100K_ShouldApplyFreeShip() {
            // Arrange
            double subtotal = 150000.0;  // > 100,000 VND
            float totalWeight = 1.5f;
            float originalFee = 50000f;
            
            when(mockCartService.calculateTotalWeight(testCart)).thenReturn(totalWeight);
            when(mockFeeCalculator.calculateFee(any(DeliveryInfo.class), eq(totalWeight)))
                .thenReturn((double) originalFee);

            // Act
            Map<String, Object> result = placeOrderService.processDeliveryFeeWithDiscount(
                validDeliveryInfo, testCart, subtotal);

            // Assert
            assertEquals(originalFee, ((Number) result.get("originalFee")).floatValue(), 0.01,
                    "Original fee should be preserved");
            assertEquals(25000f, ((Number) result.get("discount")).floatValue(), 0.01,
                    "Discount should be 25,000 VND (max)");
            assertEquals(25000f, ((Number) result.get("deliveryFee")).floatValue(), 0.01,
                    "Final delivery fee should be 50,000 - 25,000 = 25,000 VND");
        }

        @Test
        @DisplayName("[WARN] TC2.3: KHONG mien phi ship khi subtotal <= 100,000 VND")
        void testProcessDeliveryFeeWithDiscount_SubtotalUnder100K_ShouldNotApplyFreeShip() {
            // Arrange
            double subtotal = 80000.0;  // <= 100,000 VND
            float totalWeight = 1.0f;
            float originalFee = 50000f;
            
            when(mockCartService.calculateTotalWeight(testCart)).thenReturn(totalWeight);
            when(mockFeeCalculator.calculateFee(any(DeliveryInfo.class), eq(totalWeight)))
                .thenReturn((double) originalFee);

            // Act
            Map<String, Object> result = placeOrderService.processDeliveryFeeWithDiscount(
                validDeliveryInfo, testCart, subtotal);

            // Assert
            assertEquals(originalFee, ((Number) result.get("originalFee")).floatValue(), 0.01,
                    "Original fee should be preserved");
            assertEquals(0f, ((Number) result.get("discount")).floatValue(), 0.01,
                    "Discount should be 0 (no free ship)");
            assertEquals(originalFee, ((Number) result.get("deliveryFee")).floatValue(), 0.01,
                    "Final delivery fee should equal original fee");
        }

        @Test
        @DisplayName("[CALC] TC2.4: VAT = 10% cua subtotal (thue GTGT)")
        void testCalculateVAT_ShouldReturn10Percent() {
            // Arrange
            double subtotal = 200000.0;
            double expectedVAT = 20000.0;  // 10% of 200,000
            
            when(mockCartService.calculateVAT(subtotal)).thenReturn(expectedVAT);

            // Act
            double vat = placeOrderService.calculateVAT(subtotal);

            // Assert
            assertEquals(expectedVAT, vat, 0.01, "VAT should be 10% of subtotal");
            verify(mockCartService).calculateVAT(subtotal);
        }
    }

    // ==================== NHÓM 3: STOCK/INVENTORY VALIDATION (4 Tests) ====================
    
    @Nested
    @DisplayName("[NHOM 3] Stock/Inventory Validation - Kiem tra ton kho")
    class StockValidationTests {

        @Test
        @DisplayName("[FAIL] TC3.1: Gio hang trong -> Nem EmptyCartException")
        void testCreateOrderFromCart_EmptyCart_ShouldThrowEmptyCartException() {
            // Arrange
            Cart emptyCart = new Cart(1, 1);
            // Cart has no items

            // Act & Assert
            assertThrows(EmptyCartException.class, 
                () -> placeOrderService.createOrderFromCart(emptyCart),
                "Should throw EmptyCartException for empty cart");
        }

        @Test
        @DisplayName("[FAIL] TC3.2: Gio hang null -> Nem EmptyCartException")
        void testCreateOrderFromCart_NullCart_ShouldThrowEmptyCartException() {
            // Act & Assert
            assertThrows(EmptyCartException.class, 
                () -> placeOrderService.createOrderFromCart(null),
                "Should throw EmptyCartException for null cart");
        }

        @Test
        @DisplayName("[PASS] TC3.3: Tat ca items du stock -> Tra ve Order thanh cong")
        void testCreateOrderFromCart_SufficientStock_ShouldReturnOrder() throws Exception {
            // Arrange
            testBook.setStock(10);  // Plenty of stock
            testCart.addProduct(testBook, 2);  // Request 2, stock = 10
            
            when(mockStorage.save(any(Order.class))).thenReturn(123);

            // Act
            Order order = placeOrderService.createOrderFromCart(testCart);

            // Assert
            assertNotNull(order, "Order should be created");
            assertEquals(123, order.getOrderId(), "Order ID should be set from storage");
            assertEquals("pending", order.getStatus(), "Order status should be pending");
            verify(mockStorage).save(any(Order.class));
        }

        @Test
        @DisplayName("[WARN] TC3.4: Item thieu stock -> Nem OutOfStockException")
        void testCreateOrderFromCart_InsufficientStock_ShouldThrowOutOfStockException() {
            // Arrange
            testBook.setStock(1);  // Only 1 in stock
            testCart.addProduct(testBook, 5);  // Request 5, stock = 1

            // Act & Assert
            OutOfStockException exception = assertThrows(OutOfStockException.class, 
                () -> placeOrderService.createOrderFromCart(testCart),
                "Should throw OutOfStockException when stock insufficient");
            
            assertTrue(exception.getMessage().contains(testBook.getTitle()),
                    "Exception message should contain product name");
        }
    }

    // ==================== NHÓM 4: ORDER CREATION & POST-ACTIONS (4 Tests) ====================
    
    @Nested
    @DisplayName("[NHOM 4] Order Creation & Post-actions - Tao don & xu ly sau don")
    class OrderCreationTests {

        @Test
        @DisplayName("[PASS] TC4.1: Gio hang hop le -> Tao order voi status = 'pending'")
        void testCreateOrder_ValidCart_ShouldReturnOrderWithPendingStatus() {
            // Arrange
            testCart.addProduct(testBook, 2);

            // Act
            Order order = placeOrderService.createOrder(testCart);

            // Assert
            assertNotNull(order, "Order should not be null");
            assertEquals("pending", order.getStatus(), "Order status should be 'pending'");
            assertNotNull(order.getCreatedDate(), "Created date should be set");
        }

        @Test
        @DisplayName("[FAIL] TC4.2: Gio hang trong -> createOrder() tra ve null")
        void testCreateOrder_EmptyCart_ShouldReturnNull() {
            // Arrange
            Cart emptyCart = new Cart(1, 1);

            // Act
            Order order = placeOrderService.createOrder(emptyCart);

            // Assert
            assertNull(order, "Order should be null for empty cart");
        }

        @Test
        @DisplayName("[DB] TC4.3: Luu order vao DB -> Tra ve order voi ID tu sinh")
        void testSaveOrderToDatabase_ValidOrder_ShouldReturnOrderWithId() {
            // Arrange
            testCart.addProduct(testBook, 1);
            Order order = placeOrderService.createOrder(testCart);
            int expectedId = 456;
            
            when(mockStorage.save(order)).thenReturn(expectedId);

            // Act
            Order savedOrder = placeOrderService.saveOrderToDatabase(order);

            // Assert
            assertNotNull(savedOrder, "Saved order should not be null");
            assertEquals(expectedId, savedOrder.getOrderId(), "Order ID should be set from storage");
            verify(mockStorage).save(order);
        }

        @Test
        @DisplayName("[GRACEFUL] TC4.4: Email service loi -> Order van thanh cong (non-critical)")
        void testSendOrderConfirmationEmail_EmailServiceDown_ShouldNotThrowException() {
            // Arrange
            testCart.addProduct(testBook, 1);
            Order order = placeOrderService.createOrder(testCart);
            TransactionInfo transactionInfo = new TransactionInfo(
                1, 1, "VietQR", java.math.BigDecimal.valueOf(100000), "VND");
            
            // Mock email sender to throw exception
            doThrow(new RuntimeException("Email service unavailable"))
                .when(mockEmailSender).sendConfirmation(any(Order.class), anyString());

            // Act & Assert - should NOT throw exception
            assertDoesNotThrow(() -> {
                placeOrderService.sendOrderConfirmationEmail(order, validDeliveryInfo, transactionInfo);
                // Give async operation time to complete
                Thread.sleep(100);
            }, "Email failure should not cause exception");
        }
    }

    // ==================== ADDITIONAL EDGE CASE TESTS ====================
    
    @Nested
    @DisplayName("[NHOM 5] Edge Cases - Cac truong hop bien")
    class EdgeCaseTests {

        @Test
        @DisplayName("[PARSE] TC5.1: Parse phi ship tu string '50,000 VND' -> 50000.0")
        void testParseDeliveryFee_ValidFormat_ShouldReturnCorrectValue() {
            // Arrange
            String feeText = "50,000 VND";

            // Act
            double fee = placeOrderService.parseDeliveryFee(feeText);

            // Assert
            assertEquals(50000.0, fee, 0.01, "Should parse 50,000 VND correctly");
        }

        @Test
        @DisplayName("[PARSE] TC5.2: Parse phi ship tu null/empty -> tra ve 0")
        void testParseDeliveryFee_NullOrEmpty_ShouldReturnZero() {
            // Act & Assert
            assertEquals(0.0, placeOrderService.parseDeliveryFee(null), 0.01);
            assertEquals(0.0, placeOrderService.parseDeliveryFee(""), 0.01);
            assertEquals(0.0, placeOrderService.parseDeliveryFee("   "), 0.01);
        }

        @Test
        @DisplayName("[PASS] TC5.3: Tao DeliveryInfo tu form voi du lieu hop le")
        void testCreateDeliveryInfoFromForm_ValidData_ShouldReturnDeliveryInfo() 
                throws InvalidDeliveryInfoException {
            // Arrange
            String name = "Nguyen Van A";
            String phone = "0912345678";
            String email = "test@email.com";
            String province = "Hà Nội";
            String ward = "Cau Giay";
            String address = "123 ABC Street, Cau Giay District";
            String instructions = "Call before delivery";

            // Act
            DeliveryInfo info = placeOrderService.createDeliveryInfoFromForm(
                name, phone, email, province, ward, address, instructions);

            // Assert
            assertNotNull(info, "DeliveryInfo should be created");
            assertEquals(name, info.getRecipientName());
            assertEquals(phone, info.getPhoneNumber());
            assertEquals(email, info.getEmail());
            assertEquals(province, info.getProvince());
            assertEquals(address, info.getAddress());
        }

        @Test
        @DisplayName("[FAIL] TC5.4: Tao DeliveryInfo voi du lieu sai -> Nem exception")
        void testCreateDeliveryInfoFromForm_InvalidData_ShouldThrowException() {
            // Arrange - missing required fields
            String name = "";
            String phone = "invalid";
            String email = null;
            String province = null;
            String ward = null;
            String address = "";
            String instructions = null;

            // Act & Assert
            assertThrows(InvalidDeliveryInfoException.class,
                () -> placeOrderService.createDeliveryInfoFromForm(
                    name, phone, email, province, ward, address, instructions),
                "Should throw exception for invalid delivery info");
        }
    }
}

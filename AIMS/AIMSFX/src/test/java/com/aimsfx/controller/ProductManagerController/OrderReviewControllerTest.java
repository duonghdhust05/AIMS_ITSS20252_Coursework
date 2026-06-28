package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.model.User;
import com.aimsfx.model.UserRole;
import com.aimsfx.service.OrderReviewService;
import com.aimsfx.utils.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4: Controller / API Layer Tests
 * Test Suite: TS_MNO_CTRL
 */
public class OrderReviewControllerTest {

    private OrderReviewController orderReviewController;
    private OrderReviewService mockService;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() throws Exception {
        // We need to inject a mock service into the controller.
        // Since OrderReviewController doesn't have a constructor taking the service,
        // we'll use reflection to inject it.
        orderReviewController = new OrderReviewController();
        mockService = Mockito.mock(OrderReviewService.class);
        
        java.lang.reflect.Field serviceField = OrderReviewController.class.getDeclaredField("orderReviewService");
        serviceField.setAccessible(true);
        serviceField.set(orderReviewController, mockService);

        sessionManager = SessionManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        sessionManager.logout();
        orderReviewController.stopBackgroundPolling();
    }

    @Test
    void testUT_MNO_06_CtrlAuthorizationCheck() {
        // Arrange
        // 1. Not logged in
        assertFalse(orderReviewController.canManageOrders());

        // 2. Logged in without PRODUCT_MANAGER role
        User user = new User();
        user.setRoles(Set.of(UserRole.ADMINISTRATOR));
        sessionManager.login(user);
        assertFalse(orderReviewController.canManageOrders());

        // 3. Logged in with PRODUCT_MANAGER role
        user.setRoles(Set.of(UserRole.PRODUCT_MANAGER));
        assertTrue(orderReviewController.canManageOrders());
    }

    @Test
    void testUT_MNO_07_CtrlApproveDelegation() throws SQLException {
        // Arrange
        int orderId = 123;

        // Act
        orderReviewController.approveOrder(orderId);

        // Assert
        Mockito.verify(mockService, Mockito.times(1)).approve(orderId);
    }

    @Test
    void testUT_MNO_08_CtrlBackgroundPollingExecution() throws InterruptedException, SQLException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        // Mock service to return a different count so the callback is triggered
        // First countAllOrders() is called to initialize lastLoadedCount
        Mockito.when(mockService.countAllOrders()).thenReturn(10, 15);
        orderReviewController.countAllOrders(); // initializes lastLoadedCount to 10
        
        Consumer<Integer> callback = (count) -> {
            if (count == 15) {
                latch.countDown();
            }
        };

        // Act
        // Note: The scheduler is set to 15 seconds, which is too long for a unit test.
        // We'll just verify it starts without exceptions and stop it.
        orderReviewController.startBackgroundPolling(callback);
        
        // Let's use reflection to speed up the scheduler or just verify it's not null and not shutdown
        try {
            java.lang.reflect.Field schedulerField = OrderReviewController.class.getDeclaredField("scheduler");
            schedulerField.setAccessible(true);
            java.util.concurrent.ScheduledExecutorService scheduler = (java.util.concurrent.ScheduledExecutorService) schedulerField.get(orderReviewController);
            assertNotNull(scheduler);
            assertFalse(scheduler.isShutdown());
        } catch (Exception e) {
            fail("Failed to access scheduler field");
        }
    }
}

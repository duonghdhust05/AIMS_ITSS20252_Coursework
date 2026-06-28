package com.aimsfx.controller;

import com.aimsfx.exception.EmptyCartException;
import com.aimsfx.exception.InvalidDeliveryInfoException;
import com.aimsfx.model.Cart;
import com.aimsfx.model.DeliveryInfo;
import com.aimsfx.model.Order;
import com.aimsfx.repository.OrderRepository;
import com.aimsfx.service.PlaceOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Phase 4: Controller Layer
 * Test Suite: TS_PLO_CTRL
 */
public class PlaceOrderControllerTest {

    private PlaceOrderController controller;
    private PlaceOrderService mockService;
    private OrderRepository mockRepo;

    @BeforeEach
    void setUp() throws Exception {
        controller = new PlaceOrderController();
        mockService = Mockito.mock(PlaceOrderService.class);
        mockRepo = Mockito.mock(OrderRepository.class);

        // Inject mocks via reflection
        java.lang.reflect.Field serviceField = PlaceOrderController.class.getDeclaredField("placeOrderService");
        serviceField.setAccessible(true);
        serviceField.set(controller, mockService);

        java.lang.reflect.Field repoField = PlaceOrderController.class.getDeclaredField("orderRepository");
        repoField.setAccessible(true);
        repoField.set(controller, mockRepo);
    }

    @Test
    void testUT_PLO_07_CtrlSubmitOrder_EmptyCart() {
        // Arrange
        Cart emptyCart = new Cart(1, 1);
        DeliveryInfo info = new DeliveryInfo();

        // Act & Assert
        assertThrows(EmptyCartException.class, () -> controller.processOrderCreation(emptyCart, info));
    }

    @Test
    void testUT_PLO_07_CtrlSubmitOrder_Success() throws Exception {
        // Arrange
        Cart cart = new Cart(1, 1);
        cart.addProduct(new com.aimsfx.model.Book(), 1);
        DeliveryInfo info = new DeliveryInfo();
        
        Order mockOrder = new Order();
        when(mockService.checkProductAvailability(cart)).thenReturn(null);
        when(mockService.createAndSaveOrder(eq(cart), eq(info), any())).thenReturn(mockOrder);

        // Act
        Order result = controller.processOrderCreation(cart, info);

        // Assert
        assertNotNull(result);
        verify(mockService, times(1)).createAndSaveOrder(eq(cart), eq(info), any());
    }

    @Test
    void testUT_PLO_07_CtrlSubmitDeliveryInfo_Invalid() {
        // Arrange
        Order order = new Order();
        DeliveryInfo info = Mockito.mock(DeliveryInfo.class);
        when(info.checkValidityOfDeliveryInfo()).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidDeliveryInfoException.class, () -> controller.submitDeliveryInfo(order, info));
    }

    @Test
    void testUT_PLO_07_CtrlSubmitDeliveryInfo_Success() throws Exception {
        // Arrange
        Order order = new Order();
        DeliveryInfo info = Mockito.mock(DeliveryInfo.class);
        when(info.checkValidityOfDeliveryInfo()).thenReturn(true);
        
        Map<String, Object> expectedMap = new HashMap<>();
        when(mockService.processDeliveryAndCreateInvoice(order, info)).thenReturn(expectedMap);

        // Act
        Map<String, Object> result = controller.submitDeliveryInfo(order, info);

        // Assert
        assertSame(expectedMap, result);
        verify(mockService, times(1)).processDeliveryAndCreateInvoice(order, info);
    }
}

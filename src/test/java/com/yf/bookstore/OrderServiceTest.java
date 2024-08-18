package com.yf.bookstore;

import com.yf.bookstore.exception.inventory.InsufficientStockException;
import com.yf.bookstore.exception.order.OrderAlreadyProcessedException;
import com.yf.bookstore.exception.order.OrderNotFoundException;
import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.model.order.Order;
import com.yf.bookstore.model.order.OrderStatus;
import com.yf.bookstore.repository.OrderRepository;
import com.yf.bookstore.repository.SkuRepository;
import com.yf.bookstore.service.OrderService;
import com.yf.bookstore.service.StockService;
import com.yf.bookstore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createOrder_InsufficientStockException_ShouldThrowException() {
        Sku sku = new Sku();
        sku.setId(1L);

        when(skuRepository.findById(anyLong())).thenReturn(Optional.of(sku));
        when(stockService.decreaseStock(anyLong(), anyInt())).thenThrow(new InsufficientStockException(1L));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder("1", sku));
    }

    @Test
    public void createOrder_ValidInput_ShouldCreateOrder() {
        Sku sku = new Sku();
        sku.setId(1L);

        when(skuRepository.findById(anyLong())).thenReturn(Optional.of(sku));

        Order order = orderService.createOrder("1", sku);

        assertNotNull(order);
        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void cancelOrder_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    public void cancelOrder_OrderAlreadyProcessed_ShouldThrowException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        assertThrows(OrderAlreadyProcessedException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    public void cancelOrder_ValidInput_ShouldCancelOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        Order cancelledOrder = orderService.cancelOrder(1L);

        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(orderRepository, times(1)).save(cancelledOrder);
    }

    @Test
    public void getOrderStatus_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderStatus(1L));
    }

    @Test
    public void getOrderStatus_ValidInput_ShouldReturnStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        OrderStatus status = orderService.getOrderStatus(1L);

        assertNotNull(status);
        assertEquals(OrderStatus.CREATED, status);
    }
}


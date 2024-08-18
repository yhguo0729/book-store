package com.yf.bookstore.service;

import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.model.order.Order;
import com.yf.bookstore.model.order.OrderStatus;

public interface OrderService {

    Order createOrder(String customerId, Sku sku);

    Order cancelOrder(Long orderId);

    OrderStatus getOrderStatus(Long orderId);
}


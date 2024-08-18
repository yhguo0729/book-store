package com.yf.bookstore.exception.order;

import com.yf.bookstore.exception.BaseException;

public class OrderNotFoundException extends BaseException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }
}
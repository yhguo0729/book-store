package com.yf.bookstore.exception.order;

import com.yf.bookstore.exception.BaseException;

public class OrderAlreadyProcessedException extends BaseException {
    public OrderAlreadyProcessedException(Long orderId) {
        super("Order with ID: " + orderId + " has already been processed and cannot be cancelled.");
    }
}

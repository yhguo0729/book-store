package com.yf.bookstore.exception.inventory;

import com.yf.bookstore.exception.BaseException;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(Long skuId) {
        super("Insufficient stock for SKU: " + skuId);
    }
}

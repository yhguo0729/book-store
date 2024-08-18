package com.yf.bookstore.exception.inventory;

import com.yf.bookstore.exception.BaseException;

public class StockNotFoundException extends BaseException {
    public StockNotFoundException(Long skuId) {
        super("Stock not found with ID: " + skuId);
    }
}
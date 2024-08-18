package com.yf.bookstore.exception.commodity;

import com.yf.bookstore.exception.BaseException;

public class SkuNotFoundException extends BaseException {
    public SkuNotFoundException(Long skuId) {
        super("Sku not found with ID: " + skuId);
    }
}

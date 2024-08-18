package com.yf.bookstore.service;

import com.yf.bookstore.model.inventory.Stock;

public interface StockService {
    Stock increaseStock(Long skuId, int quantity);

    Stock decreaseStock(Long skuId, int quantity);

    Stock getStockBySkuId(Long skuId);

    Stock deleteStock(Long skuId);

    Stock updateStock(Stock stock);

    Stock createStock(Stock stock);

}

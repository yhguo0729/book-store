package com.yf.bookstore.service;

import com.yf.bookstore.model.commodity.Sku;


public interface SkuService {

    Sku createSku(Sku sku);

    Sku updateSku(Sku sku);

    void deleteSku(Long id);

    Sku getSkuById(Long id);
}

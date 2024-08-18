package com.yf.bookstore.repository;

import com.yf.bookstore.model.commodity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {
}

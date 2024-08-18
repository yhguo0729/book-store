package com.yf.bookstore.repository;

import com.yf.bookstore.model.inventory.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}

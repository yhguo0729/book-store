package com.yf.bookstore.controller;

import com.yf.bookstore.model.inventory.Stock;
import com.yf.bookstore.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器，处理与库存相关的HTTP请求
 */
@RestController
@RequestMapping("/stocks")
public class StockController {

    /**
     * 注入的库存服务层，用于处理库存业务逻辑
     */
    private final StockService stockService;

    /**
     * 构造器注入库存服务
     *
     * @param stockService 库存服务层实例
     */
    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 创建库存记录
     *
     * @param stock 新的库存记录
     * @return 创建后的库存记录
     */
    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        Stock createdStock = stockService.createStock(stock);
        return ResponseEntity.ok(createdStock);
    }

    /**
     * 更新库存记录
     *
     * @param id    库存记录的ID
     * @param stock 更新后的库存记录
     * @return 更新后的库存记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id, @RequestBody Stock stock) {
        stock.setId(id);
        Stock updatedStock = stockService.updateStock(stock);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * 删除库存记录
     *
     * @param id 库存记录的ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取库存记录
     *
     * @param id 库存记录的ID
     * @return 库存记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        Stock stock = stockService.getStockBySkuId(id);
        return ResponseEntity.ok(stock);
    }
}

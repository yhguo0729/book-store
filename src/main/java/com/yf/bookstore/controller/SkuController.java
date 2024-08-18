package com.yf.bookstore.controller;

import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SkuController 类用于处理与Sku相关的HTTP请求
 */
@RestController
@RequestMapping("/skus")
public class SkuController {

    /**
     * skuService 是与Sku相关的业务逻辑的接口
     */
    private final SkuService skuService;

    /**
     * 构造函数注入SkuService
     *
     * @param skuService SkuService实例
     */
    @Autowired
    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    /**
     * 创建一个新的Sku
     *
     * @param sku 要创建的Sku对象
     * @return 创建后的Sku对象
     */
    @PostMapping
    public ResponseEntity<Sku> createSku(@RequestBody Sku sku) {
        Sku createdSku = skuService.createSku(sku);
        return ResponseEntity.ok(createdSku);
    }

    /**
     * 更新现有的Sku
     *
     * @param id  Sku的ID
     * @param sku 要更新的Sku对象
     * @return 更新后的Sku对象
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sku> updateSku(@PathVariable Long id, @RequestBody Sku sku) {
        sku.setId(id);
        Sku updatedSku = skuService.updateSku(sku);
        return ResponseEntity.ok(updatedSku);
    }

    /**
     * 删除指定的Sku
     *
     * @param id Sku的ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSku(@PathVariable Long id) {
        skuService.deleteSku(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取Sku
     *
     * @param id Sku的ID
     * @return 请求的Sku对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sku> getSkuById(@PathVariable Long id) {
        Sku sku = skuService.getSkuById(id);
        return ResponseEntity.ok(sku);
    }
}
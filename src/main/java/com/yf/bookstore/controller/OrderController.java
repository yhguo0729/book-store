package com.yf.bookstore.controller;

import com.yf.bookstore.exception.commodity.SkuNotFoundException;
import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.model.order.Order;
import com.yf.bookstore.model.order.OrderStatus;
import com.yf.bookstore.service.OrderService;
import com.yf.bookstore.service.SkuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    // 注入订单服务
    private final OrderService orderService;
    // 注入库存单位服务
    private final SkuService skuService;

    // 构造函数，用于注入服务层对象
    public OrderController(OrderService orderService, SkuService skuService) {
        this.orderService = orderService;
        this.skuService = skuService;
    }

    /**
     * 创建订单
     *
     * @param customerId 顾客ID
     * @param skuId      商品库存单位ID
     * @return 创建完成的订单
     * @throws SkuNotFoundException 如果库存单位不存在
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam String customerId,
                                             @RequestParam Long skuId) {
        // 根据ID获取商品库存单位
        Sku sku = skuService.getSkuById(skuId);
        if (sku == null) {
            // 如果商品库存单位不存在，则抛出异常
            throw new SkuNotFoundException(skuId);
        }
        // 创建订单并返回
        Order order = orderService.createOrder(customerId, sku);
        return ResponseEntity.ok(order);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 取消后的订单信息
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        // 取消订单并返回
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * 获取订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatus> getOrderStatus(@PathVariable Long orderId) {
        // 获取并返回订单状态
        OrderStatus status = orderService.getOrderStatus(orderId);
        return ResponseEntity.ok(status);
    }
}

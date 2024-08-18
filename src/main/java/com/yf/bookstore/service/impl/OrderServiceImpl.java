package com.yf.bookstore.service.impl;

import com.yf.bookstore.exception.inventory.InsufficientStockException;
import com.yf.bookstore.exception.order.OrderAlreadyProcessedException;
import com.yf.bookstore.exception.order.OrderNotFoundException;
import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.model.order.Order;
import com.yf.bookstore.model.order.OrderStatus;
import com.yf.bookstore.repository.OrderRepository;
import com.yf.bookstore.repository.SkuRepository;
import com.yf.bookstore.service.OrderService;
import com.yf.bookstore.service.StockService;
import org.springframework.stereotype.Service;

/**
 * 订单服务实现类
 * 该类实现了OrderService接口，提供了具体的订单服务功能
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final SkuRepository skuRepository;
    private final StockService stockService;

    public OrderServiceImpl(OrderRepository orderRepository, SkuRepository skuRepository, StockService stockService) {
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
        this.stockService = stockService;
    }

    /**
     * 创建一个订单
     *
     * @param customerId 客户ID
     * @param sku        商品库存单位（Stock Keeping Unit）
     * @return 创建的订单对象
     * @throws InsufficientStockException 如果库存不足
     */
    public Order createOrder(String customerId, Sku sku) {
        // 初始化订单对象
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.CREATED);

        // 尝试减少库存
        try {
            stockService.decreaseStock(sku.getId(), 1); // 减少库存
        } catch (InsufficientStockException e) {
            // 如果库存不足，抛出异常
            throw new InsufficientStockException(sku.getId());
        }

        // 保存订单并返回
        return orderRepository.save(order);
    }


    /**
     * 取消指定的订单
     * 如果订单状态为"已创建"，则取消订单并更新库存，否则抛出异常
     *
     * @param orderId 订单ID
     * @return 取消后的订单对象
     * @throws OrderNotFoundException         如果找不到指定ID的订单
     * @throws OrderAlreadyProcessedException 如果订单已被处理（状态不为"已创建"）
     */
    public Order cancelOrder(Long orderId) {
        // 通过ID查找订单，如果不存在则抛出异常
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        // 检查订单状态是否为"已创建"
        if (order.getStatus() == OrderStatus.CREATED) {
            // 取消订单，设置状态为"已取消"
            order.setStatus(OrderStatus.CANCELLED);

            // 增加库存：将取消订单的商品数量重新加入库存
            stockService.increaseStock(order.getId(), 1);

            // 保存更新后的订单
            return orderRepository.save(order);
        } else {
            // 如果订单状态不是"已创建"，抛出异常
            throw new OrderAlreadyProcessedException(orderId);
        }
    }


    public OrderStatus getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return order.getStatus();
    }

}
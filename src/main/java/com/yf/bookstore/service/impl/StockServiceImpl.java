package com.yf.bookstore.service.impl;

import com.yf.bookstore.model.inventory.Stock;
import com.yf.bookstore.repository.StockRepository;
import com.yf.bookstore.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * StockServiceImpl类是StockService接口的一个实现类
 * 它提供了具体的方法来管理库存
 */
@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public StockServiceImpl(StockRepository stockRepository, RedisTemplate<String, Object> redisTemplate) {
        this.stockRepository = stockRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Stock increaseStock(Long skuId, int quantity) {
        Stock stock = stockRepository.findById(skuId)
                .orElseGet(() -> createStockIfNotExists(skuId));

        stock.setStock(stock.getStock() + quantity);

        // 使用乐观锁
        Stock updatedStock = stockRepository.saveAndFlush(stock);

        // Check if the optimistic lock failed
        if (updatedStock == null) {
            // Optimistic locking failed, rollback the transaction
            throw new OptimisticLockingFailureException("Optimistic locking failed for stock with SKU ID: " + skuId);
        }

        // Atomically increase the stock in Redis using Lua script
        String key = "stock:" + skuId;
        // Use Lua script to atomically decrease stock
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(
                "if redis.call('exists', KEYS[1]) == 1 then " +
                        "local stock = tonumber(redis.call('get', KEYS[1])) " +
                        "if stock >= ARGV[1] then " +
                        "    redis.call('set', KEYS[1], stock - ARGV[1]) " +
                        "    return stock - ARGV[1] " +
                        "else " +
                        "    return nil " +
                        "end " +
                        "else " +
                        "    return nil " +
                        "end"
        );
        luaScript.setResultType(Long.class);

// Try to update Redis outside of the transaction
        Long newStockInRedis = null;
        try {
            newStockInRedis = redisTemplate.execute(luaScript, Collections.singletonList(key), quantity);
        } catch (Exception e) {
            // Handle Redis update failure
            // Retry mechanism
            if (!retryUpdateRedis(luaScript, updatedStock, key, quantity)) {
                // Send mq todo
            }
        }

        return updatedStock;
    }

    @Override
    @Transactional
    public Stock decreaseStock(Long skuId, int quantity) {
        Stock stock = stockRepository.findById(skuId)
                .orElseGet(() -> createStockIfNotExists(skuId));

        stock.setStock(stock.getStock() - quantity);

        // Use optimistic lock
        Stock updatedStock = stockRepository.saveAndFlush(stock);

        // Check if the optimistic lock failed
        if (updatedStock == null) {
            // Optimistic locking failed, rollback the transaction
            throw new OptimisticLockingFailureException("Optimistic locking failed for stock with SKU ID: " + skuId);
        }

        // Atomically decrease the stock in Redis using Lua script
        String key = "stock:" + skuId;
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(
                "if redis.call('exists', KEYS[1]) == 1 then " +
                        "local stock = tonumber(redis.call('get', KEYS[1])) " +
                        "if stock >= ARGV[1] then " +
                        "    redis.call('set', KEYS[1], stock - ARGV[1]) " +
                        "    return stock - ARGV[1] " +
                        "else " +
                        "    return nil " +
                        "end " +
                        "else " +
                        "    return nil " +
                        "end"
        );
        luaScript.setResultType(Long.class);

        // Try to update Redis outside of the transaction
        Long newStockInRedis = null;
        try {
            newStockInRedis = redisTemplate.execute(luaScript, Collections.singletonList(key), quantity);
        } catch (Exception e) {
            // Handle Redis update failure
            // Log the error and continue
            logger.error("Failed to update Redis: {}", e.getMessage(), e);
            // Retry mechanism
            if (!retryUpdateRedis(luaScript, updatedStock, key, -quantity)) {
                // Send mq todo
            }
        }

        return updatedStock;
    }

    @Override
    @Transactional
    public Stock deleteStock(Long skuId) {
        Stock stock = stockRepository.findById(skuId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for SKU ID: " + skuId));

        stockRepository.delete(stock);

        // Remove from Redis
        String key = "stock:" + skuId;
        redisTemplate.delete(key);

        return stock;
    }

    @Override
    @Transactional
    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
    }


    @Override
    @Transactional
    public Stock updateStock(Stock stock) {
        Stock newStock = stockRepository.findById(stock.getSkuId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for SKU ID: " + stock.getSkuId()));


        // Update in Redis
        String key = "stock:" + stock.getSkuId();
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(
                "if redis.call('exists', KEYS[1]) == 1 then " +
                        "redis.call('set', KEYS[1], ARGV[1]) " +
                        "return true " +
                        "else " +
                        "return false " +
                        "end"
        );
        luaScript.setResultType(Long.class);

        // Update Redis
        Object updatedInRedis = redisTemplate.execute(luaScript, Collections.singletonList(key), newStock);

        if (!(Boolean) updatedInRedis) {
            logger.warn("Failed to update Redis for stock with SKU ID: {}", stock.getSkuId());
        }

        return stockRepository.save(stock);
    }

    private boolean retryUpdateRedis(DefaultRedisScript<Long> luaScript, Stock updatedStock, String key, int quantity) {
        int maxRetries = 3; // Maximum number of retries
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                Long newStockInRedis = redisTemplate.execute(new DefaultRedisScript<>(luaScript.getScriptAsString(), Long.class), Collections.singletonList(key), quantity);
                if (newStockInRedis != null) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("Retry {} failed to update Redis: {}", retryCount + 1, e.getMessage(), e);
            }
            retryCount++;
        }
        return false;
    }

    public Stock getStockBySkuId(Long skuId) {
        // Try to get from Redis cache
        Object stockObj = redisTemplate.opsForValue().get("stock:" + skuId);
        if (stockObj instanceof Stock) {
            return (Stock) stockObj;
        }

        // If not in cache, retrieve from database
        Stock stock = stockRepository.findById(skuId)
                .orElseGet(() -> createStockIfNotExists(skuId));

        // Save to Redis cache
        redisTemplate.opsForValue().set("stock:" + skuId, stock, 5, TimeUnit.MINUTES);

        return stock;
    }

    private Stock createStockIfNotExists(Long skuId) {
        Stock newStock = new Stock();
        newStock.setSkuId(skuId);
        newStock.setStock(0);
        return stockRepository.save(newStock);
    }
}
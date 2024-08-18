package com.yf.bookstore;

import com.yf.bookstore.model.inventory.Stock;
import com.yf.bookstore.repository.StockRepository;
import com.yf.bookstore.service.StockService;
import com.yf.bookstore.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private StockServiceImpl stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void increaseStock_StockExists_UpdatesStock() {
        Long skuId = 1L;
        int quantity = 5;

        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));
        when(stockRepository.saveAndFlush(any(Stock.class))).thenReturn(stock);

        Stock updatedStock = stockService.increaseStock(skuId, quantity);

        assertEquals(15, updatedStock.getStock());
        verify(stockRepository, times(1)).saveAndFlush(any(Stock.class));
    }

    @Test
    void increaseStock_StockDoesNotExist_CreatesNewStock() {
        Long skuId = 1L;
        int quantity = 5;

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenReturn(new Stock());

        Stock updatedStock = stockService.increaseStock(skuId, quantity);

        assertNotNull(updatedStock);
        assertEquals(skuId, updatedStock.getSkuId());
        assertEquals(quantity, updatedStock.getStock());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    void increaseStock_OptimisticLockingFailureException() {
        Long skuId = 1L;
        int quantity = 5;

        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));
        when(stockRepository.saveAndFlush(any(Stock.class))).thenReturn(null);

        assertThrows(OptimisticLockingFailureException.class, () -> stockService.increaseStock(skuId, quantity));
    }

    @Test
    void decreaseStock_StockExists_UpdatesStock() {
        Long skuId = 1L;
        int quantity = 5;

        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));
        when(stockRepository.saveAndFlush(any(Stock.class))).thenReturn(stock);

        Stock updatedStock = stockService.decreaseStock(skuId, quantity);

        assertEquals(5, updatedStock.getStock());
        verify(stockRepository, times(1)).saveAndFlush(any(Stock.class));
    }

    @Test
    void decreaseStock_OptimisticLockingFailureException() {
        Long skuId = 1L;
        int quantity = 5;

        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));
        when(stockRepository.saveAndFlush(any(Stock.class))).thenReturn(null);

        assertThrows(OptimisticLockingFailureException.class, () -> stockService.decreaseStock(skuId, quantity));
    }

    @Test
    void deleteStock_StockExists_DeletesStock() {
        Long skuId = 1L;

        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));
        doNothing().when(stockRepository).delete(any(Stock.class));

        Stock deletedStock = stockService.deleteStock(skuId);

        assertNotNull(deletedStock);
        assertEquals(skuId, deletedStock.getSkuId());
        verify(stockRepository, times(1)).delete(any(Stock.class));
    }

    @Test
    void createStock_NewStock_CreatesStock() {
        Stock stock = new Stock();
        stock.setSkuId(1L);
        stock.setStock(10);

        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        Stock createdStock = stockService.createStock(stock);

        assertNotNull(createdStock);
        assertEquals(stock.getSkuId(), createdStock.getSkuId());
        assertEquals(stock.getStock(), createdStock.getStock());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    void updateStock_StockExists_UpdatesStock() {
        Stock stock = new Stock();
        stock.setSkuId(1L);
        stock.setStock(10);

        when(stockRepository.findById(stock.getSkuId())).thenReturn(java.util.Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        Stock updatedStock = stockService.updateStock(stock);

        assertNotNull(updatedStock);
        assertEquals(stock.getSkuId(), updatedStock.getSkuId());
        assertEquals(stock.getStock(), updatedStock.getStock());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    void getStockBySkuId_StockInRedis_ReturnsFromRedis() {
        Long skuId = 1L;
        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(redisTemplate.opsForValue().get("stock:" + skuId)).thenReturn(stock);

        Stock retrievedStock = stockService.getStockBySkuId(skuId);

        assertEquals(stock, retrievedStock);
        verify(redisTemplate, times(1)).opsForValue().get("stock:" + skuId);
    }

    @Test
    void getStockBySkuId_StockNotInRedis_RetrieveFromDBAndCacheInRedis() {
        Long skuId = 1L;
        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setStock(10);

        when(redisTemplate.opsForValue().get("stock:" + skuId)).thenReturn(null);
        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.of(stock));

        Stock retrievedStock = stockService.getStockBySkuId(skuId);

        assertEquals(stock, retrievedStock);
        verify(redisTemplate, times(1)).opsForValue().set("stock:" + skuId, stock, 5, TimeUnit.MINUTES);
    }

    @Test
    void getStockBySkuId_StockDoesNotExistInDB_CreatesNewStockAndCachesInRedis() {
        Long skuId = 1L;

        when(redisTemplate.opsForValue().get("stock:" + skuId)).thenReturn(null);
        when(stockRepository.findById(skuId)).thenReturn(java.util.Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenReturn(new Stock());

        Stock retrievedStock = stockService.getStockBySkuId(skuId);

        assertNotNull(retrievedStock);
        assertEquals(skuId, retrievedStock.getSkuId());
        verify(redisTemplate, times(1)).opsForValue().set("stock:" + skuId, retrievedStock, 5, TimeUnit.MINUTES);
    }
}


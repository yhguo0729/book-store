package com.yf.bookstore;

import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.repository.SkuRepository;
import com.yf.bookstore.service.SkuService;
import com.yf.bookstore.service.impl.SkuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SkuServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SkuServiceImpl skuService;

    @BeforeEach
    public void setUp() {
        // 清空数据库
        entityManager.getEntityManager().getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    @Sql(scripts = {"classpath:data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testLoadDataBeforeTestMethod() {
        List<Sku> skus = skuRepository.findAll();
        assertNotNull(skus);
        assertEquals(3, skus.size());

        // 验证数据
        Sku productA = skus.get(0);
        assertEquals("Product A", productA.getTitle());
        assertEquals(10000L, productA.getPrice());

        Sku productB = skus.get(1);
        assertEquals("Product B", productB.getTitle());
        assertEquals(20000L, productB.getPrice());

        Sku productC = skus.get(2);
        assertEquals("Product C", productC.getTitle());
        assertEquals(30000L, productC.getPrice());
    }

    @Test
    void createSku_ShouldReturnCreatedSku() {
        Sku sku = new Sku();
        sku.setId(1L);

        when(skuRepository.save(any(Sku.class))).thenReturn(sku);

        Sku createdSku = skuService.createSku(sku);

        assertNotNull(createdSku);
        assertEquals(1L, createdSku.getId());
        verify(skuRepository, times(1)).save(any(Sku.class));
    }

    @Test
    void updateSku_ShouldReturnUpdatedSku() {
        Sku sku = new Sku();
        sku.setId(1L);

        when(skuRepository.save(any(Sku.class))).thenReturn(sku);

        Sku updatedSku = skuService.updateSku(sku);

        assertNotNull(updatedSku);
        assertEquals(1L, updatedSku.getId());
        verify(skuRepository, times(1)).save(any(Sku.class));
    }

    @Test
    void deleteSku_ShouldDeleteSkuById() {
        Long id = 1L;

        doNothing().when(skuRepository).deleteById(id);

        skuService.deleteSku(id);

        verify(skuRepository, times(1)).deleteById(id);
    }

    @Test
    void getSkuById_SkuExists_ShouldReturnSku() {
        Long id = 1L;
        Sku sku = new Sku();
        sku.setId(id);

        when(skuRepository.findById(id)).thenReturn(Optional.of(sku));

        Sku foundSku = skuService.getSkuById(id);

        assertNotNull(foundSku);
        assertEquals(id, foundSku.getId());
        verify(skuRepository, times(1)).findById(id);
    }

    @Test
    void getSkuById_SkuNotExists_ShouldThrowException() {
        Long id = 1L;

        when(skuRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> skuService.getSkuById(id));
        verify(skuRepository, times(1)).findById(id);
    }
}

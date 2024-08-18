package com.yf.bookstore.service.impl;

import com.yf.bookstore.model.commodity.Sku;
import com.yf.bookstore.repository.SkuRepository;
import com.yf.bookstore.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Sku服务的实现类
 */
@Service
public class SkuServiceImpl implements SkuService {

    private final SkuRepository skuRepository;

    /**
     * 构造函数，注入SkuRepository
     *
     * @param skuRepository Sku的数据访问层接口实现
     */
    @Autowired
    public SkuServiceImpl(SkuRepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    /**
     * 创建一个新的Sku
     *
     * @param sku 待创建的Sku对象
     * @return 创建后的Sku对象
     */
    @Override
    public Sku createSku(Sku sku) {
        return skuRepository.save(sku);
    }

    /**
     * 更新一个已存在的Sku
     *
     * @param sku 待更新的Sku对象
     * @return 更新后的Sku对象
     */
    @Override
    public Sku updateSku(Sku sku) {
        return skuRepository.save(sku);
    }

    /**
     * 删除指定ID的Sku
     *
     * @param id Sku的ID
     */
    @Override
    public void deleteSku(Long id) {
        skuRepository.deleteById(id);
    }

    /**
     * 根据ID获取Sku
     *
     * @param id Sku的ID
     * @return 对应的Sku对象，如果找不到则抛出异常
     * @throws RuntimeException 如果Sku不存在
     */
    @Override
    public Sku getSkuById(Long id) {
        return skuRepository.findById(id).orElseThrow(() -> new RuntimeException("Sku not found"));
    }
}
package com.ms.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.common.enums.CategoryLevelEnum;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.product.entity.Product;
import com.ms.product.entity.ProductCategory;
import com.ms.product.entity.StockDTO;
import com.ms.product.mapper.CategoryMapper;
import com.ms.product.mapper.ProductMapper;
import com.ms.product.service.ProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public String saveProduct(Product product) {
        ProductCategory productCategory = categoryMapper.selectById(product.getGoodsCategoryId());
        if (null == productCategory || productCategory.getCategoryLevel().intValue() != CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (getBaseMapper().selectCount(new QueryWrapper<Product>().eq("goods_name", product.getGoodsName()).eq("goods_category_id", product.getGoodsCategoryId())) == 0) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (save(product)) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
    }

    @Override
    public String updateProduct(Product product) {
        ProductCategory productCategory = categoryMapper.selectById(product.getGoodsCategoryId());
        if (null == productCategory || productCategory.getCategoryLevel().intValue() != CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        Product tem = getById(product.getGoodsId());
        if (null == tem) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        Product product1 = getBaseMapper().selectOne(new QueryWrapper<Product>().eq("goods_name", product.getGoodsName()).eq("goods_category_id", product.getGoodsCategoryId()));

        if (null != product1 && !product1.getGoodsId().equals(product.getGoodsId())) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        product.setUpdateTime(new Date());
        boolean b = updateById(product);
        if (b) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
    }

    @Override
    public int updateStatus(Long[] ids, int status) {
        return getBaseMapper().batchUpdateStatus(ids, status);
    }

    @Override
    public int updateStock(List<StockDTO> stockDTOS) {
        int i = getBaseMapper().updateStock(stockDTOS);
        return i;
    }
}

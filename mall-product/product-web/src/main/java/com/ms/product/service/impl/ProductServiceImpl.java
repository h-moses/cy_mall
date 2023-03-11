package com.ms.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.common.enums.CategoryLevelEnum;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.product.entity.Product;
import com.ms.product.entity.ProductCategory;
import com.ms.product.entity.StockNumDTO;
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
        if (null == productCategory || productCategory.getCategoryLevel().intValue() == CategoryLevelEnum.LEVEL_ONE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        Product tem = getById(product.getGoodsId());
//        要更新的商品不存在
        if (null == tem) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
//        QueryWrapper<Product> wrapper = new QueryWrapper<Product>().eq("goods_name", product.getGoodsName()).eq("goods_category_id", product.getGoodsCategoryId());
//        List<Product> list = list(wrapper);
////        存在同样的商品，但是ID不一致
//        if (list != null && list.size() > 1) {
//            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
//        }
//        更新信息
        product.setUpdateTime(new Date());
        boolean b = updateById(product);
        if (b) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
    }

    @Override
    public boolean updateStatus(Long[] ids, int status) {
        UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("goods_sell_status", status);
        updateWrapper.in("goods_id", ids);
        return update(null, updateWrapper);
    }

    @Override
    public int updateStock(List<StockNumDTO> stocks) {
//        用原有库存减去输入的数量
        int i = getBaseMapper().updateStock(stocks);
        return i;
    }
}

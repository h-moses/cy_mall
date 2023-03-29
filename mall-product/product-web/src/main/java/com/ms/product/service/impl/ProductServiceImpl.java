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

    /**
     * 添加商品服务
     */
    @Override
    public String saveProduct(Product product) {
//        查询该商品所属的类别
        ProductCategory productCategory = categoryMapper.selectById(product.getGoodsCategoryId());
//        不存在,或不是三级类别,报错
        if (null == productCategory || productCategory.getCategoryLevel().intValue() != CategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
//        查询是否存在同名商品
        if (getBaseMapper().selectCount(new QueryWrapper<Product>().eq("goods_name", product.getGoodsName()).eq("goods_category_id", product.getGoodsCategoryId())) == 0) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
//        添加商品到数据库
        if (save(product)) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
    }

    /**
     * 更新商品信息
     */
    @Override
    public String updateProduct(Product product) {
//        查询该商品所属的类别
        ProductCategory productCategory = categoryMapper.selectById(product.getGoodsCategoryId());
//        不存在,或不是三级类别,报错
        if (null == productCategory || productCategory.getCategoryLevel().intValue() == CategoryLevelEnum.LEVEL_ONE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        Product tem = getById(product.getGoodsId());
//        要更新的商品不存在
        if (null == tem) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
//        更新信息
        product.setUpdateTime(new Date());
        boolean b = updateById(product);
        if (b) {
            return ServiceResultEnum.SUCCESS.getResult();
        } else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
    }

    /**
     * 批量更新商品的售卖状态
     */
    @Override
    public boolean updateStatus(Long[] ids, int status) {
        UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("goods_sell_status", status);
        updateWrapper.in("goods_id", ids);
        return update(null, updateWrapper);
    }

    /**
     * 更新商品库存
     * 若是取消订单,则增加商品库存
     * 否则,减少商品库存
     */
    @Override
    public int updateStock(List<StockNumDTO> stocks, boolean isCancel) {
        int res = 0;
        if (isCancel) {
            res = getBaseMapper().increaseStock(stocks);
        } else {
            //        用原有库存减去输入的数量
            res = getBaseMapper().updateStock(stocks);
        }
        return res;
    }
}

package com.ms.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<ProductCategory> {
}

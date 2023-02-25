package com.ms.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.cart.entity.ShoppingCartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCartItem> {
}

package com.ms.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.cart.controller.param.SaveCartItemParam;
import com.ms.cart.controller.param.UpdateCartItemParam;
import com.ms.cart.entity.ShoppingCartItem;
import com.ms.cart.mapper.ShoppingCartMapper;
import com.ms.cart.service.ShoppingCartService;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.utils.BeanUtil;
import com.ms.product.ProductServiceFeign;
import com.ms.product.dto.ProductDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCartItem> implements ShoppingCartService {

    @Resource
    private ProductServiceFeign productServiceFeign;

    @Override
    public String saveItem(SaveCartItemParam saveCartItemParam, Long userId) {
        ShoppingCartItem cartItem = getOne(new QueryWrapper<ShoppingCartItem>().eq("user_id", userId).eq("goods_id", saveCartItemParam.getGoodsId()).eq("is_deleted", 0));
//        购物车已经存在该商品
        if (null != cartItem) {
            MallException.fail(ServiceResultEnum.SHOPPING_CART_ITEM_EXIST_ERROR.getResult());
        }
        CommonResult<ProductDTO> result = productServiceFeign.getGoodsDetail(saveCartItemParam.getGoodsId());
//        该商品不存在
        if (null == result || result.getCode() != 200) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
//        添加到购物车的商品数量小于1,返回
        if (saveCartItemParam.getGoodsCount() < 1) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_NUMBER_ERROR.getResult();
        }
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        BeanUtil.copyProperties(saveCartItemParam, shoppingCartItem);
        shoppingCartItem.setUserId(userId);
        if (save(shoppingCartItem)) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateItem(UpdateCartItemParam updateCartItemParam, Long userId) {
//        查询购物车中的商品
        ShoppingCartItem cartItem = getById(updateCartItemParam.getCartItemId());
        if (null == cartItem) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
//        当前登录id和商品的用户id不同
        if (!cartItem.getUserId().equals(userId)) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
//        数量相同
        if (cartItem.getGoodsCount().equals(updateCartItemParam.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        cartItem.setGoodsCount(updateCartItemParam.getGoodsCount());
        cartItem.setUpdateTime(new Date());
        if (updateById(cartItem)) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }
}

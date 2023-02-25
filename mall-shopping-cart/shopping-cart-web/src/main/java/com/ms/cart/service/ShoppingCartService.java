package com.ms.cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.cart.controller.param.SaveCartItemParam;
import com.ms.cart.controller.param.UpdateCartItemParam;
import com.ms.cart.entity.ShoppingCartItem;
import com.ms.common.pojo.UserToken;

public interface ShoppingCartService extends IService<ShoppingCartItem> {
    String saveItem(SaveCartItemParam saveCartItemParam, Long userId);

    String updateItem(UpdateCartItemParam updateCartItemParam, UserToken token);
}

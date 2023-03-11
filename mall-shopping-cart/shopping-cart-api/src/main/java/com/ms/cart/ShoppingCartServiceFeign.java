package com.ms.cart;

import com.ms.cart.dto.ShoppingCartItemDTO;
import com.ms.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "mall-shopping-cart-service", path = "/shop-cart")
public interface ShoppingCartServiceFeign {

    @GetMapping(value = "/listByCartItemIds")
    CommonResult<List<ShoppingCartItemDTO>> listByCartItemIds(@RequestParam(value = "cartItemIds") List<Long> cartItemIds);

    @DeleteMapping(value = "/deleteByCartItemIds")
    CommonResult<Boolean> deleteByCartItemIds(@RequestParam(value = "cartItemIds") List<Long> cartItemIds);
}

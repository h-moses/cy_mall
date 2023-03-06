package com.ms.cart.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ms.cart.controller.param.SaveCartItemParam;
import com.ms.cart.controller.param.UpdateCartItemParam;
import com.ms.cart.entity.ShoppingCartItem;
import com.ms.cart.service.ShoppingCartService;
import com.ms.common.annotation.TokenToMallUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.pojo.UserToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Service;
import org.apache.catalina.User;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @GetMapping("/shop-cart/page")
    @ApiOperation(value = "购物车列表(每页默认5条)", notes = "传参为页码")
    public CommonResult cartItemPageList(@RequestParam("pageNum") Integer pageNum, @TokenToMallUser UserToken userToken) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        Page<ShoppingCartItem> shoppingCartItemPage = new Page<>(pageNum, 5);
        Page<ShoppingCartItem> itemPage = shoppingCartService.page(shoppingCartItemPage, new QueryWrapper<ShoppingCartItem>().eq("user_id", userToken.getUserId()).eq("is_deleted", 0));
        return CommonResult.success(itemPage);
    }

    @PostMapping("/shop-cart")
    @ApiOperation(value = "添加商品到购物车接口", notes = "传参为商品id、数量")
    public CommonResult addItem(@RequestBody SaveCartItemParam saveCartItemParam, @TokenToMallUser UserToken userToken){
        String result = shoppingCartService.saveItem(saveCartItemParam, userToken.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return CommonResult.success();
        }
        return CommonResult.failure(result);
    }

    @PutMapping("/shop-cart")
    @ApiOperation(value = "修改购物项数据", notes = "传参为购物项id、数量")
    public CommonResult updateItem(@RequestBody UpdateCartItemParam updateCartItemParam, @TokenToMallUser UserToken token) {
        String res = shoppingCartService.updateItem(updateCartItemParam, token);
        if (ServiceResultEnum.SUCCESS.getResult().equals(res)) {
            return CommonResult.success();
        }
        return CommonResult.failure(res);
    }

    @DeleteMapping("/shop-cart/{cartItemId}")
    @ApiOperation(value = "删除购物项", notes = "传参为购物项id")
    public CommonResult deleteItem(@PathVariable("cartItemId") Long cartItemId, @TokenToMallUser UserToken userToken) {
        ShoppingCartItem cartItem = shoppingCartService.getById(cartItemId);
        if (null == cartItem) {
            return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!cartItem.getUserId().equals(userToken.getUserId())) {
            return CommonResult.failure(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        boolean b = shoppingCartService.removeById(cartItemId);
        if (b) {
            return CommonResult.success();
        }
        return CommonResult.failure(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @DeleteMapping("/shop-cart/deleteByCartItemIds")
    @ApiOperation(value = "批量删除购物项", notes = "")
    public CommonResult deleteItemByBatch(@RequestParam List<Long> ids, @TokenToMallUser UserToken userToken) {
        if (CollectionUtils.isEmpty(ids)) {
            return CommonResult.failure(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        boolean b = shoppingCartService.removeBatchByIds(ids);
        if (b) {
            return CommonResult.success();
        }
        return CommonResult.failure();
    }

    @GetMapping("/shop-cart/settle")
    @ApiOperation(value = "根据购物项id数组查询购物项明细", notes = "确认订单页面使用")
    public CommonResult settle(Long[] ids, @TokenToMallUser UserToken userToken) {
        if (ids.length < 1) {
            MallException.fail("参数异常");
        }
        List<ShoppingCartItem> list = shoppingCartService.list(new QueryWrapper<ShoppingCartItem>().eq("user_id", userToken.getUserId()).in("cart_item_id", ids));
        if (CollectionUtils.isEmpty(list)) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (list.size() != ids.length) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return CommonResult.success(list);
    }
}

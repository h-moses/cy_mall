package com.ms.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ms.common.annotation.TokenToMallUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.pojo.UserToken;
import com.ms.order.controller.param.SaveOrderParam;
import com.ms.order.controller.vo.MallOrderListVO;
import com.ms.order.entity.MallOrder;
import com.ms.order.entity.MallUserAddress;
import com.ms.order.service.MallOrderService;
import com.ms.order.service.MallUserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;

@RestController
@Slf4j
@Api(tags = "商城订单管理接口")
@RequestMapping("/order/mall")
public class MallOrderController {

    @Resource
    private MallOrderService mallOrderService;

    @Resource
    private MallUserAddressService userAddressService;

    @PostMapping("/saveOrder")
    @ApiOperation(value = "生成订单接口", notes = "传参为地址id和待结算的购物项id数组")
    public CommonResult prodOrder(@ApiParam(value = "订单参数") @RequestBody SaveOrderParam saveOrderParam, @TokenToMallUser UserToken userToken) {
        if (null == saveOrderParam || null == saveOrderParam.getAddressId() || null == saveOrderParam.getCartItemIds()) {
            return CommonResult.failure(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        if (saveOrderParam.getCartItemIds().length < 1) {
            return CommonResult.failure(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        MallUserAddress address = userAddressService.getById(saveOrderParam.getAddressId());
        if (null == address) {
            return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!userToken.getUserId().equals(address.getUserId())) {
            return CommonResult.failure(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        String order = mallOrderService.saveOrder(userToken.getUserId(), address, Arrays.asList(saveOrderParam.getCartItemIds()));
        return CommonResult.success(order);
    }

    @GetMapping("/order/{orderNo}")
    @ApiOperation(value = "订单详情接口", notes = "传参为订单号")
    public CommonResult orderDetail(@ApiParam(value = "订单号") @PathVariable("orderNo") String orderNo, @TokenToMallUser UserToken userToken) {
        return CommonResult.success(mallOrderService.getOrderByNo(orderNo, userToken.getUserId()));
    }

    @GetMapping("/order")
    @ApiOperation(value = "订单列表接口", notes = "传参为页码")
    public CommonResult orderList(@ApiParam(value = "页码") @RequestParam(required = false) Integer pageNum,
                                  @ApiParam(value = "订单状态:0.待支付 1.待确认 2.待发货 3:已发货 4.交易成功") @RequestParam(required = false) Integer status,
                                  @TokenToMallUser UserToken userToken) {
        if (null == pageNum || pageNum < 1) {
            pageNum = 1;
        }
        Page<MallOrder> page = new Page<>(pageNum, 5);
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userToken.getUserId().toString());
        map.put("orderStatus", status.toString());
        Page<MallOrderListVO> orderList = mallOrderService.getOrderList(page, map);
        return CommonResult.success(orderList);
    }

    @PutMapping("/order/{orderNo}/cancel")
    @ApiOperation(value = "订单取消接口", notes = "传参为订单号")
    public CommonResult cancelOrder(@ApiParam(value = "订单号") @PathVariable("orderNo") String orderNo, @TokenToMallUser UserToken userToken) {
        String order = mallOrderService.cancelOrder(orderNo, userToken.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(order)) {
            return CommonResult.success(order);
        } else {
            return CommonResult.failure(order);
        }
    }

    @PutMapping("/order/{orderNo}/finish")
    @ApiOperation(value = "确认收货接口", notes = "传参为订单号")
    public CommonResult finishOrder(@ApiParam("订单号") @PathVariable("orderNo") String orderNo, @TokenToMallUser UserToken userToken) {
        String s = mallOrderService.finishOrder(orderNo, userToken.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(s)) {
            return CommonResult.success(s);
        } else {
            return CommonResult.failure(s);
        }
    }

    @GetMapping("/paySuccess")
    @ApiOperation(value = "模拟支付成功回调的接口", notes = "传参为订单号和支付方式")
    public CommonResult paySuccess(@ApiParam(value = "订单号") @RequestParam("orderNo") String orderNo, @ApiParam(value = "支付方式") @RequestParam("payType") int payType) {
        String payResult = mallOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return CommonResult.success(payResult);
        } else {
            return CommonResult.failure(payResult);
        }
    }
}

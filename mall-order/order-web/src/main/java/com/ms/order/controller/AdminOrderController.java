package com.ms.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ms.common.annotation.TokenToAdminUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.order.controller.param.BatchIdParam;
import com.ms.order.controller.vo.MallOrderDetailVO;
import com.ms.order.controller.vo.MallOrderListVO;
import com.ms.order.entity.LoginAdmin;
import com.ms.order.entity.MallOrder;
import com.ms.order.service.MallOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@RestController
@Api(tags = "后台管理系统订单模块接口")
@RequestMapping("/order/admin")
public class AdminOrderController {
    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    @Resource
    private MallOrderService mallOrderService;

    @GetMapping(value = "/list")
    @ApiOperation(value = "订单列表", notes = "可根据订单号和订单状态筛选")
    public CommonResult list(@RequestParam(required = false) @ApiParam(value = "页码") Integer pageNum,
                             @RequestParam(required = false) @ApiParam(value = "每页条数") Integer pageSize,
                             @RequestParam(required = false) @ApiParam(value = "订单号") String orderNo,
                             @RequestParam(required = false) @ApiParam(value = "订单状态") Integer orderStatus,
                             @TokenToAdminUser LoginAdmin adminUser) {
        if (pageNum ==null || pageNum < 1 || pageSize == null || pageSize < 10) {
            pageNum = 1;
            pageSize = 10;
        }
        Page<MallOrder> mallOrderPage = new Page<>(pageNum, pageSize);
        HashMap<String, String> map = new HashMap<>(8);
        map.put("orderNo", orderNo);
        map.put("orderStatus", orderStatus.toString());
        Page<MallOrderListVO> orderList = mallOrderService.getOrderList(mallOrderPage, map);
        return CommonResult.success(orderList);
    }

    @GetMapping("/detail/{orderId}")
    @ApiOperation(value = "订单详情接口", notes = "传参为订单号")
    public CommonResult<MallOrderDetailVO> orderDetailPage(@ApiParam(value = "订单号") @PathVariable("orderId") Long orderId, @TokenToAdminUser LoginAdmin adminUser) {
        return CommonResult.success(mallOrderService.getOrderById(orderId));
    }

    @PutMapping("/checkDone")
    @ApiOperation(value = "修改订单状态为配货成功", notes = "批量修改")
    public CommonResult checkDone(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser LoginAdmin adminUser) {
        if (null == batchIdParam || batchIdParam.getIds().length < 1) {
            return CommonResult.failure("参数异常");
        }
        String result = mallOrderService.checkDone(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failure(result);
        }
    }

    @PutMapping(value = "/checkOut")
    @ApiOperation(value = "修改订单状态为已出库", notes = "批量修改")
    public CommonResult checkout(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser LoginAdmin adminUser) {
        if (null == batchIdParam || batchIdParam.getIds().length < 1) {
            return CommonResult.failure("参数异常");
        }
        String result = mallOrderService.checkout(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return CommonResult.success();
        } else {
            return CommonResult.failure(result);
        }
    }

    @PutMapping(value = "/close")
    @ApiOperation(value = "修改订单状态为商家关闭", notes = "批量修改")
    public CommonResult closeOrder(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser LoginAdmin adminUser) {
        if (batchIdParam == null || batchIdParam.getIds().length < 1) {
            return CommonResult.failure("参数异常");
        }
        String rs = mallOrderService.closeOrder(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(rs)) {
            return CommonResult.success();
        } else {
            return CommonResult.failure(rs);
        }
    }
}

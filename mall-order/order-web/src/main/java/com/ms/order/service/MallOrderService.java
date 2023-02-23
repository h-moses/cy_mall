package com.ms.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.order.controller.vo.MallOrderDetailVO;
import com.ms.order.controller.vo.MallOrderListVO;
import com.ms.order.entity.MallOrder;

import java.util.Map;

public interface MallOrderService extends IService<MallOrder> {

    public MallOrderDetailVO getOrderByNo(String orderNo, Long userId);

    public MallOrderDetailVO getOrderById(Long orderId);

    public Page<MallOrderListVO> getOrderList(Page page, Map map);

    String cancelOrder(String orderNo, Long userId);

    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    String checkDone(Long[] ids);

    String checkout(Long[] ids);

    String closeOrder(Long[] ids);
}

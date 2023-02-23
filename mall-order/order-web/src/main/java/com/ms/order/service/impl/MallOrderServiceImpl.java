package com.ms.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.common.enums.MallOrderStatusEnum;
import com.ms.common.enums.PayStatusEnum;
import com.ms.common.enums.PayTypeEnum;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.utils.BeanUtil;
import com.ms.order.controller.vo.MallOrderDetailVO;
import com.ms.order.controller.vo.MallOrderItemVO;
import com.ms.order.controller.vo.MallOrderListVO;
import com.ms.order.entity.MallOrder;
import com.ms.order.entity.MallOrderItem;
import com.ms.order.mapper.MallOrderItemMapper;
import com.ms.order.mapper.MallOrderMapper;
import com.ms.order.service.MallOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    @Resource
    private MallOrderItemMapper orderItemMapper;

    @Override
    public MallOrderDetailVO getOrderById(Long orderId) {
        MallOrder order = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderId));
        if (null == order) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        List<MallOrderItem> orderItemList = orderItemMapper.selectList(new QueryWrapper<MallOrderItem>().eq("order_id", order.getOrderId()));
        if (CollectionUtils.isEmpty(orderItemList)) {
            MallException.fail(ServiceResultEnum.ORDER_ITEM_NULL_ERROR.getResult());
        }
        List<MallOrderItemVO> mallOrderItemVOS = BeanUtil.copyList(orderItemList, MallOrderItemVO.class);
        MallOrderDetailVO mallOrderDetailVO = new MallOrderDetailVO();
        BeanUtil.copyProperties(order, mallOrderDetailVO);
        mallOrderDetailVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(mallOrderDetailVO.getOrderStatus()).getName());
        mallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(mallOrderDetailVO.getPayType()).getName());
        mallOrderDetailVO.setNewBeeMallOrderItemVOS(mallOrderItemVOS);
        return mallOrderDetailVO;
    }

    @Override
    public Page<MallOrderListVO> getOrderList(Page page, Map map) {
        QueryWrapper<MallOrder> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.hasText((String) map.get("orderNo")), "order_no", map.get("orderNo"));
        wrapper.eq(StringUtils.hasText((String) map.get("userId")), "user_id", map.get("userId"));
        wrapper.eq(StringUtils.hasText((String) map.get("payType")), "pay_type", map.get("payType"));
        wrapper.eq(StringUtils.hasText((String) map.get("orderStatus")), "order_status", map.get("orderStatus"));
        wrapper.eq(StringUtils.hasText((String) map.get("isDeleted")), "is_deleted", map.get("isDeleted"));
        wrapper.orderByDesc("create_time");
        Page<MallOrder> result = page(page, wrapper);
        List<MallOrderListVO> orderListVOS = new ArrayList<>();
        if (result.getTotal() > 0) {
            orderListVOS = BeanUtil.copyList(result.getRecords(), MallOrderListVO.class);
            for (MallOrderListVO orderListVO: orderListVOS) {
                orderListVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(orderListVO.getOrderStatus()).getName());
            }
            List<Long> list = result.getRecords().stream().map(MallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(list)) {
                List<MallOrderItem> mallOrderItems = orderItemMapper.selectBatchIds(list);
                Map<Long, List<MallOrderItem>> listMap = mallOrderItems.stream().collect(Collectors.groupingBy(MallOrderItem::getOrderId));
                for (MallOrderListVO orderListVO: orderListVOS) {
                    if (listMap.containsKey(orderListVO.getOrderId())) {
                        List<MallOrderItem> mallOrderItems1 = listMap.get(orderListVO.getOrderId());
                        List<MallOrderItemVO> mallOrderItemVOS = BeanUtil.copyList(mallOrderItems1, MallOrderItemVO.class);
                        orderListVO.setNewBeeMallOrderItemVOS(mallOrderItemVOS);
                    }
                }
            }
        }
        Page<MallOrderListVO> listVOPage = new Page<>();
        BeanUtil.copyProperties(result, listVOPage, "records");
        listVOPage.setRecords(orderListVOS);
        return listVOPage;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        MallOrder mallOrder = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo));
        if (null != mallOrder) {
            if (!userId.equals(mallOrder.getUserId())) {
                MallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            if (mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            UpdateWrapper<MallOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("order_status", MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
            updateWrapper.set("update_time", LocalDateTime.now());
            updateWrapper.in("order_id", Collections.singletonList(mallOrder.getOrderId()));
            boolean r = update(null, updateWrapper);
            if (r) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        MallOrder mallOrder = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo));
        if (null != mallOrder) {
            if (!userId.equals(mallOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            if (mallOrder.getOrderStatus().intValue() != MallOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            mallOrder.setOrderStatus((byte) MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            mallOrder.setUpdateTime(new Date());
            if (updateById(mallOrder)) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        MallOrder mallOrder = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo));
        if (null != mallOrder) {
            if (mallOrder.getOrderStatus().intValue() != MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            mallOrder.setOrderStatus((byte) MallOrderStatusEnum.ORDER_PAID.getOrderStatus());
            mallOrder.setPayType((byte) payType);
            mallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            mallOrder.setPayTime(new Date());
            mallOrder.setUpdateTime(new Date());
            if (updateById(mallOrder)) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        List<MallOrder> mallOrders = listByIds(Collections.singletonList(ids));
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(mallOrders)) {
            for (MallOrder order: mallOrders) {
                if (order.getIsDeleted() == 1) {
                    builder.append(order.getOrderNo() + " ");
                    continue;
                }
                if (order.getOrderStatus() != 1) {
                    builder.append(order.getOrderNo() + " ");
                }
            }
            String s = builder.toString();
            if (!StringUtils.hasText(s)) {
                UpdateWrapper<MallOrder> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("order_status", 2);
                updateWrapper.set("update_time", LocalDateTime.now());
                updateWrapper.in("order_id", ids);
                boolean update = update(null, updateWrapper);
                if (update) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                if (s.length() > 0 && s.length() < 100) {
                    return s + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkout(Long[] ids) {
        List<MallOrder> mallOrders = listByIds(Collections.singletonList(ids));
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(mallOrders)) {
            for (MallOrder mallOrder: mallOrders) {
                if (mallOrder.getIsDeleted() == 1) {
                    builder.append(mallOrder.getOrderNo() + " ");
                    continue;
                }
                if (mallOrder.getOrderStatus() != 1 && mallOrder.getOrderStatus() != 2) {
                    builder.append(mallOrder.getOrderNo() + " ");
                }
            }
            String errorNos = builder.toString();
            if (!StringUtils.hasText(errorNos)) {
                UpdateWrapper<MallOrder> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("order_status", 3);
                updateWrapper.set("update_time", LocalDateTime.now());
                updateWrapper.in("order_id", ids);
                boolean update = update(null, updateWrapper);
                if (update) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                if (errorNos.length() > 0 && errorNos.length() < 100) {
                    return errorNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        List<MallOrder> mallOrders = listByIds(Collections.singletonList(ids));
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(mallOrders)) {
            for (MallOrder order:
                 mallOrders) {
//                isDeleted == 1，一定为已关闭订单
                if (order.getIsDeleted() == 1) {
                    builder.append(order.getOrderNo() + " ");
                    continue;
                }
//                已关闭或者已完成无法关闭的订单
                if (order.getOrderStatus() == 4 || order.getOrderStatus() < 0) {
                    builder.append(order.getOrderNo() + " ");
                }
            }
            String result = builder.toString();
            if (!StringUtils.hasText(result)) {
//                订单状态正常，可以执行关闭操作，修改订单状态和时间
                UpdateWrapper<MallOrder> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("order_status", MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus());
                updateWrapper.set("update_time", LocalDateTime.now());
                updateWrapper.in("order_id", ids);
                boolean update = update(null, updateWrapper);
                if (update) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
//                订单此时不可执行关闭操作
                if (result.length() > 0 && result.length() < 100) {
                    return result + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public MallOrderDetailVO getOrderByNo(String orderNo, Long userId) {
        MallOrder order = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo).eq("is_deleted", 0));
        if (null == order) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!userId.equals(order.getUserId())) {
            MallException.fail(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        List<MallOrderItem> orderItemList = orderItemMapper.selectList(new QueryWrapper<MallOrderItem>().eq("order_id", order.getOrderId()));
        if (CollectionUtils.isEmpty(orderItemList)) {
            MallException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<MallOrderItemVO> mallOrderItemVOS = BeanUtil.copyList(orderItemList, MallOrderItemVO.class);
        MallOrderDetailVO mallOrderDetailVO = new MallOrderDetailVO();
        BeanUtil.copyProperties(order, mallOrderDetailVO);
        mallOrderDetailVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(mallOrderDetailVO.getOrderStatus()).getName());
        mallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(mallOrderDetailVO.getPayType()).getName());
        mallOrderDetailVO.setNewBeeMallOrderItemVOS(mallOrderItemVOS);
        return mallOrderDetailVO;
    }
}

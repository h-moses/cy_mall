package com.ms.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.cart.ShoppingCartServiceFeign;
import com.ms.cart.dto.ShoppingCartItemDTO;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.MallOrderStatusEnum;
import com.ms.common.enums.PayStatusEnum;
import com.ms.common.enums.PayTypeEnum;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.utils.BeanUtil;
import com.ms.common.utils.NumberUtil;
import com.ms.order.controller.vo.MallOrderDetailVO;
import com.ms.order.controller.vo.MallOrderItemVO;
import com.ms.order.controller.vo.MallOrderListVO;
import com.ms.order.entity.MallOrder;
import com.ms.order.entity.MallOrderAddress;
import com.ms.order.entity.MallOrderItem;
import com.ms.order.entity.MallUserAddress;
import com.ms.order.mapper.MallOrderAddressMapper;
import com.ms.order.mapper.MallOrderItemMapper;
import com.ms.order.mapper.MallOrderMapper;
import com.ms.order.service.MallOrderItemService;
import com.ms.order.service.MallOrderService;
import com.ms.product.ProductServiceFeign;
import com.ms.product.dto.ProductDTO;
import com.ms.product.dto.StockNumDTO;
import com.ms.product.dto.UpdateStockNumDTO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    @Resource
    private MallOrderItemService orderItemService;

    @Resource
    private MallOrderMapper orderMapper;

    @Resource
    private MallOrderAddressMapper orderAddressMapper;

    @Resource
    private ProductServiceFeign productServiceFeign;

    @Resource
    private ShoppingCartServiceFeign cartServiceFeign;

    @Override
    public MallOrderDetailVO getOrderByNo(String orderNo, Long userId) {
        MallOrder order = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo));
        if (null == order) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!userId.equals(order.getUserId())) {
            MallException.fail(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        List<MallOrderItem> orderItemList = orderItemService.list(new QueryWrapper<MallOrderItem>().eq("order_id", order.getOrderId()));
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

    @Override
    public MallOrderDetailVO getOrderById(Long orderId) {
        MallOrder order = getOne(new QueryWrapper<MallOrder>().eq("order_id", orderId));
        if (null == order) {
            MallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        List<MallOrderItem> orderItemList = orderItemService.list(new QueryWrapper<MallOrderItem>().eq("order_id", order.getOrderId()));
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
        wrapper.eq(StringUtils.hasText((String) map.get("orderNo")), "order_no", map.get("orderNo"))
                .eq(StringUtils.hasText((String) map.get("userId")), "user_id", map.get("userId"))
                .eq(StringUtils.hasText((String) map.get("payType")), "pay_type", map.get("payType"))
                .eq(StringUtils.hasText((String) map.get("orderStatus")), "order_status", map.get("orderStatus"))
                .orderByDesc("create_time");
        Page<MallOrder> result = page(page, wrapper);
        List<MallOrderListVO> orderListVOS = new ArrayList<>();
        if (result.getTotal() > 0) {
//            数据转换
            orderListVOS = BeanUtil.copyList(result.getRecords(), MallOrderListVO.class);
//            设置订单状态，详细描述
            for (MallOrderListVO orderListVO: orderListVOS) {
                orderListVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(orderListVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = result.getRecords().stream().map(MallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
//                根据订单ID获取对应的商品项
                List<MallOrderItem> mallOrderItems = orderItemService.listByIds(orderIds);
//                根据订单ID进行分组
                Map<Long, List<MallOrderItem>> listMap = mallOrderItems.stream().collect(Collectors.groupingBy(MallOrderItem::getOrderId));
                for (MallOrderListVO orderListVO: orderListVOS) {
//                    将每个订单包含的商品封装到订单中
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
//            验证是否是当前userId下的订单
            if (!userId.equals(mallOrder.getUserId())) {
                MallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
//            若订单状态不符合取消条件，返回
            if (mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            UpdateWrapper<MallOrder> updateWrapper = new UpdateWrapper<>();
//            设置为用户关闭状态
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
//            验证是否是当前userId下的订单
            if (!userId.equals(mallOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
//            非出库状态下不能进行修改操作
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
//        查询订单
        MallOrder mallOrder = getOne(new QueryWrapper<MallOrder>().eq("order_no", orderNo));
        if (null != mallOrder) {
//            订单状态不是待支付的状态
            if (mallOrder.getOrderStatus().intValue() != MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
//            订单状态设为已支付
            mallOrder.setOrderStatus((byte) MallOrderStatusEnum.ORDER_PAID.getOrderStatus());
//            设置支付方式
            mallOrder.setPayType((byte) payType);
//            设置支付状态
            mallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            mallOrder.setPayTime(new Date());
            mallOrder.setUpdateTime(new Date());
//            更新订单
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
        List<MallOrder> mallOrders = listByIds(Arrays.asList(ids));
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(mallOrders)) {
            for (MallOrder order: mallOrders) {
//                已删除订单
                if (order.getIsDeleted() == 1) {
                    builder.append(order.getOrderNo() + " ");
                    continue;
                }
//                若订单不是已支付状态，不能配货
                if (order.getOrderStatus() != 1) {
                    builder.append(order.getOrderNo() + " ");
                }
            }
            String s = builder.toString();
//            订单状态符合条件
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
                    return s + "订单的状态不是支付成功无法执行配货操作";
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
        List<MallOrder> mallOrders = listByIds(Arrays.asList(ids));
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(mallOrders)) {
            for (MallOrder mallOrder: mallOrders) {
//                已删除订单
                if (mallOrder.getIsDeleted() == 1) {
                    builder.append(mallOrder.getOrderNo() + " ");
                    continue;
                }
//                若订单不是已支付或配货完成的状态，则不能进行出库
                if (mallOrder.getOrderStatus() != 1 && mallOrder.getOrderStatus() != 2) {
                    builder.append(mallOrder.getOrderNo() + " ");
                }
            }
            String errorNos = builder.toString();
//            若订单状态均符合操作条件
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
//        查询订单
        List<MallOrder> mallOrders = listByIds(Arrays.asList(ids));
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

    /*
    * 生成订单
    * */
    @Override
    @Transactional
    @GlobalTransactional
    public String saveOrder(Long userId, MallUserAddress address, List<Long> cartItemIds) {
        CommonResult<List<ShoppingCartItemDTO>> listCommonResult = cartServiceFeign.listByCartItemIds(cartItemIds);
        if (listCommonResult == null || listCommonResult.getCode() != 200) {
            MallException.fail("查询购物车商品失败");
        }
        List<ShoppingCartItemDTO> cartItemDTOS = listCommonResult.getData();
        if (CollectionUtils.isEmpty(cartItemDTOS)) {
            MallException.fail("获取购物车商品数据失败");
        }
        List<Long> itemIdList = cartItemDTOS.stream().map(ShoppingCartItemDTO::getCartItemId).collect(Collectors.toList());
        List<Long> goodsId = cartItemDTOS.stream().map(ShoppingCartItemDTO::getGoodsId).collect(Collectors.toList());

        CommonResult<List<ProductDTO>> listByGoodsIds = productServiceFeign.listByGoodsIds(goodsId);
        if (listByGoodsIds == null || listByGoodsIds.getCode() != 200) {
            MallException.fail("获取商品数据失败");
        }
        List<ProductDTO> productDTOList = listByGoodsIds.getData();
        List<ProductDTO> productDTOSNotSelling = productDTOList.stream().filter(productDTO -> productDTO.getGoodsSellStatus() != 0).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(productDTOSNotSelling)) {
            MallException.fail(productDTOSNotSelling.get(0).getGoodsName() + "已下架， 无法生成订单");
        }
        Map<Long, ProductDTO> productDTOMap = productDTOList.stream().collect(Collectors.toMap(ProductDTO::getGoodsId, Function.identity(), (productDTO, productDTO2) -> productDTO));
//        判断商品库存
        for (ShoppingCartItemDTO itemDTO:
             cartItemDTOS) {
//            查出的商品不存在购物车中的这个商品，报错
            if (!productDTOMap.containsKey(itemDTO.getGoodsId())) {
                MallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
//            购买数量大于库存的情况，报错
            if (itemDTO.getGoodsCount() > productDTOMap.get(itemDTO.getGoodsId()).getStockNum()) {
                MallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
//        删除购买物品
        if (!CollectionUtils.isEmpty(itemIdList) && !CollectionUtils.isEmpty(goodsId) && !CollectionUtils.isEmpty(productDTOList)) {
//            调用购物车服务删除数据
            CommonResult<Boolean> deleteByCartItemIdsResult = cartServiceFeign.deleteByCartItemIds(itemIdList);
            if (deleteByCartItemIdsResult != null && deleteByCartItemIdsResult.getCode() == 200) {
                List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(cartItemDTOS, StockNumDTO.class);
                UpdateStockNumDTO updateStockNumDTO = new UpdateStockNumDTO();
                updateStockNumDTO.setStockNumDTOS(stockNumDTOS);
//                调用商品服务修改库存
                CommonResult<Boolean> updateStockResult = productServiceFeign.updateStock(updateStockNumDTO);
                if (updateStockResult == null || updateStockResult.getCode() != 200) {
                    MallException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
                }
                if (!updateStockResult.getData()) {
                    MallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
                }
//                生成订单号
                String orderNo = NumberUtil.genOrderNo();
                int totalPrice = 0;
//                保存订单
                MallOrder mallOrder = new MallOrder();
                mallOrder.setOrderNo(orderNo);
                mallOrder.setUserId(userId);
//                计算总价
                for (ShoppingCartItemDTO item:
                        cartItemDTOS) {
                    totalPrice += item.getGoodsCount() * productDTOMap.get(item.getGoodsId()).getSellingPrice();
                }
                if (totalPrice < 1) {
                    MallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
                }
                mallOrder.setTotalPrice(totalPrice);
                mallOrder.setExtraInfo("");
//                生成订单项并保存
                if (orderMapper.insert(mallOrder) > 0) {
//                    生成收货地址快照，保存至数据库
                    MallOrderAddress orderAddress = new MallOrderAddress();
                    BeanUtil.copyProperties(address, orderAddress);
                    orderAddress.setOrderId(mallOrder.getOrderId());
//                    生成所有的订单项快照，保存至数据库
                    List<MallOrderItem> orderItemList = new ArrayList<>();
                    for (ShoppingCartItemDTO item:
                         cartItemDTOS) {
                        MallOrderItem mallOrderItem = new MallOrderItem();
                        BeanUtil.copyProperties(item, mallOrderItem);
                        mallOrderItem.setGoodsCoverImg(productDTOMap.get(item.getGoodsId()).getGoodsCoverImg());
                        mallOrderItem.setGoodsName(productDTOMap.get(item.getGoodsId()).getGoodsName());
                        mallOrderItem.setSellingPrice(productDTOMap.get(item.getGoodsId()).getSellingPrice());
                        mallOrderItem.setOrderId(mallOrder.getOrderId());
                        orderItemList.add(mallOrderItem);
                    }
//                    保存到数据库
                    if (orderItemService.saveBatch(orderItemList) && orderAddressMapper.insert(orderAddress) > 0) {
                        return orderNo;
                    }
                }
            }
            MallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        MallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
    }
}

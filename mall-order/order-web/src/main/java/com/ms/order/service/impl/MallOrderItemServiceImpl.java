package com.ms.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.order.entity.MallOrderItem;
import com.ms.order.mapper.MallOrderItemMapper;
import com.ms.order.service.MallOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem> implements MallOrderItemService {
}

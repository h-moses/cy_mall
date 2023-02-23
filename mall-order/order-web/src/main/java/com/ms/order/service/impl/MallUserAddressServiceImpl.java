package com.ms.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.order.entity.MallUserAddress;
import com.ms.order.mapper.MallUserAddressMapper;
import com.ms.order.service.MallUserAddressService;
import org.springframework.stereotype.Service;

@Service
public class MallUserAddressServiceImpl extends ServiceImpl<MallUserAddressMapper, MallUserAddress> implements MallUserAddressService {
}

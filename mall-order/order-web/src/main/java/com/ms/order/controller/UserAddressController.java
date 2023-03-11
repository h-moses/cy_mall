package com.ms.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ms.common.annotation.TokenToMallUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.pojo.UserToken;
import com.ms.common.utils.BeanUtil;
import com.ms.order.controller.param.UpdateUserAddressParam;
import com.ms.order.controller.param.UserAddressParam;
import com.ms.order.controller.vo.MallUserAddressVO;
import com.ms.order.entity.MallUserAddress;
import com.ms.order.service.MallUserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api(tags = "个人地址相关接口")
@RequestMapping("/mall")
public class UserAddressController {

    @Resource
    private MallUserAddressService addressService;

    @GetMapping("/address")
    @ApiOperation(value = "我的收货地址列表", notes = "")
    public CommonResult addressList(@TokenToMallUser UserToken userToken) {
        return CommonResult.success(addressService.list(new QueryWrapper<MallUserAddress>().eq("user_id", userToken.getUserId()).eq("is_deleted", 0)));
    }

    @PostMapping("/address")
    @ApiOperation(value = "添加地址", notes = "")
    public CommonResult saveUserAddress(@RequestBody UserAddressParam userAddressParam, @TokenToMallUser UserToken userToken) {
        MallUserAddress mallUserAddress = new MallUserAddress();
        BeanUtil.copyProperties(userAddressParam, mallUserAddress);
        mallUserAddress.setUserId(userToken.getUserId());
        boolean save = addressService.save(mallUserAddress);
        if (save) {
            return CommonResult.success();
        } else {
            return CommonResult.failure("添加失败");
        }
    }

    @PutMapping("/address")
    @ApiOperation(value = "修改地址", notes = "")
    public CommonResult  updateUserAddress(@RequestBody UpdateUserAddressParam updateUserAddressParam, @TokenToMallUser UserToken userToken) {
        MallUserAddress address = addressService.getById(updateUserAddressParam.getAddressId());
        if (!userToken.getUserId().equals(address.getUserId())) {
            return CommonResult.failure(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        MallUserAddress mallUserAddress = new MallUserAddress();
        BeanUtil.copyProperties(updateUserAddressParam, mallUserAddress);
        mallUserAddress.setUserId(userToken.getUserId());
        boolean b = addressService.updateById(mallUserAddress);
        if (b) {
            return CommonResult.success();
        } else {
            return CommonResult.failure("修改失败");
        }
    }

    @GetMapping("/address/{addressId}")
    @ApiOperation(value = "获取收货地址详情", notes = "传参为地址id")
    public CommonResult getUserAddressDetail(@PathVariable("addressId") Long addressId, @TokenToMallUser UserToken userToken) {
        MallUserAddress address = addressService.getById(addressId);
        MallUserAddressVO mallUserAddressVO = new MallUserAddressVO();
        BeanUtil.copyProperties(address, mallUserAddressVO);
        if (!userToken.getUserId().equals(address.getUserId())) {
            return CommonResult.failure(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        } else {
            return CommonResult.success(mallUserAddressVO);
        }
    }

    @GetMapping("/address/default")
    @ApiOperation(value = "获取默认收货地址", notes = "无传参")
    public CommonResult getDefaultAddress(@TokenToMallUser UserToken userToken) {
        QueryWrapper<MallUserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userToken.getUserId())
                .eq("default_flag", 1);
        MallUserAddress one = addressService.getOne(wrapper);
        return CommonResult.success(one);
    }

    @DeleteMapping("/address/{addressId}")
    @ApiOperation(value = "删除收货地址", notes = "传参为地址id")
    public CommonResult deleteAddress(@PathVariable("addressId") Long addressId,
                                      @TokenToMallUser UserToken userToken) {
        MallUserAddress address = addressService.getById(addressId);
        if (null == address) {
            return CommonResult.failure(ServiceResultEnum.NULL_ADDRESS_ERROR.getResult());
        }
        if (!address.getUserId().equals(userToken.getUserId())) {
            return CommonResult.failure(ServiceResultEnum.REQUEST_FORBIDDEN_ERROR.getResult());
        }
        boolean b = addressService.removeById(addressId);
        if (b) {
            return CommonResult.success();
        } else {
            return CommonResult.failure(ServiceResultEnum.OPERATE_ERROR.getResult());
        }
    }
}

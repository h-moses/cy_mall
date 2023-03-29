package com.ms.user.controller;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ms.common.annotation.TokenToMallUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.pojo.UserToken;
import com.ms.common.utils.NumberUtil;
import com.ms.common.utils.SystemUtil;
import com.ms.user.controller.param.UserLoginParam;
import com.ms.user.controller.param.UserRegisterParam;
import com.ms.user.controller.param.UserUpdateParam;
import com.ms.user.entity.User;
import com.ms.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@Api(tags = "用户操作相关接口")
@RequestMapping(value = "/user/mall/")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, UserToken> redisTemplate;

    @PostMapping("login")
    @ApiOperation(value = "登录接口", notes = "返回token")
    public CommonResult login(@RequestBody @Valid UserLoginParam userLoginParam) {
        if (!NumberUtil.isPhone(userLoginParam.getLoginName())) {
            return CommonResult.failure(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        User user = userService.getOne(new QueryWrapper<User>().eq("login_name", userLoginParam.getLoginName()).eq("password_md5", MD5Utils.md5Hex(userLoginParam.getPasswordMd5(), "UTF-8")));
        if (null != user) {
            if (user.getLockedFlag().intValue() == 1) {
                return CommonResult.failure(ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult());
            }
            String token = SystemUtil.genToken(System.currentTimeMillis() + "", user.getUserId());
            UserToken userToken = new UserToken();
            userToken.setUserId(user.getUserId());
            userToken.setToken(token);
            ValueOperations<String, UserToken> ops = redisTemplate.opsForValue();
            ops.set(token, userToken, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
            return CommonResult.success(userToken);
        } else {
            MallException.fail(ServiceResultEnum.USER_NULL_ERROR.getResult());
        }

        return CommonResult.failure("登录失败");
    }

    @PostMapping("logout")
    @ApiOperation(value = "登出接口", notes = "清除token")
    public CommonResult logout(@TokenToMallUser UserToken userToken) {
        Boolean delete = redisTemplate.delete(userToken.getToken());
        if (delete) {
            return CommonResult.success();
        } else {
            return CommonResult.failure("登出失败");
        }
    }

    @PostMapping("register")
    @ApiOperation(value = "用户注册", notes = "")
    public CommonResult register(@RequestBody @Valid UserRegisterParam userRegisterParam) {
        if (!NumberUtil.isPhone(userRegisterParam.getLoginName())) {
            return CommonResult.failure(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        User user = userService.getOne(new QueryWrapper<User>().eq("login_name", userRegisterParam.getLoginName()));
        if (null != user) {
            return CommonResult.failure(ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult());
        }
        User user1 = new User();
        user1.setLoginName(userRegisterParam.getLoginName());
        user1.setNickName(userRegisterParam.getLoginName());
        user1.setPasswordMd5(MD5Utils.md5Hex(userRegisterParam.getPassword(), "UTF-8"));
        if (userService.save(user1)) {
            return CommonResult.success();
        } else {
            return CommonResult.failure("注册失败");
        }
    }

    @PutMapping("update")
    @ApiOperation(value = "修改用户信息", notes = "")
    public CommonResult update(@RequestBody @ApiParam(value = "用户信息")UserUpdateParam userUpdateParam, @TokenToMallUser UserToken userToken) {
        User user = userService.getById(userToken.getUserId());
        if (null == user) {
            return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        user.setLoginName(userUpdateParam.getNickName());
        user.setIntroduceSign(userUpdateParam.getIntroduceSign());
        if (!MD5Utils.md5Hex("", "UTF-8").equals(userUpdateParam.getPasswordMd5())) {
            user.setPasswordMd5(userUpdateParam.getPasswordMd5());
        }
        if (userService.updateById(user)) {
            return CommonResult.success();
        }
        return CommonResult.failure();
    }

    @GetMapping(value = "getDetailByToken")
    @ApiOperation(value = "根据token获取用户信息")
    public CommonResult getUserByToken(@RequestParam("token") String token) {
        ValueOperations<String, UserToken> ops = redisTemplate.opsForValue();
        UserToken userToken = ops.get(token);
        if (null != userToken) {
            User user = userService.getById(userToken.getUserId());
            if (null == user) {
                return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
            }
            if (user.getLockedFlag().intValue() == 1) {
                return CommonResult.failure(ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult());
            }
            return CommonResult.success(user);
        }
        return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
    }
}

package com.ms.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ms.common.api.CommonResult;
import com.ms.common.pojo.AdminToken;
import com.ms.common.utils.SystemUtil;
import com.ms.user.config.annotations.TokenToAdminUser;
import com.ms.user.controller.param.AdminLoginParam;
import com.ms.user.controller.param.UpdateAdminNameParam;
import com.ms.user.controller.param.UpdateAdminPasswordParam;
import com.ms.user.entity.Admin;
import com.ms.user.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@Api(tags = "管理员操作相关接口")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "登录接口", notes = "返回token")
    @PostMapping(value = "/users/admin/login")
    public CommonResult login(@RequestBody @Valid AdminLoginParam adminLoginParam) {
        Admin admin = adminService.getOne(new QueryWrapper<Admin>().eq("login_user_name", adminLoginParam.getUserName()).eq("login_password", adminLoginParam.getPasswordMD5()).eq("locked", 0));
        if (null != admin) {
            String token = SystemUtil.genToken(System.currentTimeMillis() + "", admin.getAdminUserId());
            AdminToken adminToken = new AdminToken();
            adminToken.setAdminId(admin.getAdminUserId());
            adminToken.setToken(token);
            ValueOperations<String, AdminToken> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(token, adminToken, 2 * 24 * 60 * 60, TimeUnit.SECONDS);
            return CommonResult.success(token);
        }
        return CommonResult.failure("登录失败");
    }

    @ApiOperation(value = "获取管理员信息接口")
    @PostMapping(value = "/users/admin/profile")
    public CommonResult profile(@TokenToAdminUser AdminToken adminToken) {
        Admin admin = adminService.getById(adminToken.getAdminId());
        if (null != admin) {
            admin.setLoginPassword("******");
            return CommonResult.success(admin);
        }
        return CommonResult.failure("无此用户");
    }

    @ApiOperation(value = "修改管理员密码接口")
    @PutMapping(value = "/users/admin/password")
    public CommonResult updatePass(@RequestBody @Valid UpdateAdminPasswordParam adminPasswordParam, @TokenToAdminUser AdminToken adminToken) {
        Admin admin = adminService.getById(adminToken.getAdminId());
        if (null != admin) {
            if (adminPasswordParam.getOriginalPassword() == admin.getLoginPassword()) {
                admin.setLoginPassword(adminPasswordParam.getNewPassword());
                boolean b = adminService.updateById(admin);
                if (b) {
                    return CommonResult.success("修改成功");
                }
            }
        }
        return CommonResult.failure();
    }

    @ApiOperation(value = "修改管理员信息接口")
    @RequestMapping(value = "/users/admin/name", method = RequestMethod.PUT)
    public CommonResult updateName(@RequestBody @Valid UpdateAdminNameParam adminNameParam, @TokenToAdminUser AdminToken adminToken) {
        Admin admin = adminService.getById(adminToken.getAdminId());
        if (null != admin) {
            if (adminNameParam.getLoginUserName() == admin.getLoginUserName()) {
                admin.setLoginUserName(adminNameParam.getLoginUserName());
                boolean b = adminService.updateById(admin);
                if (b) {
                    return CommonResult.success("修改成功");
                }
            }
        }
        return CommonResult.failure();
    }

    @ApiOperation(value = "管理员退出登录的接口")
    @DeleteMapping(value = "/users/admin/logout")
    public CommonResult logout(@TokenToAdminUser AdminToken adminToken) {
        Boolean delete = redisTemplate.delete(adminToken.getToken());
        if (delete) {
            return CommonResult.success();
        } else {
            return CommonResult.failure();
        }
    }

    @ApiOperation(value = "根据token获取管理员信息的接口", notes = "OpenFeign调用")
    @GetMapping(value = "/users/admin/{token}")
    public CommonResult getAdminInfoByToken(@PathVariable("token") String token) {
        ValueOperations<String, AdminToken> ops = redisTemplate.opsForValue();
        AdminToken adminToken = ops.get(token);
        if (null != adminToken) {
            Admin admin = adminService.getById(adminToken.getAdminId());
            admin.setLoginPassword("******");
            return CommonResult.success(admin);
        }
        return CommonResult.failure("无此用户");
    }
}

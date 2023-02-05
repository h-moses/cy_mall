package com.ms.user.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class UserLoginParam implements Serializable {
    @ApiModelProperty("登录名")
    @NotEmpty(message = "登录名不能为空")
    private String loginName;

    @ApiModelProperty("用户密码(需要MD5加密)")
    @NotEmpty(message = "密码不能为空")
    private String passwordMd5;
}

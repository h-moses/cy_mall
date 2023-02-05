package com.ms.user.controller.param;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class AdminLoginParam implements Serializable {
    @NotEmpty(message = "用户名不能为空")
    @ApiModelProperty("登录名")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @ApiModelProperty("密码（MD5加密）")
    private String passwordMD5;
}

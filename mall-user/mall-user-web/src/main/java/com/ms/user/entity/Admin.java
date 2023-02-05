package com.ms.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tb_newbee_mall_admin_user")
public class Admin {

    private Long adminUserId;

    private String loginUserName;

    private String loginPassword;

    private String nickName;

    private Byte locked;
}

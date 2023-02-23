package com.ms.order.entity;

import lombok.Data;

@Data
public class LoginAdmin {
    private Long adminUserId;

    private String loginUserName;

    private String loginPassword;

    private String nickName;

    private Byte locked;
}

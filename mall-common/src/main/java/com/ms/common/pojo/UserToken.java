package com.ms.common.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserToken implements Serializable {

    private Long userId;

    private String token;
}

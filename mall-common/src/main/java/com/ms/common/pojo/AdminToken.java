package com.ms.common.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminToken implements Serializable {

    private Long adminId;

    private String token;
}

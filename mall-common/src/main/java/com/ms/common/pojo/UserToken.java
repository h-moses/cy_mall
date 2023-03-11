package com.ms.common.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
public class UserToken implements Serializable {

    private Long userId;

    private String token;
}

package com.ms.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {

    private Integer code;

    private String message;

    private T data;

    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> CommonResult<T> failure() {
        return new CommonResult<T>(ResultCode.FAILURE.getCode(), ResultCode.FAILURE.getMessage(), null);
    }

    public static <T> CommonResult<T> failure(String message) {
        return new CommonResult<T>(ResultCode.FAILURE.getCode(), message, null);
    }
}

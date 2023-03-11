package com.ms.common.exception;

import com.ms.common.api.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"com.ms.user", "com.ms.cart"})
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MallException.class)
    public CommonResult mallException(MallException e) {
        return CommonResult.failure(e.getMessage());
    }
}

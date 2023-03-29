package com.ms.common.exception;

import com.ms.common.api.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"com.ms.user", "com.ms.cart", "com.ms.order", "com.ms.product"})
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MallException.class)
    public CommonResult handleMallException(MallException e) {
        return CommonResult.failure(e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public CommonResult handleException(Exception e) {
        return CommonResult.failure(e.getMessage());
    }
}

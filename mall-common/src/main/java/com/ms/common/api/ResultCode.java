package com.ms.common.api;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILURE(400, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(404, "禁止操作");


    private Integer code;

    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

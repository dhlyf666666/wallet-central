package com.dhlyf.walletmodel.common;


import lombok.Data;

@Data
public class CommonResult<T> {
    private Integer code;
    private String message;
    private T data;

    // 省略构造函数和getter、setter方法

    public CommonResult() {}

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 返回成功结果，不带数据
    public static <T> CommonResult<T> success() {
        return new CommonResult<>(200, "success", null);
    }

    // 返回成功结果，带数据
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "success", data);
    }

    // 返回失败结果
    public static <T> CommonResult<T> failed(Integer code, String message) {
        return new CommonResult<>(code, message, null);
    }
}


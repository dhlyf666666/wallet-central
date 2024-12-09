package com.dhlyf.walletmodel.common;

import lombok.Data;

@Data
public class WalletException extends RuntimeException {
    private int code;
    private String msg;
    private Object data;

    // 私有构造函数，只能通过 Builder 创建实例
    private WalletException(int code, String msg, Object data) {
        super(msg); // 调用父类构造函数，设置异常消息
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // Builder 类
    public static class Builder {
        private int code;
        private String msg;
        private Object data;

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public WalletException build() {
            return new WalletException(code, msg, data);
        }
    }

    // 默认成功方法，返回 Builder 类的实例
    public static Builder success(Object data) {
        return new Builder().code(0).msg("Success").data(data);
    }

    // Getters for code, msg, and data
}


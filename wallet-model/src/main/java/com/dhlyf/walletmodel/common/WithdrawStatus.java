package com.dhlyf.walletmodel.common;

public enum WithdrawStatus {
    //已保存
    SAVED(0),
    //中间状态 上链中
    PENDING(1),
    //已上链
    HASHED(2),
    //已推送
    PUSHED(3),
    //待撤销
    WAIT_CANCEL(4),
    //撤销
    CANCEL(5),
    //余额不足
    BALANCE_NOT_ENOUGH(6),
    ;
    private Integer value;

    WithdrawStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}

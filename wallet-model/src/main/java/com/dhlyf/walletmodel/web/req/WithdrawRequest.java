package com.dhlyf.walletmodel.web.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest extends IRequestParams{

    //提现地址
    private String address;
    //提现金额
    private BigDecimal amount;
    //合约
    private String contract;
}

package com.dhlyf.walletbtc.util.btc;

import lombok.Data;

@Data
public class UnSpentUtxo {

    private static final long serialVersionUID = -7417428486644921613L;

    //交易hash
    private String hash;

    //交易输出索引
    private long txN;

    //金额
    private long value;

    //区块高度
    private int height;

    //hex
    private String script;

    //钱包地址
    private String address;

}

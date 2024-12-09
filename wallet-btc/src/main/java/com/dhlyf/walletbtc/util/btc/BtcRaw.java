package com.dhlyf.walletbtc.util.btc;

import lombok.Data;

import java.util.List;

@Data
public class BtcRaw {

    private String privateKey;

    //收款地址
    private String recevieAddr;
    //发送地址
    private String fromAddr;
    //金额
    private Long amount;
    //fee 手续费(自定义 或者 默认)
    private Long fee;
    //未交易的utxo
    List<UnSpentUtxo> unUtxos;
}

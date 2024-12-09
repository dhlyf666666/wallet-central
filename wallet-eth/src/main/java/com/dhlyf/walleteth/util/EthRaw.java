package com.dhlyf.walleteth.util;

import lombok.Data;

import java.math.BigInteger;

@Data
public class EthRaw {

    private String privateKey;
    private String toAddress;
    private String contractAddress;
    private BigInteger value;
    private String data;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private BigInteger nonce;
    private Integer type;

}

package com.dhlyf.walletservice.service;

import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;

import java.math.BigDecimal;

public interface ChainService {
    default Long getNowBlockNumber(){
        return 0L;
    };

    default String transfer(String address, BigDecimal amount){
        return "";
    };

    default String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount){
        return "";
    };

    default String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return "";
    };

    default String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return "";
    };

    default BigDecimal getGas(){
        return BigDecimal.ZERO;
    };

    default BigDecimal getBalance(String address){
        return BigDecimal.ZERO;
    };

    default String getHotWalletAddress(){
        return "";
    };

    default BigDecimal processCollectGas(Coin coin, Account account){
        return getGas();
    };

    String getChain();

    default BigDecimal getTokenBalance(String address, String address1){
        return BigDecimal.ZERO;
    };
}

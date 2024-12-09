package com.dhlyf.walletfil.service;

import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletfil.model.fil.BalanceResult;
import com.dhlyf.walletfil.util.FilUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.FeeTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
public class FILChainServiceImpl implements ChainService {


    @Autowired
    private FeeTransferService feeTransferService;

    @Override
    public Long getNowBlockNumber() {
        return FilUtil.getNowBlockNumber();
    }

//    @Override
//    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount) {
//        return null;
//    }

    @Override
    public String transfer(String address, BigDecimal amount) {
        return FilUtil.transferFilRaw(address,amount);
    }

    @Override
    public String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return FilUtil.transferFilRaw(address,amount,fromAddress,fromPrivateKey);
    };

//    @Override
//    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
//        return FilUtil.transferTrc20Raw(contract,address,amount,coinDecimals,fromAddress,fromPrivateKey);
//    };

    @Override
    public BigDecimal getGas(){
        return new BigDecimal("0.5");
    };

    @Override
    public BigDecimal getBalance(String address){
        BalanceResult result = FilUtil.getBalance(address);
        if(result == null){
            return null;
        }else{
            return result.getBalance();
        }
    };

    @Override
    public String getHotWalletAddress(){
        return FilUtil.getHotWalletAddress();
    };

    @Override
    public BigDecimal processCollectGas(Coin coin, Account account){
        if(CoinType.main.name().equals(coin.getType())){

            return new BigDecimal("0.5");


        }else if(CoinType.token.name().equals(coin.getType())){

            return new BigDecimal("0.5");
        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }
        return null;
    };

    public String getChain(){
        return Chain.FIL.name();
    }

}

package com.dhlyf.walletbtc.service;

import com.dhlyf.walletbtc.util.btc.BitcoinRPCClient;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.FeeTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
public class BTCChainServiceImpl implements ChainService {


    @Autowired
    private FeeTransferService feeTransferService;
    @Autowired
    BitcoinRPCClient rpcClient;
    @Value("${hot.wallet.address}")
    private String hotWalletAddress;
    @Value("${hot.wallet.privatekey}")
    private String hotWalletAddressPrivatekey;
    @Override
    public Long getNowBlockNumber() {
        return Long.valueOf(rpcClient.getBlockCount());
    }

//    @Override
//    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount) {
//        return null;
//    }

    @Override
    public String transfer(String address, BigDecimal amount) {
        return rpcClient.transferRaw(address,amount,hotWalletAddress,hotWalletAddressPrivatekey);
    }

    @Override
    public String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return rpcClient.transferRaw(address,amount,fromAddress,fromPrivateKey);
    };

//    @Override
//    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
//        return rpcClient.transferTrc20Raw(contract,address,amount,coinDecimals,fromAddress,fromPrivateKey);
//    };

    @Override
    public BigDecimal getGas(){
        return new BigDecimal("0.5");
    };

    @Override
    public BigDecimal getBalance(String address){
        try{
            BigDecimal balance = new BigDecimal(String.valueOf(rpcClient.getBalance(address)));
            return balance;
        }catch (Exception e){
            log.error("{} getBalance error: {}", address, e.getMessage());
            e.printStackTrace();
        }
        return null;
    };

    @Override
    public String getHotWalletAddress(){
        return hotWalletAddress;
    };

    @Override
    public BigDecimal processCollectGas(Coin coin, Account account){
        if(CoinType.main.name().equals(coin.getType())){

            int estimatedSize = rpcClient.estimateTransactionSize(1, 1);
            BigDecimal fee = rpcClient.convertToBitcoins(rpcClient.getTransferFee(estimatedSize));
            return fee;

        }else if(CoinType.token.name().equals(coin.getType())){

            return null;
        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }
        return null;
    };

    public String getChain(){
        return Chain.BTC.name();
    }
}

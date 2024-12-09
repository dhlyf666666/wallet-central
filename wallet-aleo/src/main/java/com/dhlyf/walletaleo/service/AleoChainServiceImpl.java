package com.dhlyf.walletaleo.service;

import com.dhlyf.walletaleo.util.AleoUtil;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
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
public class AleoChainServiceImpl implements ChainService {


    @Autowired
    private FeeTransferService feeTransferService;
    @Autowired
    private ChainService chainService;

    @Override
    public Long getNowBlockNumber() {
        return AleoUtil.getNowBlockNumber();
    }

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount) {
//        return AleoUtil.transferTrc20Raw(contract,address,amount,coinDecimals);
        return "";
    }

    @Override
    public String transfer(String address, BigDecimal amount) {
        return AleoUtil.transferRaw(address,amount);
    }

    @Override
    public String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
//        return AleoUtil.transferTrxRaw(address,amount,fromAddress,fromPrivateKey);
        return "";
    };

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
//        return AleoUtil.transferTrc20Raw(contract,address,amount,coinDecimals,fromAddress,fromPrivateKey);
        return "";
    };

    @Override
    public BigDecimal getGas(){
        return new BigDecimal("0.0");
    };

    @Override
    public BigDecimal getBalance(String address){
        return AleoUtil.getBalance(address);
    };


    @Override
    public String getHotWalletAddress(){
        return AleoUtil.getHotWalletAddress();
    };

    @Override
    public BigDecimal processCollectGas(Coin coin, Account account){
        if(CoinType.main.name().equals(coin.getType())){

            return new BigDecimal("0.0");


        }else if(CoinType.token.name().equals(coin.getType())){

        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }
        return null;
    };


    //热钱包打手续费
    public String feeTransfer(Account account, BigDecimal fee){
//        log.info("账户ID {} 地址 {} 开始打手续费", account.getId(), account.getAddress());
//        String hash = AleoUtil.transferTrxRaw(account.getAddress(), fee);
//        log.info("账户ID {} 地址 {} 手续费转账成功，hash:{}", account.getId(), account.getAddress(), hash);
//        FeeTransfer feeTransfer = new FeeTransfer();
//        feeTransfer.setAccountId(account.getId());
//        feeTransfer.setFromAddress(AleoUtil.getHotWalletAddress());
//        feeTransfer.setToAddress(account.getAddress());
//        feeTransfer.setAmount(fee);
//        feeTransfer.setTxid(hash);
//        feeTransfer.setStatus(1);
//        feeTransfer.setChainType(chainService.getChain());
//        feeTransferService.save(feeTransfer);
//        return hash;

        return "";
    }

    public String getChain(){
        return Chain.ALEO.name();
    }
}

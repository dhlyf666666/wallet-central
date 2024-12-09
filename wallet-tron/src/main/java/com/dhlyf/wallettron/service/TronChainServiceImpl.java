package com.dhlyf.wallettron.service;

import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletdao.mybatis.orm.FeeTransfer;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.FeeTransferService;
import com.dhlyf.wallettron.util.ItrxUtil;
import com.dhlyf.wallettron.util.TronUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;


@Service
@Slf4j
public class TronChainServiceImpl implements ChainService {


    @Autowired
    private FeeTransferService feeTransferService;
    @Autowired
    private ChainService chainService;

    @Override
    public Long getNowBlockNumber() {
        return TronUtil.getNowBlockNumber();
    }

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount) {
        return TronUtil.transferTrc20Raw(contract,address,amount,coinDecimals);
    }

    @Override
    public String transfer(String address, BigDecimal amount) {
        return TronUtil.transferTrxRaw(address,amount);
    }

    @Override
    public String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return TronUtil.transferTrxRaw(address,amount,fromAddress,fromPrivateKey);
    };

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return TronUtil.transferTrc20Raw(contract,address,amount,coinDecimals,fromAddress,fromPrivateKey);
    };

    @Override
    public BigDecimal getGas(){
        return new BigDecimal("0.5");
    };

    @Override
    public BigDecimal getBalance(String address){
        return TronUtil.getTrxBalance(address);
    };

    @Override
    public String getHotWalletAddress(){
        return TronUtil.getHotWalletAddress();
    };

    @Override
    public BigDecimal processCollectGas(Coin coin, Account account){
        if(CoinType.main.name().equals(coin.getType())){

            return new BigDecimal("0.5");


        }else if(CoinType.token.name().equals(coin.getType())){


            //trx转账需要带宽400
            boolean netEnough = netEnough(account.getAddress(), new BigDecimal("400"));
            if(!netEnough){
                log.warn("归集账户 ID:{} 带宽不足，地址:{} ",account.getId(), account.getAddress());

                //带宽不足查trx
                boolean trxEnough = trxEnough(account.getAddress(), new BigDecimal("0.5"));
                if(!trxEnough){
                    feeTransfer(account, new BigDecimal("0.5"));
                }else{
                    log.info("归集账户 ID {} 地址 {} 带宽不足，但是trx余额充足 {}", account.getId(), account.getAddress());
                    netEnough = true;
                }
            }



            boolean usdtEnough = usdtEnough(account.getAddress());
            BigDecimal targetEnergyLimit = usdtEnough ? new BigDecimal("32000") : new BigDecimal("64000L");
            boolean energyEnough = energyEnough(account.getAddress(), targetEnergyLimit);

            if(!energyEnough){
                log.info("归集账户 ID {} 地址 {} 开始租用能量 {} ",
                        account.getId(), account.getAddress(), targetEnergyLimit.longValue());
                ItrxUtil.rentEnergy1H(account.getAddress(), targetEnergyLimit.longValue());
            }


            if(!netEnough || !energyEnough){
                log.warn("归集账户 ID:{} 能量或带宽不足 本次归集跳过，地址:{} ",account.getId(), account.getAddress());
                return null;
            }

            return new BigDecimal("0.5");
        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }
        return null;
    };

    public boolean energyEnough(String address, BigDecimal targetEnergyLimit){
        Response.AccountResourceMessage accountResourceMessage = TronUtil.getAccountResource(address);
        BigDecimal energyLimit = new BigDecimal(accountResourceMessage.getEnergyLimit());
        BigDecimal EnergyUsed = new BigDecimal(accountResourceMessage.getEnergyUsed());
        BigDecimal availableEnergyLimit = energyLimit.subtract(EnergyUsed);
        if(availableEnergyLimit.compareTo(targetEnergyLimit) < 0){
            log.info("账户 address {} 能量不足， 已有能量 {} 目标能量 {}",address, energyLimit, targetEnergyLimit);
            return false;
        }else{
            log.info("账户 address {} 能量充足， 已有能量 {} 目标能量 {}",address, energyLimit, targetEnergyLimit);
            return true;
        }
    }

    //根据账户查询带宽，判断带宽是否充足
    public boolean netEnough(String address, BigDecimal targetFreeNetLimit){
        Response.AccountResourceMessage accountResourceMessage = TronUtil.getAccountResource(address);
        BigDecimal freeNetLimit = new BigDecimal(accountResourceMessage.getFreeNetLimit());
        BigDecimal freeNetUsed = new BigDecimal(accountResourceMessage.getFreeNetUsed());
        BigDecimal availableNetLimit = freeNetLimit.subtract(freeNetUsed);
        if(availableNetLimit.compareTo(targetFreeNetLimit) < 0){
            log.info("账户 address {} 带宽不足， 已有带宽 {} 目标带宽 {}",address, availableNetLimit, targetFreeNetLimit);
            return false;
        }else{
            log.info("账户 address {} 带宽充足， 已有带宽 {} 目标带宽 {}",address, availableNetLimit, targetFreeNetLimit);
            return true;
        }
    }

    //根据账户查询是否有Usdt
    public boolean usdtEnough(String address){
        BigDecimal usdtAmount = TronUtil.getTrc20Balance(address, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
        if(usdtAmount.compareTo(new BigDecimal("0")) <= 0){
            log.info("账户 address {} USDT不足， 已有USDT {}",address, usdtAmount);
            return false;
        }else{
            log.info("账户 address {} USDT充足， 已有USDT {}",address, usdtAmount);
            return true;
        }
    }

    //根据账户查询是否有trx
    public boolean trxEnough(String address, BigDecimal targetTrxAmount){
        BigDecimal trxAmount = TronUtil.getTrxBalance(address);
        if(trxAmount.compareTo(targetTrxAmount) <= 0){
            log.info("账户 address {} TRX不足， 已有TRX {}",address, trxAmount);
            return false;
        }else{
            log.info("账户 address {} TRX充足， 已有TRX {}",address, trxAmount);
            return true;
        }
    }

    //热钱包打手续费
    public String feeTransfer(Account account, BigDecimal fee){
        log.info("账户ID {} 地址 {} 开始打手续费", account.getId(), account.getAddress());
        String hash = TronUtil.transferTrxRaw(account.getAddress(), fee);
        log.info("账户ID {} 地址 {} 手续费转账成功，hash:{}", account.getId(), account.getAddress(), hash);
        FeeTransfer feeTransfer = new FeeTransfer();
        feeTransfer.setAccountId(account.getId());
        feeTransfer.setFromAddress(TronUtil.getHotWalletAddress());
        feeTransfer.setToAddress(account.getAddress());
        feeTransfer.setAmount(fee);
        feeTransfer.setTxid(hash);
        feeTransfer.setStatus(1);
        feeTransfer.setChainType(chainService.getChain());
        feeTransferService.save(feeTransfer);
        return hash;
    }

    public String getChain(){
        return Chain.TRX.name();
    }
}

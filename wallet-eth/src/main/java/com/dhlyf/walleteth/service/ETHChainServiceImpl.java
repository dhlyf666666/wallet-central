package com.dhlyf.walleteth.service;

import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletdao.mybatis.orm.FeeTransfer;
import com.dhlyf.walleteth.util.EthUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.FeeTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;


@Service
@Slf4j
public class ETHChainServiceImpl implements ChainService {


    @Autowired
    private FeeTransferService feeTransferService;
    @Autowired
    private ChainService chainService;

    @Override
    public Long getNowBlockNumber() {
        return EthUtil.getNowBlockNumber();
    }

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount) {
        return EthUtil.transferErc20Raw(contract,coinDecimals,address,amount);
    }

    @Override
    public String transfer(String address, BigDecimal amount) {
        return EthUtil.transferEthRaw(address,amount);
    }

    @Override
    public String transfer(String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return EthUtil.transferEthRaw(address,amount,fromAddress,fromPrivateKey);
    };

    @Override
    public String transferToken(String contract, Integer coinDecimals, String address, BigDecimal amount, String fromAddress, String fromPrivateKey){
        return EthUtil.transferErc20Raw(contract,coinDecimals,address,amount,fromAddress,fromPrivateKey);
    };

    @Override
    public BigDecimal getGas(){
        return new BigDecimal("0.5");
    };

    @Override
    public BigDecimal getBalance(String address){
        return EthUtil.getBalance(address);
    };

    @Override
    public String getHotWalletAddress(){
        return EthUtil.getHotWalletAddress();
    };

    private static final BigInteger ETH_GAS_LIMIT = BigInteger.valueOf(21000);
    private static final BigInteger TOKEN_GAS_LIMIT = BigInteger.valueOf(100000);

    @Override
    public BigDecimal processCollectGas(Coin coin, Account account){

        JSONObject jsonObject = EthUtil.getGastracker();
        BigDecimal FastGasPrice = jsonObject.getBigDecimal("FastGasPrice");
        // 假设Gas Price为100 Gwei
        BigInteger gasPrice = Convert.toWei(FastGasPrice.stripTrailingZeros().toPlainString(), Convert.Unit.GWEI).toBigInteger();
        BigInteger feeInGwei = gasPrice.multiply(ETH_GAS_LIMIT);
        // 将手续费从Wei转换成ETH
        BigDecimal fee = new BigDecimal(feeInGwei).divide(new BigDecimal("1e18"));

        BigDecimal balance = getBalance(account.getAddress());
        if(balance.compareTo(fee) < 0){
            feeTransfer(account,fee);
            return null;
        }
        if(CoinType.main.name().equals(coin.getType())){

            return fee;

        }else if(CoinType.token.name().equals(coin.getType())){

            return fee;
        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }
        return null;
    };

    public String feeTransfer(Account account, BigDecimal fee){
        log.info("账户ID {} 地址 {} 开始打手续费", account.getId(), account.getAddress());
        String hash = EthUtil.transferEthRaw(account.getAddress(), fee);
        log.info("账户ID {} 地址 {} 手续费转账成功，hash:{}", account.getId(), account.getAddress(), hash);
        FeeTransfer feeTransfer = new FeeTransfer();
        feeTransfer.setAccountId(account.getId());
        feeTransfer.setFromAddress(EthUtil.getHotWalletAddress());
        feeTransfer.setToAddress(account.getAddress());
        feeTransfer.setAmount(fee);
        feeTransfer.setTxid(hash);
        feeTransfer.setStatus(1);
        feeTransfer.setChainType(chainService.getChain());
        feeTransferService.save(feeTransfer);
        return hash;
    }

    public String getChain(){
        return Chain.ETH.name();
    }
}

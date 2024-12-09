package com.dhlyf.walletservice.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.AccountTransactionMapper;
import com.dhlyf.walletdao.mybatis.mapper.CollectMapper;
import com.dhlyf.walletdao.mybatis.orm.*;
import com.dhlyf.walletmodel.common.AccountTransactionType;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Autowired
    private AddressServiceImpl addressService;
    @Autowired
    private FeeTransferService feeTransferService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountTransactionMapper accountTransactionMapper;
    @Autowired
    private ChainService chainService;

    //冷钱包地址
    @Value("${wallet.collectAddress}")
    private String collectAddress;

    @Override
    public void collectAccount(){
        LambdaQueryWrapper<Coin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coin::getChainType, chainService.getChain());
        List<Coin> coinList = contractService.list(queryWrapper);
        if(coinList == null || coinList.size() == 0){
            log.error("合约列表为空");
            return;
        }

        for(Coin coin : coinList){
            //当前币种最小归集数量
            BigDecimal minCollectAmount = coin.getMinCollectAmount();
            //根据币种和最小归集数量，查询accoount表中的数据
            List<Account> accountList = accountService.listByCoinAndAmount(coin.getId(), minCollectAmount);
            if(accountList == null || accountList.size() == 0){
                log.info("币种:{} {} 无可归集账户",coin.getCoinName(), coin.getId());
                continue;
            }

            for(Account account : accountList) {
                try {
                    collectAccountOne(coin, account);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("归集账户 ID:{} error:{}", account.getId(), e.getMessage());
                } finally {

                }
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void collectAccountOne(Coin coin, Account account){

        BigDecimal fee = chainService.processCollectGas(coin, account);
        if(fee == null){
            log.error("gasfee 处理结果为Null 跳过");
            return;
        }

        //如果是主链币
        if(CoinType.main.name().equals(coin.getType())){

            BigDecimal balance = account.getBalance();
            BigDecimal collectAmount = balance.subtract(fee);

            AccountTransaction accountTransaction = new AccountTransaction();
            accountTransaction.setAccountId(account.getId());
            accountTransaction.setCoinId(coin.getId());
            accountTransaction.setCoinName(coin.getCoinName());
            accountTransaction.setType(AccountTransactionType.collect.name());
            accountTransaction.setFromAddress(account.getAddress());
            accountTransaction.setToAddress(collectAddress);
            accountTransaction.setAmount(collectAmount);
            accountTransaction.setRealAmount(collectAmount);
            accountTransaction.setFee(fee);
            accountTransaction.setRefId(0L);
            accountTransaction.setChainType(chainService.getChain());
            accountService.transfer(accountTransaction);


            Address dbAddress = addressService.getByAddress(account.getAddress());

//            String hash = EthUtil.transferEthRaw(collectAddress, collectAmount,
//                    account.getAddress(), dbAddress.getPrivateKey());
            String hash = chainService.transfer(collectAddress, collectAmount, account.getAddress(), dbAddress.getPrivateKey());
            log.info("充值归集trx ID:{} 上链成功，hash:{}",account.getId(), hash);

            Collect collect = new Collect();
            collect.setTransactionId(accountTransaction.getId());
            collect.setCoinId(coin.getId());
            collect.setCoinName(coin.getCoinName());
            collect.setType(coin.getType());
            collect.setContractAddress(coin.getAddress());
            collect.setCollectFromAddress(account.getAddress());
            collect.setCollectToAddress(collectAddress);
            collect.setCollectAmount(collectAmount);
            collect.setCollectHash(hash);
            collect.setChainType(chainService.getChain());
            save(collect);

            accountTransaction.setRefId(collect.getId());
            accountTransactionMapper.updateById(accountTransaction);

        }else if(CoinType.token.name().equals(coin.getType())){
            //获取eth余额
            BigDecimal ethBalance = chainService.getBalance(account.getAddress());
            //小于手续费先打手续费
            if(ethBalance.compareTo(fee) < 0){
                feeTransfer(account,fee);
                return;
            }

            //合约转账
            //余额
            BigDecimal balance = account.getBalance();
            BigDecimal collectAmount = balance.subtract(BigDecimal.ZERO);

            AccountTransaction accountTransaction = new AccountTransaction();
            accountTransaction.setAccountId(account.getId());
            accountTransaction.setCoinId(coin.getId());
            accountTransaction.setCoinName(coin.getCoinName());
            accountTransaction.setType(AccountTransactionType.collect.name());
            accountTransaction.setFromAddress(account.getAddress());
            accountTransaction.setToAddress(collectAddress);
            accountTransaction.setAmount(balance);
            accountTransaction.setRealAmount(collectAmount);
            accountTransaction.setFee(fee);
            accountTransaction.setRefId(0L);
            accountTransaction.setChainType(chainService.getChain());
            accountService.transfer(accountTransaction);

            Address dbAddress = addressService.getByAddress(account.getAddress());

            String hash = chainService.transferToken(coin.getAddress(),coin.getCoinDecimals(),
                    collectAddress,collectAmount, dbAddress.getAddress(), dbAddress.getPrivateKey());
            log.info("充值归集trx ID:{} 上链成功，hash:{}",account.getId(), hash);

            Collect collect = new Collect();
            collect.setTransactionId(accountTransaction.getId());
            collect.setCoinId(coin.getId());
            collect.setCoinName(coin.getCoinName());
            collect.setType(coin.getType());
            collect.setContractAddress(coin.getAddress());
            collect.setCollectFromAddress(account.getAddress());
            collect.setCollectToAddress(collectAddress);
            collect.setCollectAmount(account.getBalance());
            collect.setCollectHash(hash);
            collect.setChainType(chainService.getChain());
            save(collect);

            accountTransaction.setRefId(collect.getId());
            accountTransactionMapper.updateById(accountTransaction);

        }else{
            log.error("币种类型错误，coinId:{} coinType:{}",coin.getId(), coin.getType());
        }

    }


    //热钱包打手续费
    public String feeTransfer(Account account, BigDecimal fee){
        log.info("账户ID {} 地址 {} 开始打手续费", account.getId(), account.getAddress());
        String hash = chainService.transfer(account.getAddress(), fee);
        log.info("账户ID {} 地址 {} 手续费转账成功，hash:{}", account.getId(), account.getAddress(), hash);
        FeeTransfer feeTransfer = new FeeTransfer();
        feeTransfer.setAccountId(account.getId());
        feeTransfer.setFromAddress(chainService.getHotWalletAddress());
        feeTransfer.setToAddress(account.getAddress());
        feeTransfer.setAmount(fee);
        feeTransfer.setTxid(hash);
        feeTransfer.setStatus(1);
        feeTransfer.setChainType(chainService.getChain());
        feeTransferService.save(feeTransfer);
        return hash;
    }

}

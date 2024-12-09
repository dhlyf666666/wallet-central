package com.dhlyf.walletservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.AccountMapper;
import com.dhlyf.walletdao.mybatis.mapper.AccountTransactionMapper;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.AccountTransaction;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.AccountTransactionType;
import com.dhlyf.walletmodel.common.WalletException;
import com.dhlyf.walletmodel.common.WalletResult;
import com.dhlyf.walletservice.service.AccountService;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountTransactionMapper accountTransactionMapper;
    @Autowired
    private ChainService chainService;
    @Autowired
    private ContractService contractService;


    //转账
    @Transactional
    @Override
    public void transfer(AccountTransaction accountTransaction) {
        // 使用 FOR UPDATE 锁定账户
        Account account = accountMapper.selectByIdForUpdate(accountTransaction.getAccountId());

        // 判断转账数量是否合法
        if (accountTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("转账数量不合法:{}",accountTransaction.getAmount().stripTrailingZeros().toPlainString());
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }

        // 如果流水类型是归集，余额要大于归集数量
        if (accountTransaction.getType().equals(AccountTransactionType.collect.name())) {
            if (account.getBalance().compareTo(accountTransaction.getAmount()) < 0) {
                log.error("余额不足 余额 {} 转账数量 {}",account.getBalance().stripTrailingZeros().toPlainString(),accountTransaction.getAmount().stripTrailingZeros().toPlainString());
                throw new WalletException.Builder()
                        .code(WalletResult.UNKNOWN_ERROR.getCode())
                        .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                        .build();
            }
        }

        // 更新余额
        if (accountTransaction.getType().equals(AccountTransactionType.collect.name())) {
            account.setBalance(account.getBalance().subtract(accountTransaction.getAmount()));
        } else if (accountTransaction.getType().equals(AccountTransactionType.deposit.name())) {
            account.setBalance(account.getBalance().add(accountTransaction.getAmount()));
        } else {
            log.error("交易类型不合法");
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }

        // 更新账户余额
        int updateResult = accountMapper.updateById(account);
        if (updateResult != 1) {
            log.error("更新账户余额失败 {} ",updateResult);
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }

        accountTransactionMapper.insert(accountTransaction);
    }


    //查询一个账户 不存在创建账户
    @Override
    public Account getAccount(Long coinId, String address) {
        Account account = this.lambdaQuery().eq(Account::getCoinId, coinId).eq(Account::getAddress, address).one();
        if (account == null) {
            Coin coin = contractService.getById(coinId);
            account = this.createAccount(coinId, address, coin.getCoinName());
        }
        return account;
    }

    //创建一个账户
    @Override
    public Account createAccount(Long coinId, String address, String coinName) {
        Account account = new Account();
        account.setCoinId(coinId);
        account.setCoinName(coinName);
        account.setAddress(address);
        account.setBalance(BigDecimal.ZERO);
        account.setChainType(chainService.getChain());
        this.save(account);
        return account;
    }

    @Override
    public List<Account> listByCoinAndAmount(Long id, BigDecimal minCollectAmount) {
        return this.lambdaQuery().eq(Account::getCoinId, id).ge(Account::getBalance, minCollectAmount).list();
    }


}

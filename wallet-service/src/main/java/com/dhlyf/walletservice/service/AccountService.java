package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.AccountTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService extends IService<Account> {

    void transfer(AccountTransaction accountTransaction);

    Account getAccount(Long coinId, String address);

    Account createAccount(Long coinId, String address, String coinName);

    List<Account> listByCoinAndAmount(Long id, BigDecimal minCollectAmount);
}

package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Account;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletdao.mybatis.orm.Collect;

public interface CollectService extends IService<Collect> {
    void collectAccount();

    void collectAccountOne(Coin coin, Account account);

}

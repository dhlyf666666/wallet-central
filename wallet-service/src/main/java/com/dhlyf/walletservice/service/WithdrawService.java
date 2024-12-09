package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Withdraw;

import java.util.List;

public interface WithdrawService extends IService<Withdraw> {

    List<Withdraw> findByWithdraw(Withdraw withdraw);

    void updateByRequiresNew(Withdraw withdraw);

    void pullWithdraw();

    void handleWithdraw();

    void pushWithdraw();
    void cancelWithdraw();
}

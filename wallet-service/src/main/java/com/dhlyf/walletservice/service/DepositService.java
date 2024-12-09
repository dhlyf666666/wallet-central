package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Deposit;

import java.util.List;

public interface DepositService extends IService<Deposit> {
    List<Deposit> getDepositList(long lastetNumber);

    void updateByRequiresNew(Deposit deposit);


    List<Deposit> getByEntity(Deposit deposit);
}

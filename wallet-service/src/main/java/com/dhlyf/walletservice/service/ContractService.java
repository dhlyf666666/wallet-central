package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;

public interface ContractService extends IService<Coin> {

    Coin selectOneByEntity(Coin entity);

    boolean isExist(String address);

    Coin getByAddress(String address);

    Coin getByType(CoinType type, Chain chain);

}
